package ai.foresignal.sink;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** BaseStation SBS-1 lines (prompt 110). */
class SbsEncoderTest {

    private static final Instant AT = Instant.parse("2026-10-15T12:34:56.789Z");

    @Test
    @DisplayName("MSG,1 carries the callsign")
    void callsign() {
        String line = SbsEncoder.callsign("4CA1B2", "RCH123", AT);
        assertThat(line).startsWith("MSG,1,1,1,4CA1B2,1,2026/10/15,12:34:56.789,2026/10/15,12:34:56.789,RCH123,");
        assertThat(line.split(",", -1)).hasSize(22);
    }

    @Test
    @DisplayName("MSG,3 carries the position with the altitude, speed and track")
    void position() {
        String line = SbsEncoder.position("4CA1B2", "RCH123", 35000, 480.0, 90.0, 44.5, -3.2, false, AT);
        assertThat(line).contains("MSG,3,1,1,4CA1B2,1,");
        assertThat(line).contains(",35000,480,90,44.5,-3.2,");
    }

    @Test
    @DisplayName("MSG,4 carries the velocity")
    void velocity() {
        String line = SbsEncoder.velocity("4CA1B2", "RCH123", 480.0, 90.0, -2048, AT);
        assertThat(line).contains("MSG,4,1,1,4CA1B2,1,");
        assertThat(line).contains(",480,90,");
        assertThat(line).contains(",-2048,");
    }

    @Test
    @DisplayName("free-text fields are CSV-safe: commas and line breaks cannot add columns or messages")
    void fieldEscaping() {
        assertThat(SbsEncoder.field("AB,C\r\nMSG,3")).isEqualTo("AB C  MSG 3");
        assertThat(SbsEncoder.field(null)).isEmpty();
        String line = SbsEncoder.callsign("ABC123", "EVIL,1,2\r\nMSG", Instant.parse("2026-10-16T12:00:00Z"));
        assertThat(line.split(",", -1)).hasSize(22);
        assertThat(line).doesNotContain("\r", "\n");
    }
}
