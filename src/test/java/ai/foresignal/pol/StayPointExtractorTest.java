package ai.foresignal.pol;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.pol.StayPointExtractor.Candidate;
import ai.foresignal.pol.StayPointExtractor.Fix;
import ai.foresignal.pol.StayPointExtractor.Result;
import ai.foresignal.pol.StayPointExtractor.Stay;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Stay-point extraction (prompt 114): dwell, drive-through, gaps, jitter, incremental passes and flushing. */
class StayPointExtractorTest {

    static final Instant T = Instant.parse("2026-10-01T08:00:00Z");
    static final PolParams P = PolParams.DEFAULTS;

    /** About 1 m of latitude in degrees. */
    static final double METER = 1 / 110_574.0;

    static Fix fix(int minute, double lon, double lat) {
        return new Fix(T.plus(Duration.ofMinutes(minute)), lon, lat, "src/a", minute);
    }

    /** {@code count} fixes every {@code step} minutes from {@code start}, jittered within ±{@code jitterM}. */
    static List<Fix> dwell(int start, int count, int step, double lon, double lat, double jitterM) {
        List<Fix> out = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double j = (i % 2 == 0 ? 1 : -1) * jitterM * METER;
            out.add(fix(start + i * step, lon + j, lat - j));
        }
        return out;
    }

    @Test
    @DisplayName("fixes within the radius spanning the dwell make one stay with the mean centroid")
    void dwellMakesAStay() {
        List<Fix> fixes = new ArrayList<>(dwell(0, 7, 5, -97.74, 30.27, 0));
        fixes.add(fix(40, -97.70, 30.27));
        Result r = StayPointExtractor.extract(null, fixes, P);
        assertThat(r.stays()).singleElement().satisfies(s -> {
            assertThat(s.arrive()).isEqualTo(T);
            assertThat(s.depart()).isEqualTo(T.plus(Duration.ofMinutes(30)));
            assertThat(s.fixes()).isEqualTo(7);
            assertThat(s.lon()).isCloseTo(-97.74, org.assertj.core.data.Offset.offset(1e-9));
            assertThat(s.dwell()).isEqualTo(Duration.ofMinutes(30));
            assertThat(s.recordRef()).isEqualTo("src/a");
        });
        assertThat(r.open()).as("the breaking fix anchors the next candidate").isNotNull();
    }

    @Test
    @DisplayName("jitter within the radius stays one candidate")
    void jitterWithinRadius() {
        List<Fix> fixes = new ArrayList<>(dwell(0, 13, 5, -97.74, 30.27, 25));
        fixes.add(fix(70, -97.60, 30.27));
        assertThat(StayPointExtractor.extract(null, fixes, P).stays()).singleElement()
                .satisfies(s -> assertThat(s.fixes()).isEqualTo(13));
    }

    @Test
    @DisplayName("a drive-through never spans the dwell within the radius, so it makes no stay")
    void driveThrough() {
        List<Fix> fixes = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            fixes.add(fix(i, -97.74 + i * 0.005, 30.27));
        }
        Result r = StayPointExtractor.extract(null, fixes, P);
        assertThat(r.stays()).isEmpty();
    }

    @Test
    @DisplayName("a gap over maxGap ends a stay even at the same place")
    void gapEndsAStay() {
        List<Fix> fixes = new ArrayList<>(dwell(0, 4, 5, -97.74, 30.27, 0));
        fixes.addAll(dwell(60, 4, 5, -97.74, 30.27, 0));
        fixes.add(fix(200, -97.60, 30.27));
        Result r = StayPointExtractor.extract(null, fixes, P);
        assertThat(r.stays()).hasSize(2);
        assertThat(r.stays().get(0).depart()).isEqualTo(T.plus(Duration.ofMinutes(15)));
        assertThat(r.stays().get(1).arrive()).isEqualTo(T.plus(Duration.ofMinutes(60)));
    }

    @Test
    @DisplayName("a candidate shorter than minDwell is dropped when broken")
    void shortCandidateDropped() {
        List<Fix> fixes = new ArrayList<>(dwell(0, 2, 4, -97.74, 30.27, 0));
        fixes.add(fix(10, -97.60, 30.27));
        assertThat(StayPointExtractor.extract(null, fixes, P).stays()).isEmpty();
    }

    @Test
    @DisplayName("a stay spanning two passes is found once through the carried candidate")
    void incremental() {
        List<Fix> first = dwell(0, 3, 5, -97.74, 30.27, 0);
        Result a = StayPointExtractor.extract(null, first, P);
        assertThat(a.stays()).isEmpty();
        Map<String, Object> stored = a.open().toMap();
        Candidate restored = Candidate.fromMap(stored);
        assertThat(restored).isNotNull();
        List<Fix> second = new ArrayList<>(dwell(15, 4, 5, -97.74, 30.27, 0));
        second.add(fix(60, -97.60, 30.27));
        Result b = StayPointExtractor.extract(restored, second, P);
        assertThat(b.stays()).singleElement().satisfies(s -> {
            assertThat(s.fixes()).isEqualTo(7);
            assertThat(s.arrive()).isEqualTo(T);
        });
    }

    @Test
    @DisplayName("fixes not after the candidate's last fix are ignored")
    void outOfOrderIgnored() {
        Result a = StayPointExtractor.extract(null, dwell(0, 3, 5, -97.74, 30.27, 0), P);
        Result b = StayPointExtractor.extract(a.open(), List.of(fix(5, -97.0, 30.0), fix(10, -97.0, 30.0)), P);
        assertThat(b.stays()).isEmpty();
        assertThat(b.open().lastAt()).isEqualTo(T.plus(Duration.ofMinutes(10)));
        assertThat(b.open().anchorLon()).isEqualTo(-97.74);
    }

    @Test
    @DisplayName("flush closes a qualifying candidate after maxGap of silence and keeps a fresh one open")
    void flush() {
        Result a = StayPointExtractor.extract(null, dwell(0, 5, 5, -97.74, 30.27, 0), P);
        Result early = StayPointExtractor.flush(a.open(), T.plus(Duration.ofMinutes(40)), P);
        assertThat(early.stays()).isEmpty();
        assertThat(early.open()).isNotNull();
        Result late = StayPointExtractor.flush(a.open(), T.plus(Duration.ofMinutes(60)), P);
        assertThat(late.stays()).singleElement().extracting(Stay::fixes).isEqualTo(5);
        assertThat(late.open()).isNull();
        assertThat(StayPointExtractor.flush(null, T, P).stays()).isEmpty();
    }

    @Test
    @DisplayName("an unreadable stored candidate reads as none")
    void unreadableCandidate() {
        assertThat(Candidate.fromMap(Map.of("anchorLon", "x"))).isNull();
        assertThat(Candidate.fromMap("nope")).isNull();
    }
}
