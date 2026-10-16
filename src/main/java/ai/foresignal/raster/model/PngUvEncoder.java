package ai.foresignal.raster.model;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.imageio.ImageIO;

/**
 * The {@code png-uv} wind-field encoding (prompt 106; Weather-Imagery-Design §6.4): an 8-bit RGB PNG
 * with {@code R = u} and {@code G = v} linearly scaled over {@code [uMin, uMax]} and {@code [vMin, vMax]}
 * and {@code B = 0}. This is the format WebGL wind-particle layers expect; the companion manifest
 * carries the scale bounds.
 *
 * <p>Input is Foresignal's planar little-endian Float32 ({@code u} then {@code v}); the output is a
 * content-addressed PNG served like a tile. Non-finite samples are mapped to 0 in their channel.
 */
public final class PngUvEncoder {

    private PngUvEncoder() {}

    /**
     * The PNG and its scale bounds.
     *
     * @param png the PNG bytes
     * @param uMin the u value mapped to 0
     * @param uMax the u value mapped to 255
     * @param vMin the v value mapped to 0
     * @param vMax the v value mapped to 255
     * @param width columns
     * @param height rows
     */
    public record Result(byte[] png, double uMin, double uMax, double vMin, double vMax, int width, int height) {}

    /** Encodes planar u/v Float32 as an RGB PNG. */
    public static Result encode(byte[] raw, int nx, int ny) {
        if (raw.length != nx * ny * 8) {
            throw new IllegalArgumentException("Field data is " + raw.length + " bytes; a " + nx + "x" + ny
                    + " grid needs " + (nx * ny * 8));
        }
        ByteBuffer in = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
        int cells = nx * ny;
        double[] u = new double[cells];
        double[] v = new double[cells];
        double uMin = Double.POSITIVE_INFINITY;
        double uMax = Double.NEGATIVE_INFINITY;
        double vMin = Double.POSITIVE_INFINITY;
        double vMax = Double.NEGATIVE_INFINITY;
        for (int k = 0; k < cells; k++) {
            double uk = in.getFloat(k * 4);
            double vk = in.getFloat((cells + k) * 4);
            u[k] = uk;
            v[k] = vk;
            if (Double.isFinite(uk)) {
                uMin = Math.min(uMin, uk);
                uMax = Math.max(uMax, uk);
            }
            if (Double.isFinite(vk)) {
                vMin = Math.min(vMin, vk);
                vMax = Math.max(vMax, vk);
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
        BufferedImage image = new BufferedImage(nx, ny, BufferedImage.TYPE_INT_RGB);
        for (int row = 0; row < ny; row++) {
            for (int col = 0; col < nx; col++) {
                int k = row * nx + col;
                int r = channel(u[k], uMin, uMax);
                int g = channel(v[k], vMin, vMax);
                image.setRGB(col, row, (r << 16) | (g << 8));
            }
        }
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ImageIO.write(image, "png", bytes);
            return new Result(bytes.toByteArray(), uMin, uMax, vMin, vMax, nx, ny);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not encode the png-uv field: " + e.getMessage(), e);
        }
    }

    /** One channel value, 0..255. */
    static int channel(double value, double min, double max) {
        if (!Double.isFinite(value) || max <= min) {
            return 0;
        }
        double scaled = (value - min) / (max - min) * 255.0;
        return (int) Math.max(0, Math.min(255, Math.round(scaled)));
    }
}
