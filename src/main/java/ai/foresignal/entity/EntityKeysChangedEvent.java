package ai.foresignal.entity;

/**
 * Published after an entity's keys, aliases or links change (prompt 098), so watchlist rules that
 * name an {@code entity:<id>} item rebuild their key expansion.
 *
 * @param entityId the canonical entity whose keys changed
 */
public record EntityKeysChangedEvent(String entityId) {}
