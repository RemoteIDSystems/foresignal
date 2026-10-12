package ai.foresignal.source.spi;

import java.util.List;

/**
 * Decoded band arrays of one frame, row-major, rows north to south.
 *
 * <p>Exactly one of the two forms is set:
 *
 * <ul>
 *   <li>{@code physical}: physical values; {@code NaN} marks a missing sample. The raster pipeline
 *       quantizes them with the product's scale, offset and nodata code.
 *   <li>{@code codes}: raw codes already in the product's code space (for products with special
 *       codes, such as MRMS reflectivity's no-coverage and no-echo). For {@link RasterDtype#RGBA8}
 *       each int is one pixel packed as {@code 0xRRGGBBAA}.
 * </ul>
 *
 * @param physical one array per band, or null
 * @param codes one array per band, or null
 */
public record DecodedBands(List<float[]> physical, List<int[]> codes) {

    /** Checks that exactly one form is present and non-empty. */
    public DecodedBands {
        if ((physical == null) == (codes == null)) {
            throw new IllegalArgumentException("Exactly one of physical values or raw codes is required");
        }
        physical = physical == null ? null : List.copyOf(physical);
        codes = codes == null ? null : List.copyOf(codes);
        if ((physical != null && physical.isEmpty()) || (codes != null && codes.isEmpty())) {
            throw new IllegalArgumentException("At least one band is required");
        }
    }

    /** Bands of physical values. */
    public static DecodedBands physical(float[]... bands) {
        return new DecodedBands(List.of(bands), null);
    }

    /** Bands of raw codes. */
    public static DecodedBands codes(int[]... bands) {
        return new DecodedBands(null, List.of(bands));
    }

    /** The number of bands. */
    public int bandCount() {
        return physical != null ? physical.size() : codes.size();
    }

    /** The number of samples in band {@code band}. */
    public int length(int band) {
        return physical != null ? physical.get(band).length : codes.get(band).length;
    }
}
