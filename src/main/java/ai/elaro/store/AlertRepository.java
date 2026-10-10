package ai.elaro.store;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlertRepository extends JpaRepository<AlertEntity, Long> {

    /** Cooldown check: has this rule already fired for this record since {@code since}? */
    boolean existsByRuleIdAndSourceIdAndExternalIdAndFiredAtAfter(
            UUID ruleId, String sourceId, String externalId, Instant since);

    /** Used by retention to drop alerts older than the configured window. */
    @Modifying
    @Query("delete from AlertEntity a where a.firedAt < :before")
    int deleteByFiredAtBefore(@Param("before") Instant before);
}
