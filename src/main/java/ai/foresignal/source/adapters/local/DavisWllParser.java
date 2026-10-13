package ai.foresignal.source.adapters.local;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses a Davis WeatherLink Live {@code /v1/current_conditions} answer (prompt 069; the WeatherLink Live
 * Local API, https://weatherlink.github.io/weatherlink-live-local-api/, read 2026-10-13).
 *
 * <p>The body is {@code {data: {did, ts, conditions: [...]}, error}}; each condition has a
 * {@code data_structure_type}:
 *
 * <ul>
 *   <li><strong>1, ISS:</strong> {@code temp, dew_point} (°F), {@code hum} (%), {@code wind_speed_last} and
 *       {@code wind_speed_hi_last_10_min} (mph), {@code wind_dir_last} (°), {@code rain_rate_last}
 *       (counts/hour), {@code rainfall_daily} (counts), {@code rain_size} and {@code trans_battery_flag}.
 *       The first ISS record is read.
 *   <li><strong>3, barometer:</strong> {@code bar_sea_level} and {@code bar_trend} (inHg).
 *   <li><strong>4, indoor:</strong> {@code temp_in} (°F) and {@code hum_in}, only with {@code includeIndoor}.
 *   <li><strong>2, leaf/soil:</strong> ignored.
 * </ul>
 *
 * <p>Units become SI ({@link WeatherStations}). Rain counts times the collector size give millimeters:
 * {@code rain_size} 1 = 0.01 in, 2 = 0.2 mm, 3 = 0.1 mm, 4 = 0.001 in; with any other size the rain fields
 * are left out. A null value is absent. {@code externalId = data.did}, {@code observedAt = eventTime =
 * data.ts}. A body without {@code data}, {@code did} or {@code ts}, or with an {@code error}, is an
 * {@link UpstreamFormatException}.
 */
public final class DavisWllParser {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private DavisWllParser() {}

    /**
     * Parses a body.
     *
     * @param lat the station latitude, rounded here to 2 decimals
     * @param lon the station longitude, rounded here to 2 decimals
     * @param includeIndoor true to keep the indoor temperature and humidity
     * @throws UpstreamFormatException when the body is not a current-conditions document
     */
    public static NormalizedRecord parse(byte[] body, double lat, double lon, boolean includeIndoor) {
        JsonNode root;
        try {
            root = body == null || body.length == 0 ? null : JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("current_conditions is not JSON: " + e.getOriginalMessage(),
                    snippet(body), e);
        }
        if (root == null || !root.isObject()) {
            throw new UpstreamFormatException("current_conditions is not a JSON object", snippet(body));
        }
        JsonNode error = root.get("error");
        if (error != null && !error.isNull()) {
            throw new UpstreamFormatException("WeatherLink Live answered an error: " + error, snippet(body));
        }
        JsonNode data = root.get("data");
        if (data == null || !data.isObject()) {
            throw new UpstreamFormatException("current_conditions has no data object", snippet(body));
        }
        JsonNode did = data.get("did");
        Double ts = number(data.get("ts"));
        JsonNode conditions = data.get("conditions");
        if (did == null || !did.isString() || did.stringValue().isBlank() || ts == null
                || conditions == null || !conditions.isArray()) {
            throw new UpstreamFormatException("current_conditions lacks did, ts or conditions", snippet(body));
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        boolean issSeen = false;
        for (JsonNode condition : conditions) {
            Double type = number(condition.get("data_structure_type"));
            if (type == null) {
                continue;
            }
            switch (type.intValue()) {
                case 1 -> {
                    if (!issSeen) {
                        iss(condition, payload);
                        issSeen = true;
                    }
                }
                case 3 -> barometer(condition, payload);
                case 4 -> {
                    if (includeIndoor) {
                        indoor(condition, payload);
                    }
                }
                default -> {
                    // 2 (leaf/soil) and unknown types are not read.
                }
            }
        }
        Instant at = Instant.ofEpochSecond(Math.round(ts));
        return NormalizedRecord.builder(did.stringValue().trim(), WeatherStations.KIND)
                .observedAt(at)
                .eventTime(at)
                .point(WeatherStations.coarse(lon), WeatherStations.coarse(lat))
                .putAll(payload)
                .build();
    }

    /** Millimeters per rain count of a {@code rain_size}, or null for an unknown size. */
    public static Double millimetersPerCount(Integer rainSize) {
        if (rainSize == null) {
            return null;
        }
        return switch (rainSize) {
            case 1 -> 0.01 * WeatherStations.INCH_TO_MM;
            case 2 -> 0.2;
            case 3 -> 0.1;
            case 4 -> 0.001 * WeatherStations.INCH_TO_MM;
            default -> null;
        };
    }

    private static void iss(JsonNode c, Map<String, Object> out) {
        Double temp = number(c.get("temp"));
        if (temp != null) {
            out.put("tempC", WeatherStations.fahrenheitToCelsius(temp));
        }
        Double hum = number(c.get("hum"));
        if (hum != null) {
            out.put("rh", LocalReceivers.round(hum, 1));
        }
        Double dew = number(c.get("dew_point"));
        if (dew != null) {
            out.put("dewPointC", WeatherStations.fahrenheitToCelsius(dew));
        }
        Double wind = number(c.get("wind_speed_last"));
        if (wind != null) {
            out.put("windSpeedMs", WeatherStations.mphToMps(wind));
        }
        Double dir = number(c.get("wind_dir_last"));
        if (dir != null) {
            out.put("windDirDeg", LocalReceivers.round(dir, 0));
        }
        Double gust = number(c.get("wind_speed_hi_last_10_min"));
        if (gust != null) {
            out.put("windGustMs", WeatherStations.mphToMps(gust));
        }
        Double perCount = millimetersPerCount(integer(c.get("rain_size")));
        if (perCount != null) {
            Double rate = number(c.get("rain_rate_last"));
            if (rate != null) {
                out.put("rainRateMmH", LocalReceivers.round(rate * perCount, 2));
            }
            Double daily = number(c.get("rainfall_daily"));
            if (daily != null) {
                out.put("rainDailyMm", LocalReceivers.round(daily * perCount, 2));
            }
        }
        Double battery = number(c.get("trans_battery_flag"));
        if (battery != null) {
            out.put("batteryLow", battery != 0.0);
        }
    }

    private static void barometer(JsonNode c, Map<String, Object> out) {
        Double sea = number(c.get("bar_sea_level"));
        if (sea != null) {
            out.put("pressureHpa", WeatherStations.inHgToHpa(sea));
        }
        Double trend = number(c.get("bar_trend"));
        if (trend != null) {
            out.put("pressureTrendHpa", WeatherStations.inHgToHpa(trend));
        }
    }

    private static void indoor(JsonNode c, Map<String, Object> out) {
        Double temp = number(c.get("temp_in"));
        if (temp != null) {
            out.put("indoorTempC", WeatherStations.fahrenheitToCelsius(temp));
        }
        Double hum = number(c.get("hum_in"));
        if (hum != null) {
            out.put("indoorRh", LocalReceivers.round(hum, 1));
        }
    }

    private static Double number(JsonNode node) {
        if (node == null || !node.isNumber()) {
            return null;
        }
        double value = node.doubleValue();
        return Double.isFinite(value) ? value : null;
    }

    private static Integer integer(JsonNode node) {
        Double value = number(node);
        return value == null || value != Math.rint(value) ? null : value.intValue();
    }

    private static String snippet(byte[] body) {
        return body == null ? "" : new String(body, 0, Math.min(body.length, 1000), StandardCharsets.UTF_8);
    }
}
