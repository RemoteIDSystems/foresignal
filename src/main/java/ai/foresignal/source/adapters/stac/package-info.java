/**
 * Satellite scene footprints and links from the Earth Search STAC API (prompt 096).
 *
 * <p>{@code stac-sentinel2} ({@code sentinel-2-c1-l2a}), {@code stac-landsat} ({@code landsat-c2-l2}) and
 * {@code stac-sentinel1} ({@code sentinel-1-grd}, disabled by default) record one {@code satellite-scene}
 * per STAC item over the operator's AOI: the footprint, acquisition time, cloud cover, AOI coverage and
 * links to the STAC item and any public thumbnail. Foresignal <strong>never</strong> downloads, proxies
 * or signs an imagery asset; clients follow the links. The feed list is static and code-reviewed
 * ({@link ai.foresignal.source.adapters.stac.StacSources}).
 */
package ai.foresignal.source.adapters.stac;
