package ai.foresignal.retention;

/** The state of one leaf's downsampling in {@code history_downsample} (prompt 044). */
public enum DownsampleStatus {
    /** The rewrite is in progress (or was interrupted; startup recovery resolves it). */
    RUNNING,
    /** The thinned copy is attached in place of the original leaf. */
    DONE,
    /** The rewrite gave up; the original leaf is untouched and may be tried again. */
    FAILED
}
