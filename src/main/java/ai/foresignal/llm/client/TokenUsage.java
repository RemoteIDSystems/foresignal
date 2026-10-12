package ai.foresignal.llm.client;

/**
 * Tokens one call (or a sum of calls) used.
 *
 * @param inputTokens prompt tokens billed at the input rate (for Anthropic, excluding cache reads)
 * @param outputTokens generated tokens, including thinking
 * @param cachedTokens prompt tokens served from the provider's prompt cache (Anthropic
 *     {@code cache_read_input_tokens}, OpenAI {@code prompt_tokens_details.cached_tokens})
 */
public record TokenUsage(long inputTokens, long outputTokens, long cachedTokens) {

    /** No tokens. */
    public static final TokenUsage ZERO = new TokenUsage(0, 0, 0);

    /** Input plus output: what a daily token budget counts. */
    public long total() {
        return inputTokens + outputTokens;
    }

    /** The element-wise sum. */
    public TokenUsage plus(TokenUsage other) {
        if (other == null) {
            return this;
        }
        return new TokenUsage(inputTokens + other.inputTokens, outputTokens + other.outputTokens,
                cachedTokens + other.cachedTokens);
    }
}
