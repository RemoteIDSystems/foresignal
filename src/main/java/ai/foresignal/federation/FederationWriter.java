package ai.foresignal.federation;

import ai.foresignal.ingest.FetchLogEntry;
import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.ingest.IngestRecord;
import ai.foresignal.source.spi.Coverage;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.FetchStatus;
import ai.foresignal.store.FetchTrigger;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/**
 * Writes federated records straight to the ingest queue (prompt 113): a RECEIVE push, a snapshot replay (with
 * forced touches, so heads the snapshot saw are not tombstoned as absent) and a record that belongs to another
 * peer source than the one that received it (origin ownership). Each call is one batch with a {@code fetch_log}
 * row whose license is {@value PeerDescriptors#LICENSE}; the per-record license is {@code _foresignal.originLicence}.
 * Only the writer persists anything (ING-1).
 */
@Component
public class FederationWriter {

    /** How long to wait for room in the ingest queue. */
    static final Duration QUEUE_TIMEOUT = Duration.ofSeconds(30);

    /** How long to wait for the writer's outcome. */
    static final Duration OUTCOME_TIMEOUT = Duration.ofSeconds(120);

    private final IngestQueue queue;
    private final Clock clock;
    private final AtomicLong arrival = new AtomicLong();

    /** Creates the writer. */
    public FederationWriter(IngestQueue queue, Clock clock) {
        this.queue = queue;
        this.clock = clock;
    }

    /**
     * Submits {@code records} as one batch per target source and waits for every outcome.
     *
     * @param bySource the records, grouped by local source
     * @param trigger {@code PUSH} for a RECEIVE push, {@code STREAM} for the PULL runtime
     * @param principal who sent them ({@code peer:{id}})
     * @param forceTouch true during a snapshot replay
     * @return the outcomes by source
     * @throws IllegalStateException when the queue stayed full or the writer failed
     */
    public Map<String, IngestOutcome> submit(Map<String, List<NormalizedRecord>> bySource, FetchTrigger trigger,
            String principal, boolean forceTouch) {
        Map<String, CompletableFuture<IngestOutcome>> futures = new LinkedHashMap<>();
        Instant now = clock.instant();
        bySource.forEach((sourceId, records) -> {
            if (records.isEmpty()) {
                return;
            }
            List<IngestRecord> batch = new ArrayList<>(records.size());
            for (NormalizedRecord record : records) {
                batch.add(new IngestRecord(record, now, arrival.incrementAndGet()));
            }
            FetchLogEntry entry = new FetchLogEntry(sourceId, trigger, FetchStatus.OK, now, now, null, 0, null, null,
                    null, null, null, PeerDescriptors.LICENSE, principal, Coverage.none(), null);
            futures.put(sourceId, queue.submit(sourceId, entry, batch, QUEUE_TIMEOUT, forceTouch));
        });
        Map<String, IngestOutcome> out = new LinkedHashMap<>();
        for (Map.Entry<String, CompletableFuture<IngestOutcome>> future : futures.entrySet()) {
            try {
                out.put(future.getKey(), future.getValue().get(OUTCOME_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("interrupted while the writer stored federated records", e);
            } catch (Exception e) {
                throw new IllegalStateException("the writer could not store federated records for "
                        + future.getKey() + ": " + e.getMessage(), e);
            }
        }
        return out;
    }

    /** Groups mapped records by their target source, in arrival order. */
    public static Map<String, List<NormalizedRecord>> group(List<FederationIngestor.Mapped> mapped) {
        Map<String, List<NormalizedRecord>> out = new LinkedHashMap<>();
        for (FederationIngestor.Mapped m : mapped) {
            out.computeIfAbsent(m.sourceId(), k -> new ArrayList<>()).add(m.record());
        }
        return out;
    }
}
