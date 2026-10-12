package ai.foresignal.security.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.retention.RetentionJob;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The audit log (prompt 023): what is recorded, what never is, the query and its retention. */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
class AuditIT {

    private static final Instant T0 = Instant.parse("2026-10-12T08:00:00Z");

    @Autowired private MockMvc mvc;
    @Autowired private AdminTokenService adminTokens;
    @Autowired private TokenService tokens;
    @Autowired private AuditService audit;
    @Autowired private RetentionJob retention;
    @Autowired private AppSettingService settings;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private MutableClock clock;
    @Autowired private JsonMapper json;

    private String admin;
    private TokenService.CreatedToken agent;

    @BeforeEach
    void setUp() {
        clock.setInstant(T0);
        settings.invalidateCache();
        admin = "Bearer " + adminTokens.rotate();
        agent = TestTokens.create(tokens, "openclaw", Scope.AGENT);
        audit.flush();
        jdbc.update("DELETE FROM audit_event");
    }

    private MockHttpServletResponse perform(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request).andReturn().getResponse();
    }

    private List<Map<String, Object>> rows() {
        audit.flush();
        return jdbc.queryForList("SELECT * FROM audit_event ORDER BY id");
    }

    @Test
    @DisplayName("an authenticated write is audited with the token's name, scope, operation and status")
    void writesAreAudited() throws Exception {
        perform(post("/v1/triggers").header(HttpHeaders.AUTHORIZATION, TestTokens.bearer(agent))
                .contentType(MediaType.APPLICATION_JSON).content("{}"));

        List<Map<String, Object>> rows = rows();
        assertThat(rows).hasSize(1);
        Map<String, Object> row = rows.get(0);
        assertThat(row.get("actor_kind")).isEqualTo("token");
        assertThat(row.get("token_name")).isEqualTo("openclaw");
        assertThat(row.get("token_id").toString()).isEqualTo(agent.entity().getId().toString());
        assertThat(row.get("principal_id")).isEqualTo(agent.entity().getPrincipalId());
        assertThat(row.get("scope")).isEqualTo("agent");
        assertThat(row.get("method")).isEqualTo("POST");
        assertThat(row.get("path")).isEqualTo("/v1/triggers");
        assertThat(row.get("operation_id")).isEqualTo("createTrigger");
        assertThat(row.get("status")).isEqualTo(400);
        assertThat(row.get("target_type")).isEqualTo("triggers");
        assertThat(row.get("remote_ip")).isEqualTo("127.0.0.1");
    }

    @Test
    @DisplayName("GETs are not audited")
    void readsAreNotAudited() throws Exception {
        perform(get("/v1/records").header(HttpHeaders.AUTHORIZATION, TestTokens.bearer(agent)));
        perform(get("/v1/settings/retention").header(HttpHeaders.AUTHORIZATION, admin));

        assertThat(rows()).isEmpty();
    }

    @Test
    @DisplayName("401 and 403 on write paths are audited with the problem code; the query string is stripped")
    void refusalsAreAudited() throws Exception {
        perform(put("/v1/sources/" + CollectorTestSources.KEYED + "?secret=x").contentType(MediaType.APPLICATION_JSON)
                .content("{}"));
        perform(put("/v1/sources/" + CollectorTestSources.KEYED).header(HttpHeaders.AUTHORIZATION, TestTokens.bearer(agent))
                .contentType(MediaType.APPLICATION_JSON).content("{}"));

        List<Map<String, Object>> rows = rows();
        assertThat(rows).extracting(r -> r.get("status")).containsExactly(401, 403);
        assertThat(rows.get(0).get("token_id")).isNull();
        assertThat(rows.get(0).get("path")).isEqualTo("/v1/sources/" + CollectorTestSources.KEYED);
        assertThat(rows.get(0).get("detail").toString()).contains("UNAUTHORIZED");
        assertThat(rows.get(1).get("detail").toString()).contains("INSUFFICIENT_SCOPE");
    }

    @Test
    @DisplayName("secrets, token values and request bodies never reach the audit log")
    void noSecrets() throws Exception {
        perform(put("/v1/sources/" + CollectorTestSources.KEYED + "/credentials").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"values\":{\"apiKey\":\"super-secret-key-123\"}}"));
        MockHttpServletResponse created = perform(post("/v1/tokens").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"another\",\"scope\":\"read\"}"));
        String newToken = json.readTree(created.getContentAsString()).get("token").asString();

        String dump = jdbc.queryForList("SELECT * FROM audit_event").toString() + rows();
        assertThat(dump).doesNotContain("super-secret-key-123").doesNotContain(newToken)
                .doesNotContain(admin.substring(7)).doesNotContain(agent.plaintext());
    }

    @Test
    @DisplayName("token revocations are audited")
    void revocationsAreAudited() throws Exception {
        perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                "/v1/tokens/" + agent.entity().getId()).header(HttpHeaders.AUTHORIZATION, admin));

        assertThat(rows()).singleElement().satisfies(r -> {
            assertThat(r.get("operation_id")).isEqualTo("revokeToken");
            assertThat(r.get("target_type")).isEqualTo("tokens");
            assertThat(r.get("target_id")).isEqualTo(agent.entity().getId().toString());
            assertThat(r.get("status")).isEqualTo(204);
        });
    }

    @Test
    @DisplayName("GET /v1/audit filters by token, operation and status and pages newest first")
    void queryFiltersAndPages() throws Exception {
        for (int i = 0; i < 3; i++) {
            perform(post("/v1/triggers").header(HttpHeaders.AUTHORIZATION, TestTokens.bearer(agent))
                    .contentType(MediaType.APPLICATION_JSON).content("{}"));
            clock.advance(Duration.ofSeconds(1));
        }
        perform(put("/v1/settings/cors").contentType(MediaType.APPLICATION_JSON).content("{}"));
        audit.flush();

        JsonNode page1 = json.readTree(perform(get("/v1/audit?tokenId=" + agent.entity().getId() + "&limit=2")
                .header(HttpHeaders.AUTHORIZATION, admin)).getContentAsString());
        assertThat(page1.get("items")).hasSize(2);
        assertThat(page1.get("items").get(0).get("id").asLong()).isGreaterThan(page1.get("items").get(1).get("id").asLong());
        String cursor = page1.get("nextCursor").asString();
        JsonNode page2 = json.readTree(perform(get("/v1/audit").param("tokenId", agent.entity().getId().toString())
                .param("limit", "2").param("cursor", cursor).header(HttpHeaders.AUTHORIZATION, admin))
                .getContentAsString());
        assertThat(page2.get("items")).hasSize(1);
        assertThat(page2.get("nextCursor").isNull()).isTrue();

        JsonNode refused = json.readTree(perform(get("/v1/audit?status=401").header(HttpHeaders.AUTHORIZATION, admin))
                .getContentAsString());
        assertThat(refused.get("items")).singleElement().satisfies(e -> {
            assertThat(e.get("operationId").asString()).isEqualTo("updateCors");
            assertThat(e.get("actorKind").asString()).isEqualTo("token");
        });
        JsonNode byOperation = json.readTree(perform(get("/v1/audit?operationId=createTrigger")
                .header(HttpHeaders.AUTHORIZATION, admin)).getContentAsString());
        assertThat(byOperation.get("items")).hasSize(3);
        JsonNode since = json.readTree(perform(get("/v1/audit?since=" + T0.plusSeconds(1))
                .header(HttpHeaders.AUTHORIZATION, admin)).getContentAsString());
        assertThat(since.get("items")).hasSize(3);

        assertThat(perform(get("/v1/audit?cursor=nonsense").header(HttpHeaders.AUTHORIZATION, admin)).getStatus())
                .isEqualTo(400);
        assertThat(perform(get("/v1/audit").header(HttpHeaders.AUTHORIZATION, TestTokens.bearer(agent))).getStatus())
                .isEqualTo(403);
    }

    @Test
    @DisplayName("the audit retention step deletes entries older than retention.audit (default 90d)")
    void retentionStep() {
        jdbc.update("INSERT INTO audit_event (at, actor_kind, detail) VALUES (?, 'system', '{}'::jsonb)",
                Timestamp.from(T0.minus(Duration.ofDays(91))));
        jdbc.update("INSERT INTO audit_event (at, actor_kind, detail) VALUES (?, 'system', '{}'::jsonb)",
                Timestamp.from(T0.minus(Duration.ofDays(89))));

        retention.run();

        assertThat(jdbc.queryForObject("SELECT count(*) FROM audit_event WHERE actor_kind = 'system'", Long.class))
                .isEqualTo(1L);
    }

    @Test
    @DisplayName("a full queue drops and counts instead of blocking")
    void overflowDrops() {
        long before = audit.droppedCount();
        AuditRecord entry = new AuditRecord(T0, AuditRecord.SYSTEM, null, null, null, null, null, null, null, null,
                null, null, null, Map.of());
        int queued = 0;
        for (int i = 0; i < AuditService.QUEUE_CAPACITY * 2; i++) {
            if (audit.record(entry)) {
                queued++;
            }
        }
        audit.flush();
        assertThat(queued).isLessThanOrEqualTo(AuditService.QUEUE_CAPACITY * 2);
        assertThat(audit.droppedCount() - before).isEqualTo(AuditService.QUEUE_CAPACITY * 2L - queued);
        jdbc.update("DELETE FROM audit_event");
    }
}
