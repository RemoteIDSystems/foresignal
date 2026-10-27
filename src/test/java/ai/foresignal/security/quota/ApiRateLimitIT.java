package ai.foresignal.security.quota;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.user.BuiltInRole;
import ai.foresignal.security.user.PasswordHasher;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.security.user.UserRoleRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.security.TestUsers;
import java.time.Instant;
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
 * The per-user API rate limit (prompt R2-026) through the real filter chain: over {@code apiRateLimitPerMinute} a user
 * gets {@code 429} with {@code Retry-After}; the budget is the user's across sessions and own tokens, and per user.
 * The per-client limits stay off in the test profile, so only this limit is in play.
 */
@IntegrationTest
class ApiRateLimitIT {

    private static final Instant T = Instant.parse("2026-10-26T12:00:00Z");

    @Autowired private MockMvc mvc;
    @Autowired private JsonMapper json;
    @Autowired private PrincipalService principals;
    @Autowired private UserRepository users;
    @Autowired private UserRoleRepository userRoles;
    @Autowired private PasswordHasher hasher;
    @Autowired private QuotaService quotas;
    @Autowired private MutableClock clock;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        quotas.resetState();
    }

    private UserEntity user(String email) {
        return TestUsers.active(principals, users, userRoles, hasher, email, BuiltInRole.EDITOR);
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

    @Test
    @DisplayName("under the limit passes; over it is 429 RATE_LIMITED with Retry-After and class user")
    void overTheLimit() throws Exception {
        UserEntity u = user("rl@example.com");
        String session = login("rl@example.com");
        quotas.replace(u.getId(), new Quota(3, null, null, null, null));

        for (int i = 0; i < 3; i++) {
            assertThat(call(get("/v1/me"), session).getStatus()).as("request %d", i + 1).isEqualTo(200);
        }
        MockHttpServletResponse refused = call(get("/v1/me"), session);

        assertThat(refused.getStatus()).isEqualTo(429);
        assertThat(Long.parseLong(refused.getHeader(HttpHeaders.RETRY_AFTER))).isPositive();
        JsonNode problem = json.readTree(refused.getContentAsString());
        assertThat(problem.get("code").asString()).isEqualTo("RATE_LIMITED");
        assertThat(problem.get("class").asString()).isEqualTo("user");
        assertThat(problem.get("limit").asString()).isEqualTo("apiRateLimitPerMinute");

        clock.setInstant(T.plusSeconds(61));
        assertThat(call(get("/v1/me"), session).getStatus()).as("refilled after a minute").isEqualTo(200);
    }

    @Test
    @DisplayName("the budget is the user's: a second session and the user's own token draw on it too")
    void aggregatePerUser() throws Exception {
        UserEntity u = user("agg@example.com");
        String first = login("agg@example.com");
        String second = login("agg@example.com");
        MockHttpServletResponse minted = call(post("/v1/me/tokens").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"script\",\"scope\":\"read\"}"), first);
        assertThat(minted.getStatus()).as(minted.getContentAsString()).isEqualTo(201);
        String token = json.readTree(minted.getContentAsString()).get("token").asString();
        quotas.replace(u.getId(), new Quota(3, null, null, null, null));

        assertThat(call(get("/v1/me"), first).getStatus()).isEqualTo(200);
        assertThat(call(get("/v1/me"), second).getStatus()).isEqualTo(200);
        assertThat(call(get("/v1/me"), token).getStatus()).isEqualTo(200);
        assertThat(call(get("/v1/me"), token).getStatus()).isEqualTo(429);
        assertThat(call(get("/v1/me"), first).getStatus()).isEqualTo(429);
    }

    @Test
    @DisplayName("limits are per user, not global: one user at the limit does not slow another")
    void perUser() throws Exception {
        UserEntity a = user("ua@example.com");
        UserEntity b = user("ub@example.com");
        String sa = login("ua@example.com");
        String sb = login("ub@example.com");
        quotas.replace(a.getId(), new Quota(1, null, null, null, null));
        quotas.replace(b.getId(), new Quota(1, null, null, null, null));

        assertThat(call(get("/v1/me"), sa).getStatus()).isEqualTo(200);
        assertThat(call(get("/v1/me"), sa).getStatus()).isEqualTo(429);
        assertThat(call(get("/v1/me"), sb).getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("with no quota set, a user is not limited by this filter")
    void unsetIsUnlimited() throws Exception {
        user("free@example.com");
        String session = login("free@example.com");

        for (int i = 0; i < 50; i++) {
            assertThat(call(get("/v1/me"), session).getStatus()).isEqualTo(200);
        }
    }

    @Test
    @DisplayName("the instance default applies to a user whose own limit is null")
    void instanceDefault() throws Exception {
        user("def@example.com");
        String session = login("def@example.com");
        quotas.setInstanceDefault(new Quota(2, null, null, null, null));
        try {
            assertThat(call(get("/v1/me"), session).getStatus()).isEqualTo(200);
            assertThat(call(get("/v1/me"), session).getStatus()).isEqualTo(200);
            assertThat(call(get("/v1/me"), session).getStatus()).isEqualTo(429);
        } finally {
            quotas.setInstanceDefault(Quota.UNSET);
        }
    }
}
