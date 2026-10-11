package ai.foresignal.stream;

import java.time.Instant;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;

/**
 * A live-only change of one entity in one batch: content moved without a new version
 * (Core-Data-Model §10.1).
 *
 * @param sourceId the source
 * @param externalId the entity
 * @param kind the entity's kind
 * @param observedAt the latest accepted {@code observed_at}
 * @param validUntil the effective expiry, or null for never
 * @param version the entity's current persisted version
 * @param geometry the live geometry, or null
 * @param bbox the live geometry's box, or null
 * @param prevBbox the live geometry's box before this batch, or null
 * @param properties the kinematic overlay on the persisted payload; a null value means the key was
 *     removed. A live update never changes significant keys
 * @param quality the latest quality, or null
 */
public record LiveUpdate(
        String sourceId,
        String externalId,
        String kind,
        Instant observedAt,
        Instant validUntil,
        int version,
        Geometry geometry,
        Bbox bbox,
        Bbox prevBbox,
        Map<String, Object> properties,
        Map<String, Object> quality) {}
