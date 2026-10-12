package ai.foresignal.llm.client;

import java.util.Objects;

/**
 * An explicit embedding model: which provider, which model, how many dimensions. Prompt 027 embeds
 * queries with the outgoing spec while a reindex to a new one is in progress.
 *
 * @param providerId the provider id
 * @param model the provider's model name
 * @param dimensions the vector width
 */
public record EmbeddingModelSpec(String providerId, String model, int dimensions) {

    /** Validates the members. */
    public EmbeddingModelSpec {
        Objects.requireNonNull(providerId, "providerId");
        Objects.requireNonNull(model, "model");
        if (dimensions < 1) {
            throw new IllegalArgumentException("dimensions must be positive");
        }
    }

    /** {@code <providerId>:<model>:<dimensions>}, the {@code model_id} tag of embedded documents (027). */
    public String modelId() {
        return providerId + ":" + model + ":" + dimensions;
    }
}
