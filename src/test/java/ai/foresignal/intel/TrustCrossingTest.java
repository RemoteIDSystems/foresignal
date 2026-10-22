package ai.foresignal.intel;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.intel.TrustCrossing.Level;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The trust-threshold crossings behind the first-class confidence-upgrade signal (prompt 151). */
class TrustCrossingTest {

    private static Level level(String status, String confidence, String quality, boolean calibrated) {
        return new Level(status, confidence, quality, calibrated);
    }

    private static List<String> dimensions(List<Map<String, Object>> crossings) {
        return crossings.stream().map(c -> String.valueOf(c.get("dimension"))).toList();
    }

    @Test
    @DisplayName("no change emits nothing")
    void stable() {
        Level same = level("active", "moderate", "moderate", false);
        assertThat(TrustCrossing.crossings(same, same)).isEmpty();
    }

    @Test
    @DisplayName("emerging to active is a status crossing")
    void status() {
        List<Map<String, Object>> crossings = TrustCrossing.crossings(
                level("emerging", "low", "weak", false), level("active", "low", "weak", false));
        assertThat(dimensions(crossings)).containsExactly("status");
        assertThat(crossings.get(0)).containsEntry("from", "emerging").containsEntry("to", "active");
    }

    @Test
    @DisplayName("a confidence tier up fires; a tier down does not")
    void confidence() {
        assertThat(dimensions(TrustCrossing.crossings(
                level("active", "low", "weak", false), level("active", "moderate", "weak", false))))
                .containsExactly("confidenceLabel");
        assertThat(TrustCrossing.crossings(
                level("active", "high", "weak", false), level("active", "moderate", "weak", false))).isEmpty();
    }

    @Test
    @DisplayName("reaching two families is both an evidence-quality tier and corroboration")
    void corroboration() {
        List<Map<String, Object>> crossings = TrustCrossing.crossings(
                level("active", "low", "weak", false), level("active", "low", "moderate", false));
        assertThat(dimensions(crossings)).containsExactly("evidenceQuality", "corroboration");
    }

    @Test
    @DisplayName("calibrated turning true fires")
    void calibrated() {
        assertThat(dimensions(TrustCrossing.crossings(
                level("active", "high", "strong", false), level("active", "high", "strong", true))))
                .containsExactly("calibrated");
    }

    @Test
    @DisplayName("several dimensions can cross at once")
    void several() {
        List<Map<String, Object>> crossings = TrustCrossing.crossings(
                level("emerging", "low", "insufficient", false), level("active", "high", "strong", true));
        assertThat(dimensions(crossings))
                .containsExactly("status", "confidenceLabel", "evidenceQuality", "corroboration", "calibrated");
    }
}
