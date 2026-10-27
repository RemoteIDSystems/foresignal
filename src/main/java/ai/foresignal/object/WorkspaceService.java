package ai.foresignal.object;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.Ulids;
import ai.foresignal.security.quota.QuotaService;
import ai.foresignal.share.ShareLinkService;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * Workspaces and their synchronized context (prompt 129, docs/Client-Contract.md §5–§6; invariant OBJ-1).
 *
 * <p>The context is versioned: a JSON merge patch with {@code If-Match: <contextVersion>} applies only to the current
 * version ({@code 412 WORKSPACE_CONTEXT_STALE} otherwise), is validated against the contract schema, increments the
 * version and is broadcast as {@code workspace.context} with the sender's {@code originClientId}, so clients ignore
 * their own patches. Layout is opaque client JSON (≤ 64 KB). Workspaces the caller neither owns nor belongs to are
 * invisible ({@code 404 UNKNOWN_WORKSPACE}); insufficient roles are {@code 403 WORKSPACE_FORBIDDEN}
 * ({@link WorkspaceAccess}).
 */
@Service
public class WorkspaceService {

    private QuotaService quotas;

    /**
     * Wires the per-user quotas (prompt R2-026).
     *
     * @param quotas the quotas
     */
    @Autowired(required = false)
    public void setQuotas(QuotaService quotas) {
        this.quotas = quotas;
    }

    /** Largest layout, in bytes. */
    public static final int MAX_LAYOUT_BYTES = 65_536;

    /** Most members. */
    public static final int MAX_MEMBERS = 200;

    private static final Set<String> INPUT_FIELDS = Set.of("name", "members", "context", "layout", "savedViewIds",
            "investigationId", "private");
    private static final Set<String> PATCH_FIELDS = Set.of("name", "members", "savedViewIds", "investigationId",
            "private");
    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    private final WorkspaceRepository workspaces;
    private final WorkspaceTopic topic;
    private final ShareLinkService shares;
    private final WorkspaceAccess access;
    private final JsonMapper json;
    private final Clock clock;
    private final TransactionTemplate tx;

    /** Creates the service. */
    public WorkspaceService(WorkspaceRepository workspaces, WorkspaceTopic topic, ShareLinkService shares,
            WorkspaceAccess access, JsonMapper json, Clock clock, PlatformTransactionManager transactions) {
        this.access = access;
        this.workspaces = workspaces;
        this.topic = topic;
        this.shares = shares;
        this.json = json;
        this.clock = clock;
        this.tx = new TransactionTemplate(transactions);
    }

    /**
     * Refuses a write that would take the workspace owner past {@code maxStorageBytes} (prompt R2-026). A workspace is
     * measured as the JSON text of its context and layout, the measure {@code QuotaService} sums.
     */
    private void checkStorage(String owner, long addedBytes) {
        if (quotas != null) {
            quotas.checkStorage(owner, addedBytes);
        }
    }

    private long jsonBytes(Map<String, Object> value) {
        return value == null ? 0 : json.writeValueAsBytes(value).length;
    }

    // ------------------------------------------------------------------------- views

    /** The {@code Workspace} view (layout omitted in lists), with the caller's effective role. */
    public Map<String, Object> view(WorkspaceEntity w, ObjectCaller caller, boolean withLayout) {
        return view(w, access.role(w, caller), withLayout);
    }

    /** The {@code Workspace} view (layout omitted in lists) for a caller holding {@code role}. */
    public static Map<String, Object> view(WorkspaceEntity w, String role, boolean withLayout) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", w.getId());
        out.put("ref", ObjectTypes.WORKSPACE + ":" + w.getId());
        out.put("name", w.getName());
        out.put("owner", w.getOwner());
        out.put("members", w.getMembers());
        out.put("callerRole", role == null ? "viewer" : role);
        out.put("context", w.getContext());
        out.put("contextVersion", w.getContextVersion());
        if (withLayout) {
            out.put("layout", w.getLayout());
        }
        out.put("savedViewIds", w.getSavedViewIds());
        out.put("investigationId", w.getInvestigationId());
        out.put("private", w.isPrivateWorkspace());
        out.put("createdAt", w.getCreatedAt());
        out.put("updatedAt", w.getUpdatedAt());
        return out;
    }

    /** The {@code WorkspaceContextState} view. */
    public static Map<String, Object> contextState(WorkspaceEntity w) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("workspaceId", w.getId());
        out.put("contextVersion", w.getContextVersion());
        out.put("context", w.getContext());
        return out;
    }

    /** The ETag of a workspace: its context version, quoted. */
    public static String etag(WorkspaceEntity w) {
        return "\"" + w.getContextVersion() + "\"";
    }

    // ------------------------------------------------------------------------- reads

    /**
     * The caller's own and member workspaces, oldest first ({@code listWorkspaces}).
     *
     * @return the {@code WorkspacePage} view
     */
    public Map<String, Object> list(ObjectCaller caller, String cursor, int limit) {
        List<WorkspaceEntity> page = workspaces.visibleTo(caller.principal(), cursor == null ? "" : cursor,
                limit + 1);
        boolean more = page.size() > limit;
        List<WorkspaceEntity> rows = more ? page.subList(0, limit) : page;
        List<Map<String, Object>> items = new ArrayList<>();
        for (WorkspaceEntity w : rows) {
            if (access.canRead(w, caller)) {
                items.add(view(w, caller, false));
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", more ? rows.get(rows.size() - 1).getId() : null);
        return out;
    }

    /** The workspace {@code id} if the caller may read it ({@code 404 UNKNOWN_WORKSPACE} otherwise). */
    public WorkspaceEntity get(String id, ObjectCaller caller) {
        return workspaces.findById(id).filter(w -> access.canRead(w, caller))
                .orElseThrow(() -> unknown(id));
    }

    // ------------------------------------------------------------------------- writes

    /**
     * Creates a workspace owned by the caller, at context version 1 ({@code createWorkspace}).
     *
     * @param body the {@code WorkspaceInput}
     * @param caller the caller (a token)
     * @return the new workspace
     */
    public WorkspaceEntity create(JsonNode body, ObjectCaller caller) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The body must be a WorkspaceInput object.");
        }
        for (String field : body.propertyNames()) {
            if (!INPUT_FIELDS.contains(field)) {
                throw ValidationException.forField(field, "is not a WorkspaceInput member");
            }
        }
        String name = name(body.get("name"));
        Instant now = clock.instant();
        WorkspaceEntity w = new WorkspaceEntity(Ulids.next(now), name, caller.principal(), now);
        w.setMembers(members(body.get("members"), caller.principal()));
        JsonNode context = body.get("context");
        if (context != null && !context.isNull()) {
            WorkspaceContextValidator.validate(context, "context");
            w.setContext(json.convertValue(ContextLinks.stripNulls(context), MAP));
        }
        w.setLayout(layout(body.get("layout")));
        w.setSavedViewIds(strings(body.get("savedViewIds"), "savedViewIds"));
        w.setInvestigationId(optionalText(body.get("investigationId"), "investigationId"));
        w.setPrivateWorkspace(body.hasNonNull("private") && body.get("private").asBoolean(false));
        checkStorage(w.getOwner(), jsonBytes(w.getContext()) + jsonBytes(w.getLayout()));
        return tx.execute(s -> workspaces.save(w));
    }

    /** Applies a JSON merge patch to the metadata ({@code updateWorkspace}). */
    public WorkspaceEntity patch(String id, JsonNode body, ObjectCaller caller) {
        return tx.execute(s -> {
            WorkspaceEntity w = get(id, caller);
            if (!access.canEditMetadata(w, caller)) {
                throw forbidden("Changing a workspace needs its owner, or an editor with agent scope.");
            }
            if (body == null || !body.isObject()) {
                throw new ValidationException("The body must be a WorkspacePatch object.");
            }
            for (String field : body.propertyNames()) {
                if (!PATCH_FIELDS.contains(field)) {
                    throw ValidationException.forField(field, "is not a WorkspacePatch member (context and layout"
                            + " have their own operations)");
                }
            }
            if (body.has("name")) {
                w.setName(name(body.get("name")));
            }
            if (body.has("members")) {
                JsonNode patched = JsonMergePatch.apply(json.valueToTree(w.getMembers()), body.get("members"));
                w.setMembers(members(patched, w.getOwner()));
            }
            if (body.has("savedViewIds")) {
                w.setSavedViewIds(strings(body.get("savedViewIds"), "savedViewIds"));
            }
            if (body.has("investigationId")) {
                w.setInvestigationId(optionalText(body.get("investigationId"), "investigationId"));
            }
            if (body.has("private")) {
                w.setPrivateWorkspace(body.get("private").asBoolean(false));
            }
            w.setUpdatedAt(clock.instant());
            return workspaces.save(w);
        });
    }

    /** Deletes a workspace ({@code deleteWorkspace}; owner or admin). */
    public void delete(String id, ObjectCaller caller) {
        tx.executeWithoutResult(s -> {
            WorkspaceEntity w = get(id, caller);
            if (!access.canDelete(w, caller)) {
                throw forbidden("Only the owner or an admin may delete a workspace.");
            }
            workspaces.delete(w);
        });
    }

    /** Replaces the opaque layout ({@code putWorkspaceLayout}; owner or editor). */
    public WorkspaceEntity putLayout(String id, JsonNode body, ObjectCaller caller) {
        return tx.execute(s -> {
            WorkspaceEntity w = get(id, caller);
            if (!access.canEditContext(w, caller)) {
                throw forbidden("Changing the layout needs the owner or an editor.");
            }
            if (body == null || !body.isObject()) {
                throw new ValidationException("The layout must be a JSON object.");
            }
            long before = jsonBytes(w.getLayout());
            w.setLayout(layout(body));
            checkStorage(w.getOwner(), jsonBytes(w.getLayout()) - before);
            w.setUpdatedAt(clock.instant());
            return workspaces.save(w);
        });
    }

    /**
     * Patches the shared context ({@code patchWorkspaceContext}).
     *
     * @param id the workspace
     * @param ifMatch the {@code If-Match} header: the context version the patch was made against
     * @param patch the JSON merge patch
     * @param originClientId the sender's client id ({@code Foresignal-Client-Id}), or null
     * @param caller the caller (owner or editor)
     * @return the new state
     * @throws ForesignalException 412 {@code WORKSPACE_CONTEXT_STALE} on a stale version
     */
    public WorkspaceEntity patchContext(String id, String ifMatch, JsonNode patch, String originClientId,
            ObjectCaller caller) {
        if (ifMatch == null || ifMatch.isBlank()) {
            throw ValidationException.forField("If-Match", "is required: the contextVersion the patch was made"
                    + " against");
        }
        long expected = version(ifMatch);
        return applyContext(id, expected, patch, originClientId, caller);
    }

    /**
     * Points every open client at {@code ref} (MCP {@code set_workspace_focus}): sets {@code focusRef} and adds it to
     * {@code selectedRefs}, at the current version.
     */
    public WorkspaceEntity setFocus(String id, ObjectRef ref, String originClientId, ObjectCaller caller) {
        WorkspaceEntity w = get(id, caller);
        ObjectNode patch = JsonNodeFactory.instance.objectNode();
        patch.put("focusRef", ref.format());
        List<String> selected = new ArrayList<>();
        if (w.getContext().get("selectedRefs") instanceof List<?> existing) {
            existing.forEach(r -> selected.add(String.valueOf(r)));
        }
        if (!selected.contains(ref.format())) {
            selected.add(ref.format());
            while (selected.size() > WorkspaceContextValidator.MAX_SELECTED) {
                selected.remove(0);
            }
            patch.set("selectedRefs", json.valueToTree(selected));
        }
        return applyContext(id, w.getContextVersion(), patch, originClientId, caller);
    }

    private WorkspaceEntity applyContext(String id, long expected, JsonNode patch, String originClientId,
            ObjectCaller caller) {
        if (patch == null || !patch.isObject()) {
            throw new ValidationException("The body must be a JSON merge patch object of the workspace context.");
        }
        WorkspaceEntity saved = tx.execute(s -> {
            WorkspaceEntity w = workspaces.findLocked(id).filter(x -> access.canRead(x, caller))
                    .orElseThrow(() -> unknown(id));
            if (!access.canEditContext(w, caller)) {
                throw forbidden("Changing the context needs the owner or an editor.");
            }
            if (w.getContextVersion() != expected) {
                throw new ForesignalException(HttpStatus.PRECONDITION_FAILED, ErrorCodes.WORKSPACE_CONTEXT_STALE,
                        "The context is at version " + w.getContextVersion() + ", not " + expected
                                + ". Re-read the workspace, re-apply your change and retry.")
                        .withProperty("contextVersion", w.getContextVersion());
            }
            JsonNode merged = JsonMergePatch.apply(json.valueToTree(w.getContext()), patch);
            WorkspaceContextValidator.validate(merged, "context");
            long before = jsonBytes(w.getContext());
            w.setContext(json.convertValue(ContextLinks.stripNulls(merged), MAP));
            checkStorage(w.getOwner(), jsonBytes(w.getContext()) - before);
            w.setContextVersion(w.getContextVersion() + 1);
            w.setUpdatedAt(clock.instant());
            return workspaces.save(w);
        });
        topic.publish(saved.getId(), WorkspaceTopic.CONTEXT_EVENT, WorkspaceTopic.contextPayload(saved.getId(),
                saved.getContextVersion(), json.convertValue(patch, MAP), originClientId, caller.principal(),
                clock.instant()));
        return saved;
    }

    /** The deep link to a workspace's context ({@code getWorkspaceContextLink}). */
    public Map<String, Object> contextLink(String id, ObjectCaller caller) {
        WorkspaceEntity w = get(id, caller);
        return ContextLinks.encode(w.getId(), json.valueToTree(w.getContext()));
    }

    /**
     * Issues a read-only 112 share link to the workspace context ({@code shareWorkspace}; owner or editor, agent
     * scope by the access policy). A private workspace is never shared, and the workspace's TLP label must allow a
     * public link: only {@code CLEAR} does (prompt 130).
     */
    public ShareLinkService.Created share(String id, JsonNode body, ObjectCaller caller,
            ShareLinkService.Caller shareCaller) {
        WorkspaceEntity w = get(id, caller);
        if (!access.canEditContext(w, caller) && !caller.admin()) {
            throw forbidden("Sharing a workspace needs its owner or an editor.");
        }
        java.util.Optional<String> label = access.tlp(w);
        if (label.isPresent()) {
            // A 112 share link is public: the workspace's TLP label must allow it (prompt 130, COL-2).
            ai.foresignal.security.TlpTarget.Decision decision = ai.foresignal.security.EgressPolicy.tlp(
                    ai.foresignal.security.Tlp.fromWire(label.get()), ai.foresignal.security.TlpTarget.publicLink());
            if (!decision.allowed()) {
                throw new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.TLP_VIOLATION,
                        "The workspace's TLP label forbids a public share link.").withProperty("rule",
                                decision.rule());
            }
        }
        Instant expiresAt = null;
        Integer maxUses = null;
        if (body != null && body.isObject()) {
            if (body.hasNonNull("expiresAt")) {
                try {
                    expiresAt = Instant.parse(body.get("expiresAt").asString());
                } catch (RuntimeException e) {
                    throw ValidationException.forField("expiresAt", "must be an RFC 3339 date-time");
                }
            }
            if (body.hasNonNull("maxUses")) {
                if (!body.get("maxUses").isIntegralNumber()) {
                    throw ValidationException.forField("maxUses", "must be an integer");
                }
                maxUses = body.get("maxUses").asInt();
            }
        }
        return shares.create(new ShareLinkService.Create(WorkspaceShareTarget.TYPE, w.getId(), null, null, expiresAt,
                maxUses), shareCaller);
    }

    // ------------------------------------------------------------------------- helpers

    /** Parses an {@code If-Match} value ({@code 42}, {@code "42"} or {@code W/"42"}). */
    static long version(String ifMatch) {
        String v = ifMatch.strip();
        if (v.startsWith("W/")) {
            v = v.substring(2);
        }
        v = v.replace("\"", "").strip();
        try {
            return Long.parseLong(v);
        } catch (NumberFormatException e) {
            throw ValidationException.forField("If-Match", "must be the contextVersion number");
        }
    }

    private static NotFoundException unknown(String id) {
        return new NotFoundException(ErrorCodes.UNKNOWN_WORKSPACE,
                "No workspace '" + id + "' exists, or you are not a member.");
    }

    private static ForesignalException forbidden(String detail) {
        return new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.WORKSPACE_FORBIDDEN, detail);
    }

    private static String name(JsonNode node) {
        if (node == null || !node.isString() || node.asString().isBlank() || node.asString().length() > 200) {
            throw ValidationException.forField("name", "must be 1 to 200 characters");
        }
        return node.asString().strip();
    }

    private static Map<String, String> members(JsonNode node, String owner) {
        Map<String, String> out = new LinkedHashMap<>();
        if (node == null || node.isNull()) {
            return out;
        }
        if (!node.isObject()) {
            throw ValidationException.forField("members", "must be an object of principal id → viewer | editor");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String principal : node.propertyNames()) {
            String role = node.get(principal).isString() ? node.get(principal).asString() : null;
            if (principal.isBlank() || principal.length() > 200) {
                errors.add(new FieldError("members", "principal ids are 1 to 200 characters"));
            } else if (!"viewer".equals(role) && !"editor".equals(role)) {
                errors.add(new FieldError("members." + principal, "must be viewer or editor"));
            } else if (!principal.equals(owner)) {
                out.put(principal, role);
            }
        }
        if (out.size() > MAX_MEMBERS) {
            errors.add(new FieldError("members", "at most " + MAX_MEMBERS + " members"));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The members are not valid.", errors);
        }
        return out;
    }

    private Map<String, Object> layout(JsonNode node) {
        if (node == null || node.isNull()) {
            return new LinkedHashMap<>();
        }
        if (!node.isObject()) {
            throw ValidationException.forField("layout", "must be a JSON object");
        }
        int bytes = node.toString().getBytes(StandardCharsets.UTF_8).length;
        if (bytes > MAX_LAYOUT_BYTES) {
            throw new ForesignalException(HttpStatus.CONTENT_TOO_LARGE, ErrorCodes.PAYLOAD_TOO_LARGE,
                    "The layout is " + bytes + " bytes; at most " + MAX_LAYOUT_BYTES + " are stored.");
        }
        return json.convertValue(node, MAP);
    }

    private static List<String> strings(JsonNode node, String field) {
        List<String> out = new ArrayList<>();
        if (node == null || node.isNull()) {
            return out;
        }
        if (!node.isArray() || node.size() > 100) {
            throw ValidationException.forField(field, "must be an array of at most 100 strings");
        }
        for (JsonNode n : node) {
            if (!n.isString() || n.asString().isBlank() || n.asString().length() > 200) {
                throw ValidationException.forField(field, "each entry is a string of 1 to 200 characters");
            }
            if (!out.contains(n.asString())) {
                out.add(n.asString());
            }
        }
        return out;
    }

    private static String optionalText(JsonNode node, String field) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isString() || node.asString().length() > 64) {
            throw ValidationException.forField(field, "must be a string of at most 64 characters or null");
        }
        return node.asString().isBlank() ? null : node.asString();
    }
}
