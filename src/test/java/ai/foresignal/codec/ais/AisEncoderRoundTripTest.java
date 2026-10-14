package ai.foresignal.codec.ais;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The encoder for prompt 110 (prompt 085): {@code decode(encode(x)) == x} for types 1, 2, 3, 5, 18 and 24 A/B,
 * sentinels included, and {@link NmeaWriter}'s fragments within the 82-character limit with the right sequential ids
 * and fill bits.
 */
class AisEncoderRoundTripTest {

    private static final List<String> ENCODED = List.of("type1", "type2", "type3", "type1-sentinels", "type5",
            "type5-sentinels", "type18", "type24a", "type24b", "gpsd-type1", "gpsd-type5");

    @Test
    @DisplayName("every encodable golden decodes back to itself after encoding")
    void roundTrip() {
        for (String name : ENCODED) {
            AisMessage x = AisGoldens.decode(name);
            assertThat(AisDecoder.decode(AisEncoder.encode(x))).as(name).isEqualTo(x);
        }
    }

    @Test
    @DisplayName("the encoder reproduces the golden payloads bit for bit (independently produced sentences)")
    void sameBits() {
        for (String name : List.of("type1", "type2", "type3", "type1-sentinels", "type18", "type24a", "type24b")) {
            assertThat(AisEncoder.encode(AisGoldens.decode(name)).payload()).as(name)
                    .isEqualTo(AisGoldens.sentence(name, 0).payload());
        }
    }

    @Test
    @DisplayName("hand-built messages on the wire resolution round-trip, with absent values as sentinels")
    void handBuilt() {
        AisMessage.ClassAPosition a = new AisMessage.ClassAPosition(3, 1, 257_000_001, null, null, null, false, null,
                null, null, null, 60, 0, false, 0);
        assertThat(AisDecoder.decode(AisEncoder.encode(a))).isEqualTo(a);

        AisMessage.StaticVoyage v = new AisMessage.StaticVoyage(0, 257_000_002, 1, 1_234_567, "LA1", "TEST",
                null, new AisMessage.Dimension(10, 0, 0, 0), 1, new AisMessage.Eta(12, 31, 23, 59), 25.5, "X", true);
        assertThat(AisDecoder.decode(AisEncoder.encode(v))).isEqualTo(v);

        AisMessage.ClassBPosition b = new AisMessage.ClassBPosition(0, 257_000_003, 0.0, false, -180.0, -90.0, 359.9,
                359, 0, 3, false, true, false, false, false, true, true, 1_048_575);
        assertThat(AisDecoder.decode(AisEncoder.encode(b))).isEqualTo(b);
    }

    @Test
    @DisplayName("types the encoder does not write are refused")
    void unsupported() {
        assertThatThrownBy(() -> AisEncoder.encode(AisGoldens.decode("type27")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AisEncoder.encode(AisGoldens.decode("type19")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AisEncoder.encode(new AisMessage.ClassAPosition(4, 0, 257_000_001, null, null, null,
                false, null, null, null, null, 0, 0, false, 0))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("a type 5 splits into two sentences of at most 82 characters with CR LF, fill bits on the last")
    void fragments() {
        NmeaWriter writer = new NmeaWriter();
        List<String> lines = writer.write(AisGoldens.decode("type5"), "A");

        assertThat(lines).hasSize(2);
        assertThat(lines).allSatisfy(l -> assertThat(l.length() + 2).isLessThanOrEqualTo(NmeaWriter.MAX_SENTENCE));
        assertThat(lines).as("the same sentences an independent encoder produced").isEqualTo(AisGoldens.sentences("type5"));
        NmeaSentence first = ((NmeaSentence.Parsed) NmeaSentence.parse(lines.get(0))).sentence();
        NmeaSentence last = ((NmeaSentence.Parsed) NmeaSentence.parse(lines.get(1))).sentence();
        assertThat(first.fillBits()).isZero();
        assertThat(last.fillBits()).isEqualTo(2);
        assertThat(first.sequentialId()).isZero();
        assertThat(last.sequentialId()).isZero();

        FragmentAssembler assembler = new FragmentAssembler();
        assembler.add(first, null, Instant.EPOCH);
        Optional<FragmentAssembler.Assembled> done = assembler.add(last, null, Instant.EPOCH);
        assertThat(AisDecoder.decode(done.orElseThrow().armored())).isEqualTo(AisGoldens.decode("type5"));
    }

    @Test
    @DisplayName("sequential ids cycle 0 to 9; single sentences carry none")
    void sequentialIds() {
        NmeaWriter writer = new NmeaWriter("AIVDO");
        AisMessage five = AisGoldens.decode("type5");
        for (int i = 0; i < 12; i++) {
            NmeaSentence s = ((NmeaSentence.Parsed) NmeaSentence.parse(writer.write(five, "B").getFirst())).sentence();
            assertThat(s.sequentialId()).isEqualTo(i % 10);
            assertThat(s.talker()).isEqualTo("AIVDO");
        }
        List<String> single = writer.write(AisGoldens.decode("type1"), "A");
        assertThat(single).hasSize(1);
        assertThat(((NmeaSentence.Parsed) NmeaSentence.parse(single.getFirst())).sentence().sequentialId()).isNull();
    }

    @Test
    @DisplayName("a payload needing more than 9 sentences is refused")
    void tooLong() {
        String payload = "0".repeat(60 * 9 + 1);
        assertThatThrownBy(() -> new NmeaWriter().write(new SixBit.Armored(payload, 0), "A"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(new NmeaWriter().write(new SixBit.Armored("0".repeat(540), 0), "A")).hasSize(9);
    }
}
