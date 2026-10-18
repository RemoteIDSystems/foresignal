package ai.foresignal.chronicle.network;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Access requests (prompt 134). */
public interface ChronicleAccessRequestRepository extends JpaRepository<ChronicleAccessRequestEntity, String> {

    /** How many requests a principal sent since {@code since}. */
    long countByRequesterPrincipalAndCreatedAtAfter(String principal, Instant since);

    /** A principal's outgoing requests, newest first. */
    List<ChronicleAccessRequestEntity> findByRequesterPrincipalOrderByCreatedAtDescIdDesc(String principal);

    /** An owner's incoming requests, newest first. */
    List<ChronicleAccessRequestEntity> findByOwnerPrincipalOrderByCreatedAtDescIdDesc(String owner);

    /** A requester's requests on a link, newest first. */
    List<ChronicleAccessRequestEntity> findByLinkIdAndRequesterPrincipalOrderByCreatedAtDescIdDesc(String linkId,
            String principal);

    /** A requester's declined requests to an owner since {@code since}. */
    List<ChronicleAccessRequestEntity> findByRequesterPrincipalAndOwnerPrincipalAndStatusAndDecidedAtAfter(
            String principal, String owner, String status, Instant since);

    /** A requester's declined requests to a remote target since {@code since}. */
    List<ChronicleAccessRequestEntity> findByRequesterPrincipalAndTargetNodeAndStatusAndDecidedAtAfter(
            String principal, String targetNode, String status, Instant since);

    /** An incoming remote request by its requester instance and that instance's id. */
    Optional<ChronicleAccessRequestEntity> findByRequesterInstanceAndRemoteRequestId(String instance,
            String remoteRequestId);

    /** The requests targeting a local chronicle. */
    List<ChronicleAccessRequestEntity> findByTargetChronicleId(String chronicleId);
}
