package ai.foresignal.evidence;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * The read side of evidence identity for the API and MCP (prompt 116): canonical event pages and details, and
 * the evidence identity of one record. Observations from sources the caller may not see are left out; an event
 * built only from such sources does not exist for the caller ({@code 404 UNKNOWN_CANONICAL_EVENT}).
 */
@Service
public class EvidenceQueries {

    /** The default page size. */
    public static final int DEFAULT_LIMIT = 100;

    /** The largest page size. */
    public static final int MAX_LIMIT = 500;

    private final JdbcTemplate jdbc;
    private final EvidenceStore store;
    private final OriginatorRegistry originators;

    /** Creates the service. */
    public EvidenceQueries(JdbcTemplate jdbc, EvidenceStore store, OriginatorRegistry originators) {
        this.jdbc = jdbc;
        this.store = store;
        this.originators = originators;
    }

    /**
     * A page of canonical events, newest event time first.
     *
     * @param family only this family, or null
     * @param region only events tagged with this region code, or null
     * @param since only events with {@code eventTime ≥ since}, or null
     * @param flags only events carrying all of these flags
     * @param cursor the previous page's {@code nextCursor}, or null
     * @param limit the page size (1–500)
     * @param excluded sources the caller may not see
     */
    public Map<String, Object> list(String family, String region, Instant since, List<String> flags, String cursor,
            int limit, Set<String> excluded) {
        int size = Math.max(1, Math.min(MAX_LIMIT, limit <= 0 ? DEFAULT_LIMIT : limit));
        for (String flag : flags) {
            if (!QualityFlags.ALL.contains(flag)) {
                throw ValidationException.forField("flags", "unknown quality flag " + flag);
            }
        }
        StringBuilder sql = new StringBuilder("SELECT id FROM canonical_event WHERE status <> 'merged'");
        List<Object> args = new ArrayList<>();
        if (family != null) {
            sql.append(" AND family = ?");
            args.add(family);
        }
        if (region != null) {
            sql.append(" AND ? = ANY(region_codes)");
            args.add(region);
        }
        if (since != null) {
            sql.append(" AND event_time >= ?");
            args.add(Timestamp.from(since));
        }
        if (!flags.isEmpty()) {
            sql.append(" AND quality_flags @> CAST(? AS text[])");
            args.add(flags.toArray(String[]::new));
        }
        if (!excluded.isEmpty()) {
            sql.append(" AND NOT (source_ids <@ CAST(? AS text[]))");
            args.add(excluded.toArray(String[]::new));
        }
        if (cursor != null) {
            String[] parts = decode(cursor);
            sql.append(" AND (event_time, id) < (?, ?)");
            args.add(Timestamp.from(Instant.ofEpochMilli(Long.parseLong(parts[0]))));
            args.add(parts[1]);
        }
        sql.append(" ORDER BY event_time DESC, id DESC LIMIT ?");
        args.add(size + 1);
        List<String> ids = jdbc.queryForList(sql.toString(), String.class, args.toArray());
        boolean more = ids.size() > size;
        List<String> page = more ? ids.subList(0, size) : ids;
        Map<String, EvidenceStore.StoredEvent> byId = new LinkedHashMap<>();
        for (EvidenceStore.StoredEvent event : store.events(page)) {
            byId.put(event.id(), event);
        }
        List<Map<String, Object>> items = new ArrayList<>();
        EvidenceStore.StoredEvent last = null;
        for (String id : page) {
            EvidenceStore.StoredEvent event = byId.get(id);
            if (event != null) {
                items.add(summary(event));
                last = event;
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", more && last != null ? encode(last.eventTime().toEpochMilli() + "|" + last.id()) : null);
        return out;
    }

    /**
     * One canonical event with its visible observations (originators included), evidence groups and flags.
     *
     * @throws NotFoundException 404 {@code UNKNOWN_CANONICAL_EVENT}
     */
    public Map<String, Object> detail(String id, Set<String> excluded) {
        EvidenceStore.StoredEvent event = store.event(id).orElseThrow(() -> unknown(id));
        if (!visible(event, excluded)) {
            throw unknown(id);
        }
        Map<String, Object> out = summary(event);
        List<Map<String, Object>> observations = new ArrayList<>();
        for (EvidenceStore.StoredLink link : store.links(id)) {
            if (!excluded.contains(link.facts().sourceId())) {
                observations.add(link(link, true));
            }
        }
        out.put("observations", observations);
        List<Map<String, Object>> groups = new ArrayList<>();
        for (EvidenceStore.StoredGroup group : store.groups(id)) {
            groups.add(group(group));
        }
        out.put("evidenceGroups", groups);
        return out;
    }

    /**
     * The evidence identity of a record: its originator, its links and the canonical events they point to.
     *
     * @throws NotFoundException 404 when the caller may not see the source or the record does not exist
     */
    public Map<String, Object> ofRecord(String sourceId, String externalId, Set<String> excluded) {
        if (excluded.contains(sourceId)) {
            throw new NotFoundException("Record not found: " + sourceId + "/" + externalId);
        }
        List<EvidenceStore.StoredLink> links = store.linksOf(sourceId, externalId);
        if (links.isEmpty()) {
            Integer heads = jdbc.queryForObject("SELECT count(*) FROM record_head WHERE source_id = ? AND external_id = ?",
                    Integer.class, sourceId, externalId);
            if (heads == null || heads == 0) {
                throw new NotFoundException("Record not found: " + sourceId + "/" + externalId);
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sourceId", sourceId);
        out.put("externalId", externalId);
        out.put("originator", originator(originators.resolve(sourceId)));
        List<Map<String, Object>> linkViews = new ArrayList<>();
        List<String> eventIds = new ArrayList<>();
        for (EvidenceStore.StoredLink link : links) {
            linkViews.add(link(link, false));
            eventIds.add(link.canonicalEventId());
        }
        out.put("links", linkViews);
        List<Map<String, Object>> events = new ArrayList<>();
        for (EvidenceStore.StoredEvent event : store.events(eventIds)) {
            events.add(summary(event));
        }
        out.put("canonicalEvents", events);
        return out;
    }

    // ------------------------------------------------------------------------- views

    /** The {@code CanonicalEvent} summary (without observations and groups). */
    public static Map<String, Object> summary(EvidenceStore.StoredEvent event) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", event.id());
        out.put("ref", "canonical-event:" + event.id());
        out.put("family", event.family());
        out.put("eventTime", event.eventTime());
        out.put("geometry", event.lon() == null || event.lat() == null ? null
                : Map.of("type", "Point", "coordinates", List.of(event.lon(), event.lat())));
        out.put("regionCodes", event.regionCodes());
        out.put("firstObservedAt", event.firstObservedAt());
        out.put("firstObservationRef", event.firstObservationRef());
        out.put("observationCount", event.observationCount());
        out.put("evidenceGroupCount", event.evidenceGroupCount());
        out.put("qualityFlags", event.qualityFlags());
        out.put("status", event.status());
        out.put("mergedInto", event.mergedInto());
        out.put("resolverVersion", event.resolverVersion());
        out.put("createdAt", event.createdAt());
        out.put("updatedAt", event.updatedAt());
        return out;
    }

    /** The {@code ObservationLink} shape. */
    Map<String, Object> link(EvidenceStore.StoredLink link, boolean withOriginator) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("canonicalEventId", link.canonicalEventId());
        out.put("recordRef", RecordRef.format(link.facts().sourceId(), link.facts().externalId(), link.facts().seq()));
        out.put("sourceId", link.facts().sourceId());
        out.put("externalId", link.facts().externalId());
        out.put("seq", link.facts().seq());
        out.put("version", link.facts().version());
        out.put("originatorId", link.originatorId());
        if (withOriginator) {
            out.put("originator", originator(originators.originator(link.originatorId())
                    .orElseGet(() -> originators.resolve(link.facts().sourceId()))));
        }
        out.put("upstreamEventId", link.upstreamEventId());
        out.put("evidenceGroupId", link.evidenceGroupId());
        out.put("linkConfidence", link.linkConfidence());
        out.put("linkReason", link.linkReason());
        out.put("linkedAt", link.linkedAt());
        return out;
    }

    /** The {@code EvidenceGroup} shape. */
    static Map<String, Object> group(EvidenceStore.StoredGroup group) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", group.id());
        out.put("canonicalEventId", group.canonicalEventId());
        out.put("originatorRoot", group.originatorRoot());
        out.put("memberCount", group.memberCount());
        out.put("firstSeq", group.firstSeq());
        return out;
    }

    /** The {@code Originator} shape. */
    public static Map<String, Object> originator(Originator originator) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", originator.id());
        out.put("class", originator.originatorClass());
        out.put("independenceGroup", originator.independenceGroup());
        out.put("aggregates", originator.aggregates());
        out.put("notes", originator.notes());
        return out;
    }

    private static boolean visible(EvidenceStore.StoredEvent event, Set<String> excluded) {
        if (event.sourceIds().isEmpty()) {
            return true;
        }
        for (String source : event.sourceIds()) {
            if (!excluded.contains(source)) {
                return true;
            }
        }
        return false;
    }

    private static NotFoundException unknown(String id) {
        return new NotFoundException(ErrorCodes.UNKNOWN_CANONICAL_EVENT, "No such canonical event: " + id);
    }

    private static String encode(String text) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    private static String[] decode(String cursor) {
        try {
            String text = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = text.split("\\|", 2);
            Long.parseLong(parts[0]);
            if (parts.length != 2) {
                throw new IllegalArgumentException("cursor");
            }
            return parts;
        } catch (RuntimeException e) {
            throw ValidationException.forField("cursor", "is not a cursor from a previous page");
        }
    }
}
