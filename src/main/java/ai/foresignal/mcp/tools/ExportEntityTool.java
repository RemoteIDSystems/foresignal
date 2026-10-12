package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.audit.AuditRecord;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.store.purge.EntityAudit;
import ai.foresignal.store.purge.EntityExportService;
import ai.foresignal.store.purge.ExportFormat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * {@code export_entity} (admin, prompt 039): everything retained about one entity, as
 * {@code exportRecord} writes it in ndjson or GeoJSON, with the hashed manifest. Bounded to
 * {@link #MAX_BYTES}; larger exports must use the REST endpoint. Audited. Never offered to Ask's tool
 * loop (Ask offers only read-only {@code read} tools).
 */
@Component
@RestrictedRead("admin only; admin tokens carry no restriction")
public class ExportEntityTool implements ForesignalMcpTool {

    /** The largest export the tool returns. */
    public static final int MAX_BYTES = 1_000_000;

    static final Map<String, Object> INPUT = object(props(
            "sourceId", string("The source id.", 1, 64),
            "externalId", string("The entity's external id.", 1, 256),
            "format", enumeration("ndjson (one typed JSON object per line) or geojson.", "ndjson", "ndjson", "geojson")),
            "sourceId", "externalId");

    private final EntityExportService exports;
    private final EntityAudit audit;

    /** Creates the tool. */
    public ExportEntityTool(EntityExportService exports, EntityAudit audit) {
        this.exports = exports;
        this.audit = audit;
    }

    @Override
    public String name() {
        return "export_entity";
    }

    @Override
    public String title() {
        return "Export one entity";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Exports everything retained about one entity: its current state, every"
                + " retained version with observers, the alerts referencing it, and a manifest with counts and the"
                + " SHA-256 of the content before it. Up to 1 MB; use GET /v1/records/{sourceId}/{externalId}/export"
                + " for more. Needs an admin token; audited.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Scope requiredScope() {
        return Scope.ADMIN;
    }

    @Override
    public boolean readOnlyHint() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String sourceId = McpArgs.requiredText(args, "sourceId");
        String externalId = McpArgs.requiredText(args, "externalId");
        String formatArg = McpArgs.text(args, "format");
        ExportFormat format = ExportFormat.parse(formatArg == null ? "ndjson" : formatArg);
        if (format == ExportFormat.GPX) {
            throw new McpToolException("format must be ndjson or geojson.");
        }
        exports.require(sourceId, externalId);
        CappedBuffer out = new CappedBuffer(MAX_BYTES);
        EntityExportService.Summary summary;
        try {
            summary = exports.write(sourceId, externalId, format, out);
        } catch (TooLarge e) {
            throw new McpToolException("The export is larger than 1 MB; use GET /v1/records/" + sourceId
                    + "/{externalId}/export instead.");
        }
        CurrentToken token = context.caller().token() == null ? null : new CurrentToken(context.caller().token(), false);
        audit.record(AuditRecord.TARGET_ENTITY_EXPORT, token, null, "mcp:" + name(), name(), 200, sourceId,
                externalId, context.caller().remoteIp(), Map.of("format", format.wireValue(),
                        "versions", summary.versions(), "alerts", summary.alerts(), "sha256", summary.sha256()));
        return ToolResult.text(out.text());
    }

    /** Thrown by {@link CappedBuffer} when the cap is exceeded. */
    static final class TooLarge extends RuntimeException {

        private static final long serialVersionUID = 1L;

        TooLarge() {
            super("export too large");
        }
    }

    /** A byte buffer that refuses to grow past its cap. */
    static final class CappedBuffer extends OutputStream {

        private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        private final int cap;

        CappedBuffer(int cap) {
            this.cap = cap;
        }

        @Override
        public void write(int b) {
            check(1);
            bytes.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            check(len);
            bytes.write(b, off, len);
        }

        private void check(int more) {
            if (bytes.size() + more > cap) {
                throw new TooLarge();
            }
        }

        String text() {
            return bytes.toString(StandardCharsets.UTF_8);
        }
    }
}
