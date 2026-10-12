package ai.foresignal.inbound.edp;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses an observation's {@code t} (docs/External-Data-Protocol.md §3): {@code YYYY},
 * {@code YYYY-Qn}, {@code YYYY-MM}, {@code YYYY-Www} (ISO week), {@code YYYY-MM-DD}, or an RFC 3339
 * timestamp with an offset. Periods are UTC intervals {@code [start, end)}; an instant is the
 * degenerate interval {@code [t, t]}.
 */
public final class EdpPeriods {

    private static final Pattern YEAR = Pattern.compile("^(\\d{4})$");
    private static final Pattern QUARTER = Pattern.compile("^(\\d{4})-Q([1-4])$");
    private static final Pattern MONTH = Pattern.compile("^(\\d{4})-(0[1-9]|1[0-2])$");
    private static final Pattern WEEK = Pattern.compile("^(\\d{4})-W(0[1-9]|[1-4]\\d|5[0-3])$");
    private static final Pattern DAY = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final Pattern INSTANT =
            Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2}(\\.\\d+)?)?(Z|[+-]\\d{2}:\\d{2})$");

    private EdpPeriods() {}

    /**
     * A parsed period.
     *
     * @param granularity {@code year}, {@code quarter}, {@code month}, {@code week}, {@code day} or
     *     {@code instant}
     * @param start the inclusive start, UTC
     * @param end the exclusive end (equal to start for an instant)
     */
    public record Period(String granularity, Instant start, Instant end) {}

    /** Parses {@code t}; null when it is not one of the accepted forms or not a real date. */
    public static Period parse(String t) {
        if (t == null) {
            return null;
        }
        try {
            Matcher m;
            if ((m = YEAR.matcher(t)).matches()) {
                LocalDate start = LocalDate.of(Integer.parseInt(m.group(1)), 1, 1);
                return period("year", start, start.plusYears(1));
            }
            if ((m = QUARTER.matcher(t)).matches()) {
                int quarter = Integer.parseInt(m.group(2));
                LocalDate start = LocalDate.of(Integer.parseInt(m.group(1)), (quarter - 1) * 3 + 1, 1);
                return period("quarter", start, start.plusMonths(3));
            }
            if ((m = MONTH.matcher(t)).matches()) {
                YearMonth month = YearMonth.of(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
                return period("month", month.atDay(1), month.plusMonths(1).atDay(1));
            }
            if ((m = WEEK.matcher(t)).matches()) {
                int year = Integer.parseInt(m.group(1));
                int week = Integer.parseInt(m.group(2));
                LocalDate jan4 = LocalDate.of(year, 1, 4);
                if (week > jan4.range(IsoFields.WEEK_OF_WEEK_BASED_YEAR).getMaximum()) {
                    return null;
                }
                LocalDate start = jan4.with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
                        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                return period("week", start, start.plusWeeks(1));
            }
            if (DAY.matcher(t).matches()) {
                LocalDate start = LocalDate.parse(t);
                return period("day", start, start.plusDays(1));
            }
            if (INSTANT.matcher(t).matches()) {
                Instant instant = OffsetDateTime.parse(t).toInstant();
                return new Period("instant", instant, instant);
            }
        } catch (java.time.DateTimeException e) {
            return null;
        }
        return null;
    }

    private static Period period(String granularity, LocalDate start, LocalDate end) {
        return new Period(granularity, start.atStartOfDay(ZoneOffset.UTC).toInstant(),
                end.atStartOfDay(ZoneOffset.UTC).toInstant());
    }
}
