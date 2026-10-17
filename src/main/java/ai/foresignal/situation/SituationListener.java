package ai.foresignal.situation;

/** Receives situation stream events from the {@link SituationEventBus} (prompt 118). */
public interface SituationListener {

    /** One event. */
    void onEvent(SituationEvent event);

    /** The listener lagged and events were dropped; live state must be resynced. */
    default void onGap() {}
}
