package ai.foresignal.ingest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** ING-3: {@code ingested_at} never decreases with seq; the clock guard (Core-Data-Model §6.1). */
class IngestClockGuardTest {

    private static final Instant T = Instant.parse("2026-03-01T12:00:00Z");
    private final ClockGuard guard = new ClockGuard(Duration.ofMinutes(5));

    @Test
    @DisplayName("behind only when the clock is more than the tolerance before the last ingest time")
    void behind() {
        assertThat(guard.isBehind(T, null)).isFalse();
        assertThat(guard.isBehind(T.minus(Duration.ofMinutes(5)), T)).isFalse();
        assertThat(guard.isBehind(T.minus(Duration.ofMinutes(5)).minusMillis(1), T)).isTrue();
        assertThat(guard.isBehind(T.plusSeconds(1), T)).isFalse();
    }

    @Test
    @DisplayName("ingestedAt clamps a backward step to the last ingest time")
    void clamp() {
        assertThat(ClockGuard.ingestedAt(T, null)).isEqualTo(T);
        assertThat(ClockGuard.ingestedAt(T.minusSeconds(10), T)).isEqualTo(T);
        assertThat(ClockGuard.ingestedAt(T.plusSeconds(10), T)).isEqualTo(T.plusSeconds(10));
    }

    @Test
    @DisplayName("a jump is a wall/monotonic divergence above the tolerance, with a direction")
    void jump() {
        long tenSeconds = Duration.ofSeconds(10).toNanos();
        assertThat(guard.jump(Duration.ofSeconds(10), tenSeconds)).isEmpty();
        assertThat(guard.jump(Duration.ofMinutes(5).plusSeconds(10), tenSeconds)).isEmpty();
        assertThat(guard.jump(Duration.ofMinutes(6), tenSeconds)).contains(ClockGuard.Direction.FORWARD);
        assertThat(guard.jump(Duration.ofMinutes(-6), tenSeconds)).contains(ClockGuard.Direction.BACKWARD);
        assertThat(ClockGuard.Direction.FORWARD.tag()).isEqualTo("forward");
    }
}
