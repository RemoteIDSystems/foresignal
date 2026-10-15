package ai.foresignal.backtest;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code backtest_job} access (prompt 103). */
public interface BacktestJobRepository extends JpaRepository<BacktestJobEntity, String> {

    /** Newest jobs first. */
    List<BacktestJobEntity> findAllByOrderByCreatedAtDescIdDesc(Limit limit);

    /** Newest jobs of a status. */
    List<BacktestJobEntity> findByStatusOrderByCreatedAtDescIdDesc(String status, Limit limit);

    /** Newest jobs of a status and type. */
    List<BacktestJobEntity> findByStatusAndTypeOrderByCreatedAtDescIdDesc(String status, String type, Limit limit);

    /** Jobs of one creator in the given statuses (the one-job-per-token rule). */
    List<BacktestJobEntity> findByCreatedByAndStatusIn(String createdBy, List<String> statuses);

    /** Jobs finished before {@code before} (30-day retention). */
    List<BacktestJobEntity> findByFinishedAtBefore(Instant before);
}
