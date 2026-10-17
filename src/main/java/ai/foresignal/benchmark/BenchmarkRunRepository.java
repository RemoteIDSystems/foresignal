package ai.foresignal.benchmark;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code benchmark_run} rows (prompt 123). */
public interface BenchmarkRunRepository extends JpaRepository<BenchmarkRunEntity, String> {

    /** The finished runs with a status, newest first. */
    List<BenchmarkRunEntity> findByStatusNotNullOrderByFinishedAtDescIdDesc();

    /** The runs that have not finished. */
    List<BenchmarkRunEntity> findByFinishedAtIsNull();
}
