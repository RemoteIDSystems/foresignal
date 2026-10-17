package ai.foresignal.precursor;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

/**
 * The deterministic plain-language descriptions of precursors (prompt 120 §4): the same numbers always give the same
 * sentence, and every sentence speaks of association in stored data.
 */
public final class PrecursorText {

    /** The association statement every precursor carries. */
    public static final String ASSOCIATION_NOTE = "association in stored data; not causation";

    private PrecursorText() {}

    /**
     * The description of an event precursor: "In {regions}, {antecedent} was followed by {consequent} within {lag} in
     * {hits}/{n} cases ({rate}, 95% interval {lo}–{hi}) vs {base} expected by chance; validated on {window}."
     *
     * @param regions the regions
     * @param antecedent the antecedent label
     * @param consequent the consequent label
     * @param lagMax the longest lag
     * @param hits the hits
     * @param n the antecedents
     * @param interval the Wilson interval {@code [lo, hi]}
     * @param baseRate the base rate
     * @param windowStart the window start
     * @param windowEnd the window end
     * @param validated true when validated on that window (otherwise "tested on")
     * @return the description
     */
    public static String event(List<String> regions, String antecedent, String consequent, Duration lagMax, long hits,
            long n, double[] interval, double baseRate, Instant windowStart, Instant windowEnd, boolean validated) {
        double rate = n == 0 ? 0 : (double) hits / n;
        return "In " + regions(regions) + ", " + antecedent + " was followed by " + consequent + " within "
                + lag(lagMax) + " in " + hits + "/" + n + " cases (" + pct(rate) + ", 95% interval "
                + pct(interval[0]) + "–" + pct(interval[1]) + ") vs " + pct(baseRate) + " expected by chance; "
                + (validated ? "validated on " : "tested on ") + window(windowStart, windowEnd) + ".";
    }

    /**
     * The description of a series precursor.
     *
     * @param regions the regions
     * @param antecedent the antecedent label
     * @param consequent the consequent label
     * @param bestLag the best lag
     * @param correlation the correlation at the best lag
     * @param p the p-value on that window
     * @param windowStart the window start
     * @param windowEnd the window end
     * @param validated true when validated on that window
     * @return the description
     */
    public static String series(List<String> regions, String antecedent, String consequent, Duration bestLag,
            double correlation, double p, Instant windowStart, Instant windowEnd, boolean validated) {
        String direction = correlation >= 0 ? "in the same direction" : "in the opposite direction";
        return "In " + regions(regions) + ", moves in " + antecedent + " were followed " + direction + " by moves in "
                + consequent + " " + lag(bestLag) + " later (correlation " + num(correlation) + ", p = " + num(p)
                + "); " + (validated ? "validated on " : "tested on ") + window(windowStart, windowEnd) + ".";
    }

    /** "the stored regions" or a comma list. */
    static String regions(List<String> regions) {
        return regions == null || regions.isEmpty() ? "the stored data" : String.join(", ", regions);
    }

    /** A lag in words: "3 days", "1 day", "2 weeks", "6 hours". */
    public static String lag(Duration d) {
        if (d == null) {
            return "an unknown lag";
        }
        long hours = d.toHours();
        if (d.toSeconds() % (7 * 86_400) == 0 && d.toDays() >= 7) {
            long w = d.toDays() / 7;
            return w + (w == 1 ? " week" : " weeks");
        }
        if (d.toSeconds() % 86_400 == 0) {
            long days = d.toDays();
            return days + (days == 1 ? " day" : " days");
        }
        return hours + (hours == 1 ? " hour" : " hours");
    }

    /** A rate as a percentage with no decimals ("38%"). */
    public static String pct(double rate) {
        if (!Double.isFinite(rate)) {
            return "n/a";
        }
        return String.format(Locale.ROOT, "%.0f%%", rate * 100);
    }

    /** A number with up to three significant digits. */
    public static String num(double value) {
        if (!Double.isFinite(value)) {
            return "n/a";
        }
        double abs = Math.abs(value);
        if (abs >= 100) {
            return String.format(Locale.ROOT, "%.0f", value);
        }
        if (abs >= 10) {
            return String.format(Locale.ROOT, "%.1f", value);
        }
        if (abs >= 0.01 || abs == 0) {
            return String.format(Locale.ROOT, "%.2f", value);
        }
        return String.format(Locale.ROOT, "%.4f", value);
    }

    /** A window as "2025-11-01 to 2026-03-01". */
    static String window(Instant start, Instant end) {
        return (start == null ? "?" : start.toString().substring(0, 10)) + " to "
                + (end == null ? "?" : end.toString().substring(0, 10));
    }
}
