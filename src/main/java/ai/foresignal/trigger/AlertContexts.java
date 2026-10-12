package ai.foresignal.trigger;

import ai.foresignal.source.formats.geojson.GeoJsonReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

/**
 * Builds an alert's computed context and its template variables (prompt 036):
 *
 * <ul>
 *   <li>{@code context.near = {placeIds, placeVersions}} for rules near places, with
 *       {@code {{place.name}}} (the names, comma-separated) and {@code {{place.distanceKm}}} (the
 *       distance from the record to the nearest place, rounded to 0.1 km);
 *   <li>{@code context.anchor = {source, externalId, seq, distanceKm}} for moving-anchor rules, with
 *       {@code {{anchor.distanceKm}}}.
 * </ul>
 *
 * <p>Coordinates of places and anchors are never put into the context or a variable; only names and
 * rounded distances are.
 */
public final class AlertContexts {

    private AlertContexts() {}

    /**
     * The context and the variables.
     *
     * @param json the {@code AlertContext} members, or null when there are none
     * @param vars the template variables (empty when none)
     */
    public record Built(Map<String, Object> json, Map<String, String> vars) {}

    /**
     * Builds them for one alert.
     *
     * @param rule the rule
     * @param row the matching row
     * @param anchor the anchor's position for an anchored rule, or null
     * @param anchorSeq the anchor's seq, or null
     * @param places the place lookup, or null
     */
    @SuppressWarnings("unchecked")
    public static Built build(RuleSpec rule, MatchRow row, Point anchor, Long anchorSeq, PlaceLookup places) {
        Map<String, Object> json = new LinkedHashMap<>();
        Map<String, String> vars = new HashMap<>();
        Geometry geometry = geometry(row.geometry());
        Map<String, Object> near = rule.near();
        if (near != null && near.get("placeIds") instanceof List<?> ids) {
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("placeIds", ids);
            Object versions = near.get(RuleValidator.PLACE_VERSIONS);
            context.put("placeVersions", versions instanceof List<?> list ? list : List.of());
            json.put("near", context);
            if (places != null) {
                List<String> names = new ArrayList<>();
                Double best = null;
                for (Object id : ids) {
                    String placeId = String.valueOf(id);
                    places.name(placeId).ifPresent(names::add);
                    Optional<Geometry> origin = places.origin(placeId);
                    if (origin.isPresent() && geometry != null) {
                        double km = distanceKm(origin.get(), geometry);
                        best = best == null ? km : Math.min(best, km);
                    }
                }
                vars.put("place.name", String.join(", ", names));
                vars.put("place.distanceKm", best == null ? "" : round(best));
            }
        }
        if (rule.anchored() && anchor != null) {
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("source", rule.anchor().source());
            context.put("externalId", rule.anchor().externalId());
            context.put("seq", anchorSeq);
            Double km = geometry == null ? null : AnchorDistance.distanceMeters(anchor, geometry) / 1000.0;
            context.put("distanceKm", km == null ? null : Double.valueOf(round(km)));
            json.put("anchor", context);
            vars.put("anchor.distanceKm", km == null ? "" : round(km));
        }
        return new Built(json.isEmpty() ? null : json, vars);
    }

    /** The distance in km between a place's own geometry and a record's geometry. */
    static double distanceKm(Geometry place, Geometry record) {
        if (place.intersects(record)) {
            return 0;
        }
        if (place instanceof Point p) {
            return AnchorDistance.distanceMeters(p, record) / 1000.0;
        }
        Point from = record instanceof Point p ? p : record.getInteriorPoint();
        return AnchorDistance.distanceMeters(from, place) / 1000.0;
    }

    /** {@code km} rounded to 0.1, as text. */
    static String round(double km) {
        return String.valueOf(Math.round(km * 10) / 10.0);
    }

    private static Geometry geometry(String geoJson) {
        if (geoJson == null || geoJson.isBlank()) {
            return null;
        }
        try {
            return new GeoJsonReader().read(geoJson);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
