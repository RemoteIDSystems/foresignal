package ai.foresignal.social.correlation;

import java.time.Instant;
import java.util.List;

/**
 * Where the comparator looks for independent physical evidence (prompt 137): canonical events and findings of the
 * claim's families with non-social originators, located within the family's tolerance and inside the window, plus
 * agency or sensor records that explicitly negate the claim.
 */
public interface PhysicalEvidenceSource {

    /**
     * What to look for.
     *
     * @param rule the family rule (tolerance, canonical and finding families, negations)
     * @param lon the claim place longitude, or null
     * @param lat the claim place latitude, or null
     * @param regionCode the claim place's 040 region, or null
     * @param from the window start
     * @param to the window end (clipped to now by the caller)
     */
    record Query(CompareRules.FamilyRule rule, Double lon, Double lat, String regionCode, Instant from, Instant to) {}

    /**
     * One piece of physical evidence ({@code SocialPhysicalEvidence}).
     *
     * @param ref the object ref ({@code canonical-event:…}, {@code finding:…}, {@code record:…})
     * @param family the canonical or signal family
     * @param originator the originator id
     * @param originatorClass the originator class ({@code agency}, {@code sensor-network}, {@code derived}, …)
     * @param firstObservedAt the first observation time
     * @param distanceKm the distance from the claim place, or null when matched by region
     * @param negates true for a record that explicitly negates the claim
     */
    record Evidence(String ref, String family, String originator, String originatorClass, Instant firstObservedAt,
            Double distanceKm, boolean negates) {

        /** True for an agency record (official first observation). */
        public boolean official() {
            return "agency".equals(originatorClass);
        }

        /** True for social evidence, which never counts (SOC-1). */
        public boolean social() {
            return "social".equals(originatorClass);
        }
    }

    /** The matching evidence, social originators already left out. */
    List<Evidence> find(Query query);
}
