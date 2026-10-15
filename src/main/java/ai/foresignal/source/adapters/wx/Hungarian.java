package ai.foresignal.source.adapters.wx;

import java.util.Arrays;

/**
 * The Hungarian algorithm for the rectangular linear assignment problem (prompt 105): given a cost
 * matrix, choose a one-to-one matching of rows to columns that minimises the total cost. It runs in
 * O(n³) with the potentials method (rows must not exceed columns; the matrix is transposed when they
 * do). A cost at or above {@link #FORBIDDEN} is treated as forbidden and left unmatched.
 *
 * <p>An in-house implementation, so no new dependency (Conventions "statistics libraries" keeps the
 * build to Apache-2.0 libraries) and the storm tracker stays reproducible.
 */
public final class Hungarian {

    /** A cost at or above this is a forbidden pair. */
    public static final double FORBIDDEN = 1e6;

    private Hungarian() {}

    /**
     * Solves the assignment.
     *
     * @param cost the cost matrix ({@code rows × cols}); may be rectangular
     * @return one column per row, or −1 for an unmatched row (only when the column count is smaller,
     *     or every pairing is forbidden)
     */
    public static int[] solve(double[][] cost) {
        if (cost.length == 0) {
            return new int[0];
        }
        int rows = cost.length;
        int cols = cost[0].length;
        int[] result = new int[rows];
        Arrays.fill(result, -1);
        if (cols == 0) {
            return result;
        }
        boolean transposed = rows > cols;
        double[][] a = transposed ? transpose(cost) : cost;
        int n = a.length;
        int m = a[0].length;
        double[] u = new double[n + 1];
        double[] v = new double[m + 1];
        int[] p = new int[m + 1];
        int[] way = new int[m + 1];
        for (int i = 1; i <= n; i++) {
            p[0] = i;
            int j0 = 0;
            double[] minv = new double[m + 1];
            Arrays.fill(minv, Double.POSITIVE_INFINITY);
            boolean[] used = new boolean[m + 1];
            do {
                used[j0] = true;
                int i0 = p[j0];
                int j1 = -1;
                double delta = Double.POSITIVE_INFINITY;
                for (int j = 1; j <= m; j++) {
                    if (used[j]) {
                        continue;
                    }
                    double cur = a[i0 - 1][j - 1] - u[i0] - v[j];
                    if (cur < minv[j]) {
                        minv[j] = cur;
                        way[j] = j0;
                    }
                    if (minv[j] < delta) {
                        delta = minv[j];
                        j1 = j;
                    }
                }
                for (int j = 0; j <= m; j++) {
                    if (used[j]) {
                        u[p[j]] += delta;
                        v[j] -= delta;
                    } else {
                        minv[j] -= delta;
                    }
                }
                j0 = j1;
            } while (p[j0] != 0);
            do {
                int j1 = way[j0];
                p[j0] = p[j1];
                j0 = j1;
            } while (j0 != 0);
        }
        for (int j = 1; j <= m; j++) {
            if (p[j] == 0) {
                continue;
            }
            int row = p[j] - 1;
            int col = j - 1;
            double value = transposed ? cost[col][row] : cost[row][col];
            if (value >= FORBIDDEN) {
                continue;
            }
            if (transposed) {
                result[col] = row;
            } else {
                result[row] = col;
            }
        }
        return result;
    }

    private static double[][] transpose(double[][] cost) {
        int rows = cost.length;
        int cols = cost[0].length;
        double[][] out = new double[cols][rows];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                out[c][r] = cost[r][c];
            }
        }
        return out;
    }

    /**
     * A greedy assignment by cost, used when the matrix is larger than the Hungarian budget: sort the
     * pairs by cost and take each unused row and column.
     *
     * @return one column per row, or −1 when unmatched
     */
    public static int[] greedy(double[][] cost) {
        int rows = cost.length;
        int[] result = new int[rows];
        Arrays.fill(result, -1);
        if (rows == 0 || cost[0].length == 0) {
            return result;
        }
        int cols = cost[0].length;
        record Pair(double cost, int row, int col) {}
        java.util.List<Pair> pairs = new java.util.ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cost[r][c] < FORBIDDEN) {
                    pairs.add(new Pair(cost[r][c], r, c));
                }
            }
        }
        pairs.sort(java.util.Comparator.comparingDouble(Pair::cost));
        boolean[] usedRow = new boolean[rows];
        boolean[] usedCol = new boolean[cols];
        for (Pair pair : pairs) {
            if (usedRow[pair.row()] || usedCol[pair.col()]) {
                continue;
            }
            usedRow[pair.row()] = true;
            usedCol[pair.col()] = true;
            result[pair.row()] = pair.col();
        }
        return result;
    }
}
