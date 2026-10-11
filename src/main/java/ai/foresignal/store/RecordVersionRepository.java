package ai.foresignal.store;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Read access to record revisions.
 *
 * <p>{@link RecordVersionEntity} is immutable and written by the ingest writer in JDBC batches
 * (Architecture §6.2), so this repository is for reads. Later prompts add the native queries the
 * API and tiles need.
 */
public interface RecordVersionRepository extends JpaRepository<RecordVersionEntity, Long> {

    Optional<RecordVersionEntity> findBySourceIdAndExternalIdAndVersion(
            String sourceId, String externalId, int version);

    List<RecordVersionEntity> findBySourceIdAndExternalIdOrderByVersionDesc(
            String sourceId, String externalId);

    long countBySourceId(String sourceId);
}
