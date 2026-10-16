package ai.foresignal.ical;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A hand-written RFC 5545 writer for Foresignal's calendar feeds (prompt 112): {@code VCALENDAR} with
 * {@code PRODID}, {@code VERSION:2.0}, {@code CALSCALE:GREGORIAN}, {@code METHOD:PUBLISH}, {@code X-WR-CALNAME},
 * {@code REFRESH-INTERVAL;VALUE=DURATION:PT15M} (RFC 7986) and {@code X-PUBLISHED-TTL:PT15M}, then one
 * {@code VEVENT} per event.
 *
 * <p>Times are UTC with {@code Z}; lines end with CRLF and are folded at 75 octets without splitting a UTF-8
 * sequence (RFC 5545 §3.1); TEXT values escape backslash, semicolon, comma and newlines (§3.3.11).
 */
public final class ICalWriter {

    /** The iCalendar media type. */
    public static final String MEDIA_TYPE = "text/calendar";

    /** The most octets on one line, excluding the CRLF. */
    public static final int MAX_OCTETS = 75;

    /** How often clients should refresh. */
    public static final String REFRESH = "PT15M";

    private static final DateTimeFormatter UTC = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'", Locale.ROOT)
            .withZone(ZoneOffset.UTC);

    private ICalWriter() {}

    /**
     * One event.
     *
     * @param uid the stable UID
     * @param sequence the revision ({@code SEQUENCE}): the record version
     * @param dtstamp {@code DTSTAMP}: when this revision was ingested
     * @param lastModified {@code LAST-MODIFIED}, or null
     * @param start {@code DTSTART}
     * @param end {@code DTEND}, or null (an open-ended event)
     * @param summary {@code SUMMARY}
     * @param description {@code DESCRIPTION}, or null
     * @param location {@code LOCATION}, or null
     * @param geo {@code GEO} as {@code [lat, lon]}, or null
     * @param url {@code URL}, or null
     * @param status {@code CONFIRMED}, {@code TENTATIVE} or {@code CANCELLED}, or null
     * @param extra further properties ({@code X-…}), in order
     */
    public record Event(String uid, int sequence, Instant dtstamp, Instant lastModified, Instant start, Instant end,
            String summary, String description, String location, double[] geo, String url, String status,
            Map<String, String> extra) {

        /** Copies the extra properties, keeping their order. */
        public Event {
            extra = extra == null ? Map.of() : java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(extra));
        }
    }

    /**
     * The whole calendar.
     *
     * @param version the Foresignal version (in {@code PRODID})
     * @param name {@code X-WR-CALNAME}
     * @param events the events
     * @param truncated true to add {@code X-FORESIGNAL-TRUNCATED:TRUE}
     * @return the document
     */
    public static String write(String version, String name, List<Event> events, boolean truncated) {
        StringBuilder out = new StringBuilder(4096);
        line(out, "BEGIN:VCALENDAR");
        line(out, "PRODID:-//Foresignal//" + version + "//EN");
        line(out, "VERSION:2.0");
        line(out, "CALSCALE:GREGORIAN");
        line(out, "METHOD:PUBLISH");
        line(out, "X-WR-CALNAME:" + escape(name));
        line(out, "REFRESH-INTERVAL;VALUE=DURATION:" + REFRESH);
        line(out, "X-PUBLISHED-TTL:" + REFRESH);
        if (truncated) {
            line(out, "X-FORESIGNAL-TRUNCATED:TRUE");
        }
        for (Event e : events) {
            event(out, e);
        }
        line(out, "END:VCALENDAR");
        return out.toString();
    }

    private static void event(StringBuilder out, Event e) {
        line(out, "BEGIN:VEVENT");
        line(out, "UID:" + escape(e.uid()));
        line(out, "DTSTAMP:" + time(e.dtstamp()));
        line(out, "SEQUENCE:" + Math.max(0, e.sequence()));
        if (e.lastModified() != null) {
            line(out, "LAST-MODIFIED:" + time(e.lastModified()));
        }
        line(out, "DTSTART:" + time(e.start()));
        if (e.end() != null && e.end().isAfter(e.start())) {
            line(out, "DTEND:" + time(e.end()));
        }
        line(out, "SUMMARY:" + escape(e.summary() == null ? "" : e.summary()));
        if (e.description() != null) {
            line(out, "DESCRIPTION:" + escape(e.description()));
        }
        if (e.location() != null) {
            line(out, "LOCATION:" + escape(e.location()));
        }
        if (e.geo() != null) {
            line(out, String.format(Locale.ROOT, "GEO:%.6f;%.6f", e.geo()[0], e.geo()[1]));
        }
        if (e.url() != null) {
            line(out, "URL:" + e.url());
        }
        if (e.status() != null) {
            line(out, "STATUS:" + e.status());
        }
        for (Map.Entry<String, String> x : e.extra().entrySet()) {
            line(out, x.getKey() + ":" + escape(x.getValue()));
        }
        line(out, "END:VEVENT");
    }

    /** An instant as a UTC DATE-TIME ({@code 20261016T120000Z}). */
    public static String time(Instant instant) {
        return UTC.format((instant == null ? Instant.EPOCH : instant).truncatedTo(ChronoUnit.SECONDS));
    }

    /** Escapes a TEXT value: {@code \ ; ,} and newlines (CR, LF, CRLF become {@code \n}). */
    public static String escape(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length() + 8);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case ';' -> sb.append("\\;");
                case ',' -> sb.append("\\,");
                case '\r' -> {
                    sb.append("\\n");
                    if (i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                        i++;
                    }
                }
                case '\n' -> sb.append("\\n");
                default -> {
                    if (c >= 0x20 || c == '\t') {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    /** Appends one content line, folded at {@link #MAX_OCTETS} octets, with CRLF. */
    static void line(StringBuilder out, String content) {
        out.append(fold(content)).append("\r\n");
    }

    /**
     * Folds a content line: no physical line exceeds {@link #MAX_OCTETS} octets of UTF-8 (a continuation line's
     * leading space included), and a break never falls inside a UTF-8 sequence or a surrogate pair.
     */
    public static String fold(String content) {
        StringBuilder out = new StringBuilder(content.length() + 16);
        int octets = 0;
        int limit = MAX_OCTETS;
        int i = 0;
        while (i < content.length()) {
            int cp = content.codePointAt(i);
            int chars = Character.charCount(cp);
            int size = new String(Character.toChars(cp)).getBytes(StandardCharsets.UTF_8).length;
            if (octets + size > limit) {
                out.append("\r\n ");
                octets = 1;
                limit = MAX_OCTETS;
            }
            out.appendCodePoint(cp);
            octets += size;
            i += chars;
        }
        return out.toString();
    }
}
