package ai.foresignal.intel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Validates a correlation recipe definition (prompt 101). A recipe whose kinds are missing loads
 * disabled with a warning rather than failing startup; a structurally invalid recipe is rejected.
 */
public final class RecipeDefinitionValidator {

    private RecipeDefinitionValidator() {}

    /**
     * The verdict.
     *
     * @param valid true when the recipe may run
     * @param warnings why it is disabled or which kinds are missing
     */
    public record Validation(boolean valid, List<String> warnings) {}

    /** Validates {@code recipe} against the kinds the registry provides. */
    public static Validation validate(RecipeDefinition recipe, Set<String> availableKinds) {
        List<String> warnings = new ArrayList<>();
        boolean valid = true;
        if (recipe.type() == null || !EventTypes.ALL.contains(recipe.type())) {
            warnings.add("unknown event type " + recipe.type());
            valid = false;
        }
        if (recipe.seed().kinds().isEmpty()) {
            warnings.add("seed has no kinds");
            valid = false;
        }
        for (String kind : recipe.allKinds()) {
            if (!availableKinds.contains(kind)) {
                warnings.add("kind '" + kind + "' is not installed");
                valid = false;
            }
        }
        for (RecipeDefinition.Join join : recipe.joins()) {
            if (join.weight() < 0 || join.weight() > 1) {
                warnings.add("join weight " + join.weight() + " is outside 0..1");
                valid = false;
            }
            if (join.signalFamily() != null && !SignalFamilies.ALL.contains(join.signalFamily())) {
                warnings.add("unknown signal family " + join.signalFamily());
                valid = false;
            }
        }
        if (recipe.minMembers() < 1) {
            warnings.add("minMembers must be at least 1");
            valid = false;
        }
        if (recipe.quietPeriod() == null || recipe.quietPeriod().isNegative() || recipe.quietPeriod().isZero()) {
            warnings.add("quietPeriod must be positive");
            valid = false;
        }
        return new Validation(valid, warnings);
    }
}
