package ai.foresignal.inbound.edp.binding;

import ai.foresignal.ingest.FetchLogEntry;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.ingest.IngestRecord;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.FetchTrigger;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/**
 * Writes the records a binding converts (prompt 138: {@code social-post} and {@code news-article} records) under the
 * provider's own source id, so they keep its provenance, license, egress and privacy. Waits for the writer.
 */
@Component
public class BindingIngest {

    /** How long a write waits for room and for the writer. */
    static final Duration WAIT = Duration.ofSeconds(30);

    private final IngestQueue queue;
    private final Clock clock;
    private final AtomicLong arrival = new AtomicLong();

    /** Creates the writer. */
    public BindingIngest(IngestQueue queue, Clock clock) {
        this.queue = queue;
        this.clock = clock;
    }

    /**
     * Writes {@code record} under {@code sourceId}.
     *
     * @throws IllegalStateException when the writer refused or failed the batch
     */
    public void write(String sourceId, NormalizedRecord record) {
        Instant now = clock.instant();
        try {
            queue.submit(sourceId, FetchLogEntry.ok(sourceId, FetchTrigger.PUSH, now, now),
                    List.of(new IngestRecord(record, now, arrival.incrementAndGet())), WAIT)
                    .get(WAIT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while writing " + record.externalId(), e);
        } catch (Exception e) {
            throw new IllegalStateException("the writer did not store " + record.externalId() + ": " + e, e);
        }
    }
}
