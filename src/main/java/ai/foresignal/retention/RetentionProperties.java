package ai.foresignal.retention;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Retention job settings ({@code foresignal.retention.*}, prompt 019). The windows themselves are
 * operator settings ({@code app_setting['retention']}); these are bootstrap mechanics.
 *
 * @param scheduled whether the hourly run happens on its own; off in the test profile
 * @param interval the delay between scheduled runs
 * @param initialDelay the delay before the first scheduled run
 * @param chunkSize rows deleted per STANDARD delete transaction
 * @param purgeTimeout how long a run waits for the writer to finish its head purge
 */
@ConfigurationProperties("foresignal.retention")
public record RetentionProperties(
        @DefaultValue("true") boolean scheduled,
        @DefaultValue("1h") Duration interval,
        @DefaultValue("5m") Duration initialDelay,
        @DefaultValue("10000") int chunkSize,
        @DefaultValue("10m") Duration purgeTimeout) {

    /** Fills missing values with the defaults. */
    public RetentionProperties {
        interval = interval == null ? Duration.ofHours(1) : interval;
        initialDelay = initialDelay == null ? Duration.ofMinutes(5) : initialDelay;
        chunkSize = chunkSize < 1 ? 10_000 : chunkSize;
        purgeTimeout = purgeTimeout == null ? Duration.ofMinutes(10) : purgeTimeout;
    }
}
