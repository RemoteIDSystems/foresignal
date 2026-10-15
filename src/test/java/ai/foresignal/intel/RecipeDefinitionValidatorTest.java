package ai.foresignal.intel;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.trigger.OperatorRegistry;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Recipe definition parsing and validation (prompt 101). */
class RecipeDefinitionValidatorTest {

    private static Map<String, Object> definition() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "maritime-disruption");
        map.put("minMembers", 2);
        map.put("minSignalFamilies", 2);
        map.put("quietPeriod", "PT6H");
        map.put("activateAt", Map.of("members", 2));
        map.put("seed", Map.of("kinds", List.of("ais-gap", "loiter")));
        map.put("joins", List.of(Map.of("kinds", List.of("nav-warning"), "withinKm", 150, "signalFamily",
                "nav-warning", "weight", 0.6)));
        map.put("contradictions", List.of(Map.of("healthOf", List.of("aisstream"), "alternative",
                "AIS receiver coverage gap")));
        map.put("dedup", Map.of("radiusKm", 100, "window", "PT6H"));
        return map;
    }

    @Test
    @DisplayName("a valid definition parses and validates")
    void valid() {
        RecipeDefinition recipe = RecipeDefinition.of("maritime-disruption", 1, definition(),
                OperatorRegistry.builtIn());
        assertThat(recipe.type()).isEqualTo("maritime-disruption");
        assertThat(recipe.seed().kinds()).containsExactly("ais-gap", "loiter");
        assertThat(recipe.joins()).hasSize(1);
        assertThat(recipe.quietPeriod()).isEqualTo(java.time.Duration.ofHours(6));
        RecipeDefinitionValidator.Validation validation =
                RecipeDefinitionValidator.validate(recipe, Set.of("ais-gap", "loiter", "nav-warning"));
        assertThat(validation.valid()).isTrue();
        assertThat(validation.warnings()).isEmpty();
    }

    @Test
    @DisplayName("a missing kind disables the recipe with a warning, not an exception")
    void missingKind() {
        RecipeDefinition recipe = RecipeDefinition.of("maritime-disruption", 1, definition(),
                OperatorRegistry.builtIn());
        RecipeDefinitionValidator.Validation validation =
                RecipeDefinitionValidator.validate(recipe, Set.of("ais-gap"));
        assertThat(validation.valid()).isFalse();
        assertThat(validation.warnings()).anyMatch(w -> w.contains("loiter"));
    }

    @Test
    @DisplayName("an unknown type, a bad weight or a non-positive quiet period are invalid")
    void badFields() {
        Map<String, Object> map = definition();
        map.put("type", "not-a-type");
        assertThat(RecipeDefinitionValidator.validate(
                RecipeDefinition.of("x", 1, map, OperatorRegistry.builtIn()), Set.of("ais-gap", "loiter", "nav-warning"))
                .valid()).isFalse();

        Map<String, Object> badWeight = definition();
        badWeight.put("joins", List.of(Map.of("kinds", List.of("nav-warning"), "weight", 2.0)));
        assertThat(RecipeDefinitionValidator.validate(
                RecipeDefinition.of("x", 1, badWeight, OperatorRegistry.builtIn()),
                Set.of("ais-gap", "loiter", "nav-warning")).valid()).isFalse();

        Map<String, Object> badQuiet = definition();
        badQuiet.put("quietPeriod", "PT0S");
        assertThat(RecipeDefinitionValidator.validate(
                RecipeDefinition.of("x", 1, badQuiet, OperatorRegistry.builtIn()),
                Set.of("ais-gap", "loiter", "nav-warning")).valid()).isFalse();
    }
}
