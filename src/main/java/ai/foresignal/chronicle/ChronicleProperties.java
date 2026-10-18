package ai.foresignal.chronicle;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Chronicle settings ({@code foresignal.chronicles.*}, prompt 131). The default size cap and the auto-activation caps
 * are app settings ({@code chronicle.defaultCapGb}, {@code chronicle.autoActivatePerDay},
 * {@code chronicle.autoActivateCapGb}); these are deployment knobs.
 *
 * @param autoStart whether the capture loop, its change-bus subscriber and the situation and alert listeners start on
 *     their own (off in tests, which call {@code ChronicleCaptureService.catchUp()})
 * @param pollInterval how long the capture loop sleeps when nothing woke it (default 2 s)
 * @param chunkSeqs the seq range read from {@code record_version} per capture pass (default 10,000)
 * @param firstAppearancesPerHour the most {@code first-appearance} thread items per layer per hour (default 20)
 * @param seriesSampleInterval how often {@code rollup:} and {@code rule:} series layers are sampled (default 15 min)
 * @param retentionScheduled whether the chronicles' own retention runs hourly (off in tests)
 * @param asyncJobs whether backfills, archives and restores run on their own virtual thread (off in tests, which then
 *     see them finished when the request returns)
 */
@ConfigurationProperties("foresignal.chronicles")
public record ChronicleProperties(
        @DefaultValue("true") boolean autoStart,
        @DefaultValue("2s") Duration pollInterval,
        @DefaultValue("10000") long chunkSeqs,
        @DefaultValue("20") int firstAppearancesPerHour,
        @DefaultValue("15m") Duration seriesSampleInterval,
        @DefaultValue("true") boolean retentionScheduled,
        @DefaultValue("true") boolean asyncJobs) {

    /** Fills missing or invalid values with the defaults. */
    public ChronicleProperties {
        pollInterval = pollInterval == null || pollInterval.isNegative() || pollInterval.isZero()
                ? Duration.ofSeconds(2) : pollInterval;
        chunkSeqs = chunkSeqs <= 0 ? 10_000 : chunkSeqs;
        firstAppearancesPerHour = firstAppearancesPerHour < 0 ? 20 : firstAppearancesPerHour;
        seriesSampleInterval = seriesSampleInterval == null || seriesSampleInterval.isNegative()
                || seriesSampleInterval.isZero() ? Duration.ofMinutes(15) : seriesSampleInterval;
    }
}
