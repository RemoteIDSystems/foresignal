package ai.foresignal.api.mapper;

import ai.foresignal.api.dto.FetchLogEntryDto;
import ai.foresignal.api.dto.FetchPage;
import ai.foresignal.store.FetchLogEntity;
import ai.foresignal.store.query.FetchLogQuery;

/**
 * Maps {@code fetch_log} rows to {@code FetchLogEntry}. Bodies are never stored on the row, so none
 * can leak; {@code urlRedacted} is copied as prompt 005 redacted it.
 */
public final class FetchLogMapper {

    private FetchLogMapper() {}

    /** The API page. */
    public static FetchPage page(FetchLogQuery.Page page) {
        return new FetchPage(page.items().stream().map(FetchLogMapper::entry).toList(), page.nextAfter());
    }

    /** One API entry. */
    public static FetchLogEntryDto entry(FetchLogEntity e) {
        return new FetchLogEntryDto(
                e.getId(),
                e.getTrigger() == null ? null : e.getTrigger().name(),
                e.getStatus() == null ? null : e.getStatus().name(),
                e.getStartedAt(),
                e.getReceivedAt(),
                e.getIngestedAt(),
                e.getHttpStatus(),
                e.getRequestCount(),
                e.getUrlRedacted(),
                e.getEtag(),
                e.getBytes(),
                e.getBodySha256(),
                e.getAdapterVersion(),
                e.getLicence(),
                e.getPrincipal(),
                e.getRecordsSeen(),
                e.getRejected(),
                e.getLate(),
                e.getSkipped(),
                e.getVersionsWritten(),
                e.getLiveUpdates(),
                e.getTouches(),
                e.getFromSeq(),
                e.getToSeq(),
                HealthMapper.coverage(e.getCoverage()),
                e.getError(),
                e.getRunId());
    }
}
