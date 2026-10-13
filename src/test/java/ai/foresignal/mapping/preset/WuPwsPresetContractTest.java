package ai.foresignal.mapping.preset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * The {@code wu-pws} preset (prompt 059): Weather Underground protocol (GET with PASSWORD). The captured fixture under
 * {@code fixtures/mapping/wu-pws/} gives the golden records, and no secret is stored.
 */
class WuPwsPresetContractTest {

    @Test
    @DisplayName("the fixture maps to the golden records and drops every secret")
    void golden() {
        PresetContract.assertGolden(PresetContract.run("wu-pws"));
    }

    @Test
    @DisplayName("the time base is RECEIVED: every observation carries the receive time")
    void receivedTime() {
        PresetContract.Run run = PresetContract.run("wu-pws");
        assertThat(run.merged().path("observedAtMode").asString()).isEqualTo("RECEIVED");
        assertThat(run.parsed().records().getFirst().observedAt()).isEqualTo(Instant.parse("2026-10-13T15:05:00Z"));
    }
}
