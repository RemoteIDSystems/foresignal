package ai.foresignal.store;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookRepository extends JpaRepository<WebhookEntity, UUID> {

    List<WebhookEntity> findByEnabledTrue();
}
