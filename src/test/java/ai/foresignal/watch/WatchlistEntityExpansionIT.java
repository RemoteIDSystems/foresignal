package ai.foresignal.watch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.entity.EntityKeyExpansion;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * An {@code entity:<id>} watchlist item (prompt 098) fires for records of any source the entity is
 * linked to, and follows a key re-assignment.
 */
@IntegrationTest
@Import(TestIngestSources.class)
class WatchlistEntityExpansionIT {

    private static final String A = "it-wl-aircraft-a";
    private static final String B = "it-wl-aircraft-b";
    private static final String VESSEL = "it-wl-vessel";
    private static final Instant NOW = Instant.parse("2026-10-15T00:00:00Z");

    @Autowired
    private WatchlistOperator inWatchlistOperator;

    @Autowired
    private WatchlistSnapshot snapshot;

    @Autowired
    private EntityKeyExpansion expansion;

    @Autowired
    private SourceRegistry registry;

    @Autowired
    private IngestQueue queue;

    @Autowired
    private MutableClock clock;

    @Autowired
    private EntityLinkRepository links;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void unregister() {
        for (String id : List.of(A, B, VESSEL)) {
            if (registry.contains(id)) {
                registry.unregisterDynamic(id);
            }
        }
    }

    @Test
    void anEntityItemFiresOnAnyOfItsSources() {
        register(A, "aircraft", "icao24", "hex");
        register(B, "aircraft", "icao24", "hex");
        TestRecords records = new TestRecords(queue, clock);
        clock.setInstant(NOW);
        records.ingest(A, aircraft("a1", "ae1234"));
        records.ingest(B, aircraft("b1", "ae1234"));
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(links.findOpen(A, "a1")).isPresent());
        String entityId = links.findOpen(A, "a1").orElseThrow().getEntityId();
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(links.findOpenByEntityId(entityId)).hasSize(2));

        UUID listId = entityList(entityId);
        assertThat(inWatchlistOperator.identityPaths(listId.toString(), List.of(A, B))).containsKeys(A, B);
        assertThat(inWatchlistOperator.evaluate("ae1234", listId.toString()).holds()).isTrue();
        assertThat(inWatchlistOperator.evaluate("ae9999", listId.toString()).holds()).isFalse();
    }

    @Test
    void anEntityItemFollowsAKeyReassignment() {
        register(VESSEL, "vessel", "mmsi", "mmsi", "imo", "imo");
        TestRecords records = new TestRecords(queue, clock);
        clock.setInstant(NOW);
        records.ingest(VESSEL, vessel("v1", "9074729", "366123456"));
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(links.findOpen(VESSEL, "v1")).isPresent());
        String first = links.findOpen(VESSEL, "v1").orElseThrow().getEntityId();

        UUID listId = entityList(first);
        assertThat(inWatchlistOperator.evaluate("366123456", listId.toString()).holds()).isTrue();

        // A new imo asserts the same mmsi: the key is re-assigned away from the first entity.
        clock.advance(Duration.ofMinutes(1));
        records.ingest(VESSEL, vessel("v2", "9161235", "366123456"));
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(expansion.valuesFor(List.of(first))).doesNotContain("366123456"));
        assertThat(inWatchlistOperator.evaluate("366123456", listId.toString()).holds()).isFalse();
    }

    private UUID entityList(String entityId) {
        UUID listId = UUID.randomUUID();
        jdbc.update("INSERT INTO watch_list (id, name, identity, normalize, item_count, created_at, updated_at)"
                + " VALUES (?, ?, 'entity', 'none', 1, now(), now())", listId, "entities-" + entityId);
        jdbc.update("INSERT INTO watch_list_item (list_id, value, added_at, origin) VALUES (?, ?, now(), 'manual')",
                listId, "entity:" + entityId);
        snapshot.loadAll();
        expansion.loadAll();
        return listId;
    }

    private void register(String id, String kind, String... entityKeys) {
        if (registry.contains(id)) {
            registry.unregisterDynamic(id);
        }
        SourceDescriptor.Builder builder = SourceDescriptor.builder(id)
                .name("Watchlist entity test " + id)
                .kind(kind)
                .mode(Mode.PUSH)
                .defaultBudget(Budget.of(60, 10_000));
        for (int i = 0; i < entityKeys.length; i += 2) {
            builder.entityKey(entityKeys[i], entityKeys[i + 1]);
        }
        registry.registerDynamic(builder.build());
    }

    private static NormalizedRecord aircraft(String id, String hex) {
        return NormalizedRecord.builder(id, "aircraft").observedAt(NOW).put("hex", hex).point(0, 0).build();
    }

    private static NormalizedRecord vessel(String id, String imo, String mmsi) {
        return NormalizedRecord.builder(id, "vessel").observedAt(NOW)
                .put("imo", imo).put("mmsi", mmsi).point(0, 0).build();
    }
}
