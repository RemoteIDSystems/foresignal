package ai.foresignal.pol;

import ai.foresignal.retention.RetentionRun;
import ai.foresignal.retention.RetentionStep;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.Durations;
import ai.foresignal.settings.SettingKeys;
import java.sql.Timestamp;
import java.time.Duration;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

/**
 * Deletes pattern-of-life stays that departed, and places last seen, longer ago than {@code pol.retention}
 * (prompt 114; default 180 days). These stores outlive the TRACK window by design; this step is their only
 * pruning. The {@code pol-place} record of a deleted place is tombstoned by its generator on the next tick.
 */
@Component
public class PolRetentionStep implements RetentionStep {

    /** The default retention. */
    public static final Duration DEFAULT = Duration.ofDays(180);

    private final AppSettingService settings;

    /** Creates the step. */
    public PolRetentionStep(AppSettingService settings) {
        this.settings = settings;
    }

    /** The effective {@code pol.retention}: the setting, else 180 days. */
    public Duration retention() {
        String value = settings.get(SettingKeys.POL_RETENTION, String.class).orElse(null);
        if (value == null || value.isBlank()) {
            return DEFAULT;
        }
        try {
            return Durations.parsePositive(value);
        } catch (RuntimeException e) {
            return DEFAULT;
        }
    }

    @Override
    public String name() {
        return "pattern-of-life";
    }

    @Override
    public int order() {
        return 760;
    }

    @Override
    public void run(RetentionRun run) {
        MapSqlParameterSource cutoff = new MapSqlParameterSource("t", Timestamp.from(run.now().minus(retention())));
        run.deleted("pol_stay", run.jdbc().update("DELETE FROM pol_stay WHERE depart < :t", cutoff));
        run.deleted("pol_place", run.jdbc().update("DELETE FROM pol_place WHERE last_seen < :t", cutoff));
        run.jdbc().update("UPDATE pol_stay s SET place_id = NULL, cluster_id = NULL WHERE place_id IS NOT NULL"
                + " AND NOT EXISTS (SELECT 1 FROM pol_place p WHERE p.id = s.place_id)", new MapSqlParameterSource());
    }
}
