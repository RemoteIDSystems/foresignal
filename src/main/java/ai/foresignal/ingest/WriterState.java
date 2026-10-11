package ai.foresignal.ingest;

/** What the ingest writer is doing (Core-Data-Model §6.1). */
public enum WriterState {
    /** Processing commands. */
    RUNNING(0),
    /** Holding batches because the clock is behind the last ingest time. */
    CLOCK_BEHIND(1),
    /** Holding batches because the advisory lock connection was lost. */
    LOCK_LOST(2),
    /** Not started, or shut down. */
    STOPPED(3);

    private final int code;

    WriterState(int code) {
        this.code = code;
    }

    /** The value of the {@code foresignal.ingest.writer.state} gauge. */
    public int code() {
        return code;
    }
}
