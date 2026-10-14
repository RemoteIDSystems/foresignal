package ai.foresignal.source.formats.aprs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link AprsParser} (prompt 082): one line per supported format from {@code fixtures/aprs-is/formats.tsv} (real
 * lines from the APRS-IS capture where available, hand-made otherwise), plus Mic-E details, the q-construct and the
 * comment rules. Expected values were computed by hand from APRS101.
 */
class AprsParserTest {

    private static final Map<String, String> LINES = lines();

    private static Map<String, String> lines() {
        Map<String, String> out = new LinkedHashMap<>();
        for (String row : AdapterFixtures.text("aprs-is", "formats.tsv").split("\n")) {
            if (!row.isBlank()) {
                String[] parts = row.split("\t", 2);
                out.put(parts[0], parts[1]);
            }
        }
        return out;
    }

    private static AprsPacket parse(String label) {
        return AprsParser.parse(LINES.get(label));
    }

    @Test
    @DisplayName("uncompressed position: lat/lon, symbol, CSE/SPD in knots to m/s, /A= feet to meters, comment")
    void uncompressed() {
        AprsPacket p = parse("uncompressed");

        assertThat(p.type()).isEqualTo(AprsPacket.Type.POSITION);
        assertThat(p.source()).isEqualTo("N0CALL-9");
        assertThat(p.latitude()).isEqualTo(37.75);
        assertThat(p.longitude()).isCloseTo(-122.416667, within(1e-6));
        assertThat(p.symbol()).isEqualTo("/>");
        assertThat(p.courseDeg()).isEqualTo(90.0);
        assertThat(p.speedMps()).isEqualTo(18.52);
        assertThat(p.altitudeM()).isEqualTo(376.1);
        assertThat(p.comment()).isEqualTo("mobile");
        assertThat(p.qConstruct()).isEqualTo("qAR");
        assertThat(p.igate()).isEqualTo("N0CALL-10");
        assertThat(p.path()).containsExactly("WIDE1-1", "qAR", "N0CALL-10");
    }

    @Test
    @DisplayName("timestamped (@) and ambiguous positions: the timestamp is skipped, ambiguity spaces read as zero")
    void timestampedAndAmbiguous() {
        AprsPacket timed = parse("timestamped");
        AprsPacket vague = parse("ambiguous");

        assertThat(timed.latitude()).isEqualTo(37.5325);
        assertThat(timed.symbol()).isEqualTo("\\>");
        assertThat(timed.speedMps()).isEqualTo(0.0);
        assertThat(timed.altitudeM()).isEqualTo(38.1);
        assertThat(timed.comment()).isEqualTo("TD-H9");
        assertThat(vague.latitude()).isCloseTo(37.533333, within(1e-6));
        assertThat(vague.longitude()).isCloseTo(-122.001667, within(1e-6));
        assertThat(vague.courseDeg()).isNull();
        assertThat(vague.qConstruct()).isEqualTo("qAO");
    }

    @Test
    @DisplayName("compressed positions: base-91 lat/lon, course/speed or altitude from cs, an overlay table")
    void compressed() {
        AprsPacket course = parse("compressed");
        AprsPacket altitude = parse("compressed-altitude");
        AprsPacket overlay = parse("compressed-overlay");

        assertThat(course.latitude()).isCloseTo(37.75, within(1e-5));
        assertThat(course.longitude()).isCloseTo(-122.4, within(1e-5));
        assertThat(course.courseDeg()).isEqualTo(88.0);
        assertThat(course.speedMps()).isEqualTo(10.66);
        assertThat(course.comment()).isEqualTo("compressed course");
        assertThat(altitude.altitudeM()).isEqualTo(914.1);
        assertThat(altitude.courseDeg()).isNull();
        assertThat(altitude.symbol()).isEqualTo("/O");
        assertThat(overlay.symbol()).isEqualTo("R&");
        assertThat(overlay.latitude()).isCloseTo(38.104162, within(1e-6));
        assertThat(overlay.longitude()).isCloseTo(-122.211768, within(1e-6));
        assertThat(overlay.comment()).isEqualTo("DigiPi http://digipi.org/");
    }

    @Test
    @DisplayName("Mic-E: latitude and message bits from the destination, longitude, speed, course from the info field")
    void micE() {
        AprsPacket p = parse("mic-e");

        assertThat(p.type()).isEqualTo(AprsPacket.Type.MIC_E);
        assertThat(p.latitude()).isCloseTo(37.502333, within(1e-6));
        assertThat(p.longitude()).isCloseTo(-122.4805, within(1e-6));
        assertThat(p.micEMessage()).isEqualTo("Off Duty");
        assertThat(p.courseDeg()).isEqualTo(26.0);
        assertThat(p.speedMps()).isEqualTo(0.0);
        assertThat(p.symbol()).isEqualTo("/Y");
        assertThat(p.igate()).isEqualTo("W6PW-10");
    }

    @Test
    @DisplayName("Mic-E with the base-91 altitude and a type byte: In Service, 7 knots at 37 degrees, 12 m")
    void micEAltitude() {
        AprsPacket p = parse("mic-e-altitude");

        assertThat(p.micEMessage()).isEqualTo("In Service");
        assertThat(p.latitude()).isCloseTo(37.442, within(1e-6));
        assertThat(p.longitude()).isCloseTo(-122.215167, within(1e-6));
        assertThat(p.courseDeg()).isEqualTo(37.0);
        assertThat(p.speedMps()).isEqualTo(3.6);
        assertThat(p.altitudeM()).isEqualTo(12.0);
        assertThat(p.comment()).isEqualTo("_1");
    }

    @Test
    @DisplayName("Mic-E message table: standard, custom, emergency and mixed")
    void micEMessages() {
        assertThat(AprsParser.micEMessage("PPP000")).isEqualTo("Off Duty");
        assertThat(AprsParser.micEMessage("PP0000")).isEqualTo("En Route");
        assertThat(AprsParser.micEMessage("AAA000")).isEqualTo("Custom-0");
        assertThat(AprsParser.micEMessage("00A000")).isEqualTo("Custom-6");
        assertThat(AprsParser.micEMessage("000000")).isEqualTo("Emergency");
        assertThat(AprsParser.micEMessage("PA0000")).isEqualTo("Unknown");
        assertThat(AprsParser.micEDigit('K')).isZero();
        assertThat(AprsParser.micEDigit('M')).isEqualTo(-1);
    }

    @Test
    @DisplayName("position weather: wind, gust, temperature, rain, humidity and pressure in SI")
    void weather() {
        AprsPacket p = parse("weather");

        assertThat(p.type()).isEqualTo(AprsPacket.Type.WEATHER);
        assertThat(p.weather()).containsEntry("windDirDeg", 206).containsEntry("windSpeedMs", 3.13)
                .containsEntry("windGustMs", 7.6).containsEntry("tempC", 17.2).containsEntry("rainLastHourMm", 0.0)
                .containsEntry("rain24hMm", 0.25).containsEntry("rainDailyMm", 0.25).containsEntry("rh", 68)
                .containsEntry("pressureHpa", 1016.7).hasSize(9);
        assertThat(p.comment()).isEqualTo("eMB57");
    }

    @Test
    @DisplayName("weather values: missing dots are left out, h00 is 100%, three humidity digits are accepted")
    void weatherValues() {
        Map<String, Object> w = new LinkedHashMap<>();
        String rest = AprsParser.weatherValues("g...t-04h00b.....xyz", w);
        Map<String, Object> three = new LinkedHashMap<>();
        AprsParser.weatherValues("h074b10184", three);

        assertThat(w).containsEntry("tempC", -20.0).containsEntry("rh", 100).doesNotContainKeys("windGustMs",
                "pressureHpa");
        assertThat(rest).isEqualTo("xyz");
        assertThat(three).containsEntry("rh", 74).containsEntry("pressureHpa", 1018.4);
    }

    @Test
    @DisplayName("objects: name, live object position; a killed object is flagged")
    void objects() {
        AprsPacket live = parse("object");
        AprsPacket killed = parse("killed-object");

        assertThat(live.type()).isEqualTo(AprsPacket.Type.OBJECT);
        assertThat(live.objectName()).isEqualTo("K6SDR-10");
        assertThat(live.entityId()).isEqualTo("K6SDR-10");
        assertThat(live.killed()).isFalse();
        assertThat(live.latitude()).isCloseTo(37.966667, within(1e-6));
        assertThat(live.symbol()).isEqualTo("Wa");
        assertThat(live.comment()).isEqualTo("144.910MHz Winlink Packet Gateway");
        assertThat(killed.objectName()).isEqualTo("EVENT-1");
        assertThat(killed.killed()).isTrue();
    }

    @Test
    @DisplayName("items: a 3-9 character name ended by ! (live) or _ (killed)")
    void items() {
        AprsPacket live = parse("item");
        AprsPacket killed = parse("killed-item");

        assertThat(live.type()).isEqualTo(AprsPacket.Type.ITEM);
        assertThat(live.objectName()).isEqualTo("AID2");
        assertThat(live.latitude()).isCloseTo(49.058333, within(1e-6));
        assertThat(live.longitude()).isCloseTo(-72.029167, within(1e-6));
        assertThat(live.comment()).isEqualTo("first aid");
        assertThat(killed.killed()).isTrue();
    }

    @Test
    @DisplayName("status with a timestamp, a message with an id, and an acknowledgement")
    void statusAndMessages() {
        AprsPacket status = parse("status");
        AprsPacket message = parse("message");
        AprsPacket ack = parse("ack");

        assertThat(status.type()).isEqualTo(AprsPacket.Type.STATUS);
        assertThat(status.comment()).isEqualTo("Net tonight at 8pm");
        assertThat(message.type()).isEqualTo(AprsPacket.Type.MESSAGE);
        assertThat(message.addressee()).isEqualTo("N0CALL-2");
        assertThat(message.messageText()).isEqualTo("Hello there");
        assertThat(message.messageId()).isEqualTo("42");
        assertThat(message.ack()).isFalse();
        assertThat(ack.ack()).isTrue();
        assertThat(ack.messageId()).isEqualTo("42");
    }

    @Test
    @DisplayName("telemetry, a malformed position and a line without a header are unsupported, with a reason")
    void unsupported() {
        for (String label : new String[] {"telemetry", "malformed-position", "no-header"}) {
            AprsPacket p = parse(label);
            assertThat(p.type()).as(label).isEqualTo(AprsPacket.Type.UNSUPPORTED);
            assertThat(p.why()).as(label).isNotBlank();
            assertThat(p.hasPosition()).as(label).isFalse();
        }
        assertThat(AprsParser.parse(null).type()).isEqualTo(AprsPacket.Type.UNSUPPORTED);
        assertThat(AprsParser.parse("N0CALL>APRS:").type()).isEqualTo(AprsPacket.Type.UNSUPPORTED);
        assertThat(AprsParser.parse("N0CALL>APRS:`").type()).isEqualTo(AprsPacket.Type.UNSUPPORTED);
        assertThat(AprsParser.parse("N0 CALL>APRS:>x").type()).isEqualTo(AprsPacket.Type.UNSUPPORTED);
    }

    @Test
    @DisplayName("comments: control characters stripped, CR/LF terminator ignored, at most 256 characters")
    void comments() {
        AprsPacket p = AprsParser.parse("N0CALL>APRS:>bell\u0007 tab\there\r\n");
        String longText = "x".repeat(400);

        assertThat(p.comment()).isEqualTo("bell tabhere");
        assertThat(p.raw()).isEqualTo("N0CALL>APRS:>bell\u0007 tab\there");
        assertThat(AprsParser.parse("N0CALL>APRS:>" + longText).comment()).hasSize(AprsParser.MAX_COMMENT);
        assertThat(AprsParser.clean("  ")).isNull();
    }

    @Test
    @DisplayName("every line of the real APRS-IS capture parses, apart from telemetry, beacons and igate status")
    void realCapture() {
        int data = 0;
        int unsupported = 0;
        for (String line : AdapterFixtures.text("aprs-is", "stream-r-sf-bay.txt").split("\r\n")) {
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            data++;
            AprsPacket p = AprsParser.parse(line);
            if (p.type() == AprsPacket.Type.UNSUPPORTED) {
                unsupported++;
                assertThat(p.why()).as(line).startsWith("unsupported data type");
            }
        }
        assertThat(data).isEqualTo(181);
        assertThat(unsupported).isEqualTo(6);
    }
}
