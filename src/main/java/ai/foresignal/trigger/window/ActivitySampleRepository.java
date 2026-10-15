package ai.foresignal.trigger.window;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code activity_sample} access (prompt 100). */
public interface ActivitySampleRepository extends JpaRepository<ActivitySampleEntity, ActivitySampleEntity.Key> {

    /** One group's samples, oldest first. */
    List<ActivitySampleEntity> findByRuleIdAndGroupKeyOrderByBucketStartAsc(UUID ruleId, String groupKey);

    /** Every sample of a rule, oldest first. */
    List<ActivitySampleEntity> findByRuleIdOrderByBucketStartAsc(UUID ruleId);

    /** Removes every sample of a rule. */
    void deleteByRuleId(UUID ruleId);

    /** Removes samples older than {@code before}. */
    long deleteByBucketStartBefore(Instant before);
}
