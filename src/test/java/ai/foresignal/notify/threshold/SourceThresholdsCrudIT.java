package ai.foresignal.notify.threshold;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.user.BuiltInRole;
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
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.security.TestUsers;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Per-source thresholds over HTTP (prompt SG-003): a {@code self.alerts} user gets (empty → no entries) and replaces
 * their thresholds; an unknown source or a bad severity is {@code 400}; an API token is {@code 401}; a Viewer without
 * {@code self.alerts} is {@code 403}.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
class SourceThresholdsCrudIT {

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
    String viewerSession;
    String agent;

    @BeforeEach
    void setUp() throws Exception {
        alertsUser("alerts-user@example.com");
        TestUsers.active(principals, users, userRoles, hasher, "alerts-viewer@example.com", BuiltInRole.VIEWER);
        session = login("alerts-user@example.com");
        viewerSession = login("alerts-viewer@example.com");
        agent = "Bearer " + adminTokens.rotate();
    }

    private void alertsUser(String email) {
        UserEntity user = TestUsers.active(principals, users, userRoles, hasher, email);
        Instant now = Instant.now();
        UUID roleId = UUID.randomUUID();
        roles.saveAndFlush(new RoleEntity(roleId, "Alerts " + email, "self.alerts only", false, now));
        rolePermissions.saveAndFlush(new RolePermissionEntity(roleId, Permissions.SELF_ALERTS.key()));
        rolePermissions.saveAndFlush(new RolePermissionEntity(roleId, Permissions.DATA_READ.key()));
        userRoles.saveAndFlush(new UserRoleEntity(user.getId(), roleId));
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

    @Test
    @DisplayName("get is empty by default, replace stores the whole set, and bad input is 400")
    void getAndReplace() throws Exception {
        assertThat(send(get("/v1/me/source-thresholds"), session, null, 200).get("entries").size()).isZero();

        String body = ("{'entries':[{'sourceId':'" + CollectorTestSources.POLL + "','minSeverity':'critical',"
                + "'mute':false,'cooldown':'PT1H'}]}").replace('\'', '"');
        JsonNode replaced = send(put("/v1/me/source-thresholds"), session, body, 200);
        assertThat(replaced.get("entries").size()).isEqualTo(1);
        JsonNode entry = replaced.get("entries").get(0);
        assertThat(entry.get("sourceId").asString()).isEqualTo(CollectorTestSources.POLL);
        assertThat(entry.get("minSeverity").asString()).isEqualTo("critical");
        assertThat(entry.get("cooldown").asString()).isEqualTo("PT1H");

        assertThat(send(get("/v1/me/source-thresholds"), session, null, 200).get("entries").size()).isEqualTo(1);

        // An unknown source and a bad severity are both 400.
        send(put("/v1/me/source-thresholds"), session, "{\"entries\":[{\"sourceId\":\"not-a-source\"}]}", 400);
        send(put("/v1/me/source-thresholds"), session, "{\"entries\":[{\"sourceId\":\"" + CollectorTestSources.POLL
                + "\",\"minSeverity\":\"huge\"}]}", 400);

        // Replacing with an empty set clears it.
        assertThat(send(put("/v1/me/source-thresholds"), session, "{\"entries\":[]}", 200).get("entries").size())
                .isZero();
    }

    @Test
    @DisplayName("an API token is 401 and a Viewer without self.alerts is 403")
    void authGating() throws Exception {
        send(get("/v1/me/source-thresholds"), agent, null, 401);
        send(get("/v1/me/source-thresholds"), viewerSession, null, 403);
    }
}
