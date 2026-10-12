package ai.foresignal.inbound.edp;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code edp_registration_token} rows (prompt 024). */
public interface RegistrationTokenRepository extends JpaRepository<RegistrationTokenEntity, String> {

    /** The token whose SHA-256 is {@code hash}. */
    Optional<RegistrationTokenEntity> findByTokenHash(String hash);

    /** Every token, newest first. */
    List<RegistrationTokenEntity> findAllByOrderByCreatedAtDesc();
}
