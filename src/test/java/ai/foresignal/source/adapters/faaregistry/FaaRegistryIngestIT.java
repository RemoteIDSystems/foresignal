package ai.foresignal.source.adapters.faaregistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.entity.EntityKeyRepository;
import ai.foresignal.entity.EntityLinkRepository;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Ingesting {@code faa-registry} snapshots (prompt 098): a changed {@code modeSHex} supersedes the old
 * {@code icao24} key, and a deregistered N-number is tombstoned and its link closed. Seeded through the
 * writer with {@link TestRecords} (the executor-driven complete-set reconciliation is covered by
 * {@code TombstoneAbsentIT}).
 */
@IntegrationTest
@Import(TestIngestSources.class)
class FaaRegistryIngestIT {

    private static final String ID = "it-faa-registry";
    private static final Instant NOW = Instant.parse("2026-10-15T00:00:00Z");

    @Autowired
    private SourceRegistry registry;

    @Autowired
    private IngestQueue queue;

    @Autowired
    private MutableClock clock;

    @Autowired
    private EntityKeyRepository keys;

    @Autowired
    private EntityLinkRepository links;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void unregister() {
        if (registry.contains(ID)) {
            registry.unregisterDynamic(ID);
        }
    }

    @Test
    void aChangedHexSupersedesTheOldIcao24Key() {
        register();
        TestRecords records = new TestRecords(queue, clock);
        clock.setInstant(NOW);

        records.ingest(ID, registration("N123AB", "ab1234"));
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(links.findOpen(ID, "N123AB")).isPresent());
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(keys.findOpen("icao24", "ab1234")).isPresent());

        clock.advance(Duration.ofDays(1));
        records.ingest(ID, registration("N123AB", "cd5678"));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(keys.findOpen("icao24", "cd5678")).isPresent());
        assertThat(keys.findOpen("icao24", "ab1234")).isEmpty();
    }

    @Test
    void aMissingNNumberIsTombstonedAndItsLinkClosed() {
        register();
        TestRecords records = new TestRecords(queue, clock);
        clock.setInstant(NOW);

        records.ingest(ID, registration("N999ZZ", "ef9999"));
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(links.findOpen(ID, "N999ZZ")).isPresent());

        clock.advance(Duration.ofDays(1));
        records.ingest(ID, NormalizedRecord.tombstone("N999ZZ", FaaRegistryParser.KIND, clock.instant()));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(links.findOpen(ID, "N999ZZ")).isEmpty());
        Boolean tombstoned = jdbc.queryForObject(
                "select tombstone from record_head where source_id = ? and external_id = ?", Boolean.class, ID, "N999ZZ");
        assertThat(tombstoned).isTrue();
    }

    private void register() {
        if (registry.contains(ID)) {
            registry.unregisterDynamic(ID);
        }
        registry.registerDynamic(SourceDescriptor.builder(ID)
                .name("FAA registry ingest test")
                .kind(FaaRegistryParser.KIND)
                .mode(Mode.PUSH)
                .defaultBudget(Budget.of(60, 10_000))
                .entityKey("airframe", "serial")
                .entityKey("registration", "nNumber")
                .entityKey("icao24", "modeSHex")
                .entityAttribute("manufacturer", "manufacturer")
                .build());
    }

    private static NormalizedRecord registration(String nNumber, String hex) {
        return NormalizedRecord.builder(nNumber, FaaRegistryParser.KIND)
                .observedAt(NOW)
                .put("nNumber", nNumber)
                .put("serial", "1234")
                .put("mfrMdlCode", "S22")
                .put("manufacturer", "CESSNA")
                .put("model", "172")
                .put("modeSHex", hex)
                .build();
    }
}
