package ai.foresignal.object;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.ApiTokenRepository;
import ai.foresignal.security.Scope;
import ai.foresignal.security.Ulids;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.stream.Subscription;
import ai.foresignal.stream.sse.EventTopic;
import ai.foresignal.stream.sse.ReplayOutcome;
import ai.foresignal.stream.sse.ReplayRequest;
import ai.foresignal.stream.sse.ReplaySink;
import ai.foresignal.stream.sse.SseHub;
import ai.foresignal.stream.sse.StreamPrincipal;
import ai.foresignal.stream.sse.TopicParams;
import java.time.Clock;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * The {@code workspace} SSE topic on {@code /v1/events/stream} (prompt 129, docs/Client-Contract.md §5):
 * {@code workspace.context} ({@code ContextPatchEvent}) when a member patches the context, and
 * {@code workspace.presence} ({@code PresenceEvent}) when a client joins or leaves. Both are id-less control events:
 * a client that misses one re-reads the workspace (a version gap tells it to). Filter fields: {@code workspaceId}
 * (required), {@code clientId} (generated when absent) and {@code clientKind}. Only the workspace's owner, members
 * (and admins) may follow it; a token is required.
 */
@Component
public class WorkspaceTopic implements EventTopic<WorkspaceFilter> {

    /** The topic name. */
    public static final String NAME = "workspace";

    /** The composite cursor key (the topic carries no id-bearing events). */
    public static final String CURSOR_KEY = "w";

    /** The context event. */
    public static final String CONTEXT_EVENT = "workspace.context";

    /** The presence event. */
    public static final String PRESENCE_EVENT = "workspace.presence";

    private static final Pattern CLIENT_ID = Pattern.compile("^[A-Za-z0-9._:-]{1,128}$");

    private final WorkspaceRepository workspaces;
    private final WorkspaceAccess access;
    private final ApiTokenRepository tokens;
    private final Clock clock;
    private volatile SseHub hub;

    /** Creates the topic. */
    public WorkspaceTopic(WorkspaceRepository workspaces, WorkspaceAccess access, ApiTokenRepository tokens,
            Clock clock) {
        this.workspaces = workspaces;
        this.access = access;
        this.tokens = tokens;
        this.clock = clock;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String cursorKey() {
        return CURSOR_KEY;
    }

    @Override
    public int cursorOrder() {
        return 90;
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public Class<WorkspaceFilter> filterType() {
        return WorkspaceFilter.class;
    }

    @Override
    public WorkspaceFilter parseQuery(TopicParams params, StreamPrincipal principal) {
        return filter(params.single("workspaceId"), params.single("clientId"), params.single("clientKind"),
                principal, params.prefix());
    }

    @Override
    public WorkspaceFilter parseJson(JsonNode block, StreamPrincipal principal) {
        if (block == null || block.isNull() || !block.isObject()) {
            throw ValidationException.forField(NAME, "must be an object with workspaceId");
        }
        for (String field : block.propertyNames()) {
            if (!java.util.Set.of("workspaceId", "clientId", "clientKind").contains(field)) {
                throw ValidationException.forField(NAME + "." + field, "is not a workspace filter field");
            }
        }
        return filter(text(block, "workspaceId"), text(block, "clientId"), text(block, "clientKind"), principal,
                NAME + ".");
    }

    private WorkspaceFilter filter(String workspaceId, String clientId, String clientKind, StreamPrincipal principal,
            String prefix) {
        if (principal.anonymous()) {
            throw new ForesignalException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED,
                    "The workspace topic needs a token.");
        }
        if (workspaceId == null || !Ulids.isUlid(workspaceId)) {
            throw ValidationException.forField(prefix + "workspaceId", "is required (a workspace ULID)");
        }
        String kind = clientKind == null ? "other" : clientKind;
        if (!ObjectTypes.CLIENT_KINDS.contains(kind)) {
            throw ValidationException.forField(prefix + "clientKind", "must be one of " + ObjectTypes.CLIENT_KINDS);
        }
        if (clientId != null && !CLIENT_ID.matcher(clientId).matches()) {
            throw ValidationException.forField(prefix + "clientId", "must be 1 to 128 of A-Z a-z 0-9 . _ : -");
        }
        String principalId = principalOf(principal);
        ObjectCaller caller = new ObjectCaller(principalId, principal.id(), principal.scope(),
                SourceVisibility.canSeePrivate(principal.scope(), principal.privateAccess())
                        && !principal.restriction().excludePrivate(), java.util.Set.of());
        WorkspaceEntity w = workspaces.findById(workspaceId).filter(x -> access.canRead(x, caller))
                .orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_WORKSPACE,
                        "No workspace '" + workspaceId + "' exists, or you are not a member."));
        return new WorkspaceFilter(w.getId(), clientId == null ? "c-" + UUID.randomUUID() : clientId, kind,
                principalId, clock.instant());
    }

    /** The principal behind a stream: the token's principal, or {@code token:<id>} without one. */
    String principalOf(StreamPrincipal principal) {
        String id = principal.id();
        if (id.startsWith("token:")) {
            try {
                UUID tokenId = UUID.fromString(id.substring("token:".length()));
                return tokens.findById(tokenId).map(t -> t.getPrincipalId() != null ? t.getPrincipalId() : id)
                        .orElse(id);
            } catch (IllegalArgumentException e) {
                return id;
            }
        }
        return id;
    }

    @Override
    public long head() {
        return 0;
    }

    @Override
    public ReplayOutcome replay(ReplayRequest request, WorkspaceFilter filter, ReplaySink sink) {
        return new ReplayOutcome.Done(request.after());
    }

    @Override
    public Subscription subscribe(SseHub target) {
        this.hub = target;
        return new Subscription() {
            @Override
            public String name() {
                return "sse-workspace";
            }

            @Override
            public void close() {
                hub = null;
            }
        };
    }

    /**
     * Sends an event to every stream following {@code workspaceId}.
     *
     * @return how many streams got it
     */
    public int publish(String workspaceId, String event, Map<String, Object> payload) {
        SseHub target = hub;
        if (target == null) {
            return 0;
        }
        return target.publishControl(NAME, f -> f instanceof WorkspaceFilter wf && wf.follows(workspaceId), event,
                payload);
    }

    /**
     * The {@code ContextPatchEvent} payload of {@code workspace.context}.
     *
     * @param workspaceId the workspace
     * @param version the new context version
     * @param patch the merge patch applied
     * @param originClientId the sender's client id, or null
     * @param principal the sender's principal, or null
     * @param at when it was applied
     */
    public static Map<String, Object> contextPayload(String workspaceId, long version, Map<String, Object> patch,
            String originClientId, String principal, java.time.Instant at) {
        Map<String, Object> event = new java.util.LinkedHashMap<>();
        event.put("workspaceId", workspaceId);
        event.put("version", version);
        event.put("patch", patch);
        event.put("originClientId", originClientId);
        event.put("principal", principal);
        event.put("at", at);
        return event;
    }

    /**
     * The {@code PresenceEvent} payload of {@code workspace.presence}.
     *
     * @param workspaceId the workspace
     * @param clients the connected clients, oldest first
     * @param change {@code joined} or {@code left}, or null for a snapshot
     * @param clientId the client that joined or left, or null
     * @param at when it was observed
     */
    public static Map<String, Object> presencePayload(String workspaceId, java.util.List<WorkspaceFilter> clients,
            String change, String clientId, java.time.Instant at) {
        Map<String, Object> out = new java.util.LinkedHashMap<>();
        out.put("workspaceId", workspaceId);
        java.util.List<Map<String, Object>> views = new java.util.ArrayList<>();
        for (WorkspaceFilter f : clients) {
            Map<String, Object> v = new java.util.LinkedHashMap<>();
            v.put("clientId", f.clientId());
            v.put("clientKind", f.clientKind());
            v.put("principal", f.principal());
            v.put("connectedAt", f.connectedAt());
            views.add(v);
        }
        out.put("clients", views);
        if (change == null) {
            out.put("change", null);
        } else {
            Map<String, Object> c = new java.util.LinkedHashMap<>();
            c.put("type", change);
            c.put("clientId", clientId);
            out.put("change", c);
        }
        out.put("at", at);
        return out;
    }

    /** The hub, once attached (presence reads its clients). */
    SseHub hub() {
        return hub;
    }

    private static String text(JsonNode block, String field) {
        JsonNode n = block.get(field);
        return n == null || n.isNull() ? null : n.asString();
    }
}
