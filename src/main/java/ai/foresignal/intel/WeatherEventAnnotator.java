package ai.foresignal.intel;

import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.source.spi.Geo;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The {@code outage-weather} annotator (prompt 105): for {@code infrastructure-disruption} and
 * {@code weather-impact} events, adds {@code context.weather} when severe weather was within ±3 hours of the
 * event and within {@value #MAX_KM} km of its area, and the alternative "weather-related damage".
 *
 * <p>Only weather that could plausibly damage infrastructure qualifies (prompt 105 §5.2):
 * <ul>
 *   <li>NWS <em>warnings</em> ({@code nws-alert} whose event names a warning) active during the window;
 *   <li>radar ≥ {@value #MIN_DBZ} dBZ: a {@code wx-area} reflectivity area at a threshold of at least
 *       {@value #MIN_DBZ}, or a {@code storm-track} whose cell reached it;
 *   <li>gusts ≥ {@value #MIN_GUST_MS} m/s: a forecast ({@code forecast-area} for a gust threshold of at least
 *       {@value #MIN_GUST_MS} m/s valid during the window) or an observation (a {@code metar} gust of at least
 *       {@value #MIN_GUST_MS} m/s).
 * </ul>
 * The records are read from stored versions inside the window ({@link RecordWindowReader}), so weather that
 * has since changed or ended still counts; without a reader (unit tests) the current heads are used.
 *
 * <p>They enter as <strong>context members</strong>, never as supporting evidence, so they never raise the
 * event's confidence; the "weather-related damage" alternative lists them as context. Missing optional
 * sources simply produce fewer sections; the annotator never fails the event.
 */
@Component
public class WeatherEventAnnotator implements EventAnnotator {

    /** The annotator id. */
    public static final String ID = "outage-weather";

    /** The event types it applies to. */
    public static final Set<String> TYPES = Set.of(EventTypes.INFRASTRUCTURE_DISRUPTION, EventTypes.WEATHER_IMPACT);

    /** The time half-window, hours. */
    public static final long WINDOW_HOURS = 3;

    /** The maximum distance from the event, km. */
    public static final double MAX_KM = 25;

    /** The radar threshold, dBZ. */
    public static final double MIN_DBZ = 50;

    /** The gust threshold, m/s. */
    public static final double MIN_GUST_MS = 25;

    /** One knot in m/s. */
    static final double KNOT_MS = 0.514444;

    /** How far before the window a long-lived record (a warning, a forecast area) may have been issued. */
    static final Duration ISSUE_LOOKBACK = Duration.ofHours(24);

    private static final int LIMIT = 500;

    private final RecordWindowReader reader;

    /** Creates the annotator over current heads only (tests). */
    public WeatherEventAnnotator() {
        this((RecordWindowReader) null);
    }

    /** Creates the annotator over the stored versions in each event's window. */
    @Autowired
    public WeatherEventAnnotator(ObjectProvider<RecordWindowReader> reader) {
        this(reader.getIfAvailable());
    }

    /**
     * Creates the annotator.
     *
     * @param reader the version reader, or null to read current heads
     */
    public WeatherEventAnnotator(RecordWindowReader reader) {
        this.reader = reader;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Set<String> types() {
        return TYPES;
    }

    /** One weather input: where to read it and when a version qualifies. */
    record Input(String source, String kind, boolean longLived) {}

    @Override
    public Result annotate(IntelligenceEventEntity event, AnnotatorContext ctx) {
        if (ctx.area() == null || ctx.area().isEmpty()) {
            return Result.none();
        }
        Geometry near = ctx.area().buffer(MAX_KM / 111.0);
        Point centroid = ctx.area().getCentroid();
        List<Map<String, Object>> members = new ArrayList<>();
        List<Map<String, Object>> warnings = new ArrayList<>();
        List<Map<String, Object>> radar = new ArrayList<>();
        List<Map<String, Object>> tracks = new ArrayList<>();
        List<Map<String, Object>> gusts = new ArrayList<>();
        collect(ctx, new Input("nws-alerts", "nws-alert", true), near, centroid, warnings, members);
        collect(ctx, new Input("wx-areas", "wx-area", false), near, centroid, radar, members);
        collect(ctx, new Input("wx-storm-tracks", "storm-track", false), near, centroid, tracks, members);
        collect(ctx, new Input("wx-forecast-areas", "forecast-area", true), near, centroid, gusts, members);
        collect(ctx, new Input("awc-metars", "metar", false), near, centroid, gusts, members);
        if (members.isEmpty()) {
            return Result.none();
        }
        Map<String, Object> weather = new LinkedHashMap<>();
        weather.put("status", "ok");
        weather.put("from", ctx.from().toString());
        weather.put("to", ctx.to().toString());
        weather.put("warnings", warnings);
        weather.put("radar", radar);
        weather.put("tracks", tracks);
        weather.put("gusts", gusts);
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("annotator", ID);
        context.put("weather", weather);
        List<Map<String, Object>> alternatives = new ArrayList<>();
        Map<String, Object> alternative = new LinkedHashMap<>();
        alternative.put("hypothesis", "weather-related damage");
        alternative.put("basis", "NWS warnings, radar >= " + (int) MIN_DBZ + " dBZ or gusts >= " + (int) MIN_GUST_MS
                + " m/s within " + WINDOW_HOURS + " h and " + (int) MAX_KM
                + " km of the event (context, not supporting evidence)");
        alternative.put("supportingMemberIds", List.of());
        alternatives.add(alternative);
        return new Result(context, alternatives, members);
    }

    private void collect(AnnotatorContext ctx, Input input, Geometry near, Point centroid,
            List<Map<String, Object>> into, List<Map<String, Object>> members) {
        Set<String> seen = new LinkedHashSet<>();
        for (HeadSnapshot head : candidates(ctx, input, near)) {
            if (head.tombstone() || head.geometry() == null || head.geometry().isEmpty()
                    || !seen.add(head.externalId())) {
                continue;
            }
            if (!inWindow(input, head, ctx.from(), ctx.to()) || !qualifies(input.kind(), head.payload())) {
                seen.remove(head.externalId());
                continue;
            }
            if (!near.intersects(head.geometry())) {
                seen.remove(head.externalId());
                continue;
            }
            Point point = head.geometry().getCentroid();
            double km = Geo.haversineKm(centroid.getY(), centroid.getX(), point.getY(), point.getX());
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", head.externalId());
            entry.put("kind", input.kind());
            entry.put("distanceKm", Math.round(km * 10) / 10.0);
            if (head.observedAt() != null) {
                entry.put("at", head.observedAt().toString());
            }
            Object severity = head.payload().get("severity");
            if (severity != null) {
                entry.put("severity", severity);
            }
            into.add(entry);
            members.add(contextMember(head));
        }
    }

    /** The versions (or, without a reader, the current heads) that may qualify, newest first. */
    private List<HeadSnapshot> candidates(AnnotatorContext ctx, Input input, Geometry near) {
        if (reader == null) {
            return ctx.records().current(List.of(input.source()), List.of(input.kind()), LIMIT);
        }
        Instant from = input.longLived() ? ctx.from().minus(ISSUE_LOOKBACK) : ctx.from();
        Envelope bbox = near.getEnvelopeInternal();
        return reader.versions(input.source(), input.kind(), from, ctx.to(), bbox, LIMIT);
    }

    /**
     * True when a version is relevant to {@code [from, to]}: a warning or a forecast area whose valid interval
     * overlaps the window; any other record observed inside it.
     */
    static boolean inWindow(Input input, HeadSnapshot head, Instant from, Instant to) {
        Map<String, Object> p = head.payload();
        Instant start;
        Instant end;
        switch (input.kind()) {
            case "nws-alert" -> {
                start = first(instant(p.get("onset")), instant(p.get("effective")), instant(p.get("sent")),
                        head.observedAt());
                end = later(instant(p.get("expires")), instant(p.get("ends")));
            }
            case "forecast-area" -> {
                start = instant(p.get("firstExceedAt"));
                end = instant(p.get("validUntil"));
            }
            default -> {
                start = head.observedAt();
                end = head.observedAt();
            }
        }
        if (start == null) {
            return false;
        }
        if (end == null) {
            end = start;
        }
        return !start.isAfter(to) && !end.isBefore(from);
    }

    /** True when a version shows weather severe enough to damage infrastructure. */
    static boolean qualifies(String kind, Map<String, Object> p) {
        return switch (kind) {
            case "nws-alert" -> p.get("event") != null
                    && p.get("event").toString().toLowerCase(Locale.ROOT).contains("warning");
            case "wx-area" -> "refl".equals(String.valueOf(p.get("field"))) && number(p.get("threshold")) >= MIN_DBZ
                    && number(p.get("areaKm2")) > 0;
            case "storm-track" -> p.get("cell") instanceof Map<?, ?> cell && number(cell.get("maxDbz")) >= MIN_DBZ;
            case "forecast-area" -> "gust".equals(String.valueOf(p.get("field")))
                    && p.get("threshold") instanceof Map<?, ?> t && ">=".equals(String.valueOf(t.get("op")))
                    && number(t.get("value")) >= MIN_GUST_MS;
            case "metar" -> number(p.get("gustKt")) * KNOT_MS >= MIN_GUST_MS;
            default -> false;
        };
    }

    /** A number from a payload value (numbers or numeric strings); NaN otherwise. */
    static double number(Object value) {
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        if (value == null) {
            return Double.NaN;
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    private static Instant instant(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value.toString());
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Instant first(Instant... values) {
        for (Instant v : values) {
            if (v != null) {
                return v;
            }
        }
        return null;
    }

    private static Instant later(Instant a, Instant b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.isAfter(b) ? a : b;
    }

    private static Map<String, Object> contextMember(HeadSnapshot head) {
        Map<String, Object> member = new LinkedHashMap<>();
        member.put("role", "context");
        member.put("memberType", "record");
        member.put("sourceId", head.sourceId());
        member.put("externalId", head.externalId());
        member.put("seq", head.seq());
        member.put("version", head.version());
        member.put("kind", head.kind());
        member.put("observedAt", head.observedAt() == null ? null : head.observedAt().toString());
        member.put("joinReason", Map.of("clause", "context", "annotator", ID));
        member.put("signalFamily", SignalFamilies.WEATHER_HAZARD);
        member.put("weight", 0.0);
        member.put("snapshot", Map.of("sourceId", head.sourceId(), "externalId", head.externalId(),
                "kind", head.kind(), "payload", head.payload()));
        return member;
    }
}
