package ai.foresignal.cycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.llm.LlmSettings;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

/**
 * Cycle scheduling (prompt 128 §1): the disabled defaults on first start; a run missed while the server was down runs
 * once, as {@code missed}, and the next fire time moves past now; at most one run per cycle; a manual run of a cycle
 * with roles and no provider is refused with {@code LLM_NOT_CONFIGURED}, a digest-only cycle never needs one.
 */
@IntegrationTest
@Import(AskTestSources.class)
class CycleSchedulerIT extends CycleItSupport {

    @Test
    @DisplayName("the three defaults are created once, disabled, in the operator timezone")
    void defaults() {
        assertThat(cycles.createDefaults()).isEqualTo(3);
        assertThat(cycles.createDefaults()).isZero();
        List<AnalysisCycleEntity> all = cycles.list(null, 10).items();
        assertThat(all).extracting(AnalysisCycleEntity::getName).containsExactly("Daily brief", "Weekly assessment",
                "Monthly outlook");
        assertThat(all).allSatisfy(c -> {
            assertThat(c.isEnabled()).isFalse();
            assertThat(c.isBuiltIn()).isTrue();
            assertThat(c.getNextRunAt()).isNull();
        });
        assertThat(all.get(1).getSchedule()).containsEntry("cron", "0 7 * * 1");
        assertThat(all.get(2).getSchedule()).containsEntry("cron", "0 8 1 * *");
    }

    @Test
    @DisplayName("a missed run runs once, as missed, and the next run moves past now")
    void missedOnce() {
        AnalysisCycleEntity cycle = cycle("daily", "[]", null, null);
        Instant past = clock.instant().minus(Duration.ofDays(3));
        jdbc.update("UPDATE analysis_cycle SET enabled = true, next_run_at = ? WHERE id = ?", Timestamp.from(past),
                cycle.getId());
        List<String> queued = scheduler.tick();
        assertThat(queued).hasSize(1);
        assertThat(runs.findById(queued.get(0)).orElseThrow().getTrigger()).isEqualTo("missed");
        await().atMost(Duration.ofSeconds(30)).until(() -> CycleVocabulary.DIGEST_ONLY.equals(
                runs.findById(queued.get(0)).orElseThrow().getStatus()));
        AnalysisCycleEntity after = cycles.get(cycle.getId());
        assertThat(after.getNextRunAt()).isAfter(clock.instant());
        await().atMost(Duration.ofSeconds(10)).until(() -> cycles.get(cycle.getId()).getRunningSince() == null);
        assertThat(scheduler.tick()).isEmpty();
        assertThat(runs.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("one run per cycle: a second manual run while one is in flight is refused")
    void onePerCycle() {
        AnalysisCycleEntity cycle = cycle("daily", "[]", null, null);
        jdbc.update("UPDATE analysis_cycle SET running_since = ? WHERE id = ?", Timestamp.from(clock.instant()),
                cycle.getId());
        assertThatThrownBy(() -> scheduler.runNow(cycles.get(cycle.getId()))).isInstanceOfSatisfying(
                ConflictException.class, e -> assertThat(e.getCode()).isEqualTo(ErrorCodes.CYCLE_RUN_IN_PROGRESS));
        assertThatThrownBy(() -> cycles.delete(cycle.getId())).isInstanceOf(ConflictException.class);
        jdbc.update("UPDATE analysis_cycle SET running_since = NULL WHERE id = ?", cycle.getId());
        CycleRunEntity run = scheduler.runNow(cycles.get(cycle.getId()));
        assertThat(run.getTrigger()).isEqualTo("manual");
        await().atMost(Duration.ofSeconds(30)).until(() -> cycles.get(cycle.getId()).getRunningSince() == null);
        assertThat(cycles.get(cycle.getId()).getLastRunId()).isEqualTo(run.getId());
    }

    @Test
    @DisplayName("roles without a provider are refused for manual runs; a digest-only cycle runs")
    void llmNotConfigured() {
        llmSettings.update(new LlmSettings(null, null, true, true, false, 200_000, false));
        AnalysisCycleEntity withRoles = cycle("daily", "[\"analyst\"]", null, null);
        assertThatThrownBy(() -> scheduler.runNow(withRoles)).isInstanceOfSatisfying(ConflictException.class,
                e -> assertThat(e.getCode()).isEqualTo(ErrorCodes.LLM_NOT_CONFIGURED));
        AnalysisCycleEntity digest = cycle("daily", "[]", null, null);
        assertThat(scheduler.runNow(digest).getStatus()).isEqualTo(CycleVocabulary.QUEUED);
    }

    @Test
    @DisplayName("an on-demand assessment of a situation runs once at a time")
    void situationOnce() {
        String situationId = situation("Red Sea shipping", "warning").getId();
        CycleRunEntity queued = runner.queueSituation(situationId, List.of(), null);
        assertThatThrownBy(() -> scheduler.runSituation(situationId, new CycleInputs.SituationRequest(List.of(),
                null))).isInstanceOf(ConflictException.class);
        runner.execute(queued.getId());
        CycleRunEntity next = scheduler.runSituation(situationId, new CycleInputs.SituationRequest(List.of(), null));
        await().atMost(Duration.ofSeconds(30)).until(() -> CycleVocabulary.DIGEST_ONLY.equals(
                runs.findById(next.getId()).orElseThrow().getStatus()));
    }
}
