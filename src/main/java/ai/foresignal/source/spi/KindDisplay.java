package ai.foresignal.source.spi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Client display hints for one kind (descriptor {@code kindDisplay}, OpenAPI schema
 * {@code KindDisplay}).
 *
 * <p>{@code titleTemplate} is a client hint only; the server-side title comes from
 * {@code ai.foresignal.ingest.TitleResolver} and never from this template.
 *
 * <p>The color is serialized under the JSON key {@code colour}, which is the published wire name in
 * the OpenAPI document; the Java accessor is {@link #color()}.
 *
 * <p>Prompt 058 adds the three style fields the generated MapLibre style and KML read. Each must name
 * one of the source's {@code tileProperties} ({@code DescriptorValidator}), because the style reads them
 * from the vector tile features: {@code rotateField} rotates the icon (for example {@code track}),
 * {@code sizeField} scales a circle's radius (for example {@code frp} or {@code mag}) and
 * {@code colorField} drives a data-driven color match (by default {@code severity}, when the source
 * carries it). The {@code icon} must be a name in the shipped sprite ({@code ai.foresignal.map.SpriteCatalog}).
 *
 * @param titleTemplate a client title template with {@code {field}} placeholders
 * @param icon an icon name from the sprite catalog, or null for a circle
 * @param color {@code #RRGGBB}
 * @param sidc an optional MIL-STD-2525 symbol identification code of 15 or 20 characters
 * @param rotateField a tile property rotating the icon, or null
 * @param sizeField a tile property scaling the circle radius, or null
 *
 * <p>Two further fields choose by a feature's value rather than by its kind. {@code iconField} names a tile
 * property and {@code iconCases} maps its values to sprite icons, with {@code icon} drawn for any other value
 * (the airports' chart symbols). {@code colorCases} maps values of {@code colorField} to colors, replacing the
 * severity colors that field otherwise matches, with {@code colour} for any other value. A value is matched
 * as text, lowercased.
 *
 * @param colorField a tile property driving a data-driven color match, or null
 * @param iconField a tile property choosing the icon through {@code iconCases}, or null
 * @param iconCases icon names by {@code iconField} value (lowercase), or null; needs {@code iconField} and
 *     {@code icon}
 * @param colorCases {@code #RRGGBB} colors by {@code colorField} value (lowercase), or null; needs
 *     {@code colorField}
 */
public record KindDisplay(
        String titleTemplate,
        String icon,
        @JsonProperty("colour") String color,
        String sidc,
        String rotateField,
        String sizeField,
        String colorField,
        String iconField,
        Map<String, String> iconCases,
        Map<String, String> colorCases) {

    /** Copies the case maps, keeping their order. */
    public KindDisplay {
        iconCases = iconCases == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(iconCases));
        colorCases = colorCases == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(colorCases));
    }

    /** Display hints with the prompt 058 style fields and no value-driven choices. */
    public KindDisplay(String titleTemplate, String icon, String color, String sidc, String rotateField,
            String sizeField, String colorField) {
        this(titleTemplate, icon, color, sidc, rotateField, sizeField, colorField, null, null, null);
    }

    /** Display hints without style fields (the shape before prompt 058). */
    public KindDisplay(String titleTemplate, String icon, String color, String sidc) {
        this(titleTemplate, icon, color, sidc, null, null, null);
    }

    /** This hint with {@code newIcon} as its icon. */
    public KindDisplay withIcon(String newIcon) {
        return new KindDisplay(titleTemplate, newIcon, color, sidc, rotateField, sizeField, colorField, iconField,
                iconCases, colorCases);
    }
}
