package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** EDR coordinate parsing (prompt 108). */
class OgcControllerTest {

    @Test
    @DisplayName("POINT(lon lat) parses")
    void point() {
        assertThat(OgcController.parsePoint("POINT(-3.5 44.2)")).containsExactly(-3.5, 44.2);
        assertThatThrownBy(() -> OgcController.parsePoint("LINESTRING(0 0, 1 1)"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("a POLYGON becomes its bounding box")
    void polygon() {
        assertThat(OgcController.parsePolygonBox("POLYGON((0 0, 2 0, 2 3, 0 3, 0 0))"))
                .containsExactly(0, 0, 2, 3);
    }
}
