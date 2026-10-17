package ai.foresignal.markets;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The history backfill jobs (prompt 117). */
public interface BackfillJobRepository extends JpaRepository<BackfillJobEntity, String> {

    /** The jobs of a source in the given states. */
    List<BackfillJobEntity> findBySourceIdAndStatusIn(String sourceId, List<String> statuses);

    /** Every job in the given states. */
    List<BackfillJobEntity> findByStatusIn(List<String> statuses);
}
