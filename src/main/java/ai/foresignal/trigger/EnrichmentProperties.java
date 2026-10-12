package ai.foresignal.trigger;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Alert enrichment mechanics ({@code foresignal.enrichment.*}, prompt 030, Architecture §23).
 * Whether enrichment runs at all is the operator's {@code llm.enrichmentEnabled} setting and each
 * rule's {@code enrich} flag; these are not operator settings.
 *
 * @param autoStart whether the worker subscribes to the alert bus at startup; tests turn it off and
 *     start it themselves
 * @param queueCapacity alerts that may wait for enrichment; more are dropped and counted
 * @param concurrency enrichments running at once
 * @param timeout how long one LLM call may take before it counts as failed
 * @param maxAge alerts that fired longer ago than this are not enriched (catch-up after downtime)
 * @param maxContextBytes the most bytes of serialized context sent to the model
 * @param maxTokens the output cap of one summary
 * @param editWindow how old an already-sent message may be and still be edited to add the summary
 */
@ConfigurationProperties("foresignal.enrichment")
public record EnrichmentProperties(
        @DefaultValue("true") boolean autoStart,
        @DefaultValue("256") int queueCapacity,
        @DefaultValue("4") int concurrency,
        @DefaultValue("20s") Duration timeout,
        @DefaultValue("10m") Duration maxAge,
        @DefaultValue("24576") int maxContextBytes,
        @DefaultValue("600") int maxTokens,
        @DefaultValue("1h") Duration editWindow) {

    /** Fills missing or invalid values with the defaults. */
    public EnrichmentProperties {
        queueCapacity = queueCapacity < 1 ? 256 : queueCapacity;
        concurrency = concurrency < 1 ? 4 : concurrency;
        timeout = timeout == null || timeout.isNegative() || timeout.isZero() ? Duration.ofSeconds(20) : timeout;
        maxAge = maxAge == null || maxAge.isNegative() ? Duration.ofMinutes(10) : maxAge;
        maxContextBytes = maxContextBytes < 1024 ? 24 * 1024 : maxContextBytes;
        maxTokens = maxTokens < 1 ? 600 : maxTokens;
        editWindow = editWindow == null || editWindow.isNegative() ? Duration.ofHours(1) : editWindow;
    }

    /** The production defaults. */
    public static EnrichmentProperties defaults() {
        return new EnrichmentProperties(true, 256, 4, Duration.ofSeconds(20), Duration.ofMinutes(10), 24 * 1024, 600,
                Duration.ofHours(1));
    }
}
