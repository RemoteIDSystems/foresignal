package ai.foresignal.source.codec;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.formats.readsb.AircraftFields;
import ai.foresignal.source.spi.Framing;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

/**
 * Codec {@code sbs1} (prompt 069): all eight message types merged per hex into one snapshot with the readsb
 * payload keys, feet to meters, the RECEIVED time base (no {@code observedAt}), malformed lines skipped and
 * counted, and eviction after five silent minutes.
 */
class Sbs1CodecTest {

    private static final Instant T0 = Instant.parse("2026-10-13T12:00:00Z");

    private final Sbs1Codec codec = new Sbs1Codec();

    private static byte[] line(String text) {
        return text.getBytes(StandardCharsets.US_ASCII);
    }

    private static List<String> fixtureLines(String name) {
        return AdapterFixtures.text("local-receivers", name).lines().toList();
    }

    @Test
    @DisplayName("the codec frames lines and writes nothing on connect")
    void shape() {
        assertThat(codec.id()).isEqualTo("sbs1");
        assertThat(codec.framing()).isEqualTo(Framing.LINES);
        assertThat(codec.onConnect()).isNull();
    }

    @Test
    @DisplayName("200 lines of all eight types: one snapshot per message, merged per hex, nothing skipped")
    void twoHundredLines() {
        CodecSession session = codec.newSession(CodecContext.of("local-sbs-1"));
        Map<String, NormalizedRecord> latest = new LinkedHashMap<>();
        int emitted = 0;
        for (String l : fixtureLines("sbs-200.txt")) {
            for (NormalizedRecord r : session.onFrame(line(l), T0)) {
                latest.put(r.externalId(), r);
                emitted++;
            }
        }
        assertThat(emitted).isEqualTo(200);
        assertThat(session.skipped()).isZero();
        assertThat(latest).hasSize(10);
        NormalizedRecord a = latest.get("a1b2c3");
        assertThat(a.kind()).isEqualTo(AircraftFields.KIND);
        assertThat(a.observedAt()).as("RECEIVED: the framework imputes it").isNull();
        assertThat(a.payload()).containsKeys(AircraftFields.HEX, AircraftFields.FLIGHT, AircraftFields.SQUAWK,
                AircraftFields.GROUND_SPEED, AircraftFields.TRACK, AircraftFields.BARO_RATE, AircraftFields.ON_GROUND)
                .containsEntry(AircraftFields.POS_SOURCE, "other");
        assertThat(a.geometry()).isInstanceOf(Point.class);
        assertThat(a.observers()).singleElement().satisfies(o -> assertThat(o.receiverId()).isEqualTo("local-sbs-1"));
    }

    @Test
    @DisplayName("each type contributes its fields to one hex's snapshot; altitude feet become meters")
    void mergePerType() {
        CodecSession s = codec.newSession(CodecContext.of("local-sbs-1"));
        String p = "MSG,%d,1,1,ABC123,1,2026/10/13,12:00:00.000,2026/10/13,12:00:00.000,";
        List<NormalizedRecord> out = new ArrayList<>();
        out.addAll(s.onFrame(line(p.formatted(1) + "DAL77   ,,,,,,,,,,,"), T0));
        out.addAll(s.onFrame(line(p.formatted(3) + ",10000,,,40.5,-105.25,,,0,0,0,0"), T0));
        out.addAll(s.onFrame(line(p.formatted(4) + ",,250.04,93.27,,,-640,,,,,"), T0));
        out.addAll(s.onFrame(line(p.formatted(5) + ",10100,,,,,,,0,,0,0"), T0));
        out.addAll(s.onFrame(line(p.formatted(6) + ",10100,,,,,,1234,0,0,0,0"), T0));
        out.addAll(s.onFrame(line(p.formatted(7) + ",10200,,,,,,,,,,"), T0));
        out.addAll(s.onFrame(line(p.formatted(8) + ",,,,,,,,,,,0"), T0));
        assertThat(out).hasSize(7);
        Map<String, Object> last = out.getLast().payload();
        assertThat(last).containsEntry(AircraftFields.HEX, "abc123")
                .containsEntry(AircraftFields.FLIGHT, "DAL77")
                .containsEntry(AircraftFields.ALT_BARO, AircraftFields.feetToMeters(10200))
                .containsEntry(AircraftFields.GROUND_SPEED, 250.0)
                .containsEntry(AircraftFields.TRACK, 93.3)
                .containsEntry(AircraftFields.BARO_RATE, AircraftFields.fpmToMps(-640))
                .containsEntry(AircraftFields.SQUAWK, "1234")
                .containsEntry(AircraftFields.ON_GROUND, false)
                .doesNotContainKey(AircraftFields.EMERGENCY);
        assertThat(last.get(AircraftFields.ALT_BARO)).isEqualTo(3109);
        Point point = (Point) out.getLast().geometry();
        assertThat(point.getX()).isEqualTo(-105.25);
        assertThat(point.getY()).isEqualTo(40.5);

        // Type 2 (surface): on the ground, so alt_baro is left out.
        NormalizedRecord surface = s.onFrame(line(p.formatted(2) + ",,5.0,180.0,40.6,-105.3,,,,,,"), T0).getFirst();
        assertThat(surface.payload()).containsEntry(AircraftFields.ON_GROUND, true)
                .doesNotContainKey(AircraftFields.ALT_BARO);
    }

    @Test
    @DisplayName("malformed lines are skipped and counted; non-MSG lines are ignored; ~ hexes and 7700 emergencies map")
    void edgeCases() {
        CodecSession s = codec.newSession(CodecContext.of("local-sbs-1"));
        List<NormalizedRecord> out = new ArrayList<>();
        for (String l : fixtureLines("sbs-edge.txt")) {
            out.addAll(s.onFrame(line(l), T0));
        }
        // Lines: ok, 21 fields, type 9, bad hex, bad number, ok (7700), SEL, ok (~hex).
        assertThat(s.skipped()).isEqualTo(4);
        assertThat(out).hasSize(3);
        assertThat(out.get(1).payload()).containsEntry(AircraftFields.EMERGENCY, "general")
                .containsEntry(AircraftFields.SQUAWK, "7700");
        assertThat(out.get(2).externalId()).isEqualTo("~2f0001");
        assertThat(out.get(2).payload()).containsEntry(AircraftFields.FLIGHT, "TISB1");
        assertThat(Sbs1Codec.emergencyFor("7500")).isEqualTo("unlawful");
        assertThat(Sbs1Codec.emergencyFor("7600")).isEqualTo("nordo");
        assertThat(Sbs1Codec.emergencyFor(null)).isEqualTo("general");
    }

    @Test
    @DisplayName("a hex silent for more than five minutes is evicted, so its next message starts a fresh snapshot")
    void eviction() {
        Sbs1Codec.Session s = (Sbs1Codec.Session) codec.newSession(CodecContext.of("local-sbs-1"));
        String p = "MSG,%d,1,1,%s,1,2026/10/13,12:00:00.000,2026/10/13,12:00:00.000,";
        s.onFrame(line(p.formatted(1, "AAAAAA") + "OLD1    ,,,,,,,,,,,"), T0);
        s.onFrame(line(p.formatted(1, "BBBBBB") + "KEEP    ,,,,,,,,,,,"), T0.plus(Duration.ofMinutes(4)));
        assertThat(s.size()).isEqualTo(2);

        List<NormalizedRecord> later = s.onFrame(line(p.formatted(5, "BBBBBB") + ",5000,,,,,,,,,,"),
                T0.plus(Duration.ofMinutes(5)).plusSeconds(30));
        assertThat(s.size()).as("aaaaaa evicted, bbbbbb kept").isEqualTo(1);
        assertThat(later.getFirst().payload()).containsEntry(AircraftFields.FLIGHT, "KEEP");

        List<NormalizedRecord> back = s.onFrame(line(p.formatted(5, "AAAAAA") + ",6000,,,,,,,,,,"),
                T0.plus(Duration.ofMinutes(6)));
        assertThat(back.getFirst().payload()).doesNotContainKey(AircraftFields.FLIGHT);
    }

    @Test
    @DisplayName("a new session starts empty: nothing survives a reconnect")
    void sessionsAreIndependent() {
        CodecSession first = codec.newSession(CodecContext.of("local-sbs-1"));
        first.onFrame(line("MSG,1,1,1,ABCDEF,1,,,,,UAL1    ,,,,,,,,,,,"), T0);
        CodecSession second = codec.newSession(CodecContext.of("local-sbs-1"));
        NormalizedRecord r = second.onFrame(line("MSG,5,1,1,ABCDEF,1,,,,,,1000,,,,,,,,,,"), T0).getFirst();
        assertThat(r.payload()).doesNotContainKey(AircraftFields.FLIGHT);
    }
}
