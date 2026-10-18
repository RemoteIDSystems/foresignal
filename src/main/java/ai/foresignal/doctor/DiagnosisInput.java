package ai.foresignal.doctor;

import ai.foresignal.store.CoverageSample;
import java.time.Instant;
import java.util.List;

/**
 * Everything the deterministic rules look at (prompt 140, task 3). Built by {@link DoctorService}; pure data, so
 * {@link DiagnosisRules} can be tested with one fixture per finding.
 *
 * @param sourceId the source
 * @param zeroResultsNormal the descriptor's {@code zeroResultsNormal}
 * @param health the source's health facts
 * @param recentFetches the last 20 {@code fetch_log} rows, newest first
 * @param coverage the recent coverage reports, oldest first (the latest last)
 * @param budget the budget facts, or null for a source without a request budget
 * @param backoffUntil when the current backoff ends, or null
 * @param now the decision time
 * @param probe the probe facts
 * @param probeStructure the probe body's structure, or null when the probe was skipped or returned no body
 * @param fixtureStructure the fixture's structure, or null when there is no fixture
 * @param diff the structure diff, or null when there was nothing to compare
 * @param literals the parser literals of the adapter source
 */
public record DiagnosisInput(
        String sourceId,
        boolean zeroResultsNormal,
        HealthFacts health,
        List<FetchFacts> recentFetches,
        List<CoverageSample> coverage,
        BudgetFacts budget,
        Instant backoffUntil,
        Instant now,
        ProbeFacts probe,
        Structure probeStructure,
        Structure fixtureStructure,
        StructureDiff diff,
        ParserLiterals literals) {

    /** Normalizes the members. */
    public DiagnosisInput {
        recentFetches = recentFetches == null ? List.of() : List.copyOf(recentFetches);
        coverage = coverage == null ? List.of() : List.copyOf(coverage);
        literals = literals == null ? ParserLiterals.NONE : literals;
        probe = probe == null ? ProbeFacts.skipped("not requested") : probe;
        health = health == null ? new HealthFacts(null, null, 0) : health;
    }

    /**
     * The health facts.
     *
     * @param status the derived status ({@code healthy}, {@code degraded}, …), or null
     * @param reason the status reason, or null
     * @param consecutiveFailures failures since the last success
     */
    public record HealthFacts(String status, String reason, int consecutiveFailures) {}

    /**
     * One {@code fetch_log} row.
     *
     * @param status {@code OK}, {@code NOT_MODIFIED}, {@code FAILED}, {@code REFUSED} or {@code DISCARDED}
     * @param httpStatus the HTTP status, or null
     * @param error the redacted error, or null
     * @param recordsSeen records seen
     * @param startedAt when the fetch started
     */
    public record FetchFacts(String status, Integer httpStatus, String error, int recordsSeen, Instant startedAt) {}

    /**
     * The budget facts.
     *
     * @param perMinute the descriptor's (or operator's) per-minute limit
     * @param perDay the per-day limit
     * @param usedToday requests made today, UTC
     * @param requestsLastHour requests sent in the last hour
     * @param exhausted true when nothing more may be sent right now
     */
    public record BudgetFacts(int perMinute, int perDay, long usedToday, long requestsLastHour, boolean exhausted) {}

    /**
     * A redirect the probe followed or received.
     *
     * @param status 301, 302, 307 or 308
     * @param fromHost the requested host
     * @param location the {@code Location} header (redacted)
     * @param toHost the host of the location, or null when relative
     */
    public record Redirect(int status, String fromHost, String location, String toHost) {}

    /**
     * The probe facts. The body head is held in memory only, for block-page and login-wall detection; it is never
     * stored.
     *
     * @param skipped true when no probe was sent
     * @param skipReason why: {@code budget}, {@code backoff}, {@code no-upstream}, {@code not-requested}; null when
     *     sent
     * @param httpStatus the last response's HTTP status, or null
     * @param contentType the last response's content type, or null
     * @param bodyHead up to 4 KB of the last body, or null
     * @param parseError the adapter's parse error, or null
     * @param records the records the adapter parsed (at most 100 are counted)
     * @param redirects the redirects seen
     */
    public record ProbeFacts(boolean skipped, String skipReason, Integer httpStatus, String contentType,
            String bodyHead, String parseError, int records, List<Redirect> redirects) {

        /** Copies the list. */
        public ProbeFacts {
            redirects = redirects == null ? List.of() : List.copyOf(redirects);
        }

        /** A probe that was not sent. */
        public static ProbeFacts skipped(String reason) {
            return new ProbeFacts(true, reason, null, null, null, null, 0, List.of());
        }
    }
}
