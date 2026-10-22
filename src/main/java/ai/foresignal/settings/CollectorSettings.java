package ai.foresignal.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The collector's runtime settings (prompt 150), stored under {@link SettingKeys#COLLECTOR} (OpenAPI schema
 * {@code CollectorSettings}).
 *
 * @param maxConcurrentPulls how many polling fetches may pull from upstream at once across every source: the size
 *     of the global pull governor ({@code ai.foresignal.collect.PullGovernor}), {@value #MIN_PULLS} to
 *     {@value #MAX_PULLS}, default {@value #DEFAULT_PULLS}. Streams are not counted.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CollectorSettings(int maxConcurrentPulls) {

    /** The default cap. */
    public static final int DEFAULT_PULLS = 2;

    /** The smallest cap. */
    public static final int MIN_PULLS = 1;

    /** The largest cap. */
    public static final int MAX_PULLS = 16;

    /** Clamps {@code maxConcurrentPulls} into {@value #MIN_PULLS}..{@value #MAX_PULLS}, so a stored value never escapes. */
    public CollectorSettings {
        maxConcurrentPulls = clamp(maxConcurrentPulls);
    }

    /** The defaults: {@value #DEFAULT_PULLS} concurrent pulls. */
    public static CollectorSettings defaults() {
        return new CollectorSettings(DEFAULT_PULLS);
    }

    /** {@code value} clamped into {@value #MIN_PULLS}..{@value #MAX_PULLS}. */
    public static int clamp(int value) {
        return Math.max(MIN_PULLS, Math.min(MAX_PULLS, value));
    }
}
