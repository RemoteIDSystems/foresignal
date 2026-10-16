package ai.foresignal.backtest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.trigger.MatchRow;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The pure parts of the rule backtest and the job framework (prompt 103). */
class RuleBacktestTest {

    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

    private static MatchRow row(long seq, Instant validFrom) {
        return new MatchRow(seq, null, "s", "e" + seq, "k", 1, (short) 0, T0, null, validFrom, null, false, null, "{}",
                "match");
    }

    @Test
    @DisplayName("a forecast-horizon rule keeps only rows within the horizon of their own ingest time")
    void noLookAheadOnHorizon() {
        Duration horizon = Duration.ofHours(6);
        List<MatchRow> rows = List.of(row(1, null), row(2, T0.plus(Duration.ofHours(5))),
                row(3, T0.plus(Duration.ofHours(7))), row(4, T0.plus(Duration.ofHours(7))));
        Map<Long, Instant> ingested = Map.of(2L, T0, 3L, T0, 4L, T0.plus(Duration.ofHours(2)));
        List<Long> kept = RuleBacktest.keepKnownAtIngest(rows, ingested, horizon).stream().map(MatchRow::seq).toList();
        // Row 3 starts 7 h after it was ingested: the live engine would not yet have matched it.
        assertThat(kept).containsExactly(1L, 2L, 4L);
    }

    @Test
    @DisplayName("the sample size comes from the result of each job type")
    void sampleSize() {
        assertThat(BacktestJobService.sampleSize("rule", Map.of("wouldFire", 3L))).isEqualTo(3);
        assertThat(BacktestJobService.sampleSize("precursor", Map.of("nAntecedent", 12L))).isEqualTo(12);
        assertThat(BacktestJobService.sampleSize("recipe", Map.of("eventCount", 0L))).isZero();
        assertThat(BacktestJobService.sampleSize("quality", Map.of())).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    @DisplayName("a checkpoint records progress and stops a canceled job")
    void checkpoint() {
        List<Double> progress = new ArrayList<>();
        boolean[] canceled = {false};
        JobMonitor monitor = new JobMonitor() {
            @Override
            public boolean canceled() {
                return canceled[0];
            }

            @Override
            public void progress(double fraction) {
                progress.add(fraction);
            }
        };
        monitor.checkpoint(0.25);
        monitor.checkpoint(1.5);
        assertThat(progress).containsExactly(0.25, 1.0);
        canceled[0] = true;
        assertThatThrownBy(() -> monitor.checkpoint(0.5)).isInstanceOf(JobMonitor.Canceled.class);
        JobMonitor.NONE.checkpoint(0.5);
    }
}
