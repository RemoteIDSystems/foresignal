package ai.foresignal.security.quota;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code user_quota} rows (prompt R2-026). */
public interface UserQuotaRepository extends JpaRepository<UserQuotaEntity, UUID> {}
