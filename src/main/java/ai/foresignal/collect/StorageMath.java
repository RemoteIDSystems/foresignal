package ai.foresignal.collect;

import java.time.Duration;

/**
 * Computes {@link SourceStorage} from {@code source_usage} totals (Core-Data-Model §15).
 *
 * <p>Pure arithmetic, separate from {@link HealthService} so the formulas are testable without a
 * database.
 */
public final class StorageMath {

    /** The window {@code bytesPerDay} is averaged over. */
    public static final Duration BYTES_WINDOW = Duration.ofDays(7);

    /**
     * The shortest span {@code bytesPerDay} is extrapolated from.
     *
     * <p>Without a floor, a source's first minute would be multiplied by 1,440 into a daily figure.
     */
    public static final Duration MIN_BYTES_SPAN = Duration.ofHours(1);

    private static final double SECONDS_PER_DAY = Duration.ofDays(1).toSeconds();

    private StorageMath() {}

    /**
     * The storage figures.
     *
     * @param lastHour usage over the last hour
     * @param window usage over the last {@link #BYTES_WINDOW}
     * @param observedSpan how long, within that window, the source has had any usage; clamped to
     *     [{@link #MIN_BYTES_SPAN}, {@link #BYTES_WINDOW}], so a source running for two days is
     *     averaged over two days rather than diluted over seven
     * @param retention the source's retention window, or null when unknown
     */
    public static SourceStorage compute(
            UsageTotalsWindow lastHour, UsageTotalsWindow window, Duration observedSpan, Duration retention) {
        IngestCounts hour = lastHour == null ? IngestCounts.ZERO : lastHour.ingest();
        IngestCounts week = window == null ? IngestCounts.ZERO : window.ingest();

        double observationsPerMinute = hour.recordsSeen() / 60.0;
        double versionsPerMinute = hour.versions() / 60.0;
        double persistRatio = ratio(hour.versions(), hour.recordsSeen());
        double triggerEdgeShare = ratio(hour.triggerEdges(), hour.versions());

        long bytesPerDay = 0;
        if (week.versionBytes() > 0) {
            Duration span = clamp(observedSpan);
            bytesPerDay = Math.round(week.versionBytes() * SECONDS_PER_DAY / span.toSeconds());
        }
        long projectedBytes =
                retention == null || retention.isNegative()
                        ? 0
                        : Math.round(bytesPerDay * (retention.toSeconds() / SECONDS_PER_DAY));

        return new SourceStorage(
                observationsPerMinute,
                versionsPerMinute,
                persistRatio,
                triggerEdgeShare,
                bytesPerDay,
                projectedBytes,
                hour.late(),
                hour.clamped());
    }

    /** {@code part / whole} clamped to 0..1, 0 when {@code whole} is 0. */
    static double ratio(long part, long whole) {
        if (whole <= 0) {
            return 0.0;
        }
        return Math.min(1.0, Math.max(0.0, (double) part / whole));
    }

    static Duration clamp(Duration span) {
        if (span == null || span.compareTo(MIN_BYTES_SPAN) < 0) {
            return MIN_BYTES_SPAN;
        }
        return span.compareTo(BYTES_WINDOW) > 0 ? BYTES_WINDOW : span;
    }
}
