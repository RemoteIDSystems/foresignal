package ai.foresignal.source.adapters.humanitarian;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.ingest.PersistReason;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * End-to-end ingest for the prompt 090 humanitarian sources: OpenFEMA records reach the store and the source
 * carries its eligibility {@code llmNotice}; a US travel-advisory level change is persisted SIGNIFICANT.
 */
class HumanitarianIngestIT extends WeatherIngestSupport {

    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired SourceRegistry registry;
    @Autowired McpOutputShaper shaper;

    @Test
    @DisplayName("OpenFEMA records ingest and the descriptor carries the eligibility notice")
    void openFema() {
        UPSTREAM.stubFor(get(urlPathEqualTo("/api/open/v2/DisasterDeclarationsSummaries"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("openfema-declarations", "declarations.json"))));
        configure("openfema-declarations", true, Map.of());
        fetch("openfema-declarations");

        assertThat(versions("openfema-declarations", (PersistReason) null)).isGreaterThan(0);
        assertThat(registry.get("openfema-declarations").descriptor().llmNotice())
                .contains("DisasterAssistance.gov");
        // the registered notice is what every LLM-context path attaches (McpOutputShaper; AskLlmNoticeIT
        // proves the Ask tool result end to end with a test source)
        ObjectNode hit = JSON.createObjectNode();
        shaper.searchHitNotice(Map.of("docType", "record", "docId", "openfema-declarations:x"), hit);
        assertThat(hit.path("llmNotice").asString()).isEqualTo(OpenFemaDeclarationsAdapter.LLM_NOTICE);
    }

    @Test
    @DisplayName("a travel-advisory level change is persisted SIGNIFICANT")
    void travelAdvisoryLevelChange() {
        serve(advisories(3));
        configure("travel-advisory-us", true, Map.of());
        fetch("travel-advisory-us");
        assertThat(versions("travel-advisory-us", (PersistReason) null)).isGreaterThan(0);

        serve(advisories(4));
        fetch("travel-advisory-us");
        assertThat(versions("travel-advisory-us", PersistReason.SIGNIFICANT)).isGreaterThanOrEqualTo(1);
    }

    private static void serve(byte[] body) {
        UPSTREAM.stubFor(get(urlPathEqualTo("/api/TravelAdvisories"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(body)));
    }

    private static byte[] advisories(int level) {
        JsonNode root = JSON.readTree(AdapterFixtures.bytes("travel-advisory-us", "advisories.json"));
        ObjectNode first = (ObjectNode) root.get(0);
        first.put("Title", "Saudi Arabia - Level " + level + ": Reconsider Travel");
        return root.toString().getBytes(StandardCharsets.UTF_8);
    }
}
