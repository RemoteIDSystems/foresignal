package ai.foresignal.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import ai.foresignal.provenance.LicenceTerms;
import ai.foresignal.source.spi.Geo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.transaction.PlatformTransactionManager;

/** The dependency rules (prompt 104) on synthetic nodes, the label ceilings, overrides and licenses. */
class DependencyRulesTest {

    private static final LicenceTerms ODBL = new LicenceTerms("ODbL-1.0", "ODbL", LicenceTerms.CommercialUse.ALLOWED,
            true, true, LicenceTerms.Redistribution.ALLOWED, "© OpenStreetMap contributors", null);
    private static final LicenceTerms CC_BY = new LicenceTerms("CC-BY-4.0", "CC BY", LicenceTerms.CommercialUse.ALLOWED,
            false, true, LicenceTerms.Redistribution.ALLOWED, "WRI", null);
    private static final LicenceTerms CC_BY_SA = new LicenceTerms("CC-BY-SA-4.0", "CC BY-SA",
            LicenceTerms.CommercialUse.ALLOWED, true, true, LicenceTerms.Redistribution.ALLOWED, "TeleGeography", null);

    private final InfraGraphBuilder builder = new InfraGraphBuilder(null, null, null, null, null, null,
            mock(PlatformTransactionManager.class), null);
    private final List<InfraGraphBuilder.NodeData> nodes = new ArrayList<>();

    /** A km east of (lon0, lat0) at latitude 32, in degrees of longitude. */
    private static double eastKm(double km) {
        return km / (111.320 * Math.cos(Math.toRadians(32)));
    }

    private InfraGraphBuilder.NodeData node(String type, Geometry geom, Map<String, Object> attributes,
            LicenceTerms licence) {
        Point point = geom == null ? null : geom.getInteriorPoint();
        InfraGraphBuilder.NodeData node = new InfraGraphBuilder.NodeData(nodes.size(), 100 + nodes.size(),
                "test", "e" + nodes.size(), type, type + nodes.size(), geom, point, attributes, licence);
        nodes.add(node);
        return node;
    }

    private static Map<String, InfraRules.Rule> rules(Map<String, Object> overrides) {
        Map<String, Object> bundled = Map.of("version", 2, "rules", List.of(
                Map.of("id", "cable-landing", "enabled", true, "method", "topological", "label", "likely",
                        "relation", "lands_at", "params", Map.of("withinKm", 5)),
                Map.of("id", "line-substation", "enabled", true, "method", "topological", "label", "likely",
                        "relation", "transmits_power", "params", Map.of("withinMeters", 50)),
                Map.of("id", "plant-substation", "enabled", true, "method", "proximity", "label", "confirmed",
                        "relation", "supplies_power", "params", Map.of("maxKm", 5, "capacityFactorKm", 2)),
                Map.of("id", "substation-critical-load", "enabled", true, "method", "proximity", "label", "possible",
                        "relation", "supplies_power", "params", Map.of("ruralKm", 10))));
        Map<String, InfraRules.Rule> out = new java.util.LinkedHashMap<>();
        for (InfraRules.Rule rule : InfraRules.effective(bundled, overrides)) {
            out.put(rule.id(), rule);
        }
        return out;
    }

    @Test
    @DisplayName("a cable landing is a geometry match: topological and likely, never declared or confirmed")
    void cableLanding() {
        var cable = node("telecom.cable", Geo.factory().createLineString(new Coordinate[] {new Coordinate(-95, 32),
                new Coordinate(-90, 32)}), Map.of(), CC_BY_SA);
        var landing = node("telecom.landing", Geo.point(-95, 32.01), Map.of(), CC_BY_SA);
        node("telecom.landing", Geo.point(-95, 33), Map.of(), CC_BY_SA);
        var edges = builder.cableLanding(rules(null).get("cable-landing"), nodes);
        assertThat(edges).hasSize(1);
        assertThat(edges.getFirst().from()).isEqualTo(cable);
        assertThat(edges.getFirst().to()).isEqualTo(landing);
        assertThat(edges.getFirst().method()).isEqualTo("topological");
        assertThat(edges.getFirst().label()).isEqualTo("likely");
        assertThat((Double) edges.getFirst().evidence().get("distanceKm")).isBetween(1.0, 1.2);
    }

    @Test
    @DisplayName("a line reaching one substation is likely; reaching two different substations is confirmed")
    void lineEndpoints() {
        var subA = node("power.substation", Geo.point(-95, 32), Map.of(), ODBL);
        var subB = node("power.substation", Geo.point(-95 + eastKm(10), 32), Map.of(), ODBL);
        var both = node("power.line", Geo.factory().createLineString(new Coordinate[] {new Coordinate(-95, 32.0001),
                new Coordinate(-95 + eastKm(10), 32.0001)}), Map.of(), ODBL);
        var half = node("power.line", Geo.factory().createLineString(new Coordinate[] {new Coordinate(-95, 32.0002),
                new Coordinate(-94, 31)}), Map.of(), ODBL);
        var edges = builder.lineSubstation(rules(null).get("line-substation"), nodes);
        assertThat(edges).filteredOn(e -> e.to().equals(both)).hasSize(2)
                .allMatch(e -> e.label().equals("confirmed") && e.method().equals("topological"))
                .extracting(InfraGraphBuilder.EdgeData::from).containsExactly(subA, subB);
        assertThat(edges).filteredOn(e -> e.to().equals(half)).singleElement()
                .matches(e -> e.label().equals("likely") && e.from().equals(subA));
    }

    @Test
    @DisplayName("the plant radius scales with capacity, and proximity never beats possible")
    void plantRadius() {
        var substation = node("power.substation", Geo.point(-95 + eastKm(3), 32), Map.of(), ODBL);
        var small = node("power.generation", Geo.point(-95, 32), Map.of("capacityMw", 1), CC_BY);
        var big = node("power.generation", Geo.point(-95, 32.0001), Map.of("capacityMw", 100), CC_BY);
        var edges = builder.plantSubstation(rules(null).get("plant-substation"), nodes);
        // 1 MW: radius min(5, 2 x 1) = 2 km < 3 km; 100 MW: radius 5 km.
        assertThat(edges).extracting(InfraGraphBuilder.EdgeData::from).containsExactly(big);
        assertThat(edges.getFirst().to()).isEqualTo(substation);
        assertThat(edges.getFirst().label()).isEqualTo("possible");
        assertThat(edges).noneMatch(e -> e.from().equals(small));
    }

    @Test
    @DisplayName("one substation in range is possible; two or more give the two nearest, each speculative")
    void criticalLoad() {
        var near = node("power.substation", Geo.point(-95 + eastKm(1), 32), Map.of(), ODBL);
        var far = node("power.substation", Geo.point(-95 + eastKm(4), 32), Map.of(), ODBL);
        node("power.substation", Geo.point(-95 + eastKm(8), 32), Map.of(), ODBL);
        var hospital = node("health.hospital", Geo.point(-95, 32), Map.of(), ODBL);
        var lonely = node("transport.airport", Geo.point(-80, 40), Map.of(), CC_BY);
        var lonelySub = node("power.substation", Geo.point(-80 + 0.01, 40), Map.of(), ODBL);
        List<String> warnings = new ArrayList<>();
        var edges = builder.substationLoad(rules(null).get("substation-critical-load"), nodes, warnings);
        assertThat(edges).filteredOn(e -> e.to().equals(hospital)).hasSize(2)
                .allMatch(e -> e.label().equals("speculative"))
                .extracting(InfraGraphBuilder.EdgeData::from).containsExactly(near, far);
        assertThat(edges).filteredOn(e -> e.to().equals(lonely)).singleElement()
                .matches(e -> e.label().equals("possible") && e.from().equals(lonelySub));
        assertThat(warnings).anyMatch(w -> w.contains("rural radius"));
    }

    @Test
    @DisplayName("overrides change enabled and params, never the method or label; duplicates are dropped")
    void overrides() {
        Map<String, InfraRules.Rule> rules = rules(Map.of("rules", List.of(Map.of("id", "plant-substation",
                "enabled", false, "label", "confirmed", "method", "declared", "params", Map.of("maxKm", 9)))));
        InfraRules.Rule plant = rules.get("plant-substation");
        assertThat(plant.enabled()).isFalse();
        assertThat(plant.method()).isEqualTo("proximity");
        assertThat(plant.label()).isEqualTo("possible");
        assertThat(plant.number("maxKm", 0)).isEqualTo(9);
        assertThat(plant.number("capacityFactorKm", 0)).isEqualTo(2);

        node("power.substation", Geo.point(-95, 32), Map.of(), ODBL);
        node("power.line", Geo.factory().createLineString(new Coordinate[] {new Coordinate(-95, 32),
                new Coordinate(-95, 32.00001)}), Map.of(), ODBL);
        List<String> warnings = new ArrayList<>();
        var edges = builder.edges(new ArrayList<>(rules.values()), nodes, warnings);
        // Both ends hit the same substation: one edge, not two.
        assertThat(edges).hasSize(1);
        assertThat(warnings).contains("rule plant-substation is disabled");
    }

    @Test
    @DisplayName("label ceilings and ranks")
    void labels() {
        assertThat(InfraRules.cap("proximity", "confirmed")).isEqualTo("possible");
        assertThat(InfraRules.cap("proximity", "speculative")).isEqualTo("speculative");
        assertThat(InfraRules.cap("topological", "confirmed")).isEqualTo("confirmed");
        assertThat(InfraRules.rank("speculative")).isLessThan(InfraRules.rank("possible"));
        assertThat(InfraRules.rank("likely")).isLessThan(InfraRules.rank("confirmed"));
        assertThat(InfraImpactService.label(1)).isEqualTo("possible");
        assertThat(InfraImpactService.label(3)).isEqualTo("confirmed");
    }

    @Test
    @DisplayName("an edge carries the most restrictive license of its inputs")
    void licences() {
        assertThat(InfraLicences.mostRestrictive(List.of(ODBL, CC_BY))).isEqualTo("ODbL-1.0");
        assertThat(InfraLicences.mostRestrictive(List.of(CC_BY, CC_BY))).isEqualTo("CC-BY-4.0");
        assertThat(InfraLicences.mostRestrictive(List.of(ODBL, CC_BY_SA))).isEqualTo("CC-BY-SA-4.0 AND ODbL-1.0");
        assertThat(InfraLicences.mostRestrictive(List.of(CC_BY, LicenceTerms.unknown()))).isEqualTo("unknown");
    }

    @Test
    @DisplayName("distances are great-circle distances to the nearest point of a geometry")
    void distances() {
        var line = Geo.factory().createLineString(new Coordinate[] {new Coordinate(-95, 32), new Coordinate(-94, 32)});
        assertThat(InfraGraphBuilder.distanceKm(line, Geo.point(-94.5, 32.01))).isBetween(1.0, 1.2);
        assertThat(InfraGraphBuilder.endpoints(line)).hasSize(2);
        assertThat(InfraGraphBuilder.endpoints(Geo.point(0, 0))).isNull();
    }
}
