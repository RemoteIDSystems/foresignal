package ai.foresignal.media;

/**
 * What {@link ImageGuard} read from an image header (prompt 043).
 *
 * @param format the sniffed format
 * @param width the width in pixels
 * @param height the height in pixels
 * @param bitDepth bits per sample of the first band (16 for a 16-bit grayscale PNG)
 * @param hasAlpha true when the image has an alpha channel or transparency
 */
public record ImageInfo(ImageFormat format, int width, int height, int bitDepth, boolean hasAlpha) {

    /** The long edge, pixels. */
    public int longEdge() {
        return Math.max(width, height);
    }
}
