package ai.foresignal.derive;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.stream.ChangeBatch;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link DerivedRunner#merge} and the {@code changeInterval} default. */
class DerivedRunnerMergeTest {

    private static final Instant T = Instant.parse("2026-10-22T12:00:00Z");

    @Test
    @DisplayName("two batches of one source merge: changes in order, the seq range of both, the later id and time")
    void merge() {
        ChangeBatch first = new ChangeBatch("ais-digitraffic", 10, T, 100, 104, List.of());
        ChangeBatch second = new ChangeBatch("ais-digitraffic", 11, T.plusSeconds(4), 105, 109, List.of());

        ChangeBatch merged = DerivedRunner.merge(first, second);

        assertThat(merged.sourceId()).isEqualTo("ais-digitraffic");
        assertThat(merged.batchId()).isEqualTo(11);
        assertThat(merged.ingestedAt()).isEqualTo(T.plusSeconds(4));
        assertThat(merged.fromSeq()).isEqualTo(100);
        assertThat(merged.toSeq()).isEqualTo(109);
    }

    @Test
    @DisplayName("change invocations are 30 s apart by default; a negative interval falls back to the default")
    void interval() {
        assertThat(DerivedProperties.defaults().changeInterval()).isEqualTo(Duration.ofSeconds(30));
        assertThat(new DerivedProperties(true, 64, Duration.ofSeconds(1), Duration.ofSeconds(60),
                Duration.ofSeconds(-1)).changeInterval()).isEqualTo(Duration.ofSeconds(30));
        assertThat(new DerivedProperties(true, 64, Duration.ofSeconds(1), Duration.ofSeconds(60), Duration.ZERO)
                .changeInterval()).isZero();
    }
}
