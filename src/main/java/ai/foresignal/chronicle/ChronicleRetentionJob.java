package ai.foresignal.chronicle;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * A chronicle's own retention (prompt 131, CHR-1): global retention never deletes chronicle storage; only this job
 * (or deleting the chronicle) does. A stopped or archived chronicle whose {@code retention} is a duration loses its
 * captured history (leaf, series points, rollups, frame pins, cold package) once that duration has passed since it
 * stopped; {@code forever} (the default) keeps it. The chronicle, its layers and its thread stay, and the thread
 * records the removal. Runs hourly unless {@code foresignal.chronicles.retention-scheduled} is false (tests call
 * {@link #run()}).
 *
 * <p>Prompt R2-015: while {@code retention.capHistory} is on with {@code maxDays}, a stopped or archived chronicle
 * keeps its history at most that long after it stopped, {@code forever} included. This job still does the removal,
 * so CHR-1 holds; a running chronicle is never touched.
 */
@Component
public class ChronicleRetentionJob {

    private static final Logger log = LoggerFactory.getLogger(ChronicleRetentionJob.class);

    private final ChronicleRepository chronicles;
    private final ChronicleStorage storage;
    private final ChronicleThreadService thread;
    private final ChronicleCaptureService capture;
    private final ChronicleJobs jobs;
    private final ChronicleProperties properties;
    private final Clock clock;
    private final TransactionTemplate tx;
    private final ai.foresignal.retention.RetentionResolver retention;

    /** Creates the job without the cap-history bound (unit tests). */
    public ChronicleRetentionJob(ChronicleRepository chronicles, ChronicleStorage storage,
            ChronicleThreadService thread, ChronicleCaptureService capture, ChronicleJobs jobs,
            ChronicleProperties properties, Clock clock, PlatformTransactionManager transactions) {
        this(chronicles, storage, thread, capture, jobs, properties, clock, transactions, null);
    }

    /**
     * Creates the job.
     *
     * @param retention the retention settings, for the cap-history bound (prompt R2-015), or null for none
     */
    @org.springframework.beans.factory.annotation.Autowired
    public ChronicleRetentionJob(ChronicleRepository chronicles, ChronicleStorage storage,
            ChronicleThreadService thread, ChronicleCaptureService capture, ChronicleJobs jobs,
            ChronicleProperties properties, Clock clock, PlatformTransactionManager transactions,
            ai.foresignal.retention.RetentionResolver retention) {
        this.retention = retention;
        this.chronicles = chronicles;
        this.storage = storage;
        this.thread = thread;
        this.capture = capture;
        this.jobs = jobs;
        this.properties = properties;
        this.clock = clock;
        this.tx = new TransactionTemplate(transactions);
    }

    /** The hourly run. */
    @Scheduled(fixedDelayString = "${foresignal.chronicles.retention-interval:1h}", initialDelayString = "10m")
    public void scheduled() {
        if (properties.retentionScheduled()) {
            try {
                run();
            } catch (RuntimeException e) {
                log.warn("Chronicle retention failed: {}", e.toString());
            }
        }
    }

    /**
     * Removes the captured history of every stopped or archived chronicle whose retention elapsed.
     *
     * @return the chronicles whose history was removed
     */
    public int run() {
        Instant now = clock.instant();
        int removed = 0;
        for (ChronicleEntity c : chronicles.findByStatusInOrderByCreatedAtAscIdAsc(List.of(ChronicleVocabulary.STOPPED,
                ChronicleVocabulary.ARCHIVED))) {
            if (c.getHistoryExpiredAt() != null || c.getStoppedAt() == null) {
                continue;
            }
            Duration keep = keep(c.getRetention(), cap());
            if (keep == null || c.getStoppedAt().plus(keep).isAfter(now)) {
                continue;
            }
            String why = keep.equals(duration(c.getRetention())) ? c.getRetention() + " after stop"
                    : "cap history, " + keep.toDays() + " days after stop";
            capture.exclusive(() -> {
                storage.deleteCaptured(c.getId());
                jobs.deletePackage(c.getId());
                tx.executeWithoutResult(s -> {
                    ChronicleEntity ch = chronicles.findById(c.getId()).orElseThrow();
                    ch.setHistoryExpiredAt(now);
                    ch.setLeafCreated(false);
                    ch.setBytesUsed(0);
                    ch.setRowsCaptured(0);
                    ch.touch(now);
                    chronicles.save(ch);
                    thread.statusChange(ch, ch.getStatus(), "history removed by the chronicle's own retention ("
                            + why + ")", 0);
                });
                return null;
            });
            removed++;
        }
        return removed;
    }

    /** The cap-history bound in force (prompt R2-015), or null. */
    private Duration cap() {
        return retention == null ? null : retention.settings().capWindow().orElse(null);
    }

    /**
     * How long a stopped chronicle keeps its history: its own retention, bounded by the cap-history window
     * (prompt R2-015).
     *
     * @param retention the chronicle's retention ({@code forever} or an ISO-8601 duration)
     * @param cap the cap-history window, or null
     * @return the duration, or null to keep it
     */
    public static Duration keep(String retention, Duration cap) {
        Duration own = duration(retention);
        if (cap == null) {
            return own;
        }
        return own == null || own.compareTo(cap) > 0 ? cap : own;
    }

    /** The retention duration, or null for {@code forever} (or an unreadable value, which keeps the history). */
    static Duration duration(String retention) {
        if (retention == null || "forever".equals(retention)) {
            return null;
        }
        try {
            return Duration.parse(retention);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
