package ai.foresignal.entity;

/**
 * What the resolver decided for one record's keys (prompt 098): the entity the record links to, and
 * the key that produced the link.
 *
 * @param entityId the canonical entity's id
 * @param entityType the entity's type
 * @param linkKey the key the link is recorded against
 */
public record Resolution(String entityId, EntityType entityType, Key linkKey) {}
