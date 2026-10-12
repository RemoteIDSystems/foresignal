package ai.foresignal.source.spi;

import java.util.Locale;

/**
 * The stored sample type of a raster product and of its {@code .fsg} frame files
 * (Weather-Imagery-Design §2.3).
 *
 * <p>The numeric {@link #code()} is the value written at byte 6 of the {@code .fsg} header, so the
 * order and codes of these constants are part of the file format and must never change.
 */
public enum RasterDtype {

    /** Unsigned 8-bit codes (MRMS reflectivity, categorical products). */
    U8(1, "u8", 1, 0, 255),

    /** Unsigned 16-bit codes (precipitation rate, GOES IR). */
    U16(2, "u16", 2, 0, 65_535),

    /** Signed 16-bit codes (model scalars with scale and offset). */
    I16(3, "i16", 2, -32_768, 32_767),

    /** 32-bit IEEE floats (wind fields); scale and offset are normally 1 and 0. */
    F32(4, "f32", 4, -Float.MAX_VALUE, Float.MAX_VALUE),

    /** Four bytes per pixel, red, green, blue, alpha (SatDump composites). */
    RGBA8(5, "rgba8", 4, 0, 0xFFFF_FFFFL);

    private final int code;
    private final String wireValue;
    private final int bytesPerSample;
    private final double min;
    private final double max;

    RasterDtype(int code, String wireValue, int bytesPerSample, double min, double max) {
        this.code = code;
        this.wireValue = wireValue;
        this.bytesPerSample = bytesPerSample;
        this.min = min;
        this.max = max;
    }

    /** The header code (1 u8, 2 u16, 3 i16, 4 f32, 5 rgba8). */
    public int code() {
        return code;
    }

    /** The lower-case name used in product specs and the API. */
    public String wireValue() {
        return wireValue;
    }

    /** Bytes one sample takes in a decoded band. */
    public int bytesPerSample() {
        return bytesPerSample;
    }

    /** The smallest raw code this type holds. */
    public double minCode() {
        return min;
    }

    /** The largest raw code this type holds. */
    public double maxCode() {
        return max;
    }

    /** True for the integer code types that quantization maps physical values onto. */
    public boolean isInteger() {
        return this == U8 || this == U16 || this == I16;
    }

    /**
     * The type with header code {@code code}.
     *
     * @throws IllegalArgumentException for an unknown code
     */
    public static RasterDtype fromCode(int code) {
        for (RasterDtype dtype : values()) {
            if (dtype.code == code) {
                return dtype;
            }
        }
        throw new IllegalArgumentException("Unknown raster dtype code " + code);
    }

    /**
     * The type named {@code value} ({@code u8}, {@code u16}, {@code i16}, {@code f32}, {@code rgba8}).
     *
     * @throws IllegalArgumentException for an unknown name
     */
    public static RasterDtype fromWire(String value) {
        if (value != null) {
            String wanted = value.strip().toLowerCase(Locale.ROOT);
            for (RasterDtype dtype : values()) {
                if (dtype.wireValue.equals(wanted)) {
                    return dtype;
                }
            }
        }
        throw new IllegalArgumentException("Unknown raster dtype '" + value + "'");
    }
}
