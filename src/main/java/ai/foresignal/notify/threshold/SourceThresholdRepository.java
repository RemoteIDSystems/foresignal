package ai.foresignal.notify.threshold;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code source_threshold} rows (prompt SG-003). */
public interface SourceThresholdRepository extends JpaRepository<SourceThresholdEntity, SourceThresholdId> {

    /** Every entry a user set, by their principal. */
    List<SourceThresholdEntity> findByOwnerPrincipalId(String ownerPrincipalId);

    /** Deletes every entry a user set; returns how many (reassigner). */
    long deleteByOwnerPrincipalId(String ownerPrincipalId);
}
