package ai.foresignal.search;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.config.StartupOrder;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.llm.EmbeddingModelChangedEvent;
import ai.foresignal.llm.LlmBudgetExceededException;
import ai.foresignal.llm.LlmSettingsService;
import ai.foresignal.llm.client.EmbeddingModelSpec;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Zero-downtime reindex (prompt 027): a new embedding model is built in the inactive vector slot
 * while searches keep answering from the active one, then swapped in.
 *
 * <p>Triggered by {@link EmbeddingModelChangedEvent} (026) or {@code POST /v1/search/reindex} (the
 * same model is allowed, to rebuild), one at a time ({@code 409 REINDEX_RUNNING}). Steps:
 *
 * <ol>
 *   <li>Pick the inactive slot (slot A for the first index), recreate its columns at the new width
 *       and record {@code pending} with {@code total} = eligible current records + other documents.
 *   <li>{@link SearchIndexState.Phase#RECORDS}: backfill from {@code record_head} (keyset on
 *       {@code (source_id, external_id)}, {@value EmbeddingIndexer#BATCH} per call, budgeted).
 *       Meanwhile the worker dual-writes new documents into both slots.
 *   <li>{@link SearchIndexState.Phase#DOCUMENTS}: briefing runs and enriched alerts, from their stored
 *       text.
 *   <li>{@link SearchIndexState.Phase#INDEXING}: {@code CREATE INDEX CONCURRENTLY} the pending HNSW
 *       index, then re-embed every document whose pending model is not the new one (stragglers).
 *   <li>{@link SearchIndexState.Phase#SWAPPING}: under the slot write lock, check that no straggler is
 *       left, flip {@code activeSlot}/{@code activeModelId}/{@code activeSpec} in one settings write,
 *       then drop the old slot's index and columns.
 * </ol>
 *
 * <p>Progress ({@code done} and the keyset position) is stored after every batch, so a restart
 * resumes where it stopped. A budget refusal waits for the next minute. {@code DELETE
 * /v1/search/reindex} cancels: {@code pending} and the pending slot are cleared.
 */
@Component
public class ReindexService implements IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(ReindexService.class);

    /** The window of the moving-average rate. */
    static final Duration RATE_WINDOW = Duration.ofMinutes(10);

    private final SearchIndexStateStore states;
    private final EmbeddingSchemaManager schema;
    private final SemanticCapability capability;
    private final LlmSettingsService llmSettings;
    private final EmbeddingIndexer indexer;
    private final EmbeddingStore store;
    private final EmbeddingEligibility eligibility;
    private final VersionRows versions;
    private final Clock clock;
    private final TransactionTemplate newTransaction;

    private final AtomicLong generation = new AtomicLong();
    private final Deque<Sample> samples = new ArrayDeque<>();
    private volatile Thread runner;
    private volatile boolean backgroundEnabled = true;

    private record Sample(Instant at, long done) {}

    /** Creates the service. */
    public ReindexService(SearchIndexStateStore states, EmbeddingSchemaManager schema, SemanticCapability capability,
            LlmSettingsService llmSettings, EmbeddingIndexer indexer, EmbeddingStore store,
            EmbeddingEligibility eligibility, VersionRows versions, Clock clock, PlatformTransactionManager transactions) {
        this.states = states;
        this.schema = schema;
        this.capability = capability;
        this.llmSettings = llmSettings;
        this.indexer = indexer;
        this.store = store;
        this.eligibility = eligibility;
        this.versions = versions;
        this.clock = clock;
        this.newTransaction = new TransactionTemplate(transactions);
        this.newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    // ------------------------------------------------------------------------- triggers

    /**
     * Brings the index in line with the assigned embedding model at start: resumes a pending reindex,
     * or starts one when there is no index or it was built with another model (a change missed while
     * the application was down).
     */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.EMBEDDING + 1)
    public synchronized void reconcile() {
        if (!capability.available()) {
            return;
        }
        EmbeddingModelSpec spec = llmSettings.embeddingSpec().orElse(null);
        SearchIndexState state = states.get();
        if (state.hasActive() && !slotMatches(state.activeSlot(), state.activeSpec())) {
            log.warn("The active vector slot {} no longer matches {}; the index is rebuilt", state.activeSlot(),
                    state.activeModelId());
            states.put(SearchIndexState.EMPTY);
            state = SearchIndexState.EMPTY;
        }
        SearchIndexState.Pending pending = state.pending();
        if (pending != null) {
            if (spec != null && pending.modelId().equals(spec.modelId()) && slotMatches(pending.slot(), spec)) {
                log.info("Resuming the reindex to {} at {} of {}", pending.modelId(), pending.done(), pending.total());
                launch();
                return;
            }
            cancel();
            state = states.get();
        }
        if (spec != null && (!state.hasActive() || !state.activeModelId().equals(spec.modelId()))) {
            start(spec);
        }
    }

    /**
     * Reacts to a change of the embedding model (026): cancel, start, or nothing. The event is
     * published after the settings change commits, so the state is written in a transaction of its
     * own.
     */
    @EventListener
    public void onModelChanged(EmbeddingModelChangedEvent event) {
        if (!capability.available()) {
            return;
        }
        newTransaction.executeWithoutResult(status -> handle(event.newSpec()));
    }

    private synchronized void handle(EmbeddingModelSpec next) {
        SearchIndexState state = states.get();
        if (next == null) {
            cancel();
            return;
        }
        if (state.pending() != null) {
            if (state.pending().modelId().equals(next.modelId())) {
                return;
            }
            cancel();
            state = states.get();
        }
        if (state.hasActive() && state.activeModelId().equals(next.modelId())) {
            return;
        }
        start(next);
    }

    /**
     * {@code POST /v1/search/reindex}: rebuilds with the assigned embedding model (the same model is
     * allowed).
     *
     * @throws ConflictException {@code REINDEX_RUNNING} while one runs, {@code SEMANTIC_UNAVAILABLE}
     *     without pgvector or an embedding provider
     */
    public synchronized void requestReindex() {
        start(llmSettings.embeddingSpec().orElse(null));
    }

    /**
     * Starts a reindex to {@code spec}.
     *
     * @throws ConflictException {@code REINDEX_RUNNING} or {@code SEMANTIC_UNAVAILABLE}
     */
    public synchronized void start(EmbeddingModelSpec spec) {
        SearchIndexState state = states.get();
        if (state.pending() != null) {
            throw new ConflictException(ErrorCodes.REINDEX_RUNNING, "A reindex to " + state.pending().modelId()
                    + " is already running; cancel it with DELETE /v1/search/reindex first.");
        }
        if (!capability.available()) {
            throw new ConflictException(ErrorCodes.SEMANTIC_UNAVAILABLE, capability.reason());
        }
        if (spec == null) {
            throw new ConflictException(ErrorCodes.SEMANTIC_UNAVAILABLE, SearchStatusService.NO_PROVIDER);
        }
        VectorSlot slot = state.hasActive() ? state.activeSlot().other() : VectorSlot.A;
        Lock lock = states.slots().writeLock();
        lock.lock();
        try {
            schema.resetSlot(slot, spec.dimensions());
            long total = versions.countHeads(eligibility.sources()) + store.countNonRecord();
            SearchIndexState.Pending pending = new SearchIndexState.Pending(slot, spec.modelId(), spec,
                    state.activeModelId(), clock.instant(), total, 0, SearchIndexState.Phase.RECORDS, null, null);
            states.put(states.get().withPending(pending));
        } finally {
            lock.unlock();
        }
        synchronized (samples) {
            samples.clear();
            samples.add(new Sample(clock.instant(), 0));
        }
        log.info("Reindex started: {} -> {} in slot {}", state.activeModelId(), spec.modelId(), slot);
        launch();
    }

    /** {@code DELETE /v1/search/reindex}: clears {@code pending} and the pending slot. Idempotent. */
    public synchronized void cancel() {
        generation.incrementAndGet();
        Thread current = runner;
        if (current != null) {
            current.interrupt();
        }
        Lock lock = states.slots().writeLock();
        lock.lock();
        try {
            SearchIndexState state = states.get();
            SearchIndexState.Pending pending = state.pending();
            if (pending != null) {
                if (capability.available() && (!state.hasActive() || state.activeSlot() != pending.slot())) {
                    schema.dropSlot(pending.slot());
                }
                states.put(state.withPending(null));
                log.info("Reindex to {} canceled at {} of {}", pending.modelId(), pending.done(), pending.total());
            }
        } finally {
            lock.unlock();
        }
        synchronized (samples) {
            samples.clear();
        }
    }

    // ------------------------------------------------------------------------- work

    /**
     * Runs one unit of the pending reindex: one backfill batch, one straggler batch, the index build
     * or the swap.
     *
     * @return true while more work remains
     * @throws LlmBudgetExceededException when the embedding budget refuses a call
     */
    public synchronized boolean step() {
        SearchIndexState state = states.get();
        SearchIndexState.Pending pending = state.pending();
        if (pending == null) {
            return false;
        }
        return switch (pending.phase()) {
            case RECORDS -> backfillRecords(pending);
            case DOCUMENTS -> backfillDocuments(pending);
            case INDEXING -> index(pending);
            case SWAPPING -> swap(pending);
        };
    }

    /** Runs {@link #step()} until the reindex is done (tests, and the background loop). */
    public void runToCompletion() {
        while (step()) {
            // keep going
        }
    }

    /**
     * Switches the background loop off or on (tests drive {@link #step()} themselves). Turning it on
     * resumes a pending reindex.
     */
    public void setBackgroundEnabled(boolean enabled) {
        backgroundEnabled = enabled;
        if (enabled && states.get().pending() != null) {
            launch();
        }
    }

    @Override
    public void resetState() {
        generation.incrementAndGet();
        Thread current = runner;
        if (current != null) {
            current.interrupt();
        }
        synchronized (samples) {
            samples.clear();
        }
    }

    private boolean backfillRecords(SearchIndexState.Pending pending) {
        Set<String> sources = eligibility.sources();
        List<VersionRows.Row> rows = versions.heads(sources, pending.afterKey(), pending.afterId(),
                EmbeddingIndexer.BATCH);
        if (rows.isEmpty()) {
            save(pending, pending.at(SearchIndexState.Phase.DOCUMENTS));
            return true;
        }
        List<DocInput> docs = new ArrayList<>(rows.size());
        for (VersionRows.Row row : rows) {
            Optional<SourceDescriptor> descriptor = eligibility.descriptor(row.sourceId());
            if (descriptor.isPresent()) {
                DocInput doc = versions.document(row, descriptor.get());
                if (doc != null) {
                    docs.add(doc);
                }
            }
        }
        indexer.fillPending(docs);
        VersionRows.Row last = rows.getLast();
        save(pending, pending.advanced(rows.size(), last.sourceId(), last.externalId()));
        return true;
    }

    private boolean backfillDocuments(SearchIndexState.Pending pending) {
        List<EmbeddingStore.StoredDoc> docs = store.missing(pending.slot(), pending.modelId(), true,
                pending.afterKey(), pending.afterId(), EmbeddingIndexer.BATCH);
        if (docs.isEmpty()) {
            save(pending, pending.at(SearchIndexState.Phase.INDEXING));
            return true;
        }
        indexer.fillSlot(pending.slot(), pending.spec(), docs);
        EmbeddingStore.StoredDoc last = docs.getLast();
        save(pending, pending.advanced(docs.size(), last.docType(), last.docId()));
        return true;
    }

    private boolean index(SearchIndexState.Pending pending) {
        schema.buildIndex(pending.slot());
        List<EmbeddingStore.StoredDoc> stragglers = store.missing(pending.slot(), pending.modelId(), false, null, null,
                EmbeddingIndexer.BATCH);
        if (!stragglers.isEmpty()) {
            indexer.fillSlot(pending.slot(), pending.spec(), stragglers);
            return true;
        }
        save(pending, pending.at(SearchIndexState.Phase.SWAPPING));
        return true;
    }

    private boolean swap(SearchIndexState.Pending pending) {
        Lock lock = states.slots().writeLock();
        lock.lock();
        try {
            SearchIndexState state = states.get();
            if (state.pending() == null || !state.pending().modelId().equals(pending.modelId())) {
                return false;
            }
            if (store.countMissing(pending.slot(), pending.modelId()) > 0) {
                states.put(state.withPending(pending.at(SearchIndexState.Phase.INDEXING)));
                return true;
            }
            VectorSlot old = state.activeSlot();
            states.put(new SearchIndexState(pending.slot(), pending.modelId(), pending.spec(), null));
            if (old != null && old != pending.slot()) {
                schema.dropSlot(old);
            }
            log.info("Reindex complete: slot {} ({}) is now active after {} documents", pending.slot(),
                    pending.modelId(), pending.done());
        } finally {
            lock.unlock();
        }
        synchronized (samples) {
            samples.clear();
        }
        return false;
    }

    /** Stores {@code next} when {@code expected} is still the pending reindex (it may have been canceled). */
    private void save(SearchIndexState.Pending expected, SearchIndexState.Pending next) {
        SearchIndexState state = states.get();
        SearchIndexState.Pending current = state.pending();
        if (current == null || !current.modelId().equals(expected.modelId())
                || !current.startedAt().equals(expected.startedAt())) {
            return;
        }
        states.put(state.withPending(next));
        if (next.done() != expected.done()) {
            synchronized (samples) {
                Instant now = clock.instant();
                samples.add(new Sample(now, next.done()));
                while (samples.size() > 2 && samples.peekFirst().at().isBefore(now.minus(RATE_WINDOW))) {
                    samples.pollFirst();
                }
            }
        }
    }

    private void launch() {
        if (!backgroundEnabled) {
            return;
        }
        Thread current = runner;
        if (current != null && current.isAlive()) {
            return;
        }
        long gen = generation.get();
        runner = Thread.ofVirtual().name("search-reindex").start(() -> loop(gen));
    }

    private void loop(long gen) {
        while (gen == generation.get() && backgroundEnabled) {
            try {
                if (!step()) {
                    return;
                }
            } catch (LlmBudgetExceededException e) {
                Instant now = clock.instant();
                Duration wait = Duration.between(now, now.truncatedTo(ChronoUnit.MINUTES).plus(Duration.ofMinutes(1)));
                log.info("Reindex paused for {} s: the embedding budget is exhausted", wait.toSeconds());
                if (!sleep(wait)) {
                    return;
                }
            } catch (RuntimeException e) {
                log.warn("Reindex step failed; retrying in a minute: {}", e.getMessage());
                if (!sleep(Duration.ofMinutes(1))) {
                    return;
                }
            }
        }
    }

    private static boolean sleep(Duration wait) {
        try {
            Thread.sleep(Math.max(1_000, Math.min(wait.toMillis(), 60_000)));
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private boolean slotMatches(VectorSlot slot, EmbeddingModelSpec spec) {
        OptionalInt dims = schema.dimensions(slot);
        return dims.isPresent() && dims.getAsInt() == spec.dimensions();
    }

    // ------------------------------------------------------------------------- status

    /** The {@code SearchReindexState} object of the status. */
    public Map<String, Object> reindexState() {
        SearchIndexState state = states.get();
        SearchIndexState.Pending pending = state.pending();
        Map<String, Object> out = new LinkedHashMap<>();
        if (pending == null) {
            out.put("state", "idle");
            out.put("fromModel", null);
            out.put("toModel", null);
            out.put("total", null);
            out.put("done", null);
            out.put("percent", null);
            out.put("ratePerMinute", null);
            out.put("etaSeconds", null);
            out.put("startedAt", null);
            return out;
        }
        long total = Math.max(pending.total(), pending.done());
        Double rate = ratePerMinute();
        out.put("state", pending.phase().wireValue());
        out.put("fromModel", pending.fromModelId());
        out.put("toModel", pending.modelId());
        out.put("total", total);
        out.put("done", pending.done());
        out.put("percent", total == 0 ? 100.0 : Math.min(100.0, Math.round(pending.done() * 1000.0 / total) / 10.0));
        out.put("ratePerMinute", rate);
        out.put("etaSeconds", rate == null || rate <= 0 ? null
                : (long) Math.ceil(Math.max(0, total - pending.done()) / rate * 60.0));
        out.put("startedAt", pending.startedAt());
        return out;
    }

    /** Documents per minute over the last {@link #RATE_WINDOW}, or null without two samples. */
    Double ratePerMinute() {
        synchronized (samples) {
            if (samples.size() < 2) {
                return null;
            }
            Sample first = samples.peekFirst();
            Sample last = samples.peekLast();
            long millis = Duration.between(first.at(), last.at()).toMillis();
            if (millis <= 0) {
                return null;
            }
            return Math.round((last.done() - first.done()) * 60_000.0 / millis * 10.0) / 10.0;
        }
    }
}
