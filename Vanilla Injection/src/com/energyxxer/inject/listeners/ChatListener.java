package com.energyxxer.inject.listeners;

/**
 * The listener interface for receiving chat events.
 * If you're interesting in reading changes in the log file, use a LogListener instead.
 */
public interface ChatListener {
    /**
     * Fires every time a player sends a chat message.
     *
     * @param e The ChatEvent indicating the information about the chat message sent.
     * */
    void onChat(ChatEvent e);
}
