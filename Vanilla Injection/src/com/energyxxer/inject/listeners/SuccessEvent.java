package com.energyxxer.inject.listeners;

/**
 * An event class that indicates that a certain command has been run successfully, specifying its time of invocation, its invoker, and its output message.
 */
public class SuccessEvent {
    /**
     * The command's execution time as obtained from the log file.
     * */
    private final String timestamp;
    /**
     * The command's invoker name.
     * */
    private final String invoker;
    /**
     * The command's output message as obtained from the log file.
     * */
    private final String message;

    /**
     * Creates a log event from the given line.
     *
     * @param timestamp The timestamp of the command's execution as printed to the log file.
     * @param invoker The name of the executing command block minecart.
     * @param message The command's success message.
     * */
    private SuccessEvent(String timestamp, String invoker, String message) {
        this.timestamp = timestamp;
        this.invoker = invoker;
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
     * Gets the name of the entity that ran this command.
     *
     * @return The name of the entity.
     * */
    public String getInvoker() {
        return invoker;
    }

    /**
     * Gets the output message from the command block.
     *
     * @return The command's success message.
     * */
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return timestamp + " [" + invoker + ": " + message + "]";
    }

    /**
     * Attempts to create a SuccessEvent from the given log line matching this pattern:
     * <br>
     * <code>[<em>timestamp</em>] [Server thread/INFO]: [<em>invoker</em>: <em>message</em>]</code>
     *
     * @param line The log line to create a SuccessEvent from.
     * @param name The name of the entity to run the command.
     *
     * @return A SuccessEvent representing this log line, if it matches the pattern. Returns <code>null</code> otherwise.
     * */
    public static SuccessEvent createFromLogLine(String line, String name) {
        String header = "[Server thread/INFO]: [" + name + ": ";
        int index = line.indexOf(header);
        if(index >= 0 && index < 15) {
            String timestamp = line.substring(0, index - 1);
            String str = line.substring(index + header.length());
            return new SuccessEvent(timestamp, name, str.substring(0, str.length()-1));
        }
        return null;
    }
}
