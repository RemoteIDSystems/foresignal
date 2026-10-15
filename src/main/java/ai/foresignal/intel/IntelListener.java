package ai.foresignal.intel;

/** Receives intelligence-event stream events from the {@link IntelEventBus}. */
public interface IntelListener {

    /** One event. */
    void onEvent(IntelEvent event);

    /** The listener lagged and events were dropped; live state must be resynced. */
    default void onGap() {}
}
