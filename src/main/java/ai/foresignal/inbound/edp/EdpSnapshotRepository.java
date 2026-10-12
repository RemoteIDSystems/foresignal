package ai.foresignal.inbound.edp;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code edp_snapshot} rows (prompt 024). */
public interface EdpSnapshotRepository extends JpaRepository<EdpSnapshotEntity, String> {

    /** A source's snapshots. */
    List<EdpSnapshotEntity> findBySourceId(String sourceId);
}
