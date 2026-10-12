package ai.foresignal.trigger;

import org.locationtech.jts.geom.MultiPolygon;

/**
 * Resolves a rule's {@link GeofenceRef} into the geometry the trigger gate tests (prompt 040);
 * implemented over the reference tables by {@code ai.foresignal.reference.RegionGeometries}.
 */
public interface RegionFences {

    /** True when every table the preset needs holds rows. */
    boolean available(GeofenceRef ref);

    /**
     * The union of the preset's geometries, exactly as {@link GeofenceRef#unionSql} computes it in SQL.
     *
     * @throws IllegalStateException when the reference data is missing or names none of the codes
     */
    MultiPolygon union(GeofenceRef ref);
}
