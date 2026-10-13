package ai.foresignal.source.adapters.satdump;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link SatDumpApiParser} and {@link GroundStationRecords} against the pinned-version {@code /api}
 * fixtures (prompt 049): both shapes, string and array leniency, state derivation, quantization and
 * version-drift detection.
 */
class SatDumpApiParserTest {

    @Test
    @DisplayName("legacy live: module statistics at top level, folded into one snapshot")
    void live() {
        ApiSnapshot s = SatDumpApiParser.parse(SatDumpFixtures.text("api/live.json"));

        assertThat(s.mode()).isEqualTo(ApiSnapshot.LIVE);
        assertThat(s.driftReason()).isNull();
        assertThat(s.modules()).containsExactly("meteor_lrpt_decoder", "psk_demod");
        assertThat(s.snr()).isEqualTo(11.42);
        assertThat(s.peakSnr()).isEqualTo(14.8);
        assertThat(s.ber()).isEqualTo(0.0841);
        assertThat(s.deframerState()).isEqualTo("SYNCED");
        assertThat(s.locked()).isTrue();
        assertThat(s.rsAvg()).isEqualTo(3.0);
        assertThat(s.target()).isNull();
        assertThat(s.frequencyHz()).isNull();
        assertThat(GroundStationRecords.state(s)).isEqualTo(GroundStationRecords.LOCKED);
    }

    @Test
    @DisplayName("legacy autotrack: tracker, frequency, strings and arrays read leniently")
    void autotrack() {
        ApiSnapshot s = SatDumpApiParser.parse(SatDumpFixtures.text("api/autotrack.json"));

        assertThat(s.mode()).isEqualTo(ApiSnapshot.AUTOTRACK);
        assertThat(s.driftReason()).isNull();
        assertThat(s.target()).isEqualTo("METEOR-M2 3");
        assertThat(s.frequencyHz()).isEqualTo(137_900_000L);
        assertThat(s.az()).isEqualTo(212.37);
        assertThat(s.el()).isEqualTo(38.62);
        assertThat(s.nextAos()).isEqualTo(Instant.parse("2026-10-12T19:21:05Z"));
        assertThat(s.nextLos()).isEqualTo(Instant.parse("2026-10-12T19:33:40Z"));
        assertThat(s.rotatorEngaged()).isTrue();
        // "9.6" as a string, peak_snr as an array (best), viterbi_ber as an array (mean), rs_avg as an array (mean).
        assertThat(s.snr()).isEqualTo(9.6);
        assertThat(s.peakSnr()).isEqualTo(13.4);
        assertThat(s.ber()).isEqualTo(0.1, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(s.rsAvg()).isEqualTo(3.0);
        assertThat(s.locked()).isTrue();
    }

    @Test
    @DisplayName("states: idle below the horizon, tracking above it, receiving with a pipeline, locked with a lock")
    void states() {
        ApiSnapshot idle = SatDumpApiParser.parse(SatDumpFixtures.text("api/autotrack-idle.json"));
        assertThat(GroundStationRecords.state(idle)).isEqualTo(GroundStationRecords.IDLE);

        ApiSnapshot tracking = SatDumpApiParser.parse("{\"object_tracker\":{\"object_name\":\"METEOR-M2 4\","
                + "\"sat_current_pos\":{\"az\":10,\"el\":4.2},\"next_aos_time\":0},\"frequency\":137100000}");
        assertThat(GroundStationRecords.state(tracking)).isEqualTo(GroundStationRecords.TRACKING);

        ApiSnapshot receiving = SatDumpApiParser.parse("{\"psk_demod\":{\"snr\":2.1}}");
        assertThat(GroundStationRecords.state(receiving)).isEqualTo(GroundStationRecords.RECEIVING);

        ApiSnapshot noTarget = SatDumpApiParser.parse("{\"object_tracker\":{\"object_name\":\"None\","
                + "\"sat_current_pos\":{\"az\":10,\"el\":40},\"next_aos_time\":0}}");
        assertThat(noTarget.target()).isNull();
        assertThat(GroundStationRecords.state(noTarget)).isEqualTo(GroundStationRecords.IDLE);

        assertThat(GroundStationRecords.state(null)).isEqualTo(GroundStationRecords.UNKNOWN);
        assertThat(GroundStationRecords.payload("home", null)).containsEntry("state", "unknown")
                .containsEntry("title", "SatDump station home: unknown");
    }

    @Test
    @DisplayName("quantized payload: az/el to 1°, SNR to 1 dB; a small change writes the same payload")
    void quantization() {
        ApiSnapshot a = SatDumpApiParser.parse(SatDumpFixtures.text("api/autotrack.json"));
        Map<String, Object> payload = GroundStationRecords.payload("home", a);

        assertThat(payload).containsEntry("state", "locked").containsEntry("az", 212L).containsEntry("el", 39L)
                .containsEntry("snrDb", 10L).containsEntry("currentTarget", "METEOR-M2 3")
                .containsEntry("frequencyHz", 137_900_000L).containsEntry("satdumpMode", "autotrack")
                .containsEntry("rotatorEngaged", true).containsEntry("nextAos", "2026-10-12T19:21:05Z")
                .containsEntry("title", "SatDump station home: locked METEOR-M2 3");

        String nudged = SatDumpFixtures.text("api/autotrack.json").replace("212.37", "212.41").replace("38.62", "38.71")
                .replace("\"9.6\"", "\"9.9\"");
        assertThat(GroundStationRecords.payload("home", SatDumpApiParser.parse(nudged))).isEqualTo(payload);

        String moved = SatDumpFixtures.text("api/autotrack.json").replace("38.62", "40.2");
        assertThat(GroundStationRecords.payload("home", SatDumpApiParser.parse(moved))).isNotEqualTo(payload);
    }

    @Test
    @DisplayName("drift: non-JSON, a non-object, a tracker without its keys, or a pipeline without a tracker")
    void drift() {
        assertThat(SatDumpApiParser.parse("<html>").driftReason()).isEqualTo(SatDumpApiParser.DRIFT_REASON);
        assertThat(SatDumpApiParser.parse("[1,2]").driftReason()).isEqualTo(SatDumpApiParser.DRIFT_REASON);
        assertThat(SatDumpApiParser.parse("{\"object_tracker\":{\"name\":\"x\"}}").driftReason())
                .isEqualTo(SatDumpApiParser.DRIFT_REASON);
        assertThat(SatDumpApiParser.parse("{\"live_pipeline\":{}}").driftReason())
                .isEqualTo(SatDumpApiParser.DRIFT_REASON);
        assertThat(SatDumpApiParser.parse("{\"psk_demod\":{\"snr\":1},\"version\":2}").driftReason())
                .isEqualTo(SatDumpApiParser.DRIFT_REASON);
        // An empty live body is a pipeline that has not started, not drift.
        assertThat(SatDumpApiParser.parse("{}").driftReason()).isNull();
    }

    @Test
    @DisplayName("numbers: strings, arrays and junk")
    void numbers() {
        tools.jackson.databind.json.JsonMapper json = tools.jackson.databind.json.JsonMapper.builder().build();
        assertThat(SatDumpApiParser.number(json.readTree("\" 4.5 \""), SatDumpApiParser.Agg.FIRST)).isEqualTo(4.5);
        assertThat(SatDumpApiParser.number(json.readTree("[1, \"x\", 3]"), SatDumpApiParser.Agg.MAX)).isEqualTo(3.0);
        assertThat(SatDumpApiParser.number(json.readTree("[1, 3]"), SatDumpApiParser.Agg.MEAN)).isEqualTo(2.0);
        assertThat(SatDumpApiParser.number(json.readTree("[]"), SatDumpApiParser.Agg.MEAN)).isNull();
        assertThat(SatDumpApiParser.number(json.readTree("\"n/a\""), SatDumpApiParser.Agg.FIRST)).isNull();
        assertThat(SatDumpApiParser.number(json.readTree("{}"), SatDumpApiParser.Agg.FIRST)).isNull();
    }
}
