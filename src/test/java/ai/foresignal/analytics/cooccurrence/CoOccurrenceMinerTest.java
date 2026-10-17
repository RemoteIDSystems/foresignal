package ai.foresignal.analytics.cooccurrence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The co-occurrence miner (prompt 119): a planted joint exceedance survives Benjamini–Hochberg; independent noise
 * across many regions does not (fixed seed); uncovered days and short baselines yield no test.
 */
class CoOccurrenceMinerTest {

    private static CoOccurrenceMiner.Daily daily(Random random, int n, double lambda, Double lastDay) {
        double[] v = new double[n];
        boolean[] ok = new boolean[n];
        Arrays.fill(ok, true);
        for (int t = 0; t < n; t++) {
            v[t] = poisson(random, lambda);
        }
        if (lastDay != null) {
            v[n - 1] = lastDay;
        }
        return new CoOccurrenceMiner.Daily(v, ok);
    }

    @Test
    @DisplayName("a planted joint exceedance survives the FDR control with a large lift")
    void planted() {
        Random random = new Random(119);
        Map<String, Map<String, CoOccurrenceMiner.Daily>> counts = new TreeMap<>();
        CoOccurrenceMiner.Daily conflict = daily(random, 400, 4, 30.0);
        CoOccurrenceMiner.Daily ais = daily(random, 400, 3, 25.0);
        // The lift is observed over expected joint days over the history, so a single joint day against ~4% exceedance
        // rates caps it near 2; the association is planted on three earlier days too (weekdays other than the mined
        // day's, so they stay out of its baseline).
        for (int day : new int[] {150, 220, 300}) {
            conflict.values()[day] = 30;
            ais.values()[day] = 25;
        }
        counts.put("YE", Map.of("conflict", conflict, "ais-behaviour", ais, "outage", daily(random, 400, 2, null)));
        for (int r = 0; r < 20; r++) {
            counts.put("R" + (char) ('A' + r), Map.of("conflict", daily(random, 400, 4, null), "fire",
                    daily(random, 400, 5, null)));
        }
        List<CoOccurrenceMiner.Test> tests = CoOccurrenceMiner.mine(counts, 0.05);
        CoOccurrenceMiner.Test hit = tests.stream().filter(t -> t.region().equals("YE")
                && t.familyA().equals("ais-behaviour") && t.familyB().equals("conflict")).findFirst().orElseThrow();
        assertThat(hit.survives()).isTrue();
        assertThat(hit.adjustedP()).isLessThan(0.001);
        assertThat(hit.lift()).isGreaterThan(3);
        assertThat(hit.severity()).isEqualTo("significant");
        assertThat(hit.jointDays()).isGreaterThanOrEqualTo(1);
        assertThat(tests.stream().filter(CoOccurrenceMiner.Test::survives).count()).as("only the planted pair")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("independent noise across many regions yields no survivors (fixed seed)")
    void noise() {
        Random random = new Random(7);
        Map<String, Map<String, CoOccurrenceMiner.Daily>> counts = new TreeMap<>();
        for (int r = 0; r < 60; r++) {
            counts.put("N" + r, Map.of("conflict", daily(random, 400, 4, null), "fire", daily(random, 400, 6, null),
                    "seismic", daily(random, 400, 1, null)));
        }
        List<CoOccurrenceMiner.Test> tests = CoOccurrenceMiner.mine(counts, 0.05);
        assertThat(tests).hasSize(180);
        assertThat(tests).noneMatch(CoOccurrenceMiner.Test::survives);
    }

    @Test
    @DisplayName("an uncovered day or a baseline shorter than eight weeks gives no p-value")
    void coverage() {
        double[] v = new double[100];
        boolean[] ok = new boolean[100];
        Arrays.fill(v, 3);
        Arrays.fill(ok, true);
        ok[99] = false;
        double[][] p = CoOccurrenceMiner.pValues(new CoOccurrenceMiner.Daily(v, ok));
        assertThat(p[0][99]).isNaN();
        assertThat(p[0][50]).as("fewer than eight earlier weeks at day 50").isNaN();
        assertThat(p[0][98]).isBetween(0.0, 1.0);
        assertThat(CoOccurrenceMiner.mine(Map.of("XX", Map.of("a", new CoOccurrenceMiner.Daily(v, ok), "b",
                new CoOccurrenceMiner.Daily(v, ok))), 0.05)).isEmpty();
    }

    private static int poisson(Random random, double lambda) {
        double l = Math.exp(-lambda);
        int k = 0;
        double p = 1;
        do {
            k++;
            p *= random.nextDouble();
        } while (p > l);
        return k - 1;
    }
}
