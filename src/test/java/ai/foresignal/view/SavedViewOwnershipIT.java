package ai.foresignal.view;

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
import ai.foresignal.settings.AppSettingService;
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
 * Saved views owned by users through the real filter chain (prompt R2-012): a user lists, reads and changes only their
 * own and the shared views, another user's view is {@code 404} by id and by slug, a shared view is read-only to a
 * user, {@code self.views} gates the operations, and deleting a user moves or deletes their views.
 */
@IntegrationTest
class SavedViewOwnershipIT {

    @Autowired private MockMvc mvc;
    @Autowired private JsonMapper json;
    @Autowired private PrincipalService principals;
    @Autowired private UserRepository users;
    @Autowired private UserRoleRepository userRoles;
    @Autowired private PasswordHasher hasher;
    @Autowired private TokenService tokens;
    @Autowired private SavedViewRepository views;
    @Autowired private UserAdminService userAdmin;
    @Autowired private AppSettingService settings;

    private String adminToken;
    private UserEntity alice;
    private UserEntity root;
    private String aliceSession;
    private String bobSession;
    private String viewerSession;
    private String auditorSession;
    private String rootSession;

    @BeforeEach
    void setUp() throws Exception {
        settings.invalidateCache();
        adminToken = TestTokens.create(tokens, "views-it-admin", Scope.ADMIN).plaintext();
        alice = TestUsers.active(principals, users, userRoles, hasher, "alice@example.com", BuiltInRole.EDITOR);
        TestUsers.active(principals, users, userRoles, hasher, "bob@example.com", BuiltInRole.EDITOR);
        TestUsers.active(principals, users, userRoles, hasher, "viewer@example.com", BuiltInRole.VIEWER);
        TestUsers.active(principals, users, userRoles, hasher, "audit@example.com", BuiltInRole.AUDITOR);
        root = TestUsers.active(principals, users, userRoles, hasher, "root@example.com", BuiltInRole.ADMIN);
        aliceSession = login("alice@example.com");
        bobSession = login("bob@example.com");
        viewerSession = login("viewer@example.com");
        auditorSession = login("audit@example.com");
        rootSession = login("root@example.com");
    }

    // ------------------------------------------------------------------------- helpers

    private MockHttpServletResponse call(MockHttpServletRequestBuilder builder, String bearer) throws Exception {
        if (bearer != null) {
            builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer);
        }
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

    private static String view(String slug) {
        return "{'slug':'" + slug + "','name':'View " + slug + "'}";
    }

    private JsonNode create(String bearer, String slug) throws Exception {
        MockHttpServletResponse r = call(jsonBody(post("/v1/views"), view(slug)), bearer);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(201);
        return body(r);
    }

    private List<String> slugs(String bearer) throws Exception {
        MockHttpServletResponse r = call(get("/v1/views"), bearer);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        List<String> out = new ArrayList<>();
        body(r).get("items").forEach(v -> out.add(v.get("slug").asString()));
        return out;
    }

    private void assertDenied(MockHttpServletResponse r, String permission) throws Exception {
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(403);
        assertThat(body(r).get("code").asString()).isEqualTo(ErrorCodes.PERMISSION_DENIED);
        assertThat(body(r).get("requiredPermission").asString()).isEqualTo(permission);
    }

    private void assertUnknown(MockHttpServletResponse r) throws Exception {
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(404);
        assertThat(body(r).get("code").asString()).isEqualTo(ErrorCodes.UNKNOWN_VIEW);
    }

    // ------------------------------------------------------------------------- tests

    @Test
    @DisplayName("a user's view is theirs: listed for them, 404 to another user by id and by slug")
    void ownViewsOnly() throws Exception {
        JsonNode mine = create(aliceSession, "alice-ships");
        assertThat(mine.get("shared").asBoolean()).isFalse();
        assertThat(mine.get("ownerPrincipalId").asString()).isEqualTo(alice.getPrincipalId());
        create(bobSession, "bob-quakes");
        create(adminToken, "instance-fires");

        assertThat(slugs(aliceSession)).containsExactly("alice-ships", "instance-fires");
        assertThat(slugs(bobSession)).containsExactly("bob-quakes", "instance-fires");
        assertThat(slugs(adminToken)).containsExactly("alice-ships", "bob-quakes", "instance-fires");
        assertThat(slugs(rootSession)).as("users.manage sees every view")
                .containsExactly("alice-ships", "bob-quakes", "instance-fires");

        String id = mine.get("id").asString();
        assertUnknown(call(get("/v1/views/" + id), bobSession));
        assertUnknown(call(get("/v1/views/alice-ships"), bobSession));
        assertUnknown(call(jsonBody(put("/v1/views/" + id), view("alice-ships")), bobSession));
        assertUnknown(call(delete("/v1/views/alice-ships"), bobSession));
        assertUnknown(call(get("/v1/views/" + id + "/records"), bobSession));
        assertUnknown(call(get("/v1/views/" + id + "/tiles.json"), bobSession));

        assertThat(call(get("/v1/views/" + id), aliceSession).getStatus()).isEqualTo(200);
        MockHttpServletResponse replaced = call(jsonBody(put("/v1/views/" + id),
                "{'slug':'alice-ships','name':'Renamed'}"), aliceSession);
        assertThat(replaced.getStatus()).as(replaced.getContentAsString()).isEqualTo(200);
        assertThat(body(replaced).get("ownerPrincipalId").asString()).as("a replace keeps the owner")
                .isEqualTo(alice.getPrincipalId());
        assertThat(call(delete("/v1/views/" + id), aliceSession).getStatus()).isEqualTo(204);
    }

    @Test
    @DisplayName("a shared view is readable by users and changed only by an admin or a token")
    void sharedViews() throws Exception {
        JsonNode shared = create(adminToken, "instance-fires");
        assertThat(shared.get("shared").asBoolean()).isTrue();
        assertThat(shared.get("ownerPrincipalId").isNull()).isTrue();
        String id = shared.get("id").asString();

        assertThat(call(get("/v1/views/" + id), aliceSession).getStatus()).isEqualTo(200);
        assertDenied(call(jsonBody(put("/v1/views/" + id), view("instance-fires")), aliceSession), "users.manage");
        assertDenied(call(delete("/v1/views/" + id), aliceSession), "users.manage");
        assertThat(call(jsonBody(put("/v1/views/" + id), view("instance-fires")), rootSession).getStatus())
                .isEqualTo(200);
        assertThat(call(jsonBody(put("/v1/views/" + id), view("instance-fires")), adminToken).getStatus())
                .isEqualTo(200);
        assertThat(views.findById(UUID.fromString(id)).orElseThrow().getOwnerPrincipalId()).isNull();
    }

    @Test
    @DisplayName("slugs stay unique across users: a taken slug is 409 whoever owns it")
    void slugsAreGlobal() throws Exception {
        create(aliceSession, "harbor");
        MockHttpServletResponse r = call(jsonBody(post("/v1/views"), view("harbor")), bobSession);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(409);
        assertThat(body(r).get("code").asString()).isEqualTo(ErrorCodes.VIEW_EXISTS);
    }

    @Test
    @DisplayName("self.views gates the writes; the reads also take data.read")
    void gate() throws Exception {
        JsonNode viewers = create(viewerSession, "viewer-own");
        assertThat(viewers.get("shared").asBoolean()).as("a Viewer holds self.views and owns what it saves")
                .isFalse();
        create(adminToken, "instance-fires");
        assertDenied(call(jsonBody(post("/v1/views"), view("auditor-own")), auditorSession), "self.views");
        assertThat(slugs(auditorSession)).as("an Auditor reads the shared views with data.read")
                .containsExactly("instance-fires");
    }

    @Test
    @DisplayName("deleting a user moves their views to the receiving admin, or deletes them")
    void deletion() throws Exception {
        UUID kept = UUID.fromString(create(aliceSession, "alice-kept").get("id").asString());
        UserAdminService.Deletion d = userAdmin.delete(alice.getId(), true, root.getId(), "127.0.0.1");
        assertThat(d.counts()).containsEntry("savedViews", 1);
        assertThat(views.findById(kept).orElseThrow().getOwnerPrincipalId()).isEqualTo(root.getPrincipalId());

        UserEntity bob = users.findByEmail("bob@example.com").orElseThrow();
        UUID gone = UUID.fromString(create(bobSession, "bob-gone").get("id").asString());
        d = userAdmin.delete(bob.getId(), false, root.getId(), "127.0.0.1");
        assertThat(d.counts()).containsEntry("savedViews", 1);
        assertThat(views.findById(gone)).isEmpty();
    }

    @Test
    @DisplayName("the settings export carries the shared views only")
    void exportSharedOnly() throws Exception {
        create(aliceSession, "alice-private");
        create(adminToken, "instance-fires");
        MockHttpServletResponse r = call(get("/v1/settings/export?includePlaces=true"), adminToken);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        List<String> exported = new ArrayList<>();
        body(r).get("views").forEach(v -> exported.add(v.get("slug").asString()));
        assertThat(exported).containsExactly("instance-fires");
    }
}
