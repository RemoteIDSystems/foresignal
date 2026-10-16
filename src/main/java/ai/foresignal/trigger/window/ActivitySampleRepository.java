package ai.foresignal.trigger.window;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/** {@code activity_sample} access (prompt 100). */
public interface ActivitySampleRepository extends JpaRepository<ActivitySampleEntity, ActivitySampleEntity.Key> {

    /** One group's samples, oldest first. */
    List<ActivitySampleEntity> findByRuleIdAndGroupKeyOrderByBucketStartAsc(UUID ruleId, String groupKey);

    /** A rule's samples from {@code since} on, oldest first (the baseline lookback). */
    List<ActivitySampleEntity> findByRuleIdAndBucketStartGreaterThanEqualOrderByBucketStartAsc(UUID ruleId,
            Instant since);

    /** A rule's newest sample, if any (the cursor when none was stored). */
    java.util.Optional<ActivitySampleEntity> findFirstByRuleIdOrderByBucketStartDesc(UUID ruleId);

    /** Every sample of a rule, oldest first. */
    List<ActivitySampleEntity> findByRuleIdOrderByBucketStartAsc(UUID ruleId);

    /** Removes every sample of a rule. */
    @Transactional
    void deleteByRuleId(UUID ruleId);

    /** Removes samples older than {@code before}. */
    @Transactional
    long deleteByBucketStartBefore(Instant before);
}
