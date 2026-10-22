package ai.foresignal.ingest;

import ai.foresignal.collect.IngestCounts;
import ai.foresignal.collect.UsageMeter;
import ai.foresignal.config.StartupOrder;
import ai.foresignal.isolation.WorkRole;
import ai.foresignal.isolation.WorkThreads;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.CoverageSample;
import ai.foresignal.store.history.PartitionManager;
import ai.foresignal.stream.Bbox;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.ChangeSummary;
import ai.foresignal.stream.LiveBatch;
import ai.foresignal.stream.LiveBus;
import ai.foresignal.stream.LiveUpdate;
import ai.foresignal.stream.Touch;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.jdbc.autoconfigure.JdbcConnectionDetails;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * The single ingest writer (Core-Data-Model §6, Architecture §6.1): the only code that inserts
 * {@code record_version} and writes {@code record_head}, {@code record_live} and {@code seq_clock}
 * (invariant I1).
 *
 * <h2>Why one writer</h2>
 *
 * <p>One platform thread, {@code foresignal-ingest-writer}, takes commands from the
 * {@link IngestQueue} and processes them one at a time, each batch in one transaction. Because only
 * this thread allocates seqs, and it allocates them inside the transaction <em>after</em> every
 * earlier batch committed, <strong>commit order equals seq order</strong>: every seq of a batch is
 * greater than every seq of any batch committed before it (invariant I2). A reader that has seen
 * seq {@code s} committed will never later see a smaller seq appear. That is what lets
 * {@code /v1/changes?after=s}, the SSE stream and the trigger engine page by seq without ever
 * missing a version. In production the advisory lock ({@code foresignal.ingest.writer.exclusive})
 * keeps a second instance on the same database from breaking the rule.
 *
 * <p><strong>Seq is monotonic in commit order, not gap-free.</strong> {@code nextval} is not
 * transactional: a batch that rolls back (a transient error, a constraint failure) has still
 * consumed its seqs, and the next batch allocates above them ({@code lastAllocatedSeq} advances
 * immediately). Those gaps are expected and harmless; nothing may assume consecutive seqs.
 *
 * <h2>Lifecycle</h2>
 *
 * <ul>
 *   <li>Started on {@code ApplicationReadyEvent} at {@link StartupOrder#WRITER}; with
 *       {@code exclusive=true} it first takes the advisory lock on a dedicated connection, and
 *       refuses to start when another instance holds it.
 *   <li>{@link #resetState()} loads {@code lastSeq}, {@code lastIngestedAt} and
 *       {@code lastAllocatedSeq} from the database; the test cleaner calls it after truncating.
 *   <li>Clock guard ({@link ClockGuard}): when the clock is behind the last ingest time by more
 *       than the tolerance, batches are held ({@link WriterState#CLOCK_BEHIND}) and the clock is
 *       re-checked periodically; per batch, a wall/monotonic divergence counts a clock jump; a small
 *       backward step is absorbed by {@code ingestedAt = max(now, lastIngestedAt)} (invariant I3).
 *   <li>The loop catches everything; <strong>the thread never dies</strong>. Shutdown drains the
 *       queue for up to {@code shutdownDrain} and fails whatever is left.
 * </ul>
 *
 * <h2>One batch (Core-Data-Model §6.2, steps 1–15)</h2>
 *
 * <ol>
 *   <li>Pre-flight: make sure a partition covers every seq the batch could allocate.
 *   <li>Normalize (duplicates kept); 3. stable sort (the normalizer's).
 *   <li value="4">Recipe check and lazy migration, in separate transactions.
 *   <li>Begin; {@code SET LOCAL lock_timeout}; {@code ingestedAt = max(now, lastIngestedAt)}.
 *   <li>Load head ⋈ live with one query, repair missing live rows, fold ({@link BatchFold}).
 *   <li>Allocate {@code n} seqs, sorted, assigned in fold order.
 *   <li>Insert the {@code fetch_log} row → {@code batch_id}.
 *   <li>Insert versions; 10. upsert heads; 11. upsert live rows; 12. touch.
 *   <li value="13">Insert {@code seq_clock} (every batch).
 *   <li>Commit; advance {@code lastSeq} and {@code lastIngestedAt}.
 *   <li>Publish {@link ChangeBatch}, then {@link LiveBatch}, complete the future, record usage,
 *       notify {@link EntityKeyListener}s.
 * </ol>
 *
 * <p>SQLState {@code 08*}, {@code 40*}, {@code 55P03} and {@code 57014} roll back and retry steps
 * 5–14, re-folding from fresh state, with the configured backoff. Anything else fails only that
 * batch's future.
 */
@Component
public class IngestWriter implements IngestStateReset, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(IngestWriter.class);

    /** The writer thread's name. */
    public static final String THREAD_NAME = "foresignal-ingest-writer";

    /** Heads deleted per purge transaction. */
    static final int DEFAULT_PURGE_CHUNK = 10_000;

    /** Tombstones written per reconciliation turn (prompt 094). */
    static final int RECONCILE_CHUNK = 10_000;

    /** The {@code fetch_log.principal} of a reconciliation's tombstone batches. */
    static final String RECONCILE_PRINCIPAL = "reconcile";

    private static final int MAX_WARNINGS = 20;
    private static final Duration POLL = Duration.ofMillis(250);
    private static final Duration LOCK_RETRY_START = Duration.ofSeconds(1);

    private static final String SELECT_LAST_VERSION =
            "SELECT seq, ingested_at FROM record_version ORDER BY seq DESC LIMIT 1";
    private static final String SELECT_MAX_MINUTE = "SELECT max(minute) FROM seq_clock";
    private static final String SELECT_MAX_FETCH = "SELECT max(ingested_at) FROM fetch_log";
    private static final String SELECT_LAST_ALLOCATED =
            "SELECT CASE WHEN is_called THEN last_value ELSE last_value - 1 END FROM record_seq";
    private static final String ALLOCATE = "SELECT nextval('record_seq') FROM generate_series(1, ?)";

    private static final String INSERT_FETCH_LOG =
            """
            INSERT INTO fetch_log (source_id, trigger, status, started_at, received_at, ingested_at, http_status,
                                   request_count, url_redacted, etag, bytes, body_sha256, adapter_version, licence,
                                   principal, records_seen, rejected, late, skipped, versions_written, live_updates,
                                   touches, from_seq, to_seq, coverage, error, run_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?)
            RETURNING id
            """;

    private static final String INSERT_VERSION =
            """
            INSERT INTO record_version (seq, ingested_at, observed_at, received_at, event_time, valid_until,
                                        valid_until_explicit, prev_seq, batch_id, version, history_tier,
                                        persist_reason, qa_flags, tombstone, source_id, external_id, kind, geom,
                                        payload, quality, observer, valid_from)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ST_GeomFromWKB(?, 4326), ?::jsonb, ?::jsonb,
                    ?::jsonb, ?)
            """;

    private static final String UPSERT_HEAD =
            """
            INSERT INTO record_head (source_id, external_id, kind, seq, version, event_time, valid_until_explicit,
                                     valid_from, tombstone, static_hash, hash_recipe, persisted_observed_at,
                                     persisted_received_at, persisted_geom, payload, quality, search_text, title)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ST_GeomFromWKB(?, 4326), ?::jsonb, ?::jsonb, ?, ?)
            ON CONFLICT (source_id, external_id) DO UPDATE SET
                kind = EXCLUDED.kind, seq = EXCLUDED.seq, version = EXCLUDED.version,
                event_time = EXCLUDED.event_time, valid_until_explicit = EXCLUDED.valid_until_explicit,
                valid_from = EXCLUDED.valid_from, tombstone = EXCLUDED.tombstone,
                static_hash = EXCLUDED.static_hash, hash_recipe = EXCLUDED.hash_recipe,
                persisted_observed_at = EXCLUDED.persisted_observed_at,
                persisted_received_at = EXCLUDED.persisted_received_at,
                persisted_geom = EXCLUDED.persisted_geom, payload = EXCLUDED.payload, quality = EXCLUDED.quality,
                search_text = EXCLUDED.search_text, title = EXCLUDED.title
            """;

    private static final String UPSERT_LIVE =
            """
            INSERT INTO record_live (source_id, external_id, observed_at, last_received_at, valid_until, geom,
                                     full_hash, dirty, kinematic, quality, qa_flags)
            VALUES (?, ?, ?, ?, ?, ST_GeomFromWKB(?, 4326), ?, ?, ?::jsonb, ?::jsonb, ?)
            ON CONFLICT (source_id, external_id) DO UPDATE SET
                observed_at = EXCLUDED.observed_at, last_received_at = EXCLUDED.last_received_at,
                valid_until = EXCLUDED.valid_until, geom = EXCLUDED.geom, full_hash = EXCLUDED.full_hash,
                dirty = EXCLUDED.dirty, kinematic = EXCLUDED.kinematic, quality = EXCLUDED.quality,
                qa_flags = EXCLUDED.qa_flags
            """;

    private static final String TOUCH =
            """
            UPDATE record_live SET observed_at = ?, last_received_at = ?, valid_until = ?, qa_flags = ?
             WHERE source_id = ? AND external_id = ? AND observed_at <= ?
            """;

    private static final String INSERT_SEQ_CLOCK =
            "INSERT INTO seq_clock (minute, first_seq) VALUES (date_trunc('minute', ?::timestamptz), ?)"
                    + " ON CONFLICT DO NOTHING";

    private static final String SELECT_REPAIR =
            """
            SELECT h.external_id, h.kind, h.event_time, h.valid_until_explicit, h.valid_from, h.tombstone,
                   h.payload::text, ST_AsBinary(h.persisted_geom), h.quality::text,
                   h.persisted_observed_at, h.persisted_received_at
              FROM record_head h
             WHERE h.source_id = ? AND h.external_id = ANY(?)
            """;

    private static final String INSERT_REPAIRED_LIVE =
            """
            INSERT INTO record_live (source_id, external_id, observed_at, last_received_at, valid_until, geom,
                                     full_hash, dirty, kinematic, quality, qa_flags)
            VALUES (?, ?, ?, ?, ?, ST_GeomFromWKB(?, 4326), ?, false, ?::jsonb, ?::jsonb, 0)
            """;

    private static final String SELECT_PURGE_CANDIDATES =
            """
            SELECT h.external_id FROM record_head h JOIN record_live l USING (source_id, external_id)
             WHERE h.source_id = ? AND h.seq < ? AND l.last_received_at < ?
             LIMIT ?
            """;

    /** {@link #SELECT_PURGE_CANDIDATES} restricted to one kind ({@link PurgeSpec#kind()}). */
    private static final String SELECT_PURGE_CANDIDATES_OF_KIND =
            """
            SELECT h.external_id FROM record_head h JOIN record_live l USING (source_id, external_id)
             WHERE h.source_id = ? AND h.seq < ? AND l.last_received_at < ? AND h.kind = ?
             LIMIT ?
            """;

    private static final String DELETE_LIVE = "DELETE FROM record_live WHERE source_id = ? AND external_id = ANY(?)";
    private static final String DELETE_HEAD = "DELETE FROM record_head WHERE source_id = ? AND external_id = ANY(?)";

    /** Prompt 094: the records of a complete-set dataset the run did not see. */
    private static final String SELECT_RECONCILE_CANDIDATES =
            """
            SELECT h.external_id, h.kind FROM record_head h JOIN record_live l USING (source_id, external_id)
             WHERE h.source_id = ? AND h.kind = ANY(?) AND NOT h.tombstone AND l.last_received_at < ?
             LIMIT ?
            """;

    private static final String COUNT_RECONCILE_CANDIDATES =
            """
            SELECT count(*) FROM record_head h JOIN record_live l USING (source_id, external_id)
             WHERE h.source_id = ? AND h.kind = ANY(?) AND NOT h.tombstone AND l.last_received_at < ?
            """;

    private static final String COUNT_RECONCILE_TOTAL =
            """
            SELECT count(*) FROM record_head h JOIN record_live l USING (source_id, external_id)
             WHERE h.source_id = ? AND h.kind = ANY(?) AND NOT h.tombstone
            """;

    private static final String SELECT_PURGE_TARGET =
            """
            SELECT h.seq, h.version, h.kind, h.persisted_observed_at, l.observed_at,
                   ST_XMin(h.persisted_geom), ST_YMin(h.persisted_geom), ST_XMax(h.persisted_geom),
                   ST_YMax(h.persisted_geom), ST_XMin(l.geom), ST_YMin(l.geom), ST_XMax(l.geom), ST_YMax(l.geom)
              FROM record_head h LEFT JOIN record_live l USING (source_id, external_id)
             WHERE h.source_id = ? AND h.external_id = ?
               FOR UPDATE OF h
            """;

    private static final String INSERT_PURGE_FETCH_LOG =
            """
            INSERT INTO fetch_log (source_id, trigger, status, started_at, received_at, ingested_at, request_count,
                                   principal, records_seen, rejected, late, skipped, versions_written, live_updates,
                                   touches, from_seq, to_seq)
            VALUES (?, 'MANUAL', 'OK', ?, ?, ?, 0, 'purge', 0, 0, 0, 0, 1, 0, 0, ?, ?)
            RETURNING id
            """;

    private static final String INSERT_PURGE_TOMBSTONE =
            """
            INSERT INTO record_version (seq, ingested_at, observed_at, received_at, event_time, valid_until,
                                        valid_until_explicit, prev_seq, batch_id, version, history_tier,
                                        persist_reason, qa_flags, tombstone, source_id, external_id, kind, geom,
                                        payload, quality, observer, valid_from)
            VALUES (?, ?, ?, ?, NULL, NULL, NULL, ?, ?, ?, ?, ?, ?, true, ?, ?, ?, NULL, '{}'::jsonb, NULL, NULL, NULL)
            """;

    private static final String DELETE_PURGED_VERSIONS =
            "DELETE FROM record_version WHERE source_id = ? AND external_id = ? AND seq < ?";

    private static final String UPDATE_PURGED_HEAD =
            """
            UPDATE record_head SET seq = ?, version = ?, tombstone = true, event_time = NULL,
                   valid_until_explicit = NULL, valid_from = NULL, static_hash = 0, persisted_observed_at = ?,
                   persisted_received_at = ?, persisted_geom = NULL, payload = '{}'::jsonb, quality = NULL,
                   search_text = NULL, title = NULL
             WHERE source_id = ? AND external_id = ?
            """;

    private static final String UPSERT_PURGED_LIVE =
            """
            INSERT INTO record_live (source_id, external_id, observed_at, last_received_at, valid_until, geom,
                                     full_hash, dirty, kinematic, quality, qa_flags)
            VALUES (?, ?, ?, ?, ?, NULL, 0, false, '{}'::jsonb, NULL, ?)
            ON CONFLICT (source_id, external_id) DO UPDATE SET
                observed_at = EXCLUDED.observed_at, last_received_at = EXCLUDED.last_received_at,
                valid_until = EXCLUDED.valid_until, geom = NULL, full_hash = 0, dirty = false,
                kinematic = '{}'::jsonb, quality = NULL, qa_flags = EXCLUDED.qa_flags
            """;

    private final IngestQueue queue;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final PartitionManager partitions;
    private final SourceIngestProfiles profiles;
    private final PersistGate gate;
    private final ChangeBus changeBus;
    private final LiveBus liveBus;
    private final UsageMeter usage;
    private final ObjectProvider<EntityKeyListener> keyListeners;
    private final ObjectProvider<JdbcConnectionDetails> connectionDetails;
    private final Clock clock;
    private final MeterRegistry meters;
    private final IngestProperties.Writer settings;
    private final Normalizer normalizer;
    private final IngestJson json;
    private final ClockGuard clockGuard;
    private final RecipeMigrator migrator;

    private final ReentrantLock stateLock = new ReentrantLock();
    private final Map<String, Integer> migratedRecipes = new HashMap<>();
    private final Deque<String> warnings = new ArrayDeque<>();
    private final Map<CompletableFuture<PurgeOutcome>, Map<String, Integer>> purgeProgress = new IdentityHashMap<>();
    private final Map<CompletableFuture<TombstoneOutcome>, ReconcileProgress> reconcileProgress = new IdentityHashMap<>();
    private final AtomicBoolean ensureAheadRunning = new AtomicBoolean();

    private final Counter retries;
    private final Counter lockLost;
    private final Counter clockClamped;
    private final Counter liveRepaired;
    private final Counter purgeRequeued;
    private final Map<String, Timer> phaseTimers = new HashMap<>();

    private volatile WriterState state = WriterState.STOPPED;
    private volatile boolean running;
    private volatile boolean inFlight;
    private volatile long lastSeq;
    private volatile Instant lastIngestedAt;
    private volatile long lastAllocatedSeq;
    private int purgeChunk = DEFAULT_PURGE_CHUNK;
    private Thread thread;
    private IngestWriterLock lock;
    private long lastKeepaliveNanos;
    private long nextLockAttemptNanos;
    private Duration lockBackoff = LOCK_RETRY_START;
    private Instant previousBatchWall;
    private long previousBatchNanos;

    /** Creates the writer; it does nothing until {@link #start()}. */
    public IngestWriter(
            IngestQueue queue,
            JdbcTemplate jdbc,
            PlatformTransactionManager transactionManager,
            PartitionManager partitions,
            SourceIngestProfiles profiles,
            PersistGate gate,
            ChangeBus changeBus,
            LiveBus liveBus,
            UsageMeter usage,
            ObjectProvider<EntityKeyListener> keyListeners,
            ObjectProvider<EntityKeyExtractor> keyExtractor,
            ObjectProvider<JdbcConnectionDetails> connectionDetails,
            Clock clock,
            MeterRegistry meters,
            tools.jackson.databind.json.JsonMapper jsonMapper,
            IngestProperties properties) {
        this.queue = queue;
        this.jdbc = jdbc;
        this.tx = new TransactionTemplate(transactionManager);
        this.partitions = partitions;
        this.profiles = profiles;
        this.gate = gate;
        this.changeBus = changeBus;
        this.liveBus = liveBus;
        this.usage = usage;
        this.keyListeners = keyListeners;
        this.connectionDetails = connectionDetails;
        this.clock = clock;
        this.meters = meters;
        this.settings = properties.writer();
        this.normalizer = new Normalizer(keyExtractor.getIfAvailable(() -> DescriptorEntityKeyExtractor.INSTANCE));
        this.json = new IngestJson(jsonMapper);
        this.clockGuard = new ClockGuard(settings.clockTolerance());
        this.migrator = new RecipeMigrator(jdbc, tx, json);
        this.retries = meters.counter("foresignal.ingest.batch.retries");
        this.lockLost = meters.counter("foresignal.ingest.writer.lock_lost");
        this.clockClamped = meters.counter("foresignal.ingest.clock.clamped");
        this.liveRepaired = meters.counter("foresignal.ingest.repair.live_missing");
        this.purgeRequeued = meters.counter("foresignal.ingest.purge.requeued");
        for (String phase : List.of("normalize", "load", "fold", "write", "publish")) {
            phaseTimers.put(phase, Timer.builder("foresignal.ingest.batch.duration").tag("phase", phase).register(meters));
        }
        Gauge.builder("foresignal.ingest.writer.state", this, writer -> writer.state.code()).register(meters);
    }

    // ------------------------------------------------------------------ lifecycle

    /** Starts the writer once the application is ready, before the trigger engine and scheduler. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.WRITER)
    public void onApplicationReady() {
        start();
    }

    /**
     * Takes the lock (when exclusive), loads state and starts the writer thread. Idempotent.
     *
     * @throws IllegalStateException when another instance holds the writer lock
     */
    public synchronized void start() {
        if (thread != null) {
            return;
        }
        if (settings.exclusive()) {
            acquireLockAtStart();
        }
        running = true;
        state = WriterState.RUNNING;
        resetState();
        thread = WorkThreads.start(WorkRole.INGEST, THREAD_NAME, this::runLoop);
        log.info("Ingest writer started (exclusive={}, lastSeq={}, lastAllocatedSeq={})",
                settings.exclusive(), lastSeq, lastAllocatedSeq);
    }

    /**
     * Stops the writer: waits up to {@code shutdownDrain} for the queue to drain, stops the thread,
     * fails any command left in the queue and releases the lock.
     */
    public synchronized void stop() {
        if (thread == null) {
            closeLock();
            return;
        }
        long deadline = System.nanoTime() + settings.shutdownDrain().toNanos();
        while ((queue.depth() > 0 || inFlight) && state == WriterState.RUNNING && System.nanoTime() < deadline) {
            sleepQuietly(Duration.ofMillis(50));
        }
        running = false;
        thread.interrupt();
        try {
            thread.join(TimeUnit.SECONDS.toMillis(10));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        thread = null;
        for (IngestCommand left : queue.drain()) {
            IllegalStateException stopped = new IllegalStateException("The ingest writer stopped before processing this command");
            switch (left) {
                case IngestBatch batch -> batch.future().completeExceptionally(stopped);
                case HeadPurge purge -> purge.future().completeExceptionally(stopped);
                case EntityPurge purge -> purge.future().completeExceptionally(stopped);
                case TombstoneAbsent reconcile -> reconcile.future().completeExceptionally(stopped);
            }
        }
        state = WriterState.STOPPED;
        closeLock();
        log.info("Ingest writer stopped");
    }

    @Override
    public void destroy() {
        stop();
    }

    /**
     * Reloads the writer's state from the database (Core-Data-Model §6.1): {@code lastSeq} and
     * {@code lastIngestedAt} from the newest version, {@code lastIngestedAt} raised to the newest
     * {@code seq_clock} minute and {@code fetch_log.ingested_at}, {@code lastAllocatedSeq} from
     * {@code record_seq}; clears the migrated-recipe map, resets the {@link ChangeBus} watermark and
     * re-runs the clock guard. Serialized with batch processing.
     */
    @Override
    public void resetState() {
        stateLock.lock();
        try {
            List<Object[]> last =
                    jdbc.query(SELECT_LAST_VERSION, (rs, row) -> new Object[] {rs.getLong(1), JdbcValues.instant(rs, 2)});
            long seq = last.isEmpty() ? 0L : (Long) last.get(0)[0];
            Instant ingested = last.isEmpty() ? null : (Instant) last.get(0)[1];
            ingested = latest(ingested, jdbc.query(SELECT_MAX_MINUTE, (rs, row) -> JdbcValues.instant(rs, 1)).get(0));
            ingested = latest(ingested, jdbc.query(SELECT_MAX_FETCH, (rs, row) -> JdbcValues.instant(rs, 1)).get(0));
            Long allocated = jdbc.queryForObject(SELECT_LAST_ALLOCATED, Long.class);
            lastSeq = seq;
            lastIngestedAt = ingested;
            lastAllocatedSeq = Math.max(seq, allocated == null ? 0L : allocated);
            migratedRecipes.clear();
            previousBatchWall = null;
            changeBus.resetWatermark(seq);
            if (state == WriterState.RUNNING || state == WriterState.CLOCK_BEHIND) {
                applyClockGuard();
            }
        } finally {
            stateLock.unlock();
        }
    }

    /** The writer's state, for {@code /v1/system} (prompt 019). */
    public WriterStatus status() {
        List<String> current;
        synchronized (warnings) {
            current = List.copyOf(warnings);
        }
        return new WriterStatus(state, current, lastSeq, lastIngestedAt, queue.depth());
    }

    /** The highest seq allocated so far, including allocations of rolled-back batches. */
    public long lastAllocatedSeq() {
        return lastAllocatedSeq;
    }

    /** Sets the heads deleted per purge transaction; tests use a small chunk to exercise re-enqueue. */
    void setPurgeChunk(int chunk) {
        if (chunk < 1) {
            throw new IllegalArgumentException("chunk must be positive");
        }
        this.purgeChunk = chunk;
    }

    // ------------------------------------------------------------------ loop

    private void runLoop() {
        while (running) {
            try {
                if (state == WriterState.LOCK_LOST) {
                    tryReacquireLock();
                    continue;
                }
                if (state == WriterState.CLOCK_BEHIND) {
                    if (!recheckClock()) {
                        sleepWhileClockBehind(settings.clockRecheck());
                    }
                    continue;
                }
                keepaliveIfDue();
                if (state != WriterState.RUNNING) {
                    continue;
                }
                IngestCommand command = queue.poll(POLL);
                if (command == null) {
                    continue;
                }
                inFlight = true;
                stateLock.lock();
                try {
                    dispatch(command);
                } finally {
                    stateLock.unlock();
                    inFlight = false;
                }
            } catch (InterruptedException e) {
                // stop() interrupts; the loop condition decides.
            } catch (Throwable t) {
                log.error("Ingest writer loop error; continuing", t);
            }
        }
    }

    private void dispatch(IngestCommand command) {
        switch (command) {
            case IngestBatch batch -> processBatch(batch);
            case HeadPurge purge -> processPurge(purge);
            case EntityPurge purge -> processEntityPurge(purge);
            case TombstoneAbsent reconcile -> processTombstoneAbsent(reconcile);
        }
    }

    // ------------------------------------------------------------------ lock

    private void acquireLockAtStart() {
        lock = newLock();
        boolean acquired;
        try {
            acquired = lock.tryAcquire();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not open the ingest writer lock connection: " + e.getMessage(), e);
        }
        if (!acquired) {
            lock = null;
            throw new IllegalStateException(IngestWriterLock.HELD_MESSAGE);
        }
        lastKeepaliveNanos = System.nanoTime();
    }

    private IngestWriterLock newLock() {
        JdbcConnectionDetails details = connectionDetails.getIfAvailable();
        if (details == null) {
            throw new IllegalStateException("No JdbcConnectionDetails bean: cannot open the ingest writer lock connection");
        }
        return new IngestWriterLock(details.getJdbcUrl(), details.getUsername(), details.getPassword());
    }

    private void keepaliveIfDue() {
        if (!settings.exclusive() || lock == null) {
            return;
        }
        long now = System.nanoTime();
        if (now - lastKeepaliveNanos < settings.keepalive().toNanos()) {
            return;
        }
        lastKeepaliveNanos = now;
        if (!lock.keepalive()) {
            lockLost.increment();
            state = WriterState.LOCK_LOST;
            lockBackoff = LOCK_RETRY_START;
            nextLockAttemptNanos = now;
            warn("Lost the ingest writer lock connection; ingest is paused until it is reacquired");
        }
    }

    private void tryReacquireLock() {
        long now = System.nanoTime();
        if (now < nextLockAttemptNanos) {
            sleepWhileRunning(Duration.ofNanos(Math.min(nextLockAttemptNanos - now, POLL.toNanos())));
            return;
        }
        boolean acquired = false;
        try {
            acquired = lock.tryAcquire();
        } catch (SQLException e) {
            log.warn("Reacquiring the ingest writer lock failed: {}", e.getMessage());
        }
        if (acquired) {
            lastKeepaliveNanos = System.nanoTime();
            state = WriterState.RUNNING;
            log.info("Reacquired the ingest writer lock");
            resetState();
            return;
        }
        nextLockAttemptNanos = System.nanoTime() + lockBackoff.toNanos();
        Duration doubled = lockBackoff.multipliedBy(2);
        lockBackoff = doubled.compareTo(settings.lockRetryMax()) > 0 ? settings.lockRetryMax() : doubled;
    }

    private void closeLock() {
        if (lock != null) {
            lock.close();
            lock = null;
        }
    }

    // ------------------------------------------------------------------ clock

    private void applyClockGuard() {
        Instant now = clock.instant();
        if (clockGuard.isBehind(now, lastIngestedAt)) {
            if (state != WriterState.CLOCK_BEHIND) {
                warn("The clock (" + now + ") is more than " + settings.clockTolerance()
                        + " behind the last ingest time (" + lastIngestedAt + "); holding ingest until it catches up");
            }
            state = WriterState.CLOCK_BEHIND;
        } else if (state == WriterState.CLOCK_BEHIND) {
            state = WriterState.RUNNING;
            log.info("The clock caught up with the last ingest time; ingest resumes");
        }
    }

    private boolean recheckClock() {
        stateLock.lock();
        try {
            applyClockGuard();
            return state != WriterState.CLOCK_BEHIND;
        } finally {
            stateLock.unlock();
        }
    }

    private void checkClockJump(Instant now) {
        long nanos = System.nanoTime();
        if (previousBatchWall != null) {
            clockGuard
                    .jump(Duration.between(previousBatchWall, now), nanos - previousBatchNanos)
                    .ifPresent(direction -> {
                        meters.counter("foresignal.ingest.clock.jump", "direction", direction.tag()).increment();
                        warn("The wall clock jumped " + direction.tag() + " relative to monotonic time between batches");
                    });
        }
        previousBatchWall = now;
        previousBatchNanos = nanos;
    }

    // ------------------------------------------------------------------ batches

    private void processBatch(IngestBatch batch) {
        try {
            Committed committed = runBatch(batch);
            publish(batch, committed);
        } catch (Throwable t) {
            log.error("Ingest batch for source {} failed: {}", batch.sourceId(), t.toString(), t);
            batch.future().completeExceptionally(t);
        }
    }

    private Committed runBatch(IngestBatch batch) {
        String sourceId = batch.sourceId();
        SourceIngestProfile profile = profiles.get(sourceId);
        GateSnapshot snapshot = Objects.requireNonNullElse(gate.snapshot(), GateSnapshot.EMPTY);
        checkClockJump(clock.instant());

        // Step 1: pre-flight on the seqs this batch could allocate.
        preflight(profile.tier(), Math.max(1, batch.records().size()));

        // Steps 2-3: normalize and sort (keeps duplicates).
        NormalizedBatch normalized = time("normalize", () -> normalizer.normalize(profile, batch.records()));
        List<Observation> observations = normalized.observations().stream().map(IngestWriter::toMicros).toList();
        if (normalized.validUntilIgnored() > 0) {
            meters.counter("foresignal.ingest.records.valid_until_ignored", "source", sourceId)
                    .increment(normalized.validUntilIgnored());
        }

        // Step 4: lazy recipe migration, in its own transactions.
        Integer migrated = migratedRecipes.get(sourceId);
        if (migrated == null || migrated != profile.recipe()) {
            if (migrator.needed(profile)) {
                int count = migrator.migrate(profile);
                log.info("Migrated {} head(s) of {} to hash recipe {}", count, sourceId, profile.recipe());
            }
            migratedRecipes.put(sourceId, profile.recipe());
        }

        // Steps 5-14, retried as a whole on transient errors.
        List<Duration> backoff = settings.retryBackoff();
        int attempt = 0;
        while (true) {
            try {
                Committed committed = tx.execute(status -> writeBatch(batch, profile, snapshot, normalized, observations));
                if (committed == null) {
                    throw new IllegalStateException("The batch transaction returned nothing");
                }
                // Step 14: advance state after commit.
                if (committed.toSeq > 0) {
                    lastSeq = Math.max(lastSeq, committed.toSeq);
                }
                lastIngestedAt = latest(lastIngestedAt, committed.ingestedAt);
                return committed;
            } catch (RuntimeException e) {
                String sqlState = sqlState(e);
                if (attempt < backoff.size() && isRetryable(sqlState, e)) {
                    retries.increment();
                    log.warn("Ingest batch for {} failed with SQLState {}; retry {} of {} in {}",
                            sourceId, sqlState, attempt + 1, backoff.size(), backoff.get(attempt));
                    if (isMissingPartition(sqlState, e)) {
                        partitions.refresh();
                    }
                    sleepQuietly(backoff.get(attempt));
                    attempt++;
                    continue;
                }
                throw e;
            }
        }
    }

    private Committed writeBatch(
            IngestBatch batch,
            SourceIngestProfile profile,
            GateSnapshot snapshot,
            NormalizedBatch normalized,
            List<Observation> observations) {
        String sourceId = batch.sourceId();
        jdbc.queryForObject(
                "SELECT set_config('lock_timeout', ?, true)", String.class, settings.lockTimeout().toMillis() + "ms");

        // Step 5.
        Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);
        if (lastIngestedAt != null && now.isBefore(lastIngestedAt)) {
            clockClamped.increment();
        }
        Instant ingestedAt = ClockGuard.ingestedAt(now, lastIngestedAt);

        // Step 6: load and fold.
        Set<String> ids = new LinkedHashSet<>();
        observations.forEach(o -> ids.add(o.externalId()));
        Map<String, EntityState> loaded = time("load", () -> load(profile, snapshot, ids));
        BatchFold.Result fold =
                time("fold", () -> BatchFold.fold(profile, snapshot, observations, loaded, batch.forceTouch()));

        long writeStart = System.nanoTime();
        // Step 7: allocate.
        int n = fold.persisted();
        long seqClockFirst = lastAllocatedSeq + 1;
        long[] allocated = new long[n];
        if (n > 0) {
            List<Long> seqs = jdbc.queryForList(ALLOCATE, Long.class, n);
            for (int i = 0; i < n; i++) {
                allocated[i] = seqs.get(i);
            }
            Arrays.sort(allocated);
            lastAllocatedSeq = Math.max(lastAllocatedSeq, allocated[n - 1]);
            ensureCovered(profile.tier(), allocated[0], allocated[n - 1]);
        }
        long fromSeq = n > 0 ? allocated[0] : 0;
        long toSeq = n > 0 ? allocated[n - 1] : 0;

        // Step 8.
        long batchId = insertFetchLog(batch, ingestedAt, normalized, fold, fromSeq, toSeq);

        // Step 9: versions.
        List<ChangeSummary> changes = new ArrayList<>(n);
        long versionBytes = insertVersions(sourceId, profile, fold, allocated, batchId, ingestedAt, changes);

        // Step 10: heads.
        List<BatchFold.EntityPlan> persistedEntities =
                fold.entities.values().stream().filter(p -> p.persisted).toList();
        assertUniqueKeys(persistedEntities);
        upsertHeads(sourceId, profile, persistedEntities, allocated);

        // Step 11: live rows.
        List<BatchFold.EntityPlan> liveEntities =
                fold.entities.values().stream().filter(BatchFold.EntityPlan::needsLiveUpsert).toList();
        assertUniqueKeys(liveEntities);
        upsertLive(sourceId, liveEntities);

        // Step 12: touches.
        List<BatchFold.EntityPlan> touchEntities =
                fold.entities.values().stream().filter(BatchFold.EntityPlan::touchOnly).toList();
        touch(sourceId, touchEntities);

        // Step 13.
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(INSERT_SEQ_CLOCK);
            JdbcValues.setInstant(ps, 1, ingestedAt);
            ps.setLong(2, seqClockFirst);
            return ps;
        });
        phaseTimers.get("write").record(System.nanoTime() - writeStart, TimeUnit.NANOSECONDS);

        LiveBatch liveBatch = liveBatch(sourceId, fold);
        IngestOutcome outcome =
                new IngestOutcome(
                        batch.records().size(),
                        n,
                        fold.live,
                        fold.touches,
                        fold.throttled,
                        fold.late,
                        fold.skipped,
                        normalized.rejections().size(),
                        normalized.clamped(),
                        fromSeq,
                        toSeq,
                        fold.byReason,
                        versionBytes,
                        normalized.rejections());
        ChangeBatch changeBatch = n > 0 ? new ChangeBatch(sourceId, batchId, ingestedAt, fromSeq, toSeq, changes) : null;
        return new Committed(batchId, ingestedAt, fromSeq, toSeq, outcome, changeBatch, liveBatch, persistedEntities, normalized);
    }

    private void publish(IngestBatch batch, Committed committed) {
        long start = System.nanoTime();
        String sourceId = batch.sourceId();
        // Step 15: change before live, then the future, then usage and entity keys.
        if (committed.changes != null) {
            changeBus.publish(committed.changes);
        }
        if (!committed.live.isEmpty()) {
            liveBus.publish(committed.live);
        }
        batch.future().complete(committed.outcome);
        IngestOutcome o = committed.outcome;
        usage.recordIngest(
                sourceId,
                new IngestCounts(
                        o.seen(), o.persisted(), o.live(), o.touched(), o.late(), o.skipped(), o.rejected(),
                        o.versionBytes(), o.clamped(), o.persisted(PersistReason.TRIGGER_EDGE)));
        countRecords(sourceId, o);
        for (BatchFold.EntityPlan plan : committed.persistedEntities) {
            Map<String, String> keys = plan.lastPersisted.observation().entityKeys();
            if (keys.isEmpty()) {
                continue;
            }
            keyListeners.orderedStream().forEach(listener -> {
                try {
                    listener.onEntityKeys(sourceId, plan.externalId, keys);
                } catch (RuntimeException e) {
                    log.warn("Entity key listener {} failed for {}/{}", listener.getClass().getName(), sourceId, plan.externalId, e);
                }
            });
        }
        phaseTimers.get("publish").record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
    }

    private void countRecords(String sourceId, IngestOutcome o) {
        increment("foresignal.ingest.records.seen", sourceId, o.seen());
        increment("foresignal.ingest.records.persisted", sourceId, o.persisted());
        increment("foresignal.ingest.records.live", sourceId, o.live());
        increment("foresignal.ingest.records.touched", sourceId, o.touched());
        increment("foresignal.ingest.records.touches_throttled", sourceId, o.touchesThrottled());
        increment("foresignal.ingest.records.late", sourceId, o.late());
        increment("foresignal.ingest.records.skipped", sourceId, o.skipped());
        increment("foresignal.ingest.records.clamped", sourceId, o.clamped());
        increment("foresignal.ingest.records.rejected", sourceId, o.rejected());
        o.byReason().forEach((reason, count) -> {
            if (count > 0) {
                meters.counter("foresignal.ingest.persist", "source", sourceId, "reason", reason.name()).increment(count);
            }
        });
    }

    private void increment(String name, String sourceId, long amount) {
        Counter counter = meters.counter(name, "source", sourceId);
        if (amount > 0) {
            counter.increment(amount);
        }
    }

    // ------------------------------------------------------------------ step 1

    private void preflight(HistoryTier tier, int maxVersions) {
        long from = lastAllocatedSeq + 1;
        long to = lastAllocatedSeq + maxVersions;
        if (partitions.coveredUpTo(tier, from) <= to) {
            partitions.ensureRange(tier, from, to);
            if (partitions.coveredUpTo(tier, from) <= to) {
                partitions.refresh();
                if (partitions.coveredUpTo(tier, from) <= to) {
                    throw new IllegalStateException(
                            "No record_version partition covers seq " + from + ".." + to + " of tier " + tier);
                }
            }
        }
        long headroom = partitions.coveredUpTo(tier, from) - to;
        if (headroom < partitions.block(tier) && ensureAheadRunning.compareAndSet(false, true)) {
            warn("record_version " + tier + " headroom is " + headroom + " seq, under one block; ensuring partitions ahead");
            WorkThreads.start(WorkRole.INGEST, "foresignal-ensure-ahead", () -> {
                try {
                    partitions.ensureAhead();
                } catch (RuntimeException e) {
                    log.warn("Asynchronous partition ensureAhead failed", e);
                } finally {
                    ensureAheadRunning.set(false);
                }
            });
        }
    }

    private void ensureCovered(HistoryTier tier, long from, long to) {
        if (partitions.coveredUpTo(tier, from) > to) {
            return;
        }
        partitions.ensureRange(tier, from, to);
        if (partitions.coveredUpTo(tier, from) <= to) {
            throw new IllegalStateException("No record_version partition covers allocated seq " + from + ".." + to);
        }
    }

    // ------------------------------------------------------------------ step 6

    private Map<String, EntityState> load(SourceIngestProfile profile, GateSnapshot snapshot, Set<String> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<String> paths = new ArrayList<>(profile.loadPaths(snapshot));
        boolean track = profile.isTrack();
        boolean headGeometry = profile.needsPersistedGeometry(snapshot);
        StringBuilder sql = new StringBuilder(
                """
                SELECT h.external_id, h.seq, h.version, h.tombstone, h.static_hash,
                       h.persisted_observed_at, h.persisted_received_at,
                       ST_X(ST_Centroid(h.persisted_geom)), ST_Y(ST_Centroid(h.persisted_geom)),
                       ST_XMin(h.persisted_geom), ST_YMin(h.persisted_geom), ST_XMax(h.persisted_geom), ST_YMax(h.persisted_geom),
                       l.external_id IS NOT NULL,
                       l.observed_at, l.last_received_at, l.valid_until, l.full_hash, l.dirty, l.qa_flags, l.kinematic::text,
                       ST_XMin(l.geom), ST_YMin(l.geom), ST_XMax(l.geom), ST_YMax(l.geom)""");
        if (track) {
            sql.append(", ST_AsBinary(l.geom)");
        }
        for (int i = 0; i < paths.size(); i++) {
            sql.append(", (h.payload #> ?::text[])::text");
        }
        if (headGeometry) {
            sql.append(", ST_AsBinary(h.persisted_geom)");
        }
        sql.append(
                """

                  FROM record_head h LEFT JOIN record_live l USING (source_id, external_id)
                 WHERE h.source_id = ? AND h.external_id = ANY(?)""");
        Map<String, EntityState> states = new HashMap<>();
        List<String> missingLive = new ArrayList<>();
        jdbc.query(
                con -> {
                    PreparedStatement ps = con.prepareStatement(sql.toString());
                    int index = 1;
                    for (String path : paths) {
                        ps.setArray(index++, con.createArrayOf("text", path.split("\\.")));
                    }
                    ps.setString(index++, profile.sourceId());
                    ps.setArray(index, con.createArrayOf("text", ids.toArray()));
                    return ps;
                },
                rs -> {
                    String externalId = rs.getString(1);
                    int column = 26;
                    Geometry liveGeometry = null;
                    if (track) {
                        liveGeometry = IngestJson.geometry(rs.getBytes(column++));
                    }
                    Map<String, Object> pathValues = new HashMap<>();
                    for (String path : paths) {
                        pathValues.put(path, json.readValue(rs.getString(column++)));
                    }
                    Geometry headGeom = headGeometry ? IngestJson.geometry(rs.getBytes(column)) : null;
                    Double cx = JdbcValues.nullableDouble(rs, 8);
                    Double cy = JdbcValues.nullableDouble(rs, 9);
                    EntityState.Head head =
                            new EntityState.Head(
                                    true,
                                    rs.getLong(2),
                                    rs.getInt(3),
                                    rs.getBoolean(4),
                                    rs.getLong(5),
                                    JdbcValues.instant(rs, 6),
                                    JdbcValues.instant(rs, 7),
                                    cx == null || cy == null ? null : new Coordinate(cx, cy),
                                    envelope(rs, 10),
                                    pathValues,
                                    headGeom,
                                    null);
                    if (!rs.getBoolean(14)) {
                        missingLive.add(externalId);
                        states.put(externalId, new EntityState(head, null));
                        return;
                    }
                    EntityState.Live live =
                            new EntityState.Live(
                                    JdbcValues.instant(rs, 15),
                                    JdbcValues.instant(rs, 16),
                                    JdbcValues.instant(rs, 17),
                                    rs.getLong(18),
                                    rs.getBoolean(19),
                                    json.readKeepingNulls(rs.getString(21)),
                                    liveGeometry,
                                    envelope(rs, 22),
                                    rs.getInt(20));
                    states.put(externalId, new EntityState(head, live));
                });
        if (!missingLive.isEmpty()) {
            repairLive(profile, missingLive, states);
        }
        return states;
    }

    /** Rebuilds missing live rows from their heads (invariant I10) and writes them in this transaction. */
    private void repairLive(SourceIngestProfile profile, List<String> ids, Map<String, EntityState> states) {
        List<Object[]> rows = new ArrayList<>();
        jdbc.query(
                con -> {
                    PreparedStatement ps = con.prepareStatement(SELECT_REPAIR);
                    ps.setString(1, profile.sourceId());
                    ps.setArray(2, con.createArrayOf("text", ids.toArray()));
                    return ps;
                },
                rs -> {
                    String externalId = rs.getString(1);
                    String kind = rs.getString(2);
                    Instant eventTime = JdbcValues.instant(rs, 3);
                    Instant validUntilExplicit = JdbcValues.instant(rs, 4);
                    Instant validFrom = JdbcValues.instant(rs, 5);
                    boolean tombstone = rs.getBoolean(6);
                    Map<String, Object> payload = json.readPayload(rs.getString(7));
                    Geometry geometry = IngestJson.geometry(rs.getBytes(8));
                    String quality = rs.getString(9);
                    Instant observedAt = JdbcValues.instant(rs, 10);
                    Instant receivedAt = JdbcValues.instant(rs, 11);
                    Instant validUntil = validUntilExplicit;
                    if (validUntil == null && profile.ttl() != null) {
                        validUntil = profile.anchor(observedAt, receivedAt).plus(profile.ttl());
                    }
                    long fullHash =
                            ContentHasher.fullHash(
                                    kind, eventTime, validUntilExplicit, validFrom, tombstone, geometry, payload,
                                    profile.volatileFields());
                    Map<String, Object> kinematic = tombstone ? Map.of() : ContentHasher.kinematicSubset(payload, profile);
                    rows.add(new Object[] {externalId, observedAt, receivedAt, validUntil, geometry, fullHash, kinematic, quality});
                    EntityState current = states.get(externalId);
                    EntityState.Live live =
                            new EntityState.Live(
                                    observedAt, receivedAt, validUntil, fullHash, false, kinematic,
                                    profile.isTrack() ? geometry : null,
                                    geometry == null ? null : geometry.getEnvelopeInternal(), 0);
                    states.put(externalId, new EntityState(current.head(), live));
                });
        jdbc.batchUpdate(INSERT_REPAIRED_LIVE, rows, rows.size(), (ps, row) -> {
            ps.setString(1, profile.sourceId());
            ps.setString(2, (String) row[0]);
            JdbcValues.setInstant(ps, 3, (Instant) row[1]);
            JdbcValues.setInstant(ps, 4, (Instant) row[2]);
            JdbcValues.setInstant(ps, 5, (Instant) row[3]);
            JdbcValues.setBytes(ps, 6, IngestJson.wkb((Geometry) row[4]));
            ps.setLong(7, (Long) row[5]);
            ps.setString(8, json.write(row[6]));
            JdbcValues.setString(ps, 9, (String) row[7]);
        });
        liveRepaired.increment(rows.size());
        log.warn("Repaired {} missing live row(s) of {}", rows.size(), profile.sourceId());
    }

    private static Envelope envelope(ResultSet rs, int first) throws SQLException {
        Double minX = JdbcValues.nullableDouble(rs, first);
        Double minY = JdbcValues.nullableDouble(rs, first + 1);
        Double maxX = JdbcValues.nullableDouble(rs, first + 2);
        Double maxY = JdbcValues.nullableDouble(rs, first + 3);
        if (minX == null || minY == null || maxX == null || maxY == null) {
            return null;
        }
        return new Envelope(minX, maxX, minY, maxY);
    }

    // ------------------------------------------------------------------ steps 8-12

    private long insertFetchLog(
            IngestBatch batch, Instant ingestedAt, NormalizedBatch normalized, BatchFold.Result fold, long fromSeq, long toSeq) {
        FetchLogEntry entry = batch.log();
        if (entry == null) {
            throw new IllegalArgumentException("An ingest batch needs a FetchLogEntry");
        }
        CoverageSample coverage = CoverageSample.from(entry.coverage());
        List<Long> ids = jdbc.query(
                con -> {
                    PreparedStatement ps = con.prepareStatement(INSERT_FETCH_LOG);
                    ps.setString(1, batch.sourceId());
                    JdbcValues.setString(ps, 2, entry.trigger() == null ? null : entry.trigger().name());
                    JdbcValues.setString(ps, 3, entry.status() == null ? null : entry.status().name());
                    JdbcValues.setInstant(ps, 4, entry.startedAt());
                    JdbcValues.setInstant(ps, 5, entry.receivedAt());
                    JdbcValues.setInstant(ps, 6, ingestedAt);
                    JdbcValues.setInteger(ps, 7, entry.httpStatus());
                    ps.setInt(8, entry.requestCount());
                    JdbcValues.setString(ps, 9, entry.urlRedacted());
                    JdbcValues.setString(ps, 10, entry.etag());
                    JdbcValues.setLong(ps, 11, entry.bytes());
                    JdbcValues.setString(ps, 12, entry.bodySha256());
                    JdbcValues.setString(ps, 13, entry.adapterVersion());
                    JdbcValues.setString(ps, 14, entry.licence());
                    JdbcValues.setString(ps, 15, entry.principal());
                    ps.setInt(16, batch.records().size());
                    ps.setInt(17, normalized.rejections().size());
                    ps.setInt(18, fold.late);
                    ps.setInt(19, fold.skipped);
                    ps.setInt(20, fold.persisted());
                    ps.setInt(21, fold.live);
                    ps.setInt(22, fold.touches);
                    JdbcValues.setLong(ps, 23, fold.persisted() > 0 ? fromSeq : null);
                    JdbcValues.setLong(ps, 24, fold.persisted() > 0 ? toSeq : null);
                    JdbcValues.setString(ps, 25, coverage == null ? null : json.write(coverage));
                    JdbcValues.setString(ps, 26, entry.error());
                    JdbcValues.setString(ps, 27, entry.runId());
                    return ps;
                },
                (rs, row) -> rs.getLong(1));
        return ids.get(0);
    }

    private long insertVersions(
            String sourceId,
            SourceIngestProfile profile,
            BatchFold.Result fold,
            long[] allocated,
            long batchId,
            Instant ingestedAt,
            List<ChangeSummary> changes) {
        if (fold.versions.isEmpty()) {
            return 0;
        }
        long bytes = 0;
        List<Object[]> encoded = new ArrayList<>(fold.versions.size());
        for (BatchFold.PlannedVersion v : fold.versions) {
            Observation o = v.observation();
            String payload = json.write(o.tombstone() ? Map.of() : o.payload());
            String quality = o.quality().isEmpty() ? null : json.write(o.quality());
            String observer = o.observers().isEmpty() ? null : json.write(o.observers());
            bytes += IngestJson.utf8Length(payload) + IngestJson.utf8Length(quality) + IngestJson.utf8Length(observer) + 120;
            long seq = allocated[v.index()];
            Long prevSeq = v.prevSeq() == null ? null : BatchFold.Result.resolve(v.prevSeq(), allocated);
            Geometry geometry = o.tombstone() ? null : o.geometry();
            encoded.add(new Object[] {v, seq, prevSeq, payload, quality, observer, IngestJson.wkb(geometry)});
            changes.add(
                    new ChangeSummary(
                            seq,
                            prevSeq,
                            profile.tier(),
                            sourceId,
                            o.externalId(),
                            o.kind(),
                            v.version(),
                            v.reason(),
                            o.observedAt(),
                            o.eventTime(),
                            v.validUntil(),
                            o.tombstone(),
                            o.qaFlags(),
                            Bbox.of(geometry),
                            Bbox.of(v.prevPersistedBbox()),
                            Bbox.of(v.prevLiveBbox())));
        }
        jdbc.batchUpdate(INSERT_VERSION, encoded, encoded.size(), (ps, row) -> {
            BatchFold.PlannedVersion v = (BatchFold.PlannedVersion) row[0];
            Observation o = v.observation();
            ps.setLong(1, (Long) row[1]);
            JdbcValues.setInstant(ps, 2, ingestedAt);
            JdbcValues.setInstant(ps, 3, o.observedAt());
            JdbcValues.setInstant(ps, 4, o.receivedAt());
            JdbcValues.setInstant(ps, 5, o.eventTime());
            JdbcValues.setInstant(ps, 6, v.validUntil());
            JdbcValues.setInstant(ps, 7, o.validUntilExplicit());
            JdbcValues.setLong(ps, 8, (Long) row[2]);
            ps.setLong(9, batchId);
            ps.setInt(10, v.version());
            ps.setShort(11, profile.tier().code());
            ps.setShort(12, v.reason().code());
            ps.setShort(13, (short) o.qaFlags());
            ps.setBoolean(14, o.tombstone());
            ps.setString(15, sourceId);
            ps.setString(16, o.externalId());
            ps.setString(17, o.kind());
            JdbcValues.setBytes(ps, 18, (byte[]) row[6]);
            ps.setString(19, (String) row[3]);
            JdbcValues.setString(ps, 20, (String) row[4]);
            JdbcValues.setString(ps, 21, (String) row[5]);
            JdbcValues.setInstant(ps, 22, o.validFrom());
        });
        return bytes;
    }

    private void upsertHeads(
            String sourceId, SourceIngestProfile profile, List<BatchFold.EntityPlan> entities, long[] allocated) {
        if (entities.isEmpty()) {
            return;
        }
        jdbc.batchUpdate(UPSERT_HEAD, entities, entities.size(), (ps, plan) -> {
            EntityState.Head head = plan.state.head();
            Observation o = plan.lastPersisted.observation();
            ps.setString(1, sourceId);
            ps.setString(2, plan.externalId);
            ps.setString(3, o.kind());
            ps.setLong(4, BatchFold.Result.resolve(head.seq(), allocated));
            ps.setInt(5, head.version());
            JdbcValues.setInstant(ps, 6, o.eventTime());
            JdbcValues.setInstant(ps, 7, o.validUntilExplicit());
            JdbcValues.setInstant(ps, 8, o.validFrom());
            ps.setBoolean(9, head.tombstone());
            ps.setLong(10, head.staticHash());
            ps.setInt(11, profile.recipe());
            JdbcValues.setInstant(ps, 12, head.persistedObservedAt());
            JdbcValues.setInstant(ps, 13, head.persistedReceivedAt());
            JdbcValues.setBytes(ps, 14, IngestJson.wkb(head.persistedGeometry()));
            ps.setString(15, json.write(head.persistedPayload() == null ? Map.of() : head.persistedPayload()));
            JdbcValues.setString(ps, 16, o.quality().isEmpty() ? null : json.write(o.quality()));
            JdbcValues.setString(ps, 17, o.searchText());
            JdbcValues.setString(ps, 18, o.title());
        });
    }

    private void upsertLive(String sourceId, List<BatchFold.EntityPlan> entities) {
        if (entities.isEmpty()) {
            return;
        }
        jdbc.batchUpdate(UPSERT_LIVE, entities, entities.size(), (ps, plan) -> {
            EntityState.Live live = plan.state.live();
            Observation content = plan.lastContent.observation();
            ps.setString(1, sourceId);
            ps.setString(2, plan.externalId);
            JdbcValues.setInstant(ps, 3, live.observedAt());
            JdbcValues.setInstant(ps, 4, latest(plan.maxReceivedNonLate, live.lastReceivedAt()));
            JdbcValues.setInstant(ps, 5, live.validUntil());
            JdbcValues.setBytes(ps, 6, IngestJson.wkb(live.geometry()));
            ps.setLong(7, live.fullHash());
            ps.setBoolean(8, live.dirty());
            ps.setString(9, json.write(live.kinematic()));
            JdbcValues.setString(ps, 10, content.quality().isEmpty() ? null : json.write(content.quality()));
            ps.setShort(11, (short) content.qaFlags());
        });
    }

    private void touch(String sourceId, List<BatchFold.EntityPlan> entities) {
        if (entities.isEmpty()) {
            return;
        }
        int[][] counts = jdbc.batchUpdate(TOUCH, entities, entities.size(), (ps, plan) -> {
            EntityState.Live live = plan.state.live();
            JdbcValues.setInstant(ps, 1, live.observedAt());
            JdbcValues.setInstant(ps, 2, live.lastReceivedAt());
            JdbcValues.setInstant(ps, 3, live.validUntil());
            ps.setShort(4, (short) live.qaFlags());
            ps.setString(5, sourceId);
            ps.setString(6, plan.externalId);
            JdbcValues.setInstant(ps, 7, live.observedAt());
        });
        long updated = Arrays.stream(counts).flatMapToInt(Arrays::stream).asLongStream().sum();
        if (updated != entities.size()) {
            throw new IllegalStateException(
                    "Touch updated " + updated + " live row(s) of " + sourceId + " but expected " + entities.size());
        }
    }

    private static void assertUniqueKeys(List<BatchFold.EntityPlan> entities) {
        Set<String> seen = new HashSet<>();
        for (BatchFold.EntityPlan plan : entities) {
            if (!seen.add(plan.externalId)) {
                throw new IllegalStateException("Duplicate entity " + plan.externalId + " in one upsert batch");
            }
        }
    }

    private LiveBatch liveBatch(String sourceId, BatchFold.Result fold) {
        List<LiveUpdate> updates = new ArrayList<>();
        List<Touch> touches = new ArrayList<>();
        for (BatchFold.EntityPlan plan : fold.entities.values()) {
            EntityState.Live live = plan.state.live();
            if (live == null) {
                continue;
            }
            if (plan.endsLive) {
                Observation content = plan.lastContent.observation();
                EntityState.Live before = plan.initial.live();
                updates.add(
                        new LiveUpdate(
                                sourceId,
                                plan.externalId,
                                content.kind(),
                                live.observedAt(),
                                live.validUntil(),
                                plan.state.head().version(),
                                live.geometry(),
                                Bbox.of(live.bbox()),
                                before == null ? null : Bbox.of(before.bbox()),
                                new LinkedHashMap<>(live.kinematic()),
                                content.quality().isEmpty() ? null : json.readMap(json.write(content.quality()))));
            } else if (plan.touches > 0) {
                touches.add(new Touch(sourceId, plan.externalId, live.observedAt(), live.validUntil(), Bbox.of(live.bbox())));
            }
        }
        return new LiveBatch(sourceId, updates, touches);
    }

    // ------------------------------------------------------------------ head purge

    private void processPurge(HeadPurge purge) {
        CompletableFuture<PurgeOutcome> future = purge.future();
        try {
            List<PurgeSpec> remaining = new ArrayList<>(purge.specs());
            Map<String, Integer> totals = purgeProgress.computeIfAbsent(future, f -> new LinkedHashMap<>());
            while (!remaining.isEmpty()) {
                PurgeSpec spec = remaining.get(0);
                int deleted = purgeChunk(spec);
                totals.merge(spec.sourceId(), deleted, Integer::sum);
                if (deleted < purgeChunk) {
                    remaining.remove(0);
                }
                if (remaining.isEmpty()) {
                    break;
                }
                HeadPurge rest = new HeadPurge(remaining, future);
                if (queue.offer(rest)) {
                    purgeRequeued.increment();
                    return;
                }
                // The queue is full: continue inline rather than block the writer on its own queue.
            }
            purgeProgress.remove(future);
            // Read on the writer thread, so no batch can publish between the last delete and this.
            future.complete(new PurgeOutcome(totals, changeBus.lastPublishedSeq()));
        } catch (Throwable t) {
            purgeProgress.remove(future);
            log.error("Head purge failed", t);
            future.completeExceptionally(t);
        }
    }

    private int purgeChunk(PurgeSpec spec) {
        Integer deleted = tx.execute(status -> {
            List<String> ids = jdbc.query(
                    con -> {
                        boolean ofKind = spec.kind() != null;
                        PreparedStatement ps =
                                con.prepareStatement(ofKind ? SELECT_PURGE_CANDIDATES_OF_KIND : SELECT_PURGE_CANDIDATES);
                        ps.setString(1, spec.sourceId());
                        ps.setLong(2, spec.floorSeq());
                        JdbcValues.setInstant(ps, 3, spec.staleBefore());
                        if (ofKind) {
                            ps.setString(4, spec.kind());
                            ps.setInt(5, purgeChunk);
                        } else {
                            ps.setInt(4, purgeChunk);
                        }
                        return ps;
                    },
                    (rs, row) -> rs.getString(1));
            if (ids.isEmpty()) {
                return 0;
            }
            for (String sql : List.of(DELETE_LIVE, DELETE_HEAD)) {
                jdbc.update(con -> {
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, spec.sourceId());
                    Array array = con.createArrayOf("text", ids.toArray());
                    ps.setArray(2, array);
                    return ps;
                });
            }
            return ids.size();
        });
        return deleted == null ? 0 : deleted;
    }

    // ------------------------------------------------------------------ complete-set reconciliation (094)

    /**
     * Tombstones the records a complete-set reference dataset no longer lists (prompt 094), in chunks of
     * {@value #RECONCILE_CHUNK}, re-enqueuing itself at the queue tail so one writer turn stays short. The
     * shrink guard refuses a run that would remove more than the descriptor's {@code reconcile.maxAbsentRatio}
     * of the dataset, unless the operator passed {@code allowShrink}.
     */
    private void processTombstoneAbsent(TombstoneAbsent reconcile) {
        CompletableFuture<TombstoneOutcome> future = reconcile.future();
        try {
            ReconcileProgress progress = reconcileProgress.computeIfAbsent(future, f -> new ReconcileProgress());
            if (!progress.decided) {
                int total = countReconcileHeads(reconcile, false);
                int candidates = countReconcileHeads(reconcile, true);
                if (total == 0 || candidates == 0) {
                    reconcileProgress.remove(future);
                    future.complete(TombstoneOutcome.ran(candidates, total, 0));
                    return;
                }
                double ratio = (double) candidates / total;
                if (!reconcile.allowShrink() && ratio > reconcile.maxAbsentRatio()) {
                    int percent = (int) Math.round(ratio * 100);
                    String reason = TombstoneOutcome.SHRINK_REASON_PREFIX + percent + "% — reconciliation skipped";
                    reconcileProgress.remove(future);
                    log.warn("{}: {} ({} of {} records absent)", reconcile.sourceId(), reason, candidates, total);
                    future.complete(TombstoneOutcome.discarded(candidates, total, reason));
                    return;
                }
                progress.decided = true;
                progress.total = total;
                progress.candidates = candidates;
            }
            while (true) {
                List<Object[]> chunk = nextReconcileCandidates(reconcile, RECONCILE_CHUNK);
                if (chunk.isEmpty()) {
                    break;
                }
                writeTombstones(reconcile.sourceId(), reconcile.runId(), chunk);
                progress.tombstoned += chunk.size();
                if (chunk.size() < RECONCILE_CHUNK) {
                    break;
                }
                TombstoneAbsent rest = new TombstoneAbsent(reconcile.sourceId(), reconcile.kinds(),
                        reconcile.runStartedAt(), reconcile.maxAbsentRatio(), reconcile.allowShrink(),
                        reconcile.runId(), future);
                if (queue.offer(rest)) {
                    return;
                }
                // The queue is full: continue inline rather than block the writer on its own queue.
            }
            int tombstoned = progress.tombstoned;
            reconcileProgress.remove(future);
            future.complete(TombstoneOutcome.ran(progress.candidates, progress.total, tombstoned));
        } catch (Throwable t) {
            reconcileProgress.remove(future);
            log.error("Complete-set reconciliation of {} failed", reconcile.sourceId(), t);
            future.completeExceptionally(t);
        }
    }

    /** The number of the dataset's live, non-tombstoned heads of the reconciled kinds, all or absent-only. */
    private int countReconcileHeads(TombstoneAbsent reconcile, boolean absentOnly) {
        Integer count = jdbc.query(
                con -> {
                    PreparedStatement ps =
                            con.prepareStatement(absentOnly ? COUNT_RECONCILE_CANDIDATES : COUNT_RECONCILE_TOTAL);
                    ps.setString(1, reconcile.sourceId());
                    ps.setArray(2, con.createArrayOf("text", reconcile.kinds().toArray()));
                    if (absentOnly) {
                        JdbcValues.setInstant(ps, 3, reconcile.runStartedAt());
                    }
                    return ps;
                },
                rs -> rs.next() ? rs.getInt(1) : 0);
        return count == null ? 0 : count;
    }

    /** Up to {@code limit} absent heads, as {@code [externalId, kind]} rows. */
    private List<Object[]> nextReconcileCandidates(TombstoneAbsent reconcile, int limit) {
        return jdbc.query(
                con -> {
                    PreparedStatement ps = con.prepareStatement(SELECT_RECONCILE_CANDIDATES);
                    ps.setString(1, reconcile.sourceId());
                    ps.setArray(2, con.createArrayOf("text", reconcile.kinds().toArray()));
                    JdbcValues.setInstant(ps, 3, reconcile.runStartedAt());
                    ps.setInt(4, limit);
                    return ps;
                },
                (rs, row) -> new Object[] {rs.getString(1), rs.getString(2)});
    }

    /**
     * Writes the tombstones of one chunk through the normal fold; the batch's {@code fetch_log} row carries the
     * reconciled run's {@code runId}, so it joins that run's chunk rows.
     */
    private void writeTombstones(String sourceId, String runId, List<Object[]> chunk) {
        Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);
        FetchLogEntry entry = new FetchLogEntry(
                sourceId, ai.foresignal.store.FetchTrigger.MANUAL, ai.foresignal.store.FetchStatus.OK, now, now,
                null, 0, null, null, null, null, null, null, RECONCILE_PRINCIPAL,
                ai.foresignal.source.spi.Coverage.none(), null, runId);
        List<IngestRecord> batch = new ArrayList<>(chunk.size());
        long arrival = 0;
        for (Object[] row : chunk) {
            NormalizedRecord tombstone =
                    NormalizedRecord.tombstone((String) row[0], (String) row[1], now);
            batch.add(new IngestRecord(tombstone, now, ++arrival));
        }
        IngestBatch ingest = new IngestBatch(sourceId, entry, batch, new CompletableFuture<>(), true);
        Committed committed = runBatch(ingest);
        publish(ingest, committed);
    }

    /** A reconciliation's running totals, kept across the re-enqueued turns of one run. */
    private static final class ReconcileProgress {
        boolean decided;
        int total;
        int candidates;
        int tombstoned;
    }

    // ------------------------------------------------------------------ entity purge (039)

    private void processEntityPurge(EntityPurge purge) {
        try {
            SourceIngestProfile profile = profiles.get(purge.sourceId());
            checkClockJump(clock.instant());
            preflight(profile.tier(), 1);
            List<Duration> backoff = settings.retryBackoff();
            int attempt = 0;
            PurgeCommitted committed;
            while (true) {
                try {
                    committed = tx.execute(status -> writeEntityPurge(purge, profile));
                    break;
                } catch (RuntimeException e) {
                    String sqlState = sqlState(e);
                    if (attempt < backoff.size() && isRetryable(sqlState, e)) {
                        retries.increment();
                        if (isMissingPartition(sqlState, e)) {
                            partitions.refresh();
                        }
                        sleepQuietly(backoff.get(attempt));
                        attempt++;
                        continue;
                    }
                    throw e;
                }
            }
            if (committed == null) {
                throw new IllegalStateException("The entity purge transaction returned nothing");
            }
            if (committed.outcome().found()) {
                lastSeq = Math.max(lastSeq, committed.outcome().tombstoneSeq());
                lastIngestedAt = latest(lastIngestedAt, committed.ingestedAt());
                // Change before the future, as for batches.
                changeBus.publish(committed.changes());
            }
            purge.future().complete(committed.outcome());
        } catch (Throwable t) {
            log.error("Entity purge of {} failed: {}", purge.sourceId(), t.toString(), t);
            purge.future().completeExceptionally(t);
        }
    }

    private PurgeCommitted writeEntityPurge(EntityPurge purge, SourceIngestProfile profile) {
        String sourceId = purge.sourceId();
        String externalId = purge.externalId();
        jdbc.queryForObject(
                "SELECT set_config('lock_timeout', ?, true)", String.class, settings.lockTimeout().toMillis() + "ms");
        List<Object[]> heads = jdbc.query(SELECT_PURGE_TARGET, (rs, row) -> new Object[] {
                rs.getLong(1), rs.getInt(2), rs.getString(3), JdbcValues.instant(rs, 4), JdbcValues.instant(rs, 5),
                envelope(rs, 6), envelope(rs, 10)},
                sourceId, externalId);
        if (heads.isEmpty()) {
            return new PurgeCommitted(EntityPurgeOutcome.notFound(), null, null);
        }
        Object[] head = heads.get(0);
        long headSeq = (Long) head[0];
        int version = (Integer) head[1] + 1;
        String kind = (String) head[2];

        Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);
        if (lastIngestedAt != null && now.isBefore(lastIngestedAt)) {
            clockClamped.increment();
        }
        Instant ingestedAt = ClockGuard.ingestedAt(now, lastIngestedAt);
        // ING-4: per entity, observed_at never decreases.
        Instant observedAt = latest(latest(now, (Instant) head[3]), (Instant) head[4]);

        long seqClockFirst = lastAllocatedSeq + 1;
        long seq = jdbc.queryForList(ALLOCATE, Long.class, 1).get(0);
        lastAllocatedSeq = Math.max(lastAllocatedSeq, seq);
        ensureCovered(profile.tier(), seq, seq);

        Long batchId = jdbc.query(con -> {
            PreparedStatement ps = con.prepareStatement(INSERT_PURGE_FETCH_LOG);
            ps.setString(1, sourceId);
            JdbcValues.setInstant(ps, 2, now);
            JdbcValues.setInstant(ps, 3, now);
            JdbcValues.setInstant(ps, 4, ingestedAt);
            ps.setLong(5, seq);
            ps.setLong(6, seq);
            return ps;
        }, (rs, row) -> rs.getLong(1)).get(0);

        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(INSERT_PURGE_TOMBSTONE);
            ps.setLong(1, seq);
            JdbcValues.setInstant(ps, 2, ingestedAt);
            JdbcValues.setInstant(ps, 3, observedAt);
            JdbcValues.setInstant(ps, 4, now);
            ps.setLong(5, headSeq);
            ps.setLong(6, batchId);
            ps.setInt(7, version);
            ps.setShort(8, profile.tier().code());
            ps.setShort(9, PersistReason.TOMBSTONE.code());
            ps.setShort(10, (short) QaFlags.PURGED);
            ps.setString(11, sourceId);
            ps.setString(12, externalId);
            ps.setString(13, kind);
            return ps;
        });

        int deleted = jdbc.update(DELETE_PURGED_VERSIONS, sourceId, externalId, seq);

        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(UPDATE_PURGED_HEAD);
            ps.setLong(1, seq);
            ps.setInt(2, version);
            JdbcValues.setInstant(ps, 3, observedAt);
            JdbcValues.setInstant(ps, 4, now);
            ps.setString(5, sourceId);
            ps.setString(6, externalId);
            return ps;
        });
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(UPSERT_PURGED_LIVE);
            ps.setString(1, sourceId);
            ps.setString(2, externalId);
            JdbcValues.setInstant(ps, 3, observedAt);
            JdbcValues.setInstant(ps, 4, now);
            JdbcValues.setInstant(ps, 5, now);
            ps.setShort(6, (short) QaFlags.PURGED);
            return ps;
        });
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(INSERT_SEQ_CLOCK);
            JdbcValues.setInstant(ps, 1, ingestedAt);
            ps.setLong(2, seqClockFirst);
            return ps;
        });

        ChangeSummary change = new ChangeSummary(seq, headSeq, profile.tier(), sourceId, externalId, kind, version,
                PersistReason.TOMBSTONE, observedAt, null, null, true, QaFlags.PURGED, null,
                Bbox.of((Envelope) head[5]), Bbox.of((Envelope) head[6]));
        ChangeBatch batch = new ChangeBatch(sourceId, batchId, ingestedAt, seq, seq, List.of(change));
        return new PurgeCommitted(new EntityPurgeOutcome(true, seq, deleted, kind), batch, ingestedAt);
    }

    /** What one committed entity purge produced. */
    private record PurgeCommitted(EntityPurgeOutcome outcome, ChangeBatch changes, Instant ingestedAt) {}

    // ------------------------------------------------------------------ helpers

    private <T> T time(String phase, Supplier<T> work) {
        long start = System.nanoTime();
        try {
            return work.get();
        } finally {
            phaseTimers.get(phase).record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        }
    }

    private void warn(String message) {
        log.warn(message);
        synchronized (warnings) {
            warnings.addLast(Instant.now(clock) + " " + message);
            while (warnings.size() > MAX_WARNINGS) {
                warnings.removeFirst();
            }
        }
    }

    /** The SQLState of the first {@link SQLException} in the cause chain, or null. */
    static String sqlState(Throwable error) {
        for (Throwable t = error; t != null; t = t.getCause()) {
            if (t instanceof SQLException sql && sql.getSQLState() != null) {
                return sql.getSQLState();
            }
            if (t.getCause() == t) {
                break;
            }
        }
        return null;
    }

    /** True for SQLState {@code 08*}, {@code 40*}, {@code 55P03} and {@code 57014}. */
    static boolean isTransient(String sqlState) {
        return sqlState != null
                && (sqlState.startsWith("08")
                        || sqlState.startsWith("40")
                        || sqlState.equals("55P03")
                        || sqlState.equals("57014"));
    }

    private static boolean isRetryable(String sqlState, Throwable error) {
        return isTransient(sqlState) || isMissingPartition(sqlState, error);
    }

    /**
     * A version insert found no partition: another context or process changed the leaves behind
     * this one's in-memory map. Refreshing the map and retrying lets the post-allocation check
     * create the leaf.
     */
    private static boolean isMissingPartition(String sqlState, Throwable error) {
        if (!"23514".equals(sqlState)) {
            return false;
        }
        for (Throwable t = error; t != null; t = t.getCause()) {
            if (t.getMessage() != null && t.getMessage().contains("no partition of relation")) {
                return true;
            }
            if (t.getCause() == t) {
                break;
            }
        }
        return false;
    }

    /** {@code observation} with every instant truncated to the database's microsecond precision. */
    static Observation toMicros(Observation o) {
        return new Observation(
                o.index(),
                o.arrival(),
                o.externalId(),
                o.kind(),
                micros(o.observedAt()),
                micros(o.receivedAt()),
                micros(o.eventTime()),
                o.geometry(),
                micros(o.validUntilExplicit()),
                micros(o.validUntil()),
                micros(o.validFrom()),
                o.payload(),
                o.tombstone(),
                o.quality(),
                o.observers(),
                o.qaFlags(),
                o.searchText(),
                o.title(),
                o.entityKeys());
    }

    private static Instant micros(Instant instant) {
        return instant == null ? null : instant.truncatedTo(ChronoUnit.MICROS);
    }

    private static Instant latest(Instant a, Instant b) {
        if (a == null) {
            return b;
        }
        return b == null || a.isAfter(b) ? a : b;
    }

    private void sleepWhileRunning(Duration duration) {
        long deadline = System.nanoTime() + duration.toNanos();
        while (running) {
            long left = deadline - System.nanoTime();
            if (left <= 0) {
                return;
            }
            try {
                Thread.sleep(Duration.ofNanos(Math.min(left, POLL.toNanos())));
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    /**
     * Sleeps up to {@code duration} while the writer is held for its clock, returning as soon as another thread
     * releases the hold ({@link #resetState()} re-applies the clock guard). Checkpoint 041: the writer slept out the
     * whole 30 s recheck after a reload had already released it, so ingest stayed held for that long.
     */
    private void sleepWhileClockBehind(Duration duration) {
        long deadline = System.nanoTime() + duration.toNanos();
        while (running && state == WriterState.CLOCK_BEHIND) {
            long left = deadline - System.nanoTime();
            if (left <= 0) {
                return;
            }
            try {
                Thread.sleep(Duration.ofNanos(Math.min(left, POLL.toNanos())));
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private static void sleepQuietly(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** What one committed batch produced, carried from the transaction to step 15. */
    private record Committed(
            long batchId,
            Instant ingestedAt,
            long fromSeq,
            long toSeq,
            IngestOutcome outcome,
            ChangeBatch changes,
            LiveBatch live,
            List<BatchFold.EntityPlan> persistedEntities,
            NormalizedBatch normalized) {}
}
