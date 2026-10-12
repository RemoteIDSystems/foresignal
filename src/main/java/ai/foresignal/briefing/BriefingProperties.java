package ai.foresignal.briefing;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Briefing scheduler mechanics ({@code foresignal.briefing.*}, prompt 029). Schedules themselves are
 * per briefing; these are not operator settings.
 *
 * @param scheduled whether the scheduler ticks on its own; tests turn it off and call
 *     {@code BriefingScheduler.tick()}
 * @param pollInterval how often due briefings are looked for (Architecture §22: 30 s)
 * @param runTimeout how long {@code runBriefing} waits for a run before answering with it still
 *     {@code running}
 * @param staleRun how old a run-in-progress mark may get before it is treated as abandoned (a crash
 *     mid-run)
 * @param batchSize the most due briefings one tick claims
 */
@ConfigurationProperties("foresignal.briefing")
public record BriefingProperties(
        @DefaultValue("true") boolean scheduled,
        @DefaultValue("30s") Duration pollInterval,
        @DefaultValue("120s") Duration runTimeout,
        @DefaultValue("15m") Duration staleRun,
        @DefaultValue("20") int batchSize) {

    /** Fills missing values with the defaults. */
    public BriefingProperties {
        pollInterval = pollInterval == null ? Duration.ofSeconds(30) : pollInterval;
        runTimeout = runTimeout == null ? Duration.ofSeconds(120) : runTimeout;
        staleRun = staleRun == null ? Duration.ofMinutes(15) : staleRun;
        batchSize = batchSize < 1 ? 20 : batchSize;
    }

    /** The production defaults. */
    public static BriefingProperties defaults() {
        return new BriefingProperties(true, Duration.ofSeconds(30), Duration.ofSeconds(120), Duration.ofMinutes(15), 20);
    }
}
