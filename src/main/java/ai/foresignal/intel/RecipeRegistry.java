package ai.foresignal.intel;

import ai.foresignal.config.StartupOrder;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.trigger.OperatorRegistry;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Loads and versions the built-in correlation recipes (prompt 101). A bundled recipe is inserted on
 * first start and refreshed when the bundled {@code version} is newer; the operator's {@code enabled}
 * flag and parameter overrides are kept. A recipe whose kinds are not installed loads disabled with a
 * warning rather than failing startup.
 */
@Component
public class RecipeRegistry {

    private static final Logger log = LoggerFactory.getLogger(RecipeRegistry.class);

    /** The bundled recipes, by resource name. */
    public static final List<String> BUNDLED = List.of("maritime-disruption", "aviation-anomaly",
            "infrastructure-disruption");

    private static final String DIR = "/intel/recipes/";

    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    private final CorrelationRecipeRepository recipes;
    private final OperatorRegistry operators;
    private final SourceRegistry sources;
    private final JsonMapper json;

    /** Creates the registry. */
    public RecipeRegistry(CorrelationRecipeRepository recipes, OperatorRegistry operators, SourceRegistry sources,
            JsonMapper json) {
        this.recipes = recipes;
        this.operators = operators;
        this.sources = sources;
        this.json = json;
    }

    /** Syncs the bundled recipes once the application is ready. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.INTEL)
    public void sync() {
        for (String name : BUNDLED) {
            try {
                Map<String, Object> bundled = read(name);
                if (bundled == null) {
                    continue;
                }
                int version = RecipeDefinition.integer(bundled.get("version"), 1);
                CorrelationRecipeEntity stored = recipes.findById(name).orElse(null);
                if (stored != null && stored.getVersion() >= version) {
                    continue;
                }
                RecipeDefinition definition = RecipeDefinition.of(name, version, bundled, operators);
                RecipeDefinitionValidator.Validation validation =
                        RecipeDefinitionValidator.validate(definition, sources.kinds());
                boolean fresh = stored == null;
                CorrelationRecipeEntity entity = fresh ? new CorrelationRecipeEntity(name, version, bundled) : stored;
                entity.setVersion(version);
                entity.setDefinition(bundled);
                if (fresh) {
                    entity.setEnabled(validation.valid());
                } else if (!validation.valid()) {
                    entity.setEnabled(false);
                }
                entity.setLastError(validation.valid() ? null : String.join("; ", validation.warnings()));
                recipes.save(entity);
                if (!validation.valid()) {
                    log.warn("Recipe {} loaded disabled: {}", name, String.join("; ", validation.warnings()));
                }
            } catch (RuntimeException e) {
                log.warn("Recipe {} could not be loaded: {}", name, e.toString());
            }
        }
    }

    /** The enabled recipes as parsed definitions. */
    public List<RecipeDefinition> enabledDefinitions() {
        List<RecipeDefinition> out = new ArrayList<>();
        for (CorrelationRecipeEntity entity : recipes.findByEnabledTrueOrderByIdAsc()) {
            out.add(RecipeDefinition.of(entity.getId(), entity.getVersion(),
                    override(entity.getDefinition(), entity.getParams()), operators));
        }
        return out;
    }

    private Map<String, Object> read(String name) {
        try (InputStream in = RecipeRegistry.class.getResourceAsStream(DIR + name + ".json")) {
            return in == null ? null : json.readValue(in, MAP);
        } catch (Exception e) {
            log.warn("Could not read bundled recipe {}: {}", name, e.toString());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> override(Map<String, Object> definition, Map<String, Object> overrides) {
        if (overrides == null || overrides.isEmpty()) {
            return definition;
        }
        Map<String, Object> merged = new java.util.LinkedHashMap<>(definition);
        merged.putAll(overrides);
        return merged;
    }
}
