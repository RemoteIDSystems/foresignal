package ai.foresignal.chronicle.network;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.chronicle.ChronicleEntity;
import ai.foresignal.collab.AclRole;
import ai.foresignal.collab.MeEvents;
import ai.foresignal.federation.PeerEntity;
import ai.foresignal.federation.PeerRepository;
import ai.foresignal.federation.PeerService;
import ai.foresignal.security.Scope;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.JsonNode;

/**
 * Access requests (prompt 134): a request reveals only the requester's handle, message and share offer; a grant applies
 * an access-list grant here or sends a signed package to a remote requester; a decline is told as a status only; the
 * daily limit and the 30-day suppression after a decline hold.
 */
@IntegrationTest
@Import(AskTestSources.class)
class AccessRequestIT extends NetworkItSupport {

    @Autowired PeerService peerService;
    @Autowired PeerRepository peerRows;

    private final List<String> createdPeers = new ArrayList<>();

    @AfterEach
    void deletePeers() {
        for (String id : createdPeers) {
            if (peerRows.existsById(id)) {
                peerService.delete(id);
            }
        }
        createdPeers.clear();
        federatedDiscovery(false);
    }

    /** Alice's discoverable chronicle and Bob's, over the same war, linked; returns the link id. */
    private String linked(Person alice, ChronicleEntity a, Person bob, ChronicleEntity b) {
        war("w", 60, 30.0);
        discoverable(alice, a, "instance");
        discoverable(bob, b, "instance");
        sketchAndDiscover();
        return linkRows.findAll().get(0).getId();
    }

    @Test
    @DisplayName("a request carries only the handle, the message and the offer; a grant shares both ways")
    void localGrant() throws Exception {
        Person alice = person("alice-acc", Scope.AGENT, null);
        Person bob = person("bob-acc", Scope.AGENT, null);
        ChronicleEntity a = active(alice, "Alice's secret front");
        ChronicleEntity b = active(bob, "Bob's own front");
        String linkId = linked(alice, a, bob, b);
        call(bob.bearer(), get("/v1/chronicles/" + b.getId() + "/related"));

        MockHttpServletResponse created = call(bob.bearer(), json(post("/v1/chronicle-network/links/" + linkId
                + "/request-access"), "{'message':'Working the same front','offerShare':'" + b.getId() + "'}"));
        assertThat(created.getStatus()).as(created.getContentAsString()).isEqualTo(201);
        JsonNode outgoing = body(created);
        assertThat(outgoing.get("direction").asString()).isEqualTo("outgoing");
        assertThat(outgoing.get("chronicleId").isNull()).as("the target is not revealed to the requester").isTrue();
        assertThat(outgoing.get("status").asString()).isEqualTo("pending");
        assertThat(inboxEvents(alice.id())).contains(MeEvents.CHRONICLE_ACCESS_REQUESTED);

        JsonNode incoming = body(call(alice.bearer(), get("/v1/chronicle-network/requests?direction=incoming")))
                .get("items");
        assertThat(incoming).hasSize(1);
        JsonNode request = incoming.get(0);
        assertThat(request.get("requesterHandle").asString()).isEqualTo("bob-acc");
        assertThat(request.get("message").asString()).isEqualTo("Working the same front");
        assertThat(request.get("chronicleId").asString()).isEqualTo(a.getId());
        assertThat(request.get("offerShare").get("chronicleId").asString()).isEqualTo(b.getId());
        String text = request.toString();
        assertThat(text).doesNotContain("Bob's own front");
        assertThat(access.role(alice.caller(), "chronicle:" + b.getId())).as("nothing shared before the grant")
                .isNull();

        MockHttpServletResponse granted = call(alice.bearer(), json(post("/v1/chronicle-network/requests/"
                + request.get("id").asString() + "/grant"), "{'role':'viewer'}"));
        assertThat(granted.getStatus()).as(granted.getContentAsString()).isEqualTo(200);
        assertThat(body(granted).get("grant").get("deliveredAs").asString()).isEqualTo("acl");
        assertThat(access.role(bob.caller(), "chronicle:" + a.getId())).isEqualTo(AclRole.VIEWER);
        assertThat(access.role(alice.caller(), "chronicle:" + b.getId())).as("the offer").isEqualTo(AclRole.VIEWER);
        assertThat(inboxEvents(bob.id())).contains(MeEvents.CHRONICLE_ACCESS_GRANTED);
        MockHttpServletResponse again = call(alice.bearer(), post("/v1/chronicle-network/requests/"
                + request.get("id").asString() + "/decline"));
        assertThat(again.getStatus()).isEqualTo(409);
        assertThat(code(again)).isEqualTo("ACCESS_REQUEST_DECIDED");
        JsonNode full = body(call(bob.bearer(), get("/v1/chronicles/" + b.getId() + "/related"))).get("items")
                .get(0);
        assertThat(full.get("disclosure").asString()).isEqualTo("full");
    }

    @Test
    @DisplayName("a decline is told as a status, repeats to that owner are suppressed, and 10 a day is the limit")
    void declineAndLimits() throws Exception {
        Person alice = person("alice-lim", Scope.AGENT, null);
        Person bob = person("bob-lim", Scope.AGENT, null);
        ChronicleEntity a = active(alice, "Front A");
        ChronicleEntity b = active(bob, "Front B");
        String linkId = linked(alice, a, bob, b);
        call(bob.bearer(), get("/v1/chronicles/" + b.getId() + "/related"));
        String first = body(call(bob.bearer(), json(post("/v1/chronicle-network/links/" + linkId + "/request-access"),
                "{'message':'please'}"))).get("id").asString();
        assertThat(call(alice.bearer(), post("/v1/chronicle-network/requests/" + first + "/decline")).getStatus())
                .isEqualTo(200);
        assertThat(inboxEvents(bob.id())).contains(MeEvents.CHRONICLE_ACCESS_DECLINED);
        assertThat(body(call(bob.bearer(), get("/v1/chronicle-network/requests?direction=outgoing"))).get("items")
                .get(0).get("status").asString()).isEqualTo("declined");

        for (int i = 2; i <= 10; i++) {
            assertThat(call(bob.bearer(), json(post("/v1/chronicle-network/links/" + linkId + "/request-access"),
                    "{'message':'again " + i + "'}")).getStatus()).isEqualTo(201);
        }
        JsonNode incoming = body(call(alice.bearer(), get("/v1/chronicle-network/requests?direction=incoming")))
                .get("items");
        assertThat(incoming).as("repeats after a decline are suppressed for 30 days").hasSize(1);
        MockHttpServletResponse limited = call(bob.bearer(), json(post("/v1/chronicle-network/links/" + linkId
                + "/request-access"), "{'message':'eleventh'}"));
        assertThat(limited.getStatus()).isEqualTo(429);
        assertThat(code(limited)).isEqualTo("REQUEST_RATE_LIMITED");

        Person carol = person("carol-lim", Scope.READ, null);
        MockHttpServletResponse stranger = call(carol.bearer(), post("/v1/chronicle-network/links/" + linkId
                + "/request-access"));
        assertThat(stranger.getStatus()).as("a principal who sees neither side").isEqualTo(404);
        assertThat(code(stranger)).isEqualTo("UNKNOWN_LINK");
    }

    @Test
    @DisplayName("a remote requester is granted a signed package of a chronicle extract")
    void remoteGrant() throws Exception {
        federatedDiscovery(true);
        assertThat(call(admin, json(put("/v1/chronicle-network/salts"),
                "{'networkId':'gulf-net','saltRef':'agreed-out-of-band-0123456789'}")).getStatus()).isEqualTo(200);
        peerService.create(JSON.readTree("{\"id\":\"peer-r\",\"name\":\"Peer R\",\"baseUrl\":"
                + "\"http://127.0.0.1:9\",\"mode\":\"RECEIVE\",\"exchangeKey\":\"fsp_test-exchange-key\"}"));
        createdPeers.add("peer-r");
        PeerEntity peer = peerRows.findById("peer-r").orElseThrow();
        peer.setRemoteInstanceId("fs-peer-r");
        peerRows.save(peer);

        Person alice = person("alice-rem", Scope.AGENT, null);
        ChronicleEntity a = active(alice, "Front A");
        war("m", 60, 30.0);
        shares.replace("chronicle:" + a.getId(), node("{'entries':[],'tlp':'GREEN'}"), alice.caller(), "test");
        discoverable(alice, a, "network");
        sketcher.refreshAll();
        NetworkSalts.Salt net = salts.networkSalt("gulf-net").orElseThrow();
        String sketchId = keyStore.state(a.getId(), net.id(), null).sketchId();

        accessRequests.receiveRemoteRequest(node("{'requestId':'01JA8Q3V7S2K9W4T6Y1B5N0M3R','sketchId':'" + sketchId
                + "','requesterHandle':'xavier','message':'Same convoy','offerShare':true}"), "fs-peer-r", "gulf-net");
        JsonNode incoming = body(call(alice.bearer(), get("/v1/chronicle-network/requests"))).get("items");
        assertThat(incoming).hasSize(1);
        assertThat(incoming.get(0).get("requesterInstanceId").asString()).isEqualTo("fs-peer-r");
        assertThat(incoming.get(0).get("requesterHandle").asString()).isEqualTo("xavier");
        assertThat(inboxEvents(alice.id())).contains(MeEvents.CHRONICLE_ACCESS_REQUESTED);

        MockHttpServletResponse granted = call(alice.bearer(), json(post("/v1/chronicle-network/requests/"
                + incoming.get(0).get("id").asString() + "/grant"), "{'deliverAs':'chronicle-extract'}"));
        assertThat(granted.getStatus()).as(granted.getContentAsString()).isEqualTo(200);
        JsonNode grant = body(granted).get("grant");
        assertThat(grant.get("deliveredAs").asString()).isEqualTo("chronicle-extract");
        assertThat(grant.get("packageId").asString()).hasSize(26);
        assertThat(access.role(alice.caller(), "chronicle:" + a.getId())).isEqualTo(AclRole.OWNER);
    }
}
