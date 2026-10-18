package ai.foresignal.benchmark.scenario;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.analytics.series.SeriesData;
import ai.foresignal.analytics.series.SeriesFrequency;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.backtest.PrecursorTester;
import ai.foresignal.evidence.ObservationFacts;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link ScenarioGenerator} (prompt 123, {@code bench-scenarios} 1.0.0): every scenario is deterministic per seed,
 * another seed gives other data, and each truth record matches what was generated.
 */
class ScenarioGeneratorTest {

    static final long SEED = 1_230_001L;

    static double[] values(InMemorySeries s, Instant asOf) {
        double[] out = new double[s.size()];
        for (int i = 0; i < s.size(); i++) {
            out[i] = s.valueAsOf(i, asOf);
        }
        return out;
    }

    static double mean(double[] v, int from, int to) {
        double sum = 0;
        for (int i = from; i < to; i++) {
            sum += v[i];
        }
        return sum / (to - from);
    }

    @Test
    @DisplayName("Rng: the same seed and parts give the same draws, other parts other draws")
    void rng() {
        Rng a = Rng.of(SEED, "x", "1");
        Rng b = Rng.of(SEED, "x", "1");
        Rng c = Rng.of(SEED, "x", "2");
        boolean differs = false;
        for (int i = 0; i < 50; i++) {
            double ua = a.uniform();
            assertThat(ua).isEqualTo(b.uniform());
            differs |= ua != c.uniform();
            assertThat(a.poisson(5)).isEqualTo(b.poisson(5));
            assertThat(a.normal()).isEqualTo(b.normal());
            assertThat(a.thin(10, 0.5)).isEqualTo(b.thin(10, 0.5));
        }
        assertThat(differs).isTrue();
        assertThat(a.poisson(0)).isZero();
        assertThat(a.poisson(-1)).isZero();
    }

    @Test
    @DisplayName("daily counts are deterministic per seed for every series scenario, and other seeds differ")
    void dailyDeterministic() {
        for (ScenarioType type : List.of(ScenarioType.NULL_SEASONAL, ScenarioType.STEP_CHANGE, ScenarioType.SPIKE,
                ScenarioType.COVERAGE_OUTAGE, ScenarioType.LATE_REVISION)) {
            ScenarioGenerator.SeriesScenario a = ScenarioGenerator.dailyCounts(type, SEED, 3);
            ScenarioGenerator.SeriesScenario b = ScenarioGenerator.dailyCounts(type, SEED, 3);
            ScenarioGenerator.SeriesScenario c = ScenarioGenerator.dailyCounts(type, SEED + 1, 3);
            assertThat(values(a.series(), null)).as(type.wire()).containsExactly(values(b.series(), null));
            assertThat(a.truth()).isEqualTo(b.truth());
            assertThat(values(a.series(), null)).isNotEqualTo(values(c.series(), null));
            assertThat(a.series().size()).isEqualTo(ScenarioGenerator.HISTORY_DAYS + ScenarioGenerator.EVAL_DAYS);
            assertThat(a.evalFrom()).isEqualTo(ScenarioGenerator.HISTORY_DAYS);
        }
    }

    @Test
    @DisplayName("step-change: the planted shift is at the recorded index, of the recorded direction and size")
    void stepChange() {
        for (int r = 0; r < 4; r++) {
            ScenarioGenerator.SeriesScenario s = ScenarioGenerator.dailyCounts(ScenarioType.STEP_CHANGE, SEED, r);
            int c = ((Number) s.truth().get("changeIndex")).intValue();
            assertThat(c).isBetween(s.evalFrom(), s.series().size() - 1);
            double[] v = values(s.series(), null);
            double before = mean(v, c - 42, c);
            double after = mean(v, c, s.series().size());
            boolean up = "up".equals(s.truth().get("direction"));
            if (up) {
                assertThat(after).isGreaterThan(before * 1.2);
            } else {
                assertThat(after).isLessThan(before * 0.8);
            }
            assertThat(s.truth().get("effectSd")).isIn(2.0, 3.0);
        }
        assertThat(ScenarioGenerator.dailyCounts(ScenarioType.NULL_SEASONAL, SEED, 0).truth())
                .doesNotContainKeys("changeIndex", "spikeIndexes", "outageFromIndex", "revisedFromIndex");
    }

    @Test
    @DisplayName("spike: the two recorded days carry the burst")
    void spike() {
        ScenarioGenerator.SeriesScenario s = ScenarioGenerator.dailyCounts(ScenarioType.SPIKE, SEED, 1);
        @SuppressWarnings("unchecked")
        List<Number> idx = (List<Number>) s.truth().get("spikeIndexes");
        double[] v = values(s.series(), null);
        double baseline = mean(v, idx.get(0).intValue() - 28, idx.get(0).intValue());
        assertThat(v[idx.get(0).intValue()]).isGreaterThan(2 * baseline);
        assertThat(v[idx.get(1).intValue()]).isGreaterThan(2 * baseline);
    }

    @Test
    @DisplayName("coverage-outage: only the recorded span is uncovered (gap variant) or thinly detectable")
    void outage() {
        for (int r = 0; r < 2; r++) {
            ScenarioGenerator.SeriesScenario s = ScenarioGenerator.dailyCounts(ScenarioType.COVERAGE_OUTAGE, SEED, r);
            int from = ((Number) s.truth().get("outageFromIndex")).intValue();
            int to = ((Number) s.truth().get("outageToIndex")).intValue();
            boolean gap = "gap".equals(s.truth().get("variant"));
            assertThat(gap).isEqualTo(r % 2 == 0);
            for (int i = 0; i < s.series().size(); i++) {
                boolean inside = i >= from && i < to;
                assertThat(s.series().coverageOf(i)).isEqualTo(inside && gap ? 0.3 : 1.0);
                assertThat(s.series().detectabilityOf(i)).isEqualTo(inside && !gap ? 0.3 : 1.0);
            }
            assertThat(s.series().canSeeDecline(from, to).ok()).isFalse();
            assertThat(s.series().canSeeDecline(0, from).ok()).isTrue();
            SeriesData read = s.series().read(from, to, null);
            assertThat(read.grid().gap()).as("uncovered buckets are gaps").containsOnly(gap);
        }
    }

    @Test
    @DisplayName("late-revision: revised buckets have a second version known 30 days later, invisible before then")
    void lateRevision() {
        ScenarioGenerator.SeriesScenario s = ScenarioGenerator.dailyCounts(ScenarioType.LATE_REVISION, SEED, 0);
        int from = ((Number) s.truth().get("revisedFromIndex")).intValue();
        int to = ((Number) s.truth().get("revisedToIndex")).intValue();
        InMemorySeries series = s.series();
        for (int i = 0; i < series.size(); i++) {
            assertThat(series.versions(i)).hasSize(i >= from && i < to ? 2 : 1);
        }
        InMemorySeries.Version first = series.versions(from).get(0);
        InMemorySeries.Version revised = series.versions(from).get(1);
        assertThat(revised.knownAt()).isEqualTo(series.endOf(from).plus(Duration.ofDays(30)));
        assertThat(series.valueAsOf(from, revised.knownAt().minusSeconds(1))).isEqualTo(first.value());
        assertThat(series.valueAsOf(from, revised.knownAt())).isEqualTo(revised.value());
        SeriesData early = series.read(from - 5, to, revised.knownAt().minusSeconds(1));
        for (int i = 0; i < early.grid().size(); i++) {
            Instant known = early.grid().knownAt()[i];
            assertThat(known == null || !known.isAfter(revised.knownAt().minusSeconds(1))).isTrue();
        }
    }

    @Test
    @DisplayName("in-memory series: reads honor asOf, coverage and bucket bounds like the 119 reader")
    void inMemorySeries() {
        InMemorySeries s = new InMemorySeries(SeriesRef.rollup("day", "*", "k", "XB-10"), SeriesFrequency.DAY,
                ScenarioGenerator.EPOCH, 5, "count");
        for (int i = 0; i < 5; i++) {
            s.publish(i, i, s.endOf(i));
        }
        s.coverage(2, 0.5);
        SeriesData all = s.read(0, 5, null);
        assertThat(all.counts()).isTrue();
        assertThat(all.grid().gap()).containsExactly(false, false, true, false, false);
        assertThat(all.gaps()).hasSize(1);
        SeriesData asOf = s.read(0, 5, s.endOf(1));
        assertThat(asOf.points()).hasSize(2);
        assertThat(s.indexOf(s.startOf(3).plusSeconds(10))).isEqualTo(3);
        assertThat(s.read(-3, 99, null).grid().size()).isEqualTo(5);
        assertThat(Double.isNaN(s.valueAsOf(4, s.startOf(0)))).isTrue();
    }

    @Test
    @DisplayName("hourly counts: deterministic; spike and outage at the recorded hours")
    void hourly() {
        ScenarioGenerator.HourlyScenario a = ScenarioGenerator.hourlyCounts(ScenarioType.SPIKE, SEED, 0);
        ScenarioGenerator.HourlyScenario b = ScenarioGenerator.hourlyCounts(ScenarioType.SPIKE, SEED, 0);
        assertThat(a.counts()).containsExactly(b.counts());
        int spike = ((Number) a.truth().get("spikeFromIndex")).intValue();
        assertThat(spike).isGreaterThanOrEqualTo(a.evalFrom());
        int burst = a.counts()[spike] + a.counts()[spike + 1] + a.counts()[spike + 2];
        assertThat(burst).isGreaterThan(3 * 2 * (int) ScenarioGenerator.HOURLY_MEAN / 2);
        ScenarioGenerator.HourlyScenario o = ScenarioGenerator.hourlyCounts(ScenarioType.COVERAGE_OUTAGE, SEED, 0);
        int outage = ((Number) o.truth().get("outageFromIndex")).intValue();
        for (int h = 0; h < o.coverage().length; h++) {
            assertThat(o.coverage()[h]).isEqualTo(h >= outage && h < outage + 12 ? 0.3 : 1.0);
        }
        assertThat(ScenarioGenerator.hourlyCounts(ScenarioType.NULL_SEASONAL, SEED, 0).truth())
                .doesNotContainKeys("spikeFromIndex", "outageFromIndex");
    }

    @Test
    @DisplayName("pre-event-move: six planted and six placebo events, distinct, with abnormal returns before the planted")
    void preEventMove() {
        ScenarioGenerator.MarketScenario a = ScenarioGenerator.preEventMove(SEED, 0);
        ScenarioGenerator.MarketScenario b = ScenarioGenerator.preEventMove(SEED, 0);
        assertThat(a.trueEvents()).isEqualTo(b.trueEvents());
        assertThat(values(a.series(), null)).containsExactly(values(b.series(), null));
        assertThat(a.trueEvents()).hasSize(6);
        assertThat(a.placeboEvents()).hasSize(6);
        Set<Instant> all = new HashSet<>(a.trueEvents());
        all.addAll(a.placeboEvents());
        assertThat(all).hasSize(12);
        double[] p = values(a.series(), null);
        double plantedReturn = 0;
        double placeboReturn = 0;
        for (Instant e : a.trueEvents()) {
            int t = a.series().indexOf(e);
            plantedReturn += Math.log(p[t - 1] / p[t - 6]);
        }
        for (Instant e : a.placeboEvents()) {
            int t = a.series().indexOf(e);
            placeboReturn += Math.log(p[t - 1] / p[t - 6]);
        }
        assertThat(plantedReturn / 6).isGreaterThan(0.1);
        assertThat(Math.abs(placeboReturn / 6)).isLessThan(0.1);
        assertThat(a.truth().get("trueEvents")).isEqualTo(a.trueEvents().stream().map(Instant::toString).toList());
    }

    @Test
    @DisplayName("co-occurrence: four planted joint spikes with alphabetical pairs, none on null data")
    void coOccurrence() {
        ScenarioGenerator.CoOccurrenceScenario s = ScenarioGenerator.coOccurrence(ScenarioType.SPIKE, SEED, 0);
        assertThat(s.planted()).hasSize(4);
        for (String key : s.planted()) {
            String[] parts = key.split("\\|");
            int day = Integer.parseInt(parts[0]);
            assertThat(day).isGreaterThanOrEqualTo(s.evalFrom());
            assertThat(parts[2].compareTo(parts[3])).isNegative();
            double[] a = s.counts().get(parts[1]).get(parts[2]);
            assertThat(a[day]).isGreaterThan(mean(a, day - 28, day));
        }
        assertThat(ScenarioGenerator.coOccurrence(ScenarioType.NULL_SEASONAL, SEED, 0).planted()).isEmpty();
        assertThat(ScenarioGenerator.coOccurrence(ScenarioType.SPIKE, SEED, 0).counts().get("XA").get("fam-a"))
                .containsExactly(s.counts().get("XA").get("fam-a"));
    }

    @Test
    @DisplayName("true-precursor: consequents follow antecedents at the planted lag about as often as planted")
    void truePrecursor() {
        ScenarioGenerator.EventStreamScenario s = ScenarioGenerator.eventStreams(ScenarioType.TRUE_PRECURSOR, SEED, 0);
        assertThat(s.decoys()).hasSize(4);
        int followed = 0;
        for (PrecursorTester.Occurrence a : s.antecedents()) {
            for (PrecursorTester.Occurrence c : s.consequents()) {
                Duration lag = Duration.between(a.time(), c.time());
                if (lag.compareTo(Duration.ofHours(42)) >= 0 && lag.compareTo(Duration.ofHours(54)) <= 0) {
                    followed++;
                    break;
                }
            }
        }
        double share = (double) followed / s.antecedents().size();
        assertThat(share).isBetween(0.45, 0.85);
        assertThat(((Number) s.truth().get("plantedFollowers")).intValue()).isLessThanOrEqualTo(followed);
        assertThat(s.truth().get("plantedHitRate")).isEqualTo(ScenarioGenerator.PLANTED_HIT_RATE);
        assertThat(ScenarioGenerator.eventStreams(ScenarioType.TRUE_PRECURSOR, SEED, 0).antecedents())
                .isEqualTo(s.antecedents());
        assertThat(ScenarioGenerator.eventStreams(ScenarioType.COMMON_DRIVER, SEED, 0).truth().get("leadLag"))
                .isEqualTo("none");
    }

    @Test
    @DisplayName("common-driver series share the weekly pattern; null series have their own")
    void seriesPair() {
        ScenarioGenerator.SeriesPairScenario c = ScenarioGenerator.seriesPair(ScenarioType.COMMON_DRIVER, SEED, 0);
        assertThat(c.antecedent()).hasSize(ScenarioGenerator.PRECURSOR_DAYS);
        assertThat(correlation(weekdayMeans(c.antecedent()), weekdayMeans(c.consequent()))).isGreaterThan(0.8);
        ScenarioGenerator.SeriesPairScenario n = ScenarioGenerator.seriesPair(ScenarioType.NULL_SEASONAL, SEED, 0);
        assertThat(correlation(weekdayMeans(n.antecedent()), weekdayMeans(n.consequent()))).isLessThan(0.8);
        assertThat(ScenarioGenerator.seriesPair(ScenarioType.COMMON_DRIVER, SEED, 0).antecedent())
                .containsExactly(c.antecedent());
    }

    static double[] weekdayMeans(double[] v) {
        double[] out = new double[7];
        int[] n = new int[7];
        for (int t = 0; t < v.length; t++) {
            out[t % 7] += v[t];
            n[t % 7]++;
        }
        for (int d = 0; d < 7; d++) {
            out[d] /= n[d];
        }
        return out;
    }

    static double correlation(double[] a, double[] b) {
        double ma = mean(a, 0, a.length);
        double mb = mean(b, 0, b.length);
        double sab = 0;
        double saa = 0;
        double sbb = 0;
        for (int i = 0; i < a.length; i++) {
            sab += (a[i] - ma) * (b[i] - mb);
            saa += (a[i] - ma) * (a[i] - ma);
            sbb += (b[i] - mb) * (b[i] - mb);
        }
        return sab / Math.sqrt(saa * sbb);
    }

    @Test
    @DisplayName("duplicate-echo: one to three originals, ten echoes of the agency's report")
    void duplicateEcho() {
        for (int r = 0; r < 3; r++) {
            ScenarioGenerator.EchoScenario s = ScenarioGenerator.duplicateEcho(SEED, r);
            assertThat(s.originals()).hasSize(1 + r % 3);
            assertThat(s.echoes()).hasSize(ScenarioGenerator.ECHOES).allMatch(ScenarioGenerator.EchoMember::echo);
            assertThat(s.truth().get("expectedGroups")).isEqualTo(1 + r % 3);
            assertThat(s.originals().get(0).sourceId()).isEqualTo("bench-src-agency");
            assertThat(s.echoes().stream().filter(e -> e.citation() != null)).allMatch(e -> e.citation()
                    .contains("agency.bench.example"));
            assertThat(ScenarioGenerator.duplicateEcho(SEED, r)).isEqualTo(s);
        }
        assertThat(ScenarioGenerator.echoRegistryDocument()).containsKeys("originators", "sources");
    }

    @Test
    @DisplayName("canonical events: seq order, aligned truth, every planted event seen by two or three sources")
    void canonicalEvents() {
        ScenarioGenerator.LinkingScenario s = ScenarioGenerator.canonicalEvents(SEED, 0);
        assertThat(s.observations()).hasSameSizeAs(s.truthEvent());
        Map<String, Integer> seen = new HashMap<>();
        long seq = 0;
        for (int i = 0; i < s.observations().size(); i++) {
            ObservationFacts f = s.observations().get(i);
            assertThat(f.seq()).isGreaterThan(seq);
            seq = f.seq();
            seen.merge(s.truthEvent().get(i), 1, Integer::sum);
            assertThat(f.located()).isTrue();
        }
        assertThat(seen).hasSize(10);
        assertThat(seen.values()).allMatch(n -> n >= 2 && n <= 3);
        assertThat(ScenarioGenerator.canonicalEvents(SEED, 0).truthEvent()).isEqualTo(s.truthEvent());
    }

    @Test
    @DisplayName("storylines: forty storyline and ten background inputs in time order; thirty background on null")
    void storylines() {
        ScenarioGenerator.StorylineScenario s = ScenarioGenerator.storylines(ScenarioType.SPIKE, SEED, 0);
        assertThat(s.inputs()).hasSize(50);
        assertThat(s.truthGroup().stream().filter(g -> g.startsWith("story-")).count()).isEqualTo(40);
        for (int i = 1; i < s.inputs().size(); i++) {
            assertThat(s.inputs().get(i).observedAt()).isAfterOrEqualTo(s.inputs().get(i - 1).observedAt());
        }
        ScenarioGenerator.StorylineScenario n = ScenarioGenerator.storylines(ScenarioType.NULL_SEASONAL, SEED, 0);
        assertThat(n.inputs()).hasSize(30);
        assertThat(new HashSet<>(n.truthGroup())).hasSize(30);
        assertThat(ScenarioGenerator.storylines(ScenarioType.SPIKE, SEED, 0).truthGroup()).isEqualTo(s.truthGroup());
    }

    @Test
    @DisplayName("forecasting: questions every four weeks per region with outcomes from the counts; drift changes rates")
    void forecasting() {
        ScenarioGenerator.ForecastScenario s = ScenarioGenerator.forecasting(ScenarioType.BASE_RATE_DRIFT, SEED, 0);
        int perRegion = (ScenarioGenerator.FORECAST_WEEKS - ScenarioGenerator.HORIZON_WEEKS
                - ScenarioGenerator.FIRST_QUESTION_WEEK) / ScenarioGenerator.HORIZON_WEEKS + 1;
        assertThat(s.questions()).hasSize(ScenarioGenerator.FORECAST_REGIONS * perRegion);
        for (ScenarioGenerator.Question q : s.questions()) {
            long[] c = s.counts().get(q.region());
            long sum = 0;
            for (int k = 0; k < ScenarioGenerator.HORIZON_WEEKS; k++) {
                sum += c[q.openWeek() + k];
            }
            assertThat(q.outcome()).isEqualTo(sum >= 1);
        }
        @SuppressWarnings("unchecked")
        Map<String, List<Double>> rates = (Map<String, List<Double>>) s.truth().get("weeklyRates");
        assertThat(rates.values()).allMatch(l -> l.size() == 2 && !l.get(0).equals(l.get(1)));
        assertThat(s.weekStarts()[1]).isEqualTo(s.weekStarts()[0].plusWeeks(1));
        ScenarioGenerator.ForecastScenario n = ScenarioGenerator.forecasting(ScenarioType.NULL_SEASONAL, SEED, 0);
        assertThat(n.truth().get("driftWeek")).isNull();
        assertThat(ScenarioGenerator.forecasting(ScenarioType.NULL_SEASONAL, SEED, 0).questions())
                .isEqualTo(n.questions());
    }

    @Test
    @DisplayName("scenario types: ten synthetic scenarios plus the six social scenarios of 137, with wire names")
    void types() {
        assertThat(ScenarioType.SYNTHETIC).hasSize(16).doesNotContain(ScenarioType.HISTORICAL);
        assertThat(ScenarioType.SYNTHETIC.stream().map(ScenarioType::wire)).containsExactly("null-seasonal",
                "step-change", "spike", "coverage-outage", "duplicate-echo", "late-revision", "common-driver",
                "true-precursor", "base-rate-drift", "pre-event-move", "social-planted-covered", "social-planted-thin",
                "social-event-discussed", "social-event-silent", "social-reused-image", "social-cue-loop");
        assertThat(ScenarioType.SOCIAL).hasSize(6);
        assertThat(ScenarioType.SUITE_VERSION).isEqualTo("bench-scenarios 1.0.0");
    }
}
