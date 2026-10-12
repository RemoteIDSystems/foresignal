package ai.foresignal.llm.client;

import java.util.List;
import java.util.Objects;

/**
 * A model's answer, normalized.
 *
 * @param content the text of the turn (text blocks concatenated)
 * @param toolCalls the tools the model asked for, in order
 * @param stopReason why the model stopped; check {@link StopReason#REFUSAL} and
 *     {@link StopReason#MAX_TOKENS} before acting on content or tool calls
 * @param refusalCategory the provider's refusal category ({@code stop_details.category}) when
 *     {@code stopReason} is {@code refusal}, else null
 * @param usage tokens used
 * @param model the model that answered (after any server-side fallback)
 * @param rawAssistantTurn the provider's own representation of the assistant turn (for Anthropic
 *     the SDK message param with every content block, thinking included). Opaque: append it
 *     unchanged to continue the conversation, through {@link #assistantMessage()}
 */
public record ChatResponse(String content, List<ToolCall> toolCalls, StopReason stopReason, String refusalCategory,
        TokenUsage usage, String model, Object rawAssistantTurn) {

    /** Normalizes the members. */
    public ChatResponse {
        content = content == null ? "" : content;
        toolCalls = toolCalls == null ? List.of() : List.copyOf(toolCalls);
        Objects.requireNonNull(stopReason, "stopReason");
        usage = usage == null ? TokenUsage.ZERO : usage;
    }

    /** The assistant turn to append to the conversation, carrying {@link #rawAssistantTurn()} unchanged. */
    public ChatMessage.Assistant assistantMessage() {
        return new ChatMessage.Assistant(rawAssistantTurn, content, toolCalls);
    }
}
