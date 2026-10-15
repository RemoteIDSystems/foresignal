package ai.foresignal.source.adapters.derived;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.Observation;
import ai.foresignal.derive.StatefulGenerator;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.formats.readsb.AircraftDescriptors;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PositionSource;
import ai.foresignal.source.spi.Quality;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * {@code derived-aircraft} (prompt 099 §3.10): fusion by hex across every {@code adsblol-*},
 * {@code airplaneslive-*}, {@code adsbfi-*}, {@code opensky-states} and {@code local-adsb-*}/{@code
 * local-sbs-*} source. Since every aircraft source uses {@code externalId = hex}, the freshest
 * observation by {@code observedAt} wins — never merge order — and ties prefer ADS-B over MLAT over
 * TIS-B, then a local receiver over the network.
 *
 * <p>The one DERIVED source that is a TRACK: its output is a continuous track (§5.3), so it keeps a
 * 5-minute TTL and must be registered with {@code HistoryTier.TRACK}. It carries the winning
 * fields plus {@code sources[]}, {@code primarySource} and {@code positionSource}.
 */
@Component
public class AircraftFusionGenerator extends LayerGenerator {

    /** The source id. */
    public static final String ID = "derived-aircraft";

    /** The record kind, shared with the raw aircraft sources so rules and tiles apply unchanged. */
    public static final String KIND = DerivedSources.AIRCRAFT;

    /** The TTL of a fused track. */
    public static final Duration TTL = Duration.ofMinutes(5);

    private final SourceDescriptor descriptor = buildDescriptor();

    /** Creates the generator. */
    public AircraftFusionGenerator(ObjectProvider<SourceRegistry> registry) {
        super(registry);
    }

    /** The descriptor. */
    public static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("Fused aircraft track (derived)")
                .description("One record per ICAO 24-bit address: the freshest observation across the"
                        + " ADS-B/MLAT network and the operator's local receivers, with the sources that"
                        + " reported it. Automated, not a warning.")
                .kind(KIND)
                .attribution("Derived by Foresignal from its aircraft sources")
                .licence("derived (most restrictive input; see inputs)")
                .mode(Mode.DERIVED)
                .defaultBudget(Budget.of(1, 1))
                .historyTier(HistoryTier.TRACK)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(TTL)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .significantFields(AircraftDescriptors.SIGNIFICANT_FIELDS.toArray(String[]::new))
                .tileProperties(AircraftDescriptors.TILE_PROPERTIES.toArray(String[]::new))
                .aggBelowZoom(AircraftDescriptors.AGG_BELOW_ZOOM)
                .zeroResultsNormal(true)
                .defaultIngestPolicy(IngestPolicy.all())
                .param(ParamSpec.optional("maxEntities", ParamType.INTEGER,
                        "The most aircraft fused per tick before the excess is dropped", DEFAULT_MAX_ENTITIES))
                .build();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    protected Set<String> inputPrefixes() {
        return DerivedSources.AIRCRAFT_PREFIXES;
    }

    @Override
    protected Set<String> inputKinds() {
        return Set.of(KIND);
    }

    @Override
    protected void clearState() {
        // Stateless: the fusion is recomputed from the current feed each tick; the emit ledger
        // suppresses unchanged versions.
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        int cap = maxEntities(ctx.params());
        List<Observation> observations =
                ctx.observations().recent(inputSources(), Set.of(KIND), now.minus(TTL), cap);
        Map<String, List<Observation>> byHex = new LinkedHashMap<>();
        for (Observation observation : observations) {
            byHex.computeIfAbsent(observation.externalId(), k -> new ArrayList<>(4)).add(observation);
        }
        for (Map.Entry<String, List<Observation>> entry : byHex.entrySet()) {
            List<Observation> group = entry.getValue();
            Observation winner = winner(group);
            if (winner == null || !winner.located()) {
                continue;
            }
            ctx.emit(record(winner, group));
        }
    }

    /** The freshest observation of a hex, tie-broken by position source then locality then source id. */
    static Observation winner(List<Observation> group) {
        return group.stream()
                .min(FUSION_ORDER)
                .orElse(null);
    }

    /** Newest first; ties prefer ADS-B over MLAT over other, then a local receiver over the network. */
    private static final Comparator<Observation> FUSION_ORDER = Comparator
            .comparing((Observation o) -> o.observedAt(), Comparator.nullsFirst(Comparator.naturalOrder()))
            .reversed()
            .thenComparingInt(o -> Fusion.rank(o))
            .thenComparing(Observation::sourceId);

    /** The fused record: the winning payload plus its provenance. */
    static NormalizedRecord record(Observation winner, List<Observation> group) {
        Map<String, Object> payload = new LinkedHashMap<>(winner.payload());
        Set<String> sources = new TreeSet<>();
        for (Observation observation : group) {
            sources.add(observation.sourceId());
        }
        payload.put("sources", new ArrayList<>(sources));
        payload.put("primarySource", winner.sourceId());
        payload.put("positionSource", positionSource(winner));
        return NormalizedRecord.builder(winner.externalId(), KIND)
                .observedAt(winner.observedAt())
                .geometry(winner.geometry())
                .putAll(payload)
                .quality(Quality.positionSource(winner.positionSource()))
                .build();
    }

    /** The winning observation's {@code posSource} payload value, or its position source. */
    static String positionSource(Observation winner) {
        String raw = winner.text("posSource");
        return raw != null ? raw : winner.positionSource().wireValue();
    }

    /** Ranks observations: freshest wins; ties prefer ADS-B, then local. */
    static final class Fusion {

        private Fusion() {}

        static int rank(Observation observation) {
            return positionRank(observation.positionSource()) * 10 + (local(observation) ? 0 : 1);
        }

        static int positionRank(PositionSource source) {
            return switch (source) {
                case REPORTED -> 0;
                case CALCULATED -> 1;
                default -> 2;
            };
        }

        static boolean local(Observation observation) {
            return observation.sourceId().startsWith("local-");
        }
    }
}
