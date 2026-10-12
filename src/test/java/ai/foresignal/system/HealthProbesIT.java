package ai.foresignal.system;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.store.history.PartitionManager;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.TestPartitions;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The probe groups (prompt 019): liveness is {@code livenessState} only, readiness adds the
 * database, the writer and the partitions, and {@code catchup} is independent of readiness.
 *
 * <p>The database and writer contributors and the catch-up monitor are mocks, standing in for a
 * stopped database, a writer without state and a backlog, which a shared test database cannot
 * provide (and the monitor's own 5-second refresh would overwrite).
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RetentionTestSources.class)
// Spring Boot 4.1 declares dbHealthContributor as a HealthContributor, so a HealthIndicator mock of
// that name is a second definition rather than a type-compatible replacement, and Spring refuses it
// unless overriding is allowed (prompt 022). This context is already this class's own.
@TestPropertySource(properties = "spring.main.allow-bean-definition-overriding=true")
class HealthProbesIT {

    @Autowired MockMvc mvc;
    @Autowired PartitionManager partitions;

    @MockitoBean
    CatchUpMonitor catchUp;

    @MockitoBean(name = "dbHealthContributor")
    HealthIndicator db;

    @MockitoBean
    IngestWriterHealthIndicator writer;

    @BeforeEach
    void up() {
        TestPartitions.restore(partitions);
        stub(db, Health.up().build());
        stub(writer, Health.up().build());
        catchingUp(false);
    }

    /**
     * Stubs both {@code health()} and {@code health(boolean)}: Spring Boot 4.1's registry calls the
     * default {@code health(boolean)}, which a Mockito mock does not route to {@code health()}, so
     * stubbing only {@code health()} left the probes reading null (prompt 022).
     */
    static void stub(HealthIndicator indicator, Health health) {
        when(indicator.health()).thenReturn(health);
        when(indicator.health(anyBoolean())).thenReturn(health);
    }

    void catchingUp(boolean value) {
        when(catchUp.last()).thenReturn(new CatchUpMonitor.CatchUp(value ? CatchUpMonitor.LAG_THRESHOLD + 1 : 0, 0, value));
    }

    @Test
    @DisplayName("with everything up, liveness and readiness are UP")
    void allUp() throws Exception {
        mvc.perform(get("/actuator/health/liveness")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        mvc.perform(get("/actuator/health/readiness")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    @DisplayName("a database outage leaves liveness UP and takes readiness DOWN")
    void databaseDown() throws Exception {
        stub(db, Health.down().build());

        mvc.perform(get("/actuator/health/liveness")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        mvc.perform(get("/actuator/health/readiness")).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"));
    }

    @Test
    @DisplayName("a writer without state takes readiness DOWN")
    void writerDown() throws Exception {
        stub(writer, Health.down().build());

        mvc.perform(get("/actuator/health/readiness")).andExpect(status().isServiceUnavailable());
        mvc.perform(get("/actuator/health/liveness")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("catching up is OUT_OF_SERVICE on the catchup group while readiness stays UP")
    void catchUpIndependent() throws Exception {
        catchingUp(true);

        mvc.perform(get("/actuator/health/catchup")).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("OUT_OF_SERVICE"));
        mvc.perform(get("/actuator/health/readiness")).andExpect(status().isOk());

        catchingUp(false);
        mvc.perform(get("/actuator/health/catchup")).andExpect(status().isOk());
    }
}
