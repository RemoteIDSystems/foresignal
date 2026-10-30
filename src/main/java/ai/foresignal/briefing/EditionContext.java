package ai.foresignal.briefing;

import java.util.Set;
import java.util.UUID;

/**
 * Who an edition is for and in what language (prompt NP-003), resolved when it runs.
 *
 * @param ownerUserId the user who owns the edition, or null for an instance briefing with an output language
 * @param language the canonical BCP 47 language the edition is written and spoken in: its {@code outputLanguage},
 *     else the owner's {@code UserPreferences.locale}; null to leave the briefing in its source language
 * @param ownerHiddenSources the sources the owner turned off in their source view (R2-010): the edition follows the
 *     owner's data, so they are counted but never listed, as private sources are
 */
public record EditionContext(UUID ownerUserId, String language, Set<String> ownerHiddenSources) {

    /** Copies the set. */
    public EditionContext {
        ownerHiddenSources = ownerHiddenSources == null ? Set.of() : Set.copyOf(ownerHiddenSources);
    }

    /** True when this is a user's own edition. */
    public boolean owned() {
        return ownerUserId != null;
    }
}
