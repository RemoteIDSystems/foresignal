package ai.foresignal.weather;

import ai.foresignal.raster.sample.SampleService;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The one deterministic sentence of a weather context (prompt 056), built from its sections in a fixed order:
 * radar (with the nearest cell's motion), lightning, precipitation, NWS warnings, wind and smoke, then the footer
 * {@value #FOOTER}.
 *
 * <p>A non-ok section is never dropped and never worded as absence: it reads "lightning data stale since 14:30
 * UTC", "radar data has no coverage here (not evidence of clear weather)" or "smoke data unavailable". Times are
 * UTC, {@code HH:mm} on the day computed for and {@code yyyy-MM-dd HH:mm} otherwise. Upstream text (NWS event
 * names) is cut to {@value #MAX_EVENT} characters and stripped of control characters.
 */
public final class WeatherSummary {

    /** The footer of every summary. */
    public static final String FOOTER = "Automated — not an official warning.";

    /** The summary of a context whose computation failed. */
    public static final String UNAVAILABLE =
            "Weather context unavailable; this is not evidence of clear weather. " + FOOTER;

    /** The longest NWS event name kept. */
    static final int MAX_EVENT = 60;

    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT)
            .withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter DATE_CLOCK = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ROOT)
            .withZone(ZoneOffset.UTC);

    private WeatherSummary() {}

    /**
     * The sentence.
     *
     * @param sections the sections by name
     * @param at the time the context is for (decides whether {@code asOf} times carry a date)
     */
    @SuppressWarnings("unchecked")
    public static String of(Map<String, Map<String, Object>> sections, Instant at) {
        List<String> parts = new ArrayList<>();
        radar(sections.get("radar"), at, parts);
        lightning(sections.get("lightning"), at, parts);
        precip(sections.get("precip"), at, parts);
        warnings(sections.get("warnings"), at, parts);
        wind(sections.get("wind"), at, parts);
        smoke(sections.get("smoke"), at, parts);
        if (parts.isEmpty()) {
            return UNAVAILABLE;
        }
        String joined = String.join("; ", parts);
        return Character.toUpperCase(joined.charAt(0)) + joined.substring(1) + ". " + FOOTER;
    }

    private static void radar(Map<String, Object> s, Instant at, List<String> parts) {
        if (s == null) {
            return;
        }
        if (!WeatherSections.isOk(s)) {
            parts.add(notOk("radar", s, at));
            return;
        }
        StringBuilder out = new StringBuilder("radar max ").append(number(s.get("maxDbz"))).append(" dBZ within 10 km");
        if (s.get("nearestCell") instanceof Map<?, ?> cell) {
            out.append("; cell ").append(number(cell.get("distanceKm"))).append(" km away");
            if (cell.get("motion") instanceof Map<?, ?> motion && motion.get("speedKmh") instanceof Number speed
                    && motion.get("bearingDeg") instanceof Number bearing) {
                out.append(" moving ").append(SampleService.compass(bearing.doubleValue())).append(" at ")
                        .append(number(speed)).append(" km/h");
            }
            if (cell.get("meshMaxMm") instanceof Number mesh && mesh.doubleValue() > 0) {
                out.append(" (MESH ").append(number(mesh)).append(" mm)");
            }
        }
        parts.add(out.toString());
    }

    private static void lightning(Map<String, Object> s, Instant at, List<String> parts) {
        if (s == null) {
            return;
        }
        if (!WeatherSections.isOk(s)) {
            parts.add(notOk("lightning", s, at));
            return;
        }
        long n = s.get("flashes20km15m") instanceof Number num ? num.longValue() : 0;
        parts.add(n + (n == 1 ? " GLM flash" : " GLM flashes") + " within 20 km (15 min)");
    }

    private static void precip(Map<String, Object> s, Instant at, List<String> parts) {
        if (s == null) {
            return;
        }
        if (!WeatherSections.isOk(s)) {
            parts.add(notOk("precipitation", s, at));
            return;
        }
        StringBuilder out = new StringBuilder("rain rate ").append(number(s.get("rateMmH"))).append(" mm/h");
        if (s.get("qpe1hMm") instanceof Number qpe) {
            out.append(", ").append(number(qpe)).append(" mm in the last hour");
        }
        parts.add(out.toString());
    }

    private static void warnings(Map<String, Object> s, Instant at, List<String> parts) {
        if (s == null) {
            return;
        }
        if (!WeatherSections.isOk(s)) {
            parts.add(notOk("NWS warnings", s, at));
            return;
        }
        List<String> events = new ArrayList<>();
        if (s.get("items") instanceof List<?> items) {
            for (Object item : items) {
                if (item instanceof Map<?, ?> m && m.get("event") != null) {
                    String event = clean(m.get("event").toString());
                    if (!event.isEmpty() && !events.contains(event)) {
                        events.add(event);
                    }
                }
            }
        }
        parts.add(events.isEmpty() ? "no active NWS warnings" : "NWS " + String.join(", ", events) + " in effect");
    }

    private static void wind(Map<String, Object> s, Instant at, List<String> parts) {
        if (s == null) {
            return;
        }
        if (!WeatherSections.isOk(s)) {
            parts.add(notOk("wind", s, at));
            return;
        }
        StringBuilder out = new StringBuilder("wind ").append(number(s.get("speedMs"))).append(" m/s");
        if (s.get("fromDeg") instanceof Number from) {
            out.append(" from ").append(SampleService.compass(from.doubleValue()));
        }
        if (s.get("gustMs") instanceof Number gust) {
            out.append(", gusts ").append(number(gust)).append(" m/s");
        }
        out.append(" (").append(model(s.get("productId"))).append(')');
        parts.add(out.toString());
    }

    private static void smoke(Map<String, Object> s, Instant at, List<String> parts) {
        if (s == null) {
            return;
        }
        if (!WeatherSections.isOk(s)) {
            parts.add(notOk("smoke", s, at));
            return;
        }
        parts.add("near-surface smoke " + number(s.get("pm25UgM3")) + " µg/m³ (HRRR)");
    }

    /** The wording of a non-ok section: never absence. */
    static String notOk(String name, Map<String, Object> s, Instant at) {
        String status = WeatherSections.status(s);
        Instant asOf = null;
        if (s.get("asOf") != null) {
            try {
                asOf = Instant.parse(s.get("asOf").toString());
            } catch (RuntimeException e) {
                asOf = null;
            }
        }
        return switch (status) {
            case WeatherSections.STALE -> name + " data stale" + (asOf == null ? "" : " since " + time(asOf, at));
            case WeatherSections.NO_COVERAGE -> name + " data has no coverage here (not evidence of clear weather)";
            default -> name + " data unavailable";
        };
    }

    /** {@code HH:mm UTC} on the day of {@code at}, else {@code yyyy-MM-dd HH:mm UTC}. */
    static String time(Instant t, Instant at) {
        boolean sameDay = at != null && t.atZone(ZoneOffset.UTC).toLocalDate().equals(at.atZone(ZoneOffset.UTC).toLocalDate());
        return (sameDay ? CLOCK : DATE_CLOCK).format(t) + " UTC";
    }

    private static String model(Object productId) {
        String id = productId == null ? "" : productId.toString();
        return id.startsWith("hrrr") ? "HRRR" : id.startsWith("gfs") ? "GFS" : id;
    }

    /** A number without a trailing {@code .0}. */
    static String number(Object value) {
        if (!(value instanceof Number n)) {
            return "?";
        }
        double d = n.doubleValue();
        if (d == Math.rint(d) && !Double.isInfinite(d)) {
            return Long.toString((long) d);
        }
        return Double.toString(Math.round(d * 10) / 10.0);
    }

    /** Upstream text without control characters, cut to {@value #MAX_EVENT} characters. */
    static String clean(String text) {
        String s = text.replaceAll("[\\p{Cntrl}]", " ").replaceAll("\\s+", " ").strip();
        return s.length() <= MAX_EVENT ? s : s.substring(0, MAX_EVENT).strip();
    }
}
