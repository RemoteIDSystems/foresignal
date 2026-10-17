package ai.foresignal.analytics.series;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

/**
 * The frequency of a series (prompt 119): {@code hour}, {@code day}, {@code week} (ISO weeks starting Monday, UTC),
 * {@code month} or {@code irregular}. Buckets are UTC.
 */
public enum SeriesFrequency {

    /** Hourly buckets. */
    HOUR("hour"),
    /** Daily buckets. */
    DAY("day"),
    /** Weekly buckets, Monday 00:00 UTC. */
    WEEK("week"),
    /** Calendar months. */
    MONTH("month"),
    /** No regular bucket (points as stored). */
    IRREGULAR("irregular");

    private final String wire;

    SeriesFrequency(String wire) {
        this.wire = wire;
    }

    /** The wire value. */
    public String wire() {
        return wire;
    }

    /** Parses a wire value (case-insensitive), or null. */
    public static SeriesFrequency parse(String value) {
        if (value == null) {
            return null;
        }
        for (SeriesFrequency f : values()) {
            if (f.wire.equals(value.strip().toLowerCase(Locale.ROOT))) {
                return f;
            }
        }
        return null;
    }

    /** The frequency of a 093 ISO-8601 period ({@code PT1H}, {@code P1D}, {@code P1W}, {@code P1M}), else irregular. */
    public static SeriesFrequency ofIsoPeriod(String iso) {
        if (iso == null) {
            return IRREGULAR;
        }
        return switch (iso.strip().toUpperCase(Locale.ROOT)) {
            case "PT1H" -> HOUR;
            case "P1D" -> DAY;
            case "P1W" -> WEEK;
            case "P1M" -> MONTH;
            default -> IRREGULAR;
        };
    }

    /** True for a frequency with regular buckets. */
    public boolean regular() {
        return this != IRREGULAR;
    }

    /** The bucket containing {@code t}. */
    public Instant floor(Instant t) {
        return switch (this) {
            case HOUR -> t.truncatedTo(ChronoUnit.HOURS);
            case DAY -> t.truncatedTo(ChronoUnit.DAYS);
            case WEEK -> {
                LocalDate d = t.atZone(ZoneOffset.UTC).toLocalDate()
                        .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                yield d.atStartOfDay(ZoneOffset.UTC).toInstant();
            }
            case MONTH -> t.atZone(ZoneOffset.UTC).toLocalDate().withDayOfMonth(1).atStartOfDay(ZoneOffset.UTC)
                    .toInstant();
            case IRREGULAR -> t;
        };
    }

    /** The start of the bucket after the one starting at {@code start}. */
    public Instant next(Instant start) {
        return switch (this) {
            case HOUR -> start.plus(Duration.ofHours(1));
            case DAY -> start.plus(Duration.ofDays(1));
            case WEEK -> start.plus(Duration.ofDays(7));
            case MONTH -> start.atZone(ZoneOffset.UTC).plusMonths(1).toInstant();
            case IRREGULAR -> start;
        };
    }

    /** The start of the bucket {@code n} buckets after (negative: before) the one starting at {@code start}. */
    public Instant plus(Instant start, int n) {
        return switch (this) {
            case HOUR -> start.plus(Duration.ofHours(n));
            case DAY -> start.plus(Duration.ofDays(n));
            case WEEK -> start.plus(Duration.ofDays(7L * n));
            case MONTH -> start.atZone(ZoneOffset.UTC).plusMonths(n).toInstant();
            case IRREGULAR -> start;
        };
    }

    /** The typical bucket length (months as 30 days), for sizing default ranges. */
    public Duration approximateWidth() {
        return switch (this) {
            case HOUR -> Duration.ofHours(1);
            case DAY -> Duration.ofDays(1);
            case WEEK -> Duration.ofDays(7);
            case MONTH -> Duration.ofDays(30);
            case IRREGULAR -> Duration.ofDays(1);
        };
    }

    /** Buckets per year, for base rates per year. */
    public double perYear() {
        return 365.25 * 86_400 / approximateWidth().toSeconds();
    }
}
