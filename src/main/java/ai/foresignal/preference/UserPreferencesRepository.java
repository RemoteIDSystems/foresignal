package ai.foresignal.preference;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** The stored user preferences (prompt R2-012), one row per user. */
public interface UserPreferencesRepository extends JpaRepository<UserPreferencesEntity, UUID> {}
