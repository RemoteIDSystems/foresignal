package ai.foresignal.kinematics;

import java.time.Duration;
import java.time.Instant;

/**
 * Closest point of approach and ETA under constant velocity (prompt 114), in a local equirectangular
 * frame at the pair's midpoint latitude. This is the one implementation: 099's {@code derived-cpa}
 * ({@link ai.foresignal.source.adapters.derived.CpaGenerator}) calls {@link #closest} too and keeps only its
 * vessel banding.
 *
 * <p>The frame is accurate for pairs a few hundred kilometers apart; {@link #MAX_SEPARATION_M} (500 km) is
 * the limit beyond which a caller refuses the question ({@code 400 CPA_RANGE}).
 */
public final class CpaCalculator {

    /** The largest separation at {@code t0} the frame is trusted for, meters. */
    public static final double MAX_SEPARATION_M = 500_000;

    /** The default CPA horizon. */
    public static final Duration DEFAULT_HORIZON = Duration.ofHours(2);

    /** Relative speeds below this (m²/s²) have no meaningful closing velocity. */
    static final double MIN_RELATIVE_SPEED2 = 1e-7;

    /** The slowest speed that counts as moving toward a target, m/s. */
    public static final double MIN_APPROACH_SPEED_MPS = 0.5;

    /** The widest bearing difference that counts as approaching, degrees. */
    public static final double MAX_APPROACH_BEARING_DIFF = 45;

    private CpaCalculator() {}

    /**
     * An unclamped closest approach.
     *
     * @param tcpa time to CPA in the velocity's time unit (negative when the pair is diverging)
     * @param dcpa distance at CPA in the position's unit
     */
    public record Raw(double tcpa, double dcpa) {}

    /**
     * The closest approach of {@code b} relative to {@code a}: {@code tcpa = −(Δp·Δv)/|Δv|²},
     * {@code dcpa = |Δp + Δv·tcpa|}.
     *
     * @param dx the east offset of b from a
     * @param dy the north offset of b from a
     * @param ux the east relative velocity (b − a)
     * @param uy the north relative velocity (b − a)
     * @param minSpeed2 the squared relative speed below which there is no closing velocity
     * @return the raw approach, or null when the relative speed is negligible
     */
    public static Raw closest(double dx, double dy, double ux, double uy, double minSpeed2) {
        double speed2 = ux * ux + uy * uy;
        if (speed2 < minSpeed2) {
            return null;
        }
        double tcpa = -(dx * ux + dy * uy) / speed2;
        return new Raw(tcpa, Math.hypot(dx + ux * tcpa, dy + uy * tcpa));
    }

    /**
     * A CPA result.
     *
     * @param t0 the common time, {@code max(observedAt)}
     * @param separationMeters the separation at {@code t0}
     * @param tcpaSeconds the time to CPA, clamped to {@code [0, horizon]}
     * @param dcpaMeters the distance at CPA
     * @param verticalSeparationM the vertical separation at CPA when both have altitude, else null
     * @param inRange false when the separation exceeds {@link #MAX_SEPARATION_M} (the other fields are then
     *     still filled, but not meaningful)
     */
    public record Cpa(Instant t0, double separationMeters, double tcpaSeconds, double dcpaMeters,
            Double verticalSeparationM, boolean inRange) {

        /** The time of the closest approach. */
        public Instant cpaAt() {
            return t0.plusMillis(Math.round(tcpaSeconds * 1000));
        }
    }

    /**
     * The CPA of two states: both are projected to {@code t0 = max(observedAt)} at constant velocity,
     * then {@code tcpa} is clamped to {@code [0, horizon]}. A state without speed or course counts as
     * stationary. Altitude is held constant, so the vertical separation is the altitude difference.
     *
     * @param a the first state
     * @param b the second state
     * @param horizon the horizon
     */
    public static Cpa cpa(KinematicState a, KinematicState b, Duration horizon) {
        Instant t0 = a.observedAt().isAfter(b.observedAt()) ? a.observedAt() : b.observedAt();
        double[] pa = a.projectedTo(t0);
        double[] pb = b.projectedTo(t0);
        double separation = GeoMath.distanceMeters(pa[0], pa[1], pb[0], pb[1]);
        double[] d = GeoMath.offsetMeters(pa[0], pa[1], pb[0], pb[1]);
        double[] va = a.velocity();
        double[] vb = b.velocity();
        double ux = vb[0] - va[0];
        double uy = vb[1] - va[1];
        double max = Math.max(0, horizon.toMillis() / 1000.0);
        Raw raw = closest(d[0], d[1], ux, uy, MIN_RELATIVE_SPEED2);
        double tcpa = raw == null ? 0 : Math.max(0, Math.min(max, raw.tcpa()));
        double dcpa = Math.hypot(d[0] + ux * tcpa, d[1] + uy * tcpa);
        Double vertical = a.altitudeM() != null && b.altitudeM() != null ? Math.abs(b.altitudeM() - a.altitudeM()) : null;
        return new Cpa(t0, separation, tcpa, dcpa, vertical, separation <= MAX_SEPARATION_M);
    }

    /**
     * An ETA result.
     *
     * @param distanceMeters the great-circle distance to the target
     * @param bearingDeg the bearing from the state to the target
     * @param bearingDiffDeg {@code |course − bearing|}, or null without a course
     * @param approaching true when the bearing difference is ≤ 45° and the speed ≥ 0.5 m/s
     * @param etaSeconds {@code distance / (speed · cos Δ)} when approaching, else null
     */
    public record Eta(double distanceMeters, double bearingDeg, Double bearingDiffDeg, boolean approaching,
            Double etaSeconds) {}

    /** The ETA of {@code state} to {@code (lon, lat)} along its track. */
    public static Eta eta(KinematicState state, double lon, double lat) {
        double distance = GeoMath.distanceMeters(state.lon(), state.lat(), lon, lat);
        double bearing = GeoMath.bearingDegrees(state.lon(), state.lat(), lon, lat);
        if (state.courseDeg() == null || state.speedMps() == null) {
            return new Eta(distance, bearing, null, false, null);
        }
        double diff = GeoMath.angleDifference(state.courseDeg(), bearing);
        boolean approaching = diff <= MAX_APPROACH_BEARING_DIFF && state.speedMps() >= MIN_APPROACH_SPEED_MPS;
        Double eta = approaching ? distance / (state.speedMps() * Math.cos(Math.toRadians(diff))) : null;
        return new Eta(distance, bearing, diff, approaching, eta);
    }
}
