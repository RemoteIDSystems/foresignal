package ai.foresignal.briefing;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code briefing} rows. */
public interface BriefingRepository extends JpaRepository<BriefingEntity, UUID> {

    /** Every briefing, oldest first. */
    List<BriefingEntity> findAllByOrderByCreatedAtAscIdAsc();
}
