package ai.foresignal.api.dto;

import java.util.List;

/**
 * The {@code FetchPage} schema: a keyset page of fetch-log entries, newest first.
 *
 * @param items the entries
 * @param nextAfter pass as {@code after} for the next page; null on the last page
 */
public record FetchPage(List<FetchLogEntryDto> items, Long nextAfter) {}
