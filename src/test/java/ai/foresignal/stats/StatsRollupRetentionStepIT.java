package ai.foresignal.stats;

import static ai.foresignal.testsupport.stats.StatsTestSources.POINTS;
import static ai.foresignal.testsupport.stats.StatsTestSources.POINT_KIND;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.retention.RetentionSettingsService;
import ai.foresignal.retention.RetentionStep;
import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.stats.StatsItSupport;
import ai.foresignal.testsupport.stats.StatsTestSources;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link StatsRollupRetentionStep} (prompt 038): hourly rollup rows go after
 * {@code RetentionSettings.stats.hourly} (35 d), daily rows after {@code stats.daily} (1825 d), both
 * configurable through {@code updateRetention}.
 */
@IntegrationTest
@Import(StatsTestSources.class)
class StatsRollupRetentionStepIT extends StatsItSupport {

    @Autowired List<RetentionStep> steps;
    @Autowired RetentionSettingsService retentionSettings;
    @Autowired JsonMapper json;

    private long rowsOf(int resolution) {
        Long n = jdbc.queryForObject("SELECT count(*) FROM activity_rollup WHERE resolution = ?", Long.class,
                (short) resolution);
        return n == null ? 0 : n;
    }

    private void seedADay() {
        seed(POINTS, T0.plusSeconds(600), point("p", POINT_KIND, 10, 50, T0.plusSeconds(600), 1));
        rollUpAt(T0.plus(Duration.ofHours(24)).plus(Duration.ofMinutes(15)));
        assertThat(rowsOf(0)).isPositive();
        assertThat(rowsOf(1)).isPositive();
    }

    @Test
    @DisplayName("the step is registered between the briefing runs and the audit log")
    void registered() {
        assertThat(steps).filteredOn(s -> s instanceof StatsRollupRetentionStep).singleElement()
                .satisfies(s -> {
                    assertThat(s.order()).isEqualTo(730);
                    assertThat(s.name()).isEqualTo("stats-rollups");
                });
        assertThat(StatsRollupRetentionStep.windows(RetentionSettings.Stats.defaults()))
                .containsEntry(RollupResolution.HOUR, Duration.ofDays(35))
                .containsEntry(RollupResolution.DAY, Duration.ofDays(1825));
    }

    @Test
    @DisplayName("hourly rows older than 35 days are deleted; daily rows stay for 1825 days")
    void defaults() {
        seedADay();

        runAt(T0.plus(Duration.ofDays(36)));

        assertThat(rowsOf(0)).isZero();
        assertThat(rowsOf(1)).isPositive();
        assertThat(row(1, T0, POINTS, POINT_KIND, H3Cells.ALL_CELLS)).containsEntry("versions", 1L);
    }

    @Test
    @DisplayName("updateRetention stores stats.hourly and stats.daily, and the step follows them")
    void configured() {
        seedADay();
        retentionSettings.update(json.readTree("{\"default\": \"7d\", \"stats\": {\"hourly\": \"2d\", \"daily\": \"3d\","
                + " \"regionHourly\": \"1095d\"}}"));
        assertThat(retentionSettings.get().stats()).isEqualTo(new RetentionSettings.Stats(Duration.ofDays(2),
                Duration.ofDays(3)));

        runAt(T0.plus(Duration.ofDays(2)).plus(Duration.ofHours(12)));
        assertThat(rowsOf(0)).as("hours of day one are older than 2 days").isZero();
        assertThat(rowsOf(1)).isPositive();

        runAt(T0.plus(Duration.ofDays(4)));
        assertThat(rowsOf(1)).isZero();
    }
}
