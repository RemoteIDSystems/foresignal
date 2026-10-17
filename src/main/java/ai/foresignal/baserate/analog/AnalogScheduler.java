package ai.foresignal.baserate.analog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The analog schedule (prompt 121): every five minutes, the signatures due (a newly activated situation's first, each
 * daily one in the first 30 days, and a backfill) and the outcomes due. {@code foresignal.analytics.scheduled=false}
 * turns it off (tests call {@link SignatureService#computeDue} directly).
 */
@Component
public class AnalogScheduler {

    private static final Logger log = LoggerFactory.getLogger(AnalogScheduler.class);

    private final SignatureService signatures;
    private final boolean scheduled;

    /** Creates the scheduler. */
    public AnalogScheduler(SignatureService signatures,
            @Value("${foresignal.analytics.scheduled:true}") boolean scheduled) {
        this.signatures = signatures;
        this.scheduled = scheduled;
    }

    /** The signature and outcome pass. */
    @Scheduled(fixedDelayString = "PT5M", initialDelayString = "PT6M")
    public void run() {
        if (!scheduled) {
            return;
        }
        try {
            int written = signatures.computeDue();
            if (written > 0) {
                log.debug("Analog signatures and outcomes written: {}", written);
            }
        } catch (RuntimeException e) {
            log.warn("The analog signature run failed: {}", e.toString());
        }
    }
}
