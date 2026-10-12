package ai.foresignal.store.query;

import java.time.Instant;

/**
 * One row of a read query, before mapping to an API shape: a current record (head plus live), a
 * persisted version, an asOf result or a change. Fields a query does not select are null.
 *
 * @param sourceId the source
 * @param externalId the entity id within the source
 * @param kind the kind
 * @param version the per-entity version number
 * @param seq the global seq (the head's for current rows)
 * @param observedAt live observed time for current rows, the version's otherwise
 * @param persistedObservedAt observed time of the persisted version
 * @param eventTime the event time
 * @param validFrom start of validity
 * @param validUntil effective expiry
 * @param tombstone true for a deletion marker
 * @param title the stored title ({@code record_head.title}); null for versions, whose title the
 *     mapper resolves from the payload
 * @param geometry the geometry as GeoJSON or WKT text, per the query's encoding
 * @param payload the payload JSON text (merged, projected or whole), or null when not selected
 * @param titlePayload the whole payload JSON text when {@link #payload} is projected and a title
 *     must be resolved from it
 * @param quality the quality JSON text
 * @param live {@code record_live.dirty} for current rows
 * @param qaFlags the quality bitmask
 * @param distanceMeters geography distance from {@code near}, for {@code sort=distance}
 * @param observer the observer JSON array text, when included
 * @param ingestedAt the batch time of a version
 * @param receivedAt receive time of a version
 * @param persistReason the stored persist reason code
 * @param prevSeq the previous version's seq
 * @param historyTier the stored tier code
 * @param batchId the {@code fetch_log} id of the batch
 * @param bbox {@code [minX, minY, maxX, maxY]} of the geometry, for changes; null otherwise
 * @param prevBbox {@code [minX, minY, maxX, maxY]} of the previous version's geometry, selected only
 *     by the SSE replay ({@code ChangesQuery.prevBoxes}); null otherwise
 */
public record RecordRow(
        String sourceId,
        String externalId,
        String kind,
        int version,
        long seq,
        Instant observedAt,
        Instant persistedObservedAt,
        Instant eventTime,
        Instant validFrom,
        Instant validUntil,
        boolean tombstone,
        String title,
        String geometry,
        String payload,
        String titlePayload,
        String quality,
        Boolean live,
        int qaFlags,
        Double distanceMeters,
        String observer,
        Instant ingestedAt,
        Instant receivedAt,
        Short persistReason,
        Long prevSeq,
        Short historyTier,
        Long batchId,
        double[] bbox,
        double[] prevBbox) {

    /** {@code <source>:<externalId>}. */
    public String id() {
        return sourceId + ":" + externalId;
    }

    /** A copy with {@code payload} replaced. */
    public RecordRow withPayload(String newPayload) {
        return new RecordRow(sourceId, externalId, kind, version, seq, observedAt, persistedObservedAt, eventTime,
                validFrom, validUntil, tombstone, title, geometry, newPayload, titlePayload, quality, live, qaFlags,
                distanceMeters, observer, ingestedAt, receivedAt, persistReason, prevSeq, historyTier, batchId, bbox,
                prevBbox);
    }
}
