package ai.foresignal.source.adapters.roads;

import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import tools.jackson.databind.JsonNode;

/**
 * Parses a WZDx 4.0–4.2 feed (a GeoJSON FeatureCollection of road events) into road events (prompt 074).
 *
 * <p><strong>Version:</strong> read from {@code feed_info.version}, else {@code road_event_feed_info.version}
 * (4.0). A 3.x or CWZ document, or one without either feed-info member, is refused with
 * {@code UpstreamFormatException("unsupported WZDx version …")} so source health shows it.
 *
 * <p><strong>Per feature:</strong> {@code id} → {@code externalId}; {@code core_details.update_date} →
 * {@code observedAt}; {@code core_details.event_type} → {@code category} ({@link #CATEGORY}) and {@code subtype};
 * {@code road_names}, {@code direction} ({@link #DIRECTION}), {@code description}, and {@code name} (4.2) as the
 * headline (else the first sentence of the description); {@code start_date}/{@code end_date} → {@code startsAt},
 * {@code endsAt}, {@code validFrom}, {@code validUntil}; {@code vehicle_impact} → {@code lanesAffected}, the
 * severity ({@link #SEVERITY}; WZDx has no severity of its own) and {@code isClosure} ({@code all-lanes-closed});
 * {@code reduced_speed_limit_kph} → {@code speedLimitKph} (0 means none). An {@code event_status} of
 * {@code completed} or {@code cancelled} marks the event ended (an immediate tombstone); {@code planned} or
 * {@code pending} is {@code status = planned}. Geometry (LineString or MultiPoint) as published.
 *
 * <p><strong>Duplicate ids:</strong> several feeds reuse one feature id for distinct entries (on 2026-10-13 UDOT
 * listed 744 features under 171 ids, WSDOT 545 under 539). Every feature of a repeated id gets the id plus
 * {@code ~} and 8 hex characters of SHA-256 over its data source, direction, start and end dates, creation date
 * and first coordinate ({@link #discriminator}), so none is lost; an exact repeat is skipped as a duplicate.
 */
public final class WzdxParser {

    /** {@code core_details.event_type} → category. */
    public static final Map<String, String> CATEGORY = Map.of(
            "work-zone", "roadwork",
            "detour", "closure",
            "restriction", "restriction");

    /** {@code vehicle_impact} → severity. */
    public static final Map<String, String> SEVERITY = Map.ofEntries(
            Map.entry("all-lanes-closed", "major"),
            Map.entry("some-lanes-closed", "moderate"),
            Map.entry("some-lanes-closed-merge-left", "moderate"),
            Map.entry("some-lanes-closed-merge-right", "moderate"),
            Map.entry("all-lanes-open-shift-left", "minor"),
            Map.entry("all-lanes-open-shift-right", "minor"),
            Map.entry("some-lanes-closed-split", "moderate"),
            Map.entry("flagging", "moderate"),
            Map.entry("temporary-traffic-signal", "moderate"),
            Map.entry("alternating-one-way", "moderate"),
            Map.entry("all-lanes-open", "minor"),
            Map.entry("unknown", "unknown"));

    /** {@code core_details.direction} → direction. */
    public static final Map<String, String> DIRECTION = Map.of(
            "northbound", "northbound",
            "southbound", "southbound",
            "eastbound", "eastbound",
            "westbound", "westbound",
            "inbound", "inbound",
            "outbound", "outbound",
            "both", "both",
            "undefined", "unknown",
            "unknown", "unknown");

    private WzdxParser() {}

    /**
     * The document's WZDx version.
     *
     * @throws UpstreamFormatException when it is not 4.x
     */
    public static String version(JsonNode root) {
        String version = RoadEvents.text(root.path("feed_info"), "version");
        if (version == null) {
            version = RoadEvents.text(root.path("road_event_feed_info"), "version");
        }
        if (version == null || !version.strip().startsWith("4")) {
            throw new UpstreamFormatException("unsupported WZDx version " + (version == null ? "(none)" : version)
                    + "; only 4.0 to 4.2 are read", "");
        }
        return version.strip();
    }

    /**
     * Parses one feed document.
     *
     * @param body the GeoJSON body
     * @param now the fetch time (for {@code status})
     * @throws UpstreamFormatException when the body is not a WZDx 4.x FeatureCollection
     */
    public static ParsedEvents parse(byte[] body, Instant now) {
        JsonNode root = RoadEvents.readJson(body, "the WZDx feed");
        if (root.isString()) {
            // NJIT serves its feed JSON-encoded inside one JSON string (verified 2026-10-22).
            root = RoadEvents.readJson(root.asString().getBytes(StandardCharsets.UTF_8), "the WZDx feed");
        }
        if (!"FeatureCollection".equals(RoadEvents.text(root, "type"))) {
            throw new UpstreamFormatException("the WZDx feed is not a GeoJSON FeatureCollection", "");
        }
        version(root);
        JsonNode features = root.get("features");
        if (features == null || !features.isArray()) {
            throw new UpstreamFormatException("the WZDx feed has no features array", "");
        }
        Map<String, Integer> counts = new HashMap<>();
        for (JsonNode f : features) {
            String id = RoadEvents.text(f, "id");
            if (id != null) {
                counts.merge(id, 1, Integer::sum);
            }
        }
        ParsedEvents out = new ParsedEvents();
        for (JsonNode f : features) {
            String id = RoadEvents.text(f, "id");
            if (id == null) {
                out.skip(ParsedEvents.MISSING_ID);
                continue;
            }
            feature(f, counts.get(id) > 1 ? id + "~" + discriminator(f) : id, now, out);
        }
        return out;
    }

    private static void feature(JsonNode f, String id, Instant now, ParsedEvents out) {
        JsonNode props = f.path("properties");
        JsonNode core = props.path("core_details");
        String eventStatus = lower(RoadEvents.text(props, "event_status"));
        if ("completed".equals(eventStatus) || "cancelled".equals(eventStatus) || "canceled".equals(eventStatus)) {
            out.ended(id);
            return;
        }
        Instant observedAt = RoadEvents.instant(RoadEvents.text(core, "update_date"));
        if (observedAt == null) {
            out.skip(ParsedEvents.MISSING_OBSERVED_AT);
            return;
        }
        FeatureGeometry.Result geometry = RoadEvents.geometry(f.get("geometry"));
        if (geometry == null) {
            out.skip(ParsedEvents.BAD_GEOMETRY);
            return;
        }
        String type = lower(RoadEvents.text(core, "event_type"));
        String impact = lower(RoadEvents.text(props, "vehicle_impact"));
        String description = RoadEvents.text(core, "description");
        RoadEventPayload p = new RoadEventPayload("wzdx")
                .category(type == null ? "other" : CATEGORY.getOrDefault(type, "other"))
                .subtype(type)
                .severity(impact == null ? "unknown" : SEVERITY.getOrDefault(impact, "unknown"))
                .description(description)
                .lanesAffected(impact)
                .isClosure("all-lanes-closed".equals(impact))
                .speedLimitKph(RoadEvents.number(props, "reduced_speed_limit_kph"))
                .language("en");
        JsonNode roads = core.get("road_names");
        if (roads != null && roads.isArray()) {
            roads.forEach(r -> p.road(r.isString() || r.isNumber() ? r.asString() : null));
        }
        String direction = lower(RoadEvents.text(core, "direction"));
        p.direction(direction == null ? null : DIRECTION.getOrDefault(direction, "unknown"));
        String headline = RoadEvents.text(core, "name");
        if (headline == null) {
            headline = RoadEventPayload.firstSentence(description);
        }
        if (headline == null) {
            String what = type == null ? "Road event" : RoadEventPayload.words(type);
            headline = p.toMap().get("roads") instanceof java.util.List<?> list && !list.isEmpty()
                    ? what + " on " + list.getFirst() : what;
        }
        p.headline(headline);
        Instant start = RoadEvents.instant(RoadEvents.text(props, "start_date"));
        Instant end = RoadEvents.instant(RoadEvents.text(props, "end_date"));
        p.startsAt(start).endsAt(end);
        if ("planned".equals(eventStatus) || "pending".equals(eventStatus)) {
            p.status("planned");
        } else if ("active".equals(eventStatus)) {
            p.status("active");
        } else {
            p.statusFrom(start, now);
        }
        out.add(RoadEvents.record(id, observedAt, geometry, start, end, p));
    }

    /**
     * 8 hex characters of SHA-256 over the feature's data source, direction, start and end dates, creation date and
     * first coordinate: the fields that tell apart the entries one id is reused for, and that do not change when an
     * entry is updated.
     */
    static String discriminator(JsonNode f) {
        JsonNode props = f.path("properties");
        JsonNode core = props.path("core_details");
        JsonNode first = f.path("geometry").path("coordinates");
        while (first.isArray() && !first.isEmpty() && first.get(0).isArray()) {
            first = first.get(0);
        }
        String key = String.join("|", String.valueOf(RoadEvents.text(core, "data_source_id")),
                String.valueOf(RoadEvents.text(core, "direction")), String.valueOf(RoadEvents.text(props,
                        "start_date")), String.valueOf(RoadEvents.text(props, "end_date")),
                String.valueOf(RoadEvents.text(core, "creation_date")), first.toString());
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, 4);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private static String lower(String text) {
        return text == null ? null : text.strip().toLowerCase(Locale.ROOT);
    }
}
