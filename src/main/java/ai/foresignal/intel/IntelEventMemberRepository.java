package ai.foresignal.intel;

import jakarta.persistence.QueryHint;
import java.util.List;
import org.hibernate.jpa.HibernateHints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

/** {@code intelligence_event_member} access (prompt 101). */
public interface IntelEventMemberRepository extends JpaRepository<IntelEventMemberEntity, Long> {

    /** Every member of an event, in insertion order. */
    List<IntelEventMemberEntity> findByEventIdOrderByIdAsc(String eventId);

    /**
     * Every member of an event, in insertion order, loaded read-only: for code that only reads them (scoring,
     * titles). Hibernate keeps no snapshot of a read-only entity and skips it when dirty-checking at a flush, so a
     * large event's members (each with JSON {@code snapshot} and {@code joinReason} maps) cost nothing at every later
     * query in the same transaction. A member already loaded for writing stays writable.
     */
    @QueryHints(@QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true"))
    @Query("select m from IntelEventMemberEntity m where m.eventId = ?1 order by m.id asc")
    List<IntelEventMemberEntity> readByEventIdOrderByIdAsc(String eventId);

    /**
     * The pin keys of an event's members ({@code memberType|sourceId|externalId|seq}, as
     * {@code CorrelationEngine.memberKey}), read without loading the members as entities.
     */
    @Query("select concat(m.memberType, '|', m.sourceId, '|', m.externalId, '|', cast(m.seq as String))"
            + " from IntelEventMemberEntity m where m.eventId = ?1")
    List<String> pinKeys(String eventId);

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
