package ai.foresignal.api.dto;

import java.util.List;

/**
 * The {@code UsageSeries} schema: a source's usage over a range.
 *
 * @param sourceId the source
 * @param resolution {@code minute}, {@code hour} or {@code day}
 * @param points non-empty buckets in time order (empty buckets are omitted)
 * @param totals the sum over every point
 */
public record UsageSeries(String sourceId, String resolution, List<UsagePoint> points, UsagePoint totals) {}
