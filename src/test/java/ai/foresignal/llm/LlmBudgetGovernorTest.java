package ai.foresignal.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.notify.SystemEvent;
import ai.foresignal.test.MutableClock;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

/**
 * {@link LlmBudgetGovernor}: per-minute requests, daily tokens, estimates, seeding and UTC rollover, and the
 * USD caps per hour, day and month.
 */
class LlmBudgetGovernorTest {

    final MutableClock clock = new MutableClock(Instant.parse("2026-10-12T23:58:00Z"));
    final LlmUsageMeter usage = mock(LlmUsageMeter.class);
    final List<Object> events = new ArrayList<>();
    final ApplicationEventPublisher publisher = events::add;
    LlmBudgetGovernor governor;

    @BeforeEach
    void setUp() {
        when(usage.tokensToday(anyString())).thenReturn(0L);
        governor = new LlmBudgetGovernor(usage, publisher, clock);
    }

    static LlmProviderConfig provider(int perMinute, long perDay) {
        return new LlmProviderConfig("p", LlmProviderType.OLLAMA, "http://ollama:11434", "m", null, null, null, null,
                null, Map.of(), false, null, perMinute, perDay, true, null);
    }

    @Test
    @DisplayName("the estimate is characters / 4 rounded up plus the output cap")
    void estimate() {
        assertThat(LlmBudgetGovernor.estimate(0, 16)).isEqualTo(16);
        assertThat(LlmBudgetGovernor.estimate(29, 16)).isEqualTo(8 + 16);
        assertThat(LlmBudgetGovernor.estimate(400, 0)).isEqualTo(100);
        assertThat(LlmBudgetGovernor.estimate(4, -5)).isEqualTo(1);
    }

    @Test
    @DisplayName("the per-minute request budget refuses the next call with a Retry-After, then refills")
    void perMinute() {
        LlmProviderConfig p = provider(2, 1_000_000);
        governor.reserve(p, 10);
        governor.reserve(p, 10);

        assertThatThrownBy(() -> governor.reserve(p, 10)).isInstanceOfSatisfying(LlmBudgetExceededException.class,
                e -> {
                    assertThat(e.getCode()).isEqualTo("LLM_BUDGET_EXCEEDED");
                    assertThat(e.getStatus().value()).isEqualTo(429);
                    assertThat(e.getHeaders()).containsKey("Retry-After");
                    assertThat(e.retryAfter()).isPositive();
                });
        verify(usage).recordRefused("p");
        assertThat(events).isEmpty();

        clock.advance(Duration.ofSeconds(31));
        governor.reserve(p, 10);
    }

    @Test
    @DisplayName("an estimate larger than what is left today is refused, notified once, and not counted")
    void daily() {
        LlmProviderConfig p = provider(100, 1000);
        LlmBudgetGovernor.Reservation r = governor.reserve(p, 600);
        assertThat(governor.usedToday(p)).isEqualTo(600);

        assertThatThrownBy(() -> governor.reserve(p, 500)).isInstanceOf(LlmBudgetExceededException.class)
                .hasMessageContaining("400 of its 1000");
        assertThatThrownBy(() -> governor.reserve(p, 500)).isInstanceOf(LlmBudgetExceededException.class);
        assertThat(events).singleElement().isInstanceOfSatisfying(SystemEvent.class,
                e -> assertThat(e.subtype()).isEqualTo("system.llm_budget_exhausted"));
        verify(usage, times(2)).recordRefused("p");

        // Settling replaces the estimate with what the provider reported.
        governor.settle(r, 100);
        assertThat(governor.usedToday(p)).isEqualTo(100);
        governor.reserve(p, 500);
    }

    @Test
    @DisplayName("a daily refusal's Retry-After points at the next UTC midnight")
    void retryAfterMidnight() {
        LlmProviderConfig p = provider(100, 1000);

        assertThatThrownBy(() -> governor.reserve(p, 5000)).isInstanceOfSatisfying(LlmBudgetExceededException.class,
                e -> assertThat(e.retryAfter()).isEqualTo(Duration.ofMinutes(2)));
    }

    @Test
    @DisplayName("today's figure is seeded from llm_usage, so a restart cannot reset the allowance")
    void seeded() {
        when(usage.tokensToday("p")).thenReturn(900L);
        LlmProviderConfig p = provider(100, 1000);

        assertThat(governor.usedToday(p)).isEqualTo(900);
        assertThatThrownBy(() -> governor.reserve(p, 200)).isInstanceOf(LlmBudgetExceededException.class);
        governor.reserve(p, 100);
    }

    @Test
    @DisplayName("at UTC midnight the day starts again; yesterday's reservations do not touch it")
    void rollover() {
        LlmProviderConfig p = provider(100, 1000);
        LlmBudgetGovernor.Reservation yesterday = governor.reserve(p, 900);
        assertThatThrownBy(() -> governor.reserve(p, 200)).isInstanceOf(LlmBudgetExceededException.class);

        clock.advance(Duration.ofMinutes(3)); // 2026-10-13T00:01Z
        governor.reserve(p, 800);
        governor.settle(yesterday, 10);

        assertThat(governor.usedToday(p)).isEqualTo(800);
        // The new day may notify again.
        assertThatThrownBy(() -> governor.reserve(p, 300)).isInstanceOf(LlmBudgetExceededException.class);
        assertThat(events).hasSize(2);
    }

    @Test
    @DisplayName("a changed budget rebuilds the bucket but keeps today's tokens")
    void changedBudget() {
        governor.reserve(provider(1, 1000), 300);
        assertThatThrownBy(() -> governor.reserve(provider(1, 1000), 1)).isInstanceOf(LlmBudgetExceededException.class);

        LlmProviderConfig bigger = provider(5, 2000);
        governor.reserve(bigger, 1);
        assertThat(governor.usedToday(bigger)).isEqualTo(301);
    }

    @Test
    @DisplayName("invalidate and reset drop the state; the next use re-seeds")
    void reset() {
        LlmProviderConfig p = provider(100, 1000);
        governor.reserve(p, 500);
        governor.invalidate("p");
        assertThat(governor.usedToday(p)).isZero();
        governor.reserve(p, 500);
        governor.resetState();
        assertThat(governor.usedToday(p)).isZero();
        governor.settle(new LlmBudgetGovernor.Reservation("unknown", 1, clock.instant().atZone(java.time.ZoneOffset.UTC)
                .toLocalDate()), 5);
    }

    static LlmProviderConfig capped(String hourly, String daily, String monthly) {
        return new LlmProviderConfig("p", LlmProviderType.OPENAI_COMPATIBLE, "https://api.example.test/v1", "m", null,
                null, null, null, null, Map.of(), false, "k", 1000, 100_000_000, true, null,
                new LlmPricing(BigDecimal.ONE, BigDecimal.ONE, null, null),
                new LlmSpendCaps(hourly == null ? null : new BigDecimal(hourly),
                        daily == null ? null : new BigDecimal(daily), monthly == null ? null : new BigDecimal(monthly)));
    }

    /** Reserves and settles one call that cost {@code usd}. */
    void spend(LlmProviderConfig p, String usd) {
        LlmBudgetGovernor.Reservation r = governor.reserve(p, 10, new BigDecimal("0.01"));
        governor.settle(r, 10, new BigDecimal(usd));
    }

    @Test
    @DisplayName("hourly cap: refused once the hour's spend reaches it, Retry-After at the top of the hour, then reset")
    void hourlyCap() {
        clock.setInstant(Instant.parse("2026-10-13T10:40:00Z"));
        LlmProviderConfig p = capped("1", null, null);
        spend(p, "0.6");
        spend(p, "0.5"); // allowed: the hour had $0.60 when it was reserved

        assertThatThrownBy(() -> governor.reserve(p, 10, BigDecimal.ZERO))
                .isInstanceOfSatisfying(LlmBudgetExceededException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("LLM_BUDGET_EXCEEDED");
                    assertThat(e.retryAfter()).isEqualTo(Duration.ofMinutes(20));
                    assertThat(e.getMessage()).contains("hourly cap of $1");
                });
        assertThat(events).singleElement().isInstanceOfSatisfying(SystemEvent.class,
                e -> assertThat(e.subtype()).isEqualTo("system.llm_spend_cap_reached"));
        assertThat(governor.spend("p").get(LlmSpendWindow.HOUR).spentUsd()).isEqualByComparingTo("1.1");

        clock.setInstant(Instant.parse("2026-10-13T11:00:00Z"));
        spend(p, "0.2");
        assertThat(governor.spend("p").get(LlmSpendWindow.HOUR).spentUsd()).isEqualByComparingTo("0.2");
        assertThat(governor.spend("p").get(LlmSpendWindow.DAY).spentUsd()).isEqualByComparingTo("1.3");
    }

    @Test
    @DisplayName("daily cap: resets at 00:00 UTC, the daily token budget's boundary")
    void dailyCap() {
        clock.setInstant(Instant.parse("2026-10-13T22:30:00Z"));
        LlmProviderConfig p = capped(null, "2", null);
        spend(p, "2");

        assertThatThrownBy(() -> governor.reserve(p, 10, BigDecimal.ZERO))
                .isInstanceOfSatisfying(LlmBudgetExceededException.class,
                        e -> assertThat(e.retryAfter()).isEqualTo(Duration.ofMinutes(90)));

        clock.setInstant(Instant.parse("2026-10-14T00:00:00Z"));
        governor.reserve(p, 10, BigDecimal.ZERO);
    }

    @Test
    @DisplayName("monthly cap: resets on the 1st at 00:00 UTC, and the latest reset wins when several caps are spent")
    void monthlyCap() {
        clock.setInstant(Instant.parse("2026-10-31T22:30:00Z"));
        LlmProviderConfig p = capped("5", null, "5");
        spend(p, "5");

        assertThatThrownBy(() -> governor.reserve(p, 10, BigDecimal.ZERO))
                .isInstanceOfSatisfying(LlmBudgetExceededException.class, e -> {
                    // The hour resets at 23:00, but the month is spent too and resets at 2026-11-01T00:00Z.
                    assertThat(e.retryAfter()).isEqualTo(Duration.ofMinutes(90));
                    assertThat(e.getMessage()).contains("spent $5 of its monthly cap of $5");
                });

        clock.setInstant(Instant.parse("2026-10-31T23:59:59Z"));
        assertThatThrownBy(() -> governor.reserve(p, 10, BigDecimal.ZERO)).isInstanceOf(LlmBudgetExceededException.class);
        clock.setInstant(Instant.parse("2026-11-01T00:00:00Z"));
        governor.reserve(p, 10, BigDecimal.ZERO);
        assertThat(governor.spend("p").get(LlmSpendWindow.MONTH).start()).isEqualTo(Instant.parse("2026-11-01T00:00:00Z"));
        assertThat(governor.spend("p").get(LlmSpendWindow.MONTH).resetsAt())
                .isEqualTo(Instant.parse("2026-12-01T00:00:00Z"));
    }

    @Test
    @DisplayName("calls in flight hold their estimate against the cap until they settle")
    void inFlight() {
        clock.setInstant(Instant.parse("2026-10-13T10:00:00Z"));
        LlmProviderConfig p = capped("1", null, null);
        LlmBudgetGovernor.Reservation big = governor.reserve(p, 10, new BigDecimal("1"));

        assertThatThrownBy(() -> governor.reserve(p, 10, BigDecimal.ZERO)).isInstanceOf(LlmBudgetExceededException.class);
        assertThat(governor.spend("p").get(LlmSpendWindow.HOUR).reservedUsd()).isEqualByComparingTo("1");

        governor.settle(big, 10, new BigDecimal("0.1"));
        assertThat(governor.spend("p").get(LlmSpendWindow.HOUR).reservedUsd()).isEqualByComparingTo("0");
        governor.reserve(p, 10, BigDecimal.ZERO);
    }

    @Test
    @DisplayName("each window is seeded from llm_usage, so a restart cannot reset the spend")
    void seededSpend() {
        clock.setInstant(Instant.parse("2026-10-13T10:15:00Z"));
        when(usage.costSince(eq("p"), any())).thenReturn(BigDecimal.ZERO);
        when(usage.costSince("p", Instant.parse("2026-10-01T00:00:00Z"))).thenReturn(new BigDecimal("9.5"));
        LlmProviderConfig p = capped("1", "1", "10");
        governor.reserve(p, 10, BigDecimal.ZERO);

        spend(p, "0.5");
        assertThatThrownBy(() -> governor.reserve(p, 10, BigDecimal.ZERO))
                .isInstanceOfSatisfying(LlmBudgetExceededException.class,
                        e -> assertThat(e.getMessage()).contains("monthly cap of $10"));
    }

    @Test
    @DisplayName("a refused token budget releases the USD it held; spend is tracked without caps")
    void releaseAndUncapped() {
        clock.setInstant(Instant.parse("2026-10-13T10:00:00Z"));
        LlmProviderConfig tight = new LlmProviderConfig("p", LlmProviderType.OPENAI_COMPATIBLE, "https://x.test/v1",
                "m", null, null, null, null, null, Map.of(), false, "k", 1000, 1000, true, null,
                new LlmPricing(BigDecimal.ONE, BigDecimal.ONE, null, null), LlmSpendCaps.NONE);

        assertThatThrownBy(() -> governor.reserve(tight, 5000, new BigDecimal("3")))
                .isInstanceOf(LlmBudgetExceededException.class);
        assertThat(governor.spend("p").get(LlmSpendWindow.HOUR).reservedUsd()).isEqualByComparingTo("0");

        LlmBudgetGovernor.Reservation r = governor.reserve(tight, 10, new BigDecimal("3"));
        governor.settle(r, 10, new BigDecimal("7.25"));
        assertThat(governor.spend("p").get(LlmSpendWindow.MONTH).spentUsd()).isEqualByComparingTo("7.25");
    }
}
