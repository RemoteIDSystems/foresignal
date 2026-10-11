package ai.foresignal.ingest;

import java.util.List;

/**
 * What {@link Normalizer#normalize} produced for one batch.
 *
 * @param observations accepted observations, stable-sorted by
 *     {@code (externalId, observedAt, arrival)}; duplicates are kept
 * @param rejections refused records, in submission order
 * @param clamped observations whose {@code observed_at} was clamped by more than the skew
 *     tolerance (flagged {@link QaFlags#FUTURE_CLAMPED}); the writer counts
 *     {@code foresignal.ingest.records.clamped}
 * @param validUntilIgnored explicit {@code validUntil} values dropped because the source is TRACK;
 *     the writer counts {@code foresignal.ingest.records.valid_until_ignored}
 */
public record NormalizedBatch(
        List<Observation> observations, List<Rejection> rejections, int clamped, int validUntilIgnored) {

    public NormalizedBatch {
        observations = observations == null ? List.of() : List.copyOf(observations);
        rejections = rejections == null ? List.of() : List.copyOf(rejections);
    }
}
