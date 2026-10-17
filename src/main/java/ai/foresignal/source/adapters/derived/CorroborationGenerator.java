package ai.foresignal.source.adapters.derived;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.LayerStatus;
import ai.foresignal.evidence.EvidenceStore;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * {@code derived-corroboration} (prompt 099; produced from 116 canonical events since prompt 116): one record per
 * canonical event of the earthquake, fire, conflict and outage families, so a single event reported by several
 * feeds is one corroborated record. The matching (099's tolerances: earthquakes Δt ≤ 60 s, ≤ 100 km,
 * |ΔM| ≤ 0.8; fires ≤ 1 km, ≤ 1 h; conflict events same 0.1° cell and UTC day; outages same country or ASN,
 * overlapping) now lives in the {@code EvidenceResolver}, and independence is counted in evidence groups:
 * re-reports of one originator never raise credibility (EVD-1).
 *
 * <p>The kind and the id form are 099's: kind {@code corroboration}, {@code externalId}
 * {@code {family}:{clusterId}}, the cluster id now being the canonical event id. The payload keeps 099's
 * {@code members}, {@code independentSources} (one source per evidence group), {@code credibility} (the
 * Admiralty grade 109 reads: 2 "probably true" for ≥ 2 independent evidence groups, 3 "possibly true" for one)
 * and {@code reliability} ({@code F}), and adds {@code canonicalEventId}, {@code evidenceGroups} (the count),
 * {@code independentOriginators} (the group roots) and {@code qualityFlags}. A merged canonical event's record is
 * tombstoned. Members from sources {@link EgressPolicy} does not serve are left out.
 */
@Component
public class CorroborationGenerator extends LayerGenerator {

    /** The source id. */
    public static final String ID = "derived-corroboration";

    /** The record kind. */
    public static final String KIND = "corroboration";

    /** How far back canonical-event changes are picked up each tick. */
    static final Duration WINDOW = Duration.ofHours(24);

    /** One 099 family: its id in record ids, the canonical family and the sources it reads. */
    enum Family {

        EARTHQUAKE("earthquake", "seismic", Set.of("usgs-earthquakes", "emsc-seismic", "geofon", "ingv", "gdacs"),
                Set.of("earthquake")),
        FIRE("fire", "fire", Set.of("nasa-firms", "goes-fdc", "hms-fire"), Set.of("fire-detection")),
        CONFLICT("conflict", "conflict", Set.of("gdelt-events", "acled", "ucdp-ged"), Set.of("conflict-event")),
        OUTAGE("outage", "outage", Set.of("ioda-outages", "cloudflare-radar-outages"), Set.of("outage",
                "internet-outage"));

        final String id;
        final String canonical;
        final Set<String> sources;
        final Set<String> kinds;

        Family(String id, String canonical, Set<String> sources, Set<String> kinds) {
            this.id = id;
            this.canonical = canonical;
            this.sources = sources;
            this.kinds = kinds;
        }

        /** The 099 family of a canonical family, or null. */
        static Family ofCanonical(String canonical) {
            for (Family family : values()) {
                if (family.canonical.equals(canonical)) {
                    return family;
                }
            }
            return null;
        }
    }

    private final SourceDescriptor descriptor = layer(ID, "Event corroboration (derived)",
            "Events reported by several independent sources, one record per canonical event, with credibility from"
                    + " independent evidence groups. Automated, not a warning.",
            "derived (input licences)", KIND).build();

    private final ObjectProvider<EvidenceStore> evidence;
    private final ObjectProvider<EgressPolicy> egress;

    /** Creates the generator. */
    public CorroborationGenerator(ObjectProvider<SourceRegistry> registry, ObjectProvider<EvidenceStore> evidence,
            ObjectProvider<EgressPolicy> egress) {
        super(registry);
        this.evidence = evidence;
        this.egress = egress;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    protected Set<String> inputPrefixes() {
        return Set.of();
    }

    @Override
    protected Set<String> explicitInputSources() {
        Set<String> all = new TreeSet<>();
        for (Family family : Family.values()) {
            all.addAll(family.sources);
        }
        return all;
    }

    @Override
    protected Set<String> inputKinds() {
        Set<String> all = new TreeSet<>();
        for (Family family : Family.values()) {
            all.addAll(family.kinds);
        }
        return all;
    }

    @Override
    protected Duration tick() {
        return Duration.ofMinutes(5);
    }

    @Override
    protected void clearState() {
        // Stateless: records are rebuilt from the canonical events changed in the window each tick.
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        EvidenceStore store = evidence.getIfAvailable();
        if (store == null) {
            return;
        }
        List<String> families = new ArrayList<>();
        for (Family family : Family.values()) {
            families.add(family.canonical);
        }
        Predicate<String> served = servedPredicate();
        for (EvidenceStore.StoredEvent event : store.updatedSince(families, now.minus(WINDOW), maxEntities(ctx.params()))) {
            Family family = Family.ofCanonical(event.family());
            if (family == null) {
                continue;
            }
            String id = family.id + ":" + event.id();
            if ("merged".equals(event.status())) {
                ctx.tombstone(KIND, id);
                continue;
            }
            Map<String, Object> payload = payload(event, store.links(event.id()), store.groups(event.id()), served);
            if (payload == null) {
                continue;
            }
            NormalizedRecord.Builder builder = NormalizedRecord.builder(id, KIND)
                    .observedAt(event.firstObservedAt() == null ? now : event.firstObservedAt());
            if (event.lon() != null && event.lat() != null) {
                builder.geometry(Geo.point(event.lon(), event.lat()));
            }
            ctx.emit(builder.putAll(payload).build());
        }
    }

    private Predicate<String> servedPredicate() {
        EgressPolicy policy = egress.getIfAvailable();
        if (policy == null) {
            return source -> true;
        }
        return source -> policy.decide(EgressPath.API_READ, source, null, null, EgressPolicy.EgressContext.NONE)
                .allowed();
    }

    /**
     * The record payload of a canonical event; null when no member may be served.
     *
     * @param served true for a source whose records may appear in the payload
     */
    static Map<String, Object> payload(EvidenceStore.StoredEvent event, List<EvidenceStore.StoredLink> links,
            List<EvidenceStore.StoredGroup> groups, Predicate<String> served) {
        List<Map<String, Object>> members = new ArrayList<>();
        Map<String, String> sourceOfGroup = new LinkedHashMap<>();
        for (EvidenceStore.StoredLink link : links) {
            if (!served.test(link.facts().sourceId())) {
                continue;
            }
            Map<String, Object> member = new LinkedHashMap<>();
            member.put("sourceId", link.facts().sourceId());
            member.put("externalId", link.facts().externalId());
            member.put("observedAt", link.facts().observedAt() == null ? null : link.facts().observedAt().toString());
            members.add(member);
            if (link.evidenceGroupId() != null) {
                sourceOfGroup.putIfAbsent(link.evidenceGroupId(), link.facts().sourceId());
            }
        }
        if (members.isEmpty()) {
            return null;
        }
        Set<String> roots = new LinkedHashSet<>();
        for (EvidenceStore.StoredGroup group : groups) {
            roots.add(group.originatorRoot());
        }
        int groupCount = Math.max(1, groups.size());
        Map<String, Object> payload = LayerPayload.base(ID, LayerStatus.OK, 0.5, List.of());
        payload.put("members", members);
        payload.put("independentSources", new ArrayList<>(new LinkedHashSet<>(sourceOfGroup.values())));
        payload.put("credibility", groupCount >= 2 ? 2 : 3);
        payload.put("reliability", "F");
        payload.put("canonicalEventId", event.id());
        payload.put("evidenceGroups", groupCount);
        payload.put("independentOriginators", new ArrayList<>(roots));
        payload.put("qualityFlags", event.qualityFlags());
        return payload;
    }
}
