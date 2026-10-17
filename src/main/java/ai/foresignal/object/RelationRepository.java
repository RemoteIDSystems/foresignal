package ai.foresignal.object;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code relation} table. */
public interface RelationRepository extends JpaRepository<RelationEntity, String> {

    /** Every projected relation of an owner, retired ones included. */
    List<RelationEntity> findByOwnerKey(String ownerKey);

    /** The projected relation with a projection key. */
    Optional<RelationEntity> findByProjectionKey(String projectionKey);

    /** How many projected relations exist (to seed a first reconcile). */
    long countByProjectedTrue();

    /** Projected relations of owners whose key starts with a prefix, for a reconcile sweep. */
    List<RelationEntity> findByOwnerKeyIn(Collection<String> ownerKeys);
}
