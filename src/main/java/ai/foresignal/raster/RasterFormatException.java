package ai.foresignal.raster;

/**
 * A {@code .fsg} frame file is not one this build can read: wrong magic, unknown version, non-zero
 * reserved bytes, a CRC32C mismatch, a short payload or an impossible header. Since prompt 046 also
 * thrown by the upstream decoders ({@code ai.foresignal.raster.decode}) for an unreadable GRIB2 or
 * NetCDF file, or one outside the directories they may open.
 */
public class RasterFormatException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Creates the exception. */
    public RasterFormatException(String message) {
        super(message);
    }

    /** Creates the exception with a cause. */
    public RasterFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
