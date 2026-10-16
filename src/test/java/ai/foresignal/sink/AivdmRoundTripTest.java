package ai.foresignal.sink;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.codec.ais.AisDecoder;
import ai.foresignal.codec.ais.AisEncoder;
import ai.foresignal.codec.ais.AisMessage;
import ai.foresignal.codec.ais.NmeaSentence;
import ai.foresignal.codec.ais.NmeaWriter;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The AIVDM encoder round trips through the 085 decoder for types 1 and 18 (prompt 110). */
class AivdmRoundTripTest {

    @Test
    @DisplayName("a class A position (type 1) round trips")
    void classA() {
        AisMessage.ClassAPosition position = new AisMessage.ClassAPosition(1, 0, 123456789, 0, null, 12.3, true, 4.5,
                52.1, 90.0, 45, 0, 0, false, 0);
        List<String> sentences = new NmeaWriter().write(position, "A");
        assertThat(sentences).isNotEmpty();
        NmeaSentence.Parsed parsed = (NmeaSentence.Parsed) NmeaSentence.parse(sentences.get(0));
        AisMessage decoded = AisDecoder.decode(parsed.sentence().payload(), parsed.sentence().fillBits());
        assertThat(decoded).isInstanceOf(AisMessage.ClassAPosition.class);
        AisMessage.ClassAPosition back = (AisMessage.ClassAPosition) decoded;
        assertThat(back.mmsi()).isEqualTo(123456789);
        assertThat(back.latitude()).isCloseTo(52.1, org.assertj.core.data.Offset.offset(0.001));
        assertThat(back.longitude()).isCloseTo(4.5, org.assertj.core.data.Offset.offset(0.001));
        assertThat(back.sog()).isCloseTo(12.3, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    @DisplayName("a class B position (type 18) round trips")
    void classB() {
        AisMessage.ClassBPosition position = new AisMessage.ClassBPosition(0, 987654321, 8.0, true, -3.2, 44.5, 180.0,
                90, 0, 0, false, false, false, false, false, false, false, 0);
        List<String> sentences = new NmeaWriter().write(position, "B");
        NmeaSentence.Parsed parsed = (NmeaSentence.Parsed) NmeaSentence.parse(sentences.get(0));
        AisMessage decoded = AisDecoder.decode(parsed.sentence().payload(), parsed.sentence().fillBits());
        assertThat(decoded).isInstanceOf(AisMessage.ClassBPosition.class);
        assertThat(((AisMessage.ClassBPosition) decoded).mmsi()).isEqualTo(987654321);
    }
}
