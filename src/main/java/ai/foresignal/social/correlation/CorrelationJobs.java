package ai.foresignal.social.correlation;

import java.time.Clock;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The social–physical schedule (prompt 137), on while {@code foresignal.social.correlation-scheduled} is true (off in
 * tests, which call the steps directly):
 *
 * <ul>
 *   <li>every 15 minutes: compare significant narratives and re-evaluate open comparisons ({@link
 *       ComparisonService#run}), run queued cues ({@link SocialCueService#runQueued}) and refresh cue narratives
 *       ({@link SocialCueService#refreshNarratives});</li>
 *   <li>hourly (at :25): attention gaps ({@link AttentionGapDetector#run}) and canonical-event cue triggers ({@link
 *       CueTriggers#scanCanonical}).</li>
 * </ul>
 */
@Component
public class CorrelationJobs {

    private static final Logger log = LoggerFactory.getLogger(CorrelationJobs.class);

    private final ComparisonService comparisons;
    private final SocialCueService cues;
    private final AttentionGapDetector gaps;
    private final CueTriggers triggers;
    private final Clock clock;
    private final boolean scheduled;

    /** Creates the jobs. */
    public CorrelationJobs(ComparisonService comparisons, SocialCueService cues, AttentionGapDetector gaps,
            CueTriggers triggers, Clock clock,
            @Value("${foresignal.social.correlation-scheduled:true}") boolean scheduled) {
        this.comparisons = comparisons;
        this.cues = cues;
        this.gaps = gaps;
        this.triggers = triggers;
        this.clock = clock;
        this.scheduled = scheduled;
    }

    /** The 15-minute tick. */
    @Scheduled(fixedDelayString = "${foresignal.social.correlation-interval:15m}", initialDelayString = "120s")
    public void tick() {
        if (!scheduled) {
            return;
        }
        try {
            comparisons.run();
        } catch (RuntimeException e) {
            log.warn("Social comparison pass failed: {}", e.toString());
        }
        try {
            cues.runQueued();
            cues.refreshNarratives();
        } catch (RuntimeException e) {
            log.warn("Social cue pass failed: {}", e.toString());
        }
    }

    /** The hourly pass. */
    @Scheduled(cron = "0 25 * * * *", zone = "UTC")
    public void hourly() {
        if (!scheduled) {
            return;
        }
        try {
            gaps.run();
        } catch (RuntimeException e) {
            log.warn("Attention-gap pass failed: {}", e.toString());
        }
        try {
            triggers.scanCanonical(clock.instant().minus(Duration.ofHours(1)).minus(Duration.ofMinutes(5)));
        } catch (RuntimeException e) {
            log.warn("Canonical cue trigger pass failed: {}", e.toString());
        }
    }
}
