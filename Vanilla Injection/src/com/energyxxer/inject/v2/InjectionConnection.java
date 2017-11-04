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

  private final Path logFile;
  private final Path worldDir;
  private final Path structureDir;
  private final String identifier;

  private @Nullable ScheduledExecutorService executor;
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
  private volatile boolean logAdminCommands;
  private final Object logAdminCommandsLock = new Object();

  private final Collection<Command> commandBuffer = new ConcurrentLinkedQueue<>();

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
    scheduleLogCheck();
    Semaphore semaphore = new Semaphore(0);
    // Any successful command will do
    injectCommand("gamerule logAdminCommands true", e -> semaphore.release());
    flush();
    logger.info("Waiting for Minecraft's response");
    semaphore.acquire();
    scheduleFlush();
    logger.info("Successfully established {}", this);
  }

  @Override
  public void close() {
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
    scheduleLogCheck();
    scheduleFlush();
  }

  private void scheduleLogCheck() {
    logCheckFuture =
        executor.scheduleAtFixedRate(this::checkLog, 0, logCheckPeriod, logCheckTimeUnit);
  }

  private void scheduleFlush() {
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
    cancelLogCheck();
    cancelFlush();
  }

  private void cancelLogCheck() {
    logCheckFuture.cancel(false);
    logCheckFuture = null;
  }

  private void cancelFlush() {
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
   * @param logCheckPeriod the new value for {@link #logCheckPeriod}
   * @param logCheckTimeUnit the new value for {@link #logCheckTimeUnit}
   */
  public void setLogCheckFrequency(long logCheckPeriod, TimeUnit logCheckTimeUnit) {
    this.logCheckPeriod = logCheckPeriod;
    this.logCheckTimeUnit = checkNotNull(logCheckTimeUnit, "logCheckTimeUnit == null!");
    if (isActive()) {
      cancelLogCheck();
      scheduleLogCheck();
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
   * @param flushPeriod the new value for {@link #flushPeriod}
   * @param flushTimeUnit the new value for {@link #flushTimeUnit}
   */
  public void setFlushFrequency(long flushPeriod, TimeUnit flushTimeUnit) {
    this.flushPeriod = flushPeriod;
    this.flushTimeUnit = checkNotNull(flushTimeUnit, "flushTimeUnit == null!");
    if (isActive()) {
      cancelFlush();
      scheduleFlush();
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
    if (commandBuffer.isEmpty()) {
      logger.trace("Skipping flush due to empty buffer");
      return;
    }
    int structureId = this.structureId.getAndIncrement();

    Structure structure = new Structure(922, "Vanilla-Injection");
    structure.setBackground(new SimpleBlockState("minecraft:air"));
    structure
        .addBlock(new StructureBlock(new Coordinate3I(), LOAD, getStructureName(structureId + 1)));

    structure.addBlock(new SimpleBlock("minecraft:redstone_block", new Coordinate3I(0, 0, 2)));
    structure.addBlock(new SimpleBlock("minecraft:activator_rail", new Coordinate3I(0, 1, 2)));

    Coordinate3D minecartPos = new Coordinate3D(0.5, 1.0625, 2.5);
    boolean logAdminCommands = this.logAdminCommands;
    synchronized (logAdminCommandsLock) {
      this.logAdminCommands = false;
      if (logAdminCommands) {
        Command command = new Command("gamerule logAdminCommands true");
        structure.addEntity(new CommandBlockMinecart(command, minecartPos));
      }
      Iterators.consumingIterator(commandBuffer.iterator()).forEachRemaining(command -> {
        structure.addEntity(new CommandBlockMinecart(command, minecartPos));
      });
    }
    if (logAdminCommands) {
      Command command = new Command("gamerule logAdminCommands false");
      structure.addEntity(new CommandBlockMinecart(command, minecartPos));
    }
    structure.addEntity(new CommandBlockMinecart(
        new Command("kill @e[type=commandblock_minecart,dy=0]"), minecartPos));
    structure.writeTo(structureDir.resolve(getStructureName(structureId) + ".nbt").toFile());
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
    synchronized (logAdminCommandsLock) {
      logAdminCommands = true;
    }
    injectCommand(command);
    addSuccessListener(command.getName(), false, listener);
  }

  @Override
  public String toString() {
    return "connection '" + identifier + "' to Minecraft world: " + worldDir;
  }
}
