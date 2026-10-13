package ai.foresignal.source.adapters.satdump;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link RfSession} (prompt 049, Weather-Imagery-Design §4.2): sampling about every 2 s, the 30-s
 * flush cadence, the LOS aggregates and the length of the SNR-against-elevation series.
 */
class RfSessionTest {

    static final Instant T0 = Instant.parse("2026-10-12T18:02:10Z");

    static ApiSnapshot reading(Double snr, Double ber, boolean locked, double el) {
        return new ApiSnapshot(ApiSnapshot.AUTOTRACK, 137_900_000L, "METEOR-M2 3", 200.0, el, null, null, true,
                List.of("psk_demod"), snr, snr == null ? null : snr + 2, ber, locked ? "SYNCED" : "NOSYNC", locked, 2.0,
                null);
    }

    @Test
    @DisplayName("a sample is kept only when about 2 s have passed")
    void sampling() {
        RfSession s = new RfSession("home:meteor-m2-3:1", "meteor-m2-3", "METEOR-M2 3", ApiSnapshot.AUTOTRACK, T0);
        assertThat(s.sample(T0, reading(5.0, 0.1, false, 3))).isTrue();
        assertThat(s.sample(T0.plusMillis(900), reading(5.0, 0.1, false, 3))).isFalse();
        assertThat(s.sample(T0.plusMillis(2000), reading(5.0, 0.1, false, 3))).isTrue();
        assertThat(s.sample(T0.plusMillis(3900), reading(5.0, 0.1, false, 3))).isTrue();
        assertThat(s.samples()).hasSize(3);
        assertThat(s.frequencyHz()).isEqualTo(137_900_000L);
        assertThat(s.lastActivity()).isEqualTo(T0.plusMillis(3900));
    }

    @Test
    @DisplayName("the flush is due every 30 s")
    void flush() {
        RfSession s = new RfSession("p", "k", "K", ApiSnapshot.AUTOTRACK, T0);
        assertThat(s.flushDue(T0.plusSeconds(29))).isFalse();
        assertThat(s.flushDue(T0.plusSeconds(30))).isTrue();
        s.flushed(T0.plusSeconds(30));
        assertThat(s.flushDue(T0.plusSeconds(59))).isFalse();
        assertThat(s.flushDue(T0.plusSeconds(60))).isTrue();
    }

    @Test
    @DisplayName("LOS aggregates: SNR max and mean, BER min and mean, synced fraction, lock times and elevations")
    void aggregates() {
        RfSession s = new RfSession("p", "k", "K", ApiSnapshot.AUTOTRACK, T0);
        s.sample(T0, reading(2.0, 0.5, false, 2));
        s.sample(T0.plusSeconds(2), reading(8.0, 0.2, true, 10));
        s.sample(T0.plusSeconds(4), reading(12.0, 0.1, true, 30));
        s.sample(T0.plusSeconds(6), reading(4.0, 0.4, false, 8));
        Map<String, Object> rf = s.aggregate();

        assertThat(rf).containsEntry("samples", 4).containsEntry("snrMaxDb", 12.0).containsEntry("snrMeanDb", 6.5)
                .containsEntry("berMin", 0.1).containsEntry("berMean", 0.3).containsEntry("syncedFraction", 0.5)
                .containsEntry("firstLockAt", "2026-10-12T18:02:12Z").containsEntry("lastLockAt", "2026-10-12T18:02:14Z")
                .containsEntry("firstLockElevationDeg", 10.0).containsEntry("lastLockElevationDeg", 30.0);
        assertThat(s.firstLock()).isEqualTo(T0.plusSeconds(2));
        assertThat(s.hadLock()).isTrue();
        assertThat(s.maxElevation()).isEqualTo(30.0);
    }

    @Test
    @DisplayName("no lock: no lock fields, synced fraction 0; no samples: just the count")
    void noLock() {
        RfSession s = new RfSession("p", "k", "K", ApiSnapshot.LIVE, T0);
        assertThat(s.aggregate()).containsExactly(Map.entry("samples", 0));
        s.sample(T0, reading(null, null, false, 5));
        assertThat(s.aggregate()).containsEntry("syncedFraction", 0.0).doesNotContainKeys("firstLockAt", "snrMaxDb");
        assertThat(s.hadLock()).isFalse();
    }

    @Test
    @DisplayName("the SNR-against-elevation series has at most 60 points, in time order")
    void series() {
        RfSession s = new RfSession("p", "k", "K", ApiSnapshot.AUTOTRACK, T0);
        for (int i = 0; i < 400; i++) {
            double el = i < 200 ? i * 0.3 : (400 - i) * 0.3;
            s.sample(T0.plusSeconds(2L * i), reading(el / 6, 0.1, true, el));
        }
        @SuppressWarnings("unchecked")
        List<List<Double>> series = (List<List<Double>>) s.aggregate().get("snrVsElevation");
        assertThat(series).hasSize(RfSession.SERIES_POINTS);
        assertThat(series.get(0).get(0)).isLessThan(series.get(29).get(0));
        assertThat(series.get(59).get(0)).isLessThan(series.get(30).get(0));
        assertThat(RfSession.series(s.samples().subList(0, 10))).hasSize(10);
    }

    @Test
    @DisplayName("the ring keeps at most 2,000 samples")
    void ringCapacity() {
        RfSession s = new RfSession("p", "k", "K", ApiSnapshot.AUTOTRACK, T0);
        for (int i = 0; i < RfSession.RING_CAPACITY + 10; i++) {
            s.sample(T0.plusSeconds(2L * i), reading(1.0, 0.1, false, 1));
        }
        assertThat(s.samples()).hasSize(RfSession.RING_CAPACITY);
        assertThat(s.samples().get(0).at()).isEqualTo(T0.plusSeconds(20).toEpochMilli());
    }
}
