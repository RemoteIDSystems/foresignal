package ai.foresignal.source.adapters.satdump;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.adapters.satdump.SatDumpFiles.FormatException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The lenient SatDump parse_dcs JSON reader (prompt 107). */
class DcsJsonParserContractTest {

    @Test
    @DisplayName("parses an object with byte-array data and common keys")
    void object() throws FormatException {
        String json = "{\"address\":\"abc123\",\"timestamp\":\"2026-10-15T12:00:00Z\",\"channel\":7,"
                + "\"signal_dbm\":-95.5,\"freq_offset\":12,\"data\":[65,66,67],\"failure\":0}";
        List<DcsJsonParser.Message> messages = DcsJsonParser.parse(json.getBytes(StandardCharsets.UTF_8));
        assertThat(messages).hasSize(1);
        DcsJsonParser.Message m = messages.get(0);
        assertThat(m.address()).isEqualTo("ABC123");
        assertThat(m.time()).isEqualTo(Instant.parse("2026-10-15T12:00:00Z"));
        assertThat(m.channel()).isEqualTo(7);
        assertThat(m.signalDbm()).isEqualTo(-95.5);
        assertThat(m.data()).containsExactly(65, 66, 67);
        assertThat(m.ascii()).isEqualTo("ABC");
        assertThat(m.failureCode()).isZero();
    }

    @Test
    @DisplayName("parses a hex data string and a messages array")
    void hexAndArray() throws FormatException {
        String json = "[{\"dcp_address\":\"FF01\",\"message_time\":1777000000,\"data_hex\":\"48656c6c6f\"}]";
        List<DcsJsonParser.Message> messages = DcsJsonParser.parse(json.getBytes(StandardCharsets.UTF_8));
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).address()).isEqualTo("FF01");
        assertThat(messages.get(0).time()).isEqualTo(Instant.ofEpochSecond(1777000000L));
        assertThat(messages.get(0).ascii()).isEqualTo("Hello");
    }

    @Test
    @DisplayName("a message without an address is skipped")
    void noAddress() throws FormatException {
        assertThat(DcsJsonParser.parse("{\"channel\":1}".getBytes(StandardCharsets.UTF_8))).isEmpty();
    }

    @Test
    @DisplayName("malformed JSON is a format error")
    void malformed() {
        org.assertj.core.api.Assertions
                .assertThatThrownBy(() -> DcsJsonParser.parse("not json".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(FormatException.class);
    }
}
