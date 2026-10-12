package ai.foresignal.trigger;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

/** {@link AlertContexts} (prompt 036): place names, versions and rounded distances, never coordinates. */
class AlertContextsTest {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");

    static MatchRow row(String geometry) {
        return new MatchRow(10, null, "s", "e", "k", 1, (short) 1, T, null, null, null, false, geometry, "{}", "enter");
    }

    @Test
    @DisplayName("a near-places rule: context.near with ids and versions; place.name and place.distanceKm variables")
    void places() {
        RuleSpec base = TestRules.rule().build();
        RuleSpec rule = new RuleSpec(base.id(), base.name(), true, base.sources(), base.kinds(), base.tags(),
                base.conditions(), base.fence(), null, null, 0, base.mode(), false, false, base.cooldown(),
                base.cooldownScope(), base.severity(), null, null, false, null, null, null, null, null, null, false, null,
                Map.of("placeIds", List.of("home"), "radiusKm", 0.0, RuleValidator.PLACE_VERSIONS, List.of(4)), false);
        PlaceLookup lookup = new PlaceLookup() {
            @Override
            public Optional<Geometry> geometry(String id) {
                return Optional.empty();
            }

            @Override
            public Optional<String> name(String id) {
                return Optional.of("Home");
            }

            @Override
            public Optional<Geometry> origin(String id) {
                return Optional.of(TestRules.GEO.createPoint(new Coordinate(0, 0)));
            }
        };
        AlertContexts.Built built = AlertContexts.build(rule, row("{\"type\":\"Point\",\"coordinates\":[0.1,0]}"), null,
                null, lookup);
        assertThat(built.json()).containsKey("near").doesNotContainKey("anchor");
        assertThat(built.json().get("near").toString()).contains("home").contains("4");
        assertThat(built.vars()).containsEntry("place.name", "Home").containsEntry("place.distanceKm", "11.1");
        assertThat(built.json().toString()).doesNotContain("0.1");
    }

    @Test
    @DisplayName("an anchored rule: context.anchor with seq and distance; no context for a plain rule")
    void anchor() {
        RuleSpec rule = TestRules.rule().build().withAnchor(new NearAnchor("phone", "p", 50, null, 200));
        AlertContexts.Built built = AlertContexts.build(rule, row("{\"type\":\"Point\",\"coordinates\":[0,0.2]}"),
                TestRules.GEO.createPoint(new Coordinate(0, 0)), 42L, null);
        assertThat(built.json().get("anchor").toString()).contains("seq=42").contains("distanceKm=22.2");
        assertThat(built.vars()).containsEntry("anchor.distanceKm", "22.2");
        assertThat(AlertContexts.build(TestRules.rule().build(), row(null), null, null, null).json()).isNull();
        assertThat(AlertContexts.round(0.04)).isEqualTo("0.0");
    }
}
