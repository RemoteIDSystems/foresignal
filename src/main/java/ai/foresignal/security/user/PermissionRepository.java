package ai.foresignal.security.user;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Read access to the seeded {@link PermissionEntity} catalog. */
public interface PermissionRepository extends JpaRepository<PermissionEntity, String> {

    /** The whole catalog, ordered by key. */
    List<PermissionEntity> findAllByOrderByPermissionKeyAsc();
}
