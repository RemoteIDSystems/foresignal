package ai.foresignal.exchange;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Foreign objects received in packages (prompt 132). */
public interface ReceivedObjectRepository extends JpaRepository<ReceivedObjectEntity, String> {

    /** The received copies of one remote reference. */
    List<ReceivedObjectEntity> findByRemoteRef(String remoteRef);

    /** The objects of one received package. */
    List<ReceivedObjectEntity> findByReceivedId(String receivedId);
}
