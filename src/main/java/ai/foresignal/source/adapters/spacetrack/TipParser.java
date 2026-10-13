package ai.foresignal.source.adapters.spacetrack;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses Space-Track {@code class/tip} JSON (prompt 064) into {@code reentry-prediction} records.
 *
 * <p>Confirmed fields: {@code NORAD_CAT_ID, MSG_EPOCH, DECAY_EPOCH, WINDOW} (minutes). {@code LAT, LON, INCL,
 * DIRECTION, REV, HIGH_INTEREST, NEXT_REPORT} are unverified and read when present; the fixture is synthetic
 * until it is recaptured with a real account. Space-Track writes values as strings and times as
 * {@code yyyy-MM-dd HH:mm:ss} in UTC. When a predicted position is present it becomes the geometry, with
 * {@code geometryNote} saying it is the position at 10 km altitude, not an impact point.
 */
public final class TipParser {

    /** The record kind. */
    public static final String KIND = "reentry-prediction";

    /** The note carried with a predicted position. */
    public static final String GEOMETRY_NOTE = "predicted position at 10 km altitude, not an impact point";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSSSSS][.SSS][.S]", Locale.ROOT);

    /**
     * What a parse produced.
     *
     * @param records one record per message, newest message per object last
     * @param skipped messages that could not be read
     */
    public record Parsed(List<NormalizedRecord> records, int skipped) {

        /** Copies the list. */
        public Parsed {
            records = List.copyOf(records);
        }
    }

    private TipParser() {}

    /**
     * Parses a {@code class/tip/…/format/json} response: a JSON array of messages.
     *
     * @throws UpstreamFormatException when the body is not a JSON array
     */
    public static Parsed parse(byte[] body) {
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (RuntimeException e) {
            throw new UpstreamFormatException("Space-Track TIP response could not be parsed", snippet(body), e);
        }
        if (root == null || !root.isArray()) {
            throw new UpstreamFormatException("Space-Track TIP response is not an array", snippet(body));
        }
        List<NormalizedRecord> out = new ArrayList<>();
        int skipped = 0;
        for (JsonNode m : root) {
            try {
                NormalizedRecord r = map(m);
                if (r == null) {
                    skipped++;
                } else {
                    out.add(r);
                }
            } catch (IllegalArgumentException | DateTimeParseException e) {
                skipped++;
            }
        }
        return new Parsed(out, skipped);
    }

    /** One message, or null without the confirmed fields. */
    static NormalizedRecord map(JsonNode m) {
        String norad = text(m, "NORAD_CAT_ID");
        String msgEpoch = text(m, "MSG_EPOCH");
        String decayEpoch = text(m, "DECAY_EPOCH");
        if (norad == null || msgEpoch == null || decayEpoch == null) {
            return null;
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(Long.toString(Long.parseLong(norad)), KIND)
                .observedAt(time(msgEpoch))
                .eventTime(time(decayEpoch))
                .put("window", integer(text(m, "WINDOW")))
                .put("inclination", number(text(m, "INCL")))
                .put("direction", text(m, "DIRECTION"))
                .put("rev", integer(text(m, "REV")))
                .put("highInterest", flag(text(m, "HIGH_INTEREST")))
                .put("nextReport", integer(text(m, "NEXT_REPORT")));
        Double lat = number(text(m, "LAT"));
        Double lon = number(text(m, "LON"));
        if (lat != null && lon != null && Math.abs(lat) <= 90 && Math.abs(lon) <= 180) {
            b.point(lon, lat).put("lat", lat).put("lon", lon).put("geometryNote", GEOMETRY_NOTE);
        }
        return b.build();
    }

    /** {@code yyyy-MM-dd HH:mm:ss} (UTC) or ISO-8601 → an instant. */
    static Instant time(String text) {
        String t = text.strip();
        if (t.endsWith("Z")) {
            return Instant.parse(t);
        }
        if (t.contains("T")) {
            return LocalDateTime.parse(t).toInstant(ZoneOffset.UTC);
        }
        return LocalDateTime.parse(t, TIME).toInstant(ZoneOffset.UTC);
    }

    private static Integer integer(String text) {
        Double d = number(text);
        return d == null ? null : (int) Math.round(d);
    }

    private static Double number(String text) {
        if (text == null) {
            return null;
        }
        try {
            double d = Double.parseDouble(text);
            return Double.isFinite(d) ? d : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Boolean flag(String text) {
        if (text == null) {
            return null;
        }
        return "Y".equalsIgnoreCase(text) || "true".equalsIgnoreCase(text) || "1".equals(text);
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull() || v.isContainer()) {
            return null;
        }
        String s = v.asString().strip();
        return s.isEmpty() ? null : s;
    }

    private static String snippet(byte[] body) {
        return new String(body, 0, Math.min(body.length, 500), StandardCharsets.UTF_8);
    }
}
