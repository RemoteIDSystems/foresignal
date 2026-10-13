package ai.foresignal.source.adapters.local;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * A tolerant parser of an Ecowitt gateway's {@code /get_livedata_info} (prompt 069). <strong>Experimental:</strong>
 * there is no official specification (verified 2026-10); the shape below was read from Ecowitt's own Home
 * Assistant integration ({@code Ecowitt/ha-ecowitt-iot} and its {@code wittiot} library, read only, nothing
 * copied) and is firmware-dependent.
 *
 * <ul>
 *   <li><strong>{@code common_list}</strong> {@code [{id, val, unit?}]}: a table maps the known ids:
 *       {@code 0x02} temperature, {@code 0x07} humidity, {@code 0x03} dew point, {@code 0x0B} wind speed,
 *       {@code 0x0C} gust, {@code 0x0A} wind direction, {@code 0x15} solar radiation, {@code 0x17} UV index.
 *       Ids that are known but not stored ({@code 0x19} daily maximum wind, {@code 3} feels-like, {@code 4}
 *       apparent temperature, {@code 5} VPD, {@code 0x6D} 10-minute direction, {@code 0xA1}/{@code 0xA2}
 *       globe temperatures) are skipped; any other id is skipped and counted ({@link Result#unknownIds()}).
 *   <li><strong>{@code rain}</strong> (or {@code piezoRain} when there is no {@code rain}): {@code 0x0E} rate
 *       and {@code 0x10} daily total.
 *   <li><strong>{@code wh25[0]}</strong>: {@code rel} (relative, sea-level) pressure, and with
 *       {@code includeIndoor} {@code intemp}/{@code inhumi}.
 *   <li><strong>Units</strong> come from the {@code unit} member or the value's own suffix ({@code "1.2 m/s"},
 *       {@code "29.92 inHg"}, {@code "45%"}) and are converted before storing: °F, mph, km/h, knots, ft/s,
 *       inHg, mmHg, kPa and inches become SI. A value whose unit is not recognized is left out.
 * </ul>
 *
 * <p>A body that is not a JSON object, or that has none of {@code common_list}, {@code rain},
 * {@code piezoRain} and {@code wh25}, is an {@link UpstreamFormatException}, so the health format-change
 * rule notices a firmware update that changed the shape.
 */
public final class EcowittLiveDataParser {

    /** The {@code common_list} ids mapped to payload keys. */
    static final Map<String, String> COMMON_IDS = Map.of(
            "0x02", "tempC",
            "0x07", "rh",
            "0x03", "dewPointC",
            "0x0B", "windSpeedMs",
            "0x0C", "windGustMs",
            "0x0A", "windDirDeg",
            "0x15", "solarWm2",
            "0x17", "uvIndex");

    /** {@code common_list} ids that are known and deliberately not stored. */
    static final Set<String> IGNORED_IDS = Set.of("0x19", "3", "4", "5", "0x6D", "0xA1", "0xA2");

    /** A number and an optional unit suffix. */
    static final Pattern VALUE = Pattern.compile("^\\s*(-?\\d+(?:\\.\\d+)?)\\s*(.*?)\\s*$");

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private EcowittLiveDataParser() {}

    /**
     * The record and how many {@code common_list} ids were not recognized.
     *
     * @param record the observation
     * @param unknownIds ids outside the table and the ignored set
     */
    public record Result(NormalizedRecord record, int unknownIds) {}

    /**
     * Parses a body.
     *
     * @param externalId the record id (the source id: the endpoint names no device)
     * @param lat the station latitude, rounded here to 2 decimals
     * @param lon the station longitude, rounded here to 2 decimals
     * @param includeIndoor true to keep the console's indoor values
     * @throws UpstreamFormatException when the body is not live data
     */
    public static Result parse(byte[] body, String externalId, double lat, double lon, boolean includeIndoor) {
        JsonNode root;
        try {
            root = body == null || body.length == 0 ? null : JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("get_livedata_info is not JSON: " + e.getOriginalMessage(), snippet(body), e);
        }
        if (root == null || !root.isObject()) {
            throw new UpstreamFormatException("get_livedata_info is not a JSON object", snippet(body));
        }
        JsonNode common = root.get("common_list");
        JsonNode rain = root.get("rain");
        JsonNode piezo = root.get("piezoRain");
        JsonNode wh25 = root.get("wh25");
        if (!isArray(common) && !isArray(rain) && !isArray(piezo) && !isArray(wh25)) {
            throw new UpstreamFormatException("get_livedata_info has none of common_list, rain, piezoRain and wh25",
                    snippet(body));
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        int unknown = 0;
        if (isArray(common)) {
            for (JsonNode entry : common) {
                String id = text(entry.get("id"));
                if (id == null) {
                    unknown++;
                    continue;
                }
                String key = COMMON_IDS.get(id);
                if (key == null) {
                    if (!IGNORED_IDS.contains(id)) {
                        unknown++;
                    }
                    continue;
                }
                Double value = convert(key, text(entry.get("val")), text(entry.get("unit")));
                if (value != null) {
                    payload.put(key, value);
                }
            }
        }
        JsonNode rainList = isArray(rain) ? rain : piezo;
        if (isArray(rainList)) {
            for (JsonNode entry : rainList) {
                String id = text(entry.get("id"));
                String key = "0x0E".equals(id) ? "rainRateMmH" : "0x10".equals(id) ? "rainDailyMm" : null;
                if (key != null) {
                    Double value = convert(key, text(entry.get("val")), text(entry.get("unit")));
                    if (value != null) {
                        payload.put(key, value);
                    }
                }
            }
        }
        if (isArray(wh25) && !wh25.isEmpty()) {
            JsonNode console = wh25.get(0);
            String unit = text(console.get("unit"));
            Double pressure = convert("pressureHpa", text(console.get("rel")), null);
            if (pressure != null) {
                payload.put("pressureHpa", pressure);
            }
            if (includeIndoor) {
                Double temp = convert("indoorTempC", text(console.get("intemp")), unit);
                if (temp != null) {
                    payload.put("indoorTempC", temp);
                }
                Double rh = convert("indoorRh", text(console.get("inhumi")), null);
                if (rh != null) {
                    payload.put("indoorRh", rh);
                }
            }
        }
        NormalizedRecord record = NormalizedRecord.builder(externalId, WeatherStations.KIND)
                .point(WeatherStations.coarse(lon), WeatherStations.coarse(lat))
                .putAll(payload)
                .build();
        return new Result(record, unknown);
    }

    /**
     * {@code raw} (a number with an optional unit suffix) in the SI unit of {@code key}, or null when it is not
     * a number or its unit is not recognized.
     *
     * @param unit the entry's {@code unit} member, which wins over a suffix; may be null
     */
    static Double convert(String key, String raw, String unit) {
        if (raw == null) {
            return null;
        }
        Matcher m = VALUE.matcher(raw);
        if (!m.matches()) {
            return null;
        }
        double value = Double.parseDouble(m.group(1));
        String u = (unit != null && !unit.isBlank() ? unit : m.group(2)).trim().toLowerCase(Locale.ROOT)
                .replace("°", "").replace("℃", "c").replace("℉", "f");
        return switch (key) {
            case "tempC", "dewPointC", "indoorTempC" -> switch (u) {
                case "c", "" -> LocalReceivers.round(value, 1);
                case "f" -> WeatherStations.fahrenheitToCelsius(value);
                default -> null;
            };
            case "rh", "indoorRh" -> u.isEmpty() || u.equals("%") ? LocalReceivers.round(value, 1) : null;
            case "windSpeedMs", "windGustMs" -> switch (u) {
                case "m/s" -> LocalReceivers.round(value, 1);
                case "km/h", "kmh" -> LocalReceivers.round(value * WeatherStations.KMH_TO_MPS, 1);
                case "mph" -> WeatherStations.mphToMps(value);
                case "knots", "kn", "kt", "kts" -> LocalReceivers.round(value * WeatherStations.KNOT_TO_MPS, 1);
                case "ft/s", "fps" -> LocalReceivers.round(value * WeatherStations.FPS_TO_MPS, 1);
                default -> null;
            };
            case "windDirDeg" -> u.isEmpty() || u.equals("deg") ? LocalReceivers.round(value, 0) : null;
            case "solarWm2" -> u.equals("w/m2") || u.equals("w/m²") ? LocalReceivers.round(value, 1) : null;
            case "uvIndex" -> u.isEmpty() ? LocalReceivers.round(value, 1) : null;
            case "pressureHpa" -> switch (u) {
                case "hpa", "mbar", "mb" -> LocalReceivers.round(value, 1);
                case "inhg" -> WeatherStations.inHgToHpa(value);
                case "mmhg" -> LocalReceivers.round(value * WeatherStations.MMHG_TO_HPA, 1);
                case "kpa" -> LocalReceivers.round(value * 10.0, 1);
                default -> null;
            };
            case "rainRateMmH" -> switch (u) {
                case "mm/hr", "mm/h" -> LocalReceivers.round(value, 1);
                case "in/hr", "in/h" -> LocalReceivers.round(value * WeatherStations.INCH_TO_MM, 1);
                default -> null;
            };
            case "rainDailyMm" -> switch (u) {
                case "mm" -> LocalReceivers.round(value, 1);
                case "in" -> LocalReceivers.round(value * WeatherStations.INCH_TO_MM, 1);
                default -> null;
            };
            default -> null;
        };
    }

    private static boolean isArray(JsonNode node) {
        return node != null && node.isArray();
    }

    private static String text(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.asString();
        }
        if (!node.isString()) {
            return null;
        }
        String value = node.stringValue().trim();
        return value.isEmpty() ? null : value;
    }

    private static String snippet(byte[] body) {
        return body == null ? "" : new String(body, 0, Math.min(body.length, 1000), StandardCharsets.UTF_8);
    }
}
