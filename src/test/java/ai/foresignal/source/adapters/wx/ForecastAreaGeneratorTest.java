package ai.foresignal.source.adapters.wx;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.derive.DefaultDerivedContext;
import ai.foresignal.raster.GridDef;
import ai.foresignal.raster.model.EcmwfDefinition;
import ai.foresignal.raster.sample.FrameView;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.testsupport.raster.SampleFixtures;
import ai.foresignal.testsupport.weather.FakeDerived;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code wx-forecast-areas} (prompt 105 §2, post-review): thresholding, the union of the steps,
 * {@code validFrom}/{@code validUntil}, emit only on a change of the significant payload ({@code run}
 * excluded) or of the geometry by more than 10%, the tombstone when a newer run drops the exceedance, the
 * minimum as the peak of a {@code <=} threshold, per-model staleness and the ECMWF attribution.
 */
class ForecastAreaGeneratorTest {

    static final Instant RUN = Instant.parse("2026-10-15T00:00:00Z");
    static final Instant NOW = RUN.plus(Duration.ofHours(2));
    static final String GUST = "ifs-gust";
    static final String T2M = "ifs-t2m";

    private static ValidatedParams params(String... items) {
        return ValidatedParams.of(Map.of(WxForecastAreasGenerator.THRESHOLDS_PARAM, List.of(items)));
    }

    /** A field of {@code background} with a disc of {@code value} of radius {@code km} around (lon, lat). */
    private static float[] disc(double lon, double lat, double km, float value, float background) {
        GridDef g = GridDef.of(SampleFixtures.TEXAS);
        int nx = SampleFixtures.TEXAS.nx();
        int ny = SampleFixtures.TEXAS.ny();
        float[] out = new float[nx * ny];
        for (int r = 0; r < ny; r++) {
            for (int c = 0; c < nx; c++) {
                double[] ll = g.indexToLonLat(c, r);
                out[r * nx + c] = Geo.haversineKm(lat, lon, ll[1], ll[0]) <= km ? value : background;
            }
        }
        return out;
    }

    private static FrameView step(String productId, Instant run, int hours, float[] values) {
        ProductSpec spec = SampleFixtures.gusts(productId);
        return SampleFixtures.physical(spec, SampleFixtures.TEXAS,
                SampleFixtures.row(productId, run.plus(Duration.ofHours(hours)), run, hours), values);
    }

    private static FakeDerived world() {
        FakeDerived world = new FakeDerived(NOW);
        world.addPlace("home", "Home", -98.0, 30.5, 30, 1);
        return world;
    }

    private static List<NormalizedRecord> reconcile(FakeDerived world, WxForecastAreasGenerator g, ValidatedParams p) {
        DefaultDerivedContext ctx = world.context(g.descriptor(), p);
        g.reconcile(List.of(), ctx);
        return world.collect(ctx);
    }

    private static List<NormalizedRecord> tick(FakeDerived world, WxForecastAreasGenerator g, ValidatedParams p) {
        DefaultDerivedContext ctx = world.context(g.descriptor(), p);
        g.onTick(world.clock.instant(), ctx);
        return world.collect(ctx);
    }

    @Test
    @DisplayName("the exceeding steps union into one area with validFrom, validUntil, peak, run and the ECMWF attribution")
    void thresholdAndUnion() {
        FakeDerived world = world();
        world.addRun(GUST, RUN, List.of(
                step(GUST, RUN, 3, disc(-98.1, 30.5, 15, 30, 5)),
                step(GUST, RUN, 4, disc(-97.9, 30.5, 15, 28, 5)),
                step(GUST, RUN, 5, disc(-98.0, 30.5, 15, 10, 5))));
        WxForecastAreasGenerator g = new WxForecastAreasGenerator();
        List<NormalizedRecord> out = reconcile(world, g, params("ifs-gust:>=:25"));

        NormalizedRecord r = out.stream().filter(x -> x.externalId().equals("ifs:gust:>=25:aoi")).findFirst()
                .orElseThrow();
        assertThat(r.kind()).isEqualTo("forecast-area");
        assertThat(r.validFrom()).isEqualTo(RUN.plus(Duration.ofHours(3)));
        assertThat(r.validUntil()).as("the last exceeding step plus the 1 h step").isEqualTo(RUN.plus(Duration.ofHours(5)));
        assertThat(r.payload()).containsEntry("status", "ok").containsEntry("peakValue", 30L)
                .containsEntry("peakAt", RUN.plus(Duration.ofHours(3)).toString())
                .containsEntry("run", RUN.toString()).containsEntry("attribution", EcmwfDefinition.CHANGES_MADE)
                .containsEntry("automated", true).containsEntry("notAWarning", true);
        assertThat(r.geometry()).isNotNull();
        // Two 15 km discs 19 km apart: the union is larger than either disc alone (about 707 km²).
        assertThat(((Number) r.payload().get("areaKm2")).doubleValue()).isGreaterThan(800);
    }

    @Test
    @DisplayName("a newer run with the same area writes nothing (run is volatile); a change of under 10% keeps the geometry")
    void emitOnlyOnChange() {
        FakeDerived world = world();
        world.addRun(GUST, RUN, List.of(step(GUST, RUN, 3, disc(-98.0, 30.5, 20, 30, 5))));
        WxForecastAreasGenerator g = new WxForecastAreasGenerator();
        ValidatedParams p = params("ifs-gust:>=:25");
        NormalizedRecord first = reconcile(world, g, p).get(0);

        Instant next = RUN.plus(Duration.ofHours(1));
        world.addRun(GUST, next, List.of(step(GUST, next, 2, disc(-98.0, 30.5, 20, 30, 5))));
        assertThat(tick(world, g, p)).as("same valid time, peak and area: no version").isEmpty();
        assertThat(g.states().get("ifs:gust:>=25:aoi").run).isEqualTo(next);

        Instant third = RUN.plus(Duration.ofHours(2));
        world.addRun(GUST, third, List.of(step(GUST, third, 1, disc(-98.0, 30.5, 20.5, 30, 5))));
        assertThat(tick(world, g, p)).as("a 5% larger disc keeps the stored geometry and area").isEmpty();
        assertThat(g.states().get("ifs:gust:>=25:aoi").geometry).isEqualTo(first.geometry());

        Instant fourth = RUN.plus(Duration.ofHours(3));
        world.addRun(GUST, fourth, List.of(step(GUST, fourth, 0, disc(-98.0, 30.5, 30, 30, 5))));
        NormalizedRecord grown = tick(world, g, p).get(0);
        assertThat(grown.geometry()).isNotEqualTo(first.geometry());
        assertThat(grown.payload()).containsEntry("run", fourth.toString());
    }

    @Test
    @DisplayName("a newer run without the exceedance tombstones the area; it does not write a record without geometry")
    void tombstoneWhenGone() {
        FakeDerived world = world();
        world.addRun(GUST, RUN, List.of(step(GUST, RUN, 3, disc(-98.0, 30.5, 20, 30, 5))));
        WxForecastAreasGenerator g = new WxForecastAreasGenerator();
        ValidatedParams p = params("ifs-gust:>=:25");
        assertThat(reconcile(world, g, p)).hasSize(1);

        Instant next = RUN.plus(Duration.ofHours(1));
        world.addRun(GUST, next, List.of(step(GUST, next, 2, disc(-98.0, 30.5, 20, 12, 5))));
        List<NormalizedRecord> out = tick(world, g, p);
        assertThat(out).singleElement().satisfies(r -> {
            assertThat(r.tombstone()).isTrue();
            assertThat(r.externalId()).isEqualTo("ifs:gust:>=25:aoi");
        });
        assertThat(tick(world, g, p)).as("nothing more while the exceedance stays away").isEmpty();
    }

    @Test
    @DisplayName("a <= threshold reports the minimum as its peak")
    void coldPeakIsTheMinimum() {
        FakeDerived world = world();
        float[] cold = disc(-98.0, 30.5, 20, 258f, 280f);
        float[] colder = disc(-98.0, 30.5, 10, 250f, 280f);
        for (int i = 0; i < cold.length; i++) {
            cold[i] = Math.min(cold[i], colder[i]);
        }
        world.addRun(T2M, RUN, List.of(step(T2M, RUN, 3, cold)));
        List<NormalizedRecord> out = reconcile(world, new WxForecastAreasGenerator(), params("ifs-t2m:<=:263.15"));
        assertThat(out).singleElement().satisfies(r -> assertThat(r.payload()).containsEntry("peakValue", 250L));
    }

    @Test
    @DisplayName("IFS stays ok for 22 h (cadence + lag + ingest), then goes stale keeping its values; GFS at 11.5 h")
    void perModelStaleness() {
        assertThat(ModelRunAge.of("ifs-gust")).isEqualTo(Duration.ofHours(22));
        assertThat(ModelRunAge.of("gfs-gust")).isEqualTo(Duration.ofMinutes(690));
        FakeDerived world = world();
        world.addRun(GUST, RUN, List.of(step(GUST, RUN, 3, disc(-98.0, 30.5, 20, 30, 5))));
        WxForecastAreasGenerator g = new WxForecastAreasGenerator();
        ValidatedParams p = params("ifs-gust:>=:25");
        NormalizedRecord ok = reconcile(world, g, p).get(0);

        world.clock.setInstant(RUN.plus(Duration.ofHours(21)));
        assertThat(tick(world, g, p)).as("13 h after the old 12 h limit IFS is still current").isEmpty();

        world.clock.setInstant(RUN.plus(Duration.ofHours(23)));
        NormalizedRecord stale = tick(world, g, p).get(0);
        assertThat(stale.payload()).containsEntry("status", "stale").containsEntry("asOf", RUN.toString())
                .containsEntry("peakValue", ok.payload().get("peakValue"))
                .containsEntry("areaKm2", ok.payload().get("areaKm2"));
        assertThat(stale.geometry()).isEqualTo(ok.geometry());
    }

    @Test
    @DisplayName("an area that never exceeded writes nothing while its input is unavailable")
    void noRecordWithoutExceedance() {
        FakeDerived world = world();
        List<NormalizedRecord> out = reconcile(world, new WxForecastAreasGenerator(), params("ifs-gust:>=:25"));
        assertThat(out).isEmpty();
        List<String> ids = new ArrayList<>();
        WxForecastAreasGenerator.definitions(ValidatedParams.empty()).forEach(d -> ids.add(d.externalId()));
        assertThat(ids).contains("ifs:gust:>=25:aoi", "gfs:t2m:<=263.15:aoi");
    }
}
