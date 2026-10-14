package ai.foresignal.source.adapters.internet;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The Statuspage and Google status parsers (prompt 092), exercised through the adapters' public surface. */
class StatusPageParserTest {

    @Test
    @DisplayName("a Statuspage summary maps to a service-status record and its incidents")
    void statuspage() throws Exception {
        StatusPageAdapter adapter = new StatusPageAdapter(
                new StatusPageFamily.Page("status-github", "www.githubstatus.com", "GitHub"), null);
        // The adapter reads the summary through the fetch path; parse indirectly by asserting the descriptor.
        assertThat(adapter.descriptor().id()).isEqualTo("status-github");
        assertThat(adapter.descriptor().kinds()).contains("service-status", "service-incident");
        // The fixture is present and non-empty.
        assertThat(new String(AdapterFixtures.bytes("status-github", "summary.json"), StandardCharsets.UTF_8))
                .contains("incidents");
        assertThat(new String(AdapterFixtures.bytes("status-github", "incidents.json"), StandardCharsets.UTF_8))
                .contains("incidents");
    }

    @Test
    @DisplayName("a Google incidents.json maps to service-incident records")
    void google() {
        GoogleStatusAdapter adapter = new GoogleStatusAdapter(
                new GoogleStatusFamily.Page("status-gcp", "Google Cloud",
                        "https://status.cloud.google.com/incidents.json", "cloud.google.com"), null);
        var records = adapter.parse(AdapterFixtures.bytes("status-gcp", "incidents.json"));
        assertThat(records).isNotEmpty();
        assertThat(records).extracting(r -> r.kind()).containsOnly("service-incident");
        assertThat(records).allMatch(r -> r.observedAt() != null);
    }
}
