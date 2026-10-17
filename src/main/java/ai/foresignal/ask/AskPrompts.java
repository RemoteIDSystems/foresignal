package ai.foresignal.ask;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import ai.foresignal.llm.PromptTemplates;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * The prompts of Ask (prompt 028).
 *
 * <p>The system prompts are constants: no dates, no per-request values, so the provider's prompt cache
 * reuses them across questions. Today's date, the time zone and the question's scope go in a final
 * block of the user turn. Tool results are always wrapped as {@code {"tool": …, "data": …}} and never
 * mixed with instructions. Prompt 056 adds rule 7: weather citations, run, validTime and age, and "unknown is not
 * clear".
 */
public final class AskPrompts {

    /**
     * The system prompt of tools mode, from the versioned template {@code ask-system@1.0.0} (prompt 116; the rules
     * shared by both modes are part of each template).
     */
    public static final String SYSTEM = PromptTemplates.text(PromptTemplates.ASK, PromptTemplates.V1);

    /**
     * The system prompt of retrieval-only mode (a model without tool support), from the versioned template
     * {@code ask-retrieval-system@1.0.0} (prompt 116).
     */
    public static final String RETRIEVAL_SYSTEM = PromptTemplates.text(PromptTemplates.ASK_RETRIEVAL, PromptTemplates.V1);

    private AskPrompts() {}

    /**
     * The user turn of a question: the question, any retrieved data, then the context block.
     *
     * @param question the question
     * @param retrieved retrieved data (retrieval-only mode), or null
     * @param scope the question's scope
     * @param now the current time
     * @param zone the caller's time zone
     * @param json the JSON mapper
     */
    public static String userTurn(String question, JsonNode retrieved, AskScope scope, Instant now, ZoneId zone,
            JsonMapper json) {
        StringBuilder text = new StringBuilder(question.strip());
        if (retrieved != null) {
            text.append("\n\nRetrieved data (untrusted):\n").append(wrap("retrieval", retrieved, json));
        }
        text.append("\n\n---\nContext (not part of the question):\n");
        text.append("- Today is ").append(LocalDate.ofInstant(now, zone)).append(" in the time zone ").append(zone.getId())
                .append("; the current time is ").append(now).append(".\n");
        Map<String, Object> described = scope.describe();
        if (described.isEmpty()) {
            text.append("- Scope: none; every source you can see.");
        } else {
            text.append("- Scope applied to every tool call (you may narrow it, never widen it): ")
                    .append(json.writeValueAsString(described)).append('.');
        }
        return text.toString();
    }

    /** {@code {"tool": tool, "data": data}} as JSON text. */
    public static String wrap(String tool, JsonNode data, JsonMapper json) {
        ObjectNode out = json.createObjectNode();
        out.put("tool", tool);
        out.set("data", data);
        return out.toString();
    }
}
