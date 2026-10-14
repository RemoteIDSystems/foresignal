package ai.foresignal.source.formats.gnss;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.formats.gnss.GnssIntegrityEvaluator.Assessment;
import ai.foresignal.source.formats.gnss.GnssIntegrityEvaluator.State;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link GnssIntegrityEvaluator} (prompt 086): each state from scripted 30 s windows of 1 Hz samples, the baseline built
 * only from OK windows, and the precedence of the rules.
 */
class GnssIntegrityEvaluatorTest {

    static final Instant T0 = Instant.parse("2026-10-13T12:00:00Z");
    static final double LAT = 40.0123;
    static final double LON = -105.1234;

    /** One second of samples. */
    record Second(int mode, Double lat, Double lon, Double eph, Integer uSat, Double cn0, Long clockNs, Integer jamState) {

        static Second good() {
            return new Second(3, LAT, LON, 4.0, 8, 40.0, 100L, null);
        }
    }

    /** Feeds {@code windows} windows of {@code second}, starting at {@code start} s, and returns the assessments. */
    static List<Assessment> run(GnssIntegrityEvaluator e, int start, int windows, Second second) {
        List<Assessment> out = new ArrayList<>();
        for (int s = start; s < start + windows * 30; s++) {
            Optional<Assessment> a = e.onTpv(T0.plusSeconds(s), second.mode(), second.lat(), second.lon(), second.eph());
            a.ifPresent(out::add);
            e.onSky(12, second.uSat(), 0.9, second.cn0());
            if (second.clockNs() != null) {
                e.onClockOffset(second.clockNs());
            }
            if (second.jamState() != null) {
                e.onReceiverJamming(150, second.jamState());
            }
        }
        return out;
    }

    /** An evaluator with five OK windows of baseline, at second 150. */
    static GnssIntegrityEvaluator withBaseline(boolean fixed) {
        GnssIntegrityEvaluator e = new GnssIntegrityEvaluator(fixed);
        List<Assessment> warmup = run(e, 0, 5, Second.good());
        assertThat(warmup).extracting(Assessment::state).containsOnly(State.OK);
        return e;
    }

    /** The state of the window after {@code second} (one more TPV closes it). */
    static Assessment next(GnssIntegrityEvaluator e, int start, Second second) {
        List<Assessment> out = run(e, start, 1, second);
        out.addAll(run(e, start + 30, 1, Second.good()).stream().limit(1).toList());
        return out.getLast();
    }

    @Test
    @DisplayName("OK: steady fixes; the baseline holds only OK windows and is trimmed to 15 minutes")
    void ok() {
        GnssIntegrityEvaluator e = withBaseline(true);
        assertThat(e.baselineSize()).isEqualTo(4);
        run(e, 150, 40, Second.good());
        assertThat(e.baselineSize()).isLessThanOrEqualTo(31);
    }

    @Test
    @DisplayName("LOST: no fix for more than 60 s; 60 s or less without a fix is only degraded")
    void lost() {
        GnssIntegrityEvaluator e = withBaseline(true);
        Second noFix = new Second(1, null, null, null, 0, null, null, null);
        List<Assessment> out = run(e, 150, 3, noFix);
        out.addAll(run(e, 240, 1, Second.good()));
        assertThat(out).extracting(Assessment::state)
                .containsExactly(State.OK, State.DEGRADED, State.DEGRADED, State.LOST);
        assertThat(out.getLast().reason()).contains("60 s");
    }

    @Test
    @DisplayName("LOST from the start when no fix ever arrives")
    void lostFromStart() {
        GnssIntegrityEvaluator e = new GnssIntegrityEvaluator(true);
        List<Assessment> out = run(e, 0, 4, new Second(1, null, null, null, 0, null, null, null));
        assertThat(out).extracting(Assessment::state).containsExactly(State.DEGRADED, State.DEGRADED, State.LOST);
    }

    @Test
    @DisplayName("JAMMED: the receiver's own jamming state, or a 10 dB C/N0 drop with half the satellites lost")
    void jammed() {
        GnssIntegrityEvaluator e = withBaseline(true);
        Assessment reported = next(e, 150, new Second(3, LAT, LON, 4.0, 8, 40.0, 100L, 3));
        assertThat(reported.state()).isEqualTo(State.JAMMED);
        assertThat(reported.window().jamIndicator()).isEqualTo(150);

        GnssIntegrityEvaluator f = withBaseline(true);
        Assessment inferred = next(f, 150, new Second(3, LAT, LON, 9.0, 4, 29.0, 100L, null));
        assertThat(inferred.state()).isEqualTo(State.JAMMED);
        assertThat(inferred.reason()).contains("C/N0 fell");

        GnssIntegrityEvaluator g = withBaseline(true);
        assertThat(next(g, 150, new Second(3, LAT, LON, 4.0, 7, 29.0, 100L, null)).state())
                .as("a C/N0 drop alone is not jamming").isEqualTo(State.OK);
    }

    @Test
    @DisplayName("SUSPECT_SPOOF: a fixed station jumps more than 500 m, or its clock steps over 1 s, while C/N0 rises")
    void spoof() {
        GnssIntegrityEvaluator e = withBaseline(true);
        Assessment jump = next(e, 150, new Second(3, LAT + 0.01, LON, 4.0, 8, 46.0, 100L, null));
        assertThat(jump.state()).isEqualTo(State.SUSPECT_SPOOF);
        assertThat(jump.reason()).contains("moved");

        GnssIntegrityEvaluator f = withBaseline(true);
        Assessment step = next(f, 150, new Second(3, LAT, LON, 4.0, 8, 46.0, 2_000_000_000L, null));
        assertThat(step.state()).isEqualTo(State.SUSPECT_SPOOF);
        assertThat(step.reason()).contains("clock");

        GnssIntegrityEvaluator g = withBaseline(true);
        assertThat(next(g, 150, new Second(3, LAT + 0.01, LON, 4.0, 8, 38.0, 100L, null)).state())
                .as("no C/N0 rise, no spoofing").isEqualTo(State.OK);

        GnssIntegrityEvaluator moving = withBaseline(false);
        assertThat(next(moving, 150, new Second(3, LAT + 0.01, LON, 4.0, 8, 46.0, 100L, null)).state())
                .as("a moving station is never judged by position").isEqualTo(State.OK);
    }

    @Test
    @DisplayName("DEGRADED: eph above three times the baseline, or fewer than four satellites")
    void degraded() {
        GnssIntegrityEvaluator e = withBaseline(true);
        assertThat(next(e, 150, new Second(3, LAT, LON, 13.0, 8, 40.0, 100L, null)).state()).isEqualTo(State.DEGRADED);
        GnssIntegrityEvaluator f = withBaseline(true);
        assertThat(next(f, 150, new Second(3, LAT, LON, 4.0, 3, 40.0, 100L, null)).state()).isEqualTo(State.DEGRADED);
        GnssIntegrityEvaluator g = new GnssIntegrityEvaluator(true);
        assertThat(run(g, 0, 2, new Second(3, LAT, LON, 40.0, 3, 40.0, null, null)))
                .extracting(Assessment::state).containsExactly(State.DEGRADED);
    }

    @Test
    @DisplayName("window summary: the last fix, means and the last values; SKY before the first TPV seeds the window")
    void summary() {
        GnssIntegrityEvaluator e = new GnssIntegrityEvaluator(true);
        e.onSky(10, 6, 1.1, null);
        e.onClockOffset(5L);
        e.onReceiverJamming(1, 1);
        assertThat(e.onTpv(null, 3, LAT, LON, 2.0)).isEmpty();
        List<Assessment> out = run(e, 0, 2, Second.good());
        GnssIntegrityEvaluator.Window w = out.getFirst().window();
        assertThat(w.start()).isEqualTo(T0);
        assertThat(w.lastTime()).isEqualTo(T0.plusSeconds(29));
        assertThat(w.lat()).isEqualTo(LAT);
        assertThat(w.uSat()).isEqualTo(8);
        assertThat(w.nSat()).isEqualTo(12);
        assertThat(w.cn0MeanDb()).isEqualTo(40.0);
        assertThat(w.clockOffsetNs()).isEqualTo(100L);
        assertThat(GnssIntegrityEvaluator.meters(LAT, LON, LAT + 0.01, LON)).isBetween(1100.0, 1120.0);
    }
}
