package ai.foresignal.source.spi;

/**
 * An upstream file decoded, but a field or variable the adapter expects is missing
 * (Weather-Imagery-Design §2.2, Health row: "silent-format-change detection").
 *
 * <p>Thrown by a {@link RasterDecoder} or an adapter. The raster pipeline records the reason against
 * the product, so the source's health degrades with it, and writes <strong>no</strong> frame: an
 * empty frame would read as "clear weather" rather than "unknown". It is an
 * {@link UpstreamFormatException}, so the collector also records the fetch as failed.
 */
public class SilentFormatChangeException extends UpstreamFormatException {

    private static final long serialVersionUID = 1L;

    private final String productId;

    /**
     * Creates the exception.
     *
     * @param productId the product whose input changed, or null when not known yet
     * @param message what is missing, for example "variable MergedReflectivityQCComposite not found"
     */
    public SilentFormatChangeException(String productId, String message) {
        super(message, null);
        this.productId = productId;
    }

    /** The product whose input changed, or null. */
    public String productId() {
        return productId;
    }
}
