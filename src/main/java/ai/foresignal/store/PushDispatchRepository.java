package ai.foresignal.store;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Per-target pushes (prompt 031). The claim is native SQL in {@code PushDispatchStore}. */
public interface PushDispatchRepository extends JpaRepository<PushDispatchEntity, Long> {

    /** The dispatches of one delivery, oldest first. */
    List<PushDispatchEntity> findByDeliveryIdOrderByIdAsc(long deliveryId);

    /** True when the delivery already fanned out. */
    boolean existsByDeliveryId(long deliveryId);
}
