package ai.foresignal.source.group;

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
 * Source groups over HTTP (prompt SG-002): a {@code self.sources} session creates a group (owner forced, members
 * validated, unknown id 400), lists (cursor-paged) only its own, reads/updates/deletes it; another user's group is
 * {@code 404}; an API token is {@code 401}.
 */
@IntegrationTest
@AutoConfigureMockMvc
class SourceGroupCrudIT {

    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired MockMvc mvc;
    @Autowired PrincipalService principals;
    @Autowired UserRepository users;
    @Autowired UserRoleRepository userRoles;
    @Autowired RoleRepository roles;
    @Autowired RolePermissionRepository rolePermissions;
    @Autowired PasswordHasher hasher;
    @Autowired AdminTokenService adminTokens;

    String session;
    String otherSession;
    String agent;
    String member;

    @BeforeEach
    void setUp() throws Exception {
        sourcesUser("group-owner@example.com");
        sourcesUser("group-other@example.com");
        session = login("group-owner@example.com");
        otherSession = login("group-other@example.com");
        agent = "Bearer " + adminTokens.rotate();
        // A custom source the owner owns, used as a valid group member.
        member = "custom-member-" + Long.toString(System.nanoTime() % 1_000_000, 36);
        String body = ("{'id':'" + member + "','name':'Member','kinds':['sensor-reading'],'observedAtMode':'RECEIVED',"
                + "'ttlAnchor':'RECEIVED','titleField':'name'}").replace('\'', '"');
        assertThat(send(post("/v1/me/custom-sources"), session, body, 201)).isNotNull();
    }

    private UUID sourcesUser(String email) {
        UserEntity user = TestUsers.active(principals, users, userRoles, hasher, email);
        Instant now = Instant.now();
        UUID roleId = UUID.randomUUID();
        roles.saveAndFlush(new RoleEntity(roleId, "Sources " + email, "self.sources only", false, now));
        rolePermissions.saveAndFlush(new RolePermissionEntity(roleId, Permissions.SELF_SOURCES.key()));
        rolePermissions.saveAndFlush(new RolePermissionEntity(roleId, Permissions.DATA_READ.key()));
        userRoles.saveAndFlush(new UserRoleEntity(user.getId(), roleId));
        return user.getId();
    }

    private String login(String email) throws Exception {
        MockHttpServletResponse response = mvc.perform(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(TestUsers.loginBody(email))).andReturn().getResponse();
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(200);
        return "Bearer " + JSON.readTree(response.getContentAsString()).get("token").asString();
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

    private String groupBody(String name) {
        return ("{'name':'" + name + "','sourceIds':['" + member + "'],"
                + "'correlation':{'correlationLevel':'aggressive'},"
                + "'notificationThreshold':{'minSeverity':'critical','cooldown':'PT1H'}}").replace('\'', '"');
    }

    @Test
    @DisplayName("create forces the owner and validates members; another user's group is 404; a token is 401")
    void crudOwnershipAndIsolation() throws Exception {
        UUID owner = users.findByEmail("group-owner@example.com").orElseThrow().getId();

        JsonNode created = send(post("/v1/me/source-groups"), session, groupBody("Weather"), 201);
        String id = created.get("id").asString();
        assertThat(created.get("ownerUserId").asString()).isEqualTo(owner.toString());
        assertThat(created.get("sourceIds").get(0).asString()).isEqualTo(member);
        assertThat(created.get("correlation").get("correlationLevel").asString()).isEqualTo("aggressive");
        assertThat(created.get("notificationThreshold").get("minSeverity").asString()).isEqualTo("critical");

        // An unknown member source is 400.
        send(post("/v1/me/source-groups"), session,
                "{\"name\":\"Bad\",\"sourceIds\":[\"not-a-source\"]}", 400);

        // Read and update the owned group.
        assertThat(send(get("/v1/me/source-groups/" + id), session, null, 200).get("name").asString())
                .isEqualTo("Weather");
        assertThat(send(put("/v1/me/source-groups/" + id), session, groupBody("Renamed"), 200).get("name").asString())
                .isEqualTo("Renamed");

        // Another user neither sees nor reaches it.
        send(get("/v1/me/source-groups/" + id), otherSession, null, 404);
        send(put("/v1/me/source-groups/" + id), otherSession, groupBody("Taken"), 404);
        send(delete("/v1/me/source-groups/" + id), otherSession, null, 404);

        // An API token has no session.
        send(get("/v1/me/source-groups"), agent, null, 401);
        send(post("/v1/me/source-groups"), agent, groupBody("Tokened"), 401);

        // Delete removes it.
        send(delete("/v1/me/source-groups/" + id), session, null, 204);
        send(get("/v1/me/source-groups/" + id), session, null, 404);
    }

    @Test
    @DisplayName("the list is cursor-paged and shows only the caller's own groups")
    void listIsCursorPaged() throws Exception {
        String first = send(post("/v1/me/source-groups"), session, groupBody("One"), 201).get("id").asString();
        send(post("/v1/me/source-groups"), session, groupBody("Two"), 201);

        JsonNode page1 = send(get("/v1/me/source-groups?limit=1"), session, null, 200);
        assertThat(page1.get("items").size()).isEqualTo(1);
        assertThat(page1.get("items").get(0).get("id").asString()).isEqualTo(first);
        String cursor = page1.get("nextCursor").asString();
        assertThat(cursor).isNotBlank();

        JsonNode page2 = send(get("/v1/me/source-groups?limit=1&cursor=" + cursor), session, null, 200);
        assertThat(page2.get("items").size()).isEqualTo(1);
        assertThat(page2.get("items").get(0).get("id").asString()).isNotEqualTo(first);

        // The other user sees none of these.
        assertThat(send(get("/v1/me/source-groups"), otherSession, null, 200).get("items").size()).isZero();
    }
}
