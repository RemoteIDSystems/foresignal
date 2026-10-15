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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.springframework.data.domain.Limit;
import tools.jackson.databind.JsonNode;
import ai.foresignal.source.spi.Geo;

/**
 * Creates, edits and merges intelligence events (prompt 101). Manual and agent events need at least
 * one member that exists (verified by seq); the client's confidence is always rejected and the
 * scorer computes it. Optimistic locking surfaces as 409 {@code EVENT_VERSION_CONFLICT}.
 */
@Service
public class IntelEventService {

    private final IntelligenceEventRepository events;
    private final IntelEventMemberRepository members;
    private final IntelEventLogRepository logs;
    private final RecordQueryService records;
    private final HealthService health;
    private final IntelEventBus bus;
    private final Clock clock;

    /** Creates the service. */
    public IntelEventService(IntelligenceEventRepository events, IntelEventMemberRepository members,
            IntelEventLogRepository logs, RecordQueryService records, HealthService health, IntelEventBus bus,
            Clock clock) {
        this.events = events;
        this.members = members;
        this.logs = logs;
        this.records = records;
        this.health = health;
        this.bus = bus;
        this.clock = clock;
    }

    /** One event, or 404. */
    public IntelligenceEventEntity get(String id) {
        return events.findById(id).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_EVENT, "No such event: " + id));
    }

    /** The members of an event. */
    public List<IntelEventMemberEntity> members(String id) {
        return members.findByEventIdOrderByIdAsc(id);
    }

    /** The log of an event. */
    public List<IntelEventLogEntity> log(String id) {
        return logs.findByEventIdOrderByIdAsc(id);
    }

    /** Lists events, filtering in Java (a keyset cursor is prompt 103). */
    public List<IntelligenceEventEntity> list(String type, String status, Instant since, Double minConfidence,
            int limit) {
        List<IntelligenceEventEntity> out = new ArrayList<>();
        for (IntelligenceEventEntity event : events.findAllByOrderByUpdatedAtDescIdDesc(Limit.of(2_000))) {
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

    /** Creates an agent/operator event from a body. */
    @Transactional
    public IntelligenceEventEntity create(JsonNode body, String actor, String origin) {
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
        JsonNode memberArray = body.get("members");
        if (memberArray == null || !memberArray.isArray() || memberArray.isEmpty()) {
            throw ValidationException.forField("members", "must be a non-empty array");
        }
        Instant now = clock.instant();
        Instant start = body.hasNonNull("startTime") ? Instant.parse(body.get("startTime").asString()) : now;
        IntelligenceEventEntity event =
                new IntelligenceEventEntity(Ulids.next(now), type, title.strip(), start);
        event.setOrigin(origin);
        event.setCreatedAt(now);
        event.setUpdatedAt(now);
        event.setSeverity(body.hasNonNull("severity") ? text(body, "severity") : "warning");
        event.setStatus("active");
        event.setConfidenceMethod(EventScorer.METHOD);
        events.save(event);

        int added = 0;
        for (JsonNode node : memberArray) {
            addMemberInternal(event, node, actor, now);
            added++;
        }
        rescore(event);
        log(event, now, actor, "created", Map.of("members", added));
        events.save(event);
        bus.publish(new IntelEvent(lastLogId(event), event.getId(), IntelEvent.CREATED, event.getVersion(),
                IntelEventMapper.summary(event), null));
        return event;
    }

    /** Patches title, status, a severity override and notes. */
    @Transactional
    public IntelligenceEventEntity patch(String id, JsonNode body, String actor) {
        IntelligenceEventEntity event = get(id);
        Integer version = body.hasNonNull("version") ? body.get("version").asInt() : null;
        if (version != null && version != event.getVersion()) {
            throw new ConflictException(ErrorCodes.EVENT_VERSION_CONFLICT,
                    "Event " + id + " is at version " + event.getVersion() + ", not " + version + ".");
        }
        Instant now = clock.instant();
        Map<String, Object> before = IntelEventMapper.summary(event);
        if (body.hasNonNull("title")) {
            event.setTitle(text(body, "title"));
        }
        if (body.hasNonNull("status")) {
            event.setStatus(text(body, "status"));
        }
        if (body.hasNonNull("severity")) {
            event.setSeverity(text(body, "severity"));
        }
        if (body.hasNonNull("notes")) {
            event.setNarrative(text(body, "notes"));
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
    public IntelligenceEventEntity addMember(String id, JsonNode node, String actor) {
        IntelligenceEventEntity event = get(id);
        Instant now = clock.instant();
        addMemberInternal(event, node, actor, now);
        rescore(event);
        event.setUpdatedAt(now);
        IntelEventLogEntity entry = log(event, now, actor, "member_added", null);
        events.save(event);
        bus.publish(new IntelEvent(entry.getId(), event.getId(), IntelEvent.UPDATED, event.getVersion(),
                IntelEventMapper.summary(event), null));
        return event;
    }

    /** Removes a member. */
    @Transactional
    public void removeMember(String id, long memberId, String actor) {
        IntelligenceEventEntity event = get(id);
        IntelEventMemberEntity member = members.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.EVENT_MEMBER_NOT_FOUND, "No such member: " + memberId));
        if (!member.getEventId().equals(id)) {
            throw new NotFoundException(ErrorCodes.EVENT_MEMBER_NOT_FOUND, "Member " + memberId + " is not on " + id);
        }
        members.delete(member);
        Instant now = clock.instant();
        event.setUpdatedAt(now);
        rescore(event);
        log(event, now, actor, "edited", Map.of("removedMember", memberId));
        events.save(event);
    }

    /** Merges {@code id} into {@code intoId}, moving members and tombstoning the merged event. */
    @Transactional
    public IntelligenceEventEntity merge(String id, String intoId, String actor) {
        if (id.equals(intoId)) {
            throw ValidationException.forField("intoId", "must differ from the event id");
        }
        IntelligenceEventEntity merged = get(id);
        IntelligenceEventEntity target = get(intoId);
        if (!merged.getType().equals(target.getType())) {
            throw ValidationException.forField("intoId", "must be the same type");
        }
        members.moveAll(intoId, id);
        Instant now = clock.instant();
        merged.setMergedInto(intoId);
        merged.setStatus("resolved");
        merged.setUpdatedAt(now);
        events.save(merged);
        rescore(target);
        target.setUpdatedAt(now);
        IntelEventLogEntity entry = log(target, now, actor, "merged", Map.of("merged", id));
        events.save(target);
        bus.publish(new IntelEvent(entry.getId(), target.getId(), IntelEvent.MERGED, target.getVersion(),
                IntelEventMapper.summary(target), null));
        bus.publish(new IntelEvent(entry.getId(), merged.getId(), IntelEvent.RESOLVED, merged.getVersion(),
                IntelEventMapper.summary(merged), intoId));
        return target;
    }

    private void addMemberInternal(IntelligenceEventEntity event, JsonNode node, String actor, Instant now) {
        String memberType = text(node, "memberType");
        String sourceId = text(node, "sourceId");
        String externalId = text(node, "externalId");
        long seq = node.path("seq").asLong(0);
        if (memberType == null || sourceId == null || externalId == null || seq <= 0) {
            throw ValidationException.forField("members", "each member needs memberType, sourceId, externalId and seq");
        }
        Map<Long, RecordRow> found = records.versionsBySeq(List.of(seq));
        RecordRow row = found.get(seq);
        if (row == null) {
            throw ValidationException.forField("members", "no version at seq " + seq);
        }
        IntelEventMemberEntity member = new IntelEventMemberEntity(event.getId(), memberType, sourceId, externalId, seq);
        member.setRole(text(node, "role") == null ? "supporting" : text(node, "role"));
        member.setVersion(row.version());
        member.setKind(row.kind());
        member.setObservedAt(row.observedAt());
        member.setSignalFamily(text(node, "signalFamily"));
        member.setWeight(node.path("weight").isNumber() ? node.get("weight").asDouble() : 0.6);
        member.setJoinReason(Map.of("clause", "manual"));
        member.setAddedAt(now);
        member.setAddedBy(actor);
        members.save(member);
    }

    /** Recomputes confidence and evidence quality from the event's members. */
    void rescore(IntelligenceEventEntity event) {
        List<EventScorer.MemberView> views = new ArrayList<>();
        for (IntelEventMemberEntity member : members.findByEventIdOrderByIdAsc(event.getId())) {
            if ("contradicting".equals(member.getRole())) {
                continue;
            }
            boolean healthy;
            try {
                healthy = health.health(member.getSourceId()).status() == HealthStatus.HEALTHY;
            } catch (RuntimeException e) {
                healthy = false;
            }
            views.add(new EventScorer.MemberView(member.getSignalFamily(),
                    member.getWeight() == null ? 0.6 : member.getWeight(), member.getSourceId(), healthy, null));
        }
        long contradictions = members.findByEventIdOrderByIdAsc(event.getId()).stream()
                .filter(m -> "contradicting".equals(m.getRole())).count();
        EventScorer.Score score = EventScorer.score(views, (int) contradictions);
        event.setConfidence(score.confidence());
        event.setConfidenceLabel(score.label());
        event.setEvidenceQuality(score.evidenceQuality());
        event.setConfidenceMethod(EventScorer.METHOD);
    }

    private IntelEventLogEntity log(IntelligenceEventEntity event, Instant now, String actor, String action,
            Map<String, Object> after) {
        IntelEventLogEntity entry = new IntelEventLogEntity(event.getId(), now, actor, action);
        if (after != null) {
            entry.setAfter(new LinkedHashMap<>(after));
        }
        return logs.save(entry);
    }

    private long lastLogId(IntelligenceEventEntity event) {
        return logs.findTop1ByOrderByIdDesc().map(IntelEventLogEntity::getId).orElse(0L);
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

    /** True when an event exists. */
    public Optional<IntelligenceEventEntity> find(String id) {
        return events.findById(id);
    }
}
