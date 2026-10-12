package ai.foresignal.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Invariant SEC-5 (prompt 023): every LLM-spending or otherwise cost-bearing path is
 * {@code alwaysAuthenticated}, so it needs a token even while {@code openReads} is on. Driven by
 * {@link AccessPolicy#alwaysAuthenticated()} through the real filter chain; operations later prompts
 * implement are refused by the filter before routing, so they are covered already.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
class AlwaysAuthenticatedIT {

    @Autowired private MockMvc mvc;
    @Autowired private AccessPolicy policy;
    @Autowired private TokenService tokens;
    @Autowired private AppSettingService settings;

    @BeforeEach
    void openReads() {
        settings.invalidateCache();
        settings.put(SettingKeys.SECURITY, new SecuritySettings(true));
    }

    private static String sample(String pattern) {
        return pattern.replace("/**", "/sample").replace("*", "sample");
    }

    private MockHttpServletResponse call(AccessPolicy.EndpointRule rule, String bearer) throws Exception {
        HttpMethod method = rule.method() == null ? HttpMethod.GET : rule.method();
        var builder = request(method, sample(rule.pattern())).contentType(MediaType.APPLICATION_JSON).content("{}");
        if (bearer != null) {
            builder.header(HttpHeaders.AUTHORIZATION, bearer);
        }
        return mvc.perform(builder).andReturn().getResponse();
    }

    @Test
    @DisplayName("the always-authenticated set includes Ask, search, Web Push and ticket minting")
    void coversTheCostBearingPaths() {
        assertThat(policy.alwaysAuthenticated()).extracting(AccessPolicy.EndpointRule::pattern)
                .contains("/v1/ask", "/v1/ask/stream", "/v1/ask/threads/**", "/v1/search", "/v1/push/subscriptions",
                        "/v1/push/subscriptions/*", "/v1/stream-tickets");
    }

    @Test
    @DisplayName("without a token every always-authenticated path is 401, even under openReads")
    void anonymousIsRefused() throws Exception {
        for (AccessPolicy.EndpointRule rule : policy.alwaysAuthenticated()) {
            assertThat(call(rule, null).getStatus()).as(rule.toString()).isEqualTo(401);
        }
    }

    @Test
    @DisplayName("a read token gets past the filter on the read-scope ones")
    void readTokenPasses() throws Exception {
        String bearer = TestTokens.bearer(TestTokens.create(tokens, "reader", Scope.READ));
        for (AccessPolicy.EndpointRule rule : policy.alwaysAuthenticated()) {
            AccessRequirement requirement = policy.requirementFor(
                    rule.method() == null ? HttpMethod.GET : rule.method(), sample(rule.pattern()));
            if (requirement instanceof AccessRequirement.RequiresScope r && r.scope() == Scope.READ) {
                assertThat(call(rule, bearer).getStatus()).as(rule.toString()).isNotIn(401, 403);
            }
        }
    }
}
