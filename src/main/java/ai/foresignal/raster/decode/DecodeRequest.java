package ai.foresignal.raster.decode;

import java.util.Objects;

/**
 * What to decode from a file (prompt 046).
 *
 * <p>GRIB2 fields are selected by parameter (discipline, category, number) and, optionally, by level
 * (type and value); never by message index, because an upstream that inserts a message would
 * otherwise silently shift every field. NetCDF variables are selected by name, with an optional
 * quality variable (GOES {@code DQF}) whose values above {@code maxQuality} mark a sample missing.
 *
 * @param productId the product the decode is for, carried into a {@code SilentFormatChangeException}
 * @param format {@link Format#GRIB2} or {@link Format#NETCDF}
 * @param label the field's upstream name for messages, for example {@code MergedReflectivityQCComposite}
 * @param discipline GRIB2 discipline (MRMS uses the local discipline 209), or null for NetCDF
 * @param category GRIB2 parameter category, or null
 * @param number GRIB2 parameter number, or null
 * @param levelType GRIB2 first fixed surface type (102 = altitude above MSL), or null for any level
 * @param levelValue GRIB2 first fixed surface value, or null for any value
 * @param levelLabel the level for messages, for example {@code 500 m}, or null
 * @param variable the NetCDF variable name, or null for GRIB2
 * @param qualityVariable the NetCDF quality-flag variable, or null
 * @param maxQuality the largest quality value still accepted; larger (or fill) marks a sample missing
 * @param headerOnly true to read the grid and times but no samples (probes, and the grid an adapter
 *     needs before it commits)
 * @param gridded false for a NetCDF variable with no projected grid (the self-test's NetCDF4 fixture)
 */
public record DecodeRequest(String productId, Format format, String label, Integer discipline, Integer category,
        Integer number, Integer levelType, Double levelValue, String levelLabel, String variable,
        String qualityVariable, Integer maxQuality, boolean headerOnly, boolean gridded) {

    /** The two input formats. */
    public enum Format {

        /** GRIB edition 2. */
        GRIB2,

        /** NetCDF4 / HDF5 (and NetCDF-3). */
        NETCDF
    }

    /** Checks that the selector fits the format. */
    public DecodeRequest {
        Objects.requireNonNull(format, "format");
        if (format == Format.GRIB2 && (discipline == null || category == null || number == null)) {
            throw new IllegalArgumentException("A GRIB2 request needs discipline, category and number");
        }
        if (format == Format.NETCDF && (variable == null || variable.isBlank())) {
            throw new IllegalArgumentException("A NetCDF request needs a variable name");
        }
        if (qualityVariable != null && maxQuality == null) {
            throw new IllegalArgumentException("A quality variable needs maxQuality");
        }
        if (label == null || label.isBlank()) {
            label = format == Format.NETCDF ? variable : discipline + "/" + category + "/" + number;
        }
    }

    /**
     * A GRIB2 field.
     *
     * @param levelType the first fixed surface type, or null for any level
     * @param levelValue its value, or null for any value
     */
    public static DecodeRequest grib2(String productId, String label, int discipline, int category, int number,
            Integer levelType, Double levelValue, String levelLabel) {
        return new DecodeRequest(productId, Format.GRIB2, label, discipline, category, number, levelType, levelValue,
                levelLabel, null, null, null, false, true);
    }

    /** A gridded NetCDF variable. */
    public static DecodeRequest netcdf(String productId, String variable) {
        return new DecodeRequest(productId, Format.NETCDF, variable, null, null, null, null, null, null, variable, null,
                null, false, true);
    }

    /** The same request, marking samples missing where {@code qualityVariable} exceeds {@code maxQuality}. */
    public DecodeRequest withQuality(String qualityVariable, int maxQuality) {
        return new DecodeRequest(productId, format, label, discipline, category, number, levelType, levelValue,
                levelLabel, variable, qualityVariable, maxQuality, headerOnly, gridded);
    }

    /** The same request, reading the grid and times only. */
    public DecodeRequest header() {
        return new DecodeRequest(productId, format, label, discipline, category, number, levelType, levelValue,
                levelLabel, variable, qualityVariable, maxQuality, true, gridded);
    }

    /** The same request for a NetCDF variable with no projected grid (values only). */
    public DecodeRequest valuesOnly() {
        return new DecodeRequest(productId, format, label, discipline, category, number, levelType, levelValue,
                levelLabel, variable, qualityVariable, maxQuality, headerOnly, false);
    }

    /** {@code label}, plus {@code at level <levelLabel>} when a level is selected; for messages. */
    public String describe() {
        return levelLabel == null ? label : label + " at level " + levelLabel;
    }
}
