package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code derived-loiter} radius test (prompt 099). */
class LoiterTest {

    @Test
    @DisplayName("fixes inside the radius hold station; one outside breaks it")
    void radius() {
        Instant t = Instant.parse("2026-10-15T10:00:00Z");
        List<LoiterGenerator.Fix> tight = List.of(new LoiterGenerator.Fix(t, -95.0, 32.0),
                new LoiterGenerator.Fix(t.plusSeconds(600), -95.001, 32.001),
                new LoiterGenerator.Fix(t.plusSeconds(1200), -95.0, 32.0));
        assertThat(LoiterGenerator.withinRadius(tight, -95.0, 32.0, LoiterGenerator.RADIUS_KM)).isTrue();
        List<LoiterGenerator.Fix> loose = List.of(new LoiterGenerator.Fix(t, -95.0, 32.0),
                new LoiterGenerator.Fix(t.plusSeconds(600), -95.05, 32.05));
        assertThat(LoiterGenerator.withinRadius(loose, -95.0, 32.0, LoiterGenerator.RADIUS_KM)).isFalse();
    }
}
