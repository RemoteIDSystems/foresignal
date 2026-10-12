package ai.foresignal.llm;

import java.time.Instant;

/**
 * Usage in one bucket (OpenAPI {@code LlmUsagePoint}).
 *
 * @param t the bucket start
 * @param requests calls sent
 * @param inputTokens input tokens
 * @param outputTokens output tokens
 * @param cachedTokens cached input tokens
 * @param errors failed calls
 * @param refused calls the budget refused
 */
public record LlmUsagePoint(Instant t, long requests, long inputTokens, long outputTokens, long cachedTokens,
        long errors, long refused) {

    /** The element-wise sum, keeping this bucket's time. */
    public LlmUsagePoint plus(LlmUsagePoint other) {
        return new LlmUsagePoint(t, requests + other.requests, inputTokens + other.inputTokens,
                outputTokens + other.outputTokens, cachedTokens + other.cachedTokens, errors + other.errors,
                refused + other.refused);
    }

    /** This point at another time. */
    public LlmUsagePoint at(Instant time) {
        return new LlmUsagePoint(time, requests, inputTokens, outputTokens, cachedTokens, errors, refused);
    }
}
