package ai.foresignal.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.mcp.tools.ExportEntityTool;
import ai.foresignal.mcp.tools.PurgeEntityTool;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.audit.AuditRecord;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.store.purge.EntityAudit;
import ai.foresignal.store.purge.EntityExportService;
import ai.foresignal.store.purge.EntityPurgeService;
import ai.foresignal.store.purge.ExportFormat;
import ai.foresignal.store.purge.PurgeReport;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code export_entity} and {@code purge_entity} (prompt 039): admin scope, never in Ask's read-tool
 * catalog, results and audit.
 */
class EntityToolsTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    final EntityExportService exports = mock(EntityExportService.class);
    final EntityPurgeService purges = mock(EntityPurgeService.class);
    final EntityAudit audit = mock(EntityAudit.class);
    final ExportEntityTool exportTool = new ExportEntityTool(exports, audit);
    final PurgeEntityTool purgeTool = new PurgeEntityTool(purges, audit,
            new McpOutputShaper(JSON, mock(SourceRegistry.class)));

    static ToolContext admin() {
        AuthenticatedToken token = new AuthenticatedToken(UUID.randomUUID(), "ops", Scope.ADMIN, TokenRestrictions.NONE,
                null, "p1", false);
        return new ToolContext(new McpCaller(token, new ReadCaller(Scope.ADMIN, false, true), Set.of(), "10.0.0.2"),
                "s", "t");
    }

    @Test
    @DisplayName("both tools are admin, well formed, and excluded from Ask's read-tool catalog")
    void shape() {
        for (ForesignalMcpTool tool : new ForesignalMcpTool[] {exportTool, purgeTool}) {
            assertThat(tool.requiredScope()).isEqualTo(Scope.ADMIN);
            assertThat(ToolCatalog.isRead(tool)).as(tool.name() + " is not an Ask tool").isFalse();
            McpToolSchemaTest.assertWellFormed(tool);
        }
        assertThat(exportTool.name()).isEqualTo("export_entity");
        assertThat(exportTool.description()).startsWith(ForesignalMcpTool.UNTRUSTED_NOTE);
        assertThat(purgeTool.name()).isEqualTo("purge_entity");
        assertThat(purgeTool.destructiveHint()).isTrue();
    }

    @Test
    @DisplayName("export_entity returns the export text and audits it")
    void export() {
        when(exports.write(eq("src"), eq("a"), eq(ExportFormat.NDJSON), any())).thenAnswer(inv -> {
            OutputStream out = inv.getArgument(3);
            out.write("{\"type\":\"manifest\"}\n".getBytes(StandardCharsets.UTF_8));
            return new EntityExportService.Summary(0, 0, "abc");
        });
        ToolResult result = exportTool.call(admin(), JSON.valueToTree(Map.of("sourceId", "src", "externalId", "a")));
        assertThat(result.content().toString()).contains("manifest");
        verify(audit).record(eq(AuditRecord.TARGET_ENTITY_EXPORT), any(), any(), anyString(), eq("export_entity"),
                anyInt(), eq("src"), eq("a"), any(), any());
    }

    @Test
    @DisplayName("export_entity refuses gpx and exports over 1 MB")
    void exportLimits() {
        assertThatThrownBy(() -> exportTool.call(admin(),
                JSON.valueToTree(Map.of("sourceId", "src", "externalId", "a", "format", "gpx"))))
                .isInstanceOf(McpToolException.class);
        doAnswer(inv -> {
            OutputStream out = inv.getArgument(3);
            out.write(new byte[ExportEntityTool.MAX_BYTES + 1]);
            return null;
        }).when(exports).write(eq("src"), eq("big"), any(), any());
        assertThatThrownBy(() -> exportTool.call(admin(), JSON.valueToTree(Map.of("sourceId", "src", "externalId", "big"))))
                .isInstanceOf(McpToolException.class).hasMessageContaining("1 MB");
    }

    @Test
    @DisplayName("purge_entity returns the PurgeReport and audits it")
    void purge() {
        when(purges.purge("src", "a", true)).thenReturn(
                new PurgeReport(42, 3, 0, 1, 2, 1, new PurgeReport.CitingDocuments(1, 0)));
        ToolResult result = purgeTool.call(admin(),
                JSON.valueToTree(Map.of("sourceId", "src", "externalId", "a", "purgeAlerts", true)));
        assertThat(result.structured().get("tombstoneSeq").asLong()).isEqualTo(42);
        assertThat(result.structured().get("citingDocuments").get("askMessages").asInt()).isEqualTo(1);
        verify(audit).record(eq(AuditRecord.TARGET_ENTITY_PURGE), any(), any(), anyString(), eq("purge_entity"),
                anyInt(), eq("src"), eq("a"), any(), any());
    }
}
