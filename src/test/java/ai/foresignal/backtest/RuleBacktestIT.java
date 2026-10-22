package ai.foresignal.backtest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.retention.RetentionStateStore;
import ai.foresignal.security.Ulids;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.trigger.TriggerRuleService;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * A rule backtest (prompt 103): the job completes with caveats and a would-fire count, an open-ended
 * seq range is clamped to the watermark (so the job is bounded), variants run side by side, ranges
 * are capped and clamped, and a cancellation sticks.
 */
@WebIntegrationTest
@Import(TestIngestSources.class)
class RuleBacktestIT {

    /** How long a test waits for a job to finish. */
    static final Duration WAIT = Duration.ofSeconds(60);

    @Autowired BacktestJobService jobs;
    @Autowired TriggerRuleService rules;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired JsonMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired RetentionStateStore retention;
    @Autowired BacktestJobRepository repository;

    private UUID seed(int count) {
        clock.setInstant(Instant.parse("2026-03-01T12:00:00Z"));
        Instant now = clock.instant();
        // Disabled, so the live trigger engine never fires on the seeded records: a backtest replays the rule
        // whether or not it is enabled, and the actual block (its real alerts) is then deterministically empty
        // instead of depending on the live engine's timing.
        UUID ruleId = rules.create(json.readTree("{\"name\":\"big things\",\"enabled\":false,"
                + "\"sources\":[\"test-standard\"],"
                + "\"kinds\":[\"test-record\"],\"mode\":\"match\",\"conditions\":[{\"path\":\"size\",\"op\":\">=\","
                + "\"value\":5}]}")).rule().getId();
        for (int i = 0; i < count; i++) {
            records.ingestAt(TestIngestSources.STANDARD, now, NormalizedRecord.builder("r" + i,
                    TestIngestSources.STANDARD_KIND).observedAt(now)
                    .point(-95, 32).put("name", "r" + i).put("size", i).build());
        }
        return ruleId;
    }

    /** Waits until the job leaves queued/running (the worker or this thread ran it). */
    static BacktestJobEntity await(BacktestJobService jobs, String id) {
        jobs.run(id);
        Instant deadline = Instant.now().plus(WAIT);
        BacktestJobEntity job = jobs.get(id);
        while (List.of("queued", "running").contains(job.getStatus()) && Instant.now().isBefore(deadline)) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            job = jobs.get(id);
        }
        return job;
    }

    @Test
    @DisplayName("a rule job over an open-ended seq range completes, bounded by the watermark")
    void ruleJob() {
        UUID ruleId = seed(10);
        BacktestJobEntity job = jobs.submit("rule", ruleId.toString(), null,
                json.readTree("{\"fromSeq\":0,\"toSeq\":" + Long.MAX_VALUE + "}"), null, null);
        assertThat(job.getToSeq()).isEqualTo(jobs.watermark());
        BacktestJobEntity done = await(jobs, job.getId());
        assertThat(done.getStatus()).isEqualTo("done");
        assertThat(done.getProgress()).isEqualTo(1.0);
        assertThat(done.getResult()).containsEntry("wouldFire", 5).containsEntry("ruleType", "record");
        assertThat(done.getCaveats()).anyMatch(c -> BacktestCaveat.NOT_GROUND_TRUTH.equals(c.get("code")));
        assertThat(done.getCaveats()).anyMatch(c -> BacktestCaveat.SMALL_SAMPLE.equals(c.get("code")));
        assertThat(done.getCaveats()).noneMatch(c -> BacktestCaveat.WINDOW_APPROXIMATE.equals(c.get("code")));
        assertThat(done.getDetectorVersion()).isEqualTo(BacktestJobService.RULE_BACKTEST_VERSION);
        assertThat(done.getDefinition()).containsKey("rule");
    }

    @Test
    @DisplayName("variants run side by side with the rule")
    @SuppressWarnings("unchecked")
    void variants() {
        UUID ruleId = seed(10);
        BacktestJobEntity job = jobs.submit("rule", ruleId.toString(), null, json.readTree("{\"fromSeq\":0}"),
                List.of(Map.of("name", "size>=8", "conditions", List.of(Map.of("path", "size", "op", ">=", "value", 8)))),
                null);
        BacktestJobEntity done = await(jobs, job.getId());
        assertThat(done.getStatus()).isEqualTo("done");
        List<Map<String, Object>> variants = (List<Map<String, Object>>) done.getResult().get("variants");
        assertThat(variants).hasSize(1);
        assertThat(variants.get(0)).containsEntry("name", "size>=8").containsEntry("wouldFire", 2)
                .containsEntry("index", 0).containsKey("patch");
        assertThat((Map<String, Object>) done.getResult().get("actual")).containsEntry("alerts", 0)
                .containsEntry("comparable", true);
    }

    @Test
    @DisplayName("a time range is honored and capped at 90 days; inverted ranges are 400")
    void timeRange() {
        UUID ruleId = seed(3);
        Instant now = clock.instant();
        BacktestJobEntity job = jobs.submit("recipe", null, null, json.readTree("{\"from\":\""
                + now.minus(Duration.ofHours(2)) + "\",\"to\":\"" + now.plus(Duration.ofMinutes(1)) + "\"}"), null, null);
        assertThat(job.getRangeFrom()).isEqualTo(now.minus(Duration.ofHours(2)));
        assertThat(job.getRangeTo()).isEqualTo(now);
        assertThatThrownBy(() -> jobs.submit("rule", ruleId.toString(), null, json.readTree("{\"from\":\""
                + now.minus(Duration.ofDays(91)) + "\",\"to\":\"" + now + "\"}"), null, null))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> jobs.submit("rule", ruleId.toString(), null, json.readTree("{\"from\":\""
                + now + "\",\"to\":\"" + now.minus(Duration.ofHours(1)) + "\"}"), null, null))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> jobs.submit("rule", ruleId.toString(), null, json.readTree("{\"fromSeq\":5,"
                + "\"toSeq\":5}"), null, null)).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("a range starting below the retained floor is clamped and says so")
    void clampedByRetention() {
        UUID ruleId = seed(10);
        long floor = jobs.watermark() - 3;
        jdbc.update("INSERT INTO retention_state (source_id, kind, floor_seq, purge_seq, purged_at, last_run_at)"
                + " VALUES (?, '*', ?, 0, NULL, ?)", TestIngestSources.STANDARD, floor, Timestamp.from(clock.instant()));
        retention.reload();
        try {
            BacktestJobEntity job = jobs.submit("rule", ruleId.toString(), null, json.readTree("{\"fromSeq\":0}"),
                    null, null);
            assertThat(job.getFromSeq()).isEqualTo(floor);
            assertThat(job.getRangeClamped()).isTrue();
            BacktestJobEntity done = await(jobs, job.getId());
            assertThat(done.getCaveats())
                    .anyMatch(c -> BacktestCaveat.RANGE_CLAMPED_BY_RETENTION.equals(c.get("code")));
            assertThatThrownBy(() -> jobs.submit("rule", ruleId.toString(), null,
                    json.readTree("{\"fromSeq\":0,\"toSeq\":" + (floor - 1) + "}"), null, null))
                    .isInstanceOf(ValidationException.class);
        } finally {
            jdbc.update("DELETE FROM retention_state");
            retention.reload();
        }
    }

    @Test
    @DisplayName("a canceled job stays canceled: it is never claimed, and a running one stops at its checkpoint")
    void cancelSticks() {
        UUID ruleId = seed(3);
        BacktestJobEntity queued = new BacktestJobEntity(Ulids.next(clock.instant()), "rule", clock.instant());
        queued.setTargetId(ruleId.toString());
        queued.setFromSeq(0L);
        queued.setToSeq(jobs.watermark());
        repository.save(queued);
        jobs.cancel(queued.getId());
        jobs.run(queued.getId());
        BacktestJobEntity after = jobs.get(queued.getId());
        assertThat(after.getStatus()).isEqualTo("cancelled");
        assertThat(after.isCanceled()).isTrue();
        assertThat(after.getResult()).isNull();

        BacktestJobEntity running = new BacktestJobEntity(Ulids.next(clock.instant()), "rule", clock.instant());
        running.setStatus("running");
        repository.save(running);
        JobMonitor monitor = jobs.monitor(running.getId());
        monitor.checkpoint(0.5);
        assertThat(jobs.get(running.getId()).getProgress()).isEqualTo(0.5);
        jobs.cancel(running.getId());
        assertThatThrownBy(() -> monitor.checkpoint(0.75)).isInstanceOf(JobMonitor.Canceled.class);
        assertThat(jobs.get(running.getId()).getStatus()).isEqualTo("cancelled");
    }

    @Test
    @DisplayName("finished jobs older than 30 days are purged")
    void purge() {
        UUID ruleId = seed(1);
        BacktestJobEntity job = jobs.submit("rule", ruleId.toString(), null, json.readTree("{\"fromSeq\":0}"), null,
                null);
        await(jobs, job.getId());
        jdbc.update("UPDATE backtest_job SET finished_at = ? WHERE id = ?",
                Timestamp.from(clock.instant().minus(Duration.ofDays(31))), job.getId());
        assertThat(jobs.purgeFinished()).isEqualTo(1);
    }
}
