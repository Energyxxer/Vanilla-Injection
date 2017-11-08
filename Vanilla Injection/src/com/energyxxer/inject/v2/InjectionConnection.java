package com.energyxxer.inject.v2;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.io.Files.createParentDirs;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.Integers;

import com.energyxxer.inject.listeners.SuccessEvent;
import com.google.common.base.Charsets;
import com.google.common.primitives.Ints;

import de.adrodoc55.minecraft.structure.Structure;

/**
 * A connection to inject {@link Command}s to be executed by Minecraft and in return listen to
 * Minecraft's {@link MinecraftLogObserver#logfile log file} by using a
 * {@link MinecraftLogObserver}.
 * <p>
 * When first {@link #open() opening} a connection to Minecraft you need to place a
 * <a href="https://minecraft.gamepedia.com/Structure_Block">structure block</a> and load the
 * structure "inject/<i>{@link #identifier}</i>/0" where <i>{@link #identifier}</i> is the
 * {@link #identifier} of the connection.<br>
 * <b>Make sure to set "Include entities" to "ON".</b><br>
 * Once the structure is in place and loaded an {@link InjectionConnection} can automatically
 * reconnect without additional player interaction.
 *
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

  private final Logger logger;
  private final MinecraftLogObserver logObserver;
  private final InjectionBuffer injectionBuffer;

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
   * The file used to persist the {@link #structureId}. This is a file in the {@link #structureDir}
   * called "inject/<i>{@link #identifier}</i>/data.txt".
   */
  private final Path dataFile;
  private @Nullable FileChannel dataFileChannel;

  /**
   * The {@link ScheduledExecutorService} to use for periodic {@link #flush() flushing}. The
   * {@link #executor} is {@code null} unless the connection {@link #isOpen()}.
   */
  private @Nullable ScheduledExecutorService executor;
  /**
   * The {@link ScheduledFuture} used to cancel periodic {@link #flush() flushing}. The
   * {@link #flushFuture} is {@code null} unless the connection {@link #isActive()}.
   */
  private @Nullable ScheduledFuture<?> flushFuture;
  private long flushPeriod = 20;
  private TimeUnit flushTimeUnit = MILLISECONDS;

  /**
   * The ID of the next {@link Structure} that will be generated by {@link #flush()}. This ID is
   * also persisted in the {@link #dataFile}.
   */
  private final AtomicInteger structureId = new AtomicInteger();
  /**
   * ID of the last {@link Structure} file known to be loaded by Minecraft or {@code -1} if
   * {@code this} connection {@link #isClosed()}. This is used to determine whether {@code this}
   * connection {@link #isTimedOut()}.
   * <p>
   * This is initialized during {@link #open()} when a connection is successfully established.
   * Afterwards this is updated for every {@value #TIME_OUT_CHECK_FREQUENCY} structures that were
   * loaded by Minecraft, because after every {@value #TIME_OUT_CHECK_FREQUENCY} {@link #flush()}
   * operations a timeout check is injected by {@link #injectTimeoutCheckIfNeccessary(int)}.
   */
  private int lastConfirmedStructureId = -1;

  /**
   * Create and {@link #open()} a new {@link InjectionConnection} with the specified parameters.
   *
   * @param logFile the {@link MinecraftLogObserver#logFile log file} of the Minecraft installation
   * @param worldDir the {@link #worldDir} of the Minecraft world
   * @param identifier the unique {@link #identifier} for {@code this} connection in the Minecraft
   *        world
   * @throws IOException if an I/O error occurs while {@link #open() opening} the connection
   * @throws InterruptedException if the current thread is interrupted while waiting for Minecraft's
   *         response
   */
  public InjectionConnection(Path logFile, Path worldDir, String identifier)
      throws IOException, InterruptedException {
    this.worldDir = checkNotNull(worldDir, "worldDir == null!");
    checkArgument(isDirectory(worldDir), "%s is not a directory!", worldDir);
    this.identifier = checkNotNull(identifier, "identifier == null!");
    logger = LogManager.getLogger(toString());
    logObserver = new MinecraftLogObserver(logFile);
    injectionBuffer = new InjectionBuffer(this::getStructureName);
    structureDir = worldDir.resolve("structures");
    dataFile = structureDir.resolve(getStructureNamePrefix() + "data.txt");
    open();
  }

  private String getStructureNamePrefix() {
    return "inject/" + identifier + "/";
  }

  private String getStructureName(int structureId) {
    return getStructureNamePrefix() + structureId;
  }

  private Path getStructureFile(int structureId) {
    return structureDir.resolve(getStructureName(structureId) + ".nbt");
  }

  /**
   * @return the value of {@link #logObserver}
   */
  public MinecraftLogObserver getLogObserver() {
    return logObserver;
  }

  /**
   * {@link #isOpen() Open} {@code this} connection and wait for a response from Minecraft. Once the
   * response is received, {@link #isActive() activate} {@code this} connection.
   *
   * @throws IOException if an I/O error occurs while opening the
   *         {@link MinecraftLogObserver#logFile log file}, locking the {@link #counterFile} or
   *         {@link #flush() flushing}
   * @throws InterruptedException if the current thread is interrupted while waiting for Minecraft's
   *         response
   */
  public void open() throws IOException, InterruptedException {
    checkState(!isOpen(), "This connection is already established!");
    logger.info("Establishing connection");
    lockDataFile();
    logObserver.open();
    executor = Executors.newSingleThreadScheduledExecutor();
    int structureId = loadStructureId();
    this.structureId.set(structureId);
    logger.info("Using structure '{}'", getStructureName(structureId));
    Semaphore semaphore = new Semaphore(0);
    injectCommand(SUCCESSFUL_COMMAND, e -> {
      confirmStructure(structureId);
      semaphore.release();
    });
    flush();
    logger.info("Waiting for Minecraft's response");
    semaphore.acquire();
    schedulePeriodicFlush();
    logger.info("Successfully established connection");
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
    createParentDirs(dataFile.toFile());
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
    closeDataFileChannel();
    throw new IllegalStateException("A " + this + " is already open", ex);
  }

  /**
   * Load the value of {@link #structureId} from {@link #dataFile}.
   *
   * @return the value for {@link #structureId}
   * @throws IOException if an I/O error occurs while reading {@link #dataFile}
   */
  private int loadStructureId() throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(Ints.checkedCast(dataFileChannel.size()));
    dataFileChannel.read(buffer);
    String fileContent = new String(buffer.array(), Charsets.UTF_8);
    return Integers.parseInt(fileContent);
  }

  /**
   * Save the specified value of {@link #structureId} to {@link #dataFile}.
   *
   * @param structureId the value of {@link #structureId} (this is a parameter instead of
   *        {@code this.structirId.get()} due to concurrency)
   * @throws IOException if an I/O error occurs while writing to {@link #dataFile}
   */
  private void saveStructureId(int structureId) throws IOException {
    dataFileChannel.position(0);
    dataFileChannel.write(Charsets.UTF_8.encode(String.valueOf(structureId + 1)));
    dataFileChannel.truncate(dataFileChannel.position());
  }

  @Override
  public void close() throws IOException {
    if (isOpen()) {
      logger.info("Closing connection");
      lastConfirmedStructureId = -1;
      executor.shutdown();
      logObserver.close();
      flush();
      executor = null;
      closeDataFileChannel();
    }
  }

  private void closeDataFileChannel() throws IOException {
    dataFileChannel.close();
    dataFileChannel = null;
  }

  /**
   * Return whether {@code this} connection {@link #isActive()} or is ready to be activated.
   * <p>
   * A connection is opened after construction or by calling {@link #open()}.<br>
   * To {@link #isClosed() close} {@code this} connection call {@link #close()}.
   *
   * @return whether {@code this} connection is open
   */
  public boolean isOpen() {
    return executor != null && dataFileChannel != null;
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
   * stop periodic {@link MinecraftLogObserver#checkLog() log checking}.<br>
   * After pausing {@link #isOpen()} will return {@code true} and {@link #isActive()} will return
   * {@code false}.
   *
   * @throws IllegalStateException if {@code this} connection is not {@link #isOpen() open}
   */
  public void pause() throws IllegalStateException {
    checkOpen();
    if (isActive()) {
      logger.info("Pausing connection");
      cancelPeriodicFlush();
    }
  }

  /**
   * Resume {@code this} connection by scheduling periodic {@link #flush() flushing}.<br>
   * After resuming both {@link #isOpen()} and {@link #isActive()} will return {@code true}.
   *
   * @throws IllegalStateException if {@code this} connection is not {@link #isOpen() open}
   */
  public void resume() throws IllegalStateException {
    checkOpen();
    if (isPaused()) {
      logger.info("Resuming connection");
      schedulePeriodicFlush();
    }
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

  private void schedulePeriodicFlush() {
    flushFuture = executor.scheduleAtFixedRate(() -> {
      try {
        if (isTimedOut()) {
          logger.warn("Connection timed out");
          pause();
        } else {
          flush();
        }
      } catch (Throwable t1) {
        logger.error("Periodic flush encountered an error", t1);
        try {
          close();
        } catch (Throwable t2) {
          logger.error("Closing encountered an error", t2);
        }
      }
    }, 0, flushPeriod, flushTimeUnit);
  }

  private void cancelPeriodicFlush() {
    flushFuture.cancel(false);
    flushFuture = null;
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
   * Flush the contents of the underlying {@link #injectionBuffer} by creating a new
   * {@link Structure} file in the {@link #structureDir}.
   * <p>
   * If {@code this} connection {@link #isActive()} a flush will be performed periodically according
   * to {@link #flushPeriod} and {@link #flushTimeUnit}.
   *
   * @throws IllegalStateException if {@code this} connection is not {@link #isOpen() open}
   * @throws IOException if an I/O error occurs while creating the {@link Structure} file
   */
  public void flush() throws IllegalStateException, IOException {
    checkOpen();
    int structureId;
    synchronized (this.structureId) { // Synchronized incrementing of structureId
      structureId = this.structureId.get();
      injectTimeoutCheckIfNeccessary(structureId);
      Structure structure = injectionBuffer.createStructure(structureId);
      if (structure == null) {
        return;
      }
      structure.writeTo(getStructureFile(structureId).toFile());
      // Don't increment if no structure was written
      this.structureId.incrementAndGet();
    }
    saveStructureId(structureId);
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
    if (lastConfirmedStructureId != -1 && structureId % TIME_OUT_CHECK_FREQUENCY == 0) {
      injectCommand(SUCCESSFUL_COMMAND, e -> {
        confirmStructure(structureId);
        if (isPaused() && !isTimedOut()) {
          logger.warn("Connection is no longer timed out");
          resume();
        }
      });
    }
  }

  /**
   * Update {@link #lastConfirmedStructureId} and delete old {@link Structure} files.
   *
   * @param structureId the ID of the new {@link #lastConfirmedStructureId}
   */
  private void confirmStructure(int structureId) {
    int i;
    if (lastConfirmedStructureId < 0) {
      i = structureId - TIME_OUT_CHECK_FREQUENCY;
    } else {
      i = lastConfirmedStructureId;
    }
    i = Math.max(0, i); // There should not be negative structureIds
    if (i <= structureId) {
      logger.debug("cleaning up old structure files, ID {} up to {}", i, structureId);
    }
    for (; i <= structureId; i++) {
      Path structureFile = getStructureFile(i);
      try {
        Files.deleteIfExists(structureFile);
      } catch (IOException ex) {
        logger.warn("Failed to clean up old structure file", ex);
      }
    }
    lastConfirmedStructureId = structureId;
  }

  public void injectCommand(String command) throws IllegalStateException {
    injectCommand(new Command(command));
  }

  public void injectCommand(Command command) throws IllegalStateException {
    checkOpen();
    injectionBuffer.addCommand(command);
  }

  public void injectCommand(String command, Consumer<SuccessEvent> listener)
      throws IllegalStateException {
    String name = UUID.randomUUID().toString();
    injectCommand(new Command(name, command), listener);
  }

  public void injectCommand(Command command, Consumer<SuccessEvent> listener)
      throws IllegalStateException {
    logObserver.addSuccessListener(command.getName(), false, listener);
    injectionBuffer.addFetchCommand(command);
  }

  @Override
  public String toString() {
    return "connection '" + identifier + "' to Minecraft world '" + worldDir.getFileName() + "'";
  }
}
