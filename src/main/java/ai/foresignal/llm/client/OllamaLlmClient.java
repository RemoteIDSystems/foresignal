package ai.foresignal.llm.client;

import ai.foresignal.llm.LlmHttp;
import ai.foresignal.llm.LlmProviderConfig;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Provider type {@code ollama}: a local Ollama server through its native API.
 *
 * <ul>
 *   <li>{@code POST {baseUrl}/api/chat} with {@code tools}, {@code stream: false} or NDJSON streaming;
 *       {@code options.num_predict} carries the output cap and {@code options.temperature} the
 *       configured temperature.
 *   <li>{@code POST {baseUrl}/api/embed} with a batch {@code input} (and {@code dimensions} when an
 *       explicit {@link EmbeddingModelSpec} is given).
 *   <li>{@code GET {baseUrl}/api/tags}.
 * </ul>
 *
 * <p>When the model rejects tools ({@code "does not support tools"}) the call fails with
 * {@link LlmProviderException.Kind#TOOLS_UNSUPPORTED}; the service records {@code supportsTools=false}
 * so Ask runs retrieval-only (028). Ollama assigns no tool-call ids, so they are synthesized
 * ({@code call_<n>}) and tool results are matched by {@code tool_name}.
 */
public class OllamaLlmClient implements LlmClient {

    private final LlmProviderConfig config;
    private final LlmHttp http;
    private final JsonMapper json;

    /** Creates the client. */
    public OllamaLlmClient(LlmProviderConfig config, LlmHttp http, JsonMapper json) {
        this.config = Objects.requireNonNull(config, "config");
        this.http = Objects.requireNonNull(http, "http");
        this.json = Objects.requireNonNull(json, "json");
    }

    @Override
    public String providerId() {
        return config.id();
    }

    private Map<String, String> headers() {
        Map<String, String> headers = new LinkedHashMap<>(config.extraHeaders());
        if (config.apiKey() != null) {
            headers.put("Authorization", "Bearer " + config.apiKey());
        }
        return headers;
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        if (config.chatModel() == null) {
            throw new LlmProviderException(LlmProviderException.Kind.INVALID_REQUEST, config.id(),
                    "no chatModel is configured");
        }
        HttpRequest httpRequest = HttpJson.request(http.uri(config.id(), config.baseUrl(), "/api/chat"),
                        HttpJson.REQUEST_TIMEOUT, headers())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body(request))))
                .build();
        if (request.stream()) {
            return streamed(httpRequest, request.onDelta(), !request.tools().isEmpty());
        }
        HttpResponse<String> response = http.send(config.id(), httpRequest);
        if (response.statusCode() / 100 != 2) {
            throw failure(response.statusCode(), response.body(), !request.tools().isEmpty());
        }
        JsonNode node = HttpJson.parse(json, config.id(), response.body());
        JsonNode message = node.get("message");
        if (message == null || !message.isObject()) {
            throw new LlmProviderException(LlmProviderException.Kind.INVALID_RESPONSE, config.id(),
                    "answered without a message");
        }
        return response(message, node);
    }

    /** Builds the request body. Visible for tests. */
    ObjectNode body(ChatRequest request) {
        ObjectNode body = json.createObjectNode();
        body.put("model", config.chatModel());
        ArrayNode messages = body.putArray("messages");
        if (!request.system().isEmpty()) {
            messages.addObject().put("role", "system").put("content", request.system());
        }
        for (ChatMessage message : request.messages()) {
            switch (message) {
                case ChatMessage.User user -> messages.addObject().put("role", "user").put("content", user.text());
                case ChatMessage.Assistant assistant -> messages.add(assistant.rawTurn() instanceof ObjectNode raw
                        ? raw
                        : rebuiltAssistant(assistant));
                case ChatMessage.ToolResults results -> {
                    for (ToolResult result : results.results()) {
                        ObjectNode tool = messages.addObject().put("role", "tool")
                                .put("content", result.isError() ? "ERROR: " + result.content() : result.content());
                        if (result.toolName() != null) {
                            tool.put("tool_name", result.toolName());
                        }
                    }
                }
            }
        }
        if (!request.tools().isEmpty()) {
            ArrayNode tools = body.putArray("tools");
            for (ToolSpec tool : request.tools()) {
                ObjectNode function = tools.addObject().put("type", "function").putObject("function");
                function.put("name", tool.name());
                function.put("description", tool.description());
                function.set("parameters", tool.inputSchema());
            }
        }
        body.put("stream", request.stream());
        ObjectNode options = body.putObject("options");
        options.put("num_predict", request.maxTokens() != null ? request.maxTokens() : config.effectiveMaxTokens());
        if (config.temperature() != null) {
            options.put("temperature", config.temperature());
        }
        return body;
    }

    private ObjectNode rebuiltAssistant(ChatMessage.Assistant assistant) {
        ObjectNode message = json.createObjectNode().put("role", "assistant").put("content", assistant.text());
        if (!assistant.toolCalls().isEmpty()) {
            ArrayNode calls = message.putArray("tool_calls");
            for (ToolCall call : assistant.toolCalls()) {
                ObjectNode function = calls.addObject().putObject("function");
                function.put("name", call.name());
                function.set("arguments", call.input());
            }
        }
        return message;
    }

    private ChatResponse response(JsonNode message, JsonNode envelope) {
        List<ToolCall> calls = new ArrayList<>();
        JsonNode toolCalls = message.get("tool_calls");
        if (toolCalls != null && toolCalls.isArray()) {
            for (JsonNode call : toolCalls) {
                JsonNode function = call.get("function");
                if (function == null || function.get("name") == null) {
                    continue;
                }
                JsonNode arguments = function.get("arguments");
                JsonNode input = arguments != null && arguments.isObject() ? arguments
                        : arguments != null && arguments.isString()
                                ? HttpJson.parse(json, config.id(), arguments.asString())
                                : json.createObjectNode();
                String id = call.get("id") != null && call.get("id").isString() ? call.get("id").asString()
                        : "call_" + calls.size();
                calls.add(new ToolCall(id, function.get("name").asString(), input));
            }
        }
        String doneReason = envelope.get("done_reason") == null || envelope.get("done_reason").isNull() ? null
                : envelope.get("done_reason").asString();
        StopReason stop = !calls.isEmpty() ? StopReason.TOOL_USE
                : "length".equals(doneReason) ? StopReason.MAX_TOKENS : StopReason.END;
        JsonNode content = message.get("content");
        TokenUsage usage = new TokenUsage(HttpJson.longOf(envelope, "prompt_eval_count"),
                HttpJson.longOf(envelope, "eval_count"), 0);
        String model = envelope.get("model") != null && envelope.get("model").isString()
                ? envelope.get("model").asString() : config.chatModel();
        return new ChatResponse(content == null || content.isNull() ? "" : content.asString(), calls, stop, null,
                usage, model, message);
    }

    private ChatResponse streamed(HttpRequest request, Consumer<String> onDelta, boolean withTools) {
        HttpResponse<Stream<String>> response = http.sendLines(config.id(), request);
        if (response.statusCode() / 100 != 2) {
            String body;
            try (Stream<String> lines = response.body()) {
                body = String.join("\n", lines.toList());
            }
            throw failure(response.statusCode(), body, withTools);
        }
        StringBuilder text = new StringBuilder();
        ArrayNode toolCalls = json.createArrayNode();
        JsonNode last = json.createObjectNode();
        try (Stream<String> lines = response.body()) {
            for (String line : (Iterable<String>) lines::iterator) {
                if (line.isBlank()) {
                    continue;
                }
                JsonNode chunk = HttpJson.parse(json, config.id(), line);
                if (chunk.get("error") != null) {
                    throw failure(500, line, withTools);
                }
                JsonNode message = chunk.get("message");
                if (message != null) {
                    JsonNode content = message.get("content");
                    if (content != null && content.isString() && !content.asString().isEmpty()) {
                        text.append(content.asString());
                        if (onDelta != null) {
                            onDelta.accept(content.asString());
                        }
                    }
                    JsonNode calls = message.get("tool_calls");
                    if (calls != null && calls.isArray()) {
                        calls.forEach(toolCalls::add);
                    }
                }
                last = chunk;
                if (chunk.get("done") != null && chunk.get("done").asBoolean()) {
                    break;
                }
            }
        }
        ObjectNode message = json.createObjectNode().put("role", "assistant").put("content", text.toString());
        if (!toolCalls.isEmpty()) {
            message.set("tool_calls", toolCalls);
        }
        return response(message, last);
    }

    private LlmProviderException failure(int status, String body, boolean withTools) {
        String message = HttpJson.errorMessage(json, body);
        if (withTools && message != null && message.toLowerCase(Locale.ROOT).contains("does not support tools")) {
            return new LlmProviderException(LlmProviderException.Kind.TOOLS_UNSUPPORTED, config.id(),
                    "model " + config.chatModel() + " does not support tools");
        }
        return HttpJson.failure(config.id(), status, body, null, json);
    }

    @Override
    public EmbeddingResponse embed(List<String> texts) {
        if (config.embeddingModel() == null) {
            throw new LlmProviderException(LlmProviderException.Kind.INVALID_REQUEST, config.id(),
                    "no embeddingModel is configured");
        }
        return embed(texts, config.embeddingModel(), null, config.embeddingDimensions());
    }

    @Override
    public EmbeddingResponse embed(List<String> texts, EmbeddingModelSpec spec) {
        if (!config.id().equals(spec.providerId())) {
            throw new IllegalArgumentException("The spec names provider '" + spec.providerId() + "', not '"
                    + config.id() + "'");
        }
        return embed(texts, spec.model(), spec.dimensions(), spec.dimensions());
    }

    private EmbeddingResponse embed(List<String> texts, String model, Integer sendDimensions, Integer expected) {
        ObjectNode body = json.createObjectNode().put("model", model);
        ArrayNode input = body.putArray("input");
        texts.forEach(input::add);
        if (sendDimensions != null) {
            body.put("dimensions", sendDimensions);
        }
        HttpRequest request = HttpJson.request(http.uri(config.id(), config.baseUrl(), "/api/embed"),
                        HttpJson.REQUEST_TIMEOUT, headers())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)))
                .build();
        JsonNode response = HttpJson.call(http, json, config.id(), request);
        JsonNode embeddings = response.get("embeddings");
        if (embeddings == null || !embeddings.isArray() || embeddings.size() != texts.size()) {
            throw new LlmProviderException(LlmProviderException.Kind.INVALID_RESPONSE, config.id(),
                    "returned " + (embeddings == null ? 0 : embeddings.size()) + " embeddings for " + texts.size()
                            + " texts");
        }
        List<float[]> vectors = new ArrayList<>(embeddings.size());
        for (JsonNode vector : embeddings) {
            vectors.add(HttpJson.vector(vector));
        }
        int width = vectors.isEmpty() ? (expected == null ? 0 : expected) : vectors.getFirst().length;
        if (expected != null && width != expected) {
            throw new LlmProviderException(LlmProviderException.Kind.INVALID_RESPONSE, config.id(),
                    "returned " + width + "-dimensional embeddings; " + expected + " are configured");
        }
        String answeredModel = response.get("model") != null && response.get("model").isString()
                ? response.get("model").asString() : model;
        return new EmbeddingResponse(vectors, answeredModel, width,
                new TokenUsage(HttpJson.longOf(response, "prompt_eval_count"), 0, 0));
    }

    @Override
    public List<LlmModelInfo> models() {
        HttpRequest request = HttpJson.request(http.uri(config.id(), config.baseUrl(), "/api/tags"),
                HttpJson.LIST_TIMEOUT, headers()).GET().build();
        JsonNode response = HttpJson.call(http, json, config.id(), request);
        JsonNode models = response.get("models");
        List<LlmModelInfo> out = new ArrayList<>();
        if (models != null && models.isArray()) {
            for (JsonNode model : models) {
                JsonNode name = model.get("model") != null ? model.get("model") : model.get("name");
                if (name == null || !name.isString()) {
                    continue;
                }
                String family = model.path("details").path("family").asString("");
                boolean embedding = family.toLowerCase(Locale.ROOT).contains("bert")
                        || name.asString().toLowerCase(Locale.ROOT).contains("embed");
                out.add(new LlmModelInfo(name.asString(),
                        model.get("name") != null && model.get("name").isString() ? model.get("name").asString() : null,
                        null, null, embedding ? Boolean.TRUE : null));
            }
        }
        return out;
    }
}
