package ai.elaro.store;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecordHeadRepository extends JpaRepository<RecordHeadEntity, RecordHeadId> {

    /** Backs {@code /v1/kinds}: the current record count per kind. */
    @Query(
            """
            select new ai.elaro.store.KindCount(h.kind, count(h))
              from RecordHeadEntity h
             group by h.kind
             order by h.kind asc
            """)
    List<KindCount> countByKind();

    List<RecordHeadEntity> findByIdSourceId(String sourceId);

    long countByIdSourceId(String sourceId);

    /** Used by retention: current but stale heads, per Architecture §11 step 2. */
    @Modifying
    @Query("delete from RecordHeadEntity h where h.id.sourceId = :sourceId and h.observedAt < :before")
    int deleteBySourceIdAndObservedAtBefore(
            @Param("sourceId") String sourceId, @Param("before") Instant before);
}
