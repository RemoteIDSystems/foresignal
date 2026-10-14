package ai.foresignal.codec.ais;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Golden sentences for every decoded type (prompt 085), with the values pyais 3.3.1 reads from the same sentences
 * (an independent decoder, used only as an oracle), and every sentinel turned into an absent value.
 */
class AisMessageDecoderTest {

    @Test
    @DisplayName("type 1: Class A position with every field")
    void type1() {
        AisMessage.ClassAPosition m = (AisMessage.ClassAPosition) AisGoldens.decode("type1");

        assertThat(m.type()).isEqualTo(1);
        assertThat(m.mmsi()).isEqualTo(257_123_450);
        assertThat(m.mmsiText()).isEqualTo("257123450");
        assertThat(m.navStatus()).isZero();
        assertThat(m.rot()).as("raw ROT_AIS of -10 deg/min").isEqualTo(-15);
        assertThat(m.sog()).isEqualTo(12.3);
        assertThat(m.accuracy()).isTrue();
        assertThat(m.longitude()).isEqualTo(10.5);
        assertThat(m.latitude()).isEqualTo(59.9);
        assertThat(m.cog()).isEqualTo(224.0);
        assertThat(m.heading()).isEqualTo(215);
        assertThat(m.second()).isEqualTo(33);
        assertThat(m.raim()).isTrue();
    }

    @Test
    @DisplayName("types 2 and 3 decode as Class A positions with their own type")
    void types2And3() {
        AisMessage.ClassAPosition two = (AisMessage.ClassAPosition) AisGoldens.decode("type2");
        assertThat(two.type()).isEqualTo(2);
        assertThat(two.navStatus()).isEqualTo(1);
        assertThat(two.sog()).isEqualTo(0.1);
        assertThat(two.cog()).isEqualTo(90.5);
        assertThat(two.maneuver()).isEqualTo(1);

        AisMessage.ClassAPosition three = (AisMessage.ClassAPosition) AisGoldens.decode("type3");
        assertThat(three.type()).isEqualTo(3);
        assertThat(three.navStatus()).isEqualTo(5);
        assertThat(three.rot()).isEqualTo(127);
        assertThat(three.longitude()).isEqualTo(-123.5);
        assertThat(three.latitude()).isEqualTo(48.25);
    }

    @Test
    @DisplayName("the gpsd sample: MMSI 371798000 at 48.381633, -123.395383, 12.3 kn")
    void gpsdSample() {
        AisMessage.ClassAPosition m = (AisMessage.ClassAPosition) AisGoldens.decode("gpsd-type1");
        assertThat(m.mmsi()).isEqualTo(371_798_000);
        assertThat(m.latitude()).isCloseTo(48.381633, within(1e-6));
        assertThat(m.longitude()).isCloseTo(-123.395383, within(1e-6));
        assertThat(m.sog()).isEqualTo(12.3);
        assertThat(m.heading()).isEqualTo(215);
        assertThat(m.radio()).isEqualTo(34017);
    }

    @Test
    @DisplayName("type 1 sentinels: lat 91, lon 181, SOG 102.3, COG 360, heading 511, ROT -128, status 15 are absent")
    void type1Sentinels() {
        AisMessage.ClassAPosition m = (AisMessage.ClassAPosition) AisGoldens.decode("type1-sentinels");
        assertThat(m.latitude()).isNull();
        assertThat(m.longitude()).isNull();
        assertThat(m.sog()).isNull();
        assertThat(m.cog()).isNull();
        assertThat(m.heading()).isNull();
        assertThat(m.rot()).isNull();
        assertThat(m.navStatus()).isNull();
    }

    @Test
    @DisplayName("type 5: static and voyage data")
    void type5() {
        AisMessage.StaticVoyage m = (AisMessage.StaticVoyage) AisGoldens.decode("type5");
        assertThat(m.mmsi()).isEqualTo(257_123_450);
        assertThat(m.imo()).isEqualTo(9_134_270);
        assertThat(m.callsign()).isEqualTo("LAXY7");
        assertThat(m.name()).isEqualTo("NORDIC STAR");
        assertThat(m.shipType()).isEqualTo(70);
        assertThat(m.dimension()).isEqualTo(new AisMessage.Dimension(120, 30, 10, 12));
        assertThat(m.dimension().lengthM()).isEqualTo(150);
        assertThat(m.dimension().beamM()).isEqualTo(22);
        assertThat(m.epfd()).isEqualTo(1);
        assertThat(m.eta()).isEqualTo(new AisMessage.Eta(10, 14, 21, 30));
        assertThat(m.draft()).isEqualTo(8.4);
        assertThat(m.destination()).isEqualTo("BERGEN");
        assertThat(m.dte()).isFalse();

        AisMessage.StaticVoyage gpsd = (AisMessage.StaticVoyage) AisGoldens.decode("gpsd-type5");
        assertThat(gpsd.name()).isEqualTo("EVER DIADEM");
        assertThat(gpsd.callsign()).isEqualTo("3FOF8");
        assertThat(gpsd.destination()).isEqualTo("NEW YORK");
        assertThat(gpsd.draft()).isEqualTo(12.2);
        assertThat(gpsd.eta()).isEqualTo(new AisMessage.Eta(5, 15, 14, 0));
    }

    @Test
    @DisplayName("type 5 sentinels: IMO 0, empty texts, type 0, zero dimensions, ETA month 0 and draft 0 are absent")
    void type5Sentinels() {
        AisMessage.StaticVoyage m = (AisMessage.StaticVoyage) AisGoldens.decode("type5-sentinels");
        assertThat(m.imo()).isNull();
        assertThat(m.callsign()).isNull();
        assertThat(m.name()).isNull();
        assertThat(m.shipType()).isNull();
        assertThat(m.dimension()).isNull();
        assertThat(m.eta()).isNull();
        assertThat(m.draft()).isNull();
        assertThat(m.destination()).isNull();
    }

    @Test
    @DisplayName("type 18: Class B position, heading 511 absent")
    void type18() {
        AisMessage.ClassBPosition m = (AisMessage.ClassBPosition) AisGoldens.decode("type18");
        assertThat(m.mmsi()).isEqualTo(257_987_650);
        assertThat(m.sog()).isEqualTo(5.5);
        assertThat(m.accuracy()).isTrue();
        assertThat(m.longitude()).isEqualTo(10.75);
        assertThat(m.latitude()).isEqualTo(59.875);
        assertThat(m.cog()).isEqualTo(180.0);
        assertThat(m.heading()).isNull();
        assertThat(m.cs()).isTrue();
        assertThat(m.dsc()).isTrue();
        assertThat(m.band()).isTrue();
        assertThat(m.msg22()).isTrue();
        assertThat(m.display()).isFalse();
    }

    @Test
    @DisplayName("type 19: extended Class B with name, type and dimensions")
    void type19() {
        AisMessage.ClassBExtended m = (AisMessage.ClassBExtended) AisGoldens.decode("type19");
        assertThat(m.mmsi()).isEqualTo(257_987_651);
        assertThat(m.sog()).isEqualTo(7.2);
        assertThat(m.longitude()).isEqualTo(11.0);
        assertThat(m.latitude()).isEqualTo(58.5);
        assertThat(m.heading()).isEqualTo(44);
        assertThat(m.name()).isEqualTo("SEA BREEZE");
        assertThat(m.shipType()).isEqualTo(37);
        assertThat(m.dimension()).isEqualTo(new AisMessage.Dimension(8, 4, 2, 2));
    }

    @Test
    @DisplayName("type 24 parts A and B")
    void type24() {
        AisMessage.StaticDataA a = (AisMessage.StaticDataA) AisGoldens.decode("type24a");
        assertThat(a.mmsi()).isEqualTo(257_987_650);
        assertThat(a.name()).isEqualTo("LITTLE WAVE");

        AisMessage.StaticDataB b = (AisMessage.StaticDataB) AisGoldens.decode("type24b");
        assertThat(b.shipType()).isEqualTo(36);
        assertThat(b.vendorId()).isEqualTo("ABC");
        assertThat(b.model()).isEqualTo(2);
        assertThat(b.serial()).isEqualTo(12345);
        assertThat(b.callsign()).isEqualTo("LH2345");
        assertThat(b.dimension()).isEqualTo(new AisMessage.Dimension(6, 3, 1, 2));
        assertThat(b.mothershipMmsi()).isNull();
    }

    @Test
    @DisplayName("type 27: long-range position at 1/10 minute, and its sentinels (SOG 63, COG 511, lat 91, lon 181)")
    void type27() {
        AisMessage.LongRangePosition m = (AisMessage.LongRangePosition) AisGoldens.decode("type27");
        assertThat(m.mmsi()).isEqualTo(257_123_454);
        assertThat(m.longitude()).isEqualTo(10.5);
        assertThat(m.latitude()).isEqualTo(59.9);
        assertThat(m.sog()).isEqualTo(12.0);
        assertThat(m.cog()).isEqualTo(224.0);
        assertThat(m.navStatus()).isZero();
        assertThat(m.gnss()).isFalse();

        AisMessage.LongRangePosition s = (AisMessage.LongRangePosition) AisGoldens.decode("type27-sentinels");
        assertThat(s.longitude()).isNull();
        assertThat(s.latitude()).isNull();
        assertThat(s.sog()).isNull();
        assertThat(s.cog()).isNull();
        assertThat(s.navStatus()).isNull();
        assertThat(s.gnss()).isTrue();
    }

    @Test
    @DisplayName("types 4 and 21 are recognized for counting only; other types are unsupported")
    void countedOnly() {
        assertThat(AisGoldens.decode("type4")).isEqualTo(new AisMessage.BaseStationReport(0, 2_573_485));
        assertThat(AisGoldens.decode("type21")).isEqualTo(new AisMessage.AidToNavigation(0, 992_576_001));

        SixBit.Armored type8 = new SixBit.Writer().unsigned(6, 8).unsigned(2, 0).unsigned(30, 257_123_450)
                .unsigned(10, 0).armor();
        assertThat(AisDecoder.decode(type8)).isEqualTo(new AisMessage.Unsupported(8, 0, 257_123_450));
    }

    @Test
    @DisplayName("a payload too short for its type, or for a header, fails to decode")
    void tooShort() {
        String type1 = AisGoldens.sentence("type1", 0).payload();
        assertThatThrownBy(() -> AisDecoder.decode(type1.substring(0, 20), 0))
                .isInstanceOf(AisDecoder.AisDecodeException.class);
        assertThatThrownBy(() -> AisDecoder.decode("13", 0)).isInstanceOf(AisDecoder.AisDecodeException.class);
        assertThatThrownBy(() -> AisDecoder.decode("13~~", 0)).isInstanceOf(AisDecoder.AisDecodeException.class);
    }

    @Test
    @DisplayName("an auxiliary craft's part B carries the mother ship instead of dimensions")
    void auxiliary() {
        AisMessage.StaticDataB aux = new AisMessage.StaticDataB(0, 982_570_001, 36, "ABC", 1, 2, "LH1", null,
                257_123_450);
        AisMessage decoded = AisDecoder.decode(AisEncoder.encode(aux));
        assertThat(decoded).isEqualTo(aux);
        assertThat(AisMessage.StaticDataB.auxiliary(982_570_001)).isTrue();
        assertThat(AisMessage.StaticDataB.auxiliary(257_123_450)).isFalse();
    }
}
