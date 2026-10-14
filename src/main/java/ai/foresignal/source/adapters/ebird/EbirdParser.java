package ai.foresignal.source.adapters.ebird;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PlaceTimeZones;
import ai.foresignal.source.spi.Quality;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses eBird API 2.0 {@code /data/obs/geo/recent/notable} and {@code /data/obs/geo/recent} responses into
 * {@code bird-sighting} records (prompt 095).
 *
 * <p>A pure function of its inputs (adapter rule 3). eBird sends {@code obsDt} as a local wall-clock time with
 * no zone ("2024-05-01 08:30"); {@link #eventTime} converts it to an instant with the 040 timezone at the
 * point (UTC when none is known), and the raw local text is kept as the {@code obsDtLocal} payload field.
 *
 * <p><strong>Privacy.</strong> When {@code locationPrivate} is true (often a home), the coordinates are rounded
 * to {@value #PRIVATE_DEGREES}° and the record carries {@link Quality#accuracyMeters()} of
 * {@value #PRIVATE_ACCURACY_METERS}; {@code locName} is omitted entirely. Only a bird's {@code subId},
 * {@code speciesCode}, common and scientific names, counts and review flags are read.
 */
public final class EbirdParser {

    /** The record kind. */
    public static final String KIND = "bird-sighting";

    /** The coordinate decimals kept for a private location. */
    public static final double PRIVATE_DEGREES = 0.05;

    /** The accuracy reported for a rounded private location, metres. */
    public static final double PRIVATE_ACCURACY_METERS = 5000;

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final Pattern CONTROL = Pattern.compile("\\p{Cntrl}");
    private static final DateTimeFormatter OBS_DT = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm", Locale.ROOT);
    private static final DateTimeFormatter OBS_DATE = DateTimeFormatter.ofPattern("uuuu-MM-dd", Locale.ROOT);

    private EbirdParser() {}

    /**
     * One parsed response.
     *
     * @param records the kept sightings, in response order
     * @param rows the length of the array, kept or not
     * @param skipped rows dropped for a missing species code, subId or observation time
     */
    public record Result(List<NormalizedRecord> records, int rows, int skipped) {

        /** Copies the records. */
        public Result {
            records = List.copyOf(records);
        }
    }

    /**
     * Parses one response array.
     *
     * @param json the response body
     * @param zones the timezone lookup for the local {@code obsDt}; never null
     * @return the records and counts
     * @throws UpstreamFormatException when the body is empty, not JSON, or not a JSON array
     */
    public static Result parse(byte[] json, PlaceTimeZones zones) {
        if (json == null || json.length == 0) {
            throw new UpstreamFormatException("eBird response is empty", "");
        }
        JsonNode root;
        try {
            root = JSON.readTree(json);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("eBird response is not valid JSON: " + e.getOriginalMessage(),
                    snippet(json), e);
        }
        if (root == null || !root.isArray()) {
            throw new UpstreamFormatException("eBird response is not a JSON array", snippet(json));
        }
        List<NormalizedRecord> records = new ArrayList<>();
        int skipped = 0;
        for (JsonNode row : root) {
            NormalizedRecord record = sighting(row, zones);
            if (record == null) {
                skipped++;
            } else {
                records.add(record);
            }
        }
        return new Result(records, root.size(), skipped);
    }

    private static NormalizedRecord sighting(JsonNode row, PlaceTimeZones zones) {
        if (row == null || !row.isObject()) {
            return null;
        }
        String speciesCode = text(row.get("speciesCode"));
        String subId = text(row.get("subId"));
        if (speciesCode == null || subId == null) {
            return null;
        }
        Double lat = number(row.get("lat"));
        Double lon = number(row.get("lng"));
        if (lon == null) {
            lon = number(row.get("lon"));
        }
        boolean privateLocation = truthy(row.get("locationPrivate"));
        double[] point = null;
        if (valid(lon, lat)) {
            point = privateLocation ? new double[] {round(lon), round(lat)} : new double[] {lon, lat};
        }
        String obsDt = text(row.get("obsDt"));
        Instant eventTime = eventTime(obsDt, point, zones);

        NormalizedRecord.Builder b = NormalizedRecord.builder("ebird:" + subId + ":" + speciesCode, KIND)
                .eventTime(eventTime);
        if (point != null) {
            b.point(point[0], point[1]);
        }
        b.put("speciesCode", speciesCode);
        b.put("comName", text(row.get("comName")));
        b.put("sciName", text(row.get("sciName")));
        b.put("howMany", integral(row.get("howMany")));
        b.put("obsValid", bool(row.get("obsValid")));
        b.put("obsReviewed", bool(row.get("obsReviewed")));
        b.put("locId", locId(row));
        b.put("obsDtLocal", obsDt);
        if (!privateLocation) {
            b.put("locName", text(row.get("locName")));
        }
        if (privateLocation) {
            b.quality(new Quality(PRIVATE_ACCURACY_METERS, null, null, Map.of()));
        }
        return b.build();
    }

    /** {@code locID} (the wire name) or {@code locId}. */
    static String locId(JsonNode row) {
        String id = text(row.get("locID"));
        return id != null ? id : text(row.get("locId"));
    }

    /** The local {@code obsDt} at the point's zone (UTC when no point or no zone). */
    static Instant eventTime(String obsDt, double[] point, PlaceTimeZones zones) {
        if (obsDt == null) {
            return null;
        }
        ZoneId zone = zoneAt(point, zones);
        String value = obsDt.strip();
        try {
            return LocalDateTime.parse(value, OBS_DT).atZone(zone).toInstant();
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(value, OBS_DATE).atStartOfDay(zone).toInstant();
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }

    private static ZoneId zoneAt(double[] point, PlaceTimeZones zones) {
        if (point == null || zones == null) {
            return ZoneOffset.UTC;
        }
        Optional<ZoneId> zone = zones.byPoint(point[0], point[1]);
        return zone.orElse(ZoneOffset.UTC);
    }

    private static double round(double value) {
        return Math.round(value / PRIVATE_DEGREES) * PRIVATE_DEGREES;
    }

    private static boolean valid(Double lon, Double lat) {
        return lon != null && lat != null && lon >= -180 && lon <= 180 && lat >= -90 && lat <= 90;
    }

    private static Boolean bool(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.isBoolean() ? node.asBoolean() : truthy(node) ? Boolean.TRUE : Boolean.FALSE;
    }

    private static boolean truthy(JsonNode node) {
        if (node == null || node.isNull()) {
            return false;
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        String text = text(node);
        return text != null && (text.equalsIgnoreCase("true") || text.equals("1"));
    }

    private static Long integral(JsonNode node) {
        if (node == null || !node.isNumber()) {
            return null;
        }
        return node.canConvertToLong() ? node.asLong() : (long) node.asDouble();
    }

    private static Double number(JsonNode node) {
        if (node == null || !node.isNumber()) {
            return null;
        }
        double d = node.asDouble();
        return Double.isFinite(d) ? d : null;
    }

    private static String text(JsonNode node) {
        if (node == null || node.isNull() || node.isContainer()) {
            return null;
        }
        String raw = node.asString();
        String clean = CONTROL.matcher(raw).replaceAll("").strip();
        return clean.isEmpty() ? null : clean;
    }

    private static String snippet(byte[] json) {
        int length = Math.min(json.length, UpstreamFormatException.MAX_SNIPPET_LENGTH * 4);
        return new String(json, 0, length, StandardCharsets.UTF_8);
    }
}
