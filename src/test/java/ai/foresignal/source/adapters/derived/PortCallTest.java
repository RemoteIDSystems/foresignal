package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code derived-port-calls} buffer containment (prompt 099). */
class PortCallTest {

    @Test
    @DisplayName("the smallest buffer containing the point wins")
    void containing() {
        List<ReferenceData.Port> ports = List.of(
                new ReferenceData.Port("1", "Big", -95, 32, 10),
                // About 7 km from Big's center: (-95, 32) is inside Big only; (-95.045, 32.045) is inside both.
                new ReferenceData.Port("2", "Small", -95.05, 32.05, 3));
        assertThat(PortCallGenerator.containing(ports, -95.0, 32.0).portNumber()).isEqualTo("1");
        assertThat(PortCallGenerator.containing(ports, -95.045, 32.045).portNumber()).isEqualTo("2");
        assertThat(PortCallGenerator.containing(ports, -94.0, 33.0)).isNull();
    }
}
