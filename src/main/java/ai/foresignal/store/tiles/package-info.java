/**
 * Mapbox Vector Tiles of current live state (prompt 016).
 *
 * <p>{@link ai.foresignal.store.tiles.TileLayerResolver} turns a layer name (a source id or a kind)
 * into a {@link ai.foresignal.store.tiles.TileLayer}: the sources it covers, its feature properties,
 * its {@code aggBelowZoom} and one composable SQL filter. {@link ai.foresignal.store.tiles.TileQueryService}
 * renders raw tiles from {@code record_live l JOIN record_head h} (geometry and {@code observedAt}
 * from {@code l}, so live positions show before any new version is persisted) and aggregated
 * grid or hex tiles below {@code aggBelowZoom}.
 */
package ai.foresignal.store.tiles;
