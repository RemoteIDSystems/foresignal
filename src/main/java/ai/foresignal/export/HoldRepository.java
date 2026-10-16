package ai.foresignal.export;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Legal holds (prompt 111). */
public interface HoldRepository extends JpaRepository<HoldEntity, String> {

    /** Every hold, newest first. */
    List<HoldEntity> findAllByOrderByCreatedAtDesc();
}
