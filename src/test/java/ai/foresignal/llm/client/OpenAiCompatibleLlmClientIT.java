package ai.foresignal.llm.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.llm.LlmHttp;
import ai.foresignal.testsupport.net.TestOutbound;
import ai.foresignal.llm.LlmProviderConfig;
import ai.foresignal.llm.LlmProviderType;
import ai.foresignal.test.WireMockSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@link OpenAiCompatibleLlmClient} against WireMock stubs of the OpenAI API. */
class OpenAiCompatibleLlmClientIT extends WireMockSupport {

    static final JsonMapper JSON = JsonMapper.builder().build();

    static LlmProviderConfig config(Double temperature) {
        return new LlmProviderConfig("router", LlmProviderType.OPENAI_COMPATIBLE, baseUrl() + "/v1", "gpt-test",
                "text-embedding-test", 4, null, 2048, temperature, Map.of("HTTP-Referer", "https://foresignal.ai"),
                false, "sk-test-not-a-key", 30, 2_000_000, true, null);
    }

    final OpenAiCompatibleLlmClient client = new OpenAiCompatibleLlmClient(config(0.3), new LlmHttp(TestOutbound.offlinePolicy(), TestOutbound.pinnedHttp()), JSON);

    static ChatRequest withTool() {
        return new ChatRequest("system text", List.of(new ChatMessage.User("weather?")),
                List.of(new ToolSpec("get_weather", "Weather", JSON.readTree(
                        "{\"type\":\"object\",\"properties\":{\"city\":{\"type\":\"string\"}},\"required\":[\"city\"]}"))),
                null, null, Purpose.ASK, false, null);
    }

    @Test
    @DisplayName("chat: bearer key, extra headers, tools, max_tokens, temperature; text and usage normalized")
    void chat() {
        stubFor(post(urlPathEqualTo("/v1/chat/completions")).willReturn(okJson("""
                {"id":"c1","model":"gpt-test-2026","choices":[{"index":0,"finish_reason":"stop",
                  "message":{"role":"assistant","content":"Sunny."}}],
                 "usage":{"prompt_tokens":40,"completion_tokens":5,"prompt_tokens_details":{"cached_tokens":32}}}""")));

        ChatResponse response = client.chat(withTool());

        assertThat(response.content()).isEqualTo("Sunny.");
        assertThat(response.stopReason()).isEqualTo(StopReason.END);
        assertThat(response.model()).isEqualTo("gpt-test-2026");
        assertThat(response.usage()).isEqualTo(new TokenUsage(40, 5, 32));
        var request = findAll(postRequestedFor(urlPathEqualTo("/v1/chat/completions"))).getFirst();
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer sk-test-not-a-key");
        assertThat(request.getHeader("HTTP-Referer")).isEqualTo("https://foresignal.ai");
        JsonNode body = JSON.readTree(request.getBodyAsString());
        assertThat(body.get("model").asString()).isEqualTo("gpt-test");
        assertThat(body.at("/messages/0/role").asString()).isEqualTo("system");
        assertThat(body.at("/messages/1/content").asString()).isEqualTo("weather?");
        assertThat(body.at("/tools/0/type").asString()).isEqualTo("function");
        assertThat(body.at("/tools/0/function/name").asString()).isEqualTo("get_weather");
        assertThat(body.at("/tools/0/function/parameters/required/0").asString()).isEqualTo("city");
        assertThat(body.get("max_tokens").asInt()).isEqualTo(2048);
        assertThat(body.get("temperature").asDouble()).isEqualTo(0.3);
    }

    @Test
    @DisplayName("tool calls: arguments parsed, finish_reason tool_calls, raw message replayed, one tool message per result")
    void toolCalls() {
        stubFor(post(urlPathEqualTo("/v1/chat/completions")).willReturn(okJson("""
                {"model":"gpt-test","choices":[{"index":0,"finish_reason":"tool_calls",
                  "message":{"role":"assistant","content":null,"tool_calls":[
                    {"id":"call_9","type":"function","function":{"name":"get_weather","arguments":"{\\"city\\":\\"Oslo\\"}"}}]}}],
                 "usage":{"prompt_tokens":10,"completion_tokens":7}}""")));

        ChatRequest request = withTool();
        ChatResponse first = client.chat(request);
        assertThat(first.stopReason()).isEqualTo(StopReason.TOOL_USE);
        assertThat(first.toolCalls()).singleElement().satisfies(call -> {
            assertThat(call.id()).isEqualTo("call_9");
            assertThat(call.input().get("city").asString()).isEqualTo("Oslo");
        });

        List<ChatMessage> conversation = new ArrayList<>(request.messages());
        conversation.add(first.assistantMessage());
        conversation.add(new ChatMessage.ToolResults(List.of(ToolResult.ok(first.toolCalls().getFirst(), "12C"),
                new ToolResult("call_x", "other", "failed", true))));
        client.chat(request.withMessages(conversation));

        JsonNode body = JSON.readTree(findAll(postRequestedFor(urlPathEqualTo("/v1/chat/completions"))).get(1)
                .getBodyAsString());
        JsonNode messages = body.get("messages");
        assertThat(messages.get(2)).isEqualTo(JSON.readTree("""
                {"role":"assistant","content":null,"tool_calls":[
                  {"id":"call_9","type":"function","function":{"name":"get_weather","arguments":"{\\"city\\":\\"Oslo\\"}"}}]}"""));
        assertThat(messages.get(3).get("role").asString()).isEqualTo("tool");
        assertThat(messages.get(3).get("tool_call_id").asString()).isEqualTo("call_9");
        assertThat(messages.get(3).get("content").asString()).isEqualTo("12C");
        assertThat(messages.get(4).get("content").asString()).startsWith("ERROR:");
    }

    @Test
    @DisplayName("finish reasons: content_filter → refusal, length → max_tokens")
    void finishReasons() {
        stubFor(post(urlPathEqualTo("/v1/chat/completions")).willReturn(okJson("""
                {"model":"gpt-test","choices":[{"index":0,"finish_reason":"content_filter",
                  "message":{"role":"assistant","content":""}}],"usage":{"prompt_tokens":1,"completion_tokens":0}}""")));
        ChatResponse refused = client.chat(withTool());
        assertThat(refused.stopReason()).isEqualTo(StopReason.REFUSAL);
        assertThat(refused.refusalCategory()).isEqualTo("content_filter");

        assertThat(OpenAiCompatibleLlmClient.stopReason("length")).isEqualTo(StopReason.MAX_TOKENS);
        assertThat(OpenAiCompatibleLlmClient.stopReason("weird")).isEqualTo(StopReason.ERROR);
        assertThat(OpenAiCompatibleLlmClient.stopReason(null)).isEqualTo(StopReason.END);
    }

    @Test
    @DisplayName("streaming: SSE chunks assembled, deltas delivered, tool-call fragments joined")
    void streaming() {
        String sse = String.join("\n",
                "data: {\"model\":\"gpt-test\",\"choices\":[{\"index\":0,\"delta\":{\"role\":\"assistant\",\"content\":\"Hel\"}}]}",
                "",
                "data: {\"model\":\"gpt-test\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"lo\"}}]}",
                "",
                "data: {\"model\":\"gpt-test\",\"choices\":[{\"index\":0,\"delta\":{\"tool_calls\":[{\"index\":0,\"id\":\"c1\","
                        + "\"function\":{\"name\":\"get_weather\",\"arguments\":\"{\\\"ci\"}}]}}]}",
                "",
                "data: {\"model\":\"gpt-test\",\"choices\":[{\"index\":0,\"delta\":{\"tool_calls\":[{\"index\":0,"
                        + "\"function\":{\"arguments\":\"ty\\\":\\\"Rome\\\"}\"}}]},\"finish_reason\":\"tool_calls\"}]}",
                "",
                "data: {\"model\":\"gpt-test\",\"choices\":[],\"usage\":{\"prompt_tokens\":9,\"completion_tokens\":4}}",
                "",
                "data: [DONE]", "");
        stubFor(post(urlPathEqualTo("/v1/chat/completions")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/event-stream").withBody(sse)));
        List<String> deltas = new ArrayList<>();

        ChatResponse response = client.chat(new ChatRequest("", List.of(new ChatMessage.User("x")), List.of(), null,
                null, Purpose.ASK, true, deltas::add));

        assertThat(deltas).containsExactly("Hel", "lo");
        assertThat(response.content()).isEqualTo("Hello");
        assertThat(response.stopReason()).isEqualTo(StopReason.TOOL_USE);
        assertThat(response.toolCalls()).singleElement()
                .satisfies(c -> assertThat(c.input().get("city").asString()).isEqualTo("Rome"));
        assertThat(response.usage()).isEqualTo(new TokenUsage(9, 4, 0));
        JsonNode body = JSON.readTree(findAll(postRequestedFor(urlPathEqualTo("/v1/chat/completions"))).getFirst()
                .getBodyAsString());
        assertThat(body.get("stream").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("embeddings: configured model without dimensions; explicit spec sends dimensions; order by index")
    void embeddings() {
        stubFor(post(urlPathEqualTo("/v1/embeddings")).willReturn(okJson("""
                {"model":"text-embedding-test","data":[
                  {"index":1,"embedding":[0.5,0.6,0.7,0.8]},{"index":0,"embedding":[0.1,0.2,0.3,0.4]}],
                 "usage":{"prompt_tokens":6}}""")));

        EmbeddingResponse configured = client.embed(List.of("a", "b"));
        EmbeddingResponse explicit = client.embed(List.of("a", "b"),
                new EmbeddingModelSpec("router", "text-embedding-test", 4));

        assertThat(configured.vectors().get(0)).containsExactly(0.1f, 0.2f, 0.3f, 0.4f);
        assertThat(configured.vectors().get(1)[0]).isEqualTo(0.5f);
        assertThat(configured.dimensions()).isEqualTo(4);
        assertThat(configured.usage().inputTokens()).isEqualTo(6);
        assertThat(explicit.vectors()).hasSize(2);
        var requests = findAll(postRequestedFor(urlPathEqualTo("/v1/embeddings")));
        assertThat(JSON.readTree(requests.get(0).getBodyAsString()).get("dimensions")).isNull();
        assertThat(JSON.readTree(requests.get(1).getBodyAsString()).get("dimensions").asInt()).isEqualTo(4);
        assertThatThrownBy(() -> client.embed(List.of("a"), new EmbeddingModelSpec("other", "m", 4)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("embeddings of the wrong width are refused")
    void embeddingWidth() {
        stubFor(post(urlPathEqualTo("/v1/embeddings")).willReturn(okJson(
                "{\"data\":[{\"index\":0,\"embedding\":[0.1,0.2]}]}")));

        assertThatThrownBy(() -> client.embed(List.of("a"))).isInstanceOfSatisfying(LlmProviderException.class,
                e -> assertThat(e.kind()).isEqualTo(LlmProviderException.Kind.INVALID_RESPONSE));
    }

    @Test
    @DisplayName("models: GET /models")
    void models() {
        stubFor(get(urlPathEqualTo("/v1/models")).withHeader("Authorization", equalTo("Bearer sk-test-not-a-key"))
                .willReturn(okJson("{\"data\":[{\"id\":\"gpt-test\"},{\"id\":\"text-embedding-test\",\"context_length\":8192}]}")));

        assertThat(client.models()).extracting(LlmModelInfo::id).containsExactly("gpt-test", "text-embedding-test");
        assertThat(client.models().get(1).contextWindow()).isEqualTo(8192L);
    }

    @Test
    @DisplayName("errors: 401 → authentication, 429 → rate limited with retry-after, 503 → overloaded, garbage → invalid response")
    void errors() {
        stubFor(post(urlPathEqualTo("/v1/chat/completions")).willReturn(aResponse().withStatus(401)
                .withBody("{\"error\":{\"message\":\"Incorrect API key\"}}")));
        assertThatThrownBy(() -> client.chat(withTool())).isInstanceOfSatisfying(LlmProviderException.class, e -> {
            assertThat(e.kind()).isEqualTo(LlmProviderException.Kind.AUTHENTICATION);
            assertThat(e.getMessage()).contains("Incorrect API key").doesNotContain("sk-test-not-a-key");
        });

        stubFor(post(urlPathEqualTo("/v1/chat/completions")).willReturn(aResponse().withStatus(429)
                .withHeader("retry-after", "20").withBody("{\"error\":{\"message\":\"rate\"}}")));
        assertThatThrownBy(() -> client.chat(withTool())).isInstanceOfSatisfying(LlmProviderException.class, e -> {
            assertThat(e.kind()).isEqualTo(LlmProviderException.Kind.RATE_LIMITED);
            assertThat(e.retryAfter()).hasSeconds(20);
        });

        stubFor(post(urlPathEqualTo("/v1/chat/completions")).willReturn(aResponse().withStatus(503).withBody("down")));
        assertThatThrownBy(() -> client.chat(withTool())).isInstanceOfSatisfying(LlmProviderException.class,
                e -> assertThat(e.kind()).isEqualTo(LlmProviderException.Kind.OVERLOADED));

        stubFor(post(urlPathEqualTo("/v1/chat/completions")).willReturn(aResponse().withStatus(200).withBody("<html>")));
        assertThatThrownBy(() -> client.chat(withTool())).isInstanceOfSatisfying(LlmProviderException.class,
                e -> assertThat(e.kind()).isEqualTo(LlmProviderException.Kind.INVALID_RESPONSE));
    }

    @Test
    @DisplayName("an unreachable provider is a connection error")
    void unreachable() {
        OpenAiCompatibleLlmClient dead = new OpenAiCompatibleLlmClient(new LlmProviderConfig("dead",
                LlmProviderType.OPENAI_COMPATIBLE, "http://127.0.0.1:1", "m", null, null, null, null, null, Map.of(),
                false, null, 30, 2_000_000, true, null), new LlmHttp(TestOutbound.offlinePolicy(), TestOutbound.pinnedHttp()), JSON);

        assertThatThrownBy(() -> dead.chat(withTool())).isInstanceOfSatisfying(LlmProviderException.class,
                e -> assertThat(e.kind()).isEqualTo(LlmProviderException.Kind.CONNECTION));
    }
}
