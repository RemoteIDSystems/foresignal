package ai.foresignal.source.adapters.satdump;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.raster.decode.GeosGrids;
import ai.foresignal.source.spi.FrameInput;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterDtype;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.PrecisionModel;
import tools.jackson.databind.json.JsonMapper;

/**
 * Geostationary xRIT frames (prompt 049, Weather-Imagery-Design §4.3 and §4.5 item 1): partial frames as
 * channels validate, complete after 120 s of quiet or when the next folder appears, a {@code product.cbor}
 * caught mid-rewrite retried on the next scan, the {@code geos} grid from {@code projection_cfg} for both
 * sweep axes, the visible-disc footprint against coastline points, and the FY-4 refusal.
 */
class XritFrameTest {

    static final Instant T = Instant.parse("2026-10-12T18:01:00Z");
    static final Instant FRAME = Instant.parse("2026-10-12T18:00:20Z");
    static final String ID_2 = "satdump-home-goes-19-full-disk-2";
    static final String ID_13 = "satdump-home-goes-19-full-disk-13";
    static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);

    @TempDir Path temp;

    SatDumpTestContext context(Path root) throws Exception {
        Path station = Files.createDirectories(root.resolve("satdump/home"));
        SatDumpTestContext ctx = new SatDumpTestContext(root, T);
        ctx.params.put(SatDumpDescriptor.INBOX_PATH, station.toRealPath());
        return ctx;
    }

    Path station(Path root) {
        return root.resolve("satdump/home");
    }

    @Test
    @DisplayName("partial on the first scan, complete after 120 s without changes, one record per channel")
    void partialThenQuiet() throws Exception {
        Path root = Files.createDirectories(temp.resolve("inbox"));
        SatDumpTestContext ctx = context(root);
        SatDumpFixtures.copy("xrit", station(root));
        SatDumpStation s = ctx.station();

        s.scanInboxOnce();
        assertThat(ctx.rasters.registered).extracting(ProductSpec::id).containsExactly(ID_2, ID_13);
        assertThat(ctx.rasters.commits).hasSize(2).allMatch(FrameInput::partial)
                .allMatch(f -> f.validTime().equals(FRAME));
        assertThat(ctx.drain()).isEmpty();

        ctx.clock.advance(Duration.ofSeconds(60));
        s.scanInboxOnce();
        assertThat(ctx.rasters.commits).hasSize(2);

        ctx.clock.advance(Duration.ofSeconds(60));
        s.scanInboxOnce();
        assertThat(ctx.rasters.commits).hasSize(4);
        assertThat(ctx.rasters.commits.subList(2, 4)).noneMatch(FrameInput::partial);
        List<NormalizedRecord> records = ctx.drain();
        assertThat(records).extracting(NormalizedRecord::externalId).containsExactly(ID_2, ID_13);

        ctx.clock.advance(Duration.ofSeconds(60));
        s.scanInboxOnce();
        assertThat(ctx.rasters.commits).hasSize(4);
        assertThat(ctx.drain()).isEmpty();
    }

    @Test
    @DisplayName("a newer timestamp folder for the same satellite and region completes the older one at once")
    void nextFolder() throws Exception {
        Path root = Files.createDirectories(temp.resolve("inbox"));
        SatDumpTestContext ctx = context(root);
        SatDumpFixtures.copy("xrit", station(root));
        SatDumpStation s = ctx.station();
        s.scanInboxOnce();

        Path next = station(root).resolve("IMAGES/GOES-19/Full Disk/2026-10-12_18-10-20");
        SatDumpFixtures.copy(SatDumpFixtures.XRIT_FOLDER, next);
        ctx.clock.advance(Duration.ofSeconds(30));
        s.scanInboxOnce();
        List<NormalizedRecord> records = ctx.drain();
        assertThat(records).extracting(NormalizedRecord::externalId).containsExactly(ID_2, ID_13);
        assertThat(records).allMatch(r -> r.observedAt().equals(FRAME));
        // The newer folder's channels are partial; the cadence is now the observed 10 minutes.
        assertThat(ctx.rasters.commits).filteredOn(f -> f.validTime().equals(Instant.parse("2026-10-12T18:10:20Z")))
                .hasSize(2).allMatch(FrameInput::partial);
        assertThat(ctx.rasters.registered).last().satisfies(spec ->
                assertThat(spec.temporal().cadence()).isEqualTo(Duration.ofMinutes(10)));
    }

    @Test
    @DisplayName("product.cbor caught mid-rewrite is retried on the next scan")
    void cborRetry() throws Exception {
        Path root = Files.createDirectories(temp.resolve("inbox"));
        SatDumpTestContext ctx = context(root);
        Path folder = station(root).resolve("IMAGES/GOES-19/Full Disk/2026-10-12_18-00-20");
        SatDumpFixtures.copy(SatDumpFixtures.XRIT_FOLDER, folder);
        Files.write(folder.resolve("product.cbor"), SatDumpFixtures.bytes("xrit-truncated/product.cbor"));
        SatDumpStation s = ctx.station();

        s.scanInboxOnce();
        assertThat(ctx.rasters.commits).isEmpty();

        Files.write(folder.resolve("product.cbor"), SatDumpFixtures.bytes(SatDumpFixtures.XRIT_FOLDER + "/product.cbor"));
        ctx.clock.advance(Duration.ofSeconds(30));
        s.scanInboxOnce();
        assertThat(ctx.rasters.commits).hasSize(2).allMatch(FrameInput::partial);
    }

    @Test
    @DisplayName("FY-4 is refused with the health reason, and nothing is committed")
    void fy4() throws Exception {
        Path root = Files.createDirectories(temp.resolve("inbox"));
        SatDumpTestContext ctx = context(root);
        SatDumpFixtures.copy("xrit-fy4", station(root));
        SatDumpStation s = ctx.station();
        s.scanInboxOnce();
        assertThat(ctx.rasters.commits).isEmpty();
        assertThat(ctx.rasters.registered).isEmpty();
        assertThat(ctx.state.get("health.degraded")).contains(XritGeometry.FY4_MSG_REASON);
        assertThatThrownBy(() -> XritGeometry.grid("MSG-4", "12", null, 10, 10))
                .hasMessage(XritGeometry.FY4_MSG_REASON);
        assertThatThrownBy(() -> XritGeometry.grid("GOES-19", "2", null, 10, 10))
                .hasMessageContaining("no geos projection_cfg");
    }

    @Test
    @DisplayName("projection_cfg → geos grid: scan angles from scalar/offset over altitude, scaled to the image")
    void gridFromProjection() throws Exception {
        var projection = SatDumpFiles.product(SatDumpFixtures.bytes(SatDumpFixtures.XRIT_FOLDER + "/product.cbor"))
                .projection();
        GridDefinition grid = XritGeometry.grid("GOES-19", "13", projection, 64, 64);
        assertThat(grid.type()).isEqualTo(GridDefinition.GEOS);
        assertThat(grid.params()).containsEntry("lon0", -75.2).containsEntry("sweep", "x")
                .containsEntry("h", 35_786_023.0);
        double dx = ((Number) grid.params().get("dx")).doubleValue();
        double x0 = ((Number) grid.params().get("x0")).doubleValue();
        assertThat(dx).isCloseTo(4008.0 * 2712 / 64 / 35_786_023.0, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(x0).isCloseTo(-1356 * 4008.0 / 35_786_023.0, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(((Number) grid.params().get("dy")).doubleValue()).isNegative();
        assertThat(grid.west()).isLessThan(-140);
        assertThat(grid.east()).isGreaterThan(-10);

        // The Meteosat axis order (Elektro-L, sweep_x false) gives a different, still valid grid.
        String elektro = "{\"type\":\"geos\",\"lon0\":76.0,\"sweep_x\":false,\"scalar_x\":4008.0,"
                + "\"scalar_y\":-4008.0,\"offset_x\":-5434848.0,\"offset_y\":5434848.0,\"width\":2712,"
                + "\"height\":2712,\"altitude\":35786023.0}";
        GridDefinition y = XritGeometry.grid("ELEKTRO-L 4", "1", JsonMapper.builder().build().readTree(elektro), 128,
                128);
        assertThat(y.params()).containsEntry("sweep", "y").containsEntry("lon0", 76.0);
        assertThat(y.params().get("dx")).isNotEqualTo(grid.params().get("dx"));
    }

    @Test
    @DisplayName("the footprint is the disc visible from lon0, checked against coastline points")
    void coastline() throws Exception {
        MultiPolygon goes19 = GeosGrids.visibleDisc(-75.2, 35_786_023.0);
        MultiPolygon goes18 = GeosGrids.visibleDisc(-137.0, 35_786_023.0);
        MultiPolygon elektro = GeosGrids.visibleDisc(76.0, 35_786_023.0);
        assertThat(goes19.getNumGeometries()).isEqualTo(1);
        assertThat(goes18.getNumGeometries()).isEqualTo(2);
        List<String> lines = Files.readAllLines(SatDumpFixtures.root().getParent().resolve("coastline-points.csv"));
        int checked = 0;
        for (String line : lines) {
            if (line.startsWith("#") || line.isBlank()) {
                continue;
            }
            String[] f = line.split(",");
            var point = GEOMETRY.createPoint(new Coordinate(Double.parseDouble(f[1]), Double.parseDouble(f[2])));
            assertThat(goes19.contains(point)).as(f[0] + " from GOES-19").isEqualTo(Boolean.parseBoolean(f[3]));
            assertThat(goes18.contains(point)).as(f[0] + " from GOES-18").isEqualTo(Boolean.parseBoolean(f[4]));
            assertThat(elektro.contains(point)).as(f[0] + " from Elektro-L 4").isEqualTo(Boolean.parseBoolean(f[5]));
            assertThat(GeosGrids.visible(-75.2, 35_786_023.0, point.getX(), point.getY()))
                    .isEqualTo(Boolean.parseBoolean(f[3]));
            checked++;
        }
        assertThat(checked).isEqualTo(12);
        assertThat(ctxFootprint()).isEqualTo(goes19);
    }

    private MultiPolygon ctxFootprint() throws Exception {
        var projection = SatDumpFiles.product(SatDumpFixtures.bytes(SatDumpFixtures.XRIT_FOLDER + "/product.cbor"))
                .projection();
        return XritGeometry.footprint(XritGeometry.grid("GOES-19", "13", projection, 64, 64));
    }

    @Test
    @DisplayName("product ids, dtypes and specs")
    void products() throws Exception {
        assertThat(XritProducts.productId("home", "goes-19", null, "13")).isEqualTo("satdump-home-goes-19-fd-13");
        assertThat(XritProducts.productId("home", "goes-19", "Full Disk", "13"))
                .isEqualTo("satdump-home-goes-19-full-disk-13");
        String longId = XritProducts.productId("a-very-long-station-name-for-testing", "elektro-l4", "Full Disk Region",
                "channel-10-8um");
        assertThat(longId).hasSizeLessThanOrEqualTo(64).matches("^[a-z0-9][a-z0-9-]{0,63}$");

        ProductSpec gray = XritProducts.spec(ID_13, "t", RasterDtype.U8, 8, Duration.ofMinutes(15), "GOES-19");
        assertThat(gray.defaultStyle()).isEqualTo("ir-gray@1");
        assertThat(gray.physical(255)).isEqualTo(180.0);
        assertThat(gray.physical(0)).isEqualTo(330.0);
        assertThat(gray.nodata()).isEqualTo(0.0);
        assertThat(gray.temporal().maxGap()).isEqualTo(Duration.ofMinutes(45));
        assertThat(gray.spatial().coverage()).isEqualTo("station-disc");
        assertThat(gray.retention().maxAge()).isEqualTo(Duration.ofHours(72));
        assertThat(gray.category()).isEqualTo("imagery");
        ProductSpec color = XritProducts.spec("x", "t", RasterDtype.RGBA8, null, null, "GOES-19");
        assertThat(color.defaultStyle()).isEqualTo("rgb@1");
        assertThat(color.temporal().cadence()).isEqualTo(XritProducts.DEFAULT_CADENCE);
        assertThat(XritProducts.medianInterval(List.of(0L, 600L, 1200L, 2400L))).isEqualTo(Duration.ofMinutes(10));
        assertThat(XritProducts.medianInterval(List.of(5L))).isNull();

        var image = new java.awt.image.BufferedImage(4, 2, java.awt.image.BufferedImage.TYPE_USHORT_GRAY);
        image.getRaster().setSample(1, 0, 0, 65_535);
        assertThat(XritProducts.dtype(image)).isEqualTo(RasterDtype.U16);
        assertThat(XritProducts.bands(image, RasterDtype.U16, 10).codes().get(0)[1]).isEqualTo(1023);
        var rgb = new java.awt.image.BufferedImage(2, 1, java.awt.image.BufferedImage.TYPE_INT_RGB);
        rgb.setRGB(1, 0, 0x102030);
        assertThat(XritProducts.dtype(rgb)).isEqualTo(RasterDtype.RGBA8);
        int[] packed = XritProducts.bands(rgb, RasterDtype.RGBA8, null).codes().get(0);
        assertThat(packed[0]).isZero();
        assertThat(packed[1]).isEqualTo(0x102030FF);
    }

    @Test
    @DisplayName("folder paths: satellite, optional region and UTC time")
    void paths() {
        var withRegion = XritFrames.parse("IMAGES/GOES-19/Full Disk/2026-10-10_14-30-00");
        assertThat(withRegion.satellite()).isEqualTo("GOES-19");
        assertThat(withRegion.region()).isEqualTo("Full Disk");
        assertThat(withRegion.time()).isEqualTo(Instant.parse("2026-10-10T14:30:00Z"));
        var noRegion = XritFrames.parse("out/IMAGES/ELEKTRO-L 4/2026-10-10_14-30-00");
        assertThat(noRegion.region()).isNull();
        assertThat(XritFrames.parse("IMAGES/GOES-19/not-a-time")).isNull();
        assertThat(XritFrames.parse("pass/2026-10-10_14-30-00")).isNull();
    }
}
