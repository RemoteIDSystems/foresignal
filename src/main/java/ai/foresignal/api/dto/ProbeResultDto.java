package ai.foresignal.api.dto;

import ai.foresignal.collect.ProbeResult;
import java.util.List;

/**
 * The {@code ProbeResult} schema: recorded exchanges (credentials redacted) and parsed previews.
 *
 * @param sourceId the source
 * @param requests the recorded exchanges, already in the spec's request shape
 * @param records up to 100 previews
 * @param parseError the parse failure, or null
 */
public record ProbeResultDto(
        String sourceId,
        List<ProbeResult.Request> requests,
        List<NormalizedRecordPreviewDto> records,
        String parseError) {}
