package ai.foresignal.quality;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** The tuning log (prompt 115). */
public interface RuleTuningLogRepository extends JpaRepository<RuleTuningLogEntity, String> {

    /** A rule's entries, newest first. */
    List<RuleTuningLogEntity> findByRuleIdOrderByIdDesc(UUID ruleId);
}
