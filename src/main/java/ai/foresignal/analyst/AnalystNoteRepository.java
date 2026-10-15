package ai.foresignal.analyst;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code analyst_note} access (prompt 102). */
public interface AnalystNoteRepository extends JpaRepository<AnalystNoteEntity, String> {

    /** Notes newest first. */
    List<AnalystNoteEntity> findAllByOrderByCreatedAtDescIdDesc(Limit limit);

    /** A profile's notes, newest first. */
    List<AnalystNoteEntity> findByProfileIdOrderByCreatedAtDescIdDesc(String profileId, Limit limit);

    /** A profile's notes of a status, newest first. */
    List<AnalystNoteEntity> findByProfileIdAndStatusOrderByCreatedAtDescIdDesc(String profileId, String status,
            Limit limit);

    /** Notes since an instant, for aggregates. */
    long countByCreatedAtGreaterThanEqual(Instant since);

    /** Notes of a status since an instant. */
    long countByStatusAndCreatedAtGreaterThanEqual(String status, Instant since);
}
