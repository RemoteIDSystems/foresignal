package ai.foresignal.store;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SourceUsageRepository extends JpaRepository<SourceUsageEntity, SourceUsageId> {

    /**
     * Totals for one source over {@code [from, to)}.
     *
     * <p>Returns empty when the window holds no rows, rather than a row of nulls, so callers can
     * distinguish "no usage recorded" from "zero usage".
     */
    @Query(
            """
            select new ai.foresignal.store.UsageTotals(
                       coalesce(sum(u.requests), 0),
                       coalesce(sum(u.bytes), 0),
                       coalesce(sum(u.errors), 0),
                       coalesce(sum(u.refused), 0))
              from SourceUsageEntity u
             where u.id.sourceId = :sourceId
               and u.id.minute >= :from
               and u.id.minute < :to
             group by u.id.sourceId
            """)
    Optional<UsageTotals> totalsForSource(
            @Param("sourceId") String sourceId,
            @Param("from") Instant from,
            @Param("to") Instant to);

    List<SourceUsageEntity> findByIdSourceIdAndIdMinuteGreaterThanEqualAndIdMinuteLessThanOrderByIdMinuteAsc(
            String sourceId, Instant from, Instant to);

    /** Used by retention to drop usage counters older than the configured window. */
    @Modifying
    @Query("delete from SourceUsageEntity u where u.id.minute < :before")
    int deleteByMinuteBefore(@Param("before") Instant before);
}
