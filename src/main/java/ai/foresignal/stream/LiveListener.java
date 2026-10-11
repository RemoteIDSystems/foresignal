package ai.foresignal.stream;

/**
 * Receives live batches from the {@link LiveBus}, on the subscriber's own virtual thread.
 */
public interface LiveListener {

    /** One committed live batch. */
    void onBatch(LiveBatch batch);

    /**
     * The subscriber's queue overflowed and at least one live batch was dropped. Live state is
     * conflated, so the right reaction is a resync of live state (SseHub sends
     * {@code resync{reason:"live_dropped"}}), not a replay.
     */
    void onLiveDropped();
}
