package ai.foresignal.view;

import ai.foresignal.security.restrict.RecordFilter;
import ai.foresignal.trigger.RecordPredicate;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.locationtech.jts.geom.Geometry;

/**
 * A {@link RecordFilter} over one {@link RecordPredicate} (prompt 036): the form {@code place=},
 * {@code view=} and {@code inWatchlist=} take when they narrow a read. The predicate renders its own
 * SQL and Java tests; this class adds the membership memory the SSE live lane follows (bounded to
 * {@value #MAX_MEMBERS} entities: beyond it, live updates of new entities wait for their next
 * persisted version).
 */
public final class PredicateFilter implements RecordFilter {

    /** The most entities one filter remembers. */
    public static final int MAX_MEMBERS = 100_000;

    private final RecordPredicate predicate;
    private final String canonical;
    private final Set<String> members = ConcurrentHashMap.newKeySet();

    /**
     * Creates the filter.
     *
     * @param predicate the predicate
     * @param canonical a stable text form of what it means (for cursors and cache keys)
     */
    public PredicateFilter(RecordPredicate predicate, String canonical) {
        this.predicate = predicate;
        this.canonical = canonical;
    }

    /** The predicate. */
    public RecordPredicate predicate() {
        return predicate;
    }

    @Override
    public String canonical() {
        return canonical;
    }

    @Override
    public boolean needsPayload() {
        return predicate.hasConditions();
    }

    @Override
    public boolean testShape(String sourceId, String kind, Geometry geometry) {
        return predicate.testShape(sourceId, kind, geometry);
    }

    @Override
    public boolean testPayload(String sourceId, Map<String, Object> payload) {
        return predicate.testConditions(sourceId, payload);
    }

    @Override
    public Optional<String> sql(String sourceColumn, String kindColumn, String geomColumn, String payloadExpression,
            Function<Object, String> bind) {
        if (predicate.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(predicate.sql(sourceColumn, kindColumn, geomColumn, payloadExpression,
                RecordPredicate.binder(bind)));
    }

    @Override
    public void remember(String sourceId, String externalId, boolean member) {
        String key = sourceId + '\u0000' + externalId;
        if (!member) {
            members.remove(key);
        } else if (members.size() < MAX_MEMBERS) {
            members.add(key);
        }
    }

    @Override
    public boolean remembered(String sourceId, String externalId) {
        return members.contains(sourceId + '\u0000' + externalId);
    }
}
