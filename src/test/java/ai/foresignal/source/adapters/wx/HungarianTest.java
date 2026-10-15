package ai.foresignal.source.adapters.wx;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The Hungarian assignment (prompt 105): optimal matching, rectangular matrices, forbidden pairs. */
class HungarianTest {

    @Test
    @DisplayName("solves a known optimum")
    void optimal() {
        double[][] cost = {
                {4, 1, 3},
                {2, 0, 5},
                {3, 2, 2}
        };
        int[] result = Hungarian.solve(cost);
        assertThat(result).hasSize(3);
        // row 0 -> col 1 (1), row 1 -> col 0 (2), row 2 -> col 2 (2) = 5, optimal.
        assertThat(result[0]).isEqualTo(1);
        assertThat(result[1]).isEqualTo(0);
        assertThat(result[2]).isEqualTo(2);
    }

    @Test
    @DisplayName("a rectangular matrix (more columns than rows) matches each row")
    void rectangular() {
        double[][] cost = {
                {10, 1, 9, 9},
                {9, 9, 1, 9}
        };
        int[] result = Hungarian.solve(cost);
        assertThat(result[0]).isEqualTo(1);
        assertThat(result[1]).isEqualTo(2);
    }

    @Test
    @DisplayName("a matrix with more rows than columns transposes and leaves rows unmatched")
    void transposes() {
        double[][] cost = {
                {1, 9},
                {9, 1},
                {5, 5}
        };
        int[] result = Hungarian.solve(cost);
        assertThat(result[0]).isEqualTo(0);
        assertThat(result[1]).isEqualTo(1);
        assertThat(result[2]).isEqualTo(-1);
    }

    @Test
    @DisplayName("a forbidden pair is left unmatched")
    void forbidden() {
        double[][] cost = {
                {Hungarian.FORBIDDEN, 2},
                {1, Hungarian.FORBIDDEN}
        };
        int[] result = Hungarian.solve(cost);
        assertThat(result[0]).isEqualTo(1);
        assertThat(result[1]).isEqualTo(0);
    }

    @Test
    @DisplayName("greedy takes the cheapest pairs without sharing")
    void greedy() {
        double[][] cost = {
                {1, 2},
                {1, 9}
        };
        int[] result = Hungarian.greedy(cost);
        assertThat(result).containsExactly(0, 1);
    }

    @Test
    @DisplayName("empty matrices")
    void empty() {
        assertThat(Hungarian.solve(new double[0][])).isEmpty();
        assertThat(Hungarian.solve(new double[][] {{}})).containsExactly(-1);
    }
}
