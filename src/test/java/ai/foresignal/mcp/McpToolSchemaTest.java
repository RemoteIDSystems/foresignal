package ai.foresignal.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.mcp.tools.AcknowledgeAlertTool;
import ai.foresignal.mcp.tools.ListEscalationPoliciesTool;
import ai.foresignal.mcp.tools.ListNotificationsTool;
import ai.foresignal.mcp.tools.MuteWatchTool;
import ai.foresignal.mcp.tools.SnoozeAlertTool;
import ai.foresignal.mcp.tools.CreateWatchTool;
import ai.foresignal.mcp.tools.DeleteWatchTool;
import ai.foresignal.mcp.tools.GetAlertTool;
import ai.foresignal.mcp.tools.GetChangesTool;
import ai.foresignal.mcp.tools.GetRecordHistoryTool;
import ai.foresignal.mcp.tools.GetRecordTool;
import ai.foresignal.mcp.tools.ListAlertsTool;
import ai.foresignal.mcp.tools.ListKindsTool;
import ai.foresignal.mcp.tools.ListSourcesTool;
import ai.foresignal.mcp.tools.ListWatchesTool;
import ai.foresignal.mcp.tools.QueryRecordsTool;
import ai.foresignal.mcp.tools.SetAlertStatusTool;
import ai.foresignal.mcp.tools.SourceHealthTool;
import ai.foresignal.mcp.tools.SystemStatusTool;
import ai.foresignal.mcp.tools.TestWatchTool;
import ai.foresignal.mcp.tools.UpdateWatchTool;
import ai.foresignal.security.Scope;
import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Every MCP tool of prompt 025 declares a well-formed contract: a valid JSON Schema 2020-12 input
 * schema that is a closed object ({@code additionalProperties: false}) with described members and
 * existing required members, a description, and annotations consistent with its scope (read tools are
 * {@code readOnlyHint}; agent and admin tools are not, unless the tool declares itself read-only). {@link McpServerIT} applies {@link #assertWellFormed} to every
 * registered bean, including later prompts' tools.
 */
class McpToolSchemaTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    private static final SchemaRegistry REGISTRY = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);

    /** The 2020-12 meta-schema, loaded from the validator's bundled copy (never fetched). */
    private static final Schema META_SCHEMA =
            REGISTRY.getSchema(SchemaLocation.of("https://json-schema.org/draft/2020-12/schema"));

    /** The tools whose results carry text from external sources. */
    static final Set<String> TEXT_BEARING = Set.of("query_records", "get_record", "get_record_history", "get_changes",
            "source_health", "list_alerts", "get_alert", "test_watch", "list_notifications");

    /** The tools of prompt 025, constructed without services: only their contracts are read. */
    static Stream<ForesignalMcpTool> tools() {
        return Stream.of(
                new QueryRecordsTool(null, null, null),
                new GetRecordTool(null, null, null),
                new GetRecordHistoryTool(null, null, null),
                new GetChangesTool(null, null, null),
                new ListKindsTool(null, null),
                new ListSourcesTool(null, null, null, null),
                new SourceHealthTool(null, null, null, null),
                new ListAlertsTool(null, null, null),
                new GetAlertTool(null, null),
                new AcknowledgeAlertTool(null, null, null, null),
                new SetAlertStatusTool(null, null, null, null),
                new ListWatchesTool(null, null, null),
                new CreateWatchTool(null, null, null),
                new UpdateWatchTool(null, null, null),
                new DeleteWatchTool(null, null),
                new TestWatchTool(null, null, null),
                new SystemStatusTool(null, null),
                // 033
                new ListNotificationsTool(null, null),
                new SnoozeAlertTool(null, null, null, null),
                new MuteWatchTool(null, null, null),
                new ListEscalationPoliciesTool(null, null));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("tools")
    @DisplayName("each tool's schema, description and annotations are well formed")
    void wellFormed(ForesignalMcpTool tool) {
        assertWellFormed(tool);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("tools")
    @DisplayName("text-bearing tools start their description with the untrusted-content note")
    void untrustedNote(ForesignalMcpTool tool) {
        if (TEXT_BEARING.contains(tool.name())) {
            assertThat(tool.description()).startsWith(ForesignalMcpTool.UNTRUSTED_NOTE);
        }
    }

    @Test
    @DisplayName("the catalog has 025's seventeen tools and 033's four, with the scopes they list")
    void catalog() {
        Map<String, Scope> scopes = new java.util.TreeMap<>();
        tools().forEach(t -> scopes.put(t.name(), t.requiredScope()));
        assertThat(scopes).containsOnlyKeys("query_records", "get_record", "get_record_history", "get_changes",
                "list_kinds", "list_sources", "source_health", "list_alerts", "get_alert", "acknowledge_alert",
                "set_alert_status", "list_watches", "create_watch", "update_watch", "delete_watch", "test_watch",
                "system_status", "list_notifications", "snooze_alert", "mute_watch", "list_escalation_policies");
        assertThat(scopes).containsEntry("list_notifications", Scope.READ)
                .containsEntry("list_escalation_policies", Scope.READ)
                .containsEntry("snooze_alert", Scope.AGENT)
                .containsEntry("mute_watch", Scope.AGENT)
                .containsEntry("acknowledge_alert", Scope.AGENT)
                .containsEntry("set_alert_status", Scope.AGENT)
                .containsEntry("create_watch", Scope.AGENT)
                .containsEntry("update_watch", Scope.AGENT)
                .containsEntry("delete_watch", Scope.AGENT)
                .containsEntry("test_watch", Scope.READ)
                .containsEntry("query_records", Scope.READ)
                .containsEntry("system_status", Scope.READ);
        assertThat(tools()).noneMatch(ForesignalMcpTool::costBearing);
    }

    @Test
    @DisplayName("limits match the prompt: query_records ≤ 200, get_changes ≤ 500")
    void limits() {
        assertThat(maximum(new QueryRecordsTool(null, null, null), "limit")).isEqualTo(200);
        assertThat(maximum(new GetChangesTool(null, null, null), "limit")).isEqualTo(500);
        assertThat(new QueryRecordsTool(null, null, null).inputSchema().get("properties").toString())
                .contains("timeField").contains("effective");
    }

    @Test
    @DisplayName("the meta-schema check rejects a broken schema, so it cannot pass vacuously")
    void metaSchemaCheckIsReal() {
        JsonNode broken = JSON.valueToTree(Map.of("type", "object", "properties", Map.of("a", Map.of("type", 5))));
        assertThat(META_SCHEMA.validate(broken)).isNotEmpty();
    }

    /**
     * Asserts {@code tool}'s contract: name, description, a valid 2020-12 closed object input schema
     * with described members, a valid output schema when declared, and annotations consistent with
     * its scope.
     */
    public static void assertWellFormed(ForesignalMcpTool tool) {
        assertThat(tool.name()).as("name").matches("^[a-z][a-z0-9_]{0,63}$");
        assertThat(tool.description()).as(tool.name() + " description").isNotBlank().hasSizeGreaterThan(40);

        JsonNode input = JSON.valueToTree(tool.inputSchema());
        List<Error> errors = META_SCHEMA.validate(input);
        assertThat(errors).as(tool.name() + " input schema against the 2020-12 meta-schema").isEmpty();
        assertThat(input.path("type").asString()).as(tool.name() + " input type").isEqualTo("object");
        assertThat(input.path("additionalProperties").isBoolean() && !input.path("additionalProperties").asBoolean())
                .as(tool.name() + " additionalProperties false").isTrue();
        JsonNode properties = input.path("properties");
        assertThat(properties.isObject()).as(tool.name() + " properties").isTrue();
        for (String member : properties.propertyNames()) {
            assertThat(properties.get(member).path("description").asString(""))
                    .as(tool.name() + "." + member + " description").isNotBlank();
        }
        for (JsonNode required : input.path("required")) {
            assertThat(properties.has(required.asString()))
                    .as(tool.name() + " required member " + required.asString() + " is declared").isTrue();
        }
        if (tool.outputSchema() != null) {
            JsonNode output = JSON.valueToTree(tool.outputSchema());
            assertThat(META_SCHEMA.validate(output)).as(tool.name() + " output schema").isEmpty();
            assertThat(output.path("type").asString()).isEqualTo("object");
        }

        McpSchema.Tool definition = McpToolRegistry.definition(tool);
        McpSchema.ToolAnnotations annotations = definition.annotations();
        assertThat(annotations).as(tool.name() + " annotations").isNotNull();
        if (tool.requiredScope() == Scope.READ) {
            assertThat(annotations.readOnlyHint()).as(tool.name() + " readOnlyHint").isTrue();
            assertThat(annotations.destructiveHint()).as(tool.name() + " destructiveHint").isFalse();
        } else if (tool.readOnlyHint()) {
            // An agent or admin tool that changes nothing (039's export_entity) says so; it is then never destructive.
            // Scope is who may call a tool, not whether it writes (checkpoint 041).
            assertThat(annotations.readOnlyHint()).as(tool.name() + " read-only tool readOnlyHint").isTrue();
            assertThat(annotations.destructiveHint()).as(tool.name() + " read-only tool destructiveHint").isFalse();
        } else {
            assertThat(annotations.readOnlyHint()).as(tool.name() + " write tool readOnlyHint").isFalse();
        }
        assertThat(annotations.openWorldHint()).isFalse();
    }

    private static long maximum(ForesignalMcpTool tool, String member) {
        JsonNode input = JSON.valueToTree(tool.inputSchema());
        return input.path("properties").path(member).path("maximum").asLong();
    }
}
