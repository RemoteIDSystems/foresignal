package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.settings.AppSettingChangedEvent;
import ai.foresignal.settings.CollectorSettings;
import ai.foresignal.settings.CollectorSettingsService;
import ai.foresignal.settings.SettingKeys;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** {@link PullGovernor}: the global bound on concurrent upstream pulls (prompt 150). */
class PullGovernorTest {

    /** Runs {@code pulls} tasks that each hold a permit for {@code hold}, and returns the most that ran at once. */
    private static int peakConcurrency(PullGovernor governor, int pulls, Duration hold) throws Exception {
        AtomicInteger running = new AtomicInteger();
        AtomicInteger peak = new AtomicInteger();
        try (ExecutorService pool = Executors.newFixedThreadPool(pulls)) {
            CountDownLatch start = new CountDownLatch(1);
            List<Future<?>> done = new ArrayList<>();
            for (int i = 0; i < pulls; i++) {
                done.add(pool.submit(() -> {
                    start.await();
                    try (PullGovernor.Permit permit = governor.acquire()) {
                        peak.accumulateAndGet(running.incrementAndGet(), Math::max);
                        Thread.sleep(hold.toMillis());
                        running.decrementAndGet();
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> f : done) {
                f.get(30, TimeUnit.SECONDS);
            }
        }
        return peak.get();
    }

    @Test
    @DisplayName("concurrent pulls never exceed the cap, and every pull runs")
    void boundsConcurrentPullsToTheCap() throws Exception {
        PullGovernor governor = new PullGovernor(3);

        int peak = peakConcurrency(governor, 20, Duration.ofMillis(30));

        assertThat(peak).isEqualTo(3);
        assertThat(governor.inUse()).isZero();
    }

    @Test
    @DisplayName("a cold start with every source due at once fills the pool to the cap and queues the rest")
    void coldStartFillNeverExceedsTheCap() throws Exception {
        int due = 50;
        int cap = CollectorSettings.DEFAULT_PULLS;
        PullGovernor governor = new PullGovernor(cap);
        try (ExecutorService pool = Executors.newFixedThreadPool(due)) {
            CompletionService<PullGovernor.Permit> admitted = new ExecutorCompletionService<>(pool);
            for (int i = 0; i < due; i++) {
                admitted.submit(governor::acquire);
            }
            await().atMost(Duration.ofSeconds(5)).until(() -> governor.waiting() == due - cap);
            assertThat(governor.inUse()).isEqualTo(cap);

            // Release each pull as it is admitted: every waiter gets through, never more than the cap at once.
            for (int i = 0; i < due; i++) {
                PullGovernor.Permit permit = admitted.take().get();
                assertThat(governor.inUse()).isLessThanOrEqualTo(cap);
                permit.close();
            }
        }
        assertThat(governor.inUse()).isZero();
        assertThat(governor.waiting()).isZero();
    }

    @Test
    @DisplayName("growing the cap admits waiting pulls at once")
    void growAdmitsWaiters() throws Exception {
        PullGovernor governor = new PullGovernor(1);
        PullGovernor.Permit held = governor.acquire();
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            Future<PullGovernor.Permit> a = pool.submit(governor::acquire);
            Future<PullGovernor.Permit> b = pool.submit(governor::acquire);
            await().atMost(Duration.ofSeconds(5)).until(() -> governor.waiting() == 2);

            governor.resize(3);

            a.get(5, TimeUnit.SECONDS).close();
            b.get(5, TimeUnit.SECONDS).close();
        }
        held.close();
        assertThat(governor.cap()).isEqualTo(3);
        assertThat(governor.inUse()).isZero();
    }

    @Test
    @DisplayName("shrinking the cap interrupts no running pull and admits no new one until enough finish")
    void shrinkWaitsForRunningPulls() throws Exception {
        PullGovernor governor = new PullGovernor(3);
        PullGovernor.Permit one = governor.acquire();
        PullGovernor.Permit two = governor.acquire();
        PullGovernor.Permit three = governor.acquire();

        governor.resize(1);

        assertThat(governor.cap()).isEqualTo(1);
        assertThat(governor.inUse()).isEqualTo(3);
        try (ExecutorService pool = Executors.newSingleThreadExecutor()) {
            Future<PullGovernor.Permit> next = pool.submit(governor::acquire);
            one.close();
            two.close();
            // Two finished, but the one still running fills the new cap of one.
            Thread.sleep(200);
            assertThat(next.isDone()).isFalse();
            three.close();
            next.get(5, TimeUnit.SECONDS).close();
        }
        assertThat(governor.inUse()).isZero();
        assertThat(peakConcurrency(governor, 8, Duration.ofMillis(20))).isEqualTo(1);
    }

    @Test
    @DisplayName("a collector setting change resizes the pool live; another key changes nothing")
    void settingChangeResizesLive() throws Exception {
        CollectorSettingsService settings = Mockito.mock(CollectorSettingsService.class);
        Mockito.when(settings.get()).thenReturn(new CollectorSettings(2));
        PullGovernor governor = new PullGovernor(settings);
        assertThat(governor.cap()).isEqualTo(2);

        Mockito.when(settings.get()).thenReturn(new CollectorSettings(5));
        governor.onSettingChanged(new AppSettingChangedEvent(SettingKeys.OUTBOUND));
        assertThat(governor.cap()).isEqualTo(2);

        governor.onSettingChanged(new AppSettingChangedEvent(SettingKeys.COLLECTOR));
        assertThat(governor.cap()).isEqualTo(5);
        assertThat(peakConcurrency(governor, 20, Duration.ofMillis(30))).isEqualTo(5);

        Mockito.when(settings.get()).thenReturn(new CollectorSettings(1));
        governor.onSettingChanged(new AppSettingChangedEvent(SettingKeys.COLLECTOR));
        assertThat(governor.cap()).isEqualTo(1);
        assertThat(peakConcurrency(governor, 6, Duration.ofMillis(20))).isEqualTo(1);
    }

    @Test
    @DisplayName("the cap is clamped to 1..16, and closing a permit twice releases it once")
    void clampAndDoubleClose() throws Exception {
        assertThat(new PullGovernor(0).cap()).isEqualTo(1);
        assertThat(new PullGovernor(99).cap()).isEqualTo(16);
        PullGovernor governor = new PullGovernor(2);
        governor.resize(-4);
        assertThat(governor.cap()).isEqualTo(1);

        PullGovernor.Permit permit = governor.acquire();
        permit.close();
        permit.close();
        assertThat(governor.inUse()).isZero();
        assertThat(peakConcurrency(governor, 4, Duration.ofMillis(10))).isEqualTo(1);
    }
}
