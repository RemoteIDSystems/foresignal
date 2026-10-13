package ai.foresignal.collect;

import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.spi.Coverage;
import ai.foresignal.store.CoverageSample;
import ai.foresignal.store.SourceHealthEntity;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Records what happens each time Foresignal talks to a source, into {@code source_health}
 * (Architecture §5.4, FR-10, FR-11).
 *
 * <p><strong>Writes only its own columns.</strong> The row may already exist, created by
 * {@link BackoffRegistry} or {@link PersistentAdapterStateStore}, and those components own
 * {@code backoff_until} and {@code adapter_state}. Every statement here is an upsert or update that
 * names the health columns and nothing else, which is why this is JDBC rather than saving the JPA
 * entity: a whole-entity save would write back a stale backoff deadline or ETag read moments earlier.
 *
 * <p>Every update is a short transaction of its own ({@code REQUIRES_NEW}), so health survives the
 * rollback of whatever fetch or batch reported it — a failure is most worth recording exactly when
 * the surrounding work is being thrown away.
 *
 * <p><strong>Refusals and backoff skips are not failures.</strong> Foresignal chose not to ask; the
 * upstream did nothing wrong. {@link #recordFailure} therefore ignores {@link BudgetExceededException}
 * and {@link InBackoffException}, and {@link #recordRefused} leaves the row alone.
 *
 * <p>Recording never throws: health is a diagnostic, and a database hiccup while writing it must not
 * fail the fetch it describes. Failures to record are logged.
 */
@Component
public class HealthTracker {

    private static final Logger log = LoggerFactory.getLogger(HealthTracker.class);

    /** The longest stored error message, in characters, including any truncation marker. */
    public static final int MAX_ERROR_LENGTH = 2000;

    /** Appended to a truncated error message. */
    static final String TRUNCATION_MARKER = "… (truncated)";

    private static final TypeReference<List<Integer>> COUNTS_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<CoverageSample>> COVERAGE_TYPE = new TypeReference<>() {};

    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final TransactionTemplate tx;
    private final BackoffRegistry backoff;
    private final CredentialsAccess credentials;
    private final Clock clock;

    /** When this tracker started; a streaming source with no stream event yet is down since then. */
    private final Instant startedAt;

    /** Stream connection state per source, in memory because a connection belongs to this process. */
    private final Map<String, StreamState> streams = new ConcurrentHashMap<>();

    /** Creates the tracker. */
    public HealthTracker(
            JdbcTemplate jdbc,
            JsonMapper json,
            PlatformTransactionManager transactionManager,
            BackoffRegistry backoff,
            CredentialsAccess credentials,
            Clock clock) {
        this.jdbc = jdbc;
        this.json = json;
        this.tx = new TransactionTemplate(transactionManager);
        this.tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.backoff = backoff;
        this.credentials = credentials;
        this.clock = clock;
        this.startedAt = clock.instant();
    }

    // ---------- polling outcomes ----------

    /** Records that a fetch is starting: sets {@code last_attempt_at}. */
    public void recordAttempt(String sourceId) {
        Instant now = clock.instant();
        write(
                sourceId,
                "attempt",
                () ->
                        jdbc.update(
                                """
                                insert into source_health (source_id, consecutive_failures, recent_record_counts,
                                                           adapter_state, last_attempt_at)
                                values (?, 0, '[]'::jsonb, '{}'::jsonb, ?)
                                on conflict (source_id) do update set last_attempt_at = excluded.last_attempt_at
                                """,
                                sourceId,
                                Timestamp.from(now)));
    }

    /**
     * Records a successful fetch by its counts.
     *
     * @param seen records the fetch returned
     * @param changed records it changed
     * @param notModified true for a 304, which says nothing about format health and so is kept out
     *     of the record-count ring and never sets {@code last_data_at}
     */
    public void recordSuccess(String sourceId, int seen, int changed, boolean notModified) {
        recordSuccess(sourceId, seen, changed, notModified, null);
    }

    /**
     * Records a successful fetch from the ingest writer's outcome (Core-Data-Model §6).
     *
     * <p>{@code last_records_changed} is the outcome's {@code persisted}. The late, clamped and
     * rejected counts reach health through {@code source_usage}, which the writer updates through
     * {@link UsageMeter#recordIngest}; they are not duplicated here.
     *
     * @param notModified true for a 304
     * @param coverage the fetch's coverage report, or null
     */
    public void recordSuccess(String sourceId, IngestOutcome outcome, boolean notModified, Coverage coverage) {
        IngestOutcome o = outcome == null ? IngestOutcome.empty() : outcome;
        recordSuccess(sourceId, o.seen(), o.persisted(), notModified, CoverageSample.from(coverage));
    }

    private void recordSuccess(
            String sourceId, int seen, int changed, boolean notModified, CoverageSample coverage) {
        Instant now = clock.instant();
        boolean written =
                write(
                        sourceId,
                        "success",
                        () -> {
                            ensureRow(sourceId);
                            Rings rings = lockRings(sourceId);
                            List<Integer> counts = rings.counts();
                            if (!notModified) {
                                counts.add(Math.max(0, seen));
                                trim(counts, SourceHealthEntity.RECENT_RECORD_COUNTS_LIMIT);
                            }
                            List<CoverageSample> coverageRing = rings.coverage();
                            if (coverage != null && !notModified) {
                                coverageRing.add(coverage);
                                trim(coverageRing, SourceHealthEntity.RECENT_COVERAGE_LIMIT);
                            }
                            boolean hasData = !notModified && seen > 0;
                            jdbc.update(
                                    """
                                    update source_health set
                                        last_attempt_at      = coalesce(last_attempt_at, ?),
                                        last_success_at      = ?,
                                        last_records_seen    = ?,
                                        last_records_changed = ?,
                                        consecutive_failures = 0,
                                        recent_record_counts = ?::jsonb,
                                        last_coverage        = coalesce(?::jsonb, last_coverage),
                                        recent_coverage      = ?::jsonb,
                                        last_data_at         = case when ? then ? else last_data_at end
                                     where source_id = ?
                                    """,
                                    Timestamp.from(now),
                                    Timestamp.from(now),
                                    Math.max(0, seen),
                                    Math.max(0, changed),
                                    json.writeValueAsString(counts),
                                    coverage == null || notModified ? null : json.writeValueAsString(coverage),
                                    json.writeValueAsString(coverageRing),
                                    hasData,
                                    Timestamp.from(now),
                                    sourceId);
                        });
        if (written) {
            backoff.onSuccess(sourceId);
        }
    }

    /**
     * Records a failed fetch.
     *
     * <p>Increments {@code consecutive_failures} and stores the error as
     * {@code SimpleClassName: message}, with this source's credential values masked and cut to
     * {@value #MAX_ERROR_LENGTH} characters. {@code last_http_status} is the status when the error
     * (or its cause) is an {@link UpstreamHttpException}, and is cleared otherwise so a stale status
     * is not shown beside an unrelated error.
     *
     * <p>A {@link BudgetExceededException} is a refusal and an {@link InBackoffException} a deliberate
     * skip; neither is a failure, so neither is recorded as one.
     */
    public void recordFailure(String sourceId, Throwable error) {
        if (error instanceof BudgetExceededException) {
            recordRefused(sourceId);
            return;
        }
        if (error instanceof InBackoffException) {
            return;
        }
        Instant now = clock.instant();
        String message = errorMessage(sourceId, error);
        Integer status = httpStatus(error);
        write(
                sourceId,
                "failure",
                () ->
                        jdbc.update(
                                """
                                insert into source_health (source_id, consecutive_failures, recent_record_counts,
                                                           adapter_state, last_attempt_at, last_error_at,
                                                           last_error, last_http_status)
                                values (?, 1, '[]'::jsonb, '{}'::jsonb, ?, ?, ?, ?)
                                on conflict (source_id) do update set
                                    consecutive_failures = source_health.consecutive_failures + 1,
                                    last_attempt_at      = coalesce(source_health.last_attempt_at,
                                                                    excluded.last_attempt_at),
                                    last_error_at        = excluded.last_error_at,
                                    last_error           = excluded.last_error,
                                    last_http_status     = excluded.last_http_status
                                """,
                                sourceId,
                                Timestamp.from(now),
                                Timestamp.from(now),
                                message,
                                status));
    }

    /**
     * Records a request the budget refused.
     *
     * <p>Not a failure, and deliberately not written to {@code source_health}: the refusal is
     * already counted by {@link UsageMeter#recordRefused}, called on the request path itself, and
     * health reads it from there (rule 6 of {@link HealthEvaluator}). Counting it again here would
     * double it.
     */
    public void recordRefused(String sourceId) {
        log.debug("{}: request refused by the budget; not a failure", sourceId);
    }

    // ---------- streaming hooks ----------

    /**
     * Records that a stream connected.
     *
     * <p>Counts as an attempt, and a working connection clears the failure streak and any backoff;
     * it does not touch the record-count ring, because connecting returns no records and a 0 there
     * would look like a silent break.
     */
    public void recordStreamConnected(String sourceId) {
        Instant now = clock.instant();
        streams.put(sourceId, StreamState.connectedAt(now));
        recordAttempt(sourceId);
        boolean written =
                write(
                        sourceId,
                        "stream-connected",
                        () ->
                                jdbc.update(
                                        "update source_health set consecutive_failures = 0 where source_id = ?",
                                        sourceId));
        if (written) {
            backoff.onSuccess(sourceId);
        }
    }

    /**
     * Records that a stream disconnected.
     *
     * @param error why, or null for a clean close, which is not a failure
     */
    public void recordStreamDisconnected(String sourceId, Throwable error) {
        streams.put(sourceId, StreamState.disconnectedAt(clock.instant()));
        if (error != null) {
            recordFailure(sourceId, error);
        }
    }

    /** Records a stream sink flush: a success with these counts, setting {@code last_data_at}. */
    public void recordStreamFlush(String sourceId, int seen, int changed) {
        recordSuccess(sourceId, seen, changed, false, null);
    }

    /**
     * The stream state of a source.
     *
     * <p>A source with no stream event in this process is reported as disconnected since the
     * tracker started, so an enabled stream that never connects becomes degraded after the grace
     * period rather than staying silently healthy.
     */
    public StreamState streamState(String sourceId) {
        return streams.getOrDefault(sourceId, StreamState.disconnectedAt(startedAt));
    }

    /** Forgets stream state. For tests. */
    void clearStreams() {
        streams.clear();
    }

    // ---------- helpers ----------

    private record Rings(List<Integer> counts, List<CoverageSample> coverage) {}

    private void ensureRow(String sourceId) {
        jdbc.update(
                """
                insert into source_health (source_id, consecutive_failures, recent_record_counts, adapter_state)
                values (?, 0, '[]'::jsonb, '{}'::jsonb)
                on conflict (source_id) do nothing
                """,
                sourceId);
    }

    /** Reads both rings with a row lock, so two concurrent successes cannot lose an entry. */
    private Rings lockRings(String sourceId) {
        Rings rings =
                jdbc.query(
                        """
                        select recent_record_counts::text, recent_coverage::text
                          from source_health where source_id = ? for update
                        """,
                        rs ->
                                rs.next()
                                        ? new Rings(
                                                parse(rs.getString(1), COUNTS_TYPE),
                                                parse(rs.getString(2), COVERAGE_TYPE))
                                        : new Rings(new ArrayList<>(), new ArrayList<>()),
                        sourceId);
        return rings == null ? new Rings(new ArrayList<>(), new ArrayList<>()) : rings;
    }

    private <T> List<T> parse(String text, TypeReference<List<T>> type) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        try {
            List<T> parsed = json.readValue(text, type);
            return parsed == null ? new ArrayList<>() : new ArrayList<>(parsed);
        } catch (RuntimeException e) {
            log.warn("Unreadable health ring {}; starting it afresh", text, e);
            return new ArrayList<>();
        }
    }

    private static <T> void trim(List<T> ring, int limit) {
        while (ring.size() > limit) {
            ring.remove(0);
        }
    }

    /** Runs {@code work} in its own transaction; returns false, logged, when it fails. */
    private boolean write(String sourceId, String what, Runnable work) {
        try {
            tx.executeWithoutResult(status -> work.run());
            return true;
        } catch (RuntimeException e) {
            log.error("Could not record {} health for {}", what, sourceId, e);
            return false;
        }
    }

    /**
     * {@code SimpleClassName: message}, credential values masked, at most
     * {@value #MAX_ERROR_LENGTH} characters.
     *
     * <p>When the credentials cannot be loaded nothing can be masked, so the message is withheld
     * rather than stored unmasked — the same fail-closed choice {@link UpstreamClient} makes for traces.
     */
    public String errorMessage(String sourceId, Throwable error) {
        if (error == null) {
            return "unknown error";
        }
        String className = error.getClass().getSimpleName();
        String message = error.getMessage();
        if (message == null && error.getCause() != null) {
            Throwable cause = error.getCause();
            message = cause.getClass().getSimpleName() + (cause.getMessage() == null ? "" : ": " + cause.getMessage());
        }
        if (message == null || message.isBlank()) {
            return truncate(className);
        }
        UrlRedactor redactor;
        try {
            redactor = new UrlRedactor(credentials.forSource(sourceId).values());
        } catch (RuntimeException e) {
            log.warn("Could not load {}'s credentials to redact an error; withholding the message", sourceId, e);
            return truncate(className + ": (message withheld: credentials unavailable for redaction)");
        }
        return truncate(className + ": " + redactor.redact(message));
    }

    /** Cuts {@code text} to {@value #MAX_ERROR_LENGTH} characters, marker included. */
    public static String truncate(String text) {
        if (text.length() <= MAX_ERROR_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_ERROR_LENGTH - TRUNCATION_MARKER.length()) + TRUNCATION_MARKER;
    }

    /** The HTTP status when {@code error} or a cause is an {@link UpstreamHttpException}. */
    static Integer httpStatus(Throwable error) {
        Throwable current = error;
        for (int depth = 0; current != null && depth < 10; depth++) {
            if (current instanceof UpstreamHttpException http) {
                return http.getStatus();
            }
            current = current.getCause();
        }
        return null;
    }
}
