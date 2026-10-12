package ai.foresignal.llm.client;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * One chat call.
 *
 * @param system the stable system prompt: no timestamps or per-request values, so the provider's
 *     prompt cache can reuse it (dates go in the final user turn)
 * @param messages the conversation so far, oldest first; append-only
 * @param tools the tools the model may call, in a deterministic order (the order is part of the
 *     cached prefix)
 * @param maxTokens the output cap, or null for the provider's configured {@code maxTokens}
 * @param effort the effort, or null for the provider's configured effort, else the purpose default
 * @param purpose why the call is made; logged and budgeted
 * @param stream true to stream the response; streaming is also used whenever the output cap is
 *     above 16000 tokens
 * @param onDelta receives text deltas while streaming; may be null
 */
public record ChatRequest(String system, List<ChatMessage> messages, List<ToolSpec> tools, Integer maxTokens,
        Effort effort, Purpose purpose, boolean stream, Consumer<String> onDelta) {

    /** Normalizes the members. */
    public ChatRequest {
        system = system == null ? "" : system;
        messages = messages == null ? List.of() : List.copyOf(messages);
        tools = tools == null ? List.of() : List.copyOf(tools);
        Objects.requireNonNull(purpose, "purpose");
        if (maxTokens != null && maxTokens < 1) {
            throw new IllegalArgumentException("maxTokens must be positive");
        }
    }

    /** A non-streaming request without tools. */
    public static ChatRequest simple(Purpose purpose, String system, String userText, Integer maxTokens) {
        return new ChatRequest(system, List.of(new ChatMessage.User(userText)), List.of(), maxTokens, null, purpose,
                false, null);
    }

    /** A copy with {@code newMessages}. */
    public ChatRequest withMessages(List<ChatMessage> newMessages) {
        return new ChatRequest(system, newMessages, tools, maxTokens, effort, purpose, stream, onDelta);
    }

    /** A copy with {@code newMaxTokens}. */
    public ChatRequest withMaxTokens(Integer newMaxTokens) {
        return new ChatRequest(system, messages, tools, newMaxTokens, effort, purpose, stream, onDelta);
    }

    /** Characters of system prompt, messages and tool definitions: the basis of the token estimate. */
    public long promptCharacters() {
        long chars = system.length();
        for (ChatMessage message : messages) {
            chars += switch (message) {
                case ChatMessage.User user -> user.text().length();
                case ChatMessage.Assistant assistant -> assistant.text().length()
                        + assistant.toolCalls().stream().mapToLong(c -> c.input().toString().length()).sum();
                case ChatMessage.ToolResults results ->
                        results.results().stream().mapToLong(r -> r.content().length()).sum();
            };
        }
        for (ToolSpec tool : tools) {
            chars += tool.name().length() + tool.description().length() + tool.inputSchema().toString().length();
        }
        return chars;
    }
}
