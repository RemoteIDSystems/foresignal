package ai.foresignal.trip;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The pre-trip check runs (prompt 114). */
public interface TripRunRepository extends JpaRepository<TripRunEntity, String> {

    /** A trip's runs, newest first. */
    List<TripRunEntity> findByTripIdOrderByCreatedAtDescIdDesc(String tripId);

    /** True when a non-dry-run check exists for the occurrence. */
    boolean existsByTripIdAndOccurrenceAndStatusNot(String tripId, Instant occurrence, String status);

    /** Deletes a trip's runs. */
    long deleteByTripId(String tripId);
}
