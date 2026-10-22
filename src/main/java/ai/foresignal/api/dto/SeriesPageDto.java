package ai.foresignal.api.dto;

import java.util.List;

/**
 * A page of series catalog entries (OpenAPI {@code SeriesPage}, prompt 093).
 *
 * @param items the entries
 * @param nextCursor the opaque cursor of the next page, or null at the end
 */
public record SeriesPageDto(List<SeriesInfoDto> items, String nextCursor) {}
