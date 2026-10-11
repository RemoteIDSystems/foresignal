package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsChangedEvent;
import ai.foresignal.source.SourceView;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.test.MutableClock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Budget behaviour, driven by a clock the test controls. */
class BudgetGovernorTest {

    private static final String SOURCE = "test-source";
    private static final Instant START = Instant.parse("2026-03-01T12:00:00Z");

    private MutableClock clock;
    private SourceService sources;
    private UsageMeter usage;
    private BudgetGovernor governor;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(START);
        sources = mock(SourceService.class);
        usage = mock(UsageMeter.class);
        when(usage.requestsToday(SOURCE)).thenReturn(0L);
        when(usage.bytesToday(SOURCE)).thenReturn(0L);
        when(sources.effectiveBudget(SOURCE)).thenReturn(Budget.of(3, 10));
        governor = new BudgetGovernor(sources, usage, clock);
    }

    @Nested
    @DisplayName("per-minute limit")
    class PerMinute {

        @Test
        @DisplayName("the capacity is spendable, and the next request is refused")
        void refusesPastCapacity() {
            assertThat(governor.tryConsume(SOURCE)).isTrue();
            assertThat(governor.tryConsume(SOURCE)).isTrue();
            assertThat(governor.tryConsume(SOURCE)).isTrue();

            assertThat(governor.tryConsume(SOURCE)).isFalse();
        }

        @Test
        @DisplayName("the minute refills, so the next minute is spendable again")
        void refillsAfterAMinute() {
            for (int i = 0; i < 3; i++) {
                governor.tryConsume(SOURCE);
            }
            assertThat(governor.tryConsume(SOURCE)).isFalse();

            clock.advance(Duration.ofSeconds(61));

            assertThat(governor.tryConsume(SOURCE)).isTrue();
        }

        @Test
        @DisplayName("refill is greedy, so part of the minute returns part of the capacity")
        void refillIsGreedy() {
            for (int i = 0; i < 3; i++) {
                governor.tryConsume(SOURCE);
            }

            // A third of a minute at 3/minute returns about one token.
            clock.advance(Duration.ofSeconds(21));

            assertThat(governor.tryConsume(SOURCE)).isTrue();
        }
    }

    @Nested
    @DisplayName("per-day limit")
    class PerDay {

        @Test
        @DisplayName("the daily allowance is exhausted even when minutes keep refilling")
        void exhaustsTheDay() {
            int granted = 0;
            for (int i = 0; i < 30; i++) {
                if (governor.tryConsume(SOURCE)) {
                    granted++;
                }
                clock.advance(Duration.ofSeconds(61));
            }

            assertThat(granted).isEqualTo(10);
            assertThat(governor.tryConsume(SOURCE)).isFalse();
        }

        @Test
        @DisplayName("the allowance resets at UTC midnight, not 24 hours after first use")
        void resetsAtUtcMidnight() {
            for (int i = 0; i < 10; i++) {
                governor.tryConsume(SOURCE);
                clock.advance(Duration.ofSeconds(61));
            }
            assertThat(governor.tryConsume(SOURCE)).isFalse();

            clock.setInstant(nextUtcMidnight().plusSeconds(1));

            assertThat(governor.tryConsume(SOURCE)).isTrue();
        }

        @Test
        @DisplayName("the allowance does not reset just before midnight")
        void doesNotResetBeforeMidnight() {
            for (int i = 0; i < 10; i++) {
                governor.tryConsume(SOURCE);
                clock.advance(Duration.ofSeconds(61));
            }

            clock.setInstant(nextUtcMidnight().minusSeconds(5));

            assertThat(governor.tryConsume(SOURCE)).isFalse();
        }
    }

    @Nested
    @DisplayName("seeding")
    class Seeding {

        @Test
        @DisplayName("a restart cannot reset the daily allowance")
        void seedsFromTodaysUsage() {
            when(usage.requestsToday(SOURCE)).thenReturn(8L);
            var restarted = new BudgetGovernor(sources, usage, clock);

            int granted = 0;
            for (int i = 0; i < 10; i++) {
                if (restarted.tryConsume(SOURCE)) {
                    granted++;
                }
                clock.advance(Duration.ofSeconds(61));
            }

            assertThat(granted).as("8 of 10 were already spent today").isEqualTo(2);
        }

        @Test
        @DisplayName("usage beyond the allowance leaves nothing, rather than going negative")
        void seedingClampsAtZero() {
            when(usage.requestsToday(SOURCE)).thenReturn(50L);
            var restarted = new BudgetGovernor(sources, usage, clock);

            assertThat(restarted.tryConsume(SOURCE)).isFalse();
        }
    }

    @Nested
    @DisplayName("status")
    class Status {

        @Test
        void reportsTheConfiguredLimitsAndUsage() {
            when(usage.requestsToday(SOURCE)).thenReturn(4L);
            governor.tryConsume(SOURCE);

            BudgetStatus status = governor.status(SOURCE);

            assertThat(status.sourceId()).isEqualTo(SOURCE);
            assertThat(status.perMinute()).isEqualTo(3);
            assertThat(status.perDay()).isEqualTo(10);
            assertThat(status.usedToday()).isEqualTo(4L);
            assertThat(status.minuteRemaining()).isBetween(0L, 3L);
            assertThat(status.remainingToday()).isBetween(0L, 10L);
            assertThat(status.mbPerDay()).isNull();
            assertThat(status.megabytesUsedToday()).isNull();
        }

        @Test
        @DisplayName("remaining never goes negative")
        void remainingIsClamped() {
            for (int i = 0; i < 20; i++) {
                governor.tryConsume(SOURCE);
                clock.advance(Duration.ofSeconds(61));
            }

            BudgetStatus status = governor.status(SOURCE);

            assertThat(status.remainingToday()).isNotNegative();
            assertThat(status.minuteRemaining()).isNotNegative();
        }
    }

    @Nested
    @DisplayName("rebuilding")
    class Rebuilding {

        @Test
        @DisplayName("a budget change takes effect but keeps today's usage")
        void rebuildPreservesUsage() {
            governor.tryConsume(SOURCE);
            governor.tryConsume(SOURCE);
            when(usage.requestsToday(SOURCE)).thenReturn(2L);

            governor.onSettingsChanged(settingsChanged(Budget.of(3, 10), Budget.of(10, 20)));

            int granted = 0;
            clock.advance(Duration.ofSeconds(61));
            for (int i = 0; i < 30; i++) {
                if (governor.tryConsume(SOURCE)) {
                    granted++;
                }
                clock.advance(Duration.ofSeconds(61));
            }

            assertThat(granted).as("20 per day minus the 2 already spent").isEqualTo(18);
        }

        @Test
        @DisplayName("an unchanged budget is not rebuilt")
        void unchangedBudgetIsIgnored() {
            governor.tryConsume(SOURCE);
            governor.tryConsume(SOURCE);

            governor.onSettingsChanged(settingsChanged(Budget.of(3, 10), Budget.of(3, 10)));

            // The third token of this minute is still the only one left.
            assertThat(governor.tryConsume(SOURCE)).isTrue();
            assertThat(governor.tryConsume(SOURCE)).isFalse();
        }

        @Test
        @DisplayName("a change for a source with no bucket yet is ignored")
        void unknownSourceIsIgnored() {
            governor.onSettingsChanged(settingsChanged(Budget.of(3, 10), Budget.of(99, 99)));

            assertThat(governor.bucketCount()).isZero();
        }

        @Test
        void invalidateForcesARebuild() {
            governor.tryConsume(SOURCE);
            assertThat(governor.bucketCount()).isEqualTo(1);

            governor.invalidate(SOURCE);

            assertThat(governor.bucketCount()).isZero();
        }
    }

    @Nested
    @DisplayName("byte budget")
    class ByteBudget {

        @BeforeEach
        void useAByteBudget() {
            when(sources.effectiveBudget(SOURCE)).thenReturn(Budget.of(100, 100, 1));
            governor = new BudgetGovernor(sources, usage, clock);
        }

        @Test
        @DisplayName("a known size is charged before the transfer, so a large object cannot slip past")
        void chargesInAdvance() {
            assertThat(governor.tryConsume(SOURCE, 600_000)).isTrue();

            // A second 600 KB would exceed the 1 MB day.
            assertThat(governor.tryConsume(SOURCE, 600_000)).isFalse();
        }

        @Test
        @DisplayName("the advance charge is corrected once the real size is known")
        void correctsAfterwards() {
            governor.tryConsume(SOURCE, 600_000);

            governor.correctByteCharge(SOURCE, 600_000, 100_000);

            assertThat(governor.tryConsume(SOURCE, 600_000)).isTrue();
        }

        @Test
        @DisplayName("status reports the byte budget and what has been used")
        void statusIncludesBytes() {
            when(usage.bytesToday(SOURCE)).thenReturn(512L * 1024);

            BudgetStatus status = governor.status(SOURCE);

            assertThat(status.mbPerDay()).isEqualTo(1);
            assertThat(status.megabytesUsedToday()).isCloseTo(0.5, org.assertj.core.data.Offset.offset(0.01));
            assertThat(status.isByteBudgetExhausted()).isFalse();
        }

        @Test
        @DisplayName("correcting a source with no bucket is harmless")
        void correctingUnknownSourceIsSafe() {
            governor.correctByteCharge("never-seen", 100, 50);

            assertThat(governor.bucketCount()).isZero();
        }

        @Test
        @DisplayName("without a byte budget, size is never a reason to refuse")
        void noByteBudgetMeansNoByteLimit() {
            when(sources.effectiveBudget(SOURCE)).thenReturn(Budget.of(100, 100));
            var unlimited = new BudgetGovernor(sources, usage, clock);

            assertThat(unlimited.tryConsume(SOURCE, Long.MAX_VALUE / 2)).isTrue();
        }
    }

    @Test
    @DisplayName("concurrent callers never get more than the capacity")
    void concurrentConsumeNeverExceedsCapacity() throws Exception {
        when(sources.effectiveBudget(SOURCE)).thenReturn(Budget.of(50, 10_000));
        var concurrent = new BudgetGovernor(sources, usage, clock);

        int threads = 32;
        var start = new CountDownLatch(1);
        var granted = new AtomicInteger();
        try (ExecutorService pool = Executors.newFixedThreadPool(threads)) {
            for (int i = 0; i < threads; i++) {
                pool.submit(
                        () -> {
                            start.await();
                            for (int j = 0; j < 10; j++) {
                                if (concurrent.tryConsume(SOURCE)) {
                                    granted.incrementAndGet();
                                }
                            }
                            return null;
                        });
            }
            start.countDown();
            pool.shutdown();
            assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).isTrue();
        }

        // The clock never moved, so no refill could occur: exactly the capacity is spendable.
        assertThat(granted.get()).isEqualTo(50);
    }

    @Test
    @DisplayName("the daily reset instant is the next UTC midnight")
    void computesNextUtcMidnight() {
        assertThat(governor.nextUtcMidnight()).isEqualTo(Instant.parse("2026-03-02T00:00:00Z"));

        clock.setInstant(Instant.parse("2026-03-02T23:59:59Z"));

        assertThat(governor.nextUtcMidnight()).isEqualTo(Instant.parse("2026-03-03T00:00:00Z"));
    }

    private Instant nextUtcMidnight() {
        return START.atZone(ZoneOffset.UTC)
                .toLocalDate()
                .plusDays(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();
    }

    private static SourceSettingsChangedEvent settingsChanged(Budget before, Budget after) {
        return new SourceSettingsChangedEvent(SOURCE, view(before), view(after));
    }

    private static SourceView view(Budget budget) {
        SourceDescriptor descriptor =
                SourceDescriptor.builder(SOURCE)
                        .kind("thing")
                        .interval(Duration.ofSeconds(10), Duration.ofSeconds(60))
                        .defaultBudget(budget)
                        .build();
        return new SourceView(
                descriptor,
                true,
                Duration.ofSeconds(60),
                Map.of(),
                budget,
                null,
                IngestPolicy.all(),
                false,
                Instant.EPOCH);
    }
}
