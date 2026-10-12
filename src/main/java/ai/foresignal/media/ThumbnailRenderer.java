package ai.foresignal.media;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
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
 * Renders a thumbnail from a decoded image (prompt 043, Weather-Imagery-Design §4.6): pure, with no
 * storage, so it is tested without a database.
 *
 * <ul>
 *   <li>The long edge becomes the target; the aspect ratio is kept. An image whose long edge is
 *       already at most the target is not upscaled.
 *   <li>Downscaling halves the image with bilinear interpolation while a half is still at least the
 *       target, then takes one final bicubic step to the exact size.
 *   <li>JPEG at quality {@value #JPEG_QUALITY}, or PNG when the source has alpha.
 * </ul>
 */
public final class ThumbnailRenderer {

    /** The JPEG quality of thumbnails. */
    public static final float JPEG_QUALITY = 0.85f;

    private ThumbnailRenderer() {}

    /**
     * A rendered thumbnail.
     *
     * @param bytes the encoded image
     * @param contentType {@code image/jpeg} or {@code image/png}
     * @param width the width, pixels
     * @param height the height, pixels
     */
    public record Rendition(byte[] bytes, String contentType, int width, int height) {}

    /** Renders {@code source} at long edge {@code target}, never upscaling. */
    public static Rendition render(BufferedImage source, int target) {
        boolean alpha = source.getColorModel().hasAlpha();
        BufferedImage scaled = scale(source, target, alpha);
        byte[] bytes = alpha ? png(scaled) : jpeg(scaled, JPEG_QUALITY);
        return new Rendition(bytes, alpha ? ImageFormat.PNG.contentType() : ImageFormat.JPEG.contentType(),
                scaled.getWidth(), scaled.getHeight());
    }

    /** The size {@code width x height} takes at long edge {@code target}: unchanged when already smaller. */
    public static int[] targetSize(int width, int height, int target) {
        int longEdge = Math.max(width, height);
        if (longEdge <= target) {
            return new int[] {width, height};
        }
        double ratio = (double) target / longEdge;
        return new int[] {Math.max(1, (int) Math.round(width * ratio)), Math.max(1, (int) Math.round(height * ratio))};
    }

    /**
     * Scales {@code source} to long edge {@code target}: progressive bilinear halving, then one
     * bicubic step. The result is {@code TYPE_INT_ARGB} with alpha, else {@code TYPE_INT_RGB}.
     */
    static BufferedImage scale(BufferedImage source, int target, boolean alpha) {
        int[] size = targetSize(source.getWidth(), source.getHeight(), target);
        BufferedImage current = resize(source, source.getWidth(), source.getHeight(), alpha,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        while (current.getWidth() / 2 >= size[0] && current.getHeight() / 2 >= size[1]
                && (current.getWidth() / 2 > size[0] || current.getHeight() / 2 > size[1])) {
            current = resize(current, current.getWidth() / 2, current.getHeight() / 2, alpha,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }
        if (current.getWidth() != size[0] || current.getHeight() != size[1]) {
            current = resize(current, size[0], size[1], alpha, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        }
        return current;
    }

    /**
     * Draws {@code source} into a new RGB or ARGB image of {@code width x height}. At the source's own
     * size this converts the pixel type (16-bit grayscale, indexed and 3-byte images become RGB).
     */
    private static BufferedImage resize(BufferedImage source, int width, int height, boolean alpha, Object hint) {
        BufferedImage out = new BufferedImage(width, height, alpha ? BufferedImage.TYPE_INT_ARGB
                : BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(source, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }
        return out;
    }

    /** Encodes PNG. */
    static byte[] png(BufferedImage image) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            if (!ImageIO.write(image, "png", out)) {
                throw new IllegalStateException("No PNG writer is available");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toByteArray();
    }

    /** Encodes JPEG at {@code quality}. */
    static byte[] jpeg(BufferedImage image, float quality) {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No JPEG writer is available");
        }
        ImageWriter writer = writers.next();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            writer.write(null, new IIOImage(image, null, null), param);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            writer.dispose();
        }
        return out.toByteArray();
    }
}
