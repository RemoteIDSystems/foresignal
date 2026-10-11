package ai.foresignal.collect;

import java.time.Instant;

/**
 * One bucket of a usage series.
 *
 * @param bucketStart start of the bucket, UTC
 * @param totals what happened in it
 */
public record UsageSeriesPoint(Instant bucketStart, UsageTotalsWindow totals) {}
