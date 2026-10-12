package ai.foresignal.llm;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import tools.jackson.databind.JsonNode;

/**
 * LLM roles and options in {@code app_setting['llm']} (OpenAPI {@code LlmSettings}, Architecture §19).
 *
 * @param chatProvider the provider for chat (Ask, briefings, enrichment), or null
 * @param embeddingProvider the provider for embeddings (search), or null; never {@code anthropic}
 * @param askEnabled whether Ask is offered (028)
 * @param enrichmentEnabled whether alert enrichment runs (030)
 * @param logContent whether prompt and response bodies are logged (at DEBUG); metadata is always logged
 * @param askMaxTokens the token cap of one Ask tool loop (028), at least 1000
 * @param allowPrivateLocal reserved for prompt 039 (private-source records in prompts for local
 *     providers); 026 accepts only false
 */
public record LlmSettings(String chatProvider, String embeddingProvider, boolean askEnabled,
        boolean enrichmentEnabled, boolean logContent, int askMaxTokens, boolean allowPrivateLocal) {

    /** The settings of an installation that never configured LLM. */
    public static final LlmSettings DEFAULTS = new LlmSettings(null, null, true, true, false, 200_000, false);

    /** The smallest {@code askMaxTokens}. */
    public static final int MIN_ASK_MAX_TOKENS = 1000;

    private static final Set<String> MEMBERS = Set.of("chatProvider", "embeddingProvider", "askEnabled",
            "enrichmentEnabled", "logContent", "askMaxTokens", "allowPrivateLocal");

    /** True when a chat provider is assigned. */
    public boolean configured() {
        return chatProvider != null || embeddingProvider != null;
    }

    /**
     * Parses a request body. Omitted members take their defaults (the body replaces the setting).
     *
     * @throws ValidationException listing every member error
     */
    public static LlmSettings parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be an LlmSettings object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String name : body.propertyNames()) {
            if (!MEMBERS.contains(name)) {
                errors.add(new FieldError(name, "is not a member of LlmSettings"));
            }
        }
        String chat = optionalString(body, "chatProvider", errors);
        String embedding = optionalString(body, "embeddingProvider", errors);
        boolean ask = bool(body, "askEnabled", DEFAULTS.askEnabled(), errors);
        boolean enrichment = bool(body, "enrichmentEnabled", DEFAULTS.enrichmentEnabled(), errors);
        boolean logContent = bool(body, "logContent", DEFAULTS.logContent(), errors);
        boolean allowPrivateLocal = bool(body, "allowPrivateLocal", false, errors);
        int askMaxTokens = DEFAULTS.askMaxTokens();
        JsonNode max = body.get("askMaxTokens");
        if (max != null && !max.isNull()) {
            if (!max.canConvertToInt() || !max.isIntegralNumber()) {
                errors.add(new FieldError("askMaxTokens", "must be an integer"));
            } else if (max.asInt() < MIN_ASK_MAX_TOKENS) {
                errors.add(new FieldError("askMaxTokens", "must be at least " + MIN_ASK_MAX_TOKENS));
            } else {
                askMaxTokens = max.asInt();
            }
        }
        if (allowPrivateLocal) {
            errors.add(new FieldError("allowPrivateLocal",
                    "arrives with prompt 039 (private sources); until then only false is accepted"));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The LLM settings are not valid.", errors);
        }
        return new LlmSettings(chat, embedding, ask, enrichment, logContent, askMaxTokens, false);
    }

    private static String optionalString(JsonNode body, String name, List<FieldError> errors) {
        JsonNode node = body.get(name);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isString() || node.asString().isBlank()) {
            errors.add(new FieldError(name, "must be a provider id or null"));
            return null;
        }
        return node.asString();
    }

    private static boolean bool(JsonNode body, String name, boolean def, List<FieldError> errors) {
        JsonNode node = body.get(name);
        if (node == null || node.isNull()) {
            return def;
        }
        if (!node.isBoolean()) {
            errors.add(new FieldError(name, "must be a boolean"));
            return def;
        }
        return node.asBoolean();
    }
}
