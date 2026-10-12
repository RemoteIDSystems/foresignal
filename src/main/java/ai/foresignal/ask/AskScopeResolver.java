package ai.foresignal.ask;

/**
 * Resolves the parts of an {@link AskScope} that need other features (prompt 028's hook for prompt
 * 036): {@code placeIds} become areas (the model sees names and rounded distances only) and
 * {@code viewId} a saved view's predicate.
 *
 * <p>Until a bean of this type exists, {@link AskService} refuses a scope with {@code placeIds} or
 * {@code viewId} with a {@code 400} naming prompt 036.
 */
public interface AskScopeResolver {

    /**
     * The scope with {@code placeIds} and {@code viewId} folded into plain filters.
     *
     * @param scope the requested scope
     * @return the resolved scope (its {@code placeIds} and {@code viewId} may be kept for display)
     * @throws ai.foresignal.api.error.ValidationException when a place or view is unknown
     */
    AskScope resolve(AskScope scope);
}
