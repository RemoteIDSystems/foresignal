package ai.foresignal.integrations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import ai.foresignal.testsupport.integrations.IntegrationDocs;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The importable artifacts of prompt 034 parse and have the shape their tools import: every JSON and
 * YAML file parses, the Node-RED and n8n exports and the Grafana dashboards have their expected top
 * level, the MCP client configs point at {@code /mcp} with a bearer header, and the Python bridge is
 * pinned and parses XML safely.
 */
class IntegrationArtifactsTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final Path INTEGRATIONS = IntegrationDocs.INTEGRATIONS;

    @Test
    @DisplayName("every .json artifact parses as JSON and every .yaml/.yml artifact as YAML")
    void everythingParses() {
        List<Path> json = IntegrationDocs.files(INTEGRATIONS, Set.of("json"));
        List<Path> yaml = IntegrationDocs.files(INTEGRATIONS, Set.of("yaml", "yml"));
        assertThat(json).as("JSON artifacts").hasSizeGreaterThanOrEqualTo(6);
        assertThat(yaml).as("YAML artifacts").hasSizeGreaterThanOrEqualTo(6);
        for (Path file : json) {
            assertThatCode(() -> JSON.readTree(IntegrationDocs.read(file))).as(file.toString()).doesNotThrowAnyException();
        }
        for (Path file : yaml) {
            assertThatCode(() -> IntegrationDocs.YAML.readTree(IntegrationDocs.read(file))).as(file.toString())
                    .doesNotThrowAnyException();
        }
    }

    @Test
    @DisplayName("the Node-RED export is an array of nodes with unique ids, one tab, and wires only to existing nodes")
    void nodeRed() {
        JsonNode flows = JSON.readTree(IntegrationDocs.read(INTEGRATIONS.resolve("node-red/foresignal-flows.json")));
        assertThat(flows.isArray()).isTrue();
        Set<String> ids = new HashSet<>();
        Set<String> types = new HashSet<>();
        for (JsonNode node : flows) {
            assertThat(node.path("id").asString()).as("node id").isNotBlank();
            assertThat(node.path("type").asString()).as("node type").isNotBlank();
            assertThat(ids.add(node.path("id").asString())).as("unique id %s", node.path("id").asString()).isTrue();
            types.add(node.path("type").asString());
        }
        assertThat(types).contains("tab", "http in", "http response", "function", "http request");
        String tab = null;
        for (JsonNode node : flows) {
            if ("tab".equals(node.path("type").asString())) {
                tab = node.path("id").asString();
            }
        }
        for (JsonNode node : flows) {
            String type = node.path("type").asString();
            if (!"tab".equals(type) && node.has("z")) {
                assertThat(node.path("z").asString()).as("%s on the tab", node.path("id").asString()).isEqualTo(tab);
            }
            for (JsonNode port : node.path("wires")) {
                for (JsonNode target : port) {
                    assertThat(ids).as("wire target of %s", node.path("id").asString()).contains(target.asString());
                }
            }
        }
    }

    @Test
    @DisplayName("the n8n export has name, nodes (IMAP trigger, Code, HTTP Request), connections between existing nodes and settings")
    void n8n() {
        JsonNode workflow = JSON.readTree(IntegrationDocs.read(INTEGRATIONS.resolve("n8n/imap-to-ingest.json")));
        assertThat(workflow.path("name").asString()).isNotBlank();
        assertThat(workflow.path("nodes").isArray()).isTrue();
        assertThat(workflow.path("connections").isObject()).isTrue();
        assertThat(workflow.path("settings").isObject()).isTrue();
        Set<String> names = new HashSet<>();
        Set<String> types = new HashSet<>();
        for (JsonNode node : workflow.path("nodes")) {
            assertThat(node.path("name").asString()).isNotBlank();
            assertThat(node.path("type").asString()).isNotBlank();
            assertThat(node.path("typeVersion").isNumber()).as("typeVersion of %s", node.path("name").asString()).isTrue();
            assertThat(node.path("position").isArray()).isTrue();
            assertThat(node.path("parameters").isObject()).isTrue();
            names.add(node.path("name").asString());
            types.add(node.path("type").asString());
        }
        assertThat(types).contains("n8n-nodes-base.emailReadImap", "n8n-nodes-base.code", "n8n-nodes-base.httpRequest");
        for (String from : workflow.path("connections").propertyNames()) {
            assertThat(names).as("connection source").contains(from);
            for (JsonNode port : workflow.path("connections").path(from).path("main")) {
                for (JsonNode target : port) {
                    assertThat(names).as("connection target").contains(target.path("node").asString());
                }
            }
        }
    }

    @Test
    @DisplayName("Grafana dashboards are JSON with uid, title, schemaVersion and panels; provisioning files are apiVersion 1")
    void grafana() {
        List<Path> dashboards = IntegrationDocs.files(INTEGRATIONS.resolve("grafana/dashboards"), Set.of("json"));
        assertThat(dashboards).extracting(p -> p.getFileName().toString())
                .contains("foresignal-operations.json", "foresignal-live-map.json", "foresignal-alerts.json");
        Set<String> uids = new HashSet<>();
        for (Path file : dashboards) {
            JsonNode dashboard = JSON.readTree(IntegrationDocs.read(file));
            assertThat(dashboard.path("uid").asString()).as("uid of %s", file).isNotBlank();
            assertThat(uids.add(dashboard.path("uid").asString())).as("unique uid").isTrue();
            assertThat(dashboard.path("title").asString()).as("title of %s", file).isNotBlank();
            assertThat(dashboard.path("schemaVersion").isInt()).as("schemaVersion of %s", file).isTrue();
            assertThat(dashboard.path("panels").isArray() && !dashboard.path("panels").isEmpty())
                    .as("panels of %s", file).isTrue();
        }
        JsonNode datasources = IntegrationDocs.YAML.readTree(
                IntegrationDocs.read(INTEGRATIONS.resolve("grafana/provisioning/datasources/foresignal.yaml")));
        assertThat(datasources.path("apiVersion").asInt()).isEqualTo(1);
        List<String> dsTypes = new ArrayList<>();
        datasources.path("datasources").forEach(ds -> dsTypes.add(ds.path("type").asString()));
        assertThat(dsTypes).containsExactlyInAnyOrder("prometheus", "yesoreyeram-infinity-datasource");
        assertThat(dsTypes).doesNotContain("postgres", "grafana-postgresql-datasource");
        JsonNode providers = IntegrationDocs.YAML.readTree(
                IntegrationDocs.read(INTEGRATIONS.resolve("grafana/provisioning/dashboards/foresignal.yaml")));
        assertThat(providers.path("apiVersion").asInt()).isEqualTo(1);
        assertThat(providers.path("providers").isArray()).isTrue();
    }

    @Test
    @DisplayName("MCP client configs: Claude Desktop through a pinned bridge, LibreChat streamable-http, both with a bearer header for /mcp")
    void mcpClients() {
        JsonNode desktop = JSON.readTree(IntegrationDocs.read(INTEGRATIONS.resolve("mcp-clients/claude-desktop.json")));
        JsonNode server = desktop.path("mcpServers").path("foresignal");
        assertThat(server.path("command").asString()).isNotBlank();
        String args = server.path("args").toString();
        assertThat(args).contains("/mcp").contains("--header").containsPattern("mcp-remote@\\d+\\.\\d+\\.\\d+");
        JsonNode librechat = IntegrationDocs.YAML.readTree(IntegrationDocs.read(INTEGRATIONS.resolve("mcp-clients/librechat.yaml")));
        JsonNode foresignal = librechat.path("mcpServers").path("foresignal");
        assertThat(foresignal.path("type").asString()).isEqualTo("streamable-http");
        assertThat(foresignal.path("url").asString()).endsWith("/mcp");
        assertThat(foresignal.path("headers").path("Authorization").asString()).isEqualTo("Bearer ${FORESIGNAL_TOKEN}");
    }

    @Test
    @DisplayName("Home Assistant package: webhook automation local_only with rest_command, REST sensors with headers, ingest rest_command")
    void homeAssistant() {
        JsonNode pkg = IntegrationDocs.YAML.readTree(IntegrationDocs.read(INTEGRATIONS.resolve("home-assistant/foresignal_package.yaml")));
        assertThat(pkg.path("rest_command").isObject()).isTrue();
        assertThat(pkg.path("rest").isArray()).isTrue();
        assertThat(pkg.path("automation").isArray()).isTrue();
        boolean webhook = false;
        for (JsonNode automation : pkg.path("automation")) {
            for (JsonNode trigger : automation.path("triggers").isMissingNode() ? automation.path("trigger") : automation.path("triggers")) {
                if ("webhook".equals(trigger.path("trigger").asString())) {
                    webhook = true;
                    assertThat(trigger.path("local_only").asBoolean(false)).as("local_only").isTrue();
                    assertThat(trigger.path("webhook_id").asString()).isNotBlank();
                }
            }
        }
        assertThat(webhook).as("an automation with a webhook trigger").isTrue();
        for (JsonNode resource : pkg.path("rest")) {
            assertThat(resource.path("resource").asString()).contains("/v1/");
            assertThat(resource.path("headers").isObject()).as("headers of %s", resource.path("resource").asString()).isTrue();
        }
    }

    @Test
    @DisplayName("the TAK bridge pins every requirement and parses XML only through defusedxml")
    void takBridge() {
        Path dir = INTEGRATIONS.resolve("pytak");
        List<String> requirements = IntegrationDocs.read(dir.resolve("requirements.txt")).lines()
                .map(String::strip).filter(l -> !l.isEmpty() && !l.startsWith("#")).toList();
        assertThat(requirements).isNotEmpty().allSatisfy(l -> assertThat(l).containsPattern("^[A-Za-z0-9_.\\-\\[\\]]+==[0-9][0-9A-Za-z.]*$"));
        assertThat(requirements).anySatisfy(l -> assertThat(l).startsWith("pytak=="))
                .anySatisfy(l -> assertThat(l).startsWith("defusedxml=="));
        String script = IntegrationDocs.read(dir.resolve("foresignal_tak_bridge.py"));
        assertThat(script).contains("defusedxml").doesNotContain("import xml.etree").doesNotContain("from xml.etree")
                .doesNotContain("xml.dom.minidom").contains("/v1/ingest/");
        assertThat(Files.exists(Path.of("src", "main", "java", "ai", "foresignal", "integrations"))).isFalse();
    }
}
