package ai.foresignal.derive;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import ai.foresignal.collect.FetchContextFactory;
import ai.foresignal.collect.HealthService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.ParamValidator;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeSummary;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.raster.RasterApiSupport;
import ai.foresignal.testsupport.raster.RasterTestSources;
import ai.foresignal.testsupport.raster.TestDerivedGenerator;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The DERIVED runtime end to end (prompt 052) with the test-only {@code test-derived} source: frame and change
 * inputs, frame coalescing, inbox overflow → reconcile, one {@code fetch_log} row ({@code principal=derived}) and
 * a health update per invocation, failures recorded like failed fetches, {@code ctx.http()} refused, a TTL
 * rejected by the registry, and cycle detection. Joins the raster test context.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class DerivedRunnerIT extends RasterApiSupport {

    static final String ID = TestDerivedGenerator.ID;
    static final Duration WAIT = Duration.ofSeconds(20);

    @Autowired DerivedRunner runner;
    @Autowired TestDerivedGenerator generator;
    @Autowired SourceService sources;
    @Autowired SourceRegistry registry;
    @Autowired FetchContextFactory contexts;
    @Autowired HealthService health;
    @Autowired TestRecords records;
    @Autowired JdbcTemplate jdbc;
    @Autowired ParamValidator params;

    private void enable(boolean enabled) {
        sources.update(ID, new SourceSettingsUpdate(enabled, null, Map.of(), Budget.of(1, 1), null, false, null, false));
    }

    private int fetchRows() {
        Integer n = jdbc.queryForObject("select count(*) from fetch_log where source_id = ? and principal = 'derived'",
                Integer.class, ID);
        return n == null ? 0 : n;
    }

    private int versions() {
        Integer n = jdbc.queryForObject("select count(*) from record_version where source_id = ?", Integer.class, ID);
        return n == null ? 0 : n;
    }

    /** Derived fetch_log rows of earlier tests (rows accumulate; the runner's counters restart on enable). */
    int baseRows;

    @BeforeEach
    void start() {
        generator.reset();
        baseRows = fetchRows();
        enable(true);
        await().atMost(WAIT).until(() -> runner.running().contains(ID));
        assertThat(runner.awaitIdle(ID, WAIT)).isTrue();
    }

    @AfterEach
    void stop() {
        generator.open();
        enable(false);
        await().atMost(WAIT).until(() -> !runner.running().contains(ID));
    }

    @Test
    @DisplayName("enabling runs reconcile first; every invocation leaves one derived fetch_log row and a health success")
    void reconcileAndFetchLog() {
        assertThat(generator.reconciles.get()).isEqualTo(1);
        long invocations = runner.invocations(ID);
        assertThat(invocations).isPositive();
        assertThat(fetchRows() - baseRows).isEqualTo((int) invocations);
        assertThat(versions()).isPositive();
        assertThat(health.health(ID).lastSuccessAt()).isNotNull();
        assertThat(health.health(ID).consecutiveFailures()).isZero();
    }

    @Test
    @DisplayName("frame events of the subscribed product arrive with their valid time; others do not")
    void frameInput() throws Exception {
        commitObservation(T, 0);
        assertThat(runner.awaitIdle(ID, WAIT)).isTrue();
        await().atMost(WAIT).until(() -> generator.frames.size() == 1);
        RasterFrameEvent event = generator.frames.get(0);
        assertThat(event.productId()).isEqualTo(OBS);
        assertThat(event.validTime()).isEqualTo(T);
        assertThat(event.observedAt()).isEqualTo(T);
        assertThat(event.complete()).isTrue();
        commitRun(T, 2);
        assertThat(runner.awaitIdle(ID, WAIT)).isTrue();
        assertThat(generator.frames).hasSize(1);
        assertThat(fetchRows() - baseRows).isEqualTo((int) runner.invocations(ID));
    }

    @Test
    @DisplayName("frame events are coalesced per product while the source is busy")
    void coalescing() throws Exception {
        generator.hold();
        commitObservation(T, 0);
        assertThat(generator.awaitHeld(20)).isTrue();
        commitObservation(T.plusSeconds(60), 1);
        commitObservation(T.plusSeconds(120), 2);
        generator.open();
        assertThat(runner.awaitIdle(ID, WAIT)).isTrue();
        assertThat(generator.frames).extracting(RasterFrameEvent::validTime)
                .containsExactly(T, T.plusSeconds(120));
    }

    @Test
    @DisplayName("change batches of the subscribed source arrive; the runner's own changes never do")
    void changeInput() {
        clock.setInstant(T.plusSeconds(120));
        records.ingest(TestIngestSources.STANDARD, NormalizedRecord.builder("r1", TestIngestSources.STANDARD_KIND)
                .observedAt(T).put("v", 1).build(), NormalizedRecord.builder("r2", TestIngestSources.STANDARD_KIND)
                .observedAt(T).put("v", 2).build());
        await().atMost(WAIT).until(() -> generator.changes.get() == 2);
        assertThat(runner.awaitIdle(ID, WAIT)).isTrue();
        int batches = generator.batches.get();
        // Its own counter records went through the writer and the bus, and came back to nobody.
        assertThat(versions()).isGreaterThanOrEqualTo(2);
        assertThat(generator.batches.get()).isEqualTo(batches);
    }

    @Test
    @DisplayName("an overflowing inbox drops the batches and schedules a reconcile")
    void overflow() throws Exception {
        generator.hold();
        commitObservation(T, 0);
        assertThat(generator.awaitHeld(20)).isTrue();
        int reconciles = generator.reconciles.get();
        ChangeSummary change = new ChangeSummary(1, null, ai.foresignal.source.spi.HistoryTier.STANDARD,
                TestIngestSources.STANDARD, "x", TestIngestSources.STANDARD_KIND, 1, null, T, null, null, false, 0, null,
                null, null);
        for (int i = 0; i < DerivedProperties.defaults().inboxCapacity() + 5; i++) {
            runner.onBatch(new ChangeBatch(TestIngestSources.STANDARD, i, T, i + 1, i + 1, List.of(change)));
        }
        assertThat(runner.overflows(ID)).isPositive();
        generator.open();
        assertThat(runner.awaitIdle(ID, WAIT)).isTrue();
        assertThat(generator.reconciles.get()).isEqualTo(reconciles + 1);
        assertThat(generator.changes.get()).as("the dropped batches never arrive").isLessThan(5);
    }

    @Test
    @DisplayName("a change-bus gap schedules a reconcile")
    void gap() {
        int reconciles = generator.reconciles.get();
        runner.onGap(123);
        await().atMost(WAIT).until(() -> generator.reconciles.get() == reconciles + 1);
    }

    @Test
    @DisplayName("a failing hook leaves a FAILED fetch_log row and a health failure, like a failed fetch")
    void failure() {
        Integer before = jdbc.queryForObject("select count(*) from fetch_log where source_id = ? and status = 'FAILED'",
                Integer.class, ID);
        generator.failNext();
        runner.reconcileNow(ID);
        assertThat(runner.awaitIdle(ID, WAIT)).isTrue();
        Integer failed = jdbc.queryForObject("select count(*) from fetch_log where source_id = ? and status = 'FAILED'",
                Integer.class, ID);
        assertThat(failed - before).isEqualTo(1);
        assertThat(health.health(ID).consecutiveFailures()).isEqualTo(1);
        runner.reconcileNow(ID);
        assertThat(runner.awaitIdle(ID, WAIT)).isTrue();
        assertThat(health.health(ID).consecutiveFailures()).isZero();
    }

    @Test
    @DisplayName("a derived source has no HTTP client, and the registry refuses a derived descriptor with a TTL")
    void noHttpNoTtl() {
        SourceDescriptor descriptor = registry.descriptor(ID);
        assertThatThrownBy(() -> contexts.forFetch(descriptor, null).http())
                .isInstanceOf(UnsupportedOperationException.class).hasMessageContaining("DERIVED");
        SourceDescriptor withTtl = SourceDescriptor.builder("bad-derived").name("bad").kind("x").licence("test")
                .mode(Mode.DERIVED).defaultBudget(Budget.of(1, 1)).recordTtl(Duration.ofMinutes(5)).build();
        assertThat(DescriptorValidator.problems(withTtl, generator)).anyMatch(p -> p.contains("recordTtl"));
    }

    @Test
    @DisplayName("a cycle of derived inputs fails startup")
    void cycleFailsStartup() {
        DerivedGenerator a = generator("cyc-a", "cyc-kind-a", Set.of("cyc-kind-b"));
        DerivedGenerator b = generator("cyc-b", "cyc-kind-b", Set.of("cyc-kind-a"));
        DerivedGraph graph = new DerivedGraph(List.of(a, b), params);
        assertThatThrownBy(graph::afterSingletonsInstantiated).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cycle").hasMessageContaining("cyc-a").hasMessageContaining("cyc-b");
        new DerivedGraph(List.of(a), params).afterSingletonsInstantiated();
    }

    private static DerivedGenerator generator(String id, String kind, Set<String> inputKinds) {
        SourceDescriptor d = SourceDescriptor.builder(id).name(id).kind(kind).licence("test").mode(Mode.DERIVED)
                .defaultBudget(Budget.of(1, 1)).build();
        return new DerivedGenerator() {
            @Override
            public SourceDescriptor descriptor() {
                return d;
            }

            @Override
            public DerivedInputs inputs(ValidatedParams p) {
                return new DerivedInputs(Set.of(), inputKinds, Set.of(), null, false);
            }

            @Override
            public void reconcile(List<HeadSnapshot> ownHeads, DerivedContext ctx) {}
        };
    }

    @Test
    @DisplayName("ticks reach a source only when its inputs ask for them (this one has none)")
    void noTicks() {
        clock.advance(Duration.ofMinutes(5));
        runner.tickDue();
        assertThat(runner.awaitIdle(ID, WAIT)).isTrue();
        assertThat(generator.ticks.get()).isZero();
        runner.tickNow(ID);
        assertThat(runner.awaitIdle(ID, WAIT)).isTrue();
        assertThat(generator.ticks.get()).isEqualTo(1);
    }
}
