package ai.foresignal.search;

import ai.foresignal.llm.client.EmbeddingModelSpec;
import java.time.Instant;

/**
 * {@code app_setting['search.index']}: which vector slot is live and which one a reindex is filling
 * (prompt 027). Every read and write of a vector column resolves its slot from this state; no code
 * hardcodes a column.
 *
 * @param activeSlot the slot searches read, or null before the first index is built
 * @param activeModelId the model id ({@link EmbeddingModelSpec#modelId()}) of the active slot's vectors
 * @param activeSpec the model that produced them; queries are embedded with it
 * @param pending the reindex in progress, or null
 */
public record SearchIndexState(VectorSlot activeSlot, String activeModelId, EmbeddingModelSpec activeSpec,
        Pending pending) {

    /** No index, no reindex. */
    public static final SearchIndexState EMPTY = new SearchIndexState(null, null, null, null);

    /** True when an active slot exists. */
    public boolean hasActive() {
        return activeSlot != null && activeModelId != null && activeSpec != null;
    }

    /** This state with {@code next} as the pending reindex. */
    public SearchIndexState withPending(Pending next) {
        return new SearchIndexState(activeSlot, activeModelId, activeSpec, next);
    }

    /** The phases of a reindex (the {@code SearchReindexState.state} of the status). */
    public enum Phase {
        /** Embedding current records into the pending slot. */
        RECORDS,
        /** Embedding the other documents (briefing runs, enriched alerts) into the pending slot. */
        DOCUMENTS,
        /** Building the pending slot's HNSW index and re-embedding stragglers. */
        INDEXING,
        /** Flipping the active slot. */
        SWAPPING;

        /** The wire value of {@code SearchReindexState.state}. */
        public String wireValue() {
            return switch (this) {
                case RECORDS, DOCUMENTS -> "backfilling";
                case INDEXING -> "indexing";
                case SWAPPING -> "swapping";
            };
        }
    }

    /**
     * A reindex in progress, resumable after a restart from {@code done} and the keyset position.
     *
     * @param slot the slot being filled
     * @param modelId the new model id
     * @param spec the new model
     * @param fromModelId the outgoing model id, or null for the first index
     * @param startedAt when it started
     * @param total eligible current records plus other documents when it started
     * @param done documents processed so far
     * @param phase where it is
     * @param afterKey the keyset position's first member (source id, or doc type), or null
     * @param afterId the keyset position's second member (external id, or doc id), or null
     */
    public record Pending(VectorSlot slot, String modelId, EmbeddingModelSpec spec, String fromModelId,
            Instant startedAt, long total, long done, Phase phase, String afterKey, String afterId) {

        /** This reindex at {@code nextPhase}, with the keyset reset. */
        public Pending at(Phase nextPhase) {
            return new Pending(slot, modelId, spec, fromModelId, startedAt, total, done, nextPhase, null, null);
        }

        /** This reindex having processed {@code more} documents up to the keyset position. */
        public Pending advanced(long more, String key, String id) {
            return new Pending(slot, modelId, spec, fromModelId, startedAt, total, done + more, phase, key, id);
        }
    }
}
