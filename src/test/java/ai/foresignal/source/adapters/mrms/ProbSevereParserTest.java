package ai.foresignal.source.adapters.mrms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.AreaOfInterest;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The ProbSevere v3 parser against the file captured 2026-10-13 ({@code MRMS_PROBSEVERE_20261012_235841.json},
 * 86 objects, every property a string).
 */
class ProbSevereParserTest {

    static final String FILE = "MRMS_PROBSEVERE_20261012_235841.json";
    static final double[] TEXAS = {-106.65, 25.84, -93.51, 36.50};
    static final double[] WORLD = {-180, -90, 180, 90};

    static byte[] fixture() {
        return AdapterFixtures.bytes("probsevere", FILE);
    }

    @Test
    @DisplayName("the header validTime is observedAt; eventTime is null")
    void timeBase() {
        ProbSevereParser.ParseResult result = ProbSevereParser.parse(fixture(), WORLD);
        assertThat(result.validTime()).isEqualTo(Instant.parse("2026-10-12T23:58:41Z"));
        assertThat(result.records()).hasSize(86).allSatisfy(r -> {
            assertThat(r.observedAt()).isEqualTo(result.validTime());
            assertThat(r.eventTime()).isNull();
            assertThat(r.kind()).isEqualTo("probsevere-object");
            assertThat(r.geometry().getGeometryType()).isEqualTo("Polygon");
        });
    }

    @Test
    @DisplayName("strings become numbers, quantized; automated and notAWarning are always set")
    void stringsToNumbers() {
        NormalizedRecord r = ProbSevereParser.parse(fixture(), WORLD).records().stream()
                .filter(x -> x.externalId().equals("981738")).findFirst().orElseThrow();
        assertThat(r.payload())
                .containsEntry("probSevere", 0L)
                .containsEntry("probTor", 0L)
                .containsEntry("probHail", 0L)
                .containsEntry("probWind", 0L)
                .containsEntry("meshIn", 0.0)
                .containsEntry("compRefDbz", 50L)
                .containsEntry("vil", 6.0)
                .containsEntry("echoTop50Kft", 0.0)
                .containsEntry("flashRate", 0.0)
                .containsEntry("muCape", 1962.0)
                .containsEntry("mlCape", 1256.0)
                .containsEntry("ebShearKt", 33.0)
                .containsEntry("srh01km", 32.0)
                .containsEntry("label", "ProbSevere 0% (tor 0%)")
                .containsEntry("automated", true)
                .containsEntry("notAWarning", true);
        // MOTION_EAST 5.3, MOTION_SOUTH -4 m/s: 23.9 km/h toward 053° → 25 km/h, 50°.
        assertThat(r.payload().get("motion")).isEqualTo(Map.of("speedKmh", 25L, "bearingDeg", 50L));
    }

    @Test
    @DisplayName("motion: speed to 5 km/h, bearing toward to 10°, no bearing when still")
    void motion() {
        assertThat(ProbSevereParser.motion(0.0, -10.0)).isEqualTo(Map.of("speedKmh", 35L, "bearingDeg", 0L));
        assertThat(ProbSevereParser.motion(10.0, 0.0)).isEqualTo(Map.of("speedKmh", 35L, "bearingDeg", 90L));
        assertThat(ProbSevereParser.motion(0.0, 10.0)).isEqualTo(Map.of("speedKmh", 35L, "bearingDeg", 180L));
        assertThat(ProbSevereParser.motion(-10.0, 0.0)).isEqualTo(Map.of("speedKmh", 35L, "bearingDeg", 270L));
        assertThat(ProbSevereParser.motion(-0.1, -10.0).get("bearingDeg")).isEqualTo(0L);
        assertThat(ProbSevereParser.motion(0.2, 0.1)).isEqualTo(Map.of("speedKmh", 0L));
        assertThat(ProbSevereParser.motion(null, 1.0)).isNull();
    }

    @Test
    @DisplayName("meshIn rounds to 0.25 in and the label carries the tornado probability")
    void quantization() {
        String json = """
                {"type":"FeatureCollection","validTime":"20261012_235841 UTC","features":[
                 {"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[-97,31],[-96.9,31],[-96.9,31.1],[-97,31]]]},
                  "properties":{"ID":"7","ProbSevere":"44","ProbTor":"26","ProbHail":"31.6","ProbWind":"x","MESH":"1.13",
                   "COMPREF":"57.6","MOTION_EAST":"","MOTION_SOUTH":"3"}}]}""";
        NormalizedRecord r = ProbSevereParser.parse(json.getBytes(StandardCharsets.UTF_8), TEXAS).records().get(0);
        assertThat(r.payload()).containsEntry("meshIn", 1.25).containsEntry("compRefDbz", 58L)
                .containsEntry("probHail", 32L).containsEntry("label", "ProbSevere 44% (tor 26%)")
                .doesNotContainKeys("probWind", "motion");
    }

    @Test
    @DisplayName("only objects whose polygon touches the AOI envelope are kept")
    void aoiFilter() {
        ProbSevereParser.ParseResult texas = ProbSevereParser.parse(fixture(), TEXAS);
        assertThat(texas.diagnostics().kept()).isEqualTo(10);
        assertThat(texas.diagnostics().outsideAoi()).isEqualTo(76);
        assertThat(texas.records()).allSatisfy(r -> {
            var e = r.geometry().getEnvelopeInternal();
            assertThat(AreaOfInterest.intersects(TEXAS, e.getMinX(), e.getMinY(), e.getMaxX(), e.getMaxY())).isTrue();
        });
        assertThat(ProbSevereParser.parse(fixture(), new double[] {10, 10, 11, 11}).records()).isEmpty();
    }

    @Test
    @DisplayName("every payload key is declared (011 contract helper)")
    void fieldSpecs() {
        FieldSpecContract.assertPayloadKeysDeclared(new ProbSevereAdapter((AreaOfInterest) null, "").descriptor(),
                ProbSevereParser.parse(fixture(), WORLD).records());
    }

    @Test
    @DisplayName("validTime parsing and format errors")
    void errors() {
        assertThat(ProbSevereParser.validTime("20261010_000041 UTC")).isEqualTo(Instant.parse("2026-10-10T00:00:41Z"));
        assertThat(ProbSevereParser.validTime("yesterday")).isNull();
        assertThatThrownBy(() -> ProbSevereParser.parse("{\"type\":\"FeatureCollection\",\"features\":[]}"
                .getBytes(StandardCharsets.UTF_8), TEXAS)).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("validTime");
        assertThatThrownBy(() -> ProbSevereParser.parse("[".getBytes(StandardCharsets.UTF_8), TEXAS))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("a new scan where only the environmental fields drift hashes equal (volatile fields)")
    void volatileDrift() {
        String template = """
                {"type":"FeatureCollection","validTime":"%s","features":[
                 {"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[-97,31],[-96.9,31],[-96.9,31.1],[-97,31]]]},
                  "properties":{"ID":"7","ProbSevere":"44","ProbTor":"26","ProbHail":"31","ProbWind":"12","MESH":"1.13",
                   "COMPREF":"57.6","VIL":"40","EchoTop_50":"35","MOTION_EAST":"10","MOTION_SOUTH":"-5",
                   "MUCAPE":"%s","MLCAPE":"%s","EBSHEAR":"%s","SRH01KM":"%s","FLASH_RATE":"%s"}}]}""";
        NormalizedRecord a = ProbSevereParser.parse(String.format(template, "20261012_235841 UTC", "2810", "2140", "45",
                "210", "41").getBytes(StandardCharsets.UTF_8), TEXAS).records().get(0);
        NormalizedRecord b = ProbSevereParser.parse(String.format(template, "20261013_000041 UTC", "2790", "2101", "47",
                "198", "55").getBytes(StandardCharsets.UTF_8), TEXAS).records().get(0);
        ai.foresignal.testsupport.ingest.SnapshotContract.assertStableStaticHash(
                new ProbSevereAdapter((AreaOfInterest) null, "").descriptor(), java.util.List.of(a, b));
    }
}
