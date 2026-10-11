package ai.foresignal.stream;

/**
 * Receives committed change batches from the {@link ChangeBus}.
 *
 * <p>Called on the subscriber's own virtual thread, one batch at a time, in publish order. A slow
 * listener only delays itself.
 */
public interface ChangeListener {

    /** One committed batch. */
    void onBatch(ChangeBatch batch);

    /**
     * The subscriber's queue overflowed and batches were dropped. Called once, after the queued
     * batches are delivered; the listener catches up from the database for every seq greater than
     * {@code lastDeliveredSeq}. Batches published after the gap was detected may then also arrive
     * through {@link #onBatch}, so a listener deduplicates by seq.
     *
     * @param lastDeliveredSeq the highest seq delivered before the gap
     */
    void onGap(long lastDeliveredSeq);
}
