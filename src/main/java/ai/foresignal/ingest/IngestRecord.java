package ai.foresignal.ingest;

import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import java.util.Objects;

/**
 * One adapter record as it enters ingest, stamped by the framework (Core-Data-Model §3).
 *
 * @param record what the adapter produced
 * @param receivedAt when Foresignal received the response, message or push that carried it; the
 *     collector stamps polls and the sink stamps streams on accept
 * @param arrival a per-source arrival counter, the last tiebreak of the stable sort
 *     {@code (externalId, observedAt, arrival)} so equal timestamps keep arrival order
 */
public record IngestRecord(NormalizedRecord record, Instant receivedAt, long arrival) {

    public IngestRecord {
        Objects.requireNonNull(record, "record");
        Objects.requireNonNull(receivedAt, "receivedAt");
    }
}
