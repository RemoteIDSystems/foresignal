package ai.foresignal.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.openapi.OpenApiSpec;
import ai.foresignal.testsupport.openapi.OpenApiSpec.SpecOperation;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;

/**
 * The route-level contract test (prompt 020): the application's springdoc-generated document
 * ({@code /v1/openapi-generated.json}) against the authored {@code docs/OpenAPI.json}, minus the
 * operations on {@code pending-operations.txt}.
 *
 * <p>Shares the context of {@code SourcesControllerIT} (MockMvc plus {@link CollectorTestSources},
 * which registers adapters but no controllers, so the generated document holds production routes
 * only).
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
class OpenApiContractIT {

    @Autowired private MockMvc mvc;

    private JsonNode generated;

    @BeforeEach
    void fetchGenerated() throws Exception {
        String body = mvc.perform(get("/v1/openapi-generated.json"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        generated = OpenApiSpec.JSON.readTree(body);
    }

    private RouteContract.Report report(Set<String> pending) {
        return RouteContract.compare(OpenApiSpec.routes(), pending, OpenApiSpec.operations(generated, "paths"));
    }

    @Test
    @DisplayName("authored minus pending equals generated, with matching operationIds and 2xx codes")
    void codeMatchesSpec() {
        RouteContract.Report report = report(OpenApiSpec.pending().keySet());

        assertThat(report.isEmpty()).as(report.describe()).isTrue();
    }

    @Test
    @DisplayName("an implemented operation left on the pending list fails with 'remove it from pending-operations.txt'")
    void stalePendingEntryFails() {
        Set<String> pending = new HashSet<>(OpenApiSpec.pending().keySet());
        pending.add("getSystem");

        RouteContract.Report report = report(pending);

        assertThat(report.isEmpty()).isFalse();
        assertThat(report.stalePending()).singleElement().asString()
                .contains("getSystem").contains("remove it from pending-operations.txt");
        assertThat(report.describe()).contains("Stale pending entries");
    }

    @Test
    @DisplayName("an authored operation that is neither implemented nor pending is reported missing in code")
    void unimplementedAndUnlistedFails() {
        Set<String> pending = new HashSet<>(OpenApiSpec.pending().keySet());
        String someLaterOperation = pending.iterator().next();
        pending.remove(someLaterOperation);

        RouteContract.Report report = report(pending);

        assertThat(report.missingInCode()).singleElement().asString().contains(someLaterOperation);
    }

    @Test
    @DisplayName("every generated route has an operationId and a tag from the spec's tag list")
    void generatedOperationsAreAnnotated() {
        Set<String> specTags = new HashSet<>();
        OpenApiSpec.document().get("tags").forEach(t -> specTags.add(t.get("name").asString()));
        List<SpecOperation> ops = OpenApiSpec.operations(generated, "paths");

        assertThat(ops).isNotEmpty();
        for (SpecOperation op : ops) {
            assertThat(op.operationId()).as(op.method() + " " + op.path()).isNotBlank();
            JsonNode tags = op.node().get("tags");
            assertThat(tags).as(op.label() + " tags").isNotNull();
            tags.forEach(t -> assertThat(specTags).as(op.label()).contains(t.asString()));
        }
    }

    @Test
    @DisplayName("the springdoc document and its own endpoints are not part of the generated API")
    void generatedDocumentExcludesItself() {
        assertThat(generated.get("paths").has("/v1/openapi-generated.json")).isFalse();
        assertThat(generated.get("paths").has("/v1/openapi.json")).as("getOpenApiDocument is a controller").isTrue();
    }

    @Test
    @DisplayName("GET /v1/openapi.json is byte-identical to docs/OpenAPI.json")
    void servedDocumentIsTheAuthoredOne() throws Exception {
        byte[] served = mvc.perform(get("/v1/openapi.json"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        assertThat(served).isEqualTo(OpenApiSpec.bytes());
    }
}
