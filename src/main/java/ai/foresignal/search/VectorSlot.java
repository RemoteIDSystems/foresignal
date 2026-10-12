package ai.foresignal.search;

import java.util.Locale;

/**
 * One of the two vector slots of {@code embedding_document} (prompt 027). Each slot is a
 * {@code vector(N)} column, a {@code varchar(200)} column holding the model id of each row's vector,
 * and an HNSW index. Column names come only from here, never from input.
 */
public enum VectorSlot {
    /** {@code embedding_a}, {@code model_a}, {@code embedding_document_hnsw_a}. */
    A,
    /** {@code embedding_b}, {@code model_b}, {@code embedding_document_hnsw_b}. */
    B;

    private String suffix() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** The vector column. */
    public String embeddingColumn() {
        return "embedding_" + suffix();
    }

    /** The model-id column. */
    public String modelColumn() {
        return "model_" + suffix();
    }

    /** The HNSW index. */
    public String indexName() {
        return "embedding_document_hnsw_" + suffix();
    }

    /** The other slot. */
    public VectorSlot other() {
        return this == A ? B : A;
    }
}
