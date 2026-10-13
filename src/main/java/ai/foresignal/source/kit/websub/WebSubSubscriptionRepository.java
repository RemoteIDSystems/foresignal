package ai.foresignal.source.kit.websub;

import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code websub_subscription} rows (prompt 073), keyed by source id. */
public interface WebSubSubscriptionRepository extends JpaRepository<WebSubSubscriptionEntity, String> {}
