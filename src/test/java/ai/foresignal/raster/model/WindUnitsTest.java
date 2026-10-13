package ai.foresignal.raster.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.raster.ElgCodec;
import ai.foresignal.raster.EncodedFrame;
import ai.foresignal.raster.FrameEncoder;
import ai.foresignal.raster.GridDef;
import ai.foresignal.raster.render.CompiledStyle;
import ai.foresignal.raster.render.IndexMap;
import ai.foresignal.raster.render.StyleDefinition;
import ai.foresignal.raster.render.StyleRegistry;
import ai.foresignal.raster.render.TileRenderer;
import ai.foresignal.source.adapters.gfs.GfsAdapter;
import ai.foresignal.source.spi.DecodedBands;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.testsupport.raster.ModelFixtures;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Wind arithmetic of prompt 051, without Spring: barb properties and lattice sampling, step lists, the
 * {@code derive: speed} style on a u/v frame, timeline helpers and the engine's settings.
 */
class WindUnitsTest {

    static final ProductSpec GFS = new GfsAdapter(new ModelRunIngest(null, null), "").descriptor().products().get(0);

    @Test
    @DisplayName("fromDeg is where the wind blows from; barbKt rounds knots to 5; speedMs to 0.1")
    void barbs() {
        assertThat(VectorTileService.fromDeg(10, 0)).as("westerly").isEqualTo(270);
        assertThat(VectorTileService.fromDeg(-10, 0)).as("easterly").isEqualTo(90);
        assertThat(VectorTileService.fromDeg(0, 10)).as("southerly").isEqualTo(180);
        assertThat(VectorTileService.fromDeg(0, -10)).as("northerly").isZero();
        assertThat(VectorTileService.fromDeg(-3, -4)).isEqualTo(37);
        assertThat(VectorTileService.fromDeg(-0.0001, -10)).as("359.99 rounds to 0, never 360").isZero();
        assertThat(VectorTileService.barbKt(0)).isZero();
        assertThat(VectorTileService.barbKt(1.2)).as("2.3 kt").isZero();
        assertThat(VectorTileService.barbKt(1.4)).as("2.7 kt").isEqualTo(5);
        assertThat(VectorTileService.barbKt(10)).as("19.4 kt").isEqualTo(20);
        assertThat(VectorTileService.barbKt(25.7)).as("50.0 kt").isEqualTo(50);
        VectorTileService.Barb b = VectorTileService.barb(1, 2, 3.04, 4.04);
        assertThat(b.speedMs()).isEqualTo(5.1);
        assertThat(b.x()).isEqualTo(1);
    }

    @Test
    @DisplayName("the lattice is 8 × 8 points per tile, bilinear over the u/v bands, and skips missing samples")
    void lattice() {
        EncodedFrame frame = FrameEncoder.encode(GFS, ModelFixtures.GLOBAL_1DEG,
                DecodedBands.physical(fill(360 * 181, 10), fill(360 * 181, 0)), false);
        ElgCodec.Decoded decoded = new ElgCodec.Decoded(frame.header(), frame.decoded());
        GridDef grid = GridDef.of(ModelFixtures.GLOBAL_1DEG);
        List<VectorTileService.Barb> barbs = VectorTileService.sample(GFS, decoded, grid, 1, 0, 0);
        assertThat(barbs).hasSize(64).allSatisfy(b -> {
            assertThat(b.speedMs()).isEqualTo(10.0);
            assertThat(b.fromDeg()).isEqualTo(270);
        });
        float[] u = fill(360 * 181, Float.NaN);
        EncodedFrame missing = FrameEncoder.encode(GFS, ModelFixtures.GLOBAL_1DEG,
                DecodedBands.physical(u, fill(360 * 181, 0)), false);
        assertThat(VectorTileService.sample(GFS, new ElgCodec.Decoded(missing.header(), missing.decoded()), grid, 1, 0, 0))
                .as("nodata everywhere").isEmpty();
        assertThat(VectorTileService.hasWind(GFS)).isTrue();
        ProductSpec gust = new GfsAdapter(new ModelRunIngest(null, null), "").descriptor().products().get(1);
        assertThat(VectorTileService.hasWind(gust)).isFalse();
        EncodedFrame scalar = FrameEncoder.encode(gust, ModelFixtures.GLOBAL_1DEG,
                DecodedBands.physical(fill(360 * 181, 3)), false);
        assertThatThrownBy(() -> VectorTileService.sample(gust, new ElgCodec.Decoded(scalar.header(), scalar.decoded()),
                grid, 0, 0, 0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("wind-speed@1 derives √(u² + v²) from a two-band frame; a one-band frame uses its value")
    void speedStyle() {
        StyleRegistry styles = new StyleRegistry();
        StyleDefinition windSpeed = styles.resolve("wind-speed@1").style();
        assertThat(windSpeed.derivesSpeed()).isTrue();
        assertThat(windSpeed.stops()).hasSize(6);
        assertThat(windSpeed.stops().get(5).value()).isEqualTo(30.0);
        assertThat(windSpeed.toMap(true)).containsEntry("derive", "speed");
        CompiledStyle compiled = styles.compiled(windSpeed, GFS);
        EncodedFrame frame = FrameEncoder.encode(GFS, ModelFixtures.GLOBAL_1DEG,
                DecodedBands.physical(fill(360 * 181, 18), fill(360 * 181, -24)), false);
        GridDef grid = GridDef.of(ModelFixtures.GLOBAL_1DEG);
        for (boolean bilinear : new boolean[] {false, true}) {
            IndexMap map = IndexMap.build(grid, 2, 2, (px, py, out) -> {
                out[0] = 10.3 + px;
                out[1] = 40.6 - py;
            }, bilinear);
            TileRenderer.Drawn drawn = TileRenderer.draw(new ElgCodec.Decoded(frame.header(), frame.decoded()), compiled,
                    map);
            assertThat(drawn.pixels()[0] & 0xFF).as("speed 30 m/s, the top of the ramp (bilinear " + bilinear + ")")
                    .isEqualTo(compiled.indexOfValue(30));
            assertThat(drawn.empty()).isFalse();
        }
        assertThatThrownBy(() -> new StyleDefinition("x", 1, StyleDefinition.CATEGORICAL, null, null,
                List.of(new StyleDefinition.Category(1, "#ffffff", "one")), null, null, null, null, "speed"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new StyleDefinition("x", 1, StyleDefinition.CONTINUOUS, null,
                List.of(new StyleDefinition.Stop(0, "#ffffff", null)), null, null, null, null, null, "direction"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(styles.resolve("qpe@1").style().units()).isEqualTo("mm");
    }

    @Test
    @DisplayName("step lists: N and A-B/S items, sorted and distinct, bounded")
    void steps() {
        assertThat(ForecastSteps.parse(List.of("0-48/3", "54-120/6"))).hasSize(29).startsWith(0, 3).endsWith(114, 120);
        assertThat(ForecastSteps.parse(List.of("6, 0", "3", "3"))).containsExactly(0, 3, 6);
        assertThat(ForecastSteps.parse(List.of("0-18/1"))).hasSize(19);
        for (List<String> bad : List.of(List.<String>of(), List.of("x"), List.of("10-5/1"), List.of("0-6/0"),
                List.of("385"), List.of("0-384/1"))) {
            assertThatThrownBy(() -> ForecastSteps.parse(bad)).as(bad.toString()).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    @DisplayName("engine settings: params override defaults; a bad cadence or template is an adapter error")
    void settings() throws Exception {
        ModelRunIngest.Settings defaults = ModelRunIngest.settings(GfsAdapter.MODEL, null);
        assertThat(defaults.template()).isEqualTo(GfsAdapter.DEFAULT_TEMPLATE);
        assertThat(defaults.steps()).hasSize(29);
        assertThat(defaults.runEvery()).isEqualTo(Duration.ofHours(6));
        assertThat(defaults.availabilityLag()).isEqualTo(Duration.ofMinutes(210));
        ModelRunIngest.Settings custom = ModelRunIngest.settings(GfsAdapter.MODEL, ai.foresignal.source.spi.ValidatedParams
                .of(Map.of(ModelRunIngest.PATH_TEMPLATE, "v17/{yyyyMMdd}/{HH}/f{FFF}", ModelRunIngest.STEPS,
                        List.of("0-12/6"), ModelRunIngest.RUN_EVERY, Duration.ofHours(12),
                        ModelRunIngest.AVAILABILITY_LAG, Duration.ofHours(4))));
        assertThat(custom.steps()).containsExactly(0, 6, 12);
        assertThat(custom.runEvery()).isEqualTo(Duration.ofHours(12));
        assertThatThrownBy(() -> ModelRunIngest.settings(GfsAdapter.MODEL, ai.foresignal.source.spi.ValidatedParams
                .of(Map.of(ModelRunIngest.RUN_EVERY, Duration.ofHours(5))))).hasMessageContaining("divide 24");
        assertThatThrownBy(() -> ModelRunIngest.settings(GfsAdapter.MODEL, ai.foresignal.source.spi.ValidatedParams
                .of(Map.of(ModelRunIngest.PATH_TEMPLATE, "gfs/{HH}")))).hasMessageContaining("pathTemplate");
        ModelRunIngest ingest = new ModelRunIngest(null, null);
        assertThat(ingest.runCadence("gfs")).isEmpty();
        ingest.registerDefault("gfs", Duration.ofHours(6));
        ingest.registerDefault("gfs", Duration.ofHours(1));
        assertThat(ingest.runCadence("gfs")).contains(Duration.ofHours(6));
    }

    @Test
    @DisplayName("timeline helpers: frames at or before a tick within maxGap; external time formats")
    void timelineHelpers() {
        Instant t0 = Instant.parse("2026-10-12T13:00:00Z");
        List<TimelineService.Candidate> candidates = List.of(new TimelineService.Candidate(t0, null, "a"),
                new TimelineService.Candidate(t0.plusSeconds(600), null, "b"));
        assertThat(TimelineService.choose(candidates, t0.plusSeconds(900), Duration.ofMinutes(6)).template())
                .isEqualTo("b");
        assertThat(TimelineService.choose(candidates, t0.plusSeconds(1200), Duration.ofMinutes(6))).isNull();
        assertThat(TimelineService.choose(candidates, t0.minusSeconds(1), null)).isNull();
        assertThat(TimelineService.formatTime(t0, "YYYY-MM-DD")).isEqualTo("2026-10-12");
        assertThat(TimelineService.formatTime(t0.plusMillis(500), "YYYY-MM-DDThh:mm:ssZ")).isEqualTo("2026-10-12T13:00:00Z");
        assertThat(TimelineService.maxGap(GFS)).isEqualTo(Duration.ofHours(6));
    }

    static float[] fill(int n, float v) {
        float[] a = new float[n];
        java.util.Arrays.fill(a, v);
        return a;
    }
}
