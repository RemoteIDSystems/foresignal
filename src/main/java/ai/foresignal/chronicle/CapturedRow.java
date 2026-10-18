package ai.foresignal.chronicle;

import ai.foresignal.ingest.PersistReason;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * One {@code record_version} row as the capture loop reads it (prompt 131): every column, the geometry as WKB and the
 * payload as JSON text (copied into chronicle storage unchanged), and the record's 040 region tag. The geometry and
 * the payload are parsed lazily, only when a layer's area or conditions need them.
 */
public final class CapturedRow {

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final long seq;
    private final Instant ingestedAt;
    private final Instant observedAt;
    private final Instant receivedAt;
    private final Instant eventTime;
    private final Instant validUntil;
    private final Instant validUntilExplicit;
    private final Long prevSeq;
    private final Long batchId;
    private final int version;
    private final short historyTier;
    private final short persistReason;
    private final short qaFlags;
    private final boolean tombstone;
    private final String sourceId;
    private final String externalId;
    private final String kind;
    private final byte[] geomWkb;
    private final String payload;
    private final String quality;
    private final String observer;
    private final Instant validFrom;
    private final String region;
    private Geometry geometry;
    private boolean geometryParsed;
    private Map<String, Object> payloadMap;

    /**
     * Creates a row.
     *
     * @param seq the seq
     * @param ingestedAt the ingest (transaction) time
     * @param observedAt the observation time
     * @param receivedAt the receive time
     * @param eventTime the event time, or null
     * @param validUntil the effective expiry, or null
     * @param validUntilExplicit the explicit expiry, or null
     * @param prevSeq the entity's previous seq, or null
     * @param batchId the batch, or null
     * @param version the per-entity version
     * @param historyTier the tier code
     * @param persistReason the persist reason code
     * @param qaFlags the QA flags
     * @param tombstone whether upstream said it is gone
     * @param sourceId the source
     * @param externalId the entity
     * @param kind the kind
     * @param geomWkb the geometry as WKB, or null
     * @param payload the payload JSON text
     * @param quality the quality JSON text, or null
     * @param observer the observer JSON text, or null
     * @param validFrom the valid-from time, or null
     * @param region the record's 040 region tag ({@code CC} or {@code CC-admin1}), or null
     */
    public CapturedRow(long seq, Instant ingestedAt, Instant observedAt, Instant receivedAt, Instant eventTime,
            Instant validUntil, Instant validUntilExplicit, Long prevSeq, Long batchId, int version, short historyTier,
            short persistReason, short qaFlags, boolean tombstone, String sourceId, String externalId, String kind,
            byte[] geomWkb, String payload, String quality, String observer, Instant validFrom, String region) {
        this.seq = seq;
        this.ingestedAt = ingestedAt;
        this.observedAt = observedAt;
        this.receivedAt = receivedAt;
        this.eventTime = eventTime;
        this.validUntil = validUntil;
        this.validUntilExplicit = validUntilExplicit;
        this.prevSeq = prevSeq;
        this.batchId = batchId;
        this.version = version;
        this.historyTier = historyTier;
        this.persistReason = persistReason;
        this.qaFlags = qaFlags;
        this.tombstone = tombstone;
        this.sourceId = sourceId;
        this.externalId = externalId;
        this.kind = kind;
        this.geomWkb = geomWkb;
        this.payload = payload == null ? "{}" : payload;
        this.quality = quality;
        this.observer = observer;
        this.validFrom = validFrom;
        this.region = region;
    }

    /** A minimal row for tests and in-memory decisions: no geometry, an empty payload. */
    public static CapturedRow of(long seq, String sourceId, String externalId, String kind, PersistReason reason,
            Instant observedAt) {
        return new CapturedRow(seq, observedAt, observedAt, observedAt, null, null, null, null, null, 1, (short) 0,
                reason.code(), (short) 0, reason == PersistReason.TOMBSTONE, sourceId, externalId, kind, null, "{}",
                null, null, null, null);
    }

    /** The seq. */
    public long seq() {
        return seq;
    }

    /** The ingest (transaction) time. */
    public Instant ingestedAt() {
        return ingestedAt;
    }

    /** The observation time. */
    public Instant observedAt() {
        return observedAt;
    }

    /** The receive time. */
    public Instant receivedAt() {
        return receivedAt;
    }

    /** The event time, or null. */
    public Instant eventTime() {
        return eventTime;
    }

    /** The effective expiry, or null. */
    public Instant validUntil() {
        return validUntil;
    }

    /** The explicit expiry, or null. */
    public Instant validUntilExplicit() {
        return validUntilExplicit;
    }

    /** The entity's previous seq, or null. */
    public Long prevSeq() {
        return prevSeq;
    }

    /** The batch, or null. */
    public Long batchId() {
        return batchId;
    }

    /** The per-entity version. */
    public int version() {
        return version;
    }

    /** The tier code. */
    public short historyTier() {
        return historyTier;
    }

    /** The persist reason code. */
    public short persistReasonCode() {
        return persistReason;
    }

    /** The persist reason. */
    public PersistReason persistReason() {
        return PersistReason.fromCode(persistReason);
    }

    /** The QA flags. */
    public short qaFlags() {
        return qaFlags;
    }

    /** True when upstream said the entity is gone. */
    public boolean tombstone() {
        return tombstone;
    }

    /** The source. */
    public String sourceId() {
        return sourceId;
    }

    /** The entity. */
    public String externalId() {
        return externalId;
    }

    /** The kind. */
    public String kind() {
        return kind;
    }

    /** The geometry as WKB, or null. */
    public byte[] geomWkb() {
        return geomWkb;
    }

    /** The payload JSON text. */
    public String payload() {
        return payload;
    }

    /** The quality JSON text, or null. */
    public String quality() {
        return quality;
    }

    /** The observer JSON text, or null. */
    public String observer() {
        return observer;
    }

    /** The valid-from time, or null. */
    public Instant validFrom() {
        return validFrom;
    }

    /** The record's 040 region tag, or null. */
    public String region() {
        return region;
    }

    /** The {@code source\u0000external} key of the entity. */
    public String entityKey() {
        return sourceId + '\u0000' + externalId;
    }

    /** The geometry (SRID 4326), parsed once; null without one or when the WKB is unreadable. */
    public synchronized Geometry geometry() {
        if (!geometryParsed) {
            geometryParsed = true;
            if (geomWkb != null && geomWkb.length > 0) {
                try {
                    geometry = new WKBReader().read(geomWkb);
                    geometry.setSRID(4326);
                } catch (ParseException e) {
                    geometry = null;
                }
            }
        }
        return geometry;
    }

    /** The payload as a map, parsed once; empty when it is not an object. */
    public synchronized Map<String, Object> payloadMap(JsonMapper json) {
        if (payloadMap == null) {
            try {
                payloadMap = json.readValue(payload, MAP);
            } catch (RuntimeException e) {
                payloadMap = new LinkedHashMap<>();
            }
            if (payloadMap == null) {
                payloadMap = new LinkedHashMap<>();
            }
        }
        return payloadMap;
    }

    /**
     * The estimated bytes this row occupies once captured: a fixed per-row overhead plus the payload, geometry,
     * quality and observer sizes. Deterministic, so the cap is enforced the same way on every instance.
     */
    public long estimatedBytes() {
        long bytes = ChronicleSizes.ROW_OVERHEAD + payload.length();
        bytes += geomWkb == null ? 0 : geomWkb.length;
        bytes += quality == null ? 0 : quality.length();
        bytes += observer == null ? 0 : observer.length();
        return bytes;
    }
}
