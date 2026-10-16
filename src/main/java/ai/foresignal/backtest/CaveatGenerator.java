package ai.foresignal.backtest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds the caveats of a backtest (prompt 103). Pure: given what was observed while resolving the
 * range, it emits every caveat that applies, always including {@code NOT_GROUND_TRUTH}.
 */
public final class CaveatGenerator {

    private CaveatGenerator() {}

    /**
     * The inputs the caveats are built from.
     *
     * @param rangeClampedByRetention the range was clamped to the retained floor
     * @param downsampledRange the range overlaps downsampled TRACK history
     * @param sampledSources SAMPLED or LATEST sources involved
     * @param coverageGaps true when a source had FAILED/REFUSED runs or a long no-row interval
     * @param definitionDiffers the snapshot differs from the current definition
     * @param entityResolutionChanged 098 links changed in range
     * @param windowApproximate a windowed rule replayed from versions only
     * @param sampleSize the number of antecedent or matched samples, for {@code SMALL_SAMPLE}
     */
    public record Inputs(boolean rangeClampedByRetention, boolean downsampledRange, boolean sampledSources,
            boolean coverageGaps, boolean definitionDiffers, boolean entityResolutionChanged,
            boolean windowApproximate, int sampleSize) {}

    /** The caveats, in a stable order. */
    public static List<BacktestCaveat> generate(Inputs inputs) {
        List<BacktestCaveat> out = new ArrayList<>();
        if (inputs.rangeClampedByRetention()) {
            out.add(BacktestCaveat.of(BacktestCaveat.RANGE_CLAMPED_BY_RETENTION,
                    "The requested range starts before the retained floor and was clamped."));
        }
        if (inputs.downsampledRange()) {
            out.add(BacktestCaveat.of(BacktestCaveat.DOWNSAMPLED_RANGE,
                    "The range overlaps downsampled history; edges and samples between kept rows are missing."));
        }
        if (inputs.sampledSources()) {
            out.add(BacktestCaveat.of(BacktestCaveat.SAMPLED_SOURCE,
                    "SAMPLED or LATEST sources are involved; values between persisted versions were never stored."));
        }
        if (inputs.coverageGaps()) {
            out.add(BacktestCaveat.of(BacktestCaveat.COVERAGE_GAPS,
                    "Some sources had failed runs or long gaps in the range; absence and downward results inside"
                            + " gaps are excluded."));
        }
        if (inputs.definitionDiffers()) {
            out.add(BacktestCaveat.of(BacktestCaveat.DEFINITION_DIFFERS,
                    "The snapshot differs from the current rule or recipe."));
        }
        if (inputs.entityResolutionChanged()) {
            out.add(BacktestCaveat.of(BacktestCaveat.ENTITY_RESOLUTION_CHANGED,
                    "Entity links changed in the range; identity may differ from today's."));
        }
        if (inputs.windowApproximate()) {
            out.add(BacktestCaveat.of(BacktestCaveat.WINDOW_APPROXIMATE,
                    "Windowed rules are replayed from versions only, at keyframe resolution; wouldFire, perDay,"
                            + " topEntities and samples count the versions matching the rule's filter, not window"
                            + " transitions, while window reports the replayed step evaluations."));
        }
        if (inputs.sampleSize() < 10) {
            out.add(BacktestCaveat.of(BacktestCaveat.SMALL_SAMPLE, "Fewer than ten samples."));
        }
        out.add(BacktestCaveat.of(BacktestCaveat.NOT_GROUND_TRUTH,
                "Backtests replay what Foresignal stored, not ground truth."));
        return out;
    }

    /** The stored forms of the caveats. */
    public static List<Map<String, Object>> stored(List<BacktestCaveat> caveats) {
        return BacktestCaveat.toMaps(caveats);
    }
}
