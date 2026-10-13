package ai.foresignal.weather;

import ai.foresignal.raster.FrameKeys;
import java.time.Duration;
import java.time.Instant;

/**
 * The citation ids of weather values (prompt 056): {@code wx:{productId}@{frameKey}} for a frame, and
 * {@code wx:{productId}@{run}/{step}} for a forecast step, where {@code run} is the run's ISO time as tools return
 * it. The Ask citation verifier accepts them alongside record, alert and briefing ids.
 */
public final class WeatherCitations {

    /** The prefix of every weather citation id. */
    public static final String PREFIX = "wx:";

    private WeatherCitations() {}

    /** The id of a frame, or null without a frame key. */
    public static String frame(String productId, String frameKey) {
        if (productId == null || frameKey == null || frameKey.isBlank()) {
            return null;
        }
        return PREFIX + productId + "@" + frameKey;
    }

    /** The id of a forecast step, or null without a run. */
    public static String forecast(String productId, String run, Integer step) {
        if (productId == null || run == null || run.isBlank()) {
            return null;
        }
        return PREFIX + productId + "@" + run + "/" + (step == null ? 0 : step);
    }

    /**
     * The id of a value: a forecast step when it has a run, else its frame.
     *
     * @param run the run's ISO time, or null for an observation
     */
    public static String of(String productId, String frameKey, String run, Integer step) {
        return run != null && !run.isBlank() ? forecast(productId, run, step) : frame(productId, frameKey);
    }

    /** The step in whole hours from {@code run} to {@code validTime}. */
    public static int stepHours(Instant run, Instant validTime) {
        return (int) Duration.between(run, validTime).toHours();
    }

    /** The ISO time of a run, as tools return it. */
    public static String runIso(Instant run) {
        return FrameKeys.runIso(run);
    }
}
