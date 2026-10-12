package ai.foresignal.search;

import java.io.Serializable;
import java.util.Objects;

/**
 * The primary key of {@link EmbeddingDocumentEntity}: {@code (doc_type, doc_id)}.
 *
 * @param docType {@code record}, {@code briefing} or {@code alert}
 * @param docId {@code <source>:<externalId>} for a record, the run or alert id otherwise
 */
public record EmbeddingDocumentId(String docType, String docId) implements Serializable {

    /** Validates the members. */
    public EmbeddingDocumentId {
        Objects.requireNonNull(docType, "docType");
        Objects.requireNonNull(docId, "docId");
    }
}
