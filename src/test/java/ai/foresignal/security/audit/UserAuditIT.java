package ai.foresignal.security.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.retention.RetentionJob;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.user.BuiltInRole;
import ai.foresignal.security.user.PasswordHasher;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.security.user.UserRoleRepository;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.security.TestUsers;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The user audit actor (prompt R2-013): sign-ins and what a signed-in user does are attributed to the user, secrets
 * never reach the log, an Auditor can query it by user and kind, entries cannot be changed, and retention still
 * removes old ones.
 */
@IntegrationTest
class UserAuditIT {

    static final Instant T0 = Instant.parse("2026-10-26T12:00:00Z");
    static final String SMTP_PASSWORD = "smtp-secret-never-logged";

    @Autowired private MockMvc mvc;
    @Autowired private JsonMapper json;
    @Autowired private PrincipalService principals;
    @Autowired private UserRepository users;
    @Autowired private UserRoleRepository userRoles;
    @Autowired private PasswordHasher hasher;
    @Autowired private AuditService audit;
    @Autowired private RetentionJob retention;
    @Autowired private AppSettingService settings;
    @Autowired private MutableClock clock;
    @Autowired private JdbcTemplate jdbc;

    private UserEntity admin;
    private String adminSession;

    @BeforeEach
    void setUp() throws Exception {
        clock.setInstant(T0);
        settings.invalidateCache();
        admin = TestUsers.active(principals, users, userRoles, hasher, "admin@example.com", BuiltInRole.ADMIN);
        adminSession = login("admin@example.com");
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

    private List<Map<String, Object>> rows(String operationId) {
        audit.flush();
        return jdbc.queryForList("SELECT actor_kind, user_id, user_email, operation_id, status, target_type,"
                + " target_id, detail::text AS detail FROM audit_event WHERE operation_id = ? ORDER BY id", operationId);
    }

    private static void assertUser(Map<String, Object> row, UserEntity user) {
        assertThat(row.get("actor_kind")).isEqualTo(AuditRecord.USER);
        assertThat(row.get("user_id").toString()).isEqualTo(user.getId().toString());
        assertThat(row.get("user_email")).isEqualTo(user.getEmail());
    }

    // ------------------------------------------------------------------------- sign-in

    @Test
    @DisplayName("successful and failed sign-ins are actor user; an unknown email has no user")
    void signIns() throws Exception {
        login("admin@example.com");
        mvc.perform(jsonBody(post("/v1/auth/login"), "{'email':'admin@example.com','password':'wrong-password'}"));
        mvc.perform(jsonBody(post("/v1/auth/login"), "{'email':'nobody@example.com','password':'wrong-password'}"));

        List<Map<String, Object>> rows = rows("login");
        assertThat(rows).hasSize(3);
        assertUser(rows.get(0), admin);
        assertThat(rows.get(0).get("status")).isEqualTo(200);
        assertUser(rows.get(1), admin);
        assertThat(rows.get(1).get("status")).isEqualTo(401);
        assertThat(rows.get(2).get("actor_kind")).isEqualTo(AuditRecord.USER);
        assertThat(rows.get(2).get("user_id")).isNull();
        assertThat(rows.get(2).get("status")).isEqualTo(401);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM audit_event WHERE detail::text LIKE '%wrong-password%'",
                Long.class)).isZero();
    }

    // ------------------------------------------------------------------------- admin actions

    @Test
    @DisplayName("an admin session's user, role, suspension, session and settings changes are attributed to the admin")
    void adminActions() throws Exception {
        MockHttpServletResponse created = call(jsonBody(post("/v1/users"),
                "{'email':'new@example.com','fullName':'New User','roles':['" + BuiltInRole.VIEWER.id()
                        + "'],'sendInvite':false}"), adminSession);
        assertThat(created.getStatus()).as(created.getContentAsString()).isEqualTo(201);
        String userId = body(created).get("id").asString();

        assertThat(call(jsonBody(post("/v1/users/" + userId + "/roles"),
                "{'roles':['" + BuiltInRole.EDITOR.id() + "']}"), adminSession).getStatus()).isEqualTo(200);
        assertThat(call(post("/v1/users/" + userId + "/suspend"), adminSession).getStatus()).isBetween(200, 204);
        MockHttpServletResponse smtp = call(jsonBody(put("/v1/settings/smtp"),
                "{'enabled':false,'host':'smtp.example.com','port':587,'security':'starttls','username':'ops',"
                        + "'password':'" + SMTP_PASSWORD + "','fromAddress':'ops@example.com'}"), adminSession);
        assertThat(smtp.getStatus()).as(smtp.getContentAsString()).isEqualTo(200);

        String other = login("admin@example.com");
        String otherId = null;
        for (JsonNode s : body(call(get("/v1/auth/sessions"), adminSession)).get("items")) {
            if (!s.get("current").asBoolean()) {
                otherId = s.get("id").asString();
            }
        }
        assertThat(otherId).isNotNull();
        assertThat(call(delete("/v1/auth/sessions/" + otherId), adminSession).getStatus()).isEqualTo(204);

        for (String op : List.of("createUser", "setUserRoles", "suspendUser", "updateSmtpSettings", "revokeSession")) {
            List<Map<String, Object>> rows = rows(op);
            assertThat(rows).as(op).isNotEmpty();
            rows.forEach(row -> assertUser(row, admin));
        }
        Map<String, Object> roles = rows("setUserRoles").get(0);
        assertThat(roles.get("target_type")).isEqualTo("users");
        assertThat(roles.get("target_id")).isEqualTo(userId);

        audit.flush();
        String dump = String.join("\n", jdbc.queryForList("SELECT row_to_json(a)::text FROM audit_event a",
                String.class));
        assertThat(dump).doesNotContain(SMTP_PASSWORD).doesNotContain(adminSession).doesNotContain(other);
    }

    // ------------------------------------------------------------------------- query

    @Test
    @DisplayName("an Auditor reads the log and filters by actor kind, user, operation, status, time and target")
    void auditorQueries() throws Exception {
        TestUsers.active(principals, users, userRoles, hasher, "auditor@example.com", BuiltInRole.AUDITOR);
        TestUsers.active(principals, users, userRoles, hasher, "viewer@example.com", BuiltInRole.VIEWER);
        String auditor = login("auditor@example.com");
        String viewer = login("viewer@example.com");
        call(jsonBody(post("/v1/users"), "{'email':'q@example.com','fullName':'Q','roles':[],'sendInvite':false}"),
                adminSession);
        audit.flush();

        assertThat(call(get("/v1/audit"), viewer).getStatus()).isEqualTo(403);
        MockHttpServletResponse all = call(get("/v1/audit"), auditor);
        assertThat(all.getStatus()).as(all.getContentAsString()).isEqualTo(200);

        JsonNode byUser = body(call(get("/v1/audit").param("actorKind", "user").param("userId",
                admin.getId().toString()).param("operationId", "createUser").param("status", "201")
                .param("targetType", "users").param("since", T0.minusSeconds(60).toString())
                .param("until", T0.plusSeconds(60).toString()), auditor));
        assertThat(byUser.get("items").size()).isEqualTo(1);
        JsonNode item = byUser.get("items").get(0);
        assertThat(item.get("actorKind").asString()).isEqualTo("user");
        assertThat(item.get("userId").asString()).isEqualTo(admin.getId().toString());
        assertThat(item.get("userEmail").asString()).isEqualTo("admin@example.com");

        assertThat(body(call(get("/v1/audit").param("actorKind", "system"), auditor)).get("items").size()).isZero();
        assertThat(body(call(get("/v1/audit").param("userId", "00000000-0000-0000-0000-000000000000"), auditor))
                .get("items").size()).isZero();
        assertThat(call(get("/v1/audit").param("userId", "not-a-uuid"), auditor).getStatus()).isEqualTo(400);
    }

    // ------------------------------------------------------------------------- integrity and retention

    @Test
    @DisplayName("an audit entry cannot be updated")
    void appendOnly() throws Exception {
        login("admin@example.com");
        audit.flush();
        assertThatThrownBy(() -> jdbc.update("UPDATE audit_event SET status = 1"))
                .hasMessageContaining("append-only");
    }

    @Test
    @DisplayName("retention removes user entries older than the audit window and keeps newer ones")
    void retention() {
        jdbc.update("INSERT INTO audit_event (at, actor_kind, user_id, user_email, detail)"
                + " VALUES (?, 'user', ?, 'admin@example.com', '{}'::jsonb)",
                Timestamp.from(T0.minus(Duration.ofDays(91))), admin.getId());
        jdbc.update("INSERT INTO audit_event (at, actor_kind, user_id, user_email, detail)"
                + " VALUES (?, 'user', ?, 'admin@example.com', '{}'::jsonb)",
                Timestamp.from(T0.minus(Duration.ofDays(89))), admin.getId());
        retention.run();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM audit_event WHERE actor_kind = 'user'", Long.class))
                .isEqualTo(1);
    }
}
