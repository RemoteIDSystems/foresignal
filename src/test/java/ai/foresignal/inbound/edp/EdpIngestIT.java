package ai.foresignal.inbound.edp;

import static ai.foresignal.testsupport.inbound.InboundIt.body;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.inbound.EdpConformance;
import ai.foresignal.testsupport.inbound.InboundIt;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * The External Data Protocol core end to end (prompt 024): registration by the operator and by
 * registration token, approval (nothing stored while pending, EXT-1), pushes of every item type as
 * JSON and NDJSON, per-item rejections, idempotent repeats, deletes, status items as health only,
 * atomic kinds hidden from the layer index, manifest updates, suspension and deletion.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
class EdpIngestIT {

    /** The receipt time the conformance fixtures are written against. */
    static final Instant T = Instant.parse("2026-10-11T18:00:00Z");
    static final MediaType NDJSON = MediaType.parseMediaType("application/x-ndjson");
    static final String HARBOR = "ext-harbor-cams";
    static final String HORMUZ = "ext-hormuz-transits";

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired AppSettingService settings;
    @Autowired MutableClock clock;
    @Autowired SourceRegistry registry;
    @Autowired SourceService sources;
    @Autowired JdbcTemplate jdbc;

    String admin;
    InboundIt it;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        settings.invalidateCache();
        admin = "Bearer " + adminTokens.rotate();
        it = new InboundIt(mvc, admin);
    }

    /** Registers {@code manifest} through the operator endpoint; returns the ingest key. */
    String register(String manifest, boolean approve) throws Exception {
        MockHttpServletResponse response = it.perform(post("/v1/external/sources").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"manifest\":" + manifest + ",\"approve\":" + approve + "}"));
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(201);
        return body(response).get("ingestKey").asString();
    }

    MockHttpServletResponse push(String sourceId, String key, MediaType type, String batch) throws Exception {
        return it.push("/v1/external/sources/" + sourceId + "/items", key, type, batch);
    }

    int heads(String sourceId) {
        return jdbc.queryForObject("select count(*) from record_head where source_id = ?", Integer.class, sourceId);
    }

    @Test
    @DisplayName("a pending source answers 409 and stores nothing; after approval every layer item type lands as a record")
    void pendingThenApproved() throws Exception {
        String key = register(EdpConformance.manifest("push-layer.json"), false);
        JsonNode pending = body(it.perform(get("/v1/external/sources/" + HARBOR).header(HttpHeaders.AUTHORIZATION, admin)));
        assertThat(pending.get("state").asString()).isEqualTo("pending");
        assertThat(pending.get("access").get("route").asString()).isEqualTo("official");
        assertThat(pending.get("egress").asString()).isEqualTo("OPEN");
        assertThat(pending.get("presentation").get("cameras").get("color").asString()).isEqualTo("#1971c2");

        MockHttpServletResponse refused = push(HARBOR, key, MediaType.APPLICATION_JSON, EdpConformance.batch("feature.json"));
        assertThat(refused.getStatus()).isEqualTo(409);
        assertThat(body(refused).get("code").asString()).isEqualTo("SOURCE_PENDING_APPROVAL");
        assertThat(heads(HARBOR)).isZero();

        MockHttpServletResponse approved = it.perform(post("/v1/external/sources/" + HARBOR + "/approve")
                .header(HttpHeaders.AUTHORIZATION, admin));
        assertThat(approved.getStatus()).isEqualTo(200);
        assertThat(body(approved).get("state").asString()).isEqualTo("active");
        assertThat(body(approved).get("approvedBy").asString()).isNotBlank();

        for (String batch : new String[] {"feature.json", "document.json", "forecast.json"}) {
            MockHttpServletResponse response = push(HARBOR, key, MediaType.APPLICATION_JSON, EdpConformance.batch(batch));
            assertThat(response.getStatus()).as(batch + ": " + response.getContentAsString()).isEqualTo(200);
            JsonNode result = body(response);
            assertThat(result.get("rejected")).as(batch).isEmpty();
            assertThat(result.get("outcome").get("persisted").asInt()).as(batch).isPositive();
        }
        assertThat(jdbc.queryForList("select external_id from record_head where source_id = ? order by external_id",
                String.class, HARBOR)).containsExactly("cameras:cam-1", "notes:n-1", "odds:f-1", "odds:f-2");
        String payload = jdbc.queryForObject("select payload::text from record_head where source_id = ? and"
                + " external_id = 'notes:n-1'", String.class, HARBOR);
        assertThat(payload).contains("\"mode\": \"atomic\"", "\"originator\": \"example-port-watch\"", "documentType");

        JsonNode view = body(it.perform(get("/v1/external/sources/" + HARBOR).header(HttpHeaders.AUTHORIZATION, admin)));
        assertThat(view.get("streams").findValues("items").stream().mapToLong(JsonNode::asLong).sum()).isEqualTo(4);
        assertThat(view.get("lastItemAt").isNull()).isFalse();
        assertThat(registry.descriptor(HARBOR).egress()).isEqualTo(Egress.OPEN);
    }

    @Test
    @DisplayName("NDJSON and JSON pushes of events, observations and deletes; repeats are touches; status items only change health")
    void hormuzItems() throws Exception {
        String key = register(EdpConformance.manifest("hormuz-example.json"), true);

        MockHttpServletResponse ndjson = push(HORMUZ, key, NDJSON, EdpConformance.batch("event-and-delete.ndjson"));
        assertThat(ndjson.getStatus()).as(ndjson.getContentAsString()).isEqualTo(200);
        assertThat(body(ndjson).get("accepted").asInt()).isEqualTo(2);

        MockHttpServletResponse observations = push(HORMUZ, key, MediaType.APPLICATION_JSON,
                EdpConformance.batch("observation.json"));
        assertThat(body(observations).get("accepted").asInt()).isEqualTo(2);
        String observation = jdbc.queryForObject("select payload::text from record_head where source_id = ? and"
                + " external_id = 'transits:tanker:2026-10-10'", String.class, HORMUZ);
        assertThat(observation).contains("\"seriesKey\": \"tanker\"", "effectiveKnownAt");

        clock.setInstant(T.plusSeconds(3_700));
        JsonNode repeat = body(push(HORMUZ, key, MediaType.APPLICATION_JSON, EdpConformance.batch("event.json")));
        assertThat(repeat.get("outcome").get("persisted").asInt()).isZero();
        assertThat(repeat.get("outcome").get("touched").asInt()).isEqualTo(1);

        JsonNode deleted = body(push(HORMUZ, key, MediaType.APPLICATION_JSON,
                EdpConformance.batch("event.json").replace("\"type\": \"event\"", "\"type\": \"delete\"")));
        assertThat(deleted.get("outcome").get("byReason").get("TOMBSTONE").asInt()).isEqualTo(1);

        int before = heads(HORMUZ);
        MockHttpServletResponse status = push(HORMUZ, key, MediaType.APPLICATION_JSON, EdpConformance.batch("status.json"));
        assertThat(status.getStatus()).isEqualTo(200);
        assertThat(heads(HORMUZ)).isEqualTo(before);
        assertThat(sources.view(HORMUZ).health().status()).isIn(HealthStatus.DEGRADED, HealthStatus.FAILING);
    }

    @Test
    @DisplayName("per-item validation errors are returned with schema errors; a batch for another source is 400 SOURCE_MISMATCH")
    void rejections() throws Exception {
        String key = register(EdpConformance.manifest("hormuz-example.json"), true);
        MockHttpServletResponse response = push(HORMUZ, key, MediaType.APPLICATION_JSON,
                EdpConformance.invalid("event-payload.json"));
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(200);
        JsonNode result = body(response);
        assertThat(result.get("accepted").asInt()).isZero();
        assertThat(result.get("rejected").get(0).get("reason").asString()).isEqualTo("PAYLOAD_INVALID");
        assertThat(result.get("rejected").get(0).get("schemaErrors")).isNotEmpty();
        assertThat(heads(HORMUZ)).isZero();

        MockHttpServletResponse mismatch = push(HORMUZ, key, MediaType.APPLICATION_JSON,
                "{\"protocol\":\"foresignal-edp/1.0\",\"sourceId\":\"someone-else\",\"items\":[]}");
        assertThat(mismatch.getStatus()).isEqualTo(400);
        assertThat(body(mismatch).get("code").asString()).isEqualTo("SOURCE_MISMATCH");

        MockHttpServletResponse future = push(HORMUZ, key, MediaType.APPLICATION_JSON,
                "{\"protocol\":\"foresignal-edp/1.1\",\"items\":[]}");
        assertThat(future.getStatus()).isEqualTo(400);
        assertThat(body(future).get("code").asString()).isEqualTo("PROTOCOL_UNSUPPORTED");

        JsonNode view = body(it.perform(get("/v1/external/sources/" + HORMUZ).header(HttpHeaders.AUTHORIZATION, admin)));
        assertThat(view.get("lastValidationErrors")).isNotEmpty();
    }

    @Test
    @DisplayName("a registration token registers a provider pending, once; outside its prefix is 409; reuse is 401")
    void registrationTokens() throws Exception {
        MockHttpServletResponse created = it.perform(post("/v1/external/registration-tokens")
                .header(HttpHeaders.AUTHORIZATION, admin).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"river\",\"sourceIdPrefix\":\"river\"}"));
        assertThat(created.getStatus()).isEqualTo(201);
        String token = body(created).get("token").asString();
        assertThat(token).startsWith("fsx_");

        MockHttpServletResponse registered = it.perform(post("/v1/external/register")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                .content(EdpConformance.manifest("pull-atomic.json")));
        assertThat(registered.getStatus()).as(registered.getContentAsString()).isEqualTo(201);
        assertThat(body(registered).get("source").get("state").asString()).isEqualTo("pending");
        assertThat(body(registered).get("source").get("registeredVia").asString()).isEqualTo("registration-token");
        assertThat(body(registered).get("ingestKey").asString()).startsWith("eik_");

        MockHttpServletResponse reused = it.perform(post("/v1/external/register")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                .content(EdpConformance.manifest("push-layer.json")));
        assertThat(reused.getStatus()).isEqualTo(401);
        assertThat(body(reused).get("code").asString()).isEqualTo("REGISTRATION_TOKEN_INVALID");

        String other = body(it.perform(post("/v1/external/registration-tokens").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"x\",\"sourceIdPrefix\":\"river\",\"maxUses\":2}")))
                .get("token").asString();
        MockHttpServletResponse outside = it.perform(post("/v1/external/register")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + other).contentType(MediaType.APPLICATION_JSON)
                .content(EdpConformance.manifest("push-layer.json")));
        assertThat(outside.getStatus()).isEqualTo(409);
        assertThat(body(outside).get("code").asString()).isEqualTo("SOURCE_ID_NOT_ALLOWED");

        JsonNode tokens = body(it.perform(get("/v1/external/registration-tokens").header(HttpHeaders.AUTHORIZATION, admin)));
        assertThat(tokens.toString()).doesNotContain(token);
        assertThat(tokens.findValues("uses").stream().map(JsonNode::asInt).toList()).contains(1, 0);
        String id = tokens.get(0).get("id").asString();
        assertThat(it.perform(delete("/v1/external/registration-tokens/" + id).header(HttpHeaders.AUTHORIZATION, admin))
                .getStatus()).isEqualTo(204);
        assertThat(it.perform(post("/v1/external/register").contentType(MediaType.APPLICATION_JSON)
                .content(EdpConformance.manifest("push-layer.json"))).getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("atomic kinds are hidden from the tile layer index but stay queryable; layer kinds carry their presentation")
    void atomicKindsHidden() throws Exception {
        String key = register(EdpConformance.manifest("push-layer.json"), true);
        push(HARBOR, key, MediaType.APPLICATION_JSON, EdpConformance.batch("document.json"));

        JsonNode index = body(it.perform(get("/v1/tiles.json").header(HttpHeaders.AUTHORIZATION, admin)));
        assertThat(index.findValuesAsString("id")).contains("harbor-camera").doesNotContain("harbor-note", "harbor-forecast");
        JsonNode tileJson = body(it.perform(get("/v1/tiles/harbor-camera.json").header(HttpHeaders.AUTHORIZATION, admin)));
        assertThat(tileJson.get("foresignal:presentation").get("icon").asString()).isEqualTo("camera");
        JsonNode records = body(it.perform(get("/v1/records").param("kind", "harbor-note")
                .header(HttpHeaders.AUTHORIZATION, admin)));
        assertThat(records.findValuesAsString("externalId")).contains("notes:n-1");
    }

    @Test
    @DisplayName("manifest updates: additive changes apply at once, breaking ones return the source to pending")
    void manifestUpdates() throws Exception {
        String key = register(EdpConformance.manifest("push-layer.json"), true);
        ObjectNode manifest = (ObjectNode) InboundIt.JSON.readTree(EdpConformance.manifest("push-layer.json"));
        ((ObjectNode) manifest.get("source")).put("description", "Now with a description");

        MockHttpServletResponse additive = it.perform(put("/v1/external/sources/" + HARBOR + "/manifest")
                .header("X-Foresignal-Ingest-Key", key).contentType(MediaType.APPLICATION_JSON).content(manifest.toString()));
        assertThat(additive.getStatus()).as(additive.getContentAsString()).isEqualTo(200);
        assertThat(body(additive).get("state").asString()).isEqualTo("active");
        assertThat(body(additive).get("breakingChanges")).isEmpty();
        assertThat(registry.descriptor(HARBOR).description()).isEqualTo("Now with a description");

        ((ObjectNode) manifest.get("streams").get(1)).put("kind", "harbor-memo");
        MockHttpServletResponse breaking = it.perform(put("/v1/external/sources/" + HARBOR + "/manifest")
                .header(HttpHeaders.AUTHORIZATION, admin).contentType(MediaType.APPLICATION_JSON).content(manifest.toString()));
        assertThat(breaking.getStatus()).isEqualTo(200);
        assertThat(body(breaking).get("state").asString()).isEqualTo("pending");
        assertThat(body(breaking).get("breakingChanges").toString()).contains("kind");
        assertThat(push(HARBOR, key, MediaType.APPLICATION_JSON, EdpConformance.batch("feature.json")).getStatus())
                .isEqualTo(409);
        assertThat(sources.view(HARBOR).enabled()).isFalse();
    }

    @Test
    @DisplayName("suspend refuses pushes with SOURCE_SUSPENDED; delete removes the provider and its custom source")
    void suspendAndDelete() throws Exception {
        String key = register(EdpConformance.manifest("push-layer.json"), true);
        assertThat(it.perform(post("/v1/external/sources/" + HARBOR + "/suspend").header(HttpHeaders.AUTHORIZATION, admin))
                .getStatus()).isEqualTo(200);
        MockHttpServletResponse suspended = push(HARBOR, key, MediaType.APPLICATION_JSON, EdpConformance.batch("feature.json"));
        assertThat(body(suspended).get("code").asString()).isEqualTo("SOURCE_SUSPENDED");

        assertThat(it.perform(delete("/v1/external/sources/" + HARBOR).header(HttpHeaders.AUTHORIZATION, admin)).getStatus())
                .isEqualTo(204);
        assertThat(registry.contains(HARBOR)).isFalse();
        MockHttpServletResponse gone = it.perform(get("/v1/external/sources/" + HARBOR).header(HttpHeaders.AUTHORIZATION, admin));
        assertThat(gone.getStatus()).isEqualTo(404);
        assertThat(body(gone).get("code").asString()).isEqualTo("UNKNOWN_EXTERNAL_SOURCE");
    }

    @Test
    @DisplayName("the protocol description and schemas are open; validation is a dry run; an invalid manifest is MANIFEST_INVALID")
    void protocolAndValidation() throws Exception {
        JsonNode protocol = body(it.perform(get("/v1/external/protocol")));
        assertThat(protocol.get("protocol").asString()).isEqualTo("foresignal-edp/1.0");
        assertThat(protocol.get("maxMinor").asInt()).isZero();
        MockHttpServletResponse schema = it.perform(get("/v1/external/protocol/schemas/manifest"));
        assertThat(schema.getStatus()).isEqualTo(200);
        assertThat(schema.getContentType()).startsWith("application/schema+json");
        assertThat(it.perform(get("/v1/external/protocol/schemas/nope")).getStatus()).isEqualTo(404);

        JsonNode report = body(it.perform(post("/v1/external/validate").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"manifest\":"
                        + EdpConformance.text("manifests/invalid/duplicate-stream-ids.json") + "}")));
        assertThat(report.get("manifestValid").asBoolean()).isFalse();
        assertThat(report.get("manifestErrors")).isNotEmpty();
        JsonNode items = body(it.perform(post("/v1/external/validate").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"manifest\":" + EdpConformance.manifest("push-layer.json")
                        + ",\"items\":" + InboundIt.JSON.readTree(EdpConformance.batch("forecast.json")).get("items") + "}")));
        assertThat(items.get("itemsValid").asInt()).isEqualTo(2);
        assertThat(jdbc.queryForObject("select count(*) from external_source", Integer.class)).isZero();

        MockHttpServletResponse invalid = it.perform(post("/v1/external/sources").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"manifest\":"
                        + EdpConformance.text("manifests/invalid/empty-transport.json") + "}"));
        assertThat(invalid.getStatus()).isEqualTo(400);
        assertThat(body(invalid).get("code").asString()).isEqualTo("MANIFEST_INVALID");
        MockHttpServletResponse unsupported = it.perform(post("/v1/external/sources").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"manifest\":"
                        + EdpConformance.text("manifests/invalid/protocol-too-new.json") + "}"));
        assertThat(body(unsupported).get("code").asString()).isEqualTo("PROTOCOL_UNSUPPORTED");
    }
}
