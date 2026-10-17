package ai.foresignal.analytics.finding;

/** Receives finding stream events from the {@link FindingEventBus} (prompt 119). */
public interface FindingListener {

    /** One event. */
    void onEvent(FindingEvent event);

    /** The listener lagged and events were dropped. */
    default void onGap() {}
}
