package ai.foresignal.source.codec;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.formats.sonde.RadiosondeRecords;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

/**
 * radiosonde_auto_rx {@code PAYLOAD_SUMMARY} parsing through codec {@code sonde-udp} as prompt 086 extends it: the ten
 * captured summaries map onto 077's SondeHub payload keys, other types are ignored, the station callsign parameter is the
 * observer with {@code freq} as {@code frequencyHz}, and a summary without {@code datetime} uses {@code time}.
 */
class AutoRxSummaryParserTest {

    private static final Instant RECEIVED = Instant.parse("2026-10-13T12:00:50Z");

    private List<NormalizedRecord> run(CodecSession session) {
        List<NormalizedRecord> out = new ArrayList<>();
        for (String line : AdapterFixtures.text("local-rf", "autorx-summaries.jsonl").lines().toList()) {
            out.addAll(session.onFrame(line.getBytes(StandardCharsets.UTF_8), RECEIVED));
        }
        return out;
    }

    @Test
    @DisplayName("ten summaries become radiosonde records with the 077 keys; the LOG datagram is ignored")
    void summaries() {
        AutoRxPayloadCodec codec = new AutoRxPayloadCodec();
        CodecSession session = codec.newSession(new CodecContext("local-sonde",
                ValidatedParams.of(Map.of(AutoRxPayloadCodec.STATION_CALLSIGN_PARAM, "FS0HOME"))));
        List<NormalizedRecord> records = run(session);
        assertThat(records).hasSize(10);
        assertThat(((AutoRxPayloadCodec.Session) session).ignored()).isEqualTo(1);
        assertThat(session.skipped()).isZero();
        assertThat(records).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo(RadiosondeRecords.KIND);
            assertThat(r.externalId()).isEqualTo("S1234567");
            assertThat(r.payload().keySet()).isSubsetOf(RadiosondeRecords.PAYLOAD_FIELDS);
            assertThat(r.payload()).doesNotContainKeys("frame", "comment", "sdr_device_idx", "time");
            assertThat(r.observers()).singleElement().satisfies(o -> {
                assertThat(o.receiverId()).isEqualTo("FS0HOME");
                assertThat(o.frequencyHz()).isEqualTo(403_001_000L);
            });
        });
        NormalizedRecord first = records.getFirst();
        assertThat(first.observedAt()).isEqualTo(Instant.parse("2026-10-13T12:00:00Z"));
        assertThat(first.payload()).containsEntry("altitudeM", 1700.0).containsEntry("type", "RS41")
                .containsEntry("horizontalSpeedMs", 5.0).containsEntry("headingDeg", 75.0)
                .containsEntry("frequencyMHz", 403.001).containsEntry("tempC", 12.5).containsEntry("humidityPct", 45.0);
        assertThat(((Point) first.geometry()).getY()).isEqualTo(40.1);
        assertThat(records.getLast().observedAt()).as("time only, on the receive day")
                .isEqualTo(Instant.parse("2026-10-13T12:00:45Z"));
    }

    @Test
    @DisplayName("without the parameter the observer is auto_rx's station; time-of-day keeps the nearest day")
    void defaultsAndMidnight() {
        CodecSession session = new AutoRxPayloadCodec().newSession(CodecContext.of("local-sonde"));
        assertThat(run(session).getFirst().observers().getFirst().receiverId()).isEqualTo("FS0TEST");
        assertThat(AutoRxPayloadCodec.timeOfDay("23:59:58", Instant.parse("2026-10-14T00:00:03Z")))
                .isEqualTo(Instant.parse("2026-10-13T23:59:58Z"));
        assertThat(AutoRxPayloadCodec.timeOfDay("00:00:02", Instant.parse("2026-10-13T23:59:59Z")))
                .isEqualTo(Instant.parse("2026-10-14T00:00:02Z"));
        assertThat(AutoRxPayloadCodec.timeOfDay("nope", RECEIVED)).isNull();
        assertThat(AutoRxPayloadCodec.timeOfDay(null, RECEIVED)).isNull();
    }
}
