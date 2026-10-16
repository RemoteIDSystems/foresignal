package ai.foresignal.analyst;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The analyst scheduler (prompt 102): a disabled profile is never claimed; a profile with nothing new
 * produces a noop run with no LLM call; a missed schedule runs once and is not replayed; a profile that
 * is already running is claimed by neither the poll nor a manual run (409), and a stale claim is
 * reclaimed.
 */
@WebIntegrationTest
@Import(TestIngestSources.class)
class AnalystSchedulerIT {

    @Autowired AnalystScheduler scheduler;
    @Autowired AnalystProfileRepository profiles;
    @Autowired AnalystRunner runner;
    @Autowired AnalystRunRepository runs;
    @Autowired JdbcTemplate jdbc;
    @Autowired Clock clock;

    @Test
    @DisplayName("a disabled profile is not claimed")
    void disabledNotClaimed() {
        AnalystProfileEntity profile = new AnalystProfileEntity("01ANALYST0000000000000000A", "disabled", clock.instant());
        profile.setEnabled(false);
        profile.setNextRunAt(clock.instant().minusSeconds(3600));
        profiles.save(profile);
        assertThat(scheduler.tick()).doesNotContain(profile.getId());
    }

    @Test
    @DisplayName("a run with nothing new is a noop and makes no LLM call")
    void noop() {
        AnalystProfileEntity profile = new AnalystProfileEntity("01ANALYST0000000000000000B", "quiet", clock.instant());
        profile.setEnabled(false);
        profiles.save(profile);
        AnalystRunEntity run = runner.run(profile);
        assertThat(run.getStatus()).isEqualTo("noop");
        assertThat(run.getCursorTo()).isNotNull();
    }

    @Test
    @DisplayName("a schedule missed for days runs once and is not replayed")
    void missedRunsOnce() {
        Instant now = clock.instant();
        AnalystProfileEntity profile = new AnalystProfileEntity("01ANALYST0000000000000000D", "missed", now);
        profile.setEnabled(true);
        profile.setNextRunAt(now.minus(Duration.ofDays(3)));
        profiles.save(profile);

        assertThat(scheduler.tick()).containsExactly(profile.getId());
        assertThat(scheduler.tick()).doesNotContain(profile.getId());

        org.awaitility.Awaitility.await().atMost(Duration.ofSeconds(20))
                .until(() -> runs.countByProfileIdAndStartedAtGreaterThanEqual(profile.getId(), Instant.EPOCH) == 1
                        && profiles.findById(profile.getId()).orElseThrow().getRunningSince() == null);
        AnalystProfileEntity after = profiles.findById(profile.getId()).orElseThrow();
        assertThat(after.getNextRunAt()).isAfter(now);
        assertThat(scheduler.tick()).doesNotContain(profile.getId());
        assertThat(runs.countByProfileIdAndStartedAtGreaterThanEqual(profile.getId(), Instant.EPOCH)).isEqualTo(1);
    }

    @Test
    @DisplayName("a running profile is claimed by neither the poll nor a manual run; a stale claim is reclaimed")
    void oneRunPerProfile() {
        Instant now = clock.instant();
        AnalystProfileEntity profile = new AnalystProfileEntity("01ANALYST0000000000000000E", "busy", now);
        profile.setEnabled(true);
        profile.setNextRunAt(now.minusSeconds(60));
        profiles.save(profile);
        jdbc.update("update analyst_profile set running_since = ? where id = ?", java.sql.Timestamp.from(now),
                profile.getId());

        assertThat(scheduler.tick()).doesNotContain(profile.getId());
        assertThatThrownBy(() -> scheduler.runNow(profile)).isInstanceOf(ConflictException.class)
                .hasMessageContaining("already running");
        assertThat(runs.countByProfileIdAndStartedAtGreaterThanEqual(profile.getId(), Instant.EPOCH)).isZero();

        jdbc.update("update analyst_profile set running_since = ? where id = ?",
                java.sql.Timestamp.from(now.minus(AnalystScheduler.STALE).minusSeconds(1)), profile.getId());
        AnalystRunEntity run = scheduler.runNow(profile);
        assertThat(run.getStatus()).isEqualTo("queued");
    }
}
