package ai.foresignal.source.adapters.radio;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;

/**
 * The public KiwiSDR receiver list ({@code kiwisdr_com.js}) as {@code sdr-receiver} records (prompt 082). Pure.
 *
 * <ul>
 *   <li><strong>Input:</strong> a JavaScript array literal read by {@link LenientJsLiteralParser}; every value in it
 *       is a string.
 *   <li><strong>Identity:</strong> {@code externalId = id}; an entry without one is skipped, and a repeated id is
 *       kept once.
 *   <li><strong>Geometry:</strong> {@code gps}, written {@code "(lat, lon)"}; none when missing or out of range.
 *   <li><strong>Payload:</strong> {@code name, url, antenna, sdrHw} (trimmed text), {@code bandsHz} (the
 *       {@code bands} range {@code "lo-hi"} as {@code [lo, hi]} in Hz), {@code usersMax} (integer), {@code online}
 *       ({@code offline == "no"}), and the volatile {@code users} (integer) and {@code snr} (the list of integers in
 *       {@code "all,hf"}, dB).
 *   <li><strong>Time:</strong> the source declares {@code observedAtMode} RECEIVED; no record sets {@code observedAt}.
 * </ul>
 */
public final class KiwiSdrParser {

    /** The record kind. */
    public static final String KIND = "sdr-receiver";

    /** An entry without an id. */
    static final String MISSING_ID = "missing-id";

    /** A repeated id. */
    static final String DUPLICATE = "duplicate-id";

    /** The longest text value kept. */
    static final int MAX_TEXT = 256;

    private static final Pattern GPS = Pattern.compile("^\\(\\s*(-?\\d+(?:\\.\\d+)?)\\s*,\\s*(-?\\d+(?:\\.\\d+)?)\\s*\\)$");

    private static final Pattern RANGE = Pattern.compile("^\\s*(\\d+)\\s*-\\s*(\\d+)\\s*$");

    private KiwiSdrParser() {}

    /**
     * The parse result.
     *
     * @param records the receivers
     * @param entries the array's entries
     * @param skipped skip reason to count
     */
    public record Result(List<NormalizedRecord> records, int entries, Map<String, Integer> skipped) {

        /** Copies the collections. */
        public Result {
            records = List.copyOf(records);
            skipped = Map.copyOf(skipped);
        }
    }

    /**
     * Parses the file.
     *
     * @throws UpstreamFormatException when it is not a JavaScript array literal
     */
    public static Result parse(byte[] body) {
        JsonNode root = LenientJsLiteralParser.parse(body);
        if (!root.isArray()) {
            throw new UpstreamFormatException("The KiwiSDR list is not an array", "");
        }
        List<NormalizedRecord> records = new ArrayList<>();
        Map<String, Integer> skipped = new TreeMap<>();
        Set<String> ids = new LinkedHashSet<>();
        for (JsonNode entry : root) {
            String id = text(entry.get("id"));
            if (id == null) {
                skipped.merge(MISSING_ID, 1, Integer::sum);
                continue;
            }
            if (!ids.add(id)) {
                skipped.merge(DUPLICATE, 1, Integer::sum);
                continue;
            }
            NormalizedRecord.Builder b = NormalizedRecord.builder(id, KIND)
                    .put("name", text(entry.get("name")))
                    .put("url", url(text(entry.get("url"))))
                    .put("antenna", text(entry.get("antenna")))
                    .put("bandsHz", range(text(entry.get("bands"))))
                    .put("usersMax", integer(text(entry.get("users_max"))))
                    .put("sdrHw", text(entry.get("sdr_hw")))
                    .put("online", text(entry.get("offline")) == null ? null : "no".equalsIgnoreCase(
                            text(entry.get("offline"))))
                    .put("users", integer(text(entry.get("users"))))
                    .put("snr", integers(text(entry.get("snr"))));
            double[] point = gps(text(entry.get("gps")));
            if (point != null) {
                b.point(point[1], point[0]);
            }
            records.add(b.build());
        }
        return new Result(records, root.size(), skipped);
    }

    /** {@code [lat, lon]} from {@code "(lat, lon)"}, or null when missing or out of range. */
    static double[] gps(String text) {
        if (text == null) {
            return null;
        }
        Matcher m = GPS.matcher(text);
        if (!m.matches()) {
            return null;
        }
        double lat = Double.parseDouble(m.group(1));
        double lon = Double.parseDouble(m.group(2));
        if (Math.abs(lat) > 90 || Math.abs(lon) > 180) {
            return null;
        }
        return new double[] {lat, lon};
    }

    /** {@code [lo, hi]} from {@code "lo-hi"}, or null. */
    static List<Long> range(String text) {
        if (text == null) {
            return null;
        }
        Matcher m = RANGE.matcher(text);
        if (!m.matches()) {
            return null;
        }
        long lo = Long.parseLong(m.group(1));
        long hi = Long.parseLong(m.group(2));
        return lo <= hi ? List.of(lo, hi) : null;
    }

    /** The comma-separated integers of {@code text}, or null when any is not an integer. */
    static List<Integer> integers(String text) {
        if (text == null) {
            return null;
        }
        List<Integer> out = new ArrayList<>();
        for (String part : text.split(",")) {
            Integer value = integer(part.strip());
            if (value == null) {
                return null;
            }
            out.add(value);
        }
        return out.isEmpty() ? null : out;
    }

    private static Integer integer(String text) {
        if (text == null) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** An absolute http(s) URL, or null. */
    private static String url(String text) {
        if (text == null) {
            return null;
        }
        String lower = text.toLowerCase(java.util.Locale.ROOT);
        return lower.startsWith("http://") || lower.startsWith("https://") ? text : null;
    }

    /** Trimmed text without control characters, at most {@value #MAX_TEXT} characters, or null when blank. */
    static String text(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode() || node.isContainer()) {
            return null;
        }
        StringBuilder out = new StringBuilder();
        node.asString().codePoints().filter(cp -> !Character.isISOControl(cp)).forEach(out::appendCodePoint);
        String value = out.toString().strip();
        if (value.isEmpty()) {
            return null;
        }
        return value.length() <= MAX_TEXT ? value : value.substring(0, value.offsetByCodePoints(0,
                Math.min(MAX_TEXT, value.codePointCount(0, value.length())))).strip();
    }
}
