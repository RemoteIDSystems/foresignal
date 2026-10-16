package ai.foresignal.raster.export;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The raster export jobs (prompt 108). */
public interface ExportJobRepository extends JpaRepository<ExportJobEntity, String> {

    /** Jobs that finished before {@code cutoff} (retention). */
    List<ExportJobEntity> findByFinishedAtBefore(Instant cutoff);
}
