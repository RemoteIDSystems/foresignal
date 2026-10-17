package ai.foresignal.markets;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collect.ManualFetchService;
import ai.foresignal.collect.PersistentAdapterStateStore;
import ai.foresignal.security.Ulids;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.adapters.cftc.CftcCotAdapter;
import ai.foresignal.source.spi.AdapterStateStore;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * The CFTC COT history backfill (prompt 117, {@code POST /v1/sources/cftc-cot/backfill}): a simple tracked job,
 * because the 103 job framework runs analyses and writes no records, while a backfill must reach the store through
 * the ingest pipeline like any fetch.
 *
 * <p>{@link #start} records a {@code history_backfill_job}, writes the request into {@code cftc-cot}'s adapter state
 * ({@link CftcCotAdapter#BACKFILL_REQUEST_KEY}) and asks for one manual fetch. Every run of the source then reads
 * up to {@link CftcCotAdapter#BACKFILL_PAGES_PER_RUN} older pages within its budget; the adapter keeps its bound in
 * adapter state, committed with each run's records, so the backfill is resumable after an interruption and
 * idempotent (re-reading a report makes no new version). {@link #tick} (every two minutes unless
 * {@code foresignal.backfill.scheduled=false}) copies the progress into the job row, asks for the next manual fetch
 * (refused quietly while the budget is spent or the source is in backoff), and finishes the job when the adapter
 * reports that every report reached {@code from}.
 */
@Service
public class CotBackfillService {

    /** The default oldest report date. */
    public static final LocalDate DEFAULT_FROM = LocalDate.of(2010, 1, 1);

    /** The oldest date accepted: the legacy report's history on the portal starts in 1986. */
    public static final LocalDate EARLIEST_FROM = LocalDate.of(1986, 1, 1);

    /** Job states that count as open. */
    static final List<String> OPEN = List.of("queued", "running");

    private static final Logger log = LoggerFactory.getLogger(CotBackfillService.class);

    private final BackfillJobRepository jobs;
    private final SourceService sources;
    private final ManualFetchService fetches;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final Clock clock;
    private final boolean scheduled;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "cot-backfill");
        thread.setDaemon(true);
        return thread;
    });

    /** Creates the service. */
    public CotBackfillService(BackfillJobRepository jobs, SourceService sources, ManualFetchService fetches,
            JdbcTemplate jdbc, JsonMapper json, Clock clock,
            @Value("${foresignal.backfill.scheduled:true}") boolean scheduled) {
        this.jobs = jobs;
        this.sources = sources;
        this.fetches = fetches;
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
        this.scheduled = scheduled;
    }

    /**
     * Starts a backfill of {@code cftc-cot} down to {@code from}.
     *
     * @param from the oldest report date ({@code YYYY-MM-DD}), or null for {@link #DEFAULT_FROM}
     * @param createdBy the requesting token id, or null
     * @param runFirstFetch true to ask for the first manual fetch at once (in the background)
     * @return the running job
     * @throws ValidationException 400 for an unparseable date, one before {@link #EARLIEST_FROM} or in the future
     * @throws ConflictException 409 {@code SOURCE_DISABLED} when the source is disabled, {@code BACKFILL_RUNNING}
     *     when a backfill is already open
     */
    public synchronized BackfillJobEntity start(String from, String createdBy, boolean runFirstFetch) {
        LocalDate fromDate = parseFrom(from);
        if (!sources.view(CftcCotAdapter.ID).enabled()) {
            throw new ConflictException(ErrorCodes.SOURCE_DISABLED,
                    "cftc-cot is disabled; enable it before backfilling its history.");
        }
        List<BackfillJobEntity> open = jobs.findBySourceIdAndStatusIn(CftcCotAdapter.ID, OPEN);
        if (!open.isEmpty()) {
            throw new ConflictException(ErrorCodes.BACKFILL_RUNNING,
                    "A backfill of cftc-cot is already running (" + open.get(0).getId() + ").");
        }
        Instant now = clock.instant();
        BackfillJobEntity job = new BackfillJobEntity(Ulids.next(now), CftcCotAdapter.ID, fromDate, now);
        job.setCreatedBy(createdBy);
        job.setStatus("running");
        job.setStartedAt(now);
        job.setBaselineHeads(heads());
        jobs.save(job);
        state().put(CftcCotAdapter.BACKFILL_REQUEST_KEY,
                new CftcCotAdapter.BackfillRequest(job.getId(), fromDate).encode());
        if (runFirstFetch) {
            String id = job.getId();
            executor.submit(() -> advance(id));
        }
        return job;
    }

    /** Stops the background fetch thread. */
    @jakarta.annotation.PreDestroy
    void shutdown() {
        executor.shutdownNow();
    }

    /** The job, by id. */
    public BackfillJobEntity get(String id) {
        return jobs.findById(id).orElseThrow(() -> NotFoundException.of("Backfill job", id));
    }

    /** Copies progress and drives every open job one step (the scheduled tick). */
    @Scheduled(fixedDelayString = "PT2M", initialDelayString = "PT1M")
    public void scheduledTick() {
        if (scheduled) {
            tick();
        }
    }

    /** Copies progress into every open job and asks for the next manual fetch of each. */
    public void tick() {
        for (BackfillJobEntity job : jobs.findByStatusIn(OPEN)) {
            advance(job.getId());
        }
    }

    /**
     * One step of job {@code id}: refreshes its progress, finishes it when the adapter reports it done, else asks
     * for a manual fetch and refreshes again.
     */
    public void advance(String id) {
        BackfillJobEntity job = jobs.findById(id).orElse(null);
        if (job == null || !OPEN.contains(job.getStatus())) {
            return;
        }
        if (refresh(job)) {
            return;
        }
        if (!sources.view(job.getSourceId()).enabled()) {
            finish(job, "failed", "the source was disabled; start a new backfill after enabling it");
            return;
        }
        try {
            fetches.fetchNow(job.getSourceId());
        } catch (RuntimeException e) {
            // Budget spent, backoff or a fetch already running: the next tick tries again.
            log.debug("cftc-cot backfill {}: fetch deferred ({})", id, e.getMessage());
        }
        refresh(job);
    }

    /**
     * Copies the adapter's progress into {@code job} and finishes it when done.
     *
     * @return true when the job is finished
     */
    boolean refresh(BackfillJobEntity job) {
        AdapterStateStore state = state();
        String jobId = job.getId();
        job.setPagesFetched(CftcCotAdapterProgress.pages(state, jobId));
        LocalDate reached = CftcCotAdapterProgress.oldest(state, jobId);
        if (reached != null) {
            job.setCursor(reached.isBefore(job.getFrom()) ? job.getFrom() : reached);
        }
        job.setVersionsWritten(Math.max(0, heads() - job.getBaselineHeads()));
        boolean done = jobId.equals(state.get(CftcCotAdapter.BACKFILL_FINISHED_KEY).orElse(null));
        if (done) {
            job.setCursor(job.getFrom());
            finish(job, "done", null);
            return true;
        }
        jobs.save(job);
        return false;
    }

    private void finish(BackfillJobEntity job, String status, String error) {
        job.setStatus(status);
        job.setError(error);
        job.setFinishedAt(clock.instant());
        jobs.save(job);
        AdapterStateStore state = state();
        String request = state.get(CftcCotAdapter.BACKFILL_REQUEST_KEY).orElse(null);
        CftcCotAdapter.BackfillRequest parsed = CftcCotAdapter.BackfillRequest.parse(request);
        if (parsed != null && parsed.jobId().equals(job.getId())) {
            state.remove(CftcCotAdapter.BACKFILL_REQUEST_KEY);
        }
    }

    private long heads() {
        Long count = jdbc.queryForObject("SELECT count(*) FROM record_head WHERE source_id = ? AND kind = ?",
                Long.class, CftcCotAdapter.ID, ai.foresignal.source.series.SeriesPoint.KIND);
        return count == null ? 0 : count;
    }

    private AdapterStateStore state() {
        return new PersistentAdapterStateStore(CftcCotAdapter.ID, jdbc, json);
    }

    /** The requested date, defaulted and range-checked. */
    LocalDate parseFrom(String from) {
        if (from == null || from.isBlank()) {
            return DEFAULT_FROM;
        }
        LocalDate date;
        try {
            date = LocalDate.parse(from.strip());
        } catch (DateTimeParseException e) {
            throw ValidationException.forField("from", "must be a date (YYYY-MM-DD)");
        }
        if (date.isBefore(EARLIEST_FROM)) {
            throw ValidationException.forField("from", "must be on or after " + EARLIEST_FROM);
        }
        if (date.isAfter(clock.instant().atZone(ZoneOffset.UTC).toLocalDate())) {
            throw ValidationException.forField("from", "must not be in the future");
        }
        return date;
    }

    /** Reads the backfill progress the adapter keeps in its state. */
    static final class CftcCotAdapterProgress {

        private CftcCotAdapterProgress() {}

        /** The pages read for {@code jobId}. */
        static int pages(AdapterStateStore state, String jobId) {
            return CftcCotAdapter.counter(state.get(CftcCotAdapter.BACKFILL_PAGES_KEY).orElse(null),
                    jobId);
        }

        /** The oldest report date reached for {@code jobId}, or null. */
        static LocalDate oldest(AdapterStateStore state, String jobId) {
            String stored = state.get(CftcCotAdapter.BACKFILL_OLDEST_KEY).orElse(null);
            if (stored == null) {
                return null;
            }
            int bar = stored.indexOf('|');
            if (bar < 0 || !stored.substring(0, bar).equals(jobId)) {
                return null;
            }
            try {
                return LocalDate.parse(stored.substring(bar + 1).strip());
            } catch (DateTimeParseException e) {
                return null;
            }
        }
    }
}
