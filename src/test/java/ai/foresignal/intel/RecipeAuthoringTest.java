package ai.foresignal.intel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.source.SourceRegistry;
import ai.foresignal.trigger.OperatorRegistry;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * Author-able correlation recipes (prompt 157): an operator recipe validates, persists and round-trips as a
 * parsed definition beside the bundled recipes.
 */
class RecipeAuthoringTest {

    private final CorrelationRecipeRepository recipes = mock(CorrelationRecipeRepository.class);
    private final OperatorRegistry operators = mock(OperatorRegistry.class);
    private final SourceRegistry sources = mock(SourceRegistry.class);
    private final JsonMapper json = JsonMapper.builder().build();
    private final RecipeRegistry registry = new RecipeRegistry(recipes, operators, sources, json);

    private Map<String, Object> definition() {
        return Map.of(
                "type", "maritime-disruption",
                "seed", Map.of("kinds", List.of("company")),
                "joins", List.of(Map.of("kinds", List.of("company"), "sharedAttribute", "registeredAddress")),
                "quietPeriod", "6h");
    }

    @Test
    @DisplayName("a valid operator recipe validates, persists and round-trips as a parsed definition")
    void authorsAndRoundTrips() {
        when(sources.kinds()).thenReturn(Set.of("company"));
        assertThat(registry.validate("shell-companies", definition()).valid()).isTrue();

        AtomicReference<CorrelationRecipeEntity> saved = new AtomicReference<>();
        when(recipes.findById("shell-companies")).thenAnswer(inv -> Optional.ofNullable(saved.get()));
        when(recipes.save(any())).thenAnswer(inv -> {
            saved.set(inv.getArgument(0));
            return inv.getArgument(0);
        });

        CorrelationRecipeEntity entity = registry.author("shell-companies", definition(), true);
        assertThat(entity.getId()).isEqualTo("shell-companies");
        assertThat(entity.isEnabled()).isTrue();
        assertThat(RecipeRegistry.isBundled("shell-companies")).isFalse();

        RecipeDefinition parsed = registry.definition("shell-companies").orElseThrow();
        assertThat(parsed.type()).isEqualTo("maritime-disruption");
        assertThat(parsed.joins().get(0).sharedAttribute()).isEqualTo("registeredAddress");
    }

    @Test
    @DisplayName("a recipe reading an uninstalled kind is invalid")
    void rejectsMissingKind() {
        when(sources.kinds()).thenReturn(Set.of("vessel"));

        RecipeDefinitionValidator.Validation validation = registry.validate("shell-companies", definition());

        assertThat(validation.valid()).isFalse();
        assertThat(validation.warnings()).anyMatch(w -> w.contains("company"));
    }

    @Test
    @DisplayName("a bundled id is recognized as bundled")
    void recognizesBundled() {
        assertThat(RecipeRegistry.isBundled("maritime-disruption")).isTrue();
    }
}
