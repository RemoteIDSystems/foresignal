package ai.foresignal.api.dto;

import java.util.List;

/**
 * The points of one series in {@code periodStart} order, with its catalogue entry (OpenAPI
 * {@code SeriesPointPage}, prompt 093).
 *
 * @param series the series catalogue entry
 * @param items the points
 * @param nextCursor the opaque cursor of the next page, or null at the end
 * @param truncated true when more points exist in the window than the limit returned
 */
public record SeriesPointPageDto(
        SeriesInfoDto series, List<SeriesPointDto> items, String nextCursor, boolean truncated) {}
