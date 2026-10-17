package ai.foresignal.exercise;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.backtest.AlertQualityService;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
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
import tools.jackson.databind.json.JsonMapper;

/**
 * Exercise isolation (prompt 115): exercise alerts reach only opted-in or named channels; they are hidden from
 * default lists, streams (the live filter), glance counts, rule statistics, digests and quality statistics; they
 * never escalate; and the real cooldown and rate counters are untouched.
 */
@IntegrationTest
@Import(TriggerTestSources.class)
class ExerciseIsolationIT {

    static final Instant T = Instant.parse("2026-09-02T08:00:00Z");
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
    @Autowired NotificationChannelRepository channels;
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

    NotificationChannelEntity channel(String name, boolean acceptExercise) {
        NotificationChannelEntity channel = new NotificationChannelEntity(UUID.randomUUID(), name, "webhook");
        channel.setConfig(new java.util.LinkedHashMap<>(Map.of("url", "https://hook.example.invalid/" + name)));
        channel.setEvents(new java.util.ArrayList<>(List.of("alert", "system")));
        channel.setEnabled(true);
        channel.setAcceptExercise(acceptExercise);
        channel.setCreatedAt(clock.instant());
        channel.setUpdatedAt(clock.instant());
        return channels.saveAndFlush(channel);
    }

    long deliveries(UUID channelId, boolean exercise) {
        Long n = jdbc.queryForObject("SELECT count(*) FROM notification_delivery d JOIN alert a ON a.id = d.alert_id"
                + " WHERE d.channel_id = ? AND a.exercise = ?", Long.class, channelId, exercise);
        return n == null ? 0 : n;
    }

    ExerciseRunEntity replay(UUID rule, long from, List<UUID> named) {
        String channelIds = String.join(",", named.stream().map(id -> "\"" + id + "\"").toList());
        ExerciseRunEntity run = exercises.create(JSON.readTree("{\"name\":\"isolation\",\"ruleIds\":[\"" + rule
                + "\"],\"fromSeq\":" + from + ",\"toSeq\":" + changeBus.lastPublishedSeq()
                + ",\"speed\":\"max\",\"channelIds\":[" + channelIds + "]}"), "token:test");
        await().atMost(WAIT).until(() -> !runs.findById(run.getId()).orElseThrow().active());
        return runs.findById(run.getId()).orElseThrow();
    }

    @Test
    @DisplayName("exercise alerts reach only opted-in or named channels; real alerts reach the ordinary ones")
    void routing() {
        NotificationChannelEntity optedIn = channel("opted-in", true);
        NotificationChannelEntity ordinary = channel("ordinary", false);
        NotificationChannelEntity named = channel("named", false);
        long from = Long.valueOf(jdbc.queryForObject("SELECT coalesce(max(seq), 0) FROM record_version", Long.class));
        UUID rule = fixture.rule("{'name':'Big','sources':['" + STANDARD + "'],'mode':'match',"
                + "'conditions':[{'path':'mag','op':'>=','value':6}]}");
        records.ingest(STANDARD, NormalizedRecord.builder("q1", STANDARD_KIND).observedAt(T).put("mag", 6.5).build());
        fixture.settle();
        await().atMost(WAIT).until(() -> deliveries(ordinary.getId(), false) == 1);
        ExerciseRunEntity run = replay(rule, from, List.of(named.getId()));
        assertThat(run.getStatus()).isEqualTo(ExerciseRunEntity.DONE);
        await().atMost(WAIT).until(() -> deliveries(optedIn.getId(), true) == 1 && deliveries(named.getId(), true) == 1);
        assertThat(deliveries(ordinary.getId(), true)).as("the ordinary channel never gets an exercise").isZero();
        Long marked = jdbc.queryForObject("SELECT count(*) FROM notification n JOIN alert a ON a.id = n.alert_id"
                + " WHERE a.exercise AND n.exercise AND n.title LIKE '[EXERCISE] %'", Long.class);
        assertThat(marked).isEqualTo(1);
        @SuppressWarnings("unchecked")
        Map<String, Object> projected = (Map<String, Object>) run.getStats().get("notificationsByChannel");
        assertThat(projected).containsKeys(optedIn.getId().toString(), named.getId().toString())
                .doesNotContainKey(ordinary.getId().toString());
        Long systemNotes = jdbc.queryForObject("SELECT count(*) FROM notification WHERE subtype LIKE"
                + " 'system.exercise_%' AND exercise", Long.class);
        assertThat(systemNotes).isEqualTo(2);
    }

    @Test
    @DisplayName("hidden from default lists, live filters, glance counts, rule and quality statistics; never escalated")
    void hidden() {
        long from = Long.valueOf(jdbc.queryForObject("SELECT coalesce(max(seq), 0) FROM record_version", Long.class));
        UUID rule = fixture.rule("{'name':'Big','sources':['" + STANDARD + "'],'mode':'match',"
                + "'conditions':[{'path':'mag','op':'>=','value':6}]}");
        records.ingest(STANDARD, NormalizedRecord.builder("q1", STANDARD_KIND).observedAt(T).put("mag", 6.5).build());
        fixture.settle();
        ExerciseRunEntity run = replay(rule, from, List.of());
        List<AlertView> all = alerts.list(AlertQuery.all().withIncludeExercise(true), null, 100);
        AlertView exercise = all.stream().filter(AlertView::exercise).findFirst().orElseThrow();
        assertThat(exercise.exerciseRunId()).isEqualTo(run.getId());
        assertThat(alerts.list(AlertQuery.all(), null, 100)).noneMatch(AlertView::exercise).hasSize(1);
        assertThat(AlertQuery.all().matches(exercise)).as("streams' live filter").isFalse();
        assertThat(AlertQuery.all().withIncludeExercise(true).matches(exercise)).isTrue();
        assertThat(alerts.countBySeverity(AlertQuery.all()).values().stream().mapToLong(Long::longValue).sum())
                .as("glance counts").isEqualTo(1);
        assertThat(alerts.stats(List.of(rule)).get(rule).alertCount()).as("rule statistics").isEqualTo(1);
        assertThat(quality.forRule(rule, T.minus(Duration.ofDays(1)))).containsEntry("alerts", 1);
        Long escalations = jdbc.queryForObject("SELECT count(*) FROM alert_escalation e JOIN alert a ON a.id ="
                + " e.alert_id WHERE a.exercise", Long.class);
        assertThat(escalations).isZero();
        Long signals = jdbc.queryForObject("SELECT count(*) FROM alert_signal s JOIN alert a ON a.id = s.alert_id"
                + " WHERE a.exercise", Long.class);
        assertThat(signals).isZero();
    }

    @Test
    @DisplayName("real cooldown and rate counters are untouched: a live match after the replay still fires")
    void realStateUntouched() {
        records.ingest(STANDARD, NormalizedRecord.builder("q1", STANDARD_KIND).observedAt(T).put("mag", 6.5).build());
        fixture.settle();
        long from = 0;
        // The rule is created after the version, so the live engine raised nothing for it.
        UUID rule = fixture.rule("{'name':'Cooled','sources':['" + STANDARD + "'],'mode':'match','cooldown':'1h',"
                + "'maxAlertsPerHour':1,'conditions':[{'path':'mag','op':'>=','value':6}]}");
        fixture.settle();
        assertThat(fixture.routed(rule)).isEmpty();
        ExerciseRunEntity run = replay(rule, from, List.of());
        assertThat(run.getStats().get("alertsRaised")).isEqualTo(1);
        clock.setInstant(T.plus(Duration.ofMinutes(5)));
        records.ingest(STANDARD, NormalizedRecord.builder("q1", STANDARD_KIND).observedAt(clock.instant())
                .put("mag", 6.8).build());
        List<AlertView> live = fixture.routed(rule);
        assertThat(live).as("the exercise alert counted in neither the cooldown nor the hourly cap").hasSize(1);
        assertThat(live.getFirst().exercise()).isFalse();
        Integer suppressed = jdbc.queryForObject("SELECT suppressed_count FROM trigger_rule WHERE id = ?",
                Integer.class, rule);
        assertThat(suppressed).isZero();
    }
}
