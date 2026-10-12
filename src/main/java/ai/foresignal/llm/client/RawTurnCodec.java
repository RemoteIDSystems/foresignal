package ai.foresignal.llm.client;

import com.anthropic.core.ObjectMappers;
import com.anthropic.models.beta.messages.BetaMessageParam;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Stores conversation turns as JSON and reads them back unchanged (prompt 028: Ask threads persist
 * the provider's raw turns so a follow-up continues the conversation append-only).
 *
 * <p>An assistant turn's {@link ChatMessage.Assistant#rawTurn() raw turn} is opaque and provider
 * specific: the Anthropic SDK's {@link BetaMessageParam} (thinking blocks and their signatures
 * included), a JSON object for the OpenAI-compatible and Ollama clients, or any string. It is kept as
 * <strong>its exact JSON text inside a JSON string</strong>, so storing it in a {@code jsonb} column,
 * which normalizes objects, cannot reorder or reformat it: the decoded turn serializes to the same
 * bytes the provider sent. A raw turn of any other type is dropped and the turn is replayed from its
 * text and tool calls.
 *
 * <p>Encoded shapes:
 * <ul>
 *   <li>{@code {"kind":"user","text":…}}
 *   <li>{@code {"kind":"assistant","provider":…,"format":"anthropic|json|string|none","raw":"…","text":…,
 *       "toolCalls":[{"id","name","input"}]}}
 *   <li>{@code {"kind":"tool_results","results":[{"toolCallId","toolName","content","isError"}]}}
 * </ul>
 */
public final class RawTurnCodec {

    /** {@code format} of an Anthropic SDK message param. */
    public static final String ANTHROPIC = "anthropic";

    /** {@code format} of a JSON object raw turn. */
    public static final String JSON_FORMAT = "json";

    /** {@code format} of a string raw turn. */
    public static final String STRING = "string";

    /** {@code format} when no raw turn is kept. */
    public static final String NONE = "none";

    private final JsonMapper json;

    /** Creates the codec over Foresignal's JSON mapper. */
    public RawTurnCodec(JsonMapper json) {
        this.json = Objects.requireNonNull(json, "json");
    }

    /**
     * Encodes one turn.
     *
     * @param message the turn
     * @param providerId the provider that produced an assistant turn (ignored for other turns), or null
     */
    public ObjectNode encode(ChatMessage message, String providerId) {
        ObjectNode out = json.createObjectNode();
        switch (message) {
            case ChatMessage.User user -> {
                out.put("kind", "user");
                out.put("text", user.text());
            }
            case ChatMessage.Assistant assistant -> {
                out.put("kind", "assistant");
                out.put("provider", providerId);
                encodeRaw(assistant.rawTurn(), out);
                out.put("text", assistant.text());
                ArrayNode calls = out.putArray("toolCalls");
                for (ToolCall call : assistant.toolCalls()) {
                    ObjectNode c = calls.addObject();
                    c.put("id", call.id());
                    c.put("name", call.name());
                    c.set("input", call.input());
                }
            }
            case ChatMessage.ToolResults results -> {
                out.put("kind", "tool_results");
                ArrayNode list = out.putArray("results");
                for (ToolResult result : results.results()) {
                    ObjectNode r = list.addObject();
                    r.put("toolCallId", result.toolCallId());
                    r.put("toolName", result.toolName());
                    r.put("content", result.content());
                    r.put("isError", result.isError());
                }
            }
        }
        return out;
    }

    /** Encodes {@code messages} in order. */
    public ArrayNode encodeAll(List<ChatMessage> messages, String providerId) {
        ArrayNode out = json.createArrayNode();
        for (ChatMessage message : messages) {
            out.add(encode(message, providerId));
        }
        return out;
    }

    /**
     * Decodes one turn.
     *
     * @param node an encoded turn
     * @param currentProviderId the provider the conversation continues with; an assistant turn from
     *     another provider is replayed without its raw turn (from text and tool calls), since its raw
     *     form means nothing to the new provider. Null keeps every raw turn.
     * @throws IllegalArgumentException when the node is not an encoded turn
     */
    public ChatMessage decode(JsonNode node, String currentProviderId) {
        String kind = node.path("kind").asString("");
        return switch (kind) {
            case "user" -> new ChatMessage.User(node.path("text").asString(""));
            case "assistant" -> {
                List<ToolCall> calls = new ArrayList<>();
                for (JsonNode c : node.path("toolCalls")) {
                    calls.add(new ToolCall(c.path("id").asString(""), c.path("name").asString(""),
                            c.hasNonNull("input") ? c.get("input") : json.createObjectNode()));
                }
                String provider = node.hasNonNull("provider") ? node.get("provider").asString() : null;
                boolean sameProvider = currentProviderId == null || currentProviderId.equals(provider);
                Object raw = sameProvider ? decodeRaw(node) : null;
                yield new ChatMessage.Assistant(raw, node.path("text").asString(""), calls);
            }
            case "tool_results" -> {
                List<ToolResult> results = new ArrayList<>();
                for (JsonNode r : node.path("results")) {
                    results.add(new ToolResult(r.path("toolCallId").asString(""),
                            r.hasNonNull("toolName") ? r.get("toolName").asString() : null,
                            r.path("content").asString(""), r.path("isError").asBoolean(false)));
                }
                yield new ChatMessage.ToolResults(results);
            }
            default -> throw new IllegalArgumentException("not an encoded conversation turn: kind '" + kind + "'");
        };
    }

    /** Decodes every turn of {@code nodes}, in order. */
    public List<ChatMessage> decodeAll(JsonNode nodes, String currentProviderId) {
        List<ChatMessage> out = new ArrayList<>();
        if (nodes != null && nodes.isArray()) {
            for (JsonNode node : nodes) {
                out.add(decode(node, currentProviderId));
            }
        }
        return out;
    }

    /** The exact JSON text of a raw turn, or null when it is not one this codec keeps. */
    public String rawText(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof BetaMessageParam param) {
            try {
                return ObjectMappers.jsonMapper().writeValueAsString(param);
            } catch (Exception e) {
                throw new IllegalStateException("Could not serialize an Anthropic message param", e);
            }
        }
        if (raw instanceof JsonNode node) {
            return node.toString();
        }
        if (raw instanceof String text) {
            return text;
        }
        return null;
    }

    private void encodeRaw(Object raw, ObjectNode out) {
        String format;
        if (raw instanceof BetaMessageParam) {
            format = ANTHROPIC;
        } else if (raw instanceof JsonNode) {
            format = JSON_FORMAT;
        } else if (raw instanceof String) {
            format = STRING;
        } else {
            format = NONE;
        }
        String text = format.equals(NONE) ? null : rawText(raw);
        out.put("format", format);
        if (text == null) {
            out.putNull("raw");
        } else {
            out.put("raw", text);
        }
    }

    private Object decodeRaw(JsonNode node) {
        String format = node.path("format").asString(NONE);
        JsonNode raw = node.get("raw");
        if (raw == null || raw.isNull()) {
            return null;
        }
        String text = raw.asString();
        return switch (format) {
            case ANTHROPIC -> {
                try {
                    yield ObjectMappers.jsonMapper().readValue(text, BetaMessageParam.class);
                } catch (Exception e) {
                    throw new IllegalStateException("Could not read a stored Anthropic message param", e);
                }
            }
            case JSON_FORMAT -> json.readTree(text);
            case STRING -> text;
            default -> null;
        };
    }
}
