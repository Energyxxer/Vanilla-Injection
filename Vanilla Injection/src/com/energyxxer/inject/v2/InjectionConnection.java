package com.energyxxer.inject.v2;

import static com.energyxxer.inject.structures.StructureBlock.Mode.LOAD;
import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
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

import com.energyxxer.inject.listeners.ChatEvent;
import com.energyxxer.inject.listeners.LogEvent;
import com.energyxxer.inject.listeners.SuccessEvent;
import com.energyxxer.inject.structures.StructureBlock;
import com.energyxxer.inject.utils.LogFileReader;
import com.google.common.collect.Iterators;

import de.adrodoc55.minecraft.coordinate.Coordinate3D;
import de.adrodoc55.minecraft.coordinate.Coordinate3I;
import de.adrodoc55.minecraft.structure.SimpleBlock;
import de.adrodoc55.minecraft.structure.SimpleBlockState;
import de.adrodoc55.minecraft.structure.Structure;

/**
 * @author Adrodoc55
 */
public class InjectionConnection implements AutoCloseable {
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
   * The structure directory of the Minecraft world. This is a subdirectory of {@link #worldDir}
   * called "structures".
   */
  private final Path structureDir;
  /**
   * The unique identifier for {@code this} connection in the Minecraft world. There should never be
   * more than one {@link InjectionConnection} with the same {@link #identifier} for each Minecraft
   * world.
   */
  private final String identifier;

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

  private @Nullable ScheduledFuture<?> logCheckFuture;
  private long logCheckPeriod = 20;
  private TimeUnit logCheckTimeUnit = MILLISECONDS;

  private @Nullable ScheduledFuture<?> flushFuture;
  private long flushPeriod = 20;
  private TimeUnit flushTimeUnit = MILLISECONDS;

  private final Collection<Consumer<? super LogEvent>> logListeners = new CopyOnWriteArrayList<>();
  private final Collection<Consumer<? super ChatEvent>> chatListeners =
      new CopyOnWriteArrayList<>();
  private final Collection<SuccessListenerEntry> successListeners = new ConcurrentLinkedQueue<>();

  private final AtomicInteger structureId = new AtomicInteger();
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
   * @param identifier the unique {@link #identifier} for this connection in the Minecraft world
   * @throws IOException if an I/O error occurs while {@link #open() opening} the connection
   * @throws InterruptedException if the current thread is interrupted while waiting for Minecraft's
   *         response
   */
  public InjectionConnection(Path logFile, Path worldDir, String identifier)
      throws IOException, InterruptedException {
    this.logFile = checkNotNull(logFile, "logFile == null!");
    this.worldDir = checkNotNull(worldDir, "worldDir == null!");
    structureDir = worldDir.resolve("structures");
    this.identifier = checkNotNull(identifier, "identifier == null!");
    open();
  }

  public void open() throws IOException, InterruptedException {
    checkState(!isOpen(), "This connection is already established!");
    logger.info("Establishing {}", this);
    logger.info("Using structure '{}'", getStructureName(structureId.get()));
    executor = Executors.newSingleThreadScheduledExecutor();
    reader = new LogFileReader(logFile);
    schedulePeriodicLogCheck();
    Semaphore semaphore = new Semaphore(0);
    // Any successful command will do
    injectCommand("gamerule logAdminCommands true", e -> semaphore.release());
    flush();
    logger.info("Waiting for Minecraft's response");
    semaphore.acquire();
    schedulePeriodicFlush();
    logger.info("Successfully established {}", this);
  }

  @Override
  public void close() {
    // TODO Adrodoc55 04.11.2017: Flush on close?
    if (isOpen()) {
      logger.info("Closing {}", this);
      if (isActive()) {
        cancelTasks();
      }
      executor.shutdown();
      executor = null;
      reader = null;
    }
  }

  public boolean pause() {
    checkOpen();
    if (!isActive()) {
      return false;
    }
    logger.info("Pausing {}", this);
    cancelTasks();
    return true;
  }

  public boolean resume() {
    checkOpen();
    if (isActive()) {
      return false;
    }
    logger.info("Resuming {}", this);
    scheduleTasks();
    return true;
  }

  private void checkOpen() {
    checkState(isOpen(), "This connection is not established!");
  }

  public boolean isOpen() {
    return executor != null && reader != null;
  }

  public boolean isActive() {
    return isOpen() && logCheckFuture != null && flushFuture != null;
  }

  private void scheduleTasks() {
    schedulePeriodicLogCheck();
    schedulePeriodicFlush();
  }

  private void schedulePeriodicLogCheck() {
    logCheckFuture =
        executor.scheduleAtFixedRate(this::checkLog, 0, logCheckPeriod, logCheckTimeUnit);
  }

  private void schedulePeriodicFlush() {
    flushFuture = executor.scheduleAtFixedRate(() -> {
      try {
        flush();
      } catch (IOException ex) {
        logger.error("Periodic flush encountered an error, closing " + this + "", ex);
        close();
      }
    }, 0, flushPeriod, flushTimeUnit);
  }

  private void cancelTasks() {
    cancelPeriodicLogCheck();
    cancelPeriodicFlush();
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

  public void addLogListener(Consumer<? super LogEvent> listener) {
    logListeners.add(listener);
  }

  public boolean removeLogListener(Consumer<? super LogEvent> listener) {
    return logListeners.remove(listener);
  }

  private void dispatchChatEvent(ChatEvent event) {
    for (Consumer<? super ChatEvent> listener : chatListeners) {
      listener.accept(event);
    }
  }

  public void addChatListener(Consumer<? super ChatEvent> listener) {
    chatListeners.add(listener);
  }

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

  public void flush() throws IOException {
    // Prevent concurrent adding of commands that require admin command logging
    // and synchronize the flush itself to prevent empty structures
    logAdminCommandsLock.writeLock().lock();
    if (commandBuffer.isEmpty()) {
      logAdminCommandsLock.writeLock().unlock();
      logger.trace("Skipping flush due to empty buffer");
      return;
    }
    Structure structure = new Structure(922, "Vanilla-Injection");
    boolean logAdminCommands = this.logAdminCommands;
    try {
      if (logAdminCommands) {
        structure.addEntity(newCommandBlockMinecart("gamerule logAdminCommands true"));
      }
      Iterators.consumingIterator(commandBuffer.iterator()).forEachRemaining(command -> {
        structure.addEntity(newCommandBlockMinecart(command));
      });
      this.logAdminCommands = false;
    } finally {
      logAdminCommandsLock.writeLock().unlock();
    }
    if (logAdminCommands) {
      structure.addEntity(newCommandBlockMinecart("gamerule logAdminCommands false"));
    }
    structure.addEntity(newCommandBlockMinecart("kill @e[type=commandblock_minecart,dy=0]"));

    int structureId = this.structureId.getAndIncrement();
    structure
        .addBlock(new StructureBlock(new Coordinate3I(), LOAD, getStructureName(structureId + 1)));
    structure.addBlock(new SimpleBlock("minecraft:redstone_block", new Coordinate3I(0, 0, 2)));
    structure.addBlock(new SimpleBlock("minecraft:activator_rail", new Coordinate3I(0, 1, 2)));
    structure.setBackground(new SimpleBlockState("minecraft:air"));

    structure.writeTo(structureDir.resolve(getStructureName(structureId) + ".nbt").toFile());
  }

  private static final Coordinate3D MINECART_POS = new Coordinate3D(0.5, 1.0625, 2.5);

  private static CommandBlockMinecart newCommandBlockMinecart(String command) {
    return newCommandBlockMinecart(new Command(command));
  }

  private static CommandBlockMinecart newCommandBlockMinecart(Command command) {
    return new CommandBlockMinecart(command, MINECART_POS);
  }

  private String getStructureName(int structureId) {
    return "inject/" + identifier + "/" + structureId;
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
      logAdminCommands = true;
      injectCommand(command);
      addSuccessListener(command.getName(), false, listener);
    } finally {
      logAdminCommandsLock.readLock().unlock();
    }
  }

  @Override
  public String toString() {
    return "connection '" + identifier + "' to Minecraft world: " + worldDir;
  }
}
