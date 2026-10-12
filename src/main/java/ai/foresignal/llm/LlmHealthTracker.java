package ai.foresignal.llm;

import ai.foresignal.collect.HealthEvaluator;
import ai.foresignal.notify.SystemEvent;
import java.time.Clock;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Provider health (Architecture §19: "provider health mirrors source health"): last success, last
 * error and the failure streak, kept on the {@code llm_provider} row.
 *
 * <p>The status is derived like a source's: {@code disabled} when off, {@code idle} before any call,
 * {@code failing} at {@value ai.foresignal.collect.HealthEvaluator#FAILING_CONSECUTIVE_FAILURES} or
 * more failures in a row, {@code degraded} after one or two, {@code healthy} otherwise. The call that
 * makes a provider {@code failing} raises a {@code system} notification.
 */
@Component
public class LlmHealthTracker {

    private static final Logger log = LoggerFactory.getLogger(LlmHealthTracker.class);

    /** The longest stored error message. */
    static final int MAX_ERROR_LENGTH = 500;

    private final LlmProviderRepository providers;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    /** Creates the tracker. */
    public LlmHealthTracker(LlmProviderRepository providers, ApplicationEventPublisher events, Clock clock) {
        this.providers = providers;
        this.events = events;
        this.clock = clock;
    }

    /** Records a successful call. */
    public void success(String providerId) {
        providers.recordSuccess(providerId, clock.instant());
    }

    /** Records a failed call; notifies when this failure makes the provider {@code failing}. */
    public void failure(String providerId, String message) {
        String error = message == null ? "unknown error" : message;
        if (error.length() > MAX_ERROR_LENGTH) {
            error = error.substring(0, MAX_ERROR_LENGTH) + "…";
        }
        providers.recordFailure(providerId, clock.instant(), error);
        int failures = providers.findById(providerId).map(LlmProviderEntity::getConsecutiveFailures).orElse(0);
        if (failures == HealthEvaluator.FAILING_CONSECUTIVE_FAILURES) {
            log.warn("LLM provider {} is failing: {} consecutive failures, last: {}", providerId, failures, error);
            events.publishEvent(new SystemEvent("system.llm_provider_failing", "warning",
                    "LLM provider failing: " + providerId,
                    "LLM provider `" + providerId + "` has failed " + failures + " calls in a row. Last error: "
                            + error.replace("`", "'"),
                    "/v1/llm/providers/" + providerId,
                    Map.of("providerId", providerId, "consecutiveFailures", failures)));
        }
    }

    /** Records whether the chat model accepts tools. */
    public void supportsTools(String providerId, boolean supports) {
        providers.recordSupportsTools(providerId, supports);
    }

    /** The status wire value of {@code provider} (OpenAPI {@code LlmProvider.status}). */
    public static String status(LlmProviderEntity provider) {
        if (!provider.isEnabled()) {
            return "disabled";
        }
        if (provider.getLastSuccessAt() == null && provider.getLastErrorAt() == null) {
            return "idle";
        }
        int failures = provider.getConsecutiveFailures();
        String status = failures >= HealthEvaluator.FAILING_CONSECUTIVE_FAILURES ? "failing"
                : failures > 0 ? "degraded" : "healthy";
        return status.toLowerCase(Locale.ROOT);
    }
}
