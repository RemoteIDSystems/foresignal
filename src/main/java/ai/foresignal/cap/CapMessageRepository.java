package ai.foresignal.cap;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** The stored CAP messages (prompt 112). */
public interface CapMessageRepository extends JpaRepository<CapMessageEntity, String> {

    /** The messages of an alert, oldest first. */
    List<CapMessageEntity> findByAlertIdOrderByNAsc(long alertId);

    /** The latest message of an alert. */
    Optional<CapMessageEntity> findFirstByAlertIdOrderByNDesc(long alertId);

    /** The latest message of each of {@code alertIds}. */
    @Query("SELECT m FROM CapMessageEntity m WHERE m.alertId IN :ids AND m.n = "
            + "(SELECT max(x.n) FROM CapMessageEntity x WHERE x.alertId = m.alertId)")
    List<CapMessageEntity> latestFor(@Param("ids") Collection<Long> alertIds);
}
