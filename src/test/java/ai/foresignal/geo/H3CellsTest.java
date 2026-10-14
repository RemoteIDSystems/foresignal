package ai.foresignal.geo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link H3Cells} (prompt 092): cell conversion, valid polygons and invalid-index handling. */
class H3CellsTest {

    @Test
    @DisplayName("a lat/lng maps to a valid cell whose polygon is a closed ring")
    void roundTrip() {
        long cell = H3Cells.latLngToCell(51.5, -0.12, 4);
        assertThat(H3Cells.isValid(cell)).isTrue();
        var polygon = H3Cells.cellToPolygon(cell);
        assertThat(polygon.getNumPoints()).isEqualTo(7);
        assertThat(polygon.getSRID()).isEqualTo(4326);
        assertThat(polygon.isValid()).isTrue();
        assertThat(polygon.getArea()).isPositive();
    }

    @Test
    @DisplayName("an address string converts, and an invalid index is refused")
    void address() throws Exception {
        long cell = H3Cells.latLngToCell(0, 0, 4);
        String address = com.uber.h3core.H3Core.newInstance().h3ToString(cell);
        assertThat(H3Cells.isValid(address)).isTrue();
        assertThat(H3Cells.cellToPolygon(address).getNumPoints()).isEqualTo(7);
        assertThat(H3Cells.isValid("not-an-h3")).isFalse();
        assertThatThrownBy(() -> H3Cells.cellToPolygon(-1L)).isInstanceOf(IllegalArgumentException.class);
    }
}
