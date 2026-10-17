package ai.foresignal.collab;

import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code principal_inbox} table. */
public interface InboxItemRepository extends JpaRepository<InboxItemEntity, Long> {

    /** A principal's items after a cursor, oldest first (the {@code me} topic replay). */
    List<InboxItemEntity> findByPrincipalIdAndIdGreaterThanOrderByIdAsc(String principalId, long after, Limit limit);

    /** Every item after a cursor, oldest first. */
    List<InboxItemEntity> findByIdGreaterThanOrderByIdAsc(long after, Limit limit);

    /** The newest item. */
    java.util.Optional<InboxItemEntity> findFirstByOrderByIdDesc();

    /** Unread items of a principal. */
    long countByPrincipalIdAndReadAtIsNull(String principalId);
}
