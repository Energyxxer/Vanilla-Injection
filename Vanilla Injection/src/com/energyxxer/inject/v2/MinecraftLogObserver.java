package com.energyxxer.inject.v2;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.file.Files.isRegularFile;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.energyxxer.inject.listeners.ChatEvent;
import com.energyxxer.inject.listeners.LogEvent;
import com.energyxxer.inject.listeners.SuccessEvent;
import com.energyxxer.inject.utils.LogFileReader;

/**
 * @author Adrodoc55
 */
@ThreadSafe
public class MinecraftLogObserver implements AutoCloseable {
  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * The log file of the Minecraft installation. Note that when using multiple instances of
   * Minecraft they will use the same log file and overwrite each others messages. For that reason a
   * {@link MinecraftLogObserver} only works properly when there is only one Minecraft instance.
   */
  private final Path logFile;

  /**
   * The {@link LogFileReader} to use for the periodic {@link #checkLog() log checking}. The
   * {@link #reader} is {@code null} unless the observer {@link #isOpen()}.
   */
  private @Nullable LogFileReader reader;

  /**
   * The {@link ScheduledExecutorService} to use for periodic {@link #checkLog() log checking}. The
   * {@link #executor} is {@code null} unless the observer {@link #isOpen()}.
   */
  private @Nullable ScheduledExecutorService executor;

  /**
   * The {@link ScheduledFuture} used to cancel periodic {@link #checkLog() log checking}. The
   * {@link #logCheckFuture} is {@code null} unless the connection {@link #isOpen()}.
   */
  private @Nullable ScheduledFuture<?> logCheckFuture;
  private long logCheckPeriod = 20;
  private TimeUnit logCheckTimeUnit = MILLISECONDS;

  private final List<Consumer<? super LogEvent>> logListeners = new CopyOnWriteArrayList<>();
  private final List<Consumer<? super ChatEvent>> chatListeners = new CopyOnWriteArrayList<>();
  private final Collection<SuccessListenerEntry> successListeners = new ConcurrentLinkedQueue<>();

  /**
   * Create and {@link #open()} a new {@link MinecraftLogObserver} that observes changes to
   * {@link #logFile} and dispatches events to registered listeners.
   *
   * @param logFile the {@link #logFile}
   * @throws NullPointerException if {@code logFile} is {@code null}
   * @throws IllegalArgumentException if {@code logFile} is not a regular file
   * @throws IOException if an I/O error occurs while opening the {@link #logFile}
   */
  public MinecraftLogObserver(Path logFile)
      throws NullPointerException, IllegalArgumentException, IOException {
    this.logFile = checkNotNull(logFile, "logFile == null!");
    checkArgument(isRegularFile(logFile), "%s is not a regular file!", logFile);
    open();
  }

  /**
   * Start observing the {@link #logFile} for changes if {@code this} observer is not already
   * {@link #isOpen() open}.
   *
   * @throws IOException if an I/O error occurs while opening the {@link #logFile}
   */
  public synchronized void open() throws IOException {
    if (isClosed()) {
      LOGGER.info("Opening log observer");
      reader = new LogFileReader(logFile);
      executor = Executors.newSingleThreadScheduledExecutor();
      schedulePeriodicLogCheck();
    }
  }

  /**
   * Stop observing the {@link #logFile} for changes if {@code this} observer {@link #isOpen()}.
   */
  @Override
  public synchronized void close() {
    if (isOpen()) {
      LOGGER.info("Closing log observer");
      cancelPeriodicLogCheck();
      executor.shutdown();
      executor = null;
      reader = null;
    }
  }

  /**
   * Return whether {@code this} observer is currently observing the {@link #logFile} for changes.
   * <p>
   * An observer is opened after construction or by calling {@link #open()}.<br>
   * To {@link #isClosed() close} {@code this} observer call {@link #close()}.
   *
   * @return whether {@code this} observer is open
   */
  public boolean isOpen() {
    return executor != null && reader != null && logCheckFuture != null;
  }

  public boolean isClosed() {
    return !isOpen();
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
  public synchronized void setLogCheckFrequency(long logCheckPeriod, TimeUnit logCheckTimeUnit) {
    this.logCheckPeriod = logCheckPeriod;
    this.logCheckTimeUnit = checkNotNull(logCheckTimeUnit, "logCheckTimeUnit == null!");
    if (isOpen()) {
      cancelPeriodicLogCheck();
      schedulePeriodicLogCheck();
    }
  }

  private void schedulePeriodicLogCheck() {
    logCheckFuture =
        executor.scheduleAtFixedRate(this::checkLog, 0, logCheckPeriod, logCheckTimeUnit);
  }

  private void cancelPeriodicLogCheck() {
    logCheckFuture.cancel(false);
    logCheckFuture = null;
  }

  /**
   * Check for new changes to the {@link #logFile} since the last check or alternatively since
   * {@link #open() opening} {@code this} observer and dispatch new events.
   * <p>
   * If {@code this} observer {@link #isActive()} a log check will be performed periodically
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

  /**
   * Add the specified listener to be notified whenever the success of a command that is executed by
   * {@code executor} is detected in the {@link #logFile}.
   *
   * @param executor the executor of the succesful command
   * @param repeat {@code false} if the {@code listener} should be deregistered after the first
   *        notification
   * @param listener
   */
  public void addSuccessListener(String executor, boolean repeat, Consumer<SuccessEvent> listener) {
    successListeners.add(new SuccessListenerEntry(executor, repeat, listener));
  }
}
