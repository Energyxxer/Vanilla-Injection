package com.energyxxer.inject;

import com.energyxxer.inject.exceptions.IllegalStateException;
import com.energyxxer.inject.level_utils.LevelReader;
import com.energyxxer.inject.listeners.ChatEvent;
import com.energyxxer.inject.listeners.ChatListener;
import com.energyxxer.inject.listeners.LogEvent;
import com.energyxxer.inject.listeners.LogListener;
import com.energyxxer.inject.listeners.ProcessingTickListener;
import com.energyxxer.inject.listeners.SuccessEvent;
import com.energyxxer.inject.listeners.SuccessListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Class that controls all Vanilla Injection functions.
 */
public class InjectionMaster {
    /**
     * The directory of the world to inject.
     * */
    private File worldDirectory;
    /**
     * The log file. Usually '.minecraft/logs/latest.log'
     * */
    private File logFile;

    /**
     * The prefix given to all structures spawned with this master.
     * */
    final String prefix;

    /**
     * Number of lines previously read from the log. -1 if the
     * log hasn't been read before.
     * */
    private static int lastLine = -1;

    /**
     * List containing all log listeners for this master.
     * */
    private ArrayList<LogListener> logListeners = new ArrayList<>();
    /**
     * List containing all chat listeners for this master.
     * */
    private ArrayList<ChatListener> chatListeners = new ArrayList<>();
    /**
     * List containing all processing tick listeners for this master.
     * */
    private ArrayList<ProcessingTickListener> processingTickListeners = new ArrayList<>();
    /**
     * Map containing all success listeners for this master.
     * */
    private HashMap<String, SuccessListener> successListeners = new HashMap<>();

    /**
     * Whether the injection master is running or not.
     * */
    private boolean running = false;

    /**
     * Timer to which injection events are scheduled.
     * */
    private Timer timer;

    /**
     * How often the logs should be read (in milliseconds).
     * */
    private long logCheckFrequency = 1000;
    /**
     * How often the injector should flush commands (in milliseconds).
     * */
    private long injectionFrequency = 1000;
    /**
     * How often the processing tick listeners should run (in milliseconds).
     * */
    private long processingFrequency = 1000;
    /**
     * How long to keep read chunks in memory for (in milliseconds).
     * */
    private long chunkRefreshFrequency = 1000;

    /**
     * This master's structure injector.
     * */
    public final Injector injector;
    /**
     * This master's level reading utility object.
     * */
    public final LevelReader reader;

    /**
     * Whether not to fire log events next time the log is read.
     * */
    private boolean mute = true;

    /**
     * Whether or not to print debug messages to the console.
     * */
    private boolean verbose = false;

    /**
     * The date format used for verbose timestamps.
     * */
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("[HH:mm:ss]");

    /**
     * Creates an injection master using the given world directory, log file, and injector prefix.
     *
     * @param worldDirectory File pointing to the folder of the world to inject to.
     * @param logFile File pointing to the latest.log file. This will be read for log and chat events.
     * @param prefix Prefix used in structure files for this injector.
     * */
    public InjectionMaster(File worldDirectory, File logFile, String prefix) {
        this.worldDirectory = worldDirectory;
        this.logFile = logFile;
        this.prefix = prefix;

        this.injector = new Injector(this);
        this.reader = new LevelReader(this);
    }

    /**
     * Starts the injector loop.
     * */
    public void start() {
        if(running) throw new IllegalStateException("Injection is already running.");
        if(verbose) System.out.println(TIME_FORMAT.format(new Date()) + " [InjectionMaster] Starting injection master.");

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkLogChanges();
            }
        }, 0, logCheckFrequency);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(!paused) onProcessingTick();
            }
        }, 0, processingFrequency);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(!paused) onInjectionTick();
            }
        }, 0, injectionFrequency);

        running = true;
    }

    /**
     * Stops the running injector loop.
     * */
    public void stop() {
        if(!running) throw new IllegalStateException("Injection is not currently running.");
        if(verbose) System.out.println(TIME_FORMAT.format(new Date()) + " [InjectionMaster] Stopping injection master.");

        timer.cancel();
        timer = null;

        running = false;
    }

    /**
     * Whether this injection master is paused or not.
     * This is used to ensure structure files aren't filling
     * the structures folder when the game is closed or paused.
     * */
    boolean paused = false;
    /**
     * Pauses the injection master.
     * */
    void pause() {
        if(verbose) System.out.println(TIME_FORMAT.format(new Date()) + " [InjectionMaster] Pausing injection master.");
        paused = true;
    }
    /**
     * Resumes the injection master.
     * */
    void resume() {
        if(verbose) System.out.println(TIME_FORMAT.format(new Date()) + " [InjectionMaster] Resuming injection master.");
        paused = false;
    }

    /**
     * Sets how long the master should wait before checking the log again.
     *
     * @param logCheckFrequency The time (in milliseconds) between log checks.
     * */
    public void setLogCheckFrequency(long logCheckFrequency) {
        if(running) throw new IllegalStateException("Cannot set log check frequency while injection is running.");

        if(logCheckFrequency <= 0) throw new IllegalArgumentException("Non-positive log check frequency.");

        if(verbose) System.out.println(TIME_FORMAT.format(new Date()) + " [InjectionMaster] Setting log check frequency to " + logCheckFrequency + ".");
        this.logCheckFrequency = logCheckFrequency;
    }

    /**
     * Sets how long the injector should wait before generating a structure file from the buffered commands, if there are any.
     *
     * @param injectionFrequency The time (in milliseconds) between injections.
     * */
    public void setInjectionFrequency(long injectionFrequency) {
        if(running) throw new IllegalStateException("Cannot set injection frequency while injection is running.");

        if(injectionFrequency <= 0) throw new IllegalArgumentException("Non-positive injection frequency.");

        if(verbose) System.out.println(TIME_FORMAT.format(new Date()) + " [InjectionMaster] Setting injection frequency to " + injectionFrequency + ".");
        this.injectionFrequency = injectionFrequency;
    }

    /**
     * Sets how long the master should wait before firing processing tick events.
     *
     * @param processingFrequency The time (in milliseconds) between processing ticks.
     * */
    public void setProcessingFrequency(long processingFrequency) {
        if(running) throw new IllegalStateException("Cannot set processing frequency while injection is running.");

        if(processingFrequency <= 0) throw new IllegalArgumentException("Non-positive processing frequency.");

        if(verbose) System.out.println(TIME_FORMAT.format(new Date()) + " [InjectionMaster] Setting processing frequency to " + processingFrequency + ".");
        this.processingFrequency = processingFrequency;
    }

    /**
     * Sets how long often chunks should be cleared from memory to allow new data to be read.
     *
     * @param chunkRefreshFrequency The time (in milliseconds) between chunk refreshes.
     * */
    public void setChunkRefreshFrequency(long chunkRefreshFrequency) {
        if(running) throw new IllegalStateException("Cannot set chunk refreshing frequency while injection is running.");

        if(chunkRefreshFrequency <= 0) throw new IllegalArgumentException("Non-positive chunk refreshing frequency.");

        if(verbose) System.out.println(TIME_FORMAT.format(new Date()) + " [InjectionMaster] Setting chunk refresh frequency to " + chunkRefreshFrequency + ".");
        this.chunkRefreshFrequency = chunkRefreshFrequency;
    }

    /**
     * Adds a LogListener to the master.
     *
     * @param l The LogListener to add.
     * */
    public void addLogListener(LogListener l) {
        logListeners.add(l);
    }

    /**
     * Adds a ChatListener to the master.
     *
     * @param l The ChatListener to add.
     * */
    public void addChatListener(ChatListener l) {
        chatListeners.add(l);
    }

    /**
     * Adds a ProcessingTickListener to the master.
     *
     * @param l The ProcessingTickListener to add.
     * */
    public void addProcessingTickListener(ProcessingTickListener l) {
        processingTickListeners.add(l);
    }

    /**
     * Adds a SuccessListener to the master.
     *
     * @param name The name of the entity to listen for command successes for.
     * @param l The SuccessListener to add.
     * */
    public void addSuccessListener(String name, SuccessListener l) {
        successListeners.put(name, l);
    }

    /**
     * Runs every time an injection tick occurs.
     * */
    private void onInjectionTick() {
        injector.flush();
    }

    /**
     * Runs every processing tick.
     * */
    private void onProcessingTick() {
        processingTickListeners.forEach(ProcessingTickListener::onTick);
    }

    /**
     * Checks any changes in the log file and, if not muted, dispatches the appropriate log and chat events.
     * */
    private void checkLogChanges() {

        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            inputStream = new FileInputStream(logFile.getPath());
            sc = new Scanner(inputStream, "UTF-8");

            int linesScanned = -1;

            ArrayList<String> successListenersToRemove = new ArrayList<>();

            while (sc.hasNextLine()) {
                String line = sc.nextLine();

                linesScanned++;
                if(linesScanned < lastLine) {
                    continue;
                }

                if(!mute) {
                    dispatchLogEvent(new LogEvent(line));
                    ChatEvent ce = ChatEvent.createFromLogLine(line);
                    if(ce != null) dispatchChatEvent(ce);

                    for(String invoker : successListeners.keySet()) {
                        SuccessEvent se = SuccessEvent.createFromLogLine(line, invoker);
                        if(se != null) {
                            SuccessListener listener = successListeners.get(invoker);
                            listener.onSuccess(se);
                            if(listener.doOnce() && !successListenersToRemove.contains(invoker)) successListenersToRemove.add(invoker);
                        }
                    }
                }
            }

            for(String key : successListenersToRemove) {
                successListeners.remove(key);
            }

            lastLine = linesScanned + 1;
        } catch(FileNotFoundException x) {
            stop();
            throw new RuntimeException("Log file not found. Stopping injection.", x);
        } finally {
            if(mute) mute = false;
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException x) {
                    x.printStackTrace();
                }
            }
            if (sc != null) {
                sc.close();
            }
        }
    }

    /**
     * Sends a LogEvent to all attached LogListeners.
     *
     * @param e The event to send to all log listeners.
     * */
    private void dispatchLogEvent(LogEvent e) {
        logListeners.forEach(l -> l.onLog(e));
    }

    /**
     * Sends a ChatEvent to all attached ChatListeners.
     *
     * @param e The event to send to all chat listeners.
     * */
    private void dispatchChatEvent(ChatEvent e) {
        chatListeners.forEach(l -> l.onChat(e));
    }

    /**
     * Returns this master's attached world directory.
     *
     * @return This master's log file.
     * */
    public File getWorldDirectory() {
        return worldDirectory;
    }

    /**
     * Returns this master's attached log file.
     *
     * @return This master's log file.
     * */
    public File getLogFile() {
        return logFile;
    }

    /**
     * Returns this master's injection prefix.
     *
     * @return This master's prefix.
     * */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns true if this master is set to print debug messages.
     *
     * @return This master's verbose.
     * */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Sets whether or not this injection master should print debug messages.
     *
     * @param verbose Whether this master should print debug messages.
     * */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Whether this master's level reader has been scheduled to clear its chunks but hasn't been cleared.
     * */
    private boolean chunkRefreshScheduled = false;

    /**
     * Schedules a chunk clear, if possible.
     * */
    public void scheduleChunkRefresh() {
        if(!chunkRefreshScheduled) {
            chunkRefreshScheduled = true;
            this.timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    reader.clearChunkMemory();
                    chunkRefreshScheduled = false;
                }
            },this.chunkRefreshFrequency);
        }
    }
}
