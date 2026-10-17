package ai.foresignal.impact.rate;

import ai.foresignal.impact.ImpactPairs;
import java.time.Instant;
import java.util.Collection;

/**
 * Counts historical co-occurrences of a hazard class and an impact family in the same admin1 region (prompt 127 §3).
 */
public interface CoOccurrenceCounter {

    /**
     * The counts.
     *
     * @param n hazard occurrences (admin1 region × UTC day)
     * @param impacts the occurrences followed by the impact in the same region within the lag
     */
    record Counts(long n, long impacts) {}

    /**
     * Counts hazard occurrences in {@code [from, to)} in admin1 regions of {@code countries}, and those followed by the
     * impact within {@code lagHours}.
     *
     * @param hazard the hazard class
     * @param family the impact family
     * @param lagHours the lag
     * @param countries ISO 3166-1 alpha-2 codes of the region class
     * @param from the start of history
     * @param to the end (exclusive) of the hazard occurrences counted
     */
    Counts count(ImpactPairs.HazardClass hazard, ImpactPairs.ImpactFamily family, int lagHours,
            Collection<String> countries, Instant from, Instant to);
}
