package ai.foresignal.store;

import ai.foresignal.source.spi.Coverage;
import java.util.List;
import org.locationtech.jts.geom.Envelope;

/**
 * One fetch's reported coverage, as stored in {@code source_health.last_coverage} and the
 * {@code recent_coverage} ring (Core-Data-Model §14).
 *
 * <p>Shaped like the OpenAPI {@code Coverage} schema, with the region as
 * {@code [minLon, minLat, maxLon, maxLat]} rather than a JTS {@link Envelope}, so it serializes
 * to {@code jsonb} and back without a geometry library in the way.
 *
 * @param receivers receivers that contributed, or null
 * @param expected records the upstream said to expect, or null
 * @param actual records actually returned, or null
 * @param region the covered region {@code [minLon, minLat, maxLon, maxLat]}, or null
 */
public record CoverageSample(Integer receivers, Integer expected, Integer actual, List<Double> region) {

    public CoverageSample {
        region = region == null || region.size() != 4 ? null : List.copyOf(region);
    }

    /** The stored form of an adapter's {@link Coverage}, or null when it reports nothing. */
    public static CoverageSample from(Coverage coverage) {
        if (coverage == null || coverage.isEmpty()) {
            return null;
        }
        Envelope e = coverage.region();
        List<Double> region =
                e == null || e.isNull()
                        ? null
                        : List.of(e.getMinX(), e.getMinY(), e.getMaxX(), e.getMaxY());
        return new CoverageSample(coverage.receivers(), coverage.expected(), coverage.actual(), region);
    }
}
