package ai.foresignal.situation;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.Ulids;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.JsonNode;

/**
 * Creates, shapes and re-derives situations (prompt 118 §1, §4, §6). The linker persists through
 * {@link #seed} and {@link #join}; operators and agents through {@link #create}, {@link #patch},
 * {@link #addMember}, {@link #removeMember}, {@link #merge} and {@link #split}. Every change re-derives the
 * situation ({@link SituationDeriver}), writes {@code situation_log} rows with the actor, and after commit announces
 * {@code situation.*} events on the {@link SituationEventBus}, sends the {@code situation.activated} and
 * {@code situation.escalated} notifications and asks the {@link SituationBriefService} whether the brief is due.
 *
 * <p><strong>Deterministic membership (INT-3).</strong> Members come only from the linker's scored rules or from an
 * operator or agent action, each with its {@code join_reason}; never from LLM output. Records are pinned and
 * verified by {@code seq}.
 *
 * <p><strong>Private situations (EGR-1).</strong> A situation with any private member is private; the flag is
 * derived, never taken from a client. A private situation is invisible ({@code 404 UNKNOWN_SITUATION}) to a caller
 * without private access, who also cannot pin a private member. A closed situation is final ({@code 409
 * SITUATION_CLOSED}); a stale {@code version} is {@code 409 SITUATION_VERSION_CONFLICT}.
 */
@Service
public class SituationService {

    private static final Logger log = LoggerFactory.getLogger(SituationService.class);

    /** The linker's actor. */
    public static final String LINKER_ACTOR = "linker:" + SituationScorer.VERSION;

    /** The longest title. */
    public static final int MAX_TITLE = 300;

    /** The most members a create may carry. */
    public static final int MAX_CREATE_MEMBERS = 500;

    /** The most members a split may move. */
    public static final int MAX_SPLIT_MEMBERS = 1_000;

    /** The 040 region code pattern. */
    static final Pattern REGION_CODE = Pattern.compile("^[A-Z]{2}(-[A-Z0-9]{1,3})?$");

    private final SituationRepository situations;
    private final SituationMemberRepository members;
    private final SituationLogRepository logs;
    private final SituationDeriver deriver;
    private final LinkerInputReader reader;
    private final SituationEventBus bus;
    private final JdbcTemplate jdbc;
    private final java.time.Clock clock;
    private final ObjectProvider<SituationBriefService> briefs;
    private final ObjectProvider<SituationNotifications> notifications;

    /** Creates the service. */
    public SituationService(SituationRepository situations, SituationMemberRepository members,
            SituationLogRepository logs, SituationDeriver deriver, LinkerInputReader reader, SituationEventBus bus,
            JdbcTemplate jdbc, java.time.Clock clock, ObjectProvider<SituationBriefService> briefs,
            ObjectProvider<SituationNotifications> notifications) {
        this.situations = situations;
        this.members = members;
        this.logs = logs;
        this.deriver = deriver;
        this.reader = reader;
        this.bus = bus;
        this.jdbc = jdbc;
        this.clock = clock;
        this.briefs = briefs;
        this.notifications = notifications;
    }

    // ------------------------------------------------------------------------- reads

    /** One situation, or 404, without a visibility check (internal callers only). */
    public SituationEntity get(String id) {
        return situations.findById(id).orElseThrow(() -> unknown(id));
    }

    /** One situation the caller may see, or {@code 404 UNKNOWN_SITUATION}. */
    public SituationEntity visible(String id, boolean privateAccess) {
        SituationEntity situation = get(id);
        if (situation.isPrivateSituation() && !privateAccess) {
            throw unknown(id);
        }
        return situation;
    }

    /** The members of a situation in the order they were added. */
    public List<SituationMemberEntity> members(String id) {
        return members.findBySituationIdOrderByAddedAtAscIdAsc(id);
    }

    /** The log of a situation, oldest first. */
    public List<SituationLogEntity> log(String id) {
        return logs.findBySituationIdOrderByIdAsc(id);
    }

    /**
     * A list query.
     *
     * @param statuses statuses, empty for all
     * @param themes themes, empty for all
     * @param regions region codes (any shared), empty for all
     * @param bbox {@code minLon,minLat,maxLon,maxLat}, or null
     * @param since only situations changed at or after this, or null
     * @param q a text filter on title and summary, or null
     * @param cursor the cursor of the previous page, or null
     * @param limit the page size
     */
    public record ListQuery(List<String> statuses, List<String> themes, List<String> regions, String bbox,
            Instant since, String q, String cursor, int limit) {}

    /** A page of situations and the cursor of the next one. */
    public record Page(List<SituationEntity> items, String nextCursor) {}

    /** Lists situations by latest activity (keyset on {@code last_activity_at}, {@code id}). */
    public Page list(ListQuery query, boolean privateAccess) {
        List<Object> args = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        if (!query.statuses().isEmpty()) {
            where.append(" AND status = ANY(?::text[])");
            args.add(query.statuses().toArray(String[]::new));
        }
        if (!query.themes().isEmpty()) {
            where.append(" AND theme = ANY(?::text[])");
            args.add(query.themes().toArray(String[]::new));
        }
        if (!query.regions().isEmpty()) {
            where.append(" AND region_codes && ?::text[]");
            args.add(query.regions().toArray(String[]::new));
        }
        if (query.bbox() != null) {
            double[] b = bbox(query.bbox());
            if (b[0] > b[2]) {
                where.append(" AND (geom && ST_MakeEnvelope(?, ?, 180, ?, 4326)"
                        + " OR geom && ST_MakeEnvelope(-180, ?, ?, ?, 4326))");
                args.add(b[0]);
                args.add(b[1]);
                args.add(b[3]);
                args.add(b[1]);
                args.add(b[2]);
                args.add(b[3]);
            } else {
                where.append(" AND geom && ST_MakeEnvelope(?, ?, ?, ?, 4326)");
                args.add(b[0]);
                args.add(b[1]);
                args.add(b[2]);
                args.add(b[3]);
            }
        }
        if (query.since() != null) {
            where.append(" AND updated_at >= ?");
            args.add(Timestamp.from(query.since()));
        }
        if (query.q() != null && !query.q().isBlank()) {
            where.append(" AND (title ILIKE ? OR summary ILIKE ?)");
            String like = "%" + query.q().strip().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
            args.add(like);
            args.add(like);
        }
        if (!privateAccess) {
            where.append(" AND NOT private");
        }
        if (query.cursor() != null) {
            String[] parts = decodeCursor(query.cursor());
            where.append(" AND (last_activity_at, id) < (?, ?)");
            args.add(Timestamp.from(Instant.parse(parts[0])));
            args.add(parts[1]);
        }
        args.add(query.limit() + 1);
        List<String> ids = jdbc.queryForList("SELECT id FROM situation" + where
                + " ORDER BY last_activity_at DESC, id DESC LIMIT ?", String.class, args.toArray());
        Map<String, SituationEntity> byId = new LinkedHashMap<>();
        for (SituationEntity entity : situations.findAllById(ids)) {
            byId.put(entity.getId(), entity);
        }
        List<SituationEntity> items = new ArrayList<>();
        for (String id : ids) {
            SituationEntity entity = byId.get(id);
            if (entity != null) {
                items.add(entity);
            }
        }
        String next = null;
        if (items.size() > query.limit()) {
            items = new ArrayList<>(items.subList(0, query.limit()));
            SituationEntity last = items.get(items.size() - 1);
            next = encodeCursor(last.getLastActivityAt().toString() + "|" + last.getId());
        }
        return new Page(items, next);
    }

    /** Parses and validates a bbox. */
    static double[] bbox(String text) {
        String[] parts = text.split(",");
        if (parts.length != 4) {
            throw ValidationException.forField("bbox", "must be minLon,minLat,maxLon,maxLat");
        }
        double[] out = new double[4];
        try {
            for (int i = 0; i < 4; i++) {
                out[i] = Double.parseDouble(parts[i].strip());
            }
        } catch (NumberFormatException e) {
            throw ValidationException.forField("bbox", "must be four numbers");
        }
        if (out[1] >= out[3] || out[1] < -90 || out[3] > 90 || out[0] < -180 || out[2] > 180) {
            throw ValidationException.forField("bbox", "must be minLon,minLat,maxLon,maxLat with minLat < maxLat");
        }
        return out;
    }

    /** An opaque cursor of {@code text}. */
    public static String encodeCursor(String text) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    /** The two parts of a list cursor, or 400. */
    static String[] decodeCursor(String cursor) {
        try {
            String text = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = text.split("\\|", 2);
            Instant.parse(parts[0]);
            if (parts.length != 2) {
                throw new IllegalArgumentException("cursor");
            }
            return parts;
        } catch (RuntimeException e) {
            throw ValidationException.forField("cursor", "is not a cursor of this list");
        }
    }

    /** The offset of a member or timeline cursor, or 400. */
    public static int offset(String cursor) {
        if (cursor == null) {
            return 0;
        }
        try {
            String text = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            if (!text.startsWith("o:")) {
                throw new IllegalArgumentException("cursor");
            }
            int value = Integer.parseInt(text.substring(2));
            if (value < 0) {
                throw new IllegalArgumentException("cursor");
            }
            return value;
        } catch (RuntimeException e) {
            throw ValidationException.forField("cursor", "is not a cursor of this list");
        }
    }

    /** The cursor of an offset. */
    public static String offsetCursor(int offset) {
        return encodeCursor("o:" + offset);
    }

    // ------------------------------------------------------------------------- operator and agent actions

    /**
     * Creates a situation by hand ({@code SituationInput}); initial members are verified against their pinned
     * seq, all or nothing.
     *
     * @param origin {@code operator} or {@code agent} (from the caller), or {@code analyst}
     */
    @Transactional
    public SituationEntity create(JsonNode body, String actor, String origin, boolean privateAccess) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The body must be a SituationInput object.");
        }
        String title = title(body, true);
        String theme = theme(body.get("theme"), true);
        String status = body.hasNonNull("status") ? body.get("status").asString() : SituationLifecycle.EMERGING;
        if (!SituationLifecycle.EMERGING.equals(status) && !SituationLifecycle.ACTIVE.equals(status)) {
            throw ValidationException.forField("status", "must be emerging or active");
        }
        String override = severityOverride(body);
        List<String> regionCodes = regionCodes(body.get("regionCodes"));
        JsonNode memberArray = body.get("members");
        if (memberArray != null && !memberArray.isNull() && (!memberArray.isArray()
                || memberArray.size() > MAX_CREATE_MEMBERS)) {
            throw ValidationException.forField("members", "must be an array of at most " + MAX_CREATE_MEMBERS);
        }
        List<Resolved> resolved = new ArrayList<>();
        if (memberArray != null && memberArray.isArray()) {
            for (JsonNode node : memberArray) {
                resolved.add(resolveMember(node, privateAccess));
            }
        }
        Instant now = clock.instant();
        SituationEntity situation = new SituationEntity(Ulids.next(now), title, theme, now);
        situation.setOrigin(origin);
        situation.setStatus(status);
        situation.setSeverityOverride(override);
        situation.setRegionCodes(regionCodes);
        situation.setTopicIds(strings(body.get("topicIds"), "topicIds"));
        situation.setEntityIds(strings(body.get("entityIds"), "entityIds"));
        situation.setInvestigationIds(strings(body.get("investigationIds"), "investigationIds"));
        situation.setCreatedAt(now);
        situation.setUpdatedAt(now);
        if (!resolved.isEmpty()) {
            Instant first = resolved.stream().map(r -> r.input().observedAt()).filter(i -> i != null)
                    .min(Comparator.naturalOrder()).orElse(now);
            situation.setStartedAt(first);
            situation.setLastActivityAt(first);
        }
        situations.save(situation);
        Set<String> pinned = new HashSet<>();
        for (Resolved member : resolved) {
            String key = member.input().memberType() + "|" + member.input().ref() + "|" + pinSeq(member.input());
            if (pinned.add(key)) {
                addInternal(situation, member.input(), member.role(), manualReason(origin, actor), actor, now);
            }
        }
        Map<String, Object> after = new LinkedHashMap<>();
        after.put("title", title);
        after.put("theme", theme);
        after.put("members", pinned.size());
        return finish(situation, null, actor, "created", null, after, true, false, now).situation();
    }

    /**
     * Applies a {@code SituationPatch}: rename, set theme, override severity (null clears it), activate, reopen a
     * dormant situation or close it, and set the linked topics, entities and investigations.
     */
    @Transactional
    public SituationEntity patch(String id, JsonNode body, String actor, boolean privateAccess) {
        SituationEntity situation = visible(id, privateAccess);
        checkVersion(situation, body);
        if (situation.isClosed()) {
            throw closed(id);
        }
        Instant now = clock.instant();
        Map<String, Object> before = state(situation);
        List<String[]> entries = new ArrayList<>();
        if (body.has("title")) {
            String title = title(body, true);
            if (!title.equals(situation.getTitle())) {
                entries.add(new String[] {"renamed", situation.getTitle(), title});
                situation.setTitle(title);
            }
        }
        if (body.has("theme")) {
            String theme = theme(body.get("theme"), true);
            if (!theme.equals(situation.getTheme())) {
                entries.add(new String[] {"theme", situation.getTheme(), theme});
                situation.setTheme(theme);
            }
        }
        if (body.has("severityOverride")) {
            String override = severityOverride(body);
            entries.add(new String[] {"severity", situation.getSeverityOverride(), override});
            situation.setSeverityOverride(override);
        }
        if (body.hasNonNull("status")) {
            String status = body.get("status").asString();
            if (!SituationLifecycle.ACTIVE.equals(status) && !SituationLifecycle.CLOSED.equals(status)) {
                throw ValidationException.forField("status", "must be active or closed");
            }
            if (!SituationLifecycle.operatorMayMove(situation.getStatus(), status)) {
                throw closed(id);
            }
            situation.setStatus(status);
            if (SituationLifecycle.CLOSED.equals(status)) {
                situation.setClosedAt(now);
            }
        }
        if (body.has("topicIds")) {
            situation.setTopicIds(strings(body.get("topicIds"), "topicIds"));
        }
        if (body.has("entityIds")) {
            situation.setEntityIds(strings(body.get("entityIds"), "entityIds"));
        }
        if (body.has("investigationIds")) {
            situation.setInvestigationIds(strings(body.get("investigationIds"), "investigationIds"));
        }
        String reason = body.hasNonNull("reason") ? body.get("reason").asString() : null;
        for (String[] entry : entries) {
            Map<String, Object> b = new LinkedHashMap<>();
            b.put("value", entry[1]);
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("value", entry[2]);
            if (reason != null) {
                a.put("reason", reason);
            }
            SituationLogEntity row = new SituationLogEntity(id, now, actor, entry[0]);
            row.setBefore(b);
            row.setAfter(a);
            logs.save(row);
        }
        Map<String, Object> after = new LinkedHashMap<>();
        if (reason != null) {
            after.put("reason", reason);
        }
        return finish(situation, before, actor, "edited", before, after, false, false, now).situation();
    }

    /**
     * The result of adding a member.
     *
     * @param member the member
     * @param created false when it was already present (200)
     */
    public record AddResult(SituationMemberEntity member, boolean created) {}

    /** Adds a pinned member ({@code SituationMemberInput}), optionally moving it from another situation. */
    @Transactional
    public AddResult addMember(String id, JsonNode body, String actor, String origin, boolean privateAccess) {
        SituationEntity situation = visible(id, privateAccess);
        if (situation.isClosed()) {
            throw closed(id);
        }
        Resolved resolved = resolveMember(body, privateAccess);
        LinkerInput input = resolved.input();
        Instant now = clock.instant();
        String moveFrom = body.hasNonNull("moveFromSituationId") ? body.get("moveFromSituationId").asString() : null;
        if (moveFrom != null && !moveFrom.equals(id)) {
            SituationEntity source = visible(moveFrom, privateAccess);
            if (source.isClosed()) {
                throw closed(moveFrom);
            }
            List<SituationMemberEntity> moved = new ArrayList<>();
            for (SituationMemberEntity member : members.findByMemberTypeAndRef(input.memberType(), input.ref())) {
                if (member.getSituationId().equals(moveFrom)) {
                    moved.add(member);
                }
            }
            if (!moved.isEmpty()) {
                Map<String, Object> before = state(source);
                members.deleteAll(moved);
                members.flush();
                Map<String, Object> after = new LinkedHashMap<>();
                after.put("ref", input.ref());
                after.put("movedTo", id);
                finish(source, before, actor, "member_removed", null, after, false, false, now);
            }
        }
        for (SituationMemberEntity member : members.findByMemberTypeAndRef(input.memberType(), input.ref())) {
            if (member.getSituationId().equals(id) && java.util.Objects.equals(member.getSeq(), pinSeq(input))) {
                return new AddResult(member, false);
            }
        }
        Map<String, Object> before = state(situation);
        SituationMemberEntity added = addInternal(situation, input, resolved.role(), manualReason(origin, actor), actor,
                now);
        Map<String, Object> after = new LinkedHashMap<>();
        after.put("memberId", added.getId());
        after.put("ref", input.ref());
        if (moveFrom != null) {
            after.put("movedFrom", moveFrom);
        }
        finish(situation, before, actor, "member_added", null, after, false, added.isCore(), now);
        return new AddResult(members.findById(added.getId()).orElse(added), true);
    }

    /** Removes a member and re-derives the situation. */
    @Transactional
    public void removeMember(String id, String memberId, String actor, boolean privateAccess) {
        SituationEntity situation = visible(id, privateAccess);
        if (situation.isClosed()) {
            throw closed(id);
        }
        SituationMemberEntity member = members.findById(memberId).filter(m -> m.getSituationId().equals(id))
                .orElseThrow(() -> memberNotFound("Member " + memberId + " is not in situation " + id + "."));
        Map<String, Object> before = state(situation);
        members.delete(member);
        members.flush();
        Map<String, Object> after = new LinkedHashMap<>();
        after.put("memberId", memberId);
        after.put("ref", member.getRef());
        finish(situation, before, actor, "member_removed", null, after, false, false, clock.instant());
    }

    /**
     * Merges {@code id} with {@code intoId}: the older of the two keeps its id and receives the members (a member
     * both pin is kept once), the other gets {@code merged_into}, is closed and its record tombstoned; both logs
     * record the merge.
     *
     * @param version the expected version of {@code id}, or null
     * @return the surviving situation
     */
    @Transactional
    public SituationEntity merge(String id, String intoId, Integer version, String reason, String actor,
            boolean privateAccess) {
        if (intoId == null || intoId.isBlank()) {
            throw ValidationException.forField("intoId", "is required");
        }
        if (id.equals(intoId)) {
            throw ValidationException.forField("intoId", "must differ from the situation id");
        }
        SituationEntity path = visible(id, privateAccess);
        SituationEntity other = visible(intoId, privateAccess);
        if (version != null && version != path.getVersion()) {
            throw conflict(path, version);
        }
        if (path.isClosed()) {
            throw closed(id);
        }
        if (other.isClosed()) {
            throw closed(intoId);
        }
        boolean pathOlder = path.getCreatedAt().isBefore(other.getCreatedAt())
                || (path.getCreatedAt().equals(other.getCreatedAt()) && path.getId().compareTo(other.getId()) < 0);
        SituationEntity survivor = pathOlder ? path : other;
        SituationEntity merged = pathOlder ? other : path;
        Instant now = clock.instant();
        Map<String, Object> survivorBefore = state(survivor);
        Map<String, Object> mergedBefore = state(merged);
        int moved = moveMembers(merged.getId(), survivor.getId(), null);
        survivor.setTopicIds(union(survivor.getTopicIds(), merged.getTopicIds()));
        survivor.setEntityIds(union(survivor.getEntityIds(), merged.getEntityIds()));
        survivor.setInvestigationIds(union(survivor.getInvestigationIds(), merged.getInvestigationIds()));
        merged.setMergedInto(survivor.getId());
        merged.setMemberCount(0);
        merged.setSignalFamilies(List.of());
        merged.setStatus(SituationLifecycle.CLOSED);
        merged.setClosedAt(now);
        merged.setUpdatedAt(now);
        situations.saveAndFlush(merged);
        Map<String, Object> mergedAfter = new LinkedHashMap<>();
        mergedAfter.put("mergedInto", survivor.getId());
        mergedAfter.put("moved", moved);
        if (reason != null) {
            mergedAfter.put("reason", reason);
        }
        SituationLogEntity mergedRow = new SituationLogEntity(merged.getId(), now, actor, "merged");
        mergedRow.setBefore(mergedBefore);
        mergedRow.setAfter(mergedAfter);
        mergedRow.setEvent(SituationEvent.MERGED);
        logs.save(mergedRow);
        announce(mergedRow, merged, mergedBefore);
        Map<String, Object> survivorAfter = new LinkedHashMap<>();
        survivorAfter.put("merged", merged.getId());
        survivorAfter.put("moved", moved);
        if (reason != null) {
            survivorAfter.put("reason", reason);
        }
        return finish(survivor, survivorBefore, actor, "merged", null, survivorAfter, false, moved > 0, now)
                .situation();
    }

    /** Moves the chosen members into a new situation with {@code title}; both are re-derived and logged. */
    @Transactional
    public SituationEntity split(String id, List<String> memberIds, String title, String theme, Integer version,
            String actor, String origin, boolean privateAccess) {
        SituationEntity source = visible(id, privateAccess);
        if (version != null && version != source.getVersion()) {
            throw conflict(source, version);
        }
        if (source.isClosed()) {
            throw closed(id);
        }
        if (memberIds == null || memberIds.isEmpty() || memberIds.size() > MAX_SPLIT_MEMBERS) {
            throw ValidationException.forField("memberIds", "must hold 1 to " + MAX_SPLIT_MEMBERS + " member ids");
        }
        if (title == null || title.isBlank() || title.strip().length() > MAX_TITLE) {
            throw ValidationException.forField("title", "must be 1 to " + MAX_TITLE + " characters");
        }
        String newTheme = theme == null ? source.getTheme() : theme;
        if (!SituationThemes.valid(newTheme)) {
            throw ValidationException.forField("theme", "must be one of " + SituationThemes.ALL);
        }
        Set<String> chosen = new LinkedHashSet<>(memberIds);
        Set<String> present = new HashSet<>();
        for (SituationMemberEntity member : members(id)) {
            present.add(member.getId());
        }
        for (String memberId : chosen) {
            if (!present.contains(memberId)) {
                throw memberNotFound("Member " + memberId + " is not in situation " + id + ".");
            }
        }
        Instant now = clock.instant();
        Map<String, Object> sourceBefore = state(source);
        SituationEntity created = new SituationEntity(Ulids.next(now), title.strip(), newTheme, now);
        created.setOrigin(origin);
        created.setCreatedAt(now);
        created.setUpdatedAt(now);
        created.setTopicIds(source.getTopicIds());
        situations.save(created);
        int moved = moveMembers(id, created.getId(), chosen);
        Map<String, Object> sourceAfter = new LinkedHashMap<>();
        sourceAfter.put("splitInto", created.getId());
        sourceAfter.put("moved", moved);
        finish(source, sourceBefore, actor, "split", null, sourceAfter, false, false, now);
        Map<String, Object> createdAfter = new LinkedHashMap<>();
        createdAfter.put("splitFrom", id);
        createdAfter.put("moved", moved);
        return finish(created, null, actor, "split", null, createdAfter, true, false, now).situation();
    }

    // ------------------------------------------------------------------------- linker

    /**
     * Seeds a new {@code emerging} situation from one linker input (prompt 118 §2).
     *
     * @param title the deterministic title
     * @param joinReason the seed's join reason
     * @return the new situation
     */
    @Transactional
    public SituationEntity seed(LinkerInput input, String title, Map<String, Object> joinReason) {
        Instant now = clock.instant();
        Instant observed = input.observedAt() == null ? now : input.observedAt();
        SituationEntity situation = new SituationEntity(Ulids.next(observed), title,
                input.theme() == null ? SituationThemes.MIXED : input.theme(), observed);
        situation.setOrigin("auto");
        situation.setLinkerVersion(SituationScorer.VERSION);
        situation.setCreatedAt(now);
        situation.setUpdatedAt(now);
        situations.save(situation);
        addInternal(situation, input, input.role(), joinReason, LINKER_ACTOR, now);
        Map<String, Object> after = new LinkedHashMap<>();
        after.put("seed", input.objectRef());
        after.put("title", title);
        return finish(situation, null, LINKER_ACTOR, "created", null, after, true, false, now).situation();
    }

    /** Adds one linker input to an existing situation with its scored join reason. */
    @Transactional
    public void join(String situationId, LinkerInput input, Map<String, Object> joinReason) {
        SituationEntity situation = get(situationId);
        if (situation.isClosed()) {
            log.debug("Situation {} closed before the linker could join {}", situationId, input.objectRef());
            return;
        }
        Instant now = clock.instant();
        Map<String, Object> before = state(situation);
        SituationMemberEntity added = addInternal(situation, input, input.role(), joinReason, LINKER_ACTOR, now);
        Map<String, Object> after = new LinkedHashMap<>();
        after.put("memberId", added.getId());
        after.put("ref", input.objectRef());
        after.put("score", joinReason.get("score"));
        finish(situation, before, LINKER_ACTOR, "member_added", null, after, false, added.isCore(), now);
    }

    /**
     * Applies the clock's lifecycle transitions (open → dormant after {@code dormancyWindow}, dormant → closed after
     * 90 days) to every open situation.
     *
     * @return the number of situations whose status changed
     */
    @Transactional
    public int sweep(Instant now, java.time.Duration dormancyWindow) {
        int changed = 0;
        for (SituationEntity situation : situations.findByStatusInOrderByIdAsc(SituationLifecycle.OPEN)) {
            String next = SituationLifecycle.afterTime(situation.getStatus(), situation.getLastActivityAt(), now,
                    dormancyWindow);
            if (next.equals(situation.getStatus())) {
                continue;
            }
            Map<String, Object> before = state(situation);
            situation.setStatus(next);
            if (SituationLifecycle.CLOSED.equals(next)) {
                situation.setClosedAt(now);
            }
            situation.setUpdatedAt(now);
            situations.saveAndFlush(situation);
            SituationLogEntity row = new SituationLogEntity(situation.getId(), now, LINKER_ACTOR, "status");
            row.setBefore(before);
            row.setAfter(state(situation));
            row.setEvent(SituationLifecycle.CLOSED.equals(next) ? SituationEvent.CLOSED : SituationEvent.DORMANT);
            logs.save(row);
            announce(row, situation, before);
            changed++;
        }
        return changed;
    }

    /**
     * The open situations the linker may join at {@code at}: emerging and active ones active within
     * {@code dormancyWindow}, dormant ones within {@code reopenWindow}.
     */
    public List<SituationEntity> candidates(Instant at, java.time.Duration dormancyWindow,
            java.time.Duration reopenWindow) {
        List<SituationEntity> out = new ArrayList<>();
        java.time.Duration widest = dormancyWindow.compareTo(reopenWindow) > 0 ? dormancyWindow : reopenWindow;
        for (SituationEntity situation : situations.findByStatusInAndLastActivityAtGreaterThanEqualOrderByIdAsc(
                SituationLifecycle.OPEN, at.minus(widest))) {
            java.time.Duration window = SituationLifecycle.DORMANT.equals(situation.getStatus()) ? reopenWindow
                    : dormancyWindow;
            if (!situation.getLastActivityAt().isBefore(at.minus(window))) {
                out.add(situation);
            }
        }
        return out;
    }

    /**
     * The situations that already hold {@code input}, directly or through another member (prompt 118 §2 "shared
     * member"): the input itself, an intelligence event whose pinned members include the input's record, or, for an
     * alert, the record it fired on.
     */
    public Set<String> holders(LinkerInput input) {
        Set<String> out = new TreeSet<>(holders(input.memberType(), input.ref()));
        for (String key : indirectKeys(input)) {
            int bar = key.indexOf('|');
            out.addAll(holders(key.substring(0, bar), key.substring(bar + 1)));
        }
        return out;
    }

    /**
     * The link keys ({@code memberType|ref}) through which another member already covers {@code input}: the
     * intelligence events whose pinned members include its record, and, for an alert, that record.
     */
    public List<String> indirectKeys(LinkerInput input) {
        List<String> out = new ArrayList<>();
        boolean recordLike = SituationMemberTypes.ALERT.equals(input.memberType())
                || SituationMemberTypes.RECORD.equals(input.memberType());
        if (!recordLike || input.sourceId() == null || input.externalId() == null) {
            return out;
        }
        try {
            for (String eventId : jdbc.queryForList("SELECT DISTINCT event_id FROM intelligence_event_member"
                    + " WHERE source_id = ? AND external_id = ? AND role <> 'contradicting'", String.class,
                    input.sourceId(), input.externalId())) {
                out.add(SituationMemberTypes.INTEL_EVENT + "|" + eventId);
            }
        } catch (RuntimeException e) {
            // Without the intelligence tables only the direct and record links apply.
        }
        if (SituationMemberTypes.ALERT.equals(input.memberType())) {
            out.add(SituationMemberTypes.RECORD + "|" + input.sourceId() + "/" + input.externalId());
        }
        return out;
    }

    /** The situation ids that already hold the object with {@code linkKey} ({@code memberType|ref}). */
    public Set<String> holders(String memberType, String ref) {
        Set<String> out = new TreeSet<>();
        for (SituationMemberEntity member : members.findByMemberTypeAndRef(memberType, ref)) {
            out.add(member.getSituationId());
        }
        return out;
    }

    // ------------------------------------------------------------------------- brief storage

    /**
     * Stores a generated brief (prompt 118 §5) and logs the previous one; never touches members, status or
     * severity.
     */
    @Transactional
    public void storeBrief(String id, String brief, String model, String manifestId, Map<String, Object> details) {
        SituationEntity situation = get(id);
        Instant now = clock.instant();
        Map<String, Object> before = new LinkedHashMap<>();
        before.put("brief", situation.getBrief());
        before.put("briefGeneratedAt", situation.getBriefGeneratedAt() == null ? null
                : situation.getBriefGeneratedAt().toString());
        before.put("briefModel", situation.getBriefModel());
        situation.setBrief(brief);
        situation.setBriefGeneratedAt(now);
        situation.setBriefModel(model);
        situation.setBriefManifestId(manifestId);
        situation.setBriefCoreMark(coreCount(id));
        situation.setBriefPending(false);
        situation.setUpdatedAt(now);
        situations.saveAndFlush(situation);
        SituationLogEntity row = new SituationLogEntity(id, now, "llm:" + (model == null ? "unknown" : model), "brief");
        row.setBefore(before);
        Map<String, Object> after = new LinkedHashMap<>(details == null ? Map.of() : details);
        after.put("status", "ok");
        after.put("briefModel", model);
        row.setAfter(after);
        row.setEvent(SituationEvent.UPDATED);
        logs.save(row);
        announce(row, situation, null);
    }

    /** Logs a failed or refused brief attempt; the previous brief stays in place. */
    @Transactional
    public void briefFailed(String id, String reason, boolean keepPending) {
        SituationEntity situation = situations.findById(id).orElse(null);
        if (situation == null) {
            return;
        }
        SituationLogEntity row = new SituationLogEntity(id, clock.instant(), "llm", "brief");
        Map<String, Object> after = new LinkedHashMap<>();
        after.put("status", "failed");
        after.put("reason", reason);
        row.setAfter(after);
        logs.save(row);
        if (situation.isBriefPending() != keepPending) {
            situation.setBriefPending(keepPending);
            situations.save(situation);
        }
    }

    /** Marks a situation's brief as wanted (debounced or queued). */
    @Transactional
    public void markBriefPending(String id, boolean pending) {
        situations.findById(id).ifPresent(situation -> {
            if (situation.isBriefPending() != pending) {
                situation.setBriefPending(pending);
                situations.save(situation);
            }
        });
    }

    /** The number of core members of a situation. */
    public int coreCount(String id) {
        int count = 0;
        for (SituationMemberEntity member : members(id)) {
            if (member.isCore()) {
                count++;
            }
        }
        return count;
    }

    // ------------------------------------------------------------------------- internals

    /** A resolved manual member and its role. */
    private record Resolved(LinkerInput input, String role) {}

    private Resolved resolveMember(JsonNode node, boolean privateAccess) {
        if (node == null || !node.isObject()) {
            throw ValidationException.forField("members", "each member must be a SituationMemberInput object");
        }
        String memberType = node.hasNonNull("memberType") ? node.get("memberType").asString() : null;
        String ref = node.hasNonNull("ref") ? node.get("ref").asString() : null;
        if (memberType == null || !SituationMemberTypes.ALL.contains(memberType)) {
            throw ValidationException.forField("memberType", "must be one of " + SituationMemberTypes.ALL);
        }
        if (ref == null || ref.isBlank()) {
            throw ValidationException.forField("ref", "is required");
        }
        Long seq = null;
        if (node.hasNonNull("seq")) {
            if (!node.get("seq").isIntegralNumber() || node.get("seq").asLong() <= 0) {
                throw ValidationException.forField("seq", "must be a positive integer");
            }
            seq = node.get("seq").asLong();
        }
        if (SituationMemberTypes.recordRef(memberType) && seq == null) {
            throw ValidationException.forField("seq", "is required for " + memberType + " members");
        }
        String role = node.hasNonNull("role") ? node.get("role").asString() : "core";
        if (!SituationMemberTypes.ROLES.contains(role)) {
            throw ValidationException.forField("role", "must be core or context");
        }
        LinkerInput input = reader.resolve(memberType, ref.strip(), seq).orElse(null);
        if (input == null || (!privateAccess && reader.isPrivate(input))) {
            // A private member the caller may not see is reported exactly like a missing one.
            throw memberNotFound("No " + memberType + " " + ref + (seq == null ? "" : " at seq " + seq) + ".");
        }
        return new Resolved(input, role);
    }

    /** The seq a member is pinned by: the version seq of records and published objects, the alert's seq, or null. */
    private static Long pinSeq(LinkerInput input) {
        return input.seq() > 0 ? input.seq() : null;
    }

    private SituationMemberEntity addInternal(SituationEntity situation, LinkerInput input, String role,
            Map<String, Object> joinReason, String actor, Instant now) {
        SituationMemberEntity member = new SituationMemberEntity(Ulids.next(now), situation.getId(),
                input.memberType(), input.ref(), pinSeq(input));
        member.setVersion(input.version());
        member.setKind(input.kind());
        member.setSignalFamily(input.signalFamily());
        member.setTitle(input.title() == null ? null
                : input.title().length() > 512 ? input.title().substring(0, 512) : input.title());
        member.setSeverity(input.severity());
        member.setTheme(input.theme());
        member.setObservedAt(input.observedAt());
        member.setRole(role == null ? "core" : role);
        member.setJoinReason(new LinkedHashMap<>(joinReason));
        member.setGeom(input.geom());
        member.setRegionCodes(input.regionCodes());
        member.setEntityIds(input.entityIds());
        List<String> refs = new ArrayList<>();
        for (ai.foresignal.evidence.RecordRef ref : input.evidenceRefs()) {
            refs.add(ref.sourceId() + "/" + ref.externalId());
        }
        member.setEvidenceRefs(refs);
        member.setSourceId(input.sourceId());
        member.setPrivateMember(reader.isPrivate(input));
        member.setAddedAt(now);
        member.setAddedBy(actor);
        return members.save(member);
    }

    /** The join reason of an operator, agent or analyst action. */
    private static Map<String, Object> manualReason(String origin, String actor) {
        Map<String, Object> reason = new LinkedHashMap<>();
        reason.put("method", "agent".equals(origin) || "analyst".equals(origin) ? origin : "operator");
        reason.put("score", null);
        reason.put("components", null);
        reason.put("linkerVersion", null);
        reason.put("actor", actor);
        return reason;
    }

    /** Moves the members of {@code from} (only {@code onlyIds} when given) to {@code to}, dropping duplicates. */
    private int moveMembers(String from, String to, Set<String> onlyIds) {
        Set<String> pinned = new HashSet<>();
        for (SituationMemberEntity member : members(to)) {
            pinned.add(member.pinKey());
        }
        int moved = 0;
        for (SituationMemberEntity member : members(from)) {
            if (onlyIds != null && !onlyIds.contains(member.getId())) {
                continue;
            }
            if (pinned.add(member.pinKey())) {
                member.setSituationId(to);
                members.save(member);
                moved++;
            } else {
                members.delete(member);
            }
        }
        members.flush();
        return moved;
    }

    /**
     * What one change produced.
     *
     * @param situation the situation after the change
     * @param derivation the derivation
     */
    public record Finished(SituationEntity situation, SituationDeriver.Derivation derivation) {}

    /**
     * Re-derives, saves and logs a change, and announces it after commit.
     *
     * @param before the state before (for the status and severity log rows), or null for a new situation
     * @param action the log action of the change
     * @param logBefore the {@code before} of the main log row, or null
     * @param logAfter the {@code after} of the main log row
     * @param created true for a new situation
     * @param newCoreMember true when a core member was added
     */
    private Finished finish(SituationEntity situation, Map<String, Object> before, String actor, String action,
            Map<String, Object> logBefore, Map<String, Object> logAfter, boolean created, boolean newCoreMember,
            Instant now) {
        String statusBefore = before == null ? null : (String) before.get("status");
        String severityBefore = before == null ? null : (String) before.get("severity");
        List<SituationMemberEntity> current = members(situation.getId());
        String explicitStatus = situation.getStatus();
        SituationDeriver.Derivation derivation = deriver.derive(situation, current, newCoreMember, now);
        if (SituationLifecycle.CLOSED.equals(explicitStatus)) {
            situation.setStatus(SituationLifecycle.CLOSED);
        }
        situation.setUpdatedAt(now);
        situations.saveAndFlush(situation);
        members.saveAll(current);
        SituationLogEntity main = new SituationLogEntity(situation.getId(), now, actor, action);
        main.setBefore(logBefore);
        main.setAfter(logAfter == null ? null : new LinkedHashMap<>(logAfter));
        main.setEvent(created ? SituationEvent.CREATED : SituationEvent.UPDATED);
        logs.save(main);
        List<SituationLogEntity> rows = new ArrayList<>();
        rows.add(main);
        String statusNow = situation.getStatus();
        boolean statusChanged = created ? !SituationLifecycle.EMERGING.equals(statusNow)
                : statusBefore != null && !statusBefore.equals(statusNow);
        if (statusChanged) {
            SituationLogEntity row = new SituationLogEntity(situation.getId(), now, actor, "status");
            row.setBefore(before == null ? Map.of("status", SituationLifecycle.EMERGING) : before);
            row.setAfter(state(situation));
            row.setEvent(switch (statusNow) {
                case SituationLifecycle.ACTIVE -> SituationEvent.ACTIVATED;
                case SituationLifecycle.DORMANT -> SituationEvent.DORMANT;
                case SituationLifecycle.CLOSED -> SituationEvent.CLOSED;
                default -> SituationEvent.UPDATED;
            });
            logs.save(row);
            rows.add(row);
        }
        boolean escalated = !created && severityBefore != null
                && Severities.rank(situation.getSeverity()) > Severities.rank(severityBefore);
        boolean severityChanged = !created && severityBefore != null && !severityBefore.equals(situation.getSeverity());
        if (severityChanged) {
            SituationLogEntity row = new SituationLogEntity(situation.getId(), now, actor, "severity");
            row.setBefore(before);
            row.setAfter(state(situation));
            row.setEvent(escalated ? SituationEvent.ESCALATED : null);
            logs.save(row);
            if (escalated) {
                rows.add(row);
            }
        }
        for (SituationLogEntity row : rows) {
            announce(row, situation, before);
        }
        String id = situation.getId();
        boolean activated = statusChanged && SituationLifecycle.ACTIVE.equals(statusNow);
        int coreGrowth = derivation.coreCount() - situation.getBriefCoreMark();
        afterCommit(() -> {
            SituationBriefService brief = briefs.getIfAvailable();
            if (brief != null) {
                brief.consider(id, activated, severityChanged, coreGrowth);
            }
        });
        return new Finished(situation, derivation);
    }

    /** Publishes the event of a log row after commit (and its notification when it is one of the two). */
    private void announce(SituationLogEntity row, SituationEntity situation, Map<String, Object> before) {
        if (row.getEvent() == null) {
            return;
        }
        Instant now = clock.instant();
        SituationEvent event = new SituationEvent(row.getId(), row.getEvent(), situation.getId(),
                situation.getVersion(), SituationMapper.summary(situation, now),
                before == null ? null : (String) before.get("status"),
                before == null ? null : (String) before.get("severity"), situation.getMergedInto(), row.getAt());
        afterCommit(() -> {
            bus.publish(event);
            if (SituationEvent.ACTIVATED.equals(event.event()) || SituationEvent.ESCALATED.equals(event.event())) {
                SituationNotifications sender = notifications.getIfAvailable();
                if (sender != null) {
                    try {
                        sender.send(event);
                    } catch (RuntimeException e) {
                        log.warn("Could not send the {} notification of {}: {}", event.event(), event.situationId(),
                                e.toString());
                    }
                }
            }
        });
    }

    /** Runs {@code action} after the current transaction commits, or now outside one. */
    static void afterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }

    /** The status, severity, title and theme of a situation, for log rows. */
    static Map<String, Object> state(SituationEntity situation) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("status", situation.getStatus());
        out.put("severity", situation.getSeverity());
        out.put("title", situation.getTitle());
        out.put("theme", situation.getTheme());
        out.put("memberCount", situation.getMemberCount());
        return out;
    }

    private void checkVersion(SituationEntity situation, JsonNode body) {
        if (body != null && body.hasNonNull("version")) {
            if (!body.get("version").isIntegralNumber()) {
                throw ValidationException.forField("version", "must be an integer");
            }
            int version = body.get("version").asInt();
            if (version != situation.getVersion()) {
                throw conflict(situation, version);
            }
        }
    }

    private static ConflictException conflict(SituationEntity situation, int version) {
        return new ConflictException(ErrorCodes.SITUATION_VERSION_CONFLICT, "Situation " + situation.getId()
                + " is at version " + situation.getVersion() + ", not " + version + ".");
    }

    private static ConflictException closed(String id) {
        return new ConflictException(ErrorCodes.SITUATION_CLOSED,
                "Situation " + id + " is closed; closed is final, start a new situation instead.");
    }

    private static NotFoundException unknown(String id) {
        return new NotFoundException(ErrorCodes.UNKNOWN_SITUATION, "No such situation: " + id);
    }

    private static ValidationException memberNotFound(String detail) {
        return new ValidationException(ErrorCodes.SITUATION_MEMBER_NOT_FOUND, detail,
                List.of(new FieldError("members", detail)));
    }

    private static String title(JsonNode body, boolean required) {
        JsonNode node = body.get("title");
        if (node == null || node.isNull()) {
            if (required) {
                throw ValidationException.forField("title", "is required");
            }
            return null;
        }
        String title = node.asString().strip();
        if (title.isEmpty() || title.length() > MAX_TITLE) {
            throw ValidationException.forField("title", "must be 1 to " + MAX_TITLE + " characters");
        }
        return title;
    }

    private static String theme(JsonNode node, boolean required) {
        if (node == null || node.isNull()) {
            if (required) {
                throw ValidationException.forField("theme", "is required");
            }
            return null;
        }
        String theme = node.asString();
        if (!SituationThemes.valid(theme)) {
            throw ValidationException.forField("theme", "must be one of " + SituationThemes.ALL);
        }
        return theme;
    }

    private static String severityOverride(JsonNode body) {
        JsonNode node = body.get("severityOverride");
        if (node == null || node.isNull()) {
            return null;
        }
        String severity = node.asString();
        if (!Severities.valid(severity)) {
            throw ValidationException.forField("severityOverride", "must be info, warning, critical or null");
        }
        return severity;
    }

    private static List<String> regionCodes(JsonNode node) {
        List<String> codes = strings(node, "regionCodes");
        for (String code : codes) {
            if (!REGION_CODE.matcher(code).matches()) {
                throw ValidationException.forField("regionCodes", "'" + code + "' is not a 040 region code");
            }
        }
        return new ArrayList<>(new TreeSet<>(codes));
    }

    private static List<String> strings(JsonNode node, String field) {
        List<String> out = new ArrayList<>();
        if (node == null || node.isNull()) {
            return out;
        }
        if (!node.isArray()) {
            throw ValidationException.forField(field, "must be an array of strings");
        }
        for (JsonNode item : node) {
            if (!item.isString() || item.asString().isBlank()) {
                throw ValidationException.forField(field, "must be an array of strings");
            }
            if (!out.contains(item.asString())) {
                out.add(item.asString());
            }
        }
        return out;
    }

    private static List<String> union(Collection<String> a, Collection<String> b) {
        Set<String> out = new LinkedHashSet<>(a);
        out.addAll(b);
        return new ArrayList<>(out);
    }
}
