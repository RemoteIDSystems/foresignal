package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link CollectorScheduler#firstDelay}: a restart resumes each source's interval instead of fetching at once. */
class CollectorSchedulerFirstDelayTest {

    private static final Instant NOW = Instant.parse("2026-10-22T16:00:00Z");
    private static final Duration HOURLY = Duration.ofHours(1);

    @Test
    @DisplayName("a source that ran 20 minutes into an hourly interval waits the other 40")
    void resumesInterval() {
        assertThat(CollectorScheduler.firstDelay(NOW.minus(Duration.ofMinutes(20)), HOURLY, NOW, 0))
                .isEqualTo(Duration.ofMinutes(40));
    }

    @Test
    @DisplayName("a source that never ran, or is overdue, starts at once, staggered behind the others due")
    void dueSourcesStagger() {
        assertThat(CollectorScheduler.firstDelay(null, HOURLY, NOW, 0)).isZero();
        assertThat(CollectorScheduler.firstDelay(null, HOURLY, NOW, 3)).isEqualTo(Duration.ofMillis(300));
        assertThat(CollectorScheduler.firstDelay(NOW.minus(Duration.ofHours(5)), HOURLY, NOW, 2))
                .isEqualTo(CollectorScheduler.STAGGER.multipliedBy(2));
    }

    @Test
    @DisplayName("a remaining interval shorter than the stagger still waits for its turn")
    void staggerFloor() {
        assertThat(CollectorScheduler.firstDelay(NOW.minus(Duration.ofMillis(59_950)), Duration.ofSeconds(60), NOW,
                4)).isEqualTo(Duration.ofMillis(400));
    }
}
