package ai.foresignal.notify.mesh;

import ai.foresignal.notify.DigestBuilder;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.Truncator;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The compact text of a mesh alert (prompt 084): {@code {sev} {title} {where} {hhmm}Z #{code}}.
 *
 * <ul>
 *   <li>{@code sev} is ASCII: {@code !!!} critical, {@code !!} warning, {@code i} info (an emoji costs 4 bytes).
 *   <li>{@code title} is the message's short title (the rule name for alerts), as plain text; 018's markdown renderer
 *       is bypassed.
 *   <li>{@code where} is the alert's nearest place ({@code near.name} of the region snapshot, prompt 040), else the
 *       record's position as 2-decimal {@code lat,lon}, else nothing.
 *   <li>{@code hhmm} is the fire time in UTC.
 *   <li>{@code code} is the alert's {@link MeshShortCode}; the {@code #code} suffix is always kept whole.
 * </ul>
 *
 * <p>A resolved alert renders as {@code OK {title} #{code}} and a digest as {@code {n} alerts, top: {title} #{code}}
 * (the top item: highest severity, newest). Other messages (system events, briefings, the channel test) render as
 * {@code {sev} {title} {hhmm}Z} without a code.
 *
 * <p>The head is cut with 018's {@link Truncator} on grapheme boundaries (never inside a code point), first to the
 * character cap and then to the byte cap, each less the suffix.
 */
public final class MeshShortTextFormatter {

    /**
     * The size caps of one text.
     *
     * @param maxChars the most UTF-16 characters
     * @param maxBytes the most UTF-8 bytes
     */
    public record Limit(int maxChars, int maxBytes) {

        /**
         * A MeshCore channel text: 133 characters (the companion protocol's documented text limit) and 127 UTF-8
         * bytes (the firmware's 160-byte {@code MAX_TEXT_LEN} less the longest {@code name: } prefix it adds), so the
         * firmware never cuts the {@code #code}.
         */
        public static final Limit MESHCORE_CHANNEL = new Limit(133, 127);

        /** A MeshCore direct message: 133 characters and the firmware's 160 bytes (no name prefix). */
        public static final Limit MESHCORE_DIRECT = new Limit(133, 160);

        /** A Meshtastic text: 200 UTF-8 bytes. */
        public static final Limit MESHTASTIC = new Limit(Integer.MAX_VALUE, 200);
    }

    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HHmm", Locale.ROOT)
            .withZone(ZoneOffset.UTC);

    private MeshShortTextFormatter() {}

    /** The text of {@code m} within {@code limit}. */
    public static String format(NotificationMessage m, Limit limit) {
        String code = null;
        String head;
        if (DigestBuilder.SUBTYPE.equals(m.subtype())) {
            Map<String, Object> data = m.data() == null ? Map.of() : m.data();
            Object count = data.get("count");
            Object topTitle = data.get("topTitle");
            Object topAlert = data.get("topAlertId");
            code = topAlert instanceof Number n ? MeshShortCode.of(n.longValue()) : null;
            head = (count == null ? "?" : String.valueOf(count)) + " alerts, top: "
                    + plain(topTitle == null ? m.shortTitle() : String.valueOf(topTitle));
        } else if ("alert.resolved".equals(m.subtype())) {
            code = m.alertId() == null ? null : MeshShortCode.of(m.alertId());
            head = "OK " + plain(ruleName(m));
        } else {
            code = NotificationMessage.ALERT.equals(m.event()) && m.alertId() != null
                    ? MeshShortCode.of(m.alertId()) : null;
            List<String> parts = new ArrayList<>();
            parts.add(severity(m.severity()));
            // Alerts carry the (full) rule name, not the 40-char shortTitle; the mesh cap below trims it.
            parts.add(plain(ruleName(m)));
            if (NotificationMessage.ALERT.equals(m.event())) {
                String where = where(m.data());
                if (where != null && !where.isBlank()) {
                    parts.add(plain(where));
                }
            }
            parts.add(HHMM.format(time(m)) + "Z");
            head = String.join(" ", parts.stream().filter(p -> !p.isBlank()).toList());
        }
        String suffix = code == null ? "" : " #" + code;
        return fit(head, suffix, limit);
    }

    /** {@code head} cut to fit {@code limit} with {@code suffix} kept whole after it. */
    static String fit(String head, String suffix, Limit limit) {
        int chars = limit.maxChars() == Integer.MAX_VALUE ? Integer.MAX_VALUE
                : limit.maxChars() - Truncator.size(suffix, Truncator.Unit.CHARS);
        int bytes = limit.maxBytes() - Truncator.size(suffix, Truncator.Unit.UTF8_BYTES);
        String out = head;
        if (chars != Integer.MAX_VALUE) {
            out = Truncator.truncate(out, Math.max(0, chars), Truncator.Unit.CHARS, Truncator.Mode.PLAIN, null);
        }
        out = Truncator.truncate(out, Math.max(0, bytes), Truncator.Unit.UTF8_BYTES, Truncator.Mode.PLAIN, null);
        return (out + suffix).strip();
    }

    /** The ASCII severity marker. */
    public static String severity(String severity) {
        return switch (severity == null ? "" : severity) {
            case "critical" -> "!!!";
            case "warning" -> "!!";
            default -> "i";
        };
    }

    /** The rule name of an alert message, else its short title. */
    static String ruleName(NotificationMessage m) {
        Object name = m.data() == null ? null : m.data().get("ruleName");
        return name == null || String.valueOf(name).isBlank() ? m.shortTitle() : String.valueOf(name);
    }

    /** {@code text} on one line: controls and line breaks become spaces, runs of spaces collapse. */
    static String plain(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[\\p{Cntrl}\\u2028\\u2029]+", " ").replaceAll(" {2,}", " ").strip();
    }

    /** The fire time ({@code firedAt}), else the message's creation time, else the epoch. */
    static Instant time(NotificationMessage m) {
        Object fired = m.data() == null ? null : m.data().get("firedAt");
        if (fired != null) {
            try {
                return Instant.parse(String.valueOf(fired));
            } catch (RuntimeException ignored) {
                // fall through
            }
        }
        return m.createdAt() != null ? m.createdAt() : Instant.EPOCH;
    }

    /** The nearest place's name, else {@code lat,lon} to 2 decimals, else null. */
    static String where(Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        if (data.get("region") instanceof Map<?, ?> region && region.get("near") instanceof Map<?, ?> near
                && near.get("name") != null && !String.valueOf(near.get("name")).isBlank()) {
            return String.valueOf(near.get("name"));
        }
        double[] point = point(data);
        return point == null ? null : String.format(Locale.ROOT, "%.2f,%.2f", point[1], point[0]);
    }

    /**
     * The record's position as {@code {lon, lat}}: a Point's coordinates, else the mean of every position of another
     * geometry; null when there is none.
     */
    public static double[] point(Map<String, Object> data) {
        if (data == null || !(data.get("record") instanceof Map<?, ?> record)
                || !(record.get("geometry") instanceof Map<?, ?> geometry)) {
            return null;
        }
        List<double[]> positions = new ArrayList<>();
        collect(geometry.get("coordinates"), positions);
        if (positions.isEmpty()) {
            return null;
        }
        double lon = 0;
        double lat = 0;
        for (double[] p : positions) {
            lon += p[0];
            lat += p[1];
        }
        return new double[] {lon / positions.size(), lat / positions.size()};
    }

    private static void collect(Object coordinates, List<double[]> out) {
        if (!(coordinates instanceof List<?> list) || list.isEmpty()) {
            return;
        }
        if (list.get(0) instanceof Number && list.size() >= 2 && list.get(1) instanceof Number) {
            out.add(new double[] {((Number) list.get(0)).doubleValue(), ((Number) list.get(1)).doubleValue()});
            return;
        }
        for (Object item : list) {
            collect(item, out);
        }
    }
}
