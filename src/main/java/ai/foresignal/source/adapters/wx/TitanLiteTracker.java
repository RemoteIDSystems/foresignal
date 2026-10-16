package ai.foresignal.source.adapters.wx;

import ai.foresignal.source.spi.Geo;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.AffineTransformation;

/**
 * TITAN-lite storm tracking (prompt 105; Weather-Imagery-Design §7.2): the pure association of one
 * frame's cells to the previous frame's tracks by IoU of advected polygons, with merges and splits.
 * Version {@value #VERSION}.
 *
 * <p>Association:
 * <ol>
 *   <li>each active track's last polygon is advected to the new frame time using its motion vector;
 *   <li>the cost is {@code 1 − IoU(advected_prev, current)}; when IoU = 0 it is {@code 1 + d/30}
 *       (d in km) if the centroid distance is within 30 km per 10 minutes of gap, else forbidden;
 *   <li>the assignment is solved with {@link Hungarian} when both sides are at most
 *       {@value #MAX_HUNGARIAN}, else greedily;
 *   <li>a current cell that overlaps two or more advected tracks (IoU ≥ {@value #MERGE_IOU} each)
 *       merges them: the largest-area track continues (even when the solver had paired the cell with a
 *       smaller one), the others end with {@code mergedInto};
 *   <li>an advected track that overlaps two or more current cells (IoU ≥ {@value #SPLIT_IOU} each)
 *       splits: the largest cell continues the track, the others start with {@code parentTrackId}.
 * </ol>
 * The caller owns the track identity and lifecycle; this class is stateless.
 */
public final class TitanLiteTracker {

    /** The detector version. */
    public static final String VERSION = "titan-lite-v1";

    /** The largest matrix solved exactly. */
    public static final int MAX_HUNGARIAN = 300;

    /** The centroid match distance per 10 minutes of gap, km. */
    public static final double MATCH_KM_PER_10_MIN = 30;

    /** The IoU above which two tracks merged into one cell (each). */
    public static final double MERGE_IOU = 0.1;

    /** The IoU above which one track split into two cells. */
    public static final double SPLIT_IOU = 0.1;

    /** The fewest missed frames that end a track. */
    public static final int MISSED_FRAMES = 2;

    private TitanLiteTracker() {}

    /**
     * One current cell.
     *
     * @param geometry the cell polygon
     * @param lon the centroid longitude
     * @param lat the centroid latitude
     * @param areaKm2 the area
     * @param maxDbz the maximum reflectivity
     */
    public record Cell(Geometry geometry, double lon, double lat, double areaKm2, double maxDbz) {}

    /**
     * One active track's last state.
     *
     * @param trackId the id
     * @param geometry the last polygon
     * @param lon the last centroid longitude
     * @param lat the last centroid latitude
     * @param lastTime when it was last seen
     * @param speedKmh the motion speed
     * @param bearingDeg the motion bearing (0 = north, clockwise)
     */
    public record Previous(String trackId, Geometry geometry, double lon, double lat, Instant lastTime,
            double speedKmh, double bearingDeg) {}

    /** One association. */
    public record Assignment(int previous, int cell, double iou) {}

    /** One ended track that merged into another. */
    public record Merge(int endedPrevious, int intoPrevious) {}

    /** One new track that split from a parent. */
    public record Split(int childCell, int parentPrevious) {}

    /**
     * The association result.
     *
     * @param assignments the one-to-one matches
     * @param merges the tracks that ended by merging
     * @param splits the cells that started by splitting
     * @param unmatchedCells the cells that start a new track
     * @param unmatchedPrevious the tracks not matched and not merged (missed this frame)
     * @param greedy true when the matrix was too large for the exact solver
     */
    public record Result(List<Assignment> assignments, List<Merge> merges, List<Split> splits,
            List<Integer> unmatchedCells, List<Integer> unmatchedPrevious, boolean greedy) {}

    /** Advects {@code geometry} by {@code speedKmh} at {@code bearingDeg} over {@code elapsed}. */
    public static Geometry advect(Geometry geometry, double lon, double lat, double speedKmh, double bearingDeg,
            Duration elapsed) {
        if (geometry == null || speedKmh <= 0 || elapsed == null || elapsed.isZero() || elapsed.isNegative()) {
            return geometry;
        }
        double[] destination = destination(lon, lat, speedKmh, bearingDeg, elapsed.toSeconds() / 3600.0);
        return AffineTransformation.translationInstance(destination[0] - lon, destination[1] - lat)
                .transform(geometry);
    }

    /** The point {@code distanceKm} from {@code (lon, lat)} at {@code bearingDeg}. */
    public static double[] destination(double lon, double lat, double speedKmh, double bearingDeg, double hours) {
        double distanceKm = speedKmh * hours;
        double bearing = Math.toRadians(bearingDeg);
        double dLat = distanceKm / 110.57 * Math.cos(bearing);
        double dLon = distanceKm / (111.32 * Math.max(1e-6, Math.cos(Math.toRadians(lat)))) * Math.sin(bearing);
        return new double[] {lon + dLon, lat + dLat};
    }

    /** The intersection-over-union area of two geometries. */
    public static double iou(Geometry a, Geometry b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
            return 0;
        }
        try {
            Geometry inter = a.intersection(b);
            Geometry union = a.union(b);
            double unionArea = union.getArea();
            return unionArea <= 0 ? 0 : inter.getArea() / unionArea;
        } catch (RuntimeException e) {
            return 0;
        }
    }

    /** Associates {@code cells} at {@code now} to {@code previous}. */
    public static Result associate(List<Previous> previous, List<Cell> cells, Instant now) {
        int p = previous.size();
        int c = cells.size();
        if (p == 0 || c == 0) {
            List<Integer> allCells = new ArrayList<>();
            List<Integer> allPrev = new ArrayList<>();
            for (int i = 0; i < c; i++) {
                allCells.add(i);
            }
            for (int i = 0; i < p; i++) {
                allPrev.add(i);
            }
            return new Result(List.of(), List.of(), List.of(), allCells, allPrev, false);
        }
        Geometry[] advected = new Geometry[p];
        for (int i = 0; i < p; i++) {
            Previous prev = previous.get(i);
            advected[i] = advect(prev.geometry(), prev.lon(), prev.lat(), prev.speedKmh(), prev.bearingDeg(),
                    Duration.between(prev.lastTime(), now));
        }
        double[][] iou = new double[p][c];
        double[][] cost = new double[p][c];
        for (int i = 0; i < p; i++) {
            for (int j = 0; j < c; j++) {
                iou[i][j] = iou(advected[i], cells.get(j).geometry());
                if (iou[i][j] > 0) {
                    cost[i][j] = 1 - iou[i][j];
                } else {
                    double gapMin = Math.max(2, Math.min(30, Duration.between(previous.get(i).lastTime(), now).toMinutes()));
                    double limit = MATCH_KM_PER_10_MIN * gapMin / 10.0;
                    double d = Geo.haversineKm(cells.get(j).lat(), cells.get(j).lon(), previous.get(i).lat(),
                            previous.get(i).lon());
                    cost[i][j] = d <= limit ? 1 + d / 30.0 : Hungarian.FORBIDDEN;
                }
            }
        }
        boolean greedy = p > MAX_HUNGARIAN || c > MAX_HUNGARIAN;
        int[] rowToCol = greedy ? Hungarian.greedy(cost) : Hungarian.solve(cost);
        List<Assignment> assignments = new ArrayList<>();
        List<Merge> merges = new ArrayList<>();
        List<Split> splits = new ArrayList<>();
        boolean[] cellAssigned = new boolean[c];
        boolean[] prevAssigned = new boolean[p];
        for (int i = 0; i < p; i++) {
            if (rowToCol[i] >= 0) {
                assignments.add(new Assignment(i, rowToCol[i], iou[i][rowToCol[i]]));
                cellAssigned[rowToCol[i]] = true;
                prevAssigned[i] = true;
            }
        }
        // Merges: a cell overlapping two or more advected tracks (IoU >= MERGE_IOU each). The largest-area
        // track among those still free for this cell continues; the others end with mergedInto. A track the
        // solver already gave to a different cell keeps that cell and takes no part in this merge.
        for (int j = 0; j < c; j++) {
            List<Integer> overlapping = new ArrayList<>();
            for (int i = 0; i < p; i++) {
                if (iou[i][j] >= MERGE_IOU && (!prevAssigned[i] || rowToCol[i] == j)) {
                    overlapping.add(i);
                }
            }
            if (overlapping.size() < 2) {
                continue;
            }
            int winner = largest(overlapping, advected);
            if (!(prevAssigned[winner] && rowToCol[winner] == j)) {
                List<Assignment> removed = new ArrayList<>();
                for (Assignment a : assignments) {
                    if (a.cell() == j) {
                        removed.add(a);
                        if (!overlapping.contains(a.previous())) {
                            // Paired by distance only: it becomes unmatched (missed) rather than merged.
                            prevAssigned[a.previous()] = false;
                            rowToCol[a.previous()] = -1;
                        }
                    }
                }
                assignments.removeAll(removed);
                assignments.add(new Assignment(winner, j, iou[winner][j]));
                prevAssigned[winner] = true;
                rowToCol[winner] = j;
                cellAssigned[j] = true;
            }
            for (int i : overlapping) {
                if (i != winner) {
                    merges.add(new Merge(i, winner));
                    prevAssigned[i] = true;
                    rowToCol[i] = -1;
                }
            }
        }
        // Splits: an advected track overlapping two or more cells; unmatched cells become children.
        for (Assignment a : new ArrayList<>(assignments)) {
            for (int j = 0; j < c; j++) {
                if (j != a.cell() && !cellAssigned[j] && iou[a.previous()][j] >= SPLIT_IOU) {
                    splits.add(new Split(j, a.previous()));
                    cellAssigned[j] = true;
                }
            }
        }
        List<Integer> unmatchedCells = new ArrayList<>();
        for (int j = 0; j < c; j++) {
            if (!cellAssigned[j]) {
                unmatchedCells.add(j);
            }
        }
        List<Integer> unmatchedPrev = new ArrayList<>();
        for (int i = 0; i < p; i++) {
            if (!prevAssigned[i]) {
                unmatchedPrev.add(i);
            }
        }
        return new Result(assignments, merges, splits, unmatchedCells, unmatchedPrev, greedy);
    }

    private static int largest(List<Integer> indices, Geometry[] advected) {
        int best = indices.get(0);
        for (int i : indices) {
            if (advected[i].getArea() > advected[best].getArea()) {
                best = i;
            }
        }
        return best;
    }
}
