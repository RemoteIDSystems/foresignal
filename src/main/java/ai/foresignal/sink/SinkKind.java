package ai.foresignal.sink;

/** Whether a sink consumes an ordered change feed or mirrors current state (prompt 110). */
public enum SinkKind {
    /** Ordered, at-least-once delivery of persisted versions from a durable cursor. */
    CHANGE_FEED,
    /** An external copy of current state: snapshot then ChangeBus plus LiveBus updates. */
    STATE_MIRROR
}
