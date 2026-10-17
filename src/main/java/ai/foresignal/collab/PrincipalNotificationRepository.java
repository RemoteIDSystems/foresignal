package ai.foresignal.collab;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code principal_notification} table. */
public interface PrincipalNotificationRepository extends JpaRepository<PrincipalNotificationEntity, String> {

    /** A principal's preferences, oldest first. */
    List<PrincipalNotificationEntity> findByPrincipalIdOrderByCreatedAtAscIdAsc(String principalId);
}
