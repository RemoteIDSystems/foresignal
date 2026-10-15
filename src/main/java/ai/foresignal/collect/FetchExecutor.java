package ai.foresignal.collect;

import ai.foresignal.config.ForesignalVersion;
import ai.foresignal.ingest.FetchLogEntry;
import ai.foresignal.ingest.IngestBackpressureException;
import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.ingest.IngestRecord;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.ingest.TombstoneOutcome;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Coverage;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamHaltException;
import ai.foresignal.source.spi.UpstreamQuotaDeferredException;
import ai.foresignal.store.FetchStatus;
import ai.foresignal.store.FetchTrigger;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Runs one fetch of one polling source, end to end (Architecture §5.1; Core-Data-Model, "008
 * Collector Scheduler").
 *
 * <p>The steps: record the attempt, run {@code adapter.fetch} through the {@link FetchDispatcher}
 * with the descriptor's {@code fetchTimeout}, stamp {@code receivedAt} when it returns, then hand
 * the records to the single ingest writer and wait for its outcome. The {@code fetch_log} row is
 * built from the request traces the fetch's own client collected.
 *
 * <p>Every fetch leaves <strong>exactly one</strong> {@code fetch_log} row, except a run skipped for
 * backoff or overlap:
 *
 * <table>
 *   <caption>Outcomes</caption>
 *   <tr><th>Result</th><th>{@code fetch_log}</th><th>Health and ingest</th></tr>
 *   <tr><td>Records</td><td>OK, written by the writer</td><td>submitted, awaited,
 *       {@code recordSuccess}, which sets {@code last_data_at}</td></tr>
 *   <tr><td>304</td><td>NOT_MODIFIED, here</td><td>{@code recordSuccess(notModified)};
 *       {@code last_data_at} untouched</td></tr>
 *   <tr><td>Empty</td><td>OK with zero counts, here</td><td>{@code recordSuccess} with 0 seen;
 *       {@code last_data_at} untouched</td></tr>
 *   <tr><td>Budget refused</td><td>REFUSED</td><td>not a failure</td></tr>
 *   <tr><td>Upstream quota deferred ({@link UpstreamQuotaDeferredException}, prompt 062)</td><td>REFUSED</td>
 *       <td>counted as a refusal, not a failure</td></tr>
 *   <tr><td>Upstream halt ({@link UpstreamHaltException}, prompt 064)</td><td>FAILED</td>
 *       <td>{@code recordFailure}, {@code BackoffRegistry.halt} for the exception's hold (24 h), and a
 *       {@value #HALT_EVENT} system notification; only a manual fetch releases it early</td></tr>
 *   <tr><td>In backoff</td><td>none</td><td>skipped, no health change</td></tr>
 *   <tr><td>Host pacing gate busy for the bounded wait ({@link HostPacingTimeoutException}, prompt 080)</td>
 *       <td>none</td><td>skipped ({@value FetchOutcome#SKIP_HOST_PACING}), no health change</td></tr>
 *   <tr><td>Ingest queue full, or the writer failed the batch</td><td>FAILED</td><td>a warning,
 *       not an upstream failure</td></tr>
 *   <tr><td>Any other failure, timeouts included</td><td>FAILED, redacted error and HTTP
 *       status</td><td>{@code recordFailure}</td></tr>
 *   <tr><td>Scheduled run of a source disabled in flight</td><td>DISCARDED with the record
 *       count</td><td>nothing ingested</td></tr>
 * </table>
 *
 * <p>A per-source flag prevents overlapping fetches; a run that would overlap is skipped and
 * counted as {@code foresignal.fetch.skipped{reason=overlap}}. Durations are timed as
 * {@code foresignal.fetch.duration{source,result}}.
 */
@Component
public class FetchExecutor {

    private static final Logger log = LoggerFactory.getLogger(FetchExecutor.class);

    /** The error stored on a REFUSED row. */
    public static final String BUDGET_EXCEEDED_ERROR = "budget exceeded";

    /** The start of the error stored on a REFUSED row for an upstream quota deferral (prompt 062). */
    public static final String QUOTA_DEFERRED_ERROR = "upstream quota deferred";

    /** The error stored when the ingest queue stayed full. */
    public static final String QUEUE_FULL_ERROR = "ingest queue full";

    /** The system notification subtype of an upstream halt (prompt 064). */
    public static final String HALT_EVENT = "system.source_halted";

    /** The error stored on a DISCARDED row. */
    public static final String DISCARDED_ERROR = "source disabled while the fetch was in flight";

    /**
     * The kind of the one record a complete-set reference source emits after reconciliation (prompt 094),
     * describing what the run changed. A source that declares it is reconciled automatically.
     */
    public static final String DATASET_RELEASE_KIND = "dataset-release";

    /** The largest chunk an adapter may hand to {@link ai.foresignal.source.spi.FetchContext#emitChunk}. */
    public static final int MAX_CHUNK = 5_000;

    private final SourceRegistry registry;
    private final FetchContextFactory contexts;
    private final FetchDispatcher dispatcher;
    private final HealthTracker health;
    private final BackoffRegistry backoff;
    private final IngestQueue queue;
    private final FetchLogWriter fetchLog;
    private final ForesignalVersion version;
    private final JdbcTemplate jdbc;
    private final MeterRegistry meters;
    private final CollectorProperties properties;
    private final Clock clock;

    private final ConcurrentHashMap<String, AtomicBoolean> running = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> arrivals = new ConcurrentHashMap<>();

    /** Creates the executor. */
    public FetchExecutor(
            SourceRegistry registry,
            FetchContextFactory contexts,
            FetchDispatcher dispatcher,
            HealthTracker health,
            BackoffRegistry backoff,
            IngestQueue queue,
            FetchLogWriter fetchLog,
            ForesignalVersion version,
            JdbcTemplate jdbc,
            MeterRegistry meters,
            CollectorProperties properties,
            Clock clock) {
        this.registry = registry;
        this.contexts = contexts;
        this.dispatcher = dispatcher;
        this.health = health;
        this.backoff = backoff;
        this.queue = queue;
        this.fetchLog = fetchLog;
        this.version = version;
        this.jdbc = jdbc;
        this.meters = meters;
        this.properties = properties;
        this.clock = clock;
    }

    private UsageMeter usage;

    /**
     * The usage meter that counts a quota deferral as a refusal (prompt 062), so health's refusal rule
     * sees it. Optional, so a hand-built executor in a test works without one.
     */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setUsageMeter(UsageMeter usage) {
        this.usage = usage;
    }

    private org.springframework.context.ApplicationEventPublisher events;

    /**
     * Where an upstream halt's system notification is published (prompt 064). Optional, so a hand-built
     * executor in a test works without one.
     */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setEventPublisher(org.springframework.context.ApplicationEventPublisher events) {
        this.events = events;
    }

    /** True while a fetch of {@code sourceId} is running. */
    public boolean isRunning(String sourceId) {
        AtomicBoolean flag = running.get(sourceId);
        return flag != null && flag.get();
    }

    /** How many fetches are running right now. */
    public int activeFetches() {
        return (int) running.values().stream().filter(AtomicBoolean::get).count();
    }

    /**
     * Fetches {@code sourceId} once.
     *
     * @param trigger {@link FetchTrigger#SCHEDULED} or {@link FetchTrigger#MANUAL}
     * @throws ai.foresignal.api.error.NotFoundException when the source is not registered
     * @throws IllegalArgumentException when the source is not a polling source
     */
    public FetchOutcome execute(String sourceId, FetchTrigger trigger) {
        return execute(sourceId, trigger, false);
    }

    /**
     * Fetches {@code sourceId} once.
     *
     * @param trigger {@link FetchTrigger#SCHEDULED} or {@link FetchTrigger#MANUAL}
     * @param allowShrink the operator's one-off override of the complete-set reconciliation's shrink guard
     *     (prompt 094), set only by {@code POST /v1/sources/{id}/fetch?allowShrink=true}
     * @throws ai.foresignal.api.error.NotFoundException when the source is not registered
     * @throws IllegalArgumentException when the source is not a polling source
     */
    public FetchOutcome execute(String sourceId, FetchTrigger trigger, boolean allowShrink) {
        SourceRegistry.Registration registration = registry.get(sourceId);
        SourceDescriptor descriptor = registration.descriptor();
        SourceAdapter adapter = registration.adapterIfPresent().orElse(null);
        if (descriptor.mode() != Mode.POLLING || !(adapter instanceof PollingAdapter polling)) {
            throw new IllegalArgumentException(sourceId + " is a " + descriptor.mode().wireValue()
                    + " source; only polling sources are fetched");
        }
        if (trigger != FetchTrigger.SCHEDULED && trigger != FetchTrigger.MANUAL) {
            throw new IllegalArgumentException("A fetch is SCHEDULED or MANUAL, not " + trigger);
        }

        AtomicBoolean flag = running.computeIfAbsent(sourceId, id -> new AtomicBoolean());
        if (!flag.compareAndSet(false, true)) {
            countSkip(sourceId, FetchOutcome.SKIP_OVERLAP);
            return FetchOutcome.skipped(
                    sourceId, clock.instant(), FetchOutcome.SKIP_OVERLAP, "a fetch of this source is already running");
        }
        try {
            if (backoff.isInBackoff(sourceId)) {
                countSkip(sourceId, FetchOutcome.SKIP_BACKOFF);
                return FetchOutcome.skipped(
                        sourceId,
                        clock.instant(),
                        FetchOutcome.SKIP_BACKOFF,
                        "in backoff until " + backoff.backoffUntil(sourceId));
            }
            return run(polling, descriptor, trigger, allowShrink);
        } finally {
            flag.set(false);
        }
    }

    private FetchOutcome run(
            PollingAdapter adapter, SourceDescriptor descriptor, FetchTrigger trigger, boolean allowShrink) {
        String sourceId = descriptor.id();
        health.recordAttempt(sourceId);
        Instant startedAt = clock.instant().truncatedTo(ChronoUnit.MICROS);
        long startNanos = System.nanoTime();
        Duration timeout =
                descriptor.fetchTimeout() == null ? SourceDescriptor.DEFAULT_FETCH_TIMEOUT : descriptor.fetchTimeout();
        DefaultFetchContext ctx = contexts.forFetch(descriptor, null);
        RunChunks chunks = new RunChunks(descriptor, trigger, startedAt, timeout, allowShrink);
        ctx.chunkSink(chunks::emitChunk);

        FetchOutcome outcome;
        FetchResult result;
        try {
            result = dispatcher.call(descriptor, () -> adapter.fetch(ctx), timeout);
            if (result == null) {
                result = FetchResult.of(List.of());
            }
        } catch (Exception e) {
            outcome = onFetchFailure(descriptor, trigger, startedAt, startNanos, ctx, e);
            return timed(sourceId, startNanos, outcome);
        } catch (Error e) {
            outcome = onFetchFailure(descriptor, trigger, startedAt, startNanos, ctx, e);
            return timed(sourceId, startNanos, outcome);
        }

        Instant receivedAt = clock.instant();
        chunks.setRelease(result.release());
        outcome = onFetchResult(descriptor, trigger, startedAt, startNanos, timeout, receivedAt, ctx, result, chunks);
        return timed(sourceId, startNanos, outcome);
    }

    // ------------------------------------------------------------------ results

    private FetchOutcome onFetchResult(
            SourceDescriptor descriptor,
            FetchTrigger trigger,
            Instant startedAt,
            long startNanos,
            Duration timeout,
            Instant receivedAt,
            DefaultFetchContext ctx,
            FetchResult result,
            RunChunks chunks) {
        String sourceId = descriptor.id();
        List<FetchTrace> traces = ctx.traces();
        Coverage coverage = result.coverage();
        int n = result.size();
        int chunked = chunks.seen();
        boolean reconcilable = result.complete() && descriptor.kinds().contains(DATASET_RELEASE_KIND);

        if (trigger == FetchTrigger.SCHEDULED && !isEnabled(sourceId)) {
            long id = fetchLog.write(
                    entry(descriptor, trigger, FetchStatus.DISCARDED, startedAt, receivedAt, traces, coverage,
                            DISCARDED_ERROR, null, chunks.runId()),
                    n + chunked);
            log.info("{}: discarded {} records; the source was disabled while the fetch was in flight", sourceId,
                    n + chunked);
            return FetchOutcome.withoutIngest(
                    sourceId, startedAt, elapsedMs(startNanos), false, n + chunked, DISCARDED_ERROR, id,
                    FetchStatus.DISCARDED);
        }

        if (result.notModified()) {
            long id = fetchLog.write(
                    entry(descriptor, trigger, FetchStatus.NOT_MODIFIED, startedAt, receivedAt, traces, coverage,
                            null, null, chunks.runId()),
                    0);
            health.recordSuccess(sourceId, IngestOutcome.empty(), true, coverage);
            return FetchOutcome.withoutIngest(
                    sourceId, startedAt, elapsedMs(startNanos), true, 0, null, id, FetchStatus.NOT_MODIFIED);
        }

        if (n == 0 && chunked == 0) {
            long id = fetchLog.write(
                    entry(descriptor, trigger, FetchStatus.OK, startedAt, receivedAt, traces, coverage, null, null,
                            chunks.runId()),
                    0);
            health.recordSuccess(sourceId, IngestOutcome.empty(), false, coverage);
            return FetchOutcome.withoutIngest(
                    sourceId, startedAt, elapsedMs(startNanos), true, 0, null, id, FetchStatus.OK);
        }

        Duration remaining = timeout.minusNanos(System.nanoTime() - startNanos);
        if (remaining.isNegative()) {
            remaining = Duration.ZERO;
        }
        Duration ingestTimeout = remaining.plus(properties.ingestGrace());

        if (n > 0) {
            FetchLogEntry okEntry = entry(descriptor, trigger, FetchStatus.OK, startedAt, receivedAt, traces, coverage,
                    null, null, chunks.runId());
            List<IngestRecord> records = stamp(sourceId, result.records(), receivedAt);
            CompletableFuture<IngestOutcome> future;
            try {
                future = queue.submit(sourceId, okEntry, records, ingestTimeout, reconcilable);
            } catch (IngestBackpressureException e) {
                log.warn("{}: the ingest queue stayed full for {}; {} records were not ingested", sourceId,
                        ingestTimeout, n);
                return ingestFailed(descriptor, trigger, startedAt, startNanos, receivedAt, traces, coverage, n,
                        QUEUE_FULL_ERROR);
            }
            IngestOutcome ingested;
            try {
                ingested = future.get(Math.max(1, ingestTimeout.toMillis()), TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                // The batch is still queued and the writer will write its row when it gets there, so no
                // row is written here: that would make two.
                log.warn("{}: the ingest writer did not finish the batch within {}; it is still queued", sourceId,
                        ingestTimeout);
                return FetchOutcome.withoutIngest(
                        sourceId, startedAt, elapsedMs(startNanos), false, n + chunked,
                        "ingest did not complete within " + ingestTimeout.toSeconds() + " s", null, null);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return ingestFailed(descriptor, trigger, startedAt, startNanos, receivedAt, traces, coverage, n,
                        "interrupted while waiting for ingest");
            } catch (ExecutionException e) {
                Throwable cause = e.getCause() == null ? e : e.getCause();
                log.warn("{}: the ingest writer failed the batch of {} records", sourceId, n, cause);
                return ingestFailed(descriptor, trigger, startedAt, startNanos, receivedAt, traces, coverage, n,
                        "ingest failed: " + health.errorMessage(sourceId, cause));
            }
            chunks.addOutcome(ingested);
        }

        IngestOutcome ingested = chunks.total();
        health.recordSuccess(sourceId, ingested, false, coverage);

        if (reconcilable) {
            reconcile(ctx, descriptor, chunks, startedAt, ingestTimeout);
        }
        return FetchOutcome.ingested(
                sourceId, startedAt, elapsedMs(startNanos), ingested, findWriterRow(sourceId, startedAt));
    }

    /**
     * Complete-set reconciliation (prompt 094): submits {@link ai.foresignal.ingest.TombstoneAbsent} for the
     * dataset after the last chunk, degrades health when the shrink guard trips, and emits one
     * {@code dataset-release} record describing the run.
     */
    private void reconcile(DefaultFetchContext ctx, SourceDescriptor descriptor, RunChunks chunks,
            Instant startedAt, Duration ingestTimeout) {
        String sourceId = descriptor.id();
        Set<String> kinds = new LinkedHashSet<>(descriptor.kinds());
        kinds.remove(DATASET_RELEASE_KIND);
        int disappeared = 0;
        if (!kinds.isEmpty()) {
            try {
                TombstoneOutcome outcome = queue.submitTombstoneAbsent(
                                sourceId, kinds, startedAt, descriptor.reconcileMaxAbsentRatio(),
                                chunks.allowShrink(), reconcileTimeout(ingestTimeout))
                        .get(Math.max(1, reconcileTimeout(ingestTimeout).toMillis()), TimeUnit.MILLISECONDS);
                if (outcome.discarded()) {
                    ctx.state().degrade(outcome.reason());
                }
                disappeared = outcome.tombstoned();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("{}: interrupted while reconciling the complete set", sourceId);
            } catch (TimeoutException e) {
                log.warn("{}: the complete-set reconciliation did not finish in time", sourceId);
            } catch (ExecutionException e) {
                log.warn("{}: the complete-set reconciliation failed", sourceId, e.getCause());
            }
        }
        emitDatasetRelease(descriptor, chunks, disappeared, ingestTimeout);
    }

    /** Emits one {@code dataset-release} record for the run (prompt 094). */
    private void emitDatasetRelease(SourceDescriptor descriptor, RunChunks chunks, int disappeared,
            Duration ingestTimeout) {
        String sourceId = descriptor.id();
        IngestOutcome total = chunks.total();
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("upstreamVersion", chunks.releaseLabel(sourceId));
        payload.put("records", total.seen());
        payload.put("appeared", total.persisted(PersistReason.FIRST) + total.persisted(PersistReason.REAPPEAR));
        payload.put("changed", total.persisted(PersistReason.SIGNIFICANT) + total.persisted(PersistReason.CHANGE));
        payload.put("disappeared", disappeared + total.persisted(PersistReason.TOMBSTONE));
        payload.put("runId", chunks.runId());
        NormalizedRecord release = NormalizedRecord.builder(chunks.releaseLabel(sourceId), DATASET_RELEASE_KIND)
                .observedAt(clock.instant().truncatedTo(ChronoUnit.MICROS))
                .putAll(payload)
                .build();
        Instant receivedAt = clock.instant();
        List<IngestRecord> records = stamp(sourceId, List.of(release), receivedAt);
        FetchLogEntry entry = entry(descriptor, FetchTrigger.MANUAL, FetchStatus.OK, chunks.startedAt(), receivedAt,
                List.of(), Coverage.none(), null, null, chunks.runId());
        try {
            chunks.addOutcome(await(queue.submit(sourceId, entry, records, ingestTimeout)));
        } catch (IngestBackpressureException e) {
            log.warn("{}: the ingest queue stayed full; the dataset-release record was not written", sourceId);
        } catch (RuntimeException e) {
            log.warn("{}: the dataset-release record failed to ingest", sourceId, e);
        }
    }

    private IngestOutcome await(CompletableFuture<IngestOutcome> future) {
        try {
            return future.get(properties.ingestGrace().plus(Duration.ofMinutes(2)).toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            throw new IllegalStateException(cause);
        } catch (TimeoutException e) {
            throw new IllegalStateException(e);
        }
    }

    /** How long the reconciliation of one complete set may take. */
    private static Duration reconcileTimeout(Duration ingestTimeout) {
        Duration candidate = ingestTimeout.plus(Duration.ofMinutes(2));
        return candidate.compareTo(Duration.ofMinutes(10)) > 0 ? Duration.ofMinutes(10) : candidate;
    }

    /**
     * The accumulated state of one chunked run (prompt 094): a shared {@code run_id}, the totals across the
     * chunks, and the release identity. An adapter's {@code ctx.emitChunk} blocks here until the chunk's
     * batch is ingested, so the next chunk is not accepted before the previous future completes (back-pressure).
     */
    private final class RunChunks {

        private final SourceDescriptor descriptor;
        private final FetchTrigger trigger;
        private final Instant startedAt;
        private final Duration timeout;
        private final boolean allowShrink;
        private final String runId = UUID.randomUUID().toString();
        private final EnumMap<PersistReason, Integer> byReason = new EnumMap<>(PersistReason.class);

        private int seen;
        private int persisted;
        private int live;
        private int touched;
        private int touchesThrottled;
        private int late;
        private int skipped;
        private int rejected;
        private int chunks;
        private long fromSeq;
        private long toSeq;
        private String release;

        RunChunks(SourceDescriptor descriptor, FetchTrigger trigger, Instant startedAt, Duration timeout,
                boolean allowShrink) {
            this.descriptor = descriptor;
            this.trigger = trigger;
            this.startedAt = startedAt;
            this.timeout = timeout;
            this.allowShrink = allowShrink;
        }

        void setRelease(String release) {
            this.release = release;
        }

        String runId() {
            return runId;
        }

        boolean allowShrink() {
            return allowShrink;
        }

        Instant startedAt() {
            return startedAt;
        }

        int seen() {
            return seen;
        }

        int chunks() {
            return chunks;
        }

        /** The dataset's release identity, or the source id when the upstream publishes none. */
        String releaseLabel(String sourceId) {
            return release == null || release.isBlank() ? sourceId : release;
        }

        void addOutcome(IngestOutcome outcome) {
            seen += outcome.seen();
            persisted += outcome.persisted();
            live += outcome.live();
            touched += outcome.touched();
            touchesThrottled += outcome.touchesThrottled();
            late += outcome.late();
            skipped += outcome.skipped();
            rejected += outcome.rejected();
            if (outcome.fromSeq() > 0) {
                fromSeq = fromSeq == 0 ? outcome.fromSeq() : Math.min(fromSeq, outcome.fromSeq());
            }
            toSeq = Math.max(toSeq, outcome.toSeq());
            outcome.byReason().forEach((reason, count) -> byReason.merge(reason, count, Integer::sum));
        }

        IngestOutcome total() {
            return new IngestOutcome(seen, persisted, live, touched, touchesThrottled, late, skipped, rejected, 0,
                    fromSeq, toSeq, byReason, 0, List.of());
        }

        /** {@link DefaultFetchContext#emitChunk}: submits one chunk and waits for its outcome. */
        void emitChunk(List<NormalizedRecord> records) {
            if (records == null || records.isEmpty()) {
                return;
            }
            if (records.size() > MAX_CHUNK) {
                throw new IllegalArgumentException(
                        "A chunk carries at most " + MAX_CHUNK + " records, but this one carries " + records.size());
            }
            Instant receivedAt = clock.instant();
            FetchLogEntry chunkEntry = entry(descriptor, trigger, FetchStatus.OK, startedAt, receivedAt, List.of(),
                    Coverage.none(), null, null, runId);
            List<IngestRecord> stamped = stamp(descriptor.id(), records, receivedAt);
            chunks++;
            IngestOutcome outcome = await(queue.submit(descriptor.id(), chunkEntry, stamped, timeout, true));
            addOutcome(outcome);
        }
    }

    private FetchOutcome ingestFailed(
            SourceDescriptor descriptor,
            FetchTrigger trigger,
            Instant startedAt,
            long startNanos,
            Instant receivedAt,
            List<FetchTrace> traces,
            Coverage coverage,
            int n,
            String error) {
        String message = HealthTracker.truncate(error);
        long id = fetchLog.write(
                entry(descriptor, trigger, FetchStatus.FAILED, startedAt, receivedAt, traces, coverage, message, null),
                n);
        return FetchOutcome.withoutIngest(
                descriptor.id(), startedAt, elapsedMs(startNanos), false, n, message, id, FetchStatus.FAILED);
    }

    private FetchOutcome onFetchFailure(
            SourceDescriptor descriptor,
            FetchTrigger trigger,
            Instant startedAt,
            long startNanos,
            DefaultFetchContext ctx,
            Throwable error) {
        String sourceId = descriptor.id();
        HostPacingTimeoutException pacing = causeOf(error, HostPacingTimeoutException.class);
        if (pacing != null) {
            // Prompt 080: the host's request gate stayed busy for the whole bounded wait. Nothing was sent and
            // no budget was spent; the next scheduled run tries again.
            countSkip(sourceId, FetchOutcome.SKIP_HOST_PACING);
            log.info("{}: {}", sourceId, pacing.getMessage());
            return FetchOutcome.skipped(sourceId, startedAt, FetchOutcome.SKIP_HOST_PACING, pacing.getMessage());
        }
        InBackoffException backoffError = causeOf(error, InBackoffException.class);
        if (backoffError != null) {
            countSkip(sourceId, FetchOutcome.SKIP_BACKOFF);
            return FetchOutcome.skipped(
                    sourceId, startedAt, FetchOutcome.SKIP_BACKOFF, "in backoff until " + backoffError.getUntil());
        }
        List<FetchTrace> traces = ctx.traces();
        Instant now = clock.instant();
        if (causeOf(error, BudgetExceededException.class) != null) {
            long id = fetchLog.write(
                    entry(descriptor, trigger, FetchStatus.REFUSED, startedAt, now, traces, null,
                            BUDGET_EXCEEDED_ERROR, null),
                    0);
            health.recordRefused(sourceId);
            return FetchOutcome.withoutIngest(
                    sourceId, startedAt, elapsedMs(startNanos), false, 0, BUDGET_EXCEEDED_ERROR, id,
                    FetchStatus.REFUSED);
        }
        UpstreamQuotaDeferredException deferred = causeOf(error, UpstreamQuotaDeferredException.class);
        if (deferred != null) {
            // Prompt 062: the adapter's own quota check refused before anything was sent. A refusal,
            // like a spent budget, so the source degrades through the refusal rule instead of failing.
            String reason = HealthTracker.truncate(QUOTA_DEFERRED_ERROR + ": " + deferred.getMessage()
                    + " (retry at " + deferred.retryAt() + ")");
            long id = fetchLog.write(
                    entry(descriptor, trigger, FetchStatus.REFUSED, startedAt, now, traces, null, reason, null), 0);
            if (usage != null) {
                usage.recordRefused(sourceId);
            }
            health.recordRefused(sourceId);
            log.info("{}: {}", sourceId, reason);
            return FetchOutcome.withoutIngest(
                    sourceId, startedAt, elapsedMs(startNanos), false, 0, reason, id, FetchStatus.REFUSED);
        }
        UpstreamHaltException halt = causeOf(error, UpstreamHaltException.class);
        if (halt != null) {
            onHalt(descriptor, halt);
        }
        String message = health.errorMessage(sourceId, error);
        Integer status = HealthTracker.httpStatus(error);
        long id = fetchLog.write(
                entry(descriptor, trigger, FetchStatus.FAILED, startedAt, now, traces, null, message, status), 0);
        health.recordFailure(sourceId, error);
        log.warn("{}: fetch failed: {}", sourceId, message);
        return FetchOutcome.withoutIngest(
                sourceId, startedAt, elapsedMs(startNanos), false, 0, message, id, FetchStatus.FAILED);
    }

    /**
     * Applies an upstream halt (prompt 064): holds the source for the exception's hold and tells the
     * operator through a {@value #HALT_EVENT} system notification. The failure itself is then recorded like
     * any other, so {@code last_error} carries the halt reason that {@link HealthEvaluator} reports.
     */
    private void onHalt(SourceDescriptor descriptor, UpstreamHaltException halt) {
        String sourceId = descriptor.id();
        Instant until = backoff.halt(sourceId, halt.hold());
        log.error("{}: {}; held until {}", sourceId, halt.getMessage(), until);
        if (events == null) {
            return;
        }
        java.util.Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("sourceId", sourceId);
        if (halt.status() > 0) {
            data.put("httpStatus", halt.status());
        }
        data.put("heldUntil", until.toString());
        try {
            events.publishEvent(new ai.foresignal.notify.SystemEvent(HALT_EVENT, "critical",
                    "Source " + sourceId + " halted",
                    ai.foresignal.notify.MarkdownRenderer.escapeMarkdown(halt.getMessage())
                            + ". Foresignal will not fetch **" + ai.foresignal.notify.MarkdownRenderer.escapeMarkdown(
                                    sourceId) + "** again until " + until
                            + " unless you fetch it manually after investigating.",
                    "/v1/sources/" + sourceId + "/health", data));
        } catch (RuntimeException e) {
            log.warn("{}: could not publish the halt notification", sourceId, e);
        }
    }

    // ------------------------------------------------------------------ helpers

    /** Wraps each record with {@code receivedAt} and the source's next arrival number. */
    List<IngestRecord> stamp(String sourceId, List<NormalizedRecord> records, Instant receivedAt) {
        AtomicLong arrival = arrivals.computeIfAbsent(sourceId, id -> new AtomicLong());
        List<IngestRecord> stamped = new ArrayList<>(records.size());
        for (NormalizedRecord record : records) {
            stamped.add(new IngestRecord(record, receivedAt, arrival.incrementAndGet()));
        }
        return stamped;
    }

    /**
     * Builds the provenance of one fetch from its traces: {@code request_count} is the number of
     * traces, {@code bytes} their sum, and the URL, HTTP status, ETag and body hash come from the
     * last one.
     *
     * @param httpStatusOverride the status of an {@link UpstreamHttpException}, which wins over the
     *     last trace's
     */
    FetchLogEntry entry(
            SourceDescriptor descriptor,
            FetchTrigger trigger,
            FetchStatus status,
            Instant startedAt,
            Instant receivedAt,
            List<FetchTrace> traces,
            Coverage coverage,
            String error,
            Integer httpStatusOverride) {
        return entry(descriptor, trigger, status, startedAt, receivedAt, traces, coverage, error, httpStatusOverride,
                null);
    }

    /** {@link #entry(SourceDescriptor, FetchTrigger, FetchStatus, Instant, Instant, List, Coverage, String, Integer)}
     * with the run id of a chunked fetch (prompt 094). */
    FetchLogEntry entry(
            SourceDescriptor descriptor,
            FetchTrigger trigger,
            FetchStatus status,
            Instant startedAt,
            Instant receivedAt,
            List<FetchTrace> traces,
            Coverage coverage,
            String error,
            Integer httpStatusOverride,
            String runId) {
        FetchTrace last = traces.isEmpty() ? null : traces.get(traces.size() - 1);
        Integer httpStatus = httpStatusOverride;
        if (httpStatus == null && last != null && last.status() > 0) {
            httpStatus = last.status();
        }
        Long bytes = traces.isEmpty() ? null : traces.stream().mapToLong(FetchTrace::bytes).sum();
        return new FetchLogEntry(
                descriptor.id(),
                trigger,
                status,
                startedAt,
                receivedAt,
                httpStatus,
                traces.size(),
                last == null ? null : last.redactedUrl(),
                last == null ? null : last.etag(),
                bytes,
                last == null ? null : last.bodySha256(),
                version.version(),
                descriptor.licence(),
                null,
                coverage,
                error,
                runId);
    }

    /** {@code source_settings.enabled}; a source with no settings row is disabled. */
    boolean isEnabled(String sourceId) {
        List<Boolean> rows = jdbc.query(
                "select enabled from source_settings where source_id = ?", (rs, row) -> rs.getBoolean(1), sourceId);
        return !rows.isEmpty() && Boolean.TRUE.equals(rows.get(0));
    }

    /**
     * The id of the row the writer inserted for a fetch, found by source and start time.
     *
     * <p>{@code IngestOutcome} carries no batch id, and this executor holds the overlap guard, so
     * no other fetch of the source can share the start time. Null when it cannot be found.
     */
    private Long findWriterRow(String sourceId, Instant startedAt) {
        try {
            return jdbc.queryForObject(
                    "select max(id) from fetch_log where source_id = ? and started_at = ? and ingested_at is not null",
                    Long.class,
                    sourceId,
                    OffsetDateTime.ofInstant(startedAt, ZoneOffset.UTC));
        } catch (RuntimeException e) {
            log.debug("Could not look up the fetch_log row of {}'s fetch", sourceId, e);
            return null;
        }
    }

    private FetchOutcome timed(String sourceId, long startNanos, FetchOutcome outcome) {
        String result = outcome.status() == null
                ? (outcome.wasSkipped() ? "skipped" : "pending")
                : outcome.status().name().toLowerCase(Locale.ROOT);
        Timer.builder("foresignal.fetch.duration")
                .description("Wall time of one fetch, by source and result")
                .tag("source", sourceId)
                .tag("result", result)
                .register(meters)
                .record(System.nanoTime() - startNanos, TimeUnit.NANOSECONDS);
        return outcome;
    }

    private void countSkip(String sourceId, String reason) {
        Counter.builder("foresignal.fetch.skipped")
                .description("Scheduled or manual runs that did not happen")
                .tag("source", sourceId)
                .tag("reason", reason)
                .register(meters)
                .increment();
    }

    /**
     * {@code error} or the first of its causes of type {@code type}, or null.
     *
     * <p>Adapters may wrap what the client threw, and a refused budget wrapped in an adapter's own
     * exception is still a refusal, not a failure.
     */
    static <T extends Throwable> T causeOf(Throwable error, Class<T> type) {
        Throwable current = error;
        for (int depth = 0; current != null && depth < 10; depth++) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }
            current = current.getCause();
        }
        return null;
    }

    private static long elapsedMs(long startNanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }
}
