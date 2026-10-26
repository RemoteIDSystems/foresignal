package ai.foresignal.llm;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.user.BuiltInRole;
import ai.foresignal.security.user.PasswordHasher;
import ai.foresignal.security.user.UserAdminService;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.security.user.UserRoleRepository;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.security.TestUsers;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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

/**
 * Shared and personal LLM providers through the real filter chain (prompt R2-011): who creates, sees and manages which
 * provider, per-user metering, {@code getMyLlmBudget}, and what deleting a user does to their providers. Providers
 * point at a WireMock stub of the Ollama API; no test calls a real model.
 */
@IntegrationTest
class PerUserLlmIT extends WireMockSupport {

    static final Instant T = Instant.parse("2026-10-12T12:00:00Z");

    @Autowired private MockMvc mvc;
    @Autowired private JsonMapper json;
    @Autowired private PrincipalService principals;
    @Autowired private UserRepository users;
    @Autowired private UserRoleRepository userRoles;
    @Autowired private PasswordHasher hasher;
    @Autowired private TokenService tokens;
    @Autowired private LlmProviderRepository providers;
    @Autowired private LlmUsageMeter usage;
    @Autowired private UserAdminService userAdmin;
    @Autowired private AppSettingService settings;
    @Autowired private MutableClock clock;
    @Autowired private JdbcTemplate jdbc;

    private String adminToken;
    private UserEntity alice;
    private String aliceSession;
    private String bobSession;
    private String operatorSession;

    @BeforeEach
    void setUp() throws Exception {
        settings.invalidateCache();
        clock.setInstant(T);
        adminToken = TestTokens.create(tokens, "llm-it-admin", Scope.ADMIN).plaintext();
        alice = TestUsers.active(principals, users, userRoles, hasher, "alice@example.com", BuiltInRole.EDITOR);
        TestUsers.active(principals, users, userRoles, hasher, "bob@example.com", BuiltInRole.EDITOR);
        TestUsers.active(principals, users, userRoles, hasher, "op@example.com", BuiltInRole.OPERATOR);
        aliceSession = login("alice@example.com");
        bobSession = login("bob@example.com");
        operatorSession = login("op@example.com");
    }

    // ------------------------------------------------------------------------- helpers

    private MockHttpServletResponse call(MockHttpServletRequestBuilder builder, String bearer) throws Exception {
        builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer);
        return mvc.perform(builder).andReturn().getResponse();
    }

    private static MockHttpServletRequestBuilder jsonBody(MockHttpServletRequestBuilder builder, String body) {
        return builder.contentType(MediaType.APPLICATION_JSON).content(body.replace('\'', '"'));
    }

    private JsonNode body(MockHttpServletResponse r) throws Exception {
        return json.readTree(r.getContentAsString());
    }

    private String login(String email) throws Exception {
        MockHttpServletResponse r = mvc.perform(jsonBody(post("/v1/auth/login"), TestUsers.loginBody(email)))
                .andReturn().getResponse();
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        return body(r).get("token").asString();
    }

    private String provider(String id, String extra) {
        return "{'id':'" + id + "','type':'ollama','baseUrl':'" + baseUrl() + "','chatModel':'qwen3:14b'" + extra
                + "}";
    }

    private JsonNode create(String bearer, String id, String extra) throws Exception {
        MockHttpServletResponse r = call(jsonBody(post("/v1/llm/providers"), provider(id, extra)), bearer);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(201);
        return body(r);
    }

    private List<String> ids(String bearer) throws Exception {
        MockHttpServletResponse r = call(get("/v1/llm/providers"), bearer);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        List<String> out = new ArrayList<>();
        body(r).forEach(p -> out.add(p.get("id").asString()));
        return out;
    }

    private void assertDenied(MockHttpServletResponse r, String permission) throws Exception {
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(403);
        assertThat(body(r).get("code").asString()).isEqualTo(ErrorCodes.PERMISSION_DENIED);
        assertThat(body(r).get("requiredPermission").asString()).isEqualTo(permission);
    }

    private void stubOllama() {
        WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.post(urlPathEqualTo("/api/chat"))
                .willReturn(okJson("{\"model\":\"qwen3:14b\",\"message\":{\"role\":\"assistant\",\"content\":\"ok\"},"
                        + "\"done\":true,\"done_reason\":\"stop\",\"prompt_eval_count\":12,\"eval_count\":1}")));
    }

    // ------------------------------------------------------------------------- ownership

    @Test
    @DisplayName("providers created before v2.0 and by API tokens are shared; an Editor's is personal; an Operator's shared")
    void kinds() throws Exception {
        jdbc.update("insert into llm_provider (id, type, base_url, options, requests_per_minute, tokens_per_day,"
                + " enabled, consecutive_failures, created_at, updated_at) values ('legacy', 'ollama', ?, '{}'::jsonb,"
                + " 30, 2000000, true, 0, now(), now())", baseUrl());
        assertThat(providers.findById("legacy").orElseThrow().isShared()).as("a row without an owner is shared").isTrue();
        JsonNode machine = create(adminToken, "machine", "");
        assertThat(machine.get("shared").asBoolean()).isTrue();
        assertThat(machine.get("ownerPrincipalId").isNull()).isTrue();
        JsonNode mine = create(aliceSession, "alices", "");
        assertThat(mine.get("shared").asBoolean()).isFalse();
        assertThat(mine.get("ownerPrincipalId").asString()).isEqualTo(alice.getPrincipalId());
        assertThat(create(operatorSession, "ops", "").get("shared").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("asking for the other kind needs its permission")
    void explicitKind() throws Exception {
        assertDenied(call(jsonBody(post("/v1/llm/providers"), provider("s", ",'shared':true")), aliceSession),
                "llm.shared.manage");
        assertDenied(call(jsonBody(post("/v1/llm/providers"), provider("p", ",'shared':false")), operatorSession),
                "self.llm");
        assertThat(call(jsonBody(post("/v1/llm/providers"), provider("x", ",'shared':'yes'")), aliceSession)
                .getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("a user lists their own and the shared providers; an admin token lists all")
    void listing() throws Exception {
        create(aliceSession, "alices", "");
        create(bobSession, "bobs", "");
        create(operatorSession, "ops", "");
        assertThat(ids(aliceSession)).containsExactlyInAnyOrder("alices", "ops");
        assertThat(ids(bobSession)).containsExactlyInAnyOrder("bobs", "ops");
        assertThat(ids(adminToken)).contains("alices", "bobs", "ops");
    }

    @Test
    @DisplayName("another user's provider is 404 on every per-provider operation; the key is never returned")
    void crossUser() throws Exception {
        create(aliceSession, "alices", ",'apiKey':'sk-personal-SECRET'");
        assertThat(call(get("/v1/llm/providers/alices"), bobSession).getStatus()).isEqualTo(404);
        assertThat(call(jsonBody(put("/v1/llm/providers/alices"), provider("alices", "")), bobSession).getStatus())
                .isEqualTo(404);
        assertThat(call(post("/v1/llm/providers/alices/test"), bobSession).getStatus()).isEqualTo(404);
        assertThat(call(get("/v1/llm/providers/alices/models"), bobSession).getStatus()).isEqualTo(404);
        assertThat(call(get("/v1/llm/providers/alices/budget"), bobSession).getStatus()).isEqualTo(404);
        assertThat(call(delete("/v1/llm/providers/alices"), bobSession).getStatus()).isEqualTo(404);
        assertThat(call(get("/v1/llm/providers/alices"), operatorSession).getStatus()).isEqualTo(404);
        MockHttpServletResponse own = call(get("/v1/llm/providers/alices"), aliceSession);
        assertThat(own.getStatus()).isEqualTo(200);
        assertThat(own.getContentAsString()).doesNotContain("sk-personal-SECRET");
        assertThat(body(own).get("apiKeySet").asBoolean()).isTrue();
        assertThat(call(delete("/v1/llm/providers/alices"), aliceSession).getStatus()).isEqualTo(204);
    }

    @Test
    @DisplayName("a user may read a shared provider but not change, test or budget it; an Operator may")
    void sharedManagedByOperator() throws Exception {
        create(operatorSession, "ops", "");
        assertThat(call(get("/v1/llm/providers/ops"), aliceSession).getStatus()).isEqualTo(200);
        assertDenied(call(jsonBody(put("/v1/llm/providers/ops"), provider("ops", "")), aliceSession),
                "llm.shared.manage");
        assertDenied(call(post("/v1/llm/providers/ops/test"), aliceSession), "llm.shared.manage");
        assertDenied(call(get("/v1/llm/providers/ops/budget"), aliceSession), "llm.shared.manage");
        assertDenied(call(delete("/v1/llm/providers/ops"), aliceSession), "llm.shared.manage");
        assertDenied(call(get("/v1/llm/settings"), aliceSession), "llm.shared.manage");
        MockHttpServletResponse renamed = call(jsonBody(put("/v1/llm/providers/ops"),
                provider("ops", ",'requestsPerMinute':5")), operatorSession);
        assertThat(renamed.getStatus()).as(renamed.getContentAsString()).isEqualTo(200);
        assertThat(body(renamed).get("shared").asBoolean()).as("a replace keeps the kind").isTrue();
    }

    @Test
    @DisplayName("only a shared provider takes per-user caps or an instance role")
    void sharedOnlyFeatures() throws Exception {
        String priced = ",'pricing':{'inputUsdPerMillionTokens':1,'outputUsdPerMillionTokens':1}";
        MockHttpServletResponse capped = call(jsonBody(post("/v1/llm/providers"),
                provider("alices", priced + ",'perUserMonthlyCapUsd':5")), aliceSession);
        assertThat(capped.getStatus()).as(capped.getContentAsString()).isEqualTo(400);
        create(aliceSession, "alices", "");
        MockHttpServletResponse role = call(jsonBody(put("/v1/llm/settings"), "{'chatProvider':'alices'}"),
                adminToken);
        assertThat(role.getStatus()).as(role.getContentAsString()).isEqualTo(400);
        assertThat(role.getContentAsString()).contains("personal provider");
        JsonNode shared = create(operatorSession, "ops", priced + ",'perUserMonthlyCapUsd':5");
        assertThat(shared.get("perUserMonthlyCapUsd").decimalValue()).isEqualByComparingTo("5");
        assertThat(shared.get("local").asBoolean()).isTrue();
        assertThat(create(aliceSession, "alices-local", ",'options':{'local':true}").get("local").asBoolean())
                .as("a personal provider is never local").isFalse();
    }

    // ------------------------------------------------------------------------- metering and budget

    @Test
    @DisplayName("a user's call is metered to them and to the provider; getMyLlmBudget reports it")
    void meteredPerUser() throws Exception {
        stubOllama();
        String priced = ",'pricing':{'inputUsdPerMillionTokens':1000,'outputUsdPerMillionTokens':1000},"
                + "'monthlyCapUsd':10";
        create(aliceSession, "alices", priced);
        create(operatorSession, "ops", priced.replace("'monthlyCapUsd':10", "'perUserMonthlyCapUsd':2"));
        MockHttpServletResponse test = call(post("/v1/llm/providers/alices/test"), aliceSession);
        assertThat(test.getStatus()).as(test.getContentAsString()).isEqualTo(200);

        Instant month = LlmSpendWindow.MONTH.start(T);
        assertThat(usage.userTotals("alices", alice.getPrincipalId(), month, T.plusSeconds(120)).requests())
                .isEqualTo(1);
        assertThat(usage.totals("alices", month, T.plusSeconds(120)).requests()).isEqualTo(1);
        BigDecimal spent = usage.userCostSince("alices", alice.getPrincipalId(), month);
        assertThat(spent).isPositive();

        MockHttpServletResponse r = call(get("/v1/me/llm/budget"), aliceSession);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        JsonNode budget = body(r);
        assertThat(budget.get("asOf").asString()).isEqualTo(T.toString());
        assertThat(budget.get("totalSpentUsd").decimalValue()).isEqualByComparingTo(spent);
        JsonNode mine = null;
        JsonNode ops = null;
        for (JsonNode p : budget.get("providers")) {
            if (p.get("providerId").asString().equals("alices")) {
                mine = p;
            } else if (p.get("providerId").asString().equals("ops")) {
                ops = p;
            }
        }
        assertThat(mine).isNotNull();
        assertThat(mine.get("shared").asBoolean()).isFalse();
        assertThat(mine.get("capUsd").decimalValue()).isEqualByComparingTo("10");
        assertThat(mine.get("spentUsd").decimalValue()).isEqualByComparingTo(spent);
        assertThat(ops).isNotNull();
        assertThat(ops.get("shared").asBoolean()).isTrue();
        assertThat(ops.get("capUsd").decimalValue()).as("the user's share").isEqualByComparingTo("2");
        assertThat(ops.get("spentUsd").decimalValue()).isEqualByComparingTo("0");
        assertThat(ops.get("remainingUsd").decimalValue()).isEqualByComparingTo("2");

        JsonNode bobs = body(call(get("/v1/me/llm/budget"), bobSession));
        assertThat(bobs.get("totalSpentUsd").decimalValue()).isEqualByComparingTo("0");
        assertThat(bobs.toString()).doesNotContain("alices");
    }

    @Test
    @DisplayName("a machine token's calls are the instance's: metered to the provider, not to any user")
    void machineUsage() throws Exception {
        stubOllama();
        create(adminToken, "shared", "");
        assertThat(call(post("/v1/llm/providers/shared/test"), adminToken).getStatus()).isEqualTo(200);
        usage.flush();
        Integer rows = jdbc.queryForObject("select count(*) from llm_user_usage", Integer.class);
        assertThat(rows).isZero();
        assertThat(usage.totals("shared", LlmSpendWindow.MONTH.start(T), T.plusSeconds(120)).requests()).isEqualTo(1);
    }

    @Test
    @DisplayName("getMyLlmBudget needs self.llm and a signed-in session")
    void budgetGates() throws Exception {
        TestUsers.active(principals, users, userRoles, hasher, "viewer@example.com", BuiltInRole.VIEWER);
        assertDenied(call(get("/v1/me/llm/budget"), login("viewer@example.com")), "self.llm");
        assertThat(call(get("/v1/me/llm/budget"), adminToken).getStatus()).isEqualTo(401);
    }

    // ------------------------------------------------------------------------- deletion

    @Test
    @DisplayName("deleting a user moves their providers to the acting admin")
    void reassigned() throws Exception {
        UserEntity admin = TestUsers.active(principals, users, userRoles, hasher, "admin@example.com",
                BuiltInRole.ADMIN);
        create(aliceSession, "alices", "");
        UserAdminService.Deletion deletion = userAdmin.delete(alice.getId(), true, admin.getId(), "127.0.0.1");
        assertThat(deletion.counts()).containsEntry("llmProviders", 1);
        LlmProviderEntity moved = providers.findById("alices").orElseThrow();
        assertThat(moved.getOwnerPrincipalId()).isEqualTo(admin.getPrincipalId());
        assertThat(moved.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("with no admin to receive them, a deleted user's providers become shared and disabled")
    void orphaned() throws Exception {
        create(aliceSession, "alices", "");
        userAdmin.delete(alice.getId(), true, null, "127.0.0.1");
        LlmProviderEntity orphan = providers.findById("alices").orElseThrow();
        assertThat(orphan.isShared()).isTrue();
        assertThat(orphan.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("reassign=false deletes the user's providers")
    void deleted() throws Exception {
        TestUsers.active(principals, users, userRoles, hasher, "admin@example.com", BuiltInRole.ADMIN);
        create(aliceSession, "alices", "");
        userAdmin.delete(alice.getId(), false, null, "127.0.0.1");
        assertThat(providers.existsById("alices")).isFalse();
    }
}
