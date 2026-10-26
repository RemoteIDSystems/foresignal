package ai.foresignal.store;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code trigger_rule} table. */
public interface TriggerRuleRepository extends JpaRepository<TriggerRuleEntity, UUID> {

    /** The rules the engine should evaluate. */
    List<TriggerRuleEntity> findByEnabledTrue();

    /**
     * The rules a user owns (prompt R2-008).
     *
     * @param ownerPrincipalId the user's principal
     * @return the rules
     */
    List<TriggerRuleEntity> findByOwnerPrincipalId(String ownerPrincipalId);
}
