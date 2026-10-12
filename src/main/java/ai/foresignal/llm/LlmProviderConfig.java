package ai.foresignal.llm;

import ai.foresignal.llm.client.Effort;
import ai.foresignal.llm.client.EmbeddingModelSpec;
import ai.foresignal.security.EgressPolicy;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/**
 * An immutable snapshot of one provider's configuration, with its API key decrypted, as the
 * clients need it. Equal snapshots share one built client; a changed snapshot rebuilds it.
 *
 * <p>{@link #toString()} never prints the API key.
 *
 * @param id the provider id
 * @param type the implementation
 * @param baseUrl the API base URL
 * @param chatModel the chat model, or null
 * @param embeddingModel the embedding model, or null
 * @param embeddingDimensions the embedding width, or null
 * @param effort the configured effort, or null
 * @param maxTokens the configured output cap, or null
 * @param temperature the sampling temperature, or null (never sent to Anthropic, where it is removed)
 * @param extraHeaders extra request headers (for example OpenRouter's {@code HTTP-Referer})
 * @param localFlag {@code options.local}: treat a non-Ollama provider as local for LLM egress
 * @param apiKey the decrypted API key, or null
 * @param requestsPerMinute the per-minute request budget
 * @param tokensPerDay the per-UTC-day token budget
 * @param enabled whether it may be used
 * @param knowledgeCutoff the chat model's training-data cutoff, or null
 */
public record LlmProviderConfig(String id, LlmProviderType type, String baseUrl, String chatModel,
        String embeddingModel, Integer embeddingDimensions, Effort effort, Integer maxTokens, Double temperature,
        Map<String, String> extraHeaders, boolean localFlag, String apiKey, int requestsPerMinute, long tokensPerDay,
        boolean enabled, LocalDate knowledgeCutoff) {

    /** The output cap used when neither the request nor the provider sets one. */
    public static final int DEFAULT_MAX_TOKENS = 16_000;

    /** Copies the headers. */
    public LlmProviderConfig {
        extraHeaders = extraHeaders == null ? Map.of() : Map.copyOf(extraHeaders);
    }

    /** True when prompts stay on this machine or network: Ollama, or {@code options.local=true}. */
    public boolean local() {
        return type == LlmProviderType.OLLAMA || localFlag;
    }

    /** This provider as an LLM-context egress destination. */
    public EgressPolicy.LlmDestination destination() {
        return new EgressPolicy.LlmDestination(id, type.wireValue(), local(), baseUrl);
    }

    /** The configured embedding spec, when both model and dimensions are set. */
    public Optional<EmbeddingModelSpec> embeddingSpec() {
        if (embeddingModel == null || embeddingDimensions == null || !type.supportsEmbeddings()) {
            return Optional.empty();
        }
        return Optional.of(new EmbeddingModelSpec(id, embeddingModel, embeddingDimensions));
    }

    /** The output cap: the provider's, else {@value #DEFAULT_MAX_TOKENS}. */
    public int effectiveMaxTokens() {
        return maxTokens == null ? DEFAULT_MAX_TOKENS : maxTokens;
    }

    @Override
    public String toString() {
        return "LlmProviderConfig[id=" + id + ", type=" + type.wireValue() + ", baseUrl=" + baseUrl + ", chatModel="
                + chatModel + ", embeddingModel=" + embeddingModel + ", embeddingDimensions=" + embeddingDimensions
                + ", apiKeySet=" + (apiKey != null) + ", enabled=" + enabled + "]";
    }
}
