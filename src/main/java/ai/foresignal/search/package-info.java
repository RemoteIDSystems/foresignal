/**
 * Embeddings and semantic search (Architecture §20, FR-47, FR-48, prompt 027).
 *
 * <p>{@code embedding_document} holds one row per searchable document: the latest version of a
 * record from a source that declares {@code embedFields}, a briefing run (029) or an enriched alert
 * (030). Its two vector slots ({@code embedding_a}, {@code embedding_b}) are managed only by
 * {@link ai.foresignal.search.EmbeddingSchemaManager}; which slot is live is recorded in
 * {@code app_setting['search.index']} ({@link ai.foresignal.search.SearchIndexState}), so changing
 * the embedding model builds the new index beside the old one and swaps it in when the backfill
 * completes ({@link ai.foresignal.search.ReindexService}).
 *
 * <p>{@link ai.foresignal.search.EmbeddingWorker} follows the {@code ChangeBus} with the durable
 * cursor {@code embedding.cursor}; {@link ai.foresignal.search.SearchService} answers
 * {@code GET /v1/search} in {@code text}, {@code semantic} and {@code hybrid} modes. Without pgvector
 * or an embedding provider, text search keeps working and the status explains why semantic search
 * is off.
 */
package ai.foresignal.search;
