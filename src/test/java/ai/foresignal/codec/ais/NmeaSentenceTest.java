package ai.foresignal.codec.ais;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** AIS NMEA sentences (prompt 085): fields, fill bits, checksum, talkers and malformed input. */
class NmeaSentenceTest {

    @Test
    @DisplayName("a single sentence: talker, fragment fields, channel, payload and fill bits")
    void single() {
        NmeaSentence s = AisGoldens.sentence("gpsd-type1", 0);

        assertThat(s.talker()).isEqualTo("AIVDM");
        assertThat(s.fragmentCount()).isEqualTo(1);
        assertThat(s.fragmentNumber()).isEqualTo(1);
        assertThat(s.sequentialId()).isNull();
        assertThat(s.channel()).isEqualTo("A");
        assertThat(s.payload()).isEqualTo("15RTgt0PAso;90TKcjM8h6g208CQ");
        assertThat(s.fillBits()).isZero();
        assertThat(s.single()).isTrue();
        assertThat(s.format()).isEqualTo("!AIVDM,1,1,,A,15RTgt0PAso;90TKcjM8h6g208CQ,0*4A");
    }

    @Test
    @DisplayName("the last fragment of a two-part message carries the sequential id and 2 fill bits")
    void fillBits() {
        NmeaSentence s = AisGoldens.sentence("gpsd-type5", 1);

        assertThat(s.fragmentCount()).isEqualTo(2);
        assertThat(s.fragmentNumber()).isEqualTo(2);
        assertThat(s.sequentialId()).isEqualTo(1);
        assertThat(s.fillBits()).isEqualTo(2);
        assertThat(s.single()).isFalse();
    }

    @Test
    @DisplayName("a bad checksum is reported, not parsed")
    void badChecksum() {
        assertThat(NmeaSentence.parse(AisGoldens.sentences("bad-checksum").getFirst()))
                .isInstanceOf(NmeaSentence.BadChecksum.class);
    }

    @Test
    @DisplayName("BSVDM and AIVDO are accepted; other talkers, missing checksums and broken fields are malformed")
    void talkersAndMalformed() {
        String bs = new NmeaSentence("BSVDM", 1, 1, null, "B", "15RTgt0PAso;90TKcjM8h6g208CQ", 0).format();
        assertThat(NmeaSentence.parse(bs)).isInstanceOf(NmeaSentence.Parsed.class);
        String vdo = new NmeaSentence("AIVDO", 1, 1, null, "", "15RTgt0PAso;90TKcjM8h6g208CQ", 0).format();
        assertThat(NmeaSentence.parse(vdo)).isInstanceOf(NmeaSentence.Parsed.class);

        String gp = "!GPVDM,1,1,,A,15RTgt0PAso;90TKcjM8h6g208CQ,0";
        assertThat(NmeaSentence.parse(gp + "*" + String.format("%02X", NmeaSentence.checksum(gp.substring(1)))))
                .isInstanceOf(NmeaSentence.Malformed.class);
        assertThat(NmeaSentence.parse("!AIVDM,1,1,,A,15RTgt0PAso;90TKcjM8h6g208CQ,0"))
                .isInstanceOf(NmeaSentence.Malformed.class);
        String tenFragments = "!AIVDM,10,1,,A,15RT,0";
        assertThat(NmeaSentence.parse(tenFragments + "*"
                + String.format("%02X", NmeaSentence.checksum(tenFragments.substring(1)))))
                .isInstanceOf(NmeaSentence.Malformed.class);
        String badChar = "!AIVDM,1,1,,A,15RT~,0";
        assertThat(NmeaSentence.parse(badChar + "*" + String.format("%02X", NmeaSentence.checksum(badChar.substring(1)))))
                .isInstanceOf(NmeaSentence.Malformed.class);
        assertThat(NmeaSentence.parse(null)).isInstanceOf(NmeaSentence.Malformed.class);
        assertThat(NmeaSentence.parse("$GPGGA,1*00")).isInstanceOf(NmeaSentence.Malformed.class);
    }

    @Test
    @DisplayName("trailing CR LF is ignored")
    void trailingWhitespace() {
        assertThat(NmeaSentence.parse(AisGoldens.sentences("type1").getFirst() + "\r\n"))
                .isInstanceOf(NmeaSentence.Parsed.class);
    }
}
