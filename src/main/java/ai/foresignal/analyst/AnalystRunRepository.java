package ai.foresignal.analyst;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code analyst_run} access (prompt 102). */
public interface AnalystRunRepository extends JpaRepository<AnalystRunEntity, String> {

    /** A profile's runs, newest first. */
    List<AnalystRunEntity> findByProfileIdOrderByStartedAtDesc(String profileId, Limit limit);

    /** All runs, newest first. */
    List<AnalystRunEntity> findAllByOrderByStartedAtDescIdDesc(Limit limit);

    /** Runs since an instant, for the evaluation aggregates. */
    List<AnalystRunEntity> findByStartedAtGreaterThanEqualOrderByIdAsc(Instant since);

    /** How many runs a profile started since an instant. */
    long countByProfileIdAndStartedAtGreaterThanEqual(String profileId, Instant since);
}
