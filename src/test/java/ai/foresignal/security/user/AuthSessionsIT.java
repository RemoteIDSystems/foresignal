package ai.foresignal.security.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.security.TestUsers;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
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

/** The {@code Auth} operations and session bearers through the real filter chain (prompt R2-003). */
@IntegrationTest
class AuthSessionsIT {

    @Autowired private MockMvc mvc;
    @Autowired private JsonMapper json;
    @Autowired private PrincipalService principals;
    @Autowired private UserRepository users;
    @Autowired private UserRoleRepository userRoles;
    @Autowired private PasswordHasher hasher;
    @Autowired private UserLifecycleService lifecycle;
    @Autowired private UserTokenService userTokens;
    @Autowired private TokenService tokens;
    @Autowired private AuditService audit;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PermissionResolver resolver;

    private UserEntity user(String email, BuiltInRole... roles) {
        return TestUsers.active(principals, users, userRoles, hasher, email, roles);
    }

    private MockHttpServletResponse call(MockHttpServletRequestBuilder builder, String bearer) throws Exception {
        if (bearer != null) {
            builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer);
        }
        return mvc.perform(builder).andReturn().getResponse();
    }

    private MockHttpServletResponse postJson(String path, String body) throws Exception {
        return call(post(path).contentType(MediaType.APPLICATION_JSON).content(body), null);
    }

    private JsonNode body(MockHttpServletResponse r) throws Exception {
        return json.readTree(r.getContentAsString());
    }

    private String login(String email) throws Exception {
        MockHttpServletResponse r = postJson("/v1/auth/login", TestUsers.loginBody(email));
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        return body(r).get("token").asString();
    }

    @Test
    @DisplayName("an active user signs in and gets an fss_ session with its permissions and derived scopes")
    void loginSucceeds() throws Exception {
        UserEntity u = user("ed@example.com", BuiltInRole.EDITOR);
        MockHttpServletResponse r = postJson("/v1/auth/login",
                "{\"email\":\"ED@Example.com\",\"password\":\"" + TestUsers.PASSWORD + "\"}");
        assertThat(r.getStatus()).isEqualTo(200);
        JsonNode b = body(r);
        assertThat(b.get("token").asString()).startsWith("fss_");
        assertThat(b.get("tokenPrefix").asString()).isEqualTo(b.get("token").asString().substring(0, 8));
        assertThat(b.get("userId").asString()).isEqualTo(u.getId().toString());
        assertThat(b.get("mfaRequired").asBoolean()).isFalse();
        assertThat(json.convertValue(b.get("scopes"), List.class)).containsExactly("read", "agent");
        assertThat(json.convertValue(b.get("permissions"), List.class)).containsExactlyInAnyOrderElementsOf(
                BuiltInRole.EDITOR.permissions().stream().map(Permissions::key).toList());
        assertThat(Instant.parse(b.get("expiresAt").asString()))
                .isEqualTo(Instant.parse(b.get("issuedAt").asString()).plus(SessionService.ABSOLUTE_LIFETIME));
        assertThat(users.findById(u.getId()).orElseThrow().getLastLoginAt()).isNotNull();
        assertThat(jdbc.queryForObject("select count(*) from user_session where token_hash = ?", Long.class,
                UserTokenService.sha256(b.get("token").asString()))).isEqualTo(1L);
    }

    @Test
    @DisplayName("bad password, unknown email, invited, suspended and disabled all get the same audited 401")
    void loginFailsGenerically() throws Exception {
        UserEntity s = user("sus@example.com", BuiltInRole.VIEWER);
        lifecycle.suspend(s);
        UserEntity d = user("dis@example.com", BuiltInRole.VIEWER);
        lifecycle.disable(d);
        user("ok@example.com", BuiltInRole.VIEWER);
        String detail = null;
        for (String bodyText : new String[] {
                "{\"email\":\"ok@example.com\",\"password\":\"not the password\"}",
                "{\"email\":\"nobody@example.com\",\"password\":\"" + TestUsers.PASSWORD + "\"}",
                TestUsers.loginBody("sus@example.com"), TestUsers.loginBody("dis@example.com")}) {
            MockHttpServletResponse r = postJson("/v1/auth/login", bodyText);
            assertThat(r.getStatus()).isEqualTo(401);
            JsonNode b = body(r);
            assertThat(b.get("code").asString()).isEqualTo("INVALID_CREDENTIALS");
            if (detail == null) {
                detail = b.get("detail").asString();
            }
            assertThat(b.get("detail").asString()).isEqualTo(detail);
        }
        audit.flush();
        assertThat(jdbc.queryForList("select detail->>'reason' from audit_event where operation_id = 'login' and "
                + "status = 401", String.class)).containsExactlyInAnyOrder("wrong_password", "unknown_email",
                        "status_suspended", "status_disabled");
        assertThat(postJson("/v1/auth/login", "{\"email\":\"ok@example.com\"}").getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("a session authenticates a read; it is refused once revoked, expired or idle-expired")
    void sessionLifecycle() throws Exception {
        user("vi@example.com", BuiltInRole.VIEWER);
        String a = login("vi@example.com");
        assertThat(call(get("/v1/me"), a).getStatus()).isEqualTo(200);
        assertThat(call(post("/v1/auth/logout"), a).getStatus()).isEqualTo(204);
        assertThat(call(get("/v1/me"), a).getStatus()).isEqualTo(401);

        String b = login("vi@example.com");
        jdbc.update("update user_session set expires_at = now() - interval '1 second' where token_hash = ?",
                UserTokenService.sha256(b));
        assertThat(call(get("/v1/me"), b).getStatus()).isEqualTo(401);

        String c = login("vi@example.com");
        jdbc.update("update user_session set idle_expires_at = now() - interval '1 second' where token_hash = ?",
                UserTokenService.sha256(c));
        assertThat(call(get("/v1/me"), c).getStatus()).isEqualTo(401);
        assertThat(call(get("/v1/me"), "fss_doesNotExist").getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("suspending the user ends its sessions on the next request")
    void suspensionEndsSessions() throws Exception {
        UserEntity u = user("su2@example.com", BuiltInRole.EDITOR);
        String s = login("su2@example.com");
        lifecycle.suspend(users.findById(u.getId()).orElseThrow());
        assertThat(call(get("/v1/me"), s).getStatus()).isEqualTo(401);
        assertThat(jdbc.queryForObject("select count(*) from user_session where user_id = ? and revoked_at is null",
                Long.class, u.getId())).isZero();
    }

    @Test
    @DisplayName("listSessions marks the current one; revokeSession touches only the caller's; revokeAll keeps current")
    void sessionManagement() throws Exception {
        UserEntity mine = user("me@example.com", BuiltInRole.VIEWER);
        user("other@example.com", BuiltInRole.VIEWER);
        String first = login("me@example.com");
        String second = login("me@example.com");
        String third = login("me@example.com");
        String others = login("other@example.com");

        JsonNode page = body(call(get("/v1/auth/sessions"), second));
        assertThat(page.get("items")).hasSize(3);
        long current = 0;
        for (JsonNode item : page.get("items")) {
            if (item.get("current").asBoolean()) {
                current++;
                assertThat(item.get("tokenPrefix").asString()).isEqualTo(second.substring(0, 8));
            }
            assertThat(item.has("tokenHash")).isFalse();
        }
        assertThat(current).isEqualTo(1);

        String otherId = jdbc.queryForObject("select id::text from user_session where token_hash = ?", String.class,
                UserTokenService.sha256(others));
        assertThat(call(delete("/v1/auth/sessions/" + otherId), second).getStatus()).isEqualTo(404);
        assertThat(call(get("/v1/me"), others).getStatus()).isEqualTo(200);
        assertThat(call(delete("/v1/auth/sessions/" + UUID.randomUUID()), second).getStatus()).isEqualTo(404);
        assertThat(call(delete("/v1/auth/sessions/not-a-uuid"), second).getStatus()).isEqualTo(404);

        String firstId = jdbc.queryForObject("select id::text from user_session where token_hash = ?", String.class,
                UserTokenService.sha256(first));
        assertThat(call(delete("/v1/auth/sessions/" + firstId), second).getStatus()).isEqualTo(204);
        assertThat(call(get("/v1/me"), first).getStatus()).isEqualTo(401);

        assertThat(call(delete("/v1/auth/sessions"), second).getStatus()).isEqualTo(204);
        assertThat(call(get("/v1/me"), third).getStatus()).isEqualTo(401);
        assertThat(call(get("/v1/me"), second).getStatus()).isEqualTo(200);
        assertThat(jdbc.queryForObject("select count(*) from user_session where user_id = ? and revoked_at is null",
                Long.class, mine.getId())).isEqualTo(1L);
    }

    @Test
    @DisplayName("the session operations need a session, not an API token")
    void sessionOpsNeedASession() throws Exception {
        String admin = TestTokens.create(tokens, "auth-it-admin", Scope.ADMIN).plaintext();
        assertThat(call(get("/v1/auth/sessions"), admin).getStatus()).isEqualTo(401);
        assertThat(call(get("/v1/auth/sessions"), null).getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("a reset request is 202 for known and unknown emails alike")
    void resetRequestIsUniform() throws Exception {
        user("rr@example.com", BuiltInRole.VIEWER);
        MockHttpServletResponse known = postJson("/v1/auth/password/reset-request", "{\"email\":\"rr@example.com\"}");
        MockHttpServletResponse unknown = postJson("/v1/auth/password/reset-request",
                "{\"email\":\"nobody@example.com\"}");
        assertThat(known.getStatus()).isEqualTo(202);
        assertThat(unknown.getStatus()).isEqualTo(202);
        assertThat(known.getContentAsString()).isEqualTo(unknown.getContentAsString());
        assertThat(jdbc.queryForObject("select count(*) from user_token where kind = 'reset'", Long.class))
                .isEqualTo(1L);
    }

    @Test
    @DisplayName("resetPassword consumes once (410 after), sets the password and ends existing sessions")
    void resetPassword() throws Exception {
        UserEntity u = user("rp@example.com", BuiltInRole.VIEWER);
        String old = login("rp@example.com");
        String token = userTokens.issueReset(u, null).plaintext();
        assertThat(postJson("/v1/auth/password/reset", "{\"token\":\"" + token + "\",\"newPassword\":\"short\"}")
                .getStatus()).isEqualTo(400);
        assertThat(postJson("/v1/auth/password/reset",
                "{\"token\":\"" + token + "\",\"newPassword\":\"a brand new password\"}").getStatus()).isEqualTo(204);
        assertThat(call(get("/v1/me"), old).getStatus()).isEqualTo(401);
        MockHttpServletResponse replay = postJson("/v1/auth/password/reset",
                "{\"token\":\"" + token + "\",\"newPassword\":\"a brand new password\"}");
        assertThat(replay.getStatus()).isEqualTo(410);
        assertThat(body(replay).get("code").asString()).isEqualTo("RESET_TOKEN_GONE");
        assertThat(postJson("/v1/auth/login",
                "{\"email\":\"rp@example.com\",\"password\":\"a brand new password\"}").getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("activateAccount consumes once, activates and lets the user sign in")
    void activate() throws Exception {
        UserEntity u = users.saveAndFlush(new UserEntity(UUID.randomUUID(), "new@example.com", "New",
                principals.createFor("new@example.com", ai.foresignal.security.PrincipalKind.PERSON).getId(), null,
                Instant.now()));
        String token = userTokens.issueActivation(u, null, null).plaintext();
        assertThat(postJson("/v1/auth/activate", "{\"token\":\"" + token + "\",\"password\":\"" + TestUsers.PASSWORD
                + "\"}").getStatus()).isEqualTo(204);
        assertThat(users.findById(u.getId()).orElseThrow().getStatus()).isEqualTo(UserStatus.ACTIVE);
        MockHttpServletResponse replay = postJson("/v1/auth/activate", "{\"token\":\"" + token + "\",\"password\":\""
                + TestUsers.PASSWORD + "\"}");
        assertThat(replay.getStatus()).isEqualTo(410);
        assertThat(body(replay).get("code").asString()).isEqualTo("ACTIVATION_TOKEN_GONE");
        login("new@example.com");
    }

    @Test
    @DisplayName("getMe reports the user and permissions for a session and keeps the token shape for an API token")
    void getMe() throws Exception {
        UserEntity u = user("au@example.com", BuiltInRole.AUDITOR);
        String s = login("au@example.com");
        JsonNode me = body(call(get("/v1/me"), s));
        assertThat(me.get("user").get("id").asString()).isEqualTo(u.getId().toString());
        assertThat(me.get("user").get("roles").get(0).get("name").asString()).isEqualTo("Auditor");
        assertThat(me.get("user").has("passwordHash")).isFalse();
        assertThat(json.convertValue(me.get("permissions"), List.class)).containsExactly("audit.read", "data.read");
        assertThat(me.get("token").get("tokenPrefix").asString()).isEqualTo(s.substring(0, 8));
        assertThat(me.get("principal").get("id").asString()).isEqualTo(u.getPrincipalId());

        String admin = TestTokens.create(tokens, "me-admin", Scope.ADMIN).plaintext();
        JsonNode tokenMe = body(call(get("/v1/me"), admin));
        assertThat(tokenMe.has("user")).isFalse();
        assertThat(tokenMe.has("permissions")).isFalse();
        assertThat(tokenMe.get("token").get("scope").asString()).isEqualTo("admin");
    }

    @Test
    @DisplayName("the derived scope gates operations: a Viewer session is refused an agent operation")
    void derivedScope() throws Exception {
        user("vw@example.com", BuiltInRole.VIEWER);
        String s = login("vw@example.com");
        MockHttpServletResponse r = call(get("/v1/analysis/sample"), s);
        assertThat(r.getStatus()).isEqualTo(403);
        assertThat(body(r).get("code").asString()).isEqualTo("INSUFFICIENT_SCOPE");
    }

    @Test
    @DisplayName("a session is never accepted on an ingest path, even an admin's")
    void notForIngest() throws Exception {
        user("adm@example.com", BuiltInRole.ADMIN);
        String s = login("adm@example.com");
        assertThat(call(post("/v1/ingest/sample/json").contentType(MediaType.APPLICATION_JSON).content("{}"), s)
                .getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("a role change applies to the next request of an existing session")
    void permissionsReResolve() throws Exception {
        UserEntity u = user("rc@example.com", BuiltInRole.VIEWER);
        String s = login("rc@example.com");
        assertThat(json.convertValue(body(call(get("/v1/me"), s)).get("permissions"), List.class))
                .doesNotContain("audit.read");
        userRoles.saveAndFlush(new UserRoleEntity(u.getId(), BuiltInRole.AUDITOR.id()));
        // R2-004 caches permissions per user; the role change announces itself.
        resolver.invalidate(u.getId());
        assertThat(json.convertValue(body(call(get("/v1/me"), s)).get("permissions"), List.class))
                .contains("audit.read");
    }
}
