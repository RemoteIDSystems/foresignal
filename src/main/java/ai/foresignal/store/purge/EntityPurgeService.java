package ai.foresignal.store.purge;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.ingest.EntityPurgeOutcome;
import ai.foresignal.ingest.IngestBackpressureException;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.source.SourceRegistry;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Erases one entity (prompt 039, {@code purgeRecord}, {@code purgeDevice}, MCP {@code purge_entity}).
 *
 * <p>Submits the writer's {@code EntityPurge} command and waits for it: the writer writes the
 * tombstone (qa flag {@code PURGED}, its change carries {@code purged: true}), deletes every older
 * version on both tiers and sets the head and live rows to the tombstone state, all in one
 * transaction. Then {@link PurgeFanout} erases the derived stores. Messages already delivered to
 * external channels cannot be recalled. Auditing is the caller's job ({@link EntityAudit}).
 */
@Service
public class EntityPurgeService {

    private static final Logger log = LoggerFactory.getLogger(EntityPurgeService.class);

    /** How long to wait for room in the writer queue and for the writer to finish. */
    static final Duration TIMEOUT = Duration.ofSeconds(60);

    private final IngestQueue queue;
    private final SourceRegistry registry;
    private final PurgeFanout fanout;

    /** Creates the service. */
    public EntityPurgeService(IngestQueue queue, SourceRegistry registry, PurgeFanout fanout) {
        this.queue = queue;
        this.registry = registry;
        this.fanout = fanout;
    }

    /**
     * Purges {@code sourceId}/{@code externalId}.
     *
     * @param purgeAlerts delete the alerts referencing the entity instead of redacting them
     * @return the report
     * @throws NotFoundException when the source is unknown or the entity has no head
     * @throws ForesignalException 503 {@code INGEST_BUSY} when the writer could not take or finish it
     */
    public PurgeReport purge(String sourceId, String externalId, boolean purgeAlerts) {
        registry.descriptor(sourceId);
        EntityPurgeOutcome outcome;
        try {
            outcome = queue.submitEntityPurge(sourceId, externalId, TIMEOUT)
                    .get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (IngestBackpressureException | TimeoutException e) {
            throw busy(e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw busy("Interrupted while waiting for the writer.");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new IllegalStateException("The entity purge failed", cause);
        }
        if (!outcome.found()) {
            throw NotFoundException.of("Record", sourceId + ":" + externalId);
        }
        PurgeFanout.Result result = fanout.run(sourceId, externalId, purgeAlerts);
        // The external id may be personal data; it is not logged.
        log.info("Purged an entity of {} (tombstone seq {}, {} version(s) deleted)", sourceId,
                outcome.tombstoneSeq(), outcome.versionsDeleted());
        return new PurgeReport(outcome.tombstoneSeq(), outcome.versionsDeleted(), result.alertsRedacted(),
                result.alertsDeleted(), result.embeddingsDeleted(), result.notificationsRedacted(), result.citing());
    }

    private static ForesignalException busy(String detail) {
        return new ForesignalException(HttpStatus.SERVICE_UNAVAILABLE, ErrorCodes.INGEST_BUSY,
                "The ingest writer did not complete the purge in time: " + detail);
    }
}
