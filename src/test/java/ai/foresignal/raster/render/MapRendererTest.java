package ai.foresignal.raster.render;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link MapRenderer} (prompt 055): renders of {@link MapFixtures} against the golden PNGs in
 * {@code src/test/resources/maps/} (at most 1 % of pixels may differ by more than a small per-channel
 * tolerance, which absorbs font rasterization differences between platforms), light and dark themes, the
 * note of an unavailable product, the footer and attribution, the header times, the box fitted to the map
 * area, and the PNG-8/PNG-24 choice.
 *
 * <p>The goldens were generated from these exact specs with the compiled renderer (see the prompt 055
 * Completion Report); regenerate them only when the drawing changes on purpose.
 */
class MapRendererTest {

    /** The largest fraction of pixels allowed to differ. */
    static final double MAX_DIFF = 0.01;

    /** The per-channel difference that counts a pixel as different. */
    static final int CHANNEL_TOLERANCE = 48;

    final MapFixtures.BandRenderer bands = new MapFixtures.BandRenderer();
    final MapRenderer renderer = MapFixtures.renderer(bands);

    @Test
    @DisplayName("the full light map matches its golden: product, polygon, place, rings, legend, footer")
    void goldenLight() throws Exception {
        MapRenderer.Rendered r = renderer.draw(MapFixtures.full(false));
        assertThat(r.width()).isEqualTo(800);
        assertThat(r.height()).isEqualTo(600);
        assertMatchesGolden(r.png(), "full-light.png");
        assertThat(bands.calls).as("one overlay draw").isEqualTo(1);
    }

    @Test
    @DisplayName("the dark theme matches its golden and differs from the light one")
    void goldenDark() throws Exception {
        MapRenderer.Rendered dark = renderer.draw(MapFixtures.full(true));
        assertMatchesGolden(dark.png(), "full-dark.png");
        BufferedImage light = decode(renderer.draw(MapFixtures.full(false)).png());
        BufferedImage d = decode(dark.png());
        assertThat(d.getRGB(5, 300)).as("land differs between themes").isNotEqualTo(light.getRGB(5, 300));
    }

    @Test
    @DisplayName("an unavailable product draws the no-coverage note, never silently nothing")
    void unavailableNote() throws Exception {
        MapSpec spec = MapFixtures.unavailable();
        assertThat(MapRenderer.notesOf(spec)).containsExactly(
                "Reflectivity unavailable — not evidence of clear weather");
        assertThat(MapRenderer.headerLines(spec)).containsExactly("Reflectivity — unavailable");
        MapRenderer.Rendered r = renderer.draw(spec);
        assertMatchesGolden(r.png(), "unavailable.png");
        assertThat(bands.calls).as("no frame, no overlay draw").isZero();
    }

    @Test
    @DisplayName("a stale product keeps its frame and gets a stale note")
    void staleNote() {
        MapSpec spec = new MapSpec(MapFixtures.BOX, 400, 300, List.of(new MapSpec.Layer(MapFixtures.reflectivity(),
                MapFixtures.reflectivityStyle(), MapFixtures.frame(), MapSpec.FrameStatus.STALE, "old")), null, null,
                List.of(), false, false, null);
        assertThat(MapRenderer.notesOf(spec)).containsExactly(
                "Reflectivity stale (frame 2026-10-12 21:04 UTC) — not evidence of clear weather");
        renderer.draw(spec);
        assertThat(bands.calls).isEqualTo(1);
    }

    @Test
    @DisplayName("the footer and the attribution: each product's attribution once, then Natural Earth")
    void footerAndAttribution() throws Exception {
        assertThat(MapRenderer.FOOTER).isEqualTo("Automated — not an official warning");
        MapSpec spec = MapFixtures.full(false);
        assertThat(MapRenderer.attribution(spec)).isEqualTo("Data: Test · Basemap: Natural Earth");
        BufferedImage image = decode(renderer.draw(spec).png());
        MapRenderer.Layout layout = MapRenderer.layout(spec);
        int dark = 0;
        for (int y = layout.footerTop(); y < spec.height(); y++) {
            for (int x = 0; x < 260; x++) {
                if ((image.getRGB(x, y) & 0xFF) < 0x80) {
                    dark++;
                }
            }
        }
        assertThat(dark).as("text is drawn in the footer strip").isGreaterThan(200);
    }

    @Test
    @DisplayName("header times: UTC plus the place's local time; UTC alone without a place zone")
    void headerTimes() {
        assertThat(MapRenderer.frameTime(MapFixtures.frame(), ZoneId.of("America/Chicago")))
                .isEqualTo("2026-10-12 21:04 UTC (16:04 CDT)");
        assertThat(MapRenderer.frameTime(MapFixtures.frame(), ZoneId.of("UTC"))).isEqualTo("2026-10-12 21:04 UTC");
        assertThat(MapRenderer.frameTime(null, null)).isEqualTo("unavailable");
        assertThat(MapRenderer.headerLines(MapFixtures.full(false)))
                .containsExactly("Reflectivity — 2026-10-12 21:04 UTC (16:04 CDT)");
    }

    @Test
    @DisplayName("the box is snapped and widened to the map area's aspect around the same center")
    void fittedBox() {
        MapSpec spec = MapFixtures.full(false);
        MapRenderer.Layout layout = MapRenderer.layout(spec);
        double[] b = layout.bounds();
        double w = WebMercator.x(b[2]) - WebMercator.x(b[0]);
        double h = WebMercator.y(b[3]) - WebMercator.y(b[1]);
        assertThat(w / h).isCloseTo((double) spec.width() / layout.mapHeight(), org.assertj.core.data.Offset.offset(0.01));
        assertThat(b[1]).isEqualTo(29.6);
        assertThat(b[3]).isEqualTo(31.0);
        assertThat((b[0] + b[2]) / 2).isCloseTo((MapFixtures.BOX[0] + MapFixtures.BOX[2]) / 2,
                org.assertj.core.data.Offset.offset(1e-6));
        assertThat(renderer.draw(spec).boundsHeader()).isEqualTo("-99.055089,29.6,-96.444911,31");
    }

    @Test
    @DisplayName("PNG-8 when the image has at most 256 colors, else PNG-24")
    void paletteChoice() throws Exception {
        int[] few = new int[64 * 64];
        for (int i = 0; i < few.length; i++) {
            few[i] = i % 3 == 0 ? 0xFF112233 : 0xFFFFFFFF;
        }
        byte[] png8 = MapRenderer.indexedPng(few, 64, 64);
        assertThat(png8).isNotNull();
        BufferedImage decoded = decode(png8);
        assertThat(decoded.getColorModel()).isInstanceOf(java.awt.image.IndexColorModel.class);
        assertThat(decoded.getRGB(0, 0)).isEqualTo(0xFF112233);
        int[] many = new int[64 * 64];
        for (int i = 0; i < many.length; i++) {
            many[i] = 0xFF000000 | i * 37;
        }
        assertThat(MapRenderer.indexedPng(many, 64, 64)).isNull();
        assertThat(renderer.draw(MapFixtures.full(false)).indexed()).as("antialiased map: 24-bit").isFalse();
    }

    @Test
    @DisplayName("render goes through the render queue and returns the drawn map")
    void throughQueue() {
        RenderQueue queue = new RenderQueue(1, Duration.ofSeconds(5), Clock.systemUTC());
        try {
            MapRenderer queued = new MapRenderer(new BasemapStore(), bands, new OperatorBasemap(null, null, null), queue);
            MapRenderer.Rendered r = queued.render(MapFixtures.full(false), "etag-1", "test");
            assertThat(r.png()).isNotEmpty();
            assertThat(r.boundsHeader()).isEqualTo("-99.055089,29.6,-96.444911,31");
        } finally {
            queue.destroy();
        }
    }

    @Test
    @DisplayName("helpers: destination points, nice scale lengths, distance labels")
    void helpers() {
        double[] north = MapRenderer.destination(-97.74, 30.27, 0, 111.195);
        assertThat(north[0]).isCloseTo(-97.74, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(north[1]).isCloseTo(31.27, org.assertj.core.data.Offset.offset(1e-3));
        assertThat(MapRenderer.nice(37)).isEqualTo(20);
        assertThat(MapRenderer.nice(0.7)).isCloseTo(0.5, org.assertj.core.data.Offset.offset(1e-12));
        assertThat(MapRenderer.distanceLabel(25, false)).isEqualTo("25 km");
        assertThat(MapRenderer.distanceLabel(25, true)).isEqualTo("15.5 mi");
    }

    // ------------------------------------------------------------------ golden comparison

    static void assertMatchesGolden(byte[] png, String name) throws Exception {
        BufferedImage actual = decode(png);
        BufferedImage golden;
        try (InputStream in = MapRendererTest.class.getResourceAsStream("/maps/" + name)) {
            assertThat(in).as("golden " + name).isNotNull();
            golden = ImageIO.read(in);
        }
        assertThat(actual.getWidth()).isEqualTo(golden.getWidth());
        assertThat(actual.getHeight()).isEqualTo(golden.getHeight());
        long different = 0;
        for (int y = 0; y < actual.getHeight(); y++) {
            for (int x = 0; x < actual.getWidth(); x++) {
                if (differs(actual.getRGB(x, y), golden.getRGB(x, y))) {
                    different++;
                }
            }
        }
        double fraction = (double) different / ((long) actual.getWidth() * actual.getHeight());
        assertThat(fraction).as("fraction of pixels differing from " + name).isLessThanOrEqualTo(MAX_DIFF);
    }

    static boolean differs(int a, int b) {
        for (int shift = 0; shift <= 16; shift += 8) {
            if (Math.abs((a >> shift & 0xFF) - (b >> shift & 0xFF)) > CHANNEL_TOLERANCE) {
                return true;
            }
        }
        return false;
    }

    static BufferedImage decode(byte[] png) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(png));
    }
}
