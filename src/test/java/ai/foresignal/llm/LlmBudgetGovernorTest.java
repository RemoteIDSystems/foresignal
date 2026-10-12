package ai.foresignal.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.notify.SystemEvent;
import ai.foresignal.test.MutableClock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

/** {@link LlmBudgetGovernor}: per-minute requests, daily tokens, estimates, seeding and UTC rollover. */
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
}
