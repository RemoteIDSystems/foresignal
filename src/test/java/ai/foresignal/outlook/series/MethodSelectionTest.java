package ai.foresignal.outlook.series;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Rolling-origin method selection (prompt 126): the planted best method wins on mean pinball loss at the 7-day
 * horizon, and nothing after the decision time (a future spike) changes the selection (INT-2).
 */
class MethodSelectionTest {

    static final Instant START = Instant.parse("2025-01-06T00:00:00Z");

    static Instant[] starts(int n) {
        Instant[] out = new Instant[n];
        for (int i = 0; i < n; i++) {
            out[i] = START.plus(Duration.ofDays(i));
        }
        return out;
    }

    static Instant[] knownAt(Instant[] starts) {
        Instant[] out = new Instant[starts.length];
        for (int i = 0; i < starts.length; i++) {
            out[i] = starts[i].plus(Duration.ofDays(1));
        }
        return out;
    }

    @Test
    @DisplayName("a strongly weekly continuous series selects a seasonal method over the non-seasonal ones")
    void plantedSeasonal() {
        int n = 240;
        double[] y = new double[n];
        Random random = new Random(1);
        int[] pattern = {0, 10, 20, 15, 5, -25, -30};
        for (int t = 0; t < n; t++) {
            y[t] = 200 + pattern[t % 7] + random.nextGaussian();
        }
        Instant[] s = starts(n);
        MethodSelector.Selection sel = MethodSelector.select(s, y, knownAt(s), s[n - 1].plus(Duration.ofDays(1)),
                false, 7, 1);
        assertThat(sel.method()).isIn(SeasonalNaive.ID, EtsAaa.ID);
        assertThat(sel.origins()).hasSize(MethodSelector.ORIGINS);
        assertThat(sel.scores()).anySatisfy(sc -> {
            assertThat(sc.method()).isEqualTo(EtsAnn.ID);
            assertThat(sc.horizon()).isEqualTo(7);
        });
        double best = sel.scores().stream().filter(sc -> sc.horizon() == 7 && sc.method().equals(sel.method()))
                .findFirst().orElseThrow().meanPinballLoss();
        double ann = sel.scores().stream().filter(sc -> sc.horizon() == 7 && sc.method().equals(EtsAnn.ID))
                .findFirst().orElseThrow().meanPinballLoss();
        assertThat(best).isLessThan(ann);
        assertThat(sel.backtest()).containsKeys("origins", "spacing", "selectionHorizon", "results");
    }

    @Test
    @DisplayName("a random walk with drift and no season selects a non-seasonal method")
    void plantedTrend() {
        int n = 240;
        double[] y = new double[n];
        Random random = new Random(2);
        double level = 100;
        for (int t = 0; t < n; t++) {
            level += 0.8 + random.nextGaussian() * 0.5;
            y[t] = level;
        }
        Instant[] s = starts(n);
        MethodSelector.Selection sel = MethodSelector.select(s, y, knownAt(s), s[n - 1].plus(Duration.ofDays(1)),
                false, 7, 1);
        assertThat(sel.method()).isIn(DampedTrend.ID, EtsAaa.ID);
        assertThat(sel.method()).isNotEqualTo(SeasonalNaive.ID);
    }

    @Test
    @DisplayName("no look-ahead: values known after the decision (a later spike) leave the selection unchanged")
    void noLookAhead() {
        int n = 240;
        double[] y = new double[n + 30];
        Random random = new Random(3);
        for (int t = 0; t < y.length; t++) {
            y[t] = 50 + 5 * Math.sin(2 * Math.PI * t / 7) + random.nextGaussian();
        }
        Instant[] s = starts(y.length);
        Instant decision = s[n - 1].plus(Duration.ofDays(1));
        MethodSelector.Selection before = MethodSelector.select(s, y, knownAt(s), decision, false, 7, 1);
        double[] spiked = y.clone();
        for (int t = n; t < spiked.length; t++) {
            spiked[t] = 5_000;
        }
        MethodSelector.Selection after = MethodSelector.select(s, spiked, knownAt(s), decision, false, 7, 1);
        assertThat(after.method()).isEqualTo(before.method());
        assertThat(after.backtest()).isEqualTo(before.backtest());
        // A value revised later (known only after the decision) is a gap at the decision, not its revised value.
        Instant[] late = knownAt(s);
        late[n - 3] = decision.plus(Duration.ofDays(5));
        spiked[n - 3] = 5_000;
        MethodSelector.Selection revised = MethodSelector.select(s, spiked, late, decision, false, 7, 1);
        assertThat(revised.method()).isNotNull();
    }

    @Test
    @DisplayName("steps per horizon: daily 1/7/28, weekly 1/1/4; too short a history selects nothing")
    void stepsAndShort() {
        assertThat(MethodSelector.steps(1)).containsExactly(1, 7, 28);
        assertThat(MethodSelector.steps(7)).containsExactly(1, 1, 4);
        Instant[] s = starts(20);
        MethodSelector.Selection sel = MethodSelector.select(s, new double[20], knownAt(s),
                s[19].plus(Duration.ofDays(1)), true, 7, 1);
        assertThat(sel.method()).isNull();
    }
}
