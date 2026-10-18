package ai.foresignal.exchange;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Packages received from other instances (prompt 132). */
public interface ReceivedPackageRepository extends JpaRepository<ReceivedPackageEntity, String> {

    /** The received package with the sender's id {@code packageId}. */
    Optional<ReceivedPackageEntity> findByPackageId(String packageId);

    /** Received packages of one remote object, newest first. */
    List<ReceivedPackageEntity> findByObjectRefOrderByReceivedAtDescIdDesc(String objectRef);

    /** Every received package, newest first. */
    List<ReceivedPackageEntity> findAllByOrderByReceivedAtDescIdDesc();
}
