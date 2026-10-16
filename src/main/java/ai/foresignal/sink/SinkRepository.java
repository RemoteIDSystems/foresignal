package ai.foresignal.sink;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The outbound sinks (prompt 110). */
public interface SinkRepository extends JpaRepository<SinkEntity, String> {

    /** Enabled sinks, by id. */
    List<SinkEntity> findByEnabledTrueOrderByIdAsc();

    /** Every sink, by id. */
    List<SinkEntity> findAllByOrderByIdAsc();
}
