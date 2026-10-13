package ai.foresignal.source.adapters.amtraker;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses Amtraker's {@code /v3/trains} (prompt 080): an object keyed by train number whose values are arrays of
 * trains (one per running instance of that number).
 *
 * <p>A pure function of its input (adapter rule 3).
 *
 * <ul>
 *   <li><strong>Identity:</strong> {@code externalId = trainID} ({@code {number}-{day of month it originated}}, for
 *       example {@code 1-12}; VIA and Brightline numbers carry a {@code v} or {@code b} prefix).
 *   <li><strong>Time base:</strong> {@code observedAt = lastValTS}, the time of the last valid position, fixed
 *       while the train reports nothing new, so the 5-minute TTL retires a silent train.
 *   <li><strong>Geometry:</strong> {@code lat}/{@code lon}, rounded to 6 decimals. A train without a valid
 *       position, {@code trainID} or {@code lastValTS} is skipped and counted.
 *   <li><strong>{@code Predeparture} trains are skipped and counted separately:</strong> their coordinates are the
 *       origin station and their {@code lastValTS} the scheduled departure (verified 2026-10-13: 19 of 218 trains,
 *       {@code lastValTS} up to 40 minutes ahead of the capture), so they carry no observed position.
 *   <li><strong>Payload:</strong> {@code trainNumber} ({@code trainNum}), {@code route} ({@code routeName}),
 *       {@code provider}, {@code state} ({@code trainState}), {@code timeliness} ({@code trainTimely}, omitted when
 *       blank), {@code nextStationCode} ({@code eventCode}), {@code originCode} ({@code origCode}),
 *       {@code destinationCode} ({@code destCode}), {@code speedKph} ({@code velocity} mph × 1.609344, 0.1) and
 *       {@code headingDeg} (the eight-point compass text as degrees). {@code stations[]}, {@code alerts[]}, colors,
 *       time zones, names and the other fields are dropped.
 * </ul>
 */
public final class AmtrakerParser {

    /** The record kind. */
    public static final String KIND = "train";

    /** Kilometers per mile. */
    public static final double KM_PER_MILE = 1.609344;

    /** The {@code trainState} of a train that has not left its origin. */
    public static final String PREDEPARTURE = "Predeparture";

    /** Compass text → degrees (eight points, and the sixteen-point names for safety). */
    static final Map<String, Double> COMPASS = Map.ofEntries(
            Map.entry("N", 0.0), Map.entry("NNE", 22.5), Map.entry("NE", 45.0), Map.entry("ENE", 67.5),
            Map.entry("E", 90.0), Map.entry("ESE", 112.5), Map.entry("SE", 135.0), Map.entry("SSE", 157.5),
            Map.entry("S", 180.0), Map.entry("SSW", 202.5), Map.entry("SW", 225.0), Map.entry("WSW", 247.5),
            Map.entry("W", 270.0), Map.entry("WNW", 292.5), Map.entry("NW", 315.0), Map.entry("NNW", 337.5));

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private AmtrakerParser() {}

    /**
     * The records of a document and what was left out.
     *
     * @param records the trains with an observed position, in document order
     * @param skipped trains without a usable position, identity or time
     * @param predeparture trains skipped because they have not departed
     */
    public record Result(List<NormalizedRecord> records, int skipped, int predeparture) {

        /** Copies the list. */
        public Result {
            records = List.copyOf(records);
        }
    }

    /**
     * Parses {@code /v3/trains}.
     *
     * @throws UpstreamFormatException when the body is not a JSON object of arrays
     */
    public static Result parse(byte[] body) {
        JsonNode root = read(body);
        if (!root.isObject()) {
            throw new UpstreamFormatException("Amtraker /v3/trains is not a JSON object", snippet(body));
        }
        List<NormalizedRecord> records = new ArrayList<>();
        int skipped = 0;
        int predeparture = 0;
        for (Map.Entry<String, JsonNode> number : root.properties()) {
            if (!number.getValue().isArray()) {
                throw new UpstreamFormatException("Amtraker train " + number.getKey() + " is not an array",
                        snippet(body));
            }
            for (JsonNode train : number.getValue()) {
                if (PREDEPARTURE.equals(text(train, "trainState"))) {
                    predeparture++;
                    continue;
                }
                NormalizedRecord record = record(train);
                if (record == null) {
                    skipped++;
                } else {
                    records.add(record);
                }
            }
        }
        return new Result(records, skipped, predeparture);
    }

    /** One train's record, or null when it lacks a usable position, identity or time. */
    static NormalizedRecord record(JsonNode train) {
        if (train == null || !train.isObject()) {
            return null;
        }
        String id = text(train, "trainID");
        Instant observedAt = instant(text(train, "lastValTS"));
        JsonNode lat = train.get("lat");
        JsonNode lon = train.get("lon");
        if (id == null || observedAt == null || lat == null || lon == null || !lat.isNumber() || !lon.isNumber()) {
            return null;
        }
        double y = lat.doubleValue();
        double x = lon.doubleValue();
        if (!Double.isFinite(x) || !Double.isFinite(y) || x < -180 || x > 180 || y < -90 || y > 90
                || (x == 0.0 && y == 0.0)) {
            return null;
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, KIND)
                .observedAt(observedAt)
                .point(round(x, 6), round(y, 6));
        putText(b, "trainNumber", text(train, "trainNum"));
        putText(b, "route", text(train, "routeName"));
        putText(b, "provider", text(train, "provider"));
        putText(b, "state", text(train, "trainState"));
        putText(b, "timeliness", text(train, "trainTimely"));
        putText(b, "nextStationCode", text(train, "eventCode"));
        putText(b, "originCode", text(train, "origCode"));
        putText(b, "destinationCode", text(train, "destCode"));
        JsonNode velocity = train.get("velocity");
        if (velocity != null && velocity.isNumber() && Double.isFinite(velocity.doubleValue())
                && velocity.doubleValue() >= 0) {
            b.put("speedKph", round(velocity.doubleValue() * KM_PER_MILE, 1));
        }
        Double heading = headingDegrees(text(train, "heading"));
        if (heading != null) {
            b.put("headingDeg", heading);
        }
        return b.build();
    }

    /** The degrees of a compass text, or null when it is not a compass point. */
    public static Double headingDegrees(String compass) {
        return compass == null ? null : COMPASS.get(compass.trim().toUpperCase(Locale.ROOT));
    }

    /** An ISO-8601 time with an offset or {@code Z}, or null. */
    static Instant instant(String text) {
        if (text == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(text).toInstant();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static void putText(NormalizedRecord.Builder b, String key, String value) {
        if (value != null) {
            b.put(key, value);
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        if (value == null || !value.isString()) {
            return null;
        }
        String text = value.asString().trim();
        return text.isEmpty() ? null : text;
    }

    static double round(double value, int decimals) {
        return BigDecimal.valueOf(value).setScale(decimals, RoundingMode.HALF_UP).doubleValue();
    }

    private static JsonNode read(byte[] body) {
        if (body == null || body.length == 0) {
            throw new UpstreamFormatException("Amtraker response is empty", "");
        }
        try {
            return JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("Amtraker response is not JSON: " + e.getOriginalMessage(),
                    snippet(body), e);
        }
    }

    private static String snippet(byte[] body) {
        return new String(body, 0, Math.min(body.length, 500), StandardCharsets.UTF_8);
    }
}
