package ai.foresignal.quality;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.backtest.AlertQualityService;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.trigger.TriggerFixture;
import ai.foresignal.testsupport.trigger.TriggerTestSources;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertService;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.TriggerEngine;
import ai.foresignal.trigger.TriggerRuleService;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Signals and daily trends end to end (prompt 115): a fast acknowledgement and a snooze are recorded as signals,
 * an unread notification becomes {@code UNOPENED} where read tracking is in use, the nightly job writes
 * {@code rule_quality_daily} without exercise alerts, and the quality response carries {@code signals},
 * {@code signalScore} and {@code daily[]}.
 */
@IntegrationTest
@Import(TriggerTestSources.class)
class RuleQualityDailyJobIT {

    static final Instant T = Instant.parse("2026-09-04T09:00:00Z");
    static final Duration WAIT = Duration.ofSeconds(20);

    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired TriggerRuleService rules;
    @Autowired TriggerEngine engine;
    @Autowired AlertStore alerts;
    @Autowired AlertService alertService;
    @Autowired AlertBus bus;
    @Autowired ChangeBus changeBus;
    @Autowired AlertSignalStore signals;
    @Autowired RuleQualityDailyJob job;
    @Autowired AlertQualityService quality;
    @Autowired JdbcTemplate jdbc;

    TriggerFixture fixture;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        fixture = new TriggerFixture(rules, engine, changeBus, alerts, bus);
        if (!engine.running()) {
            engine.start();
        }
    }

    @AfterEach
    void tearDown() {
        fixture.close();
    }

    long signalCount(long alertId, String type) {
        Long n = jdbc.queryForObject("SELECT count(*) FROM alert_signal WHERE alert_id = ? AND type = ?", Long.class,
                alertId, type);
        return n == null ? 0 : n;
    }

    @Test
    @DisplayName("signals, daily aggregation without exercise alerts, and daily[] in the quality response")
    @SuppressWarnings("unchecked")
    void daily() {
        UUID rule = fixture.rule("{'name':'Big','sources':['" + STANDARD + "'],'mode':'match',"
                + "'conditions':[{'path':'mag','op':'>=','value':6}]}");
        records.ingest(STANDARD, NormalizedRecord.builder("q1", STANDARD_KIND).observedAt(T).put("mag", 6.5).build());
        records.ingest(STANDARD, NormalizedRecord.builder("q2", STANDARD_KIND).observedAt(T).put("mag", 6.6).build());
        records.ingest(STANDARD, NormalizedRecord.builder("q3", STANDARD_KIND).observedAt(T).put("mag", 6.7).build());
        List<AlertView> fired = fixture.routed(rule);
        assertThat(fired).hasSize(3);
        clock.setInstant(T.plus(Duration.ofMinutes(5)));
        alertService.acknowledge(fired.get(0).id(), AlertService.Transition.NONE, "token:a");
        alertService.setFeedback(fired.get(0).id(), new AlertService.Transition(null, "useful"));
        alertService.snooze(fired.get(1).id(), T.plus(Duration.ofHours(2)));
        alertService.setFeedback(fired.get(1).id(), new AlertService.Transition(null, "noise"));
        await().atMost(WAIT).until(() -> signalCount(fired.get(0).id(), "ACK_FAST") == 1
                && signalCount(fired.get(1).id(), "SNOOZED") == 1);

        // An exercise copy of the third alert never counts.
        jdbc.update("INSERT INTO alert (rule_id, seq, source_id, external_id, kind, fired_at, observed_at, message,"
                + " severity, state, suppressed, tags, rev, sensitive, exercise, exercise_run_id) SELECT rule_id, seq,"
                + " source_id, external_id, kind, fired_at, observed_at, message, severity, 'firing', false, tags,"
                + " nextval('alert_rev'), false, true, 'R' FROM alert WHERE id = ?", fired.get(2).id());
        long exerciseId = jdbc.queryForObject("SELECT max(id) FROM alert WHERE exercise", Long.class);
        assertThat(signals.record(exerciseId, AlertSignalType.SNOOZED, T)).isFalse();

        // Read tracking is in use (a read cursor exists below the third alert's notification), so the unread
        // notification older than 24 h becomes UNOPENED.
        await().atMost(WAIT).until(() -> jdbc.queryForObject("SELECT count(*) FROM notification WHERE alert_id = ?",
                Long.class, fired.get(2).id()) == 1);
        jdbc.update("UPDATE notification SET created_at = ? WHERE alert_id IS NOT NULL",
                Timestamp.from(T.minus(Duration.ofHours(30))));
        jdbc.update("INSERT INTO notification_read_cursor (device_key, read_up_to_id, updated_at) VALUES ('d', 0, ?)",
                Timestamp.from(T));
        RuleQualityDailyJob.Result result = job.run(T.plus(Duration.ofHours(1)));
        assertThat(result.unopened()).isEqualTo(3);
        assertThat(result.dailyRows()).isPositive();

        List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM rule_quality_daily WHERE rule_id = ?", rule);
        assertThat(rows).hasSize(1);
        Map<String, Object> row = rows.getFirst();
        assertThat(((Number) row.get("fired")).intValue()).as("exercise excluded").isEqualTo(3);
        assertThat(((Number) row.get("useful")).intValue()).isEqualTo(1);
        assertThat(((Number) row.get("noise")).intValue()).isEqualTo(1);
        assertThat(((Number) row.get("median_ack_seconds")).doubleValue()).isEqualTo(300.0);

        Map<String, Object> report = quality.forRule(rule, T.minus(Duration.ofDays(1)));
        Map<String, Long> counts = (Map<String, Long>) report.get("signals");
        assertThat(counts).containsEntry("ACK_FAST", 1L).containsEntry("SNOOZED", 1L).containsEntry("UNOPENED", 3L);
        assertThat(report.get("signalScore")).isEqualTo(AlertSignalType.score(counts, 3));
        List<Map<String, Object>> daily = (List<Map<String, Object>>) report.get("daily");
        assertThat(daily).hasSize(1);
        assertThat(daily.getFirst()).containsEntry("fired", 3).containsEntry("day", "2026-09-04");
        assertThat((Map<String, Object>) daily.getFirst().get("feedback")).containsEntry("useful", 1);
        assertThat(report.get("precision")).as("feedback precision is separate (fewer than five labels)").isNull();
        // RuleQuality's keys are spelled in US English (they were the British forms before the cleanup pass).
        assertThat(report).containsKeys("labeled", "labeledShare");
    }

    @Test
    @DisplayName("a rule snooze records SNOOZED for the rule's open alerts")
    void ruleSnooze() {
        UUID rule = fixture.rule("{'name':'Big','sources':['" + STANDARD + "'],'mode':'match',"
                + "'conditions':[{'path':'mag','op':'>=','value':6}]}");
        records.ingest(STANDARD, NormalizedRecord.builder("q1", STANDARD_KIND).observedAt(T).put("mag", 6.5).build());
        AlertView alert = fixture.routed(rule).getFirst();
        rules.snooze(rule, T.plus(Duration.ofHours(1)));
        assertThat(signalCount(alert.id(), "SNOOZED")).isEqualTo(1);
    }
}
