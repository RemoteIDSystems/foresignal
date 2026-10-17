package ai.foresignal.collab;

/**
 * The {@code me} SSE topic filter of one stream (prompt 130): the principal whose personal events it carries.
 *
 * @param principalId the stream's principal
 */
public record MeFilter(String principalId) {}
