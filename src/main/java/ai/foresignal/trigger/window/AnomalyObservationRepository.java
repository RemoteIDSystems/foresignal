package ai.foresignal.trigger.window;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/** {@code anomaly_observation} access (prompt 100). */
public interface AnomalyObservationRepository extends JpaRepository<AnomalyObservationEntity, Long> {

    /** A rule's latest observations, newest first. */
    List<AnomalyObservationEntity> findTop200ByRuleIdOrderByIdDesc(UUID ruleId);

    /** A rule and group's observations, newest first. */
    List<AnomalyObservationEntity> findTop200ByRuleIdAndGroupKeyOrderByIdDesc(UUID ruleId, String groupKey);

    /** Observations whose bucket starts in {@code [from, to)} and whose id is below {@code before}, newest first. */
    List<AnomalyObservationEntity> findTop500ByBucketStartGreaterThanEqualAndBucketStartLessThanAndIdLessThanOrderByIdDesc(
            Instant from, Instant to, long before);

    /** Removes a rule's observations. */
    @Transactional
    void deleteByRuleId(UUID ruleId);
}
