package ai.foresignal.view;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** The saved views (prompt 036). */
public interface SavedViewRepository extends JpaRepository<SavedViewEntity, UUID> {

    /** The view with {@code slug}, if any. */
    Optional<SavedViewEntity> findBySlug(String slug);

    /** The views {@code ownerPrincipalId} owns (prompt R2-012), by slug. */
    List<SavedViewEntity> findByOwnerPrincipalIdOrderBySlugAsc(String ownerPrincipalId);
}
