package ai.foresignal.chronicle;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Chronicle layers (prompt 131). */
public interface ChronicleLayerRepository extends JpaRepository<ChronicleLayerEntity, String> {

    /** The layers of a chronicle, removed ones included, in the order they were added. */
    List<ChronicleLayerEntity> findByChronicleIdOrderByAddedAtAscIdAsc(String chronicleId);

    /** The layers of several chronicles. */
    List<ChronicleLayerEntity> findByChronicleIdIn(Collection<String> chronicleIds);

    /** Deletes the layers of a chronicle. */
    void deleteByChronicleId(String chronicleId);
}
