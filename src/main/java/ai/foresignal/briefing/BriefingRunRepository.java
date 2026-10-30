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

    /**
     * The most recent run of a briefing with one of the completed statuses that is not a breaking-news run (NP-006):
     * a scheduled edition's period starts where the previous scheduled edition ended.
     */
    Optional<BriefingRunEntity> findFirstByBriefingIdAndStatusInAndBreakingIsNullOrderByStartedAtDescIdDesc(
            UUID briefingId, java.util.Collection<String> statuses);

    /** The most recent run of a briefing, whatever its status. */
    Optional<BriefingRunEntity> findFirstByBriefingIdOrderByStartedAtDescIdDesc(UUID briefingId);

    /** The ten most recent runs with one of {@code statuses}, across briefings (prompt 038: the glance). */
    java.util.List<BriefingRunEntity> findTop10ByStatusInOrderByStartedAtDescIdDesc(java.util.Collection<String> statuses);

    /**
     * The most recent runs of a briefing whose status is not {@code status} (prompt 058: the run feeds
     * pass {@code running}), newest first, one page.
     */
    java.util.List<BriefingRunEntity> findByBriefingIdAndStatusNotOrderByStartedAtDescIdDesc(UUID briefingId,
            String status, org.springframework.data.domain.Pageable page);

    /** True when the briefing has any stored run. */
    boolean existsByBriefingId(UUID briefingId);
}
