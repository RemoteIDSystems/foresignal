package ai.foresignal.trip;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The trips (prompt 114). */
public interface TripRepository extends JpaRepository<TripEntity, String> {

    /** Every trip in id (creation) order. */
    List<TripEntity> findAllByOrderByIdAsc();

    /** The enabled trips. */
    List<TripEntity> findByEnabledTrue();

    /** The trips whose ephemeral rule is active. */
    List<TripEntity> findByActiveRuleIdIsNotNull();
}
