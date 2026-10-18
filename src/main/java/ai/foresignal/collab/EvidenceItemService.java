package ai.foresignal.collab;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.evidence.Originator;
import ai.foresignal.evidence.OriginatorRegistry;
import ai.foresignal.intel.InvestigationItemEntity;
import ai.foresignal.intel.InvestigationItemRepository;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.search.DocInput;
import ai.foresignal.search.EmbeddingStore;
import ai.foresignal.search.EmbeddingWorker;
import ai.foresignal.security.PrincipalEntity;
import org.springframework.beans.factory.ObjectProvider;
import ai.foresignal.security.PrincipalRepository;
import ai.foresignal.security.Tlp;
import ai.foresignal.security.TlpTarget;
import ai.foresignal.security.Ulids;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.source.formats.geojson.GeoJsonWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Limit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * Evidence items (prompt 130): user-contributed evidence with attachments and an optional link (metadata only, never
 * fetched), graded with the Admiralty system (source reliability {@code A}–{@code F}, information credibility
 * {@code 1}–{@code 6}).
 *
 * <ul>
 *   <li>An evidence item inherits the strictest TLP label of its attachments (COL-2); asking for a weaker label is
 *       {@code 403 TLP_VIOLATION}.</li>
 *   <li>Its originator for 116 independence is {@code operator:{handle}} of its creator, unless it cites a cataloged
 *       source, whose 116 originator it then is.</li>
 *   <li>Items labeled {@code CLEAR} or {@code GREEN} are embedded (027) with their attachments' text for semantic
 *       search; stricter labels are never sent to an embedding provider and match by text only.</li>
 *   <li>It can be an investigation item, ACH evidence ({@code source=evidence-item}) and a relation endpoint.</li>
 * </ul>
 */
@Service
public class EvidenceItemService {

    private static final Logger log = LoggerFactory.getLogger(EvidenceItemService.class);

    /** Admiralty source reliability grades. */
    public static final List<String> RELIABILITY = List.of("A", "B", "C", "D", "E", "F");

    private static final Set<String> INPUT = Set.of("title", "description", "attachmentIds", "url",
            "sourceDescription", "sourceReliability", "informationCredibility", "citedSourceId", "citedRefs",
            "geometry", "eventTime", "tagIds", "visibility", "tlp");
    private static final Set<String> PATCH = Set.of("title", "description", "attachmentIds", "url",
            "sourceDescription", "sourceReliability", "informationCredibility", "citedSourceId", "citedRefs",
            "geometry", "eventTime", "visibility", "tlp");

    private final EvidenceItemRepository items;
    private final AttachmentRepository attachmentRows;
    private final AttachmentService attachments;
    private final InvestigationItemRepository investigationItems;
    private final AccessResolver access;
    private final ShareService shares;
    private final TagService tags;
    private final TagAssignmentRepository assignments;
    private final SourceRegistry sources;
    private final OriginatorRegistry originators;
    private final PrincipalRepository principals;
    private final PrincipalViews views;
    private final ObjectProvider<EmbeddingWorker> embeddings;
    private final EmbeddingStore embeddingStore;
    private final Clock clock;
    private final GeoJsonReader geoReader = new GeoJsonReader();
    private final GeoJsonWriter geoWriter = new GeoJsonWriter();

    /** Creates the service. */
    public EvidenceItemService(EvidenceItemRepository items, AttachmentRepository attachmentRows,
            AttachmentService attachments, InvestigationItemRepository investigationItems, AccessResolver access,
            ShareService shares, TagService tags, TagAssignmentRepository assignments, SourceRegistry sources,
            OriginatorRegistry originators, PrincipalRepository principals, PrincipalViews views,
            ObjectProvider<EmbeddingWorker> embeddings, EmbeddingStore embeddingStore, Clock clock) {
        this.items = items;
        this.attachmentRows = attachmentRows;
        this.attachments = attachments;
        this.investigationItems = investigationItems;
        this.access = access;
        this.shares = shares;
        this.tags = tags;
        this.assignments = assignments;
        this.sources = sources;
        this.originators = originators;
        this.principals = principals;
        this.views = views;
        this.embeddings = embeddings;
        this.embeddingStore = embeddingStore;
        this.clock = clock;
    }

    /** The reference of an evidence item. */
    public static String ref(String id) {
        return ObjectTypes.EVIDENCE_ITEM + ":" + id;
    }

    // ------------------------------------------------------------------------- reads

    /**
     * The evidence item {@code id} when the caller may see it.
     *
     * @throws NotFoundException {@code UNKNOWN_EVIDENCE_ITEM} otherwise
     */
    @Transactional
    public EvidenceItemEntity get(String id, ObjectCaller caller) {
        EvidenceItemEntity e = id == null ? null : items.findById(id).orElse(null);
        if (e == null || access.role(caller, ref(e.getId())) == null) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_EVIDENCE_ITEM, "No evidence item '" + id
                    + "' exists, or you may not see it.");
        }
        return e;
    }

    /**
     * Evidence items the caller sees, newest first ({@code listEvidenceItems}).
     *
     * @return the {@code EvidenceItemPage}
     */
    @Transactional
    public Map<String, Object> list(ObjectCaller caller, String q, String reliability, String tagId,
            String investigationId, String cursor, int limit) {
        if (reliability != null && !RELIABILITY.contains(reliability)) {
            throw ValidationException.forField("sourceReliability", "must be A to F");
        }
        Set<String> tagged = null;
        if (tagId != null && !tagId.isBlank()) {
            TagEntity tag = tags.get(tagId, caller);
            tagged = new java.util.HashSet<>();
            for (TagAssignmentEntity a : assignments.findByTagId(tag.getId())) {
                tagged.add(a.getObjectRef());
            }
        }
        Set<String> inInvestigation = null;
        if (investigationId != null && !investigationId.isBlank()) {
            access.require(caller, ObjectTypes.INVESTIGATION + ":" + investigationId, AccessAction.VIEW);
            String canonical = access.record(ObjectTypes.INVESTIGATION + ":" + investigationId).orElseThrow()
                    .getObjectRef();
            inInvestigation = new java.util.HashSet<>();
            for (InvestigationItemEntity item : investigationItems.findByInvestigationIdOrderByIdAsc(
                    canonical.substring(canonical.indexOf(':') + 1))) {
                if ("evidence-item".equals(item.getItemType())) {
                    inInvestigation.add(item.getRef().startsWith("evidence-item:") ? item.getRef()
                            : ref(item.getRef()));
                }
            }
        }
        String needle = q == null || q.isBlank() ? null : q.strip().toLowerCase(Locale.ROOT);
        String before = cursor == null || cursor.isBlank() ? "~" : cursor.strip();
        List<Map<String, Object>> out = new ArrayList<>();
        String last = null;
        boolean more = false;
        while (true) {
            List<EvidenceItemEntity> page = items.findByIdLessThanOrderByIdDesc(before, Limit.of(200));
            Map<String, AclRole> roles = access.roles(caller, page.stream().map(e -> ref(e.getId())).toList());
            for (EvidenceItemEntity e : page) {
                before = e.getId();
                String r = ref(e.getId());
                if (roles.get(r) == null || (reliability != null && !reliability.equals(e.getSourceReliability()))
                        || (tagged != null && !tagged.contains(r))
                        || (inInvestigation != null && !inInvestigation.contains(r))
                        || (needle != null && !matches(e, needle))) {
                    continue;
                }
                if (out.size() == limit) {
                    more = true;
                    break;
                }
                out.add(view(e, caller));
                last = e.getId();
            }
            if (more || page.size() < 200) {
                break;
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", out);
        result.put("nextCursor", more ? last : null);
        return result;
    }

    private static boolean matches(EvidenceItemEntity e, String needle) {
        return e.getTitle().toLowerCase(Locale.ROOT).contains(needle)
                || (e.getDescription() != null && e.getDescription().toLowerCase(Locale.ROOT).contains(needle))
                || (e.getSourceDescription() != null
                        && e.getSourceDescription().toLowerCase(Locale.ROOT).contains(needle));
    }

    /**
     * ACH evidence citing an evidence item (125, {@code source=evidence-item}): the draft with its reference made
     * canonical ({@code evidence-item:{id}}), when the caller can see the item.
     *
     * @throws NotFoundException {@code UNKNOWN_EVIDENCE_ITEM} otherwise
     */
    public ai.foresignal.hypothesis.HypothesisInputs.EvidenceDraft citable(
            ai.foresignal.hypothesis.HypothesisInputs.EvidenceDraft draft, ObjectCaller caller) {
        if (draft == null || !"evidence-item".equals(draft.source()) || draft.ref() == null) {
            return draft;
        }
        String id = draft.ref().startsWith("evidence-item:") ? draft.ref().substring("evidence-item:".length())
                : draft.ref();
        EvidenceItemEntity e = get(id, caller);
        return new ai.foresignal.hypothesis.HypothesisInputs.EvidenceDraft(draft.source(), ref(e.getId()),
                draft.description(), draft.credibility(), draft.relevance(), draft.ratings(), draft.citations());
    }

    /** The originator of an evidence item for 116 independence. */
    public Originator originator(EvidenceItemEntity e) {
        if (e.getCitedSourceId() != null && sources.find(e.getCitedSourceId()).isPresent()) {
            return originators.resolve(e.getCitedSourceId());
        }
        String handle = principals.findById(e.getCreatedBy()).map(PrincipalEntity::getHandle)
                .orElse(e.getCreatedBy());
        return new Originator("operator:" + handle, "operator", null, List.of(),
                "user-contributed evidence item " + e.getId(), List.of());
    }

    // ------------------------------------------------------------------------- writes

    /** Creates an evidence item ({@code createEvidenceItem}). */
    @Transactional
    public EvidenceItemEntity create(JsonNode body, ObjectCaller caller) {
        CollabInputs.object(body, "EvidenceItemInput", INPUT);
        Instant now = clock.instant();
        EvidenceItemEntity e = new EvidenceItemEntity(Ulids.next(now), CollabInputs.required(body, "title", 1, 300)
                .strip(), caller.principal(), now);
        apply(e, body, caller);
        List<String> tagIds = CollabInputs.strings(body, "tagIds", 50);
        List<TagEntity> toTag = new ArrayList<>();
        for (String id : tagIds) {
            toTag.add(tags.get(id, caller));
        }
        Tlp inherited = inheritedTlp(e.getAttachmentIds());
        Tlp requested = body.hasNonNull("tlp") ? Tlp.parse("tlp", CollabInputs.text(body, "tlp", 1, 16), null) : null;
        Tlp tlp = labelFor(requested, inherited);
        Visibility visibility = Visibility.parse("visibility", CollabInputs.text(body, "visibility", 1, 16),
                Visibility.PRIVATE);
        shares.checkTlp(tlp, caller.principal(), List.of(), visibility);
        items.save(e);
        access.register(ref(e.getId()), caller.principal(), visibility, tlp);
        for (TagEntity t : toTag) {
            tags.bulk(t.getId(), List.of(ref(e.getId())), true, caller);
        }
        embed(e, tlp);
        return e;
    }

    /** Applies a JSON merge patch ({@code updateEvidenceItem}; owner or editor). */
    @Transactional
    public EvidenceItemEntity patch(String id, JsonNode body, ObjectCaller caller) {
        EvidenceItemEntity e = get(id, caller);
        ShareableObjectEntity rec = access.require(caller, ref(e.getId()), AccessAction.EDIT);
        CollabInputs.object(body, "EvidenceItemPatch", PATCH);
        if (body.has("title")) {
            e.setTitle(CollabInputs.required(body, "title", 1, 300).strip());
        }
        apply(e, body, caller);
        Tlp inherited = inheritedTlp(e.getAttachmentIds());
        Tlp requested = body.hasNonNull("tlp") ? Tlp.parse("tlp", CollabInputs.text(body, "tlp", 1, 16), null)
                : Tlp.fromWire(rec.getTlp());
        Tlp tlp = labelFor(requested, inherited);
        Visibility visibility = body.hasNonNull("visibility")
                ? Visibility.parse("visibility", CollabInputs.text(body, "visibility", 1, 16), null)
                : rec.getVisibility();
        List<String> grantees = new ArrayList<>();
        shares.entries(rec.getObjectRef(), PrincipalViews.cache()).forEach(g -> grantees.add(String.valueOf(
                g.get("grantee"))));
        shares.checkTlp(tlp, rec.getOwner(), grantees, visibility);
        rec.setTlp(tlp.wireValue());
        rec.setVisibility(visibility);
        access.save(rec);
        e.setUpdatedAt(clock.instant());
        items.save(e);
        embed(e, tlp);
        return e;
    }

    /** Deletes an evidence item ({@code deleteEvidenceItem}; owner only). */
    @Transactional
    public void delete(String id, ObjectCaller caller) {
        EvidenceItemEntity e = get(id, caller);
        access.require(caller, ref(e.getId()), AccessAction.DELETE);
        items.delete(e);
        assignments.deleteByObjectRef(ref(e.getId()));
        access.forget(ref(e.getId()));
        try {
            embeddingStore.delete(DocInput.EVIDENCE_ITEM, e.getId(), null);
        } catch (RuntimeException ex) {
            log.warn("Could not drop the embedding of evidence item {}: {}", e.getId(), ex.toString());
        }
    }

    /** The {@code EvidenceItem} view. */
    @Transactional
    public Map<String, Object> view(EvidenceItemEntity e, ObjectCaller caller) {
        ShareableObjectEntity rec = access.record(ref(e.getId())).orElse(null);
        Map<String, Map<String, Object>> cache = PrincipalViews.cache();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", e.getId());
        out.put("ref", ref(e.getId()));
        out.put("title", e.getTitle());
        out.put("description", e.getDescription());
        out.put("attachmentIds", e.getAttachmentIds());
        out.put("url", e.getUrl());
        out.put("sourceDescription", e.getSourceDescription());
        out.put("sourceReliability", e.getSourceReliability());
        out.put("informationCredibility", e.getInformationCredibility());
        out.put("grade", e.grade());
        out.put("citedSourceId", e.getCitedSourceId());
        out.put("originator", originator(e).id());
        out.put("citedRefs", e.getCitedRefs());
        out.put("provenanceChain", List.of());
        out.put("geometry", e.getGeom() == null ? null : geoWriter.write(e.getGeom()));
        out.put("eventTime", e.getEventTime());
        out.put("tags", tags.namesOn(ref(e.getId()), caller));
        out.put("owner", views.summary(rec == null ? e.getCreatedBy() : rec.getOwner(), cache));
        out.put("createdBy", views.summary(e.getCreatedBy(), cache));
        out.put("visibility", rec == null ? Visibility.PRIVATE.wireValue() : rec.getVisibility().wireValue());
        out.put("tlp", rec == null ? access.defaultTlp().wireValue() : rec.getTlp());
        List<Map<String, Object>> caveats = new ArrayList<>();
        if (e.getUrl() != null) {
            caveats.add(Map.of("code", "LINK_NOT_FETCHED", "text",
                    "The link is stored as metadata only; Foresignal never fetched it."));
        }
        if ("F".equals(e.getSourceReliability()) || e.getInformationCredibility() == 6) {
            caveats.add(Map.of("code", "UNGRADED", "text", "Reliability or credibility cannot be judged."));
        }
        out.put("caveats", caveats);
        out.put("createdAt", e.getCreatedAt());
        out.put("updatedAt", e.getUpdatedAt());
        return out;
    }

    // ------------------------------------------------------------------------- helpers

    private void apply(EvidenceItemEntity e, JsonNode body, ObjectCaller caller) {
        if (body.has("description")) {
            e.setDescription(CollabInputs.text(body, "description", 0, 20_000));
        }
        if (body.has("attachmentIds")) {
            List<String> ids = CollabInputs.strings(body, "attachmentIds", 50);
            for (String id : ids) {
                attachments.get(id, caller);
            }
            e.setAttachmentIds(ids);
        }
        if (body.has("url")) {
            e.setUrl(url(CollabInputs.text(body, "url", 1, 2048)));
        }
        if (body.has("sourceDescription")) {
            e.setSourceDescription(CollabInputs.text(body, "sourceDescription", 0, 2000));
        }
        if (body.hasNonNull("sourceReliability")) {
            String r = CollabInputs.text(body, "sourceReliability", 1, 1);
            if (!RELIABILITY.contains(r)) {
                throw ValidationException.forField("sourceReliability", "must be A to F (Admiralty)");
            }
            e.setSourceReliability(r);
        }
        if (body.hasNonNull("informationCredibility")) {
            JsonNode n = body.get("informationCredibility");
            if (!n.isIntegralNumber() || n.asInt() < 1 || n.asInt() > 6) {
                throw ValidationException.forField("informationCredibility", "must be 1 to 6 (Admiralty)");
            }
            e.setInformationCredibility(n.asInt());
        }
        if (body.has("citedSourceId")) {
            String source = CollabInputs.text(body, "citedSourceId", 1, 64);
            if (source != null && sources.find(source).isEmpty()) {
                throw ValidationException.forField("citedSourceId", "names no cataloged source");
            }
            e.setCitedSourceId(source);
        }
        if (body.has("citedRefs")) {
            List<String> refs = CollabInputs.strings(body, "citedRefs", 50);
            for (int i = 0; i < refs.size(); i++) {
                ObjectRef.parse(refs.get(i), "citedRefs[" + i + "]");
            }
            e.setCitedRefs(refs);
        }
        if (body.has("geometry")) {
            JsonNode g = body.get("geometry");
            try {
                e.setGeom(g == null || g.isNull() ? null : geoReader.read(g));
            } catch (InvalidInputException ex) {
                throw ValidationException.forField("geometry", ex.getMessage());
            }
        }
        if (body.has("eventTime")) {
            e.setEventTime(CollabInputs.instant(body, "eventTime"));
        }
    }

    private static String url(String value) {
        if (value == null) {
            return null;
        }
        try {
            URI uri = new URI(value.strip());
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if (!scheme.equals("http") && !scheme.equals("https")) {
                throw ValidationException.forField("url", "must be an http or https URL");
            }
            return uri.toString();
        } catch (URISyntaxException ex) {
            throw ValidationException.forField("url", "must be a URL");
        }
    }

    /** The strictest label among the attachments, or null without attachments. */
    private Tlp inheritedTlp(List<String> attachmentIds) {
        List<Tlp> labels = new ArrayList<>();
        for (String id : attachmentIds) {
            labels.add(access.tlpOf(AttachmentService.ref(id)));
        }
        return labels.isEmpty() ? null : Tlp.strictest(labels, null);
    }

    /** The label of a derived product: the requested one, never weaker than its inputs (COL-2). */
    private Tlp labelFor(Tlp requested, Tlp inherited) {
        if (requested == null) {
            return inherited == null ? access.defaultTlp() : Tlp.stricter(access.defaultTlp(), inherited);
        }
        if (inherited != null && inherited.stricterThan(requested)) {
            ShareService.enforce(new TlpTarget.Decision(false, "a derived product inherits the strictest label of"
                    + " its inputs: an attachment is " + inherited.wireValue()));
        }
        return requested;
    }

    /** Embeds a CLEAR or GREEN item with its attachments' text (027); stricter labels are removed from the index. */
    private void embed(EvidenceItemEntity e, Tlp tlp) {
        try {
            if (tlp != Tlp.CLEAR && tlp != Tlp.GREEN) {
                embeddingStore.delete(DocInput.EVIDENCE_ITEM, e.getId(), null);
                return;
            }
            StringBuilder text = new StringBuilder(e.getTitle());
            if (e.getDescription() != null) {
                text.append("\n\n").append(e.getDescription());
            }
            if (e.getSourceDescription() != null) {
                text.append("\n\nSource: ").append(e.getSourceDescription());
            }
            for (AttachmentEntity a : attachmentRows.findAllById(e.getAttachmentIds())) {
                if (a.getText() != null) {
                    text.append("\n\n").append(a.getFilename()).append(":\n").append(a.getText());
                }
            }
            EmbeddingWorker worker = embeddings.getIfAvailable();
            if (worker != null) {
                worker.embedDocument(DocInput.EVIDENCE_ITEM, e.getId(), text.toString(), e.getGeom(),
                        e.getEventTime() == null ? e.getCreatedAt() : e.getEventTime(), null, null);
            }
        } catch (RuntimeException ex) {
            // A missing embedding provider or an exhausted budget leaves the item text-searchable only.
            log.info("Evidence item {} was not embedded: {}", e.getId(), ex.toString());
        }
    }

    /** {@code 403} for callers who are not the owner or an editor. */
    static ForesignalException forbidden(String detail) {
        return new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN, detail);
    }
}
