package ai.foresignal.briefing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.briefing.BriefingItSupport;
import ai.foresignal.testsupport.briefing.BriefingTestSources;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * {@link BriefingScheduler} with the {@code MutableClock} (prompt 029): it fires at the cron time in the
 * briefing's timezone, across a DST change; runs missed while down run once; a briefing never runs
 * twice at once; and two schedulers sharing the table claim each due briefing exactly once
 * ({@code FOR UPDATE SKIP LOCKED}).
 */
@IntegrationTest
@Import(BriefingTestSources.class)
class BriefingSchedulerIT extends BriefingItSupport {

    @Autowired BriefingScheduler scheduler;
    @Autowired BriefingProperties properties;
    @Autowired PlatformTransactionManager transactionManager;

    UUID briefing(String name, String schedule, String timezone) {
        return id(briefings.create(json("{\"name\":\"" + name + "\",\"schedule\":\"" + schedule + "\",\"timezone\":\""
                + timezone + "\",\"lookback\":\"1h\"}"), agentCaller()));
    }

    Instant nextRun(UUID id) {
        return briefingRepository.findById(id).orElseThrow().getNextRunAt();
    }

    static List<BriefingRunEntity> await(List<Future<BriefingRunEntity>> futures) throws Exception {
        List<BriefingRunEntity> out = new ArrayList<>();
        for (Future<BriefingRunEntity> future : futures) {
            out.add(future.get(60, TimeUnit.SECONDS));
        }
        return out;
    }

    long runs(UUID id) {
        Long n = jdbc.queryForObject("SELECT count(*) FROM briefing_run WHERE briefing_id = ?", Long.class, id);
        return n == null ? 0 : n;
    }

    @Test
    @DisplayName("fires at 07:00 America/Chicago (12:00 UTC in daylight time), then schedules the next day")
    void firesAtCronTimeInZone() throws Exception {
        clock.setInstant(Instant.parse("2026-10-12T10:00:00Z"));
        UUID id = briefing("Morning", "0 7 * * *", "America/Chicago");
        assertThat(nextRun(id)).isEqualTo(Instant.parse("2026-10-12T12:00:00Z"));

        clock.setInstant(Instant.parse("2026-10-12T11:59:30Z"));
        assertThat(scheduler.tick()).as("not yet due").isEmpty();

        clock.setInstant(Instant.parse("2026-10-12T12:00:10Z"));
        List<BriefingRunEntity> fired = await(scheduler.tick());
        assertThat(fired).singleElement().satisfies(run -> {
            assertThat(run.getBriefingId()).isEqualTo(id);
            assertThat(run.getStatus()).isEqualTo("succeeded");
            assertThat(run.getPeriodTo()).isEqualTo(Instant.parse("2026-10-12T12:00:10Z"));
        });
        assertThat(nextRun(id)).isEqualTo(Instant.parse("2026-10-13T12:00:00Z"));
        BriefingEntity stored = briefingRepository.findById(id).orElseThrow();
        assertThat(stored.getLastRunStatus()).isEqualTo("succeeded");
        assertThat(stored.getRunningSince()).isNull();
    }

    @Test
    @DisplayName("across the end of daylight saving time, 07:00 Chicago moves from 12:00 to 13:00 UTC")
    void dstTransition() throws Exception {
        clock.setInstant(Instant.parse("2026-10-31T12:30:00Z"));
        UUID id = briefing("Morning", "0 7 * * *", "America/Chicago");
        assertThat(nextRun(id)).isEqualTo(Instant.parse("2026-11-01T13:00:00Z"));

        clock.setInstant(Instant.parse("2026-11-01T12:30:00Z"));
        assertThat(scheduler.tick()).as("07:00 CDT would have been 12:00 UTC; not due").isEmpty();

        clock.setInstant(Instant.parse("2026-11-01T13:00:01Z"));
        assertThat(await(scheduler.tick())).hasSize(1);
        assertThat(nextRun(id)).isEqualTo(Instant.parse("2026-11-02T13:00:00Z"));
    }

    @Test
    @DisplayName("runs missed during downtime run once, never replayed")
    void missedRunsOnce() throws Exception {
        clock.setInstant(Instant.parse("2026-10-12T00:30:00Z"));
        UUID id = briefing("Hourly", "0 * * * *", "UTC");
        assertThat(nextRun(id)).isEqualTo(Instant.parse("2026-10-12T01:00:00Z"));

        clock.setInstant(Instant.parse("2026-10-12T05:10:00Z"));
        assertThat(await(scheduler.tick())).hasSize(1);
        assertThat(scheduler.tick()).as("the four missed hours are not replayed").isEmpty();
        assertThat(runs(id)).isEqualTo(1);
        assertThat(nextRun(id)).isEqualTo(Instant.parse("2026-10-12T06:00:00Z"));
    }

    @Test
    @DisplayName("no overlap: a running briefing is neither claimed nor run now; a stale mark is reclaimed")
    void noOverlap() throws Exception {
        clock.setInstant(Instant.parse("2026-10-12T00:30:00Z"));
        UUID id = briefing("Hourly", "0 * * * *", "UTC");
        clock.setInstant(Instant.parse("2026-10-12T01:00:30Z"));
        jdbc.update("UPDATE briefing SET running_since = ? WHERE id = ?", Timestamp.from(clock.instant()), id);

        assertThat(scheduler.tick()).isEmpty();
        assertThatThrownBy(() -> briefings.runNow(id)).isInstanceOf(ConflictException.class)
                .hasMessageContaining("in progress");

        jdbc.update("UPDATE briefing SET running_since = ? WHERE id = ?",
                Timestamp.from(clock.instant().minus(properties.staleRun()).minusSeconds(1)), id);
        assertThat(await(scheduler.tick())).hasSize(1);
        assertThat(runs(id)).isEqualTo(1);
    }

    @Test
    @DisplayName("two schedulers ticking at once claim each due briefing exactly once (SKIP LOCKED)")
    void twoSchedulers() throws Exception {
        clock.setInstant(Instant.parse("2026-10-12T00:30:00Z"));
        List<UUID> ids = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            ids.add(briefing("B" + i, "0 * * * *", "UTC"));
        }
        clock.setInstant(Instant.parse("2026-10-12T01:00:30Z"));
        BriefingScheduler other = new BriefingScheduler(briefingRepository, briefings, properties, jdbc,
                transactionManager, clock);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Callable<List<Future<BriefingRunEntity>>> first = () -> {
                start.await();
                return scheduler.tick();
            };
            Callable<List<Future<BriefingRunEntity>>> second = () -> {
                start.await();
                return other.tick();
            };
            Future<List<Future<BriefingRunEntity>>> a = pool.submit(first);
            Future<List<Future<BriefingRunEntity>>> b = pool.submit(second);
            start.countDown();
            List<BriefingRunEntity> all = new ArrayList<>(await(a.get(60, TimeUnit.SECONDS)));
            all.addAll(await(b.get(60, TimeUnit.SECONDS)));
            assertThat(all).hasSize(6);
            assertThat(all).extracting(BriefingRunEntity::getBriefingId).containsExactlyInAnyOrderElementsOf(ids);
        } finally {
            pool.shutdownNow();
            other.stop();
        }
        for (UUID id : ids) {
            assertThat(runs(id)).as("briefing " + id).isEqualTo(1);
        }
        assertThat(scheduler.tick()).isEmpty();
    }

    @Test
    @DisplayName("a disabled briefing has no next run and is never claimed")
    void disabled() {
        clock.setInstant(Instant.parse("2026-10-12T00:30:00Z"));
        UUID id = id(briefings.create(json("{\"name\":\"Off\",\"schedule\":\"0 * * * *\",\"enabled\":false}"),
                agentCaller()));
        assertThat(nextRun(id)).isNull();
        clock.advance(Duration.ofHours(3));
        assertThat(scheduler.tick()).isEmpty();
        assertThat(runs(id)).isZero();
    }
}
