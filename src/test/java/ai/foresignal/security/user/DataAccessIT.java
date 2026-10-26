package ai.foresignal.security.user;

import static ai.foresignal.testsupport.records.RecordsTestSources.SCHEMA_A;
import static ai.foresignal.testsupport.records.RecordsTestSources.SCHEMA_B;
import static ai.foresignal.testsupport.records.RecordsTestSources.SCHEMA_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.records.RecordsTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.security.TestUsers;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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
 * Per-user data access through the real filter chain (prompt R2-013): {@code data.read} gates a session's data reads
 * and a refusal is audited; an admin-set data restriction clamps what one user reads, by area and by source, through
 * the same clamp as a restricted token; machine tokens are untouched by permissions.
 */
@IntegrationTest
@Import(RecordsTestSources.class)
class DataAccessIT {

    static final Instant T = Instant.parse("2026-10-26T12:00:00Z");

    @Autowired private MockMvc mvc;
    @Autowired private JsonMapper json;
    @Autowired private PrincipalService principals;
    @Autowired private UserRepository users;
    @Autowired private UserRoleRepository userRoles;
    @Autowired private PasswordHasher hasher;
    @Autowired private TokenService tokens;
    @Autowired private IngestQueue queue;
    @Autowired private MutableClock clock;
    @Autowired private AuditService audit;
    @Autowired private AppSettingService settings;
    @Autowired private UserDataRestrictionRepository restrictions;
    @Autowired private JdbcTemplate jdbc;

    private String adminToken;
    private UserEntity viewer;
    private String viewerSession;
    private String otherViewerSession;
    private UserEntity bare;
    private String bareSession;

    @BeforeEach
    void setUp() throws Exception {
        settings.invalidateCache();
        clock.setInstant(T);
        adminToken = TestTokens.create(tokens, "data-it-admin", Scope.ADMIN).plaintext();
        viewer = TestUsers.active(principals, users, userRoles, hasher, "viewer@example.com", BuiltInRole.VIEWER);
        TestUsers.active(principals, users, userRoles, hasher, "other@example.com", BuiltInRole.VIEWER);
        bare = TestUsers.active(principals, users, userRoles, hasher, "bare@example.com");
        viewerSession = login("viewer@example.com");
        otherViewerSession = login("other@example.com");
        bareSession = login("bare@example.com");

        TestRecords records = new TestRecords(queue, clock);
        // Two records of source A, one near (1, 1) and one far away at (50, 50), and one of source B near (1, 1).
        records.ingestAt(SCHEMA_A, T, NormalizedRecord.builder("near-a", SCHEMA_KIND).observedAt(T).point(1, 1).build(),
                NormalizedRecord.builder("far-a", SCHEMA_KIND).observedAt(T).point(50, 50).build());
        records.ingestAt(SCHEMA_B, T, NormalizedRecord.builder("near-b", SCHEMA_KIND).observedAt(T).point(1, 1).build());
        audit.flush();
        jdbc.update("DELETE FROM audit_event");
    }

    // ------------------------------------------------------------------------- helpers

    private MockHttpServletResponse call(MockHttpServletRequestBuilder builder, String bearer) throws Exception {
        builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer);
        return mvc.perform(builder).andReturn().getResponse();
    }

    private static MockHttpServletRequestBuilder jsonBody(MockHttpServletRequestBuilder builder, String body) {
        return builder.contentType(MediaType.APPLICATION_JSON).content(body.replace('\'', '"'));
    }

    private JsonNode body(MockHttpServletResponse r) throws Exception {
        return json.readTree(r.getContentAsString());
    }

    private String login(String email) throws Exception {
        MockHttpServletResponse r = mvc.perform(jsonBody(post("/v1/auth/login"), TestUsers.loginBody(email)))
                .andReturn().getResponse();
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        return body(r).get("token").asString();
    }

    private String records(String bearer) throws Exception {
        MockHttpServletResponse r = call(get("/v1/records"), bearer);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        return r.getContentAsString();
    }

    private MockHttpServletResponse restrict(UserEntity user, String restriction) throws Exception {
        return call(jsonBody(put("/v1/users/" + user.getId()), "{'dataRestriction':" + restriction + "}"),
                adminToken);
    }

    // ------------------------------------------------------------------------- data.read

    @Test
    @DisplayName("a session without data.read is 403 on a record read, and the refusal is audited to the user")
    void dataReadGates() throws Exception {
        MockHttpServletResponse r = call(get("/v1/records"), bareSession);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(403);
        assertThat(body(r).get("code").asString()).isEqualTo(ErrorCodes.PERMISSION_DENIED);
        assertThat(body(r).get("requiredPermission").asString()).isEqualTo("data.read");
        assertThat(call(get("/v1/records/" + SCHEMA_A + "/near-a/track"), bareSession).getStatus()).isEqualTo(403);
        assertThat(call(get(java.net.URI.create("/v1/objects/record:" + SCHEMA_A + "%2Fnear-a")), bareSession)
                .getStatus()).as("an object read now carries data.read too").isEqualTo(403);

        audit.flush();
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT actor_kind, user_id, user_email, operation_id, status, detail->>'requiredPermission' AS needed"
                        + " FROM audit_event WHERE operation_id = 'listRecords'");
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("actor_kind")).isEqualTo("user");
        assertThat(rows.get(0).get("user_id").toString()).isEqualTo(bare.getId().toString());
        assertThat(rows.get(0).get("user_email")).isEqualTo("bare@example.com");
        assertThat(rows.get(0).get("status")).isEqualTo(403);
        assertThat(rows.get(0).get("needed")).isEqualTo("data.read");
    }

    @Test
    @DisplayName("with data.read the same read succeeds and is not audited")
    void dataReadAllows() throws Exception {
        assertThat(records(viewerSession)).contains("near-a").contains("far-a").contains("near-b");
        audit.flush();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM audit_event WHERE operation_id = 'listRecords'",
                Long.class)).isZero();
    }

    @Test
    @DisplayName("machine tokens are unaffected by permissions: scope decides")
    void machineTokens() throws Exception {
        String reader = TestTokens.create(tokens, "data-it-reader", Scope.READ).plaintext();
        assertThat(records(reader)).contains("near-a").contains("far-a").contains("near-b");
        assertThat(records(adminToken)).contains("near-a").contains("far-a").contains("near-b");
    }

    // ------------------------------------------------------------------------- data restrictions

    @Test
    @DisplayName("an area restriction clamps that user's reads; another user still sees everything")
    void areaRestriction() throws Exception {
        MockHttpServletResponse set = restrict(viewer, "{'bbox':[0,0,2,2]}");
        assertThat(set.getStatus()).as(set.getContentAsString()).isEqualTo(200);
        assertThat(body(set).get("dataRestriction").get("bbox").size()).isEqualTo(4);

        assertThat(records(viewerSession)).contains("near-a").contains("near-b").doesNotContain("far-a");
        assertThat(records(otherViewerSession)).contains("near-a").contains("far-a").contains("near-b");
        assertThat(records(adminToken)).contains("far-a");
        MockHttpServletResponse single = call(get("/v1/records/" + SCHEMA_A + "/far-a"), viewerSession);
        assertThat(single.getStatus()).as("an out-of-area single read is 404, as for a token").isEqualTo(404);
    }

    @Test
    @DisplayName("a source restriction intersects with everything else; null lifts it")
    void sourceRestrictionAndLift() throws Exception {
        assertThat(restrict(viewer, "{'sources':['" + SCHEMA_B + "'],'bbox':[0,0,2,2]}").getStatus()).isEqualTo(200);
        assertThat(records(viewerSession)).contains("near-b").doesNotContain("near-a").doesNotContain("far-a");

        assertThat(restrict(viewer, "null").getStatus()).isEqualTo(200);
        assertThat(restrictions.findById(viewer.getId())).isEmpty();
        assertThat(records(viewerSession)).contains("near-a").contains("far-a").contains("near-b");
    }

    @Test
    @DisplayName("updateUser without dataRestriction leaves it; getUser shows it")
    void absentLeavesIt() throws Exception {
        restrict(viewer, "{'kinds':['" + SCHEMA_KIND + "']}");
        MockHttpServletResponse renamed = call(jsonBody(put("/v1/users/" + viewer.getId()), "{'fullName':'Vee'}"),
                adminToken);
        assertThat(renamed.getStatus()).isEqualTo(200);
        assertThat(body(renamed).get("dataRestriction").get("kinds").get(0).asString()).isEqualTo(SCHEMA_KIND);
        JsonNode got = body(call(get("/v1/users/" + viewer.getId()), adminToken));
        assertThat(got.get("dataRestriction").get("kinds").get(0).asString()).isEqualTo(SCHEMA_KIND);
        assertThat(body(call(get("/v1/users/" + bare.getId()), adminToken)).get("dataRestriction").isNull()).isTrue();
    }

    @Test
    @DisplayName("an invalid restriction is 400 and changes nothing")
    void invalid() throws Exception {
        for (String bad : List.of("{'endpoints':['GET /v1/records']}", "{'sources':['no-such-source']}",
                "{'bbox':[1,2,3]}", "{'maxRequestsPerMinute':5}", "'everything'")) {
            MockHttpServletResponse r = restrict(viewer, bad);
            assertThat(r.getStatus()).as(bad + " → " + r.getContentAsString()).isEqualTo(400);
        }
        assertThat(restrictions.findById(viewer.getId())).isEmpty();
    }
}
