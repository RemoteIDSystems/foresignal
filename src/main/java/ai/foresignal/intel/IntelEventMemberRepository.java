package ai.foresignal.intel;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/** {@code intelligence_event_member} access (prompt 101). */
public interface IntelEventMemberRepository extends JpaRepository<IntelEventMemberEntity, Long> {

    /** Every member of an event, in insertion order. */
    List<IntelEventMemberEntity> findByEventIdOrderByIdAsc(String eventId);

    /** One pinned version's existing member, if any. */
    List<IntelEventMemberEntity> findByEventIdAndMemberTypeAndSourceIdAndExternalIdAndSeq(
            String eventId, String memberType, String sourceId, String externalId, long seq);

    /** How many members an event has. */
    long countByEventId(String eventId);

    /** Moves every member of {@code from} to {@code to} (a merge). */
    @Modifying
    @Query("update IntelEventMemberEntity m set m.eventId = ?1 where m.eventId = ?2")
    int moveAll(String to, String from);

    /** Removes every member of an event. */
    void deleteByEventId(String eventId);
}
