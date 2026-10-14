package ai.foresignal.source.adapters.internet;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The Google status page parser (prompt 092): GCP and Workspace incidents.json. */
class GoogleStatusParserTest {

    private static GoogleStatusAdapter adapter(String id, String fixture) {
        return new GoogleStatusAdapter(new GoogleStatusFamily.Page(id, "Test",
                "https://example.org/incidents.json", "example.org"), null);
    }

    @Test
    @DisplayName("GCP incidents map to service-incident records with their update text")
    void gcp() {
        List<NormalizedRecord> records = adapter("status-gcp", "status-gcp")
                .parse(AdapterFixtures.bytes("status-gcp", "incidents.json"));
        assertThat(records).isNotEmpty();
        assertThat(records).extracting(NormalizedRecord::kind).containsOnly("service-incident");
        assertThat(records).allMatch(r -> r.eventTime() != null && r.observedAt() != null);
        assertThat(records).anyMatch(r -> r.payload().containsKey("serviceName"));
    }

    @Test
    @DisplayName("Workspace incidents parse too")
    void workspace() {
        List<NormalizedRecord> records = adapter("status-google-workspace", "status-google-workspace")
                .parse(AdapterFixtures.bytes("status-google-workspace", "incidents.json"));
        assertThat(records).isNotEmpty();
    }
}
