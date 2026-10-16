package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.raster.sample.SampleStatus;
import ai.foresignal.raster.sample.SampleValue;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterDtype;
import java.time.Duration;
import java.util.List;
import java.util.Map;
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

    @Test
    @DisplayName("an EDR point with no value answers [null] for shape [1,1,1], never []")
    void pointWithoutValue() {
        ProductSpec spec = ProductSpec.builder("t").title("T").category("radar").units("dBZ").dtype(RasterDtype.U8)
                .scaleOffset(1.0, 0.0)
                .temporal(ProductSpec.Temporal.observation(Duration.ofMinutes(2), Duration.ofMinutes(6)))
                .spatial(new ProductSpec.Spatial("CONUS", 6, 0))
                .attribution(new ProductSpec.Attribution("Test", null, null, "US-PD", null, false))
                .build();
        SampleValue none = SampleValue.unknown(spec, SampleStatus.NO_DATA, "no data", false);
        Map<String, Object> coverage = OgcController.coveragePoint(spec, new double[] {1, 2}, none);
        Map<?, ?> range = (Map<?, ?>) ((Map<?, ?>) coverage.get("ranges")).get("value");
        assertThat(range.get("shape")).isEqualTo(List.of(1, 1, 1));
        assertThat((List<?>) range.get("values")).hasSize(1).containsNull();
    }
}
