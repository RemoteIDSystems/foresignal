package ai.foresignal.raster;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.DecodedBands;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterDtype;
import ai.foresignal.testsupport.raster.RasterFixtures;
import ai.foresignal.testsupport.raster.SyntheticRasterAdapter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Quantization, statistics and footprints (prompt 042). */
class FrameEncoderTest {

    static ProductSpec spec(RasterDtype dtype, double scale, double offset, Double nodata) {
        return ProductSpec.builder("enc-test").category("model").dtype(dtype).scaleOffset(scale, offset).nodata(nodata)
                .temporal(ProductSpec.Temporal.observation(Duration.ofMinutes(5), null))
                .spatial(new ProductSpec.Spatial("global", 4, 0)).build();
    }

    @Test
    @DisplayName("u8 raw codes keep special codes out of min/max/mean and count them; the empty top row is clipped")
    void categoricalCodes() {
        EncodedFrame frame = FrameEncoder.encode(SyntheticRasterAdapter.observationSpec(), RasterFixtures.GRID,
                DecodedBands.codes(RasterFixtures.codes(0)), false);
        assertThat(frame.decoded()).hasSize(12);
        assertThat(frame.decoded()[5]).isEqualTo((byte) 66);
        assertThat(frame.header().codec()).isEqualTo(FsgHeader.CODEC_DEFLATE);
        assertThat(frame.stats()).containsEntry("min", -32.0).containsEntry("max", 30.0).containsEntry("mean", 5.6)
                .containsEntry("coveragePct", 66.67);
        assertThat(frame.stats().get("codeCounts")).isEqualTo(Map.of("0", 4L, "1", 3L));
        assertThat(frame.footprint().getEnvelopeInternal().getMaxY()).isEqualTo(32.0);
        assertThat(frame.footprint().getEnvelopeInternal().getMinY()).isEqualTo(28.0);
    }

    @Test
    @DisplayName("i16 physical values quantize with scale and offset, clamp to range, and NaN becomes nodata")
    void physicalI16() {
        ProductSpec spec = spec(RasterDtype.I16, 0.01, 100.0, -32768.0);
        GridDefinition grid = GridDefinition.latLon(2, 2, -10, -10, 10, 10);
        EncodedFrame frame = FrameEncoder.encode(spec, grid,
                DecodedBands.physical(new float[] {100.0f, 101.234f, Float.NaN, 1000.0f}), false);
        ByteBuffer b = ByteBuffer.wrap(frame.decoded()).order(ByteOrder.LITTLE_ENDIAN);
        assertThat(b.getShort(0)).isEqualTo((short) 0);
        assertThat(b.getShort(2)).isEqualTo((short) 123);
        assertThat(b.getShort(4)).isEqualTo((short) -32768);
        assertThat(b.getShort(6)).isEqualTo((short) 32767);
        assertThat(frame.stats()).containsEntry("coveragePct", 75.0);
        assertThat((Double) frame.stats().get("min")).isEqualTo(100.0);
    }

    @Test
    @DisplayName("f32 stores values as floats with NaN kept; rgba8 packs four bytes and covers by alpha")
    void floatsAndRgba() {
        EncodedFrame floats = FrameEncoder.encode(spec(RasterDtype.F32, 1, 0, null),
                GridDefinition.latLon(2, 1, 0, 0, 1, 1), DecodedBands.physical(new float[] {1.5f, Float.NaN}), false);
        ByteBuffer f = ByteBuffer.wrap(floats.decoded()).order(ByteOrder.LITTLE_ENDIAN);
        assertThat(f.getFloat(0)).isEqualTo(1.5f);
        assertThat(Float.isNaN(f.getFloat(4))).isTrue();
        assertThat(floats.stats()).containsEntry("coveragePct", 50.0);

        EncodedFrame rgba = FrameEncoder.encode(spec(RasterDtype.RGBA8, 1, 0, null),
                GridDefinition.latLon(2, 1, 0, 0, 1, 1), DecodedBands.codes(new int[] {0x11223344, 0x55667700}), false);
        assertThat(rgba.decoded()).containsExactly(0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x00);
        assertThat(rgba.stats()).containsEntry("coveragePct", 50.0).doesNotContainKey("min");
    }

    @Test
    @DisplayName("a grid crossing the antimeridian has a two-part footprint; no valid sample means no footprint")
    void footprints() {
        GridDefinition crossing = new GridDefinition(GridDefinition.LATLON, 2, 2, 170, -10, -170, 10, Map.of());
        EncodedFrame frame = FrameEncoder.encode(spec(RasterDtype.U8, 1, 0, 0.0), crossing,
                DecodedBands.codes(new int[] {5, 5, 0, 0}), false);
        assertThat(frame.footprint().getNumGeometries()).isEqualTo(2);
        EncodedFrame empty = FrameEncoder.encode(spec(RasterDtype.U8, 1, 0, 0.0), crossing,
                DecodedBands.codes(new int[] {0, 0, 0, 0}), false);
        assertThat(empty.footprint()).isNull();
        assertThat(empty.stats()).containsEntry("coveragePct", 0.0).doesNotContainKey("mean");
    }

    @Test
    @DisplayName("misfits are refused: band count, sample count, out-of-range codes, missing nodata, wrong form")
    void refusals() {
        ProductSpec u8 = spec(RasterDtype.U8, 1, 0, null);
        GridDefinition grid = GridDefinition.latLon(2, 1, 0, 0, 1, 1);
        assertThatThrownBy(() -> FrameEncoder.encode(u8, grid, DecodedBands.codes(new int[3]), false))
                .hasMessageContaining("samples");
        assertThatThrownBy(() -> FrameEncoder.encode(u8, grid, DecodedBands.codes(new int[2], new int[2]), false))
                .hasMessageContaining("band");
        assertThatThrownBy(() -> FrameEncoder.encode(u8, grid, DecodedBands.codes(new int[] {1, 300}), false))
                .hasMessageContaining("outside");
        assertThatThrownBy(() -> FrameEncoder.encode(u8, grid, DecodedBands.physical(new float[] {1, Float.NaN}), false))
                .hasMessageContaining("nodata");
        assertThatThrownBy(() -> FrameEncoder.encode(spec(RasterDtype.RGBA8, 1, 0, null), grid,
                DecodedBands.physical(new float[2]), false)).hasMessageContaining("rgba8");
        assertThatThrownBy(() -> FrameEncoder.encode(spec(RasterDtype.F32, 1, 0, null), grid,
                DecodedBands.codes(new int[2]), false)).hasMessageContaining("f32");
        assertThatThrownBy(() -> FrameEncoder.encode(new ProductSpec("x", "x", "raster"), grid,
                DecodedBands.codes(new int[2]), false)).hasMessageContaining("dtype");
    }

    @Test
    @DisplayName("the partial flag reaches the header")
    void partial() {
        assertThat(FrameEncoder.encode(SyntheticRasterAdapter.observationSpec(), RasterFixtures.GRID,
                DecodedBands.codes(RasterFixtures.codes(0)), true).header().partial()).isTrue();
    }
}
