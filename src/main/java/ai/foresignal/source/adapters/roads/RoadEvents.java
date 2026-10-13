package ai.foresignal.source.adapters.roads;

import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.MissingNode;

/**
 * Shared pieces of the road-event parsers (prompt 074): the record shape, JSON access, timestamps and geometry.
 *
 * <p>Every record has {@code kind = road-event}, {@code observedAt} = the format's update time, {@code eventTime =
 * null} (road events are periods, not instants), {@code validFrom} = the scheduled start when the upstream gives one,
 * and {@code validUntil} = the upstream-asserted end of the last schedule interval only, never computed
 * (Core-Data-Model §2). A geometry over {@value FeatureGeometry#MAX_VERTICES} vertices is simplified
 * ({@link FeatureGeometry#fit}) and the payload then carries {@code geometrySimplified} and
 * {@code simplifyToleranceDeg}.
 */
public final class RoadEvents {

    /** The kind every road-event source emits. */
    public static final String KIND = "road-event";

    /** The shared JSON reader. */
    static final JsonMapper JSON = JsonMapper.builder().build();

    private RoadEvents() {}

    /**
     * A {@code road-event} record.
     *
     * @param id the external id
     * @param observedAt the format's update time
     * @param geometry the converted geometry, or null
     * @param validFrom the scheduled start, or null
     * @param validUntil the upstream-asserted end of the last interval, or null
     * @param payload the payload
     * @return the record; when the upstream's start is after its end, {@code validFrom} is left out so the record is
     *     not rejected as an inverted range (the payload keeps both times)
     */
    public static NormalizedRecord record(String id, Instant observedAt, FeatureGeometry.Result geometry,
            Instant validFrom, Instant validUntil, RoadEventPayload payload) {
        Instant from = validFrom != null && validUntil != null && validFrom.isAfter(validUntil) ? null : validFrom;
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, KIND)
                .observedAt(observedAt)
                .validFrom(from)
                .validUntil(validUntil)
                .putAll(payload.toMap());
        if (geometry != null && geometry.geometry() != null) {
            b.geometry(geometry.geometry());
            if (geometry.simplified()) {
                b.put(FeatureGeometry.SIMPLIFIED_KEY, true);
                b.put(FeatureGeometry.TOLERANCE_KEY, geometry.toleranceDeg());
            }
        }
        return b.build();
    }

    /**
     * Parses {@code body} as JSON.
     *
     * @param what names the document in the error
     * @throws UpstreamFormatException when the body is empty or not JSON
     */
    static JsonNode readJson(byte[] body, String what) {
        try {
            JsonNode root = JSON.readTree(body);
            if (root == null || root.isMissingNode() || root.isNull()) {
                throw new UpstreamFormatException(what + " is empty", "");
            }
            return root;
        } catch (JacksonException e) {
            throw new UpstreamFormatException(what + " is not valid JSON", "", e);
        }
    }

    /**
     * Converts a GeoJSON geometry member.
     *
     * @return the geometry ({@link FeatureGeometry.Result#NONE} when absent), or null when it is malformed
     */
    static FeatureGeometry.Result geometry(JsonNode node) {
        try {
            return FeatureGeometry.fromGeoJson(node);
        } catch (UpstreamFormatException e) {
            return null;
        }
    }

    /** A stripped, non-blank string or number member; null otherwise. */
    static String text(JsonNode node, String field) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || !(value.isString() || value.isNumber())) {
            return null;
        }
        String text = value.asString().strip();
        return text.isEmpty() ? null : text;
    }

    /** A finite number from a number or numeric string member; null otherwise. */
    static Double number(JsonNode node, String field) {
        if (node == null || node.isNull()) {
            return null;
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isNumber()) {
            double d = value.asDouble();
            return Double.isFinite(d) ? d : null;
        }
        if (value.isString()) {
            try {
                double d = Double.parseDouble(value.asString().strip());
                return Double.isFinite(d) ? d : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /** The member {@code field} of {@code node}, or a missing node. */
    static JsonNode path(JsonNode node, String... fields) {
        JsonNode current = node;
        for (String field : fields) {
            if (current == null || current.isNull() || current.isMissingNode()) {
                return MissingNode.getInstance();
            }
            current = current.path(field);
        }
        return current == null ? MissingNode.getInstance() : current;
    }

    /**
     * An instant from ISO-8601 text: with an offset or {@code Z}, a local date-time read as UTC, or a date read as
     * midnight UTC.
     *
     * @return the instant, or null when blank or unreadable
     */
    static Instant instant(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String value = text.strip();
        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (DateTimeParseException ignored) {
            // fall through
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ignored) {
            // fall through
        }
        try {
            return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) {
            // fall through
        }
        try {
            return LocalDate.parse(value).atStartOfDay().toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
