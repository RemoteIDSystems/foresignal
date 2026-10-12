package ai.foresignal.notify.push.device;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Native push devices (prompt 032). Bulk deletes are native SQL in {@code PushDeviceService}. */
public interface PushDeviceRepository extends JpaRepository<PushDeviceEntity, UUID> {

    /** Every device, oldest first (the admin list). */
    List<PushDeviceEntity> findAllByOrderByCreatedAtAscIdAsc();

    /** The devices one token registered, oldest first. */
    List<PushDeviceEntity> findByTokenIdOrderByCreatedAtAscIdAsc(UUID tokenId);

    /** Devices in a status, oldest first (the fan-out). */
    List<PushDeviceEntity> findByStatusOrderByCreatedAtAsc(String status);

    /** The device of a token hash on a transport. */
    Optional<PushDeviceEntity> findByTransportAndTokenHash(String transport, String tokenHash);

    /** How many devices a token registered. */
    long countByTokenId(UUID tokenId);
}
