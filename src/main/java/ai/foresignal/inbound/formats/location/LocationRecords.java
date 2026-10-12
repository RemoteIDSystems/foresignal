package ai.foresignal.inbound.formats.location;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.inbound.format.FormatOptionSpec;
import ai.foresignal.inbound.format.FormatOptions;
import ai.foresignal.inbound.format.IngestRequest;
import ai.foresignal.inbound.format.IngestTimes;
import ai.foresignal.inbound.format.ParsedIngest;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Quality;
import ai.foresignal.source.spi.SourceDescriptor;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.MediaType;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Shared mapping and validation of the personal-location formats (prompt 039).
 *
 * <p>Every fix is checked the same way before it becomes a record of kind {@value #KIND_TRACKER}:
 * a fix without valid coordinates is {@value #INVALID_FIX}, an exact (0,0) is {@value #NULL_ISLAND},
 * one without a time is {@value #MISSING_OBSERVED_AT}, and one whose accuracy is worse than the
 * format option {@value #OPTION_MAX_ACCURACY} (default {@value #DEFAULT_MAX_ACCURACY} m) is
 * {@value #LOW_ACCURACY}. Rejections never fail the request.
 */
public final class LocationRecords {

    /** The kind of every position fix. */
    public static final String KIND_TRACKER = "tracker";

    /** The kind of an OwnTracks region transition. */
    public static final String KIND_TRANSITION = "tracker-transition";

    /** The format option with the worst accepted accuracy, in meters. */
    public static final String OPTION_MAX_ACCURACY = "maxAccuracyMeters";

    /** The default of {@link #OPTION_MAX_ACCURACY}. */
    public static final double DEFAULT_MAX_ACCURACY = 200;

    /** Rejection: the reported accuracy is worse than {@link #OPTION_MAX_ACCURACY}. */
    public static final String LOW_ACCURACY = "LOW_ACCURACY";

    /** Rejection: the app marked the fix invalid, or its coordinates are missing or out of range. */
    public static final String INVALID_FIX = "INVALID_FIX";

    /** Rejection: the fix is exactly (0,0). */
    public static final String NULL_ISLAND = "NULL_ISLAND";

    /** Rejection: the fix carries no time. */
    public static final String MISSING_OBSERVED_AT = "MISSING_OBSERVED_AT";

    /** Rejection: no entity id could be read and the caller is not a device key. */
    public static final String EXTERNAL_ID_INVALID = "EXTERNAL_ID_INVALID";

    /** Rejection: the source does not declare the record's kind. */
    public static final String UNKNOWN_KIND = "UNKNOWN_KIND";

    /** Rejection: an item that is not a readable message. */
    public static final String INVALID_ITEM = "INVALID_ITEM";

    /** The header that names the entity where a format allows it. */
    public static final String EXTERNAL_ID_HEADER = "X-Foresignal-External-Id";

    /** Knots to meters per second. */
    static final double KNOTS_TO_MS = 1852d / 3600d;

    /** Kilometers per hour to meters per second. */
    static final double KMH_TO_MS = 1000d / 3600d;

    private static final DateTimeFormatter SPACE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final MediaType FORM = MediaType.APPLICATION_FORM_URLENCODED;

    private LocationRecords() {}

    /** The {@link #OPTION_MAX_ACCURACY} option spec. */
    static FormatOptionSpec maxAccuracyOption() {
        return new FormatOptionSpec(OPTION_MAX_ACCURACY, "number", false, DEFAULT_MAX_ACCURACY,
                "Fixes with a worse horizontal accuracy (meters) are rejected as LOW_ACCURACY");
    }

    /** The worst accepted accuracy from {@code options}. */
    static double maxAccuracy(FormatOptions options) {
        JsonNode value = options.get(OPTION_MAX_ACCURACY);
        if (value != null && value.isNumber() && value.doubleValue() > 0) {
            return value.doubleValue();
        }
        return DEFAULT_MAX_ACCURACY;
    }

    /**
     * Validates {@code fix} and adds it as a {@value #KIND_TRACKER} record of item {@code index}, or
     * adds the rejection.
     *
     * @return true when a record was added
     */
    static boolean add(int index, LocationFix fix, SourceDescriptor descriptor, double maxAccuracy,
            ParsedIngest.Builder out) {
        String id = fix.externalId;
        if (id == null || id.isBlank()) {
            out.reject(index, null, EXTERNAL_ID_INVALID, "the fix names no device");
            return false;
        }
        if (!declares(descriptor, KIND_TRACKER)) {
            out.reject(index, id, UNKNOWN_KIND, "the source does not declare kind " + KIND_TRACKER);
            return false;
        }
        if (!validCoordinates(fix.lat, fix.lon)) {
            out.reject(index, id, INVALID_FIX, "the fix has no valid coordinates");
            return false;
        }
        if (fix.lat == 0d && fix.lon == 0d) {
            out.reject(index, id, NULL_ISLAND, "the fix is exactly (0,0)");
            return false;
        }
        if (fix.observedAt == null) {
            out.reject(index, id, MISSING_OBSERVED_AT, "the fix carries no time");
            return false;
        }
        if (fix.accuracyMeters != null && fix.accuracyMeters > maxAccuracy) {
            out.reject(index, id, LOW_ACCURACY,
                    "accuracy " + fix.accuracyMeters + " m is worse than " + maxAccuracy + " m");
            return false;
        }
        NormalizedRecord.Builder record = NormalizedRecord.builder(id, KIND_TRACKER)
                .observedAt(fix.observedAt)
                .point(fix.lon, fix.lat)
                .put("speedMs", fix.speedMs)
                .put("courseDeg", fix.courseDeg)
                .put("altitudeM", fix.altitudeM)
                .put("battery", fix.battery)
                .put("charging", fix.charging)
                .put("motion", fix.motion == null ? null : List.copyOf(fix.motion))
                .putAll(fix.extra);
        if (fix.accuracyMeters != null) {
            record.quality(new Quality(fix.accuracyMeters, null, null, Map.of()));
        }
        out.add(index, record.build());
        return true;
    }

    /** True when the source declares {@code kind} (a source with no kinds accepts any). */
    static boolean declares(SourceDescriptor descriptor, String kind) {
        return descriptor.kinds().isEmpty() || descriptor.kinds().contains(kind);
    }

    /** True for finite coordinates within the WGS84 ranges. */
    static boolean validCoordinates(Double lat, Double lon) {
        return lat != null && lon != null && Double.isFinite(lat) && Double.isFinite(lon)
                && lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
    }

    // ------------------------------------------------------------------ JSON

    /** Parses the body as JSON; an empty or malformed body is a 400. */
    static JsonNode readJson(JsonMapper json, IngestRequest request) {
        if (!request.hasBody()) {
            throw new InvalidInputException("The request body is empty.");
        }
        try {
            return json.readTree(request.body());
        } catch (JacksonException e) {
            throw new InvalidInputException("The request body is not valid JSON: " + e.getOriginalMessage(), e);
        }
    }

    /** True when the request should be read as JSON: a JSON content type, or a body starting with {@code {} or {@code [}. */
    static boolean isJson(IngestRequest request) {
        if (!request.hasBody()) {
            return false;
        }
        if (request.contentType() != null) {
            MediaType type = request.contentType();
            if (type.isCompatibleWith(MediaType.APPLICATION_JSON) || type.getSubtype().endsWith("+json")) {
                return true;
            }
            if (type.isCompatibleWith(FORM)) {
                return false;
            }
        }
        String text = request.bodyText().strip();
        return text.startsWith("{") || text.startsWith("[");
    }

    /** A number from a JSON value node (number or numeric string); null when absent or not numeric. */
    static Double number(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        if (node.isNumber()) {
            double value = node.doubleValue();
            return Double.isFinite(value) ? value : null;
        }
        if (node.isString()) {
            return number(node.asString());
        }
        return null;
    }

    /** The text of a JSON value node (numbers as text); null when absent, blank or not a value. */
    static String text(JsonNode node) {
        if (node == null || node.isNull() || !node.isValueNode()) {
            return null;
        }
        String value = node.asString();
        return value == null || value.isBlank() ? null : value.strip();
    }

    /** A boolean from a JSON value node ({@code true}, {@code "true"}, {@code 1}); null when absent. */
    static Boolean bool(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        return bool(text(node));
    }

    /** The member of {@code object} whose name matches {@code name} case-insensitively, or null. */
    static JsonNode memberIgnoreCase(JsonNode object, String name) {
        if (object == null || !object.isObject()) {
            return null;
        }
        JsonNode exact = object.get(name);
        if (exact != null) {
            return exact;
        }
        for (Map.Entry<String, JsonNode> entry : object.properties()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ text values

    /** A finite number from text; null when blank or not numeric. */
    static Double number(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            double value = Double.parseDouble(text.strip());
            return Double.isFinite(value) ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** A boolean from text: {@code true}/{@code 1}/{@code yes} or {@code false}/{@code 0}/{@code no}; null otherwise. */
    static Boolean bool(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return switch (text.strip().toLowerCase(Locale.ROOT)) {
            case "true", "1", "yes" -> Boolean.TRUE;
            case "false", "0", "no" -> Boolean.FALSE;
            default -> null;
        };
    }

    /** {@code value} when it is not negative (apps send -1 for "unknown"), else null. */
    static Double nonNegative(Double value) {
        return value == null || value < 0 ? null : value;
    }

    /** {@code value} scaled by {@code factor}, or null. */
    static Double scaled(Double value, double factor) {
        return value == null ? null : value * factor;
    }

    /** A battery level in percent: values in [0, 1] that are fractional are read as fractions. */
    static Double percent(Double level, boolean fraction) {
        if (level == null || level < 0) {
            return null;
        }
        return fraction && level <= 1d ? level * 100d : level;
    }

    /**
     * A fix time in any form the apps send: epoch seconds, epoch milliseconds (13 or more digits, or a
     * value above 10^11), ISO-8601, or {@code yyyy-MM-dd HH:mm:ss} in UTC.
     *
     * @return the instant, or null when {@code text} is blank
     * @throws InvalidInputException when the text is not a recognized time
     */
    static Instant time(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String value = text.strip();
        Double numeric = number(value);
        if (numeric != null) {
            boolean millis = Math.abs(numeric) >= 1e11;
            return Instant.ofEpochMilli(millis ? numeric.longValue() : (long) (numeric * 1000d));
        }
        if (value.length() == 19 && value.charAt(10) == ' ') {
            try {
                return LocalDateTime.parse(value, SPACE_FORMAT).toInstant(ZoneOffset.UTC);
            } catch (DateTimeException e) {
                throw new InvalidInputException("'" + value + "' is not a timestamp", e);
            }
        }
        return IngestTimes.parse(value);
    }

    // ------------------------------------------------------------------ parameters

    /**
     * The request's parameters: the query string, plus a form body when the request has one (an
     * {@code application/x-www-form-urlencoded} body, or a body that is not JSON). Names are kept as
     * sent; look them up with {@link #param}.
     */
    static Map<String, List<String>> params(IngestRequest request) {
        Map<String, List<String>> out = new LinkedHashMap<>();
        request.query().forEach((name, values) -> out.computeIfAbsent(name, k -> new ArrayList<>()).addAll(values));
        if (request.hasBody() && !isJson(request)) {
            for (String pair : request.bodyText().split("&")) {
                if (pair.isBlank()) {
                    continue;
                }
                int eq = pair.indexOf('=');
                String name = decode(eq < 0 ? pair : pair.substring(0, eq)).strip();
                String value = eq < 0 ? "" : decode(pair.substring(eq + 1));
                if (!name.isEmpty()) {
                    out.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
                }
            }
        }
        return out;
    }

    /** The first non-blank value of the parameter {@code name} (case-insensitive), or null. */
    static String param(Map<String, List<String>> params, String name) {
        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                for (String value : entry.getValue()) {
                    if (value != null && !value.isBlank()) {
                        return value.strip();
                    }
                }
            }
        }
        return null;
    }

    /** The first of {@code names} with a non-blank value, or null. */
    static String param(Map<String, List<String>> params, String... names) {
        for (String name : names) {
            String value = param(params, name);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /** The first non-blank of {@code values}, or null. */
    static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.strip();
            }
        }
        return null;
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return value;
        }
    }
}
