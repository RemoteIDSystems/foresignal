package ai.foresignal.llm;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/** {@code llm_provider} rows. */
public interface LlmProviderRepository extends JpaRepository<LlmProviderEntity, String> {

    /** Every provider, by id. */
    List<LlmProviderEntity> findAllByOrderByIdAsc();

    /**
     * A user's personal providers, by id (prompt R2-011).
     *
     * @param ownerPrincipalId the user's principal
     * @return the providers
     */
    List<LlmProviderEntity> findByOwnerPrincipalIdOrderByIdAsc(String ownerPrincipalId);

    /** The shared providers (no owner), by id (prompt R2-011). */
    List<LlmProviderEntity> findByOwnerPrincipalIdIsNullOrderByIdAsc();

    /** Records a successful call: clears the failure streak. */
    @Modifying
    @Transactional
    @Query("update LlmProviderEntity p set p.lastSuccessAt = :at, p.consecutiveFailures = 0 where p.id = :id")
    int recordSuccess(@Param("id") String id, @Param("at") Instant at);

    /** Records a failed call: extends the failure streak. */
    @Modifying
    @Transactional
    @Query("update LlmProviderEntity p set p.lastErrorAt = :at, p.lastError = :error,"
            + " p.consecutiveFailures = p.consecutiveFailures + 1 where p.id = :id")
    int recordFailure(@Param("id") String id, @Param("at") Instant at, @Param("error") String error);

    /** Records whether the chat model supports tools. */
    @Modifying
    @Transactional
    @Query("update LlmProviderEntity p set p.supportsTools = :supports where p.id = :id")
    int recordSupportsTools(@Param("id") String id, @Param("supports") Boolean supports);
}
