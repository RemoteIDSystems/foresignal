package ai.foresignal.store.history;

import ai.foresignal.source.spi.HistoryTier;
import java.time.Duration;
import java.time.Instant;

/**
 * How faithful stored history is to what was ingested (prompt 037; prompt 044 implements it).
 *
 * <p>History downsampling (Core-Data-Model §13, prompt 044) rewrites old track leaves and keeps only
 * some versions. Every reader of the transaction axis (asOf records and tiles, tracks, frames, the
 * timeline) asks this hook whether a seq range was thinned, how far TTL validity must be widened to
 * bridge the gaps, and before which instant history is final. Until 044 ships,
 * {@link RawHistoryFidelity} answers "raw everywhere".
 *
 * <p><strong>Prompt 044 must implement this interface</strong> (a bean of this type replaces
 * {@link RawHistoryFidelity}, which is registered only when no other bean exists).
 */
public interface HistoryFidelity {

    /**
     * True when any version of {@code tier} with a seq in {@code [fromSeq, toSeq]} lies in a
     * downsampled range, so answers over that range are approximate.
     */
    boolean downsampled(HistoryTier tier, long fromSeq, long toSeq);

    /**
     * How much to widen the TTL validity of {@code sourceId}'s versions over downsampled ranges
     * ({@code downsampleInterval}); {@link Duration#ZERO} when history is raw.
     */
    Duration validityWidening(String sourceId);

    /**
     * The instant before which history of {@code tier} will not be rewritten any more (already
     * downsampled, or never will be). An answer for a time at or after it may still change when
     * downsampling runs, so it must not be cached as immutable. {@link Instant#MAX} when history is
     * never rewritten.
     */
    Instant finalBefore(HistoryTier tier);
}
