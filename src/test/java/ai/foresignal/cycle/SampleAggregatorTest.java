package ai.foresignal.cycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.forecast.ForecastInputs.ForecastValue;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Sample aggregation (prompt 128 §3): median log-odds, per-outcome medians renormalized, monotone quantile medians. */
class SampleAggregatorTest {

    @Test
    @DisplayName("binary samples aggregate by the median of their log-odds")
    void binary() {
        ForecastValue v = SampleAggregator.aggregate("binary", List.of(ForecastValue.binary(0.2),
                ForecastValue.binary(0.6), ForecastValue.binary(0.3)));
        assertThat(v.probability()).isCloseTo(0.3, within(1e-9));
        ForecastValue even = SampleAggregator.aggregate("binary", List.of(ForecastValue.binary(0.2),
                ForecastValue.binary(0.8)));
        assertThat(even.probability()).isCloseTo(0.5, within(1e-9));
        assertThat(SampleAggregator.aggregate("binary", List.of(ForecastValue.binary(1.0))).probability())
                .isCloseTo(0.999, within(1e-9));
    }

    @Test
    @DisplayName("distributions take the median log-odds per outcome and sum to one")
    void distribution() {
        ForecastValue v = SampleAggregator.aggregate("multiple", List.of(
                ForecastValue.distribution(Map.of("a", 0.6, "b", 0.4)),
                ForecastValue.distribution(Map.of("a", 0.5, "b", 0.5)),
                ForecastValue.distribution(Map.of("a", 0.7, "b", 0.3))));
        double sum = v.distribution().values().stream().mapToDouble(Double::doubleValue).sum();
        assertThat(sum).isCloseTo(1.0, within(1e-9));
        assertThat(v.distribution().get("a")).isGreaterThan(v.distribution().get("b"));
    }

    @Test
    @DisplayName("quantiles take the median per quantile and stay monotone")
    void quantiles() {
        Map<String, Double> a = new LinkedHashMap<>(Map.of("p05", 1.0, "p50", 5.0, "p95", 9.0));
        Map<String, Double> b = new LinkedHashMap<>(Map.of("p05", 2.0, "p50", 3.0, "p95", 10.0));
        Map<String, Double> c = new LinkedHashMap<>(Map.of("p05", 6.0, "p50", 4.0, "p95", 11.0));
        ForecastValue v = SampleAggregator.aggregate("numeric", List.of(ForecastValue.quantiles(a),
                ForecastValue.quantiles(b), ForecastValue.quantiles(c)));
        assertThat(v.quantiles()).containsEntry("p05", 2.0).containsEntry("p50", 4.0).containsEntry("p95", 10.0);
        assertThat(List.copyOf(v.quantiles().keySet())).containsExactly("p05", "p50", "p95");
    }

    @Test
    @DisplayName("no usable sample gives nothing")
    void empty() {
        assertThat(SampleAggregator.aggregate("binary", List.of())).isNull();
        assertThat(SampleAggregator.aggregate("binary", Arrays.asList((ForecastValue) null))).isNull();
        assertThat(SampleAggregator.aggregate("numeric", List.of(ForecastValue.binary(0.4)))).isNull();
        assertThat(SampleAggregator.median(List.of(3.0, 1.0, 2.0))).isEqualTo(2.0);
    }
}
