package ai.foresignal.store.tiles;

import ai.foresignal.api.support.QueryParams;
import java.util.List;
import java.util.Locale;

/**
 * The {@code agg} query parameter of {@code getTile}: {@code auto} (default) aggregates with hex
 * cells when {@code z < aggBelowZoom}; {@code none} forces raw features; {@code grid} and
 * {@code hex} force aggregation at any zoom.
 */
public enum AggMode {
    AUTO,
    NONE,
    GRID,
    HEX;

    static final List<String> VALUES = List.of("auto", "none", "grid", "hex");

    /**
     * Parses the parameter; absent means {@link #AUTO}.
     *
     * @throws ai.foresignal.api.error.ValidationException for any other value
     */
    public static AggMode parse(String raw) {
        String value = QueryParams.oneOf("agg", raw, VALUES, "auto");
        return valueOf(value.toUpperCase(Locale.ROOT));
    }

    /**
     * The shape of a tile at zoom {@code z} for a layer whose {@code aggBelowZoom} is
     * {@code aggBelowZoom} (null means never aggregate under {@link #AUTO}).
     */
    public TileShape shapeFor(int z, Integer aggBelowZoom) {
        return switch (this) {
            case NONE -> TileShape.RAW;
            case GRID -> TileShape.GRID;
            case HEX -> TileShape.HEX;
            case AUTO -> aggBelowZoom != null && z < aggBelowZoom ? TileShape.HEX : TileShape.RAW;
        };
    }
}
