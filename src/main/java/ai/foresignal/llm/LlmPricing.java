package ai.foresignal.llm;

import ai.foresignal.llm.client.TokenUsage;
import java.math.BigDecimal;
import java.math.MathContext;

/**
 * A provider's prices in USD per million tokens (OpenAPI {@code LlmPricing}); any of them may be null,
 * which prices those tokens at zero.
 *
 * <p>{@link #chat} prices a chat call's metered tokens: input, output, and cached input at
 * {@code cachedInputUsdPerMillionTokens} (the input price when that is not set). Anthropic reports
 * cache reads apart from {@code inputTokens}; the OpenAI-compatible API counts them inside
 * {@code prompt_tokens}, so for every other type the cached tokens are taken out of the input before
 * it is priced. {@link #embedding} prices an embedding call's input at
 * {@code embeddingUsdPerMillionTokens}.
 *
 * @param inputUsdPerMillionTokens the input price, or null
 * @param outputUsdPerMillionTokens the output price, or null
 * @param cachedInputUsdPerMillionTokens the cached-input price, or null to use the input price
 * @param embeddingUsdPerMillionTokens the embedding price, or null (never set for anthropic)
 */
public record LlmPricing(BigDecimal inputUsdPerMillionTokens, BigDecimal outputUsdPerMillionTokens,
        BigDecimal cachedInputUsdPerMillionTokens, BigDecimal embeddingUsdPerMillionTokens) {

    /** No prices: every call costs nothing. */
    public static final LlmPricing NONE = new LlmPricing(null, null, null, null);

    private static final BigDecimal MILLION = BigDecimal.valueOf(1_000_000);

    /** True when any price is set. */
    public boolean isSet() {
        return inputUsdPerMillionTokens != null || outputUsdPerMillionTokens != null
                || cachedInputUsdPerMillionTokens != null || embeddingUsdPerMillionTokens != null;
    }

    /** The cost in USD of a chat call of provider {@code type} that used {@code usage}. */
    public BigDecimal chat(TokenUsage usage, LlmProviderType type) {
        if (usage == null) {
            return BigDecimal.ZERO;
        }
        long cached = Math.max(0, usage.cachedTokens());
        long input = Math.max(0, usage.inputTokens());
        long fresh = type == LlmProviderType.ANTHROPIC ? input : Math.max(0, input - cached);
        BigDecimal cachedPrice = cachedInputUsdPerMillionTokens != null ? cachedInputUsdPerMillionTokens
                : inputUsdPerMillionTokens;
        return price(fresh, inputUsdPerMillionTokens).add(price(cached, cachedPrice))
                .add(price(Math.max(0, usage.outputTokens()), outputUsdPerMillionTokens));
    }

    /** The estimated cost in USD of a chat call before it is sent: its estimated input and its output cap. */
    public BigDecimal chatEstimate(long inputTokens, long maxOutputTokens) {
        return price(Math.max(0, inputTokens), inputUsdPerMillionTokens)
                .add(price(Math.max(0, maxOutputTokens), outputUsdPerMillionTokens));
    }

    /** The cost in USD of an embedding call that used {@code usage}. */
    public BigDecimal embedding(TokenUsage usage) {
        return usage == null ? BigDecimal.ZERO : embedding(usage.inputTokens());
    }

    /** The cost in USD of embedding {@code inputTokens} tokens. */
    public BigDecimal embedding(long inputTokens) {
        return price(Math.max(0, inputTokens), embeddingUsdPerMillionTokens);
    }

    private static BigDecimal price(long tokens, BigDecimal perMillion) {
        if (perMillion == null || tokens == 0) {
            return BigDecimal.ZERO;
        }
        return perMillion.multiply(BigDecimal.valueOf(tokens)).divide(MILLION, MathContext.DECIMAL128);
    }
}
