package ai.elaro.store;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TriggerRuleRepository extends JpaRepository<TriggerRuleEntity, UUID> {

    /** The rules the engine should evaluate. */
    List<TriggerRuleEntity> findByEnabledTrue();
}
