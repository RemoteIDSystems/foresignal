package ai.foresignal.trigger;

import ai.foresignal.config.StartupOrder;
import ai.foresignal.ingest.GateSnapshot;
import ai.foresignal.ingest.GateSpec;
import ai.foresignal.ingest.GateState;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.ingest.PersistGate;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceRegistryChangedEvent;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.store.TriggerRuleRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * The {@link PersistGate} that makes the writer persist every predicate flip of every enabled rule
 * (prompt 017, Core-Data-Model §11, invariant I8).
 *
 * <p>For each applicable rule it compares P on the <em>persisted</em> state ({@code h.payload}
 * paths, {@code h.persisted_geom}) with P on the candidate (the incoming snapshot and its geometry
 * after carry-forward) and reports a flip when they differ. Because the comparison is against
 * persisted state, a new or edited rule converges on each entity's next observation, a flip held
 * back by {@code edgeMinInterval} (applied by the policy engine, not here) is persisted on the first
 * observation after it, and a flip that reverses before then is never persisted.
 *
 * <p><b>Exact evaluation.</b> Conditions use the {@link Operator}s' Java meaning, which the shared
 * truth table proves equal to their SQL; geofences use JTS {@link PreparedGeometry#intersects}, the
 * same as {@code ST_Intersects}. A rule with a hysteresis band is compared against both band fences,
 * so both of its edges are persisted.
 *
 * <p><b>Conservative on ambiguity.</b> When either side's value is missing or of a type the operator
 * does not expect, that condition flips iff the before and candidate values differ, whatever P
 * says.
 *
 * <p>The snapshot ({@link Compiled}) is rebuilt whole on {@link TriggerRulesChangedEvent} and
 * {@link SourceRegistryChangedEvent} and swapped through an {@link AtomicReference}.
 *
 * <p><b>Prompt 036.</b> Identity conditions read, per source, the path the rule resolved for it
 * (watchlist membership comes from the operator's live {@code WatchlistSnapshot}, so both sides are
 * judged against the current list and a list edit never makes a stationary entity flip). Anchored
 * rules compare both sides against the anchor's <em>current</em> position from
 * {@link AnchorPositions}, the snapshot the SQL also uses; the Java distance is approximate, so a
 * target within a narrow band of the radius counts as ambiguous and flips whenever its geometry
 * changed (conservative). A stale anchor suspends the rule here too.
 */
@Component
@Primary
public class TriggerPersistGate implements PersistGate, IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(TriggerPersistGate.class);

    private final TriggerRuleRepository repository;
    private final RuleSpecs specs;
    private final SourceRegistry registry;
    private final AnchorPositions anchors;
    private final java.time.Clock clock;
    private final AtomicReference<GateSnapshot> snapshot = new AtomicReference<>();

    /** Creates the gate; the first snapshot is built when the application is ready. */
    @org.springframework.beans.factory.annotation.Autowired
    public TriggerPersistGate(TriggerRuleRepository repository, RuleSpecs specs, SourceRegistry registry,
            AnchorPositions anchors, java.time.Clock clock) {
        this.repository = repository;
        this.specs = specs;
        this.registry = registry;
        this.anchors = anchors;
        this.clock = clock;
    }

    /** A gate without moving anchors (tests). */
    public TriggerPersistGate(TriggerRuleRepository repository, RuleSpecs specs, SourceRegistry registry) {
        this(repository, specs, registry, null, java.time.Clock.systemUTC());
    }

    @Override
    public GateSnapshot snapshot() {
        GateSnapshot current = snapshot.get();
        if (current == null) {
            rebuild();
            current = snapshot.get();
        }
        return current;
    }

    /** Builds the first snapshot before the writer starts. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.TRIGGER_GATE)
    public void onReady() {
        rebuild();
    }

    /** Rebuilds after a rule change. */
    @EventListener
    public void onRulesChanged(TriggerRulesChangedEvent event) {
        rebuild();
    }

    /** Rebuilds after a source registers or unregisters (kind-only rules depend on the registry). */
    @EventListener
    public void onRegistryChanged(SourceRegistryChangedEvent event) {
        rebuild();
    }

    /** Rebuilds from the database after the test cleaner truncated it. */
    @Override
    public void resetState() {
        rebuild();
    }

    /** Reloads every enabled rule and swaps the snapshot. */
    public void rebuild() {
        List<RuleSpec> compiled = new ArrayList<>();
        for (TriggerRuleEntity rule : repository.findByEnabledTrue()) {
            try {
                compiled.add(specs.compile(rule));
            } catch (RuntimeException e) {
                log.warn("Trigger rule {} is left out of the persist gate: {}", rule.getId(), e.getMessage());
            }
        }
        snapshot.set(new Compiled(compiled, id -> registry.find(id).map(r -> r.descriptor()).orElse(null),
                anchors == null ? a -> null : a -> anchors.current(a, clock.instant())));
    }

    /**
     * An immutable gate snapshot over compiled rules. Public so the truth-table test can build one
     * without Spring.
     */
    public static final class Compiled implements GateSnapshot {

        private final List<GateRule> rules;
        private final Function<String, SourceDescriptor> descriptors;
        private final Map<String, GateSpec> specs = new ConcurrentHashMap<>();

        /**
         * Compiles {@code rules} without moving anchors (an anchored rule is then suspended).
         *
         * @param rules the enabled rules
         * @param descriptors the descriptor of a source id, or null when unknown
         */
        public Compiled(List<RuleSpec> rules, Function<String, SourceDescriptor> descriptors) {
            this(rules, descriptors, a -> null);
        }

        /**
         * Compiles {@code rules}.
         *
         * @param rules the enabled rules
         * @param descriptors the descriptor of a source id, or null when unknown
         * @param anchors the current position of a fresh anchor, or null when stale or unknown
         */
        public Compiled(List<RuleSpec> rules, Function<String, SourceDescriptor> descriptors,
                Function<NearAnchor, org.locationtech.jts.geom.Point> anchors) {
            List<GateRule> list = new ArrayList<>();
            for (RuleSpec rule : rules) {
                list.add(new GateRule(rule, anchors));
            }
            this.rules = List.copyOf(list);
            this.descriptors = descriptors;
        }

        @Override
        public GateSpec spec(String sourceId) {
            return specs.computeIfAbsent(sourceId, this::computeSpec);
        }

        private GateSpec computeSpec(String sourceId) {
            SourceDescriptor descriptor = descriptors.apply(sourceId);
            Set<String> kinds = descriptor == null ? Set.of() : descriptor.kinds();
            Set<String> paths = new LinkedHashSet<>();
            boolean geometry = false;
            for (GateRule gate : rules) {
                RuleSpec rule = gate.rule;
                if (!rule.watchesSource(sourceId)) {
                    continue;
                }
                if (!rule.kinds().isEmpty() && descriptor != null && kinds.stream().noneMatch(rule::watchesKind)) {
                    continue;
                }
                for (RuleCondition condition : rule.conditions()) {
                    String path = condition.pathFor(sourceId);
                    if (path != null) {
                        paths.add(path);
                    }
                }
                geometry |= rule.spatial();
            }
            return paths.isEmpty() && !geometry ? GateSpec.NONE : new GateSpec(paths, geometry);
        }

        @Override
        public boolean flips(String sourceId, String kind, GateState persisted, GateState candidate) {
            for (GateRule gate : rules) {
                if (gate.rule.appliesTo(sourceId, kind) && gate.flips(sourceId, persisted, candidate)) {
                    return true;
                }
            }
            return false;
        }
    }

    /** One rule with its fences prepared. */
    static final class GateRule {

        final RuleSpec rule;
        final List<PreparedGeometry> fences;
        final Function<NearAnchor, org.locationtech.jts.geom.Point> anchors;

        GateRule(RuleSpec rule) {
            this(rule, a -> null);
        }

        GateRule(RuleSpec rule, Function<NearAnchor, org.locationtech.jts.geom.Point> anchors) {
            this.rule = rule;
            this.anchors = anchors;
            List<PreparedGeometry> prepared = new ArrayList<>();
            if (rule.hysteresis()) {
                prepared.add(prepare(rule.fenceInner()));
                prepared.add(prepare(rule.fenceOuter()));
            } else if (rule.hasFence()) {
                prepared.add(prepare(rule.fence()));
            }
            this.fences = List.copyOf(prepared);
        }

        private static PreparedGeometry prepare(MultiPolygon fence) {
            return PreparedGeometryFactory.prepare(fence);
        }

        /** {@link #flips(String, GateState, GateState)} for a rule without identity conditions. */
        boolean flips(GateState before, GateState candidate) {
            return flips(null, before, candidate);
        }

        /** True when P differs between {@code before} and {@code candidate}, or conservatively so. */
        boolean flips(String sourceId, GateState before, GateState candidate) {
            boolean conditionsBefore = true;
            boolean conditionsCandidate = true;
            for (RuleCondition condition : rule.conditions()) {
                String path = condition.pathFor(sourceId);
                Object b = path == null ? null : before.pathValues().get(path);
                Object c = path == null ? null : candidate.pathValues().get(path);
                Evaluation eb = condition.evaluateValue(b);
                Evaluation ec = condition.evaluateValue(c);
                if ((eb == Evaluation.AMBIGUOUS || ec == Evaluation.AMBIGUOUS) && !jsonEquals(b, c)) {
                    return true;
                }
                conditionsBefore &= eb.holds();
                conditionsCandidate &= ec.holds();
            }
            if (rule.anchored()) {
                org.locationtech.jts.geom.Point anchor = anchors.apply(rule.anchor());
                if (anchor == null) {
                    // A stale or unknown anchor suspends the rule: P is false on both sides.
                    return false;
                }
                Evaluation nb = AnchorDistance.near(rule.anchor(), anchor, before.geometry());
                Evaluation nc = AnchorDistance.near(rule.anchor(), anchor, candidate.geometry());
                if ((nb == Evaluation.AMBIGUOUS || nc == Evaluation.AMBIGUOUS)
                        && !geometryEquals(before.geometry(), candidate.geometry())) {
                    return true;
                }
                return (conditionsBefore && nb.holds()) != (conditionsCandidate && nc.holds());
            }
            if (fences.isEmpty()) {
                return conditionsBefore != conditionsCandidate;
            }
            for (PreparedGeometry fence : fences) {
                boolean pBefore = conditionsBefore && inside(fence, before.geometry());
                boolean pCandidate = conditionsCandidate && inside(fence, candidate.geometry());
                if (pBefore != pCandidate) {
                    return true;
                }
            }
            return false;
        }

        private static boolean inside(PreparedGeometry fence, Geometry geometry) {
            return geometry != null && !geometry.isEmpty() && fence.intersects(geometry);
        }
    }

    /** True when two geometries are both absent or exactly equal. */
    static boolean geometryEquals(Geometry a, Geometry b) {
        if (a == null || a.isEmpty()) {
            return b == null || b.isEmpty();
        }
        return b != null && a.equalsExact(b);
    }

    /** JSON equality: numbers by numeric value, lists and objects element-wise, the rest by equals. */
    static boolean jsonEquals(Object a, Object b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        BigDecimal na = a instanceof Boolean ? null : Operators.number(a);
        BigDecimal nb = b instanceof Boolean ? null : Operators.number(b);
        if (na != null || nb != null) {
            return na != null && nb != null && na.compareTo(nb) == 0;
        }
        if (a instanceof List<?> la && b instanceof List<?> lb) {
            if (la.size() != lb.size()) {
                return false;
            }
            Iterator<?> ia = la.iterator();
            Iterator<?> ib = lb.iterator();
            while (ia.hasNext()) {
                if (!jsonEquals(ia.next(), ib.next())) {
                    return false;
                }
            }
            return true;
        }
        if (a instanceof Map<?, ?> ma && b instanceof Map<?, ?> mb) {
            if (!ma.keySet().equals(mb.keySet())) {
                return false;
            }
            for (Map.Entry<?, ?> entry : ma.entrySet()) {
                if (!jsonEquals(entry.getValue(), mb.get(entry.getKey()))) {
                    return false;
                }
            }
            return true;
        }
        return Objects.equals(a, b);
    }
}
