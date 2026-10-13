package ai.foresignal.source.codec;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.adapters.space.localsonde.LocalSondeAdapter;
import ai.foresignal.source.formats.sonde.RadiosondeRecords;
import ai.foresignal.source.spi.Framing;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Observer;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * Codec {@code sonde-udp} ({@link AutoRxPayloadCodec}) on the generated {@code PAYLOAD_SUMMARY} datagrams: other
 * types are ignored, malformed JSON and unusable summaries are counted, records follow the shared radiosonde
 * contract.
 */
class AutoRxPayloadCodecTest {

    static final Instant RECEIVED = Instant.parse("2026-10-13T20:40:10Z");

    static List<NormalizedRecord> decodeAll(AutoRxPayloadCodec.Session session) {
        List<NormalizedRecord> out = new ArrayList<>();
        for (String line : AdapterFixtures.text("local-sonde", "datagrams.jsonl").split("\n")) {
            if (!line.isBlank()) {
                out.addAll(session.onFrame(line.getBytes(StandardCharsets.UTF_8), RECEIVED));
            }
        }
        return out;
    }

    @Test
    @DisplayName("the id is sonde-udp, one datagram is one frame, nothing is written on connect")
    void identity() {
        AutoRxPayloadCodec codec = new AutoRxPayloadCodec();
        assertThat(codec.id()).isEqualTo("sonde-udp");
        assertThat(codec.framing()).isEqualTo(Framing.RAW);
        assertThat(codec.onConnect()).isNull();
        assertThat(new CodecRegistry(List.of(codec)).require("sonde-udp")).isSameAs(codec);
    }

    @Test
    @DisplayName("the datagrams: 4 records; MODEM_STATS ignored; malformed JSON and a summary without callsign counted")
    void datagrams() {
        AutoRxPayloadCodec.Session session = (AutoRxPayloadCodec.Session) new AutoRxPayloadCodec()
                .newSession(CodecContext.of("local-sonde"));
        List<NormalizedRecord> records = decodeAll(session);
        assertThat(records).extracting(NormalizedRecord::externalId)
                .containsExactly("S1234567", "S1234567", "M1234567", "D1234567");
        assertThat(session.ignored()).isEqualTo(1);
        assertThat(session.skipped()).isEqualTo(2);

        NormalizedRecord first = records.getFirst();
        assertThat(first.kind()).isEqualTo(RadiosondeRecords.KIND);
        assertThat(first.observedAt()).isEqualTo(Instant.parse("2026-10-13T20:40:00Z"));
        assertThat(first.geometry().getCoordinate().x).isEqualTo(14.12191);
        assertThat(first.payload()).containsEntry("altitudeM", 5123.4).containsEntry("horizontalSpeedMs", 10.0)
                .containsEntry("headingDeg", 93.0).containsEntry("type", "RS41-SGP").containsEntry("frequencyMHz", 404.5)
                .containsEntry("tempC", -21.3).containsEntry("humidityPct", 45.2).containsEntry("pressureHpa", 532.11)
                .doesNotContainKeys("frame", "batt", "sats", "time", "station", "callsign");
        assertThat(first.observers()).containsExactly(new Observer("N0CALL-RX", null, null, 12.4, null));

        NormalizedRecord m10 = records.get(2);
        assertThat(m10.payload()).as("sentinels dropped; a numeric freq").containsEntry("frequencyMHz", 403.9)
                .doesNotContainKeys("tempC", "humidityPct", "pressureHpa");
        assertThat(records.get(3).geometry()).as("no position").isNull();
        FieldSpecContract.assertPayloadKeysDeclared(new LocalSondeAdapter(new AutoRxPayloadCodec()).descriptor(), records);
    }

    @Test
    @DisplayName("helpers: frequency text or number, ISO times with or without an offset, non-object JSON")
    void helpers() {
        JsonMapper json = JsonMapper.builder().build();
        assertThat(AutoRxPayloadCodec.frequency(json.readTree("\"402.500 MHz\""))).isEqualTo(402.5);
        assertThat(AutoRxPayloadCodec.frequency(json.readTree("401.1"))).isEqualTo(401.1);
        assertThat(AutoRxPayloadCodec.frequency(json.readTree("\"none\""))).isNull();
        assertThat(AutoRxPayloadCodec.frequency(json.readTree("true"))).isNull();
        assertThat(AutoRxPayloadCodec.frequency(null)).isNull();
        assertThat(AutoRxPayloadCodec.iso("2026-10-13T20:40:00Z")).isEqualTo(Instant.parse("2026-10-13T20:40:00Z"));
        assertThat(AutoRxPayloadCodec.iso("2026-10-13T20:40:00.5")).isEqualTo(Instant.parse("2026-10-13T20:40:00.5Z"));
        assertThat(AutoRxPayloadCodec.iso("20:40:00")).isNull();
        AutoRxPayloadCodec.Session session = (AutoRxPayloadCodec.Session) new AutoRxPayloadCodec()
                .newSession(CodecContext.of("local-sonde"));
        assertThat(session.onFrame("[1,2]".getBytes(StandardCharsets.UTF_8), RECEIVED)).isEmpty();
        assertThat(session.onFrame("{\"type\": \"PAYLOAD_SUMMARY\", \"callsign\": \"X\", \"datetime\": \"bad\"}"
                .getBytes(StandardCharsets.UTF_8), RECEIVED)).isEmpty();
        assertThat(session.skipped()).isEqualTo(2);
    }

    @Test
    @DisplayName("a new session starts with zero counters; repeated telemetry keeps the static hash")
    void sessionsAndSnapshot() {
        AutoRxPayloadCodec codec = new AutoRxPayloadCodec();
        AutoRxPayloadCodec.Session a = (AutoRxPayloadCodec.Session) codec.newSession(CodecContext.of("local-sonde"));
        decodeAll(a);
        AutoRxPayloadCodec.Session b = (AutoRxPayloadCodec.Session) codec.newSession(CodecContext.of("local-sonde"));
        assertThat(b.skipped()).isZero();
        assertThat(b.ignored()).isZero();
        byte[] line = AdapterFixtures.text("local-sonde", "datagrams.jsonl").split("\n")[0]
                .getBytes(StandardCharsets.UTF_8);
        SnapshotContract.assertStableStaticHash(new LocalSondeAdapter(codec).descriptor(),
                List.of(b.onFrame(line, RECEIVED).getFirst(), b.onFrame(line, RECEIVED).getFirst()));
    }
}
