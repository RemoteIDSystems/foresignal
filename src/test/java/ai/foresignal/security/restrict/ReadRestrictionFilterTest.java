package ai.foresignal.security.restrict;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.store.query.SqlParams;
import ai.foresignal.stream.Bbox;
import ai.foresignal.trigger.Operator;
import ai.foresignal.trigger.OperatorRegistry;
import ai.foresignal.trigger.RecordPredicate;
import ai.foresignal.trigger.RuleCondition;
import ai.foresignal.view.PredicateFilter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * Prompt 036's members of {@link ReadRestriction}: the request filter (rendered and tested by
 * {@link RestrictionClamp}, combined with {@link RecordFilters#and}), {@code excludeSensitive} on
 * alert rows, and {@code placeIds} resolved through {@link PlaceAreas}.
 */
class ReadRestrictionFilterTest {

    static final GeometryFactory WGS84 = new GeometryFactory(new PrecisionModel(), 4326);

    @AfterEach
    void tearDown() {
        PlaceAreas.uninstall();
    }

    static PredicateFilter magFilter() {
        Operator gte = OperatorRegistry.builtIn().find(">=").orElseThrow();
        return new PredicateFilter(RecordPredicate.of(List.of(new RuleCondition("mag", gte, gte.normalize(5)))), "mag>=5");
    }

    static PredicateFilter areaFilter() {
        Geometry box = WGS84.toGeometry(new Envelope(0, 1, 0, 1));
        box.setSRID(4326);
        return new PredicateFilter(new RecordPredicate(Set.of("s"), Set.of(), List.of(), box), "box");
    }

    @Test
    @DisplayName("a filter makes the restriction data-restricting, enters the canonical form and is ANDed with another")
    void withFilter() {
        ReadRestriction base = ReadRestriction.NONE;
        assertThat(base.withFilter(null)).isSameAs(base);
        ReadRestriction one = base.withFilter(magFilter());
        assertThat(one.hasDataRestrictions()).isTrue();
        assertThat(one.canonical()).contains("f=");
        ReadRestriction two = one.withFilter(areaFilter());
        assertThat(two.filter().canonical()).contains("mag>=5").contains("box");
        assertThat(two.filter().needsPayload()).isTrue();
        assertThat(two).isNotEqualTo(one);
        assertThat(one.toStored()).as("filters are never stored").isNull();
    }

    @Test
    @DisplayName("SQL: the filter renders with the payload expression and fails closed without one")
    void sql() {
        ReadRestriction r = ReadRestriction.NONE.withFilter(magFilter()).withFilter(areaFilter());
        SqlParams p = new SqlParams();
        String withPayload = RestrictionClamp.sqlPredicate("h.source_id", "h.kind", "l.geom", "h.payload", r, p)
                .orElseThrow();
        assertThat(withPayload).contains("h.payload #>").contains("ST_Intersects(l.geom").contains("h.source_id = ANY(");
        String without = RestrictionClamp.sqlPredicate("h.source_id", "h.kind", "l.geom", r, new SqlParams()).orElseThrow();
        assertThat(without).contains("FALSE");
    }

    @Test
    @DisplayName("in memory: the shape test applies to geometries and boxes; payloads are judged by the filter")
    void inMemory() {
        ReadRestriction r = ReadRestriction.NONE.withFilter(areaFilter());
        assertThat(RestrictionClamp.test(r, "s", "k", WGS84.createPoint(new Coordinate(0.5, 0.5)))).isTrue();
        assertThat(RestrictionClamp.test(r, "s", "k", WGS84.createPoint(new Coordinate(2, 2)))).isFalse();
        assertThat(RestrictionClamp.test(r, "other", "k", WGS84.createPoint(new Coordinate(0.5, 0.5)))).isFalse();
        assertThat(RestrictionClamp.test(r, "s", "k", new Bbox(0.2, 0.2, 0.2, 0.2))).isTrue();
        assertThat(RestrictionClamp.test(r, "s", "k", (Bbox) null)).isFalse();
        PredicateFilter mag = magFilter();
        assertThat(mag.testPayload("s", Map.of("mag", 6))).isTrue();
        assertThat(mag.testPayload("s", Map.of("mag", 4))).isFalse();
        mag.remember("s", "e", true);
        assertThat(mag.remembered("s", "e")).isTrue();
        mag.remember("s", "e", false);
        assertThat(mag.remembered("s", "e")).isFalse();
    }

    @Test
    @DisplayName("excludeSensitive hides sensitive alerts in SQL and in memory, even without data restrictions")
    void sensitive() {
        ReadRestriction anonymous = ReadRestriction.NONE.withExcludeSensitive(true);
        assertThat(anonymous.excludeSensitive()).isTrue();
        assertThat(anonymous.hasDataRestrictions()).isFalse();
        String sql = RestrictionClamp.sqlPredicate("a.source_id", "a.kind", RestrictionClamp.ALERT_GEOMETRY_SQL, anonymous,
                new SqlParams()).orElseThrow();
        assertThat(sql).contains("NOT a.sensitive");
        assertThat(RestrictionClamp.sqlPredicate("h.source_id", "h.kind", "l.geom", anonymous, new SqlParams())).isEmpty();
        assertThat(RestrictionClamp.testSnapshot(anonymous, "s", "k", null, true)).isFalse();
        assertThat(RestrictionClamp.testSnapshot(anonymous, "s", "k", null, false)).isTrue();
        assertThat(RestrictionClamp.testSnapshot(ReadRestriction.NONE, "s", "k", null, true)).isTrue();
        assertThat(anonymous.withExcludeSensitive(true)).isSameAs(anonymous);
        assertThat(anonymous.canonical()).isEmpty();
    }

    @Test
    @DisplayName("placeIds resolve to the places' areas; without a resolver or with an unknown place nothing passes")
    void placeIds() {
        TokenRestrictions stored = new TokenRestrictions(null, null, null, null, null, null, null, List.of("home"));
        assertThat(stored.hasDataRestrictions()).isTrue();
        ReadRestriction unresolved = ReadRestriction.of(stored, false);
        assertThat(unresolved.region().isEmpty()).isTrue();
        assertThat(RestrictionClamp.sqlPredicate("h.source_id", "h.kind", "l.geom", unresolved, new SqlParams())
                .orElseThrow()).contains("FALSE");
        assertThat(PlaceAreas.exists("home")).isNull();

        Geometry home = WGS84.toGeometry(new Envelope(0, 1, 0, 1));
        home.setSRID(4326);
        PlaceAreas.install((Collection<String> ids) -> ids.contains("home") ? home : null, "home"::equals);
        ReadRestriction resolved = ReadRestriction.of(stored, false);
        assertThat(resolved.region().equalsTopo(home)).isTrue();
        assertThat(RestrictionClamp.test(resolved, "s", "k", WGS84.createPoint(new Coordinate(0.5, 0.5)))).isTrue();
        assertThat(PlaceAreas.exists("home")).isTrue();
        assertThat(PlaceAreas.exists("away")).isFalse();
        TokenRestrictions both = new TokenRestrictions(null, null, null, null,
                Map.of("type", "Polygon", "coordinates", List.of(List.of(List.of(0.5, 0.5), List.of(2.0, 0.5),
                        List.of(2.0, 2.0), List.of(0.5, 2.0), List.of(0.5, 0.5)))), null, null, List.of("home"));
        assertThat(ReadRestriction.of(both, false).region().getArea()).isCloseTo(0.25, org.assertj.core.data.Offset.offset(1e-9));
    }
}
