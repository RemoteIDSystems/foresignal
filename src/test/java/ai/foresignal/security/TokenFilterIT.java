package ai.foresignal.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The token filter's decision matrix (prompt 023): {no token, read, agent, admin, revoked, expired,
 * endpoint-restricted, data-restricted} × {read op, agent op, admin op, alwaysAuthenticated op},
 * with {@code openReads} on and off, through the real filter chain and controllers.
 *
 * <p>"PASS" means the request got past the filter (any status but 401 and 403): the agent op is a
 * {@code POST /v1/triggers} with an empty rule, which the controller answers 400.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
class TokenFilterIT {

    @Autowired private MockMvc mvc;
    @Autowired private TokenService tokens;
    @Autowired private AdminTokenService adminTokens;
    @Autowired private AppSettingService settings;
    @Autowired private MutableClock clock;
    @Autowired private JsonMapper json;

    private final Map<String, String> bearers = new HashMap<>();

    @BeforeEach
    void tokens() {
        settings.invalidateCache();
        bearers.clear();
        bearers.put("admin", adminTokens.rotate());
        bearers.put("read", TestTokens.create(tokens, "reader", Scope.READ).plaintext());
        bearers.put("agent", TestTokens.create(tokens, "agent", Scope.AGENT).plaintext());
        TokenService.CreatedToken revoked = TestTokens.create(tokens, "revoked", Scope.READ);
        tokens.revoke(revoked.entity().getId());
        bearers.put("revoked", revoked.plaintext());
        bearers.put("expired", TestTokens.create(tokens, "expired", Scope.ADMIN, TokenRestrictions.NONE,
                clock.instant().plusSeconds(1)).plaintext());
        bearers.put("endpointRestricted", TestTokens.create(tokens, "kiosk", Scope.READ,
                new TokenRestrictions(null, null, null, List.of("GET /v1/records/**")), null).plaintext());
        bearers.put("dataRestricted", TestTokens.create(tokens, "boxed", Scope.READ,
                new TokenRestrictions(null, null, List.of(0.0, 0.0, 10.0, 10.0), null), null).plaintext());
        clock.advance(Duration.ofSeconds(2));
    }

    /** Other tests in this cached context expect the shipped openReads. */
    @AfterEach
    void restoreOpenReads() {
        settings.put(SettingKeys.SECURITY, SecuritySettings.defaults());
    }

    private MockHttpServletRequestBuilder request(String op) {
        return switch (op) {
            case "readOp" -> get("/v1/records");
            case "agentOp" -> post("/v1/triggers").contentType(MediaType.APPLICATION_JSON).content("{}");
            case "adminOp" -> get("/v1/settings/retention");
            case "alwaysOp" -> post("/v1/stream-tickets").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"purpose\":\"tiles\"}");
            default -> throw new IllegalArgumentException(op);
        };
    }

    private MockHttpServletResponse perform(String token, String op) throws Exception {
        MockHttpServletRequestBuilder request = request(op);
        if (!"none".equals(token)) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearers.get(token));
        }
        return mvc.perform(request).andReturn().getResponse();
    }

    @ParameterizedTest(name = "{0} on {1}, openReads={2} -> {3} {4}")
    @CsvSource({
        // ---- openReads on ----
        "none, readOp, true, PASS, ",
        "none, agentOp, true, 401, UNAUTHORIZED",
        "none, adminOp, true, 401, UNAUTHORIZED",
        "none, alwaysOp, true, 401, UNAUTHORIZED",
        "read, readOp, true, PASS, ",
        "read, agentOp, true, 403, INSUFFICIENT_SCOPE",
        "read, adminOp, true, 403, INSUFFICIENT_SCOPE",
        "read, alwaysOp, true, PASS, ",
        "agent, readOp, true, PASS, ",
        "agent, agentOp, true, PASS, ",
        "agent, adminOp, true, 403, INSUFFICIENT_SCOPE",
        "agent, alwaysOp, true, PASS, ",
        "admin, readOp, true, PASS, ",
        "admin, agentOp, true, PASS, ",
        "admin, adminOp, true, PASS, ",
        "admin, alwaysOp, true, PASS, ",
        "revoked, readOp, true, 401, UNAUTHORIZED",
        "revoked, agentOp, true, 401, UNAUTHORIZED",
        "revoked, adminOp, true, 401, UNAUTHORIZED",
        "revoked, alwaysOp, true, 401, UNAUTHORIZED",
        "expired, readOp, true, 401, UNAUTHORIZED",
        "expired, agentOp, true, 401, UNAUTHORIZED",
        "expired, adminOp, true, 401, UNAUTHORIZED",
        "expired, alwaysOp, true, 401, UNAUTHORIZED",
        "endpointRestricted, readOp, true, PASS, ",
        "endpointRestricted, agentOp, true, 403, INSUFFICIENT_SCOPE",
        "endpointRestricted, adminOp, true, 403, INSUFFICIENT_SCOPE",
        "endpointRestricted, alwaysOp, true, 403, ENDPOINT_NOT_ALLOWED",
        "dataRestricted, readOp, true, 403, RESTRICTION_NOT_ENFORCED",
        "dataRestricted, agentOp, true, 403, INSUFFICIENT_SCOPE",
        "dataRestricted, adminOp, true, 403, INSUFFICIENT_SCOPE",
        "dataRestricted, alwaysOp, true, PASS, ",
        // ---- openReads off ----
        "none, readOp, false, 401, UNAUTHORIZED",
        "none, agentOp, false, 401, UNAUTHORIZED",
        "none, adminOp, false, 401, UNAUTHORIZED",
        "none, alwaysOp, false, 401, UNAUTHORIZED",
        "read, readOp, false, PASS, ",
        "read, agentOp, false, 403, INSUFFICIENT_SCOPE",
        "read, alwaysOp, false, PASS, ",
        "agent, agentOp, false, PASS, ",
        "admin, adminOp, false, PASS, ",
        "revoked, readOp, false, 401, UNAUTHORIZED",
        "expired, readOp, false, 401, UNAUTHORIZED",
        "endpointRestricted, readOp, false, PASS, ",
        "endpointRestricted, alwaysOp, false, 403, ENDPOINT_NOT_ALLOWED",
        "dataRestricted, readOp, false, 403, RESTRICTION_NOT_ENFORCED",
    })
    void matrix(String token, String op, boolean openReads, String expected, String code) throws Exception {
        settings.put(SettingKeys.SECURITY, new SecuritySettings(openReads));

        MockHttpServletResponse response = perform(token, op);

        if ("PASS".equals(expected)) {
            assertThat(response.getStatus()).as(response.getContentAsString()).isNotIn(401, 403);
            return;
        }
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(Integer.parseInt(expected));
        JsonNode problem = json.readTree(response.getContentAsString());
        assertThat(problem.get("code").asString()).isEqualTo(code);
        if ("401".equals(expected)) {
            assertThat(response.getHeader(HttpHeaders.WWW_AUTHENTICATE)).isEqualTo("Bearer realm=\"foresignal\"");
        }
        if ("INSUFFICIENT_SCOPE".equals(code)) {
            assertThat(problem.get("requiredScope").asString()).isIn("agent", "admin");
        }
    }

    @Test
    @DisplayName("INSUFFICIENT_SCOPE names the required scope")
    void insufficientScopeNamesTheScope() throws Exception {
        MockHttpServletResponse response = perform("read", "adminOp");

        JsonNode problem = json.readTree(response.getContentAsString());
        assertThat(problem.get("requiredScope").asString()).isEqualTo("admin");
        assertThat(problem.get("type").asString()).isEqualTo("https://foresignal.ai/problems/insufficient-scope");
    }

    @Test
    @DisplayName("a ticket in the Authorization header is refused, and no response echoes a token")
    void ticketInHeaderIsRefused() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/v1/records")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer fst_AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).doesNotContain("fst_AAAA");
    }

    @Test
    @DisplayName("an invalid token on an open endpoint is ignored, not rejected")
    void openEndpointsIgnoreInvalidTokens() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/v1/system")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer fsr_wrong"))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(json.readTree(response.getContentAsString()).has("database")).isFalse();
    }

    @Test
    @DisplayName("/v1/system's full document needs a read token (019 allowed any token; 023 sets read)")
    void systemNeedsRead() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/v1/system")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearers.get("read")))
                .andReturn().getResponse();

        assertThat(json.readTree(response.getContentAsString()).has("database")).isTrue();
    }

    @Test
    @DisplayName("a named token is recorded as token:<name> on alert lifecycle fields' actor")
    void actorIsTheTokenName() throws Exception {
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
        AuthenticatedToken token = tokens.authenticate(bearers.get("agent")).orElseThrow();
        request.setAttribute(CurrentToken.ATTRIBUTE, new CurrentToken(token, false));
        assertThat(ai.foresignal.api.support.Actors.of(request)).isEqualTo("token:agent");
        AuthenticatedToken bootstrap = tokens.authenticate(bearers.get("admin")).orElseThrow();
        request.setAttribute(CurrentToken.ATTRIBUTE, new CurrentToken(bootstrap, false));
        assertThat(ai.foresignal.api.support.Actors.of(request)).isEqualTo("admin");
    }

    @Test
    @DisplayName("an agent token manages triggers but gets 403 on sources, channels, tokens and settings")
    void agentLeastPrivilege() throws Exception {
        String agent = "Bearer " + bearers.get("agent");
        for (MockHttpServletRequestBuilder admin : List.of(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/v1/sources/usgs-earthquakes")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"),
                get("/v1/channels"),
                get("/v1/tokens"),
                get("/v1/settings/security"),
                get("/v1/llm/providers"))) {
            MockHttpServletResponse response = mvc.perform(admin.header(HttpHeaders.AUTHORIZATION, agent))
                    .andReturn().getResponse();
            assertThat(response.getStatus()).isEqualTo(403);
        }
    }
}
