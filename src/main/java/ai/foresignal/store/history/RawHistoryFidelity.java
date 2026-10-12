package ai.foresignal.store.history;

import ai.foresignal.source.spi.HistoryTier;
import java.time.Duration;
import java.time.Instant;

/**
 * The {@link HistoryFidelity} of raw, never-downsampled history: nothing is downsampled, validity
 * is never widened and every instant is final. Not a bean: since prompt 044 the application uses
 * {@code ai.foresignal.retention.DownsampleFidelity}; this stays for unit tests and hand-built
 * resolvers.
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
