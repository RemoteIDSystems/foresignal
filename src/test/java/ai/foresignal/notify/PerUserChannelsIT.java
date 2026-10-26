package ai.foresignal.notify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.user.BuiltInRole;
import ai.foresignal.security.user.PasswordHasher;
import ai.foresignal.security.user.UserAdminService;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.security.user.UserRoleRepository;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Personal and global notification channels through the real filter chain (prompt R2-009): who may create, see,
 * change and delete which channel, and what happens to a deleted user's channels.
 */
@IntegrationTest
class PerUserChannelsIT {

    static final String WEBHOOK = "{\"name\":\"%s\",\"type\":\"webhook\",\"config\":{\"url\":\"http://127.0.0.1:9/%s\"}%s}";

    @Autowired private MockMvc mvc;
    @Autowired private JsonMapper json;
    @Autowired private PrincipalService principals;
    @Autowired private UserRepository users;
    @Autowired private UserRoleRepository userRoles;
    @Autowired private PasswordHasher hasher;
    @Autowired private TokenService tokens;
    @Autowired private NotificationChannelRepository channels;
    @Autowired private UserAdminService userAdmin;

    private String adminToken;
    private UserEntity alice;
    private String aliceSession;
    private String bobSession;
    private String operatorSession;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = TestTokens.create(tokens, "channels-it-admin", Scope.ADMIN).plaintext();
        alice = TestUsers.active(principals, users, userRoles, hasher, "alice@example.com", BuiltInRole.EDITOR);
        TestUsers.active(principals, users, userRoles, hasher, "bob@example.com", BuiltInRole.EDITOR);
        TestUsers.active(principals, users, userRoles, hasher, "op@example.com", BuiltInRole.OPERATOR);
        aliceSession = login("alice@example.com");
        bobSession = login("bob@example.com");
        operatorSession = login("op@example.com");
    }

    // ------------------------------------------------------------------------- helpers

    private MockHttpServletResponse call(MockHttpServletRequestBuilder builder, String bearer) throws Exception {
        builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer);
        return mvc.perform(builder).andReturn().getResponse();
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

    private static String webhook(String name, String extra) {
        return WEBHOOK.formatted(name, name, extra);
    }

    private JsonNode create(String bearer, String name, String extra) throws Exception {
        MockHttpServletResponse r = call(jsonBody(post("/v1/channels"), webhook(name, extra)), bearer);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(201);
        return body(r);
    }

    private List<String> names(String bearer) throws Exception {
        MockHttpServletResponse r = call(get("/v1/channels"), bearer);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        List<String> out = new ArrayList<>();
        body(r).forEach(c -> out.add(c.get("name").asString()));
        return out;
    }

    private void assertDenied(MockHttpServletResponse r, String permission) throws Exception {
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(403);
        assertThat(body(r).get("code").asString()).isEqualTo(ErrorCodes.PERMISSION_DENIED);
        assertThat(body(r).get("requiredPermission").asString()).isEqualTo(permission);
    }

    // ------------------------------------------------------------------------- ownership

    @Test
    @DisplayName("an Editor's channel is personal; an Operator's and an API token's are global")
    void kinds() throws Exception {
        JsonNode mine = create(aliceSession, "alice", "");
        assertThat(mine.get("global").asBoolean()).isFalse();
        assertThat(mine.get("ownerPrincipalId").asString()).isEqualTo(alice.getPrincipalId());
        JsonNode operators = create(operatorSession, "ops", "");
        assertThat(operators.get("global").asBoolean()).isTrue();
        assertThat(operators.get("ownerPrincipalId").isNull()).isTrue();
        JsonNode machine = create(adminToken, "machine", "");
        assertThat(machine.get("global").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("asking for the other kind needs its permission: global needs channels.global, personal self.channels")
    void explicitKind() throws Exception {
        assertDenied(call(jsonBody(post("/v1/channels"), webhook("g", ",\"global\":true")), aliceSession),
                "channels.global");
        assertDenied(call(jsonBody(post("/v1/channels"), webhook("p", ",\"global\":false")), operatorSession),
                "self.channels");
        assertThat(call(jsonBody(post("/v1/channels"), webhook("x", ",\"global\":\"yes\"")), aliceSession).getStatus())
                .isEqualTo(400);
    }

    @Test
    @DisplayName("a user lists their own and the global channels; an Operator the global ones; an admin token all")
    void listing() throws Exception {
        create(aliceSession, "alice", "");
        create(bobSession, "bob", "");
        create(operatorSession, "ops", "");
        assertThat(names(aliceSession)).containsExactlyInAnyOrder("alice", "ops");
        assertThat(names(bobSession)).containsExactlyInAnyOrder("bob", "ops");
        assertThat(names(operatorSession)).containsExactly("ops");
        assertThat(names(adminToken)).contains("alice", "bob", "ops");
    }

    @Test
    @DisplayName("another user's channel is 404 on every per-channel operation")
    void crossUser() throws Exception {
        String id = create(aliceSession, "alice", "").get("id").asString();
        assertThat(call(get("/v1/channels/" + id), bobSession).getStatus()).isEqualTo(404);
        assertThat(call(jsonBody(put("/v1/channels/" + id), webhook("taken", "")), bobSession).getStatus())
                .isEqualTo(404);
        assertThat(call(post("/v1/channels/" + id + "/test"), bobSession).getStatus()).isEqualTo(404);
        assertThat(call(get("/v1/channels/" + id + "/deliveries"), bobSession).getStatus()).isEqualTo(404);
        assertThat(call(delete("/v1/channels/" + id), bobSession).getStatus()).isEqualTo(404);
        assertThat(call(get("/v1/channels/" + id), operatorSession).getStatus()).isEqualTo(404);
        assertThat(channels.existsById(UUID.fromString(id))).isTrue();
        assertThat(call(delete("/v1/channels/" + id), aliceSession).getStatus()).isEqualTo(204);
    }

    @Test
    @DisplayName("a user may read a global channel but not change or delete it; an Operator may")
    void globalManagedByOperator() throws Exception {
        String id = create(operatorSession, "ops", "").get("id").asString();
        assertThat(call(get("/v1/channels/" + id), aliceSession).getStatus()).isEqualTo(200);
        assertDenied(call(jsonBody(put("/v1/channels/" + id), webhook("mine", "")), aliceSession), "channels.global");
        assertDenied(call(delete("/v1/channels/" + id), aliceSession), "channels.global");
        MockHttpServletResponse renamed = call(jsonBody(put("/v1/channels/" + id), webhook("ops2", "")),
                operatorSession);
        assertThat(renamed.getStatus()).as(renamed.getContentAsString()).isEqualTo(200);
        assertThat(body(renamed).get("global").asBoolean()).as("a replace keeps the kind").isTrue();
        assertThat(call(delete("/v1/channels/" + id), operatorSession).getStatus()).isEqualTo(204);
    }

    @Test
    @DisplayName("a Viewer reaches no channel operation, and escalation policies need channels.global")
    void gates() throws Exception {
        TestUsers.active(principals, users, userRoles, hasher, "viewer@example.com", BuiltInRole.VIEWER);
        assertDenied(call(get("/v1/channels"), login("viewer@example.com")), "self.channels");
        assertDenied(call(get("/v1/escalation-policies"), aliceSession), "channels.global");
        assertThat(call(get("/v1/escalation-policies"), operatorSession).getStatus()).isEqualTo(200);
    }

    // ------------------------------------------------------------------------- reassignment

    @Test
    @DisplayName("deleting a user moves their channels to the acting admin")
    void reassigned() throws Exception {
        UserEntity admin = TestUsers.active(principals, users, userRoles, hasher, "admin@example.com",
                BuiltInRole.ADMIN);
        UUID id = UUID.fromString(create(aliceSession, "alice", "").get("id").asString());
        UserAdminService.Deletion deletion = userAdmin.delete(alice.getId(), true, admin.getId(), "127.0.0.1");
        assertThat(deletion.counts()).containsEntry("channels", 1);
        NotificationChannelEntity moved = channels.findById(id).orElseThrow();
        assertThat(moved.getOwnerPrincipalId()).isEqualTo(admin.getPrincipalId());
        assertThat(moved.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("with no admin to receive them, a deleted user's channels become global and disabled")
    void orphaned() throws Exception {
        UUID id = UUID.fromString(create(aliceSession, "alice", "").get("id").asString());
        userAdmin.delete(alice.getId(), true, null, "127.0.0.1");
        NotificationChannelEntity orphan = channels.findById(id).orElseThrow();
        assertThat(orphan.getOwnerPrincipalId()).isNull();
        assertThat(orphan.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("reassign=false deletes the user's channels")
    void deleted() throws Exception {
        TestUsers.active(principals, users, userRoles, hasher, "admin@example.com", BuiltInRole.ADMIN);
        UUID id = UUID.fromString(create(aliceSession, "alice", "").get("id").asString());
        userAdmin.delete(alice.getId(), false, null, "127.0.0.1");
        assertThat(channels.existsById(id)).isFalse();
    }
}
