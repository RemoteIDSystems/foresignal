package ai.foresignal.trigger.window;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/** {@code window_state} access (prompt 100). */
public interface WindowStateRepository extends JpaRepository<WindowStateEntity, WindowStateEntity.Key> {

    /** Every group of one rule, ordered by group key. */
    List<WindowStateEntity> findByRuleIdOrderByGroupKey(UUID ruleId);

    /** One group, or null. */
    WindowStateEntity findByRuleIdAndGroupKey(UUID ruleId, String groupKey);

    /** Removes every group of a rule. */
    @Transactional
    void deleteByRuleId(UUID ruleId);
}
