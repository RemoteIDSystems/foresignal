package ai.foresignal.derive;

import ai.foresignal.geo.H3Cells;
import ai.foresignal.source.spi.Geo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An in-memory H3 res-5 count ring (prompt 099): it answers "how many distinct entities did a
 * source report within {@code radiusKm} of a point", which is the coverage test every
 * absence-based generator runs before it asserts a gap.
 *
 * <p>Entities are bucketed by their res-5 cell (about 8 km edge, so the ring is a handful of cells
 * for a 50 km radius); {@code gridDisk} gathers the candidate cells and the exact great-circle test
 * refines them, so the ring never scans the world.
 *
 * <p>Not thread-safe: built and queried on one generator's thread.
 */
public final class DensityIndex {

    /** The resolution of the count ring (about 8 km edge, per the prompt). */
    public static final int RES = 5;

    private record Entry(double lon, double lat, String entity) {}

    private final Map<Long, List<Entry>> byCell = new HashMap<>();

    /** Adds one sighting of {@code entity} at {@code (lon, lat)}. */
    public void add(double lon, double lat, String entity) {
        if (!Double.isFinite(lon) || !Double.isFinite(lat) || entity == null) {
            return;
        }
        long cell = H3Cells.latLngToCell(lat, lon, RES);
        byCell.computeIfAbsent(cell, c -> new ArrayList<>(4)).add(new Entry(lon, lat, entity));
    }

    /** The number of distinct entities within {@code radiusKm} of the point. */
    public int countNear(double lon, double lat, double radiusKm) {
        if (!Double.isFinite(lon) || !Double.isFinite(lat) || radiusKm <= 0) {
            return 0;
        }
        double edge = H3Cells.averageEdgeLengthKm(RES);
        int k = (int) Math.ceil(radiusKm / Math.max(edge, 0.1)) + 1;
        long center = H3Cells.latLngToCell(lat, lon, RES);
        Set<String> entities = new HashSet<>();
        for (long cell : H3Cells.gridDisk(center, k)) {
            List<Entry> entries = byCell.get(cell);
            if (entries == null) {
                continue;
            }
            for (Entry entry : entries) {
                if (Geo.haversineKm(lat, lon, entry.lat(), entry.lon()) <= radiusKm) {
                    entities.add(entry.entity());
                }
            }
        }
        return entities.size();
    }

    /** The number of cells with at least one entity. */
    public int size() {
        return byCell.size();
    }

    /** Empties the ring. */
    public void clear() {
        byCell.clear();
    }
}
