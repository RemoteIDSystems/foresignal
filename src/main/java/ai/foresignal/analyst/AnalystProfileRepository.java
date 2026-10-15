package ai.foresignal.analyst;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code analyst_profile} access (prompt 102). */
public interface AnalystProfileRepository extends JpaRepository<AnalystProfileEntity, String> {

    /** The enabled profiles. */
    List<AnalystProfileEntity> findByEnabledTrue();

    /** Profiles due at or before {@code now}. */
    List<AnalystProfileEntity> findByEnabledTrueAndNextRunAtLessThanEqualOrderByNextRunAtAsc(Instant now);
}
