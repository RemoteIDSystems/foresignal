package ai.foresignal.reference;

import ai.foresignal.store.query.PgArrays;
import ai.foresignal.trigger.GeofenceRef;
import ai.foresignal.trigger.RegionFences;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The geometry of a rule's geofence preset (prompt 040): the union {@link GeofenceRef#unionSql} computes,
 * read once and cached per preset until reference data changes.
 */
@Component
public class RegionGeometries implements RegionFences, ai.foresignal.ingest.IngestStateReset {

    private final JdbcTemplate jdbc;
    private final RegionTagger tagger;
    private final Map<String, MultiPolygon> cache = new ConcurrentHashMap<>();

    /** Creates the resolver. */
    public RegionGeometries(JdbcTemplate jdbc, RegionTagger tagger) {
        this.jdbc = jdbc;
        this.tagger = tagger;
    }

    @Override
    public boolean available(GeofenceRef ref) {
        return (ref.country().isEmpty() || tagger.loaded("ref_country"))
                && (ref.admin1().isEmpty() || tagger.loaded("ref_admin1"))
                && (ref.county().isEmpty() || tagger.loaded("ref_admin2"));
    }

    @Override
    public MultiPolygon union(GeofenceRef ref) {
        if (!available(ref)) {
            throw new IllegalStateException("The reference data for geofence preset " + ref.canonical()
                    + " is not loaded (REFERENCE_DATA_MISSING)");
        }
        return cache.computeIfAbsent(ref.canonical(), key -> load(ref));
    }

    private MultiPolygon load(GeofenceRef ref) {
        List<Object> args = new ArrayList<>();
        String union = ref.unionSql(codes -> {
            args.add(PgArrays.text(codes));
            return "CAST(? AS text[])";
        });
        byte[] wkb = jdbc.queryForObject("SELECT ST_AsBinary(" + union + ")", byte[].class, args.toArray());
        if (wkb == null) {
            throw new IllegalStateException("Geofence preset " + ref.canonical() + " names no loaded region");
        }
        try {
            Geometry geometry = new WKBReader().read(wkb);
            if (!(geometry instanceof MultiPolygon multi) || multi.isEmpty()) {
                throw new IllegalStateException("Geofence preset " + ref.canonical() + " has no area");
            }
            multi.setSRID(4326);
            return multi;
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
}
