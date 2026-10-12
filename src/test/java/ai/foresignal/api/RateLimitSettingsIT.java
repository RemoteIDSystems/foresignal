package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.ratelimit.ClientRateLimiter;
import ai.foresignal.security.ratelimit.RateLimitSettings;
import ai.foresignal.security.ratelimit.RateLimitSettingsService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import org.junit.jupiter.api.AfterEach;
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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code getRateLimits} and {@code updateRateLimits} (prompt 035): admin only, defaults, replace
 * semantics, validation, and that a change applies to the very next request.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
@org.springframework.test.context.TestPropertySource(properties = "foresignal.ratelimit.enabled=true")
class RateLimitSettingsIT {

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired AppSettingService settings;
    @Autowired TokenService tokens;
    @Autowired RateLimitSettingsService service;
    @Autowired ClientRateLimiter limiter;
    @Autowired JsonMapper json;

    String admin;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        limiter.reset();
        admin = "Bearer " + adminTokens.rotate();
    }

    @AfterEach
    void restore() {
        service.replace(RateLimitSettings.defaults());
        limiter.reset();
    }

    MockHttpServletResponse putLimits(String body) throws Exception {
        return mvc.perform(put("/v1/settings/ratelimits").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content(body.replace('\'', '"'))).andReturn().getResponse();
    }

    JsonNode getLimits() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/v1/settings/ratelimits")
                .header(HttpHeaders.AUTHORIZATION, admin)).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(200);
        return json.readTree(response.getContentAsString());
    }

    @Test
    @DisplayName("GET returns the shipped defaults until an operator sets them")
    void defaults() throws Exception {
        JsonNode body = getLimits();

        assertThat(body.get("read").asInt()).isEqualTo(600);
        assertThat(body.get("tiles").asInt()).isEqualTo(1_200);
        assertThat(body.get("stream").get("opensPerMinute").asInt()).isEqualTo(30);
        assertThat(body.get("stream").get("concurrent").asInt()).isEqualTo(8);
        assertThat(body.get("expensive").asInt()).isEqualTo(20);
        assertThat(body.get("write").asInt()).isEqualTo(120);
        assertThat(body.get("anonymous").asInt()).isEqualTo(120);
        assertThat(body.get("metricsPerMinute") == null || body.get("metricsPerMinute").isNull()).isTrue();
    }

    @Test
    @DisplayName("PUT replaces the settings: members left out return to their defaults")
    void replace() throws Exception {
        MockHttpServletResponse first = putLimits("{'read':900,'stream':{'concurrent':4},'metricsPerMinute':60}");
        assertThat(first.getStatus()).isEqualTo(200);
        assertThat(json.readTree(first.getContentAsString()).get("read").asInt()).isEqualTo(900);
        JsonNode stored = getLimits();
        assertThat(stored.get("read").asInt()).isEqualTo(900);
        assertThat(stored.get("stream").get("concurrent").asInt()).isEqualTo(4);
        assertThat(stored.get("stream").get("opensPerMinute").asInt()).isEqualTo(30);
        assertThat(stored.get("metricsPerMinute").asInt()).isEqualTo(60);

        assertThat(putLimits("{'tiles':50}").getStatus()).isEqualTo(200);
        JsonNode replaced = getLimits();
        assertThat(replaced.get("read").asInt()).isEqualTo(600);
        assertThat(replaced.get("tiles").asInt()).isEqualTo(50);
    }

    @Test
    @DisplayName("invalid values are 400 VALIDATION_FAILED with one field error each")
    void validation() throws Exception {
        MockHttpServletResponse response = putLimits("{'read':0,'stream':{'concurrent':-1},'bogus':1}");

        assertThat(response.getStatus()).isEqualTo(400);
        JsonNode problem = json.readTree(response.getContentAsString());
        assertThat(problem.get("code").asString()).isEqualTo("VALIDATION_FAILED");
        assertThat(problem.get("errors")).extracting(e -> e.get("field").asString())
                .containsExactlyInAnyOrder("read", "stream.concurrent", "bogus");
        assertThat(getLimits().get("read").asInt()).isEqualTo(600);
    }

    @Test
    @DisplayName("admin only: no token is 401, a read token is 403")
    void adminOnly() throws Exception {
        assertThat(mvc.perform(get("/v1/settings/ratelimits")).andReturn().getResponse().getStatus()).isEqualTo(401);
        TokenService.CreatedToken reader = TestTokens.create(tokens, "reader", Scope.READ);
        assertThat(mvc.perform(get("/v1/settings/ratelimits").header(HttpHeaders.AUTHORIZATION,
                TestTokens.bearer(reader))).andReturn().getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("a change applies to the next request without a restart")
    void appliesAtOnce() throws Exception {
        assertThat(putLimits("{'read':1}").getStatus()).isEqualTo(200);
        String ip = "198.51.100.23";

        assertThat(mvc.perform(get("/v1/records").with(r -> {
            r.setRemoteAddr(ip);
            return r;
        })).andReturn().getResponse().getStatus()).isEqualTo(200);
        MockHttpServletResponse limited = mvc.perform(get("/v1/records").with(r -> {
            r.setRemoteAddr(ip);
            return r;
        })).andReturn().getResponse();
        assertThat(limited.getStatus()).isEqualTo(429);
        assertThat(limited.getHeader("Retry-After")).isNotNull();
    }
}
