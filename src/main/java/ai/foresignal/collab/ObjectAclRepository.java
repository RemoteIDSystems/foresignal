package ai.foresignal.collab;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code object_acl} table. */
public interface ObjectAclRepository extends JpaRepository<ObjectAclEntity, String> {

    /** The grants of an object, oldest first. */
    List<ObjectAclEntity> findByObjectRefOrderByGrantedAtAscIdAsc(String objectRef);

    /** The grants of several objects. */
    List<ObjectAclEntity> findByObjectRefIn(Collection<String> objectRefs);

    /** The grant of an object to one grantee. */
    Optional<ObjectAclEntity> findByObjectRefAndGrantee(String objectRef, String grantee);

    /** Every grant to any of {@code grantees}, newest first. */
    List<ObjectAclEntity> findByGranteeInOrderByGrantedAtDescIdAsc(Collection<String> grantees);

    /** Deletes the grants of an object. */
    void deleteByObjectRef(String objectRef);

    /** Deletes every grant to a grantee (a deleted group). */
    void deleteByGrantee(String grantee);
}
