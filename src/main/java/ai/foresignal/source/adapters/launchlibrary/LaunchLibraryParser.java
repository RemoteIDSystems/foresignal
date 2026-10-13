package ai.foresignal.source.adapters.launchlibrary;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses one page of Launch Library 2 {@code /2.3.0/launches/?mode=normal} (prompt 064;
 * {@code https://thespacedevs.com/llapi}) into {@code launch} records.
 *
 * <p>{@code externalId} = LL2 {@code id}; {@code eventTime} = {@code net}; {@code observedAt} =
 * {@code last_updated}; geometry the pad point. The pad's {@code latitude}/{@code longitude} are numbers in the
 * 2026-10-13 capture and strings in older responses; both are read. A launch without {@code id}, {@code net} or
 * {@code last_updated} is skipped.
 */
public final class LaunchLibraryParser {

    /** The record kind. */
    public static final String KIND = "launch";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /**
     * One parsed page.
     *
     * @param records the launches
     * @param next the {@code next} page URL, or null
     * @param skipped launches that could not be read
     */
    public record Page(List<NormalizedRecord> records, String next, int skipped) {

        /** Copies the list. */
        public Page {
            records = List.copyOf(records);
        }
    }

    private LaunchLibraryParser() {}

    /**
     * Parses a page.
     *
     * @throws UpstreamFormatException when the body is not an LL2 list response
     */
    public static Page parse(byte[] body) {
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (RuntimeException e) {
            throw new UpstreamFormatException("Launch Library response could not be parsed",
                    new String(body, 0, Math.min(500, body.length), StandardCharsets.UTF_8), e);
        }
        if (root == null || !root.isObject() || !root.path("results").isArray()) {
            throw new UpstreamFormatException("Launch Library response has no results array",
                    new String(body, 0, Math.min(500, body.length), StandardCharsets.UTF_8));
        }
        List<NormalizedRecord> out = new ArrayList<>();
        int skipped = 0;
        for (JsonNode launch : root.get("results")) {
            try {
                NormalizedRecord r = map(launch);
                if (r == null) {
                    skipped++;
                } else {
                    out.add(r);
                }
            } catch (DateTimeParseException | IllegalArgumentException e) {
                skipped++;
            }
        }
        JsonNode next = root.get("next");
        return new Page(out, next == null || next.isNull() ? null : next.asString(), skipped);
    }

    /** One launch, or null without id, net or last_updated. */
    static NormalizedRecord map(JsonNode l) {
        String id = text(l, "id");
        String net = text(l, "net");
        String updated = text(l, "last_updated");
        if (id == null || net == null || updated == null) {
            return null;
        }
        JsonNode mission = l.path("mission");
        JsonNode pad = l.path("pad");
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, KIND)
                .eventTime(Instant.parse(net))
                .observedAt(Instant.parse(updated))
                .put("name", text(l, "name"))
                .put("status", text(l.path("status"), "abbrev"))
                .put("net", net)
                .put("netPrecision", text(l.path("net_precision"), "name"))
                .put("windowStart", text(l, "window_start"))
                .put("windowEnd", text(l, "window_end"))
                .put("provider", text(l.path("launch_service_provider"), "name"))
                .put("rocket", rocket(l.path("rocket").path("configuration")))
                .put("missionName", text(mission, "name"))
                .put("missionType", text(mission, "type"))
                .put("orbit", text(mission.path("orbit"), "name"))
                .put("padName", text(pad, "name"))
                .put("location", text(pad.path("location"), "name"))
                .put("webcastLive", l.path("webcast_live").isBoolean() ? l.get("webcast_live").asBoolean() : null)
                .put("probability", l.path("probability").isNumber() ? l.get("probability").asInt() : null)
                .put("missionDescription", text(mission, "description"));
        Double lat = coordinate(pad.get("latitude"));
        Double lon = coordinate(pad.get("longitude"));
        if (lat != null && lon != null && Math.abs(lat) <= 90 && Math.abs(lon) <= 180) {
            b.point(lon, lat);
        }
        return b.build();
    }

    /** A pad coordinate given as a number or a numeric string; null otherwise. */
    static Double coordinate(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.asDouble();
        }
        String s = node.asString().strip();
        if (s.isEmpty()) {
            return null;
        }
        try {
            double v = Double.parseDouble(s);
            return Double.isFinite(v) ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String rocket(JsonNode configuration) {
        String full = text(configuration, "full_name");
        return full != null ? full : text(configuration, "name");
    }

    private static String text(JsonNode node, String field) {
        if (node == null) {
            return null;
        }
        JsonNode v = node.get(field);
        if (v == null || v.isNull() || v.isContainer()) {
            return null;
        }
        String s = v.asString();
        return s == null || s.isBlank() ? null : s;
    }
}
