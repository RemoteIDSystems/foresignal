package ai.foresignal.ingest;

import ai.foresignal.source.spi.Observer;
import ai.foresignal.source.spi.Quality;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.locationtech.jts.geom.Geometry;

/**
 * One accepted observation after normalization (Core-Data-Model §2, §3): the time contract applied,
 * limits checked, geometry cleaned, validity resolved and the derived head fields computed.
 *
 * <p>Immutable. TRACK carry-forward happens later, in {@link IngestPolicyEngine#prepare}, because
 * it needs the running state; {@link #withCarriedGeometry} produces that copy.
 *
 * @param index the record's position in the submitted batch
 * @param arrival the record's arrival counter
 * @param externalId the entity
 * @param kind one of the source's kinds
 * @param observedAt when upstream observed this state, after the time base and clamp
 * @param receivedAt when Foresignal received it
 * @param eventTime the discrete event time, or null
 * @param geometry SRID 4326, two-dimensional, valid, or null
 * @param validUntilExplicit the upstream-asserted expiry, null on TRACK sources
 * @param validUntil the effective expiry: explicit, else {@code anchor + ttl}, else null
 * @param validFrom when the asserted validity starts, or null
 * @param payload the complete snapshot as JSON values; empty for a tombstone
 * @param tombstone true when upstream says the entity is gone
 * @param quality never hashed
 * @param observers at most the limit; never hashed
 * @param qaFlags {@link QaFlags} bits
 * @param searchText lowercase search text, at most 1,000 characters, or null
 * @param title the resolved title, or null
 * @param entityKeys entity key type to normalized value; empty when none
 */
public record Observation(
        int index,
        long arrival,
        String externalId,
        String kind,
        Instant observedAt,
        Instant receivedAt,
        Instant eventTime,
        Geometry geometry,
        Instant validUntilExplicit,
        Instant validUntil,
        Instant validFrom,
        Map<String, Object> payload,
        boolean tombstone,
        Quality quality,
        List<Observer> observers,
        int qaFlags,
        String searchText,
        String title,
        Map<String, String> entityKeys) {

    public Observation {
        Objects.requireNonNull(externalId, "externalId");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(observedAt, "observedAt");
        Objects.requireNonNull(receivedAt, "receivedAt");
        payload = payload == null ? Map.of() : payload;
        quality = quality == null ? Quality.none() : quality;
        observers = observers == null ? List.of() : List.copyOf(observers);
        entityKeys = entityKeys == null ? Map.of() : Map.copyOf(entityKeys);
    }

    /** True when {@code flag} is set. */
    public boolean hasFlag(int flag) {
        return QaFlags.has(qaFlags, flag);
    }

    /** This observation with an inherited geometry and {@link QaFlags#POSITION_STALE} set. */
    public Observation withCarriedGeometry(Geometry carried) {
        return new Observation(
                index, arrival, externalId, kind, observedAt, receivedAt, eventTime, carried,
                validUntilExplicit, validUntil, validFrom, payload, tombstone, quality, observers,
                qaFlags | QaFlags.POSITION_STALE, searchText, title, entityKeys);
    }
}
