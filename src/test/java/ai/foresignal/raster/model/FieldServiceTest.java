package ai.foresignal.raster.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.source.adapters.gfs.GfsAdapter;
import ai.foresignal.source.adapters.hrrr.HrrrAdapter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Field windows, cuts and parameters (prompt 051), without Spring. */
class FieldServiceTest {

    @Test
    @DisplayName("no bbox is the whole grid; a box crossing 0° wraps; a box crossing the antimeridian wraps too")
    void windows() {
        FieldService.Window whole = FieldService.window(null, 360, 181, 1, 1);
        assertThat(whole.whole()).isTrue();
        assertThat(whole.boxText()).isNull();
        assertThat(whole.byteLength()).isEqualTo(521_280);
        FieldService.Window seam = FieldService.window(new double[] {-10, 30, 10, 40}, 360, 181, 1, 1);
        assertThat(seam.col0()).isEqualTo(350);
        assertThat(seam.cols()).isEqualTo(21);
        assertThat(seam.rows()).isEqualTo(11);
        assertThat(seam.lo1()).isEqualTo(350.0);
        assertThat(seam.boxText()).isEqualTo("-10,30,10,40");
        FieldService.Window pacific = FieldService.window(new double[] {170, -10, -170, 10}, 1440, 721, 0.25, 0.25);
        assertThat(pacific.col0()).isEqualTo(680);
        assertThat(pacific.cols()).isEqualTo(81);
        assertThat(pacific.boxText()).isEqualTo("170,-10,-170,10");
        FieldService.Window world = FieldService.window(new double[] {-180, -90, 180, 90}, 360, 181, 1, 1);
        assertThat(world.whole()).isTrue();
        FieldService.Window band = FieldService.window(new double[] {-180, 0, 180, 10}, 360, 181, 1, 1);
        assertThat(band.cols()).isEqualTo(360);
        assertThat(band.col0()).isZero();
        assertThat(band.whole()).isFalse();
        assertThat(FieldService.window(FieldService.parseBox(band.boxText()), 360, 181, 1, 1)).isEqualTo(band);
    }

    @Test
    @DisplayName("a cut keeps planar u then v and wraps columns modulo nx")
    void cut() {
        int nx = 4;
        int ny = 3;
        ByteBuffer b = ByteBuffer.allocate(nx * ny * 8).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < nx * ny; i++) {
            b.putFloat(i);
        }
        for (int i = 0; i < nx * ny; i++) {
            b.putFloat(100 + i);
        }
        FieldService.Window w = new FieldService.Window(3, 1, 2, 2, 90, 90, false);
        ByteBuffer out = ByteBuffer.wrap(FieldService.cut(b.array(), nx, ny, w)).order(ByteOrder.LITTLE_ENDIAN);
        float[] values = new float[8];
        out.asFloatBuffer().get(values);
        assertThat(values).containsExactly(7, 4, 11, 8, 107, 104, 111, 108);
        assertThatThrownBy(() -> FieldService.cut(new byte[8], nx, ny, w)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("res is 1 (default) or 0.25; bbox is four finite WGS84 numbers")
    void parameters() {
        assertThat(FieldService.resolution(null)).isEqualTo("1");
        assertThat(FieldService.resolution("1.0")).isEqualTo("1");
        assertThat(FieldService.resolution(".25")).isEqualTo("0.25");
        assertThatThrownBy(() -> FieldService.resolution("0.5")).isInstanceOf(ValidationException.class);
        assertThat(FieldService.parseBox(" ")).isNull();
        assertThat(FieldService.parseBox("170,-10,-170,10")).containsExactly(170, -10, -170, 10);
        for (String bad : List.of("1,2,3", "a,b,c,d", "0,10,5,5", "-181,0,10,10", "0,0,NaN,1", "5,0,5,1")) {
            assertThatThrownBy(() -> FieldService.parseBox(bad)).as(bad).isInstanceOf(ValidationException.class);
        }
        assertThat(FieldService.window(new double[] {0, 89.9, 1, 90}, 360, 181, 1, 1).rows()).isEqualTo(2);
    }

    @Test
    @DisplayName("fields exist for gfs only: kind=field, two bands, global coverage")
    void hasFields() {
        ModelRunIngest ingest = new ModelRunIngest(null, null);
        List<ai.foresignal.source.spi.ProductSpec> gfs = new GfsAdapter(ingest, "").descriptor().products();
        assertThat(FieldService.hasFields(gfs.get(0))).isTrue();
        assertThat(FieldService.hasFields(gfs.get(1))).isFalse();
        assertThat(FieldService.hasFields(new HrrrAdapter(ingest, "large", "").descriptor().products().get(0)))
                .as("HRRR has wind but no global field").isFalse();
        assertThatThrownBy(() -> FieldService.requireFields(gfs.get(2))).isInstanceOf(ValidationException.class);
        FieldService.requireFields(gfs.get(0));
    }
}
