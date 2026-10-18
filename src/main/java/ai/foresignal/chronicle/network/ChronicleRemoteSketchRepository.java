package ai.foresignal.chronicle.network;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Sketches received from peers (prompt 134). */
public interface ChronicleRemoteSketchRepository extends JpaRepository<ChronicleRemoteSketchEntity, String> {

    /** A sketch by sender and sketch id. */
    Optional<ChronicleRemoteSketchEntity> findByInstanceIdAndSketchId(String instanceId, String sketchId);

    /** The unexpired sketches received under a salt. */
    List<ChronicleRemoteSketchEntity> findBySaltIdAndExpiresAtAfterOrderByIdAsc(String saltId, Instant now);

    /** The sketches of a network. */
    List<ChronicleRemoteSketchEntity> findByNetworkIdOrderByIdAsc(String networkId);

    /** The sketches that expired before {@code now}. */
    List<ChronicleRemoteSketchEntity> findByExpiresAtBeforeOrderByIdAsc(Instant now);
}
