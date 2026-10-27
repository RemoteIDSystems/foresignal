package ai.foresignal.security.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.security.PrincipalService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestUsers;
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

/** {@code createMyToken} (prompt R2-025): restrictions validated, reach clamped to the user, the value shown once. */
@IntegrationTest
class CreateMyTokenIT {

    @Autowired private MockMvc mvc;
    @Autowired private JsonMapper json;
    @Autowired private PrincipalService principals;
    @Autowired private UserRepository users;
    @Autowired private UserRoleRepository userRoles;
    @Autowired private PasswordHasher hasher;
    @Autowired private UserLifecycleService lifecycle;
    @Autowired private PermissionResolver resolver;
    @Autowired private JdbcTemplate jdbc;

    private UserEntity user(String email, BuiltInRole... roles) {
        return TestUsers.active(principals, users, userRoles, hasher, email, roles);
    }

    private MockHttpServletResponse call(MockHttpServletRequestBuilder builder, String bearer) throws Exception {
        if (bearer != null) {
            builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer);
        }
        return mvc.perform(builder).andReturn().getResponse();
    }

    private String login(String email) throws Exception {
        MockHttpServletResponse r = call(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(TestUsers.loginBody(email)), null);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        return json.readTree(r.getContentAsString()).get("token").asString();
    }

    private MockHttpServletResponse mint(String bearer, String body) throws Exception {
        return call(post("/v1/me/tokens").contentType(MediaType.APPLICATION_JSON).content(body.replace('\'', '"')),
                bearer);
    }

    private String code(MockHttpServletResponse r) throws Exception {
        return json.readTree(r.getContentAsString()).get("code").asString();
    }

    @Test
    @DisplayName("a Viewer mints a restricted read token: owned by them, marked self, the value shown once")
    void viewerMintsReadToken() throws Exception {
        UserEntity u = user("vt@example.com", BuiltInRole.VIEWER);
        String session = login("vt@example.com");

        MockHttpServletResponse r = mint(session, "{'name':'laptop','scope':'read',"
                + "'restrictions':{'bbox':[-10,35,30,60]}}");

        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(201);
        JsonNode b = json.readTree(r.getContentAsString());
        String value = b.get("token").asString();
        assertThat(value).startsWith("fsr_");
        assertThat(b.get("createdVia").asString()).isEqualTo("self");
        assertThat(b.get("principalId").asString()).isEqualTo(u.getPrincipalId());
        assertThat(b.get("restrictions").get("bbox").size()).isEqualTo(4);
        assertThat(jdbc.queryForObject("select count(*) from api_token where token_hash = ?", Long.class,
                UserTokenService.sha256(value))).isEqualTo(1L);
        assertThat(call(get("/v1/me"), value).getStatus()).as("the token works").isEqualTo(200);
        assertThat(call(get("/v1/me/tokens"), session).getContentAsString()).doesNotContain(value);
    }

    @Test
    @DisplayName("a Viewer cannot mint an agent or admin token, nor the private grant")
    void viewerClamped() throws Exception {
        user("vc@example.com", BuiltInRole.VIEWER);
        String session = login("vc@example.com");

        MockHttpServletResponse agent = mint(session, "{'name':'a','scope':'agent'}");
        assertThat(agent.getStatus()).isEqualTo(403);
        assertThat(code(agent)).isEqualTo("TOKEN_EXCEEDS_USER");
        assertThat(mint(session, "{'name':'a','scope':'admin'}").getStatus()).isEqualTo(403);
        assertThat(mint(session, "{'name':'a','scope':'read','privateAccess':true}").getStatus()).isEqualTo(403);
        assertThat(jdbc.queryForObject("select count(*) from api_token where created_via = 'self'", Long.class))
                .isZero();
    }

    @Test
    @DisplayName("restrictions are validated; the owner is always the caller")
    void validated() throws Exception {
        user("vv@example.com", BuiltInRole.EDITOR);
        String session = login("vv@example.com");

        assertThat(mint(session, "{'name':'a','scope':'read','restrictions':{'bbox':[1,2]}}").getStatus())
                .isEqualTo(400);
        assertThat(mint(session, "{'name':'a','scope':'read','restrictions':{'nonsense':true}}").getStatus())
                .isEqualTo(400);
        assertThat(mint(session, "{'name':'a','scope':'read','principalId':'someone-else'}").getStatus())
                .isEqualTo(400);
        assertThat(mint(session, "{'scope':'read'}").getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("an Operator may mint agent tokens but not admin ones; an Admin may mint admin")
    void operatorAndAdmin() throws Exception {
        user("op@example.com", BuiltInRole.OPERATOR);
        String operator = login("op@example.com");
        assertThat(mint(operator, "{'name':'o','scope':'admin'}").getStatus()).isEqualTo(403);
        assertThat(mint(operator, "{'name':'o','scope':'agent'}").getStatus()).isEqualTo(201);

        user("ad@example.com", BuiltInRole.ADMIN);
        assertThat(mint(login("ad@example.com"), "{'name':'a','scope':'admin'}").getStatus()).isEqualTo(201);
    }

    @Test
    @DisplayName("an API token cannot mint a token: the operation needs a session")
    void tokenCannotMint() throws Exception {
        user("tm@example.com", BuiltInRole.EDITOR);
        String session = login("tm@example.com");
        String value = json.readTree(mint(session, "{'name':'t','scope':'read'}").getContentAsString())
                .get("token").asString();

        assertThat(mint(value, "{'name':'t2','scope':'read'}").getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("a token follows its owner: a role taken away caps it, a suspension stops it")
    void followsOwner() throws Exception {
        UserEntity u = user("fo@example.com", BuiltInRole.ADMIN);
        String session = login("fo@example.com");
        String admin = json.readTree(mint(session, "{'name':'ops','scope':'admin'}").getContentAsString())
                .get("token").asString();
        assertThat(call(get("/v1/tokens"), admin).getStatus()).as("an admin read").isEqualTo(200);

        jdbc.update("delete from user_role where user_id = ?", u.getId());
        userRoles.saveAndFlush(new UserRoleEntity(u.getId(), BuiltInRole.VIEWER.id()));
        resolver.invalidate(u.getId());
        assertThat(call(get("/v1/tokens"), admin).getStatus()).as("now capped at read").isEqualTo(403);
        assertThat(call(get("/v1/me"), admin).getStatus()).isEqualTo(200);

        lifecycle.suspend(users.findById(u.getId()).orElseThrow());
        assertThat(call(get("/v1/me"), admin).getStatus()).as("the owner may not sign in").isEqualTo(401);
    }
}
