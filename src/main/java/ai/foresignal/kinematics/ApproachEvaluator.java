package ai.foresignal.kinematics;

import ai.foresignal.trigger.ApproachCondition;
import ai.foresignal.trigger.Evaluation;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

/**
 * Evaluates the trigger condition {@code approach} (prompt 114) with {@link CpaCalculator}.
 *
 * <p>The subject is the version being judged: its position, its time, and speed and course from its payload
 * (or a velocity the caller derived). The target is a place's point or a coordinate (stationary), or a record's
 * current state from {@link KinematicService#state} projected to the subject's time.
 *
 * <p>The result is {@link Evaluation#MATCH} when {@code dcpa ≤ withinMeters} and {@code tcpa ≤ withinMinutes}
 * (with the horizon {@code withinMinutes}), {@link Evaluation#NO_MATCH} when it is not, and
 * {@link Evaluation#AMBIGUOUS} when the answer is unknown: no position, no velocity, an unknown or stale target
 * (older than 2 × its TTL or than 1 h at the subject's time), or a pair beyond the 500 km frame that could still
 * close the distance. Callers treat ambiguity conservatively (Core-Data-Model §11).
 */
@Component
public class ApproachEvaluator {

    private final KinematicService kinematics;
    private final Clock clock;

    /** Creates the evaluator. */
    public ApproachEvaluator(KinematicService kinematics, Clock clock) {
        this.kinematics = kinematics;
        this.clock = clock;
    }

    /** A new evaluation session that caches target states (one per evaluated chunk or gate decision). */
    public Session session() {
        return new Session();
    }

    /** Caches the target records' current states for one pass. */
    public final class Session {

        private final Map<String, Optional<KinematicState>> targets = new HashMap<>();

        private Session() {}

        /** The current state of a target record, cached. */
        Optional<KinematicState> target(String ref) {
            return targets.computeIfAbsent(ref, r -> {
                int slash = r.indexOf('/');
                if (slash <= 0 || slash == r.length() - 1) {
                    return Optional.empty();
                }
                return kinematics.state(r.substring(0, slash), r.substring(slash + 1));
            });
        }

        /**
         * Evaluates {@code condition} for one subject state.
         *
         * @param condition the condition
         * @param sourceId the subject's source
         * @param externalId the subject's entity
         * @param geometry the subject's geometry, or null
         * @param at the subject's time
         * @param speedMps the subject's speed, or null
         * @param courseDeg the subject's course, or null
         */
        public Evaluation evaluate(ApproachCondition condition, String sourceId, String externalId, Geometry geometry,
                Instant at, Double speedMps, Double courseDeg) {
            if (geometry == null || geometry.isEmpty() || at == null || speedMps == null || courseDeg == null) {
                return Evaluation.AMBIGUOUS;
            }
            Point p = KinematicService.representative(geometry);
            KinematicState subject = new KinematicState(sourceId, externalId, null, p.getX(), p.getY(), at, speedMps,
                    courseDeg, null, false, false);
            KinematicState target;
            if (condition.point() != null) {
                target = KinematicState.fixed(condition.point()[0], condition.point()[1], at);
            } else if (condition.placeId() != null) {
                Optional<double[]> point = kinematics.placePoint(condition.placeId());
                if (point.isEmpty()) {
                    return Evaluation.AMBIGUOUS;
                }
                target = KinematicState.fixed(point.get()[0], point.get()[1], at);
            } else {
                if (condition.record().equals(sourceId + "/" + externalId)) {
                    return Evaluation.NO_MATCH;
                }
                Optional<KinematicState> state = target(condition.record());
                if (state.isEmpty() || !state.get().hasVelocity()
                        || kinematics.stale(state.get(), clock.instant())
                        || Duration.between(state.get().observedAt(), at).abs().compareTo(KinematicService.MAX_AGE) > 0) {
                    return Evaluation.AMBIGUOUS;
                }
                target = state.get();
            }
            return judge(condition, subject, target);
        }
    }

    /** The decision for two known states (public for tests). */
    public static Evaluation judge(ApproachCondition condition, KinematicState subject, KinematicState target) {
        Duration horizon = Duration.ofMillis(Math.round(condition.horizonSeconds() * 1000));
        CpaCalculator.Cpa cpa = CpaCalculator.cpa(subject, target, horizon);
        if (!cpa.inRange()) {
            double closing = (speed(subject) + speed(target)) * condition.horizonSeconds();
            return cpa.separationMeters() - closing > condition.withinMeters() ? Evaluation.NO_MATCH
                    : Evaluation.AMBIGUOUS;
        }
        boolean holds = cpa.dcpaMeters() <= condition.withinMeters()
                && cpa.tcpaSeconds() <= condition.horizonSeconds();
        return holds ? Evaluation.MATCH : Evaluation.NO_MATCH;
    }

    private static double speed(KinematicState state) {
        return state.speedMps() == null ? 0 : state.speedMps();
    }
}
