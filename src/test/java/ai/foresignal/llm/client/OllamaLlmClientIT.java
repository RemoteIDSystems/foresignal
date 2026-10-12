package ai.foresignal.llm.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
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

/** {@link OllamaLlmClient} against WireMock stubs of the Ollama API. */
class OllamaLlmClientIT extends WireMockSupport {

    static final JsonMapper JSON = JsonMapper.builder().build();

    static LlmProviderConfig config() {
        return new LlmProviderConfig("ollama", LlmProviderType.OLLAMA, baseUrl(), "qwen3:14b", "nomic-embed-text", 3,
                null, null, null, Map.of(), false, null, 30, 2_000_000, true, null);
    }

    final OllamaLlmClient client = new OllamaLlmClient(config(), new LlmHttp(), JSON);

    static ChatRequest withTool() {
        return new ChatRequest("system", List.of(new ChatMessage.User("where?")),
                List.of(new ToolSpec("lookup", "Look up", JSON.readTree("{\"type\":\"object\",\"properties\":{}}"))),
                512, null, Purpose.ASK, false, null);
    }

    @Test
    @DisplayName("chat: /api/chat with stream false, tools and num_predict; text, usage and model normalized")
    void chat() {
        stubFor(post(urlPathEqualTo("/api/chat")).willReturn(okJson("""
                {"model":"qwen3:14b","message":{"role":"assistant","content":"Here."},"done":true,
                 "done_reason":"stop","prompt_eval_count":30,"eval_count":4}""")));

        ChatResponse response = client.chat(withTool());

        assertThat(response.content()).isEqualTo("Here.");
        assertThat(response.stopReason()).isEqualTo(StopReason.END);
        assertThat(response.usage()).isEqualTo(new TokenUsage(30, 4, 0));
        assertThat(response.model()).isEqualTo("qwen3:14b");
        JsonNode body = JSON.readTree(findAll(postRequestedFor(urlPathEqualTo("/api/chat"))).getFirst()
                .getBodyAsString());
        assertThat(body.get("stream").asBoolean()).isFalse();
        assertThat(body.at("/options/num_predict").asInt()).isEqualTo(512);
        assertThat(body.at("/tools/0/function/name").asString()).isEqualTo("lookup");
        assertThat(body.at("/messages/0/role").asString()).isEqualTo("system");
    }

    @Test
    @DisplayName("tool calls: ids synthesized, raw message replayed, results carry tool_name")
    void toolCalls() {
        stubFor(post(urlPathEqualTo("/api/chat")).willReturn(okJson("""
                {"model":"qwen3:14b","message":{"role":"assistant","content":"",
                  "tool_calls":[{"function":{"name":"lookup","arguments":{"q":"Tokyo"}}}]},
                 "done":true,"done_reason":"stop","prompt_eval_count":10,"eval_count":3}""")));

        ChatRequest request = withTool();
        ChatResponse first = client.chat(request);
        assertThat(first.stopReason()).isEqualTo(StopReason.TOOL_USE);
        assertThat(first.toolCalls()).singleElement().satisfies(call -> {
            assertThat(call.id()).isEqualTo("call_0");
            assertThat(call.input().get("q").asString()).isEqualTo("Tokyo");
        });

        List<ChatMessage> conversation = new ArrayList<>(request.messages());
        conversation.add(first.assistantMessage());
        conversation.add(new ChatMessage.ToolResults(List.of(ToolResult.ok(first.toolCalls().getFirst(), "found"))));
        client.chat(request.withMessages(conversation));

        JsonNode messages = JSON.readTree(findAll(postRequestedFor(urlPathEqualTo("/api/chat"))).get(1)
                .getBodyAsString()).get("messages");
        assertThat(messages.get(2)).isEqualTo(JSON.readTree("""
                {"role":"assistant","content":"","tool_calls":[{"function":{"name":"lookup","arguments":{"q":"Tokyo"}}}]}"""));
        assertThat(messages.get(3).get("role").asString()).isEqualTo("tool");
        assertThat(messages.get(3).get("tool_name").asString()).isEqualTo("lookup");
        assertThat(messages.get(3).get("content").asString()).isEqualTo("found");
    }

    @Test
    @DisplayName("a model without tool support is detected as TOOLS_UNSUPPORTED")
    void toolsUnsupported() {
        stubFor(post(urlPathEqualTo("/api/chat")).willReturn(aResponse().withStatus(400)
                .withBody("{\"error\":\"registry.ollama.ai/library/gemma:2b does not support tools\"}")));

        assertThatThrownBy(() -> client.chat(withTool())).isInstanceOfSatisfying(LlmProviderException.class,
                e -> assertThat(e.kind()).isEqualTo(LlmProviderException.Kind.TOOLS_UNSUPPORTED));
    }

    @Test
    @DisplayName("other errors: 404 model not found → invalid request; 500 → overloaded")
    void errors() {
        stubFor(post(urlPathEqualTo("/api/chat")).willReturn(aResponse().withStatus(404)
                .withBody("{\"error\":\"model 'qwen3:14b' not found\"}")));
        assertThatThrownBy(() -> client.chat(withTool())).isInstanceOfSatisfying(LlmProviderException.class, e -> {
            assertThat(e.kind()).isEqualTo(LlmProviderException.Kind.INVALID_REQUEST);
            assertThat(e.getMessage()).contains("not found");
        });

        stubFor(post(urlPathEqualTo("/api/chat")).willReturn(aResponse().withStatus(500).withBody("{\"error\":\"oom\"}")));
        assertThatThrownBy(() -> client.chat(withTool())).isInstanceOfSatisfying(LlmProviderException.class,
                e -> assertThat(e.kind()).isEqualTo(LlmProviderException.Kind.OVERLOADED));
    }

    @Test
    @DisplayName("streaming: NDJSON chunks assembled until done")
    void streaming() {
        String ndjson = String.join("\n",
                "{\"model\":\"qwen3:14b\",\"message\":{\"role\":\"assistant\",\"content\":\"Hi\"},\"done\":false}",
                "{\"model\":\"qwen3:14b\",\"message\":{\"role\":\"assistant\",\"content\":\" there\"},\"done\":false}",
                "{\"model\":\"qwen3:14b\",\"message\":{\"role\":\"assistant\",\"content\":\"\"},\"done\":true,"
                        + "\"done_reason\":\"length\",\"prompt_eval_count\":7,\"eval_count\":2}", "");
        stubFor(post(urlPathEqualTo("/api/chat")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/x-ndjson").withBody(ndjson)));
        List<String> deltas = new ArrayList<>();

        ChatResponse response = client.chat(new ChatRequest("", List.of(new ChatMessage.User("x")), List.of(), null,
                null, Purpose.BRIEFING, true, deltas::add));

        assertThat(deltas).containsExactly("Hi", " there");
        assertThat(response.content()).isEqualTo("Hi there");
        assertThat(response.stopReason()).isEqualTo(StopReason.MAX_TOKENS);
        assertThat(response.usage()).isEqualTo(new TokenUsage(7, 2, 0));
    }

    @Test
    @DisplayName("embeddings: batch input to /api/embed; explicit spec sends dimensions")
    void embeddings() {
        stubFor(post(urlPathEqualTo("/api/embed")).willReturn(okJson("""
                {"model":"nomic-embed-text","embeddings":[[0.1,0.2,0.3],[0.4,0.5,0.6]],"prompt_eval_count":5}""")));

        EmbeddingResponse configured = client.embed(List.of("a", "b"));
        EmbeddingResponse explicit = client.embed(List.of("a", "b"), new EmbeddingModelSpec("ollama", "nomic-embed-text", 3));

        assertThat(configured.vectors()).hasSize(2);
        assertThat(configured.vectors().get(1)).containsExactly(0.4f, 0.5f, 0.6f);
        assertThat(configured.dimensions()).isEqualTo(3);
        assertThat(configured.usage().inputTokens()).isEqualTo(5);
        assertThat(explicit.model()).isEqualTo("nomic-embed-text");
        var requests = findAll(postRequestedFor(urlPathEqualTo("/api/embed")));
        JsonNode first = JSON.readTree(requests.get(0).getBodyAsString());
        assertThat(first.get("input").size()).isEqualTo(2);
        assertThat(first.get("dimensions")).isNull();
        assertThat(JSON.readTree(requests.get(1).getBodyAsString()).get("dimensions").asInt()).isEqualTo(3);
    }

    @Test
    @DisplayName("models: /api/tags")
    void models() {
        stubFor(get(urlPathEqualTo("/api/tags")).willReturn(okJson("""
                {"models":[{"name":"qwen3:14b","model":"qwen3:14b","details":{"family":"qwen3"}},
                           {"name":"nomic-embed-text:latest","model":"nomic-embed-text:latest","details":{"family":"nomic-bert"}}]}""")));

        List<LlmModelInfo> models = client.models();

        assertThat(models).extracting(LlmModelInfo::id).containsExactly("qwen3:14b", "nomic-embed-text:latest");
        assertThat(models.get(1).supportsEmbeddings()).isTrue();
        assertThat(models.get(0).supportsEmbeddings()).isNull();
    }
}
