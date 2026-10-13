package ai.foresignal.mapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;

/**
 * The built-in unit table of a {@code RecordMapping} (prompt 059): a handful of UCUM-coded conversions for
 * homelab devices, with no external library. A pair outside the table fails validation.
 *
 * <table>
 *   <caption>Conversions</caption>
 *   <tr><th>Quantity</th><th>From → to</th></tr>
 *   <tr><td>Temperature</td><td>{@code [degF]} → {@code Cel}</td></tr>
 *   <tr><td>Speed</td><td>{@code [mi_i]/h}, {@code km/h}, {@code [kn_i]} → {@code m/s}</td></tr>
 *   <tr><td>Rain</td><td>{@code [in_i]} → {@code mm}; {@code [in_i]/h} → {@code mm/h}</td></tr>
 *   <tr><td>Pressure</td><td>{@code [in_i'Hg]} → {@code hPa}; {@code Pa} → {@code hPa}</td></tr>
 *   <tr><td>Distance</td><td>{@code [mi_i]} → {@code km}; {@code [ft_i]} → {@code m}</td></tr>
 *   <tr><td>Unchanged</td><td>any listed code to itself, among them {@code W/m2}, {@code %}, {@code uSv/h} and
 *       {@code /min} (counts per minute)</td></tr>
 * </table>
 *
 * <p>Factors are exact definitions: 1 mile = 1609.344 m, 1 international knot = 1852 m/h, 1 inch = 25.4 mm,
 * 1 foot = 0.3048 m, 1 inch of mercury = 3386.389 Pa (the conventional value at 0 °C).
 */
public final class Units {

    /** Pascals per inch of mercury. */
    static final double PA_PER_INHG = 3386.389;

    /** Every unit code the table knows, in the spec's order. */
    public static final List<String> CODES = List.of("[degF]", "Cel", "[mi_i]/h", "km/h", "[kn_i]", "m/s", "[in_i]",
            "mm", "[in_i]/h", "mm/h", "[in_i'Hg]", "hPa", "Pa", "[mi_i]", "km", "[ft_i]", "m", "W/m2", "%", "uSv/h",
            "/min");

    private static final Set<String> KNOWN = Set.copyOf(CODES);

    private static final Map<String, DoubleUnaryOperator> CONVERSIONS = conversions();

    private Units() {}

    /**
     * The conversion from {@code from} to {@code to}.
     *
     * @return the function, or empty when the pair is not in the table
     */
    public static Optional<DoubleUnaryOperator> conversion(String from, String to) {
        if (from == null || to == null || !KNOWN.contains(from) || !KNOWN.contains(to)) {
            return Optional.empty();
        }
        if (from.equals(to)) {
            return Optional.of(DoubleUnaryOperator.identity());
        }
        return Optional.ofNullable(CONVERSIONS.get(from + "->" + to));
    }

    /** True when {@code code} is in the table. */
    public static boolean known(String code) {
        return code != null && KNOWN.contains(code);
    }

    /** The non-identity pairs, {@code from->to}, for documentation and tests. */
    public static Set<String> pairs() {
        return CONVERSIONS.keySet();
    }

    private static Map<String, DoubleUnaryOperator> conversions() {
        Map<String, DoubleUnaryOperator> out = new LinkedHashMap<>();
        out.put("[degF]->Cel", f -> (f - 32.0) * 5.0 / 9.0);
        out.put("[mi_i]/h->m/s", v -> v * 1609.344 / 3600.0);
        out.put("km/h->m/s", v -> v / 3.6);
        out.put("[kn_i]->m/s", v -> v * 1852.0 / 3600.0);
        out.put("[in_i]->mm", v -> v * 25.4);
        out.put("[in_i]/h->mm/h", v -> v * 25.4);
        out.put("[in_i'Hg]->hPa", v -> v * PA_PER_INHG / 100.0);
        out.put("Pa->hPa", v -> v / 100.0);
        out.put("[mi_i]->km", v -> v * 1.609344);
        out.put("[ft_i]->m", v -> v * 0.3048);
        return java.util.Collections.unmodifiableMap(out);
    }
}
