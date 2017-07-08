package com.energyxxer.inject.listeners;

/**
 * The listener interface for receiving changes in the log file.
 * If you're interested in reading chat messages, use a ChatListener instead.
 */
public interface LogListener {
    /**
     * Fires for every new line in the log file.
     *
     * @param e The event indicating that a new line has been inserted to the log file.
     * */
    void onLog(LogEvent e);
}
