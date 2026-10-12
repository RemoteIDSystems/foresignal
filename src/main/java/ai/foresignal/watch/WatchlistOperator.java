package ai.foresignal.watch;

import ai.foresignal.trigger.Evaluation;
import ai.foresignal.trigger.IdentityOperator;
import ai.foresignal.trigger.Operators;
import ai.foresignal.trigger.SqlTerm;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * {@code inWatchlist} and {@code notInWatchlist} (prompt 036): the value is a watchlist id; the
 * condition reads its path, or, without one, each source's declared identity field
 * ({@link IdentityResolver}).
 *
 * <p>Shared truth-table semantics: the payload value must be a JSON string or number (a number is
 * compared by its JSON text); anything else, a missing path or JSON null yields SQL NULL and Java
 * {@link Evaluation#AMBIGUOUS}, for both operators. The value is normalized as the list normalizes
 * ({@link Identity.Normalize}) and looked up:
 *
 * <ul>
 *   <li>SQL: {@code CASE WHEN text IS NULL THEN NULL ELSE [NOT] EXISTS (SELECT 1 FROM watch_list_item w
 *       WHERE w.list_id = :list AND w.value = norm(text)) END};
 *   <li>Java: membership in the {@link WatchlistSnapshot}.
 * </ul>
 *
 * <p>An unknown (deleted) list has no items: {@code inWatchlist} never holds and
 * {@code notInWatchlist} holds for every present value.
 */
public class WatchlistOperator implements IdentityOperator {

    private final boolean negated;
    private final WatchlistSnapshot snapshot;
    private final IdentityResolver identities;

    /**
     * Creates the operator.
     *
     * @param negated true for {@code notInWatchlist}
     * @param snapshot the lists in memory
     * @param identities the identity paths of the sources
     */
    public WatchlistOperator(boolean negated, WatchlistSnapshot snapshot, IdentityResolver identities) {
        this.negated = negated;
        this.snapshot = snapshot;
        this.identities = identities;
    }

    @Override
    public String name() {
        return negated ? "notInWatchlist" : "inWatchlist";
    }

    @Override
    public Object normalize(Object value) {
        if (!(value instanceof String s)) {
            throw new IllegalArgumentException("must be a watchlist id for " + name());
        }
        try {
            return UUID.fromString(s.strip()).toString().toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("must be a watchlist id (UUID) for " + name());
        }
    }

    @Override
    public String sql(SqlTerm term, Object value) {
        Identity.Normalize normalize = list(value).map(WatchlistSnapshot.ListSnapshot::normalize)
                .orElse(Identity.Normalize.NONE);
        String text = "(CASE WHEN " + term.typeof() + " IN ('string', 'number') THEN " + term.text() + " END)";
        String exists = "EXISTS (SELECT 1 FROM watch_list_item w WHERE w.list_id = " + term.bind("v", value, "uuid")
                + " AND w.value = " + normalize.sql(text) + ")";
        return "(CASE WHEN " + text + " IS NULL THEN NULL ELSE " + (negated ? "NOT " : "") + exists + " END)";
    }

    @Override
    public Evaluation evaluate(Object actual, Object value) {
        String text = text(actual);
        if (text == null) {
            return Evaluation.AMBIGUOUS;
        }
        UUID id = UUID.fromString((String) value);
        Optional<WatchlistSnapshot.ListSnapshot> list = snapshot.list(id);
        boolean member = list.isPresent() && list.get().values().contains(list.get().normalize().apply(text));
        return Evaluation.of(negated != member);
    }

    @Override
    public Map<String, String> identityPaths(Object value, Collection<String> sources) {
        Optional<WatchlistSnapshot.ListSnapshot> list = list(value);
        if (list.isEmpty() || list.get().identity() == Identity.CUSTOM) {
            return Map.of();
        }
        return identities.paths(list.get().identity(), sources);
    }

    @Override
    public List<String> sourcesWithoutIdentity(Object value, Collection<String> sources) {
        Optional<WatchlistSnapshot.ListSnapshot> list = list(value);
        if (list.isEmpty()) {
            return List.of();
        }
        return identities.without(list.get().identity(), sources);
    }

    @Override
    public boolean requiresPath(Object value) {
        Optional<WatchlistSnapshot.ListSnapshot> list = list(value);
        return list.isEmpty() || list.get().identity() == Identity.CUSTOM;
    }

    @Override
    public boolean known(Object value) {
        return list(value).isPresent();
    }

    private Optional<WatchlistSnapshot.ListSnapshot> list(Object value) {
        try {
            return snapshot.list(UUID.fromString(String.valueOf(value)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * The JSON text of a string or number payload value, as PostgreSQL's {@code #>>} renders it; null
     * for anything else.
     */
    static String text(Object actual) {
        if (actual instanceof String s) {
            return s;
        }
        if (actual instanceof Boolean || actual == null) {
            return null;
        }
        if (actual instanceof Integer || actual instanceof Long || actual instanceof Short || actual instanceof Byte
                || actual instanceof BigInteger) {
            return actual.toString();
        }
        BigDecimal number = Operators.number(actual);
        return number == null ? null : number.toPlainString();
    }
}
