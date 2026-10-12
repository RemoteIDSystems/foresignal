package ai.foresignal.source.spi;

import java.util.Locale;

/**
 * What a stored image is for (table {@code media.role}, OpenAPI {@code Media.role}, prompt 043).
 */
public enum MediaRole {

    /** An original image, for example a SatDump composite. */
    IMAGE,

    /** A 256 px or 1024 px rendition of another media row ({@code variant_of}). */
    THUMBNAIL,

    /** A reprojected image (prompt 054). */
    PROJECTED,

    /** A static map, for example a notification attachment (prompt 055). */
    MAP;

    /** The wire and column value: the lower-case name. */
    public String wireValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * The role with {@code value} as its wire value.
     *
     * @throws IllegalArgumentException for an unknown value
     */
    public static MediaRole parse(String value) {
        if (value != null) {
            for (MediaRole role : values()) {
                if (role.wireValue().equals(value.trim().toLowerCase(Locale.ROOT))) {
                    return role;
                }
            }
        }
        throw new IllegalArgumentException("Unknown media role '" + value + "'.");
    }
}
