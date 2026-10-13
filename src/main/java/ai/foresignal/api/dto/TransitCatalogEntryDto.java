package ai.foresignal.api.dto;

import java.util.List;

/**
 * The {@code TransitCatalogEntry} schema (prompt 070): one row of the vendored Mobility Database GTFS-RT snapshot and
 * whether it is enabled.
 *
 * @param id the Mobility Database id
 * @param provider the operating agency as the catalog names it
 * @param countryCode ISO 3166-1 alpha-2, or null
 * @param entityType the GTFS-RT entity types the URL serves, {@code |}-joined as in the catalog ({@code vp},
 *     {@code tu}, {@code sa}, {@code tu|vp}, {@code sa|tu|vp}); empty when the catalog declares none
 * @param directDownloadUrl the feed URL
 * @param licenseUrl the feed's license URL, or null
 * @param staticReference the matching static GTFS feed id, or null
 * @param enabled true when listed in {@code foresignal.transit.catalog-feeds} and registered
 * @param sourceId the first registered source id ({@code transit-mdb-{id}} or {@code transit-alerts-mdb-{id}}), or
 *     null
 * @param sourceIds every registered source id of the row; empty when not enabled
 */
public record TransitCatalogEntryDto(String id, String provider, String countryCode, String entityType,
        String directDownloadUrl, String licenseUrl, String staticReference, boolean enabled, String sourceId,
        List<String> sourceIds) {}
