package ai.foresignal.testsupport.ingest;

import ai.foresignal.ingest.FetchLogEntry;
import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.ingest.IngestRecord;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.FetchTrigger;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Seeds records through the real ingest writer (Core-Data-Model §17).
 *
 * <p>From prompt 011 on, integration tests must not write {@code record_version},
 * {@code record_head} or {@code record_live} with raw SQL; they seed through this helper so every
 * row is produced exactly as production produces it. Each call submits one batch with a synthetic
 * {@link FetchLogEntry} ({@code MANUAL}, {@code OK}) and waits for its {@link IngestOutcome}.
 */
public class TestRecords {

    /** How long a call waits for the queue and for the writer. */
    public static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final IngestQueue queue;
    private final Clock clock;
    private final AtomicLong arrival = new AtomicLong();

    /** Creates the helper. */
    public TestRecords(IngestQueue queue, Clock clock) {
        this.queue = queue;
        this.clock = clock;
    }

    /** Ingests {@code records} for {@code sourceId}, received now (the application clock). */
    public IngestOutcome ingest(String sourceId, NormalizedRecord... records) {
        return ingestAt(sourceId, clock.instant(), records);
    }

    /** Ingests {@code records} for {@code sourceId}, received at {@code receivedAt}. */
    public IngestOutcome ingestAt(String sourceId, Instant receivedAt, NormalizedRecord... records) {
        return await(submitAt(sourceId, receivedAt, Arrays.asList(records)));
    }

    /** Submits without waiting, received at {@code receivedAt}; the future completes after commit. */
    public CompletableFuture<IngestOutcome> submitAt(String sourceId, Instant receivedAt, List<NormalizedRecord> records) {
        return submit(sourceId, FetchLogEntry.ok(sourceId, FetchTrigger.MANUAL, receivedAt, receivedAt), receivedAt, records);
    }

    /** Submits with an explicit fetch-log entry. */
    public CompletableFuture<IngestOutcome> submit(
            String sourceId, FetchLogEntry log, Instant receivedAt, List<NormalizedRecord> records) {
        List<IngestRecord> batch = new ArrayList<>(records.size());
        for (NormalizedRecord record : records) {
            batch.add(new IngestRecord(record, receivedAt, arrival.incrementAndGet()));
        }
        return queue.submit(sourceId, log, batch, TIMEOUT);
    }

    /** Waits for {@code future}, unwrapping its failure. */
    public static IngestOutcome await(CompletableFuture<IngestOutcome> future) {
        try {
            return future.get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new IllegalStateException(cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } catch (TimeoutException e) {
            throw new IllegalStateException("The ingest writer did not complete the batch within " + TIMEOUT, e);
        }
    }
}
