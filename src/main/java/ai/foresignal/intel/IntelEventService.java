package ai.foresignal.intel;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collect.HealthService;
import ai.foresignal.security.Ulids;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.springframework.data.domain.Limit;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import ai.foresignal.source.spi.Geo;

/**
 * Creates, edits and merges intelligence events (prompt 101). Manual and agent events need at least
 * one member that exists (verified by seq); the client's confidence is always rejected and the
 * scorer computes it. Optimistic locking surfaces as 409 {@code EVENT_VERSION_CONFLICT}.
 *
 * <p><strong>Deterministic scoring (INT-3).</strong> A member's identity must match the stored version
 * at its seq, and its signal family and weight are derived by the server from the recipe registry
 * ({@link RecipeRegistry#familyWeight}); whatever a client or model sends for them is ignored.
 *
 * <p><strong>Private events (EGR-1).</strong> An event with any member from a private source is
 * private; the flag is derived here, never taken from the client. Every read and write takes the
 * caller's private access ({@link IntelVisibility}): a private event is invisible ({@code 404
 * UNKNOWN_EVENT}) to a caller without it, and such a caller cannot pin a private version either.
 */
@Service
public class IntelEventService {

    /** The statuses an event may take. */
    public static final Set<String> STATUSES = Set.of("emerging", "active", "resolved", "dismissed");

    /** The severities an event may take. */
    public static final Set<String> SEVERITIES = Set.of("info", "warning", "critical");

    /** The member types. */
    public static final Set<String> MEMBER_TYPES = Set.of("record", "alert", "anomaly", "event");

    /** The member roles. */
    public static final Set<String> ROLES = Set.of("supporting", "contradicting", "context");

    /** The most events the in-Java list scans (a keyset cursor is prompt 103). */
    static final int LIST_SCAN = 2_000;

    private final IntelligenceEventRepository events;
    private final IntelEventMemberRepository members;
    private final IntelEventLogRepository logs;
    private final RecordQueryService records;
    private final HealthService health;
    private final IntelEventBus bus;
    private final RecipeRegistry recipes;
    private final IntelVisibility visibility;
    private final JsonMapper json;
    private final Clock clock;

    /** Outlet reliability of news-article members (prompt 133), or null when the news layer is absent. */
    private ai.foresignal.news.NewsReliability newsReliability;

    /** Installs the outlet reliability of news-article members (prompt 133: {@code media.reliabilityMap}). */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setNewsReliability(ai.foresignal.news.NewsReliability newsReliability) {
        this.newsReliability = newsReliability;
    }

    /** The reliability of a member: the outlet grade of a news article, else null (the scorer's default). */
    private Double memberReliability(String sourceId, String externalId) {
        ai.foresignal.news.NewsReliability r = newsReliability;
        if (r == null) {
            return null;
        }
        try {
            return r.reliability(sourceId, externalId);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** Creates the service. */
    public IntelEventService(IntelligenceEventRepository events, IntelEventMemberRepository members,
            IntelEventLogRepository logs, RecordQueryService records, HealthService health, IntelEventBus bus,
            RecipeRegistry recipes, IntelVisibility visibility, JsonMapper json, Clock clock) {
        this.events = events;
        this.members = members;
        this.logs = logs;
        this.records = records;
        this.health = health;
        this.bus = bus;
        this.recipes = recipes;
        this.visibility = visibility;
        this.json = json;
        this.clock = clock;
    }

    /**
     * One event, or 404, <em>without</em> a visibility check: for internal callers only. Every egress
     * path uses {@link #visible(String, boolean)}.
     */
    public IntelligenceEventEntity get(String id) {
        return events.findById(id).orElseThrow(() -> unknown(id));
    }

    /** One event the caller may see, or {@code 404 UNKNOWN_EVENT} (a hidden private event is not disclosed). */
    public IntelligenceEventEntity visible(String id, boolean privateAccess) {
        IntelligenceEventEntity event = get(id);
        if (!IntelVisibility.visible(event, privateAccess)) {
            throw unknown(id);
        }
        return event;
    }

    /** The members of an event. */
    public List<IntelEventMemberEntity> members(String id) {
        return members.findByEventIdOrderByIdAsc(id);
    }

    /** The log of an event. */
    public List<IntelEventLogEntity> log(String id) {
        return logs.findByEventIdOrderByIdAsc(id);
    }

    /**
     * Lists the events the caller may see, filtering in Java (a keyset cursor is prompt 103).
     *
     * @param privateAccess whether the caller may see private events ({@link IntelVisibility})
     */
    public List<IntelligenceEventEntity> list(String type, String status, Instant since, Double minConfidence,
            int limit, boolean privateAccess) {
        List<IntelligenceEventEntity> out = new ArrayList<>();
        for (IntelligenceEventEntity event : events.findAllByOrderByUpdatedAtDescIdDesc(Limit.of(LIST_SCAN))) {
            if (!IntelVisibility.visible(event, privateAccess)) {
                continue;
            }
            if (type != null && !type.equals(event.getType())) {
                continue;
            }
            if (status != null && !status.equals(event.getStatus())) {
                continue;
            }
            if (since != null && (event.getUpdatedAt() == null || event.getUpdatedAt().isBefore(since))) {
                continue;
            }
            if (minConfidence != null && (event.getConfidence() == null || event.getConfidence() < minConfidence)) {
                continue;
            }
            out.add(event);
            if (out.size() >= limit) {
                break;
            }
        }
        return out;
    }

    /**
     * Creates an agent/operator/analyst event from a body.
     *
     * @param privateAccess whether the caller may pin versions of private sources
     */
    @Transactional
    public IntelligenceEventEntity create(JsonNode body, String actor, String origin, boolean privateAccess) {
        if (body.path("confidence").isNumber()) {
            throw new ValidationException("Client-supplied confidence is not accepted; the scorer computes it.");
        }
        String type = text(body, "type");
        String title = text(body, "title");
        if (type == null || !EventTypes.ALL.contains(type)) {
            throw ValidationException.forField("type", "must be one of " + EventTypes.ALL);
        }
        if (title == null || title.isBlank()) {
            throw ValidationException.forField("title", "is required");
        }
        String severity = body.hasNonNull("severity") ? text(body, "severity") : "warning";
        if (!SEVERITIES.contains(severity)) {
            throw ValidationException.forField("severity", "must be one of info, warning, critical");
        }
        JsonNode memberArray = body.get("members");
        if (memberArray == null || !memberArray.isArray() || memberArray.isEmpty()) {
            throw ValidationException.forField("members", "must be a non-empty array");
        }
        Instant now = clock.instant();
        Instant start;
        try {
            start = body.hasNonNull("startTime") ? Instant.parse(body.get("startTime").asString()) : now;
        } catch (RuntimeException e) {
            throw ValidationException.forField("startTime", "must be an RFC 3339 date-time");
        }
        IntelligenceEventEntity event =
                new IntelligenceEventEntity(Ulids.next(now), type, title.strip(), start);
        event.setOrigin(origin);
        event.setCreatedAt(now);
        event.setUpdatedAt(now);
        event.setSeverity(severity);
        // Prompt 102 §4: an analyst-proposed event starts emerging; operator and agent events are active.
        event.setStatus("analyst".equals(origin) ? "emerging" : "active");
        event.setConfidenceMethod(EventScorer.METHOD);
        if (body.hasNonNull("notes")) {
            event.setNotes(text(body, "notes"));
        }
        events.save(event);

        int added = 0;
        boolean anyPrivate = false;
        for (JsonNode node : memberArray) {
            AddedMember member = addMemberInternal(event, node, actor, now, privateAccess);
            anyPrivate = anyPrivate || member.privateSource();
            added += member.added() ? 1 : 0;
        }
        event.setPrivateEvent(anyPrivate);
        rescore(event);
        IntelEventLogEntity entry = log(event, now, actor, "created", Map.of("members", added));
        events.save(event);
        bus.publish(new IntelEvent(entry.getId(), event.getId(), IntelEvent.CREATED, event.getVersion(),
                IntelEventMapper.summary(event), null));
        return event;
    }

    /** Patches title, status, a severity override and notes (all validated). */
    @Transactional
    public IntelligenceEventEntity patch(String id, JsonNode body, String actor, boolean privateAccess) {
        IntelligenceEventEntity event = visible(id, privateAccess);
        Integer version = body.hasNonNull("version") ? body.get("version").asInt() : null;
        if (version != null && version != event.getVersion()) {
            throw new ConflictException(ErrorCodes.EVENT_VERSION_CONFLICT,
                    "Event " + id + " is at version " + event.getVersion() + ", not " + version + ".");
        }
        String title = body.hasNonNull("title") ? text(body, "title") : null;
        if (title != null && (title.isBlank() || title.length() > 200)) {
            throw ValidationException.forField("title", "must be 1 to 200 characters");
        }
        String status = body.hasNonNull("status") ? text(body, "status") : null;
        if (status != null && !STATUSES.contains(status)) {
            throw ValidationException.forField("status", "must be one of emerging, active, resolved, dismissed");
        }
        String severity = body.hasNonNull("severity") ? text(body, "severity") : null;
        if (severity != null && !SEVERITIES.contains(severity)) {
            throw ValidationException.forField("severity", "must be one of info, warning, critical");
        }
        Instant now = clock.instant();
        Map<String, Object> before = IntelEventMapper.summary(event);
        if (title != null) {
            event.setTitle(title.strip());
        }
        if (status != null) {
            event.setStatus(status);
            if (("resolved".equals(status) || "dismissed".equals(status)) && event.getEndTime() == null) {
                event.setEndTime(now);
            } else if (!"resolved".equals(status) && !"dismissed".equals(status)) {
                event.setEndTime(null);
            }
        }
        if (severity != null) {
            event.setSeverity(severity);
        }
        if (body.has("notes")) {
            // Operator or agent notes are stored apart from the LLM narrative (which is labeled speculation).
            event.setNotes(body.get("notes").isNull() ? null : text(body, "notes"));
        }
        event.setUpdatedAt(now);
        rescore(event);
        IntelEventLogEntity entry = log(event, now, actor, "edited", before);
        events.save(event);
        bus.publish(new IntelEvent(entry.getId(), event.getId(),
                "resolved".equals(event.getStatus()) ? IntelEvent.RESOLVED : IntelEvent.UPDATED, event.getVersion(),
                IntelEventMapper.summary(event), null));
        return event;
    }

    /** Adds a verified member. */
    @Transactional
    public IntelligenceEventEntity addMember(String id, JsonNode node, String actor, boolean privateAccess) {
        IntelligenceEventEntity event = visible(id, privateAccess);
        Instant now = clock.instant();
        AddedMember added = addMemberInternal(event, node, actor, now, privateAccess);
        if (added.privateSource()) {
            event.setPrivateEvent(true);
        }
        rescore(event);
        event.setUpdatedAt(now);
        IntelEventLogEntity entry = log(event, now, actor, "member_added", null);
        events.save(event);
        bus.publish(new IntelEvent(entry.getId(), event.getId(), IntelEvent.UPDATED, event.getVersion(),
                IntelEventMapper.summary(event), null));
        return event;
    }

    /** Removes a member, rescoring and announcing the change on the {@code intel} topic. */
    @Transactional
    public void removeMember(String id, long memberId, String actor, boolean privateAccess) {
        IntelligenceEventEntity event = visible(id, privateAccess);
        IntelEventMemberEntity member = members.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.EVENT_MEMBER_NOT_FOUND, "No such member: " + memberId));
        if (!member.getEventId().equals(id)) {
            throw new NotFoundException(ErrorCodes.EVENT_MEMBER_NOT_FOUND, "Member " + memberId + " is not on " + id);
        }
        members.delete(member);
        members.flush();
        Instant now = clock.instant();
        event.setUpdatedAt(now);
        rescore(event);
        IntelEventLogEntity entry = log(event, now, actor, "edited", Map.of("removedMember", memberId));
        events.save(event);
        bus.publish(new IntelEvent(entry.getId(), event.getId(), IntelEvent.UPDATED, event.getVersion(),
                IntelEventMapper.summary(event), null));
    }

    /**
     * Merges {@code id} into {@code intoId}: moves the members the target does not already pin (a member
     * both share is kept once, on the target), tombstones the merged event and logs the merge.
     */
    @Transactional
    public IntelligenceEventEntity merge(String id, String intoId, String actor, boolean privateAccess) {
        if (id.equals(intoId)) {
            throw ValidationException.forField("intoId", "must differ from the event id");
        }
        IntelligenceEventEntity merged = visible(id, privateAccess);
        IntelligenceEventEntity target = visible(intoId, privateAccess);
        if (!merged.getType().equals(target.getType())) {
            throw ValidationException.forField("intoId", "must be the same type");
        }
        int moved = moveMembers(id, intoId);
        Instant now = clock.instant();
        merged.setMergedInto(intoId);
        merged.setMemberCount((int) members.countByEventId(id));
        merged.setStatus("resolved");
        merged.setEndTime(merged.getEndTime() == null ? now : merged.getEndTime());
        merged.setUpdatedAt(now);
        events.save(merged);
        target.setPrivateEvent(target.isPrivateEvent() || merged.isPrivateEvent());
        rescore(target);
        target.setUpdatedAt(now);
        IntelEventLogEntity entry = log(target, now, actor, "merged", Map.of("merged", id, "moved", moved));
        events.save(target);
        bus.publish(new IntelEvent(entry.getId(), target.getId(), IntelEvent.MERGED, target.getVersion(),
                IntelEventMapper.summary(target), null));
        bus.publish(new IntelEvent(entry.getId(), merged.getId(), IntelEvent.RESOLVED, merged.getVersion(),
                IntelEventMapper.summary(merged), intoId));
        return target;
    }

    /**
     * Moves every member of {@code from} to {@code to}, deleting the ones {@code to} already pins (the
     * unique {@code (event_id, member_type, source_id, external_id, seq)} would otherwise reject the move).
     *
     * @return how many members moved
     */
    int moveMembers(String from, String to) {
        Set<String> pinned = new java.util.HashSet<>();
        for (IntelEventMemberEntity member : members.findByEventIdOrderByIdAsc(to)) {
            pinned.add(pinKey(member));
        }
        int moved = 0;
        for (IntelEventMemberEntity member : members.findByEventIdOrderByIdAsc(from)) {
            if (pinned.add(pinKey(member))) {
                member.setEventId(to);
                members.save(member);
                moved++;
            } else {
                members.delete(member);
            }
        }
        members.flush();
        return moved;
    }

    private static String pinKey(IntelEventMemberEntity member) {
        return member.getMemberType() + "|" + member.getSourceId() + "|" + member.getExternalId() + "|"
                + member.getSeq();
    }

    /** What adding one member did. */
    private record AddedMember(boolean added, boolean privateSource) {}

    private AddedMember addMemberInternal(IntelligenceEventEntity event, JsonNode node, String actor, Instant now,
            boolean privateAccess) {
        String memberType = text(node, "memberType");
        String sourceId = text(node, "sourceId");
        String externalId = text(node, "externalId");
        long seq = node.path("seq").asLong(0);
        if (memberType == null || sourceId == null || externalId == null || seq <= 0) {
            throw ValidationException.forField("members", "each member needs memberType, sourceId, externalId and seq");
        }
        if (!MEMBER_TYPES.contains(memberType)) {
            throw ValidationException.forField("members", "memberType must be one of " + MEMBER_TYPES);
        }
        String role = text(node, "role") == null ? "supporting" : text(node, "role");
        if (!ROLES.contains(role)) {
            throw ValidationException.forField("members", "role must be one of " + ROLES);
        }
        RecordRow row = records.versionsBySeq(List.of(seq)).get(seq);
        boolean privateSource = row != null && visibility.isPrivateSource(row.sourceId());
        if (row == null || (privateSource && !privateAccess)) {
            // A private version the caller may not see is reported exactly like a missing one.
            throw new ValidationException(ErrorCodes.EVENT_MEMBER_NOT_FOUND, "No version at seq " + seq + ".",
                    List.of(new ai.foresignal.api.error.FieldError("members", "no version at seq " + seq)));
        }
        if (!sourceId.equals(row.sourceId()) || !externalId.equals(row.externalId())) {
            throw new ValidationException(ErrorCodes.EVENT_MEMBER_NOT_FOUND,
                    "The version at seq " + seq + " is not " + sourceId + "/" + externalId + ".",
                    List.of(new ai.foresignal.api.error.FieldError("members",
                            "sourceId and externalId must match the version at seq " + seq)));
        }
        if (!members.findByEventIdAndMemberTypeAndSourceIdAndExternalIdAndSeq(event.getId(), memberType, sourceId,
                externalId, seq).isEmpty()) {
            return new AddedMember(false, privateSource);
        }
        IntelEventMemberEntity member = new IntelEventMemberEntity(event.getId(), memberType, sourceId, externalId, seq);
        member.setRole(role);
        member.setVersion(row.version());
        member.setKind(row.kind());
        member.setObservedAt(row.observedAt());
        // INT-3: the family and weight come from the recipe registry, never from the request.
        RecipeRegistry.FamilyWeight derived = recipes.familyWeight(event.getType(), row.kind());
        member.setSignalFamily(derived.family());
        member.setWeight(derived.weight());
        member.setJoinReason(Map.of("clause", "manual"));
        member.setSnapshot(SnapshotCaps.cap(snapshot(row), SnapshotCaps.MEMBER_MAX_BYTES, json));
        member.setAddedAt(now);
        member.setAddedBy(actor);
        members.save(member);
        return new AddedMember(true, privateSource);
    }

    private static Map<String, Object> snapshot(RecordRow row) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sourceId", row.sourceId());
        out.put("externalId", row.externalId());
        out.put("kind", row.kind());
        out.put("version", row.version());
        out.put("seq", row.seq());
        out.put("observedAt", row.observedAt() == null ? null : row.observedAt().toString());
        out.put("geometry", row.geometry());
        out.put("payload", row.payload());
        return out;
    }

    /**
     * Recomputes confidence and evidence quality from the event's members (116: members in one evidence group
     * contribute once) and records the analysis manifest of the score.
     */
    public void rescore(IntelligenceEventEntity event) {
        List<EventScorer.MemberView> views = new ArrayList<>();
        List<IntelEventMemberEntity> all = members.findByEventIdOrderByIdAsc(event.getId());
        event.setMemberCount(all.size());
        int contradictions = contradictions(all, event.getAlternatives());
        Map<String, ai.foresignal.evidence.EvidenceService.GroupKey> keys = evidenceKeys(all);
        List<String> refs = new ArrayList<>();
        List<Map<String, Object>> memberViews = new ArrayList<>();
        for (IntelEventMemberEntity member : all) {
            if ("contradicting".equals(member.getRole())) {
                continue;
            }
            boolean healthy;
            try {
                healthy = health.health(member.getSourceId()).status() == HealthStatus.HEALTHY;
            } catch (RuntimeException e) {
                healthy = false;
            }
            ai.foresignal.evidence.EvidenceService.GroupKey key = keys.get(
                    ai.foresignal.evidence.EvidenceService.recordKey(member.getSourceId(), member.getExternalId()));
            EventScorer.MemberView view = new EventScorer.MemberView(member.getSignalFamily(),
                    member.getWeight() == null ? EventScorer.DEFAULT_RELIABILITY : member.getWeight(),
                    member.getSourceId(), healthy, memberReliability(member.getSourceId(), member.getExternalId()),
                    key == null || !key.linked() ? null : key.key(),
                    key == null ? null : key.root());
            views.add(view);
            refs.add(ai.foresignal.evidence.RecordRef.format(member.getSourceId(), member.getExternalId(),
                    member.getSeq()));
            memberViews.add(EventScorerViews.toMap(view));
        }
        EventScorer.Score score = EventScorer.score(views, contradictions);
        event.setConfidence(score.confidence());
        event.setConfidenceLabel(score.label());
        event.setEvidenceQuality(score.evidenceQuality());
        event.setConfidenceMethod(EventScorer.METHOD);
        event.setManifestId(scoreManifest(event, memberViews, contradictions, refs));
    }

    /** The evidence identity of the members (prompt 116); empty without the evidence layer. */
    private Map<String, ai.foresignal.evidence.EvidenceService.GroupKey> evidenceKeys(List<IntelEventMemberEntity> all) {
        if (evidence == null || all.isEmpty()) {
            return Map.of();
        }
        List<ai.foresignal.evidence.RecordRef> refs = new ArrayList<>();
        for (IntelEventMemberEntity member : all) {
            refs.add(new ai.foresignal.evidence.RecordRef(member.getSourceId(), member.getExternalId(), member.getSeq()));
        }
        try {
            return evidence.groupKeys(refs);
        } catch (RuntimeException e) {
            return Map.of();
        }
    }

    /**
     * The manifest of an event's score (prompt 116): the scorer and version, the recipe, the members exactly as
     * the scorer saw them (family, weight, health, evidence group) and the contradictions, with the pinned
     * member refs as inputs. {@code EventScorerReplayer} re-scores from it.
     */
    private String scoreManifest(IntelligenceEventEntity event, List<Map<String, Object>> memberViews,
            int contradictions, List<String> refs) {
        if (manifests == null) {
            return event.getManifestId();
        }
        Map<String, Object> config = new java.util.LinkedHashMap<>();
        config.put("recipeId", event.getRecipeId());
        config.put("recipeVersion", event.getRecipeVersion());
        config.put("members", memberViews);
        config.put("contradictions", contradictions);
        config.put("cap", EventScorer.CAP);
        config.put("contradictionPenalty", EventScorer.CONTRADICTION_PENALTY);
        Object toSeq = event.getDetector() != null && event.getDetector().get("params") instanceof Map<?, ?> params
                ? params.get("toSeq") : null;
        try {
            return manifests.record(ai.foresignal.manifest.ManifestRequest.of(EventScorer.NAME, EventScorer.VERSION)
                    .config(config).registries("originators.json", "canonical-rules.json")
                    .asOf(toSeq instanceof Number n ? n.longValue() : null).inputs(refs));
        } catch (RuntimeException e) {
            return event.getManifestId();
        }
    }

    /** Evidence identity (prompt 116), or null. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.evidence.EvidenceService evidence;

    /** Analysis manifests (prompt 116), or null. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.manifest.ManifestService manifests;

    /**
     * The active contradictions of an event: one per recipe contradiction clause that added contradicting
     * members, one per manually added contradicting member, and one per {@code healthOf} alternative
     * (a healthy, covering source that observed nothing). Coverage-gap alternatives never count.
     */
    static int contradictions(List<IntelEventMemberEntity> all, List<Map<String, Object>> alternatives) {
        Set<String> clauses = new java.util.HashSet<>();
        int manual = 0;
        for (IntelEventMemberEntity member : all) {
            if (!"contradicting".equals(member.getRole())) {
                continue;
            }
            Object clause = member.getJoinReason() == null ? null : member.getJoinReason().get("clause");
            if (clause == null || "manual".equals(clause)) {
                manual++;
            } else {
                clauses.add(String.valueOf(clause));
            }
        }
        int health = 0;
        if (alternatives != null) {
            for (Map<String, Object> alternative : alternatives) {
                if ("healthOf".equals(alternative.get("clause"))) {
                    health++;
                }
            }
        }
        return clauses.size() + manual + health;
    }

    private IntelEventLogEntity log(IntelligenceEventEntity event, Instant now, String actor, String action,
            Map<String, Object> after) {
        IntelEventLogEntity entry = new IntelEventLogEntity(event.getId(), now, actor, action);
        if (after != null) {
            entry.setAfter(new LinkedHashMap<>(after));
        }
        return logs.save(entry);
    }

    private static NotFoundException unknown(String id) {
        return new NotFoundException(ErrorCodes.UNKNOWN_EVENT, "No such event: " + id);
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asString();
    }

    /** Parses a WKT geometry, or null. */
    static Geometry wkt(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            Geometry geometry = new WKTReader(Geo.factory()).read(text);
            geometry.setSRID(Geo.SRID);
            return geometry;
        } catch (Exception e) {
            return null;
        }
    }

    /** The event, when it exists (no visibility check; internal callers only). */
    public Optional<IntelligenceEventEntity> find(String id) {
        return events.findById(id);
    }
}
