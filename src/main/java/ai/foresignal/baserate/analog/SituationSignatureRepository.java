package ai.foresignal.baserate.analog;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code situation_signature} rows (prompt 121). */
public interface SituationSignatureRepository
        extends JpaRepository<SituationSignatureEntity, SituationSignatureEntity.Key> {

    /** A situation's signatures, by day. */
    List<SituationSignatureEntity> findBySituationIdOrderByDayNAsc(String situationId);
}
