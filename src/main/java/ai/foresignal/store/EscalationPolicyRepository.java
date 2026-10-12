package ai.foresignal.store;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Escalation policies (prompt 033). */
public interface EscalationPolicyRepository extends JpaRepository<EscalationPolicyEntity, UUID> {}
