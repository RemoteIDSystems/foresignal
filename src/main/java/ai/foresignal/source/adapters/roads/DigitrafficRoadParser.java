package ai.foresignal.source.adapters.roads;

import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.MissingNode;

/**
 * Parses a Digitraffic traffic-message document (Fintraffic, a GeoJSON FeatureCollection) into road events
 * (prompt 074; verified 2026-10-13 on the v2 per-type routes, whose shape equals the v1 {@code messages} route
 * except that enumerations are lower case with spaces).
 *
 * <p>Per situation: {@code situationId} → {@code externalId}; {@code versionTime} → {@code observedAt};
 * {@code situationType} and {@code trafficAnnouncementType} → {@code category} ({@link #category}) and
 * {@code subtype}; from the first announcement: {@code title} → headline, {@code location.description} and
 * {@code comment} → description, {@code locationDetails.roadAddressLocation.primaryPoint.roadAddress.road} (and
 * {@code roadName}) → {@code roads}, its {@code direction} → {@code direction} ({@code pos}/{@code neg} are
 * relative to the road address, so {@code unknown}), {@code timeAndDuration.startTime}/{@code endTime} → the
 * interval, {@code language} → {@code language} ({@code fi} when absent); a feature with unit {@code km/h} →
 * {@code speedLimitKph}; the highest road-work phase {@code severity} ({@link #SEVERITY}); the first lane
 * restriction type → {@code lanesAffected}; {@code isClosure} when a feature names a road closure
 * ({@link #CLOSURE_FEATURES}) or a phase restriction is {@code ROAD_CLOSED}.
 *
 * <p>{@code contact} (phone and e-mail) is never read. A {@code trafficAnnouncementType} of {@code ended} or
 * {@code retracted}, or an {@code earlyClosing} of {@code closed} or {@code canceled}, marks the situation ended (an
 * immediate tombstone).
 */
public final class DigitrafficRoadParser {

    /** Road-work phase {@code severity} → severity. */
    public static final Map<String, String> SEVERITY = Map.of(
            "LOW", "minor",
            "HIGH", "major",
            "HIGHEST", "severe");

    /** Feature names that mean the road itself is closed (Finnish, Swedish and English announcements). */
    public static final Set<String> CLOSURE_FEATURES = Set.of(
            "tie on suljettu liikenteeltä", "tie suljettu", "vägen är avstängd för trafik", "road closed",
            "road is closed to traffic");

    /** {@code direction} → direction. */
    public static final Map<String, String> DIRECTION = Map.of(
            "BOTH", "both",
            "POS", "unknown",
            "NEG", "unknown",
            "UNKNOWN", "unknown");

    private DigitrafficRoadParser() {}

    /**
     * The category of a situation.
     *
     * @param situationType the normalized situation type ({@code ROAD_WORK}, {@code TRAFFIC_ANNOUNCEMENT}, …)
     * @param announcementType the normalized announcement type, or null
     * @param closure true when the announcement names a road closure
     */
    public static String category(String situationType, String announcementType, boolean closure) {
        if (situationType == null) {
            return "other";
        }
        return switch (situationType) {
            case "ROAD_WORK" -> "roadwork";
            case "WEIGHT_RESTRICTION", "EXEMPTED_TRANSPORT" -> "restriction";
            case "TRAFFIC_ANNOUNCEMENT" -> {
                if ("ACCIDENT_REPORT".equals(announcementType)
                        || "PRELIMINARY_ACCIDENT_REPORT".equals(announcementType)) {
                    yield "incident";
                }
                yield closure ? "closure" : "other";
            }
            default -> "other";
        };
    }

    /**
     * Parses one document.
     *
     * @param body the GeoJSON body
     * @param now the fetch time (for {@code status})
     * @throws UpstreamFormatException when the body is not a FeatureCollection
     */
    public static ParsedEvents parse(byte[] body, Instant now) {
        JsonNode root = RoadEvents.readJson(body, "the Digitraffic traffic messages");
        JsonNode features = root.get("features");
        if (!"FeatureCollection".equals(RoadEvents.text(root, "type")) || features == null || !features.isArray()) {
            throw new UpstreamFormatException("the Digitraffic traffic messages are not a FeatureCollection", "");
        }
        ParsedEvents out = new ParsedEvents();
        for (JsonNode f : features) {
            situation(f, now, out);
        }
        return out;
    }

    private static void situation(JsonNode f, Instant now, ParsedEvents out) {
        JsonNode props = f.path("properties");
        String id = RoadEvents.text(props, "situationId");
        if (id == null) {
            out.skip(ParsedEvents.MISSING_ID);
            return;
        }
        String situationType = norm(RoadEvents.text(props, "situationType"));
        String announcementType = norm(RoadEvents.text(props, "trafficAnnouncementType"));
        JsonNode announcements = props.path("announcements");
        JsonNode a = announcements.isArray() && !announcements.isEmpty() ? announcements.get(0)
                : MissingNode.getInstance();
        String early = norm(RoadEvents.text(a, "earlyClosing"));
        if ("ENDED".equals(announcementType) || "RETRACTED".equals(announcementType) || "CLOSED".equals(early)
                || "CANCELED".equals(early) || "CANCELLED".equals(early)) {
            out.ended(id);
            return;
        }
        Instant observedAt = RoadEvents.instant(RoadEvents.text(props, "versionTime"));
        if (observedAt == null) {
            out.skip(ParsedEvents.MISSING_OBSERVED_AT);
            return;
        }
        FeatureGeometry.Result geometry = RoadEvents.geometry(f.get("geometry"));
        if (geometry == null) {
            out.skip(ParsedEvents.BAD_GEOMETRY);
            return;
        }

        boolean closure = false;
        Double speed = null;
        for (JsonNode feature : a.path("features")) {
            String name = RoadEvents.text(feature, "name");
            if (name != null && CLOSURE_FEATURES.contains(name.strip().toLowerCase(Locale.ROOT))) {
                closure = true;
            }
            String unit = RoadEvents.text(feature, "unit");
            Double quantity = RoadEvents.number(feature, "quantity");
            if (unit != null && unit.replace(" ", "").equalsIgnoreCase("km/h") && quantity != null
                    && (speed == null || quantity < speed)) {
                speed = quantity;
            }
        }
        String severity = null;
        String lanes = null;
        int rank = -1;
        List<String> order = List.of("LOW", "HIGH", "HIGHEST");
        for (JsonNode phase : a.path("roadWorkPhases")) {
            String s = norm(RoadEvents.text(phase, "severity"));
            if (s != null && order.indexOf(s) > rank) {
                rank = order.indexOf(s);
                severity = SEVERITY.get(s);
            }
            for (JsonNode restriction : phase.path("restrictions")) {
                String type = norm(RoadEvents.text(restriction, "type"));
                if ("ROAD_CLOSED".equals(type)) {
                    closure = true;
                }
                if (lanes == null && type != null && type.contains("LANE")) {
                    lanes = RoadEventPayload.kebab(type);
                }
            }
        }

        JsonNode location = RoadEvents.path(a, "locationDetails", "roadAddressLocation");
        JsonNode primary = location.path("primaryPoint");
        String language = RoadEvents.text(a, "language");
        RoadEventPayload p = new RoadEventPayload("digitraffic")
                .category(category(situationType, announcementType, closure))
                .subtype("TRAFFIC_ANNOUNCEMENT".equals(situationType) && announcementType != null
                        ? announcementType : situationType)
                .severity(severity == null ? "unknown" : severity)
                .headline(trimTitle(RoadEvents.text(a, "title")))
                .description(RoadEventPayload.join(List.of(
                        nullToEmpty(RoadEvents.text(a.path("location"), "description")),
                        nullToEmpty(RoadEvents.text(a, "comment")))))
                .road(RoadEvents.text(primary.path("roadAddress"), "road"))
                .road(RoadEvents.text(primary, "roadName"))
                .lanesAffected(lanes)
                .isClosure(closure)
                .speedLimitKph(speed)
                .language(language == null ? "fi" : language);
        String direction = norm(RoadEvents.text(location, "direction"));
        p.direction(direction == null ? null : DIRECTION.getOrDefault(direction, "unknown"));
        Instant start = RoadEvents.instant(RoadEvents.text(a.path("timeAndDuration"), "startTime"));
        Instant end = RoadEvents.instant(RoadEvents.text(a.path("timeAndDuration"), "endTime"));
        p.startsAt(start).endsAt(end).statusFrom(start, now);
        if (p.headline() == null) {
            p.headline(RoadEventPayload.words(situationType));
        }
        out.add(RoadEvents.record(id, observedAt, geometry, start, end, p));
    }

    /** An enumeration value normalized to upper snake case ({@code road work} → {@code ROAD_WORK}). */
    static String norm(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.strip().toUpperCase(Locale.ROOT).replaceAll("[\\s-]+", "_");
    }

    /** The title without its trailing separator ({@code "Tie 51, Helsinki. Tietyö. "} → {@code "… Tietyö."}). */
    private static String trimTitle(String title) {
        return title == null ? null : title.strip();
    }

    private static String nullToEmpty(String text) {
        return text == null ? "" : text;
    }

    /** The list of announcement types read (for documentation and tests). */
    static final List<String> ANNOUNCEMENT_TYPES = List.of("GENERAL", "PRELIMINARY_ACCIDENT_REPORT",
            "ACCIDENT_REPORT", "UNCONFIRMED_OBSERVATION", "ENDED", "RETRACTED");
}
