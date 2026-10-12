package ai.foresignal.ask;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** {@code ask_message} rows. */
public interface AskMessageRepository extends JpaRepository<AskMessageEntity, Long> {

    /** The messages of a thread, in order. */
    List<AskMessageEntity> findByThreadIdOrderByPositionAsc(UUID threadId);

    /** Deletes the messages of a thread. */
    @Modifying
    @Query("delete from AskMessageEntity m where m.threadId = :threadId")
    int deleteByThread(@Param("threadId") UUID threadId);
}
