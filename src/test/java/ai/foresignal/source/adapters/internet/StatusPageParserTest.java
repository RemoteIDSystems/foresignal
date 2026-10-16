package ai.foresignal.source.adapters.internet;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The Statuspage and Google status parsers (prompt 092): Statuspage, the incident.io shim and Google. */
class StatusPageParserTest {

    @Test
    @DisplayName("a Statuspage summary maps to a service-status record with the top-level indicator")
    void statuspage() {
        StatusPageAdapter.Summary summary = StatusPageAdapter.parseSummary(
                AdapterFixtures.bytes("status-github", "summary.json"));
        assertThat(summary.listsIncidents()).isTrue();
        NormalizedRecord status = summary.status();
        assertThat(status.kind()).isEqualTo("service-status");
        assertThat(status.externalId()).isEqualTo("kctbh9vrtdwd");
        assertThat(status.payload()).containsEntry("indicator", "none")
                .containsEntry("description", "All Systems Operational");
        assertThat(status.observedAt()).isNotNull();
        assertThat(summary.openIncidents()).allMatch(r -> r.kind().equals("service-incident")
                && !"resolved".equals(r.payload().get("status")));
    }

    @Test
    @DisplayName("an open incident carries its fields, capped update text and no resolvedAt")
    void openIncident() {
        String body = "{\"page\":{\"id\":\"p1\",\"updated_at\":\"2026-10-14T11:00:00Z\"},"
                + "\"status\":{\"indicator\":\"major\",\"description\":\"Partial outage\"},"
                + "\"components\":[{\"name\":\"API\",\"status\":\"major_outage\"},{\"name\":\"Web\","
                + "\"status\":\"operational\"}],"
                + "\"incidents\":[{\"id\":\"INC1\",\"name\":\"API down\",\"status\":\"identified\",\"impact\":\"major\","
                + "\"shortlink\":\"https://stspg.io/x\",\"created_at\":\"2026-10-14T10:00:00Z\","
                + "\"started_at\":\"2026-10-14T09:55:00Z\",\"updated_at\":\"2026-10-14T11:00:00Z\","
                + "\"components\":[{\"name\":\"API\"}],\"incident_updates\":[{\"body\":\"" + "x".repeat(5000)
                + "\"}]},{\"id\":\"INC0\",\"status\":\"resolved\"}]}";
        StatusPageAdapter.Summary summary = StatusPageAdapter.parseSummary(body.getBytes(StandardCharsets.UTF_8));
        assertThat(summary.status().payload()).containsEntry("indicator", "major");
        assertThat(summary.status().payload().get("degradedComponents"))
                .isEqualTo(List.of(java.util.Map.of("name", "API", "status", "major_outage")));
        assertThat(summary.openIncidents()).hasSize(1);
        NormalizedRecord incident = summary.openIncidents().get(0);
        assertThat(incident.externalId()).isEqualTo("incident:INC1");
        assertThat(incident.eventTime()).isEqualTo(java.time.Instant.parse("2026-10-14T09:55:00Z"));
        assertThat(incident.payload()).containsEntry("components", List.of("API")).containsEntry("updateCount", 1);
        assertThat(incident.payload().get("resolvedAt")).isNull();
        assertThat(((String) incident.payload().get("latestUpdate")).length())
                .isLessThanOrEqualTo(StatusPageAdapter.MAX_UPDATE_BYTES);
    }

    @Test
    @DisplayName("the incident.io shim: no incidents array in summary.json; incidents.json filtered locally")
    void incidentIoShim() {
        StatusPageAdapter.Summary summary = StatusPageAdapter.parseSummary(
                AdapterFixtures.bytes("status-openai", "summary.json"));
        assertThat(summary.listsIncidents()).isFalse();
        assertThat(summary.status().payload()).containsEntry("indicator", "none");
        assertThat(summary.status().externalId()).isEqualTo("01JMDK9XYNY6RXSED6SDWW50WY");

        var incidents = StatusPageAdapter.incidentsArray(AdapterFixtures.bytes("status-openai", "incidents.json"));
        assertThat(incidents.size()).isEqualTo(2);
        assertThat(StatusPageAdapter.openIncidents(incidents)).as("both captured incidents are resolved").isEmpty();
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
