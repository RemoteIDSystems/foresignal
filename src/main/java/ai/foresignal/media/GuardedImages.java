package ai.foresignal.media;

import ai.foresignal.source.spi.ImageRejectedException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Component;

/**
 * The only image decode entry point in Foresignal (prompt 043): every method runs
 * {@link ImageGuard#inspect} first and decodes with {@code ImageIO} only when the header passed, so no
 * caller can skip the guard. A test scans {@code src/main/java} for direct {@code ImageIO.read} calls
 * outside this class.
 *
 * <p>16-bit grayscale PNGs decode to {@link BufferedImage#TYPE_USHORT_GRAY}. For a TIFF, the first
 * image is decoded.
 */
@Component
public class GuardedImages {

    private final ImageGuard guard;

    /** Creates the decoder. */
    public GuardedImages(ImageGuard guard) {
        this.guard = guard;
    }

    /** The guard every decode runs first. */
    public ImageGuard guard() {
        return guard;
    }

    /**
     * Inspects and decodes a file of any accepted format.
     *
     * @throws ImageRejectedException when the guard refuses it or its pixels cannot be decoded
     */
    public BufferedImage decode(Path file) {
        return decode(file, null);
    }

    /**
     * Inspects and decodes a file whose declared type must agree with its content.
     *
     * @param declaredContentType the declared type, or null to accept whatever is sniffed
     * @throws ImageRejectedException when the guard refuses it or its pixels cannot be decoded
     */
    public BufferedImage decode(Path file, String declaredContentType) {
        guard.inspect(file, declaredContentType);
        try {
            return decoded(ImageIO.read(file.toFile()));
        } catch (IOException | RuntimeException e) {
            throw guard.reject(ImageRejectedException.UNREADABLE, "The image could not be decoded: " + e.getMessage());
        }
    }

    /**
     * Inspects and decodes bytes.
     *
     * @param declaredContentType the declared type, or null to accept whatever is sniffed
     * @throws ImageRejectedException when the guard refuses them or their pixels cannot be decoded
     */
    public BufferedImage decode(byte[] bytes, String declaredContentType) {
        guard.inspect(bytes, declaredContentType, false);
        try {
            return decoded(ImageIO.read(new ByteArrayInputStream(bytes)));
        } catch (IOException | RuntimeException e) {
            throw guard.reject(ImageRejectedException.UNREADABLE, "The image could not be decoded: " + e.getMessage());
        }
    }

    private BufferedImage decoded(BufferedImage image) {
        if (image == null) {
            throw guard.reject(ImageRejectedException.UNREADABLE, "No decoder produced an image.");
        }
        return image;
    }
}
