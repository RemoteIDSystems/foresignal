package ai.foresignal.news;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link CoverageAnalyzer} 1.0.0 (prompt 133): balance shares, the blindspot threshold and its minimum of rated
 * groups, the state-affiliated share and skew; and the {@code coverage-divergence} detector on a planted spike.
 */
class CoverageAnalyzerTest {

    static final Instant NOW = Instant.parse("2026-10-17T12:00:00Z");

    private static List<CoverageAnalyzer.GroupInfo> groups(int left, int center, int right, int unrated) {
        List<CoverageAnalyzer.GroupInfo> out = new ArrayList<>();
        for (int i = 0; i < left; i++) {
            out.add(new CoverageAnalyzer.GroupInfo("left", "US", "national", "4", false, 1));
        }
        for (int i = 0; i < center; i++) {
            out.add(new CoverageAnalyzer.GroupInfo("center", "GB", "wire", "5", false, 3));
        }
        for (int i = 0; i < right; i++) {
            out.add(new CoverageAnalyzer.GroupInfo("right", "US", "national", "3", false, 1));
        }
        for (int i = 0; i < unrated; i++) {
            out.add(new CoverageAnalyzer.GroupInfo("unrated", null, "other", "unrated", false, 1));
        }
        return out;
    }

    @Test
    @DisplayName("balance is the share of evidence groups per bucket; articles are counted separately")
    @SuppressWarnings("unchecked")
    void balance() {
        CoverageAnalyzer.Result r = CoverageAnalyzer.analyze("story", groups(2, 1, 1, 0), false, null, NOW);
        Map<String, Object> balance = (Map<String, Object>) r.stats().get("coverageBalance");
        assertThat(balance).containsEntry("left", 0.5).containsEntry("center", 0.25).containsEntry("right", 0.25)
                .containsEntry("unrated", 0.0);
        assertThat((Map<String, Integer>) r.stats().get("articles")).containsEntry("center", 3);
        assertThat((Map<String, Integer>) r.stats().get("evidenceGroups")).containsEntry("left", 2);
        assertThat((Map<String, Integer>) r.stats().get("byCountry")).containsEntry("US", 3).containsEntry("GB", 1);
        assertThat((Map<String, Integer>) r.stats().get("byOutletType")).containsEntry("wire", 1);
        assertThat((Map<String, Integer>) r.stats().get("byFactuality")).containsEntry("4", 2);
        assertThat(r.stats().get("nRated")).isEqualTo(4);
        assertThat(((Map<String, Object>) r.stats().get("analyzer")).get("version")).isEqualTo("1.0.0");
    }

    @Test
    @DisplayName("a blindspot needs 75% of the rated groups from one side and at least 8 rated")
    void blindspotThreshold() {
        assertThat(CoverageAnalyzer.analyze("story", groups(6, 0, 2, 5), false, null, NOW).blindspot())
                .isEqualTo("left");
        assertThat(CoverageAnalyzer.analyze("story", groups(2, 0, 6, 0), false, null, NOW).blindspot())
                .isEqualTo("right");
        assertThat(CoverageAnalyzer.analyze("story", groups(5, 1, 2, 0), false, null, NOW).blindspot())
                .as("62.5% from the left").isNull();
        assertThat(CoverageAnalyzer.analyze("story", groups(7, 0, 0, 20), false, null, NOW).blindspot())
                .as("only 7 rated, unrated groups never count").isNull();
        assertThat(CoverageAnalyzer.analyze("story", groups(0, 8, 0, 0), false, null, NOW).blindspot())
                .as("center is not a side").isNull();
    }

    @Test
    @DisplayName("state-affiliated share and the skew judgment (one bucket or state outlets, at least 3 groups)")
    void stateShareAndSkew() {
        List<CoverageAnalyzer.GroupInfo> g = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            g.add(new CoverageAnalyzer.GroupInfo("unrated", "RU", "state-affiliated", "unrated", true, 1));
        }
        g.add(new CoverageAnalyzer.GroupInfo("center", "GB", "wire", "5", false, 1));
        CoverageAnalyzer.Result r = CoverageAnalyzer.analyze("hypothesis", g, false, null, NOW);
        assertThat(r.stateAffiliatedShare()).isEqualTo(0.75);
        assertThat(r.skewed()).isTrue();
        assertThat(r.stats().get("caveats").toString()).contains("SKEWED_COVERAGE");
        assertThat(CoverageAnalyzer.analyze("hypothesis", groups(3, 0, 0, 0), false, null, NOW).skewed()).isTrue();
        assertThat(CoverageAnalyzer.analyze("hypothesis", groups(2, 0, 0, 0), false, null, NOW).skewed())
                .as("too few groups").isFalse();
        assertThat(CoverageAnalyzer.analyze("hypothesis", groups(2, 1, 1, 0), false, null, NOW).skewed()).isFalse();
    }

    @Test
    @DisplayName("an empty input has zero shares and no judgment")
    void empty() {
        CoverageAnalyzer.Result r = CoverageAnalyzer.analyze("situation", List.of(), true, 1.0, NOW);
        assertThat(r.groups()).isZero();
        assertThat(r.blindspot()).isNull();
        assertThat(r.skewed()).isFalse();
        assertThat(r.stats().get("corroborated")).isEqualTo(true);
        assertThat(r.stats().get("corroboratedShare")).isEqualTo(1.0);
    }

    @Test
    @DisplayName("factuality bands round the median to 1..5")
    void bands() {
        assertThat(CoverageAnalyzer.factualityBand(4.4)).isEqualTo("4");
        assertThat(CoverageAnalyzer.factualityBand(4.5)).isEqualTo("5");
        assertThat(CoverageAnalyzer.factualityBand(0.2)).isEqualTo("1");
        assertThat(CoverageAnalyzer.factualityBand(null)).isEqualTo("unrated");
    }

    private static Map<String, Map<Instant, Integer>> series(Instant day, int leftToday, int rightToday) {
        Map<String, Map<Instant, Integer>> out = new LinkedHashMap<>();
        for (String bucket : List.of("left", "center", "right")) {
            Map<Instant, Integer> s = new TreeMap<>();
            for (int i = 1; i <= 28; i++) {
                s.put(day.minusSeconds(86_400L * i), 2 + (i % 2));
            }
            s.put(day, bucket.equals("left") ? leftToday : bucket.equals("right") ? rightToday : 2);
            out.put(bucket, s);
        }
        return out;
    }

    @Test
    @DisplayName("divergence: a planted spike in one bucket is found; spikes in two buckets or a short baseline are not")
    void divergence() {
        Instant day = Instant.parse("2026-10-16T00:00:00Z");
        NewsCoverageJob.Divergence d = NewsCoverageJob.detect(series(day, 12, 3), day);
        assertThat(d).isNotNull();
        assertThat(d.bucket()).isEqualTo("left");
        assertThat(d.z()).isGreaterThanOrEqualTo(3.0);
        assertThat(d.otherZ()).isLessThan(3.0);
        assertThat(NewsCoverageJob.detect(series(day, 12, 12), day)).as("both sides rise").isNull();
        assertThat(NewsCoverageJob.detect(series(day, 3, 3), day)).as("no spike").isNull();
        Map<String, Map<Instant, Integer>> shortBaseline = new LinkedHashMap<>();
        for (String b : List.of("left", "center", "right")) {
            shortBaseline.put(b, new TreeMap<>(Map.of(day, 20, day.minusSeconds(86_400), 1)));
        }
        assertThat(NewsCoverageJob.detect(shortBaseline, day)).as("fewer than 14 baseline days").isNull();
    }
}
