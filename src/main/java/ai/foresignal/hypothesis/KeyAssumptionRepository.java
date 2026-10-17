package ai.foresignal.hypothesis;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code key_assumption} rows (prompt 125). */
public interface KeyAssumptionRepository extends JpaRepository<KeyAssumptionEntity, String> {

    /** The assumptions of a set, oldest first. */
    List<KeyAssumptionEntity> findBySetIdOrderByIdAsc(String setId);
}
