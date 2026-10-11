package ai.foresignal.store;

import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/** Access to {@code fetch_log}, newest first, paged by keyset on {@code id}. */
public interface FetchLogRepository extends JpaRepository<FetchLogEntity, Long> {

    /** The first page of a source's log, newest first. */
    List<FetchLogEntity> findBySourceIdOrderByIdDesc(String sourceId, Limit limit);

    /** The page after {@code beforeId}: entries with a smaller id, newest first. */
    List<FetchLogEntity> findBySourceIdAndIdLessThanOrderByIdDesc(
            String sourceId, long beforeId, Limit limit);
}
