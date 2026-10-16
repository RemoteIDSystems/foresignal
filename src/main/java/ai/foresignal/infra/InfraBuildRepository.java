package ai.foresignal.infra;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code infra_build} access (prompt 104). */
public interface InfraBuildRepository extends JpaRepository<InfraBuildEntity, Long> {

    /** The newest build, or empty. */
    Optional<InfraBuildEntity> findTop1ByOrderByIdDesc();

    /** The newest successful build (finished without an error): the current graph. */
    Optional<InfraBuildEntity> findTop1ByFinishedIsNotNullAndErrorIsNullOrderByIdDesc();

    /** The newest build still running (neither finished nor failed). */
    Optional<InfraBuildEntity> findTop1ByFinishedIsNullAndErrorIsNullOrderByIdDesc();
}
