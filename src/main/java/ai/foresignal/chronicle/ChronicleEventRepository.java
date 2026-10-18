package ai.foresignal.chronicle;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/** Chronicle thread items (prompt 131). */
public interface ChronicleEventRepository extends JpaRepository<ChronicleEventEntity, Long> {

    /** Thread items after {@code id} across chronicles, oldest first (SSE replay). */
    List<ChronicleEventEntity> findByIdGreaterThanOrderByIdAsc(long id, Limit limit);

    /** The newest item across chronicles (the SSE head). */
    Optional<ChronicleEventEntity> findTop1ByOrderByIdDesc();

    /** The items of a chronicle of one type, oldest first. */
    List<ChronicleEventEntity> findByChronicleIdAndTypeOrderByIdAsc(String chronicleId, String type);

    /** Every item of a chronicle, oldest first. */
    List<ChronicleEventEntity> findByChronicleIdOrderByIdAsc(String chronicleId);

    /** Deletes the items of a chronicle. */
    void deleteByChronicleId(String chronicleId);
}
