package ai.foresignal.chronicle.network;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ISO-8601 week labels ({@code 2026-W40}), the granularity at which sketch packages carry capture spans (prompt 134):
 * coarse enough to say nothing precise about when a chronicle ran. Pure.
 */
public final class IsoWeeks {

    private static final Pattern WEEK = Pattern.compile("^(\\d{4})-W(\\d{2})$");

    private IsoWeeks() {}

    /** The ISO week of an instant (UTC), or null. */
    public static String of(Instant t) {
        if (t == null) {
            return null;
        }
        LocalDate d = t.atZone(ZoneOffset.UTC).toLocalDate();
        return String.format(Locale.ROOT, "%04d-W%02d", d.get(IsoFields.WEEK_BASED_YEAR),
                d.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
    }

    /** True when {@code label} is an ISO week label. */
    public static boolean valid(String label) {
        return label != null && WEEK.matcher(label).matches() && start(label) != null;
    }

    /** The first instant of a week (Monday 00:00 UTC), or null when the label is invalid. */
    public static Instant start(String label) {
        if (label == null) {
            return null;
        }
        Matcher m = WEEK.matcher(label);
        if (!m.matches()) {
            return null;
        }
        int year = Integer.parseInt(m.group(1));
        int week = Integer.parseInt(m.group(2));
        if (week < 1 || week > 53) {
            return null;
        }
        LocalDate jan4 = LocalDate.of(year, 1, 4);
        if (week > jan4.range(IsoFields.WEEK_OF_WEEK_BASED_YEAR).getMaximum()) {
            return null;
        }
        LocalDate monday = jan4.with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return monday.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    /** The last instant of a week (the next Monday 00:00 UTC, exclusive end), or null when invalid. */
    public static Instant end(String label) {
        Instant start = start(label);
        return start == null ? null : start.plusSeconds(7L * 24 * 3600);
    }
}
