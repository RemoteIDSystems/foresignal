package ai.foresignal.pol;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** In-house DBSCAN (prompt 114): clusters, noise, the eps boundary, deterministic labels, grid equals brute force. */
class DbscanTest {

    static double[][] points(double[]... p) {
        return p;
    }

    @Test
    @DisplayName("two dense groups become clusters 0 and 1 in discovery order; an isolated point is noise")
    void knownClusters() {
        double[][] pts = points(
                new double[] {0, 0}, new double[] {10, 0}, new double[] {0, 10},
                new double[] {5000, 5000}, new double[] {5010, 5000}, new double[] {5000, 5010}, new double[] {5005, 5005},
                new double[] {20000, 0});
        int[] labels = Dbscan.cluster(pts, 150, 3);
        assertThat(labels).containsExactly(0, 0, 0, 1, 1, 1, 1, Dbscan.NOISE);
    }

    @Test
    @DisplayName("a neighborhood smaller than minPts is noise")
    void noise() {
        int[] labels = Dbscan.cluster(points(new double[] {0, 0}, new double[] {100, 0}), 150, 3);
        assertThat(labels).containsExactly(Dbscan.NOISE, Dbscan.NOISE);
        assertThat(Dbscan.cluster(new double[0][], 150, 3)).isEmpty();
    }

    @Test
    @DisplayName("a point exactly eps away is a neighbor; just beyond it is not")
    void epsBoundary() {
        assertThat(Dbscan.cluster(points(new double[] {0, 0}, new double[] {150, 0}, new double[] {300, 0}), 150, 2))
                .containsExactly(0, 0, 0);
        assertThat(Dbscan.cluster(points(new double[] {0, 0}, new double[] {150.001, 0}), 150, 2))
                .containsExactly(Dbscan.NOISE, Dbscan.NOISE);
    }

    @Test
    @DisplayName("a border point reachable from a core point joins the cluster")
    void borderPoint() {
        // The first point has three neighbors (itself included) where four are needed, so it is noise when visited;
        // the core point next to it then claims it as a border point.
        int[] labels = Dbscan.cluster(points(new double[] {-145, 0}, new double[] {0, 0}, new double[] {10, 0},
                new double[] {0, 10}), 150, 4);
        assertThat(labels).containsExactly(0, 0, 0, 0);
    }

    @Test
    @DisplayName("labels are deterministic: the same input gives the same labels")
    void deterministic() {
        double[][] pts = random(new Random(7), 300);
        assertThat(Dbscan.cluster(pts, 150, 3)).containsExactly(Dbscan.cluster(pts, 150, 3));
    }

    @Test
    @DisplayName("the grid index gives exactly the brute-force labels on random data")
    void gridEqualsBruteForce() {
        Random random = new Random(42);
        for (int round = 0; round < 20; round++) {
            double[][] pts = random(random, 50 + random.nextInt(400));
            double eps = 50 + random.nextInt(300);
            int minPts = 1 + random.nextInt(5);
            assertThat(Dbscan.cluster(pts, eps, minPts, true)).as("round %d", round)
                    .containsExactly(Dbscan.cluster(pts, eps, minPts, false));
        }
    }

    @Test
    @DisplayName("negative coordinates fall into the right grid cells")
    void negativeCells() {
        double[][] pts = points(new double[] {-1, -1}, new double[] {1, 1}, new double[] {-149, 0});
        assertThat(Dbscan.cluster(pts, 150, 3, true)).containsExactly(Dbscan.cluster(pts, 150, 3, false));
    }

    static double[][] random(Random random, int n) {
        List<double[]> out = new ArrayList<>();
        // A few dense centers plus uniform background noise.
        double[][] centers = {{0, 0}, {2000, 500}, {-1500, -2500}};
        for (int i = 0; i < n; i++) {
            if (random.nextDouble() < 0.7) {
                double[] c = centers[random.nextInt(centers.length)];
                out.add(new double[] {c[0] + random.nextGaussian() * 120, c[1] + random.nextGaussian() * 120});
            } else {
                out.add(new double[] {random.nextDouble() * 10_000 - 5000, random.nextDouble() * 10_000 - 5000});
            }
        }
        return out.toArray(new double[0][]);
    }
}
