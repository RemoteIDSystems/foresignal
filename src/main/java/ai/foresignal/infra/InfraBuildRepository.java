package ai.foresignal.infra;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code infra_build} access (prompt 104). */
public interface InfraBuildRepository extends JpaRepository<InfraBuildEntity, Long> {

    /** The newest build, or empty. */
    Optional<InfraBuildEntity> findTop1ByOrderByIdDesc();
}
