package ai.foresignal.source.codec;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.formats.aprs.AprsPacket;
import ai.foresignal.source.formats.aprs.AprsRecords;
import ai.foresignal.source.spi.Framing;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Observer;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link KissAx25Codec} (prompt 082) over the KISS byte stream of {@code fixtures/local-aprs/direwolf-kiss.hex}: five
 * APRS UI frames (one on TNC port 1, one with escaped FEND/FESC bytes), a KISS command, a MeshCore payload and an
 * AX.25 SABM.
 */
class KissAx25CodecTest {

    private static final Instant T = Instant.parse("2026-10-14T00:30:00Z");

    /** The fixture's bytes. */
    static byte[] fixture() {
        StringBuilder hex = new StringBuilder();
        for (String line : AdapterFixtures.text("local-aprs", "direwolf-kiss.hex").split("\n")) {
            if (!line.startsWith("#")) {
                hex.append(line.replace(" ", "").strip());
            }
        }
        return HexFormat.of().parseHex(hex);
    }

    @Test
    @DisplayName("the codec is RAW, writes nothing on connect and has the id kiss-ax25")
    void shape() {
        KissAx25Codec codec = new KissAx25Codec();

        assertThat(codec.id()).isEqualTo("kiss-ax25");
        assertThat(codec.framing()).isEqualTo(Framing.RAW);
        assertThat(codec.onConnect()).isNull();
    }

    @Test
    @DisplayName("five records from the stream fed in 7-byte reads; the non-AX.25 and non-UI frames are counted")
    void decodesFixture() {
        byte[] bytes = fixture();
        List<AprsPacket> heard = new ArrayList<>();
        int[] nonAx25 = {0};
        KissAx25Codec.Session session = new KissAx25Codec().newSession(CodecContext.of("local-aprs-1"),
                new KissAx25Codec.PacketListener() {
                    @Override
                    public void packet(AprsPacket packet, Instant receivedAt) {
                        heard.add(packet);
                    }

                    @Override
                    public void nonAx25() {
                        nonAx25[0]++;
                    }
                });
        List<NormalizedRecord> records = new ArrayList<>();
        for (int i = 0; i < bytes.length; i += 7) {
            records.addAll(session.onFrame(Arrays.copyOfRange(bytes, i, Math.min(bytes.length, i + 7)), T));
        }

        assertThat(records).extracting(NormalizedRecord::externalId)
                .containsExactly("N0CALL-9", "N8QH-9", "N0CALL-13", "N0CALL-5", "N0CALL-3");
        assertThat(records).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo(AprsRecords.STATION_KIND);
            assertThat(r.observers()).containsExactly(Observer.of("local-aprs-1"));
        });
        assertThat(records.get(1).payload()).containsEntry("micEMessage", "Off Duty")
                .containsEntry("path", "WA6TOW-2,WIDE1*,WIDE2-2");
        assertThat(records.get(2).payload()).containsEntry("type", "weather");
        assertThat(records.get(3).payload()).containsEntry("status", "escok");
        assertThat(heard).hasSize(5);
        assertThat(nonAx25[0]).isEqualTo(2);
        assertThat(session.nonAx25()).isEqualTo(2);
        assertThat(session.unsupported()).isZero();
        assertThat(session.skipped()).isEqualTo(2);
    }

    @Test
    @DisplayName("stationCallsign names the observer; includeMessages turns on aprs-message records")
    void params() {
        byte[] message = ai.foresignal.source.formats.kiss.KissFrameDecoder.encode(0,
                ai.foresignal.source.formats.kiss.Ax25UiDecoder.encode("N0CALL-1", "APRS", List.of(),
                        ":N0CALL-2 :Hello{7".getBytes()));
        ValidatedParams params = ValidatedParams.of(Map.of(KissAx25Codec.STATION_PARAM, "N0CALL-10",
                KissAx25Codec.INCLUDE_MESSAGES_PARAM, true));

        List<NormalizedRecord> with = new KissAx25Codec().newSession(new CodecContext("local-aprs-2", params),
                KissAx25Codec.PacketListener.NONE).onFrame(message, T);
        List<NormalizedRecord> without = new KissAx25Codec().newSession(CodecContext.of("local-aprs-2"))
                .onFrame(message, T);

        assertThat(with).singleElement().satisfies(r -> {
            assertThat(r.kind()).isEqualTo(AprsRecords.MESSAGE_KIND);
            assertThat(r.observers()).containsExactly(Observer.of("N0CALL-10"));
        });
        assertThat(without).isEmpty();
    }

    @Test
    @DisplayName("an unsupported APRS payload in a UI frame is reported to the listener and dropped")
    void unsupported() {
        byte[] telemetry = ai.foresignal.source.formats.kiss.KissFrameDecoder.encode(0,
                ai.foresignal.source.formats.kiss.Ax25UiDecoder.encode("N0CALL-1", "APRS", List.of(),
                        "T#001,1,2,3,4,5,00000000".getBytes()));
        List<AprsPacket> unsupported = new ArrayList<>();
        KissAx25Codec.Session session = new KissAx25Codec().newSession(CodecContext.of("local-aprs-1"),
                new KissAx25Codec.PacketListener() {
                    @Override
                    public void unsupported(AprsPacket packet, Instant receivedAt) {
                        unsupported.add(packet);
                    }
                });

        assertThat(session.onFrame(telemetry, T)).isEmpty();
        assertThat(unsupported).singleElement().satisfies(p -> assertThat(p.source()).isEqualTo("N0CALL-1"));
        assertThat(session.unsupported()).isEqualTo(1);
    }
}
