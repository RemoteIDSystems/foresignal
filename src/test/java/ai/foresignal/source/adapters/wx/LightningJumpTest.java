package ai.foresignal.source.adapters.wx;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The GLM lightning jump (prompt 105; Schultz et al. 2009). */
class LightningJumpTest {

    static final Instant NOW = Instant.parse("2026-10-15T00:00:00Z");

    private static List<Instant> flashesInBin(int bin, int count) {
        List<Instant> out = new ArrayList<>();
        Instant start = NOW.minus(Duration.ofMinutes(2L * LightningJump.BINS));
        Instant base = start.plusSeconds(120L * bin + 10);
        for (int i = 0; i < count; i++) {
            out.add(base.plusMillis(i));
        }
        return out;
    }

    @Test
    @DisplayName("a sharp rise above 2σ with a rate at the floor is a jump")
    void detectsJump() {
        List<Instant> flashes = new ArrayList<>(flashesInBin(LightningJump.BINS - 1, 40));
        var jump = LightningJump.detect(NOW, flashes);
        assertThat(jump).isPresent();
        assertThat(jump.get().rateFpm()).isGreaterThanOrEqualTo(LightningJump.RATE_FLOOR_FPM);
    }

    @Test
    @DisplayName("a rise below the rate floor is not a jump")
    void rateFloor() {
        List<Instant> flashes = new ArrayList<>(flashesInBin(LightningJump.BINS - 1, 10));
        assertThat(LightningJump.detect(NOW, flashes)).isEmpty();
    }

    @Test
    @DisplayName("no flashes is no jump")
    void empty() {
        assertThat(LightningJump.detect(NOW, List.of())).isEmpty();
        assertThat(LightningJump.detect(NOW, null)).isEmpty();
    }

    @Test
    @DisplayName("a steady high rate does not jump")
    void steady() {
        List<Instant> flashes = new ArrayList<>(flashesInBin(LightningJump.BINS - 1, 40));
        flashes.addAll(flashesInBin(LightningJump.BINS - 2, 40));
        // A flat rate produces a zero DFRDT, below the mean + 2σ of the previous zero DFRDTs only if the mean is
        // positive; with all-zero history there is no positive deviation, so no jump.
        assertThat(LightningJump.detect(NOW, flashes)).isEmpty();
    }
}
