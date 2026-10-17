package ai.foresignal.federation;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.store.FetchTrigger;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.records.RecordsTestSources;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Loop prevention and cross-path dedup (prompt 113): an item whose chain names this instance is dropped, and so is
 * one at the hop limit; the same origin arriving over two peers yields one local record (in the owner's source); a
 * stale origin seq is dropped and an equal one is a duplicate; ownership moves to the arriving peer once the owner
 * is deleted.
 */
@IntegrationTest
@Import(RecordsTestSources.class)
class LoopAndDedupTest {

    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired PeerService peers;
    @Autowired PeerStates states;
    @Autowired PeerRuntime runtime;
    @Autowired FederationIngestor ingestor;
    @Autowired FederationWriter writer;
    @Autowired FederationIdentity identity;
    @Autowired JdbcTemplate jdbc;
    @Autowired MutableClock clock;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        create("loop-a");
        create("loop-b");
    }

    @AfterEach
    void tearDown() {
        for (String id : List.of("loop-a", "loop-b")) {
            if (states.find(id).isPresent()) {
                peers.delete(id);
            }
        }
    }

    void create(String id) {
        peers.create(JSON.readTree("{\"id\":\"" + id + "\",\"name\":\"" + id + "\",\"baseUrl\":\"http://192.168.1.50:8080\","
                + "\"mode\":\"RECEIVE\",\"selection\":{\"kinds\":[\"earthquake\"]}}"));
    }

    FederationIngestor.Context context(String peerId, int maxHops) {
        PeerEntity peer = states.find(peerId).orElseThrow();
        PeerSelection selection = new PeerSelection(List.of(), List.of("earthquake"), null, null, null, "none", false,
                maxHops, null);
        return runtime.context(peer, selection, false);
    }

    static Map<String, Object> hop(String instance, String source, String external, long seq) {
        return FederationRules.hop(instance, source, external, seq, T);
    }

    static JsonNode item(List<Map<String, Object>> chain, double mag) {
        Map<String, Object> last = chain.getLast();
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("seq", last.get("seq"));
        item.put("ref", RecordRef.format(String.valueOf(last.get("instanceId")), String.valueOf(last.get("sourceId")),
                String.valueOf(last.get("externalId"))));
        item.put("sourceId", last.get("sourceId"));
        item.put("externalId", last.get("externalId"));
        item.put("kind", "earthquake");
        item.put("version", 1);
        item.put("persistReason", "FIRST");
        item.put("historyTier", "STANDARD");
        item.put("observedAt", T.toString());
        item.put("tombstone", false);
        item.put("geometry", Map.of("type", "Point", "coordinates", List.of(-97.74, 30.27)));
        item.put("payload", Map.of("mag", mag));
        item.put("licence", "CC-BY-4.0");
        item.put("provenance", Map.of("origin", chain.getFirst(), "chain", chain));
        return JSON.valueToTree(item);
    }

    FederationIngestor.Result apply(String peerId, JsonNode item) {
        FederationIngestor.Result result = ingestor.process(List.of(item), context(peerId, 3), T);
        if (!result.records().isEmpty()) {
            writer.submit(FederationWriter.group(result.records()), FetchTrigger.PUSH, "peer:" + peerId, false);
        }
        return result;
    }

    List<String> owners(String localExternalId) {
        return jdbc.queryForList("SELECT source_id FROM record_head WHERE external_id = ?", String.class,
                localExternalId);
    }

    @Test
    @DisplayName("an item whose chain names this instance is dropped as a loop")
    void ownInstanceDropped() {
        List<Map<String, Object>> chain = List.of(hop(identity.instanceId(), "usgs-earthquakes", "us1", 5),
                hop("relay", "peer-x", "usgs-earthquakes/us1", 40));
        FederationIngestor.Result result = ingestor.process(List.of(item(chain, 5)), context("loop-a", 3), T);
        assertThat(result.accepted()).isZero();
        assertThat(result.dropped()).containsEntry(FederationRules.DROP_LOOP, 1);
        assertThat(FederationRules.loopReason(chain, identity.instanceId(), 3)).isEqualTo(FederationRules.DROP_LOOP);
    }

    @Test
    @DisplayName("an item whose chain has maxHops or more entries is dropped; a higher limit lets it in")
    void maxHops() {
        List<Map<String, Object>> chain = List.of(hop("origin-x", "usgs-earthquakes", "us2", 5),
                hop("relay-1", "peer-p", "usgs-earthquakes/us2", 10), hop("relay-2", "peer-q", "usgs-earthquakes/us2", 20));
        FederationIngestor.Result limited = ingestor.process(List.of(item(chain, 5)), context("loop-a", 3), T);
        assertThat(limited.dropped()).containsEntry(FederationRules.DROP_HOPS, 1);
        FederationIngestor.Result allowed = ingestor.process(List.of(item(chain, 5)), context("loop-a", 4), T);
        assertThat(allowed.accepted()).isEqualTo(1);
        assertThat(allowed.records().getFirst().record().externalId()).isEqualTo("usgs-earthquakes/us2");
        assertThat(FederationRules.loopReason(chain.subList(0, 2), "me", 3)).isNull();
    }

    @Test
    @DisplayName("the same origin over two peers is one local record; stale seqs are dropped, equal ones are duplicates")
    void sameOriginTwoPaths() {
        Map<String, Object> origin5 = hop("origin-x", "usgs-earthquakes", "us1", 5);
        FederationIngestor.Result viaA = apply("loop-a", item(List.of(origin5,
                hop("relay-a", "usgs-earthquakes", "us1", 100)), 5.0));
        assertThat(viaA.accepted()).isEqualTo(1);
        assertThat(viaA.records().getFirst().sourceId()).isEqualTo(PeerDescriptors.standardId("loop-a"));

        Map<String, Object> origin6 = hop("origin-x", "usgs-earthquakes", "us1", 6);
        FederationIngestor.Result viaB = apply("loop-b", item(List.of(origin6,
                hop("relay-b", "usgs-earthquakes", "us1", 200)), 6.0));
        assertThat(viaB.accepted()).isEqualTo(1);
        assertThat(viaB.records().getFirst().sourceId()).as("written into the owner's source")
                .isEqualTo(PeerDescriptors.standardId("loop-a"));
        assertThat(owners("usgs-earthquakes/us1")).containsExactly(PeerDescriptors.standardId("loop-a"));
        Integer version = jdbc.queryForObject("SELECT version FROM record_head WHERE external_id = ?", Integer.class,
                "usgs-earthquakes/us1");
        assertThat(version).isEqualTo(2);

        FederationIngestor.Result stale = apply("loop-b", item(List.of(hop("origin-x", "usgs-earthquakes", "us1", 4),
                hop("relay-b", "usgs-earthquakes", "us1", 201)), 4.0));
        assertThat(stale.accepted()).isZero();
        assertThat(stale.dropped()).containsEntry(FederationRules.DROP_DEDUP, 1);

        FederationIngestor.Result again = apply("loop-a", item(List.of(origin6,
                hop("relay-a", "usgs-earthquakes", "us1", 101)), 6.0));
        assertThat(again.duplicates()).isEqualTo(1);
        assertThat(again.accepted()).isZero();
        assertThat(again.toJson()).containsEntry("duplicates", 1);
    }

    @Test
    @DisplayName("ownership moves to the arriving peer once the owning peer is deleted")
    void ownershipTransfer() {
        apply("loop-a", item(List.of(hop("origin-x", "usgs-earthquakes", "us3", 5),
                hop("relay-a", "usgs-earthquakes", "us3", 100)), 5.0));
        assertThat(owners("usgs-earthquakes/us3")).containsExactly(PeerDescriptors.standardId("loop-a"));
        peers.delete("loop-a");
        FederationIngestor.Result moved = apply("loop-b", item(List.of(hop("origin-x", "usgs-earthquakes", "us3", 7),
                hop("relay-b", "usgs-earthquakes", "us3", 200)), 7.0));
        assertThat(moved.accepted()).isEqualTo(1);
        assertThat(moved.records().getFirst().sourceId()).isEqualTo(PeerDescriptors.standardId("loop-b"));
        List<String> owners = new ArrayList<>(owners("usgs-earthquakes/us3"));
        assertThat(owners).contains(PeerDescriptors.standardId("loop-b"));
    }
}
