package ai.foresignal.store;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Notification deliveries (prompt 018). The worker's claim is native SQL in {@code DeliveryStore}. */
public interface NotificationDeliveryRepository extends JpaRepository<NotificationDeliveryEntity, Long> {

    /** The deliveries of one notification. */
    List<NotificationDeliveryEntity> findByNotificationIdOrderByIdAsc(long notificationId);

    /** The deliveries folded into a digest delivery, newest first. */
    List<NotificationDeliveryEntity> findByFoldedIntoOrderByIdDesc(long foldedInto);

    /** One channel's deliveries newest first, below {@code beforeId} ({@link Long#MAX_VALUE} for page 1). */
    @Query(
            """
            select d from NotificationDeliveryEntity d
             where d.channelId = :channel
               and d.id < :before
               and d.status in :statuses
             order by d.id desc
            """)
    List<NotificationDeliveryEntity> page(
            @Param("channel") UUID channel,
            @Param("before") long beforeId,
            @Param("statuses") Collection<DeliveryStatus> statuses,
            Limit limit);

    /**
     * The newest delivery carrying the mesh short code {@code shortCode} on one of {@code channelIds} (prompt 084:
     * a {@code #E ACK <code>} reply finds its alert here).
     */
    java.util.Optional<NotificationDeliveryEntity> findFirstByChannelIdInAndShortCodeOrderByIdDesc(
            Collection<UUID> channelIds, String shortCode);

    /** The pending deliveries of a channel. */
    List<NotificationDeliveryEntity> findByChannelIdAndStatus(UUID channelId, DeliveryStatus status);

    /**
     * Used by retention (019): settled deliveries older than the cutoff, aged by delivery for a
     * delivered row and by enqueue time for a failed or suppressed one (033); an {@code unacked} mesh delivery
     * (084) is settled too.
     */
    @Modifying
    @Query(
            """
            delete from NotificationDeliveryEntity d
             where d.status in (ai.foresignal.store.DeliveryStatus.DELIVERED,
                                ai.foresignal.store.DeliveryStatus.FAILED,
                                ai.foresignal.store.DeliveryStatus.SUPPRESSED,
                                ai.foresignal.store.DeliveryStatus.UNACKED)
               and coalesce(d.deliveredAt, d.createdAt) < :before
            """)
    int deleteSettledBefore(@Param("before") Instant before);
}
