package ai.foresignal.inbound.edp;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code external_source} rows (prompt 024). */
public interface ExternalSourceRepository extends JpaRepository<ExternalSourceEntity, String> {

    /** Every provider, ordered by id. */
    List<ExternalSourceEntity> findAllByOrderBySourceId();

    /** Providers in one state, ordered by id. */
    List<ExternalSourceEntity> findByStateOrderBySourceId(String state);
}
