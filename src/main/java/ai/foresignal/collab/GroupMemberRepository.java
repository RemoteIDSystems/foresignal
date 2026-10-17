package ai.foresignal.collab;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code group_member} table. */
public interface GroupMemberRepository extends JpaRepository<GroupMemberEntity, String> {

    /** The members of a group, oldest first. */
    List<GroupMemberEntity> findByGroupIdOrderByAddedAtAscIdAsc(String groupId);

    /** The memberships of a principal. */
    List<GroupMemberEntity> findByPrincipalId(String principalId);

    /** One membership. */
    Optional<GroupMemberEntity> findByGroupIdAndPrincipalId(String groupId, String principalId);

    /** Members of any of {@code groupIds}. */
    List<GroupMemberEntity> findByGroupIdIn(Collection<String> groupIds);

    /** How many members a group has. */
    long countByGroupId(String groupId);

    /** Deletes a group's members. */
    void deleteByGroupId(String groupId);
}
