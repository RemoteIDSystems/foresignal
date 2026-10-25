package ai.foresignal.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.llm.EmbeddingModelChangedEvent;
import ai.foresignal.llm.LlmProviderRepository;
import ai.foresignal.llm.LlmUsageMeter;
import ai.foresignal.llm.client.EmbeddingModelSpec;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.notify.NotifyTestConfig;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code LLM} operations over HTTP (prompt 026). Providers point at WireMock stubs of the Ollama
 * API; no test calls a real model.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(NotifyTestConfig.class)
@RecordApplicationEvents
class LlmControllersIT extends WireMockSupport {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();
    static final String SECRET = "sk-ant-SECRETVALUE-not-a-key";

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired AppSettingService settings;
    @Autowired LlmProviderRepository providerRows;
    @Autowired LlmUsageMeter usage;
    @Autowired MutableClock clock;
    @Autowired ApplicationEvents events;
    @Autowired JdbcTemplate jdbc;

    String token;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        token = adminTokens.rotate();
        clock.setInstant(T);
    }

    MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request) {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    ResultActions send(MockHttpServletRequestBuilder request, String body) throws Exception {
        return mvc.perform(auth(request).contentType(MediaType.APPLICATION_JSON).content(body.replace('\'', '"')));
    }

    String ollama(String id, int rpm) {
        return "{'id':'" + id + "','type':'ollama','baseUrl':'" + baseUrl() + "','chatModel':'qwen3:14b',"
                + "'embeddingModel':'nomic-embed-text','embeddingDimensions':8,'requestsPerMinute':" + rpm + "}";
    }

    void stubOllama() {
        WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.post(urlPathEqualTo("/api/chat"))
                .willReturn(okJson("{\"model\":\"qwen3:14b\",\"message\":{\"role\":\"assistant\",\"content\":\"ok\"},"
                        + "\"done\":true,\"done_reason\":\"stop\",\"prompt_eval_count\":12,\"eval_count\":1}")));
        WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.post(urlPathEqualTo("/api/embed"))
                .willReturn(okJson("{\"model\":\"nomic-embed-text\",\"embeddings\":[[0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8]],"
                        + "\"prompt_eval_count\":1}")));
    }

    @Test
    @DisplayName("CRUD: create 201 with defaults, list, get, replace keeping the key, delete 204; the key is never returned")
    void crud() throws Exception {
        send(post("/v1/llm/providers"), "{'id':'claude','type':'anthropic','apiKey':'" + SECRET + "'}")
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/v1/llm/providers/claude"))
                .andExpect(jsonPath("$.type", is("anthropic")))
                .andExpect(jsonPath("$.baseUrl", is("https://api.anthropic.com")))
                .andExpect(jsonPath("$.chatModel", is("claude-opus-5-5")))
                .andExpect(jsonPath("$.options.effort", is("medium")))
                .andExpect(jsonPath("$.options.maxTokens", is(16000)))
                .andExpect(jsonPath("$.apiKeySet", is(true)))
                .andExpect(jsonPath("$.apiKey").doesNotExist())
                .andExpect(jsonPath("$.status", is("idle")))
                .andExpect(jsonPath("$.roles", hasSize(0)))
                .andExpect(jsonPath("$.local", is(false)))
                .andExpect(jsonPath("$.tokensToday", is(0)))
                .andExpect(r -> assertThat(r.getResponse().getContentAsString()).doesNotContain("SECRETVALUE"));
        send(post("/v1/llm/providers"), "{'id':'claude','type':'anthropic','apiKey':'x'}")
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code", is("LLM_PROVIDER_EXISTS")));

        mvc.perform(auth(get("/v1/llm/providers"))).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", contains("claude")))
                .andExpect(r -> assertThat(r.getResponse().getContentAsString()).doesNotContain("SECRETVALUE"));

        send(put("/v1/llm/providers/claude"), "{'type':'anthropic','options':{'effort':'high'},'knowledgeCutoff':'2025-08-01'}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apiKeySet", is(true)))
                .andExpect(jsonPath("$.options.effort", is("high")))
                .andExpect(jsonPath("$.knowledgeCutoff", is("2025-08-01")));
        assertThat(providerRows.findById("claude")).get().satisfies(e -> {
            assertThat(e.getSecretCiphertext()).isNotNull();
            assertThat(new String(e.getSecretCiphertext())).doesNotContain("SECRETVALUE");
        });

        mvc.perform(auth(get("/v1/llm/providers/claude"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.knowledgeCutoff", is("2025-08-01")));
        mvc.perform(auth(delete("/v1/llm/providers/claude"))).andExpect(status().isNoContent());
        mvc.perform(auth(get("/v1/llm/providers/claude"))).andExpect(status().isNotFound());
        mvc.perform(auth(delete("/v1/llm/providers/claude"))).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("validation: invalid baseUrl, anthropic without key, path/body id mismatch")
    void validation() throws Exception {
        for (String url : List.of("ftp://host", "http://user:pw@host:11434", "http://host:11434/#x")) {
            send(post("/v1/llm/providers"), "{'id':'bad','type':'ollama','baseUrl':'" + url + "'}")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")))
                    .andExpect(jsonPath("$.errors[0].field", is("baseUrl")));
        }
        send(post("/v1/llm/providers"), "{'id':'claude','type':'anthropic'}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field", is("apiKey")));
        send(post("/v1/llm/providers"), ollama("local", 30)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.local", is(true)));
        send(put("/v1/llm/providers/local"), "{'id':'other','type':'ollama'}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field", is("id")));
        send(put("/v1/llm/providers/missing"), "{'type':'ollama'}").andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("roles: Anthropic as embedding provider is 400; delete while assigned is 409; roles are listed")
    void roles() throws Exception {
        send(post("/v1/llm/providers"), "{'id':'claude','type':'anthropic','apiKey':'k'}").andExpect(status().isCreated());
        send(post("/v1/llm/providers"), ollama("ollama", 30)).andExpect(status().isCreated());

        send(put("/v1/llm/settings"), "{'chatProvider':'claude','embeddingProvider':'claude'}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field", is("embeddingProvider")))
                .andExpect(jsonPath("$.errors[0].message", containsString("Anthropic has no embeddings endpoint")));
        send(put("/v1/llm/settings"), "{'chatProvider':'nobody'}").andExpect(status().isBadRequest());

        send(put("/v1/llm/settings"), "{'chatProvider':'claude','embeddingProvider':'ollama','logContent':true}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatProvider", is("claude")))
                .andExpect(jsonPath("$.askMaxTokens", is(200000)))
                .andExpect(jsonPath("$.allowPrivateLocal", is(false)));
        mvc.perform(auth(get("/v1/llm/settings"))).andExpect(jsonPath("$.embeddingProvider", is("ollama")))
                .andExpect(jsonPath("$.logContent", is(true)));
        mvc.perform(auth(get("/v1/llm/providers/ollama"))).andExpect(jsonPath("$.roles", contains("embedding")));

        mvc.perform(auth(delete("/v1/llm/providers/claude"))).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("LLM_PROVIDER_IN_USE")));
        send(put("/v1/llm/providers/claude"), "{'type':'anthropic','enabled':false}").andExpect(status().isBadRequest());

        send(put("/v1/llm/settings"), "{'embeddingProvider':'ollama'}").andExpect(status().isOk());
        mvc.perform(auth(delete("/v1/llm/providers/claude"))).andExpect(status().isNoContent());

        // Prompt 039: allowPrivateLocal is accepted and stored; a non-boolean is still refused.
        send(put("/v1/llm/settings"), "{'allowPrivateLocal':true}").andExpect(status().isOk())
                .andExpect(jsonPath("$.allowPrivateLocal", is(true)));
        send(put("/v1/llm/settings"), "{'allowPrivateLocal':'yes'}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field", is("allowPrivateLocal")));
        send(put("/v1/llm/settings"), "{'allowPrivateLocal':false}").andExpect(status().isOk());
    }

    @Test
    @DisplayName("EmbeddingModelChangedEvent: on assigning the role (null → spec) and on changing its dimensions (old → new)")
    void embeddingModelChanged() throws Exception {
        send(post("/v1/llm/providers"), ollama("ollama", 30)).andExpect(status().isCreated());
        send(put("/v1/llm/settings"), "{'embeddingProvider':'ollama'}").andExpect(status().isOk());

        EmbeddingModelSpec first = new EmbeddingModelSpec("ollama", "nomic-embed-text", 8);
        assertThat(events.stream(EmbeddingModelChangedEvent.class))
                .containsExactly(new EmbeddingModelChangedEvent(null, first));

        send(put("/v1/llm/providers/ollama"), "{'type':'ollama','baseUrl':'" + baseUrl() + "','chatModel':'qwen3:14b',"
                + "'embeddingModel':'mxbai-embed-large','embeddingDimensions':1024}").andExpect(status().isOk());
        assertThat(events.stream(EmbeddingModelChangedEvent.class)).last()
                .isEqualTo(new EmbeddingModelChangedEvent(first,
                        new EmbeddingModelSpec("ollama", "mxbai-embed-large", 1024)));
        assertThat(first.modelId()).isEqualTo("ollama:nomic-embed-text:8");

        // A change that does not touch the embedding model publishes nothing.
        long before = events.stream(EmbeddingModelChangedEvent.class).count();
        send(put("/v1/llm/providers/ollama"), "{'type':'ollama','baseUrl':'" + baseUrl() + "','chatModel':'llama3.2',"
                + "'embeddingModel':'mxbai-embed-large','embeddingDimensions':1024}").andExpect(status().isOk());
        assertThat(events.stream(EmbeddingModelChangedEvent.class).count()).isEqualTo(before);
    }

    @Test
    @DisplayName("test endpoint against a stub: chat and embedding probes, usage series, health, budget 429")
    void testEndpoint() throws Exception {
        stubOllama();
        // Two requests per minute: the first test spends both (chat and embedding).
        send(post("/v1/llm/providers"), ollama("ollama", 2)).andExpect(status().isCreated());

        mvc.perform(auth(post("/v1/llm/providers/ollama/test"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.chat.text", is("ok")))
                .andExpect(jsonPath("$.chat.model", is("qwen3:14b")))
                .andExpect(jsonPath("$.chat.inputTokens", is(12)))
                .andExpect(jsonPath("$.chat.cachedTokens", is(0)))
                .andExpect(jsonPath("$.chat.stopReason", is("end")))
                .andExpect(jsonPath("$.embedding.dimensions", is(8)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.refusalCategory", nullValue()));
        JsonNode chat = JSON.readTree(WIREMOCK.findAll(com.github.tomakehurst.wiremock.client.WireMock
                .postRequestedFor(urlPathEqualTo("/api/chat"))).getFirst().getBodyAsString());
        assertThat(chat.at("/messages/0/content").asString()).isEqualTo("Reply with the single word: ok");
        assertThat(chat.at("/options/num_predict").asInt()).isEqualTo(16);

        mvc.perform(auth(get("/v1/llm/providers/ollama"))).andExpect(jsonPath("$.status", is("healthy")))
                .andExpect(jsonPath("$.lastSuccessAt", notNullValue()))
                .andExpect(jsonPath("$.tokensToday", is(14)));

        // The minute's requests are spent: the next test is refused before anything is sent.
        mvc.perform(auth(post("/v1/llm/providers/ollama/test"))).andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code", is("LLM_BUDGET_EXCEEDED")))
                .andExpect(header().exists("Retry-After"));

        mvc.perform(auth(get("/v1/llm/usage").param("provider", "ollama").param("resolution", "minute")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider", is("ollama")))
                .andExpect(jsonPath("$.resolution", is("minute")))
                .andExpect(jsonPath("$.points", hasSize(1)))
                .andExpect(jsonPath("$.totals.requests", is(2)))
                .andExpect(jsonPath("$.totals.inputTokens", is(13)))
                .andExpect(jsonPath("$.totals.outputTokens", is(1)))
                .andExpect(jsonPath("$.totals.refused", is(1)));
        usage.flush();
        mvc.perform(auth(get("/v1/llm/usage"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.provider", nullValue()))
                .andExpect(jsonPath("$.resolution", is("hour")))
                .andExpect(jsonPath("$.totals.requests", is(2)))
                .andExpect(jsonPath("$.totals.refused", is(1)));
        mvc.perform(auth(get("/v1/llm/usage").param("resolution", "week"))).andExpect(status().isBadRequest());
        mvc.perform(auth(get("/v1/llm/usage").param("from", "2026-05-02T00:00:00Z"))).andExpect(status().isBadRequest());
        mvc.perform(auth(get("/v1/llm/usage").param("provider", "nobody"))).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("test endpoint: a failing provider is reported in the result, health degrades, models is 502")
    void failingProvider() throws Exception {
        WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.post(urlPathEqualTo("/api/chat"))
                .willReturn(aResponse().withStatus(500).withBody("{\"error\":\"model crashed\"}")));
        WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.post(urlPathEqualTo("/api/embed"))
                .willReturn(aResponse().withStatus(500).withBody("{\"error\":\"model crashed\"}")));
        WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/api/tags"))
                .willReturn(aResponse().withStatus(503)));
        send(post("/v1/llm/providers"), ollama("ollama", 30)).andExpect(status().isCreated());

        mvc.perform(auth(post("/v1/llm/providers/ollama/test"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", containsString("model crashed")));
        mvc.perform(auth(get("/v1/llm/providers/ollama"))).andExpect(jsonPath("$.status", is("degraded")))
                .andExpect(jsonPath("$.consecutiveFailures", is(2)))
                .andExpect(jsonPath("$.lastError", containsString("model crashed")));
        mvc.perform(auth(get("/v1/llm/providers/ollama/models"))).andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code", is("LLM_PROVIDER_ERROR")));
    }

    @Test
    @DisplayName("models lists the provider's models")
    void models() throws Exception {
        WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/api/tags"))
                .willReturn(okJson("{\"models\":[{\"name\":\"qwen3:14b\",\"model\":\"qwen3:14b\"}]}")));
        send(post("/v1/llm/providers"), ollama("ollama", 30)).andExpect(status().isCreated());

        mvc.perform(auth(get("/v1/llm/providers/ollama/models"))).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is("qwen3:14b")));
        mvc.perform(auth(get("/v1/llm/providers/nobody/models"))).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("every LLM operation needs admin; without an LLM, /v1/system reports llm.configured false")
    void scopeAndSystem() throws Exception {
        mvc.perform(get("/v1/llm/providers")).andExpect(status().isUnauthorized());
        mvc.perform(post("/v1/llm/providers/x/test")).andExpect(status().isUnauthorized());
        mvc.perform(auth(get("/v1/system"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.llm.configured", is(false)))
                .andExpect(jsonPath("$.llm.tokensToday", is(0)))
                .andExpect(jsonPath("$.features.llm", is(false)));

        send(post("/v1/llm/providers"), ollama("ollama", 30)).andExpect(status().isCreated());
        send(put("/v1/llm/settings"), "{'chatProvider':'ollama'}").andExpect(status().isOk());
        mvc.perform(auth(get("/v1/system"))).andExpect(jsonPath("$.llm.configured", is(true)))
                .andExpect(jsonPath("$.llm.chatProvider", is("ollama")))
                .andExpect(jsonPath("$.features.llm", is(true)));
    }

    @Test
    @DisplayName("export carries providers without keys and the roles; import upserts them")
    void exportImport() throws Exception {
        send(post("/v1/llm/providers"), "{'id':'claude','type':'anthropic','apiKey':'" + SECRET + "'}")
                .andExpect(status().isCreated());
        send(post("/v1/llm/providers"), ollama("ollama", 30)).andExpect(status().isCreated());
        send(put("/v1/llm/settings"), "{'chatProvider':'claude','embeddingProvider':'ollama'}").andExpect(status().isOk());

        String bundle = mvc.perform(auth(get("/v1/settings/export"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.llmProviders", hasSize(2)))
                .andExpect(jsonPath("$.llmProviders[0].apiKey").doesNotExist())
                .andExpect(jsonPath("$.llmSettings.chatProvider", is("claude")))
                .andReturn().getResponse().getContentAsString();
        assertThat(bundle).doesNotContain("SECRETVALUE");

        send(put("/v1/llm/settings"), "{}").andExpect(status().isOk());
        mvc.perform(auth(delete("/v1/llm/providers/ollama"))).andExpect(status().isNoContent());

        mvc.perform(auth(put("/v1/settings/import")).contentType(MediaType.APPLICATION_JSON).content(bundle))
                .andExpect(status().isOk());
        mvc.perform(auth(get("/v1/llm/providers/ollama"))).andExpect(status().isOk());
        mvc.perform(auth(get("/v1/llm/providers/claude"))).andExpect(jsonPath("$.apiKeySet", is(true)));
        mvc.perform(auth(get("/v1/llm/settings"))).andExpect(jsonPath("$.embeddingProvider", is("ollama")));
    }

    String priced(String id, String extra) {
        return "{'id':'" + id + "','type':'ollama','baseUrl':'" + baseUrl() + "','chatModel':'qwen3:14b',"
                + "'embeddingModel':'nomic-embed-text','embeddingDimensions':8,"
                + "'pricing':{'inputUsdPerMillionTokens':1,'outputUsdPerMillionTokens':2,"
                + "'embeddingUsdPerMillionTokens':3}" + extra + "}";
    }

    @Test
    @DisplayName("pricing and USD caps round-trip on the provider and in the export; bad values are refused")
    void pricingAndCaps() throws Exception {
        send(post("/v1/llm/providers"), priced("ollama", ",'hourlyCapUsd':0.5,'dailyCapUsd':5,'monthlyCapUsd':50.25"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pricing.inputUsdPerMillionTokens", is(1)))
                .andExpect(jsonPath("$.pricing.cachedInputUsdPerMillionTokens", nullValue()))
                .andExpect(jsonPath("$.hourlyCapUsd", is(0.5)))
                .andExpect(jsonPath("$.monthlyCapUsd", is(50.25)));
        mvc.perform(auth(get("/v1/llm/providers/ollama"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.pricing.outputUsdPerMillionTokens", is(2)))
                .andExpect(jsonPath("$.pricing.embeddingUsdPerMillionTokens", is(3)))
                .andExpect(jsonPath("$.dailyCapUsd", is(5)));
        mvc.perform(auth(get("/v1/settings/export"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.llmProviders[0].pricing.inputUsdPerMillionTokens", is(1)))
                .andExpect(jsonPath("$.llmProviders[0].hourlyCapUsd", is(0.5)));
        // A replace without them clears them, as every replace does.
        send(put("/v1/llm/providers/ollama"), ollama("ollama", 30)).andExpect(status().isOk())
                .andExpect(jsonPath("$.pricing.inputUsdPerMillionTokens", nullValue()))
                .andExpect(jsonPath("$.hourlyCapUsd", nullValue()));

        send(post("/v1/llm/providers"), ollama("x", 30).replace("}", ",'dailyCapUsd':5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field", is("dailyCapUsd")))
                .andExpect(jsonPath("$.errors[0].message", containsString("needs pricing")));
        send(post("/v1/llm/providers"), priced("x", ",'hourlyCapUsd':0")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field", is("hourlyCapUsd")));
        send(post("/v1/llm/providers"), "{'id':'x','type':'ollama','pricing':{'inputUsdPerMillionTokens':-1}}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field", is("pricing.inputUsdPerMillionTokens")));
        send(post("/v1/llm/providers"), "{'id':'x','type':'ollama','pricing':{'inputUsdPerMillionTokens':0.1234567}}")
                .andExpect(status().isBadRequest());
        send(post("/v1/llm/providers"), "{'id':'c','type':'anthropic','apiKey':'k',"
                + "'pricing':{'embeddingUsdPerMillionTokens':1}}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field", is("pricing.embeddingUsdPerMillionTokens")));
    }

    @Test
    @DisplayName("budget: spend at the provider's prices per window, costUsd in usage, a spent cap refuses with 429")
    void budget() throws Exception {
        stubOllama();
        send(post("/v1/llm/providers"), priced("ollama", ",'hourlyCapUsd':1")).andExpect(status().isCreated());
        mvc.perform(auth(post("/v1/llm/providers/ollama/test"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // Chat 12 in × $1/M + 1 out × $2/M, embedding 1 × $3/M = $0.000017.
        mvc.perform(auth(get("/v1/llm/providers/ollama/budget"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.providerId", is("ollama")))
                .andExpect(jsonPath("$.currency", is("USD")))
                .andExpect(jsonPath("$.priced", is(true)))
                .andExpect(jsonPath("$.windows.hour.spentUsd", is(0.000017)))
                .andExpect(jsonPath("$.windows.hour.capUsd", is(1)))
                .andExpect(jsonPath("$.windows.hour.remainingUsd", is(0.999983)))
                .andExpect(jsonPath("$.windows.hour.start", is("2026-05-01T12:00:00Z")))
                .andExpect(jsonPath("$.windows.hour.resetsAt", is("2026-05-01T13:00:00Z")))
                .andExpect(jsonPath("$.windows.day.resetsAt", is("2026-05-02T00:00:00Z")))
                .andExpect(jsonPath("$.windows.month.start", is("2026-05-01T00:00:00Z")))
                .andExpect(jsonPath("$.windows.month.resetsAt", is("2026-06-01T00:00:00Z")))
                .andExpect(jsonPath("$.windows.day.capUsd", nullValue()))
                .andExpect(jsonPath("$.windows.day.remainingUsd", nullValue()))
                .andExpect(jsonPath("$.windows.hour.exhausted", is(false)))
                .andExpect(jsonPath("$.rateLimits.tokensToday", is(14)))
                .andExpect(jsonPath("$.providerBalance", nullValue()));
        mvc.perform(auth(get("/v1/llm/usage").param("provider", "ollama")))
                .andExpect(jsonPath("$.totals.costUsd", is(0.000017)));
        usage.flush();
        mvc.perform(auth(get("/v1/llm/usage").param("provider", "ollama")))
                .andExpect(jsonPath("$.totals.costUsd", is(0.000017)));

        // After a restart the spend is seeded from llm_usage, and a spent cap refuses the next call.
        jdbc.update("insert into llm_usage (provider_id, minute, requests, input_tokens, output_tokens, cached_tokens,"
                + " errors, refused, cost_usd) values ('ollama', ?, 1, 0, 0, 0, 0, 0, 1.5) on conflict (minute, provider_id)"
                + " do update set cost_usd = llm_usage.cost_usd + excluded.cost_usd", Timestamp.from(T));
        send(put("/v1/llm/providers/ollama"), priced("ollama", ",'hourlyCapUsd':1")).andExpect(status().isOk());
        mvc.perform(auth(delete("/v1/llm/providers/ollama"))).andExpect(status().isNoContent());
        send(post("/v1/llm/providers"), priced("ollama", ",'hourlyCapUsd':1")).andExpect(status().isCreated());
        mvc.perform(auth(get("/v1/llm/providers/ollama/budget")))
                .andExpect(jsonPath("$.windows.hour.spentUsd", is(1.500017)))
                .andExpect(jsonPath("$.windows.hour.remainingUsd", is(0)))
                .andExpect(jsonPath("$.windows.hour.exhausted", is(true)));
        mvc.perform(auth(post("/v1/llm/providers/ollama/test"))).andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code", is("LLM_BUDGET_EXCEEDED")))
                .andExpect(jsonPath("$.detail", containsString("hourly cap of $1")))
                .andExpect(header().string("Retry-After", "3600"));
        mvc.perform(auth(get("/v1/llm/providers/nobody/budget"))).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("probe: tests an unsaved provider and lists its models in one call, and persists nothing")
    void probe() throws Exception {
        stubOllama();
        WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/api/tags"))
                .willReturn(okJson("{\"models\":[{\"name\":\"qwen3:14b\",\"model\":\"qwen3:14b\"},"
                        + "{\"name\":\"nomic-embed-text\",\"model\":\"nomic-embed-text\"}]}")));

        send(post("/v1/llm/probe"), "{'type':'ollama','baseUrl':'" + baseUrl() + "','chatModel':'qwen3:14b'}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.test.success", is(true)))
                .andExpect(jsonPath("$.test.chat.text", is("ok")))
                .andExpect(jsonPath("$.test.error", nullValue()))
                .andExpect(jsonPath("$.models[*].id", contains("qwen3:14b", "nomic-embed-text")))
                .andExpect(jsonPath("$.modelsError", nullValue()));
        // Without a model, reading the model list is the connection test.
        send(post("/v1/llm/probe"), "{'type':'ollama','baseUrl':'" + baseUrl() + "'}").andExpect(status().isOk())
                .andExpect(jsonPath("$.test.success", is(true)))
                .andExpect(jsonPath("$.test.chat", nullValue()))
                .andExpect(jsonPath("$.models", hasSize(2)));

        usage.flush();
        mvc.perform(auth(get("/v1/llm/providers"))).andExpect(jsonPath("$", hasSize(0)));
        assertThat(jdbc.queryForObject("select count(*) from llm_usage", Long.class)).isZero();
        assertThat(providerRows.count()).isZero();

        send(post("/v1/llm/probe"), "{'type':'ollama','baseUrl':'not a url'}").andExpect(status().isBadRequest());
        send(post("/v1/llm/probe"), "{'type':'anthropic'}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field", is("apiKey")));
        mvc.perform(post("/v1/llm/probe").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("probe: the real error when the provider fails; a saved provider's stored key when apiKey is left out")
    void probeErrorsAndStoredKey() throws Exception {
        WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/v1/models"))
                .willReturn(aResponse().withStatus(401).withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":{\"message\":\"Incorrect API key provided\"}}")));
        String openAi = "{'id':'oa','type':'openai-compatible','baseUrl':'" + baseUrl() + "/v1'";

        send(post("/v1/llm/probe"), openAi + ",'apiKey':'sk-wrong'}").andExpect(status().isOk())
                .andExpect(jsonPath("$.test.success", is(false)))
                .andExpect(jsonPath("$.test.error", containsString("Incorrect API key provided")))
                .andExpect(jsonPath("$.models", hasSize(0)))
                .andExpect(jsonPath("$.modelsError", containsString("Incorrect API key provided")));

        WIREMOCK.resetAll();
        WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/v1/models"))
                .willReturn(okJson("{\"data\":[{\"id\":\"gpt-5\",\"context_length\":400000}]}")));
        send(post("/v1/llm/providers"), openAi + ",'apiKey':'sk-stored'}").andExpect(status().isCreated());
        send(post("/v1/llm/probe"), openAi + "}").andExpect(status().isOk())
                .andExpect(jsonPath("$.test.success", is(true)))
                .andExpect(jsonPath("$.models[0].id", is("gpt-5")))
                .andExpect(jsonPath("$.models[0].contextWindow", is(400000)));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/v1/models"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer sk-stored")));
        assertThat(providerRows.findById("oa").orElseThrow().getBaseUrl()).isEqualTo(baseUrl() + "/v1");
    }
}
