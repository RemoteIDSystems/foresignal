package ai.foresignal.report;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.chronicle.ChronicleEntity;
import ai.foresignal.chronicle.ChronicleEventEntity;
import ai.foresignal.chronicle.ChronicleEventRepository;
import ai.foresignal.chronicle.ChronicleLayerEntity;
import ai.foresignal.chronicle.ChronicleService;
import ai.foresignal.chronicle.ChronicleVocabulary;
import ai.foresignal.collab.AccessAction;
import ai.foresignal.collab.AccessResolver;
import ai.foresignal.collab.CollabInputs;
import ai.foresignal.collab.TagService;
import ai.foresignal.cycle.AssessmentEntity;
import ai.foresignal.cycle.AssessmentService;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.hypothesis.HypothesisEntity;
import ai.foresignal.hypothesis.HypothesisSetEntity;
import ai.foresignal.intel.InvestigationEntity;
import ai.foresignal.intel.InvestigationItemEntity;
import ai.foresignal.intel.InvestigationService;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.RelationService;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.object.WorkspaceEntity;
import ai.foresignal.object.WorkspaceService;
import ai.foresignal.security.Scope;
import ai.foresignal.situation.SituationEntity;
import ai.foresignal.situation.SituationMemberEntity;
import ai.foresignal.situation.SituationMemberTypes;
import ai.foresignal.situation.SituationService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

/**
 * Builds deterministic report drafts (prompt 132, {@code draftReport}): the same source and template always give the
 * same blocks, without an LLM. Every block it writes is labeled {@code deterministic}.
 *
 * <ul>
 *   <li><b>situation</b> ({@code situation-brief}): key judgments from the leading hypothesis of each linked set and
 *       the ensemble of each linked ledger question (ICD 203 terms), a summary, the member timeline, the cited
 *       members, a map of the situation's extent, the hypothesis sets, the questions, coverage caveats.</li>
 *   <li><b>investigation</b> ({@code investigation-summary}): a summary, the item timeline, the cited objects, the
 *       graded evidence items, linked hypothesis sets and questions, a map of the items' extent, caveats.</li>
 *   <li><b>chronicle</b> ({@code chronicle-recap}): its summary, the thread's highlights, one map per major layer,
 *       linked hypothesis sets and their questions, capture gaps as caveats.</li>
 *   <li><b>tag</b>, <b>selection</b> and <b>workspace</b>: the visible objects grouped by type, the relations among
 *       them, a map of their extent and the situations they relate to.</li>
 *   <li><b>weekly-assessment</b> wraps a 128 assessment: its key judgments, sections and citations.</li>
 * </ul>
 * With {@code narrative=true} (agent scope, a configured chat provider and budget) {@link ReportNarrativeWriter} adds
 * a labeled LLM narrative block; it can be edited or deleted like any block.
 */
@Service
public class ReportDrafter {

    /** The members of {@code ReportDraftRequest}. */
    static final Set<String> FIELDS = Set.of("source", "template", "title", "timeWindow", "area", "narrative",
            "assessmentId");

    /** Most objects cited by a drafted objects block. */
    static final int MAX_OBJECTS = 100;

    /** Most timeline items drafted. */
    static final int MAX_TIMELINE = 50;

    /** Most hypothesis sets and questions drafted. */
    static final int MAX_LINKED = 10;

    /** Major chronicle layers mapped. */
    static final int MAJOR_LAYERS = 3;

    private final ReportService reports;
    private final ReportSnapshots snapshots;
    private final AccessResolver access;
    private final SituationService situations;
    private final InvestigationService investigations;
    private final ChronicleService chronicles;
    private final ChronicleEventRepository chronicleEvents;
    private final TagService tags;
    private final WorkspaceService workspaces;
    private final AssessmentService assessments;
    private final RelationService relations;
    private final ObjectProvider<ReportNarrativeWriter> narrative;
    private final JdbcTemplate jdbc;
    private final java.time.Clock clock;

    /** Creates the drafter. */
    public ReportDrafter(ReportService reports, ReportSnapshots snapshots, AccessResolver access,
            SituationService situations, InvestigationService investigations, ChronicleService chronicles,
            ChronicleEventRepository chronicleEvents, TagService tags, WorkspaceService workspaces,
            AssessmentService assessments, RelationService relations, ObjectProvider<ReportNarrativeWriter> narrative,
            JdbcTemplate jdbc, java.time.Clock clock) {
        this.reports = reports;
        this.snapshots = snapshots;
        this.access = access;
        this.situations = situations;
        this.investigations = investigations;
        this.chronicles = chronicles;
        this.chronicleEvents = chronicleEvents;
        this.tags = tags;
        this.workspaces = workspaces;
        this.assessments = assessments;
        this.relations = relations;
        this.narrative = narrative;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /**
     * Drafts and stores a report ({@code draftReport}).
     *
     * @param body the {@code ReportDraftRequest}
     * @param caller the owner
     * @return the stored draft
     * @throws ForesignalException {@code 403} when {@code narrative} is asked without agent scope,
     *     {@code 409 LLM_NOT_CONFIGURED} without a chat provider
     */
    public ReportEntity draft(JsonNode body, ObjectCaller caller) {
        CollabInputs.object(body, "ReportDraftRequest", FIELDS);
        Map<String, Object> source = ReportService.source(body.get("source"), "source", true);
        String template = ReportService.template(CollabInputs.text(body, "template", 1, 32));
        String title = CollabInputs.text(body, "title", 1, 300);
        Map<String, Object> window = ReportBlocks.timeRange(body.get("timeWindow"), "timeWindow");
        Map<String, Object> area = ReportBlocks.geometry(body.get("area"), "area");
        boolean wantsNarrative = CollabInputs.bool(body, "narrative", false);
        String assessmentId = CollabInputs.text(body, "assessmentId", 26, 26);
        if (wantsNarrative && !caller.agent()) {
            throw new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.INSUFFICIENT_SCOPE,
                    "A narrative needs an agent token: it spends LLM budget.").withProperty("requiredScope",
                            Scope.AGENT.name().toLowerCase(java.util.Locale.ROOT));
        }
        ReportNarrativeWriter writer = wantsNarrative ? narrative.getIfAvailable() : null;
        if (wantsNarrative) {
            if (writer == null) {
                throw new ai.foresignal.api.error.ConflictException(ErrorCodes.LLM_NOT_CONFIGURED,
                        "The narrative needs a chat provider.");
            }
            writer.requireConfigured();
        }
        Built built = switch (String.valueOf(source.get("type"))) {
            case "situation" -> situation(String.valueOf(source.get("ref")), caller);
            case "investigation" -> investigation(String.valueOf(source.get("ref")), caller);
            case "chronicle" -> chronicle(String.valueOf(source.get("ref")), caller);
            case "tag" -> tag(String.valueOf(source.get("ref")), caller);
            case "workspace" -> workspace(String.valueOf(source.get("ref")), caller);
            default -> selection(ReportBlocks.asList(source.get("refs")).stream().map(String::valueOf).toList(),
                    "Selection", caller);
        };
        if ("weekly-assessment".equals(template)) {
            built = assessment(assessmentId, built, caller);
        } else if (assessmentId != null) {
            throw ValidationException.forField("assessmentId", "applies only to the weekly-assessment template");
        }
        ReportService.Draft draft = new ReportService.Draft(title == null ? built.title : title.strip())
                .template(template).source(source).timeWindow(window != null ? window : built.window)
                .area(area != null ? area : built.area);
        for (Map<String, Object> b : built.blocks) {
            draft.block(b);
        }
        if (writer != null) {
            draft.block(writer.write(draft, caller));
        }
        return reports.save(draft, caller);
    }

    /** A drafted report: title, blocks, window and area. */
    static final class Built {

        String title;
        final List<Map<String, Object>> blocks = new ArrayList<>();
        Map<String, Object> window;
        Map<String, Object> area;

        Built(String title) {
            this.title = title;
        }

        Built add(Map<String, Object> block) {
            if (block != null) {
                blocks.add(block);
            }
            return this;
        }
    }

    // ------------------------------------------------------------------------- situation

    private Built situation(String ref, ObjectCaller caller) {
        String id = ObjectRef.parse(ref).id();
        SituationEntity s = situations.visible(id, caller.seesPrivate());
        Built out = new Built("Situation brief: " + s.getTitle());
        List<String> setRefs = linked("SELECT id FROM hypothesis_set WHERE situation_id = ? ORDER BY created_at, id",
                s.getId(), ObjectTypes.HYPOTHESIS_SET);
        List<String> questionRefs = linked("SELECT id FROM forecast_question WHERE situation_id = ? ORDER BY"
                + " created_at, id", s.getId(), ObjectTypes.QUESTION);
        out.add(bluf(setRefs, questionRefs, caller));
        StringBuilder md = new StringBuilder();
        md.append("**").append(s.getTitle()).append("** is ").append(s.getStatus()).append(" at severity ")
                .append(s.getSeverity()).append('.');
        if (s.getRegionCodes() != null && !s.getRegionCodes().isEmpty()) {
            md.append(" Regions: ").append(String.join(", ", s.getRegionCodes())).append('.');
        }
        md.append(" It has ").append(s.getMemberCount()).append(" members");
        if (s.getSignalFamilies() != null && !s.getSignalFamilies().isEmpty()) {
            md.append(" from the signal families ").append(String.join(", ", s.getSignalFamilies()));
        }
        md.append('.');
        if (s.getStartedAt() != null) {
            md.append(" Started ").append(s.getStartedAt()).append(", last active ").append(s.getLastActivityAt())
                    .append('.');
        }
        if (s.getSummary() != null && !s.getSummary().isBlank()) {
            md.append("\n\n").append(s.getSummary());
        }
        out.add(markdown("Summary", md.toString(), List.of(ref)));
        List<SituationMemberEntity> members = new ArrayList<>(situations.members(s.getId()));
        members.sort(Comparator.comparing(SituationMemberEntity::getObservedAt,
                Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(SituationMemberEntity::getId));
        List<Map<String, Object>> items = new ArrayList<>();
        List<String> memberRefs = new ArrayList<>();
        memberRefs.add(ref);
        for (SituationMemberEntity m : members) {
            String mref = memberRef(m);
            if (mref != null && memberRefs.size() < MAX_OBJECTS) {
                memberRefs.add(mref);
            }
            if (m.getObservedAt() != null && items.size() < MAX_TIMELINE) {
                items.add(timelineItem(m.getObservedAt(), m.getTitle() == null ? m.getMemberType() : m.getTitle(),
                        mref));
            }
        }
        out.add(timeline("Timeline", ref, items));
        out.add(objects("Members", memberRefs));
        if (s.getGeom() != null) {
            org.locationtech.jts.geom.Envelope e = s.getGeom().getEnvelopeInternal();
            out.add(map("Map", List.of(), null, List.of(e.getMinX(), e.getMinY(), e.getMaxX(), e.getMaxY())));
        }
        linkedBlocks(out, setRefs, questionRefs);
        out.add(coverage(List.of()));
        if (s.getStartedAt() != null) {
            Map<String, Object> w = new LinkedHashMap<>();
            w.put("start", s.getStartedAt().toString());
            w.put("end", s.getClosedAt() == null ? null : s.getClosedAt().toString());
            out.window = w;
        }
        return out;
    }

    /** The object reference of a situation member, or null. */
    static String memberRef(SituationMemberEntity m) {
        try {
            return switch (m.getMemberType()) {
                case SituationMemberTypes.RECORD, SituationMemberTypes.ANOMALY -> {
                    int slash = m.getRef().indexOf('/');
                    yield slash <= 0 ? null : ObjectRef.record(m.getRef().substring(0, slash),
                            m.getRef().substring(slash + 1), m.getSeq()).format();
                }
                case SituationMemberTypes.ALERT -> ObjectRef.of(ObjectTypes.ALERT, m.getRef()).format();
                case SituationMemberTypes.INTEL_EVENT -> ObjectRef.of(ObjectTypes.INTEL_EVENT, m.getRef()).format();
                case SituationMemberTypes.FINDING -> ObjectRef.of(ObjectTypes.FINDING, m.getRef()).format();
                case SituationMemberTypes.INVESTIGATION -> ObjectRef.of(ObjectTypes.INVESTIGATION, m.getRef()).format();
                default -> null;
            };
        } catch (RuntimeException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------------- investigation

    private Built investigation(String ref, ObjectCaller caller) {
        String id = ObjectRef.parse(ref).id();
        InvestigationEntity inv = investigations.get(id);
        access.require(caller, ObjectTypes.INVESTIGATION + ":" + inv.getId(), AccessAction.VIEW);
        Built out = new Built("Investigation summary: " + inv.getTitle());
        List<InvestigationItemEntity> items = investigations.items(inv.getId(), caller.seesPrivate());
        Map<String, Integer> counts = new TreeMap<>();
        List<String> refs = new ArrayList<>();
        List<String> evidenceIds = new ArrayList<>();
        List<Map<String, Object>> timeline = new ArrayList<>();
        for (InvestigationItemEntity item : items) {
            counts.merge(item.getItemType(), 1, Integer::sum);
            String itemRef = itemRef(item);
            if ("evidence-item".equals(item.getItemType()) && itemRef != null) {
                evidenceIds.add(ObjectRef.parse(itemRef).id());
            } else if (itemRef != null && refs.size() < MAX_OBJECTS && !refs.contains(itemRef)) {
                refs.add(itemRef);
            }
            if (timeline.size() < MAX_TIMELINE) {
                timeline.add(timelineItem(item.getAddedAt(), itemTitle(item), itemRef));
            }
        }
        StringBuilder md = new StringBuilder();
        md.append("**").append(inv.getDisplayId()).append(' ').append(inv.getTitle()).append("** is ")
                .append(inv.getStatus()).append(", opened ").append(inv.getCreatedAt()).append(". It holds ")
                .append(items.size()).append(" items");
        if (!counts.isEmpty()) {
            List<String> parts = new ArrayList<>();
            counts.forEach((k, v) -> parts.add(v + " " + k));
            md.append(" (").append(String.join(", ", parts)).append(')');
        }
        md.append('.');
        if (inv.getSummary() != null && !inv.getSummary().isBlank()) {
            md.append("\n\n").append(inv.getSummary());
        }
        out.add(markdown("Summary", md.toString(), List.of(ObjectTypes.INVESTIGATION + ":" + inv.getId())));
        out.add(timeline("Timeline", ObjectTypes.INVESTIGATION + ":" + inv.getId(), timeline));
        if (!refs.isEmpty()) {
            out.add(objects("Cited objects", refs));
        }
        if (!evidenceIds.isEmpty()) {
            out.add(evidence(evidenceIds));
        }
        out.add(extentMap(refs, caller));
        List<String> setRefs = prefixed(ObjectTypes.HYPOTHESIS_SET, inv.getHypothesisSetIds());
        List<String> questionRefs = prefixed(ObjectTypes.QUESTION, inv.getQuestionIds());
        linkedBlocks(out, setRefs, questionRefs);
        if (!setRefs.isEmpty() || !questionRefs.isEmpty()) {
            out.blocks.add(0, bluf(setRefs, questionRefs, caller));
        }
        out.add(coverage(List.of()));
        return out;
    }

    /** The object reference of an investigation item, or null for notes and briefing runs. */
    static String itemRef(InvestigationItemEntity item) {
        try {
            return switch (item.getItemType()) {
                case "object", "evidence-item", "attachment" -> ObjectRef.parse(item.getRef()).format();
                case "record", "anomaly" -> {
                    int slash = item.getRef().indexOf('/');
                    yield slash <= 0 ? null : ObjectRef.record(item.getRef().substring(0, slash),
                            item.getRef().substring(slash + 1), item.getSeq()).format();
                }
                case "alert" -> ObjectRef.of(ObjectTypes.ALERT, item.getRef()).format();
                case "event" -> ObjectRef.of(ObjectTypes.INTEL_EVENT, item.getRef()).format();
                case "place" -> ObjectRef.of(ObjectTypes.PLACE, item.getRef()).format();
                default -> null;
            };
        } catch (RuntimeException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static String itemTitle(InvestigationItemEntity item) {
        Map<String, Object> snap = item.getSnapshot();
        if (snap != null) {
            if (snap.get("envelope") instanceof Map<?, ?> env && env.get("title") != null) {
                return String.valueOf(env.get("title"));
            }
            if (snap.get("title") != null) {
                return String.valueOf(snap.get("title"));
            }
        }
        if (item.getNote() != null && !item.getNote().isBlank()) {
            String note = item.getNote().strip();
            return note.length() > 200 ? note.substring(0, 199) + "…" : note;
        }
        return item.getItemType() + " " + item.getRef();
    }

    // ------------------------------------------------------------------------- chronicle

    private Built chronicle(String ref, ObjectCaller caller) {
        ChronicleEntity c = chronicles.require(ObjectRef.parse(ref).id(), caller, AccessAction.VIEW);
        String cref = ChronicleService.ref(c.getId());
        Built out = new Built("Chronicle recap: " + c.getName());
        List<ChronicleLayerEntity> layers = new ArrayList<>(chronicles.liveLayers(c.getId()));
        StringBuilder md = new StringBuilder();
        md.append("**").append(c.getDisplayId()).append(' ').append(c.getName()).append("** is ")
                .append(c.getStatus());
        if (c.getStartedAt() != null) {
            md.append(", capturing since ").append(c.getStartedAt());
            if (c.getStoppedAt() != null) {
                md.append(" until ").append(c.getStoppedAt());
            }
        }
        md.append(". It has ").append(layers.size()).append(" layers and holds ").append(c.getRowsCaptured())
                .append(" captured versions.");
        if (c.getDescription() != null && !c.getDescription().isBlank()) {
            md.append("\n\n").append(c.getDescription());
        }
        if (!layers.isEmpty()) {
            md.append("\n\n| Layer | Captured |\n|---|---|\n");
            for (ChronicleLayerEntity l : layers) {
                md.append("| ").append(l.getName().replace("|", "\\|")).append(" | ").append(l.getRowsCaptured())
                        .append(" |\n");
            }
        }
        out.add(markdown("Summary", md.toString(), List.of(cref)));
        List<Map<String, Object>> highlights = new ArrayList<>();
        List<Map<String, Object>> caveats = new ArrayList<>();
        Set<String> skipped = Set.of(ChronicleVocabulary.ITEM_LAYER_EDIT, ChronicleVocabulary.ITEM_STATUS_CHANGE,
                ChronicleVocabulary.ITEM_CAP_WARNING, ChronicleVocabulary.ITEM_CAPTURE_GAP,
                ChronicleVocabulary.ITEM_BACKFILL);
        List<ChronicleEventEntity> events = chronicleEvents.findByChronicleIdOrderByIdAsc(c.getId());
        for (ChronicleEventEntity e : events) {
            if (e.isPrivateItem() && !caller.seesPrivate()) {
                continue;
            }
            if (ChronicleVocabulary.ITEM_CAPTURE_GAP.equals(e.getType())) {
                Map<String, Object> caveat = new LinkedHashMap<>();
                caveat.put("code", "CAPTURE_GAP");
                caveat.put("text", e.getTitle() + (e.getSummary() == null ? "" : ": " + e.getSummary()));
                if (e.getGap() != null) {
                    caveat.put("data", e.getGap());
                }
                caveats.add(caveat);
            }
            if (!skipped.contains(e.getType())) {
                highlights.add(timelineItem(e.getAt(), e.getTitle(), e.getRef() != null && ObjectRef.isValid(e.getRef())
                        ? e.getRef() : null));
            }
        }
        if (highlights.size() > MAX_TIMELINE) {
            highlights = new ArrayList<>(highlights.subList(highlights.size() - MAX_TIMELINE, highlights.size()));
        }
        out.add(timeline("Thread highlights", cref, highlights));
        Map<String, Object> areaGeometry = c.getArea() != null && c.getArea().get("geometry") instanceof Map<?, ?> g
                ? castMap(g) : null;
        layers.sort(Comparator.comparingLong(ChronicleLayerEntity::getRowsCaptured).reversed()
                .thenComparing(ChronicleLayerEntity::getId));
        for (ChronicleLayerEntity l : layers.subList(0, Math.min(MAJOR_LAYERS, layers.size()))) {
            List<String> products = new ArrayList<>();
            if (l.getSelector() != null && "raster-products".equals(l.getSelector().get("type"))) {
                for (Object p : ReportBlocks.asList(l.getSelector().get("productIds"))) {
                    if (products.size() < ReportBlocks.MAX_MAP_LAYERS) {
                        products.add(String.valueOf(p));
                    }
                }
            }
            out.add(map("Map: " + l.getName(), products, areaGeometry, null));
        }
        List<String> setRefs = prefixed(ObjectTypes.HYPOTHESIS_SET, c.getHypothesisSetIds());
        List<String> questionRefs = new ArrayList<>();
        for (String set : c.getHypothesisSetIds()) {
            questionRefs.addAll(linked("SELECT id FROM forecast_question WHERE hypothesis_set_id = ? ORDER BY"
                    + " created_at, id", set, ObjectTypes.QUESTION));
        }
        for (String sit : c.getSituationIds()) {
            questionRefs.addAll(linked("SELECT id FROM forecast_question WHERE situation_id = ? ORDER BY created_at,"
                    + " id", sit, ObjectTypes.QUESTION));
        }
        questionRefs = new ArrayList<>(new LinkedHashSet<>(questionRefs));
        if (questionRefs.size() > MAX_LINKED) {
            questionRefs = questionRefs.subList(0, MAX_LINKED);
        }
        linkedBlocks(out, setRefs, questionRefs);
        out.add(coverage(caveats));
        out.area = areaGeometry;
        if (c.getStartedAt() != null) {
            Map<String, Object> w = new LinkedHashMap<>();
            w.put("start", c.getStartedAt().toString());
            w.put("end", c.getStoppedAt() == null ? null : c.getStoppedAt().toString());
            out.window = w;
        }
        return out;
    }

    // ------------------------------------------------------------------------- tag, selection, workspace

    @SuppressWarnings("unchecked")
    private Built tag(String ref, ObjectCaller caller) {
        String id = ObjectRef.parse(ref).id();
        String name = tags.get(id, caller).getName();
        Map<String, Object> page = tags.objects(id, null, null, ReportVocabulary.MAX_SELECTION, caller);
        List<String> refs = new ArrayList<>();
        for (Object o : (List<Object>) page.get("items")) {
            if (o instanceof ObjectEnvelope e) {
                refs.add(e.ref());
            }
        }
        Built out = selection(refs, "Tag " + name, caller);
        out.title = "Tagged " + name;
        return out;
    }

    @SuppressWarnings("unchecked")
    private Built workspace(String ref, ObjectCaller caller) {
        WorkspaceEntity w = workspaces.get(ObjectRef.parse(ref).id(), caller);
        Map<String, Object> context = w.getContext() == null ? Map.of() : w.getContext();
        List<String> refs = new ArrayList<>();
        if (context.get("focusRef") instanceof String focus && ObjectRef.isValid(focus)) {
            refs.add(focus);
        }
        for (Object o : ReportBlocks.asList(context.get("selectedRefs"))) {
            if (o instanceof String s && ObjectRef.isValid(s) && !refs.contains(s)) {
                refs.add(s);
            }
        }
        for (String key : List.of("activeSituationId", "situationId")) {
            if (context.get(key) instanceof String sit && !refs.contains("situation:" + sit)
                    && ObjectRef.isValid("situation:" + sit)) {
                refs.add("situation:" + sit);
            }
        }
        Built out = selection(refs, "Workspace " + w.getName(), caller);
        out.title = "Workspace: " + w.getName();
        if (context.get("timeRange") instanceof Map<?, ?> range && range.get("start") instanceof String start) {
            Map<String, Object> window = new LinkedHashMap<>();
            window.put("start", start);
            window.put("end", range.get("end"));
            out.window = window;
        }
        return out;
    }

    private Built selection(List<String> refs, String label, ObjectCaller caller) {
        Built out = new Built(label);
        Map<String, ObjectEnvelope> visible = snapshots.envelopes(refs, caller);
        Map<String, List<String>> byType = new TreeMap<>();
        for (Map.Entry<String, ObjectEnvelope> e : visible.entrySet()) {
            byType.computeIfAbsent(e.getValue().type(), t -> new ArrayList<>()).add(e.getKey());
        }
        StringBuilder md = new StringBuilder();
        md.append(visible.size()).append(" objects");
        if (!byType.isEmpty()) {
            List<String> parts = new ArrayList<>();
            byType.forEach((t, list) -> parts.add(list.size() + " " + t));
            md.append(": ").append(String.join(", ", parts));
        }
        md.append('.');
        int hidden = refs.size() - visible.size();
        out.add(markdown("Summary", md.toString(), List.of()));
        for (Map.Entry<String, List<String>> e : byType.entrySet()) {
            List<String> list = e.getValue();
            out.add(objects(capitalize(e.getKey()), list.subList(0, Math.min(MAX_OBJECTS, list.size()))));
        }
        List<String> situationRefs = new ArrayList<>();
        try {
            List<ObjectRef> seeds = new ArrayList<>();
            for (String r : visible.keySet()) {
                seeds.add(ObjectRef.parse(r));
            }
            if (!seeds.isEmpty()) {
                Map<String, Object> graph = relations.graph(seeds, 1, Set.of(), 0, 500,
                        new ResolveContext(caller, null));
                StringBuilder rel = new StringBuilder();
                for (Object o : ReportBlocks.asList(graph.get("edges"))) {
                    if (o instanceof Map<?, ?> edge && visible.containsKey(String.valueOf(edge.get("fromRef")))
                            && visible.containsKey(String.valueOf(edge.get("toRef")))) {
                        rel.append("- `").append(edge.get("fromRef")).append("` ").append(edge.get("type"))
                                .append(" `").append(edge.get("toRef")).append('`');
                        if (edge.get("label") != null) {
                            rel.append(" (").append(edge.get("label")).append(')');
                        }
                        rel.append('\n');
                    }
                }
                if (!rel.isEmpty()) {
                    out.add(markdown("Relations among them", rel.toString(), List.of()));
                }
                for (Object o : ReportBlocks.asList(graph.get("nodes"))) {
                    String nodeRef = o instanceof ObjectEnvelope env ? env.ref()
                            : o instanceof Map<?, ?> m ? String.valueOf(m.get("ref")) : null;
                    if (nodeRef != null && nodeRef.startsWith("situation:") && !situationRefs.contains(nodeRef)) {
                        situationRefs.add(nodeRef);
                    }
                }
            }
        } catch (RuntimeException e) {
            // relations are context; a failure leaves them out
        }
        for (String r : visible.keySet()) {
            if (r.startsWith("situation:") && !situationRefs.contains(r)) {
                situationRefs.add(r);
            }
        }
        if (!situationRefs.isEmpty()) {
            out.add(objects("Related situations", situationRefs.subList(0, Math.min(MAX_LINKED,
                    situationRefs.size()))));
        }
        out.add(extentMap(new ArrayList<>(visible.keySet()), caller));
        List<Map<String, Object>> caveats = new ArrayList<>();
        if (hidden > 0) {
            Map<String, Object> caveat = new LinkedHashMap<>();
            caveat.put("code", "UNRESOLVED_REFERENCES");
            caveat.put("text", hidden + " selected references do not exist or are not visible to the author, so they"
                    + " are left out.");
            caveats.add(caveat);
        }
        out.add(coverage(caveats));
        return out;
    }

    private static String capitalize(String type) {
        return type.isEmpty() ? type : Character.toUpperCase(type.charAt(0)) + type.substring(1).replace('-', ' ')
                + "s";
    }

    // ------------------------------------------------------------------------- weekly assessment

    @SuppressWarnings("unchecked")
    private Built assessment(String assessmentId, Built base, ObjectCaller caller) {
        if (assessmentId == null) {
            throw ValidationException.forField("assessmentId", "is required for the weekly-assessment template");
        }
        AssessmentEntity a = assessments.get(assessmentId, caller.seesPrivate());
        Built out = new Built("Weekly assessment: " + (a.getHeadline() == null ? a.getPeriodEnd().toString()
                : a.getHeadline()));
        List<Map<String, Object>> judgments = new ArrayList<>();
        for (Map<String, Object> j : a.getBluf()) {
            String q = j.get("questionId") instanceof String s ? ObjectTypes.QUESTION + ":" + s : null;
            String set = j.get("hypothesisSetId") instanceof String s ? ObjectTypes.HYPOTHESIS_SET + ":" + s : null;
            Double p = j.get("probability") instanceof Number n && (q != null || set != null) ? n.doubleValue() : null;
            String confidence = j.get("confidence") instanceof String c && ReportBlocks.CONFIDENCE.contains(c) ? c
                    : null;
            judgments.add(ReportBlocks.judgment(String.valueOf(j.get("text")), p, confidence, q, set));
        }
        if (!judgments.isEmpty()) {
            out.add(block("bluf", "Key judgments", Map.of("judgments", judgments), ReportVocabulary.DETERMINISTIC));
        }
        for (Map<String, Object> section : a.getSections()) {
            String origin = "llm".equals(section.get("textOrigin")) ? ReportVocabulary.LLM
                    : ReportVocabulary.DETERMINISTIC;
            String text = section.get("markdown") == null ? "" : String.valueOf(section.get("markdown"));
            if (text.isBlank()) {
                continue;
            }
            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put("markdown", text.length() > 100_000 ? text.substring(0, 100_000) : text);
            fields.put("citations", List.of());
            out.add(block("markdown", section.get("title") == null ? null : String.valueOf(section.get("title")),
                    fields, origin));
        }
        List<String> cited = new ArrayList<>();
        cited.add("assessment:" + a.getId());
        for (String c : a.getCitations() == null ? List.<String>of() : a.getCitations()) {
            if (ObjectRef.isValid(c) && cited.size() < MAX_OBJECTS) {
                cited.add(c);
            }
        }
        out.add(objects("Assessment and citations", cited));
        for (Map<String, Object> b : base.blocks) {
            if ("coverage".equals(b.get("type")) || "map".equals(b.get("type"))) {
                out.add(b);
            }
        }
        Map<String, Object> w = new LinkedHashMap<>();
        w.put("start", a.getPeriodStart().toString());
        w.put("end", a.getPeriodEnd().toString());
        out.window = w;
        out.area = base.area;
        return out;
    }

    // ------------------------------------------------------------------------- shared blocks

    private Map<String, Object> bluf(List<String> setRefs, List<String> questionRefs, ObjectCaller caller) {
        List<Map<String, Object>> judgments = new ArrayList<>();
        for (String setRef : setRefs) {
            HypothesisSetEntity set = snapshots.setEntity(ObjectRef.parse(setRef).id(), caller).orElse(null);
            if (set == null) {
                continue;
            }
            HypothesisEntity leading = null;
            for (HypothesisEntity h : snapshots.hypothesesOf(set.getId())) {
                if (leading == null || h.getProbability() > leading.getProbability()) {
                    leading = h;
                }
            }
            if (leading != null) {
                String confidence = set.getConfidence() != null && ReportBlocks.CONFIDENCE.contains(set.getConfidence())
                        ? set.getConfidence() : null;
                judgments.add(ReportBlocks.judgment(set.getQuestion() + " Leading hypothesis " + leading.getLabel()
                        + ": " + leading.getStatement(), leading.getProbability(), confidence, null, setRef));
            }
        }
        for (String qRef : questionRefs) {
            ForecastQuestionEntity q = snapshots.questionEntity(ObjectRef.parse(qRef).id(), caller).orElse(null);
            if (q == null) {
                continue;
            }
            Double p = q.getEnsembleProbability();
            judgments.add(ReportBlocks.judgment(q.getTitle() + (p == null ? " (no ensemble forecast yet)" : ""), p,
                    null, qRef, null));
        }
        if (judgments.isEmpty()) {
            return null;
        }
        return block("bluf", "Key judgments", Map.of("judgments", judgments), ReportVocabulary.DETERMINISTIC);
    }

    private void linkedBlocks(Built out, List<String> setRefs, List<String> questionRefs) {
        for (String set : setRefs.subList(0, Math.min(MAX_LINKED, setRefs.size()))) {
            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put("hypothesisSetRef", set);
            fields.put("snapshot", null);
            fields.put("revisionIds", List.of());
            out.add(block("hypotheses", null, fields, ReportVocabulary.DETERMINISTIC));
        }
        if (!questionRefs.isEmpty()) {
            List<Map<String, Object>> questions = new ArrayList<>();
            for (String q : questionRefs.subList(0, Math.min(MAX_LINKED, questionRefs.size()))) {
                questions.add(new LinkedHashMap<>(Map.of("questionRef", q)));
            }
            out.add(block("forecasts", null, Map.of("questions", questions), ReportVocabulary.DETERMINISTIC));
        }
    }

    private Map<String, Object> extentMap(List<String> refs, ObjectCaller caller) {
        double[] box = {Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
        for (ObjectEnvelope e : snapshots.envelopes(refs, caller).values()) {
            if (e.bbox() != null && e.bbox().size() == 4) {
                box[0] = Math.min(box[0], e.bbox().get(0));
                box[1] = Math.min(box[1], e.bbox().get(1));
                box[2] = Math.max(box[2], e.bbox().get(2));
                box[3] = Math.max(box[3], e.bbox().get(3));
            } else if (e.geometry() != null) {
                double[] g = ReportPublisher.envelope(e.geometry());
                if (g != null) {
                    box[0] = Math.min(box[0], g[0]);
                    box[1] = Math.min(box[1], g[1]);
                    box[2] = Math.max(box[2], g[2]);
                    box[3] = Math.max(box[3], g[3]);
                }
            }
        }
        if (box[0] > box[2] || box[1] > box[3]) {
            return null;
        }
        if (box[0] == box[2]) {
            box[0] = Math.max(-180, box[0] - 0.25);
            box[2] = Math.min(180, box[2] + 0.25);
        }
        if (box[1] == box[3]) {
            box[1] = Math.max(-90, box[1] - 0.25);
            box[3] = Math.min(90, box[3] + 0.25);
        }
        return map("Map", List.of(), null, List.of(box[0], box[1], box[2], box[3]));
    }

    private List<String> linked(String sql, String id, String type) {
        List<String> out = new ArrayList<>();
        try {
            for (String found : jdbc.queryForList(sql, String.class, id)) {
                if (out.size() < MAX_LINKED) {
                    out.add(type + ":" + found);
                }
            }
        } catch (RuntimeException e) {
            // a missing table on a trimmed instance leaves the links out
        }
        return out;
    }

    private static List<String> prefixed(String type, List<String> ids) {
        List<String> out = new ArrayList<>();
        if (ids != null) {
            for (String id : ids) {
                if (ObjectRef.isValid(type + ":" + id) && out.size() < MAX_LINKED) {
                    out.add(type + ":" + id);
                }
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object o) {
        return (Map<String, Object>) o;
    }

    /** A deterministic block of {@code type} with {@code fields}. */
    static Map<String, Object> block(String type, String title, Map<String, Object> fields, String origin) {
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("type", type);
        b.put("title", title);
        b.put("textOrigin", origin);
        b.putAll(fields);
        return b;
    }

    static Map<String, Object> markdown(String title, String text, List<String> citations) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("markdown", text);
        fields.put("citations", citations);
        return block("markdown", title, fields, ReportVocabulary.DETERMINISTIC);
    }

    static Map<String, Object> objects(String title, List<String> refs) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (String r : refs) {
            items.add(new LinkedHashMap<>(Map.of("ref", r)));
        }
        return block("objects", title, Map.of("items", items), ReportVocabulary.DETERMINISTIC);
    }

    static Map<String, Object> timeline(String title, String sourceRef, List<Map<String, Object>> items) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("sourceRef", sourceRef);
        fields.put("items", items);
        return block("timeline", title, fields, ReportVocabulary.DETERMINISTIC);
    }

    static Map<String, Object> timelineItem(Instant at, String title, String ref) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("at", at == null ? null : at.toString());
        String t = title == null || title.isBlank() ? "(untitled)" : title;
        item.put("title", t.length() > 500 ? t.substring(0, 499) + "…" : t);
        item.put("ref", ref);
        return item;
    }

    static Map<String, Object> map(String title, List<String> layers, Map<String, Object> area, List<Double> bbox) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("layers", layers);
        fields.put("area", area);
        fields.put("bbox", bbox);
        fields.put("timeRange", null);
        fields.put("attachmentId", null);
        fields.put("mediaId", null);
        return block("map", title, fields, ReportVocabulary.DETERMINISTIC);
    }

    static Map<String, Object> evidence(List<String> ids) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (String id : ids) {
            items.add(new LinkedHashMap<>(Map.of("evidenceItemId", id)));
        }
        return block("evidence", null, Map.of("items", items), ReportVocabulary.DETERMINISTIC);
    }

    static Map<String, Object> coverage(List<Map<String, Object>> caveats) {
        List<Map<String, Object>> all = new ArrayList<>(caveats);
        Map<String, Object> media = new LinkedHashMap<>();
        media.put("code", "MEDIA_COVERAGE_UNAVAILABLE");
        media.put("text", "Media-coverage statistics (prompt 133) are not part of this draft; absence of reporting is"
                + " not evidence of absence.");
        all.add(media);
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("caveats", all);
        fields.put("coverage", List.of());
        return block("coverage", null, fields, ReportVocabulary.DETERMINISTIC);
    }

    /** The drafting clock (tests). */
    Instant now() {
        return clock.instant();
    }
}
