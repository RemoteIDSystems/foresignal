package ai.foresignal.collect;

import ai.foresignal.config.ForesignalVersion;
import ai.foresignal.ingest.FetchLogEntry;
import ai.foresignal.ingest.IngestBackpressureException;
import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.ingest.IngestRecord;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Coverage;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
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
import java.util.List;
import java.util.Locale;
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
 *   <tr><td>In backoff</td><td>none</td><td>skipped, no health change</td></tr>
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

    /** The error stored on a DISCARDED row. */
    public static final String DISCARDED_ERROR = "source disabled while the fetch was in flight";

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
            return run(polling, descriptor, trigger);
        } finally {
            flag.set(false);
        }
    }

    private FetchOutcome run(PollingAdapter adapter, SourceDescriptor descriptor, FetchTrigger trigger) {
        String sourceId = descriptor.id();
        health.recordAttempt(sourceId);
        Instant startedAt = clock.instant().truncatedTo(ChronoUnit.MICROS);
        long startNanos = System.nanoTime();
        Duration timeout =
                descriptor.fetchTimeout() == null ? SourceDescriptor.DEFAULT_FETCH_TIMEOUT : descriptor.fetchTimeout();
        DefaultFetchContext ctx = contexts.forFetch(descriptor, null);

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
        outcome = onFetchResult(descriptor, trigger, startedAt, startNanos, timeout, receivedAt, ctx, result);
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
            FetchResult result) {
        String sourceId = descriptor.id();
        List<FetchTrace> traces = ctx.traces();
        Coverage coverage = result.coverage();
        int n = result.size();

        if (trigger == FetchTrigger.SCHEDULED && !isEnabled(sourceId)) {
            long id = fetchLog.write(
                    entry(descriptor, trigger, FetchStatus.DISCARDED, startedAt, receivedAt, traces, coverage,
                            DISCARDED_ERROR, null),
                    n);
            log.info("{}: discarded {} records; the source was disabled while the fetch was in flight", sourceId, n);
            return FetchOutcome.withoutIngest(
                    sourceId, startedAt, elapsedMs(startNanos), false, n, DISCARDED_ERROR, id, FetchStatus.DISCARDED);
        }

        if (result.notModified()) {
            long id = fetchLog.write(
                    entry(descriptor, trigger, FetchStatus.NOT_MODIFIED, startedAt, receivedAt, traces, coverage,
                            null, null),
                    0);
            health.recordSuccess(sourceId, IngestOutcome.empty(), true, coverage);
            return FetchOutcome.withoutIngest(
                    sourceId, startedAt, elapsedMs(startNanos), true, 0, null, id, FetchStatus.NOT_MODIFIED);
        }

        if (n == 0) {
            long id = fetchLog.write(
                    entry(descriptor, trigger, FetchStatus.OK, startedAt, receivedAt, traces, coverage, null, null),
                    0);
            health.recordSuccess(sourceId, IngestOutcome.empty(), false, coverage);
            return FetchOutcome.withoutIngest(
                    sourceId, startedAt, elapsedMs(startNanos), true, 0, null, id, FetchStatus.OK);
        }

        FetchLogEntry okEntry =
                entry(descriptor, trigger, FetchStatus.OK, startedAt, receivedAt, traces, coverage, null, null);
        List<IngestRecord> records = stamp(sourceId, result.records(), receivedAt);
        Duration remaining = timeout.minusNanos(System.nanoTime() - startNanos);
        if (remaining.isNegative()) {
            remaining = Duration.ZERO;
        }
        Duration ingestTimeout = remaining.plus(properties.ingestGrace());

        CompletableFuture<IngestOutcome> future;
        try {
            future = queue.submit(sourceId, okEntry, records, ingestTimeout);
        } catch (IngestBackpressureException e) {
            log.warn("{}: the ingest queue stayed full for {}; {} records were not ingested", sourceId, ingestTimeout, n);
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
                    sourceId, startedAt, elapsedMs(startNanos), false, n,
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

        health.recordSuccess(sourceId, ingested, false, coverage);
        return FetchOutcome.ingested(
                sourceId, startedAt, elapsedMs(startNanos), ingested, findWriterRow(sourceId, startedAt));
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
        String message = health.errorMessage(sourceId, error);
        Integer status = HealthTracker.httpStatus(error);
        long id = fetchLog.write(
                entry(descriptor, trigger, FetchStatus.FAILED, startedAt, now, traces, null, message, status), 0);
        health.recordFailure(sourceId, error);
        log.warn("{}: fetch failed: {}", sourceId, message);
        return FetchOutcome.withoutIngest(
                sourceId, startedAt, elapsedMs(startNanos), false, 0, message, id, FetchStatus.FAILED);
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
                error);
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
