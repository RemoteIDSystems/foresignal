package ai.foresignal.source.adapters.wx;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.derive.DefaultDerivedContext;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.testsupport.raster.SampleFixtures;
import ai.foresignal.testsupport.weather.FakeDerived;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code wx-areas} (prompt 052; Weather-Imagery-Design §7.2): thresholds, the quantized area, empty → null
 * geometry, unknown keeps the previous values, and tombstones for deleted places.
 */
class WxAreasTest {

    static final Instant T = Instant.parse("2026-10-10T14:00:00Z");

    private FakeDerived world() {
        FakeDerived world = new FakeDerived(T.plusSeconds(30));
        world.addPlace("home", "Home", -98.0, 30.5, 30, 1);
        return world;
    }

    private static ValidatedParams params(String... areas) {
        return ValidatedParams.of(Map.of(WxAreasGenerator.AREAS_PARAM, List.of(areas)));
    }

    private List<NormalizedRecord> reconcile(FakeDerived world, WxAreasGenerator g, ValidatedParams p,
            List<HeadSnapshot> heads) {
        world.seed(heads);
        DefaultDerivedContext ctx = world.context(g.descriptor(), p);
        g.reconcile(heads, ctx);
        return world.collect(ctx);
    }

    @Test
    @DisplayName("refl:50:home: the area of the 52 dBZ disc to 2 significant figures, max, coverage and a polygon")
    void thresholdArea() {
        FakeDerived world = world();
        world.addFrame("mrms-reflectivity", SampleFixtures.codes(SampleFixtures.reflectivity("mrms-reflectivity"),
                SampleFixtures.TEXAS, SampleFixtures.echoes(SampleFixtures.TEXAS, -98.0, 30.5, 5, 6, 52),
                SampleFixtures.row("mrms-reflectivity", T, null, null)));
        WxAreasGenerator g = new WxAreasGenerator();
        List<NormalizedRecord> out = reconcile(world, g, params("refl:50:home", "refl:60:home"), List.of());
        NormalizedRecord r50 = out.stream().filter(r -> r.externalId().equals("refl:50:home")).findFirst().orElseThrow();
        assertThat(r50.kind()).isEqualTo("wx-area");
        assertThat(r50.payload()).containsEntry("field", "refl").containsEntry("threshold", 50L)
                .containsEntry("aoi", "home").containsEntry("status", "ok").containsEntry("maxValue", 52.0)
                .containsEntry("coveragePct", 100).containsEntry("automated", true).containsEntry("notAWarning", true);
        double area = ((Number) r50.payload().get("areaKm2")).doubleValue();
        // A 6 km disc is about 113 km²; cell-center membership and 2 significant figures keep it near that.
        assertThat(area).isBetween(100.0, 125.0);
        assertThat(area).isEqualTo(WxRound.sig2(area));
        assertThat(r50.geometry()).isNotNull();
        assertThat(r50.geometry().getDimension()).isEqualTo(2);
        NormalizedRecord r60 = out.stream().filter(r -> r.externalId().equals("refl:60:home")).findFirst().orElseThrow();
        assertThat(r60.payload()).containsEntry("areaKm2", 0.0);
        assertThat(r60.geometry()).as("nothing reaches the threshold").isNull();
    }

    @Test
    @DisplayName("defaults, parameter checks and the area id")
    void parameters() {
        WxAreasGenerator g = new WxAreasGenerator();
        assertThat(WxAreasGenerator.definitions(ValidatedParams.empty())).extracting(WxAreasGenerator.AreaDef::externalId)
                .containsExactly("refl:50:aoi", "mesh:25:aoi");
        assertThat(WxAreasGenerator.AreaDef.parse("precip:2.5:home")).hasValueSatisfying(d -> {
            assertThat(d.externalId()).isEqualTo("precip:2.5:home");
            assertThat(d.productId()).isEqualTo("mrms-precip-rate");
        });
        assertThat(WxAreasGenerator.AreaDef.parse("hail:25:aoi")).isEmpty();
        assertThat(g.checkParams(params("snow:1:aoi"))).containsKey(WxAreasGenerator.AREAS_PARAM);
        assertThat(g.checkParams(ValidatedParams.of(Map.of(WxAreasGenerator.INTERVAL_PARAM, Duration.ofMinutes(3)))))
                .containsKey(WxAreasGenerator.INTERVAL_PARAM);
        assertThat(g.checkParams(params("refl:50:aoi"))).isEmpty();
        assertThat(g.inputs(ValidatedParams.empty()).tick()).isEqualTo(Duration.ofMinutes(8));
    }

    @Test
    @DisplayName("stale input keeps the previous area with asOf; a deleted place and a removed area are tombstoned")
    void staleAndTombstones() {
        FakeDerived world = world();
        world.addFrame("mrms-reflectivity", SampleFixtures.codes(SampleFixtures.reflectivity("mrms-reflectivity"),
                SampleFixtures.TEXAS, SampleFixtures.echoes(SampleFixtures.TEXAS, -98.0, 30.5, 5, 6, 52),
                SampleFixtures.row("mrms-reflectivity", T, null, null)));
        WxAreasGenerator g = new WxAreasGenerator();
        ValidatedParams p = params("refl:50:home");
        NormalizedRecord ok = reconcile(world, g, p, List.of()).get(0);
        world.clock.setInstant(T.plus(Duration.ofMinutes(9)));
        DefaultDerivedContext tick = world.context(g.descriptor(), p);
        g.onTick(world.clock.instant(), tick);
        NormalizedRecord stale = world.collect(tick).get(0);
        assertThat(stale.payload()).containsEntry("status", "stale").containsEntry("asOf", T.toString())
                .containsEntry("areaKm2", ok.payload().get("areaKm2"));
        assertThat(stale.geometry()).isEqualTo(ok.geometry());

        world.places.clear();
        DefaultDerivedContext gone = world.context(g.descriptor(), p);
        g.onTick(world.clock.instant(), gone);
        assertThat(world.collect(gone)).singleElement().satisfies(r -> assertThat(r.tombstone()).isTrue());

        HeadSnapshot removed = new HeadSnapshot("wx-areas", "mesh:25:aoi", "wx-area", 1, 1, T, false,
                Map.of("field", "mesh", "status", "ok"), null);
        List<NormalizedRecord> rec = reconcile(world(), new WxAreasGenerator(), params("refl:50:aoi"), List.of(removed));
        assertThat(rec).anyMatch(r -> r.tombstone() && r.externalId().equals("mesh:25:aoi"));
    }
}
