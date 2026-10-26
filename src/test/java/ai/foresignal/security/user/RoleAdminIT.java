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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The {@code Roles} operations through the real filter chain (prompt R2-006). */
@IntegrationTest
class RoleAdminIT {

    @Autowired private MockMvc mvc;
    @Autowired private JsonMapper json;
    @Autowired private PrincipalService principals;
    @Autowired private UserRepository users;
    @Autowired private UserRoleRepository userRoles;
    @Autowired private PasswordHasher hasher;
    @Autowired private TokenService tokens;
    @Autowired private PermissionResolver resolver;
    @Autowired private BuiltInRoleSeeder seeder;
    @Autowired private JdbcTemplate jdbc;

    private String adminToken;

    @BeforeEach
    void setUp() {
        adminToken = TestTokens.create(tokens, "roles-it-admin", Scope.ADMIN).plaintext();
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

    private void assertCode(MockHttpServletResponse r, int status, String code) throws Exception {
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(status);
        assertThat(body(r).get("code").asString()).isEqualTo(code);
    }

    private String createRole(String name, String... permissions) throws Exception {
        MockHttpServletResponse r = admin(jsonBody(post("/v1/roles"), roleBody(name, permissions)));
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(201);
        return body(r).get("id").asString();
    }

    private static String roleBody(String name, String... permissions) {
        List<String> quoted = new ArrayList<>();
        for (String p : permissions) {
            quoted.add("\"" + p + "\"");
        }
        return "{\"name\":\"" + name + "\",\"description\":\"test\",\"permissions\":[" + String.join(",", quoted)
                + "]}";
    }

    @Test
    @DisplayName("listRoles returns the built-ins with their permissions and user counts")
    void listWithCounts() throws Exception {
        TestUsers.active(principals, users, userRoles, hasher, "v1@example.com", BuiltInRole.VIEWER);
        TestUsers.active(principals, users, userRoles, hasher, "v2@example.com", BuiltInRole.VIEWER);
        JsonNode items = body(admin(get("/v1/roles"))).get("items");
        assertThat(items).hasSize(5);
        for (JsonNode role : items) {
            assertThat(role.get("builtIn").asBoolean()).isTrue();
            if ("Viewer".equals(role.get("name").asString())) {
                assertThat(role.get("userCount").asLong()).isEqualTo(2);
                assertThat(json.convertValue(role.get("permissions"), List.class))
                        .containsExactly("data.read", "self.preferences", "self.views");
            }
            if ("Admin".equals(role.get("name").asString())) {
                assertThat(role.get("permissions")).hasSize(Permissions.values().length);
                assertThat(role.get("userCount").asLong()).isZero();
            }
        }
        JsonNode page = body(admin(get("/v1/roles?limit=2")));
        assertThat(page.get("items")).hasSize(2);
        JsonNode next = body(admin(get("/v1/roles?limit=10&cursor=" + page.get("nextCursor").asString())));
        assertThat(next.get("items")).hasSize(3);
    }

    @Test
    @DisplayName("a custom role is created from a valid subset and read back")
    void createAndGet() throws Exception {
        String id = createRole("Analyst", "data.read", "intel.read", "self.alerts");
        JsonNode role = body(admin(get("/v1/roles/" + id)));
        assertThat(role.get("builtIn").asBoolean()).isFalse();
        assertThat(json.convertValue(role.get("permissions"), List.class))
                .containsExactly("data.read", "intel.read", "self.alerts");
        assertThat(role.get("userCount").asLong()).isZero();
        assertCode(admin(get("/v1/roles/" + UUID.randomUUID())), 404, ErrorCodes.UNKNOWN_ROLE);
    }

    @Test
    @DisplayName("an unknown permission key is refused naming it; a taken or built-in name is 409 ROLE_NAME_TAKEN")
    void createRefusals() throws Exception {
        MockHttpServletResponse r = admin(jsonBody(post("/v1/roles"), roleBody("Bad", "data.read", "made.up")));
        assertThat(r.getStatus()).isEqualTo(400);
        assertThat(r.getContentAsString()).contains("made.up");
        createRole("Analyst", "data.read");
        assertCode(admin(jsonBody(post("/v1/roles"), roleBody("analyst", "data.read"))), 409,
                ErrorCodes.ROLE_NAME_TAKEN);
        assertCode(admin(jsonBody(post("/v1/roles"), roleBody("ADMIN", "data.read"))), 409,
                ErrorCodes.ROLE_NAME_TAKEN);
    }

    @Test
    @DisplayName("updating a custom role's permissions drops the cached permissions of its holders")
    void updateInvalidates() throws Exception {
        String id = createRole("Analyst", "data.read");
        UserEntity u = TestUsers.active(principals, users, userRoles, hasher, "an@example.com");
        userRoles.saveAndFlush(new UserRoleEntity(u.getId(), UUID.fromString(id)));
        resolver.invalidate(u.getId());
        assertThat(resolver.forUser(u.getId()).keys()).containsExactly("data.read");
        JsonNode updated = body(admin(jsonBody(put("/v1/roles/" + id), roleBody("Analyst 2", "data.read",
                "intel.read"))));
        assertThat(updated.get("name").asString()).isEqualTo("Analyst 2");
        assertThat(updated.get("userCount").asLong()).isEqualTo(1);
        assertThat(resolver.forUser(u.getId()).keys()).containsExactlyInAnyOrder("data.read", "intel.read");
    }

    @Test
    @DisplayName("a built-in role cannot be updated or deleted: 409 BUILTIN_ROLE_PROTECTED")
    void builtInsProtected() throws Exception {
        String viewer = BuiltInRole.VIEWER.id().toString();
        assertCode(admin(jsonBody(put("/v1/roles/" + viewer), roleBody("Reader", "data.read"))), 409,
                ErrorCodes.BUILTIN_ROLE_PROTECTED);
        assertCode(admin(jsonBody(put("/v1/roles/" + viewer), roleBody("Viewer", "data.read", "intel.read"))), 409,
                ErrorCodes.BUILTIN_ROLE_PROTECTED);
        assertCode(admin(delete("/v1/roles/" + viewer)), 409, ErrorCodes.BUILTIN_ROLE_PROTECTED);
        assertThat(jdbc.queryForObject("select count(*) from role_permission where role_id = ?", Long.class,
                BuiltInRole.VIEWER.id())).isEqualTo(3L);
    }

    @Test
    @DisplayName("a custom role nobody holds is deleted; one still held is 409 ROLE_IN_USE with userCount")
    void deleteCustomRole() throws Exception {
        String free = createRole("Temporary", "data.read");
        assertThat(admin(delete("/v1/roles/" + free)).getStatus()).isEqualTo(204);
        assertCode(admin(get("/v1/roles/" + free)), 404, ErrorCodes.UNKNOWN_ROLE);

        String held = createRole("Held", "data.read");
        UserEntity u = TestUsers.active(principals, users, userRoles, hasher, "held@example.com");
        userRoles.saveAndFlush(new UserRoleEntity(u.getId(), UUID.fromString(held)));
        MockHttpServletResponse r = admin(delete("/v1/roles/" + held));
        assertCode(r, 409, ErrorCodes.ROLE_IN_USE);
        assertThat(body(r).get("userCount").asLong()).isEqualTo(1);
    }

    @Test
    @DisplayName("a custom role edit that would leave no active holder of users.manage is 409 LAST_ADMIN")
    void lastHolderGuard() throws Exception {
        String id = createRole("Managers", "users.manage", "roles.manage", "data.read");
        UserEntity u = TestUsers.active(principals, users, userRoles, hasher, "mgr@example.com");
        userRoles.saveAndFlush(new UserRoleEntity(u.getId(), UUID.fromString(id)));
        assertCode(admin(jsonBody(put("/v1/roles/" + id), roleBody("Managers", "data.read"))), 409,
                ErrorCodes.LAST_ADMIN);
        TestUsers.active(principals, users, userRoles, hasher, "adm@example.com", BuiltInRole.ADMIN);
        assertThat(admin(jsonBody(put("/v1/roles/" + id), roleBody("Managers", "data.read"))).getStatus())
                .isEqualTo(200);
    }

    @Test
    @DisplayName("listPermissions returns the full seeded catalog, grouped identity, platform, self, data")
    void permissionCatalog() throws Exception {
        JsonNode items = body(admin(get("/v1/permissions"))).get("items");
        assertThat(items).hasSize(Permissions.values().length);
        List<String> groups = new ArrayList<>();
        for (JsonNode item : items) {
            String group = item.get("group").asString();
            if (groups.isEmpty() || !groups.get(groups.size() - 1).equals(group)) {
                groups.add(group);
            }
            assertThat(jdbc.queryForObject("select \"group\" from permission where key = ?", String.class,
                    item.get("key").asString())).isEqualTo(group);
        }
        assertThat(groups).containsExactly("identity", "platform", "self", "data");
    }

    @Test
    @DisplayName("a missing built-in role is re-seeded with its permissions")
    void reseedsBuiltIns() {
        jdbc.update("delete from role where id = ?", BuiltInRole.AUDITOR.id());
        assertThat(seeder.seed()).isEqualTo(1 + BuiltInRole.AUDITOR.permissions().size());
        assertThat(jdbc.queryForObject("select name from role where id = ? and built_in", String.class,
                BuiltInRole.AUDITOR.id())).isEqualTo("Auditor");
        assertThat(jdbc.queryForObject("select count(*) from role_permission where role_id = ?", Long.class,
                BuiltInRole.AUDITOR.id())).isEqualTo((long) BuiltInRole.AUDITOR.permissions().size());
        assertThat(seeder.seed()).isZero();
    }

    @Test
    @DisplayName("a session without roles.manage is refused; an Admin session is let through")
    void gated() throws Exception {
        TestUsers.active(principals, users, userRoles, hasher, "op@example.com", BuiltInRole.OPERATOR);
        TestUsers.active(principals, users, userRoles, hasher, "boss@example.com", BuiltInRole.ADMIN);
        assertCode(call(get("/v1/roles"), login("op@example.com")), 403, ErrorCodes.PERMISSION_DENIED);
        assertThat(call(get("/v1/permissions"), login("boss@example.com")).getStatus()).isEqualTo(200);
    }

    private String login(String email) throws Exception {
        MockHttpServletResponse r = mvc.perform(jsonBody(post("/v1/auth/login"), TestUsers.loginBody(email)))
                .andReturn().getResponse();
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        return body(r).get("token").asString();
    }
}
