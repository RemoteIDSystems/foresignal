package ai.foresignal.intel;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.Geo;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;

/** Same-event dedup (prompt 101). */
class DedupTest {

    private static final Instant T = Instant.parse("2026-10-15T10:00:00Z");

    @Test
    @DisplayName("a shared member merges; overlapping buffered geometry and time merges")
    void merges() {
        assertThat(Dedup.sharesMember(Set.of("a"), Set.of("a", "b"))).isTrue();
        assertThat(Dedup.sharesMember(Set.of("c"), Set.of("a", "b"))).isFalse();
        Geometry a = Geo.point(-95.0, 32.0).buffer(0.05);
        Geometry b = Geo.point(-95.1, 32.1).buffer(0.05);
        assertThat(Dedup.intersectsAndOverlaps(a, T, null, b, T.plusSeconds(600), null, 20, Duration.ofHours(6)))
                .isTrue();
        assertThat(Dedup.intersectsAndOverlaps(a, T, null, Geo.point(-80, 20).buffer(0.05), T, null, 20,
                Duration.ofHours(6))).isFalse();
        assertThat(Dedup.intersectsAndOverlaps(a, T, T, b, T.plus(Duration.ofDays(3)), null, 20, Duration.ofHours(6)))
                .isFalse();
    }

    @Test
    @DisplayName("different types never merge")
    void types() {
        IntelligenceEventEntity open = new IntelligenceEventEntity("E1", "aviation-anomaly", "t", T);
        assertThat(Dedup.merges("maritime-disruption", Geo.point(-95, 32), T, null, Set.of("m"),
                open, Set.of("m"), 20, Duration.ofHours(6))).isFalse();
    }
}
