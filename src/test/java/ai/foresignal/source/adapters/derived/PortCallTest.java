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
                new ReferenceData.Port("2", "Small", -95.01, 32.01, 3));
        assertThat(PortCallGenerator.containing(ports, -95.0, 32.0).portNumber()).isEqualTo("1");
        assertThat(PortCallGenerator.containing(ports, -95.005, 32.005).portNumber()).isEqualTo("2");
        assertThat(PortCallGenerator.containing(ports, -94.0, 33.0)).isNull();
    }
}
