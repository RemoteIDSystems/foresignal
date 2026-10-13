package ai.foresignal.source.adapters.digitraffic.rail;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses Digitraffic's rail documents (prompt 080): {@code /api/v1/train-locations/latest} (the newest GPS fix of
 * every running train in Finland) and the single-train lookup {@code /api/v1/trains/{departureDate}/{trainNumber}}.
 *
 * <p>A pure function of its input (adapter rule 3).
 *
 * <ul>
 *   <li><strong>Identity:</strong> {@code externalId = departureDate + ":" + trainNumber} (a train number is
 *       reused every day).
 *   <li><strong>Time base:</strong> {@code observedAt = timestamp}, the time of the fix.
 *   <li><strong>Geometry:</strong> the GeoJSON point {@code location.coordinates} ({@code [lon, lat]}), rounded to
 *       6 decimals. An entry without a valid point, at (0, 0), or without a train number, departure date or
 *       timestamp is skipped and counted.
 *   <li><strong>Payload:</strong> {@code trainNumber} (integer), {@code departureDate} ({@code YYYY-MM-DD}),
 *       {@code speedKph} (km/h), {@code gps} ({@code isGpsLocation}), {@code accuracyM} (volatile), and from the
 *       optional lookup {@code trainType, trainCategory, operatorShortCode, commuterLineID} (omitted when blank).
 * </ul>
 */
public final class DigitrafficRailParser {

    /** The record kind. */
    public static final String KIND = "train";

    /** The JSON mapper of upstream documents. */
    static final JsonMapper JSON = JsonMapper.builder().build();

    private DigitrafficRailParser() {}

    /**
     * One train's fix, before it becomes a record.
     *
     * @param trainNumber the train number
     * @param departureDate the departure date, {@code YYYY-MM-DD}
     * @param observedAt the time of the fix
     * @param lon longitude, 6 decimals
     * @param lat latitude, 6 decimals
     * @param speedKph the speed in km/h, or null
     * @param gps {@code isGpsLocation}, or null
     * @param accuracyM the reported accuracy in meters, or null
     */
    public record Position(int trainNumber, String departureDate, Instant observedAt, double lon, double lat,
            Double speedKph, Boolean gps, Integer accuracyM) {

        /** {@code departureDate:trainNumber}. */
        public String externalId() {
            return departureDate + ":" + trainNumber;
        }
    }

    /**
     * What a train lookup adds; blank values are null.
     *
     * @param trainType for example {@code IC}, {@code HL} or {@code T}
     * @param trainCategory for example {@code Long-distance}, {@code Commuter} or {@code Cargo}
     * @param operatorShortCode for example {@code vr}
     * @param commuterLineID the commuter line letter, for example {@code K}
     */
    public record Enrichment(String trainType, String trainCategory, String operatorShortCode, String commuterLineID) {

        /** An entry recording that the lookup found nothing. */
        public static final Enrichment NONE = new Enrichment(null, null, null, null);

        /** Blanks become null. */
        public Enrichment {
            trainType = blankToNull(trainType);
            trainCategory = blankToNull(trainCategory);
            operatorShortCode = blankToNull(operatorShortCode);
            commuterLineID = blankToNull(commuterLineID);
        }
    }

    /**
     * The fixes of a {@code train-locations/latest} document and how many entries were skipped.
     *
     * @param positions the fixes, in document order
     * @param skipped entries without a usable point, identity or time
     */
    public record Locations(List<Position> positions, int skipped) {

        /** Copies the list. */
        public Locations {
            positions = List.copyOf(positions);
        }
    }

    /**
     * Parses {@code train-locations/latest}.
     *
     * @throws UpstreamFormatException when the body is not a JSON array
     */
    public static Locations parseLocations(byte[] body) {
        JsonNode root = read(body);
        if (!root.isArray()) {
            throw new UpstreamFormatException("Digitraffic train-locations is not a JSON array", snippet(body));
        }
        List<Position> positions = new ArrayList<>(root.size());
        int skipped = 0;
        for (JsonNode entry : root) {
            Optional<Position> position = position(entry);
            if (position.isPresent()) {
                positions.add(position.get());
            } else {
                skipped++;
            }
        }
        return new Locations(positions, skipped);
    }

    /** One entry, or empty when it lacks a usable point, identity or time. */
    static Optional<Position> position(JsonNode entry) {
        if (entry == null || !entry.isObject()) {
            return Optional.empty();
        }
        JsonNode number = entry.get("trainNumber");
        JsonNode date = entry.get("departureDate");
        JsonNode timestamp = entry.get("timestamp");
        if (number == null || !number.isIntegralNumber() || date == null || !date.isString()
                || date.asString().isBlank() || timestamp == null || !timestamp.isString()) {
            return Optional.empty();
        }
        Instant observedAt;
        try {
            observedAt = Instant.parse(timestamp.asString());
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
        JsonNode coordinates = entry.path("location").path("coordinates");
        if (!coordinates.isArray() || coordinates.size() < 2 || !coordinates.get(0).isNumber()
                || !coordinates.get(1).isNumber()) {
            return Optional.empty();
        }
        double lon = coordinates.get(0).doubleValue();
        double lat = coordinates.get(1).doubleValue();
        if (!Double.isFinite(lon) || !Double.isFinite(lat) || lon < -180 || lon > 180 || lat < -90 || lat > 90
                || (lon == 0.0 && lat == 0.0)) {
            return Optional.empty();
        }
        JsonNode speed = entry.get("speed");
        JsonNode gps = entry.get("isGpsLocation");
        JsonNode accuracy = entry.get("accuracy");
        return Optional.of(new Position(number.intValue(), date.asString(), observedAt, round(lon, 6), round(lat, 6),
                speed != null && speed.isNumber() && speed.doubleValue() >= 0 ? round(speed.doubleValue(), 1) : null,
                gps != null && gps.isBoolean() ? gps.booleanValue() : null,
                accuracy != null && accuracy.isIntegralNumber() ? accuracy.intValue() : null));
    }

    /**
     * Parses a train lookup ({@code /api/v1/trains/{departureDate}/{trainNumber}}, an array of at most one train).
     *
     * @return the enrichment, or {@link Enrichment#NONE} for an empty array
     * @throws UpstreamFormatException when the body is not a JSON array
     */
    public static Enrichment parseTrain(byte[] body) {
        JsonNode root = read(body);
        if (!root.isArray()) {
            throw new UpstreamFormatException("Digitraffic train lookup is not a JSON array", snippet(body));
        }
        if (root.isEmpty() || !root.get(0).isObject()) {
            return Enrichment.NONE;
        }
        JsonNode train = root.get(0);
        return new Enrichment(text(train, "trainType"), text(train, "trainCategory"),
                text(train, "operatorShortCode"), text(train, "commuterLineID"));
    }

    /**
     * The record of one fix.
     *
     * @param position the fix
     * @param enrichment the train's lookup, or null when it has none (yet)
     */
    public static NormalizedRecord record(Position position, Enrichment enrichment) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("trainNumber", position.trainNumber());
        payload.put("departureDate", position.departureDate());
        if (position.speedKph() != null) {
            payload.put("speedKph", position.speedKph());
        }
        if (position.gps() != null) {
            payload.put("gps", position.gps());
        }
        if (position.accuracyM() != null) {
            payload.put("accuracyM", position.accuracyM());
        }
        if (enrichment != null) {
            putIfPresent(payload, "trainType", enrichment.trainType());
            putIfPresent(payload, "trainCategory", enrichment.trainCategory());
            putIfPresent(payload, "operatorShortCode", enrichment.operatorShortCode());
            putIfPresent(payload, "commuterLineID", enrichment.commuterLineID());
        }
        return NormalizedRecord.builder(position.externalId(), KIND)
                .observedAt(position.observedAt())
                .point(position.lon(), position.lat())
                .putAll(payload)
                .build();
    }

    private static void putIfPresent(Map<String, Object> payload, String key, String value) {
        if (value != null) {
            payload.put(key, value);
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && value.isString() ? blankToNull(value.asString()) : null;
    }

    static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    static double round(double value, int decimals) {
        return BigDecimal.valueOf(value).setScale(decimals, RoundingMode.HALF_UP).doubleValue();
    }

    private static JsonNode read(byte[] body) {
        if (body == null || body.length == 0) {
            throw new UpstreamFormatException("Digitraffic response is empty", "");
        }
        try {
            return JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("Digitraffic response is not JSON: " + e.getOriginalMessage(),
                    snippet(body), e);
        }
    }

    private static String snippet(byte[] body) {
        return new String(body, 0, Math.min(body.length, 500), StandardCharsets.UTF_8);
    }
}
