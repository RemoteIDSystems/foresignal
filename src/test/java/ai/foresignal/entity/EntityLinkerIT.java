package ai.foresignal.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * The entity linker end to end (prompt 098): a provisional merge, an MMSI re-assignment, a tombstone
 * close, a backfill from heads, the callsign-gap closure and the private-source exclusion.
 */
@IntegrationTest
@Import(TestIngestSources.class)
class EntityLinkerIT {

    private static final String VESSEL = "it-entity-vessel";
    private static final String AIRCRAFT = "it-entity-aircraft";
    private static final String PRIVATE = "it-entity-private";

    @Autowired
    private SourceRegistry registry;

    @Autowired
    private IngestQueue queue;

    @Autowired
    private MutableClock clock;

    @Autowired
    private EntityLinker linker;

    @Autowired
    private EntityLinkRepository links;

    @Autowired
    private EntityKeyRepository keys;

    @Autowired
    private EntityRepository entities;

    @AfterEach
    void unregister() {
        for (String id : new String[] {VESSEL, AIRCRAFT, PRIVATE}) {
            if (registry.contains(id)) {
                registry.unregisterDynamic(id);
            }
        }
    }

    @Test
    void mmsiThenMmsiAndImoMergesTheProvisionalIntoTheAnchor() {
        registerVessel();
        TestRecords records = new TestRecords(queue, clock);
        clock.setInstant(Instant.parse("2026-10-15T00:00:00Z"));

        records.ingest(VESSEL, vessel("v1", map("mmsi", "366123456")));
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(links.findOpen(VESSEL, "v1")).isPresent());
        String provisional = links.findOpen(VESSEL, "v1").orElseThrow().getEntityId();

        records.ingest(VESSEL, vessel("v2", map("mmsi", "366123456", "imo", "9074729")));
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(links.findOpen(VESSEL, "v2")).isPresent());
        String anchored = links.findOpen(VESSEL, "v2").orElseThrow().getEntityId();

        assertThat(anchored).isNotEqualTo(provisional);
        assertThat(entities.findById(provisional).orElseThrow().getStatus()).isEqualTo("MERGED");
        assertThat(links.findOpen(VESSEL, "v1").orElseThrow().getEntityId()).isEqualTo(anchored);
    }

    @Test
    void anMmsiMovingToANewImoIsReassigned() {
        registerVessel();
        TestRecords records = new TestRecords(queue, clock);
        clock.setInstant(Instant.parse("2026-10-15T00:00:00Z"));

        records.ingest(VESSEL, vessel("a", map("mmsi", "366123456", "imo", "9074729")));
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(links.findOpen(VESSEL, "a")).isPresent());
        String first = links.findOpen(VESSEL, "a").orElseThrow().getEntityId();

        clock.advance(Duration.ofMinutes(1));
        records.ingest(VESSEL, vessel("b", map("mmsi", "366123456", "imo", "9161235")));
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(links.findOpen(VESSEL, "b")).isPresent());
        String second = links.findOpen(VESSEL, "b").orElseThrow().getEntityId();

        assertThat(second).isNotEqualTo(first);
        assertThat(keys.findOpen("mmsi", "366123456").orElseThrow().getEntityId()).isEqualTo(second);
    }

    @Test
    void aTombstoneClosesTheLink() {
        registerVessel();
        TestRecords records = new TestRecords(queue, clock);
        clock.setInstant(Instant.parse("2026-10-15T00:00:00Z"));

        records.ingest(VESSEL, vessel("t1", map("mmsi", "366123456")));
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(links.findOpen(VESSEL, "t1")).isPresent());

        clock.advance(Duration.ofMinutes(1));
        records.ingest(VESSEL, NormalizedRecord.tombstone("t1", "vessel", clock.instant()));
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(links.findOpen(VESSEL, "t1")).isEmpty());
    }

    @Test
    void backfillsLinksFromTheCurrentHeads() {
        registerVessel();
        TestRecords records = new TestRecords(queue, clock);
        clock.setInstant(Instant.parse("2026-10-15T00:00:00Z"));

        records.ingest(VESSEL, vessel("h1", map("mmsi", "366123456", "imo", "9074729")));
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(links.findOpen(VESSEL, "h1")).isPresent());

        // A cold start sees no entity and must rebuild from the heads.
        links.deleteAll();
        keys.deleteAll();
        entities.deleteAll();

        linker.backfillHeads();
        assertThat(links.findOpen(VESSEL, "h1")).isPresent();
    }

    @Test
    void closesAnAirCallsignLinkPastTheGap() {
        register(
                AIRCRAFT,
                "aircraft",
                source(AIRCRAFT, "aircraft").entityKey("icao24", "hex").entityKey("callsign", "flight")
                        .entityKeyPrefix("callsign", "air"));
        TestRecords records = new TestRecords(queue, clock);
        clock.setInstant(Instant.parse("2026-10-15T00:00:00Z"));

        records.ingest(AIRCRAFT, NormalizedRecord.builder("f1", "aircraft").observedAt(clock.instant())
                .put("hex", "ae1234").put("flight", "UAL123").point(0, 0).build());
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(links.findOpen(AIRCRAFT, "f1")).isPresent());
        assertThat(links.findOpen(AIRCRAFT, "f1").orElseThrow().getKeyType()).isEqualTo("callsign");

        // The record is not observed again; the clock moves past the gap.
        clock.advance(Duration.ofHours(7));
        linker.closeStaleCallsignLinks();
        assertThat(links.findOpen(AIRCRAFT, "f1")).isEmpty();
    }

    @Test
    void aPrivateSourceCreatesNoLink() {
        register(PRIVATE, "vessel", source(PRIVATE, "vessel").privateSource(true)
                .entityKey("mmsi", "mmsi").entityKey("imo", "imo"));
        TestRecords records = new TestRecords(queue, clock);
        clock.setInstant(Instant.parse("2026-10-15T00:00:00Z"));

        records.ingest(PRIVATE, vessel("p1", map("mmsi", "366123456", "imo", "9074729")));
        await().during(Duration.ofMillis(500)).atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
            Optional<EntityLinkEntity> link = links.findOpen(PRIVATE, "p1");
            assertThat(link).isEmpty();
        });
    }

    private void registerVessel() {
        register(VESSEL, "vessel", source(VESSEL, "vessel").entityKey("mmsi", "mmsi").entityKey("imo", "imo"));
    }

    private void register(String id, String kind, SourceDescriptor.Builder builder) {
        if (registry.contains(id)) {
            registry.unregisterDynamic(id);
        }
        registry.registerDynamic(builder.build());
    }

    private static SourceDescriptor.Builder source(String id, String kind) {
        return SourceDescriptor.builder(id)
                .name("Entity linker test " + id)
                .kind(kind)
                .mode(Mode.PUSH)
                .defaultBudget(Budget.of(60, 10_000));
    }

    private static NormalizedRecord vessel(String id, java.util.Map<String, Object> payload) {
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, "vessel")
                .observedAt(Instant.parse("2026-10-15T00:00:00Z"));
        payload.forEach(b::put);
        return b.point(0, 0).build();
    }

    private static java.util.Map<String, Object> map(String... pairs) {
        java.util.Map<String, Object> out = new java.util.LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            out.put(pairs[i], pairs[i + 1]);
        }
        return out;
    }
}
