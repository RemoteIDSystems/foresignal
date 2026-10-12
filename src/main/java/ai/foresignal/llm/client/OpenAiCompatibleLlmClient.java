package ai.foresignal.llm.client;

import ai.foresignal.llm.LlmHttp;
import ai.foresignal.llm.LlmProviderConfig;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Provider type {@code openai-compatible}: any API that speaks OpenAI's chat completions, embeddings
 * and models endpoints (OpenAI, OpenRouter, Groq, Mistral, vLLM, LM Studio, llama.cpp server, LocalAI).
 *
 * <ul>
 *   <li>{@code POST {baseUrl}/chat/completions} with {@code tools} (function calling),
 *       {@code max_tokens} and {@code temperature} when configured; streamed as server-sent events
 *       when the request asks for it.
 *   <li>{@code POST {baseUrl}/embeddings}, with {@code dimensions} when an explicit
 *       {@link EmbeddingModelSpec} is given.
 *   <li>{@code GET {baseUrl}/models}.
 * </ul>
 *
 * <p>The API key is sent as a bearer token; {@code options.extraHeaders} are added to every request.
 * Finish reasons are normalized ({@code stop} → end, {@code tool_calls} → tool use, {@code length}
 * → max tokens, {@code content_filter} → refusal). The assistant {@code message} object is returned
 * as {@link ChatResponse#rawAssistantTurn()} and replayed unchanged.
 */
public class OpenAiCompatibleLlmClient implements LlmClient {

    private final LlmProviderConfig config;
    private final LlmHttp http;
    private final JsonMapper json;

    /** Creates the client. */
    public OpenAiCompatibleLlmClient(LlmProviderConfig config, LlmHttp http, JsonMapper json) {
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
        ObjectNode body = body(request);
        HttpRequest httpRequest = HttpJson.request(http.uri(config.id(), config.baseUrl(), "/chat/completions"),
                        HttpJson.REQUEST_TIMEOUT, headers())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)))
                .build();
        if (request.stream()) {
            return streamed(httpRequest, request.onDelta());
        }
        JsonNode response = HttpJson.call(http, json, config.id(), httpRequest);
        JsonNode choice = firstChoice(response);
        JsonNode message = choice.get("message");
        if (message == null || !message.isObject()) {
            throw new LlmProviderException(LlmProviderException.Kind.INVALID_RESPONSE, config.id(),
                    "answered without a message");
        }
        return response(message, finishReason(choice), response.get("usage"), modelOf(response));
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
                        messages.addObject()
                                .put("role", "tool")
                                .put("tool_call_id", result.toolCallId())
                                .put("content", result.isError() ? "ERROR: " + result.content() : result.content());
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
        body.put("max_tokens", request.maxTokens() != null ? request.maxTokens() : config.effectiveMaxTokens());
        if (config.temperature() != null) {
            body.put("temperature", config.temperature());
        }
        if (request.stream()) {
            body.put("stream", true);
            body.putObject("stream_options").put("include_usage", true);
        }
        return body;
    }

    private ObjectNode rebuiltAssistant(ChatMessage.Assistant assistant) {
        ObjectNode message = json.createObjectNode().put("role", "assistant");
        message.put("content", assistant.text());
        if (!assistant.toolCalls().isEmpty()) {
            ArrayNode calls = message.putArray("tool_calls");
            for (ToolCall call : assistant.toolCalls()) {
                ObjectNode entry = calls.addObject().put("id", call.id()).put("type", "function");
                entry.putObject("function").put("name", call.name()).put("arguments", call.input().toString());
            }
        }
        return message;
    }

    private ChatResponse response(JsonNode message, String finishReason, JsonNode usage, String model) {
        StopReason stop = stopReason(finishReason);
        List<ToolCall> calls = new ArrayList<>();
        JsonNode toolCalls = message.get("tool_calls");
        if (toolCalls != null && toolCalls.isArray()) {
            for (JsonNode call : toolCalls) {
                JsonNode function = call.get("function");
                String name = function == null || function.get("name") == null ? "" : function.get("name").asString();
                String id = call.get("id") == null || call.get("id").isNull() ? "call_" + calls.size()
                        : call.get("id").asString();
                calls.add(new ToolCall(id, name, arguments(function)));
            }
        }
        if (!calls.isEmpty() && stop == StopReason.END) {
            stop = StopReason.TOOL_USE;
        }
        JsonNode content = message.get("content");
        String text = content == null || content.isNull() ? "" : content.asString();
        long cached = usage == null ? 0 : HttpJson.longOf(usage.get("prompt_tokens_details"), "cached_tokens");
        TokenUsage tokens = new TokenUsage(HttpJson.longOf(usage, "prompt_tokens"),
                HttpJson.longOf(usage, "completion_tokens"), cached);
        return new ChatResponse(text, calls, stop, stop == StopReason.REFUSAL ? "content_filter" : null, tokens,
                model, message);
    }

    private JsonNode arguments(JsonNode function) {
        JsonNode arguments = function == null ? null : function.get("arguments");
        if (arguments == null || arguments.isNull()) {
            return json.createObjectNode();
        }
        if (arguments.isObject()) {
            return arguments;
        }
        try {
            JsonNode parsed = json.readTree(arguments.asString());
            return parsed.isObject() ? parsed : json.createObjectNode().set("value", parsed);
        } catch (JacksonException e) {
            // Invalid JSON arguments: pass them through so the tool can report a clear error.
            return json.createObjectNode().put("_invalidArguments", arguments.asString());
        }
    }

    private ChatResponse streamed(HttpRequest request, Consumer<String> onDelta) {
        HttpResponse<Stream<String>> response = http.sendLines(config.id(), request);
        if (response.statusCode() / 100 != 2) {
            String body;
            try (Stream<String> lines = response.body()) {
                body = String.join("\n", lines.toList());
            }
            throw HttpJson.failure(config.id(), response.statusCode(), body,
                    response.headers().firstValue("retry-after").orElse(null), json);
        }
        StringBuilder text = new StringBuilder();
        Map<Integer, ObjectNode> calls = new LinkedHashMap<>();
        String finish = null;
        String model = config.chatModel();
        JsonNode usage = null;
        try (Stream<String> lines = response.body()) {
            for (String line : (Iterable<String>) lines::iterator) {
                if (!line.startsWith("data:")) {
                    continue;
                }
                String data = line.substring(5).trim();
                if (data.equals("[DONE]")) {
                    break;
                }
                JsonNode chunk = HttpJson.parse(json, config.id(), data);
                if (chunk.get("model") != null && chunk.get("model").isString()) {
                    model = chunk.get("model").asString();
                }
                if (chunk.get("usage") != null && chunk.get("usage").isObject()) {
                    usage = chunk.get("usage");
                }
                JsonNode choices = chunk.get("choices");
                if (choices == null || !choices.isArray() || choices.isEmpty()) {
                    continue;
                }
                JsonNode choice = choices.get(0);
                if (choice.get("finish_reason") != null && !choice.get("finish_reason").isNull()) {
                    finish = choice.get("finish_reason").asString();
                }
                JsonNode delta = choice.get("delta");
                if (delta == null) {
                    continue;
                }
                JsonNode content = delta.get("content");
                if (content != null && content.isString() && !content.asString().isEmpty()) {
                    text.append(content.asString());
                    if (onDelta != null) {
                        onDelta.accept(content.asString());
                    }
                }
                JsonNode deltaCalls = delta.get("tool_calls");
                if (deltaCalls != null && deltaCalls.isArray()) {
                    for (JsonNode part : deltaCalls) {
                        int index = part.get("index") == null ? 0 : part.get("index").asInt();
                        ObjectNode call = calls.computeIfAbsent(index, i -> {
                            ObjectNode c = json.createObjectNode().put("type", "function");
                            c.putObject("function").put("name", "").put("arguments", "");
                            return c;
                        });
                        if (part.get("id") != null && part.get("id").isString()) {
                            call.put("id", part.get("id").asString());
                        }
                        JsonNode function = part.get("function");
                        if (function != null) {
                            ObjectNode target = (ObjectNode) call.get("function");
                            if (function.get("name") != null && function.get("name").isString()) {
                                target.put("name", target.get("name").asString() + function.get("name").asString());
                            }
                            if (function.get("arguments") != null && function.get("arguments").isString()) {
                                target.put("arguments",
                                        target.get("arguments").asString() + function.get("arguments").asString());
                            }
                        }
                    }
                }
            }
        }
        ObjectNode message = json.createObjectNode().put("role", "assistant").put("content", text.toString());
        if (!calls.isEmpty()) {
            ArrayNode array = message.putArray("tool_calls");
            calls.values().forEach(array::add);
        }
        return response(message, finish, usage, model);
    }

    private JsonNode firstChoice(JsonNode response) {
        JsonNode choices = response.get("choices");
        if (choices == null || !choices.isArray() || choices.isEmpty()) {
            throw new LlmProviderException(LlmProviderException.Kind.INVALID_RESPONSE, config.id(),
                    "answered without choices");
        }
        return choices.get(0);
    }

    private static String finishReason(JsonNode choice) {
        JsonNode reason = choice.get("finish_reason");
        return reason == null || reason.isNull() ? null : reason.asString();
    }

    private String modelOf(JsonNode response) {
        JsonNode model = response.get("model");
        return model != null && model.isString() ? model.asString() : config.chatModel();
    }

    /** Normalizes an OpenAI finish reason. */
    static StopReason stopReason(String finishReason) {
        if (finishReason == null) {
            return StopReason.END;
        }
        return switch (finishReason) {
            case "stop", "end_turn" -> StopReason.END;
            case "tool_calls", "function_call" -> StopReason.TOOL_USE;
            case "length" -> StopReason.MAX_TOKENS;
            case "content_filter" -> StopReason.REFUSAL;
            default -> StopReason.ERROR;
        };
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
        HttpRequest request = HttpJson.request(http.uri(config.id(), config.baseUrl(), "/embeddings"),
                        HttpJson.REQUEST_TIMEOUT, headers())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)))
                .build();
        JsonNode response = HttpJson.call(http, json, config.id(), request);
        JsonNode data = response.get("data");
        if (data == null || !data.isArray() || data.size() != texts.size()) {
            throw new LlmProviderException(LlmProviderException.Kind.INVALID_RESPONSE, config.id(),
                    "returned " + (data == null ? 0 : data.size()) + " embeddings for " + texts.size() + " texts");
        }
        float[][] ordered = new float[texts.size()][];
        for (int i = 0; i < data.size(); i++) {
            JsonNode item = data.get(i);
            int index = item.get("index") == null ? i : item.get("index").asInt();
            if (index < 0 || index >= ordered.length || item.get("embedding") == null) {
                throw new LlmProviderException(LlmProviderException.Kind.INVALID_RESPONSE, config.id(),
                        "returned an embedding without a valid index");
            }
            ordered[index] = HttpJson.vector(item.get("embedding"));
        }
        List<float[]> vectors = List.of(ordered);
        int width = vectors.isEmpty() ? (expected == null ? 0 : expected) : vectors.getFirst().length;
        if (expected != null && width != expected) {
            throw new LlmProviderException(LlmProviderException.Kind.INVALID_RESPONSE, config.id(),
                    "returned " + width + "-dimensional embeddings; " + expected + " are configured");
        }
        JsonNode usage = response.get("usage");
        String answeredModel = response.get("model") != null && response.get("model").isString()
                ? response.get("model").asString() : model;
        return new EmbeddingResponse(vectors, answeredModel, width,
                new TokenUsage(HttpJson.longOf(usage, "prompt_tokens"), 0, 0));
    }

    @Override
    public List<LlmModelInfo> models() {
        HttpRequest request = HttpJson.request(http.uri(config.id(), config.baseUrl(), "/models"),
                HttpJson.LIST_TIMEOUT, headers()).GET().build();
        JsonNode response = HttpJson.call(http, json, config.id(), request);
        JsonNode data = response.get("data");
        List<LlmModelInfo> out = new ArrayList<>();
        if (data != null && data.isArray()) {
            for (JsonNode model : data) {
                if (model.get("id") == null) {
                    continue;
                }
                JsonNode context = model.get("context_length");
                out.add(new LlmModelInfo(model.get("id").asString(),
                        model.get("name") != null && model.get("name").isString() ? model.get("name").asString() : null,
                        context != null && context.isNumber() ? context.asLong() : null, null, null));
            }
        }
        return out;
    }
}
