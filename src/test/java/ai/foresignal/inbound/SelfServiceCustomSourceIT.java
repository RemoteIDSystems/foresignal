package ai.foresignal.inbound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.user.PasswordHasher;
import ai.foresignal.security.user.Permissions;
import ai.foresignal.security.user.RoleEntity;
import ai.foresignal.security.user.RolePermissionEntity;
import ai.foresignal.security.user.RolePermissionRepository;
import ai.foresignal.security.user.RoleRepository;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.security.user.UserRoleEntity;
import ai.foresignal.security.user.UserRoleRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestUsers;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Self-service custom sources over HTTP (prompt SG-001): a signed-in user holding {@code self.sources} but no agent
 * scope creates, lists, reads, updates, deletes and re-keys their own custom sources through
 * {@code /v1/me/custom-sources}; {@code ownerUserId} is forced to the caller, another user's source is {@code 404}, and
 * an API token is {@code 401}.
 */
@IntegrationTest
@AutoConfigureMockMvc
class SelfServiceCustomSourceIT {

    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired MockMvc mvc;
    @Autowired PrincipalService principals;
    @Autowired UserRepository users;
    @Autowired UserRoleRepository userRoles;
    @Autowired RoleRepository roles;
    @Autowired RolePermissionRepository rolePermissions;
    @Autowired PasswordHasher hasher;
    @Autowired AdminTokenService adminTokens;

    String selfSession;
    String otherSession;
    String agent;

    @BeforeEach
    void setUp() throws Exception {
        selfEditionsUser("self-source@example.com");
        selfEditionsUser("other-source@example.com");
        selfSession = login("self-source@example.com");
        otherSession = login("other-source@example.com");
        agent = "Bearer " + adminTokens.rotate();
    }

    /** An active user whose only role holds {@code self.sources} and {@code data.read}: a read-scope session. */
    private UserEntity selfEditionsUser(String email) {
        UserEntity user = TestUsers.active(principals, users, userRoles, hasher, email);
        Instant now = Instant.now();
        UUID roleId = UUID.randomUUID();
        roles.saveAndFlush(new RoleEntity(roleId, "Sources " + email, "self.sources only", false, now));
        rolePermissions.saveAndFlush(new RolePermissionEntity(roleId, Permissions.SELF_SOURCES.key()));
        rolePermissions.saveAndFlush(new RolePermissionEntity(roleId, Permissions.DATA_READ.key()));
        userRoles.saveAndFlush(new UserRoleEntity(user.getId(), roleId));
        return user;
    }

    private String login(String email) throws Exception {
        MockHttpServletResponse response = mvc.perform(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(TestUsers.loginBody(email))).andReturn().getResponse();
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(200);
        return "Bearer " + JSON.readTree(response.getContentAsString()).get("token").asString();
    }

    /** A minimal valid {@code CustomSourceInput} for {@code id}. */
    static String sourceBody(String id, String name) {
        return ("{'id':'" + id + "','name':'" + name + "','kinds':['sensor-reading'],'observedAtMode':'RECEIVED',"
                + "'ttlAnchor':'RECEIVED','titleField':'name'}").replace('\'', '"');
    }

    private JsonNode send(MockHttpServletRequestBuilder request, String bearer, String body, int expected)
            throws Exception {
        if (bearer != null) {
            request.header(HttpHeaders.AUTHORIZATION, bearer);
        }
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        MockHttpServletResponse response = mvc.perform(request).andReturn().getResponse();
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(expected);
        String text = response.getContentAsString();
        return text.isBlank() ? null : JSON.readTree(text);
    }

    @Test
    @DisplayName("a self.sources user without agent scope manages their own custom sources but not the operator surface")
    void worksWithoutAgentScope() throws Exception {
        String id = "custom-mine-" + Long.toString(System.nanoTime() % 1_000_000, 36);
        // The operator create needs admin; this read-scope session is turned away.
        send(post("/v1/custom-sources"), selfSession, sourceBody(id, "Blocked"), 403);
        // The self-service create needs only the self.sources permission; the key is shown once.
        JsonNode created = send(post("/v1/me/custom-sources"), selfSession, sourceBody(id, "Mine"), 201);
        assertThat(created.get("ingestKey").asString()).startsWith("eik_");
        assertThat(created.get("ingestKeyPrefix").asString()).isEqualTo(created.get("ingestKey").asString()
                .substring(0, 8));
        // The list shows only the caller's own source; get and update work.
        JsonNode list = send(get("/v1/me/custom-sources"), selfSession, null, 200);
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.get(0).get("id").asString()).isEqualTo(id);
        assertThat(send(get("/v1/me/custom-sources/" + id), selfSession, null, 200).get("name").asString())
                .isEqualTo("Mine");
        assertThat(send(put("/v1/me/custom-sources/" + id), selfSession, sourceBody(id, "Renamed"), 200).get("name")
                .asString()).isEqualTo("Renamed");
        // Rotating the key returns a fresh one.
        String rotated = send(post("/v1/me/custom-sources/" + id + "/ingest-key/rotate"), selfSession, null, 200)
                .get("ingestKey").asString();
        assertThat(rotated).startsWith("eik_").isNotEqualTo(created.get("ingestKey").asString());
        // Delete removes it.
        send(delete("/v1/me/custom-sources/" + id), selfSession, null, 204);
        assertThat(send(get("/v1/me/custom-sources"), selfSession, null, 200).size()).isZero();
    }

    @Test
    @DisplayName("another user's custom source is 404 to get, update, delete and rotate; a session is required")
    void isolatedAndSessionOnly() throws Exception {
        String id = "custom-private-" + Long.toString(System.nanoTime() % 1_000_000, 36);
        send(post("/v1/me/custom-sources"), selfSession, sourceBody(id, "Private"), 201);

        // The other user neither sees nor reaches it.
        assertThat(send(get("/v1/me/custom-sources"), otherSession, null, 200).size()).isZero();
        send(get("/v1/me/custom-sources/" + id), otherSession, null, 404);
        send(put("/v1/me/custom-sources/" + id), otherSession, sourceBody(id, "Taken"), 404);
        send(post("/v1/me/custom-sources/" + id + "/ingest-key/rotate"), otherSession, null, 404);
        send(delete("/v1/me/custom-sources/" + id), otherSession, null, 404);

        // An API token has no session, so every self-service operation is 401.
        send(get("/v1/me/custom-sources"), agent, null, 401);
        send(post("/v1/me/custom-sources"), agent, sourceBody("custom-tokened-x", "Tokened"), 401);
        send(get("/v1/me/custom-sources/" + id), agent, null, 401);
        send(put("/v1/me/custom-sources/" + id), agent, sourceBody(id, "Tokened"), 401);
        send(delete("/v1/me/custom-sources/" + id), agent, null, 401);
    }
}
