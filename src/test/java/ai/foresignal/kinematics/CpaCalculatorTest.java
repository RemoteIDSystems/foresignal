package ai.foresignal.kinematics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.trigger.ApproachCondition;
import ai.foresignal.trigger.Evaluation;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The shared CPA maths (prompt 114): head-on, parallel, diverging (tcpa clamped to 0), vertical separation, the
 * 500 km limit, ETA approaching and not approaching, staleness, and the approach judge.
 */
class CpaCalculatorTest {

    static final Instant T = Instant.parse("2026-10-16T12:00:00Z");
    static final Duration H = Duration.ofHours(2);

    static KinematicState state(double lon, double lat, double speed, double course) {
        return new KinematicState("s", lon + "," + lat, "vessel", lon, lat, T, speed, course, null, false, false);
    }

    @Test
    @DisplayName("head-on: two vessels 10 km apart closing at 10 m/s each meet after about 500 s")
    void headOn() {
        double dLon = 10_000 / (GeoMath.METERS_PER_DEGREE_LON);
        CpaCalculator.Cpa cpa = CpaCalculator.cpa(state(0, 0, 10, 90), state(dLon, 0, 10, 270), H);
        assertThat(cpa.inRange()).isTrue();
        assertThat(cpa.tcpaSeconds()).isCloseTo(500, within(1.0));
        assertThat(cpa.dcpaMeters()).isLessThan(1.0);
        assertThat(cpa.cpaAt()).isEqualTo(T.plusMillis(Math.round(cpa.tcpaSeconds() * 1000)));
        assertThat(cpa.separationMeters()).isCloseTo(10_000, within(20.0));
    }

    @Test
    @DisplayName("parallel at the same speed: no closing velocity, tcpa 0 and dcpa the current separation")
    void parallel() {
        double dLat = 2_000 / GeoMath.METERS_PER_DEGREE_LAT;
        CpaCalculator.Cpa cpa = CpaCalculator.cpa(state(0, 0, 8, 0), state(0.01, dLat, 8, 0), H);
        assertThat(cpa.tcpaSeconds()).isZero();
        assertThat(cpa.dcpaMeters()).isCloseTo(Math.hypot(0.01 * GeoMath.METERS_PER_DEGREE_LON, 2_000), within(5.0));
    }

    @Test
    @DisplayName("diverging: the raw tcpa is negative, so it is clamped to 0")
    void diverging() {
        double dLon = 5_000 / GeoMath.METERS_PER_DEGREE_LON;
        CpaCalculator.Cpa cpa = CpaCalculator.cpa(state(0, 0, 10, 270), state(dLon, 0, 10, 90), H);
        assertThat(cpa.tcpaSeconds()).isZero();
        assertThat(cpa.dcpaMeters()).isCloseTo(5_000, within(10.0));
        CpaCalculator.Raw raw = CpaCalculator.closest(5_000, 0, 20, 0, 1e-7);
        assertThat(raw.tcpa()).isNegative();
    }

    @Test
    @DisplayName("a closest approach beyond the horizon is clamped to the horizon")
    void horizonClamp() {
        double dLon = 100_000 / GeoMath.METERS_PER_DEGREE_LON;
        CpaCalculator.Cpa cpa = CpaCalculator.cpa(state(0, 0, 5, 90), state(dLon, 0, 0, 0), Duration.ofMinutes(10));
        assertThat(cpa.tcpaSeconds()).isEqualTo(600.0);
        assertThat(cpa.dcpaMeters()).isCloseTo(97_000, within(50.0));
    }

    @Test
    @DisplayName("vertical separation is the altitude difference when both have altitude, else null")
    void vertical() {
        KinematicState a = new KinematicState("s", "a", "aircraft", 0, 0, T, 200.0, 90.0, 10_000.0, false, false);
        KinematicState b = new KinematicState("s", "b", "aircraft", 0.5, 0, T, 200.0, 270.0, 10_300.0, false, false);
        assertThat(CpaCalculator.cpa(a, b, H).verticalSeparationM()).isEqualTo(300.0);
        assertThat(CpaCalculator.cpa(a, state(0.5, 0, 200, 270), H).verticalSeparationM()).isNull();
    }

    @Test
    @DisplayName("pairs more than 500 km apart are out of range")
    void range() {
        assertThat(CpaCalculator.cpa(state(0, 0, 10, 90), state(4.4, 0, 10, 270), H).inRange()).isTrue();
        assertThat(CpaCalculator.cpa(state(0, 0, 10, 90), state(4.6, 0, 10, 270), H).inRange()).isFalse();
    }

    @Test
    @DisplayName("both states are projected to t0 = max(observedAt) before the CPA")
    void projection() {
        KinematicState older = new KinematicState("s", "a", "vessel", 0, 0, T.minusSeconds(100), 10.0, 90.0, null, false,
                false);
        double dLon = 3_000 / GeoMath.METERS_PER_DEGREE_LON;
        CpaCalculator.Cpa cpa = CpaCalculator.cpa(older, state(dLon, 0, 10, 270), H);
        assertThat(cpa.t0()).isEqualTo(T);
        // 1 km of the 3 km was covered before t0, so 2 km remain at a 20 m/s closing speed.
        assertThat(cpa.tcpaSeconds()).isCloseTo(100, within(1.0));
    }

    @Test
    @DisplayName("ETA: approaching within 45° and 0.5 m/s gives distance / (speed · cos Δ); otherwise no ETA")
    void eta() {
        KinematicState s = state(0, 0, 10, 90);
        double dLon = 10_000 / GeoMath.METERS_PER_DEGREE_LON;
        CpaCalculator.Eta straight = CpaCalculator.eta(s, dLon, 0);
        assertThat(straight.approaching()).isTrue();
        assertThat(straight.bearingDiffDeg()).isCloseTo(0, within(0.1));
        assertThat(straight.etaSeconds()).isCloseTo(1_000, within(5.0));

        CpaCalculator.Eta oblique = CpaCalculator.eta(state(0, 0, 10, 50), dLon, 0);
        assertThat(oblique.approaching()).isTrue();
        assertThat(oblique.etaSeconds()).isCloseTo(oblique.distanceMeters() / (10 * Math.cos(Math.toRadians(40))),
                within(1.0));

        assertThat(CpaCalculator.eta(state(0, 0, 10, 140), dLon, 0).approaching()).as("50° off").isFalse();
        assertThat(CpaCalculator.eta(state(0, 0, 0.4, 90), dLon, 0).approaching()).as("too slow").isFalse();
        CpaCalculator.Eta noCourse = CpaCalculator.eta(new KinematicState("s", "x", null, 0, 0, T, null, null, null,
                false, false), dLon, 0);
        assertThat(noCourse.approaching()).isFalse();
        assertThat(noCourse.etaSeconds()).isNull();
        assertThat(noCourse.bearingDiffDeg()).isNull();
    }

    @Test
    @DisplayName("staleness: age is measured from observedAt, and a state projects along its velocity")
    void staleness() {
        KinematicState s = state(0, 0, 10, 0);
        assertThat(s.age(T.plusSeconds(90))).isEqualTo(Duration.ofSeconds(90));
        assertThat(s.age(T.minusSeconds(5))).isEqualTo(Duration.ZERO);
        double[] p = s.projectedTo(T.plusSeconds(100));
        assertThat((p[1] - 0) * GeoMath.METERS_PER_DEGREE_LAT).isCloseTo(1_000, within(1.0));
        assertThat(s.withStale(true).stale()).isTrue();
        Map<String, Object> map = s.toMap();
        assertThat(map).containsEntry("speedMps", 10.0).containsEntry("stale", false);
        assertThat(KinematicState.fixed(1, 2, T).hasVelocity()).isTrue();
    }

    @Test
    @DisplayName("the approach judge: within the distance and the time matches; a far pair that cannot close does not")
    void judge() {
        double dLon = 4_000 / GeoMath.METERS_PER_DEGREE_LON;
        ApproachCondition near = new ApproachCondition(null, new double[] {dLon, 0}, null, 500, 20);
        assertThat(ApproachEvaluator.judge(near, state(0, 0, 5, 90), KinematicState.fixed(dLon, 0, T)))
                .isEqualTo(Evaluation.MATCH);
        ApproachCondition quick = new ApproachCondition(null, new double[] {dLon, 0}, null, 500, 5);
        assertThat(ApproachEvaluator.judge(quick, state(0, 0, 5, 90), KinematicState.fixed(dLon, 0, T)))
                .as("13 minutes away, 5 allowed").isEqualTo(Evaluation.NO_MATCH);
        assertThat(ApproachEvaluator.judge(near, state(0, 0, 5, 270), KinematicState.fixed(dLon, 0, T)))
                .as("heading away").isEqualTo(Evaluation.NO_MATCH);
        ApproachCondition far = new ApproachCondition(null, new double[] {6, 0}, null, 500, 20);
        assertThat(ApproachEvaluator.judge(far, state(0, 0, 5, 90), KinematicState.fixed(6, 0, T)))
                .isEqualTo(Evaluation.NO_MATCH);
        ApproachCondition jet = new ApproachCondition(null, new double[] {6, 0}, null, 500, 120);
        assertThat(ApproachEvaluator.judge(jet, state(0, 0, 250, 90), KinematicState.fixed(6, 0, T)))
                .as("beyond the frame but able to close").isEqualTo(Evaluation.AMBIGUOUS);
    }

    @Test
    @DisplayName("geodesy helpers: distance, bearing, angle difference and longitude folding")
    void geoMath() {
        assertThat(GeoMath.distanceMeters(0, 0, 1, 0)).isCloseTo(111_195, within(5.0));
        assertThat(GeoMath.bearingDegrees(0, 0, 0, 1)).isCloseTo(0, within(1e-9));
        assertThat(GeoMath.bearingDegrees(0, 0, 1, 0)).isCloseTo(90, within(1e-6));
        assertThat(GeoMath.angleDifference(350, 10)).isEqualTo(20.0);
        assertThat(GeoMath.normalizeLon(359)).isEqualTo(-1.0);
        assertThat(GeoMath.distanceMeters(179.9, 0, -179.9, 0)).isCloseTo(22_239, within(5.0));
    }

    @Test
    @DisplayName("payload fields: speed in knots or m/s, course and altitude in adapter units")
    void fields() {
        assertThat(KinematicFields.speedMps(Map.of("gs", 100))).isCloseTo(51.4444, within(1e-4));
        assertThat(KinematicFields.speedMps(Map.of("speedMs", 3.5, "sog", 10))).isEqualTo(3.5);
        assertThat(KinematicFields.courseDeg(Map.of("cog", 360))).isEqualTo(0.0);
        assertThat(KinematicFields.courseDeg(Map.of("track", 511))).isNull();
        assertThat(KinematicFields.altitudeM(Map.of("alt_baro", 3048))).isEqualTo(3048.0);
        assertThat(KinematicFields.speedMps(Map.of("gs", "fast"))).isNull();
        assertThat(KinematicFields.speedMps(null)).isNull();
    }
}
