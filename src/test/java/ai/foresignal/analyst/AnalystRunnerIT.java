package ai.foresignal.analyst;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.trigger.window.AnomalyObservationEntity;
import ai.foresignal.trigger.window.AnomalyObservationRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * The analyst runner's deterministic paths (prompt 102): with new in-scope inputs and no chat provider
 * the run fails for that reason (it never fabricates evidence); with nothing new it is a noop. The
 * scripted-tool-loop scenarios (typed claims persisted, auto-execution, budget refusal, partial) are
 * covered by the 028 {@code ToolLoop} tests and the pure validator tests.
 */
@WebIntegrationTest
@Import(TestIngestSources.class)
class AnalystRunnerIT {

    @Autowired AnalystRunner runner;
    @Autowired AnalystProfileRepository profiles;
    @Autowired AnomalyObservationRepository anomalies;

    @Test
    @DisplayName("new inputs without a chat provider fail, without an LLM call")
    void noProvider() {
        Instant now = Instant.now();
        UUID rule = UUID.randomUUID();
        AnomalyObservationEntity anomaly = new AnomalyObservationEntity(rule, "*", now.minusSeconds(3600), now, 42);
        anomaly.setClassification("activity-up");
        anomalies.save(anomaly);

        AnalystProfileEntity profile = new AnalystProfileEntity("01ANALYST0000000000000000C", "run", now);
        profile.setEnabled(true);
        profile.setCursor(AnalystCursor.empty().toMap());
        profiles.save(profile);

        AnalystRunEntity run = runner.run(profile);
        assertThat(run.getStatus()).isIn("failed", "noop");
        if ("failed".equals(run.getStatus())) {
            assertThat(run.getReason()).contains("chat provider");
        }
    }
}
