package ai.foresignal.benchmark;

import org.springframework.context.annotation.Import;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * BEN-1 (prompt 123): a full synthetic benchmark run, queued and executed as a 103 job, writes only
 * {@code benchmark_*} rows (plus its job row and its 116 manifest); every production analytic table is unchanged.
 */
@IntegrationTest
@Import(TestIngestSources.class)
class BenchmarkIsolationIT {

    static final Instant NOW = Instant.parse("2026-10-17T12:00:00Z");

    /** The production analytic tables a benchmark must never write. */
    static final List<String> ANALYTIC = List.of("record_version", "record_head", "alert", "analytic_finding",
            "analytic_finding_log", "analysis_series", "anomaly_observation", "activity_sample", "window_state",
            "intelligence_event", "intelligence_event_member", "intelligence_event_log", "investigation",
            "investigation_item", "situation", "situation_member", "situation_log", "situation_snapshot",
            "situation_signature", "situation_outcome", "canonical_event", "observation_link", "evidence_group",
            "observability_estimate", "precursor", "precursor_batch", "precursor_candidate", "base_rate",
            "reference_history", "forecast_question", "forecast", "forecast_score", "forecast_log",
            "forecast_calibration", "forecaster");

    /** The tables a run may write. */
    static final List<String> ALLOWED = List.of("benchmark_run", "benchmark_case", "backtest_job", "analysis_manifest");

    @Autowired BenchmarkService service;
    @Autowired BenchmarkRunRepository runs;
    @Autowired AppSettingService settings;
    @Autowired JdbcTemplate jdbc;
    @Autowired MutableClock clock;

    Map<String, Long> counts() {
        List<String> tables = jdbc.queryForList("SELECT table_name FROM information_schema.tables WHERE"
                + " table_schema = 'public' AND table_type = 'BASE TABLE'", String.class);
        Map<String, Long> out = new LinkedHashMap<>();
        List<String> wanted = new ArrayList<>(ANALYTIC);
        wanted.addAll(ALLOWED);
        for (String t : wanted) {
            if (tables.contains(t)) {
                out.put(t, jdbc.queryForObject("SELECT count(*) FROM " + t, Long.class));
            }
        }
        return out;
    }

    @Test
    @DisplayName("a full synthetic run writes only benchmark rows; production analytic tables are unchanged")
    void onlyBenchmarkRows() throws Exception {
        clock.setInstant(NOW);
        settings.put(SettingKeys.BENCHMARK_THRESHOLDS, Map.of("suite", Map.of("replicates", 1)));
        Map<String, Long> before = counts();
        assertThat(before).containsKeys("analytic_finding", "forecast", "situation", "benchmark_run");
        BenchmarkRunEntity run = service.submit(new BenchmarkInputs.RunRequest("synthetic",
                BenchmarkInputs.COMPONENTS, null), "test", "manual");
        BenchmarkRunEntity done = null;
        for (int i = 0; i < 1200; i++) {
            done = runs.findById(run.getId()).orElseThrow();
            if (done.getFinishedAt() != null) {
                break;
            }
            Thread.sleep(250);
        }
        assertThat(done.getFinishedAt()).as("the run finished").isNotNull();
        assertThat(done.getError()).isNull();
        assertThat(done.getStatus()).isIn("pass", "warn", "fail");
        assertThat(done.getResults()).hasSize(BenchmarkInputs.COMPONENTS.size());
        assertThat(done.getManifestId()).isNotNull();
        Map<String, Long> after = counts();
        for (String table : ANALYTIC) {
            if (before.containsKey(table)) {
                assertThat(after.get(table)).as("rows of " + table).isEqualTo(before.get(table));
            }
        }
        assertThat(after.get("benchmark_run")).isEqualTo(before.get("benchmark_run") + 1);
        assertThat(after.get("backtest_job")).isEqualTo(before.get("backtest_job") + 1);
        assertThat(service.inProgress()).isEmpty();
    }
}
