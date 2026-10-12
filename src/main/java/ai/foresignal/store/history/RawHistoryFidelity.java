package ai.foresignal.store.history;

import ai.foresignal.source.spi.HistoryTier;
import java.time.Duration;
import java.time.Instant;

/**
 * The {@link HistoryFidelity} of raw, never-downsampled history: nothing is downsampled, validity
 * is never widened and every instant is final. Registered by {@link HistoryFidelityConfiguration}
 * only while no other {@link HistoryFidelity} bean exists; prompt 044 replaces it.
 */
public class RawHistoryFidelity implements HistoryFidelity {

    /** Always false: no range is downsampled. */
    @Override
    public boolean downsampled(HistoryTier tier, long fromSeq, long toSeq) {
        return false;
    }

    /** Always zero. */
    @Override
    public Duration validityWidening(String sourceId) {
        return Duration.ZERO;
    }

    /** Always {@link Instant#MAX}: raw history is never rewritten. */
    @Override
    public Instant finalBefore(HistoryTier tier) {
        return Instant.MAX;
    }
}
