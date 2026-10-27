package ai.foresignal.security.quota;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.user.BuiltInRole;
import ai.foresignal.security.user.PasswordHasher;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.security.user.UserRoleRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.security.TestUsers;
import ai.foresignal.testsupport.trigger.TriggerTestSources;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code getMyQuota}, {@code getUserQuota} and {@code updateUserQuota} (prompt R2-026): a user reads their effective
 * limits with live usage; an admin reads and replaces any user's own limits.
 */
@IntegrationTest
@Import(TriggerTestSources.class)
class QuotaUsageIT {

    private static final String RULE = "{\"name\":\"%s\",\"sources\":[\"" + STANDARD + "\"],"
            + "\"conditions\":[{\"path\":\"mag\",\"op\":\">=\",\"value\":6}]}";

    @Autowired private MockMvc mvc;
    @Autowired private JsonMapper json;
    @Autowired private PrincipalService principals;
    @Autowired private UserRepository users;
    @Autowired private UserRoleRepository userRoles;
    @Autowired private PasswordHasher hasher;
    @Autowired private TokenService tokens;
    @Autowired private QuotaService quotas;

    private UserEntity editor;
    private String session;
    private String admin;

    @BeforeEach
    void setUp() throws Exception {
        quotas.resetState();
        editor = TestUsers.active(principals, users, userRoles, hasher, "usage@example.com", BuiltInRole.EDITOR);
        session = login("usage@example.com");
        admin = TestTokens.create(tokens, "quota-usage-admin", Scope.ADMIN).plaintext();
    }

    private MockHttpServletResponse call(MockHttpServletRequestBuilder builder, String bearer) throws Exception {
        if (bearer != null) {
            builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer);
        }
        return mvc.perform(builder).andReturn().getResponse();
    }

    private MockHttpServletResponse json(MockHttpServletRequestBuilder builder, String body, String bearer)
            throws Exception {
        return call(builder.contentType(MediaType.APPLICATION_JSON).content(body), bearer);
    }

    private JsonNode body(MockHttpServletResponse r) throws Exception {
        return json.readTree(r.getContentAsString());
    }

    private String login(String email) throws Exception {
        MockHttpServletResponse r = json(post("/v1/auth/login"), TestUsers.loginBody(email), null);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        return body(r).get("token").asString();
    }

    @Test
    @DisplayName("getMyQuota reports the effective limits (own over default) with current usage")
    void myQuota() throws Exception {
        quotas.setInstanceDefault(new Quota(600, null, 10, 5, null));
        try {
            quotas.replace(editor.getId(), new Quota(null, null, 3, null, null));
            assertThat(json(post("/v1/triggers"), RULE.formatted("mine"), session).getStatus()).isEqualTo(201);

            MockHttpServletResponse r = call(get("/v1/me/quota"), session);

            assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
            JsonNode b = body(r);
            assertThat(b.get("quota").get("maxAlerts").asInt()).isEqualTo(3);
            assertThat(b.get("quota").get("apiRateLimitPerMinute").asInt()).isEqualTo(600);
            assertThat(b.get("quota").get("maxChannels").asInt()).isEqualTo(5);
            assertThat(b.get("quota").get("maxSavedViews").isNull()).isTrue();
            assertThat(b.get("usage").get("alerts").asLong()).isEqualTo(1);
            assertThat(b.get("usage").get("channels").asLong()).isZero();
            assertThat(b.get("usage").get("savedViews").asLong()).isZero();
            assertThat(b.get("usage").get("storageBytes").asLong()).isZero();
            assertThat(b.get("usage").get("apiRequestsThisMinute").asLong()).isPositive();
        } finally {
            quotas.setInstanceDefault(Quota.UNSET);
        }
    }

    @Test
    @DisplayName("an API token with no user has no quota: getMyQuota is 401")
    void machineTokenHasNoQuota() throws Exception {
        assertThat(call(get("/v1/me/quota"), admin).getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("an admin sets and reads a user's quota; null clears a limit back to the default")
    void adminRoundTrip() throws Exception {
        String path = "/v1/users/" + editor.getId() + "/quota";

        MockHttpServletResponse empty = call(get(path), admin);
        assertThat(empty.getStatus()).isEqualTo(200);
        assertThat(body(empty).get("maxAlerts").isNull()).isTrue();

        MockHttpServletResponse set = json(put(path), "{\"apiRateLimitPerMinute\":120,\"maxStorageBytes\":1048576,"
                + "\"maxAlerts\":4,\"maxChannels\":2,\"maxSavedViews\":8}", admin);
        assertThat(set.getStatus()).as(set.getContentAsString()).isEqualTo(200);
        JsonNode read = body(call(get(path), admin));
        assertThat(read.get("apiRateLimitPerMinute").asInt()).isEqualTo(120);
        assertThat(read.get("maxStorageBytes").asLong()).isEqualTo(1_048_576L);
        assertThat(read.get("maxSavedViews").asInt()).isEqualTo(8);

        assertThat(json(put(path), "{\"maxAlerts\":4}", admin).getStatus()).isEqualTo(200);
        JsonNode replaced = body(call(get(path), admin));
        assertThat(replaced.get("maxAlerts").asInt()).isEqualTo(4);
        assertThat(replaced.get("apiRateLimitPerMinute").isNull()).as("PUT replaces whole").isTrue();

        assertThat(body(call(get("/v1/me/quota"), session)).get("quota").get("maxAlerts").asInt()).isEqualTo(4);
    }

    @Test
    @DisplayName("updateUserQuota validates: negatives and unknown members are 400; an unknown user is 404")
    void validation() throws Exception {
        String path = "/v1/users/" + editor.getId() + "/quota";

        assertThat(json(put(path), "{\"maxAlerts\":-1}", admin).getStatus()).isEqualTo(400);
        assertThat(json(put(path), "{\"maxAlert\":1}", admin).getStatus()).isEqualTo(400);
        assertThat(json(put(path), "{\"maxAlerts\":\"many\"}", admin).getStatus()).isEqualTo(400);
        assertThat(json(put("/v1/users/" + UUID.randomUUID() + "/quota"), "{}", admin).getStatus()).isEqualTo(404);
        assertThat(call(get("/v1/users/not-a-uuid/quota"), admin).getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("a user without users.manage cannot read or set another user's quota")
    void needsUsersManage() throws Exception {
        String path = "/v1/users/" + editor.getId() + "/quota";

        assertThat(call(get(path), session).getStatus()).isEqualTo(403);
        assertThat(json(put(path), "{\"maxAlerts\":100}", session).getStatus()).isEqualTo(403);
    }
}
