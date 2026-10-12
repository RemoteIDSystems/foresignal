package ai.foresignal.llm.client;

import java.util.List;

/**
 * Embedding vectors, one per input text and in input order.
 *
 * @param vectors the vectors
 * @param model the model that produced them
 * @param dimensions the vector width
 * @param usage tokens used (output is zero)
 */
public record EmbeddingResponse(List<float[]> vectors, String model, int dimensions, TokenUsage usage) {

    /** Normalizes the members. */
    public EmbeddingResponse {
        vectors = vectors == null ? List.of() : List.copyOf(vectors);
        usage = usage == null ? TokenUsage.ZERO : usage;
    }
}
