package ai.foresignal.attention;

/**
 * Published after an attention topic was created, replaced or deleted, once the change commits (prompt 135): the
 * social term index rebuilds on it.
 *
 * @param topicId the topic, or null when the examples were seeded
 */
public record AttentionTopicsChangedEvent(String topicId) {}
