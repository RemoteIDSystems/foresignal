package ai.foresignal.report;

import ai.foresignal.collab.AttachmentEntity;
import ai.foresignal.collab.AttachmentService;
import ai.foresignal.collab.EvidenceItemEntity;
import ai.foresignal.collab.EvidenceItemService;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.hypothesis.HypothesisRevisionEntity;
import ai.foresignal.hypothesis.HypothesisService;
import ai.foresignal.hypothesis.HypothesisSetEntity;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectService;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.security.Tlp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Resolves what report blocks cite (prompt 132): envelopes through the 129 object service (so access lists, TLP and
 * private sources apply exactly as everywhere else), pinned references ({@code @seq} for records, from the envelope's
 * {@code asOfSeq}), and the snapshots of hypothesis sets (with their newest revisions), ledger questions (ensemble,
 * forecasters and scores) and evidence items (with their Admiralty grade). A reference the caller cannot see is
 * reported as missing, never resolved.
 */
@Component
public class ReportSnapshots {

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    /** Revisions kept in a hypothesis snapshot. */
    static final int REVISIONS = 10;

    private final ObjectService objects;
    private final HypothesisService hypotheses;
    private final ForecastService forecasts;
    private final EvidenceItemService evidence;
    private final AttachmentService attachments;
    private final JsonMapper json;

    /** Creates the component. */
    public ReportSnapshots(ObjectService objects, HypothesisService hypotheses, ForecastService forecasts,
            EvidenceItemService evidence, AttachmentService attachments, JsonMapper json) {
        this.objects = objects;
        this.hypotheses = hypotheses;
        this.forecasts = forecasts;
        this.evidence = evidence;
        this.attachments = attachments;
        this.json = json;
    }

    /** The attachment {@code id} when the caller may see it, or empty. */
    public Optional<AttachmentEntity> attachment(String id, ObjectCaller caller) {
        try {
            return Optional.of(attachments.get(id, caller));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /** The stored bytes of an attachment. */
    public byte[] attachmentContent(AttachmentEntity a) {
        return attachments.content(a);
    }

    /**
     * What a fill collected: the pinned references, the envelope snapshots by pinned reference, the other snapshots
     * (hypothesis sets, questions, evidence items) by reference, the attachments by id, the references that could not
     * be resolved, the contributing sources and the TLP labels of the inputs.
     */
    public static final class Collected {

        /** Pinned references, in citation order. */
        public final java.util.LinkedHashSet<String> pinnedRefs = new java.util.LinkedHashSet<>();

        /** Envelope snapshots by pinned reference. */
        public final Map<String, Map<String, Object>> envelopes = new LinkedHashMap<>();

        /** Snapshots by reference (hypothesis sets, questions, evidence items). */
        public final Map<String, Object> snapshots = new LinkedHashMap<>();

        /** Attachments by id. */
        public final Map<String, AttachmentEntity> attachments = new LinkedHashMap<>();

        /** References that are missing or that the caller may not see. */
        public final java.util.LinkedHashSet<String> missing = new java.util.LinkedHashSet<>();

        /** The sources of the cited envelopes. */
        public final java.util.TreeSet<String> sourceIds = new java.util.TreeSet<>();

        /** The TLP labels of the inputs. */
        public final List<Tlp> tlps = new ArrayList<>();

        /** True when any cited envelope touches a private source. */
        public boolean anyPrivate;

        void envelope(String pinned, Map<String, Object> envelope) {
            pinnedRefs.add(pinned);
            envelopes.put(pinned, envelope);
            Tlp t = tlp(envelope);
            if (t != null) {
                tlps.add(t);
            }
            if (Boolean.TRUE.equals(envelope.get("private"))) {
                anyPrivate = true;
            }
            Object prov = envelope.get("provenance");
            if (prov instanceof Map<?, ?> m && m.get("sourceIds") instanceof List<?> ids) {
                for (Object id : ids) {
                    if (id != null) {
                        sourceIds.add(String.valueOf(id));
                    }
                }
            }
        }
    }

    /**
     * Resolves what a block cites for the caller and returns the block with its envelopes and snapshots filled.
     *
     * @param block the stored block
     * @param caller who resolves (the editor while drafting, the publisher when publishing)
     * @param pin true to pin references ({@code @seq} for records), as publishing does
     * @param out what was collected (may be shared across blocks)
     * @return a new block map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> fill(Map<String, Object> block, ObjectCaller caller, boolean pin, Collected out) {
        Map<String, Object> b = new LinkedHashMap<>(block);
        String type = String.valueOf(b.get("type"));
        switch (type) {
            case "objects" -> {
                List<Map<String, Object>> items = new ArrayList<>();
                for (Object o : ReportBlocks.asList(b.get("items"))) {
                    if (!(o instanceof Map<?, ?> m) || !(m.get("ref") instanceof String ref)) {
                        continue;
                    }
                    Optional<Pinned> p = pin(ref, caller);
                    if (p.isEmpty()) {
                        out.missing.add(ref);
                        if (!pin) {
                            Map<String, Object> item = new LinkedHashMap<>();
                            item.put("ref", ref);
                            item.put("envelope", null);
                            items.add(item);
                        }
                        continue;
                    }
                    String key = pin ? p.get().ref() : ref;
                    out.envelope(key, p.get().envelope());
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("ref", key);
                    item.put("envelope", p.get().envelope());
                    items.add(item);
                }
                b.put("items", items);
            }
            case "timeline" -> {
                List<Map<String, Object>> items = new ArrayList<>();
                for (Object o : ReportBlocks.asList(b.get("items"))) {
                    if (!(o instanceof Map<?, ?> m)) {
                        continue;
                    }
                    Map<String, Object> item = new LinkedHashMap<>((Map<String, Object>) m);
                    if (item.get("ref") instanceof String ref) {
                        Optional<Pinned> p = pin(ref, caller);
                        if (p.isEmpty()) {
                            out.missing.add(ref);
                            if (pin) {
                                continue;
                            }
                            item.put("envelope", null);
                        } else {
                            String key = pin ? p.get().ref() : ref;
                            out.envelope(key, p.get().envelope());
                            item.put("ref", key);
                            item.put("envelope", p.get().envelope());
                        }
                    } else {
                        item.put("envelope", null);
                    }
                    items.add(item);
                }
                b.put("items", items);
                if (b.get("sourceRef") instanceof String src) {
                    pin(src, caller).ifPresentOrElse(p -> out.envelope(pin ? p.ref() : src, p.envelope()),
                            () -> out.missing.add(src));
                }
            }
            case "hypotheses" -> {
                String setRef = String.valueOf(b.get("hypothesisSetRef"));
                Optional<Map<String, Object>> snap = hypothesisSet(setRef, caller);
                if (snap.isPresent()) {
                    b.put("snapshot", snap.get().get("snapshot"));
                    b.put("revisionIds", snap.get().get("revisionIds"));
                    out.snapshots.put(setRef, snap.get().get("snapshot"));
                    out.pinnedRefs.add(setRef);
                    envelope(setRef, caller).ifPresent(e -> out.envelope(setRef, e));
                } else {
                    out.missing.add(setRef);
                    b.put("snapshot", null);
                    b.put("revisionIds", List.of());
                }
            }
            case "forecasts" -> {
                List<Map<String, Object>> questions = new ArrayList<>();
                for (Object o : ReportBlocks.asList(b.get("questions"))) {
                    if (!(o instanceof Map<?, ?> m) || !(m.get("questionRef") instanceof String ref)) {
                        continue;
                    }
                    Optional<Map<String, Object>> snap = question(ref, caller);
                    if (snap.isEmpty()) {
                        out.missing.add(ref);
                        if (pin) {
                            continue;
                        }
                    } else {
                        out.snapshots.put(ref, snap.get());
                        out.pinnedRefs.add(ref);
                        envelope(ref, caller).ifPresent(e -> out.envelope(ref, e));
                    }
                    Map<String, Object> q = new LinkedHashMap<>();
                    q.put("questionRef", ref);
                    q.put("snapshot", snap.orElse(null));
                    questions.add(q);
                }
                b.put("questions", questions);
            }
            case "evidence" -> {
                List<Map<String, Object>> items = new ArrayList<>();
                for (Object o : ReportBlocks.asList(b.get("items"))) {
                    if (!(o instanceof Map<?, ?> m) || !(m.get("evidenceItemId") instanceof String id)) {
                        continue;
                    }
                    String ref = ai.foresignal.object.ObjectTypes.EVIDENCE_ITEM + ":" + id;
                    Optional<Map<String, Object>> snap = evidenceItem(id, caller);
                    if (snap.isEmpty()) {
                        out.missing.add(ref);
                        if (pin) {
                            continue;
                        }
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("evidenceItemId", id);
                        item.put("title", null);
                        item.put("grade", "F6");
                        item.put("snapshot", null);
                        items.add(item);
                        continue;
                    }
                    out.snapshots.put(ref, snap.get().get("snapshot"));
                    envelope(ref, caller).ifPresent(e -> out.envelope(ref, e));
                    items.add(snap.get());
                }
                b.put("items", items);
            }
            case "attachments" -> {
                List<String> kept = new ArrayList<>();
                for (Object id : ReportBlocks.asList(b.get("attachmentIds"))) {
                    String ref = ai.foresignal.object.ObjectTypes.ATTACHMENT + ":" + id;
                    Optional<AttachmentEntity> a = attachment(String.valueOf(id), caller);
                    if (a.isEmpty()) {
                        out.missing.add(ref);
                        if (!pin) {
                            kept.add(String.valueOf(id));
                        }
                        continue;
                    }
                    out.attachments.put(a.get().getId(), a.get());
                    envelope(ref, caller).ifPresent(e -> out.envelope(ref, e));
                    kept.add(a.get().getId());
                }
                b.put("attachmentIds", kept);
            }
            case "markdown" -> {
                List<String> cited = new ArrayList<>(ReportBlocks.asList(b.get("citations")).stream()
                        .map(String::valueOf).toList());
                for (String link : ReportBlocks.inlineLinks((String) b.get("markdown"))) {
                    if (!cited.contains(link)) {
                        cited.add(link);
                    }
                }
                for (String ref : cited) {
                    pin(ref, caller).ifPresentOrElse(p -> out.envelope(pin ? p.ref() : ref, p.envelope()),
                            () -> out.missing.add(ref));
                }
            }
            case "bluf" -> {
                for (Object o : ReportBlocks.asList(b.get("judgments"))) {
                    if (o instanceof Map<?, ?> m) {
                        for (Object r : new Object[] {m.get("questionRef"), m.get("hypothesisSetRef")}) {
                            if (r instanceof String ref) {
                                envelope(ref, caller).ifPresentOrElse(e -> out.envelope(ref, e),
                                        () -> out.missing.add(ref));
                            }
                        }
                    }
                }
            }
            default -> {
                // map, coverage and sources: nothing to resolve here
            }
        }
        return b;
    }

    /** The envelope of {@code ref} as the caller sees it now, as a plain map, or empty. */
    public Optional<Map<String, Object>> envelope(String ref, ObjectCaller caller) {
        return find(ref, caller).map(this::plain);
    }

    /** The envelope of {@code ref} as the caller sees it now, or empty. */
    public Optional<ObjectEnvelope> find(String ref, ObjectCaller caller) {
        if (ref == null || !ObjectRef.isValid(ref)) {
            return Optional.empty();
        }
        try {
            return objects.find(ObjectRef.parse(ref), new ResolveContext(caller, null));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /** Envelopes of several references in one pass (missing and invisible ones left out), keyed by the request ref. */
    public Map<String, ObjectEnvelope> envelopes(Collection<String> refs, ObjectCaller caller) {
        Map<String, ObjectEnvelope> out = new LinkedHashMap<>();
        List<ObjectRef> parsed = new ArrayList<>();
        Map<ObjectRef, String> byRef = new LinkedHashMap<>();
        for (String r : refs) {
            if (r != null && ObjectRef.isValid(r)) {
                ObjectRef p = ObjectRef.parse(r);
                parsed.add(p);
                byRef.put(p, r);
            }
        }
        if (parsed.isEmpty()) {
            return out;
        }
        Map<ObjectRef, ObjectEnvelope> found;
        try {
            found = objects.resolveRefs(parsed, new ResolveContext(caller, null));
        } catch (RuntimeException e) {
            found = Map.of();
        }
        for (ObjectRef p : parsed) {
            ObjectEnvelope e = found.get(p);
            if (e != null) {
                out.put(byRef.get(p), e);
            }
        }
        return out;
    }

    /**
     * A pinned citation.
     *
     * @param ref the pinned reference ({@code record:…@seq} for records, else the reference itself)
     * @param envelope the envelope snapshot
     */
    public record Pinned(String ref, Map<String, Object> envelope) {}

    /** Pins {@code ref} for the caller: empty when it is missing or invisible. */
    public Optional<Pinned> pin(String ref, ObjectCaller caller) {
        return find(ref, caller).map(e -> new Pinned(pinnedRef(ref, e), plain(e)));
    }

    /** The pinned form of {@code ref}: a record without {@code @seq} gets the envelope's {@code asOfSeq}. */
    static String pinnedRef(String ref, ObjectEnvelope e) {
        ObjectRef parsed = ObjectRef.parse(ref);
        if (parsed.isRecord() && parsed.recordSeq() == null && e.provenance() != null
                && e.provenance().asOfSeq() != null) {
            return parsed.format() + "@" + e.provenance().asOfSeq();
        }
        return parsed.format();
    }

    /** The TLP label of an envelope, or null for data and analytic objects. */
    public static Tlp tlp(Map<String, Object> envelope) {
        Object t = envelope == null ? null : envelope.get("tlp");
        if (!(t instanceof String s)) {
            return null;
        }
        try {
            return Tlp.fromWire(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * The snapshot of a hypothesis set: its full view and the ids of its newest revisions.
     *
     * @return {@code {snapshot, revisionIds}}, or empty when the set is missing or private to the caller
     */
    public Optional<Map<String, Object>> hypothesisSet(String setRef, ObjectCaller caller) {
        try {
            String id = ObjectRef.parse(setRef).id();
            HypothesisSetEntity set = hypotheses.get(id, caller.seesPrivate());
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("snapshot", plainMap(hypotheses.view(set, true)));
            List<String> ids = new ArrayList<>();
            for (HypothesisRevisionEntity r : hypotheses.revisionsOf(set.getId(), null, REVISIONS).items()) {
                ids.add(r.getId());
            }
            out.put("revisionIds", ids);
            return Optional.of(out);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /** The snapshot of a ledger question (ensemble, forecasters and scores), or empty. */
    public Optional<Map<String, Object>> question(String questionRef, ObjectCaller caller) {
        try {
            ForecastQuestionEntity q = forecasts.get(ObjectRef.parse(questionRef).id(), caller.seesPrivate());
            return Optional.of(plainMap(forecasts.view(q)));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /** The question entity, or empty (drafting key judgments). */
    public Optional<ForecastQuestionEntity> questionEntity(String id, ObjectCaller caller) {
        try {
            return Optional.of(forecasts.get(id, caller.seesPrivate()));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /** The set entity, or empty (drafting key judgments). */
    public Optional<HypothesisSetEntity> setEntity(String id, ObjectCaller caller) {
        try {
            return Optional.of(hypotheses.get(id, caller.seesPrivate()));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /** The hypotheses of a set, in order. */
    public List<ai.foresignal.hypothesis.HypothesisEntity> hypothesesOf(String setId) {
        return hypotheses.hypothesesOf(setId);
    }

    /**
     * The snapshot of an evidence item: {@code {evidenceItemId, title, grade, snapshot, tlp}}, or empty when missing or
     * invisible.
     */
    public Optional<Map<String, Object>> evidenceItem(String id, ObjectCaller caller) {
        try {
            EvidenceItemEntity e = evidence.get(id, caller);
            Map<String, Object> view = plainMap(evidence.view(e, caller));
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("evidenceItemId", e.getId());
            out.put("title", e.getTitle());
            out.put("grade", e.grade());
            out.put("snapshot", view);
            return Optional.of(out);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /** An envelope as a plain JSON map. */
    public Map<String, Object> plain(ObjectEnvelope e) {
        return json.convertValue(e, MAP);
    }

    /** Any view as a plain JSON map (instants as strings). */
    public Map<String, Object> plainMap(Object value) {
        return value == null ? null : json.convertValue(value, MAP);
    }
}
