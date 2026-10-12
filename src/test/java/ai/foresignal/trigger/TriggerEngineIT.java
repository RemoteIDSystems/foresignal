package ai.foresignal.trigger;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK_KIND;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TTL;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TTL_KIND;
import static ai.foresignal.testsupport.trigger.TriggerTestSources.EDGE;
import static ai.foresignal.testsupport.trigger.TriggerTestSources.EDGE_KIND;
import static ai.foresignal.testsupport.trigger.TriggerTestSources.SAMPLED;
import static ai.foresignal.testsupport.trigger.TriggerTestSources.SAMPLED_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.trigger.TriggerFixture;
import ai.foresignal.testsupport.trigger.TriggerTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The trigger engine end to end: rules, the persist gate, alerts and their lifecycle (prompt 017). */
@IntegrationTest
@AutoConfigureMockMvc
@Import(TriggerTestSources.class)
class TriggerEngineIT {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();
    static final String FENCE = "{'type':'Polygon','coordinates':[[[0,0],[1,0],[1,1],[0,1],[0,0]]]}";

    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired TriggerRuleService rules;
    @Autowired TriggerRuleRepository repository;
    @Autowired TriggerEngine engine;
    @Autowired AlertStore alertStore;
    @Autowired AlertService alertService;
    @Autowired AlertResolver resolver;
    @Autowired AlertBus bus;
    @Autowired ChangeBus changeBus;
    @Autowired JdbcTemplate jdbc;
    @Autowired ApplicationEventPublisher events;
    @Autowired TriggerTestSources.StormRecorder storms;

    TriggerFixture fixture;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        storms.clear();
        fixture = new TriggerFixture(rules, engine, changeBus, alertStore, bus);
        if (!engine.running()) {
            engine.start();
        }
    }

    @AfterEach
    void tearDown() {
        fixture.close();
        if (!engine.running()) {
            engine.start();
        }
    }

    // ------------------------------------------------------------------------- helpers

    void at(Instant instant) {
        clock.setInstant(instant);
    }

    void ingest(String source, NormalizedRecord record) {
        records.ingest(source, record);
    }

    static NormalizedRecord quake(String id, Instant at, double mag) {
        return NormalizedRecord.builder(id, STANDARD_KIND).observedAt(at).put("mag", mag).build();
    }

    static NormalizedRecord vessel(String id, Instant at, double lon, double lat) {
        return NormalizedRecord.builder(id, SAMPLED_KIND).observedAt(at).point(lon, lat).put("name", "Vessel " + id).build();
    }

    List<Integer> reasons(String source, String id) {
        return jdbc.queryForList("SELECT persist_reason FROM record_version WHERE source_id = ? AND external_id = ? ORDER BY seq",
                Integer.class, source, id);
    }

    // ------------------------------------------------------------------------- matching

    @Nested
    @DisplayName("matching")
    class Matching {

        @Test
        @DisplayName("mag >= 6 fires once per matching version, with message, snapshot, severity and tags")
        void threshold() {
            UUID rule = fixture.rule("{'name':'Big','sources':['" + STANDARD + "'],'severity':'critical','tags':['quakes'],"
                    + "'conditions':[{'path':'mag','op':'>=','value':6}],'messageTemplate':'M{{payload.mag}} {{externalId}}'}");
            ingest(STANDARD, quake("q1", T, 5.0));
            at(T.plusSeconds(10));
            ingest(STANDARD, quake("q2", T.plusSeconds(10), 6.5));
            at(T.plusSeconds(20));
            ingest(STANDARD, quake("q2", T.plusSeconds(20), 7.0));
            List<AlertView> alerts = fixture.alerts(rule);
            assertThat(alerts).hasSize(2);
            AlertView first = alerts.getFirst();
            assertThat(first.externalId()).isEqualTo("q2");
            assertThat(first.message()).isEqualTo("M6.5 q2");
            assertThat(first.severity()).isEqualTo("critical");
            assertThat(first.tags()).containsExactly("quakes");
            assertThat(first.state()).isEqualTo("firing");
            assertThat(first.observedAt()).isEqualTo(T.plusSeconds(10));
            assertThat(first.kind()).isEqualTo(STANDARD_KIND);
            JsonNode snapshot = JSON.readTree(first.recordSnapshot());
            assertThat(snapshot.get("payload").get("mag").asDouble()).isEqualTo(6.5);
            assertThat(snapshot.get("version").asInt()).isEqualTo(1);
            assertThat(snapshot.get("truncated").asBoolean()).isFalse();
            assertThat(alerts.get(1).rev()).isGreaterThan(first.rev());
            AlertEvent fired = fixture.awaitEvent(AlertEvent.FIRED, first.id());
            assertThat(fired.suppressed()).isFalse();
        }

        @Test
        @DisplayName("a kinds array with an in condition")
        void kindsAndIn() {
            UUID rule = fixture.rule("{'name':'Statuses','kinds':['" + STANDARD_KIND + "'],"
                    + "'conditions':[{'path':'status','op':'in','value':['red','amber']}]}");
            ingest(STANDARD, NormalizedRecord.builder("a", STANDARD_KIND).observedAt(T).put("status", "red").build());
            ingest(STANDARD, NormalizedRecord.builder("b", STANDARD_KIND).observedAt(T).put("status", "green").build());
            ingest(STANDARD, NormalizedRecord.builder("c", STANDARD_KIND).observedAt(T).put("status", "amber").build());
            assertThat(fixture.alerts(rule)).extracting(AlertView::externalId).containsExactly("a", "c");
        }

        @Test
        @DisplayName("startsWith on a callsign")
        void startsWith() {
            UUID rule = fixture.rule("{'name':'Tails','sources':['" + TRACK + "'],"
                    + "'conditions':[{'path':'callsign','op':'startsWith','value':'N12'}]}");
            ingest(TRACK, NormalizedRecord.builder("ac1", TRACK_KIND).observedAt(T).point(1, 1).put("callsign", "N123AB").build());
            ingest(TRACK, NormalizedRecord.builder("ac2", TRACK_KIND).observedAt(T).point(1, 1).put("callsign", "B12").build());
            assertThat(fixture.alerts(rule)).extracting(AlertView::externalId).containsExactly("ac1");
        }

        @Test
        @DisplayName("HEARTBEAT does not re-fire a match rule")
        void heartbeat() {
            UUID rule = fixture.rule("{'name':'Named','sources':['" + SAMPLED + "'],"
                    + "'conditions':[{'path':'name','op':'startsWith','value':'Vessel'}]}");
            ingest(SAMPLED, vessel("v1", T, 5, 5));
            at(T.plusSeconds(3700));
            ingest(SAMPLED, vessel("v1", T.plusSeconds(3700), 5, 5));
            assertThat(reasons(SAMPLED, "v1")).containsExactly(1, 8);
            assertThat(fixture.alerts(rule)).hasSize(1);
        }
    }

    // ------------------------------------------------------------------------- edges

    @Nested
    @DisplayName("edges and the persist gate")
    class Edges {

        @Test
        @DisplayName("a geofence crossing between samples persists TRIGGER_EDGE versions and fires one enter and one exit")
        void crossingBetweenSamples() {
            UUID enter = fixture.rule("{'name':'In','sources':['" + SAMPLED + "'],'geofence':" + FENCE + ",'mode':'enter'}");
            UUID exit = fixture.rule("{'name':'Out','sources':['" + SAMPLED + "'],'geofence':" + FENCE + ",'mode':'exit'}");
            ingest(SAMPLED, vessel("v1", T, -0.5, 0.5));
            at(T.plusSeconds(10));
            ingest(SAMPLED, vessel("v1", T.plusSeconds(10), 0.5, 0.5));
            at(T.plusSeconds(20));
            ingest(SAMPLED, vessel("v1", T.plusSeconds(20), 1.5, 0.5));
            assertThat(reasons(SAMPLED, "v1")).containsExactly(1, 9, 9);
            List<AlertView> entered = fixture.alerts(enter);
            assertThat(entered).singleElement().satisfies(a -> {
                assertThat(a.observedAt()).isEqualTo(T.plusSeconds(10));
                assertThat(a.state()).as("auto-resolved by the inverse edge").isEqualTo("resolved");
                assertThat(a.resolvedBy()).isEqualTo("inverse_edge");
            });
            assertThat(fixture.alerts(exit)).singleElement()
                    .satisfies(a -> assertThat(a.observedAt()).isEqualTo(T.plusSeconds(20)));
            fixture.awaitEvent(AlertEvent.RESOLVED, entered.getFirst().id());
        }

        @Test
        @DisplayName("a rule created while live and persisted state diverge converges on the next observation")
        void convergence() {
            ingest(SAMPLED, vessel("v2", T, -0.5, 0.5));
            at(T.plusSeconds(10));
            ingest(SAMPLED, vessel("v2", T.plusSeconds(10), 0.5, 0.5));
            assertThat(reasons(SAMPLED, "v2")).as("no rule yet: the move stays live").containsExactly(1);
            UUID enter = fixture.rule("{'name':'In','sources':['" + SAMPLED + "'],'geofence':" + FENCE + ",'mode':'enter'}");
            at(T.plusSeconds(20));
            ingest(SAMPLED, vessel("v2", T.plusSeconds(20), 0.5, 0.5));
            assertThat(reasons(SAMPLED, "v2")).containsExactly(1, 9);
            assertThat(fixture.alerts(enter)).singleElement()
                    .satisfies(a -> assertThat(a.observedAt()).isEqualTo(T.plusSeconds(20)));
        }

        @Test
        @DisplayName("flapping at a threshold is bounded by edgeMinInterval")
        void flapping() {
            UUID enter = fixture.rule("{'name':'High','sources':['" + EDGE + "'],'mode':'enter',"
                    + "'conditions':[{'path':'level','op':'>=','value':10}]}");
            Instant t = T;
            double[] levels = {5, 12, 5, 12, 5, 12};
            for (double level : levels) {
                at(t);
                ingest(EDGE, NormalizedRecord.builder("g1", EDGE_KIND).observedAt(t).put("name", "Gauge").put("level", level).build());
                t = t.plusSeconds(30);
            }
            assertThat(reasons(EDGE, "g1")).as("every flip within 5 min is held back").containsExactly(1);
            at(T.plus(TriggerTestSources.EDGE_MIN_INTERVAL).plusSeconds(1));
            ingest(EDGE, NormalizedRecord.builder("g1", EDGE_KIND).observedAt(clock.instant())
                    .put("name", "Gauge").put("level", 12.0).build());
            assertThat(reasons(EDGE, "g1")).containsExactly(1, 9);
            assertThat(fixture.alerts(enter)).hasSize(1);
        }

        @Test
        @DisplayName("a tombstone, then a REAPPEAR inside, fires enter again; the tombstone resolves the first alert")
        void reappear() {
            UUID enter = fixture.rule("{'name':'In','sources':['" + STANDARD + "'],'geofence':" + FENCE + ",'mode':'enter'}");
            ingest(STANDARD, NormalizedRecord.builder("r1", STANDARD_KIND).observedAt(T).point(0.5, 0.5).build());
            at(T.plusSeconds(10));
            ingest(STANDARD, NormalizedRecord.tombstone("r1", STANDARD_KIND, T.plusSeconds(10)));
            at(T.plusSeconds(20));
            ingest(STANDARD, NormalizedRecord.builder("r1", STANDARD_KIND).observedAt(T.plusSeconds(20)).point(0.5, 0.5).build());
            assertThat(reasons(STANDARD, "r1")).containsExactly(1, 4, 2);
            List<AlertView> alerts = fixture.alerts(enter);
            assertThat(alerts).hasSize(2);
            assertThat(alerts.getFirst().resolvedBy()).isEqualTo("tombstone");
            assertThat(alerts.get(1).state()).isEqualTo("firing");
        }

        @Test
        @DisplayName("a missing predecessor (removed by retention) fires nothing")
        void missingPredecessor() {
            UUID enter = fixture.rule("{'name':'In','sources':['" + STANDARD + "'],'geofence':" + FENCE + ",'mode':'enter'}");
            engine.stop();
            ingest(STANDARD, NormalizedRecord.builder("m1", STANDARD_KIND).observedAt(T).point(-0.5, 0.5).build());
            at(T.plusSeconds(10));
            ingest(STANDARD, NormalizedRecord.builder("m1", STANDARD_KIND).observedAt(T.plusSeconds(10)).point(0.5, 0.5).build());
            // Simulates retention deleting the first version before the engine evaluates the second.
            jdbc.update("DELETE FROM record_version WHERE source_id = ? AND external_id = 'm1' AND version = 1", STANDARD);
            engine.start();
            assertThat(fixture.alerts(enter)).isEmpty();
        }
    }

    // ------------------------------------------------------------------------- cooldown, snooze, storm

    @Nested
    @DisplayName("cooldown, snoozes and the storm cap")
    class Suppression {

        @Test
        @DisplayName("cooldown counts on observed_at per entity, and per rule with cooldownScope rule")
        void cooldown() {
            UUID perRecord = fixture.rule("{'name':'R','sources':['" + STANDARD + "'],'cooldown':'10m',"
                    + "'conditions':[{'path':'mag','op':'>=','value':6}]}");
            UUID perRule = fixture.rule("{'name':'S','sources':['" + STANDARD + "'],'cooldown':'10m','cooldownScope':'rule',"
                    + "'conditions':[{'path':'mag','op':'>=','value':6}]}");
            // The clock jumps around; only observed_at matters.
            at(T.plusSeconds(5000));
            ingest(STANDARD, quake("e1", T, 6.1));
            at(T.plusSeconds(5001));
            ingest(STANDARD, quake("e1", T.plusSeconds(300), 6.2));
            ingest(STANDARD, quake("e2", T.plusSeconds(300), 6.3));
            at(T.plusSeconds(9000));
            ingest(STANDARD, quake("e1", T.plusSeconds(660), 6.4));
            assertThat(fixture.alerts(perRecord)).extracting(a -> a.externalId() + "@" + a.observedAt())
                    .containsExactly("e1@" + T, "e2@" + T.plusSeconds(300), "e1@" + T.plusSeconds(660));
            assertThat(fixture.alerts(perRule)).extracting(a -> a.externalId() + "@" + a.observedAt())
                    .containsExactly("e1@" + T, "e1@" + T.plusSeconds(660));
        }

        @Test
        @DisplayName("a rule snooze and an alert snooze record suppressed alerts")
        void snoozes() {
            UUID rule = fixture.rule("{'name':'R','sources':['" + STANDARD + "'],'conditions':[{'path':'mag','op':'>=','value':6}]}");
            ingest(STANDARD, quake("s1", T, 6.0));
            AlertView first = fixture.alerts(rule).getFirst();
            alertService.snooze(first.id(), T.plusSeconds(3600));
            at(T.plusSeconds(2));
            ingest(STANDARD, quake("s1", T.plusSeconds(1), 6.1));
            ingest(STANDARD, quake("s2", T.plusSeconds(1), 6.1));
            rules.snooze(rule, T.plusSeconds(3600));
            ingest(STANDARD, quake("s3", T.plusSeconds(2), 6.2));
            List<AlertView> alerts = fixture.alerts(rule);
            assertThat(alerts).extracting(a -> a.externalId() + ":" + a.suppressedReason())
                    .containsExactly("s1:null", "s1:alert_snoozed", "s2:null", "s3:rule_snoozed");
            AlertEvent suppressed = fixture.awaitEvent(AlertEvent.FIRED, alerts.get(1).id());
            assertThat(suppressed.suppressed()).isTrue();
        }

        @Test
        @DisplayName("the storm cap suppresses, counts and emits one RuleStormEvent; autoDisableOnStorm disables")
        void storm() {
            UUID rule = fixture.rule("{'name':'Noisy','sources':['" + STANDARD + "'],'maxAlertsPerHour':2,"
                    + "'conditions':[{'path':'mag','op':'>','value':0}]}");
            records.ingest(STANDARD, quake("a", T, 1), quake("b", T, 1), quake("c", T, 1), quake("d", T, 1));
            ingest(STANDARD, quake("e", T, 1));
            assertThat(fixture.alerts(rule)).hasSize(2);
            assertThat(repository.findById(rule).orElseThrow().getSuppressedCount()).isEqualTo(3);
            assertThat(repository.findById(rule).orElseThrow().getLastStormAt()).isEqualTo(T);
            await().atMost(TriggerFixture.WAIT).until(() -> !storms.events().isEmpty());
            assertThat(storms.events()).singleElement().satisfies(e -> {
                assertThat(e.ruleId()).isEqualTo(rule);
                assertThat(e.maxAlertsPerHour()).isEqualTo(2);
                assertThat(e.disabled()).isFalse();
            });

            UUID disabling = fixture.rule("{'name':'Off','sources':['" + STANDARD + "'],'maxAlertsPerHour':1,"
                    + "'autoDisableOnStorm':true,'conditions':[{'path':'mag','op':'>','value':5}]}");
            records.ingest(STANDARD, quake("x", T, 6), quake("y", T, 6));
            fixture.settle();
            assertThat(repository.findById(disabling).orElseThrow().isEnabled()).isFalse();
            assertThat(fixture.alerts(disabling)).hasSize(1);
        }
    }

    // ------------------------------------------------------------------------- resolution

    @Nested
    @DisplayName("auto-resolve")
    class Resolution {

        @Test
        @DisplayName("expiry resolves an alert once the entity's live row has expired, and publishes alert.resolved")
        void expiry() {
            UUID rule = fixture.rule("{'name':'Events','sources':['" + TTL + "'],'autoResolve':{'onExpiry':true},"
                    + "'conditions':[{'path':'level','op':'>','value':1}]}");
            ingest(TTL, NormalizedRecord.builder("x1", TTL_KIND).observedAt(T).put("level", 2).build());
            AlertView alert = fixture.alerts(rule).getFirst();
            assertThat(resolver.runOnce()).isZero();
            at(T.plus(Duration.ofMinutes(11)));
            assertThat(resolver.runOnce()).isEqualTo(1);
            AlertView resolved = alertStore.find(alert.id()).orElseThrow();
            assertThat(resolved.resolvedBy()).isEqualTo("expiry");
            assertThat(resolved.resolvedAt()).isEqualTo(clock.instant());
            fixture.awaitEvent(AlertEvent.RESOLVED, alert.id());
        }

        @Test
        @DisplayName("staleAfter resolves an alert of an entity not observed for that long")
        void stale() {
            UUID rule = fixture.rule("{'name':'Quiet','sources':['" + STANDARD + "'],'autoResolve':{'onExpiry':false,'staleAfter':'30m'},"
                    + "'conditions':[{'path':'mag','op':'>','value':1}]}");
            ingest(STANDARD, quake("st", T, 2));
            AlertView alert = fixture.alerts(rule).getFirst();
            at(T.plus(Duration.ofMinutes(31)));
            resolver.runOnce();
            assertThat(alertStore.find(alert.id()).orElseThrow().resolvedBy()).isEqualTo("stale");
        }

        @Test
        @DisplayName("the resolver disables rules past activeUntil and clears expired rule snoozes")
        void housekeeping() {
            UUID ending = fixture.rule("{'name':'Ends','sources':['" + STANDARD + "'],'activeUntil':'2026-05-01T12:30:00Z'}");
            UUID snoozed = fixture.rule("{'name':'Zz','sources':['" + STANDARD + "']}");
            rules.snooze(snoozed, T.plusSeconds(60));
            at(T.plus(Duration.ofHours(1)));
            resolver.runOnce();
            assertThat(repository.findById(ending).orElseThrow().isEnabled()).isFalse();
            assertThat(repository.findById(snoozed).orElseThrow().getSnoozedUntil()).isNull();
        }
    }

    // ------------------------------------------------------------------------- durability

    @Nested
    @DisplayName("durability and isolation")
    class Durability {

        @Test
        @DisplayName("the cursor survives a restart: every version is evaluated exactly once")
        void restart() {
            UUID rule = fixture.rule("{'name':'R','sources':['" + STANDARD + "'],'conditions':[{'path':'mag','op':'>','value':1}]}");
            ingest(STANDARD, quake("k1", T, 2));
            assertThat(fixture.alerts(rule)).hasSize(1);
            engine.stop();
            ingest(STANDARD, quake("k2", T, 2));
            assertThat(jdbc.queryForObject("SELECT count(*) FROM alert", Integer.class)).isEqualTo(1);
            engine.start();
            engine.stop();
            engine.start();
            assertThat(fixture.alerts(rule)).extracting(AlertView::externalId).containsExactly("k1", "k2");
            String stored = jdbc.queryForObject("SELECT value::text FROM app_setting WHERE key = 'trigger.cursor'", String.class);
            assertThat(Long.parseLong(stored)).isEqualTo(engine.cursor());
        }

        @Test
        @DisplayName("onGap catches up from the cursor")
        void gap() {
            UUID rule = fixture.rule("{'name':'R','sources':['" + STANDARD + "'],'conditions':[{'path':'mag','op':'>','value':1}]}");
            engine.stop();
            ingest(STANDARD, quake("g1", T, 2));
            engine.onGap(0);
            assertThat(fixture.alerts(rule)).extracting(AlertView::externalId).containsExactly("g1");
        }

        @Test
        @DisplayName("a broken rule gets last_error and does not block the others")
        void brokenRule() {
            UUID good = fixture.rule("{'name':'Good','sources':['" + STANDARD + "'],'conditions':[{'path':'mag','op':'>','value':1}]}");
            UUID bad = fixture.rule("{'name':'Bad','sources':['" + STANDARD + "'],'conditions':[{'path':'mag','op':'>','value':1}]}");
            jdbc.update("UPDATE trigger_rule SET conditions = '[{\"path\":\"mag\",\"op\":\"bogus\",\"value\":1}]'::jsonb WHERE id = ?", bad);
            events.publishEvent(new TriggerRulesChangedEvent(null));
            ingest(STANDARD, quake("b1", T, 2));
            assertThat(fixture.alerts(good)).hasSize(1);
            assertThat(fixture.alerts(bad)).isEmpty();
            assertThat(repository.findById(bad).orElseThrow().getLastError()).contains("bogus");
        }
    }
}
