package ai.foresignal.source.adapters.nws;

import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses {@code api.weather.gov} {@code /points} and {@code /gridpoints} documents (prompt 053). Pure functions.
 *
 * <p><b>Gridpoint layers</b> (verified on a 2026-10-12 body): each property is
 * {@code {uom, values: [{validTime, value}]}} where {@code validTime} is an ISO-8601 interval
 * {@code start/duration} ({@code 2026-10-12T13:00:00+00:00/PT5H}, {@code …/P1DT14H}). Units: {@code temperature}
 * {@code wmoUnit:degC}, {@code windSpeed} and {@code windGust} {@code wmoUnit:km_h-1} (converted to m/s),
 * {@code windDirection} degrees, {@code probabilityOfPrecipitation} and {@code skyCover} percent,
 * {@code quantitativePrecipitation} {@code wmoUnit:mm}. {@code hazards} values are lists of
 * {@code {phenomenon, significance, event_number}}; the hazard type is {@code phenomenon.significance}
 * ({@code FZ.W}). An empty list is no hazard.
 *
 * <p><b>Expansion</b>: every interval is spread over the whole hours it covers; an accumulation
 * ({@code quantitativePrecipitation}) is divided evenly across them. The hourly series runs for
 * {@value #SERIES_HOURS} hours from the current hour; the 24-hour summaries cover its first 24 hours.
 */
public final class NwsGridpointsParser {

    /** Hours in the cached series. */
    public static final int SERIES_HOURS = 72;

    /** Hours the summaries cover. */
    public static final int SUMMARY_HOURS = 24;

    /** Hazards kept in a record. */
    public static final int MAX_HAZARDS = 10;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private NwsGridpointsParser() {}

    /**
     * The gridpoint of a place, from {@code /points/{lat},{lon}}.
     *
     * @param gridId the forecast office (WFO)
     * @param gridX the grid column
     * @param gridY the grid row
     * @param forecastGridData the gridpoint URL
     * @param timeZone the IANA zone, or null
     */
    public record Point(String gridId, int gridX, int gridY, String forecastGridData, String timeZone) {}

    /**
     * One hazard.
     *
     * @param type {@code phenomenon.significance}, for example {@code FZ.W}
     * @param validTime the ISO-8601 interval as published
     */
    public record Hazard(String type, String validTime) {}

    /**
     * A parsed gridpoint forecast.
     *
     * @param updateTime the forecast's {@code updateTime}
     * @param wfo the office ({@code gridId})
     * @param gridX the grid column
     * @param gridY the grid row
     * @param start the first hour of the series
     * @param series hourly steps, oldest first: {@code time} plus the {@code ForecastSeriesPoint} fields present
     * @param summary {@code popMax24h}, {@code qpf24hMm}, {@code tMaxC24h}, {@code tMinC24h},
     *     {@code gustMaxMs24h}, each only when the forecast has a value for it
     * @param hazards at most {@value #MAX_HAZARDS}, by start time
     */
    public record Forecast(Instant updateTime, String wfo, int gridX, int gridY, Instant start,
            List<Map<String, Object>> series, Map<String, Object> summary, List<Hazard> hazards) {}

    /** Parses a {@code /points} document. */
    public static Point parsePoint(byte[] body) {
        JsonNode p = root(body).path("properties");
        String gridId = string(p.get("gridId"));
        JsonNode x = p.get("gridX");
        JsonNode y = p.get("gridY");
        String url = string(p.get("forecastGridData"));
        if (gridId == null || x == null || !x.isNumber() || y == null || !y.isNumber() || url == null) {
            throw new UpstreamFormatException("NWS /points: gridId, gridX, gridY or forecastGridData missing", text(body));
        }
        return new Point(gridId, x.intValue(), y.intValue(), url, string(p.get("timeZone")));
    }

    /**
     * Parses a {@code /gridpoints} document.
     *
     * @param now the current time; the series starts at its hour
     */
    public static Forecast parseForecast(byte[] body, Instant now) {
        JsonNode p = root(body).path("properties");
        Instant updateTime = instant(p.get("updateTime"));
        String wfo = string(p.get("gridId"));
        JsonNode x = p.get("gridX");
        JsonNode y = p.get("gridY");
        if (updateTime == null || wfo == null || x == null || !x.isNumber() || y == null || !y.isNumber()) {
            throw new UpstreamFormatException("NWS /gridpoints: updateTime, gridId, gridX or gridY missing", text(body));
        }
        Instant start = now.truncatedTo(ChronoUnit.HOURS);
        Instant end = start.plus(Duration.ofHours(SERIES_HOURS));
        Map<Instant, Double> temp = expand(p.get("temperature"), start, end, false, Unit.CELSIUS);
        Map<Instant, Double> wind = expand(p.get("windSpeed"), start, end, false, Unit.SPEED);
        Map<Instant, Double> gust = expand(p.get("windGust"), start, end, false, Unit.SPEED);
        Map<Instant, Double> dir = expand(p.get("windDirection"), start, end, false, Unit.ANY);
        Map<Instant, Double> pop = expand(p.get("probabilityOfPrecipitation"), start, end, false, Unit.ANY);
        Map<Instant, Double> qpf = expand(p.get("quantitativePrecipitation"), start, end, true, Unit.MILLIMETERS);
        Map<Instant, Double> sky = expand(p.get("skyCover"), start, end, false, Unit.ANY);

        List<Map<String, Object>> series = new ArrayList<>();
        for (Instant t = start; t.isBefore(end); t = t.plus(Duration.ofHours(1))) {
            Map<String, Object> step = new LinkedHashMap<>();
            step.put("time", t.toString());
            putRounded(step, "tempC", temp.get(t), 1);
            putRounded(step, "windSpeedMs", wind.get(t), 1);
            putInt(step, "windFromDeg", dir.get(t) == null ? null : ((dir.get(t) % 360) + 360) % 360);
            putRounded(step, "gustMs", gust.get(t), 1);
            putInt(step, "popPct", pop.get(t));
            putRounded(step, "precipMm", qpf.get(t), 2);
            putInt(step, "skyCoverPct", sky.get(t));
            if (step.size() > 1) {
                series.add(step);
            }
        }

        Instant summaryEnd = start.plus(Duration.ofHours(SUMMARY_HOURS));
        Map<String, Object> summary = new LinkedHashMap<>();
        Double popMax = max(pop, start, summaryEnd);
        if (popMax != null) {
            summary.put("popMax24h", (int) Math.round(popMax));
        }
        Double qpfSum = sum(qpf, start, summaryEnd);
        if (qpfSum != null) {
            summary.put("qpf24hMm", round(qpfSum, 1));
        }
        Double tMax = max(temp, start, summaryEnd);
        if (tMax != null) {
            summary.put("tMaxC24h", round(tMax, 1));
        }
        Double tMin = min(temp, start, summaryEnd);
        if (tMin != null) {
            summary.put("tMinC24h", round(tMin, 1));
        }
        Double gustMax = max(gust, start, summaryEnd);
        if (gustMax != null) {
            summary.put("gustMaxMs24h", round(gustMax, 1));
        }
        return new Forecast(updateTime, wfo, x.intValue(), y.intValue(), start, series, summary, hazards(p.get("hazards")));
    }

    /** The units a layer must carry. */
    enum Unit {
        /** {@code wmoUnit:degC} (or {@code degF}, converted). */
        CELSIUS,
        /** {@code wmoUnit:km_h-1} or {@code wmoUnit:m_s-1}, to m/s. */
        SPEED,
        /** {@code wmoUnit:mm}. */
        MILLIMETERS,
        /** Taken as published (percent, degrees). */
        ANY
    }

    /**
     * Expands one layer's intervals into hourly values within {@code [from, to)}.
     *
     * @param accumulation divide each value evenly across its interval's hours
     */
    static Map<Instant, Double> expand(JsonNode layer, Instant from, Instant to, boolean accumulation, Unit unit) {
        Map<Instant, Double> out = new TreeMap<>();
        if (layer == null || !layer.isObject()) {
            return out;
        }
        String uom = string(layer.get("uom"));
        for (JsonNode v : iterable(layer.get("values"))) {
            JsonNode value = v.get("value");
            if (value == null || !value.isNumber()) {
                continue;
            }
            Interval interval = interval(string(v.get("validTime")));
            if (interval == null) {
                continue;
            }
            int hours = interval.hours();
            double converted = convert(value.doubleValue(), uom, unit);
            double perHour = accumulation ? converted / hours : converted;
            Instant t = interval.start().truncatedTo(ChronoUnit.HOURS);
            for (int h = 0; h < hours; h++, t = t.plus(Duration.ofHours(1))) {
                if (!t.isBefore(from) && t.isBefore(to)) {
                    out.put(t, perHour);
                }
            }
        }
        return out;
    }

    /**
     * An ISO-8601 interval {@code start/duration}.
     *
     * @param start the start
     * @param duration the duration, at least one hour after rounding
     */
    record Interval(Instant start, Duration duration) {

        /** The whole hours covered, at least 1. */
        int hours() {
            return (int) Math.max(1, Math.round(duration.toMinutes() / 60.0));
        }
    }

    /** Parses {@code 2026-10-12T13:00:00+00:00/PT5H}, or null when malformed. */
    static Interval interval(String validTime) {
        if (validTime == null) {
            return null;
        }
        int slash = validTime.indexOf('/');
        if (slash <= 0) {
            return null;
        }
        try {
            Instant start = OffsetDateTime.parse(validTime.substring(0, slash)).toInstant();
            Duration duration = Duration.parse(validTime.substring(slash + 1));
            if (duration.isNegative() || duration.isZero()) {
                return null;
            }
            return new Interval(start, duration);
        } catch (DateTimeParseException | ArithmeticException e) {
            return null;
        }
    }

    /** Converts a value in {@code uom} to the unit wanted. */
    static double convert(double value, String uom, Unit unit) {
        String u = uom == null ? "" : uom;
        return switch (unit) {
            case CELSIUS -> switch (u) {
                case "wmoUnit:degC" -> value;
                case "wmoUnit:degF" -> (value - 32) / 1.8;
                default -> throw new UpstreamFormatException("NWS /gridpoints: temperature in unexpected units " + u, null);
            };
            case SPEED -> switch (u) {
                case "wmoUnit:km_h-1" -> value / 3.6;
                case "wmoUnit:m_s-1" -> value;
                default -> throw new UpstreamFormatException("NWS /gridpoints: wind in unexpected units " + u, null);
            };
            case MILLIMETERS -> switch (u) {
                case "wmoUnit:mm" -> value;
                default -> throw new UpstreamFormatException("NWS /gridpoints: precipitation in unexpected units " + u,
                        null);
            };
            case ANY -> value;
        };
    }

    /** The hazards, by start time, at most {@value #MAX_HAZARDS}. */
    static List<Hazard> hazards(JsonNode layer) {
        List<Hazard> out = new ArrayList<>();
        if (layer == null || !layer.isObject()) {
            return out;
        }
        List<Map.Entry<Instant, Hazard>> found = new ArrayList<>();
        for (JsonNode v : iterable(layer.get("values"))) {
            String validTime = string(v.get("validTime"));
            Interval interval = interval(validTime);
            for (JsonNode h : iterable(v.get("value"))) {
                String phenomenon = string(h.get("phenomenon"));
                String significance = string(h.get("significance"));
                if (phenomenon == null || phenomenon.isBlank()) {
                    continue;
                }
                String type = significance == null || significance.isBlank() ? phenomenon : phenomenon + "." + significance;
                found.add(Map.entry(interval == null ? Instant.MAX : interval.start(), new Hazard(type, validTime)));
            }
        }
        found.sort(Map.Entry.comparingByKey());
        for (Map.Entry<Instant, Hazard> e : found) {
            if (out.size() >= MAX_HAZARDS) {
                break;
            }
            out.add(e.getValue());
        }
        return out;
    }

    private static Double max(Map<Instant, Double> values, Instant from, Instant to) {
        Double out = null;
        for (Map.Entry<Instant, Double> e : values.entrySet()) {
            if (!e.getKey().isBefore(from) && e.getKey().isBefore(to) && (out == null || e.getValue() > out)) {
                out = e.getValue();
            }
        }
        return out;
    }

    private static Double min(Map<Instant, Double> values, Instant from, Instant to) {
        Double out = null;
        for (Map.Entry<Instant, Double> e : values.entrySet()) {
            if (!e.getKey().isBefore(from) && e.getKey().isBefore(to) && (out == null || e.getValue() < out)) {
                out = e.getValue();
            }
        }
        return out;
    }

    private static Double sum(Map<Instant, Double> values, Instant from, Instant to) {
        Double out = null;
        for (Map.Entry<Instant, Double> e : values.entrySet()) {
            if (!e.getKey().isBefore(from) && e.getKey().isBefore(to)) {
                out = (out == null ? 0 : out) + e.getValue();
            }
        }
        return out;
    }

    /** {@code v} rounded to {@code decimals}. */
    static double round(double v, int decimals) {
        double f = Math.pow(10, decimals);
        double r = Math.round(v * f) / f;
        return r == 0 ? 0.0 : r;
    }

    private static void putRounded(Map<String, Object> m, String key, Double v, int decimals) {
        if (v != null) {
            m.put(key, round(v, decimals));
        }
    }

    private static void putInt(Map<String, Object> m, String key, Double v) {
        if (v != null) {
            m.put(key, (int) Math.round(v));
        }
    }

    private static JsonNode root(byte[] body) {
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("NWS response is not JSON: " + e.getOriginalMessage(), text(body));
        }
        if (root == null || !root.isObject()) {
            throw new UpstreamFormatException("NWS response is not a JSON object", text(body));
        }
        return root;
    }

    private static Iterable<JsonNode> iterable(JsonNode node) {
        return node != null && node.isArray() ? node : List.of();
    }

    private static String string(JsonNode node) {
        return node != null && node.isString() ? node.stringValue() : null;
    }

    private static Instant instant(JsonNode node) {
        String value = string(node);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String text(byte[] body) {
        int length = Math.min(body.length, UpstreamFormatException.MAX_SNIPPET_LENGTH * 4);
        return new String(body, 0, length, StandardCharsets.UTF_8);
    }
}
