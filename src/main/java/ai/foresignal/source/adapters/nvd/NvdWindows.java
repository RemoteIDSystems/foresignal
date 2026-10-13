package ai.foresignal.source.adapters.nvd;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The NVD {@code lastModStartDate}/{@code lastModEndDate} windows of one run (prompt 076). Pure.
 *
 * <p>The NVD CVE API refuses a last-modified range longer than {@value #MAX_WINDOW_DAYS} days, so the span from the
 * cursor to now is split into consecutive windows of at most that length. The first run starts
 * {@code backfillDays} before now, clamped to 1–{@value #MAX_BACKFILL_DAYS}.
 */
public final class NvdWindows {

    /** The longest range the API accepts, in days. */
    public static final int MAX_WINDOW_DAYS = 120;

    /** The longest first-run backfill, in days. */
    public static final int MAX_BACKFILL_DAYS = 120;

    /** The default first-run backfill, in days. */
    public static final int DEFAULT_BACKFILL_DAYS = 7;

    /** The longest range the API accepts. */
    public static final Duration MAX_WINDOW = Duration.ofDays(MAX_WINDOW_DAYS);

    /** The timestamp format the API takes ({@code 2026-10-12T00:00:00.000Z}). */
    static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT).withZone(ZoneOffset.UTC);

    /**
     * One last-modified range, start inclusive and end inclusive as the API reads it.
     *
     * @param start the range start
     * @param end the range end
     */
    public record Window(Instant start, Instant end) {

        /** The start in the API's format. */
        public String startParam() {
            return format(start);
        }

        /** The end in the API's format. */
        public String endParam() {
            return format(end);
        }
    }

    private NvdWindows() {}

    /**
     * The windows from {@code from} to {@code to}, each at most {@link #MAX_WINDOW} long; empty when {@code to} is
     * not after {@code from}.
     *
     * @param from the cursor
     * @param to now
     */
    public static List<Window> split(Instant from, Instant to) {
        List<Window> out = new ArrayList<>();
        if (from == null || to == null || !to.isAfter(from)) {
            return out;
        }
        Instant start = from;
        while (start.isBefore(to)) {
            Instant end = start.plus(MAX_WINDOW);
            if (end.isAfter(to)) {
                end = to;
            }
            out.add(new Window(start, end));
            start = end;
        }
        return out;
    }

    /**
     * The first-run cursor: {@code backfillDays} (clamped to 1–{@value #MAX_BACKFILL_DAYS}) before {@code now}.
     *
     * @param now the run time
     * @param backfillDays the configured backfill, or null for {@value #DEFAULT_BACKFILL_DAYS}
     */
    public static Instant initialCursor(Instant now, Integer backfillDays) {
        return now.minus(clampBackfill(backfillDays), ChronoUnit.DAYS);
    }

    /**
     * {@code backfillDays} clamped to 1–{@value #MAX_BACKFILL_DAYS}; {@value #DEFAULT_BACKFILL_DAYS} for null.
     *
     * @param backfillDays the configured backfill, or null
     */
    public static int clampBackfill(Integer backfillDays) {
        int days = backfillDays == null ? DEFAULT_BACKFILL_DAYS : backfillDays;
        return Math.max(1, Math.min(MAX_BACKFILL_DAYS, days));
    }

    /**
     * {@code instant} in the API's format, truncated to milliseconds.
     *
     * @param instant the instant
     */
    public static String format(Instant instant) {
        return FORMAT.format(instant.truncatedTo(ChronoUnit.MILLIS));
    }
}
