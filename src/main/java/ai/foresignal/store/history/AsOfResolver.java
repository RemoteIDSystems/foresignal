package ai.foresignal.store.history;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.ingest.SourceIngestProfiles;
import ai.foresignal.retention.RetentionResolver;
import ai.foresignal.retention.RetentionState;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.ReadQueryRunner;
import ai.foresignal.store.query.SeqClockQuery;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Shared asOf planning on the transaction axis (prompt 037; Core-Data-Model §12), used by
 * {@code /v1/records?asOf}, asOf tiles, tracks, history frames and the MCP time-travel tools.
 *
 * <p>Rules, in order:
 *
 * <ol>
 *   <li>An empty filter (no source and no kind) is {@code 400 ASOF_FILTER_REQUIRED}.
 *   <li>{@code t} in the future is {@code 400 ASOF_INVALID} (the code prompt 014 shipped and the
 *       OpenAPI document lists for {@code listRecords}).
 *   <li>Private sources are dropped unless the caller passes the private gate of prompt 023
 *       ({@link SourceVisibility}): callers pass the sources they may not see, and a plan made
 *       without a caller drops every private source.
 *   <li>{@code horizon(source) = now − window + effectiveKeyframe + 1 h}; {@code t} before the
 *       latest horizon of any involved source is {@code 400 ASOF_BEYOND_RETENTION} with
 *       {@code horizon}.
 *   <li>{@code S = S(t)} from {@code seq_clock}; TRACK {@code L = S(t − validity − 2 min)}, STANDARD
 *       {@code L = 0}. A tier's validity is {@code ttl + widening} when its widened range
 *       {@code [L, S]} is {@link HistoryFidelity#downsampled downsampled} (prompt 044: TRACK validity
 *       becomes {@code max(ttl, downsampleInterval)} over thinned history), else the plain TTL.
 *   <li>{@code approximate} = any involved source is SAMPLED or LATEST, or
 *       {@link HistoryFidelity#downsampled} reports a tier range {@code [L, S]}.
 * </ol>
 */
@Component
public class AsOfResolver {

    /** Margin past the TTL when choosing the TRACK lower seq bound. */
    public static final Duration TRACK_LOWER_MARGIN = Duration.ofMinutes(2);

    private final ReadQueryRunner runner;
    private final SeqClockQuery seqClock;
    private final SourceRegistry registry;
    private volatile SourceVisibility visibility;
    private final SourceIngestProfiles profiles;
    private final RetentionState retention;
    private final HistoryFidelity fidelity;
    private final Clock clock;

    /** Creates the resolver. */
    public AsOfResolver(
            ReadQueryRunner runner,
            SeqClockQuery seqClock,
            SourceRegistry registry,
            SourceIngestProfiles profiles,
            RetentionState retention,
            HistoryFidelity fidelity,
            Clock clock) {
        this.runner = runner;
        this.seqClock = seqClock;
        this.registry = registry;
        this.profiles = profiles;
        this.retention = retention;
        this.fidelity = fidelity;
        this.clock = clock;
    }

    /** The plan for {@code sourceIds} without a caller: every private source is dropped. */
    public AsOfPlan plan(Instant t, Set<String> sourceIds) {
        return plan(t, sourceIds, List.of(), visibility().privateSourceIds());
    }

    /** The plan for {@code sourceIds}, dropping {@code excluded} (the caller's private gate). */
    public AsOfPlan plan(Instant t, Collection<String> sourceIds, Set<String> excluded) {
        return plan(t, sourceIds, List.of(), excluded);
    }

    /**
     * The plan for the sources named in {@code sources}, or else the sources declaring one of
     * {@code kinds}, dropping {@code excluded}. Runs its own read transaction for {@code S} and
     * {@code L}.
     *
     * @throws ValidationException {@code ASOF_FILTER_REQUIRED}, {@code ASOF_INVALID} or
     *     {@code ASOF_BEYOND_RETENTION}
     * @throws ai.foresignal.api.error.NotFoundException for an unknown source id
     */
    public AsOfPlan plan(Instant t, Collection<String> sources, Collection<String> kinds, Set<String> excluded) {
        Prepared prepared = prepare(t, sources, kinds, excluded);
        return runner.read(jdbc -> finish(jdbc, prepared));
    }

    /** {@link #plan(Instant, Collection, Collection, Set)} inside the caller's read transaction. */
    public AsOfPlan plan(NamedParameterJdbcTemplate jdbc, Instant t, Collection<String> sources,
            Collection<String> kinds, Set<String> excluded) {
        return finish(jdbc, prepare(t, sources, kinds, excluded));
    }

    /** {@code now − window + effectiveKeyframe + 1 h} for one source. */
    public Instant horizon(String sourceId, Instant now) {
        // The math is RetentionResolver's (prompt 019); the window comes from RetentionState so a
        // test's stub window still applies.
        return RetentionResolver.horizon(now, retention.window(sourceId), profiles.get(sourceId).keyframe());
    }

    /** The latest horizon over {@code sourceIds} at {@code now}; null when the set is empty. */
    public Instant horizon(Collection<String> sourceIds, Instant now) {
        Instant latest = null;
        for (String id : sourceIds) {
            Instant h = horizon(id, now);
            if (latest == null || h.isAfter(latest)) {
                latest = h;
            }
        }
        return latest;
    }

    /** True when {@code sourceId}'s ingest policy stores only some observations (SAMPLED or LATEST). */
    public boolean sampled(String sourceId) {
        IngestPolicy.PolicyMode mode = profiles.get(sourceId).policy().mode();
        return mode == IngestPolicy.PolicyMode.SAMPLED || mode == IngestPolicy.PolicyMode.LATEST;
    }

    /**
     * True when history at {@code plan.t()} will not be rewritten for any involved tier:
     * {@code t < finalBefore(tier)} or the tier range {@code [L, S]} is already downsampled. Part of
     * the immutable-caching rule ({@code TimeTravelCache}).
     */
    public boolean settled(AsOfPlan plan) {
        for (HistoryTier tier : plan.sources().keySet()) {
            if (!settled(tier, plan.t(), plan.lower(tier), plan.s())) {
                return false;
            }
        }
        return true;
    }

    /** {@link #settled(AsOfPlan)} for one tier, instant and seq range. */
    public boolean settled(HistoryTier tier, Instant t, long fromSeq, long toSeq) {
        Instant finalBefore = fidelity.finalBefore(tier);
        return (finalBefore != null && t.isBefore(finalBefore)) || fidelity.downsampled(tier, fromSeq, toSeq);
    }

    /** The fidelity hook (prompt 044 implements it). */
    public HistoryFidelity fidelity() {
        return fidelity;
    }

    /**
     * The involved sources: each id of {@code sources} (404 when unknown), else every source
     * declaring one of {@code kinds}; {@code excluded} removed; sorted by id.
     */
    public List<SourceDescriptor> involved(Collection<String> sources, Collection<String> kinds, Set<String> excluded) {
        Set<String> ids = new LinkedHashSet<>();
        if (sources != null && !sources.isEmpty()) {
            for (String id : sources) {
                ids.add(registry.descriptor(id).id());
            }
        } else if (kinds != null) {
            for (SourceDescriptor descriptor : registry.descriptors()) {
                for (String kind : kinds) {
                    if (descriptor.kinds().contains(kind)) {
                        ids.add(descriptor.id());
                    }
                }
            }
        }
        if (excluded != null) {
            ids.removeAll(excluded);
        }
        List<SourceDescriptor> result = new ArrayList<>();
        for (String id : new TreeSet<>(ids)) {
            result.add(registry.descriptor(id));
        }
        result.sort(Comparator.comparing(SourceDescriptor::id));
        return result;
    }

    /** Everything that needs no database: validation, involved sources, horizon, validity. */
    private Prepared prepare(Instant t, Collection<String> sources, Collection<String> kinds, Set<String> excluded) {
        if ((sources == null || sources.isEmpty()) && (kinds == null || kinds.isEmpty())) {
            throw new ValidationException(ErrorCodes.ASOF_FILTER_REQUIRED, "asOf needs a source or kind filter.",
                    List.of());
        }
        if (t == null) {
            throw new ValidationException(ErrorCodes.ASOF_INVALID, "asOf is required.", List.of());
        }
        Instant now = clock.instant();
        if (t.isAfter(now)) {
            throw new ValidationException(ErrorCodes.ASOF_INVALID, "asOf must not be in the future.", List.of());
        }
        List<SourceDescriptor> involved = involved(sources, kinds, excluded);
        Instant horizon = null;
        boolean approximate = false;
        Map<HistoryTier, List<SourceDescriptor>> byTier = new EnumMap<>(HistoryTier.class);
        Map<String, Duration> validity = new LinkedHashMap<>();
        Map<String, Duration> widened = new LinkedHashMap<>();
        for (SourceDescriptor descriptor : involved) {
            Instant h = horizon(descriptor.id(), now);
            if (horizon == null || h.isAfter(horizon)) {
                horizon = h;
            }
            approximate |= sampled(descriptor.id());
            byTier.computeIfAbsent(descriptor.historyTier(), k -> new ArrayList<>()).add(descriptor);
            Duration ttl = descriptor.recordTtl();
            Duration widening = fidelity.validityWidening(descriptor.id());
            validity.put(descriptor.id(), ttl);
            widened.put(descriptor.id(), ttl == null ? null : ttl.plus(widening == null ? Duration.ZERO : widening));
        }
        if (horizon != null && t.isBefore(horizon)) {
            throw new ValidationException(ErrorCodes.ASOF_BEYOND_RETENTION,
                            "asOf " + t + " is before the retention horizon " + horizon + ".", List.of())
                    .withProperty("horizon", horizon.toString());
        }
        return new Prepared(t, byTier, validity, widened, horizon, approximate);
    }

    private AsOfPlan finish(NamedParameterJdbcTemplate jdbc, Prepared p) {
        long s = seqClock.seqAt(jdbc, p.t());
        Map<HistoryTier, Long> lower = new EnumMap<>(HistoryTier.class);
        Map<String, Duration> validity = new LinkedHashMap<>(p.validity());
        boolean downsampled = false;
        for (Map.Entry<HistoryTier, List<SourceDescriptor>> tier : p.byTier().entrySet()) {
            List<SourceDescriptor> descriptors = tier.getValue();
            long wide = lowerBound(jdbc, p.t(), tier.getKey(), descriptors, p.widened());
            if (fidelity.downsampled(tier.getKey(), wide, s)) {
                // Thinned history in range: bridge its gaps with the widened validity (prompt 044).
                downsampled = true;
                lower.put(tier.getKey(), wide);
                for (SourceDescriptor descriptor : descriptors) {
                    validity.put(descriptor.id(), p.widened().get(descriptor.id()));
                }
            } else {
                boolean same = descriptors.stream().allMatch(
                        d -> java.util.Objects.equals(p.validity().get(d.id()), p.widened().get(d.id())));
                lower.put(tier.getKey(), same ? wide : lowerBound(jdbc, p.t(), tier.getKey(), descriptors, p.validity()));
            }
        }
        return new AsOfPlan(p.t(), s, lower, p.horizon(), p.approximate() || downsampled, s, p.byTier(), validity,
                downsampled);
    }

    /** TRACK: {@code S(t − validity − 2 min)}, the smallest over the tier's sources; STANDARD: 0. */
    private long lowerBound(NamedParameterJdbcTemplate jdbc, Instant t, HistoryTier tier,
            List<SourceDescriptor> descriptors, Map<String, Duration> validity) {
        if (tier != HistoryTier.TRACK) {
            return 0L;
        }
        long l = Long.MAX_VALUE;
        for (SourceDescriptor descriptor : descriptors) {
            Duration v = validity.get(descriptor.id());
            long candidate = v == null ? 0L : seqClock.seqAt(jdbc, t.minus(v).minus(TRACK_LOWER_MARGIN));
            l = Math.min(l, candidate);
        }
        return l;
    }

    private record Prepared(Instant t, Map<HistoryTier, List<SourceDescriptor>> byTier,
            Map<String, Duration> validity, Map<String, Duration> widened, Instant horizon, boolean approximate) {}

    /** Injects the shared private-source visibility (prompt 039). */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setVisibility(SourceVisibility visibility) {
        this.visibility = visibility;
    }

    private SourceVisibility visibility() {
        SourceVisibility v = visibility;
        if (v == null) {
            v = new SourceVisibility(registry, null, null);
            visibility = v;
        }
        return v;
    }
}
