package ai.foresignal.source.spi;

/**
 * An image refused by {@code ImageGuard} before any pixel was decoded (prompt 043): unknown or
 * mismatched format, a truncated header, BigTIFF, too many pixels, a side over 12,000 px, a file over
 * {@code foresignal.media.max-file-bytes}, or a multi-image TIFF.
 *
 * <p>The API answers it with {@code 400 VALIDATION_FAILED} and a {@code reason} property.
 */
public class ImageRejectedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** The file is larger than the configured cap. */
    public static final String FILE_TOO_LARGE = "file-too-large";

    /** The magic bytes match no supported format. */
    public static final String UNKNOWN_FORMAT = "unknown-format";

    /** A BigTIFF file. */
    public static final String BIGTIFF = "bigtiff";

    /** The declared content type disagrees with the sniffed format. */
    public static final String TYPE_MISMATCH = "content-type-mismatch";

    /** The header is truncated or cannot be read. */
    public static final String TRUNCATED = "truncated";

    /** A side is longer than 12,000 px. */
    public static final String SIDE_TOO_LONG = "side-too-long";

    /** More than 150 megapixels. */
    public static final String TOO_MANY_PIXELS = "too-many-pixels";

    /** A TIFF with more than one image, and the caller did not opt in. */
    public static final String MULTI_IMAGE = "multi-image";

    /** The pixels could not be decoded after the header passed. */
    public static final String UNREADABLE = "unreadable";

    /** The exact reason text for BigTIFF. */
    public static final String BIGTIFF_REASON = "BigTIFF not supported";

    private final String tag;

    /**
     * Creates the exception.
     *
     * @param tag the short machine reason (one of the constants; the metric's {@code reason} tag)
     * @param reason the human reason
     */
    public ImageRejectedException(String tag, String reason) {
        super(reason);
        this.tag = tag;
    }

    /** The human reason, for example {@value #BIGTIFF_REASON}. */
    public String reason() {
        return getMessage();
    }

    /** The short machine reason, one of this class's constants. */
    public String tag() {
        return tag;
    }
}
