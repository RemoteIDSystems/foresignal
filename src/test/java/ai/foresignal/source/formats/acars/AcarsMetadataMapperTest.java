package ai.foresignal.source.formats.acars;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link AcarsMetadataMapper} (prompt 086): each decoder shape maps to the allowlisted metadata, no key named or
 * containing {@code text} survives and no message text appears anywhere in the record, and only an HFDL position report
 * gives a geometry.
 */
class AcarsMetadataMapperTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static JsonNode fixture(String name) {
        return JSON.readTree(AdapterFixtures.text("local-rf", name));
    }

    /** No key named or containing "text", and none of the message's text anywhere in the record. */
    static void assertNoText(NormalizedRecord r) {
        assertThat(r.payload().keySet()).allSatisfy(k -> {
            if (!k.equals("hasText")) {
                assertThat(k.toLowerCase(Locale.ROOT)).doesNotContain("text");
            }
        });
        assertThat(r.payload().keySet()).isSubsetOf(AcarsMetadataMapper.PAYLOAD_FIELDS);
        assertThat(r.payload().keySet()).doesNotContain("text", "msg_text", "msgText");
        assertThat(String.valueOf(r)).doesNotContain("SYNTHETIC").doesNotContain("MUST NEVER BE STORED");
        assertThat(JSON.writeValueAsString(r.payload())).doesNotContain("SYNTHETIC");
    }

    @Test
    @DisplayName("acarsdec: metadata, decoder time, the leading dot removed from the tail, no geometry, no text")
    void acarsdec() {
        NormalizedRecord r = AcarsMetadataMapper.map(fixture("acarsdec.json"), "home");
        assertThat(r.kind()).isEqualTo(AcarsMetadataMapper.KIND);
        assertThat(r.observedAt()).isEqualTo(Instant.parse("2026-10-13T14:00:00.123456Z"));
        assertThat(r.eventTime()).isEqualTo(r.observedAt());
        assertThat(r.externalId()).isEqualTo("acars|home|" + r.observedAt().toEpochMilli() + "|N123FS|M01A");
        assertThat(r.payload()).containsEntry("protocol", "acars").containsEntry("tail", "N123FS")
                .containsEntry("flight", "XX0123").containsEntry("label", "H1").containsEntry("mode", "2")
                .containsEntry("blockId", "5").containsEntry("msgNum", "M01A").containsEntry("freqMhz", 131.55)
                .containsEntry("signalDbm", -12.3).containsEntry("hasText", true)
                .doesNotContainKeys("icaoHex", "groundStation");
        assertThat(r.geometry()).isNull();
        assertThat(r.observers()).singleElement().satisfies(o -> {
            assertThat(o.receiverId()).isEqualTo("home");
            assertThat(o.frequencyHz()).isEqualTo(131_550_000L);
        });
        assertNoText(r);
    }

    @Test
    @DisplayName("dumpvdl2: aircraft ICAO address, ground station, Hz to MHz, no text")
    void dumpvdl2() {
        NormalizedRecord r = AcarsMetadataMapper.map(fixture("dumpvdl2.json"), null);
        assertThat(r.observedAt()).isEqualTo(Instant.parse("2026-10-13T14:00:10.250Z"));
        assertThat(r.payload()).containsEntry("protocol", "vdl2").containsEntry("tail", "N456FS")
                .containsEntry("flight", "YY0456").containsEntry("blockId", "7").containsEntry("msgNum", "D01")
                .containsEntry("freqMhz", 136.975).containsEntry("signalDbm", -21.5)
                .containsEntry("icaoHex", "A1B2C3").containsEntry("groundStation", "10916D")
                .containsEntry("hasText", true);
        assertThat(r.externalId()).startsWith("vdl2|FS-TEST|");
        assertThat(r.geometry()).isNull();
        assertNoText(r);
    }

    @Test
    @DisplayName("dumphfdl: the decoded position report is the geometry; ICAO from ac_info, ground station id")
    void dumphfdl() {
        NormalizedRecord r = AcarsMetadataMapper.map(fixture("dumphfdl.json"), "home");
        assertThat(r.payload()).containsEntry("protocol", "hfdl").containsEntry("tail", "G-FSTS")
                .containsEntry("freqMhz", 8.927).containsEntry("icaoHex", "ABCDEF").containsEntry("groundStation", "2")
                .containsEntry("hasText", true);
        assertThat(r.geometry()).isInstanceOf(Point.class);
        assertThat(((Point) r.geometry()).getY()).isEqualTo(51.234);
        assertThat(((Point) r.geometry()).getX()).isEqualTo(-30.567);
        assertNoText(r);
    }

    @Test
    @DisplayName("hasText is false without text; a message without time or of another shape maps to nothing")
    void edges() {
        JsonNode quiet = JSON.readTree("{\"timestamp\":1791900000,\"label\":\"_d\",\"tail\":\"..\",\"text\":\"\"}");
        NormalizedRecord r = AcarsMetadataMapper.map(quiet, null);
        assertThat(r.payload()).containsEntry("hasText", false).doesNotContainKey("tail");
        assertThat(r.externalId()).isEqualTo("acars||1791900000000||");
        assertThat(r.observers()).isEmpty();
        assertThat(AcarsMetadataMapper.map(JSON.readTree("{\"vdl2\":{\"freq\":1}}"), null)).isNull();
        assertThat(AcarsMetadataMapper.map(JSON.readTree("{\"other\":1}"), null)).isNull();
        assertThat(AcarsMetadataMapper.map(JSON.readTree("[1]"), null)).isNull();
        assertThat(AcarsMetadataMapper.map(null, null)).isNull();
        JsonNode badPos = JSON.readTree("{\"hfdl\":{\"t\":{\"sec\":1791900020},\"lpdu\":{\"hfnpdu\":{\"pos\":"
                + "{\"lat\":180.0,\"lon\":180.0}}}}}");
        assertThat(AcarsMetadataMapper.map(badPos, null).geometry()).isNull();
        assertThat(AcarsMetadataMapper.tail(".N1")).isEqualTo("N1");
    }

    @Test
    @DisplayName("the object stream splits newline-ended and back-to-back objects, across chunks, with braces in strings")
    void objectStream() {
        JsonObjectStream stream = new JsonObjectStream();
        String a = "{\"a\":\"x}{\\\"\"}\n";
        String b = "{\"b\":{\"c\":1}}{\"d\":2}";
        List<byte[]> first = stream.feed((a + b.substring(0, 5)).getBytes(StandardCharsets.UTF_8));
        List<byte[]> second = stream.feed(b.substring(5).getBytes(StandardCharsets.UTF_8));
        assertThat(first).hasSize(1);
        assertThat(new String(first.getFirst(), StandardCharsets.UTF_8)).isEqualTo(a.strip());
        assertThat(second).extracting(x -> new String(x, StandardCharsets.UTF_8))
                .containsExactly("{\"b\":{\"c\":1}}", "{\"d\":2}");
        byte[] huge = new byte[JsonObjectStream.MAX_OBJECT_BYTES + 10];
        java.util.Arrays.fill(huge, (byte) ' ');
        huge[0] = '{';
        huge[huge.length - 1] = '}';
        assertThat(stream.feed(huge)).isEmpty();
        assertThat(stream.discarded()).isEqualTo(1);
        assertThat(stream.feed(null)).isEmpty();
    }
}
