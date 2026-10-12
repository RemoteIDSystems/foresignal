package ai.foresignal.stream;

import ai.foresignal.ingest.PersistReason;
import ai.foresignal.source.spi.HistoryTier;
import java.time.Instant;

/**
 * One persisted version, as announced on the {@link ChangeBus} (Core-Data-Model §10.1).
 *
 * <p>Carries enough to filter by source, kind and area without loading the version. The two
 * previous boxes come from the entity's running state just before this version, so a subscriber
 * can tell that an entity <em>left</em> a filtered area: {@code prevPersistedBbox} is the box of
 * the previously persisted geometry ({@code h.persisted_geom}) and {@code prevLiveBbox} the box of
 * the previous live geometry ({@code l.geom}). Several versions of one entity within one batch
 * chain correctly, each seeing the state the one before it produced.
 *
 * @param seq the version's seq
 * @param prevSeq the entity's previous version's seq, or null for its first
 * @param historyTier the tier the version is stored in
 * @param sourceId the source
 * @param externalId the entity
 * @param kind the entity's kind
 * @param version the per-entity version number
 * @param persistReason why it was persisted
 * @param observedAt when upstream observed it
 * @param eventTime the discrete event time, or null
 * @param validUntil the effective expiry, or null for never
 * @param tombstone true when upstream said the entity is gone
 * @param qaFlags {@code QaFlags} bits
 * @param bbox the version's geometry box, or null
 * @param prevPersistedBbox the box of the previously persisted geometry, or null
 * @param prevLiveBbox the box of the previous live geometry, or null
 */
public record ChangeSummary(
        long seq,
        Long prevSeq,
        HistoryTier historyTier,
        String sourceId,
        String externalId,
        String kind,
        int version,
        PersistReason persistReason,
        Instant observedAt,
        Instant eventTime,
        Instant validUntil,
        boolean tombstone,
        int qaFlags,
        Bbox bbox,
        Bbox prevPersistedBbox,
        Bbox prevLiveBbox) {

    /**
     * True when this is the tombstone of an entity purge ({@code QaFlags.PURGED}, prompt 039): SSE
     * clients, {@code /v1/changes} readers and offline caches delete the entity.
     */
    public boolean purged() {
        return tombstone && ai.foresignal.ingest.QaFlags.has(qaFlags, ai.foresignal.ingest.QaFlags.PURGED);
    }
}
