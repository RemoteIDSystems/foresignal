package ai.foresignal.search;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.config.StartupOrder;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.llm.EmbeddingModelChangedEvent;
import ai.foresignal.llm.LlmBudgetExceededException;
import ai.foresignal.llm.LlmSettingsService;
import ai.foresignal.llm.client.LlmProviderException;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.ChangeListener;
import ai.foresignal.stream.ChangeSummary;
import ai.foresignal.stream.Subscription;
import jakarta.annotation.PreDestroy;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Keeps {@code embedding_document} in step with the record store (Architecture §20, prompt 027).
 *
 * <p>Runs only while semantic search is possible: pgvector is available ({@link SemanticCapability})
 * and an embedding provider is assigned. It subscribes to the {@link ChangeBus} (queue
 * {@value #QUEUE}) with the durable cursor {@code app_setting['embedding.cursor']}, catches up from
 * the database at start, on {@code onGap} and on every {@linkplain #tick tick}, and advances the
 * cursor as soon as a range is written: retention clamps to {@code min(trigger.cursor,
 * embedding.cursor) + 1} (Core-Data-Model §13), so a stalled worker holds retention back.
 *
 * <p><strong>Candidates</strong> are versions of {@linkplain EmbeddingEligibility eligible} sources
 * ({@code embedFields}, {@code embed=true}, not private). {@code HEARTBEAT} versions are skipped
 * from the batch summary without loading anything; the others are loaded by {@code seq = ANY(?)},
 * reduced to the latest version per entity and handed to {@link EmbeddingIndexer}, which skips
 * unchanged text by {@code text_hash}. A tombstone deletes the record's document.
 *
 * <p><strong>Pauses.</strong> A budget refusal, a provider failure or a database error pauses the
 * worker until the next minute <em>without</em> advancing the cursor; documents already written
 * stay, and the range is retried from the cursor.
 *
 * <p>Left out in edge mode (prompt 113: embeddings).
 */
@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = ai.foresignal.config.EdgeMode.PROPERTY,
        havingValue = ai.foresignal.config.EdgeMode.FULL, matchIfMissing = true)
public class EmbeddingWorker implements ChangeListener, IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingWorker.class);

    /** The worker's change-bus queue. */
    public static final int QUEUE = 4_096;

    /** Seqs per catch-up chunk. */
    public static final long CHUNK = 2_000;

    private final ChangeBus changeBus;
    private final SemanticCapability capability;
    private final LlmSettingsService llmSettings;
    private final EmbeddingEligibility eligibility;
    private final EmbeddingIndexer indexer;
    private final EmbeddingStore store;
    private final VersionRows versions;
    private final SearchIndexStateStore states;
    private final AppSettingService settings;
    private final JdbcTemplate jdbc;
    private final Clock clock;
    private final TransactionTemplate newTransaction;

    private volatile Subscription subscription;
    private long cursor = -1;
    private volatile Instant pausedUntil;
    private volatile String lastError;

    /** Creates the worker; nothing runs until {@link #start()}. */
    public EmbeddingWorker(ChangeBus changeBus, SemanticCapability capability, LlmSettingsService llmSettings,
            EmbeddingEligibility eligibility, EmbeddingIndexer indexer, EmbeddingStore store, VersionRows versions,
            SearchIndexStateStore states, AppSettingService settings, JdbcTemplate jdbc, Clock clock,
            PlatformTransactionManager transactions) {
        this.changeBus = changeBus;
        this.capability = capability;
        this.llmSettings = llmSettings;
        this.eligibility = eligibility;
        this.indexer = indexer;
        this.store = store;
        this.versions = versions;
        this.states = states;
        this.settings = settings;
        this.jdbc = jdbc;
        this.clock = clock;
        this.newTransaction = new TransactionTemplate(transactions);
        this.newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    // ------------------------------------------------------------------------- lifecycle

    /** Subscribes and catches up, when pgvector is available and an embedding provider is assigned. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.EMBEDDING)
    public synchronized void start() {
        if (subscribe()) {
            catchUp();
        }
    }

    /** Subscribes without catching up; true when this call subscribed. */
    private synchronized boolean subscribe() {
        if (subscription != null) {
            return false;
        }
        if (!capability.available()) {
            log.info("Embedding worker not started: {}", capability.reason());
            return false;
        }
        if (llmSettings.embeddingSpec().isEmpty()) {
            log.info("Embedding worker not started: no embedding provider is assigned");
            return false;
        }
        cursor = loadCursor();
        pausedUntil = null;
        subscription = changeBus.subscribe("embedding-worker", this, QUEUE);
        log.info("Embedding worker started at cursor {}", cursor);
        return true;
    }

    /** Unsubscribes; the cursor stays where the last written range left it. */
    @PreDestroy
    public synchronized void stop() {
        Subscription current = subscription;
        subscription = null;
        if (current != null) {
            current.close();
            log.info("Embedding worker stopped at cursor {}", cursor);
        }
    }

    /**
     * Starts when an embedding provider is assigned, stops when the role is cleared. The event is
     * published after the settings change commits, so the cursor is written in a transaction of its
     * own; any catching up happens on the next {@linkplain #tick tick} or batch, off this thread.
     */
    @EventListener
    public void onModelChanged(EmbeddingModelChangedEvent event) {
        if (event.newSpec() == null) {
            stop();
        } else {
            newTransaction.executeWithoutResult(status -> subscribe());
        }
    }

    /** True while subscribed. */
    public boolean running() {
        return subscription != null;
    }

    /** The last seq fully processed. */
    public synchronized long cursor() {
        return cursor;
    }

    /** When the current pause ends, or null when not paused. */
    public Instant pausedUntil() {
        Instant until = pausedUntil;
        return until != null && clock.instant().isBefore(until) ? until : null;
    }

    /** The last failure, or null. */
    public String lastError() {
        return lastError;
    }

    /** Re-reads the cursor and forgets a pause (the test cleaner calls it after truncating). */
    @Override
    public synchronized void resetState() {
        pausedUntil = null;
        lastError = null;
        if (subscription != null) {
            cursor = loadCursor();
        }
    }

    // ------------------------------------------------------------------------- bus

    @Override
    public synchronized void onBatch(ChangeBatch batch) {
        if (subscription == null || cursor < 0 || batch.toSeq() <= cursor || paused()) {
            return;
        }
        if (batch.fromSeq() - 1 > cursor) {
            processRange(cursor, batch.toSeq());
            return;
        }
        Set<String> eligible = eligibility.sources();
        List<Long> seqs = new ArrayList<>();
        for (ChangeSummary change : batch.changes()) {
            if (change.seq() > cursor && change.persistReason() != PersistReason.HEARTBEAT
                    && eligible.contains(change.sourceId())) {
                seqs.add(change.seq());
            }
        }
        if (seqs.isEmpty() || apply(versions.bySeq(seqs))) {
            advance(batch.toSeq());
        }
    }

    @Override
    public synchronized void onGap(long lastDeliveredSeq) {
        catchUp();
    }

    /** Retries after a pause and picks up anything the bus did not deliver. */
    @Scheduled(fixedDelay = 5_000, initialDelay = 5_000)
    public void tick() {
        if (running() && !paused()) {
            catchUp();
        }
    }

    /** Processes everything between the cursor and the bus watermark, unless paused. */
    public synchronized void catchUp() {
        if (subscription == null) {
            return;
        }
        long head = changeBus.lastPublishedSeq();
        while (cursor < head && !paused()) {
            long to = Math.min(head, cursor + CHUNK);
            if (!processRange(cursor, to)) {
                return;
            }
        }
    }

    private boolean processRange(long from, long to) {
        Set<String> eligible = eligibility.sources();
        List<VersionRows.Row> rows;
        try {
            rows = versions.range(from, to, eligible);
        } catch (DataAccessException e) {
            pause(e);
            return false;
        }
        if (rows.isEmpty() || apply(rows)) {
            advance(to);
            return true;
        }
        return false;
    }

    // ------------------------------------------------------------------------- documents

    /** Writes the documents of {@code rows}; false (and paused) when a call or write failed. */
    private boolean apply(List<VersionRows.Row> rows) {
        try {
            List<DocInput> docs = new ArrayList<>();
            for (VersionRows.Row row : VersionRows.latestPerEntity(rows)) {
                Optional<SourceDescriptor> descriptor = eligibility.descriptor(row.sourceId());
                if (descriptor.isEmpty()) {
                    continue;
                }
                DocInput doc = versions.document(row, descriptor.get());
                if (doc == null) {
                    // A tombstone, or no embed field has a value: the record is not searchable.
                    store.delete(DocInput.RECORD, row.docId(), row.seq());
                } else {
                    docs.add(doc);
                }
            }
            for (int from = 0; from < docs.size(); from += EmbeddingIndexer.BATCH) {
                indexer.write(docs.subList(from, Math.min(docs.size(), from + EmbeddingIndexer.BATCH)));
            }
            lastError = null;
            return true;
        } catch (LlmBudgetExceededException | LlmProviderException | NotFoundException | DataAccessException e) {
            pause(e);
            return false;
        }
    }

    // ------------------------------------------------------------------------- region re-embedding

    /**
     * Re-embeds one record's document from its current head (prompt 040: its country or first-level region
     * changed, so its text's region suffix did). Does nothing while not running, when the source is not
     * eligible or when the record has no document text; failures are logged and left to the next version.
     */
    public synchronized void reembed(String sourceId, String externalId) {
        if (subscription == null || paused()) {
            return;
        }
        Optional<SourceDescriptor> descriptor = eligibility.descriptor(sourceId);
        if (descriptor.isEmpty() || !eligibility.sources().contains(sourceId)) {
            return;
        }
        try {
            VersionRows.Row head = versions.head(sourceId, externalId);
            DocInput doc = head == null ? null : versions.document(head, descriptor.get());
            if (doc != null) {
                indexer.write(List.of(doc));
            }
        } catch (LlmBudgetExceededException | LlmProviderException | NotFoundException | DataAccessException e) {
            log.debug("Could not re-embed {}:{}: {}", sourceId, externalId, e.getMessage());
        }
    }

    // ------------------------------------------------------------------------- embedDocument

    /**
     * Embeds a briefing run (029) or an enriched alert (030), or any other non-record document, into
     * the active slot and, while a reindex is pending, the pending slot as well. Unchanged text only
     * updates the filter columns.
     *
     * @param docType {@code briefing} or {@code alert}
     * @param docId the run or alert id
     * @param text the text (cut to {@value EmbeddingText#MAX_CHARS} characters)
     * @param geom the geometry (SRID 4326), or null
     * @param effectiveTime the time searches filter on, or null
     * @param sourceId the source, or null
     * @param kind the kind, or null
     * @throws LlmBudgetExceededException when the embedding budget refuses the call
     */
    public void embedDocument(String docType, String docId, String text, Geometry geom, Instant effectiveTime,
            String sourceId, String kind) {
        if (text == null || text.isBlank()) {
            store.delete(docType, docId, null);
            return;
        }
        indexer.write(List.of(new DocInput(docType, docId, sourceId, kind, null, Geometries.hexEwkb(geom),
                effectiveTime, effectiveTime, text)));
    }

    // ------------------------------------------------------------------------- cursor and pauses

    private boolean paused() {
        return pausedUntil() != null;
    }

    private void pause(RuntimeException e) {
        Instant now = clock.instant();
        Instant until = now.truncatedTo(ChronoUnit.MINUTES).plus(Duration.ofMinutes(1));
        pausedUntil = until;
        lastError = e.getMessage();
        if (e instanceof LlmBudgetExceededException) {
            log.info("Embedding worker paused until {}: the embedding budget is exhausted ({})", until, e.getMessage());
        } else {
            log.warn("Embedding worker paused until {} at cursor {}: {}", until, cursor, e.getMessage());
        }
    }

    private void advance(long to) {
        cursor = to;
        settings.put(SettingKeys.EMBEDDING_CURSOR, to);
    }

    private long loadCursor() {
        List<String> values = jdbc.queryForList("SELECT value::text FROM app_setting WHERE key = ?", String.class,
                SettingKeys.EMBEDDING_CURSOR);
        long head = changeBus.lastPublishedSeq();
        if (!values.isEmpty() && values.getFirst() != null) {
            try {
                long stored = Long.parseLong(values.getFirst().strip());
                if (!states.get().hasActive() && stored < head) {
                    // No active index: the reindex backfill embeds every current record from
                    // record_head, so replaying old versions would only repeat that work.
                    settings.put(SettingKeys.EMBEDDING_CURSOR, head);
                    return head;
                }
                return stored;
            } catch (NumberFormatException e) {
                log.warn("Ignoring an unreadable embedding cursor '{}'", values.getFirst());
            }
        }
        // A fresh start: the reindex backfill covers the current records; follow from here on.
        settings.put(SettingKeys.EMBEDDING_CURSOR, head);
        return head;
    }
}
