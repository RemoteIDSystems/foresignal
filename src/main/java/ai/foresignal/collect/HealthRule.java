package ai.foresignal.collect;

import java.util.Optional;

/**
 * A pluggable health rule, contributed as a Spring bean (Weather-Imagery-Design §2, Health row).
 *
 * <p>The built-in rules of {@link HealthEvaluator} cover what every source has in common. Some
 * sources have a notion of health those rules cannot see — a raster product whose newest frame is
 * hours old is broken even though every fetch succeeds. Such sources contribute a rule here;
 * {@link FrameAgeRule} is the helper for the frame-age case.
 *
 * <p>Rules run only for an enabled source that has been attempted (a running source), and the more
 * severe of the built-in and contributed statuses wins. A rule that throws is logged and
 * ignored, so one faulty rule cannot hide every source's health.
 */
@FunctionalInterface
public interface HealthRule {

    /**
     * Judges one source.
     *
     * @return a verdict, or empty when the rule does not apply to this source
     */
    Optional<HealthVerdict> evaluate(HealthRuleContext context);
}
