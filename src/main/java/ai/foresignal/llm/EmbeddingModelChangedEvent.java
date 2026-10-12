package ai.foresignal.llm;

import ai.foresignal.llm.client.EmbeddingModelSpec;

/**
 * The active embedding model changed (prompt 026; handled by 027's reindex). Published after the
 * change commits, when the embedding provider is reassigned or the active embedding provider's model
 * or dimensions change.
 *
 * @param oldSpec the spec before, or null when no embedding provider was assigned
 * @param newSpec the spec after, or null when the embedding role was cleared
 */
public record EmbeddingModelChangedEvent(EmbeddingModelSpec oldSpec, EmbeddingModelSpec newSpec) {}
