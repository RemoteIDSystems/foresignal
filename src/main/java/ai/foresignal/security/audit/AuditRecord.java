package ai.foresignal.security.audit;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * One entry to write to the audit log (prompt 023). Prompt 025 records MCP tool calls with it, 033
 * notification actions, 039 private reads, exports and purges.
 *
 * @param at when it happened
 * @param actorKind {@code token}, {@code user}, {@code ingest_key}, {@code pairing}, {@code system} or {@code action}
 * @param tokenId the token, or null
 * @param tokenName the token's name, or null
 * @param principalId the token's principal, or null
 * @param scope the token's scope, or null
 * @param method the HTTP method, or null
 * @param path the request path without its query, or null
 * @param operationId the spec operation, or null
 * @param status the HTTP status, or null
 * @param targetType what was acted on (for example {@code tokens}), or null
 * @param targetId its id, or null
 * @param remoteIp the client address (after trusted-proxy handling), or null
 * @param detail context; never secrets, token values or request bodies
 * @param userId the signed-in user who acted (actor kind {@code user}, prompt R2-013), or null
 * @param userEmail that user's email when the entry was written, or null
 */
public record AuditRecord(
        Instant at,
        String actorKind,
        UUID tokenId,
        String tokenName,
        String principalId,
        String scope,
        String method,
        String path,
        String operationId,
        Integer status,
        String targetType,
        String targetId,
        String remoteIp,
        Map<String, Object> detail,
        UUID userId,
        String userEmail) {

    /** Actor kind of a signed-in user (prompt R2-013): {@code userId} and {@code userEmail} say who. */
    public static final String USER = "user";

    /** Actor kind of a token. */
    public static final String TOKEN = "token";

    /** Actor kind of an ingest key (024). */
    public static final String INGEST_KEY = "ingest_key";

    /** Actor kind of a pairing claim. */
    public static final String PAIRING = "pairing";

    /** Actor kind of Foresignal itself. */
    public static final String SYSTEM = "system";

    /** Actor kind of a device-scoped ingest key (039). */
    public static final String INGEST_DEVICE = "ingest_device";

    /** {@code targetType} of a private-data read by a non-admin principal (039). */
    public static final String TARGET_PRIVATE_READ = "private_read";

    /** {@code targetType} of an entity export (039). */
    public static final String TARGET_ENTITY_EXPORT = "entity_export";

    /** {@code targetType} of an entity purge (039). */
    public static final String TARGET_ENTITY_PURGE = "entity_purge";

    /** A null detail becomes empty. */
    public AuditRecord {
        detail = detail == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(detail));
    }

    /** An entry without a user (the shape before prompt R2-013). */
    public AuditRecord(Instant at, String actorKind, UUID tokenId, String tokenName, String principalId, String scope,
            String method, String path, String operationId, Integer status, String targetType, String targetId,
            String remoteIp, Map<String, Object> detail) {
        this(at, actorKind, tokenId, tokenName, principalId, scope, method, path, operationId, status, targetType,
                targetId, remoteIp, detail, null, null);
    }

    /**
     * This entry attributed to a signed-in user: actor kind {@code user}, with the user's id and email.
     *
     * @param user the user's id
     * @param email the user's email
     * @return the entry
     */
    public AuditRecord asUser(UUID user, String email) {
        return new AuditRecord(at, USER, tokenId, tokenName, principalId, scope, method, path, operationId, status,
                targetType, targetId, remoteIp, detail, user, email);
    }
}
