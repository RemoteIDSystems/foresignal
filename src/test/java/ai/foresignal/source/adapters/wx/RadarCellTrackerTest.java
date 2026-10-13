package ai.foresignal.source.adapters.wx;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.derive.DefaultDerivedContext;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.raster.sample.FrameView;
import ai.foresignal.raster.sample.GridWindow;
import ai.foresignal.source.spi.GridDefinition;
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
 * Storm-cell detection and tracking (prompt 052; Weather-Imagery-Design §7.2): labeling, the 3×3 opening, the
 * core split, centroid matching, least-squares motion and the ETA a place gets from it.
 */
class RadarCellTrackerTest {

    static final Instant T = Instant.parse("2026-10-10T14:00:00Z");
    static final GridDefinition GRID = GridDefinition.latLon(40, 40, -98.2, 30.3, -97.8, 30.7);

    /** Codes on GRID with filled rectangles {c0, r0, c1, r1, dbz}. */
    private static int[] grid(int[]... rects) {
        int[] codes = new int[40 * 40];
        java.util.Arrays.fill(codes, 1);
        for (int[] r : rects) {
            for (int y = r[1]; y <= r[3]; y++) {
                for (int x = r[0]; x <= r[2]; x++) {
                    codes[y * 40 + x] = SampleFixtures.dbz(r[4]);
                }
            }
        }
        return codes;
    }

    private static FrameView view(int[] codes, Instant t) {
        return SampleFixtures.codes(SampleFixtures.reflectivity(WxCellsGenerator.REFLECTIVITY), GRID, codes,
                SampleFixtures.row(WxCellsGenerator.REFLECTIVITY, t, null, null));
    }

    private static GridWindow all() {
        return new GridWindow(0, 0, 39, 39);
    }

    @Test
    @DisplayName("8-connected union-find labeling joins diagonal neighbors and separates gaps")
    void labeling() {
        boolean[] mask = new boolean[25];
        // (0,0)-(1,1) diagonal, and (4,4) apart.
        mask[0] = true;
        mask[6] = true;
        mask[24] = true;
        int[] labels = RadarCellTracker.label(mask, 5, 5);
        assertThat(labels[0]).isEqualTo(labels[6]).isNotZero();
        assertThat(labels[24]).isNotZero().isNotEqualTo(labels[0]);
        assertThat(labels[1]).isZero();
    }

    @Test
    @DisplayName("the 3×3 opening removes speckle and thin bridges but keeps solid blocks")
    void opening() {
        int[] codes = grid(new int[] {2, 2, 8, 8, 45}, new int[] {20, 20, 20, 20, 45}, new int[] {9, 5, 15, 5, 45},
                new int[] {16, 2, 22, 8, 45});
        List<RadarCellTracker.Cell> cells = RadarCellTracker.detect(view(codes, T), all());
        assertThat(cells).as("two blocks; the speckle and the one-pixel bridge are opened away").hasSize(2);
        assertThat(cells).allSatisfy(c -> assertThat(c.pixels()).hasSize(49));
        assertThat(cells).allSatisfy(c -> assertThat(c.maxDbz()).isEqualTo(45.0));
    }

    @Test
    @DisplayName("a component with two separate 50 dBZ cores is split; one core keeps one cell")
    void coreSplit() {
        int[] codes = grid(new int[] {2, 10, 30, 18, 45}, new int[] {4, 12, 8, 16, 55}, new int[] {24, 12, 28, 16, 56});
        List<RadarCellTracker.Cell> cells = RadarCellTracker.detect(view(codes, T), all());
        assertThat(cells).hasSize(2);
        assertThat(cells).extracting(RadarCellTracker.Cell::maxDbz).containsExactlyInAnyOrder(55.0, 56.0);
        int total = cells.stream().mapToInt(c -> c.pixels().length).sum();
        assertThat(total).isEqualTo(29 * 9);
        int[] single = grid(new int[] {2, 10, 30, 18, 45}, new int[] {4, 12, 8, 16, 55});
        assertThat(RadarCellTracker.detect(view(single, T), all())).hasSize(1);
    }

    @Test
    @DisplayName("matching is nearest-first, one-to-one, within 30 km per 10 min of gap")
    void matching() {
        RadarCellTracker.Track a = new RadarCellTracker.Track("c-a", T, T, -98.0, 30.5);
        RadarCellTracker.Track b = new RadarCellTracker.Track("c-b", T, T, -97.0, 30.5);
        RadarCellTracker.Cell nearA = new RadarCellTracker.Cell(new int[0], 50, 10, -97.95, 30.5);
        RadarCellTracker.Cell far = new RadarCellTracker.Cell(new int[0], 50, 10, -96.0, 30.5);
        Map<Integer, RadarCellTracker.Track> m = RadarCellTracker.match(List.of(nearA, far), List.of(a, b),
                T.plus(Duration.ofMinutes(2)));
        assertThat(m).containsEntry(0, a).doesNotContainKey(1);
        // 2 minutes allow 6 km; ~4.8 km matches, ~9.6 km does not.
        RadarCellTracker.Cell tenKm = new RadarCellTracker.Cell(new int[0], 50, 10, -97.9, 30.5);
        assertThat(RadarCellTracker.match(List.of(tenKm), List.of(a), T.plus(Duration.ofMinutes(2)))).isEmpty();
        assertThat(RadarCellTracker.match(List.of(tenKm), List.of(a), T.plus(Duration.ofMinutes(10)))).containsKey(0);
    }

    @Test
    @DisplayName("least-squares motion over the last positions: 60 km/h toward the east")
    void motion() {
        RadarCellTracker.Track t = new RadarCellTracker.Track("c-x", T, T, -98.0, 30.5);
        double kmPerDegLon = 111.32 * Math.cos(Math.toRadians(30.5));
        for (int i = 1; i <= 3; i++) {
            t.add(T.plus(Duration.ofMinutes(10L * i)), -98.0 + 10.0 * i / kmPerDegLon, 30.5);
        }
        double[] m = RadarCellTracker.motion(t);
        assertThat(m[0]).isBetween(58.0, 62.0);
        assertThat(m[1]).isBetween(88.0, 92.0);
        assertThat(t.size()).isEqualTo(RadarCellTracker.TRACK_POSITIONS);
        assertThat(RadarCellTracker.motion(new RadarCellTracker.Track("c-y", T, T, 0, 0))).isNull();
    }

    @Test
    @DisplayName("wx-cells: ids persist across frames, motion appears after a match, vanished cells are tombstoned")
    void generatorLifecycle() {
        FakeDerived world = new FakeDerived(T.plusSeconds(30));
        world.addPlace("home", "Home", -98.0, 30.5, 20, 1);
        WxCellsGenerator generator = new WxCellsGenerator("small");
        DefaultDerivedContext c0 = world.context(generator.descriptor(), ValidatedParams.empty());
        generator.reconcile(List.of(), c0);
        world.collect(c0);

        FrameView f1 = view(grid(new int[] {10, 10, 16, 16, 55}), T);
        DefaultDerivedContext c1 = world.context(generator.descriptor(), ValidatedParams.empty());
        generator.process(c1, f1, T);
        List<NormalizedRecord> first = world.collect(c1);
        assertThat(first).hasSize(1);
        String id = first.get(0).externalId();
        assertThat(id).startsWith("c-").hasSize(28);
        assertThat(first.get(0).payload()).containsEntry("maxDbz", 55.0).containsEntry("automated", true)
                .containsEntry("glmFlashes5m", 0).doesNotContainKey("motion");
        assertThat(first.get(0).geometry()).isNotNull();
        assertThat(first.get(0).eventTime()).isEqualTo(T);

        Instant t2 = T.plus(Duration.ofMinutes(2));
        world.clock.setInstant(t2.plusSeconds(30));
        FrameView f2 = view(grid(new int[] {12, 10, 18, 16, 55}), t2);
        DefaultDerivedContext c2 = world.context(generator.descriptor(), ValidatedParams.empty());
        generator.process(c2, f2, t2);
        List<NormalizedRecord> second = world.collect(c2);
        assertThat(second).hasSize(1);
        assertThat(second.get(0).externalId()).isEqualTo(id);
        assertThat(second.get(0).payload()).containsKey("motion");
        @SuppressWarnings("unchecked")
        Map<String, Object> motion = (Map<String, Object>) second.get(0).payload().get("motion");
        assertThat(motion).containsEntry("method", "centroid-v1").containsEntry("bearingDeg", 90);

        Instant t3 = T.plus(Duration.ofMinutes(4));
        world.clock.setInstant(t3.plusSeconds(30));
        DefaultDerivedContext c3 = world.context(generator.descriptor(), ValidatedParams.empty());
        generator.process(c3, view(grid(), t3), t3);
        List<NormalizedRecord> third = world.collect(c3);
        assertThat(third).singleElement().satisfies(r -> {
            assertThat(r.tombstone()).isTrue();
            assertThat(r.externalId()).isEqualTo(id);
        });
    }

    @Test
    @DisplayName("reconcile restores tracks from heads and tombstones nothing")
    void reconcileRestores() {
        FakeDerived world = new FakeDerived(T);
        WxCellsGenerator generator = new WxCellsGenerator("small");
        HeadSnapshot head = new HeadSnapshot("wx-cells", "c-01KEEP", "radar-cell", 3, 2, T, false,
                Map.of("maxDbz", 55.0), ai.foresignal.source.spi.Geo.point(-98.0, 30.5).buffer(0.02));
        world.seed(List.of(head));
        DefaultDerivedContext ctx = world.context(generator.descriptor(), ValidatedParams.empty());
        generator.reconcile(List.of(head), ctx);
        assertThat(world.collect(ctx)).isEmpty();
        assertThat(generator.tracks()).containsKey("c-01KEEP");
    }

    @Test
    @DisplayName("flash buckets")
    void buckets() {
        assertThat(WxCellsGenerator.bucket(0)).isZero();
        assertThat(WxCellsGenerator.bucket(2)).isEqualTo(1);
        assertThat(WxCellsGenerator.bucket(14)).isEqualTo(10);
        assertThat(WxCellsGenerator.bucket(500)).isEqualTo(200);
    }
}
