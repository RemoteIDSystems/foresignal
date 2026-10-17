package ai.foresignal.pol;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DBSCAN over stay centroids (prompt 114), written in house.
 *
 * <p>Points are planar coordinates in meters ({@link LocalFrame}). A point's neighborhood is every point within
 * {@code eps} (inclusive), itself included; a core point has at least {@code minPts} neighbors. Clusters grow from
 * core points through density reachability; points reachable from no core point are noise ({@link #NOISE}).
 *
 * <p>Deterministic: points are visited in input order (the caller passes stays in arrive order), neighborhoods are
 * expanded in index order, and clusters are numbered 0, 1, … in order of discovery. Neighborhood queries use a grid
 * index with cell size {@code eps} (the 3 × 3 surrounding cells hold every candidate); {@link #cluster} with
 * {@code grid=false} queries by brute force and gives identical labels.
 */
public final class Dbscan {

    /** The label of a noise point. */
    public static final int NOISE = -1;

    private static final int UNVISITED = -2;

    private Dbscan() {}

    /**
     * Clusters {@code points} with the grid index.
     *
     * @param points {@code {x, y}} in meters
     * @param eps the neighborhood radius, meters
     * @param minPts the minimum neighborhood size of a core point (the point itself included)
     * @return the label of each point: a cluster id from 0, or {@link #NOISE}
     */
    public static int[] cluster(double[][] points, double eps, int minPts) {
        return cluster(points, eps, minPts, true);
    }

    /**
     * Clusters {@code points}.
     *
     * @param grid true for the grid index, false for brute-force neighborhoods (tests)
     */
    public static int[] cluster(double[][] points, double eps, int minPts, boolean grid) {
        int n = points.length;
        int[] labels = new int[n];
        Arrays.fill(labels, UNVISITED);
        if (n == 0) {
            return labels;
        }
        Neighbors neighbors = grid ? new GridNeighbors(points, eps) : new BruteNeighbors(points, eps);
        int next = 0;
        for (int i = 0; i < n; i++) {
            if (labels[i] != UNVISITED) {
                continue;
            }
            int[] seeds = neighbors.of(i);
            if (seeds.length < minPts) {
                labels[i] = NOISE;
                continue;
            }
            int cluster = next++;
            labels[i] = cluster;
            Deque<Integer> queue = new ArrayDeque<>();
            for (int s : seeds) {
                if (s != i) {
                    queue.add(s);
                }
            }
            while (!queue.isEmpty()) {
                int q = queue.poll();
                if (labels[q] == NOISE) {
                    labels[q] = cluster;
                }
                if (labels[q] != UNVISITED) {
                    continue;
                }
                labels[q] = cluster;
                int[] reach = neighbors.of(q);
                if (reach.length >= minPts) {
                    for (int r : reach) {
                        if (labels[r] == UNVISITED || labels[r] == NOISE) {
                            queue.add(r);
                        }
                    }
                }
            }
        }
        return labels;
    }

    /** Neighborhood queries. */
    interface Neighbors {

        /** The indexes within eps of point {@code i}, itself included, ascending. */
        int[] of(int i);
    }

    /** Brute force: every pair. */
    static final class BruteNeighbors implements Neighbors {

        private final double[][] points;
        private final double eps2;

        BruteNeighbors(double[][] points, double eps) {
            this.points = points;
            this.eps2 = eps * eps;
        }

        @Override
        public int[] of(int i) {
            List<Integer> out = new ArrayList<>();
            for (int j = 0; j < points.length; j++) {
                if (distance2(points[i], points[j]) <= eps2) {
                    out.add(j);
                }
            }
            return out.stream().mapToInt(Integer::intValue).toArray();
        }
    }

    /** A uniform grid with cell size eps. */
    static final class GridNeighbors implements Neighbors {

        private final double[][] points;
        private final double eps;
        private final double eps2;
        private final Map<Long, List<Integer>> cells = new HashMap<>();

        GridNeighbors(double[][] points, double eps) {
            this.points = points;
            this.eps = eps;
            this.eps2 = eps * eps;
            for (int i = 0; i < points.length; i++) {
                cells.computeIfAbsent(key(cell(points[i][0]), cell(points[i][1])), k -> new ArrayList<>()).add(i);
            }
        }

        private long cell(double v) {
            return (long) Math.floor(v / eps);
        }

        private static long key(long cx, long cy) {
            return (cx << 32) ^ (cy & 0xffffffffL);
        }

        @Override
        public int[] of(int i) {
            long cx = cell(points[i][0]);
            long cy = cell(points[i][1]);
            List<Integer> out = new ArrayList<>();
            for (long dx = -1; dx <= 1; dx++) {
                for (long dy = -1; dy <= 1; dy++) {
                    List<Integer> members = cells.get(key(cx + dx, cy + dy));
                    if (members == null) {
                        continue;
                    }
                    for (int j : members) {
                        if (distance2(points[i], points[j]) <= eps2) {
                            out.add(j);
                        }
                    }
                }
            }
            int[] sorted = out.stream().mapToInt(Integer::intValue).toArray();
            Arrays.sort(sorted);
            return sorted;
        }
    }

    static double distance2(double[] a, double[] b) {
        double dx = a[0] - b[0];
        double dy = a[1] - b[1];
        return dx * dx + dy * dy;
    }
}
