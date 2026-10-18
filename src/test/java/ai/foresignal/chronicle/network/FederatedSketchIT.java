package ai.foresignal.chronicle.network;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.chronicle.ChronicleEntity;
import ai.foresignal.exchange.ExchangeTestPackages;
import ai.foresignal.federation.PeerEntity;
import ai.foresignal.federation.PeerRepository;
import ai.foresignal.federation.PeerService;
import ai.foresignal.security.Scope;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.JsonNode;

/**
 * Federated discovery (prompt 134, DISC-1): a peer with the same network salt sends a signed sketch of a chronicle that
 * captured the same war, and an opaque cross-instance link appears; a sketch under a different salt links nothing;
 * unsigned, tampered, RED or unknown-network batches are refused; received sketches expire after 30 days and salts
 * rotate after 90 with new sketch ids. One context plus a recorded peer ({@value #PEER}) signing with its own key.
 */
@IntegrationTest
@Import(AskTestSources.class)
class FederatedSketchIT extends NetworkItSupport {

    static final String PEER = "fs-peer-x";
    static final String NETWORK = "gulf-net";
    static final String SALT = "agreed-out-of-band-0123456789";

    @Autowired PeerService peerService;
    @Autowired PeerRepository peerRows;

    private final List<String> createdPeers = new ArrayList<>();

    /** A recorded peer: its signing key and the ingest key it calls this instance with. */
    record Peer(ExchangeTestPackages.Key key, String ingestKey) {}

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

    private Peer peer() {
        ExchangeTestPackages.Key key = ExchangeTestPackages.newKey();
        peerService.create(JSON.readTree("{\"id\":\"peer-x\",\"name\":\"Peer X\",\"baseUrl\":"
                + "\"http://192.168.1.71:8080\",\"mode\":\"RECEIVE\"}"));
        createdPeers.add("peer-x");
        PeerEntity p = peerRows.findById("peer-x").orElseThrow();
        p.setPinned(key.kid(), key.x());
        p.setRemoteInstanceId(PEER);
        peerRows.save(p);
        return new Peer(key, String.valueOf(peerService.rotateIngestKey("peer-x").get("key")));
    }

    private void installSalt(String network, String value) throws Exception {
        MockHttpServletResponse r = call(admin, json(put("/v1/chronicle-network/salts"), "{'networkId':'" + network
                + "','saltRef':'" + value + "'}"));
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        assertThat(r.getContentAsString()).doesNotContain(value);
    }

    private static Map<String, Object> sketch(String sketchId, String network, Object minhash, Object bands,
            String tlp) {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("sketchId", sketchId);
        s.put("instanceId", PEER);
        s.put("networkId", network);
        s.put("keyTypes", new ArrayList<>(((Map<?, ?>) minhash).keySet()));
        s.put("minhash", minhash);
        s.put("sizeBand", bands);
        s.put("captureSpan", Map.of("from", IsoWeeks.of(T)));
        s.put("title", null);
        s.put("ownerHandle", null);
        s.put("tlp", tlp);
        s.put("issuedAt", T.toString());
        return s;
    }

    private static byte[] batch(String network, List<Map<String, Object>> sketches, List<String> withdrawn) {
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("instanceId", PEER);
        b.put("networkId", network);
        b.put("sketches", sketches);
        b.put("withdrawn", withdrawn);
        return JSON.writeValueAsBytes(b);
    }

    private MockHttpServletResponse send(Peer p, byte[] body, String signature) throws Exception {
        var request = post("/v1/federation/chronicle-sketches").contentType(MediaType.APPLICATION_JSON).content(body);
        if (signature != null) {
            request.header("Foresignal-Signature", signature);
        }
        return call("Bearer " + p.ingestKey(), request);
    }

    /** The network-discoverable chronicle of a war, sketched under the network salt; returns our own package. */
    @SuppressWarnings("unchecked")
    private Map<String, Object> ourPackage(Person alice, ChronicleEntity a) {
        discoverable(alice, a, "network");
        sketcher.refreshAll();
        NetworkSalts.Salt net = salts.networkSalt(NETWORK).orElseThrow();
        List<Map<String, Object>> sketches = (List<Map<String, Object>>) federated.batch(net).get("sketches");
        assertThat(sketches).hasSize(1);
        return sketches.get(0);
    }

    @Test
    @DisplayName("a signed sketch under the shared network salt yields an opaque cross-instance link")
    void sharedSalt() throws Exception {
        federatedDiscovery(true);
        installSalt(NETWORK, SALT);
        Peer p = peer();
        Person alice = person("alice-fed", Scope.AGENT, null);
        ChronicleEntity a = active(alice, "Front A");
        war("n", 60, 30.0);
        Map<String, Object> ours = ourPackage(alice, a);
        assertThat(ours).containsKeys("sketchId", "minhash", "sizeBand", "captureSpan", "tlp");
        assertThat(ours.get("title")).as("not revealed").isNull();
        assertThat(ours.get("ownerHandle")).isNull();
        assertThat(JSON.writeValueAsString(ours)).doesNotContain("Front A").doesNotContain("n-U0")
                .doesNotContain(a.getId());

        byte[] body = batch(NETWORK, List.of(sketch("peer-sketch-1", NETWORK, ours.get("minhash"),
                ours.get("sizeBand"), "AMBER")), List.of());
        MockHttpServletResponse accepted = send(p, body, ExchangeTestPackages.sign(p.key(), body));
        assertThat(accepted.getStatus()).as(accepted.getContentAsString()).isEqualTo(202);
        assertThat(body(accepted).get("accepted").asInt()).isEqualTo(1);
        assertThat(remoteRows.findAll()).hasSize(1);

        discovery.run();
        List<ChronicleLinkEntity> links = linkRows.findAll();
        assertThat(links).hasSize(1);
        assertThat(links.get(0).getA()).isEqualTo(a.getId());
        assertThat(links.get(0).getB()).isEqualTo(NetworkVocabulary.remoteNode(PEER, "peer-sketch-1"));
        JsonNode item = body(call(alice.bearer(), get("/v1/chronicles/" + a.getId() + "/related"))).get("items")
                .get(0);
        assertThat(item.get("disclosure").asString()).isEqualTo("opaque");
        assertThat(item.get("remote").asBoolean()).isTrue();
        assertThat(item.get("strength").asString()).isEqualTo("high");
    }

    @Test
    @DisplayName("a sketch of the same war under a different salt links nothing")
    void differentSalt() throws Exception {
        federatedDiscovery(true);
        installSalt(NETWORK, SALT);
        Peer p = peer();
        Person alice = person("alice-salt", Scope.AGENT, null);
        ChronicleEntity a = active(alice, "Front A");
        war("s", 60, 30.0);
        ourPackage(alice, a);
        String local = salts.instanceSalt().id();
        KeyedHasher other = new KeyedHasher("a-different-salt-0123456789".getBytes(StandardCharsets.UTF_8));
        Map<String, List<Long>> byType = new LinkedHashMap<>();
        for (Map<String, Object> row : jdbc.queryForList("SELECT key_type, key_text FROM chronicle_key"
                + " WHERE chronicle_id = ? AND salt_id = ?", a.getId(), local)) {
            byType.computeIfAbsent(String.valueOf(row.get("key_type")), k -> new ArrayList<>())
                    .add(other.hash(String.valueOf(row.get("key_text"))));
        }
        Map<String, Object> minhash = new LinkedHashMap<>();
        Map<String, Object> bands = new LinkedHashMap<>();
        for (Map.Entry<String, List<Long>> e : byType.entrySet()) {
            minhash.put(e.getKey(), MinHashSketch.of(e.getValue()).toBase64());
            bands.put(e.getKey(), MinHashSketch.sizeBand(e.getValue().size()));
        }
        byte[] body = batch(NETWORK, List.of(sketch("peer-sketch-2", NETWORK, minhash, bands, "GREEN")), List.of());
        assertThat(send(p, body, ExchangeTestPackages.sign(p.key(), body)).getStatus()).isEqualTo(202);
        discovery.run();
        assertThat(linkRows.findAll()).isEmpty();
    }

    @Test
    @DisplayName("unsigned, tampered, RED, unknown-network and disabled batches are refused")
    void refusals() throws Exception {
        federatedDiscovery(true);
        installSalt(NETWORK, SALT);
        Peer p = peer();
        Map<String, Object> minhash = Map.of(NetworkVocabulary.EVIDENCE, MinHashSketch.of(List.of(1L, 2L, 3L))
                .toBase64());
        Map<String, Object> bands = Map.of(NetworkVocabulary.EVIDENCE, 1);
        byte[] body = batch(NETWORK, List.of(sketch("peer-sketch-3", NETWORK, minhash, bands, "AMBER")), List.of());
        String signature = ExchangeTestPackages.sign(p.key(), body);

        assertThat(call(null, post("/v1/federation/chronicle-sketches").contentType(MediaType.APPLICATION_JSON)
                .content(body)).getStatus()).isEqualTo(401);
        MockHttpServletResponse unsigned = send(p, body, null);
        assertThat(unsigned.getStatus()).isEqualTo(400);
        assertThat(code(unsigned)).isEqualTo("SKETCH_BATCH_INVALID");
        byte[] tampered = new String(body, StandardCharsets.UTF_8).replace("peer-sketch-3", "peer-sketch-4")
                .getBytes(StandardCharsets.UTF_8);
        assertThat(send(p, tampered, signature).getStatus()).isEqualTo(400);

        byte[] red = batch(NETWORK, List.of(sketch("peer-sketch-5", NETWORK, minhash, bands, "RED"),
                sketch("peer-sketch-6", NETWORK, minhash, bands, "AMBER+STRICT")), List.of());
        JsonNode result = body(send(p, red, ExchangeTestPackages.sign(p.key(), red)));
        assertThat(result.get("accepted").asInt()).isZero();
        assertThat(result.get("rejected")).hasSize(2);
        assertThat(result.get("rejected").get(0).get("reason").asString()).contains("TLP");

        byte[] unknown = batch("other-net", List.of(), List.of());
        MockHttpServletResponse noSalt = send(p, unknown, ExchangeTestPackages.sign(p.key(), unknown));
        assertThat(noSalt.getStatus()).isEqualTo(409);
        assertThat(code(noSalt)).isEqualTo("NETWORK_SALT_UNKNOWN");

        federatedDiscovery(false);
        MockHttpServletResponse off = send(p, body, signature);
        assertThat(off.getStatus()).isEqualTo(409);
        assertThat(code(off)).isEqualTo("FEDERATED_DISCOVERY_DISABLED");
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("received sketches expire after 30 days; a salt rotates after 90 days with new sketch ids")
    void expiryAndRotation() throws Exception {
        federatedDiscovery(true);
        installSalt(NETWORK, SALT);
        Peer p = peer();
        Person alice = person("alice-rot", Scope.AGENT, null);
        ChronicleEntity a = active(alice, "Front A");
        war("x", 60, 30.0);
        Map<String, Object> ours = ourPackage(alice, a);
        String firstSketchId = String.valueOf(ours.get("sketchId"));
        byte[] body = batch(NETWORK, List.of(sketch("peer-sketch-7", NETWORK, ours.get("minhash"),
                ours.get("sizeBand"), "AMBER")), List.of());
        assertThat(send(p, body, ExchangeTestPackages.sign(p.key(), body)).getStatus()).isEqualTo(202);
        discovery.run();
        assertThat(linkRows.findAll()).hasSize(1);

        clock.setInstant(T.plus(Duration.ofDays(31)));
        assertThat(federated.expire()).isEqualTo(1);
        assertThat(remoteRows.findAll()).isEmpty();
        assertThat(linkRows.findAll()).isEmpty();

        clock.setInstant(T.plus(Duration.ofDays(91)));
        assertThat(salts.networkSalt(NETWORK)).as("past its 90 days").isEmpty();
        MockHttpServletResponse stale = send(p, body, ExchangeTestPackages.sign(p.key(), body));
        assertThat(stale.getStatus()).isEqualTo(409);
        installSalt(NETWORK, "the-next-agreed-salt-0123456789");
        sketcher.refreshAll();
        NetworkSalts.Salt fresh = salts.networkSalt(NETWORK).orElseThrow();
        String secondSketchId = keyStore.state(a.getId(), fresh.id(), null).sketchId();
        assertThat(secondSketchId).isNotNull().isNotEqualTo(firstSketchId);

        keyStore.sent(a.getId(), fresh.id(), clock.instant());
        discoverable(alice, a, "instance");
        Map<String, Object> after = federated.batch(fresh);
        assertThat((List<?>) after.get("sketches")).as("no longer network-discoverable").isEmpty();
        assertThat((List<Object>) after.get("withdrawn")).as("peers are told to drop it").contains(secondSketchId);
    }
}
