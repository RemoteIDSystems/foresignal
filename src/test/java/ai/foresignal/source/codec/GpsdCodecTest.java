package ai.foresignal.source.codec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

/**
 * Codec {@code gpsd} (prompt 069): the {@code ?WATCH} write, the VERSION check, TPV and SKY merged per
 * device, the {@code navDegraded} boundaries, the deprecated {@code alt} ignored, mode-1 fixes without a
 * position and unparsable lines skipped.
 */
class GpsdCodecTest {

    private static final Instant RECEIVED = Instant.parse("2026-10-13T12:00:10Z");

    private final GpsdCodec codec = new GpsdCodec();

    private static byte[] line(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    private List<NormalizedRecord> session(List<String> lines, CodecSession s) {
        List<NormalizedRecord> out = new ArrayList<>();
        for (String l : lines) {
            out.addAll(s.onFrame(line(l), RECEIVED));
        }
        return out;
    }

    @Test
    @DisplayName("?WATCH is written on connect, exactly")
    void watch() {
        assertThat(new String(codec.onConnect(), StandardCharsets.US_ASCII))
                .isEqualTo("?WATCH={\"enable\":true,\"json\":true};\n");
        assertThat(codec.id()).isEqualTo("gpsd");
    }

    @Test
    @DisplayName("the captured session: TPV records per device, SKY merged in, a junk line skipped")
    void session() {
        CodecSession s = codec.newSession(CodecContext.of("local-gpsd-1"));
        List<NormalizedRecord> out = session(AdapterFixtures.text("local-receivers", "gpsd-session.jsonl").lines().toList(), s);
        assertThat(((GpsdCodec.Session) s).protoMajor()).isEqualTo(3);
        assertThat(s.skipped()).isEqualTo(1);
        // TPV(1), SKY, TPV(2), SKY, TPV(3), SKY, TPV(3), TPV(3).
        assertThat(out).hasSize(8);
        assertThat(out).extracting(NormalizedRecord::externalId).containsOnly("/dev/ttyACM0");
        assertThat(out).extracting(NormalizedRecord::kind).containsOnly("gnss-fix");

        NormalizedRecord mode1 = out.getFirst();
        assertThat(mode1.geometry()).isNull();
        assertThat(mode1.payload()).containsEntry("mode", 1).containsEntry("fixQuality", "none")
                .containsEntry("navDegraded", true).doesNotContainKeys("altHAE", "speed", "track");
        assertThat(mode1.observedAt()).isEqualTo(Instant.parse("2026-10-13T12:00:01Z"));

        NormalizedRecord threeD = out.get(4);
        assertThat(threeD.payload()).containsEntry("mode", 3).containsEntry("fixQuality", "3d")
                .containsEntry("altHAE", 1625.3).containsEntry("eph", 5.1).containsEntry("epv", 7.8)
                .containsEntry("speed", 0.0).containsEntry("nSat", 11).containsEntry("uSat", 5)
                .containsEntry("hdop", 1.43).containsEntry("navDegraded", false)
                .doesNotContainKey("alt");
        assertThat(((Point) threeD.geometry()).getY()).isEqualTo(40.012345678);
        assertThat(threeD.quality().accuracyMeters()).isEqualTo(5.1);

        NormalizedRecord afterSky = out.get(5);
        assertThat(afterSky.payload()).containsEntry("uSat", 8).containsEntry("nSat", 12);
    }

    @Test
    @DisplayName("navDegraded: mode < 3, uSat < 4 or eph > 50; unknown terms are left out")
    void navDegradedBoundaries() {
        assertThat(GpsdCodec.navDegraded(3, 4, 50.0)).isFalse();
        assertThat(GpsdCodec.navDegraded(3, 3, 50.0)).isTrue();
        assertThat(GpsdCodec.navDegraded(3, 4, 50.1)).isTrue();
        assertThat(GpsdCodec.navDegraded(2, 12, 1.0)).isTrue();
        assertThat(GpsdCodec.navDegraded(3, null, null)).isFalse();
        assertThat(GpsdCodec.fixQuality(0)).isEqualTo("none");
        assertThat(GpsdCodec.fixQuality(2)).isEqualTo("2d");
    }

    @Test
    @DisplayName("a SKY before any TPV emits nothing; a TPV without time is imputed and flagged")
    void skyFirstAndMissingTime() {
        CodecSession s = codec.newSession(CodecContext.of("local-gpsd-1"));
        assertThat(s.onFrame(line("{\"class\":\"SKY\",\"device\":\"d\",\"uSat\":7,\"nSat\":9}"), RECEIVED)).isEmpty();
        NormalizedRecord r = s.onFrame(line("{\"class\":\"TPV\",\"device\":\"d\",\"mode\":1}"), RECEIVED).getFirst();
        assertThat(r.observedAt()).isEqualTo(RECEIVED);
        assertThat(r.quality().extra()).containsEntry(GpsdCodec.TIME_IMPUTED_EXTRA, true);
        assertThat(r.payload()).containsEntry("uSat", 7);
    }

    @Test
    @DisplayName("devices are separate records")
    void devices() {
        CodecSession s = codec.newSession(CodecContext.of("local-gpsd-1"));
        s.onFrame(line("{\"class\":\"TPV\",\"device\":\"a\",\"mode\":3,\"time\":\"2026-10-13T12:00:00Z\",\"lat\":1,\"lon\":2}"),
                RECEIVED);
        NormalizedRecord b = s.onFrame(line("{\"class\":\"TPV\",\"mode\":2,\"time\":\"2026-10-13T12:00:00Z\",\"lat\":3,\"lon\":4}"),
                RECEIVED).getFirst();
        assertThat(b.externalId()).isEqualTo("gpsd");
    }

    @Test
    @DisplayName("a VERSION with another protocol major closes the stream")
    void wrongProtocol() {
        CodecSession s = codec.newSession(CodecContext.of("local-gpsd-1"));
        assertThatThrownBy(() -> s.onFrame(line("{\"class\":\"VERSION\",\"proto_major\":2,\"proto_minor\":1}"), RECEIVED))
                .isInstanceOf(UpstreamFormatException.class).hasMessageContaining("protocol 2");
    }
}
