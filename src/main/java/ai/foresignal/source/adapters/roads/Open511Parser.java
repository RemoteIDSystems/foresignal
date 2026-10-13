package ai.foresignal.source.adapters.roads;

import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import tools.jackson.databind.JsonNode;

/**
 * Parses an Open511 {@code /events} JSON page into road events (prompt 074; verified on DriveBC 2026-10-13).
 *
 * <p>Per event: {@code id} → {@code externalId}; {@code updated} → {@code observedAt}; {@code event_type} →
 * {@code category} ({@link #CATEGORY}); the first {@code event_subtypes} code → {@code subtype}; {@code severity}
 * ({@link #SEVERITY}); {@code roads[].name} → {@code roads}; the first road's {@code direction} and {@code state}
 * → {@code direction} and {@code lanesAffected}; {@code isClosure} when any road's {@code state} is {@code CLOSED};
 * {@code schedule.intervals} ({@code "start/end"}, open-ended allowed, read as UTC as DriveBC publishes them) →
 * {@code startsAt} and {@code validFrom} (the earliest start) and {@code endsAt} and {@code validUntil} (the end of
 * the last interval, only when it has one); {@code url}; {@code geography} (GeoJSON) → geometry. Extensions
 * ({@code +ivr_message}, {@code +linear_reference_km}) and every other member are dropped.
 *
 * <p>The headline is the upstream {@code headline} unless it merely repeats the event type (DriveBC sends
 * {@code CONSTRUCTION}), in which case it is the first sentence of the description.
 *
 * <p>An event whose {@code status} is {@code ARCHIVED} is marked ended (an immediate tombstone).
 * {@code pagination.next_url} is reported as the next page.
 */
public final class Open511Parser {

    /** {@code event_type} → category. */
    public static final Map<String, String> CATEGORY = Map.of(
            "CONSTRUCTION", "roadwork",
            "INCIDENT", "incident",
            "SPECIAL_EVENT", "event",
            "WEATHER_CONDITION", "weather",
            "ROAD_CONDITION", "weather");

    /** {@code severity} → severity. */
    public static final Map<String, String> SEVERITY = Map.of(
            "MINOR", "minor",
            "MODERATE", "moderate",
            "MAJOR", "major",
            "UNKNOWN", "unknown");

    /** Open511 road {@code direction} → direction. */
    public static final Map<String, String> DIRECTION = Map.ofEntries(
            Map.entry("BOTH", "both"),
            Map.entry("N", "northbound"), Map.entry("NORTHBOUND", "northbound"),
            Map.entry("S", "southbound"), Map.entry("SOUTHBOUND", "southbound"),
            Map.entry("E", "eastbound"), Map.entry("EASTBOUND", "eastbound"),
            Map.entry("W", "westbound"), Map.entry("WESTBOUND", "westbound"),
            Map.entry("NONE", "unknown"));

    private Open511Parser() {}

    /**
     * Parses one page.
     *
     * @param body the JSON body
     * @param language the language of the server's text
     * @param now the fetch time (for {@code status})
     * @throws UpstreamFormatException when the body is not an Open511 events document
     */
    public static ParsedEvents parse(byte[] body, String language, Instant now) {
        JsonNode root = RoadEvents.readJson(body, "the Open511 page");
        JsonNode events = root.get("events");
        if (events == null || !events.isArray()) {
            throw new UpstreamFormatException("the Open511 page has no events array", "");
        }
        ParsedEvents out = new ParsedEvents();
        for (JsonNode event : events) {
            event(event, language, now, out);
        }
        out.nextUrl(RoadEvents.text(root.path("pagination"), "next_url"));
        return out;
    }

    private static void event(JsonNode e, String language, Instant now, ParsedEvents out) {
        String id = RoadEvents.text(e, "id");
        if (id == null) {
            out.skip(ParsedEvents.MISSING_ID);
            return;
        }
        String status = upper(RoadEvents.text(e, "status"));
        if ("ARCHIVED".equals(status)) {
            out.ended(id);
            return;
        }
        Instant observedAt = RoadEvents.instant(RoadEvents.text(e, "updated"));
        if (observedAt == null) {
            out.skip(ParsedEvents.MISSING_OBSERVED_AT);
            return;
        }
        FeatureGeometry.Result geometry = RoadEvents.geometry(e.get("geography"));
        if (geometry == null) {
            out.skip(ParsedEvents.BAD_GEOMETRY);
            return;
        }
        String type = upper(RoadEvents.text(e, "event_type"));
        RoadEventPayload p = new RoadEventPayload("open511")
                .category(type == null ? "other" : CATEGORY.getOrDefault(type, "other"))
                .severity(SEVERITY.getOrDefault(upper(RoadEvents.text(e, "severity")), "unknown"))
                .language(language)
                .url(RoadEvents.text(e, "url"));
        JsonNode subtypes = e.get("event_subtypes");
        if (subtypes != null && subtypes.isArray() && !subtypes.isEmpty() && subtypes.get(0).isString()) {
            p.subtype(subtypes.get(0).asString());
        } else {
            p.subtype(type);
        }
        String description = RoadEvents.text(e, "description");
        p.description(description);
        String headline = RoadEvents.text(e, "headline");
        if (headline == null || headline.replace(' ', '_').equalsIgnoreCase(type)) {
            String sentence = RoadEventPayload.firstSentence(description);
            headline = sentence != null ? sentence : RoadEventPayload.words(type);
        }
        p.headline(headline);

        boolean closure = false;
        JsonNode roads = e.get("roads");
        if (roads != null && roads.isArray()) {
            boolean first = true;
            for (JsonNode road : roads) {
                p.road(RoadEvents.text(road, "name"));
                String state = upper(RoadEvents.text(road, "state"));
                if ("CLOSED".equals(state)) {
                    closure = true;
                }
                if (first) {
                    String direction = upper(RoadEvents.text(road, "direction"));
                    p.direction(direction == null ? null : DIRECTION.getOrDefault(direction, "unknown"));
                    p.lanesAffected(RoadEventPayload.kebab(state));
                    first = false;
                }
            }
        }
        p.isClosure(closure);

        Instant start = null;
        Instant end = null;
        JsonNode intervals = RoadEvents.path(e, "schedule", "intervals");
        List<String> list = new ArrayList<>();
        if (intervals.isArray()) {
            intervals.forEach(i -> {
                if (i.isString()) {
                    list.add(i.asString());
                }
            });
        }
        for (String interval : list) {
            int slash = interval.indexOf('/');
            Instant s = RoadEvents.instant(slash < 0 ? interval : interval.substring(0, slash));
            if (s != null && (start == null || s.isBefore(start))) {
                start = s;
            }
        }
        if (!list.isEmpty()) {
            String last = list.getLast();
            int slash = last.indexOf('/');
            end = slash < 0 ? null : RoadEvents.instant(last.substring(slash + 1));
        }
        p.startsAt(start).endsAt(end).statusFrom(start, now);
        out.add(RoadEvents.record(id, observedAt, geometry, start, end, p));
    }

    private static String upper(String text) {
        return text == null ? null : text.strip().toUpperCase(Locale.ROOT);
    }
}
