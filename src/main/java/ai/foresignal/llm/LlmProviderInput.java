package ai.foresignal.llm;

import java.time.LocalDate;
import java.util.Map;

/**
 * A validated {@code LlmProviderInput} with defaults applied.
 *
 * @param id the provider id
 * @param type the implementation
 * @param baseUrl the base URL (the type's default when omitted)
 * @param chatModel the chat model, or null
 * @param embeddingModel the embedding model, or null
 * @param embeddingDimensions the embedding width, or null
 * @param apiKey the new API key, or null to keep the stored one
 * @param options the options to store
 * @param requestsPerMinute per-minute requests
 * @param tokensPerDay tokens per UTC day
 * @param enabled whether it may be used
 * @param knowledgeCutoff the chat model's training-data cutoff, or null
 * @param pricing the prices in USD per million tokens (never null; {@link LlmPricing#NONE} when unpriced)
 * @param spendCaps the hourly, daily and monthly USD caps (never null; {@link LlmSpendCaps#NONE} when uncapped)
 */
public record LlmProviderInput(String id, LlmProviderType type, String baseUrl, String chatModel,
        String embeddingModel, Integer embeddingDimensions, String apiKey, Map<String, Object> options,
        int requestsPerMinute, long tokensPerDay, boolean enabled, LocalDate knowledgeCutoff, LlmPricing pricing,
        LlmSpendCaps spendCaps) {

    /** Copies the options; a null pricing or cap set is none. */
    public LlmProviderInput {
        options = options == null ? Map.of() : Map.copyOf(options);
        pricing = pricing == null ? LlmPricing.NONE : pricing;
        spendCaps = spendCaps == null ? LlmSpendCaps.NONE : spendCaps;
    }

    @Override
    public String toString() {
        return "LlmProviderInput[id=" + id + ", type=" + type + ", apiKeySet=" + (apiKey != null) + "]";
    }
}
