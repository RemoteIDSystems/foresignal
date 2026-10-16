package ai.foresignal.sink;

/** Whether Foresignal connects out or listens for a client (prompt 110). */
public enum SinkMode {
    /** Foresignal connects out to the target. */
    PUSH,
    /** Foresignal listens on a LAN address. */
    SERVE
}
