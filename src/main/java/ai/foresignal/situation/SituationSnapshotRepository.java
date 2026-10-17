package ai.foresignal.situation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Daily situation snapshots (prompt 118). */
public interface SituationSnapshotRepository
        extends JpaRepository<SituationSnapshotEntity, SituationSnapshotEntity.Key> {

    /** The snapshot of a situation for one day. */
    Optional<SituationSnapshotEntity> findBySituationIdAndDay(String situationId, LocalDate day);

    /** The newest snapshot of a situation on or before {@code day}. */
    Optional<SituationSnapshotEntity> findTop1BySituationIdAndDayLessThanEqualOrderByDayDesc(String situationId,
            LocalDate day);

    /** Every snapshot of a situation, oldest first. */
    List<SituationSnapshotEntity> findBySituationIdOrderByDayAsc(String situationId);
}
