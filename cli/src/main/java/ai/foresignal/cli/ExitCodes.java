package ai.foresignal.cli;

/**
 * The process exit codes of the {@code foresignal} command. Scripts can rely on them.
 */
public final class ExitCodes {

    /** The command succeeded. */
    public static final int OK = 0;

    /** Usage error: an unknown option, a missing argument, an unreadable input file or invalid input. */
    public static final int USAGE = 1;

    /** The API answered with an error; its ProblemDetail {@code code} is printed on standard error. */
    public static final int API_ERROR = 2;

    /** The server could not be reached (connection refused, DNS failure, timeout, dropped connection). */
    public static final int NETWORK = 3;

    private ExitCodes() {
    }
}
