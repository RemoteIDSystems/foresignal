package ai.foresignal.inbound.edp.binding;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.forecast.Ledger;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The decile mapping (prompt 155): a provider that emits only deciles (p10…p90, as TimesFM does) is mapped onto
 * the ledger's required quantiles (p05, p25, p50, p75, p95) binding-side, so the required set is never loosened.
 */
class DecileQuantilesTest {

    @Test
    @DisplayName("a full decile set maps to every required quantile by linear interpolation and extrapolation")
    void mapsDecilesOntoRequiredQuantiles() {
        Map<String, Double> deciles = new LinkedHashMap<>();
        for (int d = 10; d <= 90; d += 10) {
            deciles.put("p" + d, (double) d);
        }

        Map<String, Double> mapped = DecileQuantiles.toLedger(deciles);

        // Every required level is now present.
        assertThat(mapped.keySet()).containsAll(Ledger.REQUIRED_QUANTILES);
        // Interpolated within the provided range.
        assertThat(mapped.get("p25")).isEqualTo(25.0);
        assertThat(mapped.get("p50")).isEqualTo(50.0);
        assertThat(mapped.get("p75")).isEqualTo(75.0);
        // Extrapolated below p10 and above p90 from the two nearest points.
        assertThat(mapped.get("p05")).isEqualTo(5.0);
        assertThat(mapped.get("p95")).isEqualTo(95.0);
    }

    @Test
    @DisplayName("the mapped quantiles are non-decreasing even when the deciles crowd")
    void keepsOutputNonDecreasing() {
        Map<String, Double> deciles = new LinkedHashMap<>();
        deciles.put("p10", 1.0);
        deciles.put("p20", 1.0);
        deciles.put("p30", 1.0);
        deciles.put("p40", 1.0);
        deciles.put("p50", 1.0);
        deciles.put("p60", 1.0);
        deciles.put("p70", 1.0);
        deciles.put("p80", 1.0);
        deciles.put("p90", 1.0);

        Map<String, Double> mapped = DecileQuantiles.toLedger(deciles);

        double last = Double.NEGATIVE_INFINITY;
        for (String key : DecileQuantiles.OUTPUT) {
            assertThat(mapped.get(key)).isGreaterThanOrEqualTo(last);
            last = mapped.get(key);
        }
    }

    @Test
    @DisplayName("a set that already carries every required quantile is returned unchanged")
    void passesThroughCompleteSet() {
        Map<String, Double> complete = new LinkedHashMap<>();
        complete.put("p05", 5.0);
        complete.put("p25", 25.0);
        complete.put("p50", 50.0);
        complete.put("p75", 75.0);
        complete.put("p95", 95.0);

        Map<String, Double> mapped = DecileQuantiles.toLedger(complete);

        assertThat(mapped).isSameAs(complete);
    }

    @Test
    @DisplayName("a set with fewer than two points cannot be mapped and is returned as is")
    void leavesTooFewPointsAlone() {
        Map<String, Double> single = new LinkedHashMap<>();
        single.put("p50", 50.0);

        Map<String, Double> mapped = DecileQuantiles.toLedger(single);

        assertThat(mapped).isSameAs(single);
    }

    @Test
    @DisplayName("null and empty inputs are returned unchanged")
    void handlesNullAndEmpty() {
        assertThat(DecileQuantiles.toLedger(null)).isNull();
        Map<String, Double> empty = new LinkedHashMap<>();
        assertThat(DecileQuantiles.toLedger(empty)).isSameAs(empty);
    }
}
