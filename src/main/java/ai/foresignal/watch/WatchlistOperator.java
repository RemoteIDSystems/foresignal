package ai.foresignal.watch;

import ai.foresignal.entity.EntityKeyExpansion;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.PgArrays;
import ai.foresignal.trigger.Evaluation;
import ai.foresignal.trigger.IdentityOperator;
import ai.foresignal.trigger.Operators;
import ai.foresignal.trigger.SqlTerm;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

    /** The identity precedence used to pick one path per source for an {@code entity:} item. */
    private static final List<String> ENTITY_IDENTITIES = List.of("icao24", "mmsi", "imo", "registration");

    private final boolean negated;
    private final WatchlistSnapshot snapshot;
    private final IdentityResolver identities;
    private final SourceRegistry registry;
    private final EntityKeyExpansion expansion;

    /**
     * Creates the operator.
     *
     * @param negated true for {@code notInWatchlist}
     * @param snapshot the lists in memory
     * @param identities the identity paths of the sources
     * @param registry the source descriptors, for an {@code entity:} item's per-source path
     * @param expansion the entities' open keys, for an {@code entity:} item
     */
    public WatchlistOperator(boolean negated, WatchlistSnapshot snapshot, IdentityResolver identities,
            SourceRegistry registry, EntityKeyExpansion expansion) {
        this.negated = negated;
        this.snapshot = snapshot;
        this.identities = identities;
        this.registry = registry;
        this.expansion = expansion;
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
        String text = "(CASE WHEN " + term.typeof() + " IN ('string', 'number') THEN " + term.text() + " END)";
        Optional<WatchlistSnapshot.ListSnapshot> list = list(value);
        if (list.isPresent() && list.get().identity() == Identity.ENTITY) {
            // An entity item expands to its open keys; both this SQL and the persist gate read the
            // same expansion snapshot.
            Set<String> values = expansion.valuesFor(entityIds(list.get()));
            String any = term.bind("v", PgArrays.text(values), "text[]");
            return "(CASE WHEN " + text + " IS NULL THEN NULL ELSE " + (negated ? "NOT " : "") + text
                    + " = ANY(" + any + ") END)";
        }
        Identity.Normalize normalize = list.map(WatchlistSnapshot.ListSnapshot::normalize)
                .orElse(Identity.Normalize.NONE);
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
        if (list.isEmpty()) {
            return Evaluation.of(negated);
        }
        WatchlistSnapshot.ListSnapshot snapshotList = list.get();
        boolean member;
        if (snapshotList.identity() == Identity.ENTITY) {
            member = expansion.valuesFor(entityIds(snapshotList)).contains(snapshotList.normalize().apply(text));
        } else {
            member = snapshotList.values().contains(snapshotList.normalize().apply(text));
        }
        return Evaluation.of(negated != member);
    }

    @Override
    public Map<String, String> identityPaths(Object value, Collection<String> sources) {
        Optional<WatchlistSnapshot.ListSnapshot> list = list(value);
        if (list.isEmpty() || list.get().identity() == Identity.CUSTOM) {
            return Map.of();
        }
        if (list.get().identity() == Identity.ENTITY) {
            return entityPaths(sources);
        }
        return identities.paths(list.get().identity(), sources);
    }

    @Override
    public List<String> sourcesWithoutIdentity(Object value, Collection<String> sources) {
        Optional<WatchlistSnapshot.ListSnapshot> list = list(value);
        if (list.isEmpty()) {
            return List.of();
        }
        if (list.get().identity() == Identity.ENTITY) {
            Map<String, String> paths = entityPaths(sources);
            List<String> missing = new ArrayList<>();
            for (String source : sourceIds(sources)) {
                if (!paths.containsKey(source)) {
                    missing.add(source);
                }
            }
            return missing;
        }
        return identities.without(list.get().identity(), sources);
    }

    @Override
    public boolean requiresPath(Object value) {
        Optional<WatchlistSnapshot.ListSnapshot> list = list(value);
        return list.isEmpty() || list.get().identity() == Identity.CUSTOM;
    }

    /**
     * The per-source identity path for an {@code entity:} item: the source's declared entity-key field
     * for the highest-precedence registry identity it carries.
     */
    private Map<String, String> entityPaths(Collection<String> sources) {
        Map<String, String> paths = new LinkedHashMap<>();
        for (String source : sourceIds(sources)) {
            Optional<SourceDescriptor> descriptor = registry.find(source).map(r -> r.descriptor());
            if (descriptor.isEmpty()) {
                continue;
            }
            Map<String, String> keys = descriptor.get().entityKeys();
            for (String identity : ENTITY_IDENTITIES) {
                String path = keys.get(identity);
                if (path != null) {
                    paths.put(source, path);
                    break;
                }
            }
        }
        return paths;
    }

    /** The given sources, or every registered source when the collection is empty. */
    private Collection<String> sourceIds(Collection<String> sources) {
        if (sources != null && !sources.isEmpty()) {
            return sources;
        }
        return registry.descriptors().stream().map(SourceDescriptor::id).sorted().toList();
    }

    /** The entity ids of an {@code entity:} list's items. */
    private static Set<String> entityIds(WatchlistSnapshot.ListSnapshot list) {
        Set<String> ids = new LinkedHashSet<>();
        for (String value : list.values()) {
            if (value.startsWith("entity:")) {
                ids.add(value.substring("entity:".length()));
            }
        }
        return ids;
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
