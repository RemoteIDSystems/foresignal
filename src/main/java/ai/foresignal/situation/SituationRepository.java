package ai.foresignal.situation;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

/** Situations (prompt 118). */
public interface SituationRepository extends JpaRepository<SituationEntity, String> {

    /** Situations in {@code statuses} active since {@code since}, the linker's candidates, oldest first. */
    List<SituationEntity> findByStatusInAndLastActivityAtGreaterThanEqualOrderByIdAsc(Collection<String> statuses,
            Instant since);

    /** Every situation in {@code statuses}, oldest first (the lifecycle sweep and the snapshot job). */
    List<SituationEntity> findByStatusInOrderByIdAsc(Collection<String> statuses);

    /** Every situation, by id, a page at a time (the record publisher's full pass). */
    Slice<SituationEntity> findAllByOrderByIdAsc(Pageable page);

    /** Situations changed since {@code since}, by id (the record publisher's incremental pass). */
    Slice<SituationEntity> findByUpdatedAtGreaterThanEqualOrderByIdAsc(Instant since, Pageable page);

    /** Situations whose brief is wanted (the brief retry tick). */
    List<SituationEntity> findByBriefPendingTrueOrderByIdAsc();
}
