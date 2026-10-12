package ai.foresignal.llm.client;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import java.time.Duration;
import org.springframework.http.HttpStatus;

/**
 * A provider call failed. The {@link Kind} says how: callers back off on {@link Kind#RATE_LIMITED}
 * and {@link Kind#OVERLOADED}, report {@link Kind#AUTHENTICATION} to the operator, and switch Ask to
 * retrieval-only mode on {@link Kind#TOOLS_UNSUPPORTED}.
 *
 * <p>As an API problem: {@code 429 LLM_PROVIDER_RATE_LIMITED} (with {@code Retry-After} when the
 * provider sent one) for rate limiting, {@code 502 LLM_PROVIDER_ERROR} otherwise. Messages never carry
 * API keys.
 */
public class LlmProviderException extends ForesignalException {

    private static final long serialVersionUID = 1L;

    /** How the call failed. */
    public enum Kind {
        /** The provider rate-limited the call (HTTP 429). */
        RATE_LIMITED,
        /** The provider is overloaded or failing (HTTP 5xx, 529). */
        OVERLOADED,
        /** The API key was missing, wrong or not allowed (HTTP 401/403). */
        AUTHENTICATION,
        /** The provider rejected the request (HTTP 400/404/422). */
        INVALID_REQUEST,
        /** The model does not support tools; retry without them. */
        TOOLS_UNSUPPORTED,
        /** The provider could not be reached, or timed out. */
        CONNECTION,
        /** The provider answered with something Foresignal could not read. */
        INVALID_RESPONSE
    }

    private final Kind kind;
    private final String providerId;
    private final transient Duration retryAfter;

    /**
     * Creates the exception.
     *
     * @param kind how the call failed
     * @param providerId the provider
     * @param message a message without secrets
     * @param retryAfter the provider's {@code retry-after}, or null
     * @param cause the underlying exception, or null
     */
    public LlmProviderException(Kind kind, String providerId, String message, Duration retryAfter, Throwable cause) {
        super(kind == Kind.RATE_LIMITED ? HttpStatus.TOO_MANY_REQUESTS : HttpStatus.BAD_GATEWAY,
                kind == Kind.RATE_LIMITED ? ErrorCodes.LLM_PROVIDER_RATE_LIMITED : ErrorCodes.LLM_PROVIDER_ERROR,
                "LLM provider '" + providerId + "': " + message, cause);
        this.kind = kind;
        this.providerId = providerId;
        this.retryAfter = retryAfter;
        if (retryAfter != null) {
            withHeader("Retry-After", Long.toString(Math.max(1, retryAfter.toSeconds())));
        }
    }

    /** Creates the exception without a retry hint or cause. */
    public LlmProviderException(Kind kind, String providerId, String message) {
        this(kind, providerId, message, null, null);
    }

    /** How the call failed. */
    public Kind kind() {
        return kind;
    }

    /** The provider. */
    public String providerId() {
        return providerId;
    }

    /** The provider's {@code retry-after}, or null. */
    public Duration retryAfter() {
        return retryAfter;
    }

    /** Classifies an HTTP status from a provider. */
    public static Kind kindForStatus(int status) {
        if (status == 429) {
            return Kind.RATE_LIMITED;
        }
        if (status == 401 || status == 403) {
            return Kind.AUTHENTICATION;
        }
        if (status >= 500) {
            return Kind.OVERLOADED;
        }
        return Kind.INVALID_REQUEST;
    }

    /** Parses a {@code retry-after} header in seconds; null when absent or not a number. */
    public static Duration parseRetryAfter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            double seconds = Double.parseDouble(value.trim());
            return seconds < 0 ? null : Duration.ofMillis((long) Math.ceil(seconds * 1000));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
