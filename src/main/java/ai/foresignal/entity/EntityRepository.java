package ai.foresignal.entity;

import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositories for the canonical entity table (prompt 098). */
public interface EntityRepository extends JpaRepository<EntityEntity, String> {

    /** The first entities of one type in id order, for a keyset page. */
    List<EntityEntity> findByTypeOrderByIdAsc(String type, Limit limit);

    /** Entities whose id is greater than {@code after}, for a keyset page. */
    List<EntityEntity> findByIdGreaterThanOrderByIdAsc(String after, Limit limit);
}
