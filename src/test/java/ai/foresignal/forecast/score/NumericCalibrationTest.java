package ai.foresignal.forecast.score;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.forecast.ForecastScoreEntity;
import ai.foresignal.forecast.Ledger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Numeric / quantile calibration: well-calibrated quantiles score near-nominal coverage (prompt 152). */
class NumericCalibrationTest {

    private static final Instant NOW = Instant.parse("2026-10-22T00:00:00Z");

    private static final Scorecards.Filter ALL = new Scorecards.Filter(null, null, null, null);

    /**
     * One resolved numeric row whose outcome has PIT value {@code pit}: {@code y ≤ q_τ} exactly when
     * {@code pit ≤ τ}, which is how a true PIT relates to the quantile levels.
     */
    private static ForecastScoreEntity row(String id, double pit) {
        ForecastScoreEntity s = new ForecastScoreEntity("q-" + id, "timesfm");
        s.setQuestionType(Ledger.NUMERIC);
        s.setCategory("markets");
        s.setResolvedAt(NOW);
        s.setForecasterType("external");
        s.setScorerVersion(ForecastScorer.VERSION);
        Map<String, Boolean> coverage = new LinkedHashMap<>();
        for (String level : Ledger.QUANTILES) {
            coverage.put(level, pit <= Ledger.level(level));
        }
        s.setQuantileCoverage(coverage);
        return s;
    }

    /** {@code n} rows whose PIT values are evenly spread over (0, 1): a well-calibrated forecaster. */
    private static List<ForecastScoreEntity> calibrated(int n) {
        List<ForecastScoreEntity> rows = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            rows.add(row(Integer.toString(i), (i + 0.5) / n));
        }
        return rows;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> reliability(Map<String, Object> report) {
        return (List<Map<String, Object>>) report.get("quantileReliability");
    }

    @Test
    @DisplayName("evenly spread outcomes give coverage near each nominal level")
    void wellCalibrated() {
        Map<String, Object> report = Scorecards.calibration(calibrated(200), ALL, NOW);
        assertThat(report).containsEntry("type", "numeric").containsEntry("label", "ok");
        for (Map<String, Object> point : reliability(report)) {
            double nominal = (double) point.get("nominal");
            double empirical = (double) point.get("empirical");
            assertThat(empirical).as("coverage at %s", point.get("quantile")).isCloseTo(nominal, Offset.offset(0.03));
        }
        Map<String, Object> summary = (Map<String, Object>) report.get("summary");
        assertThat((double) summary.get("meanAbsoluteCoverageError")).isLessThan(0.03);
    }

    @Test
    @DisplayName("outcomes always in the low tail are over-covered and flagged by a large coverage error")
    void biased() {
        // Every outcome sits below p05 (PIT ~ 0), so every quantile over-covers: empirical coverage is ~1 at
        // every level, far above the nominal levels.
        List<ForecastScoreEntity> rows = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            rows.add(row(Integer.toString(i), 0.001));
        }
        Map<String, Object> report = Scorecards.calibration(rows, ALL, NOW);
        Map<String, Object> summary = (Map<String, Object>) report.get("summary");
        assertThat((double) summary.get("meanAbsoluteCoverageError")).isGreaterThan(0.3);
        for (Map<String, Object> point : reliability(report)) {
            assertThat((double) point.get("empirical")).isGreaterThanOrEqualTo((double) point.get("nominal"));
        }
    }

    @Test
    @DisplayName("a binary forecaster still gets the binary calibration")
    void binaryUnchanged() {
        ForecastScoreEntity binary = new ForecastScoreEntity("q-b", "base-rate");
        binary.setQuestionType(Ledger.BINARY);
        binary.setCategory("markets");
        binary.setResolvedAt(NOW);
        binary.setForecasterType("builtin");
        binary.setScorerVersion(ForecastScorer.VERSION);
        binary.setFinalProbability(0.6);
        binary.setObserved(1.0);
        Map<String, Object> report = Scorecards.calibration(List.of(binary), ALL, NOW);
        assertThat(report).doesNotContainKey("quantileReliability");
        assertThat(report).containsKey("bins");
    }
}
