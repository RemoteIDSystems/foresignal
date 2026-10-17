package ai.foresignal.baserate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import ai.foresignal.baserate.reference.ReferenceFamilies;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The pure base-rate computation (prompt 121): rate, {@code pAtLeastOne} and the Jeffreys interval against reference
 * values; one source per region and week (no double counting); windows that skip uncovered weeks;
 * {@code SMALL_SAMPLE} and {@code MIXED_SOURCES}; and a conditional rate on a planted dependency.
 */
class BaseRateCalculatorTest {

    static final LocalDate START = LocalDate.of(2020, 1, 6);

    /** Weekly counts: region → source → week → count. */
    static final class Builder {
        final Map<String, Map<String, Map<LocalDate, Long>>> counts = new HashMap<>();

        Builder put(String region, String source, LocalDate week, long n) {
            counts.computeIfAbsent(region, k -> new HashMap<>()).computeIfAbsent(source, k -> new HashMap<>())
                    .put(week, n);
            return this;
        }

        BaseRateCalculator.Counts build() {
            return new BaseRateCalculator.Counts(counts);
        }
    }

    static Map<String, BaseRateCalculator.Coverage> global(String source, LocalDate from, int weeks) {
        return Map.of(source, BaseRateCalculator.Coverage.global(List.<LocalDate[]>of(new LocalDate[] {from,
            from.plusWeeks(weeks)})));
    }

    static BaseRateCalculator.Result compute(BaseRateCalculator.Counts counts,
            Map<String, BaseRateCalculator.Coverage> coverage, String family, int windowWeeks, List<String> regions) {
        return BaseRateCalculator.compute(new BaseRateCalculator.Inputs(regions, counts, coverage,
                ReferenceFamilies.priority(family), windowWeeks, START.plusWeeks(200), null, null, 0));
    }

    @Test
    @DisplayName("Jeffreys interval: reference values, and 0 / 1 at the boundaries")
    void jeffreys() {
        double[] i = BaseRateCalculator.jeffreys(3, 10);
        assertThat(i[0]).isCloseTo(0.092695, offset(1e-5));
        assertThat(i[1]).isCloseTo(0.605818, offset(1e-5));
        double[] zero = BaseRateCalculator.jeffreys(0, 10);
        assertThat(zero[0]).isEqualTo(0);
        assertThat(zero[1]).isCloseTo(0.217196, offset(1e-5));
        double[] all = BaseRateCalculator.jeffreys(10, 10);
        assertThat(all[0]).isCloseTo(0.782804, offset(1e-5));
        assertThat(all[1]).isEqualTo(1);
        double[] mid = BaseRateCalculator.jeffreys(15, 50);
        assertThat(mid[0]).isCloseTo(0.186877, offset(1e-5));
        assertThat(mid[1]).isCloseTo(0.435528, offset(1e-5));
        assertThat(BaseRateCalculator.jeffreys(0, 0)).containsExactly(0, 1);
    }

    @Test
    @DisplayName("rate per window, pAtLeastOne and the interval over 52 weekly and 13 four-week windows")
    void rate() {
        Builder b = new Builder();
        for (int w = 0; w < 52; w += 2) {
            b.put("YE", ReferenceFamilies.UCDP, START.plusWeeks(w), 2);
        }
        Map<String, BaseRateCalculator.Coverage> coverage = global(ReferenceFamilies.UCDP, START, 52);
        BaseRateCalculator.Result weekly = compute(b.build(), coverage, ReferenceFamilies.BATTLES, 1, List.of("YE"));
        assertThat(weekly.nWindows()).isEqualTo(52);
        assertThat(weekly.nEvents()).isEqualTo(52);
        assertThat(weekly.ratePerWindow()).isEqualTo(1.0);
        assertThat(weekly.pAtLeastOne()).isEqualTo(0.5);
        assertThat(weekly.lower()).isCloseTo(0.367193, offset(1e-5));
        assertThat(weekly.upper()).isCloseTo(0.632807, offset(1e-5));
        assertThat(weekly.periodFrom()).isEqualTo(START);
        assertThat(weekly.periodTo()).isEqualTo(START.plusWeeks(52).minusDays(1));
        assertThat(weekly.sourceMix()).containsExactly(Map.entry(ReferenceFamilies.UCDP, 52L));

        BaseRateCalculator.Result monthly = compute(b.build(), coverage, ReferenceFamilies.BATTLES, 4, List.of("YE"));
        assertThat(monthly.nWindows()).isEqualTo(13);
        assertThat(monthly.ratePerWindow()).isEqualTo(4.0);
        assertThat(monthly.pAtLeastOne()).isEqualTo(1.0);
        assertThat(monthly.upper()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("ACLED and UCDP both covering a region-week: only ACLED counts; elsewhere UCDP fills in")
    void noDoubleCounting() {
        Builder b = new Builder();
        for (int w = 0; w < 52; w++) {
            b.put("YE", ReferenceFamilies.UCDP, START.plusWeeks(w), 3);
            b.put("YE", ReferenceFamilies.ACLED, START.plusWeeks(w), 5);
            b.put("SD", ReferenceFamilies.UCDP, START.plusWeeks(w), 1);
        }
        Map<String, BaseRateCalculator.Coverage> coverage = Map.of(
                ReferenceFamilies.UCDP, BaseRateCalculator.Coverage.global(List.<LocalDate[]>of(new LocalDate[] {START,
                    START.plusWeeks(52)})),
                ReferenceFamilies.ACLED, new BaseRateCalculator.Coverage(List.of(), Map.of("YE", List.<LocalDate[]>of(
                        new LocalDate[] {START, START.plusWeeks(52)}))));
        BaseRateCalculator.Result yemen = compute(b.build(), coverage, ReferenceFamilies.BATTLES, 1, List.of("YE"));
        assertThat(yemen.nEvents()).isEqualTo(5L * 52);
        assertThat(yemen.sourceMix()).containsOnlyKeys(ReferenceFamilies.ACLED);
        assertThat(yemen.sourcesUsed()).containsExactly(ReferenceFamilies.ACLED);

        BaseRateCalculator.Result both = compute(b.build(), coverage, ReferenceFamilies.BATTLES, 1,
                List.of("YE", "SD"));
        assertThat(both.nEvents()).isEqualTo(6L * 52);
        assertThat(both.sourceMix()).containsEntry(ReferenceFamilies.ACLED, 260L)
                .containsEntry(ReferenceFamilies.UCDP, 52L);
        assertThat(BaseRateCaveats.of(new BaseRateDefinition(ReferenceFamilies.BATTLES, List.of("YE", "SD"), null,
                "P7D", null), both, List.of(), List.of(), 0.8)).extracting(c -> c.get("code"))
                .containsExactly("MIXED_SOURCES");
    }

    @Test
    @DisplayName("seismic prefers USGS over Foresignal's own history; own history fills the weeks after the import")
    void ownHistoryAfterImport() {
        Builder b = new Builder();
        for (int w = 0; w < 60; w++) {
            b.put("JP", ReferenceFamilies.USGS, START.plusWeeks(w), 2);
            b.put("JP", ReferenceFamilies.FORESIGNAL, START.plusWeeks(w), 1);
        }
        Map<String, BaseRateCalculator.Coverage> coverage = Map.of(
                ReferenceFamilies.USGS, BaseRateCalculator.Coverage.global(List.<LocalDate[]>of(new LocalDate[] {START,
                    START.plusWeeks(50)})),
                ReferenceFamilies.FORESIGNAL, BaseRateCalculator.Coverage.global(List.<LocalDate[]>of(new LocalDate[] {
                    START.plusWeeks(40), START.plusWeeks(60)})));
        BaseRateCalculator.Result r = compute(b.build(), coverage, "seismic:m5", 1, List.of("JP"));
        assertThat(r.nWindows()).isEqualTo(60);
        assertThat(r.sourceMix()).containsEntry(ReferenceFamilies.USGS, 100L)
                .containsEntry(ReferenceFamilies.FORESIGNAL, 10L);
    }

    @Test
    @DisplayName("windows never span an uncovered week, and fewer than 30 windows is SMALL_SAMPLE")
    void gapsAndSmallSample() {
        Builder b = new Builder();
        b.put("YE", ReferenceFamilies.UCDP, START, 1);
        Map<String, BaseRateCalculator.Coverage> coverage = Map.of(ReferenceFamilies.UCDP,
                BaseRateCalculator.Coverage.global(List.<LocalDate[]>of(new LocalDate[] {START, START.plusWeeks(6)},
                    new LocalDate[] {START.plusWeeks(7), START.plusWeeks(15)})));
        BaseRateCalculator.Result r = compute(b.build(), coverage, ReferenceFamilies.BATTLES, 4, List.of("YE"));
        // weeks 0-5 give one 4-week window (weeks 4-5 are left over); weeks 7-14 give two
        assertThat(r.nWindows()).isEqualTo(3);
        assertThat(r.nEvents()).isEqualTo(1);
        assertThat(BaseRateCaveats.of(new BaseRateDefinition(ReferenceFamilies.BATTLES, List.of("YE"), null, "P28D",
                null), r, List.of(), List.of(), 0.8)).extracting(c -> c.get("code")).containsExactly("SMALL_SAMPLE");
    }

    @Test
    @DisplayName("a conditional rate over antecedent weeks recovers a planted dependency, with the unconditional rate")
    void conditional() {
        Builder consequent = new Builder();
        Builder antecedent = new Builder();
        for (int w = 0; w < 200; w++) {
            if (w % 10 == 0) {
                antecedent.put("YE", ReferenceFamilies.UCDP, START.plusWeeks(w), 1);
                consequent.put("YE", ReferenceFamilies.UCDP, START.plusWeeks(w + 1), 3);
            }
        }
        Map<String, BaseRateCalculator.Coverage> coverage = global(ReferenceFamilies.UCDP, START, 200);
        BaseRateCalculator.Result r = BaseRateCalculator.compute(new BaseRateCalculator.Inputs(List.of("YE"),
                consequent.build(), coverage, ReferenceFamilies.CONFLICT_PRIORITY, 1, START.plusWeeks(200),
                antecedent.build(), ReferenceFamilies.CONFLICT_PRIORITY, 7));
        assertThat(r.nWindows()).isEqualTo(20);
        assertThat(r.pAtLeastOne()).isEqualTo(1.0);
        assertThat(r.ratePerWindow()).isEqualTo(3.0);
        assertThat(r.unconditional()).isNotNull();
        assertThat(r.unconditional().nWindows()).isEqualTo(200);
        assertThat(r.unconditional().pAtLeastOne()).isEqualTo(0.1);
    }

    @Test
    @DisplayName("deterministic: identical inputs give identical results")
    void deterministic() {
        Builder b = new Builder();
        for (int w = 0; w < 80; w += 3) {
            b.put("YE", ReferenceFamilies.UCDP, START.plusWeeks(w), w % 7);
        }
        Map<String, BaseRateCalculator.Coverage> coverage = global(ReferenceFamilies.UCDP, START, 80);
        assertThat(compute(b.build(), coverage, ReferenceFamilies.BATTLES, 4, List.of("YE")))
                .usingRecursiveComparison()
                .isEqualTo(compute(b.build(), coverage, ReferenceFamilies.BATTLES, 4, List.of("YE")));
    }

    @Test
    @DisplayName("definitions: validation, canonical key and the plain-language reference class")
    void definitions() {
        BaseRateDefinition d = BaseRateDefinition.parse(Map.of("family", "conflict:battles", "regionCodes",
                List.of("YE", "SD"), "window", "P28D", "condition", Map.of("family", "conflict:protests",
                        "withinDays", 14)));
        assertThat(d.regionCodes()).containsExactly("SD", "YE");
        assertThat(d.windowWeeks()).isEqualTo(4);
        assertThat(d.referenceClass()).isEqualTo("conflict:battles events in SD, YE, per 28-day window, in windows"
                + " that followed conflict:protests within 14 days");
        BaseRateDefinition same = BaseRateDefinition.parse(Map.of("window", "P28D", "regionCodes", List.of("SD", "YE"),
                "family", "conflict:battles", "condition", Map.of("withinDays", 14, "family", "conflict:protests")));
        assertThat(BaseRateService.key(same)).isEqualTo(BaseRateService.key(d));
        assertThat(BaseRateDefinition.parse(Map.of("family", "seismic:m4.5", "window", "P90D")).windowWeeks())
                .isEqualTo(13);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> BaseRateDefinition.parse(Map.of("family",
                "conflict:battles", "window", "P30D"))).hasMessageContaining("not valid");
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> BaseRateDefinition.parse(Map.of("family",
                "conflict:battles", "window", "P7D", "regionCodes", List.of("YE"), "regionClass", "continent:AS")))
                .isInstanceOf(ai.foresignal.api.error.ValidationException.class);
    }
}
