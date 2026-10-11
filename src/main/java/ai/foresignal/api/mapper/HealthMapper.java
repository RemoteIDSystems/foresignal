package ai.foresignal.api.mapper;

import ai.foresignal.api.dto.CoverageDto;
import ai.foresignal.api.dto.SourceHealthDto;
import ai.foresignal.api.dto.StorageDto;
import ai.foresignal.collect.SourceHealthView;
import ai.foresignal.collect.SourceStorage;
import ai.foresignal.store.CoverageSample;

/**
 * Maps {@link SourceHealthView} (from {@code HealthService}) to {@link SourceHealthDto}.
 *
 * <p>A straight copy: every derived value (status, storage, budget hours, frame age) is computed by
 * {@code HealthService}, never here.
 */
public final class HealthMapper {

    private HealthMapper() {}

    /** The API form of one source's health. */
    public static SourceHealthDto health(SourceHealthView v) {
        return new SourceHealthDto(
                v.sourceId(),
                v.status(),
                v.statusReason(),
                v.lastAttemptAt(),
                v.lastSuccessAt(),
                v.lastErrorAt(),
                v.lastError(),
                v.lastHttpStatus(),
                v.consecutiveFailures(),
                v.lastRecordsSeen(),
                v.lastRecordsChanged(),
                v.errorRateLastHour(),
                v.backoffUntil(),
                SourceMapper.budget(v.budget()),
                v.budgetUsedToday(),
                v.budgetRemainingToday(),
                v.refusedLastHour(),
                v.requestsLastHour(),
                v.bytesLastHour(),
                coverage(v.coverage()),
                storage(v.storage()),
                v.lastDataAt(),
                v.budgetHoursRemaining(),
                v.newestFrameAt(),
                v.frameAgeSeconds());
    }

    /** The API form of a coverage sample; null for null. */
    public static CoverageDto coverage(CoverageSample c) {
        return c == null ? null : new CoverageDto(c.receivers(), c.expected(), c.actual(), c.region());
    }

    /** The API form of a storage figure; zeros for null. */
    public static StorageDto storage(SourceStorage s) {
        SourceStorage x = s == null ? SourceStorage.ZERO : s;
        return new StorageDto(
                x.observationsPerMinute(),
                x.versionsPerMinute(),
                x.persistRatio(),
                x.triggerEdgeShare(),
                x.bytesPerDay(),
                x.projectedBytes(),
                x.lateLastHour(),
                x.clampedLastHour());
    }
}
