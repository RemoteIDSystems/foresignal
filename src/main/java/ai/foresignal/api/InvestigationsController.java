package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.collab.AccessAction;
import ai.foresignal.collab.AccessResolver;
import ai.foresignal.collab.AclRole;
import ai.foresignal.collab.CollabInputs;
import ai.foresignal.collab.PrincipalViews;
import ai.foresignal.collab.ShareService;
import ai.foresignal.collab.ShareableObjectEntity;
import ai.foresignal.collab.Visibility;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.intel.InvestigationEntity;
import ai.foresignal.intel.InvestigationItemEntity;
import ai.foresignal.intel.InvestigationService;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectService;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.Tlp;
import ai.foresignal.security.restrict.RestrictedRead;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Investigations (prompt 101, extended by 130): case files, items with snapshots, and JSON/Markdown/GeoJSON export.
 * Items built from private sources are added, listed, removed and exported only for callers with private access
 * ({@link IntelVisibility}, EGR-1).
 *
 * <p>Prompt 130 puts investigations under access lists ({@link AccessResolver}): an investigation the caller may not
 * see is {@code 404}; editing and adding items need the {@code editor} role, deleting the {@code owner}. New
 * investigations are owned by the caller's principal and are {@code instance}-visible unless the input says
 * otherwise (as investigations were before 130). Display ids ({@code INV-yyyy-nnnn}) work in every path. Items accept
 * evidence items, attachments and any 129 object reference; an item with a stricter TLP label raises the
 * investigation's label (COL-2), and exports print the label.
 */
@RestController
@Tag(name = "Intelligence")
@RequestMapping("/v1/investigations")
public class InvestigationsController {

    private final InvestigationService investigations;
    private final IntelVisibility visibility;
    private final ObjectCallers callers;
    private final AccessResolver access;
    private final ShareService shares;
    private final ObjectService objects;
    private final PrincipalViews views;
    private final JsonMapper json;

    /** Creates the controller. */
    public InvestigationsController(InvestigationService investigations, IntelVisibility visibility,
            ObjectCallers callers, AccessResolver access, ShareService shares, ObjectService objects,
            PrincipalViews views, JsonMapper json) {
        this.investigations = investigations;
        this.visibility = visibility;
        this.callers = callers;
        this.access = access;
        this.shares = shares;
        this.objects = objects;
        this.views = views;
        this.json = json;
    }

    private static String actor(HttpServletRequest request) {
        return CurrentToken.of(request).map(t -> "token:" + t.name()).orElse("operator");
    }

    private static String ref(String id) {
        return ObjectTypes.INVESTIGATION + ":" + id;
    }

    /** The investigation when the caller may perform {@code action}; invisible ones are 404. */
    private InvestigationEntity require(String id, ObjectCaller caller, AccessAction action) {
        InvestigationEntity entity = investigations.get(id);
        AclRole role = access.role(caller, ref(entity.getId()));
        if (role == null) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_INVESTIGATION, "No such investigation: " + id);
        }
        access.require(caller, ref(entity.getId()), action);
        return entity;
    }

    /** {@code listInvestigations}: what the caller can see, by {@code scope} (mine, shared, all) and status. */
    @GetMapping
    @RestrictedRead
    @Operation(operationId = "listInvestigations", tags = "Intelligence")
    public Map<String, Object> listInvestigations(@RequestParam(required = false) String status,
            @RequestParam(required = false) String scope, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        if (status != null && !InvestigationService.STATUSES.contains(status)) {
            throw ValidationException.forField("status", "must be one of " + InvestigationService.STATUSES);
        }
        String which = QueryParams.oneOf("scope", scope, List.of("mine", "shared", "all"), "all");
        int max = QueryParams.intInRange("limit", limit, 1, 500, 100);
        List<InvestigationEntity> all = new ArrayList<>(investigations.list(status));
        all.sort(Comparator.comparing(InvestigationEntity::getUpdatedAt).reversed()
                .thenComparing(InvestigationEntity::getId));
        Map<String, AclRole> roles = access.roles(caller, all.stream().map(i -> ref(i.getId())).toList());
        List<InvestigationEntity> visible = new ArrayList<>();
        for (InvestigationEntity i : all) {
            AclRole role = roles.get(ref(i.getId()));
            if (role == null) {
                continue;
            }
            String owner = access.record(ref(i.getId())).map(ShareableObjectEntity::getOwner).orElse(null);
            boolean mine = caller.principal() != null && caller.principal().equals(owner);
            if (("mine".equals(which) && !mine) || ("shared".equals(which) && mine)) {
                continue;
            }
            visible.add(i);
        }
        int offset = CollabInputs.offset(cursor);
        List<Map<String, Object>> items = new ArrayList<>();
        for (InvestigationEntity i : visible.subList(Math.min(offset, visible.size()), Math.min(offset + max,
                visible.size()))) {
            items.add(view(i, false));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", offset + max < visible.size() ? CollabInputs.cursor(offset + max) : null);
        return out;
    }

    /** {@code getInvestigation}: by id or display id. */
    @GetMapping("/{id}")
    @RestrictedRead
    @Operation(operationId = "getInvestigation", tags = "Intelligence")
    public Map<String, Object> getInvestigation(@PathVariable String id, HttpServletRequest request) {
        InvestigationEntity entity = require(id, callers.authenticated(request), AccessAction.VIEW);
        Map<String, Object> out = new LinkedHashMap<>(view(entity, true));
        out.put("items", investigations.items(entity.getId(), visibility.seesPrivate(request)).stream()
                .map(InvestigationsController::item).toList());
        return out;
    }

    /** {@code createInvestigation}: owned by the caller; {@code visibility} (default instance) and {@code tlp}. */
    @PostMapping
    @Operation(operationId = "createInvestigation", tags = "Intelligence")
    @ApiResponse(responseCode = "201", description = "Created")
    public ResponseEntity<Map<String, Object>> createInvestigation(@RequestBody JsonNode body,
            HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be an InvestigationInput object.");
        }
        Visibility vis = Visibility.parse("visibility", CollabInputs.text(body, "visibility", 1, 16),
                Visibility.INSTANCE);
        Tlp tlp = Tlp.parse("tlp", CollabInputs.text(body, "tlp", 1, 16), access.defaultTlp());
        shares.checkTlp(tlp, caller.principal(), List.of(), vis);
        InvestigationEntity entity = investigations.create(body, actor(request));
        access.register(ref(entity.getId()), caller.principal(), vis, tlp);
        return ResponseEntity.created(URI.create("/v1/investigations/" + entity.getId())).body(view(entity, true));
    }

    /** {@code updateInvestigation}: title, status, summary, tags and links (editor). */
    @PatchMapping("/{id}")
    @Operation(operationId = "updateInvestigation", tags = "Intelligence")
    public Map<String, Object> updateInvestigation(@PathVariable String id, @RequestBody JsonNode body,
            HttpServletRequest request) {
        InvestigationEntity entity = require(id, callers.authenticated(request), AccessAction.EDIT);
        return view(investigations.patch(entity.getId(), body), true);
    }

    /** {@code deleteInvestigation}: owner only. */
    @DeleteMapping("/{id}")
    @Operation(operationId = "deleteInvestigation", tags = "Intelligence")
    @ApiResponse(responseCode = "204", description = "Deleted")
    public ResponseEntity<Void> deleteInvestigation(@PathVariable String id, HttpServletRequest request) {
        InvestigationEntity entity = require(id, callers.authenticated(request), AccessAction.DELETE);
        investigations.delete(entity.getId());
        access.forget(ref(entity.getId()));
        return ResponseEntity.noContent().build();
    }

    /** {@code addInvestigationItem}: 201 with the item (editor). */
    @PostMapping("/{id}/items")
    @Operation(operationId = "addInvestigationItem", tags = "Intelligence")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> addInvestigationItem(@PathVariable String id, @RequestBody JsonNode body,
            HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        InvestigationEntity entity = require(id, caller, AccessAction.EDIT);
        String itemType = body == null || !body.hasNonNull("itemType") ? null : body.get("itemType").asString();
        if (itemType != null && InvestigationService.OBJECT_ITEM_TYPES.contains(itemType)) {
            return item(addObject(entity, itemType, body, caller, actor(request)));
        }
        return item(investigations.addItem(entity.getId(), body, actor(request), visibility.seesPrivate(request)));
    }

    private InvestigationItemEntity addObject(InvestigationEntity entity, String itemType, JsonNode body,
            ObjectCaller caller, String actor) {
        String raw = body.hasNonNull("ref") ? body.get("ref").asString() : null;
        if (raw == null) {
            throw ValidationException.forField("ref", "is required");
        }
        String text = switch (itemType) {
            case "evidence-item" -> raw.startsWith("evidence-item:") ? raw : "evidence-item:" + raw;
            case "attachment" -> raw.startsWith("attachment:") ? raw : "attachment:" + raw;
            default -> raw;
        };
        ObjectRef ref = ObjectRef.parse(text, "ref");
        ObjectEnvelope env = objects.find(ref, new ResolveContext(caller, null))
                .orElseThrow(() -> ValidationException.forField("ref", "no such object: " + text));
        String note = body.hasNonNull("note") ? body.get("note").asString() : null;
        @SuppressWarnings("unchecked")
        Map<String, Object> snapshot = json.convertValue(env, Map.class);
        InvestigationItemEntity item = investigations.addObjectItem(entity.getId(), itemType,
                env.objectRef().withoutSeq().format(), note, snapshot, env.privateObject(), actor);
        if (env.tlp() != null) {
            ShareableObjectEntity rec = access.record(ref(entity.getId())).orElseThrow();
            Tlp current = Tlp.fromWire(rec.getTlp());
            Tlp input = Tlp.fromWire(env.tlp());
            if (input.stricterThan(current)) {
                // A case file holding stricter material carries the stricter label (COL-2); its grants must allow it.
                shares.checkTlp(input, rec.getOwner(), shares.entries(rec.getObjectRef(), PrincipalViews.cache())
                        .stream().map(e -> String.valueOf(e.get("grantee"))).toList(), rec.getVisibility());
                rec.setTlp(input.wireValue());
                access.save(rec);
            }
        }
        return item;
    }

    /** {@code removeInvestigationItem} (editor). */
    @DeleteMapping("/{id}/items/{itemId}")
    @Operation(operationId = "removeInvestigationItem", tags = "Intelligence")
    @ApiResponse(responseCode = "204", description = "Removed")
    public ResponseEntity<Void> removeInvestigationItem(@PathVariable String id, @PathVariable long itemId,
            HttpServletRequest request) {
        InvestigationEntity entity = require(id, callers.authenticated(request), AccessAction.EDIT);
        investigations.removeItem(entity.getId(), itemId, visibility.seesPrivate(request));
        return ResponseEntity.noContent().build();
    }

    /** {@code exportInvestigation}: with the TLP label printed at the top. */
    @GetMapping(value = "/{id}/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @RestrictedRead
    @Operation(operationId = "exportInvestigation", tags = "Intelligence")
    public ResponseEntity<String> exportInvestigation(@PathVariable String id,
            @RequestParam(required = false, defaultValue = "json") String format, HttpServletRequest request) {
        InvestigationEntity entity = require(id, callers.authenticated(request), AccessAction.VIEW);
        String tlp = access.tlpOf(ref(entity.getId())).wireValue();
        String body = investigations.export(entity.getId(), format, visibility.seesPrivate(request), tlp);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(InvestigationService.contentType(format)))
                .header("Foresignal-TLP", tlp).body(body);
    }

    private Map<String, Object> view(InvestigationEntity entity, boolean withMembers) {
        ShareableObjectEntity rec = access.record(ref(entity.getId())).orElse(null);
        Map<String, Map<String, Object>> cache = PrincipalViews.cache();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", entity.getId());
        out.put("ref", ref(entity.getId()));
        out.put("displayId", entity.getDisplayId());
        out.put("title", entity.getTitle());
        out.put("status", entity.getStatus());
        out.put("summary", entity.getSummary());
        out.put("tags", entity.getTags());
        out.put("createdBy", entity.getCreatedBy());
        out.put("createdAt", entity.getCreatedAt());
        out.put("updatedAt", entity.getUpdatedAt());
        if (rec != null) {
            out.put("owner", views.summary(rec.getOwner(), cache));
            out.put("visibility", rec.getVisibility().wireValue());
            out.put("tlp", rec.getTlp());
            if (withMembers) {
                out.put("members", shares.entries(rec.getObjectRef(), cache));
            }
        }
        out.put("workspaceId", entity.getWorkspaceId());
        out.put("hypothesisSetIds", entity.getHypothesisSetIds());
        out.put("questionIds", entity.getQuestionIds());
        out.put("chronicleIds", entity.getChronicleIds());
        out.put("reportIds", entity.getReportIds());
        return out;
    }

    private static Map<String, Object> item(InvestigationItemEntity entity) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", entity.getId());
        out.put("itemType", entity.getItemType());
        out.put("ref", entity.getRef());
        out.put("seq", entity.getSeq());
        out.put("note", entity.getNote());
        out.put("snapshot", entity.getSnapshot());
        out.put("addedAt", entity.getAddedAt());
        out.put("addedBy", entity.getAddedBy());
        if (InvestigationService.OBJECT_ITEM_TYPES.contains(entity.getItemType())) {
            out.put("objectRef", entity.getRef());
        }
        return out;
    }
}
