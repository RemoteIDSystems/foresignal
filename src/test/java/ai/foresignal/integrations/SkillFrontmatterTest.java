package ai.foresignal.integrations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.security.Scope;
import ai.foresignal.testsupport.integrations.IntegrationDocs;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

/**
 * The OpenClaw skill (prompt 034) is installable and stays true to the code: valid frontmatter with
 * the runtime requirements ClawHub checks, every environment variable the body uses declared, and
 * every tool named in {@code SKILL.md} and {@code docs/Agent-Guide.md} present in the MCP registry
 * with the scope the documents state.
 */
class SkillFrontmatterTest {

    /** Tools the prompt requires the skill to describe. */
    private static final List<String> REQUIRED_TOOLS = List.of("query_records", "get_record", "get_record_history",
            "get_changes", "list_kinds", "search", "list_sources", "source_health", "list_alerts", "get_alert",
            "acknowledge_alert", "set_alert_status", "list_watches", "create_watch", "update_watch", "delete_watch",
            "test_watch", "list_briefings", "create_briefing", "run_briefing", "get_briefing_run",
            "list_notifications", "snooze_alert", "system_status");

    private final IntegrationDocs.Skill skill = IntegrationDocs.skill(IntegrationDocs.read(IntegrationDocs.SKILL));

    @Test
    @DisplayName("frontmatter: name foresignal, a description, env FORESIGNAL_URL and FORESIGNAL_TOKEN, bin curl, primaryEnv the token")
    void frontmatter() {
        JsonNode fm = skill.frontmatter();
        assertThat(fm.path("name").asString()).isEqualTo("foresignal");
        assertThat(fm.path("description").asString()).isNotBlank()
                .contains("alert").contains("watch").contains("briefing");
        JsonNode openclaw = fm.path("metadata").path("openclaw");
        assertThat(openclaw.isObject()).as("metadata.openclaw").isTrue();
        assertThat(strings(openclaw.path("requires").path("env"))).contains("FORESIGNAL_URL", "FORESIGNAL_TOKEN");
        assertThat(strings(openclaw.path("requires").path("bins"))).contains("curl");
        assertThat(openclaw.path("primaryEnv").asString()).isEqualTo("FORESIGNAL_TOKEN");
    }

    @Test
    @DisplayName("every $VAR the body uses is declared in requires.env")
    void envDeclared() {
        Set<String> declared = Set.copyOf(strings(skill.frontmatter().path("metadata").path("openclaw")
                .path("requires").path("env")));
        Set<String> used = IntegrationDocs.envReferences(skill.body());
        assertThat(used).as("variables used in the body").isNotEmpty().contains("FORESIGNAL_TOKEN", "FORESIGNAL_URL");
        assertThat(declared).as("requires.env").containsAll(used);
    }

    @Test
    @DisplayName("every tool in SKILL.md exists in the MCP registry, with the scope its table states")
    void skillTools() {
        Map<String, Scope> catalog = IntegrationDocs.toolCatalog();
        Set<String> named = IntegrationDocs.toolNames(skill.body());
        assertThat(named).as("tools named in SKILL.md").containsAll(REQUIRED_TOOLS);
        assertThat(catalog.keySet()).as("MCP registry").containsAll(named);
        assertScopes(IntegrationDocs.toolScopeCells(skill.body()), catalog, "SKILL.md");
    }

    @Test
    @DisplayName("every tool in docs/Agent-Guide.md (tool tables and mcp call blocks) exists, with the scope its table states")
    void agentGuideTools() {
        String guide = IntegrationDocs.read(IntegrationDocs.AGENT_GUIDE);
        Map<String, Scope> catalog = IntegrationDocs.toolCatalog();
        Set<String> named = IntegrationDocs.toolNames(guide);
        assertThat(named).as("tools named in the Agent Guide").contains("create_briefing", "create_watch",
                "acknowledge_alert", "snooze_alert", "source_health", "list_alerts");
        assertThat(catalog.keySet()).as("MCP registry").containsAll(named);
        assertScopes(IntegrationDocs.toolScopeCells(guide), catalog, "docs/Agent-Guide.md");
        // The Agent Guide's tool table lists the whole registry.
        assertThat(IntegrationDocs.toolScopeCells(guide).keySet()).containsAll(catalog.keySet());
    }

    @Test
    @DisplayName("the skill asks for an agent token, never an admin token, and calls tool output untrusted data")
    void safety() {
        String body = skill.body().toLowerCase(Locale.ROOT);
        assertThat(body).contains("agent").contains("never").contains("admin").contains("untrusted");
        assertThat(IntegrationDocs.REAL_CREDENTIAL.matcher(skill.body()).find()).as("a real-looking credential").isFalse();
    }

    @Test
    @DisplayName("the catalog reader finds the production tools and the parsers reject malformed input")
    void helpers() {
        Map<String, Scope> catalog = IntegrationDocs.toolCatalog();
        assertThat(catalog).containsEntry("query_records", Scope.READ).containsEntry("create_watch", Scope.AGENT)
                .containsEntry("run_briefing", Scope.AGENT).hasSizeGreaterThanOrEqualTo(27);
        assertThatThrownBy(() -> IntegrationDocs.skill("# no frontmatter")).isInstanceOf(AssertionError.class);
        assertThat(IntegrationDocs.envReferences("curl -H \"Authorization: Bearer $A_B\" ${C}/x $lower"))
                .containsExactly("A_B", "C");
        String md = "| Tool | Scope |\n|---|---|\n| `a_b` | read |\n\ntext\n\n```mcp\nc_d {\"x\": 1}\n# comment\n```\n"
                + "| `not_a_tool` | x |\n";
        assertThat(IntegrationDocs.toolNames(md)).containsExactly("a_b", "c_d");
        assertThat(IntegrationDocs.toolScopeCells(md)).containsExactly(Map.entry("a_b", "read"));
    }

    private static void assertScopes(Map<String, String> cells, Map<String, Scope> catalog, String document) {
        List<String> wrong = new ArrayList<>();
        cells.forEach((tool, cell) -> {
            Scope actual = catalog.get(tool);
            String expected = actual == null ? "?" : actual.name().toLowerCase(Locale.ROOT);
            if (!cell.toLowerCase(Locale.ROOT).startsWith(expected)) {
                wrong.add(tool + ": documented '" + cell + "', registry " + expected);
            }
        });
        assertThat(wrong).as("scope cells of %s", document).isEmpty();
    }

    private static List<String> strings(JsonNode node) {
        List<String> out = new ArrayList<>();
        if (node != null && node.isArray()) {
            node.forEach(n -> out.add(n.asString()));
        }
        return out;
    }
}
