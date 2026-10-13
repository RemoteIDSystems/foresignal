package ai.foresignal.source.adapters.hydro.snotel;

import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.adapters.hydro.HydroSources;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.MappedRecord;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * NRCS AWDB REST station metadata and daily data as {@code snow-observation} records (prompt 079). Pure.
 *
 * <p>Verified 2026-10-13. {@code /v1/stations} answers an array of {@code {stationTriplet, name, elevation} (feet),
 * {@code latitude, longitude, dataTimeZone} (hours, {@code -8.0} for every Colorado SNOTEL site), …}; {@code /v1/data}
 * answers {@code [{stationTriplet, data: [{stationElement: {elementCode, storedUnitCode}, values: [{date, value}]}]}]}
 * with {@code WTEQ}, {@code SNWD} and {@code PREC} stored in inches and {@code TOBS} in °F.
 *
 * <ul>
 *   <li>{@code externalId = stationTriplet}, a point from the metadata (a station without metadata is skipped);
 *   <li>the newest date with any value is the observation; each element's value of that date is converted:
 *       {@code sweMm} ({@code WTEQ} in → mm), {@code snowDepthCm} ({@code SNWD} in → cm), {@code precipAccumMm}
 *       ({@code PREC} in → mm), {@code airTempC} ({@code TOBS} °F → °C); {@code elevationM} from feet;
 *   <li>{@code observedAt = eventTime} = that date at 00:00 in the station's {@code dataTimeZone}, in UTC.
 * </ul>
 */
public final class AwdbParser {

    /** The record kind. */
    public static final String KIND = "snow-observation";

    /** The elements requested. */
    public static final String ELEMENTS = "WTEQ,SNWD,PREC,TOBS";

    /** Feet to meters. */
    static final double FOOT_M = 0.3048;

    /** Inches to millimeters. */
    static final double INCH_MM = 25.4;

    private AwdbParser() {}

    /**
     * One station's metadata.
     *
     * @param name the station name
     * @param latitude degrees
     * @param longitude degrees
     * @param elevationFt feet, or null
     * @param timeZoneHours the data time zone offset, hours
     */
    public record Station(String name, double latitude, double longitude, Double elevationFt, double timeZoneHours) {}

    /**
     * The stations of a {@code /v1/stations} answer, by triplet; entries without a triplet or a location are left
     * out.
     *
     * @throws UpstreamFormatException when it is not an array
     */
    public static Map<String, Station> stations(JsonNode root) {
        if (root == null || !root.isArray()) {
            throw new UpstreamFormatException("AWDB stations answer is not an array", "");
        }
        Map<String, Station> out = new TreeMap<>();
        for (JsonNode s : root) {
            String triplet = HydroSources.text(s.get("stationTriplet"));
            Double lat = HydroSources.number(s.get("latitude"));
            Double lon = HydroSources.number(s.get("longitude"));
            if (triplet == null || !EnvSources.validPoint(lat, lon)) {
                continue;
            }
            Double tz = HydroSources.number(s.get("dataTimeZone"));
            out.put(triplet.toUpperCase(Locale.ROOT), new Station(HydroSources.text(s.get("name")), lat, lon,
                    HydroSources.number(s.get("elevation")), tz == null ? 0.0 : tz));
        }
        return Collections.unmodifiableMap(out);
    }

    /**
     * Maps a {@code /v1/data} answer.
     *
     * @param stations the metadata by triplet
     * @throws UpstreamFormatException when it is not an array
     */
    public static EnvSources.Parsed data(JsonNode root, Map<String, Station> stations) {
        if (root == null || !root.isArray()) {
            throw new UpstreamFormatException("AWDB data answer is not an array", "");
        }
        EnvSources.Collector collector = new EnvSources.Collector(null, null);
        for (JsonNode entry : root) {
            collector.add(station(entry, stations));
        }
        return collector.result();
    }

    /** One station's data as a record, or the skip reason. */
    static MappedRecord station(JsonNode entry, Map<String, Station> stations) {
        String triplet = HydroSources.text(entry.get("stationTriplet"));
        if (triplet == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        triplet = triplet.toUpperCase(Locale.ROOT);
        Station station = stations.get(triplet);
        if (station == null) {
            return MappedRecord.skipped(EnvSources.MISSING_LOCATION);
        }
        // element -> (date -> value converted)
        Map<String, Map<LocalDate, Double>> byElement = new LinkedHashMap<>();
        LocalDate newest = null;
        JsonNode data = entry.get("data");
        if (data != null && data.isArray()) {
            for (JsonNode element : data) {
                JsonNode se = element.path("stationElement");
                String code = HydroSources.text(se.get("elementCode"));
                String unit = HydroSources.text(se.get("storedUnitCode"));
                if (code == null) {
                    continue;
                }
                JsonNode values = element.get("values");
                if (values == null || !values.isArray()) {
                    continue;
                }
                for (JsonNode v : values) {
                    LocalDate date = date(HydroSources.text(v.get("date")));
                    Double raw = HydroSources.number(v.get("value"));
                    Double converted = raw == null ? null : convert(code.toUpperCase(Locale.ROOT), unit, raw);
                    if (date == null || converted == null) {
                        continue;
                    }
                    byElement.computeIfAbsent(code.toUpperCase(Locale.ROOT), k -> new TreeMap<>()).put(date, converted);
                    if (newest == null || date.isAfter(newest)) {
                        newest = date;
                    }
                }
            }
        }
        if (newest == null) {
            return MappedRecord.skipped(EnvSources.NO_VALUES);
        }
        Instant at;
        try {
            at = newest.atStartOfDay(ZoneOffset.ofTotalSeconds((int) Math.round(station.timeZoneHours() * 3600)))
                    .toInstant();
        } catch (DateTimeException e) {
            at = newest.atStartOfDay(ZoneOffset.UTC).toInstant();
        }
        LocalDate day = newest;
        NormalizedRecord.Builder b = NormalizedRecord.builder(triplet, KIND).observedAt(at).eventTime(at)
                .point(station.longitude(), station.latitude())
                .put("name", station.name())
                .put("elevationM", station.elevationFt() == null ? null
                        : EnvSources.round(station.elevationFt() * FOOT_M, 1))
                .put("sweMm", valueOn(byElement, "WTEQ", day))
                .put("snowDepthCm", valueOn(byElement, "SNWD", day))
                .put("precipAccumMm", valueOn(byElement, "PREC", day))
                .put("airTempC", valueOn(byElement, "TOBS", day));
        return MappedRecord.of(b.build());
    }

    private static Double valueOn(Map<String, Map<LocalDate, Double>> byElement, String code, LocalDate day) {
        Map<LocalDate, Double> values = byElement.get(code);
        return values == null ? null : values.get(day);
    }

    /**
     * {@code value} of element {@code code} stored in {@code unit}, in the payload unit: mm for {@code WTEQ} and
     * {@code PREC}, cm for {@code SNWD}, °C for {@code TOBS}; null for another element or an unknown unit.
     */
    static Double convert(String code, String unit, double value) {
        String u = unit == null ? "" : unit.toLowerCase(Locale.ROOT);
        return switch (code) {
            case "WTEQ", "PREC" -> switch (u) {
                case "in" -> EnvSources.round(value * INCH_MM, 1);
                case "mm" -> EnvSources.round(value, 1);
                case "cm" -> EnvSources.round(value * 10, 1);
                default -> null;
            };
            case "SNWD" -> switch (u) {
                case "in" -> EnvSources.round(value * INCH_MM / 10, 1);
                case "cm" -> EnvSources.round(value, 1);
                case "mm" -> EnvSources.round(value / 10, 1);
                default -> null;
            };
            case "TOBS" -> switch (u) {
                case "degf" -> EnvSources.round((value - 32) * 5 / 9, 1);
                case "degc" -> EnvSources.round(value, 1);
                default -> null;
            };
            default -> null;
        };
    }

    private static LocalDate date(String text) {
        if (text == null || text.length() < 10) {
            return null;
        }
        try {
            return LocalDate.parse(text.substring(0, 10));
        } catch (DateTimeException e) {
            return null;
        }
    }

    /** The station table as compact JSON, for the adapter state. */
    public static String stationsToJson(Map<String, Station> stations) {
        ArrayNode root = HydroSources.JSON.createArrayNode();
        stations.forEach((triplet, s) -> {
            ObjectNode n = root.addObject();
            n.put("t", triplet);
            if (s.name() != null) {
                n.put("n", s.name());
            }
            n.put("la", s.latitude());
            n.put("lo", s.longitude());
            if (s.elevationFt() != null) {
                n.put("e", s.elevationFt());
            }
            n.put("z", s.timeZoneHours());
        });
        return root.toString();
    }

    /** The station table written by {@link #stationsToJson}; empty for null or damaged text. */
    public static Map<String, Station> stationsFromJson(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            JsonNode root = HydroSources.JSON.readTree(json);
            if (root == null || !root.isArray()) {
                return Map.of();
            }
            Map<String, Station> out = new TreeMap<>();
            for (JsonNode n : root) {
                Double lat = HydroSources.number(n.get("la"));
                Double lon = HydroSources.number(n.get("lo"));
                Double tz = HydroSources.number(n.get("z"));
                String triplet = HydroSources.text(n.get("t"));
                if (triplet != null && lat != null && lon != null) {
                    out.put(triplet, new Station(HydroSources.text(n.get("n")), lat, lon,
                            HydroSources.number(n.get("e")), tz == null ? 0.0 : tz));
                }
            }
            return out;
        } catch (JacksonException e) {
            return Map.of();
        }
    }
}
