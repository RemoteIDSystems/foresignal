package ai.foresignal.source.adapters.derived;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The bundled chokepoint buffer radii (prompt 099): {@code derived/chokepoints.json} maps a
 * chokepoint name to the radius its activity is measured over. Unknown names get the default.
 */
@Component
public class Chokepoints {

    /** The resource path of the bundled table. */
    public static final String RESOURCE = "/derived/chokepoints.json";

    /** The default radius when a chokepoint is not in the table. */
    public static final double DEFAULT_RADIUS_KM = 50.0;

    private final Map<String, Double> byName;
    private final double defaultRadiusKm;

    /** Loads the bundled table. */
    public Chokepoints() {
        JsonMapper json = JsonMapper.builder().build();
        Map<String, Double> names = new HashMap<>();
        double fallback = DEFAULT_RADIUS_KM;
        try (InputStream in = Chokepoints.class.getResourceAsStream(RESOURCE)) {
            if (in != null) {
                JsonNode root = json.readTree(in);
                JsonNode entries = root.get("chokepoints");
                if (entries != null && entries.isArray()) {
                    for (JsonNode entry : entries) {
                        JsonNode name = entry.get("name");
                        JsonNode radius = entry.get("radiusKm");
                        if (name != null && radius != null) {
                            names.put(normalize(name.asString()), radius.asDouble());
                        }
                    }
                }
                JsonNode def = root.get("defaultRadiusKm");
                if (def != null && def.isNumber()) {
                    fallback = def.asDouble();
                }
            }
        } catch (Exception e) {
            // A missing or malformed table leaves the default; the layer still works.
        }
        this.byName = Map.copyOf(names);
        this.defaultRadiusKm = fallback;
    }

    /** The radius (km) for a chokepoint name. */
    public double radiusFor(String name) {
        if (name == null) {
            return defaultRadiusKm;
        }
        return byName.getOrDefault(normalize(name), defaultRadiusKm);
    }

    /** The number of named chokepoints. */
    public int size() {
        return byName.size();
    }

    private static String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
