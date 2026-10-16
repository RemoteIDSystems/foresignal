package ai.foresignal.trigger.window;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.SystemEvent;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.CoverageSupport;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import tools.jackson.databind.json.JsonMapper;

/**
 * The absence rule and its coverage gate (prompt 100): a healthy source that goes quiet fires; a failing
 * source is {@code unknown} with one system event and no alert; a 304 streak with steady coverage still
 * counts as covered; warm-up holds the rule until it and its source have existed for one full window; a
 * kind-only rule with no enabled source is never covered; and {@code active → unknown} leaves the alert
 * firing. Time is the frozen {@link MutableClock}; {@link CoverageSupport} writes the health and
 * {@code fetch_log} state the gate reads.
 */
@WebIntegrationTest
@RecordApplicationEvents
@Import({TestIngestSources.class, CoverageSupport.Config.class})
class AbsenceRuleIT {

    private static final Instant NOW = Instant.parse("2026-03-01T12:00:00Z");
    private static final String SOURCE = TestIngestSources.STANDARD;
    private static final Duration WINDOW = Duration.ofHours(2);

    @Autowired TriggerRuleService rules;
    @Autowired WindowEngine engine;
    @Autowired SourceService sources;
    @Autowired TestRecords records;
    @Autowired CoverageSupport coverage;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper json;
    @Autowired ApplicationEvents events;

    private void enable(boolean enabled) {
        sources.update(SOURCE, new SourceSettingsUpdate(enabled, null, Map.of(), Budget.of(1, 1), null, false, null, false));
    }

    @BeforeEach
    void freeze() {
        clock.setInstant(NOW);
        enable(true);
    }

    @AfterEach
    void restore() {
        enable(true);
    }

    /** An absence rule created {@code age} before {@link #NOW} (the warm-up needs one full window). */
    private UUID absenceRule(Duration age, String scope) {
        clock.setInstant(NOW.minus(age));
        UUID id = rules.create(json.readTree("{\"name\":\"quiet\",\"type\":\"absence\"," + scope
                + ",\"window\":{\"window\":\"2h\"}}")).rule().getId();
        clock.setInstant(NOW);
        return id;
    }

    private UUID absenceRule() {
        return absenceRule(Duration.ofHours(3), "\"sources\":[\"" + SOURCE + "\"]");
    }

    /** A source that has run for hours, fetched successfully every 10 minutes through the window. */
    private void runningAndCovered() {
        coverage.fetch(SOURCE, "OK", NOW.minus(Duration.ofHours(10)));
        coverage.markCovered(SOURCE, NOW.minus(WINDOW), NOW);
        for (Instant at = NOW.minus(WINDOW).plusSeconds(600); at.isBefore(NOW); at = at.plusSeconds(600)) {
            coverage.fetch(SOURCE, "OK", at);
        }
    }

    private int alerts(UUID rule) {
        Integer n = jdbc.queryForObject("select count(*) from alert where rule_id = ?", Integer.class, rule);
        return n == null ? 0 : n;
    }

    private long unknownEvents() {
        return events.stream(SystemEvent.class).filter(e -> WindowEngine.UNKNOWN_SUBTYPE.equals(e.subtype())).count();
    }

    @Test
    @DisplayName("a healthy source that goes quiet fires (once the active version is persisted)")
    void healthyQuietSourceFires() {
        runningAndCovered();
        UUID id = absenceRule();
        WindowEmitters.Ingesting emitter = new WindowEmitters.Ingesting(records, clock);

        engine.evaluate(NOW, emitter);
        assertThat(emitter.lastState(id + ":*", "state")).isEqualTo("active");

        clock.advance(Duration.ofSeconds(10));
        engine.evaluate(clock.instant(), emitter);
        assertThat(alerts(id)).isEqualTo(1);
    }

    @Test
    @DisplayName("a record inside the window keeps the rule clear")
    void recordInWindowIsClear() {
        runningAndCovered();
        records.ingestAt(SOURCE, NOW.minus(Duration.ofMinutes(20)), NormalizedRecord.builder("a",
                TestIngestSources.STANDARD_KIND).observedAt(NOW.minus(Duration.ofMinutes(20))).put("name", "a").build());
        UUID id = absenceRule();
        WindowEmitters.Capture capture = new WindowEmitters.Capture();
        engine.evaluate(NOW, capture);
        assertThat(capture.lastState(id + ":*", "state")).isEqualTo("clear");
    }

    @Test
    @DisplayName("a failing source is unknown with one system event per transition and no alert")
    void failingSourceIsUnknown() {
        runningAndCovered();
        coverage.health(SOURCE, NOW.minusSeconds(60), NOW.minusSeconds(60), 3);
        UUID id = absenceRule();
        long before = unknownEvents();

        WindowEmitters.Capture capture = new WindowEmitters.Capture();
        engine.evaluate(NOW, capture);
        assertThat(capture.lastState(id + ":*", "state")).isEqualTo("unknown");
        assertThat((String) capture.lastState(id + ":*", "reason")).contains("failing");

        clock.advance(Duration.ofMinutes(6));
        engine.evaluate(clock.instant(), capture);
        assertThat(capture.lastState(id + ":*", "state")).isEqualTo("unknown");

        assertThat(unknownEvents() - before).as("one event for the one transition into unknown").isEqualTo(1);
        assertThat(alerts(id)).isZero();
    }

    @Test
    @DisplayName("a 304 streak with steady coverage still counts as covered")
    void notModifiedStreakIsCovered() {
        coverage.fetch(SOURCE, "OK", NOW.minus(Duration.ofHours(10)));
        // The last data came ten hours ago; since then every fetch answered 304 (last success just now).
        coverage.health(SOURCE, NOW.minus(Duration.ofHours(10)), NOW.minusSeconds(30), 0);
        for (Instant at = NOW.minus(WINDOW); at.isBefore(NOW); at = at.plusSeconds(600)) {
            coverage.fetch(SOURCE, "NOT_MODIFIED", at);
        }
        UUID id = absenceRule();
        WindowEmitters.Capture capture = new WindowEmitters.Capture();
        engine.evaluate(NOW, capture);
        assertThat(capture.lastState(id + ":*", "state")).isEqualTo("active");
    }

    @Test
    @DisplayName("warm-up: a rule younger than one window is learning, never active")
    void ruleWarmUp() {
        runningAndCovered();
        UUID id = absenceRule(Duration.ofMinutes(30), "\"sources\":[\"" + SOURCE + "\"]");
        WindowEmitters.Capture capture = new WindowEmitters.Capture();
        engine.evaluate(NOW, capture);
        assertThat(capture.lastState(id + ":*", "state")).isEqualTo("learning");
        assertThat((String) capture.lastState(id + ":*", "reason")).contains("warm-up");
        assertThat(alerts(id)).isZero();
    }

    @Test
    @DisplayName("warm-up: a source that first fetched inside the window is learning")
    void sourceWarmUp() {
        coverage.markCovered(SOURCE, NOW.minus(Duration.ofMinutes(30)), NOW);
        UUID id = absenceRule();
        WindowEmitters.Capture capture = new WindowEmitters.Capture();
        engine.evaluate(NOW, capture);
        assertThat(capture.lastState(id + ":*", "state")).isEqualTo("learning");
        assertThat((String) capture.lastState(id + ":*", "reason")).contains("source " + SOURCE);
    }

    @Test
    @DisplayName("a kind-only rule whose kind no enabled source declares is never covered")
    void kindOnlyRuleWithoutSourcesIsUnknown() {
        UUID id = absenceRule(Duration.ofHours(3), "\"kinds\":[\"no-such-kind\"]");
        WindowEmitters.Capture capture = new WindowEmitters.Capture();
        engine.evaluate(NOW, capture);
        assertThat(capture.lastState(id + ":*", "state")).isEqualTo("unknown");
        assertThat((String) capture.lastState(id + ":*", "reason")).contains("no enabled source");
    }

    @Test
    @DisplayName("a covered source that goes quiet fires; losing coverage keeps the alert firing")
    void unknownKeepsAlertFiring() {
        runningAndCovered();
        UUID id = absenceRule();
        WindowEmitters.Ingesting emitter = new WindowEmitters.Ingesting(records, clock);
        engine.evaluate(NOW, emitter);
        clock.advance(Duration.ofSeconds(10));
        engine.evaluate(clock.instant(), emitter);
        assertThat(alerts(id)).isEqualTo(1);

        // Disabling the source makes the coverage gate unknown: active → unknown must not resolve.
        // Advance past the rule's step (window 2h → step 5m) so the evaluation is due.
        enable(false);
        clock.advance(Duration.ofMinutes(6));
        engine.evaluate(clock.instant(), emitter);
        assertThat(emitter.lastState(id + ":*", "state")).isEqualTo("unknown");
        assertThat(jdbc.queryForObject("select state from alert where rule_id = ?", String.class, id))
                .isEqualTo("firing");
    }
}
