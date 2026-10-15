package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.derive.DerivedRunner;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestRecords;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The prompt 099 derived layers end to end: every generator registers as a DERIVED source and runs
 * on the 052 runtime, and an enabled jamming layer completes invocations that write fetch-log rows.
 * The windows, thresholds and coverage gates are unit-tested per generator.
 */
@WebIntegrationTest
class DerivedLayersIT {

    private static final String JAMMING = GpsJammingGenerator.ID;
    private static final Duration WAIT = Duration.ofSeconds(20);

    @Autowired DerivedRunner runner;
    @Autowired SourceService sources;
    @Autowired SourceRegistry registry;
    @Autowired TestRecords records;
    @Autowired JdbcTemplate jdbc;

    private void enable(String id, boolean enabled) {
        sources.update(id, new SourceSettingsUpdate(enabled, null, Map.of(), Budget.of(1, 1), null, false, null, false));
    }

    private int derivedFetchRows(String sourceId) {
        Integer n = jdbc.queryForObject(
                "select count(*) from fetch_log where source_id = ? and principal = 'derived'", Integer.class, sourceId);
        return n == null ? 0 : n;
    }

    @AfterEach
    void stop() {
        enable(JAMMING, false);
        await().atMost(WAIT).until(() -> !runner.running().contains(JAMMING));
    }

    @Test
    @DisplayName("every derived layer is registered as a DERIVED source")
    void registered() {
        for (String id : new String[] {JAMMING, AisGapGenerator.ID, AircraftFusionGenerator.ID,
                CorroborationGenerator.ID, PortCallGenerator.ID, CpaGenerator.ID, LoiterGenerator.ID,
                RendezvousGenerator.ID, DeceptionGenerator.ID, HoldingPatternGenerator.ID,
                EmergencySquawkGenerator.ID, ChokepointGenerator.ID, MilNavalGenerator.ID,
                FeltLocallyGenerator.ID, CableRiskGenerator.ID, DarkVesselMatchGenerator.ID,
                LightningHoldoverGenerator.ID, HfBlackoutGenerator.ID}) {
            assertThat(registry.find(id)).as(id).isPresent();
            assertThat(registry.get(id).descriptor().mode()).isEqualTo(Mode.DERIVED);
        }
    }

    @Test
    @DisplayName("enabling the jamming layer runs it and leaves a derived fetch-log row")
    void jammingRuns() {
        int before = derivedFetchRows(JAMMING);
        enable(JAMMING, true);
        await().atMost(WAIT).until(() -> runner.running().contains(JAMMING));
        assertThat(runner.awaitIdle(JAMMING, WAIT)).isTrue();
        assertThat(derivedFetchRows(JAMMING)).isGreaterThan(before);
    }
}
