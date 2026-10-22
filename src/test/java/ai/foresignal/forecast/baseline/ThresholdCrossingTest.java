package ai.foresignal.forecast.baseline;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The crossing probability from a predictive quantile distribution (prompt 156): a threshold at the median is
 * about even, a threshold deep in a tail is near certainty, and the probability tracks the CDF in between.
 */
class ThresholdCrossingTest {

    private Map<String, Double> symmetric() {
        Map<String, Double> q = new LinkedHashMap<>();
        q.put("p05", 10.0);
        q.put("p25", 20.0);
        q.put("p50", 30.0);
        q.put("p75", 40.0);
        q.put("p95", 50.0);
        return q;
    }

    @Test
    @DisplayName("a threshold at the median is about even for either direction")
    void medianIsAboutEven() {
        assertThat(ThresholdCrossing.probability(symmetric(), "gt", 30.0)).isEqualTo(0.5);
        assertThat(ThresholdCrossing.probability(symmetric(), "gte", 30.0)).isEqualTo(0.5);
        assertThat(ThresholdCrossing.probability(symmetric(), "lt", 30.0)).isEqualTo(0.5);
        assertThat(ThresholdCrossing.probability(symmetric(), "lte", 30.0)).isEqualTo(0.5);
    }

    @Test
    @DisplayName("a threshold deep below the distribution makes exceedance near-certain and vice versa")
    void tailsAreNearCertainty() {
        // 5 is below p05 = 10.
        assertThat(ThresholdCrossing.probability(symmetric(), "gt", 5.0)).isEqualTo(ThresholdCrossing.MAX);
        assertThat(ThresholdCrossing.probability(symmetric(), "lt", 5.0)).isEqualTo(ThresholdCrossing.MIN);
        // 60 is above p95 = 50.
        assertThat(ThresholdCrossing.probability(symmetric(), "gt", 60.0)).isEqualTo(ThresholdCrossing.MIN);
        assertThat(ThresholdCrossing.probability(symmetric(), "lt", 60.0)).isEqualTo(ThresholdCrossing.MAX);
    }

    @Test
    @DisplayName("the probability tracks the CDF between quantiles")
    void tracksTheCdf() {
        // P(value > 20) = 1 - CDF(20) = 1 - 0.25 = 0.75.
        assertThat(ThresholdCrossing.probability(symmetric(), "gt", 20.0)).isEqualTo(0.75);
        // P(value <= 40) = CDF(40) = 0.75.
        assertThat(ThresholdCrossing.probability(symmetric(), "lte", 40.0)).isEqualTo(0.75);
        // A quarter of the way from p25 to p50 in value is a quarter of the way in level.
        assertThat(ThresholdCrossing.cdf(symmetric(), 25.0)).isEqualTo(0.375);
    }

    @Test
    @DisplayName("an equality op and a one-point distribution yield no answer")
    void unanswerableCases() {
        assertThat(ThresholdCrossing.probability(symmetric(), "eq", 30.0)).isNaN();
        Map<String, Double> single = new LinkedHashMap<>();
        single.put("p50", 30.0);
        assertThat(ThresholdCrossing.probability(single, "gt", 30.0)).isNaN();
    }

    @Test
    @DisplayName("crossing quantiles are rearranged so the CDF stays monotone")
    void rearrangesCrossedQuantiles() {
        Map<String, Double> crossed = new LinkedHashMap<>();
        crossed.put("p05", 10.0);
        crossed.put("p25", 40.0);
        crossed.put("p50", 30.0);
        crossed.put("p75", 45.0);
        crossed.put("p95", 50.0);
        double low = ThresholdCrossing.cdf(crossed, 20.0);
        double high = ThresholdCrossing.cdf(crossed, 48.0);
        assertThat(high).isGreaterThanOrEqualTo(low);
    }
}
