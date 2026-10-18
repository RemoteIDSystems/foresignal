package ai.foresignal.chronicle.network;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Discovery salts (prompt 134). */
public interface ChronicleSaltRepository extends JpaRepository<ChronicleSaltEntity, String> {

    /** The salts of a kind that are not retired, newest first. */
    List<ChronicleSaltEntity> findByKindAndRetiredAtIsNullOrderByCreatedAtDesc(String kind);

    /** The network salts of a network that are not retired, newest first. */
    List<ChronicleSaltEntity> findByKindAndNetworkIdAndRetiredAtIsNullOrderByCreatedAtDesc(String kind,
            String networkId);
}
