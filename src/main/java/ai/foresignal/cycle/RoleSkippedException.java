package ai.foresignal.cycle;

/** A role (or one step of it) could not run within its budget or was refused; it is recorded as skipped (prompt 128). */
public class RoleSkippedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** A skip with {@code reason}. */
    public RoleSkippedException(String reason) {
        super(reason);
    }
}
