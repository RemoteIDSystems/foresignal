package ai.foresignal.source.adapters.env.sensorcommunity;

import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import ai.foresignal.source.support.MappedRecord;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Sensor.Community's {@code /airrohr/v1/filter/{area}} answer as air-quality records (prompt 078). Pure.
 *
 * <p>Verified 2026-10-13 (a Berlin box, 510 KB): a JSON array of readings {@code {id, timestamp, location:
 * {id, latitude, longitude, altitude, country, indoor, exact_location}, sensor: {id, pin, sensor_type: {name,
 * …}}, sensordatavalues: [{value_type, value}]}}; coordinates and values are strings; the same sensor appears with
 * several readings (1,075 readings of 437 sensors).
 *
 * <ul>
 *   <li>One record per sensor, the newest reading ({@code externalId = sensor.id});
 *   <li>kind {@value #OUTDOOR_KIND}, or {@value #INDOOR_KIND} when {@code location.indoor == 1}; indoor sensors are
 *       skipped ({@value #INDOOR}) unless {@code includeIndoor};
 *   <li>payload {@code sensorType} ({@code sensor.sensor_type.name}), {@code pm10} ({@code P1}), {@code pm25}
 *       ({@code P2}), {@code tempC} ({@code temperature}), {@code humidityPct} ({@code humidity}),
 *       {@code pressureHpa} ({@code pressure}, pascals converted to hPa, 0.1), {@code exactLocation};
 *   <li>a reading with none of those values (noise and radiation sensors) is skipped ({@value
 *       EnvSources#NO_VALUES});
 *   <li>{@code observedAt = eventTime = timestamp} ({@code yyyy-MM-dd HH:mm:ss}, UTC).
 * </ul>
 */
public final class SensorCommunityParser {

    /** The outdoor kind. */
    public static final String OUTDOOR_KIND = "air-quality";

    /** The indoor kind. */
    public static final String INDOOR_KIND = "indoor-air-quality";

    /** The skip reason of an indoor sensor when indoor sensors are not included. */
    public static final String INDOOR = "indoor";

    /** The default {@code maxAge}. */
    public static final Duration DEFAULT_MAX_AGE = Duration.ofHours(6);

    /** {@code 2026-10-13 21:29:02}. */
    static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss", Locale.ROOT);

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private SensorCommunityParser() {}

    /**
     * Parses one answer.
     *
     * @param includeIndoor true to keep indoor sensors (as {@value #INDOOR_KIND})
     * @param now the fetch time
     * @param maxAge the oldest reading kept
     * @throws UpstreamFormatException when the body is not a JSON array
     */
    public static EnvSources.Parsed parse(byte[] body, boolean includeIndoor, Instant now, Duration maxAge) {
        JsonNode root;
        try {
            root = body == null || body.length == 0 ? null : JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("Sensor.Community answer is not JSON: " + e.getOriginalMessage(), "", e);
        }
        if (root == null || !root.isArray()) {
            throw new UpstreamFormatException("Sensor.Community answer is not a JSON array", "");
        }
        EnvSources.Collector collector = new EnvSources.Collector(now, maxAge);
        for (JsonNode reading : root) {
            collector.add(map(reading, includeIndoor));
        }
        return collector.result();
    }

    static MappedRecord map(JsonNode r, boolean includeIndoor) {
        JsonNode sensor = r.get("sensor");
        Long sensorId = JsonProps.integer(sensor, "id");
        if (sensorId == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        JsonNode location = r.get("location");
        Long indoor = JsonProps.integer(location, "indoor");
        boolean isIndoor = indoor != null && indoor == 1;
        if (isIndoor && !includeIndoor) {
            return MappedRecord.skipped(INDOOR);
        }
        Instant at = timestamp(JsonProps.text(r, "timestamp"));
        if (at == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_OBSERVED_AT);
        }
        Double lat = JsonProps.number(location, "latitude");
        Double lon = JsonProps.number(location, "longitude");
        if (!EnvSources.validPoint(lat, lon)) {
            return MappedRecord.skipped(EnvSources.MISSING_LOCATION);
        }
        Double pm10 = null;
        Double pm25 = null;
        Double temp = null;
        Double humidity = null;
        Double pressure = null;
        JsonNode values = r.get("sensordatavalues");
        if (values != null && values.isArray()) {
            for (JsonNode v : values) {
                String type = JsonProps.text(v, "value_type");
                Double value = JsonProps.number(v, "value");
                if (type == null || value == null) {
                    continue;
                }
                switch (type) {
                    case "P1" -> pm10 = value;
                    case "P2" -> pm25 = value;
                    case "temperature" -> temp = value;
                    case "humidity" -> humidity = value;
                    case "pressure" -> pressure = hectopascals(value);
                    default -> {
                        // other value types (P0, P4, N*, noise, radiation, sea-level pressure) are not stored
                    }
                }
            }
        }
        if (pm10 == null && pm25 == null && temp == null && humidity == null && pressure == null) {
            return MappedRecord.skipped(EnvSources.NO_VALUES);
        }
        Long exact = JsonProps.integer(location, "exact_location");
        return MappedRecord.of(NormalizedRecord.builder(Long.toString(sensorId), isIndoor ? INDOOR_KIND : OUTDOOR_KIND)
                .observedAt(at)
                .eventTime(at)
                .point(lon, lat)
                .put("sensorType", JsonProps.text(sensor == null ? null : sensor.get("sensor_type"), "name"))
                .put("pm10", pm10)
                .put("pm25", pm25)
                .put("tempC", temp)
                .put("humidityPct", humidity)
                .put("pressureHpa", pressure)
                .put("exactLocation", exact != null && exact == 1)
                .build());
    }

    /** A pressure in pascals (as Sensor.Community sends it) in hPa to 0.1; a value already in hPa is kept. */
    static Double hectopascals(double value) {
        return EnvSources.round(value > 2000 ? value / 100.0 : value, 1);
    }

    /** {@code yyyy-MM-dd HH:mm:ss} as a UTC instant, or null. */
    static Instant timestamp(String text) {
        if (text == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(text, TIMESTAMP).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
