package ai.foresignal.source.spi;

import java.util.Locale;

/**
 * What a stored file is for (table {@code media.role}, OpenAPI {@code Media.role}, prompt 043; prompt 141 adds
 * {@link #DOCUMENT} and {@link #AUDIO}, the roles that are not images).
 */
public enum MediaRole {

    /** An original image, for example a SatDump composite. */
    IMAGE,

    /** A 256 px or 1024 px rendition of another media row ({@code variant_of}). */
    THUMBNAIL,

    /** A reprojected image (prompt 054). */
    PROJECTED,

    /** A static map, for example a notification attachment (prompt 055). */
    MAP,

    /**
     * A generated document that is not an image, for example the CZML of a track the MCP {@code get_track} tool
     * links to (prompt 141, {@code ai.foresignal.media.MediaDocuments}); stored with an expiry, never thumbnailed.
     */
    DOCUMENT,

    /** Synthesized speech of a briefing run (prompt 141): MP3 or Ogg Opus, never passed to the image guard. */
    AUDIO;

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
