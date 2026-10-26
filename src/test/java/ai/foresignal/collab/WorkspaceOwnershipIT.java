package ai.foresignal.collab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.object.WorkspaceRepository;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.user.BuiltInRole;
import ai.foresignal.security.user.PasswordHasher;
import ai.foresignal.security.user.UserAdminService;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.security.user.UserRoleRepository;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestUsers;
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
 * Workspaces owned by signed-in users (prompt R2-012): a user's workspace is owned by the user's principal, sharing by
 * membership still works (a Viewer opens one shared with them through {@code data.read}), a non-member gets
 * {@code 404}, {@code self.workspaces} gates creating one, and deleting the user moves ownership to the receiving
 * admin with the members kept.
 */
@IntegrationTest
class WorkspaceOwnershipIT {

    @Autowired private MockMvc mvc;
    @Autowired private JsonMapper json;
    @Autowired private PrincipalService principals;
    @Autowired private UserRepository users;
    @Autowired private UserRoleRepository userRoles;
    @Autowired private PasswordHasher hasher;
    @Autowired private WorkspaceRepository workspaces;
    @Autowired private UserAdminService userAdmin;
    @Autowired private AppSettingService settings;

    private UserEntity alice;
    private UserEntity viewer;
    private UserEntity root;
    private String aliceSession;
    private String bobSession;
    private String viewerSession;

    @BeforeEach
    void setUp() throws Exception {
        settings.invalidateCache();
        alice = TestUsers.active(principals, users, userRoles, hasher, "alice@example.com", BuiltInRole.EDITOR);
        TestUsers.active(principals, users, userRoles, hasher, "bob@example.com", BuiltInRole.EDITOR);
        viewer = TestUsers.active(principals, users, userRoles, hasher, "viewer@example.com", BuiltInRole.VIEWER);
        root = TestUsers.active(principals, users, userRoles, hasher, "root@example.com", BuiltInRole.ADMIN);
        aliceSession = login("alice@example.com");
        bobSession = login("bob@example.com");
        viewerSession = login("viewer@example.com");
    }

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

    private String createShared() throws Exception {
        MockHttpServletResponse r = call(jsonBody(post("/v1/workspaces"), "{'name':'Harbor watch','members':{'"
                + viewer.getPrincipalId() + "':'viewer'}}"), aliceSession);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(201);
        return body(r).get("id").asString();
    }

    @Test
    @DisplayName("a user's workspace is owned by the user's principal and shared by membership")
    void ownedAndShared() throws Exception {
        String id = createShared();
        JsonNode own = body(call(get("/v1/workspaces/" + id), aliceSession));
        assertThat(own.get("owner").asString()).isEqualTo(alice.getPrincipalId());
        assertThat(own.get("callerRole").asString()).isEqualTo("owner");

        MockHttpServletResponse shared = call(get("/v1/workspaces/" + id), viewerSession);
        assertThat(shared.getStatus()).as("a Viewer member opens it through data.read: " + shared.getContentAsString())
                .isEqualTo(200);
        assertThat(body(shared).get("callerRole").asString()).isEqualTo("viewer");
        assertThat(body(call(get("/v1/workspaces"), viewerSession)).toString()).contains(id);

        MockHttpServletResponse other = call(get("/v1/workspaces/" + id), bobSession);
        assertThat(other.getStatus()).as(other.getContentAsString()).isEqualTo(404);
        assertThat(body(call(get("/v1/workspaces"), bobSession)).toString()).doesNotContain(id);
    }

    @Test
    @DisplayName("creating a workspace needs self.workspaces")
    void createNeedsPermission() throws Exception {
        MockHttpServletResponse r = call(jsonBody(post("/v1/workspaces"), "{'name':'Mine'}"), viewerSession);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(403);
        assertThat(body(r).get("code").asString()).isEqualTo(ErrorCodes.PERMISSION_DENIED);
        assertThat(body(r).get("requiredPermission").asString()).isEqualTo("self.workspaces");
    }

    @Test
    @DisplayName("deleting the owner hands the workspace to the receiving admin and keeps its members")
    void deletionReassigns() throws Exception {
        String id = createShared();
        UserAdminService.Deletion d = userAdmin.delete(alice.getId(), true, root.getId(), "127.0.0.1");
        assertThat(d.counts()).containsEntry("workspaces", 1);
        var w = workspaces.findById(id).orElseThrow();
        assertThat(w.getOwner()).isEqualTo(root.getPrincipalId());
        assertThat(w.getMembers()).containsEntry(viewer.getPrincipalId(), "viewer");
    }

    @Test
    @DisplayName("deleting the owner with reassign=false deletes the workspace")
    void deletionDeletes() throws Exception {
        String id = createShared();
        UserAdminService.Deletion d = userAdmin.delete(alice.getId(), false, root.getId(), "127.0.0.1");
        assertThat(d.counts()).containsEntry("workspaces", 1);
        assertThat(workspaces.findById(id)).isEmpty();
    }
}
