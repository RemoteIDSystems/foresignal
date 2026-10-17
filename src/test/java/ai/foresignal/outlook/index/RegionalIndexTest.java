package ai.foresignal.outlook.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * The regional index arithmetic and inputs (prompt 126): normalization by each transform, weights renormalized over
 * the components present, floors, a decomposition that sums to the value, deltas, and the validation of definitions
 * and weights changes.
 */
class RegionalIndexTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    static final List<IndexComputer.Component> COMPONENTS = List.of(
            new IndexComputer.Component("conflict", "rollup:day/*/conflict-event/{region}", "pctrank", 28, 1, null),
            new IndexComputer.Component("calm", "rollup:day/*/port-visit/{region}", "level", 28, -1, null),
            new IndexComputer.Component("outages", "rollup:day/*/internet-outage/{region}", "zscore", 7, 1, 20.0));

    static final Map<String, Double> WEIGHTS = Map.of("conflict", 0.5, "calm", 0.3, "outages", 0.2);

    static final double[] POOL = {0, 10, 20, 30, 40, 50, 60, 70, 80, 90};

    @Test
    @DisplayName("normalization: mid-rank percentile, min–max level and log1p, Φ of the z-score; NaN when empty")
    void normalize() {
        assertThat(IndexComputer.normalize(45, POOL, "pctrank")).isCloseTo(50, within(1e-12));
        assertThat(IndexComputer.normalize(40, POOL, "pctrank")).isCloseTo(45, within(1e-12));
        assertThat(IndexComputer.normalize(45, POOL, "level")).isCloseTo(50, within(1e-12));
        assertThat(IndexComputer.normalize(500, POOL, "level")).isEqualTo(100);
        assertThat(IndexComputer.normalize(Math.expm1(Math.log1p(90) / 2), POOL, "log1p")).isCloseTo(50,
                within(1e-9));
        assertThat(IndexComputer.normalize(45, POOL, "zscore")).isCloseTo(50, within(1e-9));
        assertThat(IndexComputer.normalize(45, new double[0], "pctrank")).isNaN();
        assertThat(IndexComputer.normalize(Double.NaN, POOL, "pctrank")).isNaN();
        assertThat(IndexComputer.normalize(5, new double[] {5, 5}, "level")).isEqualTo(50);
    }

    @Test
    @DisplayName("weights, direction and component floors; the decomposition sums to the value")
    void decomposition() {
        Map<String, Double> raw = Map.of("conflict", 45.0, "calm", 18.0, "outages", -100.0);
        Map<String, double[]> pools = Map.of("conflict", POOL, "calm", POOL, "outages", POOL);
        IndexComputer.Result r = IndexComputer.compute(COMPONENTS, WEIGHTS, raw, pools, List.of(), c -> false);
        // conflict 50; calm: level 20 reversed → 80; outages: z ≪ 0 → ≈0, floored at 20.
        double expected = 0.5 * 50 + 0.3 * 80 + 0.2 * 20;
        assertThat(r.value()).isCloseTo(expected, within(1e-9));
        double sum = r.parts().stream().mapToDouble(IndexComputer.Part::contribution).sum();
        assertThat(sum).isCloseTo(r.value(), within(1e-9));
        assertThat(r.parts()).extracting(IndexComputer.Part::key).containsExactly("conflict", "calm", "outages");
        assertThat(r.partsView().get(0)).containsKeys("key", "raw", "normalised", "weight", "contribution");
        assertThat(r.floorsApplied()).isEmpty();
    }

    @Test
    @DisplayName("a missing component's weight goes to the others, so the parts still sum to the value")
    void missingComponent() {
        Map<String, Double> raw = new LinkedHashMap<>();
        raw.put("conflict", 85.0);
        raw.put("calm", null);
        raw.put("outages", null);
        IndexComputer.Result r = IndexComputer.compute(COMPONENTS, WEIGHTS, raw, Map.of("conflict", POOL), List.of(),
                c -> false);
        assertThat(r.value()).isCloseTo(IndexComputer.normalize(85, POOL, "pctrank"), within(1e-9));
        assertThat(r.parts().get(0).weight()).isCloseTo(1, within(1e-12));
        assertThat(r.parts().get(1).contribution()).isZero();
        assertThat(r.partsView().get(1)).containsEntry("missing", true);
        IndexComputer.Result none = IndexComputer.compute(COMPONENTS, WEIGHTS, Map.of(), Map.of(), List.of(),
                c -> false);
        assertThat(none.value()).isNaN();
    }

    @Test
    @DisplayName("floors raise the value when their condition holds, and say so; the parts keep the pre-floor sum")
    void floors() {
        Map<String, Double> raw = Map.of("conflict", 5.0, "calm", 90.0, "outages", 0.0);
        Map<String, double[]> pools = Map.of("conflict", POOL, "calm", POOL, "outages", POOL);
        List<IndexComputer.Floor> floors = List.of(
                new IndexComputer.Floor("war", "UCDP war", Map.of("type", "reference", "family", "conflict:battles"),
                        70),
                new IndexComputer.Floor("hot", "conflict high", Map.of("type", "component", "key", "conflict",
                        "gte", 90), 80));
        IndexComputer.Result r = IndexComputer.compute(COMPONENTS, WEIGHTS, raw, pools, floors, c -> true);
        assertThat(r.value()).isEqualTo(70);
        assertThat(r.floorsApplied()).containsExactly("war");
        assertThat(r.beforeFloors()).isLessThan(70);
        IndexComputer.Result off = IndexComputer.compute(COMPONENTS, WEIGHTS, raw, pools, floors, c -> false);
        assertThat(off.value()).isEqualTo(off.beforeFloors());
        assertThat(off.floorsApplied()).isEmpty();
    }

    @Test
    @DisplayName("deltas against the previous values; none without one")
    void deltas() {
        assertThat(IndexComputer.delta(62.5, 55.0)).isEqualTo(7.5);
        assertThat(IndexComputer.delta(62.5, null)).isNull();
        List<LocalDate> days = RegionalIndexService.backfillDays(LocalDate.of(2026, 10, 16));
        assertThat(days).contains(LocalDate.of(2026, 10, 16), LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 9));
        assertThat(days.stream().filter(d -> d.getDayOfWeek() == java.time.DayOfWeek.MONDAY).count())
                .isGreaterThanOrEqualTo(RegionalIndexService.BACKFILL_WEEKS);
    }

    @Test
    @DisplayName("pools gather every class region's values on the same weekday over the preceding year")
    void pools() {
        double[] a = new double[400];
        double[] b = new double[400];
        java.util.Arrays.fill(a, 1);
        java.util.Arrays.fill(b, 2);
        Map<String, Map<String, double[]>> values = Map.of("YE", Map.of("conflict", a), "SA", Map.of("conflict", b));
        Map<String, Map<String, double[]>> pools = RegionalIndexService.pools(List.of(COMPONENTS.get(0)), values,
                Map.of("YE", "continent:AS", "SA", "continent:AS"), 399);
        assertThat(pools.get("continent:AS").get("conflict")).hasSize(2 * 53);
    }

    @Test
    @DisplayName("definitions: components, weights summing to 1, floors and targets are validated (INDEX_INVALID)")
    void definitionValidation() throws Exception {
        String ok = """
                {"name": "Unrest", "components": [
                  {"key": "conflict", "ref": "rollup:day/*/conflict-event/{region}", "transform": "pctrank",
                   "window": "P28D", "direction": 1},
                  {"key": "outages", "ref": "rollup:day/*/internet-outage/{region}", "transform": "zscore",
                   "window": "P7D", "direction": -1, "floor": 10}],
                 "weights": {"conflict": 0.7, "outages": 0.3},
                 "floors": [{"description": "war", "minValue": 70, "condition": {"type": "reference",
                   "family": "conflict:battles", "minFatalities": 1000}}],
                 "target": "conflict:battles", "enabled": true}
                """;
        IndexInputs.Definition d = IndexInputs.definition(JSON.readTree(ok));
        assertThat(d.components()).hasSize(2);
        assertThat(d.floors().get(0)).containsEntry("id", "floor-1");
        assertThat(IndexInputs.toComponents(d.components()).get(1).direction()).isEqualTo(-1);
        assertThat(IndexInputs.toFloors(d.floors()).get(0).minValue()).isEqualTo(70);
        for (String bad : List.of(
                ok.replace("0.7", "0.8"),
                ok.replace("{region}", "YE"),
                ok.replace("\"pctrank\"", "\"median\""),
                ok.replace("\"P7D\"", "\"P1D\""),
                ok.replace("\"direction\": -1", "\"direction\": 2"),
                ok.replace("conflict:battles\", \"enabled\"", "Battles!\", \"enabled\""),
                ok.replace("\"minFatalities\": 1000", "\"withinDays\": 30"),
                ok.replace("\"name\": \"Unrest\",", ""))) {
            assertThatThrownBy(() -> IndexInputs.definition(JSON.readTree(bad)))
                    .isInstanceOfSatisfying(ValidationException.class,
                            e -> assertThat(e.getCode()).isEqualTo(ErrorCodes.INDEX_INVALID));
        }
        assertThat(IndexInputs.definition(JSON.readTree(ok.replace("conflict:battles\", \"enabled\"",
                "finding:change-point:ais-behaviour\", \"enabled\""))).target())
                .isEqualTo("finding:change-point:ais-behaviour");
    }

    @Test
    @DisplayName("weights changes: fitted needs a version, operator needs weights summing to 1, default needs neither")
    void weightsValidation() throws Exception {
        Set<String> keys = Set.of("conflict", "outages");
        assertThat(IndexInputs.weights(JSON.readTree("{\"source\":\"fitted\",\"version\":3}"), keys).version())
                .isEqualTo(3);
        assertThat(IndexInputs.weights(JSON.readTree("{\"source\":\"default\"}"), keys).source()).isEqualTo("default");
        assertThat(IndexInputs.weights(JSON.readTree(
                "{\"source\":\"operator\",\"weights\":{\"conflict\":0.5,\"outages\":0.5}}"), keys).weights())
                .containsEntry("conflict", 0.5);
        for (String bad : List.of("{\"source\":\"fitted\"}", "{\"source\":\"operator\"}",
                "{\"source\":\"operator\",\"weights\":{\"conflict\":1.5}}", "{\"source\":\"magic\"}",
                "{\"source\":\"default\",\"weights\":{\"conflict\":1}}", "{\"source\":\"default\",\"x\":1}")) {
            assertThatThrownBy(() -> IndexInputs.weights(JSON.readTree(bad), keys))
                    .isInstanceOf(ValidationException.class);
        }
        assertThatThrownBy(() -> IndexInputs.id("Bad Id")).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("the fitter recovers a planted relationship and proposes weights that sum to 1 (pure)")
    void fitterPlanted() {
        List<IndexFitter.Row> rows = IndexFitIT.plantedRows(30, 80, 4);
        IndexFitter.Result r = IndexFitter.fit(rows, List.of("signal", "noise"), Map.of("signal", 0.5, "noise", 0.5),
                java.time.Instant.parse("2026-10-17T00:00:00Z"));
        assertThat(r.proposed()).isNotNull();
        assertThat(r.proposed().values().stream().mapToDouble(Double::doubleValue).sum()).isCloseTo(1, within(1e-5));
        assertThat(r.proposed().get("signal")).isGreaterThan(r.proposed().get("noise"));
        assertThat(r.components().get(0).significant()).isTrue();
        assertThat(r.skill()).containsKeys("evaluatedAt", "n", "pinballLoss", "baselinePinballLoss", "skillPinball");
        assertThatThrownBy(() -> IndexFitter.fit(rows.subList(0, 5), List.of("signal", "noise"), Map.of(),
                java.time.Instant.now())).isInstanceOf(IllegalArgumentException.class);
    }
}
