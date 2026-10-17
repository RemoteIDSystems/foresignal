package ai.foresignal.pol;

import ai.foresignal.source.HealthStatus;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

/**
 * The deviation rules of the pattern-of-life analysis (prompt 114). Pure.
 *
 * <ul>
 *   <li>{@code NEW_PLACE}: a stay of at least {@code minDwell} outside every place, once the subject has at least
 *       {@link #WARM_UP} (14 d) of history.
 *   <li>{@code UNUSUAL_TIME}: a stay at a known place arriving in an hour-of-week bucket holding less than
 *       {@link #UNUSUAL_SHARE} (2%) of that place's dwell.
 *   <li>{@code EXPECTED_ABSENT}: now is in a bucket where one place normally holds at least {@link #EXPECTED_SHARE}
 *       (60%) of the subject's dwell in that bucket (and at least {@link #MIN_BUCKET_MINUTES}), but the subject has
 *       been elsewhere, or unseen, for more than {@link #ABSENT_AFTER} (1 h). Health-gated: suppressed unless every
 *       member source is healthy.
 * </ul>
 */
public final class PolDeviations {

    /** {@code NEW_PLACE}. */
    public static final String NEW_PLACE = "NEW_PLACE";

    /** {@code UNUSUAL_TIME}. */
    public static final String UNUSUAL_TIME = "UNUSUAL_TIME";

    /** {@code EXPECTED_ABSENT}. */
    public static final String EXPECTED_ABSENT = "EXPECTED_ABSENT";

    /** The history a subject needs before {@code NEW_PLACE} is reported. */
    public static final Duration WARM_UP = Duration.ofDays(14);

    /** The bucket share below which a stay's time is unusual. */
    public static final double UNUSUAL_SHARE = 0.02;

    /** The bucket share from which a place is expected. */
    public static final double EXPECTED_SHARE = 0.6;

    /** The least dwell a bucket needs at the place before absence is reported, minutes. */
    public static final int MIN_BUCKET_MINUTES = 30;

    /** How long the subject must be elsewhere or unseen. */
    public static final Duration ABSENT_AFTER = Duration.ofHours(1);

    /** The tick of the {@code EXPECTED_ABSENT} check. */
    public static final Duration ABSENT_TICK = Duration.ofMinutes(10);

    private PolDeviations() {}

    /** True when a stay arriving at {@code arrive} outside every place is a {@code NEW_PLACE}. */
    public static boolean isNewPlace(Instant arrive, Instant historyStart) {
        return historyStart != null && !arrive.isBefore(historyStart.plus(WARM_UP));
    }

    /**
     * The share of {@code bucket} in a place's histogram when it is below {@link #UNUSUAL_SHARE} (an
     * {@code UNUSUAL_TIME}), else null; null for a place without dwell.
     */
    public static Double unusualTimeShare(int[] histogram, int bucket) {
        long total = PolStats.total(histogram);
        if (total <= 0) {
            return null;
        }
        double share = histogram[bucket] / (double) total;
        return share < UNUSUAL_SHARE ? share : null;
    }

    /**
     * The expected place of {@code bucket}.
     *
     * @param index the index in the given list
     * @param share its share of the subject's dwell in the bucket
     */
    public record Expected(int index, double share) {}

    /** The place holding at least 60% of the subject's dwell in {@code bucket}, or null. */
    public static Expected expectedPlace(List<int[]> histograms, int bucket) {
        long sum = 0;
        for (int[] h : histograms) {
            sum += h[bucket];
        }
        if (sum <= 0) {
            return null;
        }
        for (int i = 0; i < histograms.size(); i++) {
            int minutes = histograms.get(i)[bucket];
            double share = minutes / (double) sum;
            if (minutes >= MIN_BUCKET_MINUTES && share >= EXPECTED_SHARE) {
                return new Expected(i, share);
            }
        }
        return null;
    }

    /**
     * True when the subject is absent from its expected place.
     *
     * @param now the evaluation time
     * @param atPlace true when the latest fix is at the place
     * @param lastFixAt the latest fix's time, or null when never seen
     * @param lastAtPlace the latest time the subject was at the place, or null
     */
    public static boolean absent(Instant now, boolean atPlace, Instant lastFixAt, Instant lastAtPlace) {
        boolean unseen = lastFixAt == null || Duration.between(lastFixAt, now).compareTo(ABSENT_AFTER) > 0;
        boolean elsewhere = !atPlace
                && (lastAtPlace == null || Duration.between(lastAtPlace, now).compareTo(ABSENT_AFTER) > 0);
        return unseen || elsewhere;
    }

    /** True when the {@code EXPECTED_ABSENT} check is due: never run, or {@link #ABSENT_TICK} since the last one. */
    public static boolean absentCheckDue(Instant lastCheck, Instant now) {
        return lastCheck == null || Duration.between(lastCheck, now).compareTo(ABSENT_TICK) >= 0;
    }

    /** True when every member source is healthy (the {@code EXPECTED_ABSENT} health gate); false without any. */
    public static boolean healthy(Collection<HealthStatus> statuses) {
        return !statuses.isEmpty() && statuses.stream().allMatch(s -> s == HealthStatus.HEALTHY);
    }
}
