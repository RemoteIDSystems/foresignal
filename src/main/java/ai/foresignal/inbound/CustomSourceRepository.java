package ai.foresignal.inbound;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code custom_source} rows (prompt 024). */
public interface CustomSourceRepository extends JpaRepository<CustomSourceEntity, String> {

    /** The row whose ingest key hashes to {@code hash}. */
    Optional<CustomSourceEntity> findByIngestKeyHash(String hash);

    /** Every row of one origin, ordered by id. */
    List<CustomSourceEntity> findByOriginOrderBySourceId(String origin);

    /** One row by id and owner (prompt SG-001): the self-service ownership check. */
    Optional<CustomSourceEntity> findBySourceIdAndOwnerUserId(String sourceId, UUID ownerUserId);

    /** Every source owned by {@code ownerUserId}, ordered by id (prompt SG-001). */
    List<CustomSourceEntity> findByOwnerUserIdOrderBySourceId(UUID ownerUserId);
}
