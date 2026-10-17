package ai.foresignal.analytics.finding;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code analytic_finding} rows (prompt 119). */
public interface AnalyticFindingRepository extends JpaRepository<AnalyticFindingEntity, String> {

    /** The open findings with a dedup key. */
    List<AnalyticFindingEntity> findByDedupKeyAndStatus(String dedupKey, String status);

    /** Every finding, by id (full passes of the record publisher). */
    Slice<AnalyticFindingEntity> findAllByOrderByIdAsc(Pageable page);

    /** The findings changed since {@code since}, by id (incremental passes). */
    Slice<AnalyticFindingEntity> findByUpdatedAtGreaterThanEqualOrderByIdAsc(Instant since, Pageable page);

    /** The findings of a type detected in a range. */
    List<AnalyticFindingEntity> findByFindingTypeAndDetectedAtBetweenOrderByDetectedAtAsc(String type, Instant from,
            Instant to);
}
