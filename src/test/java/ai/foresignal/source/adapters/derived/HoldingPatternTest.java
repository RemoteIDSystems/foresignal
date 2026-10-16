package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code derived-holding} (prompt 099): the turn math, and through ticks the 299° vs 301° boundary within
 * ten minutes, a stable {@code {hex}:{start}} id and the end five minutes after the last turn.
 */
class HoldingPatternTest {

    private static final Instant T0 = Instant.parse("2026-10-15T10:00:00Z");
    private static final String SOURCE = "adsblol-area-1";

    private static HoldingPatternGenerator.Point point(double track) {
        return new HoldingPatternGenerator.Point(Instant.parse("2026-10-15T10:00:00Z"), -95, 32, 10000, track);
    }

    @Test
    @DisplayName("a full circle exceeds 300 degrees; a straight line does not")
    void turns() {
        assertThat(HoldingPatternGenerator.cumulativeTurn(List.of(point(0), point(90), point(180), point(270), point(0))))
                .isEqualTo(360);
        assertThat(HoldingPatternGenerator.cumulativeTurn(List.of(point(0), point(1), point(2)))).isLessThan(300);
    }

    @Test
    @DisplayName("wrap keeps deltas in [-180, 180]")
    void wrapping() {
        assertThat(HoldingPatternGenerator.wrap(350)).isEqualTo(-10);
        assertThat(HoldingPatternGenerator.wrap(-350)).isEqualTo(10);
    }

    /** Fixes every two minutes with the given tracks; the records emitted across all ticks. */
    private static List<NormalizedRecord> fly(LayerHarness harness, HoldingPatternGenerator generator, double... tracks) {
        List<NormalizedRecord> emitted = new ArrayList<>();
        for (int i = 0; i < tracks.length; i++) {
            Instant at = T0.plus(Duration.ofMinutes(2L * i));
            harness.put(SOURCE, "abc123", "aircraft", at, -95.0 + i * 0.01, 32.0, "hex", "abc123", "gs", 180.0,
                    "alt_baro", 3000.0, "track", tracks[i], "onGround", false);
            emitted.addAll(harness.tick(generator, at.plusSeconds(30)));
        }
        return emitted;
    }

    @Test
    @DisplayName("301 degrees of turn within ten minutes is a hold; 299 degrees is not")
    void turnBoundary() {
        LayerHarness below = new LayerHarness(T0, SOURCE);
        HoldingPatternGenerator notHolding = new HoldingPatternGenerator(below.registry(), below.reference());
        assertThat(fly(below, notHolding, 0, 75, 150, 225, 299)).isEmpty();

        LayerHarness above = new LayerHarness(T0, SOURCE);
        HoldingPatternGenerator holding = new HoldingPatternGenerator(above.registry(), above.reference());
        assertThat(fly(above, holding, 0, 75, 150, 225, 301)).singleElement().satisfies(record -> {
            assertThat(record.externalId()).isEqualTo("abc123:" + T0.getEpochSecond());
            assertThat(record.payload()).containsEntry("turnsDeg", 301L);
        });
    }

    @Test
    @DisplayName("the hold keeps its start id while it lasts and ends five minutes after the last turn")
    void endsFiveMinutesAfterTheLastTurn() {
        LayerHarness harness = new LayerHarness(T0, SOURCE);
        HoldingPatternGenerator generator = new HoldingPatternGenerator(harness.registry(), harness.reference());
        // Turning until 8 min, then straight: the window turn falls below 300° at 12 min, and the last
        // turn (8 min) is more than five minutes old at the 14 min tick.
        List<NormalizedRecord> emitted = fly(harness, generator, 0, 75, 150, 225, 301, 301, 301, 301);
        assertThat(emitted).extracting(NormalizedRecord::externalId).containsOnly("abc123:" + T0.getEpochSecond());
        assertThat(harness.tombstones).containsExactly("holding-pattern/abc123:" + T0.getEpochSecond());
    }
}
