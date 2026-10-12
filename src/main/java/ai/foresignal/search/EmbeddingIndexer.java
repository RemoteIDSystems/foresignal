package ai.foresignal.search;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.llm.LlmBudgetExceededException;
import ai.foresignal.llm.LlmService;
import ai.foresignal.llm.client.EmbeddingModelSpec;
import ai.foresignal.llm.client.EmbeddingResponse;
import ai.foresignal.llm.client.LlmProviderException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Embeds documents and writes them into the vector slots of {@link SearchIndexState} (prompt 027).
 *
 * <p>Every embedding call goes through {@link LlmService}'s governed client of the spec's provider
 * with the explicit spec (026), at most {@value #BATCH} texts per call, so it is budgeted, metered
 * and logged. A budget refusal ({@link LlmBudgetExceededException}) always propagates: the caller
 * pauses without losing its place.
 *
 * <p><strong>Dual write.</strong> A changed document is embedded with the active spec into the
 * active slot and, while a reindex is pending, also with the pending spec into the pending slot, so
 * no document is missing after the swap. When the outgoing (active) model is no longer callable
 * during a reindex, the document's active vector is cleared rather than left describing the old text.
 *
 * <p>All of this runs under the slot read lock ({@link SearchIndexStateStore#slots()}).
 */
@Component
public class EmbeddingIndexer {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingIndexer.class);

    /** Texts per embedding call. */
    public static final int BATCH = 64;

    private final SearchIndexStateStore states;
    private final EmbeddingStore store;
    private final LlmService llm;

    /** Creates the indexer. */
    public EmbeddingIndexer(SearchIndexStateStore states, EmbeddingStore store, LlmService llm) {
        this.states = states;
        this.store = store;
        this.llm = llm;
    }

    /**
     * Writes {@code docs} (all of one doc type): unchanged text (same {@code text_hash}) only updates
     * the filter columns, changed or new text is embedded into the active and pending slots.
     *
     * @return how many documents were embedded
     * @throws LlmBudgetExceededException when the provider budget refuses a call
     */
    public int write(List<DocInput> docs) {
        if (docs.isEmpty()) {
            return 0;
        }
        Lock lock = states.slots().readLock();
        lock.lock();
        try {
            SearchIndexState state = states.get();
            List<DocInput> changed = new ArrayList<>();
            List<DocInput> unchanged = new ArrayList<>();
            for (Map.Entry<String, List<DocInput>> group : byType(docs).entrySet()) {
                Map<String, EmbeddingStore.DocState> stored = store.states(group.getKey(),
                        group.getValue().stream().map(DocInput::docId).toList(), null);
                for (DocInput doc : group.getValue()) {
                    EmbeddingStore.DocState existing = stored.get(doc.docId());
                    if (existing != null && existing.textHash() == doc.textHash()) {
                        unchanged.add(doc);
                    } else {
                        changed.add(doc);
                    }
                }
            }
            store.touch(unchanged);
            if (!changed.isEmpty()) {
                embedAndUpsert(changed, state);
            }
            return changed.size();
        } finally {
            lock.unlock();
        }
    }

    /**
     * The backfill of a reindex: brings {@code docs} (current records) into the pending slot.
     * Documents already holding the pending model with the same text are skipped (a resumed
     * backfill); same text without the pending vector gets only the pending vector; new or changed
     * text is written in full to both slots.
     *
     * @return how many documents needed an embedding
     */
    public int fillPending(List<DocInput> docs) {
        if (docs.isEmpty()) {
            return 0;
        }
        Lock lock = states.slots().readLock();
        lock.lock();
        try {
            SearchIndexState state = states.get();
            SearchIndexState.Pending pending = state.pending();
            if (pending == null) {
                return 0;
            }
            List<DocInput> changed = new ArrayList<>();
            List<EmbeddingStore.StoredDoc> pendingOnly = new ArrayList<>();
            for (Map.Entry<String, List<DocInput>> group : byType(docs).entrySet()) {
                Map<String, EmbeddingStore.DocState> stored = store.states(group.getKey(),
                        group.getValue().stream().map(DocInput::docId).toList(), pending.slot());
                for (DocInput doc : group.getValue()) {
                    EmbeddingStore.DocState existing = stored.get(doc.docId());
                    if (existing == null || existing.textHash() != doc.textHash()) {
                        changed.add(doc);
                    } else if (!pending.modelId().equals(existing.model())) {
                        pendingOnly.add(new EmbeddingStore.StoredDoc(doc.docType(), doc.docId(), doc.text()));
                    }
                }
            }
            if (!changed.isEmpty()) {
                embedAndUpsert(changed, state);
            }
            if (!pendingOnly.isEmpty()) {
                store.setVectors(pending.slot(), pending.modelId(), pendingOnly,
                        embed(pending.spec(), pendingOnly.stream().map(EmbeddingStore.StoredDoc::text).toList()));
            }
            return changed.size() + pendingOnly.size();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Embeds stored documents with {@code spec} into {@code slot} (other documents during a
     * backfill, stragglers before a swap). Skipped when {@code slot} is no longer the pending one.
     */
    public void fillSlot(VectorSlot slot, EmbeddingModelSpec spec, List<EmbeddingStore.StoredDoc> docs) {
        if (docs.isEmpty()) {
            return;
        }
        Lock lock = states.slots().readLock();
        lock.lock();
        try {
            SearchIndexState.Pending pending = states.get().pending();
            if (pending == null || pending.slot() != slot || !pending.modelId().equals(spec.modelId())) {
                return;
            }
            store.setVectors(slot, spec.modelId(), docs,
                    embed(spec, docs.stream().map(EmbeddingStore.StoredDoc::text).toList()));
        } finally {
            lock.unlock();
        }
    }

    /**
     * Embeds {@code texts} with {@code spec} through the governed client of its provider, at most
     * {@value #BATCH} per call.
     *
     * @throws LlmBudgetExceededException on a budget refusal
     * @throws LlmProviderException when the provider fails
     * @throws NotFoundException when the provider no longer exists
     */
    public List<float[]> embed(EmbeddingModelSpec spec, List<String> texts) {
        List<float[]> out = new ArrayList<>(texts.size());
        for (int from = 0; from < texts.size(); from += BATCH) {
            List<String> batch = texts.subList(from, Math.min(texts.size(), from + BATCH));
            EmbeddingResponse response = llm.client(spec.providerId()).embed(batch, spec);
            if (response.vectors().size() != batch.size()) {
                throw new LlmProviderException(LlmProviderException.Kind.INVALID_RESPONSE, spec.providerId(),
                        "The provider returned " + response.vectors().size() + " embeddings for " + batch.size()
                                + " texts");
            }
            for (float[] vector : response.vectors()) {
                if (vector.length != spec.dimensions()) {
                    throw new LlmProviderException(LlmProviderException.Kind.INVALID_RESPONSE, spec.providerId(),
                            "The provider returned a " + vector.length + "-dimensional embedding; " + spec.modelId()
                                    + " needs " + spec.dimensions());
                }
                out.add(vector);
            }
        }
        return out;
    }

    private void embedAndUpsert(List<DocInput> docs, SearchIndexState state) {
        Map<VectorSlot, EmbeddingStore.SlotVectors> vectors = new EnumMap<>(VectorSlot.class);
        Set<VectorSlot> cleared = EnumSet.noneOf(VectorSlot.class);
        List<String> texts = docs.stream().map(DocInput::text).toList();
        SearchIndexState.Pending pending = state.pending();
        if (state.hasActive()) {
            try {
                vectors.put(state.activeSlot(),
                        new EmbeddingStore.SlotVectors(state.activeModelId(), embed(state.activeSpec(), texts)));
            } catch (LlmBudgetExceededException e) {
                throw e;
            } catch (LlmProviderException | NotFoundException e) {
                if (pending == null) {
                    throw e;
                }
                // The outgoing model is gone while a reindex runs: the new text keeps no stale vector.
                log.warn("The outgoing embedding model {} is not callable ({}); {} documents get only the new model's"
                        + " vector until the swap", state.activeModelId(), e.getMessage(), docs.size());
                cleared.add(state.activeSlot());
            }
        }
        if (pending != null) {
            vectors.put(pending.slot(), new EmbeddingStore.SlotVectors(pending.modelId(), embed(pending.spec(), texts)));
        }
        store.upsert(docs, vectors, cleared);
    }

    private static Map<String, List<DocInput>> byType(List<DocInput> docs) {
        Map<String, List<DocInput>> out = new LinkedHashMap<>();
        for (DocInput doc : docs) {
            out.computeIfAbsent(doc.docType(), t -> new ArrayList<>()).add(doc);
        }
        return out;
    }
}
