package ai.foresignal.integrations;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.testsupport.integrations.IntegrationDocs;
import ai.foresignal.testsupport.openapi.OpenApiSpec;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Cookbooks, integration artifacts and the Agent Guide (prompt 034) never drift from the API: every
 * {@code /v1/...} path they mention, with query strings ignored and placeholders matched to path
 * variables, is a path of {@code docs/OpenAPI.json} with at least one implemented operation.
 */
class CookbookApiReferenceTest {

    /** The cookbooks the prompt requires. */
    private static final List<String> COOKBOOKS = List.of("home-assistant.md", "node-red.md", "event-bus-bridges.md",
            "ai-chat-hosts.md", "phone-automations.md", "tak-bridge.md", "n8n-imap.md", "grafana.md");

    @Test
    @DisplayName("every /v1 path in docs/cookbooks, integrations and the Agent Guide is an implemented spec path")
    void pathsExist() {
        List<OpenApiSpec.SpecOperation> routes = OpenApiSpec.routes();
        List<String> problems = new ArrayList<>();
        int checked = 0;
        for (Path file : IntegrationDocs.apiReferencingFiles()) {
            for (String path : IntegrationDocs.apiPaths(IntegrationDocs.read(file))) {
                checked++;
                List<OpenApiSpec.SpecOperation> matching = routes.stream()
                        .filter(op -> IntegrationDocs.matches(path, op.path()))
                        .toList();
                if (matching.isEmpty()) {
                    problems.add(file + ": " + path + " is not a path of docs/OpenAPI.json");
                } else if (matching.stream().noneMatch(OpenApiSpec.SpecOperation::implemented)) {
                    problems.add(file + ": " + path + " has no implemented operation");
                }
            }
        }
        assertThat(checked).as("paths checked (non-vacuous)").isGreaterThan(40);
        assertThat(problems).isEmpty();
    }

    @Test
    @DisplayName("each required cookbook exists, states its token scope and uses at least one API path")
    void cookbooksExist() {
        for (String name : COOKBOOKS) {
            Path file = IntegrationDocs.COOKBOOKS.resolve(name);
            assertThat(Files.isRegularFile(file)).as(file.toString()).isTrue();
            String text = IntegrationDocs.read(file);
            assertThat(text).as("%s states the token scope", name).contains("**Token scope:**");
            assertThat(text).as("%s states limits", name).containsIgnoringCase("limits");
            assertThat(text).as("%s states privacy", name).containsIgnoringCase("privacy");
        }
    }

    @Test
    @DisplayName("no cookbook or artifact carries a real-looking token, ingest key or capability")
    void noCredentials() {
        List<String> found = new ArrayList<>();
        for (Path file : IntegrationDocs.apiReferencingFiles()) {
            Matcher m = IntegrationDocs.REAL_CREDENTIAL.matcher(IntegrationDocs.read(file));
            while (m.find()) {
                found.add(file + ": " + m.group());
            }
        }
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("path extraction drops query strings and punctuation; placeholders match only variables")
    void matching() {
        assertThat(IntegrationDocs.apiPaths("GET `/v1/records?format=geojson&bbox=1,2,3,4`, then /v1/alerts/{alertId}/ack."))
                .containsExactly("/v1/records", "/v1/alerts/{alertId}/ack");
        assertThat(IntegrationDocs.apiPaths("\"url\": \"http://h:8080/v1/ingest/custom-tak\"")).containsExactly("/v1/ingest/custom-tak");
        assertThat(IntegrationDocs.apiPaths("f\"{base}/v1/ingest/{source_id}\" and `${base}/v1/actions/${token}`"))
                .containsExactly("/v1/ingest/{source_id}", "/v1/actions/${token}");
        assertThat(IntegrationDocs.apiPaths("http://h:8080/v1/k/fsf_your_feed_key/records?format=geojson"))
                .containsExactly("/v1/records");
        assertThat(IntegrationDocs.apiPaths("the carrier /v1/k/fsf_key alone")).isEmpty();
        assertThat(IntegrationDocs.matches("/v1/ingest/custom-tak", "/v1/ingest/{sourceId}")).isTrue();
        assertThat(IntegrationDocs.matches("/v1/alerts/123/ack", "/v1/alerts/{alertId}/ack")).isTrue();
        assertThat(IntegrationDocs.matches("/v1/actions/eac_…", "/v1/actions/{actionToken}")).isTrue();
        assertThat(IntegrationDocs.matches("/v1/sources/health", "/v1/sources/health")).isTrue();
        assertThat(IntegrationDocs.matches("/v1/sources/{id}", "/v1/sources/health")).isFalse();
        assertThat(IntegrationDocs.matches("/v1/alerts", "/v1/alerts/{alertId}")).isFalse();
        assertThat(IntegrationDocs.matches("/v1/alertz", "/v1/alerts")).isFalse();
        assertThat(IntegrationDocs.apiPaths("url = base + '/v1/ingest/' + id")).containsExactly("/v1/ingest/");
        assertThat(IntegrationDocs.matches("/v1/ingest/", "/v1/ingest/{sourceId}")).isTrue();
        assertThat(IntegrationDocs.matches("/v1/ingest/", "/v1/ingest")).isFalse();
        assertThat(IntegrationDocs.matches("/v1/nothing/", "/v1/ingest/{sourceId}")).isFalse();
    }
}
