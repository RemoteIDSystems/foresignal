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

    private final SourceRegistry registry;
    private final SourceVisibility visibility;
    private final AppSettingService settings;
    private final ObjectProvider<OutboundUrlPolicy> outbound;

    /** Creates the policy. */
    public EgressPolicy(SourceRegistry registry, SourceVisibility visibility, AppSettingService settings,
            ObjectProvider<OutboundUrlPolicy> outbound) {
        this.registry = registry;
        this.visibility = visibility;
        this.settings = settings;
        this.outbound = outbound;
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
        return descriptor == null || descriptor.egress() == Egress.LOCAL_ONLY;
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
        if (!isPrivate(sourceId)) {
            return true;
        }
        if (path == EgressPath.FEDERATION) {
            return false;
        }
        return viewer != null && viewer.canSeePrivate() && visible(sourceId, viewer);
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
