package ai.foresignal.raster.geotiff;

/**
 * A GeoTIFF Foresignal does not read (prompt 054). The {@link #getMessage() message} is the health reason
 * the SatDump station reports, and {@link #kind()} says whether the optional GDAL path could help.
 */
public final class GeoTiffException extends Exception {

    private static final long serialVersionUID = 1L;

    /** Why the file was refused. */
    public enum Kind {

        /** A BigTIFF header (magic 43); GDAL can convert it. */
        BIGTIFF,

        /** A CRS other than EPSG:4326 or EPSG:3857; GDAL can warp it. */
        UNSUPPORTED_CRS,

        /** A rotated or sheared ModelTransformation; GDAL can warp it. */
        ROTATED,

        /** No usable georeferencing (no GeoKeys, or tie points without a pixel scale). */
        NO_GEOREFERENCE,

        /** Larger than the image guard allows, judged from the header before any pixel is decoded. */
        OVERSIZE,

        /** A layout this reader does not decode (bit depth, sample count). */
        UNSUPPORTED_LAYOUT,

        /** Not a TIFF, or truncated or corrupt. */
        MALFORMED
    }

    /** The health reason of a BigTIFF (Weather-Imagery-Design §4.3). */
    public static final String BIGTIFF_REASON = "BigTIFF not supported";

    private final Kind kind;
    private final int epsg;

    /**
     * Creates the exception.
     *
     * @param kind why
     * @param reason the health reason
     */
    public GeoTiffException(Kind kind, String reason) {
        this(kind, reason, 0, null);
    }

    /**
     * Creates the exception.
     *
     * @param kind why
     * @param reason the health reason
     * @param epsg the EPSG code found, or 0
     * @param cause the cause, or null
     */
    public GeoTiffException(Kind kind, String reason, int epsg, Throwable cause) {
        super(reason, cause);
        this.kind = kind;
        this.epsg = epsg;
    }

    /** Why the file was refused. */
    public Kind kind() {
        return kind;
    }

    /** The EPSG code of an unsupported CRS, or 0. */
    public int epsg() {
        return epsg;
    }

    /** True when the optional {@code gdalwarp} path can turn the file into a readable one. */
    public boolean gdalCanHelp() {
        return kind == Kind.BIGTIFF || kind == Kind.UNSUPPORTED_CRS || kind == Kind.ROTATED;
    }

    /** The health reason of an unsupported CRS. */
    public static String crsReason(int epsg) {
        return "GeoTIFF CRS EPSG:" + epsg + " not supported (build with WITH_GDAL=true)";
    }
}
