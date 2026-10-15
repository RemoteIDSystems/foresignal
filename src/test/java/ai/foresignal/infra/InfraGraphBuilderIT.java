package ai.foresignal.infra;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/** The infrastructure graph builder (prompt 104): a rebuild produces nodes and a build record. */
@WebIntegrationTest
@Import(TestIngestSources.class)
class InfraGraphBuilderIT {

    @Autowired InfraGraphBuilder builder;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;

    @Test
    @DisplayName("a rebuild nodes the mapped sources and finishes")
    void rebuild() {
        clock.setInstant(Instant.parse("2026-03-01T12:00:00Z"));
        Instant now = clock.instant();
        records.ingestAt("wri-power-plants", now,
                NormalizedRecord.builder("pp1", "power-plant").observedAt(now).point(-95, 32)
                        .put("name", "Plant").put("capacityMw", 500).build());
        records.ingestAt("world-port-index", now,
                NormalizedRecord.builder("wpi:1", "port").observedAt(now).point(-95.1, 32.1)
                        .put("portName", "Port").build());

        InfraBuildEntity build = builder.rebuild(java.util.Map.of());
        assertThat(build.getNodes()).isGreaterThanOrEqualTo(2);
        assertThat(build.getFinished()).isNotNull();
        assertThat(build.getEdgesByMethod()).isNotNull();
    }
}
