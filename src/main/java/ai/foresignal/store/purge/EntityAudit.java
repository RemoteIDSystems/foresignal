package ai.foresignal.store.purge;

import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.audit.AuditRecord;
import ai.foresignal.security.audit.AuditService;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Audits entity exports and purges (prompt 039, audit log of 023), from the REST endpoints and the MCP
 * tools alike. The detail names the entity and the counts, never record content.
 */
@Component
public class EntityAudit {

    private final AuditService audit;
    private final Clock clock;

    /** Creates the helper. */
    public EntityAudit(AuditService audit, Clock clock) {
        this.audit = audit;
        this.clock = clock;
    }

    /**
     * Records one export or purge.
     *
     * @param targetType {@link AuditRecord#TARGET_ENTITY_EXPORT} or {@link AuditRecord#TARGET_ENTITY_PURGE}
     * @param token the acting token, or null
     * @param method the HTTP method, or null for MCP
     * @param path the request path, or the MCP tool name
     * @param operationId the spec operation, or the MCP tool name
     * @param status the outcome status
     * @param sourceId the source
     * @param externalId the entity
     * @param remoteIp the client address, or null
     * @param extra additional detail (format, counts), or null
     */
    public void record(String targetType, CurrentToken token, String method, String path, String operationId,
            int status, String sourceId, String externalId, String remoteIp, Map<String, Object> extra) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("sourceId", sourceId);
        detail.put("externalId", externalId);
        if (extra != null) {
            detail.putAll(extra);
        }
        audit.record(new AuditRecord(clock.instant(), AuditRecord.TOKEN,
                token == null ? null : token.id(),
                token == null ? null : token.name(),
                token == null ? null : token.principalId(),
                token == null ? null : token.scope().wireValue(),
                method, path, operationId, status, targetType, sourceId + ":" + externalId, remoteIp, detail));
    }
}
