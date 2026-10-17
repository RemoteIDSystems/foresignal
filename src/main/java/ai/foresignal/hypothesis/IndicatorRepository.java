package ai.foresignal.hypothesis;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code indicator} rows (prompt 125). */
public interface IndicatorRepository extends JpaRepository<IndicatorEntity, String> {

    /** The indicators of a set, oldest first. */
    List<IndicatorEntity> findBySetIdOrderByIdAsc(String setId);

    /** The indicators watched by a rule (its watch rule or its first-stage rule). */
    List<IndicatorEntity> findByWatchRuleIdOrEmergingRuleId(UUID watchRuleId, UUID emergingRuleId);

    /** The indicators in some states whose {@code expected_by} has passed. */
    List<IndicatorEntity> findByStateInAndExpectedByLessThanEqual(Collection<String> states, Instant before,
            Limit limit);

    /** The waiting indicators of one signal type. */
    List<IndicatorEntity> findByStateIn(Collection<String> states);
}
