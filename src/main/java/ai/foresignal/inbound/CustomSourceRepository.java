package ai.foresignal.inbound;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code custom_source} rows (prompt 024). */
public interface CustomSourceRepository extends JpaRepository<CustomSourceEntity, String> {

    /** The row whose ingest key hashes to {@code hash}. */
    Optional<CustomSourceEntity> findByIngestKeyHash(String hash);

    /** Every row of one origin, ordered by id. */
    List<CustomSourceEntity> findByOriginOrderBySourceId(String origin);
}
