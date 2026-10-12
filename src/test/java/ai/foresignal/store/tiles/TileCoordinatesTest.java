package ai.foresignal.store.tiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link TileCoordinates}, {@link AggMode} and {@link TileProperties}. */
class TileCoordinatesTest {

    @Test
    @DisplayName("parses valid addresses and rejects out-of-range or malformed ones with the field name")
    void parse() {
        assertThat(TileCoordinates.parse("3", "7", "0")).isEqualTo(new TileCoordinates(3, 7, 0));
        assertThat(TileCoordinates.parse("0", "0", "0").tilesPerSide()).isEqualTo(1);
        assertField(() -> TileCoordinates.parse("17", "0", "0"), "z");
        assertField(() -> TileCoordinates.parse("-1", "0", "0"), "z");
        assertField(() -> TileCoordinates.parse("a", "0", "0"), "z");
        assertField(() -> TileCoordinates.parse("3", "8", "0"), "x");
        assertField(() -> TileCoordinates.parse("3", "0", "8"), "y");
        assertField(() -> TileCoordinates.parse("3", "-1", "0"), "x");
        assertThatThrownBy(() -> new TileCoordinates(2, 4, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TileCoordinates(17, 0, 0)).isInstanceOf(IllegalArgumentException.class);
    }

    private static void assertField(Runnable call, String field) {
        assertThatThrownBy(call::run)
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getErrors().getFirst().field()).isEqualTo(field));
    }

    @Test
    @DisplayName("tile width, cell size and the first square cell of a tile")
    void cellArithmetic() {
        TileCoordinates world = new TileCoordinates(0, 0, 0);
        assertThat(world.widthMeters()).isEqualTo(2 * TileCoordinates.HALF_WORLD_METERS);
        assertThat(world.cellSize(32)).isEqualTo(2 * TileCoordinates.HALF_WORLD_METERS / 32);
        assertThat(world.firstColumn(32)).isEqualTo(-16);
        assertThat(world.firstRow(32)).isEqualTo(-16);
        // z1: the north-east tile starts at the origin column and row.
        TileCoordinates northEast = new TileCoordinates(1, 1, 0);
        assertThat(northEast.firstColumn(32)).isZero();
        assertThat(northEast.firstRow(32)).isZero();
        TileCoordinates southWest = new TileCoordinates(1, 0, 1);
        assertThat(southWest.firstColumn(32)).isEqualTo(-32);
        assertThat(southWest.firstRow(32)).isEqualTo(-32);
        // Neighbors share an edge: the next tile's first column follows this tile's last.
        TileCoordinates a = new TileCoordinates(5, 10, 12);
        TileCoordinates b = new TileCoordinates(5, 11, 12);
        assertThat(b.firstColumn(32)).isEqualTo(a.firstColumn(32) + 32);
        TileCoordinates below = new TileCoordinates(5, 10, 13);
        assertThat(below.firstRow(32)).isEqualTo(a.firstRow(32) - 32);
        assertThat(a).hasToString("5/10/12");
    }

    @Test
    @DisplayName("agg: auto aggregates with hex below aggBelowZoom; none, grid and hex force their shape")
    void aggModes() {
        assertThat(AggMode.parse(null)).isEqualTo(AggMode.AUTO);
        assertThat(AggMode.parse("HEX")).isEqualTo(AggMode.HEX);
        assertThatThrownBy(() -> AggMode.parse("cluster")).isInstanceOf(ValidationException.class);
        assertThat(AggMode.AUTO.shapeFor(5, 6)).isEqualTo(TileShape.HEX);
        assertThat(AggMode.AUTO.shapeFor(6, 6)).isEqualTo(TileShape.RAW);
        assertThat(AggMode.AUTO.shapeFor(0, null)).isEqualTo(TileShape.RAW);
        assertThat(AggMode.NONE.shapeFor(0, 6)).isEqualTo(TileShape.RAW);
        assertThat(AggMode.GRID.shapeFor(16, null)).isEqualTo(TileShape.GRID);
        assertThat(AggMode.HEX.shapeFor(16, null)).isEqualTo(TileShape.HEX);
        assertThat(TileShape.RAW.aggValue()).isNull();
        assertThat(TileShape.GRID.aggValue()).isEqualTo("grid");
        assertThat(TileShape.HEX.aggregated()).isTrue();
    }

    @Test
    @DisplayName("tile properties: defaults and validation")
    void properties() {
        TileProperties defaults = TileProperties.defaults();
        assertThat(defaults.maxFeatures()).isEqualTo(50_000);
        assertThat(defaults.cellsPerTile()).isEqualTo(32);
        assertThat(defaults.aggMaxAge()).isEqualTo(Duration.ofSeconds(60));
        assertThat(defaults.rawMaxAge()).isEqualTo(Duration.ofSeconds(5));
        assertThat(new TileProperties(1, 2, null, null).rawMaxAge()).isEqualTo(Duration.ofSeconds(5));
        assertThatThrownBy(() -> new TileProperties(0, 32, null, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TileProperties(1, 31, null, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TileProperties(1, 512, null, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TileProperties(1, 32, Duration.ofSeconds(-1), null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(new TileResult(null, false, null).mvt()).isEmpty();
        assertThat(new TileResult(null, false, null).shape()).isEqualTo(TileShape.RAW);
    }
}
