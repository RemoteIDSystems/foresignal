package ai.foresignal.exercise;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.retention.RetentionStateStore;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.trigger.TriggerFixture;
import ai.foresignal.testsupport.trigger.TriggerTestSources;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertQuery;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.TriggerEngine;
import ai.foresignal.trigger.TriggerRuleService;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Exercise replay runs (prompt 115) over records seeded with {@code TestRecords}: a replayed window fires the
 * same alerts the live engine produced (match and enter), in run-local cooldown; a missing predecessor at the
 * window start fires nothing and adds the caveat; a window below the floor is refused naming the source; a dry
 * run counts without delivering; a cancellation stops mid-run; a purge removes alerts and notifications.
 */
@IntegrationTest
@Import(TriggerTestSources.class)
class ReplayRunIT {

    static final Instant T = Instant.parse("2026-09-01T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Duration WAIT = Duration.ofSeconds(30);

    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired TriggerRuleService rules;
    @Autowired TriggerEngine engine;
    @Autowired AlertStore alerts;
    @Autowired AlertBus bus;
    @Autowired ChangeBus changeBus;
    @Autowired ExerciseService exercises;
    @Autowired ExerciseRunRepository runs;
    @Autowired RetentionStateStore retention;
    @Autowired JdbcTemplate jdbc;

    TriggerFixture fixture;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        fixture = new TriggerFixture(rules, engine, changeBus, alerts, bus);
        if (!engine.running()) {
            engine.start();
        }
        exercises.setSleeper(ReplayPacer.THREAD_SLEEP);
    }

    @AfterEach
    void tearDown() {
        fixture.close();
        exercises.setSleeper(ReplayPacer.THREAD_SLEEP);
        retention.reload();
    }

    long maxSeq() {
        Long seq = jdbc.queryForObject("SELECT coalesce(max(seq), 0) FROM record_version", Long.class);
        return seq == null ? 0 : seq;
    }

    static NormalizedRecord quake(String id, Instant at, double mag) {
        return NormalizedRecord.builder(id, STANDARD_KIND).observedAt(at).put("mag", mag).build();
    }

    void ingest(String id, Instant at, double mag) {
        clock.setInstant(at);
        records.ingest(STANDARD, quake(id, at, mag));
    }

    String body(String name, List<UUID> ruleIds, long fromSeq, long toSeq, String speed, boolean dryRun) {
        StringBuilder ids = new StringBuilder();
        for (UUID id : ruleIds) {
            ids.append(ids.isEmpty() ? "" : ",").append('"').append(id).append('"');
        }
        return "{\"name\":\"" + name + "\",\"ruleIds\":[" + ids + "],\"fromSeq\":" + fromSeq + ",\"toSeq\":" + toSeq
                + ",\"speed\":\"" + speed + "\",\"dryRun\":" + dryRun + "}";
    }

    ExerciseRunEntity awaitFinished(String id) {
        await().atMost(WAIT).until(() -> !runs.findById(id).orElseThrow().active());
        return runs.findById(id).orElseThrow();
    }

    List<AlertView> exerciseAlerts(String runId) {
        fixture.settle();
        return alerts.list(AlertQuery.all().withIncludeExercise(true), null, 10_000).stream()
                .filter(a -> runId.equals(a.exerciseRunId()))
                .sorted((a, b) -> Long.compare(a.seq(), b.seq()))
                .toList();
    }

    @Test
    @DisplayName("a replayed window fires the same alerts (seq and observed_at) as the live engine, match and enter")
    void sameAsLive() {
        long from = maxSeq();
        UUID match = fixture.rule("{'name':'Big','sources':['" + STANDARD + "'],'mode':'match',"
                + "'conditions':[{'path':'mag','op':'>=','value':6}]}");
        UUID enter = fixture.rule("{'name':'Enter big','sources':['" + STANDARD + "'],'mode':'enter',"
                + "'conditions':[{'path':'mag','op':'>=','value':6}]}");
        ingest("q1", T, 5.0);
        ingest("q1", T.plusSeconds(60), 6.5);
        ingest("q1", T.plusSeconds(120), 7.0);
        ingest("q1", T.plusSeconds(180), 4.0);
        ingest("q1", T.plusSeconds(240), 6.1);
        ingest("q2", T.plusSeconds(300), 6.4);
        fixture.settle();
        long to = changeBus.lastPublishedSeq();
        List<Long> liveMatch = fixture.routed(match).stream().map(AlertView::seq).sorted().toList();
        List<Long> liveEnter = fixture.routed(enter).stream().map(AlertView::seq).sorted().toList();
        assertThat(liveMatch).hasSize(4);
        assertThat(liveEnter).hasSize(3);

        clock.setInstant(T.plusSeconds(600));
        ExerciseRunEntity run = exercises.create(JSON.readTree(body("rehearsal", List.of(match, enter), from, to,
                "max", false)), "token:test");
        ExerciseRunEntity done = awaitFinished(run.getId());
        assertThat(done.getStatus()).isEqualTo(ExerciseRunEntity.DONE);
        List<AlertView> replayed = exerciseAlerts(run.getId());
        assertThat(replayed.stream().filter(a -> a.ruleId().equals(match)).map(AlertView::seq).toList())
                .containsExactlyElementsOf(liveMatch);
        assertThat(replayed.stream().filter(a -> a.ruleId().equals(enter)).map(AlertView::seq).toList())
                .containsExactlyElementsOf(liveEnter);
        assertThat(replayed).allSatisfy(a -> {
            assertThat(a.exercise()).isTrue();
            assertThat(a.recordSnapshot()).isNotNull();
            assertThat(a.observedAt()).isBetween(T, T.plusSeconds(300));
        });
        @SuppressWarnings("unchecked")
        Map<String, Object> byRule = (Map<String, Object>) done.getStats().get("alertsByRule");
        assertThat(((Number) byRule.get(match.toString())).intValue()).isEqualTo(4);
        assertThat(((Number) done.getStats().get("versionsScanned")).longValue()).isGreaterThanOrEqualTo(6);
        assertThat(done.getStats().get("alertsRaised")).isEqualTo(7);
    }

    @Test
    @DisplayName("the cooldown is run-local: the live alerts in the table never suppress the replay")
    void runLocalCooldown() {
        long from = maxSeq();
        UUID rule = fixture.rule("{'name':'Cooled','sources':['" + STANDARD + "'],'mode':'match','cooldown':'10m',"
                + "'conditions':[{'path':'mag','op':'>=','value':6}]}");
        ingest("q1", T, 6.0);
        ingest("q1", T.plusSeconds(60), 6.2);
        ingest("q1", T.plusSeconds(900), 6.4);
        fixture.settle();
        assertThat(fixture.routed(rule)).hasSize(2);
        ExerciseRunEntity run = exercises.create(JSON.readTree(body("cooldown", List.of(rule), from,
                changeBus.lastPublishedSeq(), "max", false)), "token:test");
        ExerciseRunEntity done = awaitFinished(run.getId());
        assertThat(exerciseAlerts(run.getId())).hasSize(2);
        assertThat(((Number) done.getStats().get("suppressedByCooldown")).intValue()).isEqualTo(1);
        assertThat(fixture.routed(rule)).as("real alerts unchanged").hasSize(2);
    }

    @Test
    @DisplayName("a missing predecessor at the window start fires nothing and adds PREDECESSOR_OUTSIDE_WINDOW")
    @SuppressWarnings("unchecked")
    void missingPredecessor() {
        UUID enter = fixture.rule("{'name':'Enter','sources':['" + STANDARD + "'],'mode':'enter',"
                + "'conditions':[{'path':'mag','op':'>=','value':6}]}");
        ingest("q1", T, 4.0);
        long predecessor = maxSeq();
        ingest("q1", T.plusSeconds(60), 6.5);
        fixture.settle();
        assertThat(fixture.routed(enter)).hasSize(1);
        // Retention removed the predecessor: the version in the window has a prev_seq that no longer resolves.
        jdbc.update("DELETE FROM record_version WHERE seq = ?", predecessor);
        ExerciseRunEntity run = exercises.create(JSON.readTree(body("orphan", List.of(enter), predecessor,
                changeBus.lastPublishedSeq(), "max", false)), "token:test");
        ExerciseRunEntity done = awaitFinished(run.getId());
        assertThat(exerciseAlerts(run.getId())).isEmpty();
        List<Map<String, Object>> caveats = (List<Map<String, Object>>) done.getStats().get("caveats");
        assertThat(caveats).anyMatch(c -> ExerciseReplay.PREDECESSOR_OUTSIDE_WINDOW.equals(c.get("code")));
    }

    @Test
    @DisplayName("a window below a source's floor is 400 REPLAY_BEYOND_RETENTION naming the source")
    void beyondRetention() {
        long from = maxSeq();
        UUID rule = fixture.rule("{'name':'Big','sources':['" + STANDARD + "'],'mode':'match',"
                + "'conditions':[{'path':'mag','op':'>=','value':6}]}");
        ingest("q1", T, 6.0);
        ingest("q2", T.plusSeconds(60), 6.0);
        fixture.settle();
        long floor = changeBus.lastPublishedSeq();
        jdbc.update("INSERT INTO retention_state (source_id, kind, floor_seq, purge_seq, purged_at, last_run_at)"
                + " VALUES (?, '*', ?, 0, NULL, ?)", STANDARD, floor, Timestamp.from(clock.instant()));
        retention.reload();
        assertThatThrownBy(() -> exercises.create(JSON.readTree(body("old", List.of(rule), from, floor, "max", false)),
                "token:test"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining(STANDARD)
                .satisfies(e -> assertThat(((ValidationException) e).getCode())
                        .isEqualTo(ErrorCodes.REPLAY_BEYOND_RETENTION));
    }

    @Test
    @DisplayName("a dry run projects counts per rule and channel and delivers nothing")
    void dryRun() {
        long from = maxSeq();
        UUID rule = fixture.rule("{'name':'Big','sources':['" + STANDARD + "'],'mode':'match',"
                + "'conditions':[{'path':'mag','op':'>=','value':6}]}");
        ingest("q1", T, 6.0);
        ingest("q2", T.plusSeconds(60), 7.0);
        fixture.settle();
        ExerciseRunEntity projection = exercises.create(JSON.readTree(body("dry", List.of(rule), from,
                changeBus.lastPublishedSeq(), "max", true)), "token:test");
        assertThat(runs.existsById(projection.getId())).isFalse();
        @SuppressWarnings("unchecked")
        Map<String, Object> byRule = (Map<String, Object>) projection.getStats().get("alertsByRule");
        assertThat(((Number) byRule.get(rule.toString())).intValue()).isEqualTo(2);
        assertThat(projection.getStats()).containsKey("notificationsByChannel");
        assertThat(alerts.list(AlertQuery.all().withIncludeExercise(true), null, 100))
                .noneMatch(AlertView::exercise);
    }

    @Test
    @DisplayName("a cancellation stops the run between matches; one run at a time; purge needs a finished run")
    void cancellation() throws Exception {
        long from = maxSeq();
        UUID rule = fixture.rule("{'name':'Big','sources':['" + STANDARD + "'],'mode':'match',"
                + "'conditions':[{'path':'mag','op':'>=','value':6}]}");
        for (int i = 0; i < 5; i++) {
            ingest("q" + i, T.plusSeconds(60L * i), 6.0 + i);
        }
        fixture.settle();
        CountDownLatch sleeping = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        exercises.setSleeper(d -> {
            sleeping.countDown();
            release.await(WAIT.toSeconds(), TimeUnit.SECONDS);
        });
        ExerciseRunEntity run = exercises.create(JSON.readTree(body("slow", List.of(rule), from,
                changeBus.lastPublishedSeq(), "realtime", false)), "token:test");
        assertThat(sleeping.await(WAIT.toSeconds(), TimeUnit.SECONDS)).isTrue();
        assertThatThrownBy(() -> exercises.create(JSON.readTree(body("second", List.of(rule), from,
                changeBus.lastPublishedSeq(), "max", false)), "token:test"))
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> exercises.purge(run.getId(), "token:test")).isInstanceOf(ConflictException.class);
        exercises.cancel(run.getId(), "token:test");
        release.countDown();
        ExerciseRunEntity done = awaitFinished(run.getId());
        assertThat(done.getStatus()).isEqualTo(ExerciseRunEntity.CANCELED);
        assertThat(exerciseAlerts(run.getId()).size()).isLessThan(5);
        assertThatThrownBy(() -> exercises.cancel(run.getId(), "token:test")).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("purge removes the run's exercise alerts and their notifications, never real ones")
    void purge() {
        long from = maxSeq();
        UUID rule = fixture.rule("{'name':'Big','sources':['" + STANDARD + "'],'mode':'match',"
                + "'conditions':[{'path':'mag','op':'>=','value':6}]}");
        ingest("q1", T, 6.0);
        ingest("q2", T.plusSeconds(60), 7.0);
        fixture.settle();
        ExerciseRunEntity run = exercises.create(JSON.readTree(body("purge", List.of(rule), from,
                changeBus.lastPublishedSeq(), "max", false)), "token:test");
        awaitFinished(run.getId());
        List<AlertView> replayed = exerciseAlerts(run.getId());
        assertThat(replayed).hasSize(2);
        await().atMost(WAIT).until(() -> count("SELECT count(*) FROM notification n JOIN alert a ON a.id = n.alert_id"
                + " WHERE a.exercise_run_id = ?", run.getId()) == 2);
        assertThat(count("SELECT count(*) FROM notification n JOIN alert a ON a.id = n.alert_id"
                + " WHERE a.exercise_run_id = ? AND NOT n.exercise", run.getId())).isZero();
        int deleted = exercises.purge(run.getId(), "token:test");
        assertThat(deleted).isEqualTo(2);
        assertThat(exerciseAlerts(run.getId())).isEmpty();
        String ids = String.join(",", replayed.stream().map(a -> Long.toString(a.id())).toList());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM notification WHERE alert_id IN (" + ids + ")", Long.class))
                .isZero();
        assertThat(fixture.routed(rule)).as("real alerts kept").hasSize(2);
        assertThat(Set.copyOf(exercises.list(null, null, 10).stream().map(ExerciseRunEntity::getId).toList()))
                .contains(run.getId());
    }

    long count(String sql, Object arg) {
        Long n = jdbc.queryForObject(sql, Long.class, arg);
        return n == null ? 0 : n;
    }
}
