package ai.foresignal.cycle;

import java.time.Duration;
import java.util.List;

/** The fixed vocabulary of analysis cycles, runs and assessments (prompt 128). */
public final class CycleVocabulary {

    /** The daily cadence. */
    public static final String DAILY = "daily";

    /** The weekly cadence. */
    public static final String WEEKLY = "weekly";

    /** The monthly cadence. */
    public static final String MONTHLY = "monthly";

    /** The cadence of an on-demand situation assessment. */
    public static final String ON_DEMAND = "on-demand";

    /** The cadences a cycle may have. */
    public static final List<String> CADENCES = List.of(DAILY, WEEKLY, MONTHLY);

    /** Every assessment cadence. */
    public static final List<String> ASSESSMENT_CADENCES = List.of(DAILY, WEEKLY, MONTHLY, ON_DEMAND);

    /** The analyst role. */
    public static final String ANALYST = "analyst";

    /** The red-team role. */
    public static final String RED_TEAM = "red-team";

    /** The forecaster role. */
    public static final String FORECASTER = "forecaster";

    /** The proposer role. */
    public static final String PROPOSER = "proposer";

    /** Every role, in the order a run executes them. */
    public static final List<String> ROLES = List.of(ANALYST, RED_TEAM, FORECASTER, PROPOSER);

    /** Run and assessment status: every configured role succeeded. */
    public static final String OK = "ok";

    /** Some LLM roles succeeded and some did not. */
    public static final String PARTIAL = "partial";

    /** No LLM role contributed: the deterministic assessment only. */
    public static final String DIGEST_ONLY = "digest-only";

    /** The run failed. */
    public static final String FAILED = "failed";

    /** A queued run. */
    public static final String QUEUED = "queued";

    /** A running run. */
    public static final String RUNNING = "running";

    /** Role status: skipped (budget, refusal, nothing to do). */
    public static final String SKIPPED = "skipped";

    /** Role status: no provider or profile for it. */
    public static final String NOT_CONFIGURED = "not-configured";

    /** Run triggers. */
    public static final List<String> TRIGGERS = List.of("schedule", "missed", "manual", "situation");

    /** Notification priorities, lowest first. */
    public static final List<String> PRIORITIES = List.of("low", "normal", "high");

    /** The default and maximum forecaster samples. */
    public static final int DEFAULT_SAMPLES = 3;

    /** The maximum forecaster samples. */
    public static final int MAX_SAMPLES = 7;

    /** The per-section cap of the packet as the LLM sees it (the 102 cap). */
    public static final int LLM_SECTION_CAP = 30;

    /** The footer of every assessment. */
    public static final String FOOTER = "Automated analysis. Probabilities use ICD 203 terms; evidence is cited;"
            + " speculation is labeled. Verify before acting.";

    private CycleVocabulary() {}

    /** The default {@code max_questions} of a cadence: 15 daily, 40 weekly, 60 monthly (15 on demand). */
    public static int defaultMaxQuestions(String cadence) {
        return switch (cadence) {
            case WEEKLY -> 40;
            case MONTHLY -> 60;
            default -> 15;
        };
    }

    /** The default {@code max_tokens_per_cycle} of a cadence: 300k daily, 1.2M weekly, 2M monthly. */
    public static long defaultMaxTokens(String cadence) {
        return switch (cadence) {
            case WEEKLY -> 1_200_000L;
            case MONTHLY -> 2_000_000L;
            default -> 300_000L;
        };
    }

    /** The natural lookback of a cadence's first run: 1, 7 or 30 days (7 on demand). */
    public static Duration lookback(String cadence) {
        return switch (cadence) {
            case DAILY -> Duration.ofDays(1);
            case MONTHLY -> Duration.ofDays(30);
            default -> Duration.ofDays(7);
        };
    }

    /** The forecast horizon a packet looks ahead: 7 days daily, 30 weekly, 90 monthly (30 on demand). */
    public static Duration horizon(String cadence) {
        return switch (cadence) {
            case DAILY -> Duration.ofDays(7);
            case MONTHLY -> Duration.ofDays(90);
            default -> Duration.ofDays(30);
        };
    }

    /** The default effort of a cadence: {@code medium} daily, {@code high} otherwise. */
    public static String defaultEffort(String cadence) {
        return DAILY.equals(cadence) ? "medium" : "high";
    }

    /** The priority of an assessment notification: {@code normal} daily and on demand, {@code high} otherwise. */
    public static String priority(String cadence) {
        return WEEKLY.equals(cadence) || MONTHLY.equals(cadence) ? "high" : "normal";
    }

    /** True when {@code priority} is below {@code min} (unknown values count as {@code normal}). */
    public static boolean belowPriority(String priority, String min) {
        int p = PRIORITIES.indexOf(priority == null ? "normal" : priority);
        int m = PRIORITIES.indexOf(min == null ? "normal" : min);
        return (p < 0 ? 1 : p) < (m < 0 ? 1 : m);
    }

    /**
     * The effort actually used: the cadence's (or the requested) effort, capped by the profile's effort when the
     * profile sets a lower one ({@code low < medium < high < xhigh < max}).
     */
    public static String effectiveEffort(String wanted, String profileCap) {
        List<String> order = List.of("low", "medium", "high", "xhigh", "max");
        int w = order.indexOf(wanted == null ? "medium" : wanted);
        if (profileCap == null || order.indexOf(profileCap) < 0) {
            return order.get(Math.max(0, w));
        }
        return order.get(Math.min(Math.max(0, w), order.indexOf(profileCap)));
    }
}
