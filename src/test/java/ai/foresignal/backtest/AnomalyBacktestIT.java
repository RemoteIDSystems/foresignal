package ai.foresignal.backtest;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.trigger.window.AnomalyDetector;
import ai.foresignal.trigger.window.Baseline;
import ai.foresignal.trigger.window.WindowSpec;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** No look-ahead (prompt 103): a later sample never changes an earlier bucket's verdict. */
class AnomalyBacktestIT {

    private static WindowSpec spec() {
        return new WindowSpec(Duration.ofHours(1), Duration.ofMinutes(15), "entity", null, 500, null, null,
                Duration.ofHours(1), "hour-of-week", "up", "medium", 3, 8, 0.8);
    }

    @Test
    @DisplayName("the baseline at bucket b uses only samples before b")
    void noLookAhead() {
        List<Integer> before = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            before.add(10);
        }
        Baseline baseline = Baseline.of(before, List.of(1.0), "hour-of-week", Duration.ofHours(1));
        AnomalyDetector.Result normal = AnomalyDetector.evaluate(11, baseline, 1.0, true, spec());
        List<Integer> withSpike = new ArrayList<>(before);
        withSpike.add(500);
        Baseline extended = Baseline.of(withSpike, List.of(1.0), "hour-of-week", Duration.ofHours(1));
        AnomalyDetector.Result alsoNormal = AnomalyDetector.evaluate(11, extended, 1.0, true, spec());
        assertThat(normal.classification()).isEqualTo(alsoNormal.classification());
    }
}
