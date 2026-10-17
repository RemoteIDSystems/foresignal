package ai.foresignal.benchmark;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code benchmark_case} rows (prompt 123). */
public interface BenchmarkCaseRepository extends JpaRepository<BenchmarkCaseEntity, String> {

    /** All cases in id order. */
    List<BenchmarkCaseEntity> findAllByOrderByIdAsc();

    /** The cases of one source. */
    List<BenchmarkCaseEntity> findBySource(String source);
}
