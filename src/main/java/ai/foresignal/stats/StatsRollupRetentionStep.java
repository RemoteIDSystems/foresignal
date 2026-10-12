package ai.foresignal.stats;

import ai.foresignal.retention.RetentionRun;
import ai.foresignal.retention.RetentionStep;
import ai.foresignal.settings.RetentionSettings;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

/**
 * Deletes {@code activity_rollup} rows older than their resolution's window (prompt 038):
 * {@code RetentionSettings.stats.hourly} (35 d) for hours and {@code stats.daily} (1825 d) for days,
 * by {@code bucket_start}. Prompt 119 adds its region resolutions to {@link #windows}.
 */
@Component
public class StatsRollupRetentionStep implements RetentionStep {

    /** The table name reported in {@code foresignal.retention.deleted{table}}. */
    public static final String TABLE = "activity_rollup";

    @Override
    public String name() {
        return "stats-rollups";
    }

    @Override
    public int order() {
        return 730;
    }

    @Override
    public void run(RetentionRun run) {
        for (Map.Entry<RollupResolution, Duration> window : windows(run.settings().stats()).entrySet()) {
            int deleted = run.jdbc().update(
                    "DELETE FROM activity_rollup WHERE resolution = :resolution AND bucket_start < :cutoff",
                    new MapSqlParameterSource()
                            .addValue("resolution", window.getKey().code())
                            .addValue("cutoff", OffsetDateTime.ofInstant(run.now().minus(window.getValue()),
                                    ZoneOffset.UTC)));
            run.deleted(TABLE, deleted);
        }
    }

    /** The window of each resolution. */
    static Map<RollupResolution, Duration> windows(RetentionSettings.Stats stats) {
        Map<RollupResolution, Duration> out = new LinkedHashMap<>();
        out.put(RollupResolution.HOUR, stats.hourly());
        out.put(RollupResolution.DAY, stats.daily());
        return out;
    }
}
