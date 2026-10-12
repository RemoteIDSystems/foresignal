package ai.foresignal.search;

import org.springframework.data.jpa.repository.JpaRepository;

/** Read access to {@code embedding_document}; writes are native ({@link EmbeddingStore}). */
public interface EmbeddingDocumentRepository extends JpaRepository<EmbeddingDocumentEntity, EmbeddingDocumentId> {}
