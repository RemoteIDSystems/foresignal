package ai.foresignal.store;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persisted notifications (prompt 018). */
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    /** The first notification of {@code subtype} for an alert (the original of its lifecycle updates). */
    Optional<NotificationEntity> findFirstByAlertIdAndSubtypeOrderByIdAsc(Long alertId, String subtype);

    /** Notifications after {@code id}, oldest first (SSE replay). */
    List<NotificationEntity> findByIdGreaterThanOrderByIdAsc(long id, Limit limit);

    /** Notifications of one event after {@code id}, oldest first (the {@code system} topic replay). */
    List<NotificationEntity> findByEventAndIdGreaterThanOrderByIdAsc(String event, long id, Limit limit);

    /** The newest notification, for the SSE head. */
    Optional<NotificationEntity> findFirstByOrderByIdDesc();

    /** The newest notification of one event, for the {@code system} head. */
    Optional<NotificationEntity> findFirstByEventOrderByIdDesc(String event);
}
