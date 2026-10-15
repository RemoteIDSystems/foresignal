package ai.foresignal.analyst;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * The analyst scheduler (prompt 102): a disabled profile is never claimed; a profile with nothing new
 * produces a noop run with no LLM call.
 */
@WebIntegrationTest
@Import(TestIngestSources.class)
class AnalystSchedulerIT {

    @Autowired AnalystScheduler scheduler;
    @Autowired AnalystProfileRepository profiles;
    @Autowired AnalystRunner runner;
    @Autowired AnalystRunRepository runs;

    @Test
    @DisplayName("a disabled profile is not claimed")
    void disabledNotClaimed() {
        AnalystProfileEntity profile = new AnalystProfileEntity("01ANALYST0000000000000000A", "disabled", Instant.now());
        profile.setEnabled(false);
        profile.setNextRunAt(Instant.now().minusSeconds(3600));
        profiles.save(profile);
        assertThat(scheduler.tick()).doesNotContain(profile.getId());
    }

    @Test
    @DisplayName("a run with nothing new is a noop and makes no LLM call")
    void noop() {
        AnalystProfileEntity profile = new AnalystProfileEntity("01ANALYST0000000000000000B", "quiet", Instant.now());
        profile.setEnabled(false);
        profiles.save(profile);
        AnalystRunEntity run = runner.run(profile);
        assertThat(run.getStatus()).isEqualTo("noop");
        assertThat(run.getCursorTo()).isNotNull();
    }
}
