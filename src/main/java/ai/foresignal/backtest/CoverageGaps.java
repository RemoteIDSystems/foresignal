package ai.foresignal.backtest;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Coverage gaps of one source over a range, from its {@code fetch_log} rows (prompt 103 §2,
 * {@code COVERAGE_GAPS}). Pure.
 *
 * <p>A gap is either a run of {@code FAILED} or {@code REFUSED} fetches, from the first failure to
 * the next successful fetch (or the end of the range), or a silence: two consecutive rows more than
 * {@link #SILENCE_FACTOR} times the source's typical interval apart. The typical interval is the
 * median spacing of the source's rows in the range, because the configured poll interval is not
 * what the history shows when a source was paused or re-tuned. A source with no rows at all in the
 * range has unknown coverage, so the whole range counts as a gap.
 */
public final class CoverageGaps {

    /** A silence longer than this many typical intervals is a gap. */
    public static final int SILENCE_FACTOR = 3;

    /** The statuses that count as failed runs. */
    public static final Set<String> FAILED = Set.of("FAILED", "REFUSED");

    /** The statuses that end a failure run. */
    public static final Set<String> SUCCEEDED = Set.of("OK", "NOT_MODIFIED");

    private CoverageGaps() {}

    /**
     * One {@code fetch_log} row.
     *
     * @param startedAt when the run started
     * @param status how it ended
     */
    public record Run(Instant startedAt, String status) {}

    /**
     * One gap.
     *
     * @param from the start of the gap
     * @param to the end of the gap
     */
    public record Gap(Instant from, Instant to) {

        /** The gap's length. */
        public Duration length() {
            return Duration.between(from, to);
        }
    }

    /** The merged gaps of one source's runs in {@code [from, to]}. */
    public static List<Gap> gaps(List<Run> runs, Instant from, Instant to) {
        if (!to.isAfter(from)) {
            return List.of();
        }
        List<Run> sorted = new ArrayList<>();
        for (Run run : runs == null ? List.<Run>of() : runs) {
            if (run != null && run.startedAt() != null && !run.startedAt().isBefore(from)
                    && !run.startedAt().isAfter(to)) {
                sorted.add(run);
            }
        }
        if (sorted.isEmpty()) {
            return List.of(new Gap(from, to));
        }
        sorted.sort(Comparator.comparing(Run::startedAt));
        List<Gap> raw = new ArrayList<>();
        Instant failureStart = null;
        for (Run run : sorted) {
            if (FAILED.contains(run.status())) {
                if (failureStart == null) {
                    failureStart = run.startedAt();
                }
            } else if (SUCCEEDED.contains(run.status()) && failureStart != null) {
                raw.add(new Gap(failureStart, run.startedAt()));
                failureStart = null;
            }
        }
        if (failureStart != null) {
            raw.add(new Gap(failureStart, to));
        }
        Duration typical = medianSpacing(sorted);
        if (typical != null && !typical.isZero()) {
            Duration limit = typical.multipliedBy(SILENCE_FACTOR);
            for (int i = 1; i < sorted.size(); i++) {
                Instant previous = sorted.get(i - 1).startedAt();
                Instant next = sorted.get(i).startedAt();
                if (Duration.between(previous, next).compareTo(limit) > 0) {
                    raw.add(new Gap(previous, next));
                }
            }
            if (Duration.between(sorted.getLast().startedAt(), to).compareTo(limit) > 0) {
                raw.add(new Gap(sorted.getLast().startedAt(), to));
            }
            if (Duration.between(from, sorted.getFirst().startedAt()).compareTo(limit) > 0) {
                raw.add(new Gap(from, sorted.getFirst().startedAt()));
            }
        }
        return merge(raw);
    }

    /** The fraction of {@code [from, to]} the gaps cover (0–1). */
    public static double fraction(List<Gap> gaps, Instant from, Instant to) {
        long span = Duration.between(from, to).toMillis();
        if (span <= 0) {
            return 0;
        }
        long covered = 0;
        for (Gap gap : merge(gaps)) {
            Instant start = gap.from().isBefore(from) ? from : gap.from();
            Instant end = gap.to().isAfter(to) ? to : gap.to();
            if (end.isAfter(start)) {
                covered += Duration.between(start, end).toMillis();
            }
        }
        return Math.min(1.0, (double) covered / span);
    }

    /** Overlapping or touching gaps merged, sorted by start. */
    static List<Gap> merge(List<Gap> gaps) {
        List<Gap> sorted = new ArrayList<>(gaps);
        sorted.sort(Comparator.comparing(Gap::from));
        List<Gap> out = new ArrayList<>();
        for (Gap gap : sorted) {
            if (!out.isEmpty() && !gap.from().isAfter(out.getLast().to())) {
                Gap last = out.removeLast();
                out.add(new Gap(last.from(), gap.to().isAfter(last.to()) ? gap.to() : last.to()));
            } else {
                out.add(gap);
            }
        }
        return out;
    }

    /** The median spacing between consecutive runs, or null with fewer than two runs. */
    static Duration medianSpacing(List<Run> sorted) {
        if (sorted.size() < 2) {
            return null;
        }
        List<Long> spacings = new ArrayList<>();
        for (int i = 1; i < sorted.size(); i++) {
            spacings.add(Duration.between(sorted.get(i - 1).startedAt(), sorted.get(i).startedAt()).toMillis());
        }
        spacings.sort(Long::compareTo);
        return Duration.ofMillis(spacings.get(spacings.size() / 2));
    }
}
