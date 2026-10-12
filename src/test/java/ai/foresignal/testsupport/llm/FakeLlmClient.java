package ai.foresignal.testsupport.llm;

import ai.foresignal.llm.client.ChatRequest;
import ai.foresignal.llm.client.ChatResponse;
import ai.foresignal.llm.client.EmbeddingModelSpec;
import ai.foresignal.llm.client.EmbeddingResponse;
import ai.foresignal.llm.client.LlmClient;
import ai.foresignal.llm.client.LlmModelInfo;
import ai.foresignal.llm.client.StopReason;
import ai.foresignal.llm.client.TokenUsage;
import ai.foresignal.llm.client.ToolCall;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * A deterministic {@link LlmClient} for consumers of the LLM abstraction (prompt 026; Ask, briefings,
 * enrichment and search tests use it). Chat answers come from a script, in order; every request is
 * recorded. Embeddings are a deterministic function of the text. No network, no model.
 */
public class FakeLlmClient implements LlmClient {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final String providerId;
    private final Deque<Function<ChatRequest, ChatResponse>> script = new ArrayDeque<>();
    private final List<ChatRequest> requests = new ArrayList<>();
    private final int dimensions;
    private Function<ChatRequest, ChatResponse> fallback;

    /** A fake for provider {@code providerId} with 8-dimensional embeddings. */
    public FakeLlmClient(String providerId) {
        this(providerId, 8);
    }

    /** A fake with {@code dimensions}-dimensional embeddings. */
    public FakeLlmClient(String providerId, int dimensions) {
        this.providerId = providerId;
        this.dimensions = dimensions;
        this.fallback = request -> text("ok");
    }

    /** Queues a fixed response. */
    public FakeLlmClient then(ChatResponse response) {
        script.add(request -> response);
        return this;
    }

    /** Queues a response computed from the request. */
    public FakeLlmClient then(Function<ChatRequest, ChatResponse> responder) {
        script.add(responder);
        return this;
    }

    /** The response once the script is used up. */
    public FakeLlmClient otherwise(Function<ChatRequest, ChatResponse> responder) {
        this.fallback = responder;
        return this;
    }

    /** Every chat request received, in order. */
    public synchronized List<ChatRequest> requests() {
        return List.copyOf(requests);
    }

    @Override
    public String providerId() {
        return providerId;
    }

    @Override
    public synchronized ChatResponse chat(ChatRequest request) {
        requests.add(request);
        Function<ChatRequest, ChatResponse> next = script.isEmpty() ? fallback : script.poll();
        return next.apply(request);
    }

    @Override
    public EmbeddingResponse embed(List<String> texts) {
        return new EmbeddingResponse(texts.stream().map(this::vector).toList(), "fake-embed", dimensions,
                new TokenUsage(texts.stream().mapToLong(t -> (t.length() + 3) / 4).sum(), 0, 0));
    }

    @Override
    public EmbeddingResponse embed(List<String> texts, EmbeddingModelSpec spec) {
        if (!providerId.equals(spec.providerId())) {
            throw new IllegalArgumentException("spec names another provider");
        }
        FakeLlmClient sized = new FakeLlmClient(providerId, spec.dimensions());
        return sized.embed(texts);
    }

    @Override
    public List<LlmModelInfo> models() {
        return List.of(new LlmModelInfo("fake-chat", "Fake chat", 32_000L, true, false),
                new LlmModelInfo("fake-embed", "Fake embed", null, false, true));
    }

    private float[] vector(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        float[] out = new float[dimensions];
        for (int i = 0; i < bytes.length; i++) {
            out[i % dimensions] += (bytes[i] & 0xff) / 255f;
        }
        return out;
    }

    // ---------- response builders ----------

    /** A final text answer. */
    public static ChatResponse text(String content) {
        return new ChatResponse(content, List.of(), StopReason.END, null, new TokenUsage(10, 5, 0), "fake-chat",
                null);
    }

    /** A final text answer using {@code usage}. */
    public static ChatResponse text(String content, TokenUsage usage) {
        return new ChatResponse(content, List.of(), StopReason.END, null, usage, "fake-chat", null);
    }

    /** A turn asking for {@code calls}. */
    public static ChatResponse toolUse(ToolCall... calls) {
        return toolUse(new TokenUsage(10, 5, 0), calls);
    }

    /** A turn asking for {@code calls}, using {@code usage}. */
    public static ChatResponse toolUse(TokenUsage usage, ToolCall... calls) {
        return new ChatResponse("", List.of(calls), StopReason.TOOL_USE, null, usage, "fake-chat",
                "raw-" + calls.length);
    }

    /** A refusal. */
    public static ChatResponse refusal(String category, ToolCall... calls) {
        return new ChatResponse("", List.of(calls), StopReason.REFUSAL, category, new TokenUsage(10, 0, 0),
                "fake-chat", null);
    }

    /** A truncated turn. */
    public static ChatResponse maxTokens(ToolCall... calls) {
        return new ChatResponse("cut", List.of(calls), StopReason.MAX_TOKENS, null, new TokenUsage(10, 100, 0),
                "fake-chat", null);
    }

    /** A tool call with JSON {@code input}. */
    public static ToolCall call(String id, String name, String input) {
        JsonNode node = JSON.readTree(input);
        return new ToolCall(id, name, node);
    }
}
