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
            "infrastructure-disruption", "dry-lightning-ignition", "airport-weather", "smoke-aq");

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

    /**
     * Records a correlation pass of recipe {@code id}: {@code last_run_at}, and {@code last_error} (null
     * clears it). A missing recipe is ignored.
     */
    public void recordRun(String id, java.time.Instant at, String error) {
        try {
            recipes.findById(id).ifPresent(entity -> {
                entity.setLastRunAt(at);
                entity.setLastError(error == null ? null : error.length() > 1_000 ? error.substring(0, 1_000) : error);
                recipes.save(entity);
            });
        } catch (RuntimeException e) {
            log.debug("Could not record the run of recipe {}: {}", id, e.toString());
        }
    }

    /** A stored recipe (enabled or not) as a parsed definition; empty when unknown. */
    public java.util.Optional<RecipeDefinition> definition(String id) {
        return recipes.findById(id).map(entity -> RecipeDefinition.of(entity.getId(), entity.getVersion(),
                override(entity.getDefinition(), entity.getParams()), operators));
    }

    /**
     * A member's signal family and weight, derived by the server (prompt 101, invariant INT-3): from the
     * clause of a stored recipe of {@code eventType} that reads {@code kind} (a join's {@code signalFamily}
     * and {@code weight}; a seed kind contributes its {@link SignalFamilies#forKind} family at
     * {@link #SEED_WEIGHT}), otherwise {@link SignalFamilies#forKind} at {@link EventScorer#DEFAULT_RELIABILITY}.
     * Clients and models never choose either number.
     */
    public FamilyWeight familyWeight(String eventType, String kind) {
        try {
            for (CorrelationRecipeEntity entity : recipes.findAll()) {
                RecipeDefinition definition = RecipeDefinition.of(entity.getId(), entity.getVersion(),
                        override(entity.getDefinition(), entity.getParams()), operators);
                if (eventType == null || !eventType.equals(definition.type())) {
                    continue;
                }
                for (RecipeDefinition.Join join : definition.joins()) {
                    if (join.kinds().contains(kind)) {
                        return new FamilyWeight(join.signalFamily() == null ? SignalFamilies.forKind(kind)
                                : join.signalFamily(), join.weight());
                    }
                }
                if (definition.seed().kinds().contains(kind)) {
                    return new FamilyWeight(SignalFamilies.forKind(kind), SEED_WEIGHT);
                }
            }
        } catch (RuntimeException e) {
            log.debug("Recipe lookup for {}/{} failed: {}", eventType, kind, e.toString());
        }
        return new FamilyWeight(SignalFamilies.forKind(kind), EventScorer.DEFAULT_RELIABILITY);
    }

    /** The weight of a seed member (the correlation engine's seed clause). */
    public static final double SEED_WEIGHT = 0.6;

    /**
     * A server-derived signal family and recipe weight.
     *
     * @param family the signal family
     * @param weight the recipe weight (0–1)
     */
    public record FamilyWeight(String family, double weight) {}

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
