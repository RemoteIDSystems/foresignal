package ai.foresignal.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.test.MutableClock;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Per-user USD caps (prompt R2-011): a shared provider's per-user caps stop one user at their share while the others
 * continue; a personal provider's own caps are its owner's; the machine is budgeted as before.
 */
class LlmPerUserBudgetTest {

    static final String ALICE = "01ALICEPRINCIPAL0000000000";
    static final String BOB = "01BOBPRINCIPAL000000000000";

    final MutableClock clock = new MutableClock(Instant.parse("2026-10-12T12:00:00Z"));
    final LlmUsageMeter usage = mock(LlmUsageMeter.class);
    LlmBudgetGovernor governor;

    @BeforeEach
    void setUp() {
        when(usage.tokensToday(anyString())).thenReturn(0L);
        when(usage.costSince(anyString(), any())).thenReturn(BigDecimal.ZERO);
        when(usage.userCostSince(anyString(), anyString(), any())).thenReturn(BigDecimal.ZERO);
        governor = new LlmBudgetGovernor(usage, event -> { }, clock);
    }

    static LlmProviderConfig provider(String owner, LlmSpendCaps caps, LlmSpendCaps perUser) {
        return new LlmProviderConfig("p", LlmProviderType.OPENAI_COMPATIBLE, "https://api.example.test/v1", "m", null,
                null, null, null, null, Map.of(), false, null, 1000, 100_000_000L, true, null,
                new LlmPricing(BigDecimal.ONE, BigDecimal.ONE, null, null), caps, owner, perUser);
    }

    static LlmSpendCaps monthly(String usd) {
        return new LlmSpendCaps(null, null, new BigDecimal(usd));
    }

    private void spend(LlmProviderConfig p, String principal, String usd) {
        LlmBudgetGovernor.Reservation r = governor.reserve(p, 10, new BigDecimal("0.01"), principal);
        governor.settle(r, 10, new BigDecimal(usd));
    }

    @Test
    @DisplayName("a per-user cap on a shared provider stops that user at the cap while others continue")
    void perUserCapOnShared() {
        LlmProviderConfig shared = provider(null, LlmSpendCaps.NONE, monthly("1.00"));
        spend(shared, ALICE, "0.60");
        spend(shared, ALICE, "0.60");
        assertThatThrownBy(() -> governor.reserve(shared, 10, new BigDecimal("0.01"), ALICE))
                .isInstanceOfSatisfying(LlmBudgetExceededException.class, e -> {
                    assertThat(e.getStatus().value()).isEqualTo(429);
                    assertThat(e.getMessage()).contains("your monthly cap");
                    assertThat(e.retryAfter()).isPositive();
                });
        verify(usage).recordRefused("p", ALICE);
        spend(shared, BOB, "0.60");
        assertThat(governor.userSpend("p", BOB).get(LlmSpendWindow.MONTH).spentUsd()).isEqualByComparingTo("0.60");
        assertThat(governor.spend("p").get(LlmSpendWindow.MONTH).spentUsd()).as("the provider total counts everyone")
                .isEqualByComparingTo("1.80");
    }

    @Test
    @DisplayName("a refused per-user call holds nothing against the provider's own windows")
    void refusalReleases() {
        LlmProviderConfig shared = provider(null, LlmSpendCaps.NONE, monthly("0.50"));
        spend(shared, ALICE, "0.50");
        assertThatThrownBy(() -> governor.reserve(shared, 10, new BigDecimal("0.20"), ALICE))
                .isInstanceOf(LlmBudgetExceededException.class);
        assertThat(governor.spend("p").get(LlmSpendWindow.MONTH).reservedUsd()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("the per-user window is seeded from that user's metered spend, so a restart cannot reset it")
    void seeded() {
        when(usage.userCostSince(eq("p"), eq(ALICE), any())).thenReturn(new BigDecimal("2"));
        LlmProviderConfig shared = provider(null, LlmSpendCaps.NONE, monthly("1.00"));
        assertThatThrownBy(() -> governor.reserve(shared, 10, new BigDecimal("0.01"), ALICE))
                .isInstanceOf(LlmBudgetExceededException.class);
        governor.reserve(shared, 10, new BigDecimal("0.01"), BOB);
    }

    @Test
    @DisplayName("a personal provider's own caps are its owner's; per-user caps play no part")
    void personalCaps() {
        LlmProviderConfig personal = provider(ALICE, monthly("1.00"), LlmSpendCaps.NONE);
        spend(personal, ALICE, "1.00");
        assertThatThrownBy(() -> governor.reserve(personal, 10, new BigDecimal("0.01"), ALICE))
                .isInstanceOf(LlmBudgetExceededException.class);
        verify(usage, never()).userCostSince(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("machine calls keep the instance-level behavior: per-user caps do not apply to them")
    void machine() {
        LlmProviderConfig shared = provider(null, LlmSpendCaps.NONE, monthly("0.10"));
        spend(shared, null, "5.00");
        governor.reserve(shared, 10, new BigDecimal("0.01"), null);
        verify(usage, never()).recordRefused(anyString(), anyString());
        assertThat(provider(null, LlmSpendCaps.NONE, LlmSpendCaps.NONE).shared()).isTrue();
    }

    @Test
    @DisplayName("a personal provider is never local for egress, whatever its type or options")
    void personalNeverLocal() {
        LlmProviderConfig ollama = new LlmProviderConfig("o", LlmProviderType.OLLAMA, "http://ollama:11434", "m", null,
                null, null, null, null, Map.of(), true, null, 10, 1000, true, null, LlmPricing.NONE, LlmSpendCaps.NONE,
                ALICE, LlmSpendCaps.NONE);
        assertThat(ollama.local()).isFalse();
        assertThat(ollama.destination().local()).isFalse();
    }
}
