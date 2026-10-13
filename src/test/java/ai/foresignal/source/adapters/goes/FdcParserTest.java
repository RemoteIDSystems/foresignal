package ai.foresignal.source.adapters.goes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.raster.decode.NetcdfDecoder;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SilentFormatChangeException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.raster.DecoderFixtures;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Point;

/**
 * {@link FdcParser} on the captured CONUS file (prompt 048): the mask set 10–13/30–33, pixel centers
 * through the fixed grid, the AOI filter, 2-significant-figure rounding and the record shape.
 */
class FdcParserTest {

    static final String FILE = GlmParserTest.FDC;
    static final double[] TEXAS = {-106.65, 25.84, -93.51, 36.50};
    static final Instant SCAN = Instant.parse("2026-10-12T21:56:17Z");

    @TempDir Path dir;

    NetcdfDecoder decoder;
    Path file;

    @BeforeEach
    void setUp() throws Exception {
        decoder = new NetcdfDecoder(DecoderFixtures.safe(dir));
        file = Files.write(dir.resolve(FILE), AdapterFixtures.bytes("fdc", FILE));
    }

    @Test
    @DisplayName("25 detections over CONUS, masks only from 10-13 and 30-33")
    void conus() {
        FdcParser.Result r = FdcParser.parse(decoder, file, GoesFdcAdapter.ID, null);

        assertThat(r.nx()).isEqualTo(2500);
        assertThat(r.ny()).isEqualTo(1500);
        assertThat(r.fireCandidates()).isEqualTo(25);
        assertThat(r.detections()).hasSize(25);
        Map<Integer, Long> masks = r.detections().stream()
                .collect(Collectors.groupingBy(FdcParser.Detection::maskCode, Collectors.counting()));
        assertThat(masks).containsOnlyKeys(10, 12, 30, 32).containsEntry(10, 2L).containsEntry(12, 1L)
                .containsEntry(30, 19L).containsEntry(32, 3L);
    }

    @Test
    @DisplayName("the mask set: 10-13 and 30-33 are detections, 14, 15, 34, 35 and others are not")
    void maskSet() {
        for (int m : new int[] {10, 11, 12, 13, 30, 31, 32, 33}) {
            assertThat(FdcParser.isDetection(m)).as("mask %d", m).isTrue();
        }
        for (int m : new int[] {0, 14, 15, 34, 35, 40, 50, 100, 150, 245}) {
            assertThat(FdcParser.isDetection(m)).as("mask %d", m).isFalse();
        }
        assertThat(FdcParser.confidence(10)).isEqualTo("processed");
        assertThat(FdcParser.confidence(31)).isEqualTo("saturated");
        assertThat(FdcParser.confidence(12)).isEqualTo("cloudy");
        assertThat(FdcParser.confidence(33)).isEqualTo("high");
        assertThat(FdcParser.temporallyFiltered(30)).isTrue();
        assertThat(FdcParser.temporallyFiltered(13)).isFalse();
        assertThatThrownBy(() -> FdcParser.confidence(14)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("pixel to lon/lat through the fixed grid; the Texas AOI keeps two pixels")
    void texas() {
        FdcParser.Result r = FdcParser.parse(decoder, file, GoesFdcAdapter.ID, TEXAS);

        assertThat(r.fireCandidates()).isEqualTo(25);
        assertThat(r.detections()).extracting(FdcParser.Detection::externalId).containsExactly("g19:608:594",
                "g19:927:692");
        FdcParser.Detection east = r.detections().get(1);
        assertThat(east.col()).isEqualTo(927);
        assertThat(east.row()).isEqualTo(692);
        assertThat(east.lon()).isCloseTo(-94.7773, within(1e-4));
        assertThat(east.lat()).isCloseTo(31.6045, within(1e-4));
        assertThat(east.maskCode()).isEqualTo(30);
    }

    @Test
    @DisplayName("2 significant figures and 10 K rounding")
    void rounding() {
        assertThat(FdcParser.sig2(159.7772)).isEqualTo(160.0);
        assertThat(FdcParser.sig2(4.593434)).isEqualTo(4.6);
        assertThat(FdcParser.sig2(271.034)).isEqualTo(270.0);
        assertThat(FdcParser.sig2(0.0)).isEqualTo(0.0);
        assertThat(FdcParser.sig2(Double.NaN)).isNull();
        assertThat(FdcParser.sig2(-9)).isNull();
        assertThat(FdcParser.round10(454.83)).isEqualTo(450L);
        assertThat(FdcParser.round10(1156.64)).isEqualTo(1160L);
        assertThat(FdcParser.round10(Double.NaN)).isNull();
        assertThat(FdcParser.formatMw(160.0)).isEqualTo("160");
        assertThat(FdcParser.formatMw(4.6)).isEqualTo("4.6");
    }

    @Test
    @DisplayName("the record: id, point, time base and the rounded payload")
    void record() {
        FdcParser.Detection west = FdcParser.parse(decoder, file, GoesFdcAdapter.ID, TEXAS).detections().get(0);
        Instant firstSeen = SCAN.minusSeconds(600);
        NormalizedRecord r = FdcParser.record(west, SCAN, firstSeen);

        assertThat(r.externalId()).isEqualTo("g19:608:594");
        assertThat(r.kind()).isEqualTo("fire-detection");
        assertThat(r.observedAt()).isEqualTo(SCAN);
        assertThat(r.eventTime()).isEqualTo(firstSeen);
        Point p = (Point) r.geometry();
        assertThat(p.getX()).isEqualTo(-103.8093);
        assertThat(p.getY()).isEqualTo(34.3155);
        assertThat(r.payload()).containsEntry("maskCode", 30).containsEntry("confidence", "processed")
                .containsEntry("temporallyFiltered", true).containsEntry("frpMw", 160.0).containsEntry("tempK", 450L)
                .containsEntry("areaKm2", 0.21).containsEntry("satellite", "G19").containsEntry("method", "FDC")
                .containsEntry("label", "Fire detection (GOES-19, processed), 160 MW");
    }

    @Test
    @DisplayName("a cloud-contaminated pixel without FRP has no frpMw, tempK or areaKm2")
    void missingValues() {
        FdcParser.Detection cloudy = FdcParser.parse(decoder, file, GoesFdcAdapter.ID, null).detections().stream()
                .filter(d -> d.externalId().equals("g19:640:394")).findFirst().orElseThrow();
        NormalizedRecord r = FdcParser.record(cloudy, SCAN, SCAN);

        assertThat(r.payload()).doesNotContainKeys("frpMw", "tempK", "areaKm2").containsEntry("confidence", "cloudy")
                .containsEntry("label", "Fire detection (GOES-19, cloudy)");
    }

    @Test
    @DisplayName("clusters over the 25 detections: 22, three of two pixels, one linked across a 2-pixel gap")
    void clusters() {
        List<FdcParser.Detection> all = FdcParser.parse(decoder, file, GoesFdcAdapter.ID, null).detections();
        List<List<FdcParser.Detection>> components = FireClusterer.components(all);

        assertThat(components).hasSize(22);
        assertThat(components.stream().filter(c -> c.size() == 2).map(FireClusterer::pixelsOf).toList())
                .containsExactlyInAnyOrder(java.util.Set.of("1149:603", "1149:604"), java.util.Set.of("1027:725", "1027:726"),
                        java.util.Set.of("882:1335", "885:1338"));
    }

    @Test
    @DisplayName("a file without Mask is a silent format change")
    void missingVariable() throws Exception {
        Path glm = Files.write(dir.resolve(GlmParserTest.ACTIVE), AdapterFixtures.bytes("glm", GlmParserTest.ACTIVE));
        assertThatThrownBy(() -> FdcParser.parse(decoder, glm, GoesFdcAdapter.ID, null))
                .isInstanceOf(SilentFormatChangeException.class).hasMessageContaining("variable Mask missing");
    }
}
