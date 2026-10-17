package ai.foresignal.quality;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persisted tuning suggestions (prompt 115). */
public interface TuningSuggestionRepository extends JpaRepository<TuningSuggestionEntity, String> {

    /** The suggestion of a rule with this kind and patch, if any (the deduplication key). */
    Optional<TuningSuggestionEntity> findByRuleIdAndKindAndPatchHash(UUID ruleId, String kind, String patchHash);

    /** A rule's suggestions in a status. */
    List<TuningSuggestionEntity> findByRuleIdAndStatusOrderByCreatedAtDescIdDesc(UUID ruleId, String status);

    /** A rule's suggestions. */
    List<TuningSuggestionEntity> findByRuleIdOrderByCreatedAtDescIdDesc(UUID ruleId);

    /** Suggestions in a status, every rule. */
    List<TuningSuggestionEntity> findByStatus(String status);
}
