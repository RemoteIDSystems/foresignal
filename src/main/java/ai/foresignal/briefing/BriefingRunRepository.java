package ai.foresignal.briefing;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code briefing_run} rows. */
public interface BriefingRunRepository extends JpaRepository<BriefingRunEntity, UUID> {

    /** The most recent stored run of a briefing that is no longer running. */
    Optional<BriefingRunEntity> findFirstByBriefingIdAndStatusNotOrderByStartedAtDescIdDesc(UUID briefingId,
            String status);

    /** The most recent run of a briefing whose status is one of the completed ones. */
    Optional<BriefingRunEntity> findFirstByBriefingIdAndStatusInOrderByStartedAtDescIdDesc(UUID briefingId,
            java.util.Collection<String> statuses);

    /** The most recent run of a briefing, whatever its status. */
    Optional<BriefingRunEntity> findFirstByBriefingIdOrderByStartedAtDescIdDesc(UUID briefingId);

    /** True when the briefing has any stored run. */
    boolean existsByBriefingId(UUID briefingId);
}
