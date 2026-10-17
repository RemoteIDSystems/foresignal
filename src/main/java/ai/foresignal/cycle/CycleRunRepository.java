package ai.foresignal.cycle;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code cycle_run} rows (prompt 128). */
public interface CycleRunRepository extends JpaRepository<CycleRunEntity, String> {

    /** A cycle's runs, newest first. */
    List<CycleRunEntity> findByCycleIdOrderByIdDesc(String cycleId, Limit limit);

    /** A cycle's runs older than {@code before}, newest first. */
    List<CycleRunEntity> findByCycleIdAndIdLessThanOrderByIdDesc(String cycleId, String before, Limit limit);

    /** The newest run of a cycle in one of {@code statuses}. */
    Optional<CycleRunEntity> findTop1ByCycleIdAndStatusInOrderByIdDesc(String cycleId, Collection<String> statuses);

    /** The newest run of a situation in one of {@code statuses}. */
    Optional<CycleRunEntity> findTop1BySituationIdAndStatusInOrderByIdDesc(String situationId,
            Collection<String> statuses);

    /** Runs of a cycle in one of {@code statuses}. */
    long countByCycleIdAndStatusIn(String cycleId, Collection<String> statuses);

    /** Runs of a situation in one of {@code statuses}. */
    long countBySituationIdAndStatusIn(String situationId, Collection<String> statuses);
}
