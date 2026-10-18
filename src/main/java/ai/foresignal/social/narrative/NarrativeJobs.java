package ai.foresignal.social.narrative;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The narrative schedule (prompt 136), on while {@code foresignal.social.narratives-scheduled} is true (off in tests,
 * which call the steps directly):
 *
 * <ul>
 *   <li>every minute: refresh the narratives that grew past the inline refresh ({@link NarrativeService#refreshDirty})
 *       and fingerprint queued post images ({@link MediaFingerprinter#drain});
 *   <li>hourly (at :05): move velocities and states ({@link NarrativeService#refreshStates}) and run the optional LLM
 *       claim extraction within its daily cap ({@link LlmClaimExtraction#runDue});
 *   <li>daily (05:10 UTC): the coordination indicators of the day's narratives ({@link CoordinationRunner#run}).
 * </ul>
 */
@Component
public class NarrativeJobs {

    private static final Logger log = LoggerFactory.getLogger(NarrativeJobs.class);

    /** Dirty narratives refreshed per minute. */
    static final int REFRESH_BATCH = 200;

    /** Images fingerprinted per minute. */
    static final int MEDIA_BATCH = 100;

    private final NarrativeService service;
    private final MediaFingerprinter media;
    private final LlmClaimExtraction llm;
    private final CoordinationRunner coordination;
    private final boolean scheduled;

    /** Creates the jobs. */
    public NarrativeJobs(NarrativeService service, MediaFingerprinter media, LlmClaimExtraction llm,
            CoordinationRunner coordination,
            @Value("${foresignal.social.narratives-scheduled:true}") boolean scheduled) {
        this.service = service;
        this.media = media;
        this.llm = llm;
        this.coordination = coordination;
        this.scheduled = scheduled;
    }

    /** The minute tick. */
    @Scheduled(fixedDelayString = "${foresignal.social.narrative-interval:60s}", initialDelayString = "90s")
    public void minute() {
        if (!scheduled) {
            return;
        }
        try {
            service.refreshDirty(REFRESH_BATCH);
        } catch (RuntimeException e) {
            log.warn("Narrative refresh tick failed: {}", e.toString());
        }
        try {
            media.drain(MEDIA_BATCH);
        } catch (RuntimeException e) {
            log.warn("Media fingerprint tick failed: {}", e.toString());
        }
    }

    /** The hourly pass. */
    @Scheduled(cron = "0 5 * * * *", zone = "UTC")
    public void hourly() {
        if (!scheduled) {
            return;
        }
        try {
            service.refreshStates();
        } catch (RuntimeException e) {
            log.warn("Narrative state pass failed: {}", e.toString());
        }
        try {
            llm.runDue();
        } catch (RuntimeException e) {
            log.warn("LLM claim extraction pass failed: {}", e.toString());
        }
    }

    /** The daily coordination pass. */
    @Scheduled(cron = "0 10 5 * * *", zone = "UTC")
    public void daily() {
        if (!scheduled) {
            return;
        }
        try {
            coordination.run();
        } catch (RuntimeException e) {
            log.warn("Coordination pass failed: {}", e.toString());
        }
    }
}
