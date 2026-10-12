package ai.foresignal.raster.decode;

import ai.foresignal.source.spi.GridDefinition;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One decoded field (prompt 046): its grid, its samples row-major with rows north to south, and its
 * times.
 *
 * <p>At most one of {@code floatData} and {@code shortData} is set; a header-only decode
 * ({@link DecodeRequest#headerOnly()}) sets neither. Missing samples are {@code NaN} in
 * {@code floatData}.
 *
 * @param gridDefinition the native grid, or null for a gridless NetCDF variable
 *     ({@link DecodeRequest#gridded()} false)
 * @param nx columns (the sample count for a gridless variable)
 * @param ny rows (1 for a gridless variable)
 * @param floatData the samples as physical values, or null
 * @param shortData raw 16-bit samples, or null
 * @param validTime the valid time: reference plus forecast time for GRIB2, {@code time_coverage_start}
 *     for NetCDF; null when the file carries none
 * @param referenceTime the GRIB2 reference time, or null
 * @param metadata what the decoder found, for probes and logs (template numbers, levels, attributes)
 */
public record DecodedGrid(GridDefinition gridDefinition, int nx, int ny, float[] floatData, short[] shortData,
        Instant validTime, Instant referenceTime, Map<String, Object> metadata) {

    /** Checks the dimensions and copies the metadata. */
    public DecodedGrid {
        if (nx <= 0 || ny <= 0) {
            throw new IllegalArgumentException("A decoded grid needs positive dimensions, was " + nx + "x" + ny);
        }
        if (floatData != null && shortData != null) {
            throw new IllegalArgumentException("A decoded grid carries float or short samples, not both");
        }
        long cells = (long) nx * ny;
        int length = floatData != null ? floatData.length : shortData != null ? shortData.length : -1;
        if (length >= 0 && length != cells) {
            throw new IllegalArgumentException("A " + nx + "x" + ny + " grid needs " + cells + " samples, got " + length);
        }
        metadata = metadata == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(metadata));
    }

    /** True when the samples were read (not a header-only decode). */
    public boolean hasData() {
        return floatData != null || shortData != null;
    }

    /** The number of samples. */
    public int cells() {
        return Math.multiplyExact(nx, ny);
    }

    /** Sample {@code i} as a float ({@code NaN} when missing); requires data. */
    public float value(int i) {
        if (floatData != null) {
            return floatData[i];
        }
        if (shortData != null) {
            return shortData[i];
        }
        throw new IllegalStateException("A header-only decode carries no samples");
    }
}
