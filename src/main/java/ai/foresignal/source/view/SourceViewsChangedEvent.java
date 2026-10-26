package ai.foresignal.source.view;

import java.util.UUID;

/**
 * A user's source view changed or was removed (prompt R2-010), published after the change commits. The collector
 * reconciles at once, because the collection union may have changed, and cached views are dropped.
 *
 * @param userId the user whose view changed
 */
public record SourceViewsChangedEvent(UUID userId) {}
