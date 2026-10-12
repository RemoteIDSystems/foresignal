package ai.foresignal.llm.client;

import java.util.Locale;

/**
 * Why a model stopped, normalized across providers (OpenAPI {@code LlmTestResult.chat.stopReason}).
 */
public enum StopReason {
    /** The model finished its turn. */
    END,
    /** The model asked for one or more tools to be run. */
    TOOL_USE,
    /** The output hit {@code maxTokens}; the content is truncated. */
    MAX_TOKENS,
    /** The model or a safety classifier declined; see {@link ChatResponse#refusalCategory()}. */
    REFUSAL,
    /** The provider reported an error or a stop reason Foresignal does not understand. */
    ERROR;

    /** The wire value, for example {@code tool_use}. */
    public String wireValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
