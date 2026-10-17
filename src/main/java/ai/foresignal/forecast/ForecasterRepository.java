package ai.foresignal.forecast;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code forecaster} rows (prompt 122). */
public interface ForecasterRepository extends JpaRepository<ForecasterEntity, String> {

    /** All forecasters, by id. */
    List<ForecasterEntity> findAllByOrderByIdAsc();
}
