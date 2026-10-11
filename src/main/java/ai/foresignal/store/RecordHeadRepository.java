package ai.foresignal.store;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Access to persisted record heads. Only the ingest writer writes them (invariant I1); head purges
 * run on the writer as {@code HeadPurge} commands (prompt 019), never as a repository delete.
 */
public interface RecordHeadRepository extends JpaRepository<RecordHeadEntity, RecordHeadId> {

    /** Backs {@code /v1/kinds}: the current record count per kind. */
    @Query(
            """
            select new ai.foresignal.store.KindCount(h.kind, count(h))
              from RecordHeadEntity h
             group by h.kind
             order by h.kind asc
            """)
    List<KindCount> countByKind();

    /** Every head of one source. */
    List<RecordHeadEntity> findByIdSourceId(String sourceId);

    /** The number of heads of one source. */
    long countByIdSourceId(String sourceId);
}
