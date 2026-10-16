package ai.foresignal.trigger.window;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.CoverageSupport;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.trigger.RuleSpec;
import ai.foresignal.trigger.RuleSpecs;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

/**
 * Invariant INT-2 (no look-ahead) for the prompt 100 window components, against the database: a past
 * bucket is judged only from what was known by its decision time. A version ingested after it, a live
 * row received after it, or an observation after the bucket end is not counted; the coverage gate for a
 * past bucket reads {@code fetch_log} history before the bucket end and never the current health; and
 * re-evaluating a past bucket later, after late data arrived, gives the same result (INT-1).
 */
@WebIntegrationTest
@Import({TestIngestSources.class, CoverageSupport.Config.class})
class LookAheadTest {

    private static final Instant BUCKET = Instant.parse("2026-03-01T10:00:00Z");
    private static final Instant BUCKET_END = BUCKET.plus(Duration.ofHours(1));
    private static final Instant DECISION = BUCKET_END.plus(WindowEngine.SETTLE);
    private static final String SOURCE = TestIngestSources.STANDARD;

    @Autowired TriggerRuleService rules;
    @Autowired RuleSpecs specs;
    @Autowired WindowMembership membership;
    @Autowired CoverageGate gate;
    @Autowired WindowTester tester;
    @Autowired ChangeBus changeBus;
    @Autowired SourceService sources;
    @Autowired TestRecords records;
    @Autowired CoverageSupport coverage;
    @Autowired MutableClock clock;
    @Autowired JsonMapper json;

    private TriggerRuleEntity rule;

    @BeforeEach
    void setUp() {
        clock.setInstant(BUCKET);
        sources.update(SOURCE, new SourceSettingsUpdate(true, null, Map.of(), Budget.of(1, 1), null, false, null, false));
        UUID id = rules.create(json.readTree("{\"name\":\"look-ahead\",\"enabled\":false,\"type\":\"count\","
                + "\"sources\":[\"" + SOURCE + "\"],\"window\":{\"window\":\"1h\",\"op\":\">=\",\"threshold\":1}}"))
                .rule().getId();
        rule = rules.load(id);
    }

    /** Ingests one record observed at {@code observedAt}, with the application clock at {@code ingestedAt}. */
    private void seen(String id, Instant observedAt, Instant ingestedAt) {
        clock.setInstant(ingestedAt);
        records.ingestAt(SOURCE, ingestedAt, NormalizedRecord.builder(id, TestIngestSources.STANDARD_KIND)
                .observedAt(observedAt).put("name", id).build());
    }

    private int count(boolean includeLive, Instant knownBy) {
        RuleSpec spec = specs.compile(rule);
        WindowSpec window = WindowSpec.fromStored(rule.getWindowSpec());
        WindowMembership.Membership result = membership.resolve(spec, window, BUCKET, BUCKET_END,
                changeBus.lastPublishedSeq(), includeLive, knownBy, WindowMembership.ROW_CAP);
        return result.counts().getOrDefault("*", 0);
    }

    @Test
    @DisplayName("a version ingested after the decision time, or observed after the bucket, is not counted")
    void lateAndFutureDataAreNotCounted() {
        seen("on-time", BUCKET.plus(Duration.ofMinutes(20)), BUCKET.plus(Duration.ofMinutes(21)));
        seen("late", BUCKET.plus(Duration.ofMinutes(40)), BUCKET.plus(Duration.ofHours(3)));
        seen("after", BUCKET_END.plus(Duration.ofMinutes(10)), BUCKET_END.plus(Duration.ofMinutes(11)));
        clock.setInstant(BUCKET.plus(Duration.ofHours(4)));

        assertThat(count(false, DECISION)).as("versions known by the decision time").isEqualTo(1);
        assertThat(count(true, DECISION)).as("live rows received after it are not read either").isEqualTo(1);
        assertThat(count(false, BUCKET.plus(Duration.ofHours(4))))
                .as("known later: the late arrival counts, the observation after the bucket never does")
                .isEqualTo(2);
    }

    @Test
    @DisplayName("the gate for a past bucket reads fetch_log before its end, never today's health")
    void gateReadsHistoryNotCurrentHealth() {
        coverage.fetch(SOURCE, "OK", BUCKET.minus(Duration.ofDays(1)));
        for (Instant at = BUCKET; at.isBefore(BUCKET_END); at = at.plus(Duration.ofMinutes(10))) {
            coverage.fetch(SOURCE, "OK", at);
        }
        // After the bucket the source broke: five failed fetches, and its current health is failing.
        for (int i = 1; i <= 5; i++) {
            coverage.fetch(SOURCE, "FAILED", BUCKET_END.plus(Duration.ofMinutes(10L * i)));
        }
        Instant now = BUCKET.plus(Duration.ofHours(3));
        coverage.health(SOURCE, BUCKET.plus(Duration.ofMinutes(50)), BUCKET.plus(Duration.ofMinutes(50)), 5);
        clock.setInstant(now);

        CoverageGate.Gate past = gate.evaluateAsOf(Set.of(SOURCE), Set.of(), BUCKET, BUCKET_END, Duration.ofHours(1),
                0.8);
        assertThat(past.ok()).as(String.valueOf(past.reason())).isTrue();

        CoverageGate.Gate current = gate.evaluate(Set.of(SOURCE), Set.of(), now.minus(Duration.ofHours(1)), now,
                Duration.ofHours(1), 0.8);
        assertThat(current.ok()).isFalse();
        assertThat(current.reason()).contains("failing");
    }

    @Test
    @DisplayName("a past bucket judged right after it settled and judged again after late data agree")
    void reEvaluationAfterLateDataAgrees() {
        seen("on-time", BUCKET.plus(Duration.ofMinutes(20)), BUCKET.plus(Duration.ofMinutes(21)));
        clock.setInstant(DECISION.plusSeconds(1));
        WindowTester.Result first = tester.test(rule, BUCKET, BUCKET_END, null, null);

        seen("late", BUCKET.plus(Duration.ofMinutes(40)), BUCKET.plus(Duration.ofHours(3)));
        clock.setInstant(BUCKET.plus(Duration.ofHours(4)));
        WindowTester.Result later = tester.test(rule, BUCKET, BUCKET_END, null, null);

        assertThat(later.evaluations()).isEqualTo(first.evaluations());
        assertThat(first.evaluations()).isNotEmpty();
    }
}
