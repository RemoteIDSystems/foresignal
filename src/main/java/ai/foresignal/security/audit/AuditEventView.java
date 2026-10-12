package ai.foresignal.security.audit;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * One entry of {@code GET /v1/audit} (OpenAPI {@code AuditEvent}, prompt 023).
 *
 * @param id the id
 * @param at when it happened
 * @param actorKind who acted
 * @param tokenId the token, or null
 * @param tokenName the token's name, or null
 * @param principalId the token's principal, or null
 * @param scope the token's scope, or null
 * @param method the HTTP method, or null
 * @param path the path without its query, or null
 * @param operationId the spec operation, or null
 * @param status the HTTP status, or null
 * @param targetType what was acted on, or null
 * @param targetId its id, or null
 * @param remoteIp the client address, or null
 * @param detail context
 */
public record AuditEventView(
        long id,
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
        Map<String, Object> detail) {}
