package ai.foresignal.source.kit.feed;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Feed dates (prompt 073).
 *
 * <ul>
 *   <li><strong>RFC 822 / RFC 2822</strong> (RSS {@code pubDate}): an optional day name, a one- or two-digit day, a
 *       month name, a two- or four-digit year (two digits: 00–49 is 20xx, 50–99 is 19xx, as RFC 2822 §4.3 says),
 *       optional seconds, and a zone: a numeric offset, {@code GMT}/{@code UT}/{@code UTC}/{@code Z}, or a North
 *       American name ({@code EST}, {@code EDT}, {@code CST}, {@code CDT}, {@code MST}, {@code MDT}, {@code PST},
 *       {@code PDT}); a missing zone is UTC with a warning;
 *   <li><strong>RFC 3339 / ISO-8601</strong> (Atom, JSON Feed, {@code dc:date}) with an offset;
 *   <li>ISO-8601 <strong>without a zone</strong> (a local date-time or a date) is read as UTC, with a warning.
 * </ul>
 * Anything else is null, with a warning.
 */
public final class FeedDates {

    private static final Pattern RFC822 = Pattern.compile(
            "^\\s*(?:[A-Za-z]{3,9},?\\s+)?(\\d{1,2})\\s+([A-Za-z]{3,9})\\.?\\s+(\\d{2,4})\\s+(\\d{1,2}):(\\d{2})(?::(\\d{2}))?"
                    + "\\s*([A-Za-z]{1,5}|[+-]\\d{2}:?\\d{2})?\\s*$");

    private static final Map<String, Integer> MONTHS = Map.ofEntries(
            Map.entry("jan", 1), Map.entry("feb", 2), Map.entry("mar", 3), Map.entry("apr", 4), Map.entry("may", 5),
            Map.entry("jun", 6), Map.entry("jul", 7), Map.entry("aug", 8), Map.entry("sep", 9), Map.entry("oct", 10),
            Map.entry("nov", 11), Map.entry("dec", 12));

    private static final Map<String, Integer> ZONES = Map.ofEntries(
            Map.entry("GMT", 0), Map.entry("UT", 0), Map.entry("UTC", 0), Map.entry("Z", 0),
            Map.entry("EST", -5), Map.entry("EDT", -4), Map.entry("CST", -6), Map.entry("CDT", -5),
            Map.entry("MST", -7), Map.entry("MDT", -6), Map.entry("PST", -8), Map.entry("PDT", -7));

    private FeedDates() {}

    /**
     * Parses a feed date.
     *
     * @param text the date text
     * @param warnings receives a warning for a zone-less date or one that could not be read; may be null
     * @return the instant, or null
     */
    public static Instant parse(String text, List<String> warnings) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String trimmed = text.strip();
        Instant iso = iso(trimmed, warnings);
        if (iso != null) {
            return iso;
        }
        Instant rfc = rfc822(trimmed, warnings);
        if (rfc != null) {
            return rfc;
        }
        warn(warnings, "unreadable date '" + cutForMessage(trimmed) + "'");
        return null;
    }

    /** An RFC 822 date, or null. */
    static Instant rfc822(String text, List<String> warnings) {
        Matcher m = RFC822.matcher(text);
        if (!m.matches()) {
            return null;
        }
        Integer month = MONTHS.get(m.group(2).substring(0, 3).toLowerCase(Locale.ROOT));
        if (month == null) {
            return null;
        }
        int year = Integer.parseInt(m.group(3));
        if (m.group(3).length() == 2) {
            year += year < 50 ? 2000 : 1900;
        } else if (m.group(3).length() == 3) {
            year += 1900;
        }
        ZoneOffset offset;
        String zone = m.group(7);
        if (zone == null) {
            warn(warnings, "date without a zone read as UTC: '" + cutForMessage(text) + "'");
            offset = ZoneOffset.UTC;
        } else if (zone.startsWith("+") || zone.startsWith("-")) {
            String digits = zone.replace(":", "");
            int hours = Integer.parseInt(digits.substring(1, 3));
            int minutes = Integer.parseInt(digits.substring(3, 5));
            int sign = zone.startsWith("-") ? -1 : 1;
            try {
                offset = ZoneOffset.ofHoursMinutes(sign * hours, sign * minutes);
            } catch (DateTimeException e) {
                return null;
            }
        } else {
            Integer hours = ZONES.get(zone.toUpperCase(Locale.ROOT));
            if (hours == null) {
                warn(warnings, "unknown zone '" + zone + "' read as UTC");
                hours = 0;
            }
            offset = ZoneOffset.ofHours(hours);
        }
        try {
            return OffsetDateTime.of(year, month, Integer.parseInt(m.group(1)), Integer.parseInt(m.group(4)),
                    Integer.parseInt(m.group(5)), m.group(6) == null ? 0 : Integer.parseInt(m.group(6)), 0, offset)
                    .toInstant();
        } catch (DateTimeException e) {
            return null;
        }
    }

    /** An ISO-8601 date, or null. */
    static Instant iso(String text, List<String> warnings) {
        try {
            return OffsetDateTime.parse(text).toInstant();
        } catch (DateTimeParseException ignored) {
            // try the next form
        }
        try {
            return Instant.parse(text);
        } catch (DateTimeParseException ignored) {
            // try the next form
        }
        try {
            Instant local = LocalDateTime.parse(text).toInstant(ZoneOffset.UTC);
            warn(warnings, "date without a zone read as UTC: '" + cutForMessage(text) + "'");
            return local;
        } catch (DateTimeParseException ignored) {
            // try the next form
        }
        try {
            Instant day = LocalDate.parse(text).atStartOfDay(ZoneOffset.UTC).toInstant();
            warn(warnings, "date without a time read as midnight UTC: '" + cutForMessage(text) + "'");
            return day;
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private static void warn(List<String> warnings, String message) {
        if (warnings != null && warnings.size() < 50) {
            warnings.add(message);
        }
    }

    private static String cutForMessage(String text) {
        return text.length() > 60 ? text.substring(0, 60) + "…" : text;
    }
}
