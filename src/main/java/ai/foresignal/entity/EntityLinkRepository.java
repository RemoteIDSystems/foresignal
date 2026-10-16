package ai.foresignal.entity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/** Links from source records to entities (prompt 098). */
public interface EntityLinkRepository extends JpaRepository<EntityLinkEntity, Long> {

    /** The open link of a record, if any. */
    @Query("select l from EntityLinkEntity l where l.sourceId = :sourceId and l.externalId = :externalId"
            + " and l.validTo is null")
    Optional<EntityLinkEntity> findOpen(
            @Param("sourceId") String sourceId, @Param("externalId") String externalId);

    /** The open links of an entity. */
    @Query("select l from EntityLinkEntity l where l.entityId = :entityId and l.validTo is null")
    List<EntityLinkEntity> findOpenByEntityId(@Param("entityId") String entityId);

    /** Every link of an entity, open or closed, newest first. */
    List<EntityLinkEntity> findByEntityIdOrderByIdDesc(String entityId);

    /** Moves every link of one entity to another (a merge). */
    @Modifying
    @Transactional
    @Query("update EntityLinkEntity l set l.entityId = :to where l.entityId = :from")
    int moveEntityLinks(@Param("from") String from, @Param("to") String to);

    /** Open link counts per source for one entity. */
    @Query("select l.sourceId, count(l) from EntityLinkEntity l where l.entityId = :entityId and l.validTo is null"
            + " group by l.sourceId")
    List<Object[]> openCountsBySource(@Param("entityId") String entityId);

    /**
     * {@code air:} links whose record has not been observed since {@code staleBefore} (prompt 098's
     * callsign-gap closure). Returns {@code (sourceId, externalId)} pairs.
     */
    @Query(value = "select l.source_id, l.external_id from entity_link l"
            + " join record_live rl on rl.source_id = l.source_id and rl.external_id = l.external_id"
            + " where l.valid_to is null and l.key_type = 'callsign' and l.key_value like 'air:%'"
            + " and rl.observed_at < :staleBefore order by l.source_id, l.external_id limit :limit", nativeQuery = true)
    List<Object[]> findStaleAirLinks(@Param("staleBefore") Instant staleBefore, @Param("limit") int limit);

    /** Closes the open links of the given records at {@code at}. */
    @Modifying
    @Transactional
    @Query("update EntityLinkEntity l set l.validTo = :at where l.validTo is null and l.sourceId = :sourceId"
            + " and l.externalId in :externalIds")
    int closeOpenLinks(
            @Param("sourceId") String sourceId,
            @Param("externalIds") List<String> externalIds,
            @Param("at") Instant at);

    /** Deletes closed links whose {@code valid_to} is older than {@code before} (prompt 098 retention). */
    @Modifying
    @Transactional
    @Query("delete from EntityLinkEntity l where l.validTo is not null and l.validTo < :before")
    int deleteClosedBefore(@Param("before") Instant before);
}
