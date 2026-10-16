package ai.foresignal.raster.render;

import ai.foresignal.raster.ElgCodec;
import ai.foresignal.raster.FsgHeader;
import ai.foresignal.source.spi.RasterDtype;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Draws a decoded frame through an {@link IndexMap} and a {@link CompiledStyle} (prompt 045;
 * Weather-Imagery-Design §0.1, §6.3): gather the raw codes of band 0, map them to palette indices,
 * encode PNG-8. Pure and thread-safe; the caches and the queue live elsewhere.
 *
 * <p>Nearest sampling reads one code per pixel through the code table (u8, u16, i16) or the value
 * ramp (f32). Bilinear sampling (spec {@code resample: bilinear}) interpolates the physical values of
 * the non-special neighbors; a pixel whose nearest cell holds a special code keeps that code. An
 * rgba8 frame draws its own colors (32-bit PNG, the {@code rgb@1} passthrough). A
 * {@code derive: speed} style ({@code wind-speed}) on a frame with two or more bands draws the
 * magnitude of bands 0 and 1 (prompt 051): the speed raster of a u/v wind product.
 */
public final class TileRenderer {

    private TileRenderer() {}

    /**
     * A drawn raster before encoding.
     *
     * @param width pixels
     * @param height pixels
     * @param pixels palette indices (null for passthrough)
     * @param argb colors (passthrough only)
     * @param style the style used
     * @param empty true when every pixel is transparent
     */
    public record Drawn(int width, int height, byte[] pixels, int[] argb, CompiledStyle style, boolean empty) {

        /** As PNG (PNG-8 for a palette, 32-bit for passthrough). */
        public byte[] png() {
            return pixels != null ? ImageEncoding.indexedPng(width, height, pixels, style.colorModel())
                    : ImageEncoding.argbPng(width, height, argb);
        }

        /** As JPEG on black. */
        public byte[] jpeg() {
            int[] colors = pixels != null ? ImageEncoding.toArgb(pixels, style.palette()) : argb;
            return ImageEncoding.jpeg(width, height, colors);
        }

        /** As WebP (lossless for a palette, quality 0.8 for passthrough), prompt 108. */
        public byte[] webp() {
            if (pixels != null) {
                return ImageEncoding.webp(ImageEncoding.indexed(width, height, pixels, style.colorModel()), 1.0f);
            }
            return ImageEncoding.webp(ImageEncoding.argb(width, height, argb), 0.8f);
        }

        /** As a {@link java.awt.image.BufferedImage} (palette or ARGB), prompt 108 GIF loops. */
        public java.awt.image.BufferedImage image() {
            return pixels != null ? ImageEncoding.indexed(width, height, pixels, style.colorModel())
                    : ImageEncoding.argb(width, height, argb);
        }
    }

    /** Draws {@code frame} through {@code map}. */
    public static Drawn draw(ElgCodec.Decoded frame, CompiledStyle style, IndexMap map) {
        int w = map.width();
        int h = map.height();
        if (map.isEmpty()) {
            return new Drawn(w, h, style.passthrough() ? null : new byte[w * h],
                    style.passthrough() ? new int[w * h] : null, style, true);
        }
        FsgHeader header = frame.header();
        ByteBuffer data = ByteBuffer.wrap(frame.payload()).order(ByteOrder.LITTLE_ENDIAN);
        int[] nearest = map.nearest();
        if (style.passthrough() && header.dtype() == RasterDtype.RGBA8) {
            int[] argb = new int[w * h];
            boolean empty = true;
            for (int p = 0; p < argb.length; p++) {
                int cell = nearest[p];
                if (cell < 0) {
                    continue;
                }
                int o = cell * 4;
                int r = data.get(o) & 0xFF;
                int g = data.get(o + 1) & 0xFF;
                int b = data.get(o + 2) & 0xFF;
                int a = data.get(o + 3) & 0xFF;
                argb[p] = a << 24 | r << 16 | g << 8 | b;
                empty &= a == 0;
            }
            return new Drawn(w, h, null, argb, style, empty);
        }
        if (style.style().derivesSpeed() && header.bands() >= 2 && header.dtype() != RasterDtype.RGBA8) {
            return drawSpeed(data, header, style, map);
        }
        byte[] pixels = new byte[w * h];
        int[] palette = style.palette();
        byte[] table = style.table();
        RasterDtype dtype = header.dtype();
        boolean empty = true;
        boolean bilinear = map.bilinear() && dtype != RasterDtype.RGBA8;
        int[] corners = map.corners();
        float[] weights = map.weights();
        for (int p = 0; p < pixels.length; p++) {
            int cell = nearest[p];
            if (cell < 0) {
                continue;
            }
            int index;
            if (bilinear) {
                double code = code(data, dtype, cell);
                if (style.isSpecial(code)) {
                    index = style.indexOfCode(code);
                } else {
                    double sum = 0;
                    double weight = 0;
                    for (int k = 0; k < 4; k++) {
                        int c = corners[p * 4 + k];
                        float wk = weights[p * 4 + k];
                        if (c < 0 || wk <= 0) {
                            continue;
                        }
                        double ck = code(data, dtype, c);
                        if (style.isSpecial(ck)) {
                            continue;
                        }
                        sum += wk * style.spec().physical(ck);
                        weight += wk;
                    }
                    index = weight > 0 ? style.indexOfValue(sum / weight) : style.indexOfCode(code);
                }
            } else if (table != null) {
                index = switch (dtype) {
                    case U8 -> table[data.get(cell) & 0xFF] & 0xFF;
                    case U16 -> table[data.getShort(cell * 2) & 0xFFFF] & 0xFF;
                    case I16 -> table[data.getShort(cell * 2) + 32_768] & 0xFF;
                    default -> style.indexOfCode(code(data, dtype, cell));
                };
            } else {
                index = style.indexOfCode(code(data, dtype, cell));
            }
            pixels[p] = (byte) index;
            if (palette[index] >>> 24 != 0) {
                empty = false;
            }
        }
        return new Drawn(w, h, pixels, null, style, empty);
    }

    /**
     * Draws the vector magnitude {@code √(u² + v²)} of bands 0 and 1 (a {@code derive: speed} style,
     * prompt 051): nearest sampling takes one cell's components; bilinear sampling interpolates
     * {@code u} and {@code v} separately over the corners where neither is special, then takes the
     * magnitude. A cell where either component is special keeps that special code's color.
     */
    static Drawn drawSpeed(ByteBuffer data, FsgHeader header, CompiledStyle style, IndexMap map) {
        int w = map.width();
        int h = map.height();
        int cells = header.nx() * header.ny();
        RasterDtype dtype = header.dtype();
        byte[] pixels = new byte[w * h];
        int[] palette = style.palette();
        boolean empty = true;
        int[] nearest = map.nearest();
        int[] corners = map.corners();
        float[] weights = map.weights();
        boolean bilinear = map.bilinear();
        for (int p = 0; p < pixels.length; p++) {
            int cell = nearest[p];
            if (cell < 0) {
                continue;
            }
            double cu = code(data, dtype, cell);
            double cv = code(data, dtype, cell + cells);
            int index;
            if (style.isSpecial(cu) || style.isSpecial(cv)) {
                index = style.indexOfCode(style.isSpecial(cu) ? cu : cv);
            } else if (bilinear) {
                double su = 0;
                double sv = 0;
                double weight = 0;
                for (int k = 0; k < 4; k++) {
                    int c = corners[p * 4 + k];
                    float wk = weights[p * 4 + k];
                    if (c < 0 || wk <= 0) {
                        continue;
                    }
                    double ku = code(data, dtype, c);
                    double kv = code(data, dtype, c + cells);
                    if (style.isSpecial(ku) || style.isSpecial(kv)) {
                        continue;
                    }
                    su += wk * style.spec().physical(ku);
                    sv += wk * style.spec().physical(kv);
                    weight += wk;
                }
                index = weight > 0 ? style.indexOfValue(Math.hypot(su / weight, sv / weight))
                        : style.indexOfValue(Math.hypot(style.spec().physical(cu), style.spec().physical(cv)));
            } else {
                index = style.indexOfValue(Math.hypot(style.spec().physical(cu), style.spec().physical(cv)));
            }
            pixels[p] = (byte) index;
            if (palette[index] >>> 24 != 0) {
                empty = false;
            }
        }
        return new Drawn(w, h, pixels, null, style, empty);
    }

    /**
     * Gathers band 0's raw codes through {@code map}, one per pixel (prompt 108 data tiles):
     * {@link DataTileEncoder#NO_DATA} where the pixel has no cell. Sampling is always {@code nearest}.
     *
     * @return {@code width × height} codes, row-major north to south
     */
    public static int[] codes(ElgCodec.Decoded frame, IndexMap map) {
        int w = map.width();
        int h = map.height();
        int[] out = new int[w * h];
        RasterDtype dtype = frame.header().dtype();
        ByteBuffer data = ByteBuffer.wrap(frame.payload()).order(ByteOrder.LITTLE_ENDIAN);
        int[] nearest = map.nearest();
        for (int p = 0; p < out.length; p++) {
            int cell = nearest[p];
            if (cell < 0) {
                out[p] = DataTileEncoder.NO_DATA;
                continue;
            }
            double code = code(data, dtype, cell);
            out[p] = Double.isFinite(code) ? (int) code : DataTileEncoder.NO_DATA;
        }
        return out;
    }

    /** The raw code of band 0 at {@code cell} (a later band's sample at {@code cell + band × cells}). */
    static double code(ByteBuffer data, RasterDtype dtype, int cell) {
        return switch (dtype) {
            case U8 -> data.get(cell) & 0xFF;
            case U16 -> data.getShort(cell * 2) & 0xFFFF;
            case I16 -> data.getShort(cell * 2);
            case F32 -> data.getFloat(cell * 4);
            case RGBA8 -> Integer.toUnsignedLong(data.getInt(cell * 4));
        };
    }
}
