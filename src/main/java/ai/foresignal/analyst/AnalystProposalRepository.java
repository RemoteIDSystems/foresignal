package ai.foresignal.analyst;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code analyst_proposal} access (prompt 102). */
public interface AnalystProposalRepository extends JpaRepository<AnalystProposalEntity, String> {

    /** Proposals of a status, newest first. */
    List<AnalystProposalEntity> findByStatusOrderByCreatedAtDescIdDesc(String status, Limit limit);

    /** All proposals newest first. */
    List<AnalystProposalEntity> findAllByOrderByCreatedAtDescIdDesc(Limit limit);

    /** A profile's proposals since an instant. */
    long countByProfileIdAndStatusAndCreatedAtGreaterThanEqual(String profileId, String status, Instant since);

    /** A profile's auto-executed proposals since an instant. */
    long countByProfileIdAndAutoExecutedTrueAndCreatedAtGreaterThanEqual(String profileId, Instant since);

    /** Pending proposals that have expired. */
    List<AnalystProposalEntity> findByStatusAndExpiresAtBefore(String status, Instant before, Limit limit);
}
