package ai.foresignal.trigger.window;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.place.PlaceService;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.trigger.RuleSpecs;
import ai.foresignal.trigger.TriggerEngine;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The count rule end to end (prompt 100), seeded with {@link TestRecords} only: a clear → active
 * transition fires one alert that references the persisted {@code rule-windows} version and resolves on
 * active → clear; distinct entities count once however many versions they have; an entity seen only
 * through a touch (no new version) still counts through its live row; per-group states with
 * {@code groupBy=place}; a windowed rule never fires per version through the 017 engine; and a window
 * over the row cap is unknown, never a partial count.
 */
@WebIntegrationTest
@Import(TestIngestSources.class)
class WindowEvaluatorIT {

    private static final Instant T = Instant.parse("2026-03-01T12:00:00Z");
    private static final String SOURCE = TestIngestSources.STANDARD;
    private static final String KIND = TestIngestSources.STANDARD_KIND;

    @Autowired TriggerRuleService rules;
    @Autowired WindowEngine engine;
    @Autowired WindowMembership membership;
    @Autowired RuleSpecs specs;
    @Autowired TriggerEngine triggerEngine;
    @Autowired ChangeBus changeBus;
    @Autowired PlaceService places;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper json;

    @BeforeEach
    void freeze() {
        clock.setInstant(T);
    }

    private UUID countRule(int threshold, String extra) {
        return rules.create(json.readTree("{\"name\":\"count\",\"type\":\"count\",\"sources\":[\"" + SOURCE + "\"],"
                + "\"window\":{\"window\":\"30m\",\"op\":\">=\",\"threshold\":" + threshold + extra + "}}")).rule().getId();
    }

    private void thing(String id, Instant at, String name) {
        records.ingestAt(SOURCE, at, NormalizedRecord.builder(id, KIND).observedAt(at).put("name", name).build());
    }

    private void thingAt(String id, Instant at, double lon, double lat) {
        records.ingestAt(SOURCE, at,
                NormalizedRecord.builder(id, KIND).observedAt(at).point(lon, lat).put("name", id).build());
    }

    private int alerts(UUID rule) {
        Integer n = jdbc.queryForObject("select count(*) from alert where rule_id = ?", Integer.class, rule);
        return n == null ? 0 : n;
    }

    @Test
    @DisplayName("a count rule fires once on clear → active, referencing the rule-windows version, and resolves on"
            + " active → clear")
    void countFiresOnceAndResolves() {
        thing("a", T.minus(Duration.ofMinutes(5)), "a");
        thing("b", T.minus(Duration.ofMinutes(5)), "b");
        UUID id = countRule(2, "");
        WindowEmitters.Ingesting emitter = new WindowEmitters.Ingesting(records, clock);

        engine.evaluate(T, emitter);
        assertThat(emitter.hasState(id + ":*", "active")).isTrue();
        assertThat(alerts(id)).as("owed until the active version is persisted").isZero();

        clock.advance(Duration.ofSeconds(10));
        engine.evaluate(clock.instant(), emitter);
        assertThat(alerts(id)).isEqualTo(1);
        Long headSeq = jdbc.queryForObject("select seq from record_head where source_id = ? and external_id = ?",
                Long.class, WindowEngine.SOURCE_ID, id + ":*");
        Long alertSeq = jdbc.queryForObject("select seq from alert where rule_id = ?", Long.class, id);
        assertThat(alertSeq).isEqualTo(headSeq);

        // Still active a step later: no second alert.
        clock.advance(Duration.ofMinutes(6));
        engine.evaluate(clock.instant(), emitter);
        assertThat(alerts(id)).isEqualTo(1);

        // The window moves past the records: active → clear resolves the alert.
        clock.advance(Duration.ofMinutes(40));
        engine.evaluate(clock.instant(), emitter);
        assertThat(emitter.lastState(id + ":*", "state")).isEqualTo("clear");
        assertThat(jdbc.queryForObject("select state from alert where rule_id = ?", String.class, id))
                .isEqualTo("resolved");
    }

    @Test
    @DisplayName("distinct entities, not versions")
    void distinctEntities() {
        thing("a", T.minus(Duration.ofMinutes(10)), "a1");
        thing("a", T.minus(Duration.ofMinutes(5)), "a2");
        assertThat(jdbc.queryForObject("select count(*) from record_version where source_id = ? and external_id = 'a'",
                Integer.class, SOURCE)).isEqualTo(2);
        UUID id = countRule(2, "");
        WindowEmitters.Capture capture = new WindowEmitters.Capture();
        engine.evaluate(T, capture);
        assertThat(capture.lastState(id + ":*", "state")).isEqualTo("clear");
        assertThat(capture.lastState(id + ":*", "value")).isEqualTo(1.0);
    }

    @Test
    @DisplayName("an entity seen only through a touch (no new version in the window) counts through its live row")
    void touchOnlyCountsThroughTheLiveRow() {
        Instant first = T.minus(Duration.ofHours(2));
        clock.setInstant(first);
        thing("t", first, "same");
        clock.setInstant(T);
        // Identical content two hours later: a touch (row 11), which moves the live row but writes no version.
        thing("t", T, "same");
        assertThat(jdbc.queryForObject("select count(*) from record_version where source_id = ? and external_id = 't'",
                Integer.class, SOURCE)).isEqualTo(1);

        UUID id = countRule(1, "");
        WindowEmitters.Capture capture = new WindowEmitters.Capture();
        engine.evaluate(T.plusSeconds(60), capture);
        assertThat(capture.lastState(id + ":*", "state")).isEqualTo("active");
        assertThat(capture.lastState(id + ":*", "value")).isEqualTo(1.0);
    }

    @Test
    @DisplayName("per-group states with groupBy=place")
    void groupsByPlace() {
        places.create(json.readTree("{\"id\":\"east\",\"name\":\"East\",\"shape\":\"point\",\"geometry\":{\"type\":\"Point\","
                + "\"coordinates\":[-95.0,30.0]},\"radiusKm\":20,\"timezone\":\"America/Chicago\"}"));
        places.create(json.readTree("{\"id\":\"west\",\"name\":\"West\",\"shape\":\"point\",\"geometry\":{\"type\":\"Point\","
                + "\"coordinates\":[-105.0,35.0]},\"radiusKm\":20,\"timezone\":\"America/Denver\"}"));
        Instant at = T.minus(Duration.ofMinutes(5));
        thingAt("e1", at, -95.01, 30.01);
        thingAt("e2", at, -94.99, 29.99);
        thingAt("w1", at, -105.01, 35.01);
        UUID id = countRule(2, ",\"groupBy\":\"place\"");

        WindowEmitters.Capture capture = new WindowEmitters.Capture();
        engine.evaluate(T, capture);

        assertThat(capture.lastState(id + ":east", "state")).isEqualTo("active");
        assertThat(capture.lastState(id + ":east", "value")).isEqualTo(2.0);
        assertThat(capture.lastState(id + ":west", "state")).isEqualTo("clear");
        assertThat(capture.lastState(id + ":west", "value")).isEqualTo(1.0);
    }

    @Test
    @DisplayName("a windowed rule is never compiled into the per-version engine: no alert per matching version")
    void windowedRuleNeverFiresPerVersion() {
        UUID id = countRule(100, "");
        thing("a", T, "a");
        thing("b", T, "b");
        thing("a", T.plusSeconds(1), "a-changed");
        triggerEngine.catchUp();
        assertThat(alerts(id)).isZero();
    }

    @Test
    @DisplayName("a window with more candidate rows than the cap is unknown, never a partial count")
    void capIsUnknown() {
        thing("a", T.minus(Duration.ofMinutes(5)), "a");
        thing("b", T.minus(Duration.ofMinutes(5)), "b");
        UUID id = countRule(1, "");
        TriggerRuleEntity entity = rules.load(id);
        WindowSpec window = WindowSpec.fromStored(entity.getWindowSpec());

        WindowMembership.Membership capped = membership.resolve(specs.compile(entity), window,
                T.minus(Duration.ofMinutes(30)), T, changeBus.lastPublishedSeq(), true, T, 1);
        assertThat(capped.capped()).isTrue();
        assertThat(capped.counts()).isEmpty();

        WindowMembership.Membership full = membership.resolve(specs.compile(entity), window,
                T.minus(Duration.ofMinutes(30)), T, changeBus.lastPublishedSeq(), true, T, 100);
        assertThat(full.capped()).isFalse();
        assertThat(full.counts()).containsEntry("*", 2);
        assertThat(full.samples().get("*")).allSatisfy(member -> assertThat(member.seq()).isPositive());
    }
}
