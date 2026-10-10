package ai.elaro.store;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDeliveryEntity, Long> {

    /** What the retry worker should attempt now, oldest first. */
    List<WebhookDeliveryEntity> findByStatusAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(
            DeliveryStatus status, Instant due, Limit limit);

    /**
     * Used by retention: settled deliveries older than the cutoff (Architecture §11 step 3).
     *
     * <p>Ages a delivered row by when it was delivered and a failed one by its last attempt, since
     * a failed row never gets a {@code delivered_at}.
     */
    @Modifying
    @Query(
            """
            delete from WebhookDeliveryEntity d
             where d.status in (ai.elaro.store.DeliveryStatus.DELIVERED,
                                ai.elaro.store.DeliveryStatus.FAILED)
               and coalesce(d.deliveredAt, d.nextAttemptAt) < :before
            """)
    int deleteSettledBefore(@Param("before") Instant before);
}
