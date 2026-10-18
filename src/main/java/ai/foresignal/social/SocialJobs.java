package ai.foresignal.social;

import ai.foresignal.source.SourceService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The social schedule (prompt 135), on while {@code foresignal.social.scheduled} is true (off in tests, which call the
 * steps directly):
 *
 * <ul>
 *   <li>every minute: drain the collector's counters into the aggregate tables ({@link #flush}), close every hour that
 *       ended (stamp the complete flags of complete-stream platforms, then run the velocity detector), and refresh
 *       the adapters' states ({@code social.platform.state} on a change);
 *   <li>daily (04:50 UTC): prune quota uses older than eight days.
 * </ul>
 */
@Component
public class SocialJobs {

    private static final Logger log = LoggerFactory.getLogger(SocialJobs.class);

    private final SocialCollector collector;
    private final SocialVolumeStore store;
    private final StreamLedger ledger;
    private final VelocityDetector velocity;
    private final SocialPlatformService platforms;
    private final QuotaLedger quotas;
    private final ObjectProvider<SourceService> sources;
    private final Clock clock;
    private final boolean scheduled;
    private volatile Instant lastClosed;

    /** Creates the jobs. */
    public SocialJobs(SocialCollector collector, SocialVolumeStore store, StreamLedger ledger, VelocityDetector velocity,
            SocialPlatformService platforms, QuotaLedger quotas, ObjectProvider<SourceService> sources, Clock clock,
            @Value("${foresignal.social.scheduled:true}") boolean scheduled) {
        this.collector = collector;
        this.store = store;
        this.ledger = ledger;
        this.velocity = velocity;
        this.platforms = platforms;
        this.quotas = quotas;
        this.sources = sources;
        this.clock = clock;
        this.scheduled = scheduled;
    }

    /** The minute tick. */
    @Scheduled(fixedDelayString = "${foresignal.social.flush-interval:60s}", initialDelayString = "60s")
    public void minute() {
        if (!scheduled) {
            return;
        }
        try {
            flush();
            closeHours(clock.instant());
            platforms.refreshStates();
        } catch (RuntimeException e) {
            log.warn("Social aggregation tick failed: {}", e.toString());
        }
    }

    /** The daily prune. */
    @Scheduled(cron = "0 50 4 * * *", zone = "UTC")
    public void daily() {
        if (!scheduled) {
            return;
        }
        try {
            quotas.prune();
        } catch (RuntimeException e) {
            log.warn("Social quota prune failed: {}", e.toString());
        }
    }

    /**
     * Drains the counters into the store; a failed write is given back to the accumulator.
     *
     * @return the volume deltas written
     */
    public int flush() {
        VolumeAccumulator.Drain drain = collector.accumulator().drain(clock.instant());
        if (drain.isEmpty()) {
            return 0;
        }
        try {
            store.apply(drain, clock.instant());
            return drain.volumes().size();
        } catch (RuntimeException e) {
            collector.accumulator().restore(drain);
            throw e;
        }
    }

    /**
     * Closes every hour that ended since the last close (at most the last 24 on the first run): complete flags of
     * complete-stream platforms, then the velocity detector.
     *
     * @param now the current time
     * @return the hours closed
     */
    public int closeHours(Instant now) {
        Instant current = VolumeAccumulator.hourOf(now);
        Instant from = lastClosed == null ? current.minus(Duration.ofHours(1)) : lastClosed.plus(Duration.ofHours(1));
        if (from.isBefore(current.minus(Duration.ofHours(24)))) {
            from = current.minus(Duration.ofHours(24));
        }
        int n = 0;
        for (Instant hour = from; hour.isBefore(current); hour = hour.plus(Duration.ofHours(1))) {
            closeHour(hour, now);
            lastClosed = hour;
            n++;
        }
        return n;
    }

    /** Closes one hour. */
    public void closeHour(Instant hour, Instant now) {
        for (SocialPlatformCatalog.Definition d : SocialObservability.streamPlatforms()) {
            boolean enabled = enabled(d.id());
            boolean complete = enabled && ledger.completeHour(d.id(), hour);
            try {
                store.closeHour(d.network(), hour, complete, now);
            } catch (RuntimeException e) {
                log.debug("Could not close {} for {}: {}", hour, d.network(), e.toString());
            }
        }
        try {
            velocity.run(hour);
        } catch (RuntimeException e) {
            log.warn("Social velocity detection failed for {}: {}", hour, e.toString());
        }
    }

    private boolean enabled(String sourceId) {
        SourceService service = sources.getIfAvailable();
        if (service == null) {
            return false;
        }
        try {
            return service.view(sourceId).enabled();
        } catch (RuntimeException e) {
            return false;
        }
    }
}
