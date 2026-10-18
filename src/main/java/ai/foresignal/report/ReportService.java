package ai.foresignal.report;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collab.AccessAction;
import ai.foresignal.collab.AccessResolver;
import ai.foresignal.collab.AclRole;
import ai.foresignal.collab.CollabInputs;
import ai.foresignal.collab.ObjectAclEntity;
import ai.foresignal.collab.ObjectAclRepository;
import ai.foresignal.collab.PrincipalAdminService;
import ai.foresignal.collab.PrincipalViews;
import ai.foresignal.collab.ShareService;
import ai.foresignal.collab.ShareableObjectEntity;
import ai.foresignal.collab.Visibility;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.Tlp;
import ai.foresignal.security.TlpTarget;
import ai.foresignal.security.Ulids;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;

/**
 * Reports as work products (prompt 132): create, read, change and delete them and their blocks.
 *
 * <ul>
 *   <li><b>Ownership.</b> A report is owned by its creator's principal and registered with 130's
 *       {@link AccessResolver} ({@code private} by default); its other authors get the {@code editor} role. A caller
 *       without a role sees {@code 404 UNKNOWN_REPORT}; editing needs {@code editor}, deleting {@code owner}.</li>
 *   <li><b>TLP.</b> A report's label is at least the strictest label of everything it cites and of its source
 *       (COL-2): it is raised on every change, and a request to lower it below that is {@code 403 TLP_VIOLATION}.</li>
 *   <li><b>Optimistic lock.</b> Every change of the draft bumps {@code version}; a change that names a stale version
 *       (body {@code version} or {@code If-Match}) is {@code 409 REPORT_VERSION_CONFLICT}, as is a block change while
 *       the report is not a draft (set {@code status} back to {@code draft} to prepare the next revision).</li>
 * </ul>
 */
@Service
public class ReportService {

    /** The advisory-lock key that serializes display-id allocation. */
    static final long DISPLAY_ID_LOCK = 0x52505431L;

    /** The members of {@code ReportInput}. */
    static final Set<String> INPUT_FIELDS = Set.of("title", "subtitle", "authors", "tlp", "visibility", "source",
            "template", "timeWindow", "area", "blocks");

    /** The members of {@code ReportPatch}. */
    static final Set<String> PATCH_FIELDS = Set.of("title", "subtitle", "status", "authors", "tlp", "visibility",
            "timeWindow", "area", "blockOrder", "version");

    /** The most authors of a report. */
    static final int MAX_AUTHORS = 50;

    private final ReportRepository reports;
    private final ReportRevisionRepository revisions;
    private final ReportSendRepository sends;
    private final ReportSnapshots snapshots;
    private final AccessResolver access;
    private final ShareService shares;
    private final ObjectAclRepository acls;
    private final PrincipalAdminService principals;
    private final PrincipalViews views;
    private final JdbcTemplate jdbc;
    private final tools.jackson.databind.json.JsonMapper json;
    private final Clock clock;
    private final TransactionTemplate tx;

    /** Creates the service. */
    public ReportService(ReportRepository reports, ReportRevisionRepository revisions, ReportSendRepository sends,
            ReportSnapshots snapshots, AccessResolver access, ShareService shares, ObjectAclRepository acls,
            PrincipalAdminService principals, PrincipalViews views, JdbcTemplate jdbc,
            tools.jackson.databind.json.JsonMapper json, Clock clock, PlatformTransactionManager transactions) {
        this.reports = reports;
        this.revisions = revisions;
        this.sends = sends;
        this.snapshots = snapshots;
        this.access = access;
        this.shares = shares;
        this.acls = acls;
        this.principals = principals;
        this.views = views;
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
        this.tx = new TransactionTemplate(transactions);
    }

    /** The object reference of a report. */
    public static String ref(String id) {
        return ReportVocabulary.REF_PREFIX + id;
    }

    // ------------------------------------------------------------------------- reads

    /** A report by id or display id. */
    public Optional<ReportEntity> find(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        Optional<ReportEntity> found = Ulids.isUlid(id) ? reports.findById(id) : Optional.empty();
        if (found.isEmpty() && id.startsWith(ReportVocabulary.DISPLAY_PREFIX)) {
            found = reports.findByDisplayId(id);
        }
        return found;
    }

    /** A report by id or display id, or {@code 404 UNKNOWN_REPORT}. */
    public ReportEntity get(String id) {
        return find(id).orElseThrow(() -> unknown(id));
    }

    /**
     * The report when the caller may perform {@code action}.
     *
     * @throws NotFoundException {@code UNKNOWN_REPORT} when it does not exist or the caller may not see it
     * @throws ForesignalException 403 {@code FORBIDDEN} when the caller's role is too weak
     */
    public ReportEntity require(String id, ObjectCaller caller, AccessAction action) {
        ReportEntity r = find(id).orElseThrow(() -> unknown(id));
        if (access.role(caller, ref(r.getId())) == null) {
            throw unknown(id);
        }
        access.require(caller, ref(r.getId()), action);
        return r;
    }

    /** The caller's role on a report, or null. */
    public AclRole role(ReportEntity r, ObjectCaller caller) {
        return access.role(caller, ref(r.getId()));
    }

    /** The registry record (owner, visibility, TLP) of a report. */
    public ShareableObjectEntity record(ReportEntity r) {
        return access.record(ref(r.getId())).orElseThrow(() -> unknown(r.getId()));
    }

    /** The TLP label of a report. */
    public Tlp tlp(ReportEntity r) {
        return access.tlpOf(ref(r.getId()));
    }

    /**
     * The reports the caller can see, newest change first ({@code listReports}).
     *
     * @param caller the caller
     * @param status only this status, or null
     * @param scope {@code mine}, {@code shared} or {@code all}
     * @param sourceRef only reports built from this object, or null
     * @param cursor the opaque cursor, or null
     * @param limit the page size
     * @return the {@code ReportPage}
     */
    public Map<String, Object> list(ObjectCaller caller, String status, String scope, String sourceRef, String cursor,
            int limit) {
        List<ReportEntity> all = reports.findAllByOrderByUpdatedAtDescIdAsc();
        Map<String, AclRole> roles = access.roles(caller, all.stream().map(r -> ref(r.getId())).toList());
        List<ReportEntity> visible = new ArrayList<>();
        for (ReportEntity r : all) {
            if (roles.get(ref(r.getId())) == null) {
                continue;
            }
            if (status != null && !status.equals(r.getStatus())) {
                continue;
            }
            if (sourceRef != null && !sourceRef.equals(r.getSourceRef())) {
                continue;
            }
            String owner = access.record(ref(r.getId())).map(ShareableObjectEntity::getOwner).orElse(null);
            boolean mine = caller.principal() != null && (caller.principal().equals(owner)
                    || r.getAuthors().contains(caller.principal()));
            if (("mine".equals(scope) && !mine) || ("shared".equals(scope) && mine)) {
                continue;
            }
            visible.add(r);
        }
        int offset = CollabInputs.offset(cursor);
        List<Map<String, Object>> items = new ArrayList<>();
        for (ReportEntity r : visible.subList(Math.min(offset, visible.size()),
                Math.min(offset + limit, visible.size()))) {
            items.add(view(r, caller));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", offset + limit < visible.size() ? CollabInputs.cursor(offset + limit) : null);
        return out;
    }

    // ------------------------------------------------------------------------- create

    /**
     * Creates a report from a {@code ReportInput} ({@code createReport}).
     *
     * @param body the input
     * @param caller the owner
     * @return the new draft
     */
    public ReportEntity create(JsonNode body, ObjectCaller caller) {
        CollabInputs.object(body, "ReportInput", INPUT_FIELDS);
        String title = CollabInputs.required(body, "title", 1, 300).strip();
        Draft draft = new Draft(title);
        draft.subtitle = CollabInputs.text(body, "subtitle", 0, 300);
        draft.template = template(CollabInputs.text(body, "template", 1, 32));
        draft.source = source(body.get("source"), "source", false);
        draft.timeWindow = ReportBlocks.timeRange(body.get("timeWindow"), "timeWindow");
        draft.area = ReportBlocks.geometry(body.get("area"), "area");
        draft.visibility = Visibility.parse("visibility", CollabInputs.text(body, "visibility", 1, 16),
                Visibility.PRIVATE);
        draft.tlp = Tlp.parse("tlp", CollabInputs.text(body, "tlp", 1, 16), null);
        draft.authors = authorIds(CollabInputs.strings(body, "authors", MAX_AUTHORS), "authors");
        JsonNode blocks = body.get("blocks");
        if (blocks != null && !blocks.isNull()) {
            if (!blocks.isArray() || blocks.size() > ReportVocabulary.MAX_BLOCKS) {
                throw ValidationException.forField("blocks", "must be an array of at most "
                        + ReportVocabulary.MAX_BLOCKS + " blocks");
            }
            int i = 0;
            for (JsonNode n : blocks) {
                draft.blocks.add(ReportBlocks.parse(n, "blocks[" + i++ + "]", ReportVocabulary.AUTHOR));
            }
        }
        return save(draft, caller);
    }

    /**
     * A report being created: what {@link #save} stores.
     */
    public static final class Draft {

        final String title;
        String subtitle;
        String template = ReportVocabulary.CUSTOM;
        Map<String, Object> source = new LinkedHashMap<>(Map.of("type", "selection", "refs", List.of()));
        Map<String, Object> timeWindow;
        Map<String, Object> area;
        Visibility visibility = Visibility.PRIVATE;
        Tlp tlp;
        List<String> authors = new ArrayList<>();
        final List<Map<String, Object>> blocks = new ArrayList<>();

        /** A draft titled {@code title}. */
        public Draft(String title) {
            this.title = title;
        }

        /** Sets the subtitle. */
        public Draft subtitle(String value) {
            this.subtitle = value;
            return this;
        }

        /** Sets the template. */
        public Draft template(String value) {
            this.template = value;
            return this;
        }

        /** Sets the source. */
        public Draft source(Map<String, Object> value) {
            this.source = value;
            return this;
        }

        /** Sets the time window. */
        public Draft timeWindow(Map<String, Object> value) {
            this.timeWindow = value;
            return this;
        }

        /** Sets the area. */
        public Draft area(Map<String, Object> value) {
            this.area = value;
            return this;
        }

        /** Adds a block. */
        public Draft block(Map<String, Object> block) {
            this.blocks.add(block);
            return this;
        }

        /** The blocks so far. */
        public List<Map<String, Object>> blocks() {
            return blocks;
        }
    }

    /**
     * Stores a new report owned by the caller: assigns block ids, fills envelopes and snapshots for the preview,
     * inherits the strictest input TLP and registers ownership and author grants.
     *
     * @param draft the report to store
     * @param caller the owner
     * @return the stored draft
     */
    public ReportEntity save(Draft draft, ObjectCaller caller) {
        String owner = caller.principal();
        if (owner == null || caller.anonymous()) {
            throw new ForesignalException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED,
                    "Creating a report needs a token.");
        }
        Instant now = clock.instant();
        ReportSnapshots.Collected collected = new ReportSnapshots.Collected();
        List<Map<String, Object>> blocks = new ArrayList<>();
        for (Map<String, Object> b : draft.blocks) {
            Map<String, Object> filled = snapshots.fill(b, caller, false, collected);
            filled.put("id", ReportBlocks.newId(now));
            filled.put("updatedAt", now.toString());
            filled.put("updatedBy", owner);
            blocks.add(filled);
        }
        Tlp tlp = Tlp.stricter(draft.tlp == null ? access.defaultTlp() : draft.tlp, sourceTlp(draft.source, caller));
        tlp = Tlp.stricter(tlp, Tlp.strictest(collected.tlps, null));
        List<String> authors = new ArrayList<>();
        authors.add(owner);
        for (String a : draft.authors) {
            if (!authors.contains(a)) {
                authors.add(a);
            }
        }
        List<String> grantees = authors.stream().filter(a -> !a.equals(owner)).map(a -> "principal:" + a).toList();
        shares.checkTlp(tlp, owner, grantees, draft.visibility);
        final Tlp label = tlp;
        return tx.execute(s -> {
            ReportEntity r = new ReportEntity(Ulids.next(now), nextDisplayId(now), draft.title, now);
            r.setSubtitle(draft.subtitle);
            r.setTemplate(draft.template);
            r.setSource(draft.source);
            r.setTimeWindow(draft.timeWindow);
            r.setArea(draft.area);
            r.setSections(blocks);
            r.setAuthors(authors);
            r.setCreatedBy(caller.actor());
            reports.save(r);
            access.register(ref(r.getId()), owner, draft.visibility, label);
            grantAuthors(r, authors, owner, now);
            return r;
        });
    }

    private void grantAuthors(ReportEntity r, List<String> authors, String grantor, Instant now) {
        for (String a : authors) {
            ShareableObjectEntity rec = access.record(ref(r.getId())).orElse(null);
            if (rec != null && a.equals(rec.getOwner())) {
                continue;
            }
            String grantee = "principal:" + a;
            ObjectAclEntity row = acls.findByObjectRefAndGrantee(ref(r.getId()), grantee).orElse(null);
            if (row == null) {
                acls.save(new ObjectAclEntity(Ulids.next(now), ref(r.getId()), grantee, AclRole.EDITOR, grantor, now));
            } else if (!row.getRole().atLeast(AclRole.EDITOR)) {
                row.setRole(AclRole.EDITOR);
                acls.save(row);
            }
        }
    }

    // ------------------------------------------------------------------------- patch

    /**
     * Applies a {@code ReportPatch} (editors).
     *
     * @param id the report
     * @param body the merge patch
     * @param ifMatch the {@code If-Match} header, or null
     * @param caller the caller
     * @return the changed report
     * @throws ConflictException {@code 409 REPORT_VERSION_CONFLICT}
     * @throws ForesignalException {@code 403 TLP_VIOLATION}
     */
    public ReportEntity patch(String id, JsonNode body, String ifMatch, ObjectCaller caller) {
        ReportEntity found = require(id, caller, AccessAction.EDIT);
        CollabInputs.object(body, "ReportPatch", PATCH_FIELDS);
        Integer expected = body.hasNonNull("version") ? body.get("version").asInt() : null;
        checkVersion(found, expected, ifMatch);
        boolean onlyStatus = true;
        for (String member : body.propertyNames()) {
            if (!"status".equals(member) && !"version".equals(member)) {
                onlyStatus = false;
            }
        }
        String status = CollabInputs.text(body, "status", 1, 16);
        if (status != null && !ReportVocabulary.editable(status)) {
            throw ValidationException.forField("status", "must be draft or review (publish and retract have their own"
                    + " operations)");
        }
        if (!onlyStatus && !ReportVocabulary.editable(status == null ? found.getStatus() : status)) {
            throw notDraft(found);
        }
        return tx.execute(s -> {
            ReportEntity r = reports.findById(found.getId()).orElseThrow(() -> unknown(id));
            Instant now = clock.instant();
            if (body.has("title")) {
                r.setTitle(CollabInputs.required(body, "title", 1, 300).strip());
            }
            if (body.has("subtitle")) {
                r.setSubtitle(CollabInputs.text(body, "subtitle", 0, 300));
            }
            if (status != null) {
                r.setStatus(status);
            }
            if (body.has("timeWindow")) {
                r.setTimeWindow(ReportBlocks.timeRange(body.get("timeWindow"), "timeWindow"));
            }
            if (body.has("area")) {
                r.setArea(ReportBlocks.geometry(body.get("area"), "area"));
            }
            if (body.has("blockOrder")) {
                r.setSections(reorder(r.getSections(), CollabInputs.strings(body, "blockOrder",
                        ReportVocabulary.MAX_BLOCKS)));
            }
            ShareableObjectEntity rec = access.record(ref(r.getId())).orElseThrow(() -> unknown(id));
            if (body.has("authors")) {
                List<String> authors = new ArrayList<>();
                authors.add(rec.getOwner());
                for (String a : authorIds(CollabInputs.strings(body, "authors", MAX_AUTHORS), "authors")) {
                    if (!authors.contains(a)) {
                        authors.add(a);
                    }
                }
                r.setAuthors(authors);
                grantAuthors(r, authors, caller.principal(), now);
            }
            Visibility vis = body.has("visibility") ? Visibility.parse("visibility",
                    CollabInputs.text(body, "visibility", 1, 16), rec.getVisibility()) : rec.getVisibility();
            Tlp requested = body.has("tlp") ? Tlp.parse("tlp", CollabInputs.text(body, "tlp", 1, 16),
                    Tlp.fromWire(rec.getTlp())) : Tlp.fromWire(rec.getTlp());
            Tlp floor = inputTlp(r, caller);
            if (floor != null && floor.stricterThan(requested)) {
                if (body.has("tlp")) {
                    throw tlpFloor(floor);
                }
                requested = floor;
            }
            shares.checkTlp(requested, rec.getOwner(), grantees(rec), vis);
            rec.setVisibility(vis);
            rec.setTlp(requested.wireValue());
            access.save(rec);
            r.touch(now);
            return reports.save(r);
        });
    }

    /** The {@code 403 TLP_VIOLATION} of an attempt to label a report below its strictest input. */
    static ForesignalException tlpFloor(Tlp floor) {
        String rule = floor.wireValue() + ": a derived product inherits the strictest label of its inputs";
        return new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.TLP_VIOLATION,
                "The report cites material labeled " + floor.wireValue() + "; it cannot carry a weaker label.")
                .withProperty("rule", rule);
    }

    /** The grantees of a report's access list. */
    List<String> grantees(ShareableObjectEntity rec) {
        return shares.entries(rec.getObjectRef(), PrincipalViews.cache()).stream()
                .map(e -> String.valueOf(e.get("grantee"))).toList();
    }

    private static List<Map<String, Object>> reorder(List<Map<String, Object>> blocks, List<String> order) {
        Map<String, Map<String, Object>> byId = new LinkedHashMap<>();
        for (Map<String, Object> b : blocks) {
            byId.put(String.valueOf(b.get("id")), b);
        }
        if (order.size() != byId.size() || !new HashSet<>(order).equals(byId.keySet())) {
            throw ValidationException.forField("blockOrder", "must list every block id exactly once");
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (String id : order) {
            out.add(byId.get(id));
        }
        return out;
    }

    /**
     * Checks the optimistic lock: {@code expected} (body {@code version}) or {@code If-Match} must name the current
     * version when given.
     *
     * @throws ConflictException {@code 409 REPORT_VERSION_CONFLICT}
     */
    static void checkVersion(ReportEntity r, Integer expected, String ifMatch) {
        if (expected != null && expected != r.getVersion()) {
            throw versionConflict(r, String.valueOf(expected));
        }
        if (ifMatch != null && !ifMatch.isBlank() && !matches(ifMatch, r.getVersion())) {
            throw versionConflict(r, ifMatch);
        }
    }

    /** True when an {@code If-Match} value names {@code version} ({@code "3"}, {@code W/"3"}, {@code 3} or *). */
    static boolean matches(String ifMatch, int version) {
        String v = ifMatch.strip();
        if ("*".equals(v)) {
            return true;
        }
        if (v.startsWith("W/")) {
            v = v.substring(2);
        }
        return v.replace("\"", "").equals(String.valueOf(version));
    }

    /** The ETag of a report draft. */
    public static String etag(ReportEntity r) {
        return "\"" + r.getVersion() + "\"";
    }

    private static ConflictException versionConflict(ReportEntity r, String given) {
        ConflictException e = new ConflictException(ErrorCodes.REPORT_VERSION_CONFLICT, "The report changed since"
                + " version " + given + "; it is at version " + r.getVersion() + ".");
        e.withProperty("version", r.getVersion());
        return e;
    }

    /** {@code 409 REPORT_VERSION_CONFLICT}: the report is not a draft. */
    static ConflictException notDraft(ReportEntity r) {
        return new ConflictException(ErrorCodes.REPORT_VERSION_CONFLICT, "The report is " + r.getStatus()
                + "; set status to draft to edit it for the next revision.");
    }

    // ------------------------------------------------------------------------- delete

    /** Deletes a report, its draft and its revisions (owner only). */
    public void delete(String id, ObjectCaller caller) {
        ReportEntity r = require(id, caller, AccessAction.DELETE);
        tx.executeWithoutResult(s -> {
            revisions.deleteByReportId(r.getId());
            sends.deleteByReportId(r.getId());
            access.forget(ref(r.getId()));
            reports.deleteById(r.getId());
        });
    }

    // ------------------------------------------------------------------------- blocks

    /**
     * Adds a block after {@code after} (or at the end) (editors, draft only).
     *
     * @return the stored block
     */
    public Map<String, Object> addBlock(String id, JsonNode body, String after, String ifMatch, ObjectCaller caller) {
        ReportEntity found = require(id, caller, AccessAction.EDIT);
        checkVersion(found, null, ifMatch);
        if (!ReportVocabulary.editable(found.getStatus())) {
            throw notDraft(found);
        }
        Map<String, Object> parsed = ReportBlocks.parse(body, "block", ReportVocabulary.AUTHOR);
        return tx.execute(s -> {
            ReportEntity r = reports.findById(found.getId()).orElseThrow(() -> unknown(id));
            if (r.getSections().size() >= ReportVocabulary.MAX_BLOCKS) {
                throw ValidationException.forField("block", "the report already has " + ReportVocabulary.MAX_BLOCKS
                        + " blocks");
            }
            Instant now = clock.instant();
            Map<String, Object> block = snapshots.fill(parsed, caller, false, new ReportSnapshots.Collected());
            block.put("id", ReportBlocks.newId(now));
            block.put("updatedAt", now.toString());
            block.put("updatedBy", caller.principal());
            List<Map<String, Object>> blocks = new ArrayList<>(r.getSections());
            int at = blocks.size();
            if (after != null && !after.isBlank()) {
                at = indexOf(blocks, after) + 1;
            }
            blocks.add(at, block);
            r.setSections(blocks);
            r.touch(now);
            reports.save(r);
            raiseTlp(r, caller);
            return block;
        });
    }

    /**
     * Applies a merge patch to one block (editors, draft only); the block keeps its id, type and text origin.
     *
     * @return the changed block
     */
    public Map<String, Object> patchBlock(String id, String blockId, JsonNode body, String ifMatch,
            ObjectCaller caller) {
        ReportEntity found = require(id, caller, AccessAction.EDIT);
        checkVersion(found, null, ifMatch);
        if (!ReportVocabulary.editable(found.getStatus())) {
            throw notDraft(found);
        }
        if (body == null || !body.isObject()) {
            throw ValidationException.forField("block", "must be a merge patch object");
        }
        return tx.execute(s -> {
            ReportEntity r = reports.findById(found.getId()).orElseThrow(() -> unknown(id));
            List<Map<String, Object>> blocks = new ArrayList<>(r.getSections());
            int i = indexOf(blocks, blockId);
            Map<String, Object> current = blocks.get(i);
            if (body.has("type") && !String.valueOf(current.get("type")).equals(body.get("type").asString())) {
                throw ValidationException.forField("type", "cannot be changed; delete the block and add another");
            }
            Map<String, Object> merged = new LinkedHashMap<>(editable(current));
            for (String member : body.propertyNames()) {
                if (Set.of("id", "textOrigin", "updatedAt", "updatedBy").contains(member)) {
                    continue;
                }
                JsonNode v = body.get(member);
                if (v == null || v.isNull()) {
                    merged.remove(member);
                } else {
                    merged.put(member, ReportBlocks.plain(v));
                }
            }
            String origin = String.valueOf(current.getOrDefault("textOrigin", ReportVocabulary.AUTHOR));
            Map<String, Object> parsed = ReportBlocks.parse(json.valueToTree(merged), "block", origin);
            Instant now = clock.instant();
            Map<String, Object> block = snapshots.fill(parsed, caller, false, new ReportSnapshots.Collected());
            block.put("id", current.get("id"));
            block.put("updatedAt", now.toString());
            block.put("updatedBy", caller.principal());
            blocks.set(i, block);
            r.setSections(blocks);
            r.touch(now);
            reports.save(r);
            raiseTlp(r, caller);
            return block;
        });
    }

    /** Removes a block (editors, draft only), an LLM narrative block included. */
    public void deleteBlock(String id, String blockId, String ifMatch, ObjectCaller caller) {
        ReportEntity found = require(id, caller, AccessAction.EDIT);
        checkVersion(found, null, ifMatch);
        if (!ReportVocabulary.editable(found.getStatus())) {
            throw notDraft(found);
        }
        tx.executeWithoutResult(s -> {
            ReportEntity r = reports.findById(found.getId()).orElseThrow(() -> unknown(id));
            List<Map<String, Object>> blocks = new ArrayList<>(r.getSections());
            blocks.remove(indexOf(blocks, blockId));
            r.setSections(blocks);
            r.touch(clock.instant());
            reports.save(r);
        });
    }

    /** The request-side fields of a stored block (envelopes and snapshots dropped, refs unpinned). */
    @SuppressWarnings("unchecked")
    static Map<String, Object> editable(Map<String, Object> block) {
        Map<String, Object> out = new LinkedHashMap<>();
        String type = String.valueOf(block.get("type"));
        for (Map.Entry<String, Object> e : block.entrySet()) {
            String k = e.getKey();
            if (Set.of("id", "textOrigin", "updatedAt", "updatedBy", "snapshot", "revisionIds", "attachmentId",
                    "mediaId", "caveat").contains(k)) {
                continue;
            }
            Object v = e.getValue();
            if (("objects".equals(type) || "timeline".equals(type) || "evidence".equals(type)
                    || "forecasts".equals(type)) && ("items".equals(k) || "questions".equals(k))) {
                List<Object> items = new ArrayList<>();
                for (Object o : ReportBlocks.asList(v)) {
                    if (o instanceof Map<?, ?> m) {
                        Map<String, Object> item = new LinkedHashMap<>((Map<String, Object>) m);
                        item.remove("envelope");
                        item.remove("snapshot");
                        item.remove("grade");
                        if ("evidence".equals(type)) {
                            item.remove("title");
                        }
                        items.add(item);
                    }
                }
                v = items;
            }
            if (v != null) {
                out.put(k, v);
            }
        }
        return out;
    }

    private static int indexOf(List<Map<String, Object>> blocks, String blockId) {
        for (int i = 0; i < blocks.size(); i++) {
            if (String.valueOf(blocks.get(i).get("id")).equals(blockId)) {
                return i;
            }
        }
        throw new NotFoundException(ErrorCodes.NOT_FOUND, "The report has no block '" + blockId + "'.");
    }

    // ------------------------------------------------------------------------- TLP

    /** The strictest TLP of a report's inputs as the caller resolves them, or null. */
    public Tlp inputTlp(ReportEntity r, ObjectCaller caller) {
        Set<String> refs = new LinkedHashSet<>();
        for (Map<String, Object> b : r.getSections()) {
            refs.addAll(ReportBlocks.citedRefs(b));
        }
        List<Tlp> labels = new ArrayList<>();
        for (Map.Entry<String, ai.foresignal.object.ObjectEnvelope> e : snapshots.envelopes(refs, caller)
                .entrySet()) {
            if (e.getValue().tlp() != null) {
                labels.add(Tlp.fromWire(e.getValue().tlp()));
            }
        }
        labels.add(sourceTlp(r.getSource(), caller));
        return Tlp.strictest(labels.stream().filter(java.util.Objects::nonNull).toList(), null);
    }

    /** Raises a report's label to its strictest input when needed (its grants must allow the stricter label). */
    void raiseTlp(ReportEntity r, ObjectCaller caller) {
        Tlp floor = inputTlp(r, caller);
        ShareableObjectEntity rec = access.record(ref(r.getId())).orElse(null);
        if (rec == null || floor == null) {
            return;
        }
        Tlp current = Tlp.fromWire(rec.getTlp());
        if (floor.stricterThan(current)) {
            shares.checkTlp(floor, rec.getOwner(), grantees(rec), rec.getVisibility());
            rec.setTlp(floor.wireValue());
            access.save(rec);
        }
    }

    private Tlp sourceTlp(Map<String, Object> source, ObjectCaller caller) {
        if (source == null || !(source.get("ref") instanceof String ref)) {
            return null;
        }
        return snapshots.find(ref, caller).map(e -> e.tlp() == null ? null : Tlp.fromWire(e.tlp())).orElse(null);
    }

    /**
     * Checks that the report's label allows a public share link: {@code CLEAR}, or {@code GREEN} when
     * {@code allowGreen}.
     *
     * @throws ForesignalException {@code 403 TLP_VIOLATION}
     */
    public static void checkPublicLink(Tlp tlp, boolean allowGreen) {
        if (tlp == Tlp.GREEN && allowGreen) {
            return;
        }
        ShareService.enforce(EgressPolicy.tlp(tlp, TlpTarget.publicLink()));
    }

    // ------------------------------------------------------------------------- parsing

    /** A template name, defaulting to {@code custom}. */
    static String template(String value) {
        if (value == null) {
            return ReportVocabulary.CUSTOM;
        }
        if (!ReportVocabulary.TEMPLATES.contains(value)) {
            throw ValidationException.forField("template", "must be one of " + ReportVocabulary.TEMPLATES);
        }
        return value;
    }

    /**
     * Parses a {@code ReportSource}.
     *
     * @param node the source
     * @param field the request field
     * @param required true when it must be present
     * @return {@code {type, ref, refs}}
     */
    public static Map<String, Object> source(JsonNode node, String field, boolean required) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (node == null || node.isNull()) {
            if (required) {
                throw ValidationException.forField(field, "is required");
            }
            out.put("type", "selection");
            out.put("ref", null);
            out.put("refs", List.of());
            return out;
        }
        CollabInputs.object(node, "ReportSource", Set.of("type", "ref", "refs"));
        String type = CollabInputs.required(node, "type", 1, 32);
        if (!ReportVocabulary.SOURCE_TYPES.contains(type)) {
            throw ValidationException.forField(field + ".type", "must be one of " + ReportVocabulary.SOURCE_TYPES);
        }
        out.put("type", type);
        String ref = CollabInputs.text(node, "ref", 1, ObjectRef.MAX_LENGTH);
        List<String> refs = new ArrayList<>();
        if ("selection".equals(type)) {
            for (String r : CollabInputs.strings(node, "refs", ReportVocabulary.MAX_SELECTION)) {
                String formatted = ObjectRef.parse(r, field + ".refs").format();
                if (!refs.contains(formatted)) {
                    refs.add(formatted);
                }
            }
            out.put("ref", null);
        } else {
            if (ref == null) {
                throw ValidationException.forField(field + ".ref", "is required for a " + type + " source");
            }
            ObjectRef parsed = ObjectRef.parse(ref, field + ".ref");
            if (!type.equals(parsed.type())) {
                throw ValidationException.forField(field + ".ref", "must be a " + type + " reference");
            }
            out.put("ref", parsed.format());
        }
        out.put("refs", refs);
        return out;
    }

    private List<String> authorIds(List<String> values, String field) {
        List<String> out = new ArrayList<>();
        for (String v : values) {
            String id = principals.get(v.startsWith("principal:") ? v.substring("principal:".length()) : v).getId();
            if (!out.contains(id)) {
                out.add(id);
            }
        }
        return out;
    }

    // ------------------------------------------------------------------------- views

    /** The {@code Report} view. */
    public Map<String, Object> view(ReportEntity r, ObjectCaller caller) {
        ReportEntity fresh = reports.findById(r.getId()).orElse(r);
        ShareableObjectEntity rec = access.record(ref(fresh.getId())).orElse(null);
        Map<String, Map<String, Object>> cache = PrincipalViews.cache();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", fresh.getId());
        out.put("ref", ref(fresh.getId()));
        out.put("displayId", fresh.getDisplayId());
        out.put("title", fresh.getTitle());
        out.put("subtitle", fresh.getSubtitle());
        out.put("status", fresh.getStatus());
        out.put("owner", rec == null ? null : summary(rec.getOwner(), cache));
        List<Object> authors = new ArrayList<>();
        for (String a : fresh.getAuthors()) {
            authors.add(summary(a, cache));
        }
        out.put("authors", authors);
        out.put("tlp", rec == null ? access.defaultTlp().wireValue() : rec.getTlp());
        out.put("visibility", rec == null ? Visibility.PRIVATE.wireValue() : rec.getVisibility().wireValue());
        out.put("source", fresh.getSource());
        out.put("template", fresh.getTemplate());
        out.put("timeWindow", fresh.getTimeWindow());
        out.put("area", fresh.getArea());
        List<Map<String, Object>> blocks = new ArrayList<>();
        for (Map<String, Object> b : fresh.getSections()) {
            blocks.add(blockView(b, cache));
        }
        out.put("blocks", blocks);
        out.put("machineGenerated", ReportBlocks.machineGenerated(fresh.getSections()));
        out.put("version", fresh.getVersion());
        out.put("publishedRevision", fresh.getPublishedRevision());
        out.put("publishedAt", fresh.getPublishedAt() == null ? null : fresh.getPublishedAt().toString());
        out.put("manifestId", fresh.getManifestId());
        out.put("provenanceChain", List.of());
        out.put("tags", tags(fresh, caller));
        out.put("createdAt", fresh.getCreatedAt().toString());
        out.put("updatedAt", fresh.getUpdatedAt().toString());
        return out;
    }

    /** The {@code ReportBlock} view: {@code updatedBy} as a principal summary. */
    public Map<String, Object> blockView(Map<String, Object> block, Map<String, Map<String, Object>> cache) {
        Map<String, Object> out = new LinkedHashMap<>(block);
        Object by = block.get("updatedBy");
        out.put("updatedBy", by instanceof String id ? summary(id, cache) : by);
        return out;
    }

    /** A principal summary, or {@code {id}} when the principal is gone. */
    Object summary(String principalId, Map<String, Map<String, Object>> cache) {
        if (principalId == null) {
            return null;
        }
        try {
            Map<String, Object> s = views.summary(principalId, cache);
            return s == null ? Map.of("id", principalId) : s;
        } catch (RuntimeException e) {
            return Map.of("id", principalId);
        }
    }

    private ai.foresignal.collab.TagService tagService;

    /** Injects the tag service when present (tags in views). */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    void setTagService(ai.foresignal.collab.TagService tagService) {
        this.tagService = tagService;
    }

    private List<String> tags(ReportEntity r, ObjectCaller caller) {
        if (tagService == null || caller == null) {
            return List.of();
        }
        try {
            return tagService.namesOn(ref(r.getId()), caller);
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    // ------------------------------------------------------------------------- helpers

    private String nextDisplayId(Instant now) {
        int year = now.atZone(ZoneOffset.UTC).getYear();
        String prefix = ReportVocabulary.DISPLAY_PREFIX + year + "-";
        jdbc.queryForObject("SELECT count(*) FROM (SELECT pg_advisory_xact_lock(?)) AS held", Long.class,
                DISPLAY_ID_LOCK);
        Integer max = jdbc.queryForObject("SELECT max(CAST(substring(display_id FROM ?) AS integer))"
                + " FROM report WHERE display_id LIKE ? AND substring(display_id FROM ?) ~ '^[0-9]+$'",
                Integer.class, prefix.length() + 1, prefix + "%", prefix.length() + 1);
        int next = max == null ? 1 : max + 1;
        return prefix + String.format(Locale.ROOT, "%04d", next);
    }

    /** {@code 404 UNKNOWN_REPORT}. */
    public static NotFoundException unknown(String id) {
        return new NotFoundException(ErrorCodes.UNKNOWN_REPORT, "No report '" + id + "' exists, or you may not"
                + " see it.");
    }
}
