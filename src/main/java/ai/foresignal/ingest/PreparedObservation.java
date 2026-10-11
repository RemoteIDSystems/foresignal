package ai.foresignal.ingest;

import java.util.Map;
import java.util.Objects;

/**
 * An observation ready for the decision table: carry-forward applied and both hashes computed
 * ({@link IngestPolicyEngine#prepare}).
 *
 * @param observation the observation, with any carried geometry
 * @param staticHash its static hash
 * @param fullHash its full hash
 * @param kinematic its kinematic subset (neither significant nor volatile); empty for a tombstone
 */
public record PreparedObservation(
        Observation observation, long staticHash, long fullHash, Map<String, Object> kinematic) {

    public PreparedObservation {
        Objects.requireNonNull(observation, "observation");
        kinematic = kinematic == null ? Map.of() : kinematic;
    }
}
