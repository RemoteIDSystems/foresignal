package ai.foresignal.inbound.edp.binding;

import ai.foresignal.inbound.edp.EdpItemMapper;
import ai.foresignal.object.ObjectRef;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * One stored version of an EDP item as the binding router sees it (prompt 138): the record version and the item's
 * provenance from the payload ({@code edp}) and its receipt and effective availability times ({@code edpTimes},
 * invariant EXT-4).
 *
 * @param seq the version's seq
 * @param sourceId the provider ({@code ext-…})
 * @param externalId the record's external id ({@code {stream}:{id}})
 * @param kind the record kind (the stream's kind)
 * @param version the record version, or null
 * @param observedAt the version's {@code observed_at}, or null
 * @param eventTime the version's {@code event_time}, or null
 * @param ingestedAt when the version was stored, or null
 * @param lon the representative longitude, or null
 * @param lat the representative latitude, or null
 * @param payload the payload
 */
public record BoundItem(long seq, String sourceId, String externalId, String kind, Integer version,
        Instant observedAt, Instant eventTime, Instant ingestedAt, Double lon, Double lat,
        Map<String, Object> payload) {

    /** Copies the payload reference (never null). */
    public BoundItem {
        payload = payload == null ? Map.of() : payload;
    }

    /** The stream id: the {@code edp.stream} provenance, else the part of the external id before {@code :}. */
    public String streamId() {
        Object stream = edp().get("stream");
        if (stream != null) {
            return String.valueOf(stream);
        }
        int colon = externalId.indexOf(':');
        return colon < 0 ? externalId : externalId.substring(0, colon);
    }

    /** The provider's item id (the external id after {@code {stream}:}). */
    public String itemId() {
        String prefix = streamId() + ":";
        return externalId.startsWith(prefix) ? externalId.substring(prefix.length()) : externalId;
    }

    /** True when the payload carries EDP provenance (records the bindings themselves wrote do not). */
    public boolean edpItem() {
        return payload.get(EdpItemMapper.PAYLOAD_EDP) instanceof Map<?, ?>;
    }

    /** The item type ({@code edp.itemType}), or null. */
    public String itemType() {
        return text(edp().get("itemType"));
    }

    /** The originator ({@code origin.originator}, else the manifest's), or null. */
    public String originator() {
        return text(edp().get("originator"));
    }

    /** The originator class, or null. */
    public String originatorClass() {
        return text(edp().get("originatorClass"));
    }

    /** The upstream id ({@code origin.upstreamId} or an event's {@code upstreamEventId}), or null. */
    public String upstreamId() {
        return text(edp().get("upstreamId"));
    }

    /** The originators the manifest's originator republishes. */
    public List<String> aggregates() {
        Object value = edp().get("aggregates");
        if (value instanceof List<?> list) {
            return list.stream().filter(java.util.Objects::nonNull).map(String::valueOf).toList();
        }
        return List.of();
    }

    /** When Foresignal received the item ({@code edpTimes.receivedAt}), else when it was stored. */
    public Instant receivedAt() {
        Instant received = time(times().get("receivedAt"));
        return received != null ? received : ingestedAt;
    }

    /**
     * An effective availability time ({@code effectiveKnownAt}, {@code effectivePublishedAt} or
     * {@code effectiveMadeAt}), never earlier than receipt; receipt when the item carries none.
     */
    public Instant effective(String name) {
        Instant value = time(times().get(name));
        Instant received = receivedAt();
        if (value == null) {
            return received;
        }
        return received != null && value.isBefore(received) ? received : value;
    }

    /** A top-level payload value as text, or null. */
    public String text(String field) {
        return text(payload.get(field));
    }

    /** The version's {@code ObjectRef} ({@code record:{sourceId}/{externalId}@{seq}}). */
    public String ref() {
        return ObjectRef.record(sourceId, externalId, seq).format();
    }

    /** The record ref without the seq ({@code {sourceId}/{externalId}}). */
    public String recordKey() {
        return sourceId + "/" + externalId;
    }

    /** True when the version has a representative point. */
    public boolean located() {
        return lon != null && lat != null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> edp() {
        return payload.get(EdpItemMapper.PAYLOAD_EDP) instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> times() {
        return payload.get(EdpItemMapper.PAYLOAD_TIMES) instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
    }

    private static String text(Object value) {
        return value == null || value instanceof Map<?, ?> || value instanceof List<?> || String.valueOf(value).isBlank()
                ? null : String.valueOf(value);
    }

    /** Parses an RFC 3339 time, or null. */
    static Instant time(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(String.valueOf(value)).toInstant();
        } catch (java.time.DateTimeException e) {
            try {
                return Instant.parse(String.valueOf(value));
            } catch (java.time.DateTimeException ignored) {
                return null;
            }
        }
    }
}
