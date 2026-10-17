package ai.foresignal.forecast.score;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.forecast.ForecastScoreEntity;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Skill and comparisons (prompt 122): skill against the out-of-sample base-rate forecaster on shared questions only,
 * the pairwise comparison restricted to shared questions, deterministic bootstrap intervals, the bias monitor and the
 * {@code insufficient} label.
 */
class SkillAndCompareTest {

    static final Instant AT = Instant.parse("2026-06-01T00:00:00Z");

    static ForecastScoreEntity row(String question, String forecaster, double p, boolean yes) {
        ForecastScoreEntity s = new ForecastScoreEntity(question, forecaster);
        s.setForecasterType(forecaster.startsWith("operator") ? "operator" : forecaster);
        s.setCategory("conflict");
        s.setQuestionType("binary");
        s.setResolvedAt(AT);
        s.setBrier(ForecastScorer.brier(p, yes));
        s.setLogScore(ForecastScorer.logScore(p, yes));
        s.setFinalProbability(p);
        s.setObserved(yes ? 1.0 : 0.0);
        s.setNForecasts(1);
        s.setHedgedForecasts(Skill.hedged(p) ? 1 : 0);
        s.setScorerVersion(ForecastScorer.VERSION);
        s.setComputedAt(AT);
        return s;
    }

    @Test
    @DisplayName("skill 1 − S / S_ref on the questions both forecasters scored, with a paired interval")
    void skillAgainstBaseRate() {
        List<ForecastScoreEntity> rows = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            boolean yes = i % 3 == 0;
            rows.add(row("q" + i, "base-rate", 0.33, yes));
            rows.add(row("q" + i, "ensemble", yes ? 0.7 : 0.15, yes));
        }
        // A question only the ensemble forecast is not shared and leaves the skill unchanged.
        rows.add(row("extra", "ensemble", 0.9, true));
        Map<String, Object> card = Scorecards.scorecard(rows, new Scorecards.Filter("ensemble", null, null, null),
                false, Map.of(), Map.of(), AT);
        @SuppressWarnings("unchecked")
        Map<String, Object> entry = ((List<Map<String, Object>>) card.get("entries")).get(0);
        @SuppressWarnings("unchecked")
        Map<String, Object> skill = (Map<String, Object>) entry.get("skill");
        double s = 0;
        double ref = 0;
        for (int i = 0; i < 30; i++) {
            boolean yes = i % 3 == 0;
            s += ForecastScorer.brier(yes ? 0.7 : 0.15, yes);
            ref += ForecastScorer.brier(0.33, yes);
        }
        assertThat((Double) skill.get("value")).isCloseTo(1 - s / ref, within(1e-12));
        assertThat(skill.get("reference")).isEqualTo("base-rate");
        assertThat(skill.get("nShared")).isEqualTo(30);
        assertThat(skill.get("interval")).isNotNull();
        assertThat(entry.get("label")).isEqualTo("ok");
        assertThat(entry.get("nResolved")).isEqualTo(31);
    }

    @Test
    @DisplayName("comparisons use shared questions only; the bootstrap interval is deterministic")
    void compare() {
        List<ForecastScoreEntity> rows = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            boolean yes = i % 2 == 0;
            rows.add(row("q" + i, "operator:ada", yes ? 0.8 : 0.3, yes));
            if (i < 22) {
                rows.add(row("q" + i, "base-rate", 0.5, yes));
            }
        }
        Map<String, Object> first = Scorecards.compare(rows, "operator:ada", "base-rate", null, AT);
        Map<String, Object> second = Scorecards.compare(rows, "operator:ada", "base-rate", null, AT);
        assertThat(first.get("nShared")).isEqualTo(22);
        assertThat(first.get("label")).isEqualTo("ok");
        assertThat((Double) first.get("meanDifference")).isNegative();
        assertThat(first.get("interval")).isEqualTo(second.get("interval"));
        @SuppressWarnings("unchecked")
        Map<String, Object> interval = (Map<String, Object>) first.get("interval");
        assertThat(interval.get("method")).isEqualTo("paired-bootstrap");
        assertThat((Double) interval.get("lower")).isLessThanOrEqualTo((Double) interval.get("upper"));
        // Under 20 shared questions the comparison is insufficient.
        assertThat(Scorecards.compare(rows.subList(0, 20), "operator:ada", "base-rate", null, AT).get("label"))
                .isEqualTo("insufficient");
    }

    @Test
    @DisplayName("bootstrap intervals depend only on the data and the seed")
    void bootstrapDeterminism() {
        double[] a = {0.1, 0.2, 0.05, 0.3, 0.15};
        double[] b = {0.2, 0.25, 0.1, 0.3, 0.2};
        long seed = Skill.seed("x", "y");
        assertThat(Skill.pairedDifference(a, b, seed)).containsExactly(Skill.pairedDifference(a, b, seed));
        assertThat(Skill.pairedSkill(a, b, seed)).containsExactly(Skill.pairedSkill(a, b, seed));
        assertThat(Skill.pairedDifference(new double[] {1}, new double[] {2}, seed)).isNull();
        assertThat(Skill.skill(0.1, 0.0)).isNaN();
    }

    @Test
    @DisplayName("bias monitor: mean forecast minus observed frequency, hedged share, mean forecast on no")
    void bias() {
        Skill.Bias b = Skill.bias(new double[] {0.9, 0.6, 0.5}, new double[] {1, 0, 0}, 2, 4);
        assertThat(b.meanForecastMinusObserved()).isCloseTo(2.0 / 3 - 1.0 / 3, within(1e-12));
        assertThat(b.hedgedShare()).isCloseTo(0.5, within(1e-12));
        assertThat(b.meanForecastOnNo()).isCloseTo(0.55, within(1e-12));
        assertThat(Skill.bias(new double[0], new double[0], 0, 0)).isNull();
    }

    @Test
    @DisplayName("fewer than 20 resolved questions are labeled insufficient; the reference has no skill of its own")
    void insufficient() {
        List<ForecastScoreEntity> rows = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            rows.add(row("q" + i, "base-rate", 0.4, i == 0));
        }
        Map<String, Object> card = Scorecards.scorecard(rows, new Scorecards.Filter(null, "conflict", null, null),
                false, Map.of(), Map.of(), AT);
        @SuppressWarnings("unchecked")
        Map<String, Object> entry = ((List<Map<String, Object>>) card.get("entries")).get(0);
        assertThat(entry.get("label")).isEqualTo("insufficient");
        assertThat(entry.get("skill")).isNull();
        assertThat(card.get("retrospective")).isEqualTo(false);
    }
}
