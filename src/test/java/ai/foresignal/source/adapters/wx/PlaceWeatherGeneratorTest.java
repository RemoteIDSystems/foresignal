package ai.foresignal.source.adapters.wx;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.derive.DefaultDerivedContext;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.derive.RasterFrameEvent;
import ai.foresignal.raster.sample.FrameView;
import ai.foresignal.raster.sample.PlaceGridIndex;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.spi.FlashBatch;
import ai.foresignal.source.spi.FlashSink;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.testsupport.raster.SampleFixtures;
import ai.foresignal.testsupport.weather.FakeDerived;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

/**
 * {@code wx-places} without a database (prompt 052; Weather-Imagery-Design §7.2): payload shape, the 999
 * sentinel, rounding, rolling minimums, the verified-clear helpers, metric keys omitted before the first data,
 * and "unknown keeps the previous values".
 */
class PlaceWeatherGeneratorTest {

    static final Instant T = Instant.parse("2026-10-10T14:00:00Z");
    static final double LON = -98.0;
    static final double LAT = 30.5;

    FakeDerived world;
    WxPlacesGenerator generator;

    @BeforeEach
    void setUp() {
        world = new FakeDerived(T.plusSeconds(30));
        world.addPlace("home", "Home", LON, LAT, 20, 1);
        generator = new WxPlacesGenerator();
        PlaceGridIndex.clearCache();
    }

    private FrameView reflectivity(Instant t, int[] codes) {
        return SampleFixtures.codes(SampleFixtures.reflectivity(WxPlacesGenerator.REFLECTIVITY), SampleFixtures.TEXAS,
                codes, SampleFixtures.row(WxPlacesGenerator.REFLECTIVITY, t, null, null));
    }

    private List<NormalizedRecord> run(java.util.function.Consumer<DefaultDerivedContext> hook) {
        DefaultDerivedContext ctx = world.context(generator.descriptor(), ValidatedParams.empty());
        hook.accept(ctx);
        return world.collect(ctx);
    }

    private List<NormalizedRecord> frame(Instant t) {
        return run(ctx -> generator.onFrame(new RasterFrameEvent(WxPlacesGenerator.REFLECTIVITY, "f", "complete", t, null,
                false, t), ctx));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> section(NormalizedRecord r, String name) {
        return (Map<String, Object>) r.payload().get(name);
    }

    @Test
    @DisplayName("before any data every section carries its status only: metric keys omitted, never null")
    void statusOnlyBeforeData() {
        List<NormalizedRecord> out = run(ctx -> generator.reconcile(List.of(), ctx));
        assertThat(out).hasSize(1);
        NormalizedRecord r = out.get(0);
        assertThat(r.externalId()).isEqualTo("place:home");
        assertThat(r.kind()).isEqualTo("place-weather");
        assertThat(r.geometry()).as("a place position is sensitive").isNull();
        assertThat(r.payload()).containsEntry("place", "Home").containsEntry("radiusKm", 20L)
                .containsEntry("automated", true).containsEntry("notAWarning", true)
                .containsKeys("radar", "hail", "precip", "lightning", "cell", "smoke", "forecast");
        Map<String, Object> radar = section(r, "radar");
        assertThat(radar).containsEntry("status", "unavailable").doesNotContainKeys("maxDbzInRadius", "nearest50Km",
                "asOf");
        assertThat(radar).containsEntry("verifiedNearest50Km10m", 0);
        assertThat(section(r, "hail")).containsOnlyKeys("status", "statusText");
        for (Object v : radar.values()) {
            assertThat(v).isNotNull();
        }
    }

    @Test
    @DisplayName("a core 12 km east: distances rounded, 999 for none, verified helper equals the metric while ok")
    void radarMetrics() {
        world.addFrame(WxPlacesGenerator.REFLECTIVITY, reflectivity(T, SampleFixtures.echoes(SampleFixtures.TEXAS, LON,
                LAT, 12, 3, 52)));
        run(ctx -> generator.reconcile(List.of(), ctx));
        Map<String, Object> radar = section(world.emitted.get(world.emitted.size() - 1), "radar");
        assertThat(radar).containsEntry("status", "ok").doesNotContainKey("asOf");
        assertThat((Double) radar.get("nearest40Km")).isBetween(8.0, 10.0);
        assertThat((Double) radar.get("nearest50Km")).isBetween(8.0, 10.0);
        double n50 = (Double) radar.get("nearest50Km");
        assertThat(n50 * 2).as("0.5 km steps below 10 km").isEqualTo(Math.rint(n50 * 2));
        assertThat(radar).containsEntry("nearest60Km", 999.0).containsEntry("nearest50Km10m", n50)
                .containsEntry("verifiedNearest50Km10m", n50);
        assertThat(radar).containsEntry("maxDbzInRadius", 52.0);
    }

    @Test
    @DisplayName("the 10-minute rolling minimum keeps the nearest core while it moves away")
    void rollingMinimum() {
        world.addFrame(WxPlacesGenerator.REFLECTIVITY, reflectivity(T, SampleFixtures.echoes(SampleFixtures.TEXAS, LON,
                LAT, 12, 3, 52)));
        run(ctx -> generator.reconcile(List.of(), ctx));
        double first = (Double) section(world.emitted.get(world.emitted.size() - 1), "radar").get("nearest50Km");
        Instant t2 = T.plus(Duration.ofMinutes(4));
        world.clock.setInstant(t2.plusSeconds(30));
        world.addFrame(WxPlacesGenerator.REFLECTIVITY, reflectivity(t2, SampleFixtures.echoes(SampleFixtures.TEXAS, LON,
                LAT, 40, 3, 52)));
        Map<String, Object> radar = section(frame(t2).get(0), "radar");
        assertThat((Double) radar.get("nearest50Km")).isGreaterThan(30.0);
        assertThat(radar).containsEntry("nearest50Km10m", first);
        Instant t3 = T.plus(Duration.ofMinutes(12));
        world.clock.setInstant(t3.plusSeconds(30));
        world.addFrame(WxPlacesGenerator.REFLECTIVITY, reflectivity(t3, SampleFixtures.echoes(SampleFixtures.TEXAS, LON,
                LAT, 40, 3, 52)));
        Map<String, Object> later = section(frame(t3).get(0), "radar");
        assertThat((Double) later.get("nearest50Km10m")).isGreaterThan(30.0);
    }

    @Test
    @DisplayName("stale input keeps the values, sets stale with a fixed asOf, and zeroes the verified helper")
    void staleKeepsValues() {
        world.addFrame(WxPlacesGenerator.REFLECTIVITY, reflectivity(T, SampleFixtures.echoes(SampleFixtures.TEXAS, LON,
                LAT, 12, 3, 52)));
        run(ctx -> generator.reconcile(List.of(), ctx));
        Map<String, Object> ok = section(world.emitted.get(world.emitted.size() - 1), "radar");
        world.clock.setInstant(T.plus(Duration.ofMinutes(7)));
        List<NormalizedRecord> out = run(ctx -> generator.onTick(world.clock.instant(), ctx));
        Map<String, Object> stale = section(out.get(0), "radar");
        assertThat(stale).containsEntry("status", "stale").containsEntry("asOf", T.toString())
                .containsEntry("nearest50Km", ok.get("nearest50Km")).containsEntry("verifiedNearest50Km10m", 0)
                .containsKey("statusText");
        world.clock.setInstant(T.plus(Duration.ofMinutes(9)));
        assertThat(run(ctx -> generator.onTick(world.clock.instant(), ctx))).as("asOf stays fixed: nothing changed")
                .isEmpty();
    }

    @Test
    @DisplayName("a no-coverage frame does not clear the radar section")
    void noCoverageKeepsValues() {
        world.addFrame(WxPlacesGenerator.REFLECTIVITY, reflectivity(T, SampleFixtures.echoes(SampleFixtures.TEXAS, LON,
                LAT, 12, 3, 52)));
        run(ctx -> generator.reconcile(List.of(), ctx));
        Instant t2 = T.plus(Duration.ofMinutes(2));
        world.clock.setInstant(t2.plusSeconds(30));
        world.addFrame(WxPlacesGenerator.REFLECTIVITY, reflectivity(t2, SampleFixtures.noCoverage(SampleFixtures.TEXAS)));
        Map<String, Object> radar = section(frame(t2).get(0), "radar");
        assertThat(radar).containsEntry("status", "no-coverage").containsEntry("asOf", T.toString())
                .containsEntry("verifiedNearest50Km10m", 0).containsKey("nearest50Km");
        assertThat((Double) radar.get("nearest50Km")).isLessThan(20.0);
    }

    @Test
    @DisplayName("identical frames produce no new emits")
    void identicalFramesNoEmit() {
        int[] codes = SampleFixtures.echoes(SampleFixtures.TEXAS, LON, LAT, 12, 3, 52);
        world.addFrame(WxPlacesGenerator.REFLECTIVITY, reflectivity(T, codes));
        run(ctx -> generator.reconcile(List.of(), ctx));
        for (int i = 1; i <= 15; i++) {
            Instant t = T.plus(Duration.ofMinutes(2L * i));
            world.clock.setInstant(t.plusSeconds(30));
            world.addFrame(WxPlacesGenerator.REFLECTIVITY, reflectivity(t, codes));
            assertThat(frame(t)).as("frame %d", i).isEmpty();
            assertThat(run(ctx -> generator.onTick(world.clock.instant(), ctx))).isEmpty();
        }
    }

    @Test
    @DisplayName("lightning: nearest in 15 and 30 min, count within 15 km; stale after 2 min; helper zero when stale")
    void lightning() {
        ListFlashSink sink = new ListFlashSink();
        world.flashes = sink;
        sink.add(T.minusSeconds(60), LON + 0.06, LAT);
        sink.add(T.minusSeconds(20 * 60), LON + 0.03, LAT);
        sink.newest = T;
        run(ctx -> generator.reconcile(List.of(), ctx));
        Map<String, Object> l = section(world.emitted.get(world.emitted.size() - 1), "lightning");
        assertThat(l).containsEntry("status", "ok");
        assertThat((Double) l.get("nearestKm15m")).isBetween(5.0, 6.5);
        assertThat((Double) l.get("nearestKm30m")).isBetween(2.5, 3.5);
        assertThat(l).containsEntry("count15km15m", 1).containsEntry("verifiedNearestKm30m", l.get("nearestKm30m"));
        world.clock.setInstant(T.plus(Duration.ofMinutes(3)));
        Map<String, Object> stale = section(run(ctx -> generator.onTick(world.clock.instant(), ctx)).get(0), "lightning");
        assertThat(stale).containsEntry("status", "stale").containsEntry("asOf", T.toString())
                .containsEntry("nearestKm15m", l.get("nearestKm15m")).containsEntry("verifiedNearestKm30m", 0);
        world.statuses.put(WxPlacesGenerator.GLM, HealthStatus.FAILING);
        Map<String, Object> failing = section(run(ctx -> generator.onTick(world.clock.instant(), ctx)).get(0), "lightning");
        assertThat(failing).containsEntry("status", "unavailable");
    }

    @Test
    @DisplayName("no lightning within 100 km is 999, not null")
    void noLightning() {
        ListFlashSink sink = new ListFlashSink();
        sink.newest = T;
        world.flashes = sink;
        run(ctx -> generator.reconcile(List.of(), ctx));
        Map<String, Object> l = section(world.emitted.get(0), "lightning");
        assertThat(l).containsEntry("nearestKm15m", 999.0).containsEntry("nearestKm30m", 999.0)
                .containsEntry("count15km15m", 0);
    }

    @Test
    @DisplayName("a deleted place is tombstoned; reconcile tombstones heads of deleted places and restores sections")
    void deletedPlaces() {
        run(ctx -> generator.reconcile(List.of(), ctx));
        world.places.clear();
        List<NormalizedRecord> out = run(ctx -> generator.onTick(world.clock.instant(), ctx));
        assertThat(out).hasSize(1);
        assertThat(out.get(0).tombstone()).isTrue();
        assertThat(out.get(0).externalId()).isEqualTo("place:home");

        WxPlacesGenerator fresh = new WxPlacesGenerator();
        FakeDerived other = new FakeDerived(T);
        other.addPlace("work", "Work", LON, LAT, 10, 1);
        Map<String, Object> stored = Map.of("place", "Work", "radiusKm", 10,
                "radar", Map.of("status", "stale", "asOf", T.minusSeconds(600).toString(), "nearest50Km", 14,
                        "nearest50Km10m", 12, "verifiedNearest50Km10m", 0));
        List<HeadSnapshot> heads = List.of(
                new HeadSnapshot("wx-places", "place:gone", "place-weather", 1, 1, T, false, Map.of("place", "Gone"), null),
                new HeadSnapshot("wx-places", "place:work", "place-weather", 2, 4, T, false, stored, null));
        other.seed(heads);
        DefaultDerivedContext ctx = other.context(fresh.descriptor(), ValidatedParams.empty());
        fresh.reconcile(heads, ctx);
        List<NormalizedRecord> emitted = other.collect(ctx);
        assertThat(emitted).anyMatch(r -> r.tombstone() && r.externalId().equals("place:gone"));
        NormalizedRecord work = emitted.stream().filter(r -> r.externalId().equals("place:work")).findFirst().orElseThrow();
        @SuppressWarnings("unchecked")
        Map<String, Object> radar = (Map<String, Object>) work.payload().get("radar");
        assertThat(radar).as("no reflectivity input: unavailable, the old values and asOf kept")
                .containsEntry("status", "unavailable").containsEntry("asOf", T.minusSeconds(600).toString())
                .containsEntry("nearest50Km", 14).containsEntry("verifiedNearest50Km10m", 0);
    }

    @Test
    @DisplayName("cell ETA: a 55 dBZ core 30 km west moving east at 60 km/h is about 10 min out; none is 999")
    void cellEta() {
        world.statuses.put(WxPlacesGenerator.CELLS, HealthStatus.HEALTHY);
        world.addFrame(WxPlacesGenerator.REFLECTIVITY, reflectivity(T, SampleFixtures.echoes(SampleFixtures.TEXAS, LON,
                LAT, 60, 1, 30)));
        double cellLon = LON - 30 / (111.32 * Math.cos(Math.toRadians(LAT)));
        Geometry cell = Geo.point(cellLon, LAT).buffer(0.02);
        world.heads.put("wx-cells", new ArrayList<>(List.of(new HeadSnapshot("wx-cells", "c-01TEST", "radar-cell", 1, 1, T,
                false, Map.of("maxDbz", 55.0, "motion", Map.of("speedKmh", 60, "bearingDeg", 90, "method", "centroid-v1")),
                cell))));
        run(ctx -> generator.reconcile(List.of(), ctx));
        Map<String, Object> c = section(world.emitted.get(world.emitted.size() - 1), "cell");
        assertThat(c).containsEntry("status", "ok").containsEntry("eta50Min", 10).containsEntry("etaCellId", "c-01TEST")
                .containsEntry("etaSpeedKmh", 60L).containsEntry("etaBearingDeg", 90);
        world.heads.get("wx-cells").clear();
        Map<String, Object> none = section(run(ctx -> generator.onChanges(new ai.foresignal.stream.ChangeBatch("wx-cells",
                1, T, 1, 1, List.of()), ctx)).get(0), "cell");
        assertThat(none).containsEntry("eta50Min", 999).doesNotContainKey("etaCellId");
        world.statuses.put(WxPlacesGenerator.CELLS, HealthStatus.DISABLED);
        Map<String, Object> off = section(run(ctx -> generator.onTick(world.clock.instant(), ctx)).get(0), "cell");
        assertThat(off).containsEntry("status", "unavailable");
    }

    @Test
    @DisplayName("rounding helpers")
    void rounding() {
        assertThat(WxRound.km(Double.NaN)).isEqualTo(999);
        assertThat(WxRound.km(100.4)).isEqualTo(999);
        assertThat(WxRound.km(7.3)).isEqualTo(7.5);
        assertThat(WxRound.km(12.4)).isEqualTo(12);
        assertThat(WxRound.dbz(52.3)).isEqualTo(52.5);
        assertThat(WxRound.etaMin(11)).isEqualTo(15);
        assertThat(WxRound.etaMin(0)).isEqualTo(0);
        assertThat(WxRound.etaMin(Double.NaN)).isEqualTo(999);
        assertThat(WxRound.sig2(338)).isEqualTo(340);
        assertThat(WxRound.sig2(0.0123)).isEqualTo(0.012);
        assertThat(WxRound.bearing10(356)).isEqualTo(0);
        assertThat(WxRound.speedKmh(43)).isEqualTo(45);
    }

    /** An in-memory flash sink. */
    static final class ListFlashSink implements FlashSink {

        final List<double[]> flashes = new ArrayList<>();
        Instant newest;

        void add(Instant t, double lon, double lat) {
            flashes.add(new double[] {t.toEpochMilli(), lon, lat});
        }

        @Override
        public void append(String satellite, FlashBatch batch, Instant fileTime) {
            newest = fileTime;
        }

        @Override
        public FlashBatch between(Instant from, Instant to) {
            return FlashBatch.EMPTY;
        }

        @Override
        public FlashBatch flashesWithin(Point point, double radiusKm, Instant since) {
            List<double[]> in = new ArrayList<>();
            for (double[] f : flashes) {
                if (f[0] >= since.toEpochMilli() && Geo.haversineKm(point.getY(), point.getX(), f[2], f[1]) <= radiusKm) {
                    in.add(f);
                }
            }
            long[] t = new long[in.size()];
            float[] la = new float[in.size()];
            float[] lo = new float[in.size()];
            float[] e = new float[in.size()];
            for (int i = 0; i < in.size(); i++) {
                t[i] = (long) in.get(i)[0];
                lo[i] = (float) in.get(i)[1];
                la[i] = (float) in.get(i)[2];
            }
            return new FlashBatch(t, la, lo, e);
        }

        @Override
        public int countWithin(Geometry geometry, Instant since) {
            return 0;
        }

        @Override
        public OptionalDouble nearestKm(Point point, Instant since) {
            double best = Double.POSITIVE_INFINITY;
            for (double[] f : flashes) {
                if (f[0] >= since.toEpochMilli()) {
                    best = Math.min(best, Geo.haversineKm(point.getY(), point.getX(), f[2], f[1]));
                }
            }
            return Double.isInfinite(best) ? OptionalDouble.empty() : OptionalDouble.of(best);
        }

        @Override
        public Optional<Instant> newestFlashFileTime() {
            return Optional.ofNullable(newest);
        }
    }
}
