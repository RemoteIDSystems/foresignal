package ai.foresignal.trigger;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.store.history.HistoryFidelity;
import ai.foresignal.store.history.RawHistoryFidelity;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code testTrigger}'s {@code DOWNSAMPLED_RANGE} warning (prompt 044): present only when the tested
 * range {@code (fromSeq, toSeq]} overlaps downsampled TRACK history.
 */
class TriggerTestDownsampleTest {

    /** TRACK seqs {@code [1000, 2000)} are downsampled; the hook records what it was asked. */
    static final class OneRange implements HistoryFidelity {
        final List<long[]> asked = new ArrayList<>();

        @Override
        public boolean downsampled(HistoryTier tier, long fromSeq, long toSeq) {
            asked.add(new long[] {tier.code(), fromSeq, toSeq});
            return tier == HistoryTier.TRACK && fromSeq < 2000 && toSeq >= 1000;
        }

        @Override
        public Duration validityWidening(String sourceId) {
            return Duration.ZERO;
        }

        @Override
        public Instant finalBefore(HistoryTier tier) {
            return Instant.MAX;
        }
    }

    @Test
    @DisplayName("an overlapping range warns DOWNSAMPLED_RANGE with the documented message")
    void overlapping() {
        List<TriggerBacktester.Warning> warnings = TriggerBacktester.warnings(new OneRange(), 500, 1500);
        assertThat(warnings).singleElement().satisfies(w -> {
            assertThat(w.code()).isEqualTo("DOWNSAMPLED_RANGE");
            assertThat(w.message()).isEqualTo(
                    "match-mode results over downsampled history under-count; edge reasons are preserved");
        });
        assertThat(TriggerBacktester.warnings(new OneRange(), 1999, 5000)).hasSize(1);
        assertThat(TriggerBacktester.warnings(new OneRange(), 0, 1000)).as("toSeq is inclusive").hasSize(1);
    }

    @Test
    @DisplayName("a range outside every downsampled leaf, an empty range or raw history has no warning")
    void notOverlapping() {
        assertThat(TriggerBacktester.warnings(new OneRange(), 2000, 3000)).as("fromSeq is exclusive").isEmpty();
        assertThat(TriggerBacktester.warnings(new OneRange(), 0, 999)).isEmpty();
        assertThat(TriggerBacktester.warnings(new OneRange(), 1500, 1500)).as("empty range").isEmpty();
        assertThat(TriggerBacktester.warnings(new RawHistoryFidelity(), 0, Long.MAX_VALUE)).isEmpty();
        assertThat(TriggerBacktester.warnings(null, 0, 10)).isEmpty();
    }

    @Test
    @DisplayName("the TRACK tier is asked for the half-open range (fromSeq, toSeq]")
    void asksTrackForTheRange() {
        OneRange hook = new OneRange();
        TriggerBacktester.warnings(hook, 10, 20);
        assertThat(hook.asked).singleElement()
                .satisfies(a -> assertThat(a).containsExactly(HistoryTier.TRACK.code(), 11, 20));
    }

    @Test
    @DisplayName("a result copies its warnings and treats null as none")
    void resultWarnings() {
        TriggerBacktester.Result result = new TriggerBacktester.Result(null, null, List.of(), false, 0, 0, 0, false,
                null, null, false, false, null);
        assertThat(result.warnings()).isEmpty();
    }
}
