package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code derived-loiter} (prompt 099): the radius test, and through ticks the 0.49 vs 0.51 nm boundary,
 * the 29 vs 30 minute span and a fix spacing that does not land exactly on 30 minutes.
 */
class LoiterTest {

    private static final Instant T0 = Instant.parse("2026-10-15T10:00:00Z");
    private static final String AIS = "aisstream";

    @Test
    @DisplayName("fixes inside the radius hold station; one outside breaks it")
    void radius() {
        Instant t = Instant.parse("2026-10-15T10:00:00Z");
        List<LoiterGenerator.Fix> tight = List.of(new LoiterGenerator.Fix(t, -95.0, 32.0),
                new LoiterGenerator.Fix(t.plusSeconds(600), -95.001, 32.001),
                new LoiterGenerator.Fix(t.plusSeconds(1200), -95.0, 32.0));
        assertThat(LoiterGenerator.withinRadius(tight, -95.0, 32.0, LoiterGenerator.RADIUS_KM)).isTrue();
        List<LoiterGenerator.Fix> loose = List.of(new LoiterGenerator.Fix(t, -95.0, 32.0),
                new LoiterGenerator.Fix(t.plusSeconds(600), -95.05, 32.05));
        assertThat(LoiterGenerator.withinRadius(loose, -95.0, 32.0, LoiterGenerator.RADIUS_KM)).isFalse();
    }

    /**
     * Fixes alternating between two points {@code 2 × radiusNm} apart, so every fix lies exactly
     * {@code radiusNm} from the centroid, at the given minutes; the records emitted across the ticks.
     */
    private static List<NormalizedRecord> hold(double radiusNm, long... minutes) {
        LayerHarness harness = new LayerHarness(T0, AIS);
        LoiterGenerator generator = new LoiterGenerator(harness.registry(), harness.reference());
        double apartKm = 2 * radiusNm * 1.852;
        List<NormalizedRecord> emitted = new ArrayList<>();
        for (int i = 0; i < minutes.length; i++) {
            Instant at = T0.plus(Duration.ofMinutes(minutes[i]));
            double lat = i % 2 == 0 ? 30.0 : LayerHarness.north(30.0, apartKm);
            harness.put(AIS, "366123456", "vessel", at, -95.0, lat, "mmsi", "366123456", "sog", 0.4, "navStatus", 0);
            emitted.addAll(harness.tick(generator, at.plusSeconds(30)));
        }
        return emitted;
    }

    @Test
    @DisplayName("fixes 0.49 nm from their centroid over 30 minutes loiter; 0.51 nm do not")
    void radiusBoundary() {
        assertThat(hold(0.49, 0, 10, 20, 30)).singleElement()
                .satisfies(record -> assertThat(record.externalId()).isEqualTo("366123456"));
        assertThat(hold(0.51, 0, 10, 20, 30)).isEmpty();
    }

    @Test
    @DisplayName("four tight fixes spanning 29 minutes are not yet a loiter; spanning 30 minutes they are")
    void spanBoundary() {
        assertThat(hold(0.2, 0, 10, 20, 29)).isEmpty();
        assertThat(hold(0.2, 0, 10, 20, 30)).hasSize(1);
    }

    @Test
    @DisplayName("after a restart a stale loiter record whose vessel is no longer seen is tombstoned")
    void reconcileEndsAStaleLoiter() {
        LayerHarness harness = new LayerHarness(T0, AIS);
        LoiterGenerator generator = new LoiterGenerator(harness.registry(), harness.reference());
        ai.foresignal.derive.HeadSnapshot stale = new ai.foresignal.derive.HeadSnapshot(LoiterGenerator.ID,
                "366123456", LoiterGenerator.KIND, 1, 1, T0.minus(Duration.ofHours(3)), false,
                java.util.Map.of("status", "ok"), ai.foresignal.source.spi.Geo.point(-95.0, 30.0));
        harness.reconcile(generator, List.of(stale));
        harness.tick(generator, T0.plusSeconds(60));
        assertThat(harness.tombstones).containsExactly("loiter/366123456");
    }

    @Test
    @DisplayName("a reported loiter is ended when its vessel stops being seen")
    void absentVesselEndsTheLoiter() {
        LayerHarness harness = new LayerHarness(T0, AIS);
        LoiterGenerator generator = new LoiterGenerator(harness.registry(), harness.reference());
        double apartKm = 2 * 0.2 * 1.852;
        long[] minutes = {0, 10, 20, 30};
        for (int i = 0; i < minutes.length; i++) {
            Instant at = T0.plus(Duration.ofMinutes(minutes[i]));
            harness.put(AIS, "366123456", "vessel", at, -95.0, i % 2 == 0 ? 30.0 : LayerHarness.north(30.0, apartKm),
                    "mmsi", "366123456", "sog", 0.4, "navStatus", 0);
            harness.tick(generator, at.plusSeconds(30));
        }
        harness.clear(AIS);
        harness.tick(generator, T0.plus(Duration.ofMinutes(40)));
        assertThat(harness.tombstones).containsExactly("loiter/366123456");
    }

    @Test
    @DisplayName("fixes every 7 minutes still loiter once they span 30 minutes (no exact-30 requirement)")
    void unevenSpacing() {
        assertThat(hold(0.2, 0, 7, 14, 21, 28, 35)).isNotEmpty();
    }
}
