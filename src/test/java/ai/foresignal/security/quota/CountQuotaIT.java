package ai.foresignal.security.quota;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The count and storage quotas (prompt R2-026) on the self-service create paths: past {@code maxAlerts},
 * {@code maxChannels}, {@code maxSavedViews} or {@code maxStorageBytes} the create is {@code 409 QUOTA_EXCEEDED} naming
 * the limit, and nothing is created.
 */
@IntegrationTest
@Import(TriggerTestSources.class)
class CountQuotaIT {

    private static final String RULE = "{\"name\":\"%s\",\"sources\":[\"" + STANDARD + "\"],"
            + "\"conditions\":[{\"path\":\"mag\",\"op\":\">=\",\"value\":6}]}";
    private static final String WEBHOOK = "{\"name\":\"%s\",\"type\":\"webhook\","
            + "\"config\":{\"url\":\"http://127.0.0.1:9/%s\"}}";
    private static final String VIEW = "{\"slug\":\"%s\",\"name\":\"View %s\"}";

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

    @BeforeEach
    void setUp() throws Exception {
        quotas.resetState();
        editor = TestUsers.active(principals, users, userRoles, hasher, "quota@example.com", BuiltInRole.EDITOR);
        session = login("quota@example.com");
    }

    private <B extends AbstractMockHttpServletRequestBuilder<B>> MockHttpServletResponse call(B builder, String bearer)
            throws Exception {
        if (bearer != null) {
            builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer);
        }
        return mvc.perform(builder).andReturn().getResponse();
    }

    private MockHttpServletResponse postJson(String path, String body, String bearer) throws Exception {
        return call(post(path).contentType(MediaType.APPLICATION_JSON).content(body), bearer);
    }

    private String login(String email) throws Exception {
        MockHttpServletResponse r = postJson("/v1/auth/login", TestUsers.loginBody(email), null);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        return json.readTree(r.getContentAsString()).get("token").asString();
    }

    private void assertExceeded(MockHttpServletResponse r, String limit) throws Exception {
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(409);
        JsonNode problem = json.readTree(r.getContentAsString());
        assertThat(problem.get("code").asString()).isEqualTo("QUOTA_EXCEEDED");
        assertThat(problem.get("limit").asString()).isEqualTo(limit);
    }

    @Test
    @DisplayName("maxAlerts: the rule past the limit is refused; an API token's shared rule is not counted")
    void alerts() throws Exception {
        quotas.replace(editor.getId(), new Quota(null, null, 2, null, null));

        assertThat(postJson("/v1/triggers", RULE.formatted("one"), session).getStatus()).isEqualTo(201);
        assertThat(postJson("/v1/triggers", RULE.formatted("two"), session).getStatus()).isEqualTo(201);
        assertExceeded(postJson("/v1/triggers", RULE.formatted("three"), session), "maxAlerts");

        String admin = TestTokens.create(tokens, "quota-admin", Scope.ADMIN).plaintext();
        assertThat(postJson("/v1/triggers", RULE.formatted("shared"), admin).getStatus()).isEqualTo(201);
    }

    @Test
    @DisplayName("maxChannels: the personal channel past the limit is refused")
    void channels() throws Exception {
        quotas.replace(editor.getId(), new Quota(null, null, null, 1, null));

        assertThat(postJson("/v1/channels", WEBHOOK.formatted("first", "a"), session).getStatus()).isEqualTo(201);
        assertExceeded(postJson("/v1/channels", WEBHOOK.formatted("second", "b"), session), "maxChannels");
    }

    @Test
    @DisplayName("maxSavedViews: the view past the limit is refused; 0 allows none")
    void savedViews() throws Exception {
        quotas.replace(editor.getId(), new Quota(null, null, null, null, 1));

        assertThat(postJson("/v1/views", VIEW.formatted("mine-1", "1"), session).getStatus()).isEqualTo(201);
        assertExceeded(postJson("/v1/views", VIEW.formatted("mine-2", "2"), session), "maxSavedViews");

        quotas.replace(editor.getId(), new Quota(null, null, null, null, 0));
        assertExceeded(postJson("/v1/views", VIEW.formatted("mine-3", "3"), session), "maxSavedViews");
    }

    @Test
    @DisplayName("the instance default bounds a user with no own limit")
    void instanceDefault() throws Exception {
        quotas.setInstanceDefault(new Quota(null, null, 0, null, null));
        try {
            assertExceeded(postJson("/v1/triggers", RULE.formatted("none"), session), "maxAlerts");
        } finally {
            quotas.setInstanceDefault(Quota.UNSET);
        }
    }

    @Test
    @DisplayName("maxStorageBytes: an upload past the limit and a workspace past it are refused")
    void storage() throws Exception {
        quotas.replace(editor.getId(), new Quota(null, 32L, null, null, null));
        MockMultipartFile file = new MockMultipartFile("file", "notes.txt", "text/plain",
                "This note is longer than thirty-two bytes, so it does not fit.".getBytes(StandardCharsets.UTF_8));

        assertExceeded(call(multipart("/v1/attachments").file(file), session), "maxStorageBytes");
        assertExceeded(postJson("/v1/workspaces", "{\"name\":\"Big\",\"layout\":{\"panels\":"
                + "[\"map\",\"timeline\",\"alerts\",\"notes\"]}}", session), "maxStorageBytes");

        quotas.replace(editor.getId(), new Quota(null, null, null, null, null));
        assertThat(call(multipart("/v1/attachments").file(file), session).getStatus()).isEqualTo(201);
    }
}
