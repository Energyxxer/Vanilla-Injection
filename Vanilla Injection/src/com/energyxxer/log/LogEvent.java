package com.energyxxer.log;

/**
 * An event which indicates that a new line has been printed into the log.
 */
public class LogEvent {
    /**
     * The entire line added to the log.
     * */
    private final String line;

    /**
     * Creates a log event from the given line.
     *
     * @param line The new log line.
     * */
    public LogEvent(String line) {
        this.line = line;
    }

    /**
     * Returns the entire line associated with this LogEvent.
     *
     * @return The new line.
     * */
    public String getLine() {
        return line;
    }

    /**
     * Used to identify log lines that represent commands invoked by command blocks or entities by the given name.
     * Applicable lines usually follow this pattern:
     * <br>
     *     <code>[<em>timestamp</em>] [Server thread/INFO]: [<em>name of invoker</em>: <em>Return message of command</em>]</code>
     * <br>
     * @param name The name of the invoker to check for.
     *
     * @return The entire return message of the command if the line matches the pattern; otherwise null.
     * */
    public String getReturnValueFor(String name) {
        return getReturnValueFor(name, false);
    }

    /**
     * Used to identify log lines that represent commands invoked by command blocks or entities by the given name.
     * If told to scan partially, it will check if the invoker's name starts with the given name.
     * Otherwise, it will check if the entire name matches.
     * Applicable lines usually follow this pattern:
     * <br>
     *     <code>[<em>timestamp</em>] [Server thread/INFO]: [<em>name of invoker</em>: <em>Return message of command</em>]</code>
     * <br>
     *
     * <br>
     * <b>Examples:</b>
     * <br>
     * <b>Case 1: </b>
     * <br>
     * <blockquote>
     * Log line: <code>"[20:06:41] [Server thread/INFO]: [$warper a: Teleported Energyxxer to -36.40643938557535, 1.0, -79.61036981050779]"</code>
     * <br>
     * Name: "$warper"
     * <br>
     * <b>Result, if partial</b>: <code>" a: Teleported Energyxxer to -36.40643938557535, 1.0, -79.61036981050779"</code>
     * <br>
     * <b>Result, if not partial</b>: <code>null</code>
     * </blockquote>
     * <b>Case 2: </b>
     * <blockquote>
     * Log line: <code>"[20:06:41] [Server thread/INFO]: [$clock: Time is 6000]"</code>
     * <br>
     * Name: "$clock"
     * <br>
     * <b>Result, if partial</b>: <code>": Time is 6000"</code>
     * <br>
     * <b>Result, if not partial</b>: <code>"Time is 6000"</code>
     * </blockquote>
     *
     * @param name The name of the invoker to check for.
     * @param partial Whether to check for the entire name. If true, it will only return the success message if the entire name matches.
     *                If false, it will return the rest of the invoker's name, a colon, and the success message.
     *
     * @return The entire return message of the command if the line matches the pattern; otherwise null.
     * */
    public String getReturnValueFor(String name, boolean partial) {
        int index = line.indexOf("[Server thread/INFO]: [" + name + ((partial) ? "" : ":"));
        if(index >= 0 && index < 15) {
            String str = line.substring(index + ("[Server thread/INFO]: [" + name + ((partial) ? "" : ": ")).length());
            if(partial && !str.contains(":")) return null;
            return str.substring(0, str.length()-1);
        }
        return null;
    }

    @Override
    public String toString() {
        return line;
    }
}
