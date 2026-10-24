package ai.foresignal.outlook.series;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Golden values of the {@code seasonal-naive} benchmark (prompt 126). */
class SeasonalNaiveTest {

    @Test
    @DisplayName("the point forecast repeats the last value of the same season slot, for every horizon")
    void point() {
        double[] y = {1, 2, 3, 4, 5, 6, 7, 11, 12, 13, 14, 15, 16, 17};
        assertThat(SeasonalNaive.point(y, 7, 1)).isEqualTo(11);
        assertThat(SeasonalNaive.point(y, 7, 7)).isEqualTo(17);
        assertThat(SeasonalNaive.point(y, 7, 8)).isEqualTo(11);
        assertThat(SeasonalNaive.point(y, 7, 28)).isEqualTo(17);
        // m = 1 is the naive forecast.
        assertThat(SeasonalNaive.point(y, 1, 5)).isEqualTo(17);
    }

    @Test
    @DisplayName("a perfectly seasonal series has zero spread: every quantile equals the point forecast")
    void perfectSeason() {
        double[] y = new double[28];
        for (int i = 0; i < y.length; i++) {
            y[i] = 10 + i % 7;
        }
        ForecastMethod.MethodForecast f = new SeasonalNaive().forecast(y, 7, false, new int[] {1, 7, 28});
        for (int h : new int[] {1, 7, 28}) {
            double expected = 10 + (y.length - 1 + h) % 7;
            for (double q : f.quantiles().get(h)) {
                assertThat(q).isEqualTo(expected);
            }
        }
        assertThat(f.params()).containsEntry("residualSd", 0.0);
    }

    @Test
    @DisplayName("intervals: normal with the seasonal-difference SD scaled by √⌈h/m⌉ (hand-computed)")
    void intervals() {
        // Seasonal differences (m = 2): y[t] − y[t−2] for t = 2..7 = 1, 3, 1, 3, 1, 3 → mean 2, sample SD √(6/5).
        double[] y = {0, 0, 1, 3, 2, 6, 3, 9};
        double sd = Math.sqrt(6.0 / 5.0);
        assertThat(SeasonalNaive.residualSd(y, 2)).isCloseTo(sd, within(1e-12));
        ForecastMethod.MethodForecast f = new SeasonalNaive().forecast(y, 2, false, new int[] {1, 3});
        double z95 = QuantileSet.z(0.95);
        assertThat(f.quantiles().get(1)[QuantileSet.P50]).isCloseTo(3, within(1e-12));
        assertThat(f.quantiles().get(1)[QuantileSet.P95]).isCloseTo(3 + z95 * sd, within(1e-9));
        // h = 3 with m = 2: k = 2, so the spread is √2 times larger.
        assertThat(f.quantiles().get(3)[QuantileSet.P95] - f.quantiles().get(3)[QuantileSet.P50])
                .isCloseTo(z95 * sd * Math.sqrt(2), within(1e-9));
    }

    @Test
    @DisplayName("count series are floored at zero; too short a history is refused")
    void countsAndShortHistory() {
        double[] y = {0, 5, 0, 0, 5, 0};
        ForecastMethod.MethodForecast f = new SeasonalNaive().forecast(y, 3, true, new int[] {1});
        for (double q : f.quantiles().get(1)) {
            assertThat(q).isGreaterThanOrEqualTo(0);
        }
        assertThatThrownBy(() -> new SeasonalNaive().forecast(new double[] {1, 2, 3}, 7, false, new int[] {1}))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
