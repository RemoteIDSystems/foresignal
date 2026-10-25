package ai.foresignal.llm;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.llm.client.TokenUsage;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link LlmPricing}: what a call costs at a provider's prices. */
class LlmPricingTest {

    static final LlmPricing PRICES = new LlmPricing(new BigDecimal("3"), new BigDecimal("15"), new BigDecimal("0.3"),
            new BigDecimal("0.02"));

    @Test
    @DisplayName("Anthropic: input, cache reads and output each at their price (cache reads are apart from input)")
    void anthropic() {
        // 1,000,000 input × $3 + 200,000 cached × $0.30 + 100,000 output × $15 = 3 + 0.06 + 1.5
        BigDecimal cost = PRICES.chat(new TokenUsage(1_000_000, 100_000, 200_000), LlmProviderType.ANTHROPIC);

        assertThat(cost).isEqualByComparingTo("4.56");
    }

    @Test
    @DisplayName("OpenAI-compatible: cached tokens are inside prompt_tokens, so they are taken out of the input")
    void openAiCompatible() {
        // (1,000,000 − 200,000) × $3 + 200,000 × $0.30 + 100,000 × $15 = 2.4 + 0.06 + 1.5
        BigDecimal cost = PRICES.chat(new TokenUsage(1_000_000, 100_000, 200_000), LlmProviderType.OPENAI_COMPATIBLE);

        assertThat(cost).isEqualByComparingTo("3.96");
    }

    @Test
    @DisplayName("without a cached price, cached input costs the input price; an unset price costs nothing")
    void defaults() {
        LlmPricing inputOnly = new LlmPricing(new BigDecimal("2"), null, null, null);

        assertThat(inputOnly.chat(new TokenUsage(500_000, 1_000_000, 500_000), LlmProviderType.ANTHROPIC))
                .isEqualByComparingTo("2");
        assertThat(LlmPricing.NONE.chat(new TokenUsage(9, 9, 9), LlmProviderType.OLLAMA)).isEqualByComparingTo("0");
        assertThat(LlmPricing.NONE.isSet()).isFalse();
        assertThat(inputOnly.isSet()).isTrue();
    }

    @Test
    @DisplayName("embeddings are priced at the embedding price; the estimate prices the input and the output cap")
    void embeddingAndEstimate() {
        assertThat(PRICES.embedding(new TokenUsage(2_000_000, 0, 0))).isEqualByComparingTo("0.04");
        assertThat(PRICES.chatEstimate(1000, 16_000)).isEqualByComparingTo("0.243");
        assertThat(PRICES.chat(null, LlmProviderType.OLLAMA)).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("small calls keep their fractions of a cent")
    void precision() {
        LlmPricing cheap = new LlmPricing(new BigDecimal("0.075"), new BigDecimal("0.3"), null, null);

        assertThat(cheap.chat(new TokenUsage(12, 1, 0), LlmProviderType.OPENAI_COMPATIBLE))
                .isEqualByComparingTo("0.0000012");
    }
}
