package ai.foresignal.ask;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** {@code ask_action} rows. */
public interface AskActionRepository extends JpaRepository<AskActionEntity, UUID> {

    /** The actions of a thread, oldest first. */
    List<AskActionEntity> findByThreadIdOrderByCreatedAtAscIdAsc(UUID threadId);

    /** Deletes the actions of a thread. */
    @Modifying
    @Query("delete from AskActionEntity a where a.threadId = :threadId")
    int deleteByThread(@Param("threadId") UUID threadId);
}
