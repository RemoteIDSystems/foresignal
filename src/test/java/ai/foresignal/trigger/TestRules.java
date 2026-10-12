package ai.foresignal.trigger;

import ai.foresignal.store.TriggerMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

/** A fluent builder of {@link RuleSpec}s for unit tests. */
final class TestRules {

    static final GeometryFactory GEO = new GeometryFactory(new PrecisionModel(), 4326);

    UUID id = UUID.fromString("00000000-0000-0000-0000-000000000017");
    String name = "test rule";
    Set<String> sources = Set.of();
    Set<String> kinds = Set.of();
    List<String> tags = List.of();
    List<RuleCondition> conditions = new ArrayList<>();
    MultiPolygon fence;
    MultiPolygon inner;
    MultiPolygon outer;
    double hysteresis;
    TriggerMode mode = TriggerMode.MATCH;
    boolean onlyNew;
    boolean reappear;
    Duration cooldown = Duration.ZERO;
    CooldownScope scope = CooldownScope.RECORD;
    AutoResolve autoResolve;
    Integer cap;
    boolean autoDisable;
    Instant activeFrom;
    Instant activeUntil;
    ActiveWindow window;
    Duration horizon;
    Instant snoozedUntil;

    static TestRules rule() {
        return new TestRules();
    }

    TestRules when(String path, String op, Object value) {
        Operator operator = OperatorRegistry.builtIn().find(op).orElseThrow();
        conditions.add(new RuleCondition(path, operator, operator.normalize(value)));
        return this;
    }

    TestRules sources(String... ids) {
        sources = Set.of(ids);
        return this;
    }

    TestRules kinds(String... values) {
        kinds = Set.of(values);
        return this;
    }

    TestRules fence(double minX, double minY, double maxX, double maxY) {
        fence = box(minX, minY, maxX, maxY);
        return this;
    }

    TestRules band(MultiPolygon shrunk, MultiPolygon grown, double meters) {
        inner = shrunk;
        outer = grown;
        hysteresis = meters;
        return this;
    }

    TestRules mode(TriggerMode value) {
        mode = value;
        return this;
    }

    TestRules cooldown(Duration value, CooldownScope s) {
        cooldown = value;
        scope = s;
        return this;
    }

    RuleSpec build() {
        return new RuleSpec(id, name, true, sources, kinds, tags, conditions, fence, inner, outer, hysteresis, mode,
                onlyNew, reappear, cooldown, scope, Severity.WARNING, autoResolve, cap, autoDisable, activeFrom,
                activeUntil, window, horizon, null, snoozedUntil, false);
    }

    static MultiPolygon box(double minX, double minY, double maxX, double maxY) {
        Polygon polygon = (Polygon) GEO.toGeometry(new Envelope(minX, maxX, minY, maxY));
        MultiPolygon multi = GEO.createMultiPolygon(new Polygon[] {polygon});
        multi.setSRID(4326);
        return multi;
    }

    static Point point(double lon, double lat) {
        return GEO.createPoint(new Coordinate(lon, lat));
    }
}
