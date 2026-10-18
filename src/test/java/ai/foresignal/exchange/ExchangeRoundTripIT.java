package ai.foresignal.exchange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.collab.MeEvents;
import ai.foresignal.federation.FederationIdentity;
import ai.foresignal.federation.PeerEntity;
import ai.foresignal.federation.PeerRepository;
import ai.foresignal.federation.PeerService;
import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectService;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.provenance.SigningKeyService;
import ai.foresignal.report.ReportEntity;
import ai.foresignal.report.ReportItSupport;
import ai.foresignal.security.Scope;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import tools.jackson.databind.JsonNode;

/**
 * Exchange between instances (prompt 132, RPT-2), with one context plus a recorded peer: packages signed by another
 * instance's key ({@code fs-peer-a}) are imported here read-only for their named recipients only, RED stays with them
 * alone, remote references resolve with local snapshots, tampering is refused, receipts and acknowledgements are
 * recorded, and a package never loops back to an instance it came from. A package built here verifies against this
 * instance's JWKS.
 */
@IntegrationTest
@Import(AskTestSources.class)
class ExchangeRoundTripIT extends ReportItSupport {

    static final String PEER_INSTANCE = "fs-peer-a";

    @Autowired FederationIdentity identity;
    @Autowired PeerService peerService;
    @Autowired PeerRepository peerRows;
    @Autowired SigningKeyService signingKeys;
    @Autowired ExchangeSenderKeyRepository senderKeys;
    @Autowired ReceivedObjectRepository receivedObjects;
    @Autowired ObjectService objects;

    private final List<String> createdPeers = new ArrayList<>();

    @AfterEach
    void deletePeers() {
        for (String id : createdPeers) {
            if (peerRows.existsById(id)) {
                peerService.delete(id);
            }
        }
        createdPeers.clear();
    }

    private MockHttpServletResponse importPackage(byte[] zip, Map<String, Object> jwks) throws Exception {
        var request = multipart("/v1/exchange/import").file(new MockMultipartFile("file", "package.zip",
                ExchangeVocabulary.MEDIA_TYPE, zip));
        if (jwks != null) {
            request.param("jwks", JSON.writeValueAsString(jwks));
        }
        return call(admin, request);
    }

    private static String remotePath(String ref) {
        return "/v1/objects/" + ref.replace("%", "%25").replace("/", "%2F");
    }

    @Test
    @DisplayName("an imported package is read-only, for its named recipients, with remote refs and its origin shown")
    void importForRecipients() throws Exception {
        Person bob = person("bob", Scope.READ, null);
        Person carol = person("carol", Scope.READ, null);
        ExchangeTestPackages.Key key = ExchangeTestPackages.newKey();
        String situationId = "01JA8Q3V7S2K9W4T6Y1B5N0M3C";
        ExchangeTestPackages.Parts parts = ExchangeTestPackages.report(key, PEER_INSTANCE, identity.instanceId(),
                List.of("bob"), "AMBER", situationId);

        assertThat(importPackage(parts.zip(), null).getStatus()).as("no key is known yet").isEqualTo(400);
        MockHttpServletResponse imported = importPackage(parts.zip(), key.jwks());
        assertThat(imported.getStatus()).as(imported.getContentAsString()).isEqualTo(201);
        JsonNode rp = body(imported);
        String receivedId = rp.get("id").asString();
        assertThat(rp.get("signatureStatus").asString()).isEqualTo("valid");
        assertThat(rp.get("keySource").asString()).isEqualTo("pinned");
        assertThat(rp.get("hashesVerified").asBoolean()).isTrue();
        assertThat(rp.get("receivedVia").asString()).isEqualTo("import");
        assertThat(rp.get("tlp").asString()).isEqualTo("AMBER");
        assertThat(rp.get("sender").get("instanceId").asString()).isEqualTo(PEER_INSTANCE);
        assertThat(rp.get("objectRef").asString()).startsWith("remote:" + PEER_INSTANCE + "/report:");
        assertThat(rp.get("recipients").get(0).asString()).isEqualTo("bob");
        assertThat(senderKeys.findById(PEER_INSTANCE)).isPresent();

        assertThat(call(bob.bearer(), get("/v1/exchange/received/" + receivedId)).getStatus()).isEqualTo(200);
        assertThat(call(carol.bearer(), get("/v1/exchange/received/" + receivedId)).getStatus()).isEqualTo(404);
        assertThat(body(call(bob.bearer(), get("/v1/exchange/received"))).get("items").size()).isEqualTo(1);
        assertThat(body(call(carol.bearer(), get("/v1/exchange/received"))).get("items").size()).isZero();
        assertThat(inboxEvents(bob.id())).contains(MeEvents.PACKAGE_RECEIVED);

        String html = call(bob.bearer(), get("/v1/exchange/received/" + receivedId + "/rendition")
                .param("format", "html")).getContentAsString();
        assertThat(html).contains("Received from Peer A").contains("TLP:AMBER").doesNotContain("<script>");
        JsonNode bundle = body(call(bob.bearer(), get("/v1/exchange/received/" + receivedId + "/rendition")
                .param("format", "json")));
        assertThat(bundle.get("origin").get("readOnly").asBoolean()).isTrue();
        assertThat(bundle.get("origin").get("sender").get("instanceId").asString()).isEqualTo(PEER_INSTANCE);

        String remoteSituation = ObjectRef.remote(PEER_INSTANCE, "situation:" + situationId).format();
        Optional<ObjectEnvelope> seen = objects.find(ObjectRef.parse(remoteSituation),
                new ResolveContext(bob.caller(), null));
        assertThat(seen).isPresent();
        assertThat(seen.get().tlp()).isEqualTo("AMBER");
        assertThat(seen.get().summary()).startsWith("Received from");
        assertThat(objects.find(ObjectRef.parse(remoteSituation), new ResolveContext(carol.caller(), null))).isEmpty();
        assertThat(call(bob.bearer(), get(remotePath(remoteSituation))).getStatus()).isEqualTo(200);
        assertThat(receivedObjects.findByReceivedId(receivedId)).hasSize(2);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM evidence_group", Long.class))
                .as("imported material never enters local evidence").isZero();

        MockHttpServletResponse again = importPackage(parts.zip(), null);
        assertThat(again.getStatus()).isEqualTo(409);
        assertThat(code(again)).isEqualTo(ErrorCodes.PACKAGE_ALREADY_IMPORTED);
    }

    @Test
    @DisplayName("a RED package is visible to its named recipients only, not even to admins")
    void redVisibility() throws Exception {
        Person bob = person("bob-red", Scope.READ, null);
        ExchangeTestPackages.Key key = ExchangeTestPackages.newKey();
        ExchangeTestPackages.Parts parts = ExchangeTestPackages.report(key, PEER_INSTANCE, identity.instanceId(),
                List.of("bob-red"), "RED", "01JA8Q3V7S2K9W4T6Y1B5N0M3D");
        String receivedId = body(importPackage(parts.zip(), key.jwks())).get("id").asString();
        assertThat(call(bob.bearer(), get("/v1/exchange/received/" + receivedId)).getStatus()).isEqualTo(200);
        assertThat(call(admin, get("/v1/exchange/received/" + receivedId)).getStatus()).isEqualTo(404);

        ExchangeTestPackages.Parts everyone = ExchangeTestPackages.report(key, PEER_INSTANCE, identity.instanceId(),
                null, "RED", "01JA8Q3V7S2K9W4T6Y1B5N0M3E");
        MockHttpServletResponse refused = importPackage(everyone.zip(), key.jwks());
        assertThat(refused.getStatus()).isEqualTo(422);
        assertThat(code(refused)).isEqualTo(ErrorCodes.PACKAGE_RECIPIENT_UNKNOWN);

        ExchangeTestPackages.Parts green = ExchangeTestPackages.report(key, PEER_INSTANCE, identity.instanceId(),
                null, "GREEN", "01JA8Q3V7S2K9W4T6Y1B5N0M3F");
        String greenId = body(importPackage(green.zip(), key.jwks())).get("id").asString();
        Person anyone = person("anyone-red", Scope.READ, null);
        assertThat(call(anyone.bearer(), get("/v1/exchange/received/" + greenId)).getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("a peer delivers to the inbox with its ingest key; unknown recipients and tampering are refused")
    void peerInbox() throws Exception {
        Person bob = person("bob-inbox", Scope.READ, null);
        ExchangeTestPackages.Key key = ExchangeTestPackages.newKey();
        peerService.create(JSON.readTree("{\"id\":\"peer-a\",\"name\":\"Peer A\",\"baseUrl\":"
                + "\"http://192.168.1.70:8080\",\"mode\":\"RECEIVE\"}"));
        createdPeers.add("peer-a");
        PeerEntity peer = peerRows.findById("peer-a").orElseThrow();
        peer.setPinned(key.kid(), key.x());
        peer.setRemoteInstanceId(PEER_INSTANCE);
        peerRows.save(peer);
        String ingestKey = String.valueOf(peerService.rotateIngestKey("peer-a").get("key"));

        ExchangeTestPackages.Parts parts = ExchangeTestPackages.report(key, PEER_INSTANCE, identity.instanceId(),
                List.of("bob-inbox"), "AMBER", "01JA8Q3V7S2K9W4T6Y1B5N0M3G");
        assertThat(call(null, post("/v1/exchange/inbox").contentType(ExchangeVocabulary.MEDIA_TYPE)
                .content(parts.zip())).getStatus()).isEqualTo(401);
        MockHttpServletResponse delivered = call("Bearer " + ingestKey, post("/v1/exchange/inbox")
                .contentType(ExchangeVocabulary.MEDIA_TYPE).content(parts.zip()));
        assertThat(delivered.getStatus()).as(delivered.getContentAsString()).isEqualTo(202);
        JsonNode receipt = body(delivered);
        assertThat(receipt.get("type").asString()).isEqualTo("delivered");
        assertThat(receipt.get("receiverInstanceId").asString()).isEqualTo(identity.instanceId());
        assertThat(receipt.get("packageId").asString()).isEqualTo(parts.manifest().get("packageId"));

        ExchangeTestPackages.Parts unknown = ExchangeTestPackages.report(key, PEER_INSTANCE, identity.instanceId(),
                List.of("nobody-here"), "AMBER", "01JA8Q3V7S2K9W4T6Y1B5N0M3H");
        MockHttpServletResponse unknownRecipient = call("Bearer " + ingestKey, post("/v1/exchange/inbox")
                .contentType(ExchangeVocabulary.MEDIA_TYPE).content(unknown.zip()));
        assertThat(unknownRecipient.getStatus()).isEqualTo(422);

        ExchangeTestPackages.Parts fresh = ExchangeTestPackages.report(key, PEER_INSTANCE, identity.instanceId(),
                List.of("bob-inbox"), "AMBER", "01JA8Q3V7S2K9W4T6Y1B5N0M3J");
        Map<String, Object> tamperedContents = new LinkedHashMap<>(fresh.contents());
        tamperedContents.put(ExchangeVocabulary.RENDITION_MD, "# Changed\n".getBytes(StandardCharsets.UTF_8));
        byte[] tampered = PackageCodec.write(fresh.manifestBytes(), fresh.signature(), tamperedContents);
        MockHttpServletResponse bad = call("Bearer " + ingestKey, post("/v1/exchange/inbox")
                .contentType(ExchangeVocabulary.MEDIA_TYPE).content(tampered));
        assertThat(bad.getStatus()).isEqualTo(400);
        assertThat(code(bad)).isEqualTo(ErrorCodes.PACKAGE_SIGNATURE_INVALID);

        ExchangeTestPackages.Key impostor = ExchangeTestPackages.newKey();
        ExchangeTestPackages.Parts forged = ExchangeTestPackages.sign(impostor, fresh.manifest(), fresh.contents());
        assertThat(call("Bearer " + ingestKey, post("/v1/exchange/inbox").contentType(ExchangeVocabulary.MEDIA_TYPE)
                .content(forged.zip())).getStatus()).isEqualTo(400);

        String receivedId = body(call(bob.bearer(), get("/v1/exchange/received"))).get("items").get(0).get("id")
                .asString();
        MockHttpServletResponse ack = call(bob.bearer(), post("/v1/exchange/received/" + receivedId
                + "/acknowledge"));
        assertThat(ack.getStatus()).isEqualTo(200);
        assertThat(body(ack).get("type").asString()).isEqualTo("acknowledged");
        assertThat(body(ack).get("principalHandle").asString()).isEqualTo("bob-inbox");
        assertThat(body(call(bob.bearer(), get("/v1/exchange/received/" + receivedId))).get("acknowledgedAt")
                .isNull()).isFalse();
        assertThat(body(call(bob.bearer(), get("/v1/exchange/received").param("acknowledged", "false")))
                .get("items").size()).isZero();
    }

    @Test
    @DisplayName("a package built here verifies against this instance's JWKS; receipts update its delivery state")
    void builtHereAndReceipts() throws Exception {
        Person jo = person("jo-pkg", Scope.AGENT, null);
        ReportEntity r = report(jo, "{'title':'Outbound report','blocks':[{'type':'markdown','markdown':'Body.'}]}");
        MockHttpServletResponse unpublished = call(jo.bearer(), json(post("/v1/exchange/packages"), "{'objectRef':"
                + "'report:" + r.getId() + "','recipients':[{'instanceId':'fs-peer-b','principals':['xavier']}],"
                + "'deliver':'file'}"));
        assertThat(unpublished.getStatus()).isEqualTo(409);
        publish(jo, r);

        MockHttpServletResponse star = call(jo.bearer(), json(post("/v1/exchange/packages"), "{'objectRef':'report:"
                + r.getId() + "','recipients':'*','deliver':'file'}"));
        assertThat(star.getStatus()).isEqualTo(403);
        assertThat(code(star)).isEqualTo(ErrorCodes.TLP_VIOLATION);

        MockHttpServletResponse created = call(jo.bearer(), json(post("/v1/exchange/packages"), "{'objectRef':"
                + "'report:" + r.getId() + "','recipients':[{'instanceId':'fs-peer-b','principals':['xavier']}],"
                + "'message':'For review','deliver':'file'}"));
        assertThat(created.getStatus()).as(created.getContentAsString()).isEqualTo(202);
        JsonNode pkg = body(created);
        assertThat(pkg.get("status").asString()).isEqualTo("ready");
        assertThat(pkg.get("manifest").get("sender").get("instanceId").asString()).isEqualTo(identity.instanceId());
        assertThat(pkg.get("manifest").get("authors").get(0).get("handle").asString()).isEqualTo("jo-pkg");
        assertThat(pkg.get("manifest").get("provenanceChain").get(0).get("role").asString()).isEqualTo("origin");
        String packageId = pkg.get("id").asString();

        MockHttpServletResponse download = call(jo.bearer(), get("/v1/exchange/packages/" + packageId
                + "/download"));
        assertThat(download.getStatus()).isEqualTo(200);
        assertThat(download.getContentType()).startsWith(ExchangeVocabulary.MEDIA_TYPE);
        PackageCodec.Contents read = PackageCodec.read(download.getContentAsByteArray(), 100_000_000);
        @SuppressWarnings("unchecked")
        Map<String, Object> jwks = (Map<String, Object>) signingKeys.jwks();
        assertThat(PackageVerifier.verify(read.manifestBytes(), read.signature(), jwks).valid()).isTrue();
        Map<?, ?> hashes = (Map<?, ?>) JSON.readValue(read.manifestBytes(), Map.class).get("contentHashes");
        assertThat(PackageCodec.verifyHashes(hashes, read.contents())).isEmpty();
        assertThat(read.contents()).containsKeys(ExchangeVocabulary.BUNDLE, ExchangeVocabulary.RENDITION_MD,
                ExchangeVocabulary.RENDITION_HTML);

        MockHttpServletResponse loop = importPackage(download.getContentAsByteArray(), null);
        assertThat(loop.getStatus()).as("our own package never comes back").isEqualTo(409);
        assertThat(code(loop)).isEqualTo(ErrorCodes.PACKAGE_LOOP);

        peerService.create(JSON.readTree("{\"id\":\"peer-b\",\"name\":\"Peer B\",\"baseUrl\":"
                + "\"http://127.0.0.1:9\",\"mode\":\"RECEIVE\",\"exchangeKey\":\"fsp_test-exchange-key\"}"));
        createdPeers.add("peer-b");
        PeerEntity peer = peerRows.findById("peer-b").orElseThrow();
        peer.setRemoteInstanceId("fs-peer-b");
        peerRows.save(peer);
        MockHttpServletResponse viaPeer = call(jo.bearer(), json(post("/v1/exchange/packages"), "{'objectRef':"
                + "'report:" + r.getId() + "','recipients':[{'instanceId':'fs-peer-b','principals':['xavier']}]}"));
        assertThat(viaPeer.getStatus()).isEqualTo(202);
        JsonNode delivering = body(viaPeer);
        assertThat(delivering.get("deliveries").get(0).get("peerId").asString()).isEqualTo("peer-b");
        assertThat(delivering.get("deliveries").get(0).get("status").asString()).isEqualTo("failed");
        String deliveredId = delivering.get("id").asString();

        String ingestKey = String.valueOf(peerService.rotateIngestKey("peer-b").get("key"));
        MockHttpServletResponse receipt = call("Bearer " + ingestKey, json(post("/v1/exchange/receipts"),
                "{'packageId':'" + deliveredId + "','type':'acknowledged','receiverInstanceId':'fs-peer-b',"
                        + "'principalHandle':'xavier','at':'2026-10-12T13:00:00Z'}"));
        assertThat(receipt.getStatus()).isEqualTo(204);
        JsonNode after = body(call(jo.bearer(), get("/v1/exchange/packages/" + deliveredId)));
        assertThat(after.get("deliveries").get(0).get("status").asString()).isEqualTo("acknowledged");
        assertThat(call("Bearer " + ingestKey, json(post("/v1/exchange/receipts"), "{'packageId':'"
                + "01JA8Q3V7S2K9W4T6Y1B5N0M3C','type':'delivered','receiverInstanceId':'fs-peer-b',"
                + "'at':'2026-10-12T13:00:00Z'}")).getStatus()).isEqualTo(404);

        JsonNode sent = body(call(jo.bearer(), get("/v1/exchange/sent")));
        assertThat(sent.get("items").size()).isEqualTo(2);
        Person other = person("other-pkg", Scope.AGENT, null);
        assertThat(call(other.bearer(), get("/v1/exchange/packages/" + packageId)).getStatus()).isEqualTo(404);
        assertThat(body(call(other.bearer(), get("/v1/exchange/sent"))).get("items").size()).isZero();
    }

    @Test
    @DisplayName("a received package is forwarded with the original authors plus the forwarder, never back to its origin")
    void forwardingAndLoops() throws Exception {
        Person bob = person("bob-fwd", Scope.AGENT, null);
        ExchangeTestPackages.Key key = ExchangeTestPackages.newKey();
        ExchangeTestPackages.Parts parts = ExchangeTestPackages.report(key, PEER_INSTANCE, identity.instanceId(),
                List.of("bob-fwd"), "GREEN", "01JA8Q3V7S2K9W4T6Y1B5N0M3K");
        String remoteRef = body(importPackage(parts.zip(), key.jwks())).get("objectRef").asString();

        MockHttpServletResponse back = call(bob.bearer(), json(post("/v1/exchange/packages"), "{'objectRef':'"
                + remoteRef + "','recipients':[{'instanceId':'" + PEER_INSTANCE + "','principals':['alice']}],"
                + "'deliver':'file'}"));
        assertThat(back.getStatus()).isEqualTo(409);
        assertThat(code(back)).isEqualTo(ErrorCodes.PACKAGE_LOOP);

        MockHttpServletResponse forward = call(bob.bearer(), json(post("/v1/exchange/packages"), "{'objectRef':'"
                + remoteRef + "','recipients':[{'instanceId':'fs-peer-c','principals':['dana']}],'deliver':'file'}"));
        assertThat(forward.getStatus()).as(forward.getContentAsString()).isEqualTo(202);
        JsonNode manifest = body(forward).get("manifest");
        assertThat(manifest.get("authors").get(0).get("handle").asString()).isEqualTo("alice");
        JsonNode lastAuthor = manifest.get("authors").get(manifest.get("authors").size() - 1);
        assertThat(lastAuthor.get("handle").asString()).isEqualTo("bob-fwd");
        assertThat(lastAuthor.get("role").asString()).isEqualTo("forwarder");
        JsonNode chain = manifest.get("provenanceChain");
        assertThat(chain.get(0).get("instanceId").asString()).isEqualTo(PEER_INSTANCE);
        assertThat(chain.get(chain.size() - 1).get("role").asString()).isEqualTo("forwarder");
        assertThat(manifest.get("tlp").asString()).isEqualTo("GREEN");
        assertThat(manifest.get("objectRef").asString()).startsWith("report:");
    }
}
