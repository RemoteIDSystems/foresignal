package ai.foresignal.source.adapters.space.swpc;

import ai.foresignal.source.adapters.space.SpaceSources;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import ai.foresignal.source.support.MappedRecord;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * SWPC space weather messages ({@code products/alerts.json}, {@code [{product_id, issue_datetime, message}]}) as
 * {@code space-weather-alert} records (prompt 077). Pure.
 *
 * <ul>
 *   <li><strong>Header lines</strong> read from the message text: {@code Space Weather Message Code:}
 *       ({@code ALTK05}, {@code WARK04}, {@code WATA20}, {@code SUMX01}), {@code Serial Number:},
 *       {@code NOAA Scale:} (also written {@code Noaa Scale:}), {@code Valid From:}, {@code Valid To:} (or
 *       {@code Now Valid Until:} on an extended warning), {@code Threshold Reached:}, {@code Begin Time:} and
 *       {@code Cancel Serial Number:}. Times are {@code 2026 Oct 13 1431 UTC}.
 *   <li><strong>Identity:</strong> {@code externalId = code + ":" + serial}; a message without both is skipped
 *       ({@value MappedRecord#MISSING_ID}). SWPC reuses a serial for a continued or corrected message (three
 *       {@code ALTEF3:3752} on 2026-10-13): the newest issue is kept and the others counted as
 *       {@value MappedRecord#DUPLICATE_ID}.
 *   <li><strong>Category</strong> from the code prefix: {@code ALT} alert, {@code WAR} warning, {@code WAT} watch,
 *       {@code SUM} summary, {@code CAN} cancel, else {@code other}. A message whose body has a {@code CANCEL …:}
 *       line or a {@code Cancel Serial Number:} is a cancel (SWPC sends a canceled watch under the watch's own
 *       code, {@code WATA30}).
 *   <li><strong>Time:</strong> {@code observedAt = issue_datetime} (UTC); {@code eventTime} = threshold reached,
 *       else begin time; {@code validFrom}/{@code validUntil} from the upstream window (a window whose start is
 *       after its end keeps only the end).
 *   <li><strong>Payload:</strong> {@code code, category, serial, noaaScale} (the scale code, {@code G1},
 *       {@code R2}, {@code S1}), {@code kIndex} (from {@code K-index of N}), {@code message} (CR/LF normalized to
 *       LF, at most {@value #MAX_MESSAGE} characters).
 *   <li><strong>Cancel:</strong> a cancel message tombstones {@code code + ":" + cancelSerial}; the canceled
 *       message itself, when it is still in the file, is not emitted.
 * </ul>
 */
public final class SwpcMessageParser {

    /** The record kind. */
    public static final String KIND = "space-weather-alert";

    /** The longest message kept, in characters. */
    public static final int MAX_MESSAGE = 4096;

    /** {@code 2026 Oct 13 1431 UTC}. */
    static final DateTimeFormatter MESSAGE_TIME = new DateTimeFormatterBuilder().parseCaseInsensitive()
            .appendPattern("uuuu MMM d HHmm").toFormatter(Locale.US);

    private static final Pattern HEADER = Pattern.compile("^([A-Za-z][A-Za-z0-9 ]*?)\\s*:\\s*(.*)$");
    private static final Pattern K_INDEX = Pattern.compile("K-index of (\\d)");
    private static final Pattern SCALE = Pattern.compile("^([GRS][0-5])\\b");
    private static final Pattern CANCEL_LINE = Pattern.compile("^CANCEL\\b[^:]*:", Pattern.CASE_INSENSITIVE);
    private static final Pattern CODE = Pattern.compile("^[A-Z0-9]{4,8}$");
    private static final JsonMapper JSON = JsonMapper.builder().build();

    /**
     * One parsed file.
     *
     * @param records one record per message, canceled messages left out, plus a tombstone per canceled id
     * @param canceled the ids cancel messages tombstone
     * @param skipped skip reason to count
     */
    public record Messages(List<NormalizedRecord> records, Set<String> canceled, Map<String, Integer> skipped) {}

    /**
     * The header fields of one message.
     *
     * @param code the message code
     * @param serial the serial number
     * @param category alert, warning, watch, summary, cancel or other
     * @param noaaScale the scale code, or null
     * @param kIndex the K-index named in the message, or null
     * @param validFrom the upstream window start, or null
     * @param validUntil the upstream window end, or null
     * @param eventTime threshold reached or begin time, or null
     * @param cancelSerial the serial a cancel message cancels, or null
     * @param message the normalized, cut message text
     */
    public record Header(String code, Integer serial, String category, String noaaScale, Integer kIndex,
            Instant validFrom, Instant validUntil, Instant eventTime, Integer cancelSerial, String message) {

        /** {@code code:serial}, or null when either is missing. */
        public String externalId() {
            return code == null || serial == null ? null : code + ":" + serial;
        }

        /** True for a cancel message. */
        public boolean cancel() {
            return "cancel".equals(category);
        }
    }

    private SwpcMessageParser() {}

    /**
     * Parses {@code alerts.json}.
     *
     * @throws UpstreamFormatException when the body is not a JSON array
     */
    public static Messages parse(byte[] body) {
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("SWPC alerts are not JSON: " + e.getOriginalMessage(), "", e);
        }
        if (root == null || !root.isArray()) {
            throw new UpstreamFormatException("SWPC alerts are not a JSON array", "");
        }
        Map<String, Integer> skipped = new TreeMap<>();
        Map<String, NormalizedRecord> byId = new LinkedHashMap<>();
        Map<String, Instant> canceled = new LinkedHashMap<>();
        for (JsonNode item : root) {
            Instant issued = SpaceSources.utc(JsonProps.text(item, "issue_datetime"));
            Header header = header(JsonProps.text(item, "message"));
            String id = header.externalId();
            if (id == null) {
                skipped.merge(MappedRecord.MISSING_ID, 1, Integer::sum);
                continue;
            }
            if (issued == null) {
                skipped.merge(MappedRecord.MISSING_OBSERVED_AT, 1, Integer::sum);
                continue;
            }
            NormalizedRecord previous = byId.get(id);
            if (previous != null) {
                // SWPC reuses a serial for a corrected or continued message: the newest issue wins.
                skipped.merge(MappedRecord.DUPLICATE_ID, 1, Integer::sum);
                if (!issued.isAfter(previous.observedAt())) {
                    continue;
                }
            }
            byId.put(id, record(header, issued));
            if (header.cancel() && header.cancelSerial() != null) {
                canceled.merge(header.code() + ":" + header.cancelSerial(), issued,
                        (a, b) -> a.isAfter(b) ? a : b);
            }
        }
        List<NormalizedRecord> out = new ArrayList<>();
        for (NormalizedRecord r : byId.values()) {
            if (canceled.containsKey(r.externalId())) {
                skipped.merge("canceled", 1, Integer::sum);
            } else {
                out.add(r);
            }
        }
        canceled.forEach((id, at) -> out.add(NormalizedRecord.tombstone(id, KIND, at)));
        return new Messages(out, new LinkedHashSet<>(canceled.keySet()), skipped);
    }

    /** The record of one message (see the class note). */
    public static NormalizedRecord record(Header h, Instant issued) {
        Instant from = h.validFrom();
        if (from != null && h.validUntil() != null && from.isAfter(h.validUntil())) {
            from = null;
        }
        return NormalizedRecord.builder(h.externalId(), KIND)
                .observedAt(issued)
                .eventTime(h.eventTime())
                .validFrom(from)
                .validUntil(h.validUntil())
                .put("code", h.code())
                .put("category", h.category())
                .put("serial", h.serial())
                .put("noaaScale", h.noaaScale())
                .put("kIndex", h.kIndex())
                .put("message", h.message())
                .build();
    }

    /** The header fields of a message text (null text gives an empty header). */
    public static Header header(String message) {
        if (message == null) {
            return new Header(null, null, null, null, null, null, null, null, null, null);
        }
        String normalized = message.replace("\r\n", "\n").replace('\r', '\n');
        String code = null;
        Integer serial = null;
        String scale = null;
        Instant validFrom = null;
        Instant validUntil = null;
        Instant threshold = null;
        Instant begin = null;
        Integer cancelSerial = null;
        boolean cancelLine = false;
        for (String raw : normalized.split("\n")) {
            String line = raw.strip();
            if (CANCEL_LINE.matcher(line).find()) {
                cancelLine = true;
            }
            Matcher m = HEADER.matcher(line);
            if (!m.matches()) {
                continue;
            }
            String key = m.group(1).strip().toLowerCase(Locale.ROOT);
            String value = m.group(2).strip();
            switch (key) {
                case "space weather message code" -> code = code == null && CODE.matcher(value).matches() ? value : code;
                case "serial number" -> serial = serial == null ? integer(value) : serial;
                case "noaa scale" -> scale = scale == null ? scale(value) : scale;
                case "valid from" -> validFrom = validFrom == null ? time(value) : validFrom;
                case "valid to", "now valid until" -> validUntil = validUntil == null ? time(value) : validUntil;
                case "threshold reached" -> threshold = threshold == null ? time(value) : threshold;
                case "begin time" -> begin = begin == null ? time(value) : begin;
                case "cancel serial number" -> cancelSerial = cancelSerial == null ? integer(value) : cancelSerial;
                default -> {
                    // other header lines are part of the message text only
                }
            }
        }
        Matcher k = K_INDEX.matcher(normalized);
        Integer kIndex = k.find() ? Integer.valueOf(k.group(1)) : null;
        String category = cancelLine || cancelSerial != null ? "cancel" : category(code);
        return new Header(code, serial, category, scale, kIndex, validFrom, validUntil,
                threshold != null ? threshold : begin, cancelSerial, SpaceSources.cut(normalized, MAX_MESSAGE));
    }

    /** The category of a message code prefix. */
    static String category(String code) {
        if (code == null || code.length() < 3) {
            return "other";
        }
        return switch (code.substring(0, 3)) {
            case "ALT" -> "alert";
            case "WAR" -> "warning";
            case "WAT" -> "watch";
            case "SUM" -> "summary";
            case "CAN" -> "cancel";
            default -> "other";
        };
    }

    /** {@code G1 - Minor} → {@code G1}; null when the value names no scale. */
    static String scale(String value) {
        Matcher m = SCALE.matcher(value.strip().toUpperCase(Locale.ROOT));
        return m.find() ? m.group(1) : null;
    }

    /** {@code 2026 Oct 13 1431 UTC} as an instant, or null. */
    static Instant time(String value) {
        String s = value.strip();
        if (s.toUpperCase(Locale.ROOT).endsWith("UTC")) {
            s = s.substring(0, s.length() - 3).strip();
        }
        s = s.replaceAll("\\s+", " ");
        try {
            return LocalDateTime.parse(s, MESSAGE_TIME).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static Integer integer(String value) {
        try {
            return Integer.valueOf(value.strip());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
