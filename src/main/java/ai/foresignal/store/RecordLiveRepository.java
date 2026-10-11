package ai.foresignal.store;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Read access to {@code record_live}. Only the ingest writer writes it (invariant I1); current-state
 * reads join it to {@code record_head} (invariant I5).
 */
public interface RecordLiveRepository extends JpaRepository<RecordLiveEntity, RecordHeadId> {

    /** Every live row of one source. */
    List<RecordLiveEntity> findByIdSourceId(String sourceId);

    /** The number of live rows of one source. */
    long countByIdSourceId(String sourceId);
}
