package ai.foresignal.ogc;

import ai.foresignal.api.error.ValidationException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

/**
 * The OGC API {@code datetime} parameter (17-069r4 §7.15.4): an instant, a closed interval {@code a/b} or a
 * half-bounded interval {@code ../b} or {@code a/..} (an empty end counts as open). A date ({@code YYYY-MM-DD})
 * stands for the whole UTC day: as an instant it matches that day, as a start it is the day's first moment, as
 * an end its last.
 *
 * @param from the inclusive start, or null when open
 * @param to the inclusive end, or null when open
 */
public record OgcDatetime(Instant from, Instant to) {

    /**
     * Parses the parameter.
     *
     * @return the interval, or null when {@code raw} is null or blank
     * @throws ValidationException when the value is malformed or the end precedes the start
     */
    public static OgcDatetime parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String value = raw.strip();
        int slash = value.indexOf('/');
        if (slash < 0) {
            if (isDate(value)) {
                LocalDate day = date(value);
                return new OgcDatetime(day.atStartOfDay(ZoneOffset.UTC).toInstant(),
                        day.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().minusNanos(1000));
            }
            Instant at = instant(value);
            return new OgcDatetime(at, at);
        }
        if (value.indexOf('/', slash + 1) >= 0) {
            throw invalid("has more than one '/'");
        }
        String a = value.substring(0, slash).strip();
        String b = value.substring(slash + 1).strip();
        Instant from = open(a) ? null : isDate(a) ? date(a).atStartOfDay(ZoneOffset.UTC).toInstant() : instant(a);
        Instant to = open(b) ? null : isDate(b)
                ? date(b).plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().minusNanos(1000) : instant(b);
        if (from == null && to == null) {
            throw invalid("must bound at least one end");
        }
        if (from != null && to != null && to.isBefore(from)) {
            throw invalid("ends before it starts");
        }
        return new OgcDatetime(from, to);
    }

    /** True for an instant (both ends equal). */
    public boolean isInstant() {
        return from != null && from.equals(to);
    }

    private static boolean open(String end) {
        return end.isEmpty() || "..".equals(end);
    }

    private static boolean isDate(String value) {
        return value.length() == 10 && value.charAt(4) == '-' && value.charAt(7) == '-';
    }

    private static LocalDate date(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw invalid("'" + value + "' is not a date");
        }
    }

    private static Instant instant(String value) {
        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (DateTimeParseException e) {
            try {
                return Instant.parse(value);
            } catch (DateTimeParseException e2) {
                throw invalid("'" + value + "' is not an RFC 3339 date-time");
            }
        }
    }

    private static ValidationException invalid(String why) {
        return ValidationException.forField("datetime", why);
    }
}
