package ai.foresignal.security.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.security.TestUsers;
import java.util.UUID;
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

/**
 * {@code listMyTokens} and {@code revokeMyToken} (prompt R2-025): only the caller's tokens, {@code 404} for anyone
 * else's, a revoked token stops at once, and what a token does is audited as its owner.
 */
@IntegrationTest
class MyTokenListRevokeIT {

    @Autowired private MockMvc mvc;
    @Autowired private JsonMapper json;
    @Autowired private PrincipalService principals;
    @Autowired private UserRepository users;
    @Autowired private UserRoleRepository userRoles;
    @Autowired private PasswordHasher hasher;
    @Autowired private TokenService tokens;
    @Autowired private AuditService audit;
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

    private JsonNode mint(String session, String name) throws Exception {
        MockHttpServletResponse r = call(post("/v1/me/tokens").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + name + "\",\"scope\":\"read\"}"), session);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(201);
        return json.readTree(r.getContentAsString());
    }

    @Test
    @DisplayName("the list shows only the caller's tokens, never a value")
    void listsOwnOnly() throws Exception {
        user("a@example.com", BuiltInRole.EDITOR);
        user("b@example.com", BuiltInRole.EDITOR);
        String a = login("a@example.com");
        String b = login("b@example.com");
        JsonNode mine = mint(a, "mine");
        mint(b, "theirs");
        TestTokens.create(tokens, "machine", Scope.READ);

        MockHttpServletResponse r = call(get("/v1/me/tokens"), a);

        assertThat(r.getStatus()).isEqualTo(200);
        JsonNode list = json.readTree(r.getContentAsString());
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.get(0).get("id").asString()).isEqualTo(mine.get("id").asString());
        assertThat(list.get(0).has("token")).isFalse();
    }

    @Test
    @DisplayName("revoking an owned token works and it stops authenticating; anyone else's is 404")
    void revoke() throws Exception {
        user("c@example.com", BuiltInRole.EDITOR);
        user("d@example.com", BuiltInRole.EDITOR);
        String c = login("c@example.com");
        String d = login("d@example.com");
        JsonNode token = mint(c, "phone");
        String id = token.get("id").asString();
        String value = token.get("token").asString();
        assertThat(call(get("/v1/me"), value).getStatus()).isEqualTo(200);

        assertThat(call(delete("/v1/me/tokens/" + id), d).getStatus()).as("another user's").isEqualTo(404);
        assertThat(call(delete("/v1/me/tokens/" + UUID.randomUUID()), c).getStatus()).isEqualTo(404);
        assertThat(call(delete("/v1/me/tokens/not-a-uuid"), c).getStatus()).isEqualTo(404);
        assertThat(call(get("/v1/me"), value).getStatus()).as("still live").isEqualTo(200);

        assertThat(call(delete("/v1/me/tokens/" + id), c).getStatus()).isEqualTo(204);

        assertThat(call(get("/v1/me"), value).getStatus()).isEqualTo(401);
        JsonNode list = json.readTree(call(get("/v1/me/tokens"), c).getContentAsString());
        assertThat(list.get(0).get("revokedAt").isNull()).isFalse();
    }

    @Test
    @DisplayName("a write made with a user's token is audited as that user")
    void attributedToOwner() throws Exception {
        UserEntity u = user("e@example.com", BuiltInRole.EDITOR);
        String value = mint(login("e@example.com"), "agent").get("token").asString();

        MockHttpServletResponse r = call(post("/v1/stream-tickets").contentType(MediaType.APPLICATION_JSON)
                .content("{\"purpose\":\"tiles\"}"), value);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(201);

        audit.flush();
        assertThat(jdbc.queryForList("select user_id from audit_event where path = '/v1/stream-tickets'",
                UUID.class)).containsExactly(u.getId());
        assertThat(jdbc.queryForObject("select detail->>'via' from audit_event where path = '/v1/stream-tickets'",
                String.class)).isEqualTo("user-token");
    }

    @Test
    @DisplayName("an API token has no user: the operations need a session")
    void needsSession() throws Exception {
        String machine = TestTokens.bearer(TestTokens.create(tokens, "m", Scope.ADMIN));
        assertThat(call(get("/v1/me/tokens"), machine).getStatus()).isEqualTo(401);
    }
}
