package ai.foresignal.backtest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The step loop behind the {@code window} block of a windowed rule backtest (prompt 103). */
class WindowReplayTest {

    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");
    private static final Duration HOUR = Duration.ofHours(1);
    private static final Duration FIVE = Duration.ofMinutes(5);

    @Test
    @DisplayName("steps start at the first aligned instant whose whole window is inside the range")
    void firstStep() {
        assertThat(WindowReplay.firstStep(T0, HOUR, FIVE)).isEqualTo(T0.plus(HOUR));
        Instant offset = T0.plusSeconds(61);
        assertThat(WindowReplay.firstStep(offset, HOUR, FIVE)).isEqualTo(T0.plus(HOUR).plus(FIVE));
    }

    @Test
    @DisplayName("every step instant up to and including the range end is evaluated once, in order")
    void evaluatesEveryStep() {
        List<Instant[]> calls = new ArrayList<>();
        WindowReplay.Counts counts = WindowReplay.replay(T0, T0.plus(Duration.ofHours(2)), HOUR, FIVE,
                (start, at) -> {
                    calls.add(new Instant[] {start, at});
                    return false;
                }, done -> { });
        // 01:00, 01:05, …, 02:00.
        assertThat(counts.steps()).isEqualTo(13);
        assertThat(counts.unknownSteps()).isZero();
        assertThat(calls).hasSize(13);
        assertThat(calls.getFirst()).containsExactly(T0, T0.plus(HOUR));
        assertThat(calls.getLast()).containsExactly(T0.plus(HOUR), T0.plus(Duration.ofHours(2)));
        assertThat(WindowReplay.stepCount(T0, T0.plus(Duration.ofHours(2)), HOUR, FIVE)).isEqualTo(13);
    }

    @Test
    @DisplayName("unknown evaluations are counted as unknownSteps")
    void countsUnknown() {
        Instant cutoff = T0.plus(Duration.ofMinutes(90));
        WindowReplay.Counts counts = WindowReplay.replay(T0, T0.plus(Duration.ofHours(2)), HOUR, FIVE,
                (start, at) -> at.isAfter(cutoff), done -> { });
        // 01:35 … 02:00 are unknown.
        assertThat(counts).isEqualTo(new WindowReplay.Counts(13, 6));
        assertThat(counts.toMap()).isEqualTo(Map.of("steps", 13L, "unknownSteps", 6L));
    }

    @Test
    @DisplayName("a range shorter than one window has no steps")
    void shortRange() {
        WindowReplay.Counts counts = WindowReplay.replay(T0, T0.plus(Duration.ofMinutes(30)), HOUR, FIVE,
                (start, at) -> true, done -> { });
        assertThat(counts).isEqualTo(new WindowReplay.Counts(0, 0));
    }

    @Test
    @DisplayName("progress is reported before each step and a throwing consumer stops the replay")
    void progressAndCancel() {
        List<Double> progress = new ArrayList<>();
        WindowReplay.replay(T0, T0.plus(Duration.ofMinutes(70)), HOUR, FIVE, (start, at) -> false, progress::add);
        // 01:00, 01:05, 01:10.
        assertThat(progress).containsExactly(0.0, 1.0 / 3, 2.0 / 3);
        assertThatThrownBy(() -> WindowReplay.replay(T0, T0.plus(Duration.ofHours(2)), HOUR, FIVE,
                (start, at) -> false, done -> {
                    throw new JobMonitor.Canceled();
                })).isInstanceOf(JobMonitor.Canceled.class);
    }

    @Test
    @DisplayName("a step shorter than one second is rejected")
    void subSecondStep() {
        assertThatThrownBy(() -> WindowReplay.firstStep(T0, HOUR, Duration.ofMillis(500)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
