package ai.foresignal.source.series;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Turns an upstream period label into a start and end instant at a fixed frequency (prompt 093).
 *
 * <p>EIA gives daily {@code YYYY-MM-DD} and hourly {@code YYYY-MM-DDTHH} periods, AGSI a gas day, Ember a month
 * {@code YYYY-MM} and the Carbon Intensity API ISO instants. The frequency is fixed per source, so the same label
 * always maps to the same window.
 */
public final class SeriesPeriods {

    private SeriesPeriods() {}

    /** One period's ISO-8601 bounds. */
    public record Period(Instant start, Instant end) {}

    /**
     * The window of {@code period} at {@code frequency}.
     *
     * @param frequency one of {@code PT15S, PT15M, PT30M, PT1H, P1D, P1W, P1M}, or (prompt 117) {@code P3M} and
     *     {@code P1Y}; the label is the period's start (a bare year {@code YYYY} is read as January 1)
     * @param period the upstream label
     * @return the start and end, or null when the label cannot be read
     * @see #weekEnding(String)
     */
    public static Period parse(String frequency, String period) {
        if (frequency == null || period == null || period.isBlank()) {
            return null;
        }
        String text = period.strip();
        Instant start = start(text);
        if (start == null) {
            return null;
        }
        return switch (frequency) {
            case "PT15S" -> new Period(start, start.plusSeconds(15));
            case "PT15M" -> new Period(start, start.plusSeconds(900));
            case "PT30M" -> new Period(start, start.plusSeconds(1800));
            case "PT1H" -> new Period(start, start.plus(1, ChronoUnit.HOURS));
            case "P1D" -> new Period(start, start.plus(1, ChronoUnit.DAYS));
            case "P1W" -> new Period(start, start.plus(7, ChronoUnit.DAYS));
            case "P1M" -> new Period(start, start.atOffset(ZoneOffset.UTC).plusMonths(1).toInstant());
            case "P3M" -> new Period(start, start.atOffset(ZoneOffset.UTC).plusMonths(3).toInstant());
            case "P1Y" -> new Period(start, start.atOffset(ZoneOffset.UTC).plusYears(1).toInstant());
            default -> null;
        };
    }

    /**
     * The seven-day window of a week labeled by its <em>last</em> day, as EIA labels its weekly series (the WPSR
     * week ending Friday): the period runs from six days before the label to the end of the label day, so
     * {@code observedAt = periodEnd} is never in the future of the week it describes.
     *
     * @param period a {@code YYYY-MM-DD} week-ending date
     * @return the start and end, or null when the label is not a date
     */
    public static Period weekEnding(String period) {
        if (period == null || !period.strip().matches("\\d{4}-\\d{2}-\\d{2}")) {
            return null;
        }
        try {
            LocalDate last = LocalDate.parse(period.strip());
            return new Period(last.minusDays(6).atStartOfDay(ZoneOffset.UTC).toInstant(),
                    last.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static Instant start(String text) {
        try {
            if (text.matches("\\d{4}")) {
                return LocalDate.of(Integer.parseInt(text), 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant();
            }
            if (text.matches("\\d{4}-\\d{2}")) {
                return YearMonth.parse(text).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            }
            if (text.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDate.parse(text).atStartOfDay(ZoneOffset.UTC).toInstant();
            }
            if (text.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}$")) {
                return LocalDateTime.parse(text + ":00:00").toInstant(ZoneOffset.UTC);
            }
            if (text.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}$")) {
                return LocalDateTime.parse(text + ":00").toInstant(ZoneOffset.UTC);
            }
            if (text.endsWith("Z") || text.matches(".*[+-]\\d{2}:?\\d{2}$")) {
                return OffsetDateTime.parse(text).toInstant();
            }
            return LocalDateTime.parse(text).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
