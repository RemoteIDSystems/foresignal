package ai.foresignal.inbound.device;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code ingest_device} rows (prompt 039). */
public interface IngestDeviceRepository extends JpaRepository<IngestDeviceEntity, UUID> {

    /** The device whose key hashes to {@code keyHash}. */
    Optional<IngestDeviceEntity> findByKeyHash(String keyHash);

    /** The devices of {@code sourceId}, oldest first. */
    List<IngestDeviceEntity> findBySourceIdOrderByCreatedAt(String sourceId);
}
