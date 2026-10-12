package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.bool;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.api.EntityPurgeController;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.audit.AuditRecord;
import ai.foresignal.store.purge.EntityAudit;
import ai.foresignal.store.purge.EntityPurgeService;
import ai.foresignal.store.purge.PurgeReport;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * {@code purge_entity} (admin, prompt 039): erases one entity as {@code purgeRecord} does and returns
 * the {@code PurgeReport}. Audited. Never offered to Ask's tool loop (Ask offers only read-only
 * {@code read} tools).
 */
@Component
public class PurgeEntityTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "sourceId", string("The source id.", 1, 64),
            "externalId", string("The entity's external id.", 1, 256),
            "purgeAlerts", bool("Delete the alerts referencing the entity instead of redacting them.", false)),
            "sourceId", "externalId");

    private final EntityPurgeService purges;
    private final EntityAudit audit;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public PurgeEntityTool(EntityPurgeService purges, EntityAudit audit, McpOutputShaper shaper) {
        this.purges = purges;
        this.audit = audit;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "purge_entity";
    }

    @Override
    public String title() {
        return "Erase one entity";
    }

    @Override
    public String description() {
        return "Erases one entity: writes a tombstone that tells clients and caches to delete it, deletes every"
                + " older version, deletes its embeddings, redacts (or with purgeAlerts deletes) its alerts and"
                + " redacts stored notifications. Messages already delivered to external channels cannot be"
                + " recalled. Irreversible. Needs an admin token; audited.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("PurgeReport {tombstoneSeq, versionsDeleted, alertsRedacted, alertsDeleted,"
                + " embeddingsDeleted, notificationsRedacted, citingDocuments}");
    }

    @Override
    public Scope requiredScope() {
        return Scope.ADMIN;
    }

    @Override
    public boolean destructiveHint() {
        return true;
    }

    @Override
    public boolean idempotentHint() {
        return false;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String sourceId = McpArgs.requiredText(args, "sourceId");
        String externalId = McpArgs.requiredText(args, "externalId");
        boolean purgeAlerts = McpArgs.bool(args, "purgeAlerts", false);
        PurgeReport report = purges.purge(sourceId, externalId, purgeAlerts);
        CurrentToken token = context.caller().token() == null ? null : new CurrentToken(context.caller().token(), false);
        audit.record(AuditRecord.TARGET_ENTITY_PURGE, token, null, "mcp:" + name(), name(), 200, sourceId,
                externalId, context.caller().remoteIp(), EntityPurgeController.details(report, purgeAlerts));
        return ToolResult.json(shaper.tree(report));
    }
}
