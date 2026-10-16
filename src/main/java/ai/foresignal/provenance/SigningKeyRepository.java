package ai.foresignal.provenance;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** The instance signing keys (prompt 109). */
public interface SigningKeyRepository extends JpaRepository<SigningKeyEntity, String> {

    /** The keys with {@code status}. */
    List<SigningKeyEntity> findByStatus(String status);

    /** The newest key with {@code status}. */
    Optional<SigningKeyEntity> findFirstByStatusOrderByCreatedAtDesc(String status);
}
