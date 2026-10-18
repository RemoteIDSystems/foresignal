package ai.foresignal.chronicle.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The schedule of the chronicle network (prompt 134), on when {@code foresignal.chronicles.network-scheduled} is true
 * (off in tests, which call the jobs directly):
 *
 * <ul>
 *   <li>hourly: refresh every chronicle's sketches (incremental, so capture is reflected at most an hour late), then
 *       run discovery when some sketch changed by more than 10%;</li>
 *   <li>nightly (03:30 UTC): full discovery;</li>
 *   <li>daily (04:30 UTC): expire peer sketches older than 30 days and send this instance's sketch batches to peers
 *       (only while {@code network.federated} is true).</li>
 * </ul>
 */
@Component
public class ChronicleNetworkJobs {

    private static final Logger log = LoggerFactory.getLogger(ChronicleNetworkJobs.class);

    private final ChronicleSketcher sketcher;
    private final ChronicleDiscoveryJob discovery;
    private final FederatedSketches federated;
    private final boolean scheduled;

    /** Creates the jobs. */
    public ChronicleNetworkJobs(ChronicleSketcher sketcher, ChronicleDiscoveryJob discovery,
            FederatedSketches federated, @Value("${foresignal.chronicles.network-scheduled:true}") boolean scheduled) {
        this.sketcher = sketcher;
        this.discovery = discovery;
        this.federated = federated;
        this.scheduled = scheduled;
    }

    /** Hourly sketch refresh and change-triggered discovery. */
    @Scheduled(fixedDelayString = "${foresignal.chronicles.network-sketch-interval:1h}", initialDelayString = "15m")
    public void hourly() {
        if (!scheduled) {
            return;
        }
        try {
            sketcher.refreshAll();
            discovery.runIfDue();
        } catch (RuntimeException e) {
            log.warn("Chronicle sketch refresh failed: {}", e.toString());
        }
    }

    /** Nightly full discovery. */
    @Scheduled(cron = "0 30 3 * * *", zone = "UTC")
    public void nightly() {
        if (!scheduled) {
            return;
        }
        try {
            discovery.run();
        } catch (RuntimeException e) {
            log.warn("Chronicle discovery failed: {}", e.toString());
        }
    }

    /** Daily expiry of peer sketches and delivery of this instance's sketches. */
    @Scheduled(cron = "0 30 4 * * *", zone = "UTC")
    public void federation() {
        if (!scheduled) {
            return;
        }
        try {
            federated.expire();
            federated.sendAll();
        } catch (RuntimeException e) {
            log.warn("Chronicle sketch exchange failed: {}", e.toString());
        }
    }
}
