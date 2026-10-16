package ai.foresignal.source.adapters.wx;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.derive.DefaultDerivedContext;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.raster.sample.FrameView;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.testsupport.raster.SampleFixtures;
import ai.foresignal.testsupport.weather.FakeDerived;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code wx-storm-tracks} end to end over synthetic MRMS reflectivity frames (prompt 105 §3 "StormTrackIT", as a
 * generator test on the in-memory derived world): a storm keeps its id across frames, versions are written only
 * when a quantized field or the geometry changes (not for the moving history), the history ring holds 12
 * positions, a track that misses two frames ends with a final swath, a merge ends the smaller track with
 * {@code mergedInto} the larger, and a restart restores the stored swath so later frames extend it.
 */
class StormTrackGeneratorTest {

    static final Instant T0 = Instant.parse("2026-10-15T20:00:00Z");
    static final String REFLECTIVITY = WxStormTracksGenerator.REFLECTIVITY;

    private static FakeDerived world() {
        FakeDerived world = new FakeDerived(T0);
        world.addPlace("home", "Home", -98.0, 30.5, 30, 1);
        return world;
    }

    /** Reflectivity with discs {@code {lonOffsetKm, radiusKm}} of 55 dBZ east of (−98.0, 30.5). */
    private static FrameView frame(Instant t, double[]... discs) {
        int[] codes = new int[SampleFixtures.TEXAS.nx() * SampleFixtures.TEXAS.ny()];
        java.util.Arrays.fill(codes, 1);
        for (double[] d : discs) {
            int[] one = SampleFixtures.echoes(SampleFixtures.TEXAS, -98.0, 30.5, d[0], d[1], 55);
            for (int i = 0; i < codes.length; i++) {
                codes[i] = Math.max(codes[i], one[i]);
            }
        }
        return SampleFixtures.codes(SampleFixtures.reflectivity(REFLECTIVITY), SampleFixtures.TEXAS, codes,
                SampleFixtures.row(REFLECTIVITY, t, null, null));
    }

    private static List<NormalizedRecord> process(FakeDerived world, WxStormTracksGenerator g, FrameView view) {
        world.clock.setInstant(view.frame().validTime());
        DefaultDerivedContext ctx = world.context(g.descriptor(), ValidatedParams.empty());
        g.process(ctx, view, view.frame().validTime());
        return world.collect(ctx);
    }

    private static List<NormalizedRecord> tracks(List<NormalizedRecord> out) {
        return out.stream().filter(r -> !r.tombstone() && r.kind().equals(WxStormTracksGenerator.TRACK_KIND)).toList();
    }

    private static List<NormalizedRecord> swaths(List<NormalizedRecord> out) {
        return out.stream().filter(r -> !r.tombstone() && r.kind().equals(WxStormTracksGenerator.SWATH_KIND)).toList();
    }

    @Test
    @DisplayName("a stationary storm keeps its id; versions only while motion settles; a 12-position ring; the end writes a swath")
    void stationaryStorm() {
        FakeDerived world = world();
        WxStormTracksGenerator g = new WxStormTracksGenerator();
        List<Integer> versions = new ArrayList<>();
        String id = null;
        for (int k = 0; k < 14; k++) {
            List<NormalizedRecord> out = tracks(process(world, g, frame(T0.plusSeconds(120L * k), new double[] {0, 6})));
            versions.add(out.size());
            for (NormalizedRecord r : out) {
                if (id == null) {
                    id = r.externalId();
                }
                assertThat(r.externalId()).as("the id is kept").isEqualTo(id);
            }
        }
        // Frame 1 starts the track, frame 2 adds centroid-v1 motion, frame 3 switches to titan-lite-v1; after
        // that only the history moves, which is not a change.
        assertThat(versions.subList(0, 3)).containsExactly(1, 1, 1);
        assertThat(versions.subList(3, 14)).containsOnly(0);
        assertThat(g.tracks().get(id).positions).hasSize(WxStormTracksGenerator.HISTORY);

        Instant gone = T0.plusSeconds(120L * 14);
        assertThat(tracks(process(world, g, frame(gone)))).as("one missed frame is not an end").isEmpty();
        List<NormalizedRecord> end = process(world, g, frame(gone.plusSeconds(120)));
        assertThat(tracks(end)).singleElement().satisfies(r -> assertThat(r.payload()).containsEntry("status", "ended"));
        assertThat(swaths(end)).singleElement().satisfies(s -> {
            assertThat(s.payload()).containsEntry("status", "ended");
            assertThat(s.payload()).containsEntry("durationMin", 26L);
        });
    }

    @Test
    @DisplayName("a cell over two tracks merges: the larger continues, the smaller ends with mergedInto and a swath")
    void mergeSetsMergedInto() {
        FakeDerived world = world();
        WxStormTracksGenerator g = new WxStormTracksGenerator();
        List<NormalizedRecord> first = tracks(process(world, g, frame(T0, new double[] {-6, 6}, new double[] {7, 5})));
        assertThat(first).hasSize(2);
        NormalizedRecord west = first.stream().min(java.util.Comparator.comparingDouble(
                r -> r.geometry().getGeometryN(0).getCentroid().getX())).orElseThrow();
        NormalizedRecord east = first.stream().filter(r -> r != west).findFirst().orElseThrow();

        List<NormalizedRecord> merged = process(world, g, frame(T0.plusSeconds(120), new double[] {1, 13}));
        NormalizedRecord ended = tracks(merged).stream().filter(r -> r.externalId().equals(east.externalId()))
                .findFirst().orElseThrow();
        assertThat(ended.payload()).containsEntry("status", "ended").containsEntry("mergedInto", west.externalId());
        assertThat(tracks(merged)).anySatisfy(r -> {
            assertThat(r.externalId()).isEqualTo(west.externalId());
            assertThat(r.payload()).containsEntry("status", "active");
        });
        assertThat(swaths(merged)).anySatisfy(s -> {
            assertThat(s.externalId()).isEqualTo(east.externalId());
            assertThat(s.payload()).containsEntry("status", "ended");
        });
    }

    @Test
    @DisplayName("a restart restores the stored swath, history and motion; the next frame extends the union")
    void reconcileRestoresSwath() {
        FakeDerived world = world();
        WxStormTracksGenerator g = new WxStormTracksGenerator();
        List<NormalizedRecord> all = new ArrayList<>();
        for (int k = 0; k < 4; k++) {
            all.addAll(process(world, g, frame(T0.plusSeconds(120L * k), new double[] {2.0 * k, 6})));
        }
        NormalizedRecord track = tracks(all).get(tracks(all).size() - 1);
        NormalizedRecord swath = swaths(all).get(swaths(all).size() - 1);
        List<HeadSnapshot> heads = List.of(
                new HeadSnapshot(WxStormTracksGenerator.ID, track.externalId(), track.kind(), 1, 1, track.observedAt(),
                        false, track.payload(), track.geometry()),
                new HeadSnapshot(WxStormTracksGenerator.ID, swath.externalId(), swath.kind(), 2, 1, swath.observedAt(),
                        false, swath.payload(), swath.geometry()));

        WxStormTracksGenerator restarted = new WxStormTracksGenerator();
        world.seed(heads);
        DefaultDerivedContext ctx = world.context(restarted.descriptor(), ValidatedParams.empty());
        restarted.reconcile(heads, ctx);
        WxStormTracksGenerator.Track restored = restarted.tracks().get(track.externalId());
        assertThat(restored.swath).isEqualTo(swath.geometry());
        assertThat(restored.positions).hasSizeGreaterThanOrEqualTo(3);
        assertThat(restored.motionMethod).isEqualTo(TitanLiteTracker.VERSION);

        List<NormalizedRecord> next = process(world, restarted,
                frame(T0.plus(Duration.ofMinutes(12)), new double[] {10, 6}));
        NormalizedRecord extended = swaths(next).stream().filter(s -> s.externalId().equals(track.externalId()))
                .findFirst().orElseThrow();
        assertThat(extended.geometry().covers(swath.geometry().buffer(-0.001)))
                .as("the stored union is extended, not replaced").isTrue();
        assertThat(extended.geometry().getArea()).isGreaterThan(swath.geometry().getArea());
    }
}
