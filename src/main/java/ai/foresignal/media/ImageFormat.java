package ai.foresignal.media;

import java.util.Locale;
import java.util.Optional;

/** The image formats the media store accepts (prompt 043). */
public enum ImageFormat {

    /** PNG, magic {@code 89 50 4E 47}. */
    PNG("image/png", "png"),

    /** JPEG, magic {@code FF D8 FF}. */
    JPEG("image/jpeg", "jpg"),

    /** Classic TIFF, magic {@code II*\0} or {@code MM\0*}. */
    TIFF("image/tiff", "tif");

    private final String contentType;
    private final String extension;

    ImageFormat(String contentType, String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    /** The canonical content type. */
    public String contentType() {
        return contentType;
    }

    /** The file extension of stored files. */
    public String extension() {
        return extension;
    }

    /** True when a browser can show it ({@code image/png} or {@code image/jpeg}). */
    public boolean browserSafe() {
        return this != TIFF;
    }

    /**
     * The format of a declared content type: parameters are ignored and case does not matter.
     *
     * @return the format, or empty for any other type
     */
    public static Optional<ImageFormat> ofContentType(String contentType) {
        if (contentType == null) {
            return Optional.empty();
        }
        String base = contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        for (ImageFormat format : values()) {
            if (format.contentType.equals(base)) {
                return Optional.of(format);
            }
        }
        return Optional.empty();
    }
}
