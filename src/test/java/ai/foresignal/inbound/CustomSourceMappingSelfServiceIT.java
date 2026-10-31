package ai.foresignal.inbound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Self-service mapping test and samples (prompt SG-001): both delegate to the same backing services as the operator
 * endpoints after an ownership check, so they work for a source the caller owns and are {@code 404} for one they do
 * not.
 */
@IntegrationTest
@AutoConfigureMockMvc
class CustomSourceMappingSelfServiceIT {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Instant T = Instant.parse("2026-10-13T15:05:00Z");
    static final String FORM = "PASSKEY=0123456789ABCDEF&stationtype=GW2000A&dateutc=2026-10-13+15:04:05&tempf=68.4";

    @Autowired MockMvc mvc;
    @Autowired PrincipalService principals;
    @Autowired UserRepository users;
    @Autowired UserRoleRepository userRoles;
    @Autowired RoleRepository roles;
    @Autowired RolePermissionRepository rolePermissions;
    @Autowired PasswordHasher hasher;
    @Autowired MutableClock clock;
    @Autowired AppSettingService settings;

    String ownerSession;
    String otherSession;

    @BeforeEach
    void setUp() throws Exception {
        clock.setInstant(T);
        settings.invalidateCache();
        sourcesUser("map-owner@example.com");
        sourcesUser("map-other@example.com");
        ownerSession = login("map-owner@example.com");
        otherSession = login("map-other@example.com");
    }

    /** Builds an active read-scope user holding {@code self.sources} + {@code data.read}. */
    private UserEntity sourcesUser(String email) {
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

    private JsonNode createMine(String id) throws Exception {
        String body = ("{'id':'" + id + "','preset':'ecowitt','captureSamples':true}").replace('\'', '"');
        MockHttpServletResponse response = mvc.perform(post("/v1/me/custom-sources")
                .header(HttpHeaders.AUTHORIZATION, ownerSession).contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn().getResponse();
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(201);
        return JSON.readTree(response.getContentAsString());
    }

    @Test
    @DisplayName("mapping test and samples work for an owned source and are 404 for one the caller does not own")
    void ownedWorksOthers404() throws Exception {
        String id = "custom-map-" + Long.toString(System.nanoTime() % 1_000_000, 36);
        String key = createMine(id).get("ingestKey").asString();
        // Push a sample through the ingest key so the mapping test and samples have something to work on.
        assertThat(mvc.perform(post("/v1/ingest/" + id + "/k/" + key + "/ecowitt")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).content(FORM)).andReturn().getResponse()
                .getStatus()).isEqualTo(200);

        // The owner reads the captured samples and runs the stored mapping over them.
        MockHttpServletResponse samples = mvc.perform(get("/v1/me/custom-sources/" + id + "/samples")
                .header(HttpHeaders.AUTHORIZATION, ownerSession)).andReturn().getResponse();
        assertThat(samples.getStatus()).as(samples.getContentAsString()).isEqualTo(200);
        assertThat(JSON.readTree(samples.getContentAsString()).size()).isEqualTo(1);

        MockHttpServletResponse test = mvc.perform(post("/v1/me/custom-sources/" + id + "/mapping/test")
                .header(HttpHeaders.AUTHORIZATION, ownerSession)).andReturn().getResponse();
        assertThat(test.getStatus()).as(test.getContentAsString()).isEqualTo(200);
        assertThat(JSON.readTree(test.getContentAsString()).get("mappingSource").asString()).isEqualTo("stored");

        // Another user owns neither, so the ownership check turns both into 404 before any delegation.
        assertThat(mvc.perform(get("/v1/me/custom-sources/" + id + "/samples")
                .header(HttpHeaders.AUTHORIZATION, otherSession)).andReturn().getResponse().getStatus()).isEqualTo(404);
        assertThat(mvc.perform(post("/v1/me/custom-sources/" + id + "/mapping/test")
                .header(HttpHeaders.AUTHORIZATION, otherSession)).andReturn().getResponse().getStatus()).isEqualTo(404);
    }
}
