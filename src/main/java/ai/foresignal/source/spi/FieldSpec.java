package ai.foresignal.source.spi;

import java.util.Set;

/**
 * A declared payload field: its JSON type, unit, label and, for vertical values, its datum
 * (descriptor {@code fieldSpecs}, OpenAPI schema {@code FieldSpec}).
 *
 * <p>Clients use these to label values and convert units; the kind schema endpoint (prompt 013)
 * publishes them. Nothing on the ingest path reads them.
 *
 * @param type the JSON type; required
 * @param unit a UCUM unit code such as {@code m}, {@code km}, {@code [kn_i]}, {@code [ft_i]},
 *     {@code deg}, {@code s} or {@code 1}; null when the value has no unit
 * @param label a display label
 * @param description what the field means
 * @param datum the vertical datum ({@code MSL}, {@code WGS84}, {@code AGL}, {@code PRESSURE} or
 *     free text), only for vertical fields
 * @param kinds the kinds this spec applies to; empty means every kind of the source
 */
public record FieldSpec(
        FieldType type, String unit, String label, String description, String datum, Set<String> kinds) {

    public FieldSpec {
        kinds = kinds == null ? Set.of() : Set.copyOf(kinds);
    }

    /** A spec with only a type and unit, applying to every kind. */
    public static FieldSpec of(FieldType type, String unit) {
        return new FieldSpec(type, unit, null, null, null, Set.of());
    }

    /** True when this spec applies to {@code kind}. */
    public boolean appliesTo(String kind) {
        return kinds.isEmpty() || kinds.contains(kind);
    }
}
