package ai.foresignal.security.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.security.TestUsers;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code Users} operations through the real filter chain (prompt R2-005), with a recording
 * {@link AccountMailer} and {@link UserOwnedConfigReassigner} so the emails and the reassignment are observable.
 */
@IntegrationTest
@Import(UserAdminIT.Fakes.class)
class UserAdminIT {

    /** Records what the account mailer was asked to send; reports welcome emails as sent when {@link #deliver}. */
    static final class RecordingMailer implements AccountMailer {
        final List<String> sent = new ArrayList<>();
        boolean deliver = true;

        @Override
        public boolean sendPasswordReset(UserEntity user, IssuedUserToken token) {
            sent.add("reset:" + user.getEmail());
            return deliver;
        }

        @Override
        public boolean sendWelcome(UserEntity user, List<String> roleNames, IssuedUserToken token) {
            sent.add("welcome:" + user.getEmail() + ":" + String.join(",", roleNames));
            return deliver;
        }

        @Override
        public void sendRoleChanged(UserEntity user, List<String> roleNames) {
            sent.add("roles:" + user.getEmail() + ":" + String.join(",", roleNames));
        }

        @Override
        public void sendSuspended(UserEntity user) {
            sent.add("suspended:" + user.getEmail());
        }
    }

    /** Records reassign and delete calls. */
    static final class RecordingReassigner implements UserOwnedConfigReassigner {
        final List<String> calls = new ArrayList<>();

        @Override
        public String name() {
            return "testConfig";
        }

        @Override
        public int reassign(UUID owner, UUID target) {
            calls.add("reassign:" + owner + "->" + target);
            return 3;
        }

        @Override
        public int delete(UUID owner) {
            calls.add("delete:" + owner);
            return 2;
        }
    }

    /** The fakes, the mailer replacing the logging one. */
    @TestConfiguration
    static class Fakes {
        @Bean
        @Primary
        RecordingMailer recordingMailer() {
            return new RecordingMailer();
        }

        @Bean
        RecordingReassigner recordingReassigner() {
            return new RecordingReassigner();
        }
    }

    @Autowired private MockMvc mvc;
    @Autowired private JsonMapper json;
    @Autowired private PrincipalService principals;
    @Autowired private UserRepository users;
    @Autowired private UserRoleRepository userRoles;
    @Autowired private UserTokenRepository userTokens;
    @Autowired private PasswordHasher hasher;
    @Autowired private TokenService tokens;
    @Autowired private PermissionResolver resolver;
    @Autowired private AuditService audit;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private RecordingMailer mailer;
    @Autowired private RecordingReassigner reassigner;

    private String adminToken;

    @BeforeEach
    void setUp() {
        mailer.sent.clear();
        mailer.deliver = true;
        reassigner.calls.clear();
        adminToken = TestTokens.create(tokens, "users-it-admin", Scope.ADMIN).plaintext();
    }

    private UserEntity user(String email, BuiltInRole... roles) {
        return TestUsers.active(principals, users, userRoles, hasher, email, roles);
    }

    private MockHttpServletResponse call(MockHttpServletRequestBuilder builder, String bearer) throws Exception {
        builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer);
        return mvc.perform(builder).andReturn().getResponse();
    }

    private MockHttpServletResponse admin(MockHttpServletRequestBuilder builder) throws Exception {
        return call(builder, adminToken);
    }

    private static MockHttpServletRequestBuilder jsonBody(MockHttpServletRequestBuilder builder, String body) {
        return builder.contentType(MediaType.APPLICATION_JSON).content(body);
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

    private static String createBody(String email, BuiltInRole role, Boolean sendInvite) {
        return "{\"email\":\"" + email + "\",\"fullName\":\"New Person\",\"roles\":[\"" + role.id() + "\"]"
                + (sendInvite == null ? "" : ",\"sendInvite\":" + sendInvite) + "}";
    }

    private void assertCode(MockHttpServletResponse r, int status, String code) throws Exception {
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(status);
        assertThat(body(r).get("code").asString()).isEqualTo(code);
    }

    @Test
    @DisplayName("createUser invites: invited, no password, roles, an activation token and the welcome email")
    void createInvites() throws Exception {
        MockHttpServletResponse r = admin(jsonBody(post("/v1/users"),
                createBody("New@Example.com", BuiltInRole.EDITOR, null)));
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(201);
        JsonNode b = body(r);
        assertThat(b.get("email").asString()).isEqualTo("new@example.com");
        assertThat(b.get("status").asString()).isEqualTo("invited");
        assertThat(b.get("roles").get(0).get("name").asString()).isEqualTo("Editor");
        assertThat(b.get("activation").get("kind").asString()).isEqualTo("activation");
        assertThat(b.get("activation").get("emailed").asBoolean()).isTrue();
        assertThat(b.get("activation").get("token").isNull()).isTrue();
        UserEntity saved = users.findByEmail("new@example.com").orElseThrow();
        assertThat(saved.getPasswordHash()).isNull();
        assertThat(userTokens.findByUserIdAndKind(saved.getId(), UserTokenKind.ACTIVATION.wireValue())).hasSize(1);
        assertThat(mailer.sent).containsExactly("welcome:new@example.com:Editor");
    }

    @Test
    @DisplayName("with sendInvite false, or when the email does not leave, the activation token comes back once")
    void linkReturnedWhenNotEmailed() throws Exception {
        JsonNode b = body(admin(jsonBody(post("/v1/users"), createBody("a@example.com", BuiltInRole.VIEWER, false))));
        assertThat(b.get("activation").get("emailed").asBoolean()).isFalse();
        assertThat(b.get("activation").get("token").asString()).startsWith(UserTokenService.ACTIVATION_PREFIX);
        assertThat(mailer.sent).isEmpty();

        mailer.deliver = false;
        JsonNode c = body(admin(jsonBody(post("/v1/users"), createBody("b@example.com", BuiltInRole.VIEWER, true))));
        assertThat(c.get("activation").get("token").asString()).startsWith(UserTokenService.ACTIVATION_PREFIX);
    }

    @Test
    @DisplayName("a duplicate email is 409 EMAIL_TAKEN; an unknown role or bad email is 400")
    void createRefusals() throws Exception {
        user("taken@example.com", BuiltInRole.VIEWER);
        assertCode(admin(jsonBody(post("/v1/users"), createBody("TAKEN@example.com", BuiltInRole.VIEWER, null))),
                409, ErrorCodes.EMAIL_TAKEN);
        assertThat(admin(jsonBody(post("/v1/users"), "{\"email\":\"x@example.com\",\"fullName\":\"X\",\"roles\":[\""
                + UUID.randomUUID() + "\"]}")).getStatus()).isEqualTo(400);
        assertThat(admin(jsonBody(post("/v1/users"), "{\"email\":\"not an email\",\"fullName\":\"X\",\"roles\":[]}"))
                .getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("listUsers pages by email and filters by status and text; getUser and 404")
    void listAndGet() throws Exception {
        user("a1@example.com", BuiltInRole.VIEWER);
        user("a2@example.com", BuiltInRole.VIEWER);
        UserEntity s = user("b1@example.com", BuiltInRole.EDITOR);
        s.setStatus(UserStatus.SUSPENDED, s.getUpdatedAt());
        users.saveAndFlush(s);

        JsonNode first = body(admin(get("/v1/users?limit=2")));
        assertThat(first.get("items")).hasSize(2);
        assertThat(first.get("items").get(0).get("email").asString()).isEqualTo("a1@example.com");
        JsonNode second = body(admin(get("/v1/users?limit=2&cursor=" + first.get("nextCursor").asString())));
        assertThat(second.get("items").get(0).get("email").asString()).isEqualTo("b1@example.com");
        assertThat(second.get("nextCursor").isNull()).isTrue();

        assertThat(body(admin(get("/v1/users?status=suspended"))).get("items")).hasSize(1);
        assertThat(body(admin(get("/v1/users?q=A2"))).get("items")).hasSize(1);
        assertThat(admin(get("/v1/users?status=bogus")).getStatus()).isEqualTo(400);

        assertThat(body(admin(get("/v1/users/" + s.getId()))).get("status").asString()).isEqualTo("suspended");
        assertCode(admin(get("/v1/users/" + UUID.randomUUID())), 404, ErrorCodes.UNKNOWN_USER);
        assertCode(admin(get("/v1/users/not-a-uuid")), 404, ErrorCodes.UNKNOWN_USER);
    }

    @Test
    @DisplayName("updateUser changes the name and email, refusing a taken email")
    void update() throws Exception {
        UserEntity u = user("old@example.com", BuiltInRole.VIEWER);
        user("other@example.com", BuiltInRole.VIEWER);
        JsonNode b = body(admin(jsonBody(put("/v1/users/" + u.getId()),
                "{\"fullName\":\"Renamed\",\"email\":\"New@Example.com\"}")));
        assertThat(b.get("fullName").asString()).isEqualTo("Renamed");
        assertThat(b.get("email").asString()).isEqualTo("new@example.com");
        assertCode(admin(jsonBody(put("/v1/users/" + u.getId()), "{\"email\":\"other@example.com\"}")), 409,
                ErrorCodes.EMAIL_TAKEN);
        assertThat(users.findById(u.getId()).orElseThrow().getPasswordHash()).isEqualTo(u.getPasswordHash());
    }

    @Test
    @DisplayName("setUserRoles replaces the set, invalidates cached permissions and sends the role-changed email")
    void setRoles() throws Exception {
        UserEntity u = user("roles@example.com", BuiltInRole.VIEWER);
        String session = login("roles@example.com");
        assertThat(resolver.forUser(u.getId()).keys()).doesNotContain("audit.read");
        JsonNode b = body(admin(jsonBody(post("/v1/users/" + u.getId() + "/roles"),
                "{\"roles\":[\"" + BuiltInRole.AUDITOR.id() + "\"]}")));
        assertThat(b.get("roles")).hasSize(1);
        assertThat(b.get("roles").get(0).get("name").asString()).isEqualTo("Auditor");
        assertThat(resolver.forUser(u.getId()).keys()).contains("audit.read").doesNotContain("self.views");
        assertThat(call(get("/v1/audit"), session).getStatus()).isEqualTo(200);
        assertThat(mailer.sent).containsExactly("roles:roles@example.com:Auditor");
    }

    @Test
    @DisplayName("suspend revokes sessions and blocks sign-in; unsuspend restores")
    void suspendAndUnsuspend() throws Exception {
        UserEntity u = user("sus@example.com", BuiltInRole.VIEWER);
        String session = login("sus@example.com");
        assertThat(body(admin(post("/v1/users/" + u.getId() + "/suspend"))).get("status").asString())
                .isEqualTo("suspended");
        assertThat(call(get("/v1/me"), session).getStatus()).isEqualTo(401);
        assertThat(mvc.perform(jsonBody(post("/v1/auth/login"), TestUsers.loginBody("sus@example.com")))
                .andReturn().getResponse().getStatus()).isEqualTo(401);
        assertThat(mailer.sent).containsExactly("suspended:sus@example.com");
        assertCode(admin(post("/v1/users/" + u.getId() + "/suspend")), 409, ErrorCodes.USER_STATUS_CONFLICT);
        assertThat(body(admin(post("/v1/users/" + u.getId() + "/unsuspend"))).get("status").asString())
                .isEqualTo("active");
        login("sus@example.com");
    }

    @Test
    @DisplayName("resendUserInvite works only while invited and replaces the earlier token")
    void resendInvite() throws Exception {
        JsonNode created = body(admin(jsonBody(post("/v1/users"), createBody("inv@example.com", BuiltInRole.VIEWER,
                false))));
        String id = created.get("id").asString();
        String firstToken = created.get("activation").get("token").asString();
        mailer.deliver = false;
        MockHttpServletResponse r = admin(post("/v1/users/" + id + "/resend-invite"));
        assertThat(r.getStatus()).isEqualTo(202);
        assertThat(body(r).get("token").asString()).isNotEqualTo(firstToken);
        // The first link no longer activates.
        assertThat(mvc.perform(jsonBody(post("/v1/auth/activate"), "{\"token\":\"" + firstToken
                + "\",\"password\":\"" + TestUsers.PASSWORD + "\"}")).andReturn().getResponse().getStatus())
                .isEqualTo(410);
        UserEntity active = user("act@example.com", BuiltInRole.VIEWER);
        assertCode(admin(post("/v1/users/" + active.getId() + "/resend-invite")), 409, ErrorCodes.ALREADY_ACTIVE);
    }

    @Test
    @DisplayName("adminResetUserPassword issues a reset, ends sessions and reveals no password")
    void adminReset() throws Exception {
        UserEntity u = user("reset@example.com", BuiltInRole.VIEWER);
        String session = login("reset@example.com");
        MockHttpServletResponse r = admin(post("/v1/users/" + u.getId() + "/password-reset"));
        assertThat(r.getStatus()).isEqualTo(202);
        JsonNode b = body(r);
        assertThat(b.get("kind").asString()).isEqualTo("reset");
        assertThat(b.get("emailed").asBoolean()).isTrue();
        assertThat(b.get("token").isNull()).isTrue();
        assertThat(r.getContentAsString()).doesNotContain(TestUsers.PASSWORD).doesNotContain("argon2");
        assertThat(call(get("/v1/me"), session).getStatus()).isEqualTo(401);
        assertThat(mailer.sent).containsExactly("reset:reset@example.com");
    }

    @Test
    @DisplayName("delete reassigns owned config to the acting admin by default, then removes the user")
    void deleteReassignsToActor() throws Exception {
        UserEntity actor = user("boss@example.com", BuiltInRole.ADMIN);
        UserEntity gone = user("gone@example.com", BuiltInRole.EDITOR);
        String session = login("boss@example.com");
        JsonNode b = body(call(delete("/v1/users/" + gone.getId()), session));
        assertThat(b.get("deletedUserId").asString()).isEqualTo(gone.getId().toString());
        assertThat(b.get("ownedConfigDisposition").asString()).isEqualTo("reassigned");
        assertThat(b.get("reassignedTo").asString()).isEqualTo(actor.getId().toString());
        assertThat(reassigner.calls).containsExactly("reassign:" + gone.getId() + "->" + actor.getId());
        assertThat(users.findById(gone.getId())).isEmpty();
        assertThat(userRoles.findByUserId(gone.getId())).isEmpty();
        audit.flush();
        assertThat(jdbc.queryForObject("select count(*) from audit_event where operation_id = 'deleteUser' and "
                + "detail->>'event' = 'user_deleted' and target_id = ?", Long.class, gone.getId().toString()))
                .isEqualTo(1L);
    }

    @Test
    @DisplayName("with an API token the oldest other admin receives the config; reassign=false deletes it")
    void deleteTargets() throws Exception {
        UserEntity oldest = user("first@example.com", BuiltInRole.ADMIN);
        user("second@example.com", BuiltInRole.ADMIN);
        UserEntity a = user("x1@example.com", BuiltInRole.VIEWER);
        UserEntity b = user("x2@example.com", BuiltInRole.VIEWER);
        assertThat(body(admin(delete("/v1/users/" + a.getId()))).get("reassignedTo").asString())
                .isEqualTo(oldest.getId().toString());
        JsonNode deleted = body(admin(delete("/v1/users/" + b.getId() + "?reassign=false")));
        assertThat(deleted.get("ownedConfigDisposition").asString()).isEqualTo("deleted");
        assertThat(deleted.get("reassignedTo").isNull()).isTrue();
        assertThat(reassigner.calls).containsExactly("reassign:" + a.getId() + "->" + oldest.getId(),
                "delete:" + b.getId());
    }

    @Test
    @DisplayName("the last admin cannot be deleted, suspended or stripped of users.manage: 409 LAST_ADMIN")
    void lastAdminGuard() throws Exception {
        UserEntity only = user("only@example.com", BuiltInRole.ADMIN);
        assertCode(admin(delete("/v1/users/" + only.getId())), 409, ErrorCodes.LAST_ADMIN);
        assertCode(admin(post("/v1/users/" + only.getId() + "/suspend")), 409, ErrorCodes.LAST_ADMIN);
        assertCode(admin(jsonBody(post("/v1/users/" + only.getId() + "/roles"),
                "{\"roles\":[\"" + BuiltInRole.OPERATOR.id() + "\"]}")), 409, ErrorCodes.LAST_ADMIN);
        // A second admin lifts the guard.
        user("two@example.com", BuiltInRole.ADMIN);
        assertThat(admin(post("/v1/users/" + only.getId() + "/suspend")).getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("a non-admin is not guarded: deleting the only Viewer of an instance without admins works")
    void nonAdminNotGuarded() throws Exception {
        UserEntity v = user("lonely@example.com", BuiltInRole.VIEWER);
        JsonNode b = body(admin(delete("/v1/users/" + v.getId())));
        assertThat(b.get("ownedConfigDisposition").asString()).isEqualTo("reassigned");
        assertThat(b.get("reassignedTo").isNull()).isTrue();
    }

    @Test
    @DisplayName("every mutation is audited")
    void mutationsAudited() throws Exception {
        UserEntity u = user("aud@example.com", BuiltInRole.VIEWER);
        admin(jsonBody(put("/v1/users/" + u.getId()), "{\"fullName\":\"Audited\"}"));
        admin(jsonBody(post("/v1/users/" + u.getId() + "/roles"), "{\"roles\":[]}"));
        admin(post("/v1/users/" + u.getId() + "/suspend"));
        admin(post("/v1/users/" + u.getId() + "/unsuspend"));
        admin(post("/v1/users/" + u.getId() + "/password-reset"));
        admin(jsonBody(post("/v1/users"), createBody("aud2@example.com", BuiltInRole.VIEWER, false)));
        audit.flush();
        List<String> ops = jdbc.queryForList("select operation_id from audit_event where path like '/v1/users%'",
                String.class);
        assertThat(ops).contains("updateUser", "setUserRoles", "suspendUser", "unsuspendUser",
                "adminResetUserPassword", "createUser");
    }

    @Test
    @DisplayName("a session without users.manage is refused every user operation")
    void gated() throws Exception {
        user("viewer@example.com", BuiltInRole.VIEWER);
        String session = login("viewer@example.com");
        MockHttpServletResponse r = call(get("/v1/users"), session);
        assertCode(r, 403, ErrorCodes.PERMISSION_DENIED);
    }
}
