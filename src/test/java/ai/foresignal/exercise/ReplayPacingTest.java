package ai.foresignal.exercise;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.MutableClock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Replay pacing (prompt 115) on a {@link MutableClock}: sleeps scale with the speed, a single sleep is capped at
 * 60 s ÷ factor, {@code max} never sleeps, and a cancellation stops between matches.
 */
class ReplayPacingTest {

    static final Instant T = Instant.parse("2026-10-01T00:00:00Z");

    /** Items ingested at T, T+10 s, T+130 s and T+131 s. */
    static List<ReplayPacer.Paced<String>> items() {
        return List.of(new ReplayPacer.Paced<>(T, "a"), new ReplayPacer.Paced<>(T.plusSeconds(10), "b"),
                new ReplayPacer.Paced<>(T.plusSeconds(130), "c"), new ReplayPacer.Paced<>(T.plusSeconds(131), "d"));
    }

    record Run(List<Duration> sleeps, List<String> emitted, ReplayPacer.Outcome outcome, Duration elapsed) {}

    static Run run(ExerciseSpeed speed) {
        MutableClock clock = new MutableClock(T);
        List<Duration> sleeps = new ArrayList<>();
        List<String> emitted = new ArrayList<>();
        ReplayPacer pacer = new ReplayPacer(speed, d -> {
            sleeps.add(d);
            clock.advance(d);
        });
        ReplayPacer.Outcome outcome = pacer.run(items(), () -> false, emitted::add);
        return new Run(sleeps, emitted, outcome, Duration.between(T, clock.instant()));
    }

    @Test
    @DisplayName("realtime sleeps the ingest gaps, capping the 120 s gap at 60 s")
    void realtime() {
        Run run = run(ExerciseSpeed.REALTIME);
        assertThat(run.sleeps()).containsExactly(Duration.ofSeconds(10), Duration.ofSeconds(60), Duration.ofSeconds(1));
        assertThat(run.emitted()).containsExactly("a", "b", "c", "d");
        assertThat(run.elapsed()).isEqualTo(Duration.ofSeconds(71));
        assertThat(run.outcome().canceled()).isFalse();
        assertThat(run.outcome().emitted()).isEqualTo(4);
    }

    @Test
    @DisplayName("x10 divides the gaps by ten and caps a single sleep at 6 s")
    void x10() {
        Run run = run(ExerciseSpeed.X10);
        assertThat(run.sleeps()).containsExactly(Duration.ofSeconds(1), Duration.ofSeconds(6), Duration.ofMillis(100));
        assertThat(run.outcome().slept()).isEqualTo(Duration.ofMillis(7_100));
    }

    @Test
    @DisplayName("x60 caps a single sleep at 1 s")
    void x60() {
        Run run = run(ExerciseSpeed.X60);
        assertThat(run.sleeps()).allSatisfy(d -> assertThat(d).isLessThanOrEqualTo(Duration.ofSeconds(1)));
        assertThat(run.sleeps()).hasSize(3);
        assertThat(run.sleeps().get(1)).isEqualTo(Duration.ofSeconds(1));
    }

    @Test
    @DisplayName("max never sleeps")
    void max() {
        Run run = run(ExerciseSpeed.MAX);
        assertThat(run.sleeps()).isEmpty();
        assertThat(run.emitted()).hasSize(4);
        assertThat(run.elapsed()).isZero();
    }

    @Test
    @DisplayName("an unknown or out-of-order ingest time never sleeps")
    void unknownTimes() {
        assertThat(ExerciseSpeed.REALTIME.pause(null, T)).isZero();
        assertThat(ExerciseSpeed.REALTIME.pause(T, null)).isZero();
        assertThat(ExerciseSpeed.REALTIME.pause(T.plusSeconds(5), T)).isZero();
    }

    @Test
    @DisplayName("a cancellation stops between matches, including right after a sleep")
    void cancellation() {
        AtomicBoolean canceled = new AtomicBoolean();
        List<String> emitted = new ArrayList<>();
        ReplayPacer pacer = new ReplayPacer(ExerciseSpeed.REALTIME, d -> canceled.set(true));
        ReplayPacer.Outcome outcome = pacer.run(items(), canceled::get, emitted::add);
        assertThat(emitted).containsExactly("a");
        assertThat(outcome.canceled()).isTrue();
        assertThat(outcome.emitted()).isEqualTo(1);

        List<String> none = new ArrayList<>();
        ReplayPacer.Outcome stopped = new ReplayPacer(ExerciseSpeed.MAX, ReplayPacer.THREAD_SLEEP)
                .run(items(), () -> true, none::add);
        assertThat(none).isEmpty();
        assertThat(stopped.canceled()).isTrue();
    }

    @Test
    @DisplayName("an interrupted sleep counts as a cancellation")
    void interrupted() {
        List<String> emitted = new ArrayList<>();
        ReplayPacer.Outcome outcome = new ReplayPacer(ExerciseSpeed.REALTIME, d -> {
            throw new InterruptedException("stop");
        }).run(items(), () -> false, emitted::add);
        assertThat(Thread.interrupted()).isTrue();
        assertThat(outcome.canceled()).isTrue();
        assertThat(emitted).containsExactly("a");
    }
}
