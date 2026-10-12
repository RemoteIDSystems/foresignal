package ai.foresignal.llm.client;

import java.util.List;

/**
 * A chat and embedding model behind one provider configuration (Architecture §19).
 *
 * <p>Implementations translate to one provider API and normalize its answer. They throw
 * {@link LlmProviderException} for provider failures, classified so callers can retry, back off or
 * report.
 */
public interface LlmClient {

    /** The provider id this client talks to. */
    String providerId();

    /**
     * One chat turn.
     *
     * @throws LlmProviderException on a provider failure
     */
    ChatResponse chat(ChatRequest request);

    /**
     * Embeds {@code texts} with the provider's configured embedding model.
     *
     * @throws UnsupportedOperationException when the provider has no embeddings endpoint (Anthropic)
     * @throws LlmProviderException on a provider failure
     */
    EmbeddingResponse embed(List<String> texts);

    /**
     * Embeds {@code texts} with an explicit model and width.
     *
     * @throws UnsupportedOperationException when the provider has no embeddings endpoint (Anthropic)
     * @throws IllegalArgumentException when {@code spec} names another provider
     * @throws LlmProviderException on a provider failure
     */
    EmbeddingResponse embed(List<String> texts, EmbeddingModelSpec spec);

    /**
     * The provider's models.
     *
     * @throws LlmProviderException on a provider failure
     */
    List<LlmModelInfo> models();
}
