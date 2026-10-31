package ai.foresignal.source.group;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code source_group} rows (prompt SG-002). */
public interface SourceGroupRepository extends JpaRepository<SourceGroupEntity, String> {

    /** One group by id and owner: the self-service ownership check (404 when it does not match). */
    Optional<SourceGroupEntity> findByIdAndOwnerUserId(String id, UUID ownerUserId);

    /** Every group owned by {@code ownerUserId}, oldest first (for cursor paging). */
    List<SourceGroupEntity> findByOwnerUserIdOrderByCreatedAtAscIdAsc(UUID ownerUserId);

    /** Deletes every group owned by {@code ownerUserId}; returns how many (reassigner). */
    long deleteByOwnerUserId(UUID ownerUserId);
}
