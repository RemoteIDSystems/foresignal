package ai.foresignal.collab;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code saved_search} table. */
public interface SavedSearchRepository extends JpaRepository<SavedSearchEntity, String> {

    /** Saved searches that notify, by id. */
    List<SavedSearchEntity> findByNotifyTrueOrderByIdAsc();

    /** Every saved search, by id. */
    List<SavedSearchEntity> findAllByOrderByIdAsc();
}
