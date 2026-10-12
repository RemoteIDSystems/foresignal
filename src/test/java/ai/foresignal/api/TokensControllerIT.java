package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import java.util.UUID;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code /v1/tokens} and {@code /v1/tokens/current/device} (prompt 023). */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
class TokensControllerIT {

    @Autowired private MockMvc mvc;
    @Autowired private AdminTokenService adminTokens;
    @Autowired private TokenService tokens;
    @Autowired private AppSettingService settings;
    @Autowired private JsonMapper json;

    private String admin;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        admin = "Bearer " + adminTokens.rotate();
    }

    private MockHttpServletResponse perform(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request).andReturn().getResponse();
    }

    private JsonNode body(MockHttpServletResponse response) throws Exception {
        return json.readTree(response.getContentAsString());
    }

    @Test
    @DisplayName("POST /v1/tokens returns 201 with the value once; the listing never shows it")
    void createAndList() throws Exception {
        MockHttpServletResponse created = perform(post("/v1/tokens").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"kiosk\",\"scope\":\"read\",\"restrictions\":{\"endpoints\":[\"GET /v1/records/**\"]}}"));

        assertThat(created.getStatus()).as(created.getContentAsString()).isEqualTo(201);
        JsonNode token = body(created);
        String value = token.get("token").asString();
        assertThat(value).startsWith("fsr_");
        assertThat(token.get("tokenPrefix").asString()).isEqualTo(value.substring(0, 8));
        assertThat(token.get("createdVia").asString()).isEqualTo("api");
        assertThat(token.get("bootstrap").asBoolean()).isFalse();
        assertThat(token.get("restrictions").get("endpoints").get(0).asString()).isEqualTo("GET /v1/records/**");
        assertThat(token.get("principalHandle").asString()).isEqualTo("kiosk");

        MockHttpServletResponse list = perform(get("/v1/tokens").header(HttpHeaders.AUTHORIZATION, admin));
        assertThat(list.getStatus()).isEqualTo(200);
        assertThat(list.getContentAsString()).doesNotContain(value);
        JsonNode items = body(list);
        assertThat(items).hasSize(2);
        assertThat(items.findValuesAsString("name")).contains("kiosk", "bootstrap");
        assertThat(items.findValues("token")).isEmpty();
    }

    @Test
    @DisplayName("invalid input is 400 VALIDATION_FAILED listing every field")
    void validation() throws Exception {
        MockHttpServletResponse response = perform(post("/v1/tokens").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"scope\":\"owner\",\"restrictions\":{\"bbox\":[1]}}"));

        assertThat(response.getStatus()).isEqualTo(400);
        JsonNode problem = body(response);
        assertThat(problem.get("code").asString()).isEqualTo("VALIDATION_FAILED");
        assertThat(problem.get("errors").findValuesAsString("field")).contains("name", "scope", "restrictions.bbox");
    }

    @Test
    @DisplayName("DELETE revokes (204), then the token is refused; the bootstrap token is 409; unknown is 404")
    void revoke() throws Exception {
        TokenService.CreatedToken reader = TestTokens.create(tokens, "phone", Scope.READ);
        String bootstrapId = body(perform(get("/v1/tokens").header(HttpHeaders.AUTHORIZATION, admin)))
                .findParents("bootstrap").stream().filter(n -> n.get("bootstrap").asBoolean()).findFirst()
                .orElseThrow().get("id").asString();

        assertThat(perform(delete("/v1/tokens/" + reader.entity().getId()).header(HttpHeaders.AUTHORIZATION, admin))
                .getStatus()).isEqualTo(204);
        assertThat(perform(get("/v1/records").header(HttpHeaders.AUTHORIZATION, TestTokens.bearer(reader)))
                .getStatus()).isEqualTo(401);
        MockHttpServletResponse bootstrap = perform(delete("/v1/tokens/" + bootstrapId).header(HttpHeaders.AUTHORIZATION, admin));
        assertThat(bootstrap.getStatus()).isEqualTo(409);
        assertThat(body(bootstrap).get("code").asString()).isEqualTo("BOOTSTRAP_TOKEN");
        assertThat(perform(delete("/v1/tokens/" + UUID.randomUUID()).header(HttpHeaders.AUTHORIZATION, admin))
                .getStatus()).isEqualTo(404);
        assertThat(perform(delete("/v1/tokens/not-a-uuid").header(HttpHeaders.AUTHORIZATION, admin)).getStatus())
                .isEqualTo(400);
        JsonNode listed = body(perform(get("/v1/tokens").header(HttpHeaders.AUTHORIZATION, admin)));
        assertThat(listed.findParents("revokedAt").stream().filter(n -> !n.get("revokedAt").isNull())).hasSize(1);
    }

    @Test
    @DisplayName("PUT /v1/tokens/current/device: the calling read token reports its own device")
    void currentDevice() throws Exception {
        TokenService.CreatedToken reader = TestTokens.create(tokens, "phone", Scope.READ);

        MockHttpServletResponse response = perform(put("/v1/tokens/current/device")
                .header(HttpHeaders.AUTHORIZATION, TestTokens.bearer(reader)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"deviceName\":\"iPhone 17\",\"platform\":\"ios\",\"appVersion\":\"2.1\"}"));

        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(200);
        JsonNode token = body(response);
        assertThat(token.get("id").asString()).isEqualTo(reader.entity().getId().toString());
        assertThat(token.get("device").get("deviceName").asString()).isEqualTo("iPhone 17");
        assertThat(token.get("device").get("reportedAt").isNull()).isFalse();
        assertThat(token.has("token")).isFalse();
        assertThat(perform(put("/v1/tokens/current/device").contentType(MediaType.APPLICATION_JSON)
                .content("{\"deviceName\":\"x\",\"platform\":\"ios\"}")).getStatus()).isEqualTo(401);
        assertThat(perform(put("/v1/tokens/current/device").header(HttpHeaders.AUTHORIZATION, TestTokens.bearer(reader))
                .contentType(MediaType.APPLICATION_JSON).content("{\"platform\":\"palm\"}")).getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("read and agent tokens cannot list, create or revoke tokens")
    void adminOnly() throws Exception {
        for (Scope scope : new Scope[] {Scope.READ, Scope.AGENT}) {
            String bearer = TestTokens.bearer(TestTokens.create(tokens, "t-" + scope, scope));
            assertThat(perform(get("/v1/tokens").header(HttpHeaders.AUTHORIZATION, bearer)).getStatus()).isEqualTo(403);
            assertThat(perform(post("/v1/tokens").header(HttpHeaders.AUTHORIZATION, bearer)
                    .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"x\",\"scope\":\"admin\"}")).getStatus())
                    .isEqualTo(403);
        }
    }
}
