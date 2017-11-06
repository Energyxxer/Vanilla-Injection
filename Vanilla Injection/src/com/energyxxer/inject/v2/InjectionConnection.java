package com.energyxxer.inject.v2;

import static com.energyxxer.inject.structures.StructureBlock.Mode.LOAD;
import static com.energyxxer.inject.v2.CommandBlock.Type.CHAIN;
import static com.energyxxer.inject.v2.CommandBlock.Type.REPEAT;
import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static de.adrodoc55.minecraft.coordinate.Direction3.DOWN;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.Integers;

import com.energyxxer.inject.listeners.ChatEvent;
import com.energyxxer.inject.listeners.LogEvent;
import com.energyxxer.inject.listeners.SuccessEvent;
import com.energyxxer.inject.structures.StructureBlock;
import com.energyxxer.inject.utils.LogFileReader;
import com.google.common.base.Charsets;
import com.google.common.collect.Iterators;
import com.google.common.io.Files;
import com.google.common.primitives.Ints;

import de.adrodoc55.minecraft.coordinate.Coordinate3D;
import de.adrodoc55.minecraft.coordinate.Coordinate3I;
import de.adrodoc55.minecraft.structure.SimpleBlock;
import de.adrodoc55.minecraft.structure.SimpleBlockState;
import de.adrodoc55.minecraft.structure.Structure;

/**
 * @author Adrodoc55
 */
public class InjectionConnection implements AutoCloseable {
  /**
   * A command that is always successful and ideally does not disturb the player.
   */
  private static final String SUCCESSFUL_COMMAND = "gamerule logAdminCommands true";
  /**
   * See {@link #isTimedOut()}.
   */
  private static final int TIME_OUT_DELAY = 2;
  /**
   * See {@link #isTimedOut()}.
   */
  private static final int TIME_OUT_CHECK_FREQUENCY = 5;
  /**
   * See {@link #isTimedOut()}.
   */
  private static final int CONNECTION_TIME_OUT = TIME_OUT_DELAY * TIME_OUT_CHECK_FREQUENCY;

  private final Logger logger = LogManager.getLogger();

  /**
   * The log file of the Minecraft installation. Note that when using multiple instances of
   * Minecraft they will use the same log file and overwrite each others messages. For that reason
   * an {@link InjectionConnection} only works properly when there is only one Minecraft instance.
   */
  private final Path logFile;
  /**
   * The directory of the Minecraft world.
   */
  private final Path worldDir;
  /**
   * The unique identifier for {@code this} connection in the Minecraft world. There can never be
   * more than one {@link InjectionConnection} with the same {@link #identifier} for each Minecraft
   * world.
   */
  private final String identifier;
  /**
   * The {@link Structure} directory of the Minecraft world. This is a subdirectory of
   * {@link #worldDir} called "structures".
   */
  private final Path structureDir;
  /**
   * The directory to use for {@link #flush()} operations. This is a subdirectory of
   * {@link #structureDir} called "inject/<i>{@link #identifier}</i>".
   */
  private final Path injectionDir;
  /**
   * The file used to persist the {@link #structureId}. This is a file in the {@link #injectionDir}
   * called "data.txt".
   */
  private final Path dataFile;
  private @Nullable FileChannel dataFileChannel;

  /**
   * The {@link Thread} to use for the periodic actions ({@link #checkLog() log checking} and
   * {@link #flush() flushing}). The {@link #executor} is {@code null} unless the connection
   * {@link #isOpen()}.
   */
  private @Nullable ScheduledExecutorService executor;
  /**
   * The {@link LogFileReader} to use for the periodic {@link #checkLog() log checking}. The
   * {@link #reader} is {@code null} unless the connection {@link #isOpen()}.
   */
  private @Nullable LogFileReader reader;

  /**
   * The {@link ScheduledFuture} used to cancel periodic {@link #checkLog() log checking}. The
   * {@link #logCheckFuture} is {@code null} unless the connection {@link #isOpen()}.
   */
  private @Nullable ScheduledFuture<?> logCheckFuture;
  private long logCheckPeriod = 20;
  private TimeUnit logCheckTimeUnit = MILLISECONDS;

  /**
   * The {@link ScheduledFuture} used to cancel periodic {@link #flush() flushing}. The
   * {@link #flushFuture} is {@code null} unless the connection {@link #isActive()}.
   */
  private @Nullable ScheduledFuture<?> flushFuture;
  private long flushPeriod = 20;
  private TimeUnit flushTimeUnit = MILLISECONDS;

  private final List<Consumer<? super LogEvent>> logListeners = new CopyOnWriteArrayList<>();
  private final List<Consumer<? super ChatEvent>> chatListeners = new CopyOnWriteArrayList<>();
  private final Collection<SuccessListenerEntry> successListeners = new ConcurrentLinkedQueue<>();

  /**
   * The ID of the next {@link Structure} that will be generated by {@link #flush()}. This ID is
   * also persisted in the {@link #dataFile}.
   */
  private final AtomicInteger structureId = new AtomicInteger();

  /**
   * ID of the last {@link Structure} file known to be loaded by Minecraft. This is used to
   * determine whether {@code this} connection {@link #isTimedOut()}.
   * <p>
   * This is initialized during {@link #open()} when a connection is successfully established.
   * Afterwards this is updated for every {@value #TIME_OUT_CHECK_FREQUENCY} structures that were
   * loaded by Minecraft, because after every {@value #TIME_OUT_CHECK_FREQUENCY} {@link #flush()}
   * operations a timeout check is injected by {@link #injectTimeoutCheckIfNeccessary(int)}.<br>
   */
  private int lastConfirmedStructureId;

  /**
   * This flag indicates whether or not the next flush operation should generate commands that if
   * successful are written to the log file. This is usually set to {@code true} when listening for
   * the output of some commands.
   * <p>
   * <b>Implementation Notes</b><br>
   * This field is not {@code volatile} because it is only read while holding the exclusive write
   * lock of {@link #logAdminCommandsLock}.
   */
  private boolean logAdminCommands;
  /**
   * This {@link ReadWriteLock} is used to prevent concurrent adding of commands that require admin
   * command logging while flushing.<br>
   * Adding such commands is considered to be the read operation, because multiple commands may be
   * added concurrently.<br>
   * Flushing is considered to be the write operation, because while flushing you may not add such a
   * command:
   * <ul>
   * <li>If such a command is first added during a {@link #flush()} then it might be added to the
   * {@link Structure} even though the preparation to enable logging has not.</li>
   * <li>If such a command is added during a {@link #flush()} then the flag
   * {@link #logAdminCommands} might be set to {@code false} by the {@link #flush()} operation even
   * though the command might not have been added to the {@link Structure}.</li>
   * </ul>
   */
  private final ReadWriteLock logAdminCommandsLock = new ReentrantReadWriteLock();

  private final Collection<Command> commandBuffer = new ConcurrentLinkedQueue<>();

  /**
   * Creates and {@link #open() opens} a new {@link InjectionConnection} with the specified
   * parameters.
   *
   * @param logFile the {@link #logFile} of the Minecraft installation
   * @param worldDir the {@link #worldDir} of the Minecraft world
   * @param identifier the unique {@link #identifier} for {@code this} connection in the Minecraft
   *        world
   * @throws IOException if an I/O error occurs while {@link #open() opening} the connection
   * @throws InterruptedException if the current thread is interrupted while waiting for Minecraft's
   *         response
   */
  public InjectionConnection(Path logFile, Path worldDir, String identifier)
      throws IOException, InterruptedException {
    this.logFile = checkNotNull(logFile, "logFile == null!");
    this.worldDir = checkNotNull(worldDir, "worldDir == null!");
    this.identifier = checkNotNull(identifier, "identifier == null!");
    structureDir = worldDir.resolve("structures");
    injectionDir = structureDir.resolve("inject").resolve(identifier);
    dataFile = injectionDir.resolve("data.txt");
    open();
  }

  private String getStructureName(int structureId) {
    String result = structureDir.relativize(injectionDir) + "/" + structureId;
    return result.replace('\\', '/');
  }

  /**
   * {@link #isOpen() Open} {@code this} connection and wait for a response from Minecraft. Once the
   * response is received, {@link #isActive() activate} {@code this} connection.
   *
   * @throws IOException if an I/O error occurs while opening the {@link #logFile}, locking the
   *         {@link #counterFile} or {@link #flush() flushing}
   * @throws InterruptedException if the current thread is interrupted while waiting for Minecraft's
   *         response
   */
  public void open() throws IOException, InterruptedException {
    checkState(!isOpen(), "This connection is already established!");
    logger.info("Establishing {}", this);
    lockDataFile();
    int structureId = loadStructureId();
    this.structureId.set(structureId);
    logger.info("Using structure '{}'", getStructureName(structureId));
    executor = Executors.newSingleThreadScheduledExecutor();
    reader = new LogFileReader(logFile);
    schedulePeriodicLogCheck();
    Semaphore semaphore = new Semaphore(0);
    injectCommand(SUCCESSFUL_COMMAND, e -> {
      lastConfirmedStructureId = structureId;
      semaphore.release();
    });
    flush();
    logger.info("Waiting for Minecraft's response");
    semaphore.acquire();
    schedulePeriodicFlush();
    logger.info("Successfully established {}", this);
  }

  /**
   * Lock {@link #dataFile} to ensure {@code this} is the only connection with {@link #identifier}
   * for {@link #worldDir}.
   *
   * @throws IllegalStateException if a different connection with {@link #identifier} is already
   *         {@link #isOpen() open} for {@link #worldDir}
   * @throws IOException if an I/O error occurs while locking the {@link #dataFile}
   */
  private void lockDataFile() throws IllegalStateException, IOException {
    Files.createParentDirs(dataFile.toFile());
    dataFileChannel = FileChannel.open(dataFile, READ, WRITE, CREATE);
    try {
      if (dataFileChannel.tryLock() == null) {
        failedToAquireDataFileLock(null);
      }
    } catch (OverlappingFileLockException ex) {
      failedToAquireDataFileLock(ex);
    }
  }

  private void failedToAquireDataFileLock(@Nullable OverlappingFileLockException ex)
      throws IllegalStateException, IOException {
    dataFileChannel.close();
    dataFileChannel = null;
    throw new IllegalStateException("A " + this + " is already open", ex);
  }

  /**
   * Load the value for {@link #structureId} from {@link #dataFile} .
   *
   * @return the value for {@link #structureId}
   *
   * @throws IOException if an I/O error occurs while reading {@link #dataFile}
   */
  private int loadStructureId() throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(Ints.checkedCast(dataFileChannel.size()));
    dataFileChannel.read(buffer);
    String fileContent = new String(buffer.array(), Charsets.UTF_8);
    return Integers.parseInt(fileContent);
  }

  @Override
  public void close() throws IOException {
    // TODO Adrodoc55 04.11.2017: Flush on close?
    if (isOpen()) {
      logger.info("Closing {}", this);
      if (isActive()) {
        cancelPeriodicFlush();
      }
      cancelPeriodicLogCheck();
      executor.shutdown();
      executor = null;
      reader = null;
      dataFileChannel.close();
    }
  }

  /**
   * Return whether {@code this} connection periodically {@link #checkLog() checks the log file}.
   * <p>
   * A connection is opened after construction or by calling {@link #open()}.<br>
   * To {@link #isClosed() close} {@code this} connection call {@link #close()}.
   *
   * @return whether {@code this} connection is open
   */
  public boolean isOpen() {
    return executor != null && reader != null && logCheckFuture != null && dataFileChannel != null;
  }

  /**
   * Return whether {@code this} connection is not {@link #isOpen() open}.
   * <p>
   * A connection is closed by calling {@link #close()}.<br>
   * To {@link #isOpen() open} {@code this} connection call {@link #open()}.
   *
   * @return whether {@code this} connection is open
   */
  public boolean isClosed() {
    return !isOpen();
  }

  /**
   * Pause {@code this} connection by canceling periodic {@link #flush() flushing}. Pausing does not
   * stop periodic {@link #checkLog() log checking}.<br>
   * After pausing {@link #isOpen()} will return {@code true} and {@link #isActive()} will return
   * {@code false}.
   *
   * @return {@code true} if {@code this} connection was paused just now, {@code false} if it was
   *         already paused and not {@link #isActive() active}
   * @throws IllegalStateException if {@code this} connection is not {@link #isOpen() open}
   */
  public boolean pause() throws IllegalStateException {
    checkOpen();
    if (!isActive()) {
      return false;
    }
    logger.info("Pausing {}", this);
    cancelPeriodicFlush();
    return true;
  }

  /**
   * Resume {@code this} connection by scheduling periodic {@link #flush() flushing}.<br>
   * After resuming both {@link #isOpen()} and {@link #isActive()} will return {@code true}.
   *
   * @return {@code true} if {@code this} connection was activated just now, {@code false} if it was
   *         already {@link #isActive() active}
   * @throws IllegalStateException if {@code this} connection is not {@link #isOpen() open}
   */
  public boolean resume() throws IllegalStateException {
    checkOpen();
    if (isActive()) {
      return false;
    }
    logger.info("Resuming {}", this);
    schedulePeriodicFlush();
    return true;
  }

  /**
   * @throws IllegalStateException if {@code this} connection is not {@link #isOpen() open}
   */
  private void checkOpen() throws IllegalStateException {
    checkState(isOpen(), "This connection is not established!");
  }

  /**
   * Return whether {@code this} connection {@link #isOpen()} and periodically performs a
   * {@link #flush()}.
   * <p>
   * A connection is activated after {@link #open() opening} or by calling {@link #resume()}.<br>
   * To {@link #isPaused() pause} {@code this} connection call {@link #pause()}.
   *
   * @return whether {@code this} connection is active
   */
  public boolean isActive() {
    return isOpen() && flushFuture != null;
  }

  /**
   * Return whether {@code this} connection {@link #isOpen()} but does not periodically perform a
   * {@link #flush()}.
   * <p>
   * A connection is paused by calling {@link #pause()}.<br>
   * To {@link #isActive() activate} {@code this} connection call {@link #resume()}.
   *
   * @return whether {@code this} connection is paused
   */
  public boolean isPaused() {
    return isOpen() && !isActive();
  }

  private void schedulePeriodicLogCheck() {
    logCheckFuture =
        executor.scheduleAtFixedRate(this::checkLog, 0, logCheckPeriod, logCheckTimeUnit);
  }

  private void schedulePeriodicFlush() {
    flushFuture = executor.scheduleAtFixedRate(() -> {
      try {
        if (isTimedOut()) {
          logger.warn("Connection timed out ({})", this);
          pause();
        } else {
          flush();
        }
      } catch (IOException ex) {
        logger.error("Periodic flush encountered an error, closing " + this, ex);
        try {
          close();
        } catch (IOException ex2) {
          logger.error("Closing encountered an error", ex2);
        }
      }
    }, 0, flushPeriod, flushTimeUnit);
  }

  private void cancelPeriodicLogCheck() {
    logCheckFuture.cancel(false);
    logCheckFuture = null;
  }

  private void cancelPeriodicFlush() {
    flushFuture.cancel(false);
    flushFuture = null;
  }

  /**
   * @return the value of {@link #logCheckPeriod}
   */
  public long getLogCheckPeriod() {
    return logCheckPeriod;
  }

  /**
   * @return the value of {@link #logCheckTimeUnit}
   */
  public TimeUnit getLogCheckTimeUnit() {
    return logCheckTimeUnit;
  }

  /**
   * Set the frequency at which the {@link #logFile} is checked for changes.
   *
   * @param logCheckPeriod the new value for {@link #logCheckPeriod}
   * @param logCheckTimeUnit the new value for {@link #logCheckTimeUnit}
   */
  public void setLogCheckFrequency(long logCheckPeriod, TimeUnit logCheckTimeUnit) {
    this.logCheckPeriod = logCheckPeriod;
    this.logCheckTimeUnit = checkNotNull(logCheckTimeUnit, "logCheckTimeUnit == null!");
    if (isActive()) {
      cancelPeriodicLogCheck();
      schedulePeriodicLogCheck();
    }
  }

  /**
   * @return the value of {@link #flushPeriod}
   */
  public long getFlushPeriod() {
    return flushPeriod;
  }

  /**
   * @return the value of {@link #flushTimeUnit}
   */
  public TimeUnit getFlushTimeUnit() {
    return flushTimeUnit;
  }

  /**
   * Set the frequency at which {@link #flush()} is invoked periodically.
   *
   * @param flushPeriod the new value for {@link #flushPeriod}
   * @param flushTimeUnit the new value for {@link #flushTimeUnit}
   */
  public void setFlushFrequency(long flushPeriod, TimeUnit flushTimeUnit) {
    this.flushPeriod = flushPeriod;
    this.flushTimeUnit = checkNotNull(flushTimeUnit, "flushTimeUnit == null!");
    if (isActive()) {
      cancelPeriodicFlush();
      schedulePeriodicFlush();
    }
  }

  /**
   * Check for new changes to the {@link #logFile} since the last check or alternatively since
   * {@link #open() opening} {@code this} connection and dispatch new events.
   * <p>
   * If {@code this} connection {@link #isActive()} a log check will be performed periodically
   * according to {@link #logCheckPeriod} and {@link #logCheckTimeUnit}.
   */
  private void checkLog() {
    reader.readAddedLines(UTF_8, this::handleLogLine);
  }

  private void handleLogLine(String line) {
    dispatchLogEvent(new LogEvent(line));
    ChatEvent ce = ChatEvent.createFromLogLine(line);
    if (ce != null) {
      dispatchChatEvent(ce);
    }
    SuccessEvent se = SuccessEvent.createFromLogLine(line);
    if (se != null) {
      dispatchSuccessEvent(se);
    }
  }

  private void dispatchLogEvent(LogEvent event) {
    for (Consumer<? super LogEvent> listener : logListeners) {
      listener.accept(event);
    }
  }

  /**
   * Add the specified listener to be notified for every new line in the {@link #logFile}.
   *
   * @param listener
   */
  public void addLogListener(Consumer<? super LogEvent> listener) {
    logListeners.add(listener);
  }

  /**
   * Remove the specified log listener.
   *
   * @param listener
   * @return {@code true} if the specified listener was previously registered
   */
  public boolean removeLogListener(Consumer<? super LogEvent> listener) {
    return logListeners.remove(listener);
  }

  private void dispatchChatEvent(ChatEvent event) {
    for (Consumer<? super ChatEvent> listener : chatListeners) {
      listener.accept(event);
    }
  }

  /**
   * Add the specified listener to be notified whenever a chat event is detected in the
   * {@link #logFile}.
   *
   * @param listener
   */
  public void addChatListener(Consumer<? super ChatEvent> listener) {
    chatListeners.add(listener);
  }

  /**
   * Remove the specified chat listener.
   *
   * @param listener
   * @return {@code true} if the specified listener was previously registered
   */
  public boolean removeChatListener(Consumer<? super ChatEvent> listener) {
    return chatListeners.remove(listener);
  }

  private void dispatchSuccessEvent(SuccessEvent event) {
    for (SuccessListenerEntry entry : successListeners) {
      if (event.getInvoker().equals(entry.getInvoker())) {
        if (!entry.isRepeat()) {
          successListeners.remove(entry);
        }
        entry.getListener().accept(event);
      }
    }
  }

  private void addSuccessListener(String invoker, boolean repeat, Consumer<SuccessEvent> listener) {
    successListeners.add(new SuccessListenerEntry(invoker, repeat, listener));
  }

  /**
   * Flush the contents of {@code this} connection by creating a new {@link Structure} file in the
   * {@link #structureDir}.
   * <p>
   * If {@code this} connection {@link #isActive()} a flush will be performed periodically according
   * to {@link #flushPeriod} and {@link #flushTimeUnit}.
   *
   * @throws IllegalStateException if {@code this} connection is not {@link #isOpen() open}
   * @throws IOException if an I/O error occurs while creating the {@link Structure} file
   */
  public void flush() throws IllegalStateException, IOException {
    checkOpen();
    // WriteLock to prevent concurrent adding of commands that require admin command logging
    // AND synchronize the flush itself to prevent empty structures
    logAdminCommandsLock.writeLock().lock();
    boolean logAdminCommands = this.logAdminCommands; // This copy is used after releasing the lock
    int structureId; // Don't use this.structureId.get(), because that can be modified concurrently
    Structure structure;
    try {
      if (commandBuffer.isEmpty()) {
        logger.trace("Skipping flush due to empty buffer");
        return;
      }
      // StructureId is only incremented if a structure is actually generated
      structureId = this.structureId.getAndIncrement();
      structure = new Structure(922, "Vanilla-Injection");
      injectTimeoutCheckIfNeccessary(structureId);
      if (logAdminCommands) {
        structure.addEntity(newCommandBlockMinecart(structureId, "gamerule logAdminCommands true"));
      }
      Iterators.consumingIterator(commandBuffer.iterator()).forEachRemaining(command -> {
        structure.addEntity(newCommandBlockMinecart(structureId, command));
      });
      this.logAdminCommands = false;
    } finally {
      // commandBuffer no longer contains comands that require admin command loggig, releasing lock
      logAdminCommandsLock.writeLock().unlock();
    }
    if (logAdminCommands) {
      structure.addEntity(newCommandBlockMinecart(structureId, "gamerule logAdminCommands false"));
    }
    structure.addEntity(newCommandBlockMinecart(structureId,
        "kill @e[type=commandblock_minecart,dy=0,tag=" + getStructureName(structureId) + "]"));

    structure.addBlock(
        new StructureBlock(new Coordinate3I(0, 0, 0), LOAD, getStructureName(structureId + 1)));
    structure.addBlock(new SimpleBlock("minecraft:redstone_block", new Coordinate3I(0, 2, 0)));
    structure.addBlock(new SimpleBlock("minecraft:activator_rail", new Coordinate3I(0, 3, 0)));

    structure.addBlock(new CommandBlock("setblock ~ ~-1 ~ stone", new Coordinate3I(0, 1, 1), DOWN,
        CHAIN, false, true));
    structure.addBlock(new CommandBlock("setblock ~ ~-2 ~ redstone_block",
        new Coordinate3I(0, 2, 1), DOWN, REPEAT, false, false));

    structure.setBackground(new SimpleBlockState("minecraft:air"));

    structure.writeTo(structureDir.resolve(getStructureName(structureId) + ".nbt").toFile());
    persistStructureId(structureId);
  }

  private static final Coordinate3D MINECART_POS = new Coordinate3D(0.5, 3.0625, 0.5);

  private CommandBlockMinecart newCommandBlockMinecart(int structureId, String command) {
    return newCommandBlockMinecart(structureId, new Command(command));
  }

  private CommandBlockMinecart newCommandBlockMinecart(int structureId, Command command) {
    CommandBlockMinecart result = new CommandBlockMinecart(command, MINECART_POS);
    result.addTag(getStructureName(structureId));
    return result;
  }

  /**
   * Determines whether {@code this} connection is timed out.
   * <p>
   * When a timeout is detected during periodic {@link #flush() flushing} {@code this} connection is
   * {@link #pause() paused}.
   * <p>
   * A connection is considered to be timed out when the {@link Structure}s of neither of the
   * previous {@value #TIME_OUT_DELAY} {@link #flush()} operations that injected a
   * {@link #injectTimeoutCheckIfNeccessary(int) timeout check} have been loaded by Minecraft. Every
   * {@value #TIME_OUT_CHECK_FREQUENCY} {@link #flush()} operations inject a
   * {@link #injectTimeoutCheckIfNeccessary(int) timeout check}, so a connection will not time out
   * before AT LEAST {@value #CONNECTION_TIME_OUT} {@link Structure}s were not loaded by Minecraft.
   *
   * @return whether {@code this} connection is timed out
   */
  private boolean isTimedOut() {
    return lastConfirmedStructureId != -1
        && structureId.get() > lastConfirmedStructureId + CONNECTION_TIME_OUT;
  }

  /**
   * Injects a timout check every {@value #TIME_OUT_CHECK_FREQUENCY} {@link #flush()} operations. A
   * timeout check is used to update {@link #lastConfirmedStructureId} once the structure is loaded
   * by Minecraft.
   *
   * @param structureId
   */
  private void injectTimeoutCheckIfNeccessary(int structureId) {
    if (structureId != 0 && structureId % TIME_OUT_CHECK_FREQUENCY == 0) {
      injectCommand(SUCCESSFUL_COMMAND, e -> {
        lastConfirmedStructureId = structureId;
        if (isPaused() && !isTimedOut()) {
          logger.warn("Connection is no longer timed out ({})", this);
          resume();
        }
      });
    }
  }

  private void persistStructureId(int structureId) throws IOException {
    dataFileChannel.position(0);
    dataFileChannel.write(Charsets.UTF_8.encode(String.valueOf(structureId + 1)));
    dataFileChannel.truncate(dataFileChannel.position());
  }

  public void injectCommand(String command) {
    injectCommand(new Command(command));
  }

  public void injectCommand(Command command) {
    commandBuffer.add(command);
  }

  public void injectCommand(String command, Consumer<SuccessEvent> listener) {
    String name = UUID.randomUUID().toString();
    injectCommand(new Command(name, command), listener);
  }

  public void injectCommand(Command command, Consumer<SuccessEvent> listener) {
    logAdminCommandsLock.readLock().lock();
    try {
      injectCommand(command);
      addSuccessListener(command.getName(), false, listener);
      logAdminCommands = true;
    } finally {
      logAdminCommandsLock.readLock().unlock();
    }
  }

  @Override
  public String toString() {
    return "connection '" + identifier + "' to Minecraft world: " + worldDir;
  }
}
