package ai.foresignal.view;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** The saved views (prompt 036). */
public interface SavedViewRepository extends JpaRepository<SavedViewEntity, UUID> {

    /** The view with {@code slug}, if any. */
    Optional<SavedViewEntity> findBySlug(String slug);
}
