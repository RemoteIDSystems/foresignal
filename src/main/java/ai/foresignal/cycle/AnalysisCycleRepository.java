package ai.foresignal.cycle;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code analysis_cycle} rows (prompt 128). */
public interface AnalysisCycleRepository extends JpaRepository<AnalysisCycleEntity, String> {

    /** Every cycle, by id. */
    List<AnalysisCycleEntity> findAllByOrderByIdAsc();

    /** The enabled cycles of a cadence. */
    List<AnalysisCycleEntity> findByCadenceAndEnabledTrue(String cadence);

    /** Whether any built-in cycle exists. */
    boolean existsByBuiltInTrue();
}
