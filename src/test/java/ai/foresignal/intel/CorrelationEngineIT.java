package ai.foresignal.intel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * The correlation engine end to end (prompt 101), seeded only through {@code TestRecords}: a recipe
 * forms an event with pinned members and a detector; it is {@code emerging} until its activation
 * threshold and {@code active} once a second family joins from a later chunk (look-back across chunk
 * boundaries); a seed is never counted twice and a member without geometry never joins; a kind
 * contradiction lowers confidence and adds an alternative, while a {@code healthOf} source that is not
 * running never contradicts; a quiet period resolves and new evidence reopens; a rewound cursor keeps
 * membership exactly-once; and a private member marks the event private.
 */
@WebIntegrationTest
@Import(TestIngestSources.class)
class CorrelationEngineIT {

    private static final String STANDARD = TestIngestSources.STANDARD;
    private static final Instant T0 = Instant.parse("2026-03-01T12:00:00Z");

    /**
     * A second source declaring the join and contradiction kinds: ingest rejects a kind its source does not declare
     * ({@code UNKNOWN_KIND}), and {@value #STANDARD} declares only {@code test-record}.
     */
    private static final String SECONDARY = "it-correlation-secondary";

    @Autowired CorrelationEngine engine;
    @Autowired CorrelationRecipeRepository recipes;
    @Autowired IntelligenceEventRepository events;
    @Autowired IntelEventMemberRepository members;
    @Autowired IntelEventLogRepository logs;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired SourceVisibility sources;
    @Autowired SourceRegistry registry;

    @BeforeEach
    void registerSecondary() {
        if (registry.contains(SECONDARY)) {
            registry.unregisterDynamic(SECONDARY);
        }
        registry.registerDynamic(SourceDescriptor.builder(SECONDARY).name("Correlation test secondary")
                .kinds("other", "jam").mode(Mode.PUSH).defaultBudget(Budget.of(60, 10_000)).build());
    }

    @AfterEach
    void restore() {
        sources.setOperatorPrivate(STANDARD, false);
        sources.refresh();
        if (registry.contains(SECONDARY)) {
            registry.unregisterDynamic(SECONDARY);
        }
    }

    private Map<String, Object> base() {
        Map<String, Object> definition = new LinkedHashMap<>();
        definition.put("type", "custom");
        definition.put("seed", Map.of("kinds", List.of("test-record")));
        definition.put("joins", List.of());
        definition.put("minMembers", 1);
        definition.put("minSignalFamilies", 1);
        definition.put("activateAt", Map.of("members", 1));
        definition.put("quietPeriod", "PT6H");
        definition.put("dedup", Map.of("radiusKm", 100, "window", "PT6H"));
        return definition;
    }

    private void save(Map<String, Object> definition) {
        CorrelationRecipeEntity entity = new CorrelationRecipeEntity("test-recipe", 1, definition);
        entity.setEnabled(true);
        recipes.save(entity);
    }

    private void ingest(String externalId, String kind, Instant at, double lon, double lat) {
        clock.setInstant(at);
        String source = TestIngestSources.STANDARD_KIND.equals(kind) ? STANDARD : SECONDARY;
        records.ingestAt(source, at, NormalizedRecord.builder(externalId, kind).observedAt(at).point(lon, lat)
                .put("name", externalId).build());
    }

    private IntelligenceEventEntity only() {
        List<IntelligenceEventEntity> all = events.findAll();
        assertThat(all).hasSize(1);
        return all.get(0);
    }

    @Test
    @DisplayName("a recipe forms an event with pinned members and a detector")
    void forms() {
        save(base());
        ingest("t1", "test-record", T0, -95.0, 32.0);
        ingest("t2", "test-record", T0.plusSeconds(60), -95.01, 32.01);
        engine.catchUp();

        IntelligenceEventEntity event = only();
        assertThat(event.getDetector()).containsEntry("name", "event-scorer").containsKey("params");
        assertThat(event.getConfidence()).isNotNull();
        List<IntelEventMemberEntity> pinned = members.findByEventIdOrderByIdAsc(event.getId());
        assertThat(pinned).hasSize(2);
        assertThat(pinned.get(0).getJoinReason()).containsKey("matchedKeys");
    }

    @Test
    @DisplayName("emerging until the activation threshold, active when a second family joins from a later chunk")
    void emergingThenActive() {
        Map<String, Object> definition = base();
        definition.put("joins", List.of(Map.of("kinds", List.of("other"), "withinKm", 50, "before", "PT6H",
                "after", "PT6H", "role", "supporting", "signalFamily", SignalFamilies.NAV_WARNING, "weight", 0.6)));
        definition.put("activateAt", Map.of("members", 2));
        definition.put("quietPeriod", "PT48H");
        save(definition);

        ingest("t1", "test-record", T0, -95.0, 32.0);
        engine.catchUp();
        assertThat(only().getStatus()).isEqualTo("emerging");

        // A join-kind row far away never joins; neither does one outside the join window.
        ingest("far", "other", T0.plusSeconds(120), -80.0, 40.0);
        ingest("late", "other", T0.plus(Duration.ofHours(9)), -95.0, 32.0);
        engine.catchUp();
        assertThat(only().getStatus()).isEqualTo("emerging");

        ingest("o1", "other", T0.plus(Duration.ofHours(1)), -95.05, 32.02);
        engine.catchUp();
        IntelligenceEventEntity event = only();
        assertThat(event.getStatus()).isEqualTo("active");
        List<IntelEventMemberEntity> pinned = members.findByEventIdOrderByIdAsc(event.getId());
        assertThat(pinned).extracting(IntelEventMemberEntity::getExternalId).containsExactly("t1", "o1");
        assertThat(pinned.get(1).getJoinReason()).containsKeys("distanceKm", "dtMinutes");
    }

    @Test
    @DisplayName("a seed is never counted again as a join, so one seed cannot satisfy two families")
    void noDoubleCounting() {
        Map<String, Object> definition = base();
        definition.put("joins", List.of(Map.of("kinds", List.of("test-record"), "withinKm", 50, "role", "supporting",
                "signalFamily", SignalFamilies.NAV_WARNING, "weight", 0.6)));
        definition.put("minSignalFamilies", 2);
        save(definition);

        ingest("t1", "test-record", T0, -95.0, 32.0);
        engine.catchUp();

        assertThat(events.findAll()).isEmpty();
    }

    @Test
    @DisplayName("a kind contradiction lowers confidence and adds an alternative")
    void contradiction() {
        Map<String, Object> definition = base();
        definition.put("contradictions", List.of(Map.of("kinds", List.of("jam"), "withinKm", 50, "window", "PT6H",
                "alternative", "GNSS interference")));
        save(definition);

        ingest("t1", "test-record", T0, -95.0, 32.0);
        ingest("j1", "jam", T0.plusSeconds(300), -95.02, 32.01);
        engine.catchUp();

        IntelligenceEventEntity event = only();
        double uncontradicted = RecipeRegistry.SEED_WEIGHT * EventScorer.DEFAULT_RELIABILITY;
        assertThat(event.getConfidence()).isCloseTo(uncontradicted * (1 - EventScorer.CONTRADICTION_PENALTY),
                within(1e-9));
        assertThat(event.getAlternatives()).anySatisfy(a -> assertThat(a).containsEntry("hypothesis",
                "GNSS interference"));
        assertThat(members.findByEventIdOrderByIdAsc(event.getId()))
                .anySatisfy(m -> assertThat(m.getRole()).isEqualTo("contradicting"));
    }

    @Test
    @DisplayName("a healthOf source that is not running is neither a contradiction nor a coverage gap")
    void missingSourceNeverContradicts() {
        Map<String, Object> definition = base();
        definition.put("contradictions", List.of(Map.of("healthOf", List.of("no-such-ais-feed"),
                "alternative", "AIS receiver coverage gap")));
        save(definition);

        ingest("t1", "test-record", T0, -95.0, 32.0);
        engine.catchUp();

        IntelligenceEventEntity event = only();
        assertThat(event.getConfidence()).isCloseTo(RecipeRegistry.SEED_WEIGHT * EventScorer.DEFAULT_RELIABILITY,
                within(1e-9));
        assertThat(event.getAlternatives()).isEmpty();
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) event.getDetector().get("params");
        assertThat(params.get("contradictionInputs").toString()).contains("no-such-ais-feed=not-running");
    }

    @Test
    @DisplayName("a quiet period resolves the event and new evidence within the reopen window reopens it")
    void resolveAndReopen() {
        Map<String, Object> definition = base();
        definition.put("quietPeriod", "PT1H");
        definition.put("reopenWindow", "PT72H");
        save(definition);

        ingest("t1", "test-record", T0, -95.0, 32.0);
        engine.catchUp();
        String id = only().getId();

        clock.setInstant(T0.plus(Duration.ofHours(2)));
        engine.catchUp();
        assertThat(only().getStatus()).isEqualTo("resolved");

        ingest("t2", "test-record", T0.plus(Duration.ofHours(2)), -95.01, 32.01);
        engine.catchUp();
        IntelligenceEventEntity event = only();
        assertThat(event.getId()).isEqualTo(id);
        assertThat(event.getStatus()).isEqualTo("active");
        assertThat(event.getEndTime()).isNull();
        assertThat(logs.findByEventIdOrderByIdAsc(id)).extracting(IntelEventLogEntity::getAction)
                .contains("status", "reopened");
    }

    @Test
    @DisplayName("restarting from an earlier cursor keeps membership exactly-once")
    void cursorRestart() {
        save(base());
        ingest("t1", "test-record", T0, -95.0, 32.0);
        ingest("t2", "test-record", T0.plusSeconds(60), -95.01, 32.01);
        engine.catchUp();
        IntelligenceEventEntity before = only();
        int version = before.getVersion();

        engine.rewind(0);
        engine.catchUp();

        IntelligenceEventEntity after = only();
        assertThat(after.getId()).isEqualTo(before.getId());
        assertThat(after.getVersion()).isEqualTo(version);
        assertThat(members.findByEventIdOrderByIdAsc(after.getId())).hasSize(2);
    }

    @Test
    @DisplayName("a member from a private source marks the event private")
    void privateMember() {
        sources.setOperatorPrivate(STANDARD, true);
        sources.refresh();
        save(base());
        ingest("t1", "test-record", T0, -95.0, 32.0);
        engine.catchUp();

        assertThat(only().isPrivateEvent()).isTrue();
    }

    @Test
    @DisplayName("a stop does not wait for a catch-up pass in flight")
    void stopDoesNotWaitForAPass() {
        try {
            // A pass holds the monitor for its whole run; shutdown waited behind it for a catch-up of the history.
            synchronized (engine) {
                assertThat(CompletableFuture.runAsync(engine::destroy)).succeedsWithin(Duration.ofSeconds(5));
            }
            assertThat(engine.running()).isFalse();
        } finally {
            engine.start();
        }
        assertThat(engine.running()).isTrue();
    }
}
