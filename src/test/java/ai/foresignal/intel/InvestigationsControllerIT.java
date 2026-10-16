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
 * {@code /v1/investigations} over HTTP (prompt 101): CRUD, scopes, sequential display ids, items with
 * snapshots, removal scoped to the owning investigation, export with licence attributions, and
 * private items (EGR-1).
 */
@IntegrationTest
@Import(TestIngestSources.class)
class InvestigationsControllerIT {

    private static final String STANDARD = TestIngestSources.STANDARD;

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper json;
    @Autowired SourceVisibility sources;

    String read;
    String agent;
    String admin;

    @BeforeEach
    void setUp() {
        clock.setInstant(Instant.parse("2026-03-01T12:00:00Z"));
        read = TestTokens.bearer(TestTokens.create(tokens, "inv-read", Scope.READ));
        agent = TestTokens.bearer(TestTokens.create(tokens, "inv-agent", Scope.AGENT));
        admin = TestTokens.bearer(TestTokens.create(tokens, "inv-admin", Scope.ADMIN));
    }

    @AfterEach
    void restore() {
        sources.setOperatorPrivate(STANDARD, false);
        sources.refresh();
    }

    private long seed(String externalId) {
        Instant now = clock.instant();
        records.ingestAt(STANDARD, now, NormalizedRecord.builder(externalId, "thing").observedAt(now)
                .point(-95.0, 32.0).put("name", externalId).build());
        return jdbc.queryForObject("select max(seq) from record_version where source_id = ? and external_id = ?",
                Long.class, STANDARD, externalId);
    }

    private MockHttpServletResponse call(String bearer, MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.header(HttpHeaders.AUTHORIZATION, bearer)).andReturn().getResponse();
    }

    private MockHttpServletResponse send(String bearer, MockHttpServletRequestBuilder request, String body)
            throws Exception {
        return call(bearer, request.contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private JsonNode body(MockHttpServletResponse response) throws Exception {
        return json.readTree(response.getContentAsString(StandardCharsets.UTF_8));
    }

    private String create(String title) throws Exception {
        MockHttpServletResponse response = send(agent, post("/v1/investigations"), "{\"title\":\"" + title + "\"}");
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(201);
        return body(response).get("id").asString();
    }

    private long addRecord(String bearer, String investigation, String externalId, long seq, int expected)
            throws Exception {
        MockHttpServletResponse response = send(bearer, post("/v1/investigations/" + investigation + "/items"),
                "{\"itemType\":\"record\",\"ref\":\"" + STANDARD + "/" + externalId + "\",\"seq\":" + seq + "}");
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(expected);
        return expected == 200 ? body(response).get("id").asLong() : -1;
    }

    @Test
    @DisplayName("an investigation is created, patched, read, exported and deleted; writes need agent")
    void crudAndScopes() throws Exception {
        assertThat(send(read, post("/v1/investigations"), "{\"title\":\"No\"}").getStatus()).isEqualTo(403);
        String id = create("Case");
        assertThat(body(call(read, get("/v1/investigations/" + id))).get("displayId").asString())
                .isEqualTo("INV-2026-0001");
        assertThat(body(call(read, get("/v1/investigations/" + create("Second")))).get("displayId").asString())
                .isEqualTo("INV-2026-0002");
        assertThat(send(agent, patch("/v1/investigations/" + id), "{\"status\":\"closed\"}").getStatus())
                .isEqualTo(200);
        long seq = seed("c1");
        addRecord(agent, id, "c1", seq, 200);

        String md = call(read, get("/v1/investigations/" + id + "/export?format=md"))
                .getContentAsString(StandardCharsets.UTF_8);
        assertThat(md).contains(InvestigationService.BANNER).contains("[" + STANDARD + "/c1@v");
        assertThat(md).doesNotContain("GHSL");
        JsonNode exported = body(call(read, get("/v1/investigations/" + id + "/export?format=json")));
        assertThat(exported.get("attributions").toString()).contains("(");

        assertThat(call(read, delete("/v1/investigations/" + id)).getStatus()).isEqualTo(403);
        assertThat(call(agent, delete("/v1/investigations/" + id)).getStatus()).isEqualTo(204);
        assertThat(call(read, get("/v1/investigations/" + id)).getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("an item can only be removed through the investigation that holds it")
    void removeItemScoped() throws Exception {
        String first = create("First");
        String second = create("Second");
        long seq = seed("x1");
        long item = addRecord(agent, first, "x1", seq, 200);

        assertThat(call(agent, delete("/v1/investigations/" + second + "/items/" + item)).getStatus())
                .isEqualTo(404);
        assertThat(body(call(read, get("/v1/investigations/" + first))).get("items")).hasSize(1);
        assertThat(call(agent, delete("/v1/investigations/" + first + "/items/" + item)).getStatus())
                .isEqualTo(204);
        assertThat(body(call(read, get("/v1/investigations/" + first))).get("items")).isEmpty();
    }

    @Test
    @DisplayName("a missing record seq is 400 rather than an empty snapshot")
    void missingRecordRefused() throws Exception {
        String id = create("Missing");
        addRecord(agent, id, "nope", 987_654_321L, 400);
    }

    @Test
    @DisplayName("a private record item is added only with private access and shown only to such callers")
    void privateItems() throws Exception {
        sources.setOperatorPrivate(STANDARD, true);
        sources.refresh();
        String id = create("Private");
        long seq = seed("s1");

        addRecord(agent, id, "s1", seq, 400);
        addRecord(admin, id, "s1", seq, 200);

        assertThat(body(call(admin, get("/v1/investigations/" + id))).get("items")).hasSize(1);
        assertThat(body(call(read, get("/v1/investigations/" + id))).get("items")).isEmpty();
        assertThat(call(read, get("/v1/investigations/" + id + "/export?format=md"))
                .getContentAsString(StandardCharsets.UTF_8)).doesNotContain("s1");
        assertThat(call(admin, get("/v1/investigations/" + id + "/export?format=md"))
                .getContentAsString(StandardCharsets.UTF_8)).contains("s1");
    }
}
