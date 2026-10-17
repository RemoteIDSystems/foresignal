package ai.foresignal.cycle;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code maintenance_run} rows (prompt 128). */
public interface MaintenanceRunRepository extends JpaRepository<MaintenanceRunEntity, String> {

    /** The newest maintenance run with {@code status}. */
    Optional<MaintenanceRunEntity> findTop1ByStatusOrderByIdDesc(String status);
}
