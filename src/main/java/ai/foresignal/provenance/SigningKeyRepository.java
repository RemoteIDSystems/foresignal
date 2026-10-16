package ai.foresignal.provenance;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** The instance signing keys (prompt 109). */
public interface SigningKeyRepository extends JpaRepository<SigningKeyEntity, String> {

    /** The keys with {@code status}. */
    List<SigningKeyEntity> findByStatus(String status);

    /** The newest key with {@code status}. */
    Optional<SigningKeyEntity> findFirstByStatusOrderByCreatedAtDesc(String status);

    /**
     * Takes the PostgreSQL transaction-scoped advisory lock {@code key}, waiting for a concurrent holder to
     * commit (prompt 109: one active key even when two first uses or rotations race). Must run inside a
     * transaction; the lock is released at its end.
     *
     * @return always 1
     */
    @Query(value = "SELECT 1 FROM (SELECT pg_advisory_xact_lock(:key)) AS locked", nativeQuery = true)
    Integer lock(@Param("key") long key);
}
