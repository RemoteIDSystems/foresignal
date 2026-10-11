package ai.foresignal.collect;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * The frame-age check product sources contribute through {@link HealthRule}
 * (Weather-Imagery-Design §2, Health row).
 *
 * <p>Degraded when the newest frame is older than {@code maxGap} when the product declares one,
 * otherwise older than {@value #CADENCE_MULTIPLIER} × its cadence. Three cadences rather than one,
 * because upstream publication jitters and one late frame is not a broken product.
 */
public final class FrameAgeRule {

    /** How many cadences may pass without a frame before the product is stale. */
    public static final int CADENCE_MULTIPLIER = 3;

    private FrameAgeRule() {}

    /**
     * Judges a product's newest frame.
     *
     * @param newestFrameAt the newest frame time, or null when there is none yet
     * @param cadence the product's publication cadence
     * @param maxGap the largest gap the product tolerates, or null to use the cadence rule
     * @param now the evaluation instant
     * @return empty when there is no frame yet or no threshold can be derived
     */
    public static Optional<HealthVerdict> verdict(
            Instant newestFrameAt, Duration cadence, Duration maxGap, Instant now) {
        if (newestFrameAt == null) {
            return Optional.empty();
        }
        Duration limit = threshold(cadence, maxGap);
        if (limit == null) {
            return Optional.empty();
        }
        Duration age = Duration.between(newestFrameAt, now);
        if (age.compareTo(limit) > 0) {
            return Optional.of(
                    HealthVerdict.degraded(
                            "newest frame is " + age.toSeconds() + "s old (limit " + limit.toSeconds() + "s)",
                            newestFrameAt));
        }
        return Optional.of(HealthVerdict.healthy(newestFrameAt));
    }

    /** {@code maxGap} when positive, else {@code 3 × cadence}, else null. */
    static Duration threshold(Duration cadence, Duration maxGap) {
        if (maxGap != null && maxGap.isPositive()) {
            return maxGap;
        }
        if (cadence != null && cadence.isPositive()) {
            return cadence.multipliedBy(CADENCE_MULTIPLIER);
        }
        return null;
    }
}
