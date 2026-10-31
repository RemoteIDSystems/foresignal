package ai.foresignal.notify.threshold;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code source_notify_state} rows (prompt SG-003): cooldown tracking. */
public interface SourceNotifyStateRepository extends JpaRepository<SourceNotifyStateEntity, SourceThresholdId> {

    /** The last-notified state for one owner and source, if any. */
    Optional<SourceNotifyStateEntity> findByOwnerPrincipalIdAndSourceId(String ownerPrincipalId, String sourceId);

    /** Deletes every state row of a user; returns how many (reassigner). */
    long deleteByOwnerPrincipalId(String ownerPrincipalId);
}
