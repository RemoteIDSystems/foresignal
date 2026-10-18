package ai.foresignal.security;

import ai.foresignal.llm.LlmSettings;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.provenance.LicenceEgressGuard;
import ai.foresignal.provenance.LicenceTerms;
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
 *   <li><em>Later paths (109):</em> {@link #decide(EgressPath, String, LicenceTerms, Map, EgressContext)} (and
 *       its {@code allows} overloads) for exports, archive, sinks, federation, share links and feeds. A private
 *       record is excluded on every one of them except {@code ARCHIVE}, with two explicit, conditional
 *       exceptions from later prompts: {@code EXPORT} for an admin with {@code includePrivate=true} (111) and the
 *       sink paths with {@code includePrivate=true} on a local target (110). {@code FEED}, {@code SHARE},
 *       {@code PUBLIC_SHARE} and {@code FEDERATION} never carry a private record.
 * </ul>
 *
 * <p><strong>License (prompt 109).</strong> The license table ({@link LicenceEgressGuard}, an internal delegate
 * of this class) is applied on every path, {@code EXPORT} and {@code ARCHIVE} included, to the record's own
 * terms when the caller resolved them, else to {@link #sourceTerms(String)}.
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
  *
 * <p><strong>TLP (prompt 130, COL-2).</strong> A work product's {@link Tlp} label bounds who may receive it outside
 * its access list: {@link #tlp(Tlp, TlpTarget)} decides per share target ({@link TlpTarget}). {@code RED} reaches
 * only named principals (never groups, the instance, links or peers; named recipients on other instances only);
 * {@code AMBER+STRICT} named principals and groups or the instance only when everyone reached shares the owner's
 * organization, and remote recipients of that organization; {@code AMBER} as {@code AMBER+STRICT} plus named
 * recipients of other organizations on other instances; {@code GREEN} the instance and federated peers but no public
 * link; {@code CLEAR} everything.
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
    static final Set<EgressPath> NON_LOCAL_PATHS = java.util.EnumSet.of(EgressPath.SINK, EgressPath.SINK_PUSH,
            EgressPath.FEDERATION, EgressPath.SHARE, EgressPath.PUBLIC_SHARE, EgressPath.FEED);

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
     * The paths a {@code LOCAL_ONLY} source may take (prompts 062, 109): this instance's own serving surface
     * (API reads, tiles, SSE, MCP, LLM context, notifications), an export to this instance's own API client and
     * the operator-owned cold archive (111). Sinks, federation, share links and feeds (feed keys) re-serve data
     * to another party and are refused; a LAN-serving sink is allowed only on a loopback or private bind
     * ({@link EgressContext#localTarget()}), the 110 rule for {@code LOCAL_ONLY} records.
     */
    static final Set<EgressPath> LOCAL_ONLY_PATHS = java.util.EnumSet.of(EgressPath.API_READ, EgressPath.TILE,
            EgressPath.SSE, EgressPath.MCP, EgressPath.LLM_LOCAL, EgressPath.LLM_REMOTE, EgressPath.NOTIFICATION,
            EgressPath.EXPORT, EgressPath.ARCHIVE);

    /**
     * The paths on which a viewer allowed to see private sources ({@link Viewer#canSeePrivate()}) receives a
     * private record (prompt 109, task 4: "excluded on every path except {@code API_READ}/{@code TILE}/
     * {@code SSE}/{@code MCP} for permitted tokens, and {@code ARCHIVE}").
     */
    static final Set<EgressPath> PRIVATE_VIEWER_PATHS =
            java.util.EnumSet.of(EgressPath.API_READ, EgressPath.TILE, EgressPath.SSE, EgressPath.MCP);

    /** The sink paths (110), where a private record needs {@code includePrivate} and a local target. */
    static final Set<EgressPath> SINK_PATHS =
            java.util.EnumSet.of(EgressPath.SINK, EgressPath.SINK_SERVE_LAN, EgressPath.SINK_PUSH);

    /**
     * True when data marked {@code LOCAL_ONLY} that is not a record (prompt 133: media ratings of paid or
     * non-commercial providers) may be served on {@code path}: the same paths a {@code LOCAL_ONLY} source's records may
     * take ({@link #LOCAL_ONLY_PATHS}: this instance's own API, MCP, SSE, tiles, notifications, LLM context, exports and
     * archives), never share links, feeds, sinks or federation.
     */
    public boolean allowsLocalOnlyData(EgressPath path) {
        return path != null && LOCAL_ONLY_PATHS.contains(path);
    }

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
        return decide(path, sourceId, null, payload, EgressContext.of(viewer)).allowed();
    }

    /**
     * {@link #allows(EgressPath, String, Map, Viewer)} for a stored row on a path with no viewer, such as a
     * sink or a federated peer: the row's payload JSON is read for {@code posSource}; a row whose payload
     * cannot be read is refused on a non-local path when its source declares {@code posSource} (fail closed).
     */
    public boolean allows(RecordRow row, EgressPath path) {
        return decide(row, path, null, EgressContext.NONE).allowed();
    }

    /**
     * {@link #decide(EgressPath, String, LicenceTerms, Map, EgressContext)} for a stored row: the row's payload
     * JSON is read for {@code posSource}; a row whose payload cannot be read is treated as MLAT when its source
     * declares {@code posSource} (fail closed).
     *
     * @param recordTerms the row's resolved license ({@code LicenceResolver}), or null for the source's terms
     */
    public Decision decide(RecordRow row, EgressPath path, LicenceTerms recordTerms, EgressContext context) {
        Objects.requireNonNull(row, "row");
        Boolean mlat = mlatOf(row, true);
        boolean treatAsMlat = mlat == null ? reportsPosSource(row.sourceId()) : mlat;
        Map<String, Object> payload = treatAsMlat ? Map.of(POS_SOURCE, MLAT) : null;
        return decide(path, row.sourceId(), recordTerms, payload, context);
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
     * source never leaves (there is no viewer allowed to see it and no sink opt-in).
     */
    public boolean allows(String sourceId, EgressPath path) {
        return allows(path, sourceId, null);
    }

    /**
     * The source-level hook: may a record of {@code sourceId} leave on {@code path} for {@code viewer}, with no
     * per-record facts and no path options ({@link EgressContext#of(Viewer)})? See
     * {@link #decide(EgressPath, String, LicenceTerms, Map, EgressContext)} for the rules. A re-serving path that
     * has the record must call a record-level overload instead, so the MLAT rule applies.
     */
    public boolean allows(EgressPath path, String sourceId, Viewer viewer) {
        return decide(path, sourceId, null, null, EgressContext.of(viewer)).allowed();
    }

    /**
     * The per-call facts the egress decision needs beyond the record (prompts 109, 110, 111).
     *
     * @param viewer who receives the record, or null for a path with no viewer (a sink, a peer)
     * @param includePrivate the caller opted private sources in: a sink's {@code filter.includePrivate} (110) or
     *     an export's {@code includePrivate} (111)
     * @param localTarget true when the destination stays on this operator's network: a SERVE sink bound to
     *     loopback or a private address, or a PUSH target that resolves to a private address (110)
     * @param personalUse the operator exports for personal use (111): {@code LOCAL_ONLY} and {@code FORBIDDEN}
     *     licenses may then leave on {@code EXPORT}
     * @param sameOperator the federation destination is the operator's own hub (113, see {@link #federation})
     */
    public record EgressContext(Viewer viewer, boolean includePrivate, boolean localTarget, boolean personalUse,
            boolean sameOperator) {

        /** No viewer, no opt-ins. */
        public static final EgressContext NONE = new EgressContext(null, false, false, false, false);

        /** A context without the 113 same-operator exception. */
        public EgressContext(Viewer viewer, boolean includePrivate, boolean localTarget, boolean personalUse) {
            this(viewer, includePrivate, localTarget, personalUse, false);
        }

        /**
         * A federation context (113): records served to a peer, or pushed by an edge relay to its hub.
         *
         * @param sameOperator the peer is the operator's own hub ({@code sameOperator=true} on the peer or the
         *     {@code federation-push} sink): {@code LOCAL_ONLY} sources and licenses may then leave on
         *     {@code FEDERATION}, the one documented exception; private records, MLAT positions and
         *     {@code FORBIDDEN} licenses never do
         */
        public static EgressContext federation(boolean sameOperator) {
            return new EgressContext(null, false, false, false, sameOperator);
        }

        /** A viewer with no opt-ins (the source-level and read paths). */
        public static EgressContext of(Viewer viewer) {
            return new EgressContext(viewer, false, false, false);
        }

        /**
         * A sink's context (110).
         *
         * @param includePrivate the sink filter's {@code includePrivate}
         * @param localTarget the sink binds loopback/private, or its PUSH target resolves private
         */
        public static EgressContext sink(boolean includePrivate, boolean localTarget) {
            return new EgressContext(null, includePrivate, localTarget, false);
        }

        /**
         * An export's context (111).
         *
         * @param viewer the principal that requested the export
         * @param includePrivate the export spec's {@code includePrivate}
         * @param personalUse the export spec's {@code personalUse}
         */
        public static EgressContext export(Viewer viewer, boolean includePrivate, boolean personalUse) {
            return new EgressContext(viewer, includePrivate, false, personalUse);
        }
    }

    /**
     * The outcome of {@link #decide(EgressPath, String, LicenceTerms, Map, EgressContext)}.
     *
     * @param allowed true when the record may leave
     * @param reason why it may not ({@link #REASON_UNREGISTERED}, {@link #REASON_PRIVATE},
     *     {@link #REASON_LOCAL_ONLY}, {@link #REASON_MLAT}, {@link #REASON_LICENCE_FORBIDDEN},
     *     {@link #REASON_LICENCE_LOCAL_ONLY}), null when allowed
     * @param terms the license terms the decision used (after the MLAT downgrade); null when the decision was
     *     made before the license step or the license components are not wired
     */
    public record Decision(boolean allowed, String reason, LicenceTerms terms) {

        static Decision deny(String reason, LicenceTerms terms) {
            return new Decision(false, reason, terms);
        }
    }

    /** The source is not registered (fail closed). */
    public static final String REASON_UNREGISTERED = "unregistered";

    /** The source is private and the path or caller may not carry private records. */
    public static final String REASON_PRIVATE = "private";

    /** The source's effective descriptor egress is {@code LOCAL_ONLY} (062, EGR-2). */
    public static final String REASON_LOCAL_ONLY = "local-only";

    /** The record's position is MLAT (069) and the path re-serves it. */
    public static final String REASON_MLAT = "mlat";

    /** The record's license forbids redistribution on this path (109). */
    public static final String REASON_LICENCE_FORBIDDEN = "licence-forbidden";

    /** The record's license is {@code LOCAL_ONLY} (or {@code UNKNOWN}) and the path leaves the instance (109). */
    public static final String REASON_LICENCE_LOCAL_ONLY = "licence-local-only";

    /**
     * The single egress decision (prompts 039, 062, 069, 109, 110, 111): may a record of {@code sourceId} leave
     * on {@code path}? Every path that takes records off the instance goes through here, directly or through an
     * {@code allows} overload; {@link LicenceEgressGuard} is this method's internal license table and is not an
     * entry point of its own ({@code docs/Conventions.md}: one {@code EgressPolicy}). In order:
     *
     * <ol>
     *   <li><em>Registration:</em> an unregistered source never leaves (fail closed).
     *   <li><em>Private sources (039, 109):</em> a private record is excluded on every path except
     *       {@code API_READ}, {@code TILE}, {@code SSE} and {@code MCP} for a viewer that may see it, and
     *       {@code ARCHIVE} (operator-owned storage), regardless of license. Two later prompts make explicit,
     *       conditional exceptions, implemented here and nowhere else:
     *       <ul>
     *         <li>{@code EXPORT} (111: "Private sources need admin plus {@code includePrivate=true}"): an admin
     *             viewer with {@link EgressContext#includePrivate()}.
     *         <li>{@code SINK}, {@code SINK_SERVE_LAN}, {@code SINK_PUSH} (110: "excluded unless
     *             {@code filter.includePrivate=true} and the sink is SERVE on loopback or a PUSH target that
     *             resolves to a private address"): {@link EgressContext#includePrivate()} and
     *             {@link EgressContext#localTarget()}.
     *       </ul>
     *       {@code FEED}, {@code SHARE}, {@code PUBLIC_SHARE}, {@code FEDERATION}, {@code NOTIFICATION} and the
     *       LLM paths never carry a private record through this method; LLM context and notification payloads
     *       keep their dedicated 026/039 methods ({@link #filter(Collection, LlmDestination)},
     *       {@link #allowsNotificationPayload}).
     *   <li><em>Descriptor egress (062, 069):</em> a source whose effective egress is {@code LOCAL_ONLY} leaves only
     *       on {@link #LOCAL_ONLY_PATHS}, or on {@code SINK_SERVE_LAN} to a local target.
     *   <li><em>MLAT (069, 109):</em> a record whose {@code posSource} is {@code mlat} never leaves on
     *       {@link #NON_LOCAL_PATHS} nor on a non-local {@code SINK_SERVE_LAN}, and its license is downgraded to
     *       {@code LOCAL_ONLY} with its attribution kept ({@link LicenceTerms#downgradedToLocalOnly()}).
     *   <li><em>License (109):</em> the record's terms ({@code recordTerms}, else {@link #sourceTerms(String)},
     *       which follows {@code derivedFrom}) go through the license table with
     *       {@link EgressContext#personalUse()} and {@link EgressContext#localTarget()}, on every path including
     *       {@code EXPORT} and {@code ARCHIVE} (the table allows {@code ARCHIVE} for every license, 111).
     *   <li><em>Same-operator federation (113):</em> on {@code FEDERATION} with
     *       {@link EgressContext#sameOperator()} (an edge relay pushing to its operator's own hub), a
     *       {@code LOCAL_ONLY} source and a {@code LOCAL_ONLY} or {@code UNKNOWN} license may leave; private
     *       records, MLAT positions and {@code FORBIDDEN} licenses still never do. This is the one documented
     *       exception; the hub then treats such records as {@code LOCAL_ONLY} again.
     * </ol>
     *
     * @param recordTerms the record's resolved license ({@code LicenceResolver}: batch license or peer origin),
     *     or null to use the source's terms
     * @param payload the record's payload, or null when unknown (then only the source rules apply)
     * @param context the viewer and path options; null means {@link EgressContext#NONE}
     */
    public Decision decide(EgressPath path, String sourceId, LicenceTerms recordTerms, Map<String, ?> payload,
            EgressContext context) {
        Objects.requireNonNull(path, "path");
        EgressContext ctx = context == null ? EgressContext.NONE : context;
        if (sourceId == null || !registry.contains(sourceId)) {
            return Decision.deny(REASON_UNREGISTERED, null);
        }
        if (isPrivate(sourceId) && !privateAllowed(path, sourceId, ctx)) {
            return Decision.deny(REASON_PRIVATE, null);
        }
        boolean sameOperatorFederation = path == EgressPath.FEDERATION && ctx.sameOperator();
        if (isLocalOnly(sourceId) && !LOCAL_ONLY_PATHS.contains(path)
                && !(path == EgressPath.SINK_SERVE_LAN && ctx.localTarget()) && !sameOperatorFederation) {
            return Decision.deny(REASON_LOCAL_ONLY, null);
        }
        boolean mlat = isMlat(payload);
        if (mlat && (NON_LOCAL_PATHS.contains(path) || (path == EgressPath.SINK_SERVE_LAN && !ctx.localTarget()))) {
            return Decision.deny(REASON_MLAT, null);
        }
        if (licenceGuard == null || sourceLicences == null) {
            return new Decision(true, null, null);
        }
        LicenceTerms terms = recordTerms != null ? recordTerms : sourceTerms(sourceId);
        if (mlat) {
            terms = terms.downgradedToLocalOnly();
        }
        if (sameOperatorFederation && !terms.forbidsRedistribution()) {
            // The 113 exception: an edge relay pushing to its operator's own hub; the hub keeps them LOCAL_ONLY.
            return new Decision(true, null, terms);
        }
        if (licenceGuard.allows(path, terms, ctx.personalUse(), ctx.localTarget())) {
            return new Decision(true, null, terms);
        }
        return Decision.deny(terms.forbidsRedistribution() ? REASON_LICENCE_FORBIDDEN : REASON_LICENCE_LOCAL_ONLY,
                terms);
    }

    /**
     * The license terms a record of {@code sourceId} carries when nothing per record says otherwise
     * ({@code SourceLicences}, which follows {@code derivedFrom}). The operator's {@code OPEN} override of an
     * {@code egressOverridable} source (069) lifts a {@code LOCAL_ONLY} license default
     * ({@link LicenceTerms#openedByOperator()}; {@code docs/Conventions.md} "Operator choice": license egress
     * flags are defaults the operator can override per source); {@code FORBIDDEN} terms are never lifted. The
     * restrictive {@code unknown} terms when the license components are not wired.
     */
    public LicenceTerms sourceTerms(String sourceId) {
        if (sourceLicences == null) {
            return LicenceTerms.unknown();
        }
        LicenceTerms terms = sourceLicences.termsOf(sourceId);
        SourceDescriptor descriptor = sourceId == null || !registry.contains(sourceId) ? null
                : registry.descriptor(sourceId);
        if (descriptor != null && descriptor.egressOverridable() && egressOf(descriptor) == Egress.OPEN) {
            terms = terms.openedByOperator();
        }
        return terms;
    }

    private boolean privateAllowed(EgressPath path, String sourceId, EgressContext ctx) {
        if (path == EgressPath.ARCHIVE) {
            return true;
        }
        if (PRIVATE_VIEWER_PATHS.contains(path)) {
            return ctx.viewer() != null && ctx.viewer().canSeePrivate() && visible(sourceId, ctx.viewer());
        }
        if (path == EgressPath.EXPORT) {
            return ctx.includePrivate() && ctx.viewer() != null && ctx.viewer().isAdmin();
        }
        if (SINK_PATHS.contains(path)) {
            return ctx.includePrivate() && ctx.localTarget();
        }
        return false;
    }

    private LicenceEgressGuard licenceGuard;
    private ai.foresignal.provenance.SourceLicences sourceLicences;

    /**
     * Wires the license table and the source-terms resolver (prompt 109). Required in the application context;
     * the four- and five-argument constructors leave them unset so unit tests of the private and
     * {@code LOCAL_ONLY} rules need no catalogue (the license step is then skipped).
     */
    @org.springframework.beans.factory.annotation.Autowired
    public void setLicenceGuard(LicenceEgressGuard licenceGuard, ai.foresignal.provenance.SourceLicences sourceLicences) {
        this.licenceGuard = licenceGuard;
        this.sourceLicences = sourceLicences;
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

    // ------------------------------------------------------------------------- TLP (prompt 130)

    /**
     * Whether a work product labeled {@code tlp} may go to {@code target} (prompt 130, COL-2).
     *
     * @param tlp the object's label (null counts as {@link Tlp#AMBER}, the default)
     * @param target where it would go
     * @return the decision and the rule that made it
     */
    public static TlpTarget.Decision tlp(Tlp tlp, TlpTarget target) {
        Tlp label = tlp == null ? Tlp.AMBER : tlp;
        String name = label.wireValue();
        return switch (target.kind()) {
            case PRINCIPAL -> allow(name + ": a principal named in the access list may receive it");
            case GROUP -> switch (label) {
                case RED -> refuse("RED: only principals named in the access list, never a group");
                case AMBER_STRICT, AMBER -> target.sameOrganization()
                        ? allow(name + ": every member of the group belongs to the owner's organization")
                        : refuse(name + ": a group with members outside the owner's organization");
                default -> allow(name + ": groups of this instance may receive it");
            };
            case INSTANCE -> switch (label) {
                case RED -> refuse("RED: never instance visibility");
                case AMBER_STRICT, AMBER -> target.sameOrganization()
                        ? allow(name + ": every principal of this instance belongs to the owner's organization")
                        : refuse(name + ": this instance has principals outside the owner's organization");
                default -> allow(name + ": the whole instance may receive it");
            };
            case PUBLIC_LINK -> label == Tlp.CLEAR
                    ? allow("CLEAR: public share links are allowed")
                    : refuse(name + ": public share links need CLEAR");
            case FEDERATED_PEER -> label == Tlp.CLEAR || label == Tlp.GREEN
                    ? allow(name + ": federated peers may receive it")
                    : refuse(name + ": federated peers need GREEN or CLEAR");
            case REMOTE_RECIPIENT -> switch (label) {
                case RED -> target.named()
                        ? allow("RED: a named recipient on another instance")
                        : refuse("RED: only named recipients on other instances");
                case AMBER_STRICT -> target.named() && target.sameOrganization()
                        ? allow("AMBER+STRICT: a named recipient of the owner's organization on another instance")
                        : refuse("AMBER+STRICT: recipients on other instances must be named and of the owner's"
                                + " organization");
                case AMBER -> target.named()
                        ? allow("AMBER: a named recipient and their organization on another instance")
                        : refuse("AMBER: recipients on other instances must be named");
                default -> allow(name + ": recipients on other instances may receive it");
            };
        };
    }

    private static TlpTarget.Decision allow(String rule) {
        return new TlpTarget.Decision(true, rule);
    }

    private static TlpTarget.Decision refuse(String rule) {
        return new TlpTarget.Decision(false, rule);
    }
}
