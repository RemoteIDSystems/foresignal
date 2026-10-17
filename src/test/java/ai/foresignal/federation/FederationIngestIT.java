package ai.foresignal.federation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.security.InstallIdService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.federation.TestJws;
import ai.foresignal.testsupport.records.RecordsTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The hub side of an edge relay (prompt 113): {@code POST /v1/federation/ingest/{peerId}} authenticates with the
 * peer ingest key only, verifies the edge's JWS against the key pinned from its first batch, is idempotent on
 * {@code (origin_ref, origin_seq)}, accepts gzip, applies the loop rule, and accepts {@code LOCAL_ONLY} records
 * only from a {@code sameOperator} peer, keeping them {@code LOCAL_ONLY} (never re-federated).
 */
@IntegrationTest
@Import(RecordsTestSources.class)
class FederationIngestIT {

    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired PeerService peers;
    @Autowired PeerStates states;
    @Autowired FederationFeedService feed;
    @Autowired InstallIdService installId;
    @Autowired MutableClock clock;

    String admin;
    String key;
    KeyPair edge;

    @BeforeEach
    void setUp() throws Exception {
        clock.setInstant(T);
        admin = TestTokens.bearer(TestTokens.create(tokens, "fed-admin", Scope.ADMIN));
        createPeer("edge1");
        key = rotate("edge1");
        edge = TestJws.keyPair();
    }

    @AfterEach
    void tearDown() {
        for (String id : List.of("edge1", "edge2")) {
            if (states.find(id).isPresent()) {
                peers.delete(id);
            }
        }
    }

    void createPeer(String id) throws Exception {
        mvc.perform(post("/v1/peers").header(HttpHeaders.AUTHORIZATION, admin).contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"" + id + "\",\"name\":\"Cabin " + id + "\",\"baseUrl\":\"http://192.168.1.60:8080\","
                        + "\"mode\":\"RECEIVE\",\"selection\":{\"kinds\":[\"earthquake\"]}}"))
                .andExpect(status().isCreated());
    }

    String rotate(String id) throws Exception {
        String body = mvc.perform(post("/v1/peers/" + id + "/ingest-key").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String plaintext = JSON.readTree(body).get("key").asString();
        assertThat(plaintext).startsWith("fsp_");
        return plaintext;
    }

    static Map<String, Object> item(String external, long seq, String license, List<Map<String, Object>> chain) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("seq", seq);
        item.put("ref", RecordRef.format("edge-install", "local-quakes", external));
        item.put("sourceId", "local-quakes");
        item.put("externalId", external);
        item.put("kind", "earthquake");
        item.put("version", 1);
        item.put("persistReason", "FIRST");
        item.put("historyTier", "STANDARD");
        item.put("observedAt", T.toString());
        item.put("tombstone", false);
        item.put("geometry", Map.of("type", "Point", "coordinates", List.of(10.0, 50.0)));
        item.put("payload", Map.of("mag", 3.1));
        item.put("licence", license);
        item.put("provenance", Map.of("origin", chain.getFirst(), "chain", chain));
        return item;
    }

    static Map<String, Object> item(String external, long seq, String license) {
        return item(external, seq, license, List.of(FederationRules.hop("edge-install", "local-quakes", external, seq,
                T)));
    }

    static byte[] batch(Map<String, Object>... items) {
        return JSON.writeValueAsBytes(Map.of("items", List.of(items)));
    }

    MockHttpServletRequestBuilder ingest(String peerId, String bearer, byte[] body, String signature) {
        MockHttpServletRequestBuilder request = post("/v1/federation/ingest/" + peerId)
                .contentType(MediaType.APPLICATION_JSON).content(body)
                .header(FederationIngestService.KEY_HEADER, TestJws.keyHeader(edge.getPublic()));
        if (bearer != null) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer);
        }
        if (signature != null) {
            request.header("Foresignal-Signature", signature);
        }
        return request;
    }

    JsonNode send(byte[] body, int expected) throws Exception {
        String response = mvc.perform(ingest("edge1", key, body, TestJws.sign(body, edge)))
                .andExpect(status().is(expected)).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return JSON.readTree(response);
    }

    @Test
    @DisplayName("a signed batch is accepted and pins the edge's key; a re-sent batch is all duplicates")
    void acceptedAndIdempotent() throws Exception {
        byte[] body = batch(item("q1", 10, "CC-BY-4.0"), item("q2", 11, "CC-BY-4.0"));
        JsonNode first = send(body, 200);
        assertThat(first.get("accepted").asInt()).isEqualTo(2);
        assertThat(first.get("duplicates").asInt()).isZero();
        assertThat(states.find("edge1").orElseThrow().getPinnedKid()).isEqualTo(TestJws.kid(edge.getPublic()));
        JsonNode again = send(body, 200);
        assertThat(again.get("accepted").asInt()).isZero();
        assertThat(again.get("duplicates").asInt()).isEqualTo(2);
        assertThat(states.find("edge1").orElseThrow().getLastSeq()).isEqualTo(11L);
    }

    @Test
    @DisplayName("a body that does not match its signature, or a key other than the pinned one, is 403 PEER_KEY_MISMATCH")
    void signatureChecked() throws Exception {
        byte[] body = batch(item("q1", 10, "CC-BY-4.0"));
        send(body, 200);
        byte[] tampered = batch(item("q1", 12, "CC-BY-4.0"));
        String response = mvc.perform(ingest("edge1", key, tampered, TestJws.sign(body, edge)))
                .andExpect(status().isForbidden()).andReturn().getResponse().getContentAsString();
        assertThat(JSON.readTree(response).get("code").asString()).isEqualTo("PEER_KEY_MISMATCH");
        KeyPair impostor = TestJws.keyPair();
        mvc.perform(ingest("edge1", key, tampered, TestJws.sign(tampered, impostor))
                .header(FederationIngestService.KEY_HEADER, TestJws.keyHeader(impostor.getPublic())))
                .andExpect(status().isForbidden());
        mvc.perform(ingest("edge1", key, tampered, null)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("only the peer's own ingest key is accepted: no key, an admin token, a rotated key or another peer's key")
    void keyChecked() throws Exception {
        byte[] body = batch(item("q1", 10, "CC-BY-4.0"));
        String signature = TestJws.sign(body, edge);
        mvc.perform(ingest("edge1", null, body, signature)).andExpect(status().isUnauthorized());
        mvc.perform(ingest("edge1", null, body, signature).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isUnauthorized());
        createPeer("edge2");
        String other = rotate("edge2");
        mvc.perform(ingest("edge1", other, body, signature)).andExpect(status().isForbidden());
        String old = key;
        key = rotate("edge1");
        mvc.perform(ingest("edge1", old, body, signature)).andExpect(status().isUnauthorized());
        send(body, 200);
    }

    @Test
    @DisplayName("a gzip-encoded batch is accepted; the signature covers the decoded body")
    void gzip() throws Exception {
        byte[] body = batch(item("g1", 20, "CC-BY-4.0"));
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (GZIPOutputStream zip = new GZIPOutputStream(compressed)) {
            zip.write(body);
        }
        String response = mvc.perform(ingest("edge1", key, compressed.toByteArray(), TestJws.sign(body, edge))
                .header(HttpHeaders.CONTENT_ENCODING, "gzip"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(JSON.readTree(response).get("accepted").asInt()).isEqualTo(1);
    }

    @Test
    @DisplayName("an item whose chain names this instance is dropped as a loop")
    void loop() throws Exception {
        Map<String, Object> looped = item("q9", 30, "CC-BY-4.0", List.of(
                FederationRules.hop(installId.installId(), "usgs-earthquakes", "us9", 3, T),
                FederationRules.hop("edge-install", "peer-hub", "usgs-earthquakes/us9", 30, T)));
        JsonNode result = send(batch(looped), 200);
        assertThat(result.get("accepted").asInt()).isZero();
        assertThat(result.get("dropped").get("loop").asInt()).isEqualTo(1);
    }

    @Test
    @DisplayName("LOCAL_ONLY records are accepted only from a sameOperator peer, and stay LOCAL_ONLY there")
    void sameOperator() throws Exception {
        byte[] body = batch(item("local-1", 40, FederationFeedService.LOCAL_ONLY_LICENSE),
                item("open-1", 41, "CC-BY-4.0"));
        JsonNode refused = send(body, 200);
        assertThat(refused.get("accepted").asInt()).isEqualTo(1);
        assertThat(refused.get("dropped").get("licence").asInt()).isEqualTo(1);

        mvc.perform(put("/v1/peers/edge1").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Cabin edge1\",\"baseUrl\":\"http://192.168.1.60:8080\",\"sameOperator\":true,"
                        + "\"selection\":{\"kinds\":[\"earthquake\"]}}"))
                .andExpect(status().isOk());
        JsonNode accepted = send(batch(item("local-1", 40, FederationFeedService.LOCAL_ONLY_LICENSE)), 200);
        assertThat(accepted.get("accepted").asInt()).isEqualTo(1);

        FederationFeedService.Page page = feed.changes(0, 1_000, List.of(), List.of(), null,
                new FederationFeedService.Caller(ReadRestriction.NONE, false, false));
        List<String> served = page.items().stream().map(i -> String.valueOf(i.get("externalId"))).toList();
        assertThat(served).as("a received record is re-federated with its license")
                .contains("local-quakes/open-1")
                .as("a LOCAL_ONLY record from the operator's edge is never re-federated")
                .doesNotContain("local-quakes/local-1");
    }
}
