package ai.foresignal.security;

import ai.foresignal.llm.LlmSettings;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.security.visibility.PrivateFilter;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.security.visibility.Viewer;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.RecordRow;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * The one component that decides whether a record may leave Foresignal on a given path
 * ({@code docs/Conventions.md}, "Shared components"). Prompts 026 (LLM context), 039 (private sources
 * everywhere, EGR-1), 062 ({@code LOCAL_ONLY} egress) and 109 (licence policy) each extend this class;
 * no parallel policy class may exist. Prompt 039 folded the 023 private gate
 * ({@code SourceVisibilityGate}) into it, on top of {@link SourceVisibility}.
 *
 * <p><strong>Private sources (prompt 039).</strong> Whether a source is private is
 * {@link SourceVisibility#isPrivate} (descriptor flag or operator tightening). Per path:
 *
 * <ul>
 *   <li><em>API reads, tiles, SSE, MCP, alerts, stats, glance, metrics, inbox:</em> the caller's
 *       {@link #hiddenSources(Viewer)} / {@link #privateFilter(Viewer)} are excluded from every query;
 *       a direct read of a hidden record is {@code 404}. Only admin, and read/agent tokens, feed keys
 *       and tickets with the private grant, see private sources.
 *   <li><em>Search and embeddings:</em> {@link #allowsEmbedding(String)} is false for a private source,
 *       so it is never embedded; text search excludes hidden sources per caller.
 *   <li><em>LLM context (026, hardened by 039):</em> a private record is placed in a prompt only for a
 *       provider classified as local ({@link #isLocal}: Ollama or {@code options.local}, whose base URL
 *       is a loopback or private address under 035's {@code LAN_ONLY} profile) and only when
 *       {@code llm.allowPrivateLocal} is true (default false); never for a remote provider. A record
 *       whose source is not registered is dropped (fail closed).
 *   <li><em>Notifications:</em> a payload of a private-source alert carries only the rule name,
 *       severity and a link unless the channel has {@code privatePayload: true}
 *       ({@link #allowsNotificationPayload}); Web Push, APNs and FCM payloads never carry it
 *       ({@link #allowsPushPayload}).
 *   <li><em>Later paths:</em> {@link #allows(EgressPath, String, Viewer)} for exports, sinks,
 *       federation, share links and feeds.
 * </ul>
 *
 * <p><strong>{@code LOCAL_ONLY} sources (prompt 062, EGR-2).</strong> A descriptor with
 * {@code egress: LOCAL_ONLY} ({@code opensky-states}, whose terms forbid redistribution) is served to this
 * instance's own API clients, tiles, SSE, MCP and notifications as usual, but never re-served:
 * {@link #allows(EgressPath, String, Viewer)} refuses it on {@code SINK}, {@code FEDERATION},
 * {@code SHARE} and {@code FEED}, for every viewer.
 *
 * <ul>
 *   <li>Prompts 109 (operator override), 110, 112 and 113 call the hook.
 * </ul>
 *
 * <p><strong>Local receivers and MLAT (prompt 069).</strong>
 *
 * <ul>
 *   <li><em>Effective egress:</em> a descriptor that is {@code egressOverridable} (the {@code local-*},
 *       {@code station-*} and {@code wxstation-*} families, {@code LOCAL_ONLY} by default) may be set to
 *       {@code OPEN} or back in {@code source_settings.egress}; {@link #isLocalOnly} reads the effective value,
 *       cached until the source's settings change. Any other descriptor's {@code egress} is fixed.
 *   <li><em>MLAT is never redistributable</em>, even from a source set to {@code OPEN}:
 *       {@link #allows(EgressPath, String, Map, Viewer)} and {@link #allows(RecordRow, EgressPath)} refuse a
 *       record whose payload {@code posSource} is {@code mlat} on every non-local path ({@code SINK},
 *       {@code FEDERATION}, {@code SHARE}, {@code FEED}: sinks, federation, share links and public feed keys).
 *       MLAT positions are computed by aggregator networks under terms that forbid passing them on.
 *       <strong>Contract for 109, 110 and 113:</strong> a path that re-serves records calls the record-level
 *       overload with the record's payload, never only the source-level {@link #allows(EgressPath, String,
 *       Viewer)}, so this rule cannot be bypassed.
 *   <li><em>Anonymous reads:</em> while {@code security.exposeMlat} is false (the default),
 *       {@link #withoutMlatForAnonymous} strips MLAT records from responses to anonymous {@code openReads}
 *       callers; a record whose {@code posSource} cannot be read (the payload was projected away) from a
 *       source that declares {@code posSource} is stripped too (fail closed).
 * </ul>
 */
@Component
public class EgressPolicy {

    /**
     * An LLM provider as an egress destination.
     *
     * @param providerId the provider id
     * @param type the provider type wire value ({@code anthropic}, {@code openai-compatible}, {@code ollama})
     * @param local true when the provider is declared local (Ollama, or {@code options.local})
     * @param baseUrl the provider's base URL, or null when unknown; a declared-local provider is
     *     classified local only when this resolves to a loopback or private address (prompt 039)
     */
    public record LlmDestination(String providerId, String type, boolean local, String baseUrl) {

        /** Validates the members. */
        public LlmDestination {
            Objects.requireNonNull(providerId, "providerId");
            Objects.requireNonNull(type, "type");
        }

        /** A destination without a base URL (its declared locality decides). */
        public LlmDestination(String providerId, String type, boolean local) {
            this(providerId, type, local, null);
        }
    }

    /** The payload key naming how an aircraft position was obtained (prompt 062). */
    public static final String POS_SOURCE = "posSource";

    /** The {@code posSource} value of a multilateration position. */
    public static final String MLAT = "mlat";

    /**
     * The paths that take a record off this instance to another party (prompt 069): an MLAT position never
     * leaves on them, whatever the source's egress.
     */
    static final Set<EgressPath> NON_LOCAL_PATHS =
            java.util.EnumSet.of(EgressPath.SINK, EgressPath.FEDERATION, EgressPath.SHARE, EgressPath.FEED);

    private static final tools.jackson.databind.json.JsonMapper JSON =
            tools.jackson.databind.json.JsonMapper.builder().build();

    private final SourceRegistry registry;
    private final SourceVisibility visibility;
    private final AppSettingService settings;
    private final ObjectProvider<OutboundUrlPolicy> outbound;
    private final ObjectProvider<ai.foresignal.source.SourceService> sources;

    /** Effective egress per overridable source, dropped when the source's settings change (prompt 069). */
    private final java.util.concurrent.ConcurrentHashMap<String, Egress> effectiveEgress =
            new java.util.concurrent.ConcurrentHashMap<>();

    /** Creates the policy without egress overrides: every descriptor's {@code egress} is fixed (tests). */
    public EgressPolicy(SourceRegistry registry, SourceVisibility visibility, AppSettingService settings,
            ObjectProvider<OutboundUrlPolicy> outbound) {
        this(registry, visibility, settings, outbound, null);
    }

    /**
     * Creates the policy.
     *
     * @param sources reads the egress overrides of {@code egressOverridable} sources (prompt 069), resolved
     *     lazily; null for none
     */
    @org.springframework.beans.factory.annotation.Autowired
    public EgressPolicy(SourceRegistry registry, SourceVisibility visibility, AppSettingService settings,
            ObjectProvider<OutboundUrlPolicy> outbound, ObjectProvider<ai.foresignal.source.SourceService> sources) {
        this.registry = registry;
        this.visibility = visibility;
        this.settings = settings;
        this.outbound = outbound;
        this.sources = sources;
    }

    /** Drops the cached effective egress of a source whose settings changed (prompt 069). */
    @org.springframework.context.event.EventListener
    public void onSourceSettingsChanged(ai.foresignal.source.SourceSettingsChangedEvent event) {
        if (event != null && event.sourceId() != null) {
            effectiveEgress.remove(event.sourceId());
        }
    }

    // ------------------------------------------------------------------------- private sources

    /** True when {@code sourceId} is private (descriptor flag or operator tightening). */
    public boolean isPrivate(String sourceId) {
        return visibility.isPrivate(sourceId);
    }

    /** The private sources {@code viewer} may not see (empty for admin and holders of the private grant). */
    public Set<String> hiddenSources(Viewer viewer) {
        return visibility.hiddenFor(viewer.scope(), viewer.privateAccess(), viewer.restrictions());
    }

    /** {@link #hiddenSources(Viewer)} as the {@link PrivateFilter} query builders apply. */
    public PrivateFilter privateFilter(Viewer viewer) {
        return new PrivateFilter(hiddenSources(viewer));
    }

    /** True when records of {@code sourceId} may be shown to {@code viewer} on a read path. */
    public boolean visible(String sourceId, Viewer viewer) {
        return !hiddenSources(viewer).contains(sourceId);
    }

    // ------------------------------------------------------------------------- LOCAL_ONLY egress

    /**
     * The later paths a {@code LOCAL_ONLY} source may still take (prompt 062): an export to this
     * instance's own API client. Sinks, federation, share links and feeds (feed keys) re-serve data to
     * another party and are refused. Off-box cold-archive exports are 111's to refuse when it builds them.
     */
    static final Set<EgressPath> LOCAL_ONLY_PATHS = java.util.EnumSet.of(EgressPath.EXPORT);

    /**
     * True when {@code sourceId}'s descriptor declares {@code egress: LOCAL_ONLY} (prompt 062, EGR-2): its
     * records are served to this instance's own API clients and notifications, never re-served. An
     * unregistered source counts as local-only (fail closed). Prompt 109 adds the operator override.
     */
    public boolean isLocalOnly(String sourceId) {
        if (sourceId == null || !registry.contains(sourceId)) {
            return true;
        }
        SourceDescriptor descriptor = registry.descriptor(sourceId);
        return descriptor == null || egressOf(descriptor) == Egress.LOCAL_ONLY;
    }

    /**
     * The egress in force for {@code descriptor} (prompt 069): the operator's override of an
     * {@code egressOverridable} descriptor, else its {@code egress}. A failed lookup keeps the descriptor's
     * value, which for every overridable family is {@code LOCAL_ONLY} (fail closed).
     */
    Egress egressOf(SourceDescriptor descriptor) {
        if (!descriptor.egressOverridable() || sources == null) {
            return descriptor.egress();
        }
        Egress cached = effectiveEgress.get(descriptor.id());
        if (cached != null) {
            return cached;
        }
        ai.foresignal.source.SourceService service = sources.getIfAvailable();
        if (service == null) {
            return descriptor.egress();
        }
        try {
            Egress value = service.effectiveEgress(descriptor.id());
            effectiveEgress.put(descriptor.id(), value);
            return value;
        } catch (RuntimeException e) {
            return descriptor.egress();
        }
    }

    // ------------------------------------------------------------------------- MLAT (prompt 069)

    /**
     * The record-level hook (prompt 069): {@link #allows(EgressPath, String, Viewer)}, and in addition a
     * record whose {@code posSource} is {@code mlat} never leaves on a non-local path ({@code SINK},
     * {@code FEDERATION}, {@code SHARE}, {@code FEED}), even when its source is {@code OPEN}. Re-serving paths
     * (109, 110, 113) must call this overload.
     *
     * @param payload the record's payload, or null when unknown (then only the source rules apply)
     */
    public boolean allows(EgressPath path, String sourceId, Map<String, Object> payload, Viewer viewer) {
        if (!allows(path, sourceId, viewer)) {
            return false;
        }
        return !(NON_LOCAL_PATHS.contains(path) && isMlat(payload));
    }

    /**
     * {@link #allows(EgressPath, String, Map, Viewer)} for a stored row on a path with no viewer, such as a
     * sink or a federated peer: the row's payload JSON is read for {@code posSource}; a row whose payload
     * cannot be read is refused on a non-local path when its source declares {@code posSource} (fail closed).
     */
    public boolean allows(RecordRow row, EgressPath path) {
        Objects.requireNonNull(row, "row");
        if (!allows(path, row.sourceId(), null)) {
            return false;
        }
        if (!NON_LOCAL_PATHS.contains(path)) {
            return true;
        }
        Boolean mlat = mlatOf(row, true);
        return mlat == null ? !reportsPosSource(row.sourceId()) : !mlat;
    }

    /** True when {@code payload} says its position is MLAT. */
    public static boolean isMlat(Map<String, ?> payload) {
        return payload != null && MLAT.equals(payload.get(POS_SOURCE));
    }

    /**
     * Whether a stored row's payload says MLAT: true or false when {@code posSource} could be read (an absent
     * key reads as false when the payload is whole), null when the payload is missing or projected without it.
     *
     * @param payloadWhole true when {@link RecordRow#payload()} is the whole payload, not a projection
     */
    static Boolean mlatOf(RecordRow row, boolean payloadWhole) {
        String whole = row.titlePayload() != null ? row.titlePayload() : row.payload();
        if (whole == null || whole.isBlank()) {
            return null;
        }
        try {
            tools.jackson.databind.JsonNode node = JSON.readTree(whole);
            if (node == null || !node.isObject()) {
                return null;
            }
            tools.jackson.databind.JsonNode pos = node.get(POS_SOURCE);
            if (pos == null) {
                // A projected payload that left posSource out cannot say; a whole one without it is not MLAT.
                return payloadWhole || row.titlePayload() != null ? Boolean.FALSE : null;
            }
            return pos.isString() && MLAT.equals(pos.stringValue());
        } catch (tools.jackson.core.JacksonException e) {
            return null;
        }
    }

    /** True when {@code sourceId}'s descriptor declares a {@code posSource} field (an aircraft source). */
    boolean reportsPosSource(String sourceId) {
        if (sourceId == null || !registry.contains(sourceId)) {
            return true;
        }
        SourceDescriptor descriptor = registry.descriptor(sourceId);
        return descriptor == null || descriptor.fieldSpecs().containsKey(POS_SOURCE);
    }

    /** The {@code security.exposeMlat} setting (prompt 069, default false). */
    public boolean exposeMlat() {
        return Boolean.TRUE.equals(settings.getOrDefault(ai.foresignal.settings.SettingKeys.SECURITY,
                ai.foresignal.settings.SecuritySettings.class,
                ai.foresignal.settings.SecuritySettings::defaults).exposeMlat());
    }

    /**
     * The rows an anonymous {@code openReads} caller may receive (prompt 069): every row when the caller has a
     * token or {@code security.exposeMlat} is on; otherwise the rows whose {@code posSource} is not
     * {@code mlat}. A row whose {@code posSource} cannot be read from a source that declares it is left out
     * too (fail closed). Order is kept.
     *
     * @param anonymous true when no token, feed key or ticket was presented
     * @param payloadWhole true when the rows carry their whole payload (no {@code fields=} projection)
     */
    public List<RecordRow> withoutMlatForAnonymous(List<RecordRow> rows, boolean anonymous, boolean payloadWhole) {
        if (!anonymous || rows.isEmpty() || exposeMlat()) {
            return rows;
        }
        List<RecordRow> out = new ArrayList<>(rows.size());
        for (RecordRow row : rows) {
            if (allowsAnonymous(row, payloadWhole)) {
                out.add(row);
            }
        }
        return out.size() == rows.size() ? rows : out;
    }

    /**
     * True when {@code row} may be served to an anonymous caller while {@code security.exposeMlat} is off;
     * {@link #withoutMlatForAnonymous} applies it row by row, and streaming paths call it directly.
     */
    public boolean allowsAnonymous(RecordRow row, boolean payloadWhole) {
        Boolean mlat = mlatOf(row, payloadWhole);
        return mlat == null ? !reportsPosSource(row.sourceId()) : !mlat;
    }

    /**
     * {@link #allows(EgressPath, String, Viewer)} for a path with no viewer, such as a sink or a federated
     * peer (prompt 062): a {@code LOCAL_ONLY} source never leaves on a re-serving path, and a private
     * source never leaves without a viewer allowed to see it.
     */
    public boolean allows(String sourceId, EgressPath path) {
        return allows(path, sourceId, null);
    }

    /**
     * The later-path hook (prompt 039, §2): may a record of {@code sourceId} leave on {@code path} for
     * {@code viewer}? A {@code LOCAL_ONLY} source (062) leaves only on {@link #LOCAL_ONLY_PATHS}. Otherwise a
     * source that is not private always may (109 adds the licence rules here). A
     * private source may leave on {@code EXPORT}, {@code SHARE}, {@code FEED} and {@code SINK} only for a
     * viewer that can see private sources, and never on {@code FEDERATION} (a peer instance is a remote
     * party). An unregistered source never leaves (fail closed).
     */
    public boolean allows(EgressPath path, String sourceId, Viewer viewer) {
        Objects.requireNonNull(path, "path");
        if (sourceId == null || !registry.contains(sourceId)) {
            return false;
        }
        if (isLocalOnly(sourceId) && !LOCAL_ONLY_PATHS.contains(path)) {
            return false;
        }
        boolean privateOk;
        if (!isPrivate(sourceId)) {
            privateOk = true;
        } else if (path == EgressPath.FEDERATION) {
            privateOk = false;
        } else {
            privateOk = viewer != null && viewer.canSeePrivate() && visible(sourceId, viewer);
        }
        return privateOk && licenceAllows(path, sourceId);
    }

    /**
     * The licence rule (prompt 109): a re-serving path is refused when the source's structured licence
     * terms forbid it ({@link LicenceEgressGuard}). The guard is resolved lazily so the constructor
     * stays usable in tests.
     */
    private boolean licenceAllows(EgressPath path, String sourceId) {
        if (licenceGuard == null || licenceCatalog == null || !LICENCE_GUARDED.contains(path)) {
            return true;
        }
        SourceDescriptor descriptor = registry.descriptor(sourceId);
        return licenceGuard.allows(path, licenceCatalog.forDescriptor(descriptor));
    }

    /** The re-serving paths the licence guard governs (prompt 109). */
    private static final Set<EgressPath> LICENCE_GUARDED = java.util.EnumSet.of(EgressPath.SINK,
            EgressPath.SINK_SERVE_LAN, EgressPath.SINK_PUSH, EgressPath.PUBLIC_SHARE, EgressPath.FEDERATION,
            EgressPath.SHARE, EgressPath.FEED);

    private ai.foresignal.provenance.LicenceEgressGuard licenceGuard;
    private ai.foresignal.provenance.LicenceCatalog licenceCatalog;

    /** Wires the licence guard and catalogue (prompt 109); resolved lazily. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setLicenceGuard(ai.foresignal.provenance.LicenceEgressGuard licenceGuard,
            ai.foresignal.provenance.LicenceCatalog licenceCatalog) {
        this.licenceGuard = licenceGuard;
        this.licenceCatalog = licenceCatalog;
    }

    /**
     * True when a notification about a record of {@code sourceId} may carry the record's payload
     * (snapshot, title, message): always for a public source; for a private one only when the channel
     * opted in with {@code privatePayload: true} (an admin setting per channel).
     */
    public boolean allowsNotificationPayload(String sourceId, boolean channelPrivatePayload) {
        return sourceId == null || !isPrivate(sourceId) || channelPrivatePayload;
    }

    /**
     * True when a Web Push, APNs or FCM payload may carry the record's payload: never for a private
     * source (those transports cross third-party push services).
     */
    public boolean allowsPushPayload(String sourceId) {
        return sourceId == null || !isPrivate(sourceId);
    }

    // ------------------------------------------------------------------------- LLM context

    /**
     * The records of {@code records} that may be sent to {@code destination} as LLM context, in their
     * original order.
     */
    public List<RecordRow> filter(Collection<RecordRow> records, LlmDestination destination) {
        return filter(records, RecordRow::sourceId, destination);
    }

    /**
     * The items of {@code records} that may be sent to {@code destination} as LLM context, in their
     * original order; {@code sourceIdOf} names each item's source.
     */
    public <T> List<T> filter(Collection<T> records, Function<? super T, String> sourceIdOf,
            LlmDestination destination) {
        Objects.requireNonNull(destination, "destination");
        boolean privateAllowed = privateAllowedFor(destination);
        List<T> out = new ArrayList<>(records.size());
        for (T record : records) {
            String sourceId = sourceIdOf.apply(record);
            if (sourceId != null && registry.contains(sourceId) && (privateAllowed || !isPrivate(sourceId))) {
                out.add(record);
            }
        }
        return out;
    }

    /** True when records of {@code sourceId} may be sent to {@code destination} as LLM context. */
    public boolean allowsLlmContext(String sourceId, LlmDestination destination) {
        if (sourceId == null || !registry.contains(sourceId)) {
            return false;
        }
        return !isPrivate(sourceId) || privateAllowedFor(destination);
    }

    /**
     * The registered sources whose records may not be sent to {@code destination} as LLM context: every
     * private source unless the destination is local and {@code llm.allowPrivateLocal} is on (prompt
     * 028, 039). Ask, briefings and enrichment add these to the sources their reads exclude, so withheld
     * records are never even read for the model; output is then checked item by item with
     * {@link #allowsLlmContext}.
     */
    public Set<String> llmExcludedSources(LlmDestination destination) {
        Objects.requireNonNull(destination, "destination");
        if (privateAllowedFor(destination)) {
            return Set.of();
        }
        return visibility.privateSourceIds();
    }

    /**
     * True when private records may go to {@code destination}: it is classified local
     * ({@link #isLocal}) and {@code llm.allowPrivateLocal} is true.
     */
    public boolean privateAllowedFor(LlmDestination destination) {
        Objects.requireNonNull(destination, "destination");
        return allowPrivateLocal() && isLocal(destination);
    }

    /**
     * True when prompts to {@code destination} stay on this machine or network: the provider is declared
     * local (Ollama or {@code options.local}) and, when its base URL is known, the URL's host resolves
     * only to loopback or private addresses (035's {@code LAN_ONLY} profile). Anything else, including an
     * unresolvable host, is remote (fail closed).
     */
    public boolean isLocal(LlmDestination destination) {
        if (!destination.local()) {
            return false;
        }
        if (destination.baseUrl() == null || destination.baseUrl().isBlank()) {
            return true;
        }
        OutboundUrlPolicy policy = outbound == null ? null : outbound.getIfAvailable();
        if (policy == null) {
            return false;
        }
        return policy.check(destination.baseUrl(), OutboundUrlPolicy.Profile.LAN_ONLY, true).allowed();
    }

    /** The {@code llm.allowPrivateLocal} setting (default false). */
    public boolean allowPrivateLocal() {
        return settings.getOrDefault(SettingKeys.LLM, LlmSettings.class, () -> LlmSettings.DEFAULTS).allowPrivateLocal();
    }

    // ------------------------------------------------------------------------- search and embeddings

    /**
     * True when records of {@code sourceId} may be embedded into {@code embedding_document} (the search
     * path, prompt 027): the source is registered and not private. The {@code embed} setting is forced
     * off for private sources, and their existing rows are deleted when a source becomes private (039).
     */
    public boolean allowsEmbedding(String sourceId) {
        return sourceId != null && registry.contains(sourceId) && !isPrivate(sourceId);
    }
}
