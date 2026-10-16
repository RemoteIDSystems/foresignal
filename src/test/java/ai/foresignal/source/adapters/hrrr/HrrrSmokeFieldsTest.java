package ai.foresignal.source.adapters.hrrr;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.raster.model.ModelRunIngest;
import ai.foresignal.source.spi.ProductSpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The HRRR column-smoke encoding (prompt 106 task 3, post-review fix): COLMD is stored as µg/m² in i16,
 * and the scale and offset must reach dense-plume values (several g/m²) without saturating, while keeping
 * the quiet-day values (a 2026-10-15 f01 field peaked at 7.1 × 10⁻⁵ kg/m² = 71,000 µg/m²) resolvable.
 */
class HrrrSmokeFieldsTest {

    static ProductSpec column() {
        return new HrrrAdapter(new ModelRunIngest(null, null), "small", "").descriptor().products().stream()
                .filter(p -> p.id().equals(HrrrAdapter.SMOKE_COLUMN)).findFirst().orElseThrow();
    }

    /** The i16 code of {@code v}, as {@code FrameEncoder} computes it before clamping. */
    static double code(ProductSpec spec, double v) {
        return Math.rint((v - spec.offset()) / spec.scale());
    }

    @Test
    @DisplayName("0 µg/m² is a valid code, not the -32768 nodata")
    void zeroIsValid() {
        ProductSpec spec = column();
        assertThat(code(spec, 0)).isGreaterThan(-32768).isLessThanOrEqualTo(32767);
    }

    @Test
    @DisplayName("10 g/m² of column smoke is stored without saturating i16")
    void densePlumeFits() {
        ProductSpec spec = column();
        assertThat(code(spec, 10_000_000)).isLessThan(32767);
        assertThat(spec.offset() + 32767 * spec.scale()).isGreaterThan(50_000_000);
    }

    @Test
    @DisplayName("the quiet-day peak of 71,000 µg/m² keeps better than 2% resolution")
    void quietDayResolved() {
        ProductSpec spec = column();
        double v = 71_000;
        double back = code(spec, v) * spec.scale() + spec.offset();
        assertThat(Math.abs(back - v) / v).isLessThan(0.02);
    }
}
