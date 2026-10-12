package ai.foresignal.notify;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Delivery worker settings ({@code foresignal.notify.*}, prompt 018).
 *
 * @param scheduled whether the worker polls on its own; tests turn it off and call
 *     {@code DeliveryWorker.runOnce()}
 * @param pollInterval how often the worker looks for due deliveries
 * @param batchSize the most deliveries one poll claims
 * @param lease how long a claimed delivery is held before another worker may take it
 * @param connectTimeout the HTTP connect timeout
 * @param requestTimeout the HTTP request timeout
 */
@ConfigurationProperties("foresignal.notify")
public record NotifyProperties(
        @DefaultValue("true") boolean scheduled,
        @DefaultValue("2s") Duration pollInterval,
        @DefaultValue("50") int batchSize,
        @DefaultValue("60s") Duration lease,
        @DefaultValue("5s") Duration connectTimeout,
        @DefaultValue("15s") Duration requestTimeout) {

    /** Fills missing values with the defaults. */
    public NotifyProperties {
        pollInterval = pollInterval == null ? Duration.ofSeconds(2) : pollInterval;
        batchSize = batchSize < 1 ? 50 : batchSize;
        lease = lease == null ? Duration.ofSeconds(60) : lease;
        connectTimeout = connectTimeout == null ? Duration.ofSeconds(5) : connectTimeout;
        requestTimeout = requestTimeout == null ? Duration.ofSeconds(15) : requestTimeout;
    }

    /** The production defaults. */
    public static NotifyProperties defaults() {
        return new NotifyProperties(true, Duration.ofSeconds(2), 50, Duration.ofSeconds(60), Duration.ofSeconds(5),
                Duration.ofSeconds(15));
    }
}
