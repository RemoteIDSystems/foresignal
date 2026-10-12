package ai.foresignal.store.tiles;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Vector tile settings, bound from {@code foresignal.tiles.*} (prompt 016).
 *
 * @param maxFeatures the most features one raw tile carries; a tile with more matching records is
 *     cut to the newest {@code maxFeatures} and sent with {@code Foresignal-Tile-Truncated: true}
 * @param cellsPerTile aggregate cells across one tile width; even, so square cells line up with tile
 *     edges at every zoom
 * @param aggMaxAge {@code Cache-Control max-age} of aggregated tiles
 * @param rawMaxAge {@code Cache-Control max-age} of raw tiles
 */
@ConfigurationProperties("foresignal.tiles")
public record TileProperties(
        @DefaultValue("50000") int maxFeatures,
        @DefaultValue("32") int cellsPerTile,
        @DefaultValue("60s") Duration aggMaxAge,
        @DefaultValue("5s") Duration rawMaxAge) {

    /** Largest accepted {@code cellsPerTile}. */
    public static final int MAX_CELLS_PER_TILE = 256;

    /** Validates the values and fills missing durations. */
    public TileProperties {
        if (maxFeatures < 1) {
            throw new IllegalArgumentException("foresignal.tiles.max-features must be positive, got " + maxFeatures);
        }
        if (cellsPerTile < 2 || cellsPerTile > MAX_CELLS_PER_TILE || cellsPerTile % 2 != 0) {
            throw new IllegalArgumentException(
                    "foresignal.tiles.cells-per-tile must be an even number from 2 to " + MAX_CELLS_PER_TILE
                            + ", got " + cellsPerTile);
        }
        aggMaxAge = aggMaxAge == null ? Duration.ofSeconds(60) : aggMaxAge;
        rawMaxAge = rawMaxAge == null ? Duration.ofSeconds(5) : rawMaxAge;
        if (aggMaxAge.isNegative() || rawMaxAge.isNegative()) {
            throw new IllegalArgumentException("foresignal.tiles max-age values must not be negative");
        }
    }

    /** The shipped defaults: 50,000 features, 32 cells, 60 s and 5 s. */
    public static TileProperties defaults() {
        return new TileProperties(50_000, 32, Duration.ofSeconds(60), Duration.ofSeconds(5));
    }
}
