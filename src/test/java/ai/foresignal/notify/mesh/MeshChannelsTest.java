package ai.foresignal.notify.mesh;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.notify.NotificationChannel;
import ai.foresignal.notify.NotificationMessage;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The shared mesh channel settings (prompt 084): parsing, validation, sender keys, and the channel's own
 * {@code minSeverity} narrowing what a mesh channel accepts.
 */
class MeshChannelsTest {

    static Map<String, Object> config() {
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("region", "UK_868");
        c.put("channelIndex", 3);
        c.put("dutyCyclePercent", 2.5);
        c.put("minSpacing", "1m");
        c.put("maxMessagesPerHour", 10);
        c.put("reportsEnabled", true);
        c.put("allowedSenders", Map.of("A1B2C3D4", "ack", "mc:0011223344AA", "report", "name:Alice", "report"));
        return c;
    }

    @Test
    @DisplayName("common settings parse with their defaults and normalized senders")
    void parse() {
        MeshChannels.Common c = MeshChannels.common(config(), LoRaAirtime.LONGFAST);
        assertThat(c.region()).isEqualTo(MeshRegion.UK_868);
        assertThat(c.radio()).isEqualTo(LoRaAirtime.MESHTASTIC_LONGFAST);
        assertThat(c.limits()).isEqualTo(new AirtimeGovernor.Limits(MeshRegion.UK_868, 2.5, Duration.ofMinutes(1), 10));
        assertThat(c.profile()).isEqualTo("text");
        assertThat(c.minSeverity()).isEqualTo("warning");
        assertThat(c.channelIndex()).isEqualTo(3);
        assertThat(c.allowedSenders()).containsEntry("!a1b2c3d4", "ack").containsEntry("mc:0011223344aa", "report")
                .containsEntry("name:Alice", "report");
        assertThat(MeshChannels.validate(config(), LoRaAirtime.LONGFAST)).isEmpty();
    }

    @Test
    @DisplayName("validation: region, spacing, share above the ceiling, sender keys and roles, reports need senders")
    void validate() {
        Map<String, Object> c = config();
        c.put("region", "MOON");
        c.put("minSpacing", "soon");
        c.put("allowedSenders", Map.of("bob", "admin"));
        List<FieldError> errors = MeshChannels.validate(c, LoRaAirtime.LONGFAST);
        assertThat(errors).extracting(FieldError::field).contains("config.region", "config.minSpacing",
                "config.allowedSenders", "config.allowedSenders.bob");
        Map<String, Object> share = config();
        share.put("dutyCyclePercent", 11);
        assertThat(MeshChannels.validate(share, LoRaAirtime.LONGFAST)).extracting(FieldError::field)
                .contains("config.dutyCyclePercent");
        Map<String, Object> reports = config();
        reports.remove("allowedSenders");
        assertThat(MeshChannels.validate(reports, LoRaAirtime.LONGFAST)).extracting(FieldError::field)
                .containsExactly("config.allowedSenders");
        assertThat(MeshChannels.senderKey(" name: Bob ")).isEqualTo("name:Bob");
        assertThat(MeshChannels.senderKey("!XYZ")).isNull();
        assertThat(MeshChannels.senderKey(null)).isNull();
    }

    @Test
    @DisplayName("a mesh channel's config.minSeverity (default warning) narrows what it accepts")
    void minSeverity() {
        NotificationMessage info = NotificationMessage.builder("alert", "alert.fired", "x", "info").build();
        NotificationMessage warning = NotificationMessage.builder("alert", "alert.fired", "x", "warning").build();
        NotificationChannel mesh = channel("meshtastic", Map.of());
        assertThat(mesh.accepts(info)).isFalse();
        assertThat(mesh.accepts(warning)).isTrue();
        assertThat(channel("meshcore", Map.of("minSeverity", "critical")).accepts(warning)).isFalse();
        assertThat(channel("meshcore", Map.of("minSeverity", "info")).accepts(info)).isTrue();
        assertThat(channel("webhook", Map.of()).accepts(info)).as("other types are unaffected").isTrue();
        assertThat(MeshChannels.minSeverity("slack", Map.of("minSeverity", "critical"))).isNull();
    }

    static NotificationChannel channel(String type, Map<String, Object> config) {
        return new NotificationChannel(UUID.randomUUID(), "c", type, config, List.of(), List.of("alert"), null, "info",
                null, null, 0, true, null, null, 0, 20, false, 0, Instant.EPOCH, Instant.EPOCH);
    }
}
