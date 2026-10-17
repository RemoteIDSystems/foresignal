package ai.foresignal.exercise;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Exercise runs (prompt 115). */
public interface ExerciseRunRepository extends JpaRepository<ExerciseRunEntity, String> {

    /** Runs in any of {@code statuses}. */
    List<ExerciseRunEntity> findByStatusIn(Collection<String> statuses);

    /** True when a run is in any of {@code statuses}. */
    boolean existsByStatusIn(Collection<String> statuses);
}
