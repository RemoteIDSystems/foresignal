package ai.foresignal.security.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.security.TestUsers;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The permission gate through the real filter chain (prompt R2-004): a session is gated by the operation's
 * {@code x-foresignal-permission}, an API token by scope only.
 *
 * <p>Operations used: {@code POST /v1/users} ({@code users.manage}, planned: the gate answers before any controller),
 * {@code GET /v1/feed-keys} ({@code users.manage}), {@code GET /v1/records} ({@code data.read}),
 * {@code GET /v1/ingest/formats} ({@code collection.manage}), {@code GET /v1/audit} ({@code audit.read}, admin scope)
 * and {@code GET /v1/me/sources} ({@code self.sources}, planned).
 */
@IntegrationTest
class AuthorizationIT {

    @Autowired private MockMvc mvc;
    @Autowired private JsonMapper json;
    @Autowired private PrincipalService principals;
    @Autowired private UserRepository users;
    @Autowired private UserRoleRepository userRoles;
    @Autowired private PasswordHasher hasher;
    @Autowired private TokenService tokens;
    @Autowired private ApplicationEventPublisher events;

    private UserEntity user(String email, BuiltInRole... roles) {
        return TestUsers.active(principals, users, userRoles, hasher, email, roles);
    }

    private MockHttpServletResponse call(MockHttpServletRequestBuilder builder, String bearer) throws Exception {
        builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer);
        return mvc.perform(builder).andReturn().getResponse();
    }

    private String login(String email) throws Exception {
        MockHttpServletResponse r = mvc.perform(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(TestUsers.loginBody(email))).andReturn().getResponse();
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        return json.readTree(r.getContentAsString()).get("token").asString();
    }

    private static MockHttpServletRequestBuilder createUser() {
        return post("/v1/users").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"new@example.com\",\"fullName\":\"New\",\"roles\":[]}");
    }

    private void assertDenied(MockHttpServletResponse r, String permission) throws Exception {
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(403);
        JsonNode body = json.readTree(r.getContentAsString());
        assertThat(body.get("code").asString()).isEqualTo(ErrorCodes.PERMISSION_DENIED);
        assertThat(body.get("requiredPermission").asString()).isEqualTo(permission);
    }

    @Test
    @DisplayName("a Viewer session is refused createUser with PERMISSION_DENIED and reads records")
    void viewer() throws Exception {
        user("viewer@example.com", BuiltInRole.VIEWER);
        String session = login("viewer@example.com");
        assertDenied(call(createUser(), session), "users.manage");
        assertThat(call(get("/v1/records"), session).getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("an Operator session passes collection.manage and is refused users.manage, despite its admin scope")
    void operator() throws Exception {
        user("operator@example.com", BuiltInRole.OPERATOR);
        String session = login("operator@example.com");
        assertThat(call(get("/v1/ingest/formats"), session).getStatus()).isEqualTo(200);
        assertDenied(call(get("/v1/feed-keys"), session), "users.manage");
        assertDenied(call(createUser(), session), "users.manage");
    }

    @Test
    @DisplayName("an Editor session passes the self.* gate, a Viewer is refused it")
    void editorSelfService() throws Exception {
        user("editor@example.com", BuiltInRole.EDITOR);
        user("viewer2@example.com", BuiltInRole.VIEWER);
        // getMySources is planned: past the gate there is no handler yet, so anything but 401/403 means it passed.
        assertThat(call(get("/v1/me/sources"), login("editor@example.com")).getStatus()).isNotIn(401, 403);
        assertDenied(call(get("/v1/me/sources"), login("viewer2@example.com")), "self.sources");
    }

    @Test
    @DisplayName("an Auditor session reads the admin-scoped audit log on its audit.read permission")
    void auditorReadsAudit() throws Exception {
        user("auditor@example.com", BuiltInRole.AUDITOR);
        String session = login("auditor@example.com");
        assertThat(call(get("/v1/audit"), session).getStatus()).isEqualTo(200);
        assertDenied(call(get("/v1/ingest/formats"), session), "collection.manage");
    }

    @Test
    @DisplayName("an admin API token passes a users.manage operation; an agent token gets INSUFFICIENT_SCOPE")
    void machineTokensAreScopeGated() throws Exception {
        String admin = TestTokens.create(tokens, "authz-admin", Scope.ADMIN).plaintext();
        String agent = TestTokens.create(tokens, "authz-agent", Scope.AGENT).plaintext();
        assertThat(call(get("/v1/feed-keys"), admin).getStatus()).isEqualTo(200);
        MockHttpServletResponse r = call(get("/v1/feed-keys"), agent);
        assertThat(r.getStatus()).isEqualTo(403);
        assertThat(json.readTree(r.getContentAsString()).get("code").asString())
                .isEqualTo(ErrorCodes.INSUFFICIENT_SCOPE);
    }

    @Test
    @DisplayName("an Admin session passes every permission")
    void adminSession() throws Exception {
        user("admin@example.com", BuiltInRole.ADMIN);
        String session = login("admin@example.com");
        assertThat(call(get("/v1/feed-keys"), session).getStatus()).isEqualTo(200);
        assertThat(call(get("/v1/audit"), session).getStatus()).isEqualTo(200);
        assertThat(call(get("/v1/ingest/formats"), session).getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("the permissions are the union across roles")
    void unionAcrossRoles() throws Exception {
        user("both@example.com", BuiltInRole.VIEWER, BuiltInRole.AUDITOR);
        String session = login("both@example.com");
        assertThat(call(get("/v1/audit"), session).getStatus()).isEqualTo(200);
        assertThat(call(get("/v1/records"), session).getStatus()).isEqualTo(200);
        assertDenied(call(get("/v1/me/sources"), session), "self.sources");
    }

    @Test
    @DisplayName("a role change is cached until UserRolesChangedEvent invalidates it")
    void roleChangeInvalidatesTheCache() throws Exception {
        UserEntity u = user("grow@example.com", BuiltInRole.VIEWER);
        String session = login("grow@example.com");
        assertDenied(call(get("/v1/audit"), session), "audit.read");
        userRoles.saveAndFlush(new UserRoleEntity(u.getId(), BuiltInRole.AUDITOR.id()));
        // Cached: the gate does not read user_role on every request.
        assertDenied(call(get("/v1/audit"), session), "audit.read");
        events.publishEvent(new UserRolesChangedEvent(u.getId()));
        assertThat(call(get("/v1/audit"), session).getStatus()).isEqualTo(200);
        assertThat(json.convertValue(json.readTree(call(get("/v1/me"), session).getContentAsString())
                .get("permissions"), List.class)).contains("audit.read");
    }
}
