package ai.foresignal.source.adapters.satdump;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.source.spi.AreaOfInterest;
import ai.foresignal.source.spi.CurrentRecords;
import ai.foresignal.source.spi.FrameInput;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ValidatedParams;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.ObjectProvider;

/**
 * The DCS, EMWIN and manifest extras of a {@code satdump-{station}} source driven through the station's
 * inbox loop (prompt 107): EMWIN processed as each file settles without any pass folder, issue-time
 * ordering so an older {@code NEW} never resurrects a canceled warning, the stable {@code emwin-product}
 * id and issue time, the mandatory DCS bbox, DCS emit-on-change, a remote upload, and a synthetic
 * {@code foresignal-manifest.json} pass folder whose PNG is georeferenced from its bounds.
 */
class SatDumpExtrasTest {

    static final Instant T = Instant.parse("2026-10-15T23:20:00Z");

    static final String TORNADO = "WUUS54 KFWD 152300\n"
            + "TORFWD\n"
            + "BULLETIN - EAS ACTIVATION REQUESTED\n"
            + "Tornado Warning\n"
            + "/O.NEW.KFWD.TO.W.0042.261015T2300Z-261015T2345Z/\n"
            + "\n"
            + "LAT...LON 3250 09730 3240 09720 3260 09710 3250 09730\n"
            + "TIME...MOT...LOC 2300Z 250DEG 24KT 3250 09730\n";

    @TempDir Path temp;

    Path root;
    Path station;
    SatDumpTestContext ctx;
    TestAoi aoi;
    String nwsHealth = "FAILING";
    List<CurrentRecords.Row> nwsRows = new ArrayList<>();
    DcpLocationStore locations;
    SatDumpExtras extras;

    /** An AOI over Texas whose default flag a test sets. */
    static final class TestAoi implements AreaOfInterest {
        boolean isDefault;

        @Override
        public double[] envelope() {
            return new double[] {-106.65, 25.84, -93.51, 36.50};
        }

        @Override
        public long version() {
            return 1;
        }

        @Override
        public void registerConsumer(String sourceId) {}

        @Override
        public boolean isDefault() {
            return isDefault;
        }
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        root = Files.createDirectories(temp.resolve("inbox"));
        station = Files.createDirectories(root.resolve("satdump/home"));
        ctx = new SatDumpTestContext(root, T);
        ctx.params.put(SatDumpDescriptor.INBOX_PATH, station.toRealPath());
        aoi = new TestAoi();
        locations = mock(DcpLocationStore.class);
        when(locations.find(anyString())).thenReturn(Optional.empty());
        CurrentRecords records = new CurrentRecords() {
            @Override
            public List<Row> current(List<String> sources, List<String> kinds, int limit) {
                return nwsRows;
            }

            @Override
            public String health(String sourceId) {
                return nwsHealth;
            }
        };
        ObjectProvider<AreaOfInterest> aoiProvider = mock(ObjectProvider.class);
        when(aoiProvider.getIfAvailable()).thenReturn(aoi);
        extras = new SatDumpExtras(locations, records, null, aoiProvider);
    }

    SatDumpStation station() {
        return new SatDumpStation("home", ctx, ctx.sink(), null, ctx.holder(), null, null, extras);
    }

    Path write(String relative, String content, Instant modified) throws Exception {
        Path file = station.resolve(relative);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content, StandardCharsets.UTF_8);
        Files.setLastModifiedTime(file, FileTime.from(modified));
        return file;
    }

    void scan(SatDumpStation s) {
        s.scanInboxOnce();
        ctx.clock.advance(Duration.ofSeconds(30));
    }

    List<NormalizedRecord> ofKind(List<NormalizedRecord> records, String kind) {
        return records.stream().filter(r -> kind.equals(r.kind())).toList();
    }

    @Test
    @DisplayName("an EMWIN file is processed once it settles, with no pass folder, and only once")
    void emwinSettlesWithoutPassFolder() throws Exception {
        ctx.params.put(SatDumpExtras.EMWIN_ENABLED, true);
        write("live/EMWIN/2026-10-15/A_TORFWD.TXT", TORNADO, T.minusSeconds(60));
        SatDumpStation s = station();

        scan(s);
        assertThat(ofKind(ctx.drain(), SatDumpExtras.EMWIN_WARNING_KIND)).isEmpty();
        scan(s);
        List<NormalizedRecord> out = ctx.drain();
        assertThat(ofKind(out, SatDumpExtras.EMWIN_PRODUCT_KIND)).singleElement().satisfies(p -> {
            assertThat(p.externalId()).isEqualTo("WUUS54 KFWD 152300:TORFWD:2026-10-15T23:00:00Z");
            assertThat(p.observedAt()).isEqualTo(Instant.parse("2026-10-15T23:00:00Z"));
            assertThat(p.payload()).containsEntry("issuedAt", "2026-10-15T23:00:00Z");
        });
        assertThat(ofKind(out, SatDumpExtras.EMWIN_WARNING_KIND)).singleElement().satisfies(w -> {
            assertThat(w.externalId()).isEqualTo("KFWD.TO.W.0042");
            assertThat(w.tombstone()).isFalse();
            assertThat(w.payload()).containsEntry("fallback", true).doesNotContainKey("duplicateOf");
            assertThat(w.payload()).as("etn is an integer, per EmwinWarningPayload").containsEntry("etn", 42);
            assertThat(w.geometry().getEnvelopeInternal().getMinY()).isEqualTo(32.40);
            assertThat(w.geometry().getEnvelopeInternal().getMinX()).isEqualTo(-97.30);
        });
        scan(s);
        assertThat(ofKind(ctx.drain(), SatDumpExtras.EMWIN_PRODUCT_KIND)).isEmpty();
    }

    @Test
    @DisplayName("products are applied in issue order; an older NEW read later never resurrects a canceled warning")
    void olderNewNeverResurrects() throws Exception {
        ctx.params.put(SatDumpExtras.EMWIN_ENABLED, true);
        String cancel = TORNADO.replace("WUUS54 KFWD 152300", "WUUS54 KFWD 152330").replace("/O.NEW.", "/O.CAN.");
        // File names sort the cancellation first: the issue time, not the folder order, must decide.
        write("live/EMWIN/a_cancel.txt", cancel, T.minusSeconds(60));
        write("live/EMWIN/b_new.txt", TORNADO, T.minusSeconds(60));
        SatDumpStation s = station();
        scan(s);
        scan(s);
        List<NormalizedRecord> warnings = ofKind(ctx.drain(), SatDumpExtras.EMWIN_WARNING_KIND);
        assertThat(warnings).hasSize(2);
        assertThat(warnings.get(0).tombstone()).isFalse();
        assertThat(warnings.get(1).tombstone()).isTrue();

        // A copy of the old NEW arrives late (for example a retransmission).
        write("live/EMWIN/c_new_again.txt", TORNADO, ctx.clock.instant().minusSeconds(5));
        scan(s);
        scan(s);
        List<NormalizedRecord> late = ctx.drain();
        assertThat(ofKind(late, SatDumpExtras.EMWIN_PRODUCT_KIND)).hasSize(1);
        assertThat(ofKind(late, SatDumpExtras.EMWIN_WARNING_KIND)).isEmpty();
    }

    @Test
    @DisplayName("while nws-alerts is available and holds the VTEC, the warning carries duplicateOf")
    void duplicateOf() throws Exception {
        ctx.params.put(SatDumpExtras.EMWIN_ENABLED, true);
        nwsHealth = "HEALTHY";
        nwsRows.add(new CurrentRecords.Row("nws-alerts", "urn:x", "nws-alert", T,
                Map.of("vtec", List.of("/O.NEW.KFWD.TO.W.0042.261015T2300Z-261015T2345Z/"))));
        write("live/EMWIN/a.txt", TORNADO, T.minusSeconds(60));
        SatDumpStation s = station();
        scan(s);
        scan(s);
        assertThat(ofKind(ctx.drain(), SatDumpExtras.EMWIN_WARNING_KIND)).singleElement()
                .satisfies(w -> assertThat(w.payload()).containsEntry("duplicateOf",
                        Map.of("source", "nws-alerts", "externalId", "KFWD.TO.W.0042")));
        assertThat(SatDumpExtras.vtecKey("/O.CON.KFWD.SV.W.0101.000000T0000Z-261015T2345Z/")).contains("KFWD.SV.W.0101");
    }

    @Test
    @DisplayName("DCS is never processed without an effective bbox; the default AOI box does not count")
    void dcsNeedsBbox() throws Exception {
        aoi.isDefault = true;
        ctx.params.put(SatDumpExtras.DCS_ENABLED, true);
        when(locations.find("CE1234")).thenReturn(Optional.of(
                new DcpLocationStore.DcpLocation("CE1234", 32.5, -97.3, "Gauge", "USGS")));
        write("live/DCS/m1.json", "{\"address\":\"CE1234\",\"time\":\"2026-10-15T23:00:00Z\",\"data\":\"HELLO\"}",
                T.minusSeconds(60));
        SatDumpStation s = station();
        scan(s);
        scan(s);
        assertThat(ofKind(ctx.drain(), SatDumpExtras.DCP_KIND)).isEmpty();
        assertThat(s.reasons().get("extras")).contains("no bbox");

        assertThat(extras.effectiveBbox(ValidatedParams.empty())).isNull();
        aoi.isDefault = false;
        assertThat(extras.effectiveBbox(ValidatedParams.empty())).isEqualTo(new Envelope(-106.65, -93.51, 25.84, 36.50));
        Envelope param = new Envelope(-98, -97, 32, 33);
        assertThat(extras.effectiveBbox(ValidatedParams.of(Map.of(SatDumpExtras.DCS_BBOX, param)))).isEqualTo(param);
    }

    @Test
    @DisplayName("DCS with a bbox emits the newest message per address once; an older message is not emitted again")
    void dcsEmitsNewestOnce() throws Exception {
        ctx.params.put(SatDumpExtras.DCS_ENABLED, true);
        ctx.params.put(SatDumpExtras.DCS_BBOX, new Envelope(-98, -97, 32, 33));
        when(locations.find("CE1234")).thenReturn(Optional.of(
                new DcpLocationStore.DcpLocation("CE1234", 32.5, -97.3, "Gauge", "USGS")));
        when(locations.find("CE9999")).thenReturn(Optional.of(
                new DcpLocationStore.DcpLocation("CE9999", 40.0, -80.0, "Far", "USGS")));
        write("live/DCS/m1.json", "[{\"address\":\"CE1234\",\"time\":\"2026-10-15T23:00:00Z\",\"data\":\"A\"},"
                + "{\"address\":\"CE1234\",\"time\":\"2026-10-15T23:05:00Z\",\"data\":\"B\"},"
                + "{\"address\":\"CE9999\",\"time\":\"2026-10-15T23:05:00Z\",\"data\":\"C\"}]", T.minusSeconds(60));
        SatDumpStation s = station();
        scan(s);
        scan(s);
        assertThat(ofKind(ctx.drain(), SatDumpExtras.DCP_KIND)).singleElement().satisfies(r -> {
            assertThat(r.externalId()).isEqualTo("CE1234");
            assertThat(r.observedAt()).isEqualTo(Instant.parse("2026-10-15T23:05:00Z"));
        });
        write("live/DCS/m0.json", "{\"address\":\"CE1234\",\"time\":\"2026-10-15T22:55:00Z\",\"data\":\"OLD\"}",
                ctx.clock.instant().minusSeconds(5));
        scan(s);
        scan(s);
        assertThat(ofKind(ctx.drain(), SatDumpExtras.DCP_KIND)).isEmpty();
    }

    @Test
    @DisplayName("the emwin-product id is stable: the issue time, else a SHA-256 of the text")
    void productId() throws Exception {
        EmwinTextParser.Parsed parsed = EmwinTextParser.parse(TORNADO.getBytes(StandardCharsets.UTF_8));
        assertThat(SatDumpExtras.productId(parsed, Instant.parse("2026-10-15T23:00:00Z")))
                .isEqualTo("WUUS54 KFWD 152300:TORFWD:2026-10-15T23:00:00Z");
        String noIssue = SatDumpExtras.productId(parsed, null);
        assertThat(noIssue).startsWith("WUUS54 KFWD 152300:TORFWD:").hasSize("WUUS54 KFWD 152300:TORFWD:".length() + 16);
        assertThat(SatDumpExtras.productId(parsed, null)).isEqualTo(noIssue);
    }

    @Test
    @DisplayName("a synthetic manifest pass folder: the listed PNG is georeferenced from its bounds and committed")
    void manifestPassFolder() throws Exception {
        Path pass = station.resolve("pass-a");
        SatDumpFixtures.copy(SatDumpFixtures.PASS, pass);
        BufferedImage image = new BufferedImage(40, 20, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 40; x++) {
                image.setRGB(x, y, 0xFF336699);
            }
        }
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(image, "png", png);
        Files.createDirectories(pass.resolve("hook"));
        Files.write(pass.resolve("hook/rgb_equirec.png"), png.toByteArray());
        Files.writeString(pass.resolve(ForesignalManifest.FILE), "{\"schema\":1,\"satellite\":\"METEOR-M2 4\","
                + "\"timestamp\":\"2026-10-12T18:05:00Z\",\"products\":[{\"name\":\"rgb\",\"instrument\":\"MSU-MR\","
                + "\"file\":\"hook/rgb_equirec.png\",\"projection\":\"equirec\",\"bounds\":[-100,30,-90,35],"
                + "\"composite\":\"rgb_equirec\"}]}");
        SatDumpStation s = station();
        for (int i = 0; i < 3; i++) {
            scan(s);
        }
        FrameInput frame = ctx.rasters.commits.stream()
                .filter(f -> f.productId().equals(PolarOverlays.productId("home", "rgb_equirec"))).findFirst().orElseThrow();
        assertThat(frame.validTime()).isEqualTo(Instant.parse("2026-10-12T18:05:00Z"));
        GridDefinition grid = frame.grid();
        assertThat(grid.params()).containsEntry("crs", "EPSG:4326").containsEntry("gt0", -100.0)
                .containsEntry("gt1", 0.25).containsEntry("gt3", 35.0).containsEntry("gt5", -0.25);
        assertThat(List.of(grid.west(), grid.south(), grid.east(), grid.north())).containsExactly(-100.0, 30.0, -90.0, 35.0);
        assertThat(frame.footprint()).isNotNull();
    }

    @Test
    @DisplayName("manifest PNG bounds are validated, and a PNG crossing 180 degrees is unwrapped")
    void manifestBounds() {
        assertThat(PolarOverlays.validBounds(new double[] {-100, 30, -90, 35})).isTrue();
        assertThat(PolarOverlays.validBounds(new double[] {-100, 35, -90, 30})).isFalse();
        assertThat(PolarOverlays.validBounds(new double[] {-200, 30, -90, 35})).isFalse();
        assertThat(PolarOverlays.validBounds(null)).isFalse();
        BufferedImage image = new BufferedImage(20, 10, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, 0x80FF0000);
        var geo = PolarOverlays.georeferenced(image, new double[] {170, 50, -170, 60});
        assertThat(geo.georeference().gt()).containsExactly(170, 1.0, 0, 60, 0, -1.0);
        assertThat(geo.rgba()[0]).isEqualTo(0xFF000080);
        assertThat(geo.rgba()[1] & 0xFF).isZero();
    }
}
