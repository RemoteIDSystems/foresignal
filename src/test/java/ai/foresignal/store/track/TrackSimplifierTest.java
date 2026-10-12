package ai.foresignal.store.track;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link TrackSimplifier} (prompt 037): known Douglas–Peucker vectors, the time axis, kept points,
 * the {@code maxPoints} search and its fallbacks, auto tolerance, and the antimeridian split.
 */
class TrackSimplifierTest {

    private static TrackPoint p(double lon, double lat, long seconds) {
        return new TrackPoint(lon, lat, seconds * 1000, false, new Double[] {(double) seconds});
    }

    private static TrackPoint kept(double lon, double lat, long seconds) {
        return new TrackPoint(lon, lat, seconds * 1000, true, null);
    }

    @Test
    @DisplayName("a small offset is dropped above the tolerance and kept below it")
    void offsetPoint() {
        // 0.0001° of latitude is about 11.1 m.
        List<TrackPoint> line = List.of(p(0, 0, 0), p(0.5, 0.0001, 50), p(1, 0, 100));
        assertThat(TrackSimplifier.simplify(line, 20)).hasSize(2);
        assertThat(TrackSimplifier.simplify(line, 5)).hasSize(3);
    }

    @Test
    @DisplayName("the classic zigzag keeps its corners and drops the collinear points")
    void zigzag() {
        List<TrackPoint> line = List.of(p(0, 0, 0), p(0.5, 0, 5), p(1, 0, 10), p(1, 0.5, 15), p(1, 1, 20),
                p(1.5, 1, 25), p(2, 1, 30));
        List<TrackPoint> out = TrackSimplifier.simplify(line, 100);
        assertThat(out).extracting(TrackPoint::time).containsExactly(0L, 10_000L, 20_000L, 30_000L);
    }

    @Test
    @DisplayName("the time axis: a point collinear in space but far from its time-interpolated position is kept")
    void synchronizedDistance() {
        List<TrackPoint> line = List.of(p(0, 0, 0), p(0.5, 0, 10), p(1, 0, 100));
        assertThat(TrackSimplifier.simplify(line, 1_000)).as("at t=10 the segment is at lon 0.1, 44 km away")
                .hasSize(3);
        List<TrackPoint> steady = List.of(p(0, 0, 0), p(0.5, 0, 50), p(1, 0, 100));
        assertThat(TrackSimplifier.simplify(steady, 1)).hasSize(2);
    }

    @Test
    @DisplayName("points flagged keep (FIRST, REAPPEAR, SIGNIFICANT, TRIGGER_EDGE) always survive")
    void keptPoints() {
        List<TrackPoint> line = List.of(p(0, 0, 0), kept(0.5, 0, 50), p(0.75, 0, 75), p(1, 0, 100));
        assertThat(TrackSimplifier.simplify(line, 100_000)).extracting(TrackPoint::time)
                .containsExactly(0L, 50_000L, 100_000L);
    }

    private static List<TrackPoint> wave(int n) {
        List<TrackPoint> out = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            out.add(p(i * 0.001, Math.sin(i / 10.0) * 0.01, i));
        }
        return out;
    }

    @Test
    @DisplayName("maxPoints: a larger tolerance is searched until the result fits")
    void maxPointsSearch() {
        List<List<TrackPoint>> segments = List.of(wave(1000));
        TrackSimplifier.Result fine = TrackSimplifier.simplify(segments, 1, 10_000);
        assertThat(fine.points()).isGreaterThan(50);
        assertThat(fine.toleranceMeters()).isEqualTo(1.0);
        TrackSimplifier.Result capped = TrackSimplifier.simplify(segments, 1, 50);
        assertThat(capped.points()).isLessThanOrEqualTo(50).isGreaterThanOrEqualTo(2);
        assertThat(capped.toleranceMeters()).isGreaterThan(1.0);
    }

    @Test
    @DisplayName("maxPoints holds even when kept points or segments alone exceed it")
    void maxPointsFallbacks() {
        List<TrackPoint> allKept = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            allKept.add(kept(i * 0.01, (i % 2) * 0.01, i));
        }
        assertThat(TrackSimplifier.simplify(List.of(allKept), 1, 10).points()).isLessThanOrEqualTo(10);

        List<List<TrackPoint>> many = new ArrayList<>();
        for (int s = 0; s < 30; s++) {
            many.add(List.of(p(s, 0, s * 100L), p(s + 0.5, 0.5, s * 100L + 10), p(s + 0.9, 0, s * 100L + 20)));
        }
        TrackSimplifier.Result result = TrackSimplifier.simplify(many, 1, 10);
        assertThat(result.points()).isLessThanOrEqualTo(10);
        assertThat(result.segments().getLast().getLast().time()).as("the newest segments are kept")
                .isEqualTo(29 * 100_000L + 20_000L);
    }

    @Test
    @DisplayName("auto tolerance is the bbox diagonal / 2000, clamped to 5 m–2 km")
    void autoTolerance() {
        assertThat(TrackSimplifier.autoTolerance(List.of(List.of(p(0, 0, 0), p(0.0001, 0, 1))))).isEqualTo(5.0);
        assertThat(TrackSimplifier.autoTolerance(List.of(List.of(p(0, 0, 0), p(90, 0, 1))))).isEqualTo(2_000.0);
        double diagonal = TrackSimplifier.diagonalMeters(List.of(List.of(p(0, 0, 0), p(0.1, 0, 1))));
        assertThat(diagonal).isCloseTo(11_119.5, within(5.0));
        assertThat(TrackSimplifier.autoTolerance(List.of(List.of(p(0, 0, 0), p(0.1, 0, 1)))))
                .isCloseTo(diagonal / 2000, within(0.01));
        assertThat(TrackSimplifier.diagonalMeters(List.of())).isZero();
    }

    @Test
    @DisplayName("eastward across the antimeridian: unwrapped, then split at +180/-180 with interpolated time")
    void antimeridianEast() {
        List<TrackPoint> unwrapped = TrackSimplifier.unwrap(List.of(p(179, 0, 0), p(-179, 2, 100)));
        assertThat(unwrapped.get(1).lon()).isEqualTo(181.0);
        assertThat(TrackSimplifier.crossings(unwrapped)).isEqualTo(1);
        List<List<TrackPoint>> split = TrackSimplifier.splitAntimeridian(List.of(unwrapped));
        assertThat(split).hasSize(2);
        assertThat(split.get(0)).extracting(TrackPoint::lon).containsExactly(179.0, 180.0);
        assertThat(split.get(1)).extracting(TrackPoint::lon).containsExactly(-180.0, -179.0);
        assertThat(split.get(0).get(1).lat()).isCloseTo(1.0, within(1e-9));
        assertThat(split.get(0).get(1).time()).isEqualTo(50_000L);
        assertThat(split.get(1).get(0).time()).isEqualTo(50_000L);
        assertThat(split.get(0).get(1).values()[0]).as("values interpolated").isCloseTo(50.0, within(1e-9));
    }

    @Test
    @DisplayName("westward across the antimeridian, and a track that never crosses")
    void antimeridianWest() {
        List<TrackPoint> unwrapped = TrackSimplifier.unwrap(List.of(p(-179, 0, 0), p(179, 0, 100), p(178, 0, 200)));
        assertThat(unwrapped).extracting(TrackPoint::lon).containsExactly(-179.0, -181.0, -182.0);
        List<List<TrackPoint>> split = TrackSimplifier.splitAntimeridian(List.of(unwrapped));
        assertThat(split).hasSize(2);
        assertThat(split.get(0)).extracting(TrackPoint::lon).containsExactly(-179.0, -180.0);
        assertThat(split.get(1)).extracting(TrackPoint::lon).containsExactly(180.0, 179.0, 178.0);

        List<TrackPoint> plain = List.of(p(10, 0, 0), p(11, 0, 1));
        assertThat(TrackSimplifier.splitAntimeridian(List.of(TrackSimplifier.unwrap(plain)))).hasSize(1);
        assertThat(TrackSimplifier.crossings(plain)).isZero();
    }

    @Test
    @DisplayName("normalize and cell helpers")
    void helpers() {
        assertThat(TrackSimplifier.normalize(181)).isEqualTo(-179.0);
        assertThat(TrackSimplifier.normalize(-181)).isEqualTo(179.0);
        assertThat(TrackSimplifier.normalize(180)).isEqualTo(180.0);
        assertThat(TrackSimplifier.normalize(541)).isEqualTo(-179.0);
        assertThat(TrackSimplifier.cell(0)).isZero();
        assertThat(TrackSimplifier.cell(180)).isZero();
        assertThat(TrackSimplifier.cell(181)).isEqualTo(1);
        assertThat(TrackSimplifier.cell(-181)).isEqualTo(-1);
        assertThat(TrackSimplifier.simplify(List.of(p(0, 0, 0)), 1)).hasSize(1);
        assertThat(TrackSimplifier.count(List.of(List.of(p(0, 0, 0)), List.of(p(1, 1, 1), p(2, 2, 2))))).isEqualTo(3);
        TrackPoint same = TrackSimplifier.unwrap(List.of(p(5, 5, 5))).getFirst();
        assertThat(same.lon()).isEqualTo(5.0);
    }
}
