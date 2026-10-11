package ai.foresignal.api.mapper;

import ai.foresignal.api.dto.UsagePoint;
import ai.foresignal.api.dto.UsageSeries;
import ai.foresignal.collect.IngestCounts;
import ai.foresignal.collect.UsageResolution;
import ai.foresignal.collect.UsageSeriesPoint;
import ai.foresignal.collect.UsageTotalsWindow;
import java.time.Instant;
import java.util.List;

/** Maps {@code UsageMeter} series to the {@code UsageSeries} schema, with totals. */
public final class UsageMapper {

    private UsageMapper() {}

    /**
     * The API series.
     *
     * @param sourceId the source
     * @param from start of the range, used as the {@code t} of {@code totals}
     * @param resolution the bucket size
     * @param points the meter's non-empty buckets
     */
    public static UsageSeries series(
            String sourceId, Instant from, UsageResolution resolution, List<UsageSeriesPoint> points) {
        UsageTotalsWindow sum = UsageTotalsWindow.ZERO;
        for (UsageSeriesPoint point : points) {
            sum = sum.plus(point.totals());
        }
        return new UsageSeries(
                sourceId,
                resolution.wireValue(),
                points.stream().map(p -> point(p.bucketStart(), p.totals())).toList(),
                point(from, sum));
    }

    /** One API point. */
    public static UsagePoint point(Instant t, UsageTotalsWindow w) {
        IngestCounts i = w.ingest();
        return new UsagePoint(
                t,
                w.requests(),
                w.bytes(),
                w.errors(),
                w.refused(),
                i.recordsSeen(),
                i.versions(),
                i.liveUpdates(),
                i.touches(),
                i.late(),
                i.skipped(),
                i.rejected(),
                i.versionBytes());
    }
}
