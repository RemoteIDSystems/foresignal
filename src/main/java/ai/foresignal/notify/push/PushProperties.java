package ai.foresignal.notify.push;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Push mechanics ({@code foresignal.push.*}, prompt 031).
 *
 * @param scheduled whether the dispatch worker and the sweeper run on their own; tests turn them off
 *     and call {@code PushDispatchWorker.runOnce()} and {@code PushSubscriptionService.sweep()}
 * @param builtinChannel whether the built-in "Push" channel is created at startup; the test profile
 *     turns it off so other notification tests see only their own channels
 * @param pollInterval how often due dispatches are looked for
 * @param sweepInterval how often expired pending subscriptions are deleted
 * @param batchSize the most dispatches one poll claims
 * @param lease how long a claimed dispatch is held before another worker may take it
 * @param perHostRate pushes per second to one push-service host (Bucket4j)
 * @param connectTimeout the connect timeout
 * @param requestTimeout the request timeout
 */
@ConfigurationProperties("foresignal.push")
public record PushProperties(
        @DefaultValue("true") boolean scheduled,
        @DefaultValue("true") boolean builtinChannel,
        @DefaultValue("2s") Duration pollInterval,
        @DefaultValue("60s") Duration sweepInterval,
        @DefaultValue("100") int batchSize,
        @DefaultValue("60s") Duration lease,
        @DefaultValue("20") int perHostRate,
        @DefaultValue("5s") Duration connectTimeout,
        @DefaultValue("15s") Duration requestTimeout) {

    /** Fills missing values with the defaults. */
    public PushProperties {
        pollInterval = pollInterval == null ? Duration.ofSeconds(2) : pollInterval;
        sweepInterval = sweepInterval == null ? Duration.ofSeconds(60) : sweepInterval;
        batchSize = batchSize < 1 ? 100 : batchSize;
        lease = lease == null ? Duration.ofSeconds(60) : lease;
        perHostRate = perHostRate < 1 ? 20 : perHostRate;
        connectTimeout = connectTimeout == null ? Duration.ofSeconds(5) : connectTimeout;
        requestTimeout = requestTimeout == null ? Duration.ofSeconds(15) : requestTimeout;
    }
}
