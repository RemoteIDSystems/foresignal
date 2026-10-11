package ai.foresignal.store;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Read access to {@code seq_clock}. Only the ingest writer writes it (invariant I1), with native
 * SQL; retention prunes it.
 */
public interface SeqClockRepository extends JpaRepository<SeqClockEntity, Instant> {

    /** The latest minute at or before {@code t}: its {@code first_seq} is {@code cutoffSeq(t)}. */
    Optional<SeqClockEntity> findFirstByMinuteLessThanEqualOrderByMinuteDesc(Instant t);
}
