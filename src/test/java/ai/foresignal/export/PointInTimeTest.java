package ai.foresignal.export;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.HistoryTier;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The point-in-time plan of snapshots and asOf/atSeq exports (prompt 111, Core-Data-Model §12). */
class PointInTimeTest {

    static final Instant T = Instant.parse("2026-03-01T12:00:00Z");

    private static PointInTime plan() {
        Map<HistoryTier, Long> lower = new EnumMap<>(HistoryTier.class);
        lower.put(HistoryTier.TRACK, 40L);
        Map<String, Duration> validity = new LinkedHashMap<>();
        validity.put("trk", Duration.ofMinutes(5));
        validity.put("std", null);
        return new PointInTime(100, T, lower, validity, Map.of("std", List.of("h1")), true);
    }

    @Test
    @DisplayName("validity is judged at t: TTL on TRACK (explicit expiry ignored), explicit expiry first on STANDARD")
    void validity() {
        PointInTime pit = plan();
        assertThat(pit.valid(HistoryTier.TRACK, "trk", T.minusSeconds(60), T.minusSeconds(1))).isTrue();
        assertThat(pit.valid(HistoryTier.TRACK, "trk", T.minusSeconds(301), null)).isFalse();
        assertThat(pit.valid(HistoryTier.STANDARD, "std", T.minusSeconds(86_400), T.plusSeconds(1))).isTrue();
        assertThat(pit.valid(HistoryTier.STANDARD, "std", T.minusSeconds(1), T)).isFalse();
        assertThat(pit.valid(HistoryTier.STANDARD, "std", T.minusSeconds(86_400), null)).as("no TTL").isTrue();
        assertThat(pit.lower(HistoryTier.TRACK)).isEqualTo(40L);
        assertThat(pit.lower(HistoryTier.STANDARD)).isZero();
        assertThat(pit.holdsFor("std")).containsExactly("h1");
        assertThat(pit.holdsFor("trk")).isEmpty();
    }

    @Test
    @DisplayName("the stored form round-trips")
    void roundTrip() {
        PointInTime pit = plan();
        PointInTime back = PointInTimePlanner.fromMap(PointInTimePlanner.toMap(pit));
        assertThat(back).isEqualTo(pit);
    }
}
