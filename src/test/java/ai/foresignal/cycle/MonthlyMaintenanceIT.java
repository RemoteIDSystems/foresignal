package ai.foresignal.cycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.llm.LlmProviderEntity;
import ai.foresignal.llm.LlmProviderRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The monthly maintenance (prompt 128 §5): the jobs run in the documented order, a failing step is recorded without
 * stopping the later ones, one maintenance runs at a time, and the LLM retrospective evaluation runs only when the
 * provider has a {@code knowledgeCutoff}. Archival proposals flag sets past their horizon.
 */
@IntegrationTest
@Import(MutableClock.TestConfig.class)
class MonthlyMaintenanceIT {

    static final Instant NOW = Instant.parse("2026-11-01T03:30:00Z");

    @Autowired MaintenanceRunRepository runs;
    @Autowired DefaultMaintenanceActions defaults;
    @Autowired LlmProviderRepository providers;
    @Autowired ai.foresignal.llm.LlmProviderService providerService;
    @Autowired tools.jackson.databind.json.JsonMapper json;
    @Autowired ForecasterRegistry forecasters;
    @Autowired JdbcTemplate jdbc;
    @Autowired MutableClock clock;

    @BeforeEach
    void setUp() {
        clock.setInstant(NOW);
        forecasters.registerBuiltIns();
    }

    /** Records the order of the calls; fails on the steps named in {@code failing}. */
    static final class Recording implements MaintenanceActions {

        final List<String> calls = new ArrayList<>();
        final List<String> failing;
        final boolean cutoff;

        Recording(List<String> failing, boolean cutoff) {
            this.failing = failing;
            this.cutoff = cutoff;
        }

        Map<String, Object> step(String name) {
            calls.add(name);
            if (failing.contains(name)) {
                throw new IllegalStateException(name + " broke");
            }
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("jobId", "job-" + name);
            return out;
        }

        @Override
        public Map<String, Object> observabilityReestimation(Instant now) {
            return step("observability-reestimation");
        }

        @Override
        public Map<String, Object> benchmarkHistorical(Instant now) {
            return step("benchmark-historical");
        }

        @Override
        public Map<String, Object> baseRateRefresh(Instant now) {
            return step("base-rate-refresh");
        }

        @Override
        public Map<String, Object> precursorRetest(Instant now) {
            return step("precursor-retest");
        }

        @Override
        public Map<String, Object> plattRefit(Instant now) {
            return step("platt-refit");
        }

        @Override
        public Map<String, Object> ensembleWeights(Instant now) {
            return step("ensemble-weights");
        }

        @Override
        public Map<String, Object> extremizingCheck(Instant now) {
            return step("extremizing-check");
        }

        @Override
        public Map<String, Object> indexWeightFit(Instant now) {
            return step("index-weight-fit");
        }

        @Override
        public Map<String, Object> seriesMethodReselection(Instant now) {
            return step("series-method-reselection");
        }

        @Override
        public Map<String, Object> hypothesisArchivalProposals(Instant now) {
            return step("hypothesis-archival-proposals");
        }

        @Override
        public Map<String, Object> retrospectiveEvaluation(Instant now, String providerId) {
            calls.add("llm-retrospective-evaluation");
            return cutoff ? Map.of("evaluations", List.of()) : null;
        }
    }

    @Test
    @DisplayName("the steps run in order; a failure is recorded and the later steps still run")
    @SuppressWarnings("unchecked")
    void order() {
        Recording actions = new Recording(List.of("base-rate-refresh"), true);
        MonthlyMaintenance maintenance = new MonthlyMaintenance(runs, actions, clock);
        MaintenanceRunEntity run = maintenance.execute(maintenance.start("manual", null).getId(), "chat");
        assertThat(actions.calls).containsExactlyElementsOf(MonthlyMaintenance.STEPS);
        assertThat(run.getStatus()).isEqualTo("partial");
        assertThat(run.getSteps()).extracting(s -> s.get("step")).containsExactlyElementsOf(MonthlyMaintenance.STEPS);
        Map<String, Object> failed = run.getSteps().get(2);
        assertThat(failed).containsEntry("status", "failed").containsEntry("error", "base-rate-refresh broke");
        assertThat(run.getSteps().get(3)).containsEntry("status", "done").containsEntry("jobId", "job-precursor-retest");
        assertThat(run.getFinishedAt()).isNotNull();
        Map<String, Object> view = MonthlyMaintenance.view(runs.findById(run.getId()).orElseThrow());
        assertThat(view).containsEntry("trigger", "manual").containsEntry("status", "partial");
    }

    @Test
    @DisplayName("without a knowledgeCutoff the retrospective evaluation is skipped; all others done")
    void skippedWithoutCutoff() {
        Recording actions = new Recording(List.of(), false);
        MonthlyMaintenance maintenance = new MonthlyMaintenance(runs, actions, clock);
        MaintenanceRunEntity run = maintenance.execute(maintenance.start("monthly-cycle", "RUN").getId(), null);
        assertThat(run.getStatus()).isEqualTo("done");
        assertThat(run.getSteps().get(10)).containsEntry("status", "skipped");
        assertThat(run.getCycleRunId()).isEqualTo("RUN");
    }

    @Test
    @DisplayName("one maintenance at a time; a stale one is closed as failed")
    void oneAtATime() {
        MonthlyMaintenance maintenance = new MonthlyMaintenance(runs, new Recording(List.of(), true), clock);
        MaintenanceRunEntity first = maintenance.start("manual", null);
        assertThatThrownBy(() -> maintenance.start("manual", null)).isInstanceOf(ConflictException.class);
        clock.setInstant(NOW.plusSeconds(7 * 3600));
        MaintenanceRunEntity second = maintenance.start("manual", null);
        assertThat(second.getId()).isNotEqualTo(first.getId());
        assertThat(runs.findById(first.getId()).orElseThrow().getStatus()).isEqualTo("failed");
    }

    @Test
    @DisplayName("the default retrospective evaluation needs the provider's cutoff and then evaluates llm forecasters")
    @SuppressWarnings("unchecked")
    void retrospectiveOnlyWithCutoff() {
        providerService.create(json.readTree("{\"id\":\"maint\",\"type\":\"openai-compatible\",\"baseUrl\":"
                + "\"http://llm.test\",\"chatModel\":\"fake-chat\",\"requestsPerMinute\":10,\"tokensPerDay\":1000}"));
        LlmProviderEntity provider = providers.findById("maint").orElseThrow();
        forecasters.ensure("llm:default:maint", "llm", "LLM forecaster", "1.0.0", Map.of("provider", "maint"));
        assertThat(defaults.retrospectiveEvaluation(NOW, "maint")).isNull();
        assertThat(defaults.retrospectiveEvaluation(NOW, null)).isNull();
        provider = providers.findById("maint").orElseThrow();
        provider.setKnowledgeCutoff(LocalDate.parse("2026-01-01"));
        providers.save(provider);
        Map<String, Object> result = defaults.retrospectiveEvaluation(NOW, "maint");
        assertThat(result).containsEntry("knowledgeCutoff", "2026-01-01");
        assertThat((List<Map<String, Object>>) result.get("evaluations")).extracting(e -> e.get("forecasterId"))
                .contains("llm:default:maint");
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("archival proposals flag active sets past their horizon for review")
    void archival() {
        jdbc.update("INSERT INTO hypothesis_set (id, question, kind, cadence, horizon, status, origin, confidence,"
                + " private, review_flagged, version, created_at, updated_at, region_codes, themes) VALUES"
                + " ('01JA8Q3V7S2K9W4T6Y1B5N0M3C', 'Old question?', 'exclusive', 'weekly', DATE '2026-10-01', 'active',"
                + " 'operator', 'low', false, false, 0, now(), now(), '{}', '{}')");
        Map<String, Object> result = defaults.hypothesisArchivalProposals(NOW);
        assertThat((List<Object>) result.get("flaggedForArchivalReview")).containsExactly("01JA8Q3V7S2K9W4T6Y1B5N0M3C");
        assertThat(jdbc.queryForObject("select review_flagged from hypothesis_set", Boolean.class)).isTrue();
        assertThat((List<?>) defaults.hypothesisArchivalProposals(NOW).get("flaggedForArchivalReview")).isEmpty();
    }
}
