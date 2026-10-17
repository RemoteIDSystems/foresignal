package ai.foresignal.quality;

import ai.foresignal.llm.LlmService;
import ai.foresignal.llm.client.ChatMessage;
import ai.foresignal.llm.client.ChatRequest;
import ai.foresignal.llm.client.ChatResponse;
import ai.foresignal.llm.client.Effort;
import ai.foresignal.llm.client.LlmClient;
import ai.foresignal.llm.client.Purpose;
import ai.foresignal.llm.client.StopReason;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * The optional plain-language summary of a tuning suggestion ({@code explain=true}, prompt 115) through the 026
 * chat role. It is budgeted like every chat call, sees only the suggestion's kind, rationale, patch and expected
 * effect (no record content), never generates or edits a patch, and is skipped when no chat provider is
 * configured or the call fails.
 */
@Component
public class SuggestionExplainer {

    private static final Logger log = LoggerFactory.getLogger(SuggestionExplainer.class);

    /** The output budget of one explanation. */
    static final int MAX_TOKENS = 200;

    /** The system prompt, from the versioned template {@code tuning-explainer-system@1.0.0} (prompt 116). */
    static final String SYSTEM = ai.foresignal.llm.PromptTemplates.text(
            ai.foresignal.llm.PromptTemplates.TUNING_EXPLAINER, ai.foresignal.llm.PromptTemplates.V1);

    private final LlmService llm;
    private final JsonMapper json;

    /** Creates the explainer. */
    public SuggestionExplainer(LlmService llm, JsonMapper json) {
        this.llm = llm;
        this.json = json;
    }

    /**
     * A short explanation of {@code suggestion}, or empty without a chat provider or on any failure (including a
     * budget refusal).
     *
     * @param suggestion the {@code TuningSuggestionRecord} object
     */
    public Optional<String> explain(Map<String, Object> suggestion) {
        Optional<LlmClient> client;
        try {
            client = llm.chatClient();
        } catch (RuntimeException e) {
            return Optional.empty();
        }
        if (client.isEmpty()) {
            return Optional.empty();
        }
        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("kind", suggestion.get("kind"));
        data.put("rationale", suggestion.get("rationale"));
        data.put("patch", suggestion.get("patch"));
        data.put("expectedEffect", suggestion.get("expectedEffect"));
        try {
            ChatResponse response = client.get().chat(new ChatRequest(SYSTEM,
                    List.of(new ChatMessage.User("Suggestion (data):\n" + json.writeValueAsString(data))), List.of(),
                    MAX_TOKENS, Effort.LOW, Purpose.ANALYST, false, null));
            if (response.stopReason() == StopReason.REFUSAL || response.content() == null
                    || response.content().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(response.content().strip());
        } catch (RuntimeException e) {
            log.debug("No explanation for suggestion {}: {}", suggestion.get("id"), e.toString());
            return Optional.empty();
        }
    }
}
