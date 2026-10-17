package ai.foresignal.baserate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Reporting density (prompt 121): density against the median of countries of similar population, decile assignment
 * over all countries, and the {@code LOW_REPORTING_DENSITY} caveat for the bottom decile.
 */
class ReportingDensityTest {

    @Test
    @DisplayName("deciles: 20 countries rank 1 to 10, two per decile, least reported first")
    void deciles() {
        Map<String, ReportingDensity.Input> inputs = new TreeMap<>();
        for (int i = 0; i < 20; i++) {
            inputs.put(String.format("C%02d", i), new ReportingDensity.Input(i + 1, 1_000_000L));
        }
        Map<String, ReportingDensity.Profile> out = ReportingDensity.compute(inputs);
        assertThat(out.get("C00").decile()).isEqualTo(1);
        assertThat(out.get("C01").decile()).isEqualTo(1);
        assertThat(out.get("C02").decile()).isEqualTo(2);
        assertThat(out.get("C19").decile()).isEqualTo(10);
        assertThat(out.values().stream().filter(p -> p.decile() == 1).count()).isEqualTo(2);
        assertThat(ReportingDensity.decile(0, 1)).isEqualTo(1);
        assertThat(ReportingDensity.decile(9, 10)).isEqualTo(10);
    }

    @Test
    @DisplayName("density is relative to countries of similar population, so a small quiet country is not penalized")
    void populationGroups() {
        Map<String, ReportingDensity.Input> inputs = new TreeMap<>();
        // Five population quintiles, five countries each; the small countries report a tenth as much.
        for (int i = 0; i < 25; i++) {
            long population = (long) Math.pow(10, 5 + i / 5);
            double weekly = i < 5 ? 1 + (i % 5) * 0.1 : 10 + (i % 5);
            inputs.put(String.format("K%02d", i), new ReportingDensity.Input(weekly, population));
        }
        Map<String, ReportingDensity.Profile> out = ReportingDensity.compute(inputs);
        // The median small country has density 1, like the median large one.
        assertThat(out.get("K02").density()).isCloseTo(1.0, offset(1e-9));
        assertThat(out.get("K22").density()).isCloseTo(1.0, offset(1e-9));
        assertThat(ReportingDensity.median(List.of(3.0, 1.0, 2.0))).isEqualTo(2.0);
        assertThat(ReportingDensity.median(List.of(4.0, 1.0, 2.0, 3.0))).isEqualTo(2.5);
    }

    @Test
    @DisplayName("countries without population form their own group; a silent group divides by the floor")
    void unknownPopulation() {
        Map<String, ReportingDensity.Input> inputs = Map.of("AA", new ReportingDensity.Input(0, null), "BB",
                new ReportingDensity.Input(0.5, null));
        Map<String, ReportingDensity.Profile> out = ReportingDensity.compute(inputs);
        assertThat(out.get("AA").density()).isEqualTo(0);
        assertThat(out.get("BB").density()).isCloseTo(0.5 / 0.25, offset(1e-9));
    }

    @Test
    @DisplayName("a region in the bottom decile carries LOW_REPORTING_DENSITY; others do not")
    void caveat() {
        BaseRateCalculator.Result result = new BaseRateCalculator.Result(1, 0.5, 0.4, 0.6, 100, 100,
                LocalDate.of(2020, 1, 6), LocalDate.of(2021, 12, 26), Map.of("ucdp", 100L), Set.of("ucdp"), null);
        BaseRateDefinition d = new BaseRateDefinition("conflict:battles", List.of("TD"), null, "P7D", null);
        Instant now = Instant.parse("2026-10-17T00:00:00Z");
        List<Map<String, Object>> low = BaseRateCaveats.of(d, result, List.of(new RegionProfileEntity("TD", 0.05, 1,
                now)), List.of(), 0.8);
        assertThat(low).extracting(c -> c.get("code")).containsExactly("LOW_REPORTING_DENSITY");
        assertThat(low.get(0).get("data")).isEqualTo(Map.of("regions", List.of("TD")));
        assertThat(BaseRateCaveats.of(d, result, List.of(new RegionProfileEntity("TD", 0.9, 4, now)), List.of(), 0.8))
                .isEmpty();
        assertThat(BaseRateCaveats.of(d, result, List.of(), List.of("TD"), 0.8)).extracting(c -> c.get("code"))
                .containsExactly("LOW_DETECTABILITY");
    }
}
