package ai.foresignal.inbound;

import static ai.foresignal.testsupport.inbound.InboundIt.body;
import static ai.foresignal.testsupport.inbound.InboundIt.feature;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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
import ai.foresignal.testsupport.inbound.InboundIt;
import ai.foresignal.testsupport.security.TestUsers;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
 * The boundary between the operator and self-service custom-source surfaces (prompt SG-001): the operator
 * {@code /v1/custom-sources} still sees every source, an owner-less (operator-created) source is not returned by the
 * self-service {@code /v1/me/custom-sources}, and a self-service key rotation invalidates the old key at once.
 */
@IntegrationTest
@AutoConfigureMockMvc
class CustomSourceOwnershipIT {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final String OBSERVED = "2026-10-13T07:59:00Z";

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired PrincipalService principals;
    @Autowired UserRepository users;
    @Autowired UserRoleRepository userRoles;
    @Autowired RoleRepository roles;
    @Autowired RolePermissionRepository rolePermissions;
    @Autowired PasswordHasher hasher;

    String admin;
    String session;
    InboundIt it;

    @BeforeEach
    void setUp() throws Exception {
        admin = "Bearer " + adminTokens.rotate();
        it = new InboundIt(mvc, admin);
        sourcesUser("owner-boundary@example.com");
        session = login("owner-boundary@example.com");
    }

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

    private static List<String> ids(JsonNode array) {
        List<String> out = new ArrayList<>();
        array.forEach(n -> out.add(n.get("id").asString()));
        return out;
    }

    private JsonNode createMine(String id, String name) throws Exception {
        String body = SelfServiceCustomSourceIT.sourceBody(id, name);
        MockHttpServletResponse response = mvc.perform(post("/v1/me/custom-sources")
                .header(HttpHeaders.AUTHORIZATION, session).contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn().getResponse();
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(201);
        return JSON.readTree(response.getContentAsString());
    }

    @Test
    @DisplayName("an operator-created (owner-less) source is seen by the operator but not by the self-service surface")
    void ownerlessNotInSelfService() throws Exception {
        InboundIt.Created operatorSource = it.create(InboundIt.newId("operator"),
                "{'name':'Operator','kinds':['sensor-reading'],'observedAtMode':'RECEIVED'}");
        String mine = "custom-mine-" + Long.toString(System.nanoTime() % 1_000_000, 36);
        createMine(mine, "Mine");

        // The operator surface sees both.
        JsonNode operatorList = body(it.perform(get("/v1/custom-sources").header(HttpHeaders.AUTHORIZATION, admin)));
        assertThat(ids(operatorList)).contains(operatorSource.id(), mine);

        // The self-service surface shows only the caller's owned source, never the owner-less operator one.
        MockHttpServletResponse self = mvc.perform(get("/v1/me/custom-sources")
                .header(HttpHeaders.AUTHORIZATION, session)).andReturn().getResponse();
        assertThat(ids(JSON.readTree(self.getContentAsString()))).containsExactly(mine)
                .doesNotContain(operatorSource.id());
    }

    @Test
    @DisplayName("a self-service key rotation invalidates the old key at once")
    void rotationInvalidatesOldKey() throws Exception {
        String id = "custom-rekey-" + Long.toString(System.nanoTime() % 1_000_000, 36);
        String firstKey = createMine(id, "Rekey").get("ingestKey").asString();

        assertThat(it.push("/v1/ingest/" + id, firstKey, MediaType.APPLICATION_JSON, feature("a", "sensor-reading",
                OBSERVED, 1)).getStatus()).isEqualTo(200);

        MockHttpServletResponse rotated = mvc.perform(post("/v1/me/custom-sources/" + id + "/ingest-key/rotate")
                .header(HttpHeaders.AUTHORIZATION, session)).andReturn().getResponse();
        assertThat(rotated.getStatus()).isEqualTo(200);
        String newKey = JSON.readTree(rotated.getContentAsString()).get("ingestKey").asString();

        assertThat(it.push("/v1/ingest/" + id, firstKey, MediaType.APPLICATION_JSON, feature("b", "sensor-reading",
                OBSERVED, 2)).getStatus()).isEqualTo(401);
        assertThat(it.push("/v1/ingest/" + id, newKey, MediaType.APPLICATION_JSON, feature("b", "sensor-reading",
                OBSERVED, 2)).getStatus()).isEqualTo(200);
    }
}
