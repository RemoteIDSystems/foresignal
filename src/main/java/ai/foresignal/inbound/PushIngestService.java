package ai.foresignal.inbound;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collect.BudgetGovernor;
import ai.foresignal.collect.FetchLogWriter;
import ai.foresignal.collect.HealthTracker;
import ai.foresignal.collect.UsageMeter;
import ai.foresignal.inbound.format.ItemRejection;
import ai.foresignal.inbound.format.ParsedIngest;
import ai.foresignal.ingest.FetchLogEntry;
import ai.foresignal.ingest.IngestBackpressureException;
import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.ingest.IngestRecord;
import ai.foresignal.ingest.Rejection;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.Coverage;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.FetchStatus;
import ai.foresignal.store.FetchTrigger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * The shared push pipeline every inbound endpoint uses (prompt 024): native and named formats, the
 * ntfy and Gotify compatibility endpoints and EDP pushes.
 *
 * <p>Two steps, so that cheap refusals happen before a body is parsed:
 *
 * <ol>
 *   <li>{@link #admit}: the source must exist and accept pushes (PUSH mode, or a custom/external
 *       source) else 404; disabled is {@code 409 SOURCE_DISABLED}; the request takes one token from
 *       the source's request budget ({@code BudgetGovernor}), else {@code 429 RATE_LIMITED} with
 *       {@code Retry-After} and a {@code REFUSED} {@code fetch_log} row.
 *   <li>{@link #submit}: at most 10,000 records (400); the records-per-minute cap is consumed for
 *       the whole request, else {@code 429 INGEST_RECORD_RATE} with {@code Retry-After} and a
 *       {@code REFUSED} row; each record becomes an {@link IngestRecord} stamped with the request's
 *       receipt time, and one batch with a {@code PUSH} {@link FetchLogEntry} (principal, bytes,
 *       body hash, redacted URL, license) goes to the single writer, waiting at most 30 s for room
 *       ({@code 503 INGEST_BUSY}).
 * </ol>
 *
 * <p>Usage counts the request and its bytes like upstream traffic (the writer adds the record
 * counts through {@code UsageMeter.recordIngest}); health records an accepted request as a success
 * and a whole-request failure as a failure. The descriptor's {@code zeroResultsNormal} keeps a quiet
 * webhook from looking broken.
 */
@Service
public class PushIngestService {

    private static final Logger log = LoggerFactory.getLogger(PushIngestService.class);

    /** The most records one request may carry. */
    public static final int MAX_RECORDS = 10_000;

    /** How long a push waits for room in the ingest queue. */
    static final Duration QUEUE_TIMEOUT = Duration.ofSeconds(30);

    /** How long a push waits for the writer's outcome. */
    static final Duration OUTCOME_TIMEOUT = Duration.ofSeconds(60);

    private final SourceRegistry registry;
    private final SourceService sources;
    private final BudgetGovernor budgets;
    private final IngestRecordLimiter recordLimiter;
    private final CustomSourceRepository customSources;
    private final CustomSourceService customService;
    private final IngestQueue queue;
    private final FetchLogWriter fetchLog;
    private final HealthTracker health;
    private final UsageMeter usage;
    private final java.util.concurrent.atomic.AtomicLong arrival = new java.util.concurrent.atomic.AtomicLong();

    /** Creates the service. */
    public PushIngestService(SourceRegistry registry, SourceService sources, BudgetGovernor budgets,
            IngestRecordLimiter recordLimiter, CustomSourceRepository customSources, CustomSourceService customService,
            IngestQueue queue, FetchLogWriter fetchLog, HealthTracker health, UsageMeter usage) {
        this.registry = registry;
        this.sources = sources;
        this.budgets = budgets;
        this.recordLimiter = recordLimiter;
        this.customSources = customSources;
        this.customService = customService;
        this.queue = queue;
        this.fetchLog = fetchLog;
        this.health = health;
        this.usage = usage;
    }

    /**
     * One push, ready for the writer.
     *
     * @param sourceId the target source
     * @param principal the {@code fetch_log.principal}
     * @param parsed the records and format rejections
     * @param receivedAt when the request arrived
     * @param bytes the body size
     * @param bodySha256 hex SHA-256 of the body
     * @param urlRedacted the request URL with credentials masked
     */
    public record PushRequest(String sourceId, String principal, ParsedIngest parsed, Instant receivedAt, long bytes,
            String bodySha256, String urlRedacted) {}

    /**
     * What a push did.
     *
     * @param outcome the writer's outcome for the records
     * @param view the request-level outcome returned to the caller
     */
    public record PushResult(IngestOutcome outcome, IngestOutcomeView view) {}

    /**
     * Checks that {@code sourceId} accepts a push now and takes one request from its budget.
     *
     * @return the source's descriptor
     * @throws NotFoundException unknown source, or one that does not accept pushes
     * @throws ConflictException {@code SOURCE_DISABLED}
     * @throws ForesignalException {@code 429 RATE_LIMITED}
     */
    public SourceDescriptor admit(String sourceId, String principal, Instant receivedAt, String urlRedacted) {
        SourceDescriptor descriptor = registry.find(sourceId)
                .map(SourceRegistry.Registration::descriptor)
                .filter(PushIngestService::acceptsPush)
                .orElseThrow(() -> NotFoundException.of("Push source", sourceId));
        if (!sources.view(sourceId).enabled()) {
            throw new ConflictException(ErrorCodes.SOURCE_DISABLED, "Source '" + sourceId + "' is disabled.");
        }
        if (!budgets.tryConsume(sourceId)) {
            long wait = budgets.secondsUntilAvailable(sourceId);
            refused(descriptor, principal, receivedAt, urlRedacted, "request budget exceeded", 0);
            usage.recordRefused(sourceId);
            throw new ForesignalException(HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.RATE_LIMITED,
                    "The source's request budget is spent; retry after " + wait + " s.")
                    .withHeader(HttpHeaders.RETRY_AFTER, Long.toString(wait));
        }
        return descriptor;
    }

    /** True when {@code descriptor} may receive pushes: PUSH mode, or a custom or external source. */
    public static boolean acceptsPush(SourceDescriptor descriptor) {
        return descriptor.mode() == Mode.PUSH || descriptor.custom();
    }

    /**
     * Submits an admitted push to the writer and waits for its outcome.
     *
     * @throws ValidationException more than 10,000 records
     * @throws ForesignalException {@code 429 INGEST_RECORD_RATE} or {@code 503 INGEST_BUSY}
     */
    public PushResult submit(SourceDescriptor descriptor, PushRequest request) {
        String sourceId = request.sourceId();
        ParsedIngest parsed = request.parsed();
        List<NormalizedRecord> records = parsed.records();
        if (records.size() > MAX_RECORDS || parsed.items() > MAX_RECORDS) {
            throw new ValidationException("A request may carry at most " + MAX_RECORDS + " records; this one carries "
                    + Math.max(records.size(), parsed.items()) + ".");
        }
        int perMinute = customSources.findById(sourceId).map(CustomSourceEntity::getRecordsPerMinute)
                .orElse(CustomSourceInputReader.DEFAULT_RECORDS_PER_MINUTE);
        long wait = recordLimiter.tryConsume(sourceId, perMinute, records.size());
        if (wait > 0) {
            refused(descriptor, request.principal(), request.receivedAt(), request.urlRedacted(),
                    "records-per-minute cap exceeded", records.size());
            usage.recordRefused(sourceId);
            throw new ForesignalException(HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.INGEST_RECORD_RATE,
                    "The source's cap of " + perMinute + " records per minute is spent; retry after " + wait + " s.")
                    .withHeader(HttpHeaders.RETRY_AFTER, Long.toString(wait));
        }

        health.recordAttempt(sourceId);
        usage.record(sourceId, request.bytes(), true);
        List<IngestRecord> batch = new ArrayList<>(records.size());
        for (NormalizedRecord record : records) {
            batch.add(new IngestRecord(record, request.receivedAt(), arrival.incrementAndGet()));
        }
        FetchLogEntry entry = new FetchLogEntry(sourceId, FetchTrigger.PUSH, FetchStatus.OK, request.receivedAt(),
                request.receivedAt(), 200, 0, request.urlRedacted(), null, request.bytes(), request.bodySha256(), null,
                snapshotLicence(descriptor), request.principal(), Coverage.none(), null);
        IngestOutcome outcome = await(sourceId, submitBatch(sourceId, entry, batch));
        health.recordSuccess(sourceId, outcome, false, null);
        if (customSources.existsById(sourceId)) {
            customService.touchLastIngest(sourceId);
        }
        return new PushResult(outcome, IngestOutcomeView.of(outcome, parsed.items(), merge(parsed, outcome)));
    }

    /**
     * Records an accepted request that carried no records (an EDP batch of status items only): the
     * source stays "running" in health, so provider-reported status applies.
     */
    public void recordAccepted(String sourceId) {
        health.recordAttempt(sourceId);
        health.recordSuccess(sourceId, IngestOutcome.empty(), false, null);
    }

    /** Records a whole-request failure (a body that could not be parsed, a writer failure) in health. */
    public void recordFailure(String sourceId, Throwable error) {
        if (registry.contains(sourceId)) {
            health.recordAttempt(sourceId);
            health.recordFailure(sourceId, error);
        }
    }

    private CompletableFuture<IngestOutcome> submitBatch(String sourceId, FetchLogEntry entry, List<IngestRecord> batch) {
        try {
            return queue.submit(sourceId, entry, batch, QUEUE_TIMEOUT);
        } catch (IngestBackpressureException e) {
            health.recordFailure(sourceId, e);
            throw busy();
        }
    }

    private IngestOutcome await(String sourceId, CompletableFuture<IngestOutcome> future) {
        try {
            return future.get(OUTCOME_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            health.recordFailure(sourceId, cause);
            log.warn("Push into {} failed in the writer", sourceId, cause);
            if (cause instanceof ForesignalException foresignal) {
                throw foresignal;
            }
            throw new ForesignalException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.INTERNAL_ERROR,
                    "The ingest writer could not store the push.", cause);
        } catch (TimeoutException e) {
            health.recordFailure(sourceId, e);
            throw busy();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw busy();
        }
    }

    private static ForesignalException busy() {
        return new ForesignalException(HttpStatus.SERVICE_UNAVAILABLE, ErrorCodes.INGEST_BUSY,
                "The ingest queue is busy; retry shortly.")
                .withHeader(HttpHeaders.RETRY_AFTER, "30");
    }

    /** Resolves a descriptor's terms to the id snapshotted on the fetch row (prompt 109); null in unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.provenance.LicenceCatalog licenceCatalog;

    /**
     * The license id a PUSH batch snapshots on its {@code fetch_log} row (prompt 109, task 2: "PUSH batches
     * snapshot the custom source's terms"): the custom source's own {@code licenceTerms} id, else the catalog
     * id its free-text license resolves to, else the free text.
     */
    String snapshotLicence(ai.foresignal.source.spi.SourceDescriptor descriptor) {
        if (descriptor.licenceTerms() != null) {
            return descriptor.licenceTerms().id();
        }
        if (licenceCatalog != null && licenceCatalog.resolves(descriptor.licence())) {
            return licenceCatalog.terms(descriptor.licence()).id();
        }
        return descriptor.licence();
    }

    /** A {@code REFUSED} {@code fetch_log} row for a push the caps turned away. */
    private void refused(SourceDescriptor descriptor, String principal, Instant receivedAt, String urlRedacted,
            String error, int records) {
        try {
            fetchLog.write(new FetchLogEntry(descriptor.id(), FetchTrigger.PUSH, FetchStatus.REFUSED, receivedAt,
                    receivedAt, 429, 0, urlRedacted, null, null, null, null, snapshotLicence(descriptor), principal,
                    Coverage.none(), error), records);
        } catch (RuntimeException e) {
            log.warn("Could not record the refused push into {}", descriptor.id(), e);
        }
    }

    /** Format and writer rejections, by request index. */
    static List<IngestOutcomeView.RejectionView> merge(ParsedIngest parsed, IngestOutcome outcome) {
        List<IngestOutcomeView.RejectionView> merged = new ArrayList<>();
        for (ItemRejection rejection : parsed.rejections()) {
            merged.add(new IngestOutcomeView.RejectionView(rejection.index(), rejection.externalId(), rejection.reason()));
        }
        for (Rejection rejection : outcome.rejections()) {
            int position = rejection.index();
            int index = position >= 0 && position < parsed.indexes().size() ? parsed.indexes().get(position) : position;
            merged.add(new IngestOutcomeView.RejectionView(index, rejection.externalId(), rejection.reason().name()));
        }
        merged.sort(Comparator.comparingInt(IngestOutcomeView.RejectionView::index));
        return merged;
    }
}
