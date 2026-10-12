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
 * @param actorKind {@code token}, {@code ingest_key}, {@code pairing}, {@code system} or {@code action}
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
        Map<String, Object> detail) {

    /** Actor kind of a token. */
    public static final String TOKEN = "token";

    /** Actor kind of an ingest key (024). */
    public static final String INGEST_KEY = "ingest_key";

    /** Actor kind of a pairing claim. */
    public static final String PAIRING = "pairing";

    /** Actor kind of Foresignal itself. */
    public static final String SYSTEM = "system";

    /** A null detail becomes empty. */
    public AuditRecord {
        detail = detail == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(detail));
    }
}
