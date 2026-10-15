package ai.foresignal.source.adapters.wx;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * The GLM lightning jump (prompt 105; Weather-Imagery-Design §7.2; Schultz et al. 2009): a rapid
 * increase in a storm's flash rate. Pure and reproducible (method {@value #METHOD}).
 *
 * <p>Flashes inside a track's polygon (buffered by 5 km) are counted in 2-minute bins. The rate of
 * change is {@code DFRDT = (rate_t − rate_{t−1}) / 2 min}. A jump is reported when the current DFRDT
 * exceeds the mean of the previous {@value #PREVIOUS} DFRDT values by {@value #SIGMA} standard
 * deviations <strong>and</strong> the current rate is at least {@value #RATE_FLOOR_FPM} flashes/min.
 *
 * <p>GLM's detection efficiency over Texas is below 100% (its footprint is about 8–10 km), so the
 * flag is advisory, never a warning.
 */
public final class LightningJump {

    /** The detector version. */
    public static final String METHOD = "2sigma-glm-v1";

    /** The standard-deviation multiplier. */
    public static final double SIGMA = 2.0;

    /** The flash-rate floor, flashes per minute. */
    public static final double RATE_FLOOR_FPM = 10.0;

    /** The bin width. */
    public static final Duration BIN = Duration.ofMinutes(2);

    /** The previous DFRDT values in the deviation. */
    public static final int PREVIOUS = 5;

    /** The window covered: one bin plus {@value #PREVIOUS} DFRDT values. */
    public static final int BINS = PREVIOUS + 2;

    private LightningJump() {}

    /**
     * A detected jump.
     *
     * @param at when it was seen
     * @param sigma the standard deviation of the previous DFRDT values
     * @param rateFpm the current flash rate, flashes per minute
     */
    public record Jump(Instant at, double sigma, double rateFpm) {}

    /**
     * Detects a jump from the flash times inside the track.
     *
     * @param now the frame time
     * @param flashTimes the flash times within the window (any order; only recent ones are used)
     * @return the jump, or empty
     */
    public static Optional<Jump> detect(Instant now, List<Instant> flashTimes) {
        if (flashTimes == null || flashTimes.isEmpty()) {
            return Optional.empty();
        }
        long binMillis = BIN.toMillis();
        long start = now.toEpochMilli() - (long) BINS * binMillis;
        int[] counts = new int[BINS];
        for (Instant t : flashTimes) {
            long ms = t.toEpochMilli();
            if (ms < start || ms > now.toEpochMilli()) {
                continue;
            }
            int bin = (int) ((ms - start) / binMillis);
            if (bin >= 0 && bin < BINS) {
                counts[bin]++;
            }
        }
        double[] rate = new double[BINS];
        for (int i = 0; i < BINS; i++) {
            rate[i] = counts[i] / (binMillis / 60000.0);
        }
        double currentRate = rate[BINS - 1];
        if (currentRate < RATE_FLOOR_FPM) {
            return Optional.empty();
        }
        double[] dfrdt = new double[BINS - 1];
        for (int i = 1; i < BINS; i++) {
            dfrdt[i - 1] = (rate[i] - rate[i - 1]) / (binMillis / 60000.0);
        }
        double current = dfrdt[dfrdt.length - 1];
        double[] previous = new double[PREVIOUS];
        System.arraycopy(dfrdt, dfrdt.length - 1 - PREVIOUS, previous, 0, PREVIOUS);
        double mean = 0;
        for (double v : previous) {
            mean += v;
        }
        mean /= previous.length;
        double variance = 0;
        for (double v : previous) {
            variance += (v - mean) * (v - mean);
        }
        variance /= previous.length;
        double sigma = Math.sqrt(variance);
        if (current > mean + SIGMA * sigma) {
            return Optional.of(new Jump(now, sigma, currentRate));
        }
        return Optional.empty();
    }
}
