package ai.foresignal.backtest;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.DoubleConsumer;

/**
 * The step loop of a windowed (count or absence) rule backtest (prompt 103): walks the rule's step
 * instants over the range and counts how many evaluations ran and how many came out unknown, for the
 * {@code window} block of {@code RuleBacktestResult}. Pure: the evaluation of one step is supplied by
 * the caller, which resolves it through {@code WindowMembership} and {@code CoverageGate} bounded by
 * the step instant (INT-2).
 *
 * <p>The step instants follow {@code WindowTester}: aligned to multiples of the step since the epoch,
 * from the first instant whose whole window lies inside the range, up to and including the range end.
 * A range shorter than one window has no steps.
 */
public final class WindowReplay {

    private WindowReplay() {}

    /** Evaluates one step. */
    @FunctionalInterface
    public interface StepEvaluator {

        /**
         * Whether the window {@code (start, at]} evaluated at {@code at} is unknown.
         *
         * @param start the exclusive window start
         * @param at the step instant (the inclusive window end and the decision time)
         * @return true when the evaluation is unknown
         */
        boolean unknown(Instant start, Instant at);
    }

    /**
     * The counts of a replay.
     *
     * @param steps the step instants evaluated
     * @param unknownSteps the steps whose evaluation was unknown
     */
    public record Counts(long steps, long unknownSteps) {

        /**
         * The {@code window} block of {@code RuleBacktestResult}.
         *
         * @return {@code {steps, unknownSteps}}
         */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("steps", steps);
            out.put("unknownSteps", unknownSteps);
            return out;
        }
    }

    /**
     * The first step instant whose whole window lies at or after {@code from}.
     *
     * @param from the range start
     * @param window the window length
     * @param step the step
     * @return the first step instant
     * @throws IllegalArgumentException when the step is shorter than one second
     */
    public static Instant firstStep(Instant from, Duration window, Duration step) {
        if (step.toSeconds() < 1) {
            throw new IllegalArgumentException("step must be at least one second");
        }
        Instant earliest = from.plus(window);
        long seconds = step.toSeconds();
        Instant at = Instant.ofEpochSecond(Math.floorDiv(earliest.getEpochSecond(), seconds) * seconds);
        return at.isBefore(earliest) ? at.plus(step) : at;
    }

    /**
     * The number of step instants in the range.
     *
     * @param from the range start
     * @param to the range end (inclusive)
     * @param window the window length
     * @param step the step
     * @return the number of steps, zero when the range is shorter than one window
     */
    public static long stepCount(Instant from, Instant to, Duration window, Duration step) {
        Instant first = firstStep(from, window, step);
        if (first.isAfter(to)) {
            return 0;
        }
        return Duration.between(first, to).toSeconds() / step.toSeconds() + 1;
    }

    /**
     * Evaluates every step instant in the range in time order.
     *
     * @param from the range start
     * @param to the range end (inclusive)
     * @param window the window length
     * @param step the step (at least one second)
     * @param evaluator evaluates one step
     * @param progress receives the fraction of steps done before each step (it may throw to cancel)
     * @return the counts
     */
    public static Counts replay(Instant from, Instant to, Duration window, Duration step, StepEvaluator evaluator,
            DoubleConsumer progress) {
        long total = stepCount(from, to, window, step);
        long steps = 0;
        long unknown = 0;
        for (Instant at = firstStep(from, window, step); !at.isAfter(to); at = at.plus(step)) {
            progress.accept(total == 0 ? 0 : (double) steps / total);
            if (evaluator.unknown(at.minus(window), at)) {
                unknown++;
            }
            steps++;
        }
        return new Counts(steps, unknown);
    }
}
