package ai.foresignal.hypothesis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.hypothesis.HypothesisUpdater.Contribution;
import ai.foresignal.hypothesis.HypothesisUpdater.Driver;
import ai.foresignal.hypothesis.HypothesisUpdater.History;
import ai.foresignal.hypothesis.HypothesisUpdater.Hyp;
import ai.foresignal.hypothesis.HypothesisUpdater.Result;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The {@code indicator-bayes} updater (prompt 125 §4): log-odds per LR class, explicit LR bounds, absence LRs,
 * same-family and same-group suppression, daily damping, clamping, exclusive renormalization with {@code other},
 * independent sets left alone, the speculative-link cap and the base-rate refresh shift. Expected values are worked
 * by hand from the definitions in the method comments.
 */
class HypothesisUpdaterTest {

    static final String EX = HypothesisVocabulary.EXCLUSIVE;
    static final String IN = HypothesisVocabulary.INDEPENDENT;

    static Hyp hyp(String id, double p) {
        return new Hyp(id, id, p, null, false);
    }

    static Driver indicator(String id, String family, String key, Map<String, Double> lr) {
        return new Driver("indicator", id, null, key, family, lr, null, true);
    }

    static double applied(Result r, String driverId, String hypothesisId) {
        return r.contributions().stream().filter(c -> driverId.equals(c.id()) && hypothesisId.equals(c.hypothesisId()))
                .mapToDouble(Contribution::applied).sum();
    }

    static Contribution contribution(Result r, String driverId, String hypothesisId) {
        return r.contributions().stream().filter(c -> driverId.equals(c.id()) && hypothesisId.equals(c.hypothesisId()))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("class values: high 5.0/0.2, medium 2.5/0.4, low 1.4/0.7, neutral 1.0")
    void classValues() {
        assertThat(LikelihoodRatios.classValue("high", "supports")).isEqualTo(5.0);
        assertThat(LikelihoodRatios.classValue("high", "weakens")).isEqualTo(0.2);
        assertThat(LikelihoodRatios.classValue("medium", "supports")).isEqualTo(2.5);
        assertThat(LikelihoodRatios.classValue("medium", "weakens")).isEqualTo(0.4);
        assertThat(LikelihoodRatios.classValue("low", "supports")).isEqualTo(1.4);
        assertThat(LikelihoodRatios.classValue("low", "weakens")).isEqualTo(0.7);
        assertThat(LikelihoodRatios.classValue("neutral", "supports")).isEqualTo(1.0);
        assertThat(LikelihoodRatios.classValue("neutral", "weakens")).isEqualTo(1.0);
    }

    @Test
    @DisplayName("an observed indicator adds ln(LR) to the hypothesis's log-odds")
    void logOddsPerClass() {
        // logit(0.5) = 0; + ln 2.5 → 2.5 / 3.5.
        Result medium = HypothesisUpdater.update(IN, true, List.of(hyp("A", 0.5)),
                List.of(indicator("i1", "f", "k1", Map.of("A", 2.5))), History.EMPTY);
        assertThat(medium.after().get("A")).isCloseTo(2.5 / 3.5, within(1e-9));
        assertThat(applied(medium, "i1", "A")).isCloseTo(Math.log(2.5), within(1e-12));
        // + ln 1.4 → 1.4 / 2.4.
        Result low = HypothesisUpdater.update(IN, true, List.of(hyp("A", 0.5)),
                List.of(indicator("i1", "f", "k1", Map.of("A", 1.4))), History.EMPTY);
        assertThat(low.after().get("A")).isCloseTo(1.4 / 2.4, within(1e-9));
        // + ln 0.4 → 0.4 / 1.4.
        Result weakens = HypothesisUpdater.update(IN, true, List.of(hyp("A", 0.5)),
                List.of(indicator("i1", "f", "k1", Map.of("A", 0.4))), History.EMPTY);
        assertThat(weakens.after().get("A")).isCloseTo(0.4 / 1.4, within(1e-9));
        // An exclusive pair at one half each: 0.5 · 2.5 against 0.5 → 2.5 / 3.5.
        Result pair = HypothesisUpdater.update(EX, true, List.of(hyp("A", 0.5), hyp("B", 0.5)),
                List.of(indicator("i1", "f", "k1", Map.of("A", 2.5))), History.EMPTY);
        assertThat(pair.after().get("A")).isCloseTo(2.5 / 3.5, within(1e-9));
        assertThat(pair.after().get("B")).isCloseTo(1 / 3.5, within(1e-9));
        // A neutral driver changes nothing.
        Result neutral = HypothesisUpdater.update(EX, true, List.of(hyp("A", 0.3), hyp("B", 0.7)),
                List.of(indicator("i1", "f", "k1", Map.of("A", 1.0, "B", 1.0))), History.EMPTY);
        assertThat(neutral.after().get("A")).isCloseTo(0.3, within(1e-12));
        assertThat(neutral.moved()).isFalse();
    }

    @Test
    @DisplayName("an explicit LR overrides the class and is bounded to [0.05, 20]")
    void explicitBounds() {
        assertThat(LikelihoodRatios.observed(Map.of("class", "high", "direction", "supports", "value", 50.0)))
                .isEqualTo(20.0);
        assertThat(LikelihoodRatios.observed(Map.of("class", "high", "direction", "supports", "value", 0.01)))
                .isEqualTo(0.05);
        assertThat(LikelihoodRatios.observed(Map.of("class", "low", "direction", "weakens", "value", 3.3)))
                .isEqualTo(3.3);
        assertThat(LikelihoodRatios.observed(Map.of("class", "medium", "direction", "weakens"))).isEqualTo(0.4);
        assertThat(LikelihoodRatios.observed(null)).isEqualTo(1.0);
    }

    @Test
    @DisplayName("absence: the reciprocal moved halfway toward 1 in log space, scaled by detectability")
    void absence() {
        assertThat(LikelihoodRatios.defaultAbsence(5.0)).isCloseTo(1 / Math.sqrt(5), within(1e-12));
        assertThat(LikelihoodRatios.defaultAbsence(0.2)).isCloseTo(Math.sqrt(5), within(1e-12));
        assertThat(LikelihoodRatios.defaultAbsence(1.0)).isCloseTo(1.0, within(1e-12));
        double absence = LikelihoodRatios.defaultAbsence(5.0);
        assertThat(LikelihoodRatios.absenceEffective(absence, 1.0)).isCloseTo(absence, within(1e-12));
        assertThat(LikelihoodRatios.absenceEffective(absence, 0.5)).isCloseTo(1 + (absence - 1) * 0.5, within(1e-12));
        assertThat(LikelihoodRatios.absenceEffective(absence, 0.0)).isEqualTo(1.0);
        // A barely observable indicator moves the set barely at all.
        Driver faint = new Driver("indicator", "i1", null, "absence:i1", "f",
                Map.of("A", LikelihoodRatios.absenceEffective(absence, 0.05)), 0.05, true);
        Result r = HypothesisUpdater.update(IN, true, List.of(hyp("A", 0.5)), List.of(faint), History.EMPTY);
        assertThat(r.after().get("A")).isCloseTo(0.5, within(0.02));
    }

    @Test
    @DisplayName("within one family only the largest |ln LR| per hypothesis per day applies; the others are suppressed")
    void sameFamilySuppression() {
        Result r = HypothesisUpdater.update(IN, true, List.of(hyp("A", 0.5)),
                List.of(indicator("strong", "airspace", "k1", Map.of("A", 2.5)),
                        indicator("weak", "airspace", "k2", Map.of("A", 1.4))), History.EMPTY);
        assertThat(r.after().get("A")).isCloseTo(2.5 / 3.5, within(1e-9));
        Contribution weak = contribution(r, "weak", "A");
        assertThat(weak.suppressed()).isTrue();
        assertThat(weak.suppressionReason()).isEqualTo(HypothesisUpdater.SAME_FAMILY);
        assertThat(weak.applied()).isZero();
        // A stronger driver after a weaker one applies only its excess: the total is the stronger one's.
        Result reversed = HypothesisUpdater.update(IN, true, List.of(hyp("A", 0.5)),
                List.of(indicator("weak", "airspace", "k2", Map.of("A", 1.4)),
                        indicator("strong", "airspace", "k1", Map.of("A", 2.5))), History.EMPTY);
        assertThat(reversed.after().get("A")).isCloseTo(2.5 / 3.5, within(1e-9));
        assertThat(applied(reversed, "strong", "A")).isCloseTo(Math.log(2.5) - Math.log(1.4), within(1e-12));
        // Another family counts separately.
        Result other = HypothesisUpdater.update(IN, true, List.of(hyp("A", 0.5)),
                List.of(indicator("a", "airspace", "k1", Map.of("A", 1.4)),
                        indicator("b", "naval", "k2", Map.of("A", 1.4))), History.EMPTY);
        assertThat(applied(other, "b", "A")).isCloseTo(Math.log(1.4), within(1e-12));
        // Earlier today: a weaker driver of the same family is suppressed against history.
        History today = new History(Set.of(), Map.of("airspace|A", Math.log(2.5)), Map.of("A", Math.log(2.5)));
        Result later = HypothesisUpdater.update(IN, true, List.of(hyp("A", 2.5 / 3.5)),
                List.of(indicator("weak", "airspace", "k3", Map.of("A", 1.4))), today);
        assertThat(later.moved()).isFalse();
        assertThat(contribution(later, "weak", "A").suppressionReason()).isEqualTo(HypothesisUpdater.SAME_FAMILY);
    }

    @Test
    @DisplayName("observations in one evidence group are one piece of evidence: an echo never updates twice")
    void sameEvidenceGroup() {
        Result batch = HypothesisUpdater.update(IN, true, List.of(hyp("A", 0.5)),
                List.of(indicator("i1", "airspace", "group:G", Map.of("A", 2.5)),
                        indicator("i2", "naval", "group:G", Map.of("A", 2.5))), History.EMPTY);
        assertThat(contribution(batch, "i2", "A").suppressionReason()).isEqualTo(HypothesisUpdater.SAME_GROUP);
        assertThat(batch.after().get("A")).isCloseTo(2.5 / 3.5, within(1e-9));
        History seen = new History(Set.of("group:G"), Map.of(), Map.of());
        Result echo = HypothesisUpdater.update(IN, true, List.of(hyp("A", 0.5)),
                List.of(indicator("i3", "naval", "group:G", Map.of("A", 2.5))), seen);
        assertThat(echo.moved()).isFalse();
        assertThat(contribution(echo, "i3", "A").suppressed()).isTrue();
    }

    @Test
    @DisplayName("the net evidence change per hypothesis per UTC day stays within 1.5 logits")
    void dailyDamping() {
        // ln 5 = 1.609 exceeds 1.5 on its own.
        Result high = HypothesisUpdater.update(IN, true, List.of(hyp("A", 0.5)),
                List.of(indicator("i1", "f", "k1", Map.of("A", 5.0))), History.EMPTY);
        assertThat(high.after().get("A")).isCloseTo(HypothesisUpdater.sigmoid(1.5), within(1e-9));
        assertThat(contribution(high, "i1", "A").suppressionReason()).isEqualTo(HypothesisUpdater.DAMPED);
        assertThat(applied(high, "i1", "A")).isCloseTo(1.5, within(1e-12));
        // 1.2 already applied today leaves 0.3.
        History today = new History(Set.of(), Map.of(), Map.of("A", 1.2));
        Result rest = HypothesisUpdater.update(IN, true, List.of(hyp("A", 0.5)),
                List.of(indicator("i2", "g", "k2", Map.of("A", 2.5))), today);
        assertThat(applied(rest, "i2", "A")).isCloseTo(0.3, within(1e-12));
        assertThat(rest.after().get("A")).isCloseTo(HypothesisUpdater.sigmoid(0.3), within(1e-9));
        // A weakening driver may still move down by the full 2.7 room below.
        Result down = HypothesisUpdater.update(IN, true, List.of(hyp("A", 0.5)),
                List.of(indicator("i3", "g", "k3", Map.of("A", 0.4))), today);
        assertThat(applied(down, "i3", "A")).isCloseTo(Math.log(0.4), within(1e-12));
    }

    @Test
    @DisplayName("probabilities stay in [0.02, 0.98] while the set is active")
    void clamping() {
        Result active = HypothesisUpdater.update(IN, true, List.of(hyp("A", 0.97)),
                List.of(indicator("i1", "f", "k1", Map.of("A", 2.5))), History.EMPTY);
        assertThat(active.after().get("A")).isEqualTo(HypothesisUpdater.P_MAX);
        double expected = HypothesisUpdater.sigmoid(HypothesisUpdater.logit(0.97) + Math.log(2.5));
        Result inactive = HypothesisUpdater.update(IN, false, List.of(hyp("A", 0.97)),
                List.of(indicator("i1", "f", "k1", Map.of("A", 2.5))), History.EMPTY);
        assertThat(inactive.after().get("A")).isCloseTo(expected, within(1e-9));
        Result low = HypothesisUpdater.update(IN, true, List.of(hyp("A", 0.03)),
                List.of(indicator("i1", "f", "k1", Map.of("A", 0.4))), History.EMPTY);
        assertThat(low.after().get("A")).isEqualTo(HypothesisUpdater.P_MIN);
    }

    @Test
    @DisplayName("an exclusive set renormalizes, the other hypothesis taking the residual mass")
    void exclusiveRenormalization() {
        // 0.3 · 2.5 = 0.75, 0.2 · 0.4 = 0.08, other 0.5: total 1.33.
        Result r = HypothesisUpdater.update(EX, true, List.of(hyp("A", 0.3), hyp("B", 0.2), hyp("other", 0.5)),
                List.of(indicator("i1", "f", "k1", Map.of("A", 2.5, "B", 0.4))), History.EMPTY);
        assertThat(r.after().get("A")).isCloseTo(0.75 / 1.33, within(1e-9));
        assertThat(r.after().get("B")).isCloseTo(0.08 / 1.33, within(1e-9));
        assertThat(r.after().get("other")).isCloseTo(0.5 / 1.33, within(1e-9));
        assertThat(r.after().values().stream().mapToDouble(Double::doubleValue).sum()).isCloseTo(1.0, within(1e-12));
        // The bounds hold and the sum stays 1.
        double[] bounded = HypothesisUpdater.boxSimplex(new double[] {0.995, 0.004, 0.001},
                new double[] {0.02, 0.02, 0.02}, new double[] {0.98, 0.98, 0.98});
        assertThat(bounded[0] + bounded[1] + bounded[2]).isCloseTo(1.0, within(1e-12));
        assertThat(bounded[1]).isCloseTo(0.02, within(1e-12));
        assertThat(bounded[2]).isCloseTo(0.02, within(1e-12));
        assertThat(bounded[0]).isCloseTo(0.96, within(1e-12));
    }

    @Test
    @DisplayName("an independent set moves only the hypotheses a driver bears on")
    void independentUntouched() {
        Result r = HypothesisUpdater.update(IN, true, List.of(hyp("A", 0.3), hyp("B", 0.6)),
                List.of(indicator("i1", "f", "k1", Map.of("A", 2.5))), History.EMPTY);
        assertThat(r.after().get("B")).isEqualTo(0.6);
        double odds = 0.3 / 0.7 * 2.5;
        assertThat(r.after().get("A")).isCloseTo(odds / (1 + odds), within(1e-9));
    }

    @Test
    @DisplayName("a speculative link never exceeds its cap unless an operator override lifts it")
    void speculativeCap() {
        // LR = 0.8 / 0.2 = 4 applied to the prior 0.1: 0.4 / 1.3.
        double cap = HypothesisUpdater.cap(0.1, 0.2);
        assertThat(cap).isCloseTo(0.4 / 1.3, within(1e-12));
        // The LR is bounded to 20: 0.5 · 20 / (0.5 · 20 + 0.5).
        assertThat(HypothesisUpdater.cap(0.5, 0.001)).isCloseTo(10 / 10.5, within(1e-12));
        assertThat(HypothesisUpdater.cap(0.9, 0.001)).isCloseTo(HypothesisUpdater.P_MAX, within(1e-12));
        assertThat(HypothesisUpdater.cap(0.2, 0.9)).isCloseTo(0.2, within(1e-12));
        List<Hyp> hyps = List.of(new Hyp("S", "link", 0.1, cap, false), hyp("alt1", 0.4), hyp("alt2", 0.5));
        Result r = HypothesisUpdater.update(EX, true, hyps,
                List.of(indicator("i1", "markets", "k1", Map.of("S", 5.0))), History.EMPTY);
        assertThat(r.after().get("S")).isCloseTo(cap, within(1e-12));
        assertThat(r.capped()).containsExactly("S");
        assertThat(contribution(r, "i1", "S").suppressionReason()).isEqualTo(HypothesisUpdater.CAPPED);
        double rest = 1 - cap;
        assertThat(r.after().get("alt1")).isCloseTo(rest * 4 / 9, within(1e-9));
        assertThat(r.after().get("alt2")).isCloseTo(rest * 5 / 9, within(1e-9));
        Result held = HypothesisUpdater.override(EX, true, hyps, Map.of("S", 0.6), false, "operator");
        assertThat(held.after().get("S")).isCloseTo(cap, within(1e-12));
        Result lifted = HypothesisUpdater.override(EX, true, hyps, Map.of("S", 0.6), true, "operator");
        assertThat(lifted.after().get("S")).isCloseTo(0.6, within(1e-12));
        assertThat(lifted.after().get("alt1")).isCloseTo(0.4 * 4 / 9, within(1e-9));
        assertThat(lifted.capped()).isEmpty();
    }

    @Test
    @DisplayName("a base-rate refresh shifts the log-odds by the change in prior log-odds and is never damped")
    void baseRateRefresh() {
        double shift = (0.3 / 0.7) / (0.2 / 0.8);
        Driver refresh = new Driver("base-rate", null, null, null, null, Map.of("A", shift), null, false);
        History saturated = new History(Set.of(), Map.of(), Map.of("A", 1.5));
        Result r = HypothesisUpdater.update(IN, true, List.of(hyp("A", 0.3)), List.of(refresh), saturated);
        double odds = 0.3 / 0.7 * shift;
        assertThat(r.after().get("A")).isCloseTo(odds / (1 + odds), within(1e-9));
        assertThat(r.contributions().get(0).suppressionReason()).isNull();
        // Exclusive: the prior ratio, then renormalization.
        Driver ratio = new Driver("base-rate", null, null, null, null, Map.of("A", 0.3 / 0.2), null, false);
        Result ex = HypothesisUpdater.update(EX, true, List.of(hyp("A", 0.3), hyp("other", 0.7)), List.of(ratio),
                History.EMPTY);
        assertThat(ex.after().get("A")).isCloseTo(0.45 / 1.15, within(1e-9));
    }
}
