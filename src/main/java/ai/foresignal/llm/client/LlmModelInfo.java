package ai.foresignal.llm.client;

/**
 * A model a provider offers (OpenAPI {@code LlmModelInfo}).
 *
 * @param id the model id to configure
 * @param displayName a human name, or null
 * @param contextWindow the input token limit, or null when the provider does not say
 * @param supportsTools whether tool use is known to work, or null when unknown
 * @param supportsEmbeddings whether it is an embedding model, or null when unknown
 */
public record LlmModelInfo(String id, String displayName, Long contextWindow, Boolean supportsTools,
        Boolean supportsEmbeddings) {}
