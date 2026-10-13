package ai.foresignal.source.adapters.aisstream;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.adapters.aisstream.AisStreamMessageParser.Confirmation;
import ai.foresignal.source.adapters.aisstream.AisStreamMessageParser.ErrorEnvelope;
import ai.foresignal.source.adapters.aisstream.AisStreamMessageParser.Ignored;
import ai.foresignal.source.adapters.aisstream.AisStreamMessageParser.Malformed;
import ai.foresignal.source.adapters.aisstream.AisStreamMessageParser.Result;
import ai.foresignal.source.adapters.aisstream.AisStreamMessageParser.Unrecognized;
import ai.foresignal.source.adapters.aisstream.AisStreamMessageParser.Untimed;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link AisStreamMessageParser} against the fixtures in {@code fixtures/aisstream/} (prompt 063).
 */
class AisStreamMessageParserTest {

    private static List<String> lines(String name) {
        return AdapterFixtures.text(AisStreamAdapter.ID, name).lines().filter(l -> !l.isBlank()).toList();
    }

    private static List<Result> parseAll(String name) {
        return lines(name).stream().map(AisStreamMessageParser::parse).toList();
    }

    private static AisMessage message(String name, int index) {
        Result result = parseAll(name).get(index);
        assertThat(result).isInstanceOf(AisMessage.class);
        return (AisMessage) result;
    }

    @Nested
    @DisplayName("each message type of the capture")
    class Types {

        @Test
        @DisplayName("the confirmation carries CompressionEnabled and is not liveness")
        void confirmation() {
            Result result = parseAll("frames.jsonl").get(0);

            assertThat(result).isEqualTo(new Confirmation(true));
            assertThat(result.liveness()).isFalse();
            assertThat(AisStreamMessageParser.parse("{\"MessageType\":\"SubscriptionConfirmation\",\"Message\":{}}"))
                    .isEqualTo(new Confirmation(null));
        }

        @Test
        @DisplayName("a Class A PositionReport")
        void positionReport() {
            AisMessage m = message("frames.jsonl", 1);

            assertThat(m.messageType()).isEqualTo("PositionReport");
            assertThat(m.mmsi()).isEqualTo("235009802");
            assertThat(m.aisClass()).isEqualTo("A");
            assertThat(m.position()).isTrue();
            assertThat(m.latitude()).isEqualTo(50.8921);
            assertThat(m.longitude()).isEqualTo(-1.3952);
            assertThat(m.sog()).isEqualTo(12.4);
            assertThat(m.cog()).isEqualTo(135.2);
            assertThat(m.heading()).isEqualTo(136);
            assertThat(m.navStatus()).isZero();
            assertThat(m.hasStatic()).isFalse();
            assertThat(m.liveness()).isTrue();
        }

        @Test
        @DisplayName("ShipStaticData, trimmed of spaces and @ padding")
        void shipStaticData() {
            AisMessage m = message("frames.jsonl", 2);

            assertThat(m.position()).isFalse();
            assertThat(m.hasPosition()).isFalse();
            assertThat(m.aisClass()).isEqualTo("A");
            assertThat(m.name()).isEqualTo("RED FALCON");
            assertThat(m.callsign()).isEqualTo("MFZL6");
            assertThat(m.imo()).isEqualTo(9064748);
            assertThat(m.shipType()).isEqualTo(60);
            assertThat(m.dimension().lengthM()).isEqualTo(150);
            assertThat(m.dimension().beamM()).isEqualTo(25);
            assertThat(m.eta()).isEqualTo(new AisMessage.Eta(10, 13, 18, 30));
            assertThat(m.draught()).isEqualTo(4.9);
            assertThat(m.destination()).isEqualTo("EAST COWES");
        }

        @Test
        @DisplayName("a StandardClassBPositionReport has no navigational status")
        void classB() {
            AisMessage m = message("frames.jsonl", 3);

            assertThat(m.aisClass()).isEqualTo("B");
            assertThat(m.latitude()).isEqualTo(50.765);
            assertThat(m.heading()).as("511 is not available").isNull();
            assertThat(m.navStatus()).isNull();
        }

        @Test
        @DisplayName("StaticDataReport part A carries the name, part B type, call sign and dimensions")
        void staticDataReport() {
            AisMessage a = message("frames.jsonl", 4);
            AisMessage b = message("frames.jsonl", 5);

            assertThat(a.name()).isEqualTo("SEA BREEZE");
            assertThat(a.callsign()).isNull();
            assertThat(a.shipType()).isNull();
            assertThat(b.name()).isNull();
            assertThat(b.callsign()).isEqualTo("2ABC3");
            assertThat(b.shipType()).isEqualTo(37);
            assertThat(b.dimension().lengthM()).isEqualTo(12);
            assertThat(b.dimension().beamM()).isEqualTo(4);
            assertThat(a.aisClass()).isEqualTo("B");
        }

        @Test
        @DisplayName("an ExtendedClassBPositionReport carries a position and the static name, type and dimensions")
        void extendedClassB() {
            AisMessage m = message("frames.jsonl", 6);

            assertThat(m.position()).isTrue();
            assertThat(m.hasPosition()).isTrue();
            assertThat(m.name()).isEqualTo("SOLENT WORKER");
            assertThat(m.shipType()).isEqualTo(52);
            assertThat(m.dimension().lengthM()).isEqualTo(25);
        }

        @Test
        @DisplayName("a recognized type the merge does not use is Ignored, but still liveness")
        void ignored() {
            Result result = parseAll("frames.jsonl").get(8);

            assertThat(result).isEqualTo(new Ignored("BaseStationReport", "002320001"));
            assertThat(result.liveness()).isTrue();
        }

        @Test
        @DisplayName("every line of the capture parses to something other than Malformed")
        void wholeCapture() {
            assertThat(parseAll("frames.jsonl")).hasSize(10).noneMatch(r -> r instanceof Malformed);
        }
    }

    @Nested
    @DisplayName("metadata and time")
    class MetaDataAndTime {

        @Test
        @DisplayName("MetaData keys are read case-insensitively (time_utc and TimeUtc)")
        void caseInsensitive() {
            AisMessage lower = message("frames.jsonl", 1);
            AisMessage upper = message("frames.jsonl", 3);

            assertThat(lower.time()).isEqualTo(Instant.parse("2026-10-13T12:00:01.509865357Z"));
            assertThat(upper.time()).isEqualTo(Instant.parse("2026-10-13T12:00:04.250Z"));
        }

        @Test
        @DisplayName("the metadata position is used when the body has none")
        void metadataPosition() {
            String json = "{\"MessageType\":\"PositionReport\",\"MetaData\":{\"mmsi\":235000009,\"Latitude\":50.1,"
                    + "\"LONGITUDE\":-1.1,\"time_utc\":\"2026-10-13 12:00:00 +0000 UTC\"},"
                    + "\"Message\":{\"PositionReport\":{\"UserID\":235000009,\"Sog\":1.0}}}";

            AisMessage m = (AisMessage) AisStreamMessageParser.parse(json);

            assertThat(m.latitude()).isEqualTo(50.1);
            assertThat(m.longitude()).isEqualTo(-1.1);
            assertThat(m.mmsi()).isEqualTo("235000009");
        }

        @Test
        @DisplayName("time_utc is parsed to the nanosecond; ISO-8601 is accepted; garbage is null")
        void timeUtc() {
            assertThat(AisStreamMessageParser.parseTime("2023-05-10 11:46:52.509865357 +0000 UTC"))
                    .isEqualTo(Instant.parse("2023-05-10T11:46:52.509865357Z"));
            assertThat(AisStreamMessageParser.parseTime("2023-05-10 11:46:52 +0000 UTC"))
                    .isEqualTo(Instant.parse("2023-05-10T11:46:52Z"));
            assertThat(AisStreamMessageParser.parseTime("2023-05-10 13:46:52.5 +0200"))
                    .isEqualTo(Instant.parse("2023-05-10T11:46:52.500Z"));
            assertThat(AisStreamMessageParser.parseTime("2023-05-10T11:46:52.1Z"))
                    .isEqualTo(Instant.parse("2023-05-10T11:46:52.100Z"));
            assertThat(AisStreamMessageParser.parseTime("yesterday")).isNull();
            assertThat(AisStreamMessageParser.parseTime("  ")).isNull();
            assertThat(AisStreamMessageParser.parseTime(null)).isNull();
        }

        @Test
        @DisplayName("a recognized message without time_utc is Untimed: liveness, never stamped")
        void untimed() {
            Result result = AisStreamMessageParser.parse(AdapterFixtures.text(AisStreamAdapter.ID, "untimed.json"));

            assertThat(result).isEqualTo(new Untimed("PositionReport", "235400001"));
            assertThat(result.liveness()).isTrue();
        }

        @Test
        @DisplayName("the MMSI comes from MetaData, else UserID, zero-padded to nine digits")
        void mmsi() {
            String json = "{\"MessageType\":\"PositionReport\",\"MetaData\":{\"time_utc\":\"2026-10-13 12:00:00 +0000 UTC\"},"
                    + "\"Message\":{\"PositionReport\":{\"UserID\":2320001,\"Latitude\":50,\"Longitude\":-1}}}";

            assertThat(((AisMessage) AisStreamMessageParser.parse(json)).mmsi()).isEqualTo("002320001");
        }
    }

    @Nested
    @DisplayName("sentinels (adapter rule 11)")
    class Sentinels {

        @Test
        @DisplayName("lat 91 / lon 181, SOG 102.3, COG 360, heading 511 and status 15 are all omitted")
        void allSentinels() {
            AisMessage m = message("sentinels.jsonl", 0);

            assertThat(m.position()).isTrue();
            assertThat(m.hasPosition()).isFalse();
            assertThat(m.latitude()).isNull();
            assertThat(m.longitude()).isNull();
            assertThat(m.sog()).isNull();
            assertThat(m.cog()).isNull();
            assertThat(m.heading()).isNull();
            assertThat(m.navStatus()).isNull();
        }

        @Test
        @DisplayName("SOG above 102.3, COG above 360 and heading above 359 are omitted; the position is kept")
        void aboveSentinels() {
            AisMessage m = message("sentinels.jsonl", 1);

            assertThat(m.hasPosition()).isTrue();
            assertThat(m.sog()).isNull();
            assertThat(m.cog()).isNull();
            assertThat(m.heading()).isNull();
        }

        @Test
        @DisplayName("a latitude out of range is no position; valid kinematics at the edges are kept")
        void outOfRange() {
            AisMessage m = message("sentinels.jsonl", 2);

            assertThat(m.hasPosition()).isFalse();
            assertThat(m.sog()).isEqualTo(3.2);
            assertThat(m.cog()).isEqualTo(359.9);
            assertThat(m.heading()).isEqualTo(359);
            assertThat(m.navStatus()).isEqualTo(14);
        }

        @Test
        @DisplayName("IMO 0, type 0, zero dimensions, ETA not available, draught 0 and padding-only strings are omitted")
        void staticSentinels() {
            AisMessage m = message("sentinels.jsonl", 3);

            assertThat(m.name()).isNull();
            assertThat(m.callsign()).isNull();
            assertThat(m.imo()).isNull();
            assertThat(m.shipType()).isNull();
            assertThat(m.dimension()).isNull();
            assertThat(m.eta()).isNull();
            assertThat(m.draught()).isNull();
            assertThat(m.destination()).isNull();
            assertThat(m.hasStatic()).isFalse();
        }

        @Test
        @DisplayName("@ and spaces are trimmed at both ends; a half-zero dimension keeps the other half")
        void trimming() {
            AisMessage m = message("sentinels.jsonl", 4);

            assertThat(m.name()).isEqualTo("PADDED NAME");
            assertThat(m.callsign()).isEqualTo("CS1");
            assertThat(m.destination()).isEqualTo("SOUTHAMPTON");
            assertThat(m.dimension().lengthM()).isNull();
            assertThat(m.dimension().beamM()).isEqualTo(10);
            assertThat(m.draught()).isNull();
        }

        @Test
        @DisplayName("the scrub helpers at their boundaries")
        void helpers() {
            assertThat(AisStreamMessageParser.sog(102.2)).isEqualTo(102.2);
            assertThat(AisStreamMessageParser.sog(102.3)).isNull();
            assertThat(AisStreamMessageParser.sog(-0.1)).isNull();
            assertThat(AisStreamMessageParser.cog(0.0)).isZero();
            assertThat(AisStreamMessageParser.cog(360.0)).isNull();
            assertThat(AisStreamMessageParser.heading(0.0)).isZero();
            assertThat(AisStreamMessageParser.heading(511.0)).isNull();
            assertThat(AisStreamMessageParser.heading(360.0)).isNull();
            assertThat(AisStreamMessageParser.navStatus(14.0)).isEqualTo(14);
            assertThat(AisStreamMessageParser.navStatus(15.0)).isNull();
        }
    }

    @Nested
    @DisplayName("what is not AIS traffic")
    class NotTraffic {

        @Test
        @DisplayName("malformed JSON, an array and an empty text are Malformed")
        void malformed() {
            assertThat(AisStreamMessageParser.parse("{not json")).isInstanceOf(Malformed.class);
            assertThat(AisStreamMessageParser.parse("[1,2]")).isInstanceOf(Malformed.class);
            assertThat(AisStreamMessageParser.parse("")).isInstanceOf(Malformed.class);
            assertThat(AisStreamMessageParser.parse(null)).isInstanceOf(Malformed.class);
        }

        @Test
        @DisplayName("an unknown MessageType, or no body under it, is unknown_type; no MMSI is no_mmsi")
        void unrecognized() {
            assertThat(AisStreamMessageParser.parse("{\"MessageType\":\"Teleport\",\"Message\":{\"Teleport\":{}}}"))
                    .isEqualTo(new Unrecognized("unknown_type", "Teleport"));
            assertThat(AisStreamMessageParser.parse("{\"MessageType\":\"PositionReport\",\"Message\":{}}"))
                    .isEqualTo(new Unrecognized("unknown_type", "PositionReport"));
            assertThat(AisStreamMessageParser.parse("{\"MMSI\":235000001}"))
                    .isEqualTo(new Unrecognized("unknown_type", null));
            Result noMmsi = AisStreamMessageParser.parse(
                    "{\"MessageType\":\"PositionReport\",\"MetaData\":{},\"Message\":{\"PositionReport\":{\"Sog\":1}}}");
            assertThat(noMmsi).isEqualTo(new Unrecognized("no_mmsi", "PositionReport"));
            assertThat(noMmsi.liveness()).isFalse();
        }

        @Test
        @DisplayName("an error envelope naming the key is auth; any other is not")
        void errorEnvelope() {
            Result auth = AisStreamMessageParser.parse(AdapterFixtures.text(AisStreamAdapter.ID, "error-auth.json"));

            assertThat(auth).isEqualTo(new ErrorEnvelope("Api Key Is Not Valid", true));
            assertThat(AisStreamMessageParser.parse("{\"error\":\"Unauthorized\"}"))
                    .isEqualTo(new ErrorEnvelope("Unauthorized", true));
            assertThat(AisStreamMessageParser.parse("{\"error\":\"missing api key\"}"))
                    .isEqualTo(new ErrorEnvelope("missing api key", true));
            assertThat(AisStreamMessageParser.parse("{\"error\":\"Too many connections\"}"))
                    .isEqualTo(new ErrorEnvelope("Too many connections", false));
            assertThat(auth.liveness()).isFalse();
        }
    }
}
