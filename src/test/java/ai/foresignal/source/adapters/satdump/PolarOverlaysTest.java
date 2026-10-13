package ai.foresignal.source.adapters.satdump;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.raster.geotiff.GeoReference;
import ai.foresignal.source.InboxFiles;
import ai.foresignal.source.spi.FrameInput;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterDtype;
import ai.foresignal.source.spi.RemoteMediaReceiver;
import ai.foresignal.testsupport.raster.GeoTiffFixtures;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;

/**
 * Polar overlays through the station's inbox loop with recording sinks (prompt 054): the SatDump 1.2.2
 * producer fixture becomes one {@code satdump-home-…} frame with its footprint, the pass geometry and
 * product entry follow, a Bering Sea pass splits at ±180°, {@code polarGlob} is honored, a refused CRS
 * is a health reason (or goes through GDAL), the approximate swath is the fallback, remote uploads are
 * ready at once, and a pushed image attaches to its pass.
 */
class PolarOverlaysTest {

    static final Instant T = Instant.parse("2026-10-12T18:20:00Z");
    static final String PRODUCT = "satdump-home-rgb-msu-mr-221-composite";

    @TempDir Path temp;

    record Run(SatDumpTestContext ctx, SatDumpStation station, List<NormalizedRecord> records) {

        NormalizedRecord pass() {
            return records.stream().filter(r -> r.kind().equals(PassBook.KIND)).reduce((a, b) -> b).orElseThrow();
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> products() {
            return (List<Map<String, Object>>) pass().payload().get("products");
        }
    }

    /** Copies a 1.2.2 pass folder into a fresh inbox and scans until it is processed. */
    Run run(String fixture, java.util.function.Consumer<SatDumpTestContext> setup) throws Exception {
        return run(temp, fixture, setup);
    }

    /** {@link #run(String, java.util.function.Consumer)} under {@code base}. */
    static Run run(Path base, String fixture, java.util.function.Consumer<SatDumpTestContext> setup) throws Exception {
        Path root = Files.createDirectories(base.resolve("inbox"));
        Path station = root.resolve("satdump/home");
        SatDumpFixtures.copyProducer(fixture, station.resolve("pass-a"));
        SatDumpTestContext ctx = new SatDumpTestContext(root, T);
        ctx.params.put(SatDumpDescriptor.INBOX_PATH, station.toRealPath());
        setup.accept(ctx);
        SatDumpStation s = ctx.station();
        for (int i = 0; i < 3; i++) {
            s.scanInboxOnce();
            ctx.clock.advance(Duration.ofSeconds(30));
        }
        return new Run(ctx, s, ctx.drain());
    }

    @Test
    @DisplayName("the 1.2.2 producer fixture: one rgba8 affine frame with its footprint, a pass product, and the pass geometry")
    void producerFixture() throws Exception {
        Run run = run(SatDumpFixtures.PRODUCER_PASS, ctx -> {});

        assertThat(run.ctx().rasters.registered).singleElement().satisfies(spec -> {
            assertThat(spec.id()).isEqualTo(PRODUCT);
            assertThat(spec.dtype()).isEqualTo(RasterDtype.RGBA8);
            assertThat(spec.temporal().type()).isEqualTo(ProductSpec.Temporal.PASS);
            assertThat(spec.spatial().coverage()).isEqualTo("swath");
            assertThat(spec.defaultStyle()).isEqualTo("rgb@1");
            assertThat(spec.retention().maxAge()).isEqualTo(Duration.ofDays(30));
        });
        FrameInput frame = run.ctx().rasters.commits.get(0);
        assertThat(run.ctx().rasters.commits).hasSize(1);
        assertThat(frame.productId()).isEqualTo(PRODUCT);
        assertThat(frame.validTime()).isEqualTo(Instant.parse("2026-10-12T18:02:10Z"));
        GridDefinition grid = frame.grid();
        assertThat(grid.type()).isEqualTo(GridDefinition.AFFINE);
        assertThat(grid.nx()).isEqualTo(120);
        assertThat(grid.ny()).isEqualTo(160);
        assertThat(grid.params()).containsEntry("crs", "EPSG:4326").containsEntry("gt0", -110.0)
                .containsEntry("gt1", 0.25).containsEntry("gt3", 42.0).containsEntry("gt5", -0.25);
        assertThat(List.of(grid.west(), grid.south(), grid.east(), grid.north())).containsExactly(-110.0, 2.0, -80.0, 42.0);
        assertThat(frame.bands().codes().get(0)).hasSize(120 * 160);
        assertThat(frame.footprint()).isNotNull();
        assertThat(frame.footprint().getEnvelopeInternal().getMinX()).isGreaterThan(-110.0);

        assertThat(run.records()).filteredOn(r -> r.kind().equals(ProductSpec.RECORD_KIND)).hasSize(1);
        NormalizedRecord pass = run.pass();
        assertThat(pass.payload()).containsEntry("geometrySource", "footprint").containsEntry("status", "decoded");
        assertThat(pass.geometry().getGeometryType()).isIn("Polygon", "MultiPolygon");
        assertThat(pass.geometry().getEnvelopeInternal().getMaxY()).isEqualTo(42.0);
        assertThat(run.products()).filteredOn(p -> "rgb_MSU-MR_221_Composite.png".equals(p.get("composite")))
                .singleElement().satisfies(p -> {
                    assertThat(p).containsEntry("productId", PRODUCT).containsKeys("mediaId", "thumbMediaId");
                    assertThat((String) p.get("frameKey")).startsWith("20261012T180210Z~");
                    assertThat((String) p.get("tilesTemplate")).contains("/tiles/" + p.get("frameKey") + "/{z}/{x}/{y}.png");
                });
        String dir = "home/" + pass.externalId().replace(':', '_') + "/";
        assertThat(run.ctx().rasters.provenance).containsKey(dir + SatDumpFixtures.PRODUCER_TIFF);
        assertThat(run.station().reasons()).doesNotContainKey("polar");
    }

    @Test
    @DisplayName("a Bering Sea GeoTIFF past 180°: the grid box crosses (west > east) and the footprint has two parts")
    void bering() throws Exception {
        Run run = run(SatDumpFixtures.BERING_PASS, ctx -> {});
        FrameInput frame = run.ctx().rasters.commits.get(0);
        assertThat(frame.grid().west()).isEqualTo(165.0);
        assertThat(frame.grid().east()).isEqualTo(-165.0);
        MultiPolygon footprint = frame.footprint();
        assertThat(footprint.getNumGeometries()).isEqualTo(2);
        Geometry geometry = run.pass().geometry();
        assertThat(geometry.getNumGeometries()).isEqualTo(2);
        assertThat(geometry.getEnvelopeInternal().getMinX()).isEqualTo(-180.0);
        assertThat(geometry.getEnvelopeInternal().getMaxX()).isEqualTo(180.0);
    }

    @Test
    @DisplayName("polarGlob is honored: no match, no overlay; the pass gets the approximate swath from its TLE")
    void globAndSwath() throws Exception {
        Run run = run(SatDumpFixtures.PRODUCER_PASS, ctx -> ctx.params.put(SatDumpDescriptor.POLAR_GLOB, "nothing/*.tif"));
        assertThat(run.ctx().rasters.commits).isEmpty();
        assertThat(run.ctx().rasters.registered).isEmpty();
        NormalizedRecord pass = run.pass();
        assertThat(pass.payload()).containsEntry("geometrySource", "swath");
        assertThat(pass.geometry().getArea()).isGreaterThan(100.0);
        assertThat(run.products()).allSatisfy(p -> assertThat(p).doesNotContainKey("productId"));
    }

    @Test
    @DisplayName("a refused CRS is a health reason and falls back to the swath; with GDAL the warped file becomes the overlay")
    void refusedCrsAndGdal() throws Exception {
        byte[] utm = GeoTiffFixtures.wgs84(0, 0, 1).geoKeys(1024, 1, 3072, 32614).size(10, 10).build();
        Run refused = run(temp.resolve("first"), SatDumpFixtures.PRODUCER_PASS, ctx -> overwriteTiff(temp.resolve("first"), utm));
        assertThat(refused.station().reasons()).containsEntry("polar",
                "GeoTIFF CRS EPSG:32614 not supported (build with WITH_GDAL=true)");
        assertThat(refused.pass().payload()).containsEntry("geometrySource", "swath");
        assertThat(refused.ctx().rasters.warped).hasSize(1);

        byte[] warped = GeoTiffFixtures.wgs84(-100, 35, 0.5).size(10, 10)
                .pixels((x, y) -> x > 2 ? new int[] {50, 60, 70} : new int[] {0, 0, 0}).build();
        Run ok = run(temp.resolve("second"), SatDumpFixtures.PRODUCER_PASS, ctx -> {
            overwriteTiff(temp.resolve("second"), utm);
            ctx.rasters.warp = b -> Optional.of(warped);
        });
        assertThat(ok.ctx().rasters.commits).hasSize(1);
        assertThat(ok.pass().payload()).containsEntry("geometrySource", "footprint");
        assertThat(ok.station().reasons()).doesNotContainKey("polar");
    }

    /** Replaces the fixture GeoTIFF in the copied pass folder (called after the copy, before the scans). */
    static void overwriteTiff(Path base, byte[] bytes) {
        try {
            Files.write(base.resolve("inbox/satdump/home/pass-a").resolve(SatDumpFixtures.PRODUCER_TIFF), bytes);
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    @Test
    @DisplayName("a remote upload under rasters/satdump/{station}/incoming/ is processed on the first scan, without an inbox")
    void uploads() throws Exception {
        Path uploads = Files.createDirectories(temp.resolve("data/rasters/satdump"));
        SatDumpFixtures.copyProducer(SatDumpFixtures.PRODUCER_PASS, uploads.resolve("home/incoming/01jupload/pass-a"));
        Path inboxRoot = Files.createDirectories(temp.resolve("inbox"));
        SatDumpTestContext ctx = new SatDumpTestContext(inboxRoot, T);
        ctx.rasters.uploads = InboxFiles.rootedAt(uploads);
        SatDumpStation s = ctx.station();
        s.scanInboxOnce();
        List<NormalizedRecord> records = ctx.drain();
        assertThat(records).filteredOn(r -> r.kind().equals(PassBook.KIND)).singleElement()
                .satisfies(r -> assertThat(r.payload()).containsEntry("geometrySource", "footprint"));
        assertThat(ctx.rasters.commits).hasSize(1);
        assertThat(ctx.charged.get()).as("an upload paid at push time").isZero();
        s.scanInboxOnce();
        assertThat(ctx.drain()).isEmpty();
    }

    @Test
    @DisplayName("a pushed image is guarded, stored owned by the joined pass, and the pass is emitted")
    void pushedImage() throws Exception {
        Path inboxRoot = Files.createDirectories(temp.resolve("inbox"));
        SatDumpTestContext ctx = new SatDumpTestContext(inboxRoot, T);
        SatDumpStation s = ctx.station();
        byte[] png = SatDumpFixtures.bytes(SatDumpFixtures.PASS + "/MSU-MR/rgb_MSU-MR_221_Composite.png");
        RemoteMediaReceiver.Accepted accepted = s.receive(new RemoteMediaReceiver.Upload(png, "image/png", null,
                "msu_mr_rgb_221", Instant.parse("2026-10-12T18:02:10Z"), "METEOR-M2 3"));
        assertThat(accepted.passId()).isEqualTo("home:meteor-m2-3:" + Instant.parse("2026-10-12T18:02:10Z").getEpochSecond() / 60);
        assertThat(accepted.mediaId()).isNotNull();
        assertThat(ctx.media.stored).singleElement()
                .satisfies(m -> assertThat(m.recordExternalId()).isEqualTo(accepted.passId()));
        assertThat(ctx.drain()).singleElement().satisfies(r -> {
            assertThat(r.externalId()).isEqualTo(accepted.passId());
            assertThat(r.payload()).containsEntry("status", "decoded");
        });
        // The same pass id attaches a second image to the same pass.
        RemoteMediaReceiver.Accepted again = s.receive(new RemoteMediaReceiver.Upload(png, "image/png", accepted.passId(),
                "msu_mr_rgb_125", Instant.parse("2026-10-12T19:30:00Z"), "METEOR-M2 3"));
        assertThat(again.passId()).isEqualTo(accepted.passId());
    }

    @Test
    @DisplayName("composite names, product ids, merging and the grid box")
    void helpers() {
        assertThat(PolarOverlays.composite("rgb_MSU-MR_221_Composite_projected.tif")).isEqualTo("rgb_MSU-MR_221_Composite");
        assertThat(PolarOverlays.composite("avhrr_3_rgb_221.TIF")).isEqualTo("avhrr_3_rgb_221");
        assertThat(PolarOverlays.productId("home", "rgb_MSU-MR_221_Composite")).isEqualTo(PRODUCT);
        String long64 = PolarOverlays.productId("home", "x".repeat(80));
        assertThat(long64).hasSizeLessThanOrEqualTo(64).startsWith("satdump-home-x");

        PolarOverlays.Overlay overlay = new PolarOverlays.Overlay("rgb_MSU-MR_221_Composite",
                "rgb_MSU-MR_221_Composite_projected.tif", "msu_mr", PRODUCT, "20261012T180210Z~0a1b2c3d", "t", null);
        PolarOverlays.Overlay alone = new PolarOverlays.Overlay("channel_1", "channel_1_projected.tif", "msu_mr",
                "satdump-home-channel-1", null, null, null);
        List<Map<String, Object>> merged = PolarOverlays.merge(List.of(Map.of("composite",
                "rgb_MSU-MR_221_Composite.png", "mediaId", "m")), List.of(overlay, alone));
        assertThat(merged).hasSize(2);
        assertThat(merged.get(0)).containsEntry("productId", PRODUCT).containsEntry("tilesTemplate", "t")
                .containsEntry("mediaId", "m");
        assertThat(merged.get(1)).containsEntry("composite", "channel_1_projected.tif")
                .containsEntry("productId", "satdump-home-channel-1").doesNotContainKey("frameKey");

        GridDefinition global = PolarOverlays.grid(360, 10, new GeoReference(4326, new double[] {-180, 1, 0, 80, 0, -1}, false));
        assertThat(List.of(global.west(), global.east())).containsExactly(-180.0, 180.0);
        GridDefinition merc = PolarOverlays.grid(10, 10, new GeoReference(3857, new double[] {0, 1000, 0, 0, 0, -1000}, false));
        assertThat(merc.params()).containsEntry("crs", "EPSG:3857");
        assertThat(merc.north()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("the swath window: product timestamps, else AOS to LOS, else ±5 minutes")
    void window() {
        Instant t = Instant.parse("2026-10-12T18:02:10Z");
        Instant[] fromTimes = PolarOverlays.window(List.of(t.getEpochSecond() - 300.0, t.getEpochSecond() + 300.0, -1.0),
                t, null);
        assertThat(fromTimes).containsExactly(t.minusSeconds(300), t.plusSeconds(300));
        PassState pass = new PassState();
        pass.aos = t.minusSeconds(400).toEpochMilli();
        pass.los = t.plusSeconds(400).toEpochMilli();
        assertThat(PolarOverlays.window(List.of(), t, pass)).containsExactly(t.minusSeconds(400), t.plusSeconds(400));
        assertThat(PolarOverlays.window(List.of(), t, new PassState())).containsExactly(t.minusSeconds(300), t.plusSeconds(300));
    }
}
