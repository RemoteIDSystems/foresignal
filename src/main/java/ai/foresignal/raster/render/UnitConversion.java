package ai.foresignal.raster.render;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * Legend unit conversion (prompt 045; Weather-Imagery-Design §6.3): {@code mm/h ↔ in/h},
 * {@code K → °C} (metric) or {@code °F} (imperial), {@code m/s ↔ mph}, {@code mm ↔ in}. Units the
 * table does not know (dBZ, percentages, concentrations) are shown unchanged.
 *
 * @param units the converted units, as stored ({@code degC}, {@code in/h}, …), or null
 * @param label the display label ({@code °C}, {@code in/h}, …), or null
 * @param scale multiply a value by this …
 * @param offset … then add this
 */
public record UnitConversion(String units, String label, double scale, double offset) {

    /** The identity for {@code units}. */
    public static UnitConversion identity(String units) {
        return new UnitConversion(units, units, 1, 0);
    }

    /**
     * The conversion of values in {@code units} to the requested system.
     *
     * @param imperial {@code units=imperial}
     */
    public static UnitConversion of(String units, boolean imperial) {
        if (units == null) {
            return identity(null);
        }
        String u = units.strip().toLowerCase(Locale.ROOT);
        return switch (u) {
            case "k" -> imperial ? new UnitConversion("degF", "°F", 1.8, -273.15 * 1.8 + 32)
                    : new UnitConversion("degC", "°C", 1, -273.15);
            case "mm/h" -> imperial ? new UnitConversion("in/h", "in/h", 1 / 25.4, 0) : identity("mm/h");
            case "mm" -> imperial ? new UnitConversion("in", "in", 1 / 25.4, 0) : identity("mm");
            case "m/s" -> imperial ? new UnitConversion("mph", "mph", 3600 / 1609.344, 0) : identity("m/s");
            default -> identity(units);
        };
    }

    /** {@code v} converted. */
    public double apply(double v) {
        return v * scale + offset;
    }

    /** {@code v} converted, or null. */
    public Double apply(Double v) {
        return v == null ? null : apply(v.doubleValue());
    }

    /** True when values change. */
    public boolean converts() {
        return scale != 1 || offset != 0;
    }

    /** A short label for a value: 0 decimals from 10, 1 from 1, else 2, trailing zeros removed. */
    public static String format(double v) {
        double a = Math.abs(v);
        return format(v, a >= 10 ? 0 : a >= 1 ? 1 : 2);
    }

    /**
     * A label for a measured value, such as a sample readout or a distance: 0 decimals from 100,
     * 1 from 1, else 2, trailing zeros removed. Unlike legend ticks, a reading keeps its tenths
     * below 100 ({@code 52.5 dBZ}, MRMS's half-dBZ step; {@code 15.5 mi}).
     *
     * @param v the value
     * @return the label
     */
    public static String formatReading(double v) {
        double a = Math.abs(v);
        return format(v, a >= 100 ? 0 : a >= 1 ? 1 : 2);
    }

    private static String format(double v, int decimals) {
        BigDecimal d = BigDecimal.valueOf(v).setScale(decimals, RoundingMode.HALF_UP).stripTrailingZeros();
        String s = d.toPlainString();
        return s.equals("-0") ? "0" : s;
    }
}
