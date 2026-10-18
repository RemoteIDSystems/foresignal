package ai.foresignal.chronicle.network;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Chronicle clusters (prompt 134). */
public interface ChronicleClusterRepository extends JpaRepository<ChronicleClusterEntity, String> {

    /** Every cluster, newest change first. */
    List<ChronicleClusterEntity> findAllByOrderByUpdatedAtDescIdAsc();
}
