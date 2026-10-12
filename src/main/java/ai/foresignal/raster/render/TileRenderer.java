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
 * the non-special neighbours; a pixel whose nearest cell holds a special code keeps that code. An
 * rgba8 frame draws its own colors (32-bit PNG, the {@code rgb@1} passthrough).
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

    /** The raw code of band 0 at {@code cell}. */
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
