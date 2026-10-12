package ai.foresignal.watch;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** The watchlists (prompt 036). */
public interface WatchListRepository extends JpaRepository<WatchListEntity, UUID> {}
