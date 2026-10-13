package ai.foresignal.derive;

import java.time.Instant;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;

/**
 * One current record of a source, read from {@code record_head ⋈ record_live} (prompt 052): what a
 * generator rebuilds its state from in {@link DerivedGenerator#reconcile}, and what it reads of other
 * sources through {@link DerivedRecords}.
 *
 * @param sourceId the source
 * @param externalId the entity
 * @param kind the kind
 * @param seq the head's seq
 * @param version the per-entity version number
 * @param observedAt the latest accepted observation time ({@code record_live.observed_at})
 * @param tombstone true when the entity is gone
 * @param payload the current payload (empty for a tombstone)
 * @param geometry the live geometry, or null
 */
public record HeadSnapshot(String sourceId, String externalId, String kind, long seq, int version, Instant observedAt,
        boolean tombstone, Map<String, Object> payload, Geometry geometry) {

    /** Copies the payload. */
    public HeadSnapshot {
        payload = payload == null ? Map.of() : java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(payload));
    }
}
