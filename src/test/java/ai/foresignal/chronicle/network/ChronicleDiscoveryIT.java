package ai.foresignal.chronicle.network;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import ai.foresignal.chronicle.ChronicleEntity;
import ai.foresignal.collab.MeEvents;
import ai.foresignal.security.Scope;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.JsonNode;

/**
 * Local chronicle discovery (prompt 134, DISC-1, DISC-2): two analysts' chronicles over the same war find each other as
 * opaque links from fingerprints alone; once one can see both, the link is full with the shared canonical events; the
 * cluster timeline counts shared evidence once; undiscoverable and RED chronicles disclose nothing.
 */
@IntegrationTest
@Import(AskTestSources.class)
class ChronicleDiscoveryIT extends NetworkItSupport {

    @Test
    @DisplayName("two analysts over the same war get opaque links to each other's chronicles")
    void opaqueLinks() throws Exception {
        Person alice = person("alice-disc", Scope.AGENT, null);
        Person bob = person("bob-disc", Scope.AGENT, null);
        ChronicleEntity a = active(alice, "Front A");
        ChronicleEntity b = active(bob, "Front B");
        war("q", 60, 30.0);
        discoverable(alice, a, "instance");
        discoverable(bob, b, "instance");

        ChronicleDiscoveryJob.Result result = sketchAndDiscover();
        assertThat(result.eligible()).isEqualTo(2);
        assertThat(result.linksFound()).isEqualTo(1);
        List<ChronicleLinkEntity> links = linkRows.findAll();
        assertThat(links).hasSize(1);
        ChronicleLinkEntity link = links.get(0);
        assertThat(link.getStrength()).isEqualTo("high");
        assertThat(link.getDimensions()).contains(NetworkVocabulary.EVIDENCE, NetworkVocabulary.AREA_TIME);
        assertThat(link.getSharedKeyCount()).isGreaterThanOrEqualTo(60);

        MockHttpServletResponse forBob = call(bob.bearer(), get("/v1/chronicles/" + b.getId() + "/related"));
        assertThat(forBob.getStatus()).isEqualTo(200);
        JsonNode items = body(forBob).get("items");
        assertThat(items).hasSize(1);
        JsonNode opaque = items.get(0);
        assertThat(opaque.get("disclosure").asString()).isEqualTo("opaque");
        assertThat(opaque.get("strength").asString()).isEqualTo("high");
        assertThat(opaque.get("chronicleId").asString()).isEqualTo(b.getId());
        assertThat(opaque.get("title").isNull()).isTrue();
        assertThat(opaque.get("ownerHandle").isNull()).isTrue();
        assertThat(opaque.has("sharedKeyCount")).isFalse();
        assertThat(opaque.has("scores")).isFalse();
        assertThat(opaque.has("sharedCanonicalEvents")).isFalse();
        assertThat(forBob.getContentAsString()).doesNotContain(a.getId()).doesNotContain("Front A");

        JsonNode forAlice = body(call(alice.bearer(), get("/v1/chronicles/" + a.getId() + "/related"))).get("items");
        assertThat(forAlice).hasSize(1);
        assertThat(forAlice.get(0).get("disclosure").asString()).isEqualTo("opaque");
        assertThat(inboxEvents(bob.id())).contains(MeEvents.CHRONICLE_LINK_FOUND);
        assertThat(inboxEvents(alice.id())).contains(MeEvents.CHRONICLE_LINK_FOUND);

        discoverable(alice, a, "instance", true, true);
        JsonNode revealed = body(call(bob.bearer(), get("/v1/chronicles/" + b.getId() + "/related"))).get("items")
                .get(0);
        assertThat(revealed.get("title").asString()).contains("Front A");
        assertThat(revealed.get("ownerHandle").asString()).isEqualTo("alice-disc");
    }

    @Test
    @DisplayName("a viewer of both chronicles sees the full link with the shared canonical events")
    void fullLinks() throws Exception {
        Person alice = person("alice-full", Scope.AGENT, null);
        Person bob = person("bob-full", Scope.AGENT, null);
        ChronicleEntity a = active(alice, "Front A");
        ChronicleEntity b = active(bob, "Front B");
        war("f", 60, 30.0);
        sketchAndDiscover();
        assertThat(body(call(bob.bearer(), get("/v1/chronicles/" + b.getId() + "/related"))).get("items"))
                .as("neither is discoverable: nothing for bob").isEmpty();

        shareWith(alice, a, bob);
        JsonNode full = body(call(bob.bearer(), get("/v1/chronicles/" + b.getId() + "/related"))).get("items")
                .get(0);
        assertThat(full.get("disclosure").asString()).isEqualTo("full");
        assertThat(full.get("sharedCanonicalEvents")).hasSize(60);
        assertThat(full.get("sharedCanonicalEvents").get(0).asString()).startsWith("canonical-event:");
        assertThat(full.get("sharedKeyCount").asLong()).isGreaterThanOrEqualTo(60);
        assertThat(full.get("scores").has(NetworkVocabulary.EVIDENCE)).isTrue();
        assertThat(full.get("other").get("ref").asString()).isEqualTo("chronicle:" + a.getId());
        assertThat(full.get("temporal").asString()).isEqualTo(NetworkVocabulary.CONCURRENT);
    }

    @Test
    @DisplayName("the cluster timeline counts shared evidence once and lists the chronicles holding it")
    void clusterTimeline() throws Exception {
        Person alice = person("alice-clu", Scope.AGENT, null);
        Person bob = person("bob-clu", Scope.AGENT, null);
        ChronicleEntity a = active(alice, "Front A");
        ChronicleEntity b = active(bob, "Front B");
        war("c", 60, 30.0);
        shareWith(alice, a, bob);
        sketchAndDiscover();
        List<ChronicleClusterEntity> clusters = clusterRows.findAll();
        assertThat(clusters).hasSize(1);
        assertThat(clusters.get(0).getMembers()).containsExactlyInAnyOrder(a.getId(), b.getId());

        JsonNode list = body(call(bob.bearer(), get("/v1/chronicle-network/clusters"))).get("items");
        assertThat(list).hasSize(1);
        assertThat(list.get(0).get("visibleMembers").asInt()).isEqualTo(2);
        assertThat(list.get(0).get("opaqueMembers").asInt()).isZero();

        JsonNode timeline = body(call(bob.bearer(), get("/v1/chronicle-network/clusters/" + clusters.get(0).getId()
                + "/timeline?limit=500")));
        Set<String> keys = new HashSet<>();
        int records = 0;
        for (JsonNode item : timeline.get("items")) {
            assertThat(keys.add(item.get("evidenceKey").asString())).as("deduplicated by evidence key").isTrue();
            if ("record".equals(item.get("type").asString())) {
                records++;
                assertThat(item.get("evidenceKey").asString()).startsWith("ev:seismic:usgs:c-U");
                assertThat(item.get("heldBy")).hasSize(2);
            }
        }
        assertThat(records).as("60 shared events, not 120").isEqualTo(60);

        Person carol = person("carol-clu", Scope.READ, null);
        assertThat(call(carol.bearer(), get("/v1/chronicle-network/clusters/" + clusters.get(0).getId()))
                .getStatus()).as("a principal who sees no member").isEqualTo(404);
    }

    @Test
    @DisplayName("a chronicle seen only by its owner stays undisclosed, and a RED label lowers discoverability")
    void undiscoverableAndRed() throws Exception {
        Person alice = person("alice-red", Scope.AGENT, null);
        Person bob = person("bob-red", Scope.AGENT, null);
        ChronicleEntity a = active(alice, "Front A");
        ChronicleEntity b = active(bob, "Front B");
        war("r", 60, 30.0);
        discoverable(alice, a, "network");
        sketchAndDiscover();
        assertThat(body(call(bob.bearer(), get("/v1/chronicles/" + b.getId() + "/related"))).get("items"))
                .hasSize(1);

        shares.replace("chronicle:" + a.getId(), node("{'entries':[],'tlp':'RED'}"), alice.caller(), "test");
        assertThat(chronicleRows.findById(a.getId()).orElseThrow().getDiscoverability()).isEqualTo("none");
        assertThat(inboxEvents(alice.id())).contains(MeEvents.CHRONICLE_DISCOVERABILITY_LOWERED);
        assertThat(body(call(bob.bearer(), get("/v1/chronicles/" + b.getId() + "/related"))).get("items"))
                .as("RED is never discoverable").isEmpty();
        Map<String, Object> view = network.discoverability(chronicleRows.findById(a.getId()).orElseThrow());
        assertThat(view).containsEntry("maxAllowed", "none").containsEntry("tlp", "RED");
    }

    @Test
    @DisplayName("a deleted chronicle loses its links, and its owner's next chronicle waits a day")
    void deletionAndChurn() {
        Person alice = person("alice-churn", Scope.AGENT, null);
        Person bob = person("bob-churn", Scope.AGENT, null);
        ChronicleEntity a = active(alice, "Front A");
        ChronicleEntity b = active(bob, "Front B");
        war("d", 60, 30.0);
        sketchAndDiscover();
        assertThat(linkRows.findAll()).hasSize(1);
        chronicles.delete(a.getId(), a.getDisplayId(), alice.caller());
        assertThat(linkRows.findAll()).isEmpty();

        ChronicleEntity again = active(alice, "Front A again");
        war("e", 60, 60.0);
        ChronicleDiscoveryJob.Result result = sketchAndDiscover();
        assertThat(result.eligible()).as("the churned owner's new chronicle sits out").isEqualTo(1);
        assertThat(linkRows.findAll()).isEmpty();
        assertThat(again.getId()).isNotEqualTo(b.getId());
    }
}
