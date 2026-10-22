package ai.foresignal.source.adapters.derived;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.DerivedGenerator;
import ai.foresignal.derive.DerivedInputs;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.derive.LayerStatus;
import ai.foresignal.derive.StatefulGenerator;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;

/**
 * Base class for the prompt 099 derived layers: a STANDARD, ALL, {@code recordTtl=null} DERIVED source, enabled at
 * seeding unless it needs a parameter (prompt 145), evaluated on a 60-second tick, that consumes the stored and live
 * records of its input source families through the {@link ai.foresignal.derive.ObservationFeed}.
 *
 * <p>Input families are prefixes resolved against the {@link SourceRegistry} at start, so the
 * configured receiver slots ({@code local-adsb-*}, {@code adsbfi-region-*}, …) are picked up without
 * hard-coding them. The generator itself is never its own input (the graph forbids a cycle).
 *
 * <p>Every generator is coverage-aware: before it asserts an absence it checks its {@code status}
 * (a gap marks the window {@code degraded-input}), and it never tombstones because input is stale.
 */
public abstract class LayerGenerator implements DerivedGenerator, StatefulGenerator {

    /** The default per-generator state cap. */
    public static final int DEFAULT_MAX_ENTITIES = 200_000;

    protected static final Logger log = LoggerFactory.getLogger("foresignal.derived");

    private final ObjectProvider<SourceRegistry> registry;
    private volatile Set<String> resolvedSources;

    /** Creates the generator. */
    protected LayerGenerator(ObjectProvider<SourceRegistry> registry) {
        this.registry = registry;
    }

    /** The source-id prefixes this generator consumes. */
    protected abstract Set<String> inputPrefixes();

    /** Explicit source ids this generator consumes, beyond the prefixes (for example another layer). */
    protected Set<String> explicitInputSources() {
        return Set.of();
    }

    /** The record kinds this generator consumes. */
    protected abstract Set<String> inputKinds();

    /** The tick period; 60 seconds unless a layer needs otherwise. */
    protected Duration tick() {
        return Duration.ofSeconds(60);
    }

    /** True when a place edit should trigger a reconcile. */
    protected boolean wantsPlaces() {
        return false;
    }

    /**
     * The concrete input source ids: every registered id matching a prefix, plus the explicit ones,
     * never this source.
     */
    public final Set<String> inputSources() {
        Set<String> cached = resolvedSources;
        if (cached != null) {
            return cached;
        }
        Set<String> out = new TreeSet<>();
        SourceRegistry sourceRegistry = registry.getIfAvailable();
        if (sourceRegistry != null) {
            for (String id : sourceRegistry.ids()) {
                if (id.equals(id())) {
                    continue;
                }
                for (String prefix : inputPrefixes()) {
                    if (id.startsWith(prefix)) {
                        out.add(id);
                        break;
                    }
                }
            }
        }
        out.addAll(explicitInputSources());
        out.remove(id());
        cached = Set.copyOf(out);
        resolvedSources = cached;
        return cached;
    }

    @Override
    public DerivedInputs inputs(ValidatedParams params) {
        return new DerivedInputs(inputSources(), inputKinds(), Set.of(), tick(), wantsPlaces());
    }

    /**
     * Startup reconciliation (prompt 099): empties the in-memory state, then hands every live record
     * of this layer to {@link #adopt}, so a record whose condition no longer holds is still ended by the
     * layer's own rules instead of being orphaned by the restart.
     */
    @Override
    public void reconcile(List<HeadSnapshot> ownHeads, DerivedContext ctx) {
        clearState();
        for (HeadSnapshot head : ownHeads) {
            if (!head.tombstone()) {
                adopt(head, ctx);
            }
        }
    }

    /**
     * Re-adopts one of this layer's live records after {@link #clearState()}. The default keeps the
     * record as it is; a layer whose records are only ended from its in-memory state overrides this so
     * the record re-enters that state and its normal refresh-or-tombstone lifecycle applies.
     *
     * @param head a live (non-tombstoned) record of this layer
     * @param ctx the reconcile invocation's context
     */
    protected void adopt(HeadSnapshot head, DerivedContext ctx) {
        // Kept as is by default.
    }

    /** Empties the generator's in-memory state. */
    protected abstract void clearState();

    /** The coverage-aware status of this generator's window. */
    protected LayerStatus status(DerivedContext ctx, Instant windowStart) {
        return ctx.coverage().statusFor(inputSources(), windowStart);
    }

    /** The per-generator state cap. */
    protected int maxEntities(ValidatedParams params) {
        return params.integer("maxEntities", DEFAULT_MAX_ENTITIES);
    }

    @Override
    public int stateSize() {
        return 0;
    }

    /**
     * A standard layer descriptor builder: DERIVED, STANDARD, no TTL, 60 s
     * cadence, {@code zeroResultsNormal}, with the given licence and the {@code maxEntities} parameter.
     */
    protected static SourceDescriptor.Builder layer(String id, String name, String description, String licence,
            String kind) {
        return SourceDescriptor.builder(id)
                .name(name)
                .description(description)
                .kind(kind)
                .licence(licence)
                .mode(Mode.DERIVED)
                .defaultBudget(Budget.of(1, 1))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(null)
                .zeroResultsNormal(true)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(Duration.ofDays(90))
                .kindRetention(kind, Duration.ofDays(90))
                .param(ParamSpec.optional("maxEntities", ParamType.INTEGER,
                        "The most entities held in memory before the layer drops the excess",
                        DEFAULT_MAX_ENTITIES))
                .param(ParamSpec.optional("includePrivate", ParamType.BOOLEAN,
                        "Whether private sources may feed this layer", Boolean.FALSE));
    }
}
