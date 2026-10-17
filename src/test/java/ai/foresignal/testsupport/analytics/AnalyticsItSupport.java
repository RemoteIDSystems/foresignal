package ai.foresignal.testsupport.analytics;

import ai.foresignal.analytics.series.SeriesReader;
import ai.foresignal.source.series.SeriesMeta;
import ai.foresignal.source.series.SeriesPoint;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stats.RegionRollupJob;
import ai.foresignal.testsupport.ingest.CoverageSupport;
import ai.foresignal.testsupport.stats.StatsItSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Shared helpers of prompt 119's integration tests, on top of prompt 038's: daily series points, region tags written
 * straight into {@code record_region} (after the tag worker has caught up, so it does not overwrite them), the region
 * rollup driven to completion, and fetch history that makes a source covered.
 */
public abstract class AnalyticsItSupport extends StatsItSupport {

    @Autowired protected RegionRollupJob regionRollup;
    @Autowired protected SeriesReader reader;
    @Autowired protected CoverageSupport coverage;

    /** One daily point of {@code seriesId}, period {@code start} (a UTC midnight). */
    protected static NormalizedRecord dailyPoint(String seriesId, Instant start, double value, Instant observedAt) {
        return SeriesPoint.point(seriesId, "Series " + seriesId, "test", "P1D", start.toString().substring(0, 10),
                start, start.plus(Duration.ofDays(1)), value, "USD", "world", null, "final", observedAt);
    }

    /** One weekly point of {@code seriesId}. */
    protected static NormalizedRecord weeklyPoint(String seriesId, Instant start, double value, Instant observedAt) {
        return SeriesPoint.point(seriesId, "Series " + seriesId, "test", "P1W", start.toString().substring(0, 10),
                start, start.plus(Duration.ofDays(7)), value, "contracts", "world", null, "final", observedAt);
    }

    /** The {@code series-latest} record of {@code seriesId} with the 117 metadata. */
    protected static NormalizedRecord latest(String seriesId, String frequency, double value, Instant observedAt,
            SeriesMeta meta) {
        return meta.applyTo(SeriesPoint.latest(seriesId, "Series " + seriesId, "test", frequency, "latest", value,
                "USD", "world", "final", observedAt));
    }

    /**
     * Ingests one daily point per day for {@code values.length} days from {@code start}, each published at the end of
     * its day (so its {@code knownAt} is the next midnight), then the latest record.
     */
    protected void seedDaily(String source, String seriesId, Instant start, double[] values, SeriesMeta meta) {
        for (int i = 0; i < values.length; i++) {
            Instant day = start.plus(Duration.ofDays(i));
            Instant published = day.plus(Duration.ofDays(1)).plusSeconds(60);
            seed(source, published, dailyPoint(seriesId, day, values[i], published));
        }
        Instant last = start.plus(Duration.ofDays(values.length)).plusSeconds(120);
        seed(source, last, latest(seriesId, "P1D", values[values.length - 1], last, meta));
    }

    /** Writes (or replaces) the 040 tag of a record, after the tag worker has caught up. */
    protected void tag(String sourceId, String externalId, String iso2, String admin1) {
        awaitTriggerCursor();
        jdbc.update("INSERT INTO record_region (source_id, external_id, iso2, admin1, tagged_lon, tagged_lat, tagged_at)"
                + " VALUES (?, ?, ?, ?, 0, 0, now()) ON CONFLICT (source_id, external_id) DO UPDATE SET iso2 = EXCLUDED.iso2,"
                + " admin1 = EXCLUDED.admin1", sourceId, externalId, iso2, admin1);
    }

    /** Removes the 040 tag of a record (it then counts in ZZ). */
    protected void untag(String sourceId, String externalId) {
        awaitTriggerCursor();
        jdbc.update("DELETE FROM record_region WHERE source_id = ? AND external_id = ?", sourceId, externalId);
    }

    /** Runs the region rollup until it has nothing left to do at {@code now}; returns the hours rolled. */
    protected int regionRollUpAt(Instant now) {
        clock.setInstant(now);
        int hours = 0;
        for (int i = 0; i < 2_000; i++) {
            RegionRollupJob.RunResult result = regionRollup.runOnce();
            hours += result.hours();
            if (result.hours() == 0) {
                return hours;
            }
        }
        throw new AssertionError("the region rollup never caught up");
    }

    /** Fetch history that makes {@code sourceId} covered for every day of {@code [from, to)}. */
    protected void coverDays(String sourceId, Instant from, Instant to) {
        for (Instant t = from; t.isBefore(to); t = t.plus(Duration.ofHours(6))) {
            coverage.fetch(sourceId, "OK", t);
        }
        coverage.markCovered(sourceId, from, to);
    }

    /** {@code n} values equal to {@code value}. */
    protected static double[] constant(int n, double value) {
        double[] out = new double[n];
        java.util.Arrays.fill(out, value);
        return out;
    }

    /** Region-rollup rows of a resolution, as {@code source|kind|region|entities}. */
    protected List<String> regionRows(int resolution) {
        return new ArrayList<>(jdbc.queryForList("SELECT source_id || '|' || kind || '|' || region_code || '|' ||"
                + " entities FROM activity_rollup WHERE resolution = ? ORDER BY 1", String.class, (short) resolution));
    }
}
