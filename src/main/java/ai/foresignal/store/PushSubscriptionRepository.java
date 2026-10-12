package ai.foresignal.store;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Web Push subscriptions (prompt 031). Bulk deletes are native SQL in {@code PushSubscriptionService}. */
public interface PushSubscriptionRepository extends JpaRepository<PushSubscriptionEntity, UUID> {

    /** Every subscription, oldest first (the admin list). */
    List<PushSubscriptionEntity> findAllByOrderByCreatedAtAscIdAsc();

    /** The subscriptions in {@code status}, oldest first. */
    List<PushSubscriptionEntity> findByStatusOrderByCreatedAtAsc(String status);

    /** The subscriptions with this endpoint. */
    List<PushSubscriptionEntity> findByEndpoint(String endpoint);

    /** How many subscriptions of {@code tokenId} are in {@code status}. */
    long countByTokenIdAndStatus(UUID tokenId, String status);
}
