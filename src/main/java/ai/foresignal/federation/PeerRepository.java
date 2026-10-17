package ai.foresignal.federation;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** The federation peers (prompt 113). */
public interface PeerRepository extends JpaRepository<PeerEntity, String> {

    /** The peer whose current ingest key hashes to {@code hash}. */
    Optional<PeerEntity> findByIngestKeyHash(String hash);

    /** Every peer, ordered by id. */
    List<PeerEntity> findAllByOrderByIdAsc();
}
