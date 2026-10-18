package ai.foresignal.chronicle.network;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/** Deleted chronicles remembered for the churn rule (prompt 134). */
public interface ChronicleChurnRepository extends JpaRepository<ChronicleChurnEntity, Long> {

    /** An owner's deletions since {@code since}. */
    List<ChronicleChurnEntity> findByOwnerAndDeletedAtAfter(String owner, Instant since);

    /** Removes deletions older than {@code before}. */
    @Transactional
    long deleteByDeletedAtBefore(Instant before);
}
