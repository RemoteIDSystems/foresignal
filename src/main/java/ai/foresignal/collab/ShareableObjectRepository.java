package ai.foresignal.collab;

import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code shareable_object} table. */
public interface ShareableObjectRepository extends JpaRepository<ShareableObjectEntity, String> {

    /** Objects owned by {@code owner}, newest change first. */
    List<ShareableObjectEntity> findByOwnerOrderByUpdatedAtDescObjectRefAsc(String owner, Limit limit);

    /** Objects of {@code type} owned by {@code owner}, newest change first. */
    List<ShareableObjectEntity> findByOwnerAndTypeOrderByUpdatedAtDescObjectRefAsc(String owner, String type,
            Limit limit);

    /** Objects with {@code visibility}, newest change first. */
    List<ShareableObjectEntity> findByVisibilityOrderByUpdatedAtDescObjectRefAsc(String visibility, Limit limit);

    /** Registrations of {@code refs}. */
    List<ShareableObjectEntity> findByObjectRefIn(java.util.Collection<String> refs);
}
