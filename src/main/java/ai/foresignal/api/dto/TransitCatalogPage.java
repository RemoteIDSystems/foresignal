package ai.foresignal.api.dto;

import java.util.List;

/**
 * The {@code TransitCatalogPage} schema (prompt 070): a page of snapshot rows by id.
 *
 * @param items the rows
 * @param nextCursor pass as {@code cursor} for the next page; null on the last page
 */
public record TransitCatalogPage(List<TransitCatalogEntryDto> items, String nextCursor) {}
