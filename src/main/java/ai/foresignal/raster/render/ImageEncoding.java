package ai.foresignal.raster.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/**
 * Encodes rendered rasters with the JDK's ImageIO (Weather-Imagery-Design §3: PNG-8 with an
 * {@link IndexColorModel}; the JDK PNG writer emits the palette alpha as a {@code tRNS} chunk). PNG
 * is never gzipped on the wire: it is already deflated.
 */
public final class ImageEncoding {

    /** The JPEG quality of {@code /image.jpg}. */
    static final float JPEG_QUALITY = 0.85f;

    private ImageEncoding() {}

    /** A palette image as PNG-8. */
    public static byte[] indexedPng(int width, int height, byte[] pixels, IndexColorModel colors) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, colors);
        byte[] target = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(pixels, 0, target, 0, pixels.length);
        return png(image);
    }

    /** An ARGB image as 32-bit PNG (rgba8 passthrough). */
    public static byte[] argbPng(int width, int height, int[] argb) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, argb, 0, width);
        return png(image);
    }

    /** {@code image} as PNG. */
    public static byte[] png(BufferedImage image) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(16 * 1024);
        try {
            if (!ImageIO.write(image, "png", out)) {
                throw new IllegalStateException("No PNG writer is available");
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not encode PNG", e);
        }
        return out.toByteArray();
    }

    /**
     * An ARGB image as JPEG, composited onto black (JPEG has no alpha; transparent areas are black).
     */
    public static byte[] jpeg(int width, int height, int[] argb) {
        BufferedImage source = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        source.setRGB(0, 0, width, height, argb, 0, width);
        BufferedImage rgb = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        try {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, width, height);
            g.drawImage(source, 0, 0, null);
        } finally {
            g.dispose();
        }
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No JPEG writer is available");
        }
        ImageWriter writer = writers.next();
        ByteArrayOutputStream out = new ByteArrayOutputStream(32 * 1024);
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(JPEG_QUALITY);
            writer.write(null, new IIOImage(rgb, null, null), param);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not encode JPEG", e);
        } finally {
            writer.dispose();
        }
        return out.toByteArray();
    }

    /** Expands palette pixels to ARGB. */
    public static int[] toArgb(byte[] pixels, int[] palette) {
        int[] out = new int[pixels.length];
        for (int k = 0; k < pixels.length; k++) {
            out[k] = palette[pixels[k] & 0xFF];
        }
        return out;
    }
}
