package ai.foresignal.infra;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/** The affected-dependents query (prompt 104): direct seeds, caveats and attribution. */
@WebIntegrationTest
@Import(TestIngestSources.class)
class InfraImpactServiceIT {

    @Autowired InfraGraphBuilder builder;
    @Autowired InfraImpactService impact;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;

    @Test
    @DisplayName("an affected query returns a summary with caveats and attribution")
    void affected() {
        clock.setInstant(Instant.parse("2026-03-01T12:00:00Z"));
        Instant now = clock.instant();
        records.ingestAt("wri-power-plants", now,
                NormalizedRecord.builder("pp1", "power-plant").observedAt(now).point(-95, 32)
                        .put("name", "Plant").put("capacityMw", 500).build());
        builder.rebuild(Map.of());
        Map<String, Object> result = impact.affected(Geo.point(-95, 32).buffer(0.5), 2, "possible", List.of());
        assertThat(result).containsKeys("summary", "caveats", "attribution", "buildId");
        assertThat((List<?>) result.get("caveats")).isNotEmpty();
    }
}
