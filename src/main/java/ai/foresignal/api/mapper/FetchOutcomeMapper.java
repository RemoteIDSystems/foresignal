package ai.foresignal.api.mapper;

import ai.foresignal.api.dto.FetchOutcomeDto;
import ai.foresignal.api.dto.NormalizedRecordPreviewDto;
import ai.foresignal.api.dto.ObserverDto;
import ai.foresignal.api.dto.ProbeResultDto;
import ai.foresignal.api.dto.QualityDto;
import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.collect.NormalizedRecordPreview;
import ai.foresignal.collect.ProbeResult;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.source.spi.Observer;
import ai.foresignal.source.spi.Quality;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps manual-fetch outcomes and probe results to their API records.
 *
 * <p>The internal {@code status} and {@code skipReason} of a {@link FetchOutcome} are not part of the
 * schema and are dropped (a skipped manual fetch has already become a 409 in
 * {@code ManualFetchService}).
 */
public final class FetchOutcomeMapper {

    private FetchOutcomeMapper() {}

    /**
     * The API outcome. {@code byReason} lists all nine persist reasons in declaration order, zeros
     * included, and {@code recordsChanged} equals {@code persisted}.
     */
    public static FetchOutcomeDto outcome(FetchOutcome o) {
        return new FetchOutcomeDto(
                o.sourceId(),
                o.startedAt(),
                o.durationMs(),
                o.success(),
                o.recordsSeen(),
                o.persisted(),
                o.fromSeq(),
                o.toSeq(),
                o.error(),
                o.fetchId(),
                o.persisted(),
                o.live(),
                o.touched(),
                o.touchesThrottled(),
                o.late(),
                o.skipped(),
                o.rejected(),
                byReason(o.byReason()));
    }

    /** All nine reasons by name, in {@link PersistReason} order; a missing reason counts 0. */
    public static Map<String, Integer> byReason(Map<PersistReason, Integer> counts) {
        Map<String, Integer> out = new LinkedHashMap<>();
        for (PersistReason reason : PersistReason.values()) {
            Integer count = counts == null ? null : counts.get(reason);
            out.put(reason.name(), count == null ? 0 : count);
        }
        return out;
    }

    /** The API probe result. */
    public static ProbeResultDto probe(ProbeResult result) {
        return new ProbeResultDto(
                result.sourceId(),
                result.requests(),
                result.records().stream().map(FetchOutcomeMapper::preview).toList(),
                result.parseError());
    }

    /** The API form of one preview. */
    public static NormalizedRecordPreviewDto preview(NormalizedRecordPreview p) {
        return new NormalizedRecordPreviewDto(
                p.externalId(),
                p.kind(),
                p.observedAt(),
                p.geometry(),
                p.validUntil(),
                p.tombstone(),
                p.payload(),
                p.eventTime(),
                p.validFrom(),
                quality(p.quality()),
                p.observers() == null ? List.of() : p.observers().stream().map(FetchOutcomeMapper::observer).toList());
    }

    /** The API quality; null for null or empty quality. */
    public static QualityDto quality(Quality q) {
        if (q == null || q.isEmpty()) {
            return null;
        }
        return new QualityDto(
                q.accuracyMeters(),
                q.confidence(),
                q.positionSource() == null ? null : q.positionSource().wireValue(),
                q.extra());
    }

    /** The API observer. */
    public static ObserverDto observer(Observer o) {
        return new ObserverDto(o.receiverId(), o.frequencyHz(), o.rssi(), o.snr(), o.decodeQuality());
    }
}
