package ai.foresignal.backtest;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code precursor_test} access (prompt 103). */
public interface PrecursorTestRepository extends JpaRepository<PrecursorTestEntity, String> {

    /** Every test, newest first. */
    List<PrecursorTestEntity> findAllByOrderByCreatedAtDescIdDesc();

    /** How many tests were created since an instant (the multiple-testing note). */
    long countByCreatedAtGreaterThanEqual(Instant since);
}
