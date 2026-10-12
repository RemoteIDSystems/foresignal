package ai.foresignal.store.tiles;

/**
 * One rendered vector tile.
 *
 * @param mvt the Mapbox Vector Tile bytes; empty for a tile without features
 * @param truncated true when a raw tile hit {@code foresignal.tiles.maxFeatures} and carries only
 *     the newest features
 * @param shape raw, grid or hex
 */
public record TileResult(byte[] mvt, boolean truncated, TileShape shape) {

    /** Replaces a null body with an empty one. */
    public TileResult {
        mvt = mvt == null ? new byte[0] : mvt;
        shape = shape == null ? TileShape.RAW : shape;
    }
}
