package ai.foresignal.intel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.security.TestTokens;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The prompt 101 event, recipe and exposure endpoints over HTTP: CRUD with the evidence/inference/
 * speculation sections (INT-4), scopes, the optimistic-lock 409, validated patches, server-derived
 * member families and weights (INT-3), member identity checks, merges that share members, member
 * removal on the {@code intel} stream, recipe dry runs, and the private-event rule on every path
 * (EGR-1).
 */
@IntegrationTest
@Import(TestIngestSources.class)
class IntelControllersIT {

    private static final String STANDARD = TestIngestSources.STANDARD;

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper json;
    @Autowired SourceVisibility sources;
    @Autowired IntelEventBus bus;
    @Autowired CorrelationRecipeRepository recipes;

    String read;
    String readPrivate;
    String agent;
    String admin;

    @BeforeEach
    void setUp() {
        clock.setInstant(Instant.parse("2026-03-01T12:00:00Z"));
        read = TestTokens.bearer(TestTokens.create(tokens, "intel-read", Scope.READ));
        readPrivate = TestTokens.bearer(TestTokens.create(tokens, "intel-read-private", Scope.READ, true));
        agent = TestTokens.bearer(TestTokens.create(tokens, "intel-agent", Scope.AGENT));
        admin = TestTokens.bearer(TestTokens.create(tokens, "intel-admin", Scope.ADMIN));
    }

    @AfterEach
    void restore() {
        sources.setOperatorPrivate(STANDARD, false);
        sources.refresh();
    }

    private long seed(String externalId) {
        Instant now = clock.instant();
        records.ingestAt(STANDARD, now, NormalizedRecord.builder(externalId, "test-record").observedAt(now)
                .point(-95.0, 32.0).put("name", externalId).build());
        return jdbc.queryForObject("select max(seq) from record_version where source_id = ? and external_id = ?",
                Long.class, STANDARD, externalId);
    }

    private static String member(String externalId, long seq) {
        return "{\"memberType\":\"record\",\"sourceId\":\"" + STANDARD + "\",\"externalId\":\"" + externalId
                + "\",\"seq\":" + seq + "}";
    }

    private MockHttpServletResponse call(String bearer, MockHttpServletRequestBuilder request) throws Exception {
        if (bearer != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, bearer);
        }
        return mvc.perform(request).andReturn().getResponse();
    }

    private MockHttpServletResponse send(String bearer, MockHttpServletRequestBuilder request, String body)
            throws Exception {
        return call(bearer, request.contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private JsonNode body(MockHttpServletResponse response) throws Exception {
        return json.readTree(response.getContentAsString(StandardCharsets.UTF_8));
    }

    private String create(String bearer, String body) throws Exception {
        MockHttpServletResponse response = send(bearer, post("/v1/intel/events"), body);
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(201);
        return body(response).get("id").asString();
    }

    @Test
    @DisplayName("an agent event is created from a verified member and read back in three sections")
    void eventCrud() throws Exception {
        long seq = seed("e1");
        String id = create(agent, "{\"type\":\"custom\",\"title\":\"Finding\",\"members\":[" + member("e1", seq) + "]}");

        JsonNode event = body(call(read, get("/v1/intel/events/" + id)));
        assertThat(event.has("evidence")).isTrue();
        assertThat(event.has("inference")).isTrue();
        assertThat(event.has("speculation")).isTrue();
        assertThat(event.get("private").asBoolean()).isFalse();
        assertThat(body(call(read, get("/v1/intel/events"))).get("items").toString()).contains(id);
    }

    @Test
    @DisplayName("writes need agent scope; reads need only read")
    void scopes() throws Exception {
        long seq = seed("s1");
        String body = "{\"type\":\"custom\",\"title\":\"Scoped\",\"members\":[" + member("s1", seq) + "]}";
        assertThat(send(read, post("/v1/intel/events"), body).getStatus()).isEqualTo(403);
        String id = create(agent, body);
        assertThat(send(read, patch("/v1/intel/events/" + id), "{\"status\":\"resolved\"}").getStatus())
                .isEqualTo(403);
        assertThat(send(read, post("/v1/intel/recipes/maritime-disruption/test"), "{\"fromSeq\":0,\"toSeq\":1}")
                .getStatus()).isEqualTo(403);
        assertThat(send(agent, org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .put("/v1/intel/recipes/maritime-disruption"), "{\"enabled\":false}").getStatus()).isEqualTo(403);
        assertThat(call(read, get("/v1/intel/events/" + id)).getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("a stale version is 409 EVENT_VERSION_CONFLICT; bad status and severity are 400")
    void patchValidation() throws Exception {
        long seq = seed("v1");
        String id = create(agent, "{\"type\":\"custom\",\"title\":\"Locked\",\"members\":[" + member("v1", seq) + "]}");
        int version = body(call(read, get("/v1/intel/events/" + id))).get("version").asInt();

        MockHttpServletResponse stale = send(agent, patch("/v1/intel/events/" + id),
                "{\"title\":\"New\",\"version\":" + (version + 7) + "}");
        assertThat(stale.getStatus()).isEqualTo(409);
        assertThat(stale.getContentAsString()).contains("EVENT_VERSION_CONFLICT");
        assertThat(send(agent, patch("/v1/intel/events/" + id), "{\"status\":\"exploded\"}").getStatus())
                .isEqualTo(400);
        assertThat(send(agent, patch("/v1/intel/events/" + id), "{\"severity\":\"extreme\"}").getStatus())
                .isEqualTo(400);
    }

    @Test
    @DisplayName("operator notes are stored apart from the LLM narrative and are not labeled speculation")
    void notesAreNotNarrative() throws Exception {
        long seq = seed("n1");
        String id = create(agent, "{\"type\":\"custom\",\"title\":\"Noted\",\"members\":[" + member("n1", seq) + "]}");
        JsonNode patched = body(send(agent, patch("/v1/intel/events/" + id), "{\"notes\":\"call the harbor master\"}"));
        assertThat(patched.get("notes").asString()).isEqualTo("call the harbor master");
        assertThat(patched.get("narrative").isNull()).isTrue();
        assertThat(patched.get("narrativeLabel").isNull()).isTrue();
    }

    @Test
    @DisplayName("a client's weight and signal family are ignored; the server derives them (INT-3)")
    void weightsAreDerived() throws Exception {
        long seq = seed("w1");
        String id = create(agent, "{\"type\":\"custom\",\"title\":\"Weighted\",\"members\":[{\"memberType\":\"record\","
                + "\"sourceId\":\"" + STANDARD + "\",\"externalId\":\"w1\",\"seq\":" + seq
                + ",\"weight\":1.0,\"signalFamily\":\"seismic\"}]}");
        JsonNode members = body(call(read, get("/v1/intel/events/" + id))).get("evidence").get("members");
        assertThat(members.get(0).get("weight").asDouble()).isEqualTo(EventScorer.DEFAULT_RELIABILITY);
        assertThat(members.get(0).get("signalFamily").asString()).isEqualTo(SignalFamilies.forKind("test-record"));
    }

    @Test
    @DisplayName("a member whose source or external id does not match the version at its seq is 400")
    void memberIdentityChecked() throws Exception {
        long seq = seed("i1");
        MockHttpServletResponse response = send(agent, post("/v1/intel/events"),
                "{\"type\":\"custom\",\"title\":\"Wrong\",\"members\":[" + member("someone-else", seq) + "]}");
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString()).contains("EVENT_MEMBER_NOT_FOUND");
    }

    @Test
    @DisplayName("merging events that share a member keeps it once")
    void mergeSharedMember() throws Exception {
        long shared = seed("m1");
        long other = seed("m2");
        String a = create(agent, "{\"type\":\"custom\",\"title\":\"A\",\"members\":[" + member("m1", shared) + "]}");
        String b = create(agent, "{\"type\":\"custom\",\"title\":\"B\",\"members\":[" + member("m1", shared) + ","
                + member("m2", other) + "]}");

        MockHttpServletResponse merged = send(agent, post("/v1/intel/events/" + b + "/merge"),
                "{\"intoId\":\"" + a + "\"}");
        assertThat(merged.getStatus()).as(merged.getContentAsString()).isEqualTo(200);
        JsonNode members = body(merged).get("evidence").get("members");
        assertThat(members).hasSize(2);
        assertThat(body(call(read, get("/v1/intel/events/" + b))).get("mergedInto").asString()).isEqualTo(a);
    }

    @Test
    @DisplayName("removing a member rescores and announces an update on the intel stream")
    void removeMemberAnnounced() throws Exception {
        long one = seed("r1");
        long two = seed("r2");
        String id = create(agent, "{\"type\":\"custom\",\"title\":\"R\",\"members\":[" + member("r1", one) + ","
                + member("r2", two) + "]}");
        long memberId = body(call(read, get("/v1/intel/events/" + id))).get("evidence").get("members").get(1)
                .get("id").asLong();
        List<IntelEvent> seen = new java.util.concurrent.CopyOnWriteArrayList<>();
        try (var subscription = bus.subscribe("intel-controllers-it", seen::add, 16)) {
            assertThat(call(agent, delete("/v1/intel/events/" + id + "/members/" + memberId)).getStatus())
                    .isEqualTo(204);
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(5))
                    .until(() -> seen.stream().anyMatch(e -> e.eventId().equals(id)
                            && IntelEvent.UPDATED.equals(e.change())));
        }
        assertThat(body(call(read, get("/v1/intel/events/" + id))).get("evidence").get("memberCount").asInt())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("a private event is hidden from callers without private access on every read path (EGR-1)")
    void privateEventHidden() throws Exception {
        sources.setOperatorPrivate(STANDARD, true);
        sources.refresh();
        long seq = seed("p1");
        // The private flag is derived from the member's source; a client cannot clear it.
        String id = create(admin, "{\"type\":\"custom\",\"title\":\"Secret\",\"private\":false,\"members\":["
                + member("p1", seq) + "]}");

        assertThat(body(call(admin, get("/v1/intel/events/" + id))).get("private").asBoolean()).isTrue();
        assertThat(call(read, get("/v1/intel/events/" + id)).getStatus()).isEqualTo(404);
        assertThat(call(agent, get("/v1/intel/events/" + id)).getStatus()).isEqualTo(404);
        assertThat(body(call(read, get("/v1/intel/events"))).get("items").toString()).doesNotContain(id);
        assertThat(send(read, post("/v1/exposure"), "{\"eventId\":\"" + id + "\",\"population\":false}").getStatus())
                .isEqualTo(404);
        assertThat(send(agent, patch("/v1/intel/events/" + id), "{\"status\":\"dismissed\"}").getStatus())
                .isEqualTo(404);
        assertThat(call(readPrivate, get("/v1/intel/events/" + id)).getStatus()).isEqualTo(200);
        assertThat(body(call(readPrivate, get("/v1/intel/events"))).get("items").toString()).contains(id);
    }

    @Test
    @DisplayName("an agent without private access cannot pin a private version")
    void privateMemberRefused() throws Exception {
        sources.setOperatorPrivate(STANDARD, true);
        sources.refresh();
        long seq = seed("p2");
        MockHttpServletResponse response = send(agent, post("/v1/intel/events"),
                "{\"type\":\"custom\",\"title\":\"Nope\",\"members\":[" + member("p2", seq) + "]}");
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString()).contains("EVENT_MEMBER_NOT_FOUND");
    }

    @Test
    @DisplayName("a recipe dry run returns would-be events and writes nothing; an unknown recipe is 404")
    void recipeTest() throws Exception {
        java.util.Map<String, Object> definition = new java.util.LinkedHashMap<>();
        definition.put("type", "custom");
        definition.put("seed", java.util.Map.of("kinds", List.of("test-record")));
        definition.put("joins", List.of());
        definition.put("minMembers", 1);
        definition.put("minSignalFamilies", 1);
        definition.put("activateAt", java.util.Map.of("members", 1));
        CorrelationRecipeEntity recipe = new CorrelationRecipeEntity("dry-run-recipe", 1, definition);
        recipe.setEnabled(false);
        recipes.save(recipe);
        long seq = seed("d1");

        JsonNode out = body(send(agent, post("/v1/intel/recipes/dry-run-recipe/test"),
                "{\"fromSeq\":" + (seq - 1) + ",\"toSeq\":" + seq + "}"));
        assertThat(out.get("events")).hasSize(1);
        assertThat(out.get("events").get(0).get("evidence").get("memberCount").asInt()).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from intelligence_event", Integer.class)).isZero();
        assertThat(send(agent, post("/v1/intel/recipes/no-such-recipe/test"), "{\"fromSeq\":0,\"toSeq\":10}")
                .getStatus()).isEqualTo(404);
        assertThat(send(agent, post("/v1/intel/recipes/dry-run-recipe/test"), "{\"fromSeq\":0,\"toSeq\":1000000}")
                .getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("exposure accepts a geometry")
    void exposureByGeometry() throws Exception {
        JsonNode out = body(send(read, post("/v1/exposure"),
                "{\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[-95.1,31.9],[-94.9,31.9],[-94.9,32.1],"
                        + "[-95.1,32.1],[-95.1,31.9]]]},\"population\":false}"));
        assertThat(out.has("facilities")).isTrue();
        assertThat(out.has("places")).isTrue();
        assertThat(out.has("entities")).isTrue();
    }
}
