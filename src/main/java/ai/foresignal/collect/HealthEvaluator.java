package ai.foresignal.collect;

import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.store.CoverageSample;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Derives a source's {@link HealthStatus} from its health row, usage and stream state
 * (Architecture §5.4, as amended by Core-Data-Model §14).
 *
 * <p>A pure function: no database, no clock of its own, no mutable state. The rules are applied
 * <strong>in order</strong> and the first that matches decides:
 *
 * <ol>
 *   <li>not enabled → {@code disabled}
 *   <li>no attempt yet → {@code idle}
 *   <li>the last failure was a refusal of this client (HTTP 451, or 403 from a source with
 *       {@code accessDeniedIsTerminal}; prompt 062) → {@code failing}, with
 *       {@link UpstreamAccessDeniedException#reason(int)}, or the source-specific reason the refusal carried
 *       (prompt 080)
 *   <li>the last failure was an upstream halt ({@code UpstreamHaltException}, prompt 064) → {@code failing},
 *       with the halt's own reason
 *   <li>in backoff, or ≥ {@value #FAILING_CONSECUTIVE_FAILURES} consecutive failures → {@code failing}
 *   <li>the last run failed (1–2 consecutive failures) → {@code degraded}
 *   <li>coverage dropped by ≥ 50% against the median of the ring → {@code degraded}
 *   <li>zero records after a non-empty history while coverage is steady, unless
 *       {@code zeroResultsNormal} → {@code degraded} (possible upstream format change, FR-11)
 *   <li>more than {@value #REFUSAL_RATE_THRESHOLD} of last hour's attempts refused → {@code degraded}
 *   <li>a streaming source disconnected for more than {@link #STREAM_DISCONNECTED_GRACE} → {@code degraded}
 *   <li>otherwise → {@code healthy}
 * </ol>
 *
 * <p>Then, for a running source (healthy, degraded or failing), any contributed {@link HealthRule}s
 * run and the more severe status wins. They run for a failing source too, which they cannot make
 * worse, so that a product's newest frame time is still reported while its fetches fail.
 */
public final class HealthEvaluator {

    private static final Logger log = LoggerFactory.getLogger(HealthEvaluator.class);

    /** Consecutive failures at which a source is failing rather than degraded. */
    public static final int FAILING_CONSECUTIVE_FAILURES = 3;

    /** Earlier non-empty counts needed before a zero-record success looks like a format change. */
    public static final int SILENT_BREAK_MIN_NON_EMPTY = 3;

    /** Share of attempts refused in the last hour above which a source is degraded (exclusive). */
    public static final double REFUSAL_RATE_THRESHOLD = 0.20;

    /** How long a streaming source may be disconnected before it is degraded (exclusive). */
    public static final Duration STREAM_DISCONNECTED_GRACE = Duration.ofMinutes(2);

    /** A fall to this fraction of the median, or below, is a coverage drop (a fall of ≥ 50%). */
    public static final double COVERAGE_DROP_FRACTION = 0.5;

    /** Earlier coverage reports needed before a drop can be judged against their median. */
    public static final int COVERAGE_MIN_HISTORY = 3;

    /** Reason for a disabled source. */
    public static final String REASON_DISABLED = "source disabled";

    /** Reason for an idle source. */
    public static final String REASON_IDLE = "no attempt yet";

    /** Reason for the silent-break rule (FR-11). */
    public static final String REASON_FORMAT_CHANGE =
            "returned 0 records; previously non-empty — possible upstream format change";

    private final List<HealthRule> rules;

    /** An evaluator with only the built-in rules. */
    public HealthEvaluator() {
        this(List.of());
    }

    /** An evaluator that also applies {@code rules}, in order. */
    public HealthEvaluator(List<HealthRule> rules) {
        this.rules = rules == null ? List.of() : List.copyOf(rules);
    }

    /** Derives the status for {@code in}. */
    public HealthAssessment evaluate(HealthInputs in) {
        HealthAssessment builtIn = builtIn(in);
        if (rules.isEmpty() || !builtIn.status().isRunning()) {
            return builtIn;
        }
        return applyRules(in, builtIn);
    }

    private HealthAssessment builtIn(HealthInputs in) {
        // 1. Not enabled.
        if (!in.enabled()) {
            return HealthAssessment.of(HealthStatus.DISABLED, REASON_DISABLED);
        }
        HealthSnapshot h = in.health();

        // 2. Never attempted.
        if (h.lastAttemptAt() == null) {
            return HealthAssessment.of(HealthStatus.IDLE, REASON_IDLE);
        }

        // 3. The upstream refused this client (prompt 062: HTTP 451, or 403 with accessDeniedIsTerminal).
        //    Failing from the first refusal until a fetch succeeds, with the reason that points the
        //    operator at the Operator Guide; the one-hour backoff keeps retries hourly.
        if (h.consecutiveFailures() > 0 && isAccessDenied(h)) {
            return HealthAssessment.of(HealthStatus.FAILING, accessDeniedReason(h));
        }

        // 3. The upstream's usage policy said stop (prompt 064): failing with the halt reason, which tells
        //    the operator to investigate and then fetch manually.
        if (h.consecutiveFailures() > 0 && isHalt(h)) {
            return HealthAssessment.of(HealthStatus.FAILING, haltReason(h.lastError()));
        }

        // 3. Backoff, or too many failures in a row.
        if (in.backoffUntil() != null && in.backoffUntil().isAfter(in.now())) {
            return HealthAssessment.of(
                    HealthStatus.FAILING,
                    "in backoff until " + in.backoffUntil().truncatedTo(ChronoUnit.SECONDS));
        }
        if (h.consecutiveFailures() >= FAILING_CONSECUTIVE_FAILURES) {
            return HealthAssessment.of(
                    HealthStatus.FAILING,
                    h.consecutiveFailures() + " consecutive failures: " + describe(h.lastError()));
        }

        // 4. The last run failed.
        if (h.consecutiveFailures() > 0) {
            return HealthAssessment.of(
                    HealthStatus.DEGRADED, "last run failed: " + describe(h.lastError()));
        }

        // 5. Coverage dropped.
        Optional<String> dropped = coverageDrop(h);
        if (dropped.isPresent()) {
            return HealthAssessment.of(HealthStatus.DEGRADED, dropped.get());
        }

        // 6. Possible format change: zero records while coverage is steady.
        if (!in.zeroResultsNormal() && isSilentBreak(h.recentRecordCounts())) {
            return HealthAssessment.of(HealthStatus.DEGRADED, REASON_FORMAT_CHANGE);
        }

        // 7. Budget refusals.
        double refusalRate = in.lastHour().refusalRate();
        if (refusalRate > REFUSAL_RATE_THRESHOLD) {
            return HealthAssessment.of(
                    HealthStatus.DEGRADED,
                    "budget refusals " + Math.round(refusalRate * 100) + "% in last hour");
        }

        // 8. A stream that has been down too long.
        if (in.descriptor().mode() == Mode.STREAMING && in.stream() != null && !in.stream().connected()) {
            Duration down = Duration.between(in.stream().since(), in.now());
            if (down.compareTo(STREAM_DISCONNECTED_GRACE) > 0) {
                return HealthAssessment.of(
                        HealthStatus.DEGRADED, "stream not connected for " + down.toSeconds() + "s");
            }
        }

        // 9. Nothing wrong.
        return HealthAssessment.of(HealthStatus.HEALTHY, null);
    }

    private HealthAssessment applyRules(HealthInputs in, HealthAssessment builtIn) {
        HealthRuleContext context = new HealthRuleContext(in.descriptor(), in.health(), in.now());
        HealthStatus status = builtIn.status();
        String reason = builtIn.statusReason();
        Instant newestFrameAt = null;
        for (HealthRule rule : rules) {
            Optional<HealthVerdict> verdict;
            try {
                verdict = rule.evaluate(context);
            } catch (RuntimeException e) {
                log.warn("Health rule {} failed for {}; ignoring it", rule.getClass().getName(), in.descriptor().id(), e);
                continue;
            }
            if (verdict == null || verdict.isEmpty()) {
                continue;
            }
            HealthVerdict v = verdict.get();
            if (v.newestFrameAt() != null && (newestFrameAt == null || v.newestFrameAt().isAfter(newestFrameAt))) {
                newestFrameAt = v.newestFrameAt();
            }
            if (v.status().worse(status) != status) {
                status = v.status();
                reason = v.reason();
            }
        }
        return new HealthAssessment(status, reason, newestFrameAt);
    }

    /** True when the last failure was an {@code UpstreamHaltException} (prompt 064). */
    static boolean isHalt(HealthSnapshot h) {
        return h.lastError() != null && h.lastError().contains(ai.foresignal.source.spi.UpstreamHaltException.MARKER);
    }

    /** The halt reason without the exception class prefix {@code HealthTracker} stores in front of it. */
    static String haltReason(String lastError) {
        String prefix = ai.foresignal.source.spi.UpstreamHaltException.class.getSimpleName() + ": ";
        int at = lastError.indexOf(prefix);
        return at >= 0 ? lastError.substring(at + prefix.length()) : lastError;
    }

    /**
     * The reason of a refusal (prompts 062 and 080): the source-specific reason stored in {@code last_error} when
     * the refusal carried one ({@link UpstreamAccessDeniedException#reason(int, String)}), else the generic
     * {@link UpstreamAccessDeniedException#reason(int)}.
     */
    static String accessDeniedReason(HealthSnapshot h) {
        int status = h.lastHttpStatus();
        String prefix = UpstreamAccessDeniedException.detailPrefix(status);
        int at = h.lastError().indexOf(prefix);
        return at >= 0 ? h.lastError().substring(at) : UpstreamAccessDeniedException.reason(status);
    }

    /** True when the last failure was an {@link UpstreamAccessDeniedException} (prompt 062). */
    static boolean isAccessDenied(HealthSnapshot h) {
        return h.lastHttpStatus() != null
                && (h.lastHttpStatus() == 451 || h.lastHttpStatus() == 403)
                && h.lastError() != null
                && h.lastError().contains(UpstreamAccessDeniedException.REASON_PREFIX);
    }

    // ---------- silent-break rules ----------

    /**
     * True when the newest counted success saw 0 records while at least
     * {@value #SILENT_BREAK_MIN_NON_EMPTY} of the earlier counts in the ring were non-empty.
     *
     * <p>The ring holds the last five counted successes including the newest, so the earlier counts
     * are up to four. 304 responses are never in the ring, so they cannot trip this.
     */
    static boolean isSilentBreak(List<Integer> counts) {
        if (counts.isEmpty() || counts.get(counts.size() - 1) == null || counts.get(counts.size() - 1) != 0) {
            return false;
        }
        long nonEmpty =
                counts.subList(0, counts.size() - 1).stream()
                        .filter(count -> count != null && count > 0)
                        .count();
        return nonEmpty >= SILENT_BREAK_MIN_NON_EMPTY;
    }

    /**
     * A reason when the newest coverage report's receivers or expected count fell to half the median
     * of the earlier reports or below.
     */
    static Optional<String> coverageDrop(HealthSnapshot h) {
        return coverageDrop(h.recentCoverage());
    }

    /**
     * The "coverage dropped" reason (Core-Data-Model §14) for a ring of coverage reports, oldest first: the newest
     * report's receivers or expected count at half the median of the earlier ones or below. Also used by the adapter
     * doctor's {@code COVERAGE_DROPPED} rule (prompt 140).
     */
    public static Optional<String> coverageDrop(List<CoverageSample> ring) {
        if (ring == null) {
            return Optional.empty();
        }
        if (ring.size() < COVERAGE_MIN_HISTORY + 1) {
            return Optional.empty();
        }
        CoverageSample latest = ring.get(ring.size() - 1);
        List<CoverageSample> earlier = ring.subList(0, ring.size() - 1);
        Optional<String> receivers = drop("receivers", latest, earlier, CoverageSample::receivers);
        return receivers.isPresent() ? receivers : drop("expected", latest, earlier, CoverageSample::expected);
    }

    private static Optional<String> drop(
            String field,
            CoverageSample latest,
            List<CoverageSample> earlier,
            Function<CoverageSample, Integer> value) {
        Integer now = value.apply(latest);
        if (now == null) {
            return Optional.empty();
        }
        List<Integer> history = new ArrayList<>();
        for (CoverageSample sample : earlier) {
            Integer v = value.apply(sample);
            if (v != null) {
                history.add(v);
            }
        }
        if (history.size() < COVERAGE_MIN_HISTORY) {
            return Optional.empty();
        }
        double median = median(history);
        if (median <= 0 || now > median * COVERAGE_DROP_FRACTION) {
            return Optional.empty();
        }
        return Optional.of(
                "coverage dropped: " + field + " " + now + " against a median of " + formatMedian(median));
    }

    /** The median of a non-empty list. */
    static double median(List<Integer> values) {
        List<Integer> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int n = sorted.size();
        return n % 2 == 1 ? sorted.get(n / 2) : (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;
    }

    private static String formatMedian(double median) {
        return median == Math.rint(median) ? Long.toString((long) median) : Double.toString(median);
    }

    private static String describe(String lastError) {
        return lastError == null || lastError.isBlank() ? "unknown error" : lastError;
    }

    // ---------- derived figures ----------

    /**
     * Hours until today's request budget is exhausted at the last hour's rate.
     *
     * @return null when there is no budget or the last hour's rate is 0
     */
    public static Double budgetHoursRemaining(BudgetStatus budget, UsageTotalsWindow lastHour) {
        if (budget == null || lastHour == null || lastHour.requests() <= 0) {
            return null;
        }
        return (double) Math.max(0, budget.remainingToday()) / lastHour.requests();
    }

    /** {@code errors / requests} over the window, 0 when there were no requests. */
    public static double errorRate(UsageTotalsWindow window) {
        if (window == null || window.requests() <= 0) {
            return 0.0;
        }
        return Math.min(1.0, (double) window.errors() / window.requests());
    }
}
