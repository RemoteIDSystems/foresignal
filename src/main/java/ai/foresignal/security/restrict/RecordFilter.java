package ai.foresignal.security.restrict;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.locationtech.jts.geom.Geometry;

/**
 * A request-scoped narrowing of what a read returns (prompt 036): the {@code place=}, {@code view=}
 * and {@code inWatchlist=} parameters, folded into the caller's {@link ReadRestriction} with
 * {@link ReadRestriction#withFilter} so every path that already honors the restriction (records,
 * history, changes, tiles, SSE, search, MCP, Ask) intersects it too, through
 * {@link RestrictionClamp}.
 *
 * <p>A filter has a <em>shape</em> part (sources, kinds, an area) that can be tested from a record's
 * source, kind and geometry, and an optional <em>payload</em> part (view conditions, watchlist
 * membership) that needs the record's payload. Paths that have no payload (a change summary on the
 * SSE live lane) test the shape and then consult the filter's membership memory
 * ({@link #remembered}), which the change lane keeps up to date as persisted versions are judged.
 *
 * <p>Filters are never stored with a token: {@link ReadRestriction#toStored()} ignores them.
 */
public interface RecordFilter {

    /** A stable text form, for cursor fingerprints, entity tags and cache keys. */
    String canonical();

    /** True when the filter has payload conditions (so a payload is needed to judge a record). */
    boolean needsPayload();

    /**
     * The shape test: source, kind and area. A null geometry fails a filter with an area.
     *
     * @param sourceId the record's source
     * @param kind the record's kind, or null when unknown (a kind filter then fails)
     * @param geometry the record's geometry (or its box), or null
     */
    boolean testShape(String sourceId, String kind, Geometry geometry);

    /**
     * The payload test: the filter's conditions on {@code payload}; always true without conditions.
     *
     * @param sourceId the record's source (identity paths of watchlist conditions depend on it)
     * @param payload the record's payload, or null (conditions then fail)
     */
    boolean testPayload(String sourceId, Map<String, Object> payload);

    /**
     * The SQL predicate over explicit columns, binding every value through {@code bind}.
     *
     * @param sourceColumn the source id column
     * @param kindColumn the kind column
     * @param geomColumn the geometry column, or null (an area filter then fails closed)
     * @param payloadExpression the payload expression, or null (conditions then fail closed)
     * @param bind binds a value and returns its placeholder
     * @return the predicate, or empty when the filter narrows nothing
     */
    Optional<String> sql(String sourceColumn, String kindColumn, String geomColumn, String payloadExpression,
            Function<Object, String> bind);

    /**
     * Records whether entity {@code (sourceId, externalId)} matched when its latest persisted version
     * was judged with its payload, so live updates (which carry no payload) can follow it.
     */
    void remember(String sourceId, String externalId, boolean member);

    /** True when the entity's latest judged version matched ({@link #remember}). */
    boolean remembered(String sourceId, String externalId);
}
