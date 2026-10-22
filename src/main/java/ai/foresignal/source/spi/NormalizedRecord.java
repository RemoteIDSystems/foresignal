package ai.foresignal.source.spi;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;

/**
 * One observation of one upstream entity, in Foresignal's own shape (Core-Data-Model §3).
 *
 * <p><strong>The payload is a complete snapshot</strong> (adapter rule 7). Protocols that split an
 * entity's state across messages — AIS, MQTT — must merge per entity before emitting, because
 * hashing and the ingest policies both compare whole payloads. A partial payload would read as
 * "everything else was removed".
 *
 * @param externalId unique within the source, stable across observations of the same entity
 * @param kind one of the source's declared kinds
 * @param observedAt when upstream last observed this state; required unless the source declares
 *     {@link ObservedAtMode#RECEIVED}
 * @param eventTime when a discrete real-world event happened, or the measurement period; null for
 *     continuous state (adapter rule 10)
 * @param geometry JTS geometry in SRID 4326, nullable
 * @param validUntil <strong>only</strong> an upstream-asserted expiry. Not a place to pre-compute
 *     the descriptor TTL — the framework does that from {@link TtlAnchor} — and TRACK sources must
 *     leave it null (adapter rule 9)
 * @param validFrom when the asserted validity starts, for example an alert's onset; null when
 *     valid immediately. A record whose {@code validFrom} is after its {@code validUntil} is
 *     rejected by the normalizer ({@code VALID_RANGE_INVERTED})
 * @param payload the complete snapshot, JSON-serializable, slim and allowlisted (rule 8)
 * @param tombstone true when upstream says this entity is gone
 * @param quality position and value trust, never hashed
 * @param observers the receivers that contributed, at most 8
 */
public record NormalizedRecord(
        String externalId,
        String kind,
        Instant observedAt,
        Instant eventTime,
        Geometry geometry,
        Instant validUntil,
        Instant validFrom,
        Map<String, Object> payload,
        boolean tombstone,
        Quality quality,
        List<Observer> observers) {

    public NormalizedRecord {
        if (externalId == null || externalId.isBlank()) {
            throw new IllegalArgumentException("NormalizedRecord needs an externalId");
        }
        if (kind == null || kind.isBlank()) {
            throw new IllegalArgumentException(
                    "NormalizedRecord '" + externalId + "' needs a kind");
        }
        payload = payload == null ? Map.of() : Map.copyOf(payload);
        observers = observers == null ? List.of() : List.copyOf(observers);
        quality = quality == null ? Quality.none() : quality;
    }

    public static Builder builder(String externalId, String kind) {
        return new Builder(externalId, kind);
    }

    /** A tombstone, stored with no geometry and an empty payload. */
    public static NormalizedRecord tombstone(String externalId, String kind, Instant observedAt) {
        return new NormalizedRecord(
                externalId, kind, observedAt, null, null, null, null, Map.of(), true, Quality.none(),
                List.of());
    }

    /** Fluent construction, so an adapter sets only what its upstream actually provides. */
    public static final class Builder {

        private final String externalId;
        private final String kind;
        private final Map<String, Object> payload = new LinkedHashMap<>();
        private Instant observedAt;
        private Instant eventTime;
        private Geometry geometry;
        private Instant validUntil;
        private Instant validFrom;
        private boolean tombstone;
        private Quality quality = Quality.none();
        private List<Observer> observers = List.of();

        private Builder(String externalId, String kind) {
            this.externalId = externalId;
            this.kind = kind;
        }

        public Builder observedAt(Instant observedAt) {
            this.observedAt = observedAt;
            return this;
        }

        public Builder eventTime(Instant eventTime) {
            this.eventTime = eventTime;
            return this;
        }

        public Builder geometry(Geometry geometry) {
            this.geometry = geometry;
            return this;
        }

        /** Sets a point in SRID 4326. */
        public Builder point(double longitude, double latitude) {
            this.geometry = Geo.point(longitude, latitude);
            return this;
        }

        /** An upstream-asserted expiry only; see {@link NormalizedRecord#validUntil()}. */
        public Builder validUntil(Instant validUntil) {
            this.validUntil = validUntil;
            return this;
        }

        /** When the asserted validity starts; see {@link NormalizedRecord#validFrom()}. */
        public Builder validFrom(Instant validFrom) {
            this.validFrom = validFrom;
            return this;
        }

        /** Adds one payload entry. Null values are dropped, per adapter rule 8. */
        public Builder put(String key, Object value) {
            if (value != null) {
                payload.put(key, value);
            }
            return this;
        }

        /** Adds every non-null entry of {@code values}. */
        public Builder putAll(Map<String, ?> values) {
            if (values != null) {
                values.forEach(this::put);
            }
            return this;
        }

        public Builder tombstone(boolean tombstone) {
            this.tombstone = tombstone;
            return this;
        }

        public Builder quality(Quality quality) {
            this.quality = quality == null ? Quality.none() : quality;
            return this;
        }

        public Builder observers(List<Observer> observers) {
            this.observers = observers == null ? List.of() : List.copyOf(observers);
            return this;
        }

        public NormalizedRecord build() {
            return new NormalizedRecord(
                    externalId,
                    kind,
                    observedAt,
                    eventTime,
                    geometry,
                    validUntil,
                    validFrom,
                    payload,
                    tombstone,
                    quality,
                    observers);
        }
    }
}
