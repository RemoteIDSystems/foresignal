package ai.foresignal.trigger;

/**
 * Receives alert events from the {@link AlertBus}, on the listener's own virtual thread, one at a
 * time, in publish order (which is {@code rev} order).
 */
public interface AlertListener {

    /** One event. */
    void onAlert(AlertEvent event);

    /**
     * The listener's queue overflowed and events were dropped; called once after the queue drains.
     * A listener that needs every event replays from the database (the alert stream replays by
     * {@code rev}).
     */
    default void onGap() {}
}
