package ai.foresignal.intel;

import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.source.spi.Geo;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

/**
 * The {@code outage-weather} annotator (prompt 105): for {@code infrastructure-disruption} and
 * {@code weather-impact} events, adds {@code context.weather} when weather is within ±3 hours of the
 * event and near its area, and the alternative "weather-related damage".
 *
 * <p>The weather evidence is taken from stored records only (no upstream fetch): NWS warnings
 * ({@code nws-alert}), radar areas ({@code wx-area}) and storm tracks ({@code storm-track}). These
 * enter as <strong>context members</strong>, never as supporting evidence, so they never raise the
 * event's confidence; the "weather-related damage" alternative lists them as context.
 *
 * <p>Missing optional sources simply produce fewer sections; the annotator never fails the event.
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

    private static final int LIMIT = 500;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Set<String> types() {
        return TYPES;
    }

    @Override
    public Result annotate(IntelligenceEventEntity event, AnnotatorContext ctx) {
        if (ctx.area() == null || ctx.area().isEmpty()) {
            return Result.none();
        }
        Point centroid = ctx.area().getCentroid();
        List<Map<String, Object>> members = new ArrayList<>();
        List<Map<String, Object>> warnings = new ArrayList<>();
        List<Map<String, Object>> radar = new ArrayList<>();
        List<Map<String, Object>> tracks = new ArrayList<>();
        collect(ctx, "nws-alerts", "nws-alert", warnings, members, centroid);
        collect(ctx, "wx-areas", "wx-area", radar, members, centroid);
        collect(ctx, "wx-storm-tracks", "storm-track", tracks, members, centroid);
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
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("annotator", ID);
        context.put("weather", weather);
        List<Map<String, Object>> alternatives = new ArrayList<>();
        Map<String, Object> alternative = new LinkedHashMap<>();
        alternative.put("hypothesis", "weather-related damage");
        alternative.put("basis", "weather records within " + WINDOW_HOURS + " h and " + (int) MAX_KM
                + " km of the event (context, not supporting evidence)");
        alternative.put("supportingMemberIds", List.of());
        alternatives.add(alternative);
        return new Result(context, alternatives, members);
    }

    private static void collect(AnnotatorContext ctx, String source, String kind, List<Map<String, Object>> into,
            List<Map<String, Object>> members, Point centroid) {
        for (HeadSnapshot head : ctx.records().current(List.of(source), List.of(kind), LIMIT)) {
            if (head.tombstone() || head.geometry() == null || head.geometry().isEmpty()) {
                continue;
            }
            Instant at = head.observedAt();
            if (at != null && (at.isBefore(ctx.from()) || at.isAfter(ctx.to()))) {
                continue;
            }
            Point point = head.geometry().getCentroid();
            double km = Geo.haversineKm(centroid.getY(), centroid.getX(), point.getY(), point.getX());
            if (km > MAX_KM) {
                continue;
            }
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", head.externalId());
            entry.put("kind", kind);
            entry.put("distanceKm", Math.round(km * 10) / 10.0);
            if (at != null) {
                entry.put("at", at.toString());
            }
            Object severity = head.payload().get("severity");
            if (severity != null) {
                entry.put("severity", severity);
            }
            into.add(entry);
            members.add(contextMember(head));
        }
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
