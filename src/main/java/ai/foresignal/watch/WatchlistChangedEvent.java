package ai.foresignal.watch;

import java.util.UUID;

/**
 * A watchlist or its items changed (prompt 036). Published after commit; the
 * {@link WatchlistSnapshot} reloads the list. Edits converge on each entity's next observation: no
 * alert fires retroactively for entities already present.
 *
 * @param listId the list
 * @param structural true when the list itself was created, deleted or renormalized (rules then
 *     recompile their identity paths); false for item edits
 */
public record WatchlistChangedEvent(UUID listId, boolean structural) {}
