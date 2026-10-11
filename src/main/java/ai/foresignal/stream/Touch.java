package ai.foresignal.stream;

import java.time.Instant;

/**
 * An entity re-observed with unchanged content: only its times moved (Core-Data-Model §10.1).
 *
 * @param sourceId the source
 * @param externalId the entity
 * @param observedAt the new {@code observed_at}
 * @param validUntil the new effective expiry, or null for never
 * @param bbox the live geometry's box, or null
 */
public record Touch(String sourceId, String externalId, Instant observedAt, Instant validUntil, Bbox bbox) {}
