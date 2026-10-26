package ai.foresignal.llm;

import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserOwnedConfigReassigner;
import ai.foresignal.security.user.UserRepository;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Moves or deletes a deleted user's personal LLM providers (prompt R2-011, the {@link UserOwnedConfigReassigner} of
 * R2-005), audit name {@code llmProviders}.
 *
 * <p>Reassigning moves each provider, its stored key included, to the target admin's principal. With no target (no
 * admin account exists) a provider would become shared, which would put one person's paid key behind every user's
 * calls, so it is also <em>disabled</em> as it becomes shared; an admin re-enables it deliberately. Deleting removes
 * the providers (a personal provider never holds an instance role, so nothing refers to it). Budget state and cached
 * clients are dropped after the deletion commits. The user's per-user usage rows stay as the record of what was spent.
 */
@Component
public class LlmProviderReassigner implements UserOwnedConfigReassigner {

    private final UserRepository users;
    private final LlmProviderRepository providers;
    private final LlmService llm;
    private final Clock clock;

    /**
     * Creates the reassigner.
     *
     * @param users the user store, mapping a user to their principal
     * @param providers the provider store
     * @param llm the LLM service, whose cached state is dropped
     * @param clock the clock
     */
    public LlmProviderReassigner(UserRepository users, LlmProviderRepository providers, LlmService llm, Clock clock) {
        this.users = users;
        this.providers = providers;
        this.llm = llm;
        this.clock = clock;
    }

    @Override
    public String name() {
        return "llmProviders";
    }

    @Override
    public int reassign(UUID owner, UUID target) {
        String from = principal(owner);
        if (from == null) {
            return 0;
        }
        String to = target == null ? null : principal(target);
        List<LlmProviderEntity> owned = providers.findByOwnerPrincipalIdOrderByIdAsc(from);
        for (LlmProviderEntity provider : owned) {
            provider.setOwnerPrincipalId(to);
            if (to == null) {
                provider.setEnabled(false);
            }
            provider.setUpdatedAt(clock.instant());
        }
        providers.saveAllAndFlush(owned);
        forgetAfterCommit(owned);
        return owned.size();
    }

    @Override
    public int delete(UUID owner) {
        String from = principal(owner);
        if (from == null) {
            return 0;
        }
        List<LlmProviderEntity> owned = providers.findByOwnerPrincipalIdOrderByIdAsc(from);
        providers.deleteAll(owned);
        providers.flush();
        forgetAfterCommit(owned);
        return owned.size();
    }

    private void forgetAfterCommit(List<LlmProviderEntity> changed) {
        List<String> ids = changed.stream().map(LlmProviderEntity::getId).toList();
        AfterCommit.run(() -> ids.forEach(llm::forget));
    }

    private String principal(UUID user) {
        return users.findById(user).map(UserEntity::getPrincipalId).orElse(null);
    }
}
