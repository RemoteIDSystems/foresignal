package ai.foresignal.stats;

import com.uber.h3core.H3Core;
import com.uber.h3core.util.LatLng;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Component;

/**
 * H3 cells for activity rollups and stats grids (prompt 038), over Uber's H3 Java binding.
 *
 * <p>Cell {@value #NO_CELL} stands for "no geometry" in {@code activity_rollup} and
 * {@value #ALL_CELLS} for the per-{@code (source, kind)} totals row; neither is a valid H3 index (H3
 * indexes are positive). Boundaries that cross the antimeridian are split into a {@code MultiPolygon}
 * with one part on each side, so map clients never draw a cell across the whole world.
 */
@Component
public class H3Cells {

    /** The cell of rollup rows for versions without a geometry. */
    public static final long NO_CELL = 0L;

    /** The cell of the per-{@code (source, kind)} totals rows: exact distinct entities over every cell. */
    public static final long ALL_CELLS = -1L;

    private static final GeometryFactory FACTORY = new GeometryFactory();

    private final H3Core h3;

    /** Loads the native H3 library. */
    public H3Cells() {
        try {
            this.h3 = H3Core.newInstance();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not load the H3 native library", e);
        }
    }

    /**
     * The cell containing {@code (lon, lat)} at {@code res}, or {@link #NO_CELL} for a non-finite or
     * out-of-range coordinate.
     */
    public long cell(double lon, double lat, int res) {
        if (!Double.isFinite(lon) || !Double.isFinite(lat) || lat < -90 || lat > 90) {
            return NO_CELL;
        }
        double normalized = ((lon + 180) % 360 + 360) % 360 - 180;
        return h3.latLngToCell(lat, normalized, res);
    }

    /** True when {@code cell} is a valid H3 index (not {@link #NO_CELL} or {@link #ALL_CELLS}). */
    public boolean valid(long cell) {
        return cell > 0 && h3.isValidCell(cell);
    }

    /** The resolution of {@code cell}. */
    public int resolution(long cell) {
        return h3.getResolution(cell);
    }

    /** The ancestor of {@code cell} at {@code res}; the cell itself when it is not finer than {@code res}. */
    public long parent(long cell, int res) {
        if (!valid(cell) || resolution(cell) <= res) {
            return cell;
        }
        return h3.cellToParent(cell, res);
    }

    /** The center of {@code cell} as {@code [lon, lat]}. */
    public double[] center(long cell) {
        LatLng c = h3.cellToLatLng(cell);
        return new double[] {c.lng, c.lat};
    }

    /** The hexadecimal address of {@code cell}, as clients see it. */
    public String address(long cell) {
        return h3.h3ToString(cell);
    }

    /** The cell of a hexadecimal address. */
    public long parse(String address) {
        return h3.stringToH3(address);
    }

    /**
     * The boundary of {@code cell} as a GeoJSON geometry map: a {@code Polygon}, or a
     * {@code MultiPolygon} split at ±180° when the cell crosses the antimeridian.
     */
    public Map<String, Object> boundary(long cell) {
        List<LatLng> vertices = h3.cellToBoundary(cell);
        return boundary(vertices.stream().map(v -> new double[] {v.lng, v.lat}).toList());
    }

    /**
     * The GeoJSON geometry of a closed-or-open ring given as {@code [lon, lat]} vertices in
     * {@code [-180, 180]}: a {@code Polygon} when no edge spans more than 180° of longitude, else the
     * ring unwrapped eastwards and clipped into a {@code MultiPolygon} with a western and an eastern
     * part. Public for the tests.
     */
    public static Map<String, Object> boundary(List<double[]> ring) {
        boolean crosses = false;
        for (int i = 0; i < ring.size(); i++) {
            double[] a = ring.get(i);
            double[] b = ring.get((i + 1) % ring.size());
            if (Math.abs(a[0] - b[0]) > 180) {
                crosses = true;
                break;
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        if (!crosses) {
            out.put("type", "Polygon");
            out.put("coordinates", List.of(closed(ring)));
            return out;
        }
        Coordinate[] unwrapped = new Coordinate[ring.size() + 1];
        for (int i = 0; i < ring.size(); i++) {
            double lon = ring.get(i)[0];
            unwrapped[i] = new Coordinate(lon < 0 ? lon + 360 : lon, ring.get(i)[1]);
        }
        unwrapped[ring.size()] = unwrapped[0];
        Polygon polygon = FACTORY.createPolygon(unwrapped);
        Geometry west = polygon.intersection(FACTORY.toGeometry(new Envelope(-180, 180, -90, 90)));
        Geometry east = polygon.intersection(FACTORY.toGeometry(new Envelope(180, 540, -90, 90)));
        List<Object> parts = new ArrayList<>();
        addParts(parts, west, 0);
        addParts(parts, east, -360);
        out.put("type", "MultiPolygon");
        out.put("coordinates", parts);
        return out;
    }

    private static void addParts(List<Object> parts, Geometry geometry, double shift) {
        for (int g = 0; g < geometry.getNumGeometries(); g++) {
            Geometry part = geometry.getGeometryN(g);
            if (!(part instanceof Polygon polygon) || polygon.isEmpty()) {
                continue;
            }
            List<double[]> ring = new ArrayList<>();
            for (Coordinate c : polygon.getExteriorRing().getCoordinates()) {
                ring.add(new double[] {round(c.x + shift), round(c.y)});
            }
            parts.add(List.of(ring));
        }
    }

    private static List<double[]> closed(List<double[]> ring) {
        List<double[]> out = new ArrayList<>(ring.size() + 1);
        for (double[] v : ring) {
            out.add(new double[] {round(v[0]), round(v[1])});
        }
        double[] first = ring.getFirst();
        double[] last = ring.getLast();
        if (first[0] != last[0] || first[1] != last[1]) {
            out.add(new double[] {round(first[0]), round(first[1])});
        }
        return out;
    }

    /** Seven decimals (about 1 cm), so the documents stay small. */
    static double round(double value) {
        return Math.round(value * 1e7) / 1e7;
    }
}
