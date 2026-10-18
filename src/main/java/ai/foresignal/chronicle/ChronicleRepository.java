package ai.foresignal.chronicle;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Chronicles (prompt 131). */
public interface ChronicleRepository extends JpaRepository<ChronicleEntity, String> {

    /** A chronicle by display id. */
    Optional<ChronicleEntity> findByDisplayId(String displayId);

    /** Chronicles in one of {@code statuses}, oldest first. */
    List<ChronicleEntity> findByStatusInOrderByCreatedAtAscIdAsc(Collection<String> statuses);

    /** Chronicles in {@code status}, oldest first. */
    List<ChronicleEntity> findByStatusOrderByCreatedAtAscIdAsc(String status);

    /** Every chronicle, newest change first. */
    List<ChronicleEntity> findAllByOrderByUpdatedAtDescIdAsc();

    /** How many chronicles the analysis cycles activated on their own since {@code since}. */
    long countByAutoActivatedAtGreaterThanEqual(Instant since);
}
