package ai.foresignal.raster.model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * The {@code .i16} wind-field encoding (prompt 108; Weather-Imagery-Design §11): planar little-endian
 * signed int16 {@code u} then {@code v}, together with the per-component {@code scale}/{@code offset}
 * the manifest carries ({@code physical = code × scale + offset}). Values map linearly onto
 * {@code [−32767, 32767]} so a round trip stays within one quantisation step.
 *
 * <p>Input is Foresignal's planar little-endian Float32 ({@code u} then {@code v}). Non-finite samples
 * become the most negative code (the no-data value).
 */
public final class I16FieldEncoder {

    /** The code written for a non-finite sample. */
    public static final int NO_DATA = Short.MIN_VALUE;

    private I16FieldEncoder() {}

    /**
     * The int16 bytes and the quantisation the manifest advertises.
     *
     * @param bytes planar little-endian int16 {@code u} then {@code v}
     * @param uScale physical units per code, {@code u}
     * @param uOffset physical value of code 0, {@code u}
     * @param vScale physical units per code, {@code v}
     * @param vOffset physical value of code 0, {@code v}
     * @param width columns
     * @param height rows
     */
    public record Result(byte[] bytes, double uScale, double uOffset, double vScale, double vOffset, int width,
            int height) {}

    /** Encodes planar u/v Float32 as planar int16 with per-component scale and offset. */
    public static Result encode(byte[] raw, int nx, int ny) {
        if (raw.length != nx * ny * 8) {
            throw new IllegalArgumentException("Field data is " + raw.length + " bytes; a " + nx + "x" + ny
                    + " grid needs " + (nx * ny * 8));
        }
        ByteBuffer in = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
        int cells = nx * ny;
        double[] bounds = bounds(raw);
        double uScale = scale(bounds[0], bounds[1]);
        double uOffset = offset(bounds[0], uScale);
        double vScale = scale(bounds[2], bounds[3]);
        double vOffset = offset(bounds[2], vScale);
        ByteBuffer out = ByteBuffer.allocate(cells * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (int k = 0; k < cells; k++) {
            out.putShort((short) quantise(in.getFloat(k * 4), uScale, uOffset));
        }
        for (int k = 0; k < cells; k++) {
            out.putShort((short) quantise(in.getFloat((cells + k) * 4), vScale, vOffset));
        }
        return new Result(out.array(), uScale, uOffset, vScale, vOffset, nx, ny);
    }

    /** {@code {uMin, uMax, vMin, vMax}} over planar u/v Float32. */
    static double[] bounds(byte[] raw) {
        ByteBuffer in = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
        int cells = raw.length / 8;
        double uMin = Double.POSITIVE_INFINITY;
        double uMax = Double.NEGATIVE_INFINITY;
        double vMin = Double.POSITIVE_INFINITY;
        double vMax = Double.NEGATIVE_INFINITY;
        for (int k = 0; k < cells; k++) {
            float u = in.getFloat(k * 4);
            float v = in.getFloat((cells + k) * 4);
            if (Float.isFinite(u)) {
                uMin = Math.min(uMin, u);
                uMax = Math.max(uMax, u);
            }
            if (Float.isFinite(v)) {
                vMin = Math.min(vMin, v);
                vMax = Math.max(vMax, v);
            }
        }
        if (!(uMax >= uMin)) {
            uMin = 0;
            uMax = 1;
        }
        if (!(vMax >= vMin)) {
            vMin = 0;
            vMax = 1;
        }
        return new double[] {uMin, uMax, vMin, vMax};
    }

    /** Units per code so {@code [min, max]} maps onto the signed range {@code [−32767, 32767]}. */
    static double scale(double min, double max) {
        double span = max - min;
        return span > 0 ? span / 65534.0 : 1.0;
    }

    /** The physical value of code 0, placing {@code min} at {@code −32767}. */
    static double offset(double min, double scale) {
        return min + 32767.0 * scale;
    }

    /** The signed code for {@code value}; non-finite becomes {@link #NO_DATA}. */
    static int quantise(double value, double scale, double offset) {
        if (!Double.isFinite(value)) {
            return NO_DATA;
        }
        long q = Math.round((value - offset) / scale);
        return (int) Math.max(-32767, Math.min(32767, q));
    }
}
