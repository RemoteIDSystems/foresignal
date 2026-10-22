package ai.foresignal.forecast.score;

import ai.foresignal.forecast.ForecastCalibrationEntity;
import ai.foresignal.forecast.ForecastScoreEntity;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.Ledger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Builds scorecards, calibration reports and pairwise comparisons from score rows (prompt 122,
 * Intelligence-Design §8.3). Pure: the same rows give the same report, including the bootstrap intervals (INT-1).
 *
 * <ul>
 *   <li>A scorecard entry per forecaster: mean Brier, log score, RPS, CRPS and time-averaged Brier; interval coverage;
 *       the Murphy decomposition; skill {@code 1 − S / S_ref} against the out-of-sample {@code base-rate} forecaster
 *       (Brier, binary and multiple) or {@code climatology} (CRPS, numeric) on the questions both scored, with a
 *       paired bootstrap interval; the bias monitor; the Platt fit and ensemble weight when a category is given.</li>
 *   <li>Fewer than {@value #MIN_RESOLVED} resolved questions are labeled {@code insufficient}.</li>
 * </ul>
 */
public final class Scorecards {

    /** The resolved count below which a result is labeled {@code insufficient}. */
    public static final int MIN_RESOLVED = 20;

    private Scorecards() {}

    /**
     * The filters of a report.
     *
     * @param forecasterId one forecaster, or null for all
     * @param category one category, or null for all
     * @param from resolved at or after, or null
     * @param to resolved before, or null
     */
    public record Filter(String forecasterId, String category, Instant from, Instant to) {

        /** True when a row passes the category and time filters (not the forecaster filter). */
        public boolean admits(ForecastScoreEntity s) {
            return (category == null || category.equals(s.getCategory()))
                    && (from == null || !s.getResolvedAt().isBefore(from))
                    && (to == null || s.getResolvedAt().isBefore(to));
        }
    }

    /**
     * Builds a {@code ForecastScorecard}.
     *
     * @param rows every score row (references are read from them too)
     * @param filter the filters
     * @param retrospective true for an evaluation's scorecard
     * @param calibrations Platt fits by {@code forecaster|category}
     * @param weights ensemble weights by forecaster (the category's), or empty
     * @param now the computation time
     */
    public static Map<String, Object> scorecard(List<ForecastScoreEntity> rows, Filter filter, boolean retrospective,
            Map<String, ForecastCalibrationEntity> calibrations, Map<String, Double> weights, Instant now) {
        Map<String, List<ForecastScoreEntity>> byForecaster = new LinkedHashMap<>();
        List<ForecastScoreEntity> admitted = new ArrayList<>();
        for (ForecastScoreEntity s : rows) {
            if (filter.admits(s)) {
                admitted.add(s);
                if (filter.forecasterId() == null || filter.forecasterId().equals(s.getForecasterId())) {
                    byForecaster.computeIfAbsent(s.getForecasterId(), k -> new ArrayList<>()).add(s);
                }
            }
        }
        List<Map<String, Object>> entries = new ArrayList<>();
        for (Map.Entry<String, List<ForecastScoreEntity>> e : byForecaster.entrySet()) {
            entries.add(entry(e.getKey(), e.getValue(), admitted, filter.category(), calibrations, weights));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("forecasterId", filter.forecasterId());
        out.put("category", filter.category());
        out.put("from", filter.from() == null ? null : filter.from().toString());
        out.put("to", filter.to() == null ? null : filter.to().toString());
        out.put("retrospective", retrospective);
        out.put("entries", entries);
        out.put("scorerVersion", ForecastScorer.VERSION);
        out.put("computedAt", now.toString());
        return out;
    }

    /** One {@code ScorecardEntry}. */
    static Map<String, Object> entry(String forecasterId, List<ForecastScoreEntity> mine,
            List<ForecastScoreEntity> admitted, String category, Map<String, ForecastCalibrationEntity> calibrations,
            Map<String, Double> weights) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("forecasterId", forecasterId);
        out.put("forecasterType", mine.get(mine.size() - 1).getForecasterType());
        out.put("category", category);
        out.put("nResolved", mine.size());
        out.put("nForecasts", mine.stream().mapToInt(ForecastScoreEntity::getNForecasts).sum());
        out.put("label", mine.size() < MIN_RESOLVED ? "insufficient" : "ok");
        out.put("brier", mean(mine, ForecastScoreEntity::getBrier));
        out.put("logScore", mean(mine, ForecastScoreEntity::getLogScore));
        out.put("rps", mean(mine, ForecastScoreEntity::getRps));
        out.put("crps", mean(mine, ForecastScoreEntity::getCrps));
        out.put("timeAveragedBrier", mean(mine, ForecastScoreEntity::getTimeAveragedBrier));
        Map<String, Object> coverage = new LinkedHashMap<>();
        coverage.put("p05p95", hitShare(mine, "p05p95"));
        coverage.put("p25p75", hitShare(mine, "p25p75"));
        out.put("intervalCoverage", coverage);
        double[][] binary = binary(mine);
        Calibration.Murphy murphy = Calibration.murphy(binary[0], binary[1]);
        out.put("murphy", murphy == null ? null : murphyView(murphy));
        out.put("skill", skill(forecasterId, mine, admitted, category));
        int hedged = 0;
        int forecasts = 0;
        for (ForecastScoreEntity s : mine) {
            if (s.getFinalProbability() != null) {
                hedged += s.getHedgedForecasts();
                forecasts += s.getNForecasts();
            }
        }
        Skill.Bias bias = Skill.bias(binary[0], binary[1], hedged, forecasts);
        if (bias == null) {
            out.put("bias", null);
        } else {
            Map<String, Object> b = new LinkedHashMap<>();
            b.put("meanForecastMinusObserved", bias.meanForecastMinusObserved());
            b.put("hedgedShare", bias.hedgedShare());
            b.put("meanForecastOnNo", bias.meanForecastOnNo());
            out.put("bias", b);
        }
        ForecastCalibrationEntity cal = category == null ? null
                : calibrations.get(ForecastCalibrationEntity.key(forecasterId, category));
        if (cal != null && cal.hasPlatt()) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("a", cal.getPlattA());
            r.put("b", cal.getPlattB());
            r.put("n", cal.getPlattN());
            r.put("fittedAt", cal.getPlattFittedAt() == null ? null : cal.getPlattFittedAt().toString());
            out.put("recalibration", r);
        } else {
            out.put("recalibration", null);
        }
        out.put("ensembleWeight", weights.get(forecasterId));
        return out;
    }

    /** {@code skill} of an entry, or null without a reference on shared questions. */
    static Map<String, Object> skill(String forecasterId, List<ForecastScoreEntity> mine,
            List<ForecastScoreEntity> admitted, String category) {
        boolean brier = mine.stream().anyMatch(s -> s.getBrier() != null);
        String reference = brier ? ForecasterRegistry.BASE_RATE : ForecasterRegistry.CLIMATOLOGY;
        if (reference.equals(forecasterId)) {
            return null;
        }
        Function<ForecastScoreEntity, Double> metric = brier ? ForecastScoreEntity::getBrier
                : ForecastScoreEntity::getCrps;
        Map<String, Double> refByQuestion = new LinkedHashMap<>();
        for (ForecastScoreEntity s : admitted) {
            if (reference.equals(s.getForecasterId()) && metric.apply(s) != null) {
                refByQuestion.put(s.getQuestionId(), metric.apply(s));
            }
        }
        List<Double> a = new ArrayList<>();
        List<Double> r = new ArrayList<>();
        for (ForecastScoreEntity s : mine) {
            Double v = metric.apply(s);
            Double ref = refByQuestion.get(s.getQuestionId());
            if (v != null && ref != null) {
                a.add(v);
                r.add(ref);
            }
        }
        if (a.isEmpty()) {
            return null;
        }
        double[] sa = a.stream().mapToDouble(Double::doubleValue).toArray();
        double[] sr = r.stream().mapToDouble(Double::doubleValue).toArray();
        double value = Skill.skill(Skill.mean(sa), Skill.mean(sr));
        if (Double.isNaN(value)) {
            return null;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("value", value);
        out.put("reference", reference);
        out.put("nShared", sa.length);
        double[] interval = Skill.pairedSkill(sa, sr, Skill.seed("forecast-skill", forecasterId, reference,
                Objects.toString(category)));
        out.put("interval", interval == null ? null : interval(interval));
        return out;
    }

    /**
     * Builds a {@code ForecastCalibration} from the final binary forecasts.
     *
     * @param rows every score row
     * @param filter the filters (a null forecaster pools every forecaster)
     * @param now the computation time
     */
    public static Map<String, Object> calibration(List<ForecastScoreEntity> rows, Filter filter, Instant now) {
        List<ForecastScoreEntity> mine = new ArrayList<>();
        List<ForecastScoreEntity> numericRows = new ArrayList<>();
        for (ForecastScoreEntity s : rows) {
            if (!filter.admits(s)
                    || (filter.forecasterId() != null && !filter.forecasterId().equals(s.getForecasterId()))) {
                continue;
            }
            if (s.getFinalProbability() != null && s.getObserved() != null) {
                mine.add(s);
            } else if (Ledger.NUMERIC.equals(s.getQuestionType()) && s.getQuantileCoverage() != null
                    && !s.getQuantileCoverage().isEmpty()) {
                numericRows.add(s);
            }
        }
        // 152: a numeric-only forecaster or question gets the PIT / quantile-reliability report; the binary path
        // (any final-probability rows present) is unchanged.
        if (mine.isEmpty() && !numericRows.isEmpty()) {
            return numericCalibration(numericRows, filter, now);
        }
        double[][] binary = binary(mine);
        int n = binary[0].length;
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("forecasterId", filter.forecasterId());
        out.put("category", filter.category());
        out.put("n", n);
        out.put("label", n < MIN_RESOLVED ? "insufficient" : "ok");
        boolean corp = n >= Calibration.CORP_MIN;
        out.put("method", corp ? "corp" : "bins");
        List<Map<String, Object>> curve = new ArrayList<>();
        Map<String, Object> decomposition = null;
        if (corp) {
            Calibration.Corp result = Calibration.corp(binary[0], binary[1]);
            for (Calibration.Point p : result.curve()) {
                Map<String, Object> point = new LinkedHashMap<>();
                point.put("forecast", p.forecast());
                point.put("calibrated", p.calibrated());
                curve.add(point);
            }
            decomposition = new LinkedHashMap<>();
            decomposition.put("score", result.score());
            decomposition.put("miscalibration", result.miscalibration());
            decomposition.put("discrimination", result.discrimination());
            decomposition.put("uncertainty", result.uncertainty());
        }
        out.put("curve", curve);
        List<Map<String, Object>> bins = new ArrayList<>();
        for (Calibration.Bin b : Calibration.bins(binary[0], binary[1])) {
            Map<String, Object> bin = new LinkedHashMap<>();
            bin.put("lower", b.lower());
            bin.put("upper", b.upper());
            bin.put("n", b.n());
            bin.put("meanForecast", b.meanForecast());
            bin.put("observedFrequency", b.observedFrequency());
            bins.add(bin);
        }
        out.put("bins", bins);
        out.put("decomposition", decomposition);
        out.put("sharpness", Calibration.sharpness(binary[0]));
        out.put("scorerVersion", ForecastScorer.VERSION);
        out.put("computedAt", now.toString());
        return out;
    }

    /**
     * Builds a numeric {@code ForecastCalibration} (prompt 152) from resolved numeric questions: the
     * quantile-reliability diagram (the empirical coverage {@code P(y ≤ q_τ)} of each stated level vs its nominal
     * level τ), a PIT histogram over the required-quantile bins, and a calibration-error summary. A well-calibrated
     * forecaster has each level's coverage near its nominal and a near-uniform PIT (each bin's frequency near its
     * width).
     *
     * @param mine the forecaster's numeric rows (each with non-null, non-empty {@code quantileCoverage})
     * @param filter the filters
     * @param now the computation time
     */
    static Map<String, Object> numericCalibration(List<ForecastScoreEntity> mine, Filter filter, Instant now) {
        int n = mine.size();
        // Quantile-reliability: covered / n per stored level, in level order.
        Map<String, int[]> perLevel = new LinkedHashMap<>();
        for (String level : Ledger.QUANTILES) {
            perLevel.put(level, new int[2]);
        }
        // PIT histogram over the required-quantile bins: [0,.05),[.05,.25),[.25,.5),[.5,.75),[.75,.95),[.95,1].
        int[] pitCounts = new int[Ledger.REQUIRED_QUANTILES.size() + 1];
        int pitN = 0;
        for (ForecastScoreEntity s : mine) {
            Map<String, Boolean> coverage = s.getQuantileCoverage();
            for (String level : Ledger.QUANTILES) {
                Boolean covered = coverage.get(level);
                if (covered != null) {
                    perLevel.get(level)[1]++;
                    if (covered) {
                        perLevel.get(level)[0]++;
                    }
                }
            }
            Integer bin = pitBin(coverage);
            if (bin != null) {
                pitCounts[bin]++;
                pitN++;
            }
        }
        List<Map<String, Object>> reliability = new ArrayList<>();
        double errorSum = 0;
        double maxDeviation = 0;
        int levelsWithData = 0;
        for (String level : Ledger.QUANTILES) {
            int[] counts = perLevel.get(level);
            if (counts[1] == 0) {
                continue;
            }
            double nominal = Ledger.level(level);
            double empirical = counts[0] / (double) counts[1];
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("level", nominal);
            point.put("quantile", level);
            point.put("nominal", nominal);
            point.put("empirical", empirical);
            point.put("n", counts[1]);
            reliability.add(point);
            errorSum += Math.abs(empirical - nominal);
            maxDeviation = Math.max(maxDeviation, Math.abs(empirical - nominal));
            levelsWithData++;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("forecasterId", filter.forecasterId());
        out.put("category", filter.category());
        out.put("type", "numeric");
        out.put("n", n);
        out.put("label", n < MIN_RESOLVED ? "insufficient" : "ok");
        out.put("quantileReliability", reliability);
        out.put("pit", pitHistogram(pitCounts, pitN));
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("meanAbsoluteCoverageError", levelsWithData == 0 ? null : errorSum / levelsWithData);
        summary.put("maxCoverageDeviation", levelsWithData == 0 ? null : maxDeviation);
        out.put("summary", summary);
        out.put("scorerVersion", ForecastScorer.VERSION);
        out.put("computedAt", now.toString());
        return out;
    }

    /** The PIT bin of one outcome: the first required level it is at or below, or the last bin when above all. */
    private static Integer pitBin(Map<String, Boolean> coverage) {
        List<String> levels = Ledger.REQUIRED_QUANTILES;
        for (int i = 0; i < levels.size(); i++) {
            Boolean covered = coverage.get(levels.get(i));
            if (covered == null) {
                return null;
            }
            if (covered) {
                return i;
            }
        }
        return levels.size();
    }

    /** The PIT histogram: one bin per required-quantile interval, with its count, frequency and expected width. */
    private static Map<String, Object> pitHistogram(int[] counts, int n) {
        List<Double> edges = new ArrayList<>();
        edges.add(0.0);
        for (String level : Ledger.REQUIRED_QUANTILES) {
            edges.add(Ledger.level(level));
        }
        edges.add(1.0);
        List<Map<String, Object>> bins = new ArrayList<>();
        for (int i = 0; i < counts.length; i++) {
            double lower = edges.get(i);
            double upper = edges.get(i + 1);
            Map<String, Object> bin = new LinkedHashMap<>();
            bin.put("lower", lower);
            bin.put("upper", upper);
            bin.put("count", counts[i]);
            bin.put("frequency", n == 0 ? null : counts[i] / (double) n);
            bin.put("expected", upper - lower);
            bins.add(bin);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("n", n);
        out.put("bins", bins);
        return out;
    }

    /**
     * Builds a {@code ForecasterComparison} on shared questions (Brier).
     *
     * @param rows every score row
     * @param a the first forecaster
     * @param b the second forecaster
     * @param category the category, or null
     * @param now the computation time
     */
    public static Map<String, Object> compare(List<ForecastScoreEntity> rows, String a, String b, String category,
            Instant now) {
        Map<String, Double> byA = new LinkedHashMap<>();
        Map<String, Double> byB = new LinkedHashMap<>();
        for (ForecastScoreEntity s : rows) {
            if ((category != null && !category.equals(s.getCategory())) || s.getBrier() == null) {
                continue;
            }
            if (a.equals(s.getForecasterId())) {
                byA.put(s.getQuestionId(), s.getBrier());
            }
            if (b.equals(s.getForecasterId())) {
                byB.put(s.getQuestionId(), s.getBrier());
            }
        }
        List<Double> sa = new ArrayList<>();
        List<Double> sb = new ArrayList<>();
        for (Map.Entry<String, Double> e : byA.entrySet()) {
            Double other = byB.get(e.getKey());
            if (other != null) {
                sa.add(e.getValue());
                sb.add(other);
            }
        }
        double[] xa = sa.stream().mapToDouble(Double::doubleValue).toArray();
        double[] xb = sb.stream().mapToDouble(Double::doubleValue).toArray();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("a", a);
        out.put("b", b);
        out.put("category", category);
        out.put("nShared", xa.length);
        out.put("label", xa.length < MIN_RESOLVED ? "insufficient" : "ok");
        out.put("metric", "brier");
        out.put("meanScoreA", xa.length == 0 ? null : Skill.mean(xa));
        out.put("meanScoreB", xb.length == 0 ? null : Skill.mean(xb));
        out.put("meanDifference", xa.length == 0 ? null : Skill.mean(xa) - Skill.mean(xb));
        double[] interval = Skill.pairedDifference(xa, xb, Skill.seed("forecast-compare", a, b,
                Objects.toString(category)));
        out.put("interval", interval == null ? null : interval(interval));
        out.put("scorerVersion", ForecastScorer.VERSION);
        out.put("computedAt", now.toString());
        return out;
    }

    /** The headline track record of a forecaster ({@code nResolved}, mean {@code brier}, {@code label}). */
    public static Map<String, Object> trackRecord(List<ForecastScoreEntity> mine) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("nResolved", mine.size());
        out.put("brier", mean(mine, ForecastScoreEntity::getBrier));
        out.put("label", mine.size() < MIN_RESOLVED ? "insufficient" : "ok");
        return out;
    }

    /** The final probabilities and outcomes of the binary rows: {@code {p[], o[]}}. */
    static double[][] binary(List<ForecastScoreEntity> rows) {
        List<double[]> pairs = new ArrayList<>();
        for (ForecastScoreEntity s : rows) {
            if (s.getFinalProbability() != null && s.getObserved() != null) {
                pairs.add(new double[] {s.getFinalProbability(), s.getObserved()});
            }
        }
        double[] p = new double[pairs.size()];
        double[] o = new double[pairs.size()];
        for (int i = 0; i < pairs.size(); i++) {
            p[i] = pairs.get(i)[0];
            o[i] = pairs.get(i)[1];
        }
        return new double[][] {p, o};
    }

    private static Map<String, Object> murphyView(Calibration.Murphy m) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("reliability", m.reliability());
        out.put("resolution", m.resolution());
        out.put("uncertainty", m.uncertainty());
        out.put("withinBinVariance", m.withinBinVariance());
        out.put("withinBinCovariance", m.withinBinCovariance());
        return out;
    }

    private static Map<String, Object> interval(double[] bounds) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("lower", bounds[0]);
        out.put("upper", bounds[1]);
        out.put("level", Skill.LEVEL);
        out.put("method", "paired-bootstrap");
        return out;
    }

    private static Double mean(List<ForecastScoreEntity> rows, Function<ForecastScoreEntity, Double> f) {
        double sum = 0;
        int n = 0;
        for (ForecastScoreEntity s : rows) {
            Double v = f.apply(s);
            if (v != null && !v.isNaN()) {
                sum += v;
                n++;
            }
        }
        return n == 0 ? null : sum / n;
    }

    private static Double hitShare(List<ForecastScoreEntity> rows, String key) {
        int hits = 0;
        int n = 0;
        for (ForecastScoreEntity s : rows) {
            Map<String, Boolean> h = s.getIntervalHits();
            if (h != null && h.get(key) != null) {
                n++;
                if (h.get(key)) {
                    hits++;
                }
            }
        }
        return n == 0 ? null : hits / (double) n;
    }
}
