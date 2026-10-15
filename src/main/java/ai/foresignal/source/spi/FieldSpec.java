package ai.foresignal.source.spi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
 * @param noLink a display hint: clients never render the value as a link (OpenAPI {@code FieldSpec.noLink},
 *     prompt 076: URLhaus malware URLs are hostile). Serialized only when true
 */
public record FieldSpec(
        FieldType type, String unit, String label, String description, String datum, Set<String> kinds,
        @JsonInclude(JsonInclude.Include.NON_DEFAULT) boolean noLink) {

    /** Copies the kinds. */
    public FieldSpec {
        kinds = kinds == null ? Set.of() : Set.copyOf(kinds);
    }

    /**
     * The JSON creator. {@code noLink} is read as a nullable {@code Boolean}: Jackson 3 rejects an
     * absent JSON member bound to a primitive creator parameter, so the primitive {@code noLink}
     * component is not used as a creator parameter and a missing member defaults to {@code false}.
     */
    @JsonCreator
    public static FieldSpec create(
            @JsonProperty("type") FieldType type,
            @JsonProperty("unit") String unit,
            @JsonProperty("label") String label,
            @JsonProperty("description") String description,
            @JsonProperty("datum") String datum,
            @JsonProperty("kinds") Set<String> kinds,
            @JsonProperty("noLink") Boolean noLink) {
        return new FieldSpec(type, unit, label, description, datum, kinds, noLink != null && noLink);
    }

    /**
     * A spec whose value may be rendered as a link (every spec before prompt 076).
     *
     * @param type the JSON type
     * @param unit the UCUM unit, or null
     * @param label the display label, or null
     * @param description what the field means, or null
     * @param datum the vertical datum, or null
     * @param kinds the kinds this spec applies to; empty or null means every kind
     */
    public FieldSpec(FieldType type, String unit, String label, String description, String datum, Set<String> kinds) {
        this(type, unit, label, description, datum, kinds, false);
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
