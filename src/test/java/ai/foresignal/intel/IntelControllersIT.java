package ai.foresignal.intel;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.api.ExposureController;
import ai.foresignal.api.IntelController;
import ai.foresignal.api.InvestigationsController;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/** The prompt 101 controllers: event CRUD, member ops, investigations and exposure. */
@WebIntegrationTest
@Import(TestIngestSources.class)
class IntelControllersIT {

    @Autowired IntelController intel;
    @Autowired InvestigationsController investigationsController;
    @Autowired ExposureController exposure;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper json;

    @Test
    @DisplayName("an agent event is created from a verified member and read back")
    void eventCrud() {
        clock.setInstant(Instant.parse("2026-03-01T12:00:00Z"));
        Instant now = clock.instant();
        records.ingestAt(TestIngestSources.STANDARD, now,
                NormalizedRecord.builder("e1", "thing").observedAt(now).point(-95.0, 32.0).put("name", "x").build());
        Long seq = jdbc.queryForObject("select max(seq) from record_version where source_id = ?",
                Long.class, TestIngestSources.STANDARD);

        var created = intel.createIntelEvent(json.readTree("{\"type\":\"custom\",\"title\":\"Finding\","
                + "\"members\":[{\"memberType\":\"record\",\"sourceId\":\"test-standard\",\"externalId\":\"e1\","
                + "\"seq\":" + seq + "}]}"));
        assertThat(created.getStatusCode().is2xxSuccessful()).isTrue();
        String id = String.valueOf(created.getBody().get("id"));
        assertThat(intel.getIntelEvent(id)).containsKeys("evidence", "inference", "speculation");
    }

    @Test
    @DisplayName("an investigation is created and exported")
    void investigationCrud() {
        var created = investigationsController.createInvestigation(json.readTree("{\"title\":\"Case\"}"));
        String id = String.valueOf(created.getBody().get("id"));
        assertThat(investigationsController.getInvestigation(id)).containsKey("items");
        assertThat(investigationsController.exportInvestigation(id, "md").getBody())
                .contains(InvestigationService.BANNER);
    }

    @Test
    @DisplayName("exposure accepts a geometry")
    void exposureByGeometry() {
        Map<String, Object> out = exposure.computeExposure(json.readTree(
                "{\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[-95.1,31.9],[-94.9,31.9],[-94.9,32.1],"
                        + "[-95.1,32.1],[-95.1,31.9]]]},\"population\":false}"));
        assertThat(out).containsKeys("facilities", "places", "entities");
    }
}
