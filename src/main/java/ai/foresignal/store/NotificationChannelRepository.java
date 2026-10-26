package ai.foresignal.store;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Notification channels (prompt 018). */
public interface NotificationChannelRepository extends JpaRepository<NotificationChannelEntity, UUID> {

    /** Every enabled channel. */
    List<NotificationChannelEntity> findByEnabledTrue();

    /** Every channel, oldest first. */
    List<NotificationChannelEntity> findAllByOrderByCreatedAtAscIdAsc();

    /**
     * The channels a user owns (prompt R2-009).
     *
     * @param ownerPrincipalId the user's principal
     * @return the channels
     */
    List<NotificationChannelEntity> findByOwnerPrincipalId(String ownerPrincipalId);
}
