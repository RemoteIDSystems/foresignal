package ai.foresignal.intel;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The pure parts of automatic corroboration (prompt 154): the scan cap and the member snapshot shape. The
 * space/time search and independence selection are exercised by the intel integration suite (not run per the
 * owner directive).
 */
class AutoCorroborationSpecTest {

    @Test
    @DisplayName("the scan cap scales with the member limit but never drops below a floor")
    void scanLimit() {
        assertThat(new AutoCorroboration.Spec(50, Duration.ofHours(24), 1).scanLimit()).isEqualTo(50);
        assertThat(new AutoCorroboration.Spec(50, Duration.ofHours(24), 10).scanLimit()).isEqualTo(200);
    }

    @Test
    @DisplayName("a corroborator snapshot carries the same keys a pinned member's snapshot does")
    void snapshot() {
        Instant at = Instant.parse("2026-10-22T00:00:00Z");
        AutoCorroboration.Corroborator c = new AutoCorroboration.Corroborator("src-a", "x1", 42, 3, "earthquake", at,
                "usgs-anss");
        Map<String, Object> snapshot = AutoCorroboration.snapshot(c);
        assertThat(snapshot).containsEntry("sourceId", "src-a").containsEntry("externalId", "x1")
                .containsEntry("kind", "earthquake").containsEntry("version", 3).containsEntry("seq", 42L)
                .containsEntry("observedAt", at.toString());
    }
}
