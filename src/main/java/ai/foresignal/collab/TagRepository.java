package ai.foresignal.collab;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code tag} table. */
public interface TagRepository extends JpaRepository<TagEntity, String> {

    /** Tags with a name in a namespace (case-insensitive). */
    List<TagEntity> findByNamespaceAndNameIgnoreCase(String namespace, String name);

    /** Every tag, by id. */
    List<TagEntity> findAllByOrderByIdAsc();
}
