package ai.foresignal.reference;

import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.source.spi.CountyGeometries;
import ai.foresignal.store.query.PgArrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * County unions from {@code ref_admin2} for adapters (prompt 047): {@code nws-alerts} draws an alert
 * that has no polygon but names only counties as the union of those counties.
 *
 * <p>The union is computed by PostGIS ({@code ST_Union}, then {@code ST_SimplifyPreserveTopology} to
 * {@value CountyGeometries#SIMPLIFY_DEGREES}°) and cached by the sorted code list, because one county
 * set usually repeats across every update of an alert. The cache is bounded and dropped whenever
 * reference data is reloaded.
 */
@Component
public class CountyUnionService implements CountyGeometries, IngestStateReset {

    /** The most county sets kept in the cache. */
    static final int MAX_CACHED = 2_000;

    static final String UNION_SQL = """
            SELECT ST_AsBinary(ST_SimplifyPreserveTopology(ST_Union(geom), ?))
              FROM ref_admin2
             WHERE fips5 = ANY (CAST(? AS text[]))""";

    static final String STATE_SQL = """
            SELECT ST_AsBinary(ST_SimplifyPreserveTopology(ST_Union(geom), ?))
              FROM ref_admin2
             WHERE fips5 LIKE ?""";

    private final JdbcTemplate jdbc;
    private final RegionTagger tagger;
    private final Map<String, Optional<Geometry>> cache = new ConcurrentHashMap<>();

    /** Creates the service. */
    public CountyUnionService(JdbcTemplate jdbc, RegionTagger tagger) {
        this.jdbc = jdbc;
        this.tagger = tagger;
    }

    @Override
    public boolean available() {
        return tagger.loaded("ref_admin2");
    }

    @Override
    public Optional<Geometry> union(Collection<String> fips5) {
        if (fips5 == null || fips5.isEmpty() || !available()) {
            return Optional.empty();
        }
        TreeSet<String> codes = new TreeSet<>();
        for (String code : fips5) {
            if (code != null && code.matches("\\d{5}")) {
                codes.add(code);
            }
        }
        if (codes.isEmpty()) {
            return Optional.empty();
        }
        String key = String.join(",", codes);
        Optional<Geometry> cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        Optional<Geometry> computed = load(codes);
        if (cache.size() >= MAX_CACHED) {
            cache.clear();
        }
        cache.put(key, computed);
        return computed;
    }

    /** The union of every county whose FIPS code starts with {@code stateFips2} (prompt 086), cached. */
    @Override
    public Optional<Geometry> stateUnion(String stateFips2) {
        if (stateFips2 == null || !stateFips2.matches("\\d{2}") || !available()) {
            return Optional.empty();
        }
        String key = "state:" + stateFips2;
        Optional<Geometry> cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        Optional<Geometry> computed = read(jdbc.queryForObject(STATE_SQL, byte[].class, SIMPLIFY_DEGREES,
                stateFips2 + "%"));
        if (cache.size() >= MAX_CACHED) {
            cache.clear();
        }
        cache.put(key, computed);
        return computed;
    }

    private Optional<Geometry> load(Collection<String> codes) {
        return read(jdbc.queryForObject(UNION_SQL, byte[].class, SIMPLIFY_DEGREES, PgArrays.text(codes)));
    }

    private static Optional<Geometry> read(byte[] wkb) {
        if (wkb == null) {
            return Optional.empty();
        }
        try {
            Geometry geometry = new WKBReader().read(wkb);
            if (geometry.isEmpty()) {
                return Optional.empty();
            }
            geometry.setSRID(4326);
            return Optional.of(geometry);
        } catch (ParseException e) {
            throw new IllegalStateException("PostGIS returned unreadable WKB: " + e.getMessage(), e);
        }
    }

    /** Drops the cache after a reference load. */
    @EventListener
    public void onReferenceChanged(ReferenceDataChangedEvent event) {
        cache.clear();
    }

    /** Drops the cache (also after the test cleaner truncates). */
    @Override
    public void resetState() {
        cache.clear();
    }

    /** Cached county sets, for tests. */
    int cachedSets() {
        return cache.size();
    }
}
