package ai.foresignal.security.restrict;

import java.util.Collection;
import java.util.function.Function;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * Resolves the {@code placeIds} member of a stored restriction to an area (prompt 036; the hook the
 * 035 report left in {@link ReadRestriction#of}).
 *
 * <p>{@link ReadRestriction#of} is static and runs inside the token filter, so the place module
 * installs its resolver here when it starts. Without a resolver, or for an unknown place, the result
 * is an empty geometry, so a restriction naming places never widens into "anywhere": it fails
 * closed and nothing passes.
 */
public final class PlaceAreas {

    private static final GeometryFactory WGS84 = new GeometryFactory(new PrecisionModel(), 4326);

    private static volatile Function<Collection<String>, Geometry> resolver;

    private static volatile java.util.function.Predicate<String> known;

    private PlaceAreas() {}

    /**
     * Installs the resolver (the place module calls this once it is ready).
     *
     * @param value the union of the areas of some place ids, or null when one is unknown
     * @param exists whether a place id exists, for validating stored restrictions
     */
    public static void install(Function<Collection<String>, Geometry> value, java.util.function.Predicate<String> exists) {
        resolver = value;
        known = exists;
    }

    /** Removes the resolver (application shutdown, tests). */
    public static void uninstall() {
        resolver = null;
        known = null;
    }

    /**
     * True when {@code placeId} names a place; null when no place module is installed (shape-only
     * validation cannot tell).
     */
    public static Boolean exists(String placeId) {
        java.util.function.Predicate<String> current = known;
        return current == null ? null : current.test(placeId);
    }

    /**
     * The union of the areas of {@code placeIds}, SRID 4326; an empty polygon when there is no
     * resolver, or when any id is unknown.
     */
    public static Geometry resolve(Collection<String> placeIds) {
        Function<Collection<String>, Geometry> current = resolver;
        Geometry area = current == null ? null : current.apply(placeIds);
        if (area == null) {
            Geometry empty = WGS84.createPolygon();
            empty.setSRID(4326);
            return empty;
        }
        return area;
    }
}
