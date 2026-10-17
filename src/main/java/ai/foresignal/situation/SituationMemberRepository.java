package ai.foresignal.situation;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Situation members (prompt 118). */
public interface SituationMemberRepository extends JpaRepository<SituationMemberEntity, String> {

    /** The members of a situation in the order they were added. */
    List<SituationMemberEntity> findBySituationIdOrderByAddedAtAscIdAsc(String situationId);

    /** The memberships of one object across situations. */
    List<SituationMemberEntity> findByMemberTypeAndRef(String memberType, String ref);

    /** The memberships of several objects of one type. */
    List<SituationMemberEntity> findByMemberTypeAndRefIn(String memberType, Collection<String> refs);

    /** The members of several situations. */
    List<SituationMemberEntity> findBySituationIdIn(Collection<String> situationIds);

    /** How many members a situation has. */
    long countBySituationId(String situationId);
}
