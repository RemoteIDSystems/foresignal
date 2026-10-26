package ai.foresignal.security.user;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** The users' data restrictions (prompt R2-013), one row per restricted user. */
public interface UserDataRestrictionRepository extends JpaRepository<UserDataRestrictionEntity, UUID> {}
