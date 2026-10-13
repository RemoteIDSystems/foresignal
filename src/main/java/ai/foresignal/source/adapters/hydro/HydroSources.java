package ai.foresignal.source.adapters.hydro;

import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import io.micrometer.core.instrument.MeterRegistry;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.ObjectProvider;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * What the prompt 079 hydrology and coast sources share: the test base-URL property, the skip counter, the
 * sentinel scrubbing (Core-Data-Model §3: {@code -999}, {@code -9999} and {@code 0001-01-01T00:00:00Z} become omitted
 * fields), refresh bookkeeping for daily metadata, the box-size check and the field-spec helpers. Not an adapter;
 * the adapters under {@code hydro.*} use it (and the general helpers of {@link EnvSources}) and never import each
 * other.
 */
public final class HydroSources {

    /**
     * The test-only property moving every prompt 079 URL under one base as {@code {base}/{host}{path}}; blank in
     * production.
     */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.hydro.base-url";

    /** The counter of skipped entries, tagged {@code source} and {@code reason}. */
    public static final String SKIPPED_METRIC = "foresignal.hydro.skipped";

    /** The description of {@link #SKIPPED_METRIC}. */
    public static final String SKIPPED_DESCRIPTION = "Hydrology and coast entries the parsers skipped";

    /** Raw retention of gauge, snow and tide readings: the {@code state} class (365 d). */
    public static final Duration STATE_RETENTION = Duration.ofDays(365);

    /** Raw retention of drought maps and flood warnings: the {@code event} class (1825 d). */
    public static final Duration EVENT_RETENTION = Duration.ofDays(1825);

    /** The JSON mapper of upstream documents. */
    public static final JsonMapper JSON = JsonMapper.builder().build();

    private HydroSources() {}

    /**
     * {@code url} moved under {@code baseOverride} as {@code {base}/{host}{path}}, or unchanged when the override
     * is blank.
     */
    public static URI uri(String url, String baseOverride) {
        return EnvSources.uri(url, baseOverride);
    }

    /** The lazily resolved meter registry of a Spring-built adapter. */
    public static Supplier<MeterRegistry> meters(ObjectProvider<MeterRegistry> provider) {
        return provider == null ? () -> null : provider::getIfAvailable;
    }

    /** True for the numeric sentinels {@code -999} and {@code -9999}. */
    public static boolean sentinel(double value) {
        return value == -999.0 || value == -9999.0;
    }

    /**
     * A number from a JSON number or numeric text, or null when the node is missing, null, not numeric, not finite
     * or a sentinel.
     */
    public static Double number(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode() || node.isContainer()) {
            return null;
        }
        double value;
        if (node.isNumber()) {
            value = node.doubleValue();
        } else {
            String text = node.asString();
            if (text == null || text.isBlank()) {
                return null;
            }
            try {
                value = new BigDecimal(text.strip()).doubleValue();
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return Double.isFinite(value) && !sentinel(value) ? value : null;
    }

    /** The trimmed text of a node, or null when it is missing, null, a container or blank. */
    public static String text(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode() || node.isContainer()) {
            return null;
        }
        String text = node.asString();
        return text == null || text.isBlank() ? null : text.strip();
    }

    /**
     * An ISO-8601 instant with an offset ({@code Z} or {@code +00:00}), or null when blank, unreadable or the year-1
     * sentinel ({@code 0001-01-01…}).
     */
    public static Instant instant(String text) {
        if (text == null || text.isBlank() || text.strip().startsWith("0001-01-01")) {
            return null;
        }
        try {
            return OffsetDateTime.parse(text.strip()).toInstant();
        } catch (DateTimeParseException e) {
            try {
                return Instant.parse(text.strip());
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }

    /**
     * Parses a JSON body.
     *
     * @param what what is being read, for messages
     * @throws UpstreamFormatException when the body is not JSON, or is an {@code {"error": {"message": …}}} object
     *     (CO-OPS, ArcGIS)
     */
    public static JsonNode json(byte[] body, String what) {
        JsonNode root;
        try {
            root = JSON.readTree(body == null ? new byte[0] : body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException(what + " is not JSON: " + e.getOriginalMessage(), "", e);
        }
        if (root == null || root.isMissingNode() || root.isNull()) {
            throw new UpstreamFormatException(what + " is empty", "");
        }
        JsonNode error = root.isObject() ? root.get("error") : null;
        if (error != null && error.isObject()) {
            String message = text(error.get("message"));
            throw new UpstreamFormatException(what + " returned an error: " + (message == null ? error : message),
                    "");
        }
        return root;
    }

    /**
     * A problem with a box parameter, or null when it is valid.
     *
     * @param box the box
     * @param maxDegrees the largest width and height, degrees
     */
    public static String boxProblem(Envelope box, double maxDegrees) {
        if (box == null || box.isNull()) {
            return "a box is required (west,south,east,north)";
        }
        if (box.getWidth() > maxDegrees || box.getHeight() > maxDegrees) {
            return "at most " + fmt(maxDegrees) + "° × " + fmt(maxDegrees) + "°, but the box is " + fmt(box.getWidth())
                    + "° × " + fmt(box.getHeight()) + "°";
        }
        return null;
    }

    /** {@code box} as {@code [minLon, minLat, maxLon, maxLat]}. */
    public static java.util.List<Double> bboxList(Envelope box) {
        return java.util.List.of(box.getMinX(), box.getMinY(), box.getMaxX(), box.getMaxY());
    }

    private static String fmt(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    /**
     * True when the epoch-second stamp under {@code key} is missing, unreadable, or at least {@code period} before
     * {@code now}.
     */
    public static boolean due(AdapterStateStore state, String key, Instant now, Duration period) {
        return due(state.get(key), now, period);
    }

    /** True when {@code at} (epoch seconds) is missing, unreadable, or at least {@code period} before {@code now}. */
    public static boolean due(Optional<String> at, Instant now, Duration period) {
        try {
            return at.isEmpty() || !Instant.ofEpochSecond(Long.parseLong(at.get().strip())).plus(period).isAfter(now);
        } catch (NumberFormatException e) {
            return true;
        }
    }

    /** Stores {@code now} as epoch seconds under {@code key}. */
    public static void stamp(AdapterStateStore state, String key, Instant now) {
        state.put(key, Long.toString(now.getEpochSecond()));
    }

    /** Declares a text field. */
    public static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }

    /**
     * Declares a number field.
     *
     * @param unit the UCUM unit, or null when it varies per record (then carried in a sibling unit field)
     */
    public static void number(SourceDescriptor.Builder b, String key, String unit, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.NUMBER, unit, label, description, null, Set.of()));
    }

    /** Declares a number field measured against a vertical datum. */
    public static void height(SourceDescriptor.Builder b, String key, String unit, String datum, String label,
            String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.NUMBER, unit, label, description, datum, Set.of()));
    }

    /** Declares an integer field. */
    public static void integer(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.INTEGER, null, label, description, null, Set.of()));
    }

    /** Declares a boolean field. */
    public static void bool(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.BOOLEAN, null, label, description, null, Set.of()));
    }

    /** Declares a timestamp field. */
    public static void timestamp(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.TIMESTAMP, null, label, description, null, Set.of()));
    }
}
