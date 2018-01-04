package com.energyxxer.log;

/**
 * An event class that indicates that a chat message has been sent.
 */
public class ChatEvent {
    /**
     * The timestamp of this chat message, as given in the log line.
     * */
    private String timestamp;
    /**
     * The username of the player who sent this message.
     * */
    private String username;
    /**
     * The message sent by the user.
     * */
    private String message;

    /**
     * Creates a ChatEvent from the given timestamp, username and message.
     *
     * @param timestamp The event's timestamp.
     * @param username The event's username.
     * @param message The event's message.
     * */
    private ChatEvent(String timestamp, String username, String message) {
        this.timestamp = timestamp;
        this.username = username;
        this.message = message;
    }

    /**
     * Gets the timestamp of the message.
     *
     * @return The timestamp, as printed to the log.
     * */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the name of the player that sent this message.
     *
     * @return The name of the player.
     * */
    public String getSender() {
        return username;
    }

    /**
     * Gets the message sent by the player.
     *
     * @return The message sent by the player.
     * */
    public String getMessage() {
        return message;
    }

    /**
     * Attempts to create a ChatEvent from the given log line matching this pattern:
     * <br>
     * <code>[<em>timestamp</em>] [Server thread/INFO]: &lt;<em>username</em>&gt; <em>message</em></code>
     *
     * @param line The log line to create a ChatEvent from.
     *
     * @return A ChatEvent representing this log line, if it matches the pattern. Returns <code>null</code> otherwise.
     * */
    public static ChatEvent createFromLogLine(String line) {
        if(line.length() < 10) return null;
        if(line.charAt(0) != '[') return null;
        if(!line.substring(1).contains("]")) return null;
        if(!line.substring(1).contains("[")) return null;

        String timestamp = line.substring(1,line.indexOf("]"));

        String str = line.substring(1+(line.substring(1)).indexOf("["));

        if(!str.startsWith("[Server thread/INFO]: <")) return null;
        str = str.substring("[Server thread/INFO]: <".length());
        if(!str.contains(">")) return null;

        String username = str.substring(0, str.indexOf(">"));

        String message = str.substring(str.indexOf(">")+2);

        return new ChatEvent(timestamp, username, message);
    }

    @Override
    public String toString() {
        return '[' + timestamp + "] <" + username + "> " + message;
    }
}
