package ai.foresignal.doctor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpSessions;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.mcp.tools.DiagnoseSourceTool;
import ai.foresignal.mcp.tools.GetDoctorReportTool;
import ai.foresignal.security.Scope;
import ai.foresignal.source.SourceRegistry;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The doctor's MCP tools (prompt 140, task 7): {@code diagnose_source} (admin, cost-bearing, trigger MCP) and
 * {@code get_doctor_report} (agent), with compact output and the patch only with {@code detail=long}.
 */
class DoctorMcpToolsTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    DoctorService doctor;
    McpOutputShaper shaper;
    DoctorReportView report;

    @BeforeEach
    void setUp() {
        doctor = mock(DoctorService.class);
        shaper = new McpOutputShaper(JSON, mock(SourceRegistry.class));
        Map<String, Object> patch = Map.of("file", "usgs/UsgsGeoJsonParser.java", "unifiedDiff", "@@ -1 +1 @@",
                "patchApplies", true, "label", DoctorLlm.PATCH_LABEL);
        report = new DoctorReportView("01JDOCTORREPORT00000000000", "usgs-earthquakes",
                Instant.parse("2026-10-18T12:00:00Z"), DoctorTrigger.MCP, DoctorStatus.BROKEN,
                List.of(new DoctorFinding(FindingCode.FIELD_RENAMED, DoctorFinding.Severity.CRITICAL,
                        List.of("mag → magnitude"), "features[].properties.mag", "Read magnitude", true)),
                Map.of("format", "json"), Map.of("skipped", false), null,
                Map.of("status", "ok", "summary", "mag was renamed", "patch", patch), "dev");
    }

    static ToolContext context(Set<String> excluded) {
        return new ToolContext(new McpCaller(null, new ReadCaller(null, false, true), excluded, "127.0.0.1"),
                "session", McpSessions.STREAMABLE);
    }

    static JsonNode args(String json) {
        return JSON.readTree(json);
    }

    @Test
    @DisplayName("diagnose_source: admin, cost-bearing, runs with trigger MCP and the given options")
    void diagnose() {
        DiagnoseSourceTool tool = new DiagnoseSourceTool(doctor, shaper);
        when(doctor.run(eq("usgs-earthquakes"), any(), eq(DoctorTrigger.MCP))).thenReturn(report);

        ToolResult result = tool.call(context(Set.of()), args("{\"sourceId\":\"usgs-earthquakes\",\"llm\":\"off\","
                + "\"context\":\"full\",\"probe\":false}"));

        assertThat(tool.requiredScope()).isEqualTo(Scope.ADMIN);
        assertThat(tool.costBearing()).isTrue();
        assertThat(tool.alwaysAuthenticated()).isTrue();
        assertThat(tool.name()).isEqualTo("diagnose_source");
        ArgumentCaptor<DoctorOptions> options = ArgumentCaptor.forClass(DoctorOptions.class);
        verify(doctor).run(eq("usgs-earthquakes"), options.capture(), eq(DoctorTrigger.MCP));
        assertThat(options.getValue()).isEqualTo(new DoctorOptions(false, "off", "full"));
        assertThat(result.error()).isFalse();
        assertThat(result.structured().path("status").asString()).isEqualTo("BROKEN");
        assertThat(result.structured().path("patch").isMissingNode()).isTrue();
        assertThat(result.structured().path("patchAvailable").asString()).contains("detail=long");
    }

    @Test
    @DisplayName("diagnose_source: a hidden source is unknown and nothing runs")
    void diagnoseHidden() {
        DiagnoseSourceTool tool = new DiagnoseSourceTool(doctor, shaper);

        assertThat(tool.call(context(Set.of("secret-src")), args("{\"sourceId\":\"secret-src\"}")).error()).isTrue();
        org.mockito.Mockito.verifyNoInteractions(doctor);
    }

    @Test
    @DisplayName("get_doctor_report: agent; compact by default, the labeled patch with detail=long")
    void getReport() {
        GetDoctorReportTool tool = new GetDoctorReportTool(doctor, shaper);
        when(doctor.report(report.id())).thenReturn(Optional.of(report));

        JsonNode compact = tool.call(context(Set.of()), args("{\"reportId\":\"" + report.id() + "\"}")).structured();
        JsonNode full = tool.call(context(Set.of()), args("{\"reportId\":\"" + report.id() + "\",\"detail\":\"long\"}"))
                .structured();

        assertThat(tool.requiredScope()).isEqualTo(Scope.AGENT);
        assertThat(compact.path("findings").get(0).path("code").asString()).isEqualTo("FIELD_RENAMED");
        assertThat(compact.path("findings").get(0).path("evidence").isMissingNode()).isTrue();
        assertThat(compact.path("summary").asString()).isEqualTo("mag was renamed");
        assertThat(compact.path("patch").isMissingNode()).isTrue();
        assertThat(full.path("patch").path("label").asString()).isEqualTo("proposed, not applied");
        assertThat(full.path("findings").get(0).path("evidence").get(0).asString()).isEqualTo("mag → magnitude");
    }

    @Test
    @DisplayName("get_doctor_report: unknown, hidden and still-running reports")
    void getReportEdges() {
        GetDoctorReportTool tool = new GetDoctorReportTool(doctor, shaper);
        when(doctor.report(any())).thenReturn(Optional.empty());
        assertThat(tool.call(context(Set.of()), args("{\"reportId\":\"01JNOPE0000000000000000000\"}")).error())
                .isTrue();

        when(doctor.report(report.id())).thenReturn(Optional.of(report));
        assertThat(tool.call(context(Set.of("usgs-earthquakes")), args("{\"reportId\":\"" + report.id() + "\"}"))
                .error()).isTrue();

        when(doctor.isRunning("01JRUNNING0000000000000000")).thenReturn(true);
        ToolResult running = tool.call(context(Set.of()), args("{\"reportId\":\"01JRUNNING0000000000000000\"}"));
        assertThat(running.error()).isFalse();
        assertThat(running.content().get(0)).isInstanceOf(ToolResult.Text.class);
    }
}
