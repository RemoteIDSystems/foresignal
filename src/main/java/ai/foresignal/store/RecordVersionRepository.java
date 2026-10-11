package ai.foresignal.store;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Read access to record versions.
 *
 * <p>{@link RecordVersionEntity} is immutable and written only by the ingest writer with native SQL
 * (invariant I9), so this repository is for reads. History is ordered by {@code seq}, which is the
 * commit order. Later prompts add the native queries the API and tiles need.
 */
public interface RecordVersionRepository extends JpaRepository<RecordVersionEntity, Long> {

    /** One version of one record by its version number. */
    Optional<RecordVersionEntity> findBySourceIdAndExternalIdAndVersion(
            String sourceId, String externalId, int version);

    /** A record's versions, newest first. */
    List<RecordVersionEntity> findBySourceIdAndExternalIdOrderBySeqDesc(
            String sourceId, String externalId);

    /** A record's versions, oldest first. */
    List<RecordVersionEntity> findBySourceIdAndExternalIdOrderBySeqAsc(
            String sourceId, String externalId);
}
