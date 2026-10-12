package ai.foresignal.raster.render;

import ai.foresignal.raster.ElgCodec;
import ai.foresignal.raster.FsgHeader;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterDtype;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Duration;

/** Specs and decoded frames for the prompt 045 render unit tests (no Spring, no database). */
final class RenderFixtures {

    /** WebMercator's latitude limit, as a grid edge. */
    static final double LAT = WebMercator.MAX_LAT;

    private RenderFixtures() {}

    /** An MRMS-like u8 reflectivity product: dBZ = (code − 66) / 2, codes 0 and 1 special. */
    static ProductSpec reflectivity() {
        return ProductSpec.builder("test-refl")
                .title("Reflectivity")
                .category("radar")
                .units("dBZ")
                .dtype(RasterDtype.U8)
                .scaleOffset(0.5, -33.0)
                .nodata(0.0)
                .code(0, "noCoverage")
                .code(1, "noEcho")
                .defaultStyle("nws-reflectivity@1")
                .temporal(ProductSpec.Temporal.observation(Duration.ofMinutes(2), Duration.ofMinutes(6)))
                .spatial(new ProductSpec.Spatial("CONUS", 8, 0, 7, null))
                .attribution(new ProductSpec.Attribution("Test", null, null, "US-PD", null, false))
                .build();
    }

    /** An i16 temperature product in K, scale 0.01, offset 273.15, bilinear. */
    static ProductSpec temperature(boolean bilinear) {
        return ProductSpec.builder("test-temp")
                .title("Temperature")
                .category("model")
                .units("K")
                .dtype(RasterDtype.I16)
                .scaleOffset(0.01, 273.15)
                .nodata(-32768.0)
                .defaultStyle("temperature@1")
                .resample(bilinear ? ProductSpec.RESAMPLE_BILINEAR : ProductSpec.RESAMPLE_NEAREST)
                .temporal(ProductSpec.Temporal.forecast(Duration.ofHours(1), Duration.ofHours(3)))
                .spatial(new ProductSpec.Spatial("global", 4, 0))
                .attribution(new ProductSpec.Attribution("Test", null, null, "US-PD", null, false))
                .build();
    }

    /** A decoded u8 frame. */
    static ElgCodec.Decoded u8(int nx, int ny, int... codes) {
        byte[] payload = new byte[codes.length];
        for (int i = 0; i < codes.length; i++) {
            payload[i] = (byte) codes[i];
        }
        return new ElgCodec.Decoded(new FsgHeader(RasterDtype.U8, FsgHeader.CODEC_NONE, nx, ny, 1, false, 0.5, -33.0,
                0.0), payload);
    }

    /** A decoded i16 frame. */
    static ElgCodec.Decoded i16(int nx, int ny, int... codes) {
        ByteBuffer b = ByteBuffer.allocate(codes.length * 2).order(ByteOrder.LITTLE_ENDIAN);
        for (int c : codes) {
            b.putShort((short) c);
        }
        return new ElgCodec.Decoded(new FsgHeader(RasterDtype.I16, FsgHeader.CODEC_NONE, nx, ny, 1, false, 0.01,
                273.15, -32768.0), b.array());
    }
}
