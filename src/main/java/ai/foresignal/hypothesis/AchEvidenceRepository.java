package ai.foresignal.hypothesis;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code ach_evidence} rows (prompt 125). */
public interface AchEvidenceRepository extends JpaRepository<AchEvidenceEntity, String> {

    /** The rows of a set, oldest first (removed rows included). */
    List<AchEvidenceEntity> findBySetIdOrderByIdAsc(String setId);

    /** The live row of a set with an evidence key, if any. */
    List<AchEvidenceEntity> findBySetIdAndEvidenceGroupKeyAndRemovedAtIsNull(String setId, String key);
}
