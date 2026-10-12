package ai.foresignal.llm.client;

import java.util.List;
import java.util.Objects;

/**
 * One turn of a conversation. Conversations are append-only: a caller adds turns and never edits or
 * reorders earlier ones, so a provider's preserved reasoning (Anthropic thinking blocks) stays
 * valid.
 */
public sealed interface ChatMessage {

    /** A user turn. Dates and other volatile context belong here, not in the system prompt. */
    record User(String text) implements ChatMessage {

        /** Validates the text. */
        public User {
            Objects.requireNonNull(text, "text");
        }
    }

    /**
     * An assistant turn, taken from {@link ChatResponse#assistantMessage()}.
     *
     * @param rawTurn the provider's own representation of the turn ({@link ChatResponse#rawAssistantTurn()}),
     *     sent back unchanged to the provider that produced it; null for a turn built by hand
     * @param text the turn's text, used when the turn is replayed to a provider that does not
     *     understand {@code rawTurn}
     * @param toolCalls the tool calls the turn made
     */
    record Assistant(Object rawTurn, String text, List<ToolCall> toolCalls) implements ChatMessage {

        /** Normalizes the members. */
        public Assistant {
            text = text == null ? "" : text;
            toolCalls = toolCalls == null ? List.of() : List.copyOf(toolCalls);
        }
    }

    /**
     * The results of every tool call of the previous assistant turn, in one message (parallel tool
     * use). Providers that need one message per result (OpenAI-compatible, Ollama) expand it.
     */
    record ToolResults(List<ToolResult> results) implements ChatMessage {

        /** Copies the results. */
        public ToolResults {
            results = results == null ? List.of() : List.copyOf(results);
        }
    }

    /** A user turn. */
    static ChatMessage user(String text) {
        return new User(text);
    }
}
