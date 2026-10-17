package ai.foresignal.hypothesis;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** The {@code hypothesis_set} rows (prompt 125). */
public interface HypothesisSetRepository extends JpaRepository<HypothesisSetEntity, String> {

    /** The set, locked for update, so revisions of one set are written one at a time. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from HypothesisSetEntity s where s.id = :id")
    Optional<HypothesisSetEntity> findLocked(@Param("id") String id);

    /** The sets linked to a ledger question. */
    List<HypothesisSetEntity> findByQuestionId(String questionId);

    /** The sets of a status with a review due before {@code before}. */
    List<HypothesisSetEntity> findByStatusAndNextReviewAtLessThanEqual(String status, Instant before, Limit limit);

    /** The sets of a status. */
    List<HypothesisSetEntity> findByStatusOrderByIdAsc(String status, Limit limit);

    /** The sets with a band change waiting for the rate limit. */
    List<HypothesisSetEntity> findByPendingBandChangeIsNotNull(Limit limit);

    /** Every set ordered by id (record publisher full pass). */
    org.springframework.data.domain.Slice<HypothesisSetEntity> findAllByOrderByIdAsc(
            org.springframework.data.domain.Pageable page);

    /** The sets updated since {@code since} (record publisher incremental pass). */
    org.springframework.data.domain.Slice<HypothesisSetEntity> findByUpdatedAtGreaterThanEqualOrderByIdAsc(
            Instant since, org.springframework.data.domain.Pageable page);
}
