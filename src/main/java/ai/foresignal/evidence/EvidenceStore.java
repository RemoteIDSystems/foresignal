package ai.foresignal.evidence;

import ai.foresignal.security.Ulids;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The SQL of the evidence tables (prompt 116): canonical events, observation links and evidence groups. Only
 * the {@code EvidenceResolver} writes through it; {@code EvidenceService} and the controllers read.
 */
@Component
public class EvidenceStore {

    /** The most candidate events read for one observation. */
    static final int CANDIDATE_LIMIT = 200;

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final JdbcTemplate jdbc;
    private final JsonMapper json;

    /** Creates the store. */
    public EvidenceStore(JdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    /**
     * A stored link.
     *
     * @param id the link id
     * @param canonicalEventId the event
     * @param facts the observation's facts
     * @param originatorId the originator
     * @param independenceRoot the originator's root
     * @param upstreamEventId the primary upstream id, or null
     * @param evidenceGroupId the group, or null before grouping
     * @param linkConfidence the confidence
     * @param linkReason the reason
     * @param revised true when the observation was revised after first publication
     * @param linkedAt when it was linked
     */
    public record StoredLink(String id, String canonicalEventId, ObservationFacts facts, String originatorId,
            String independenceRoot, String upstreamEventId, String evidenceGroupId, double linkConfidence,
            Map<String, Object> linkReason, boolean revised, Instant linkedAt) {}

    /**
     * A stored canonical event (summary columns).
     *
     * @param id the id
     * @param family the family
     * @param eventTime the event time
     * @param lon the representative longitude, or null
     * @param lat the representative latitude, or null
     * @param regionCodes the region codes
     * @param bandValue the band value, or null
     * @param firstObservedAt the first observation time
     * @param firstObservationRef the first observation's ref
     * @param observationCount the links
     * @param evidenceGroupCount the groups
     * @param qualityFlags the flags
     * @param sourceIds the distinct sources
     * @param status the status
     * @param mergedInto the survivor, or null
     * @param resolverVersion the resolver version
     * @param createdAt the creation time
     * @param updatedAt the last change
     */
    public record StoredEvent(String id, String family, Instant eventTime, Double lon, Double lat,
            List<String> regionCodes, Double bandValue, Instant firstObservedAt, String firstObservationRef,
            int observationCount, int evidenceGroupCount, List<String> qualityFlags, List<String> sourceIds,
            String status, String mergedInto, String resolverVersion, Instant createdAt, Instant updatedAt) {}

    /**
     * A stored group.
     *
     * @param id the id
     * @param canonicalEventId the event
     * @param originatorRoot the root
     * @param memberCount the members
     * @param firstSeq the lowest member seq
     */
    public record StoredGroup(String id, String canonicalEventId, String originatorRoot, int memberCount, long firstSeq) {}

    // ------------------------------------------------------------------------- reads

    private static final String LINK_COLUMNS = "id, canonical_event_id, source_id, external_id, seq, version,"
            + " originator_id, independence_root, upstream_event_id, evidence_group_id, link_confidence,"
            + " link_reason::text AS link_reason, event_time, observed_at, lon, lat, attributes::text AS attributes,"
            + " linked_at";

    private static final String EVENT_COLUMNS = "id, family, event_time, ST_X(ST_PointOnSurface(geom)) AS lon,"
            + " ST_Y(ST_PointOnSurface(geom)) AS lat, region_codes, band_value, first_observed_at,"
            + " first_observation_ref, observation_count, evidence_group_count, quality_flags, source_ids, status,"
            + " merged_into, resolver_version, created_at, updated_at";

    /** The links of an event, by seq. */
    public List<StoredLink> links(String canonicalEventId) {
        return jdbc.query("SELECT " + LINK_COLUMNS + " FROM observation_link WHERE canonical_event_id = ? ORDER BY seq, id",
                (rs, i) -> link(rs), canonicalEventId);
    }

    /** The links of a record, newest event first. */
    public List<StoredLink> linksOf(String sourceId, String externalId) {
        return jdbc.query("SELECT " + LINK_COLUMNS + " FROM observation_link WHERE source_id = ? AND external_id = ?"
                + " ORDER BY linked_at DESC, id DESC", (rs, i) -> link(rs), sourceId, externalId);
    }

    /** One event. */
    public Optional<StoredEvent> event(String id) {
        List<StoredEvent> rows = jdbc.query("SELECT " + EVENT_COLUMNS + " FROM canonical_event WHERE id = ?",
                (rs, i) -> event(rs), id);
        return rows.stream().findFirst();
    }

    /** Events by id, in no particular order. */
    public List<StoredEvent> events(Collection<String> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return jdbc.query("SELECT " + EVENT_COLUMNS + " FROM canonical_event WHERE id = ANY(?)", (rs, i) -> event(rs),
                (Object) ids.toArray(String[]::new));
    }

    /**
     * Events of {@code families} updated at or after {@code since} (merged ones included, so their derived
     * records can be retired), newest first.
     */
    public List<StoredEvent> updatedSince(Collection<String> families, Instant since, int limit) {
        if (families.isEmpty()) {
            return List.of();
        }
        return jdbc.query("SELECT " + EVENT_COLUMNS + " FROM canonical_event WHERE family = ANY(?) AND updated_at >= ?"
                        + " ORDER BY updated_at DESC, id DESC LIMIT ?", (rs, i) -> event(rs),
                families.toArray(String[]::new), Timestamp.from(since), limit);
    }

    /** The groups of an event. */
    public List<StoredGroup> groups(String canonicalEventId) {
        return jdbc.query("SELECT id, canonical_event_id, originator_root, member_count, first_seq FROM evidence_group"
                        + " WHERE canonical_event_id = ? ORDER BY first_seq, id",
                (rs, i) -> new StoredGroup(rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4),
                        rs.getLong(5)), canonicalEventId);
    }

    /**
     * Open events of {@code family} with a link whose event time lies in {@code [from, to]} and, when a box is
     * given, whose point lies in it (or has none).
     */
    public List<String> candidates(String family, Instant from, Instant to, double[] box) {
        List<Object> args = new ArrayList<>(List.of(family, Timestamp.from(from), Timestamp.from(to)));
        String spatial = "";
        if (box != null) {
            spatial = " AND (l.lon IS NULL OR (l.lon BETWEEN ? AND ? AND l.lat BETWEEN ? AND ?))";
            args.add(box[0]);
            args.add(box[2]);
            args.add(box[1]);
            args.add(box[3]);
        }
        args.add(CANDIDATE_LIMIT);
        return jdbc.queryForList("SELECT DISTINCT c.id, c.created_at FROM canonical_event c JOIN observation_link l"
                        + " ON l.canonical_event_id = c.id WHERE c.family = ? AND c.status = 'open'"
                        + " AND l.event_time BETWEEN ? AND ?" + spatial + " ORDER BY c.created_at, c.id LIMIT ?",
                args.toArray()).stream().map(row -> String.valueOf(row.get("id"))).toList();
    }

    /** Open events of {@code family} with a link carrying one of {@code upstreamIds}. */
    public List<String> byUpstream(String family, Collection<String> upstreamIds) {
        if (upstreamIds.isEmpty()) {
            return List.of();
        }
        return jdbc.queryForList("SELECT DISTINCT c.id, c.created_at FROM canonical_event c JOIN observation_link l"
                        + " ON l.canonical_event_id = c.id WHERE c.family = ? AND c.status = 'open'"
                        + " AND (l.upstream_event_id = ANY(?) OR jsonb_exists_any(COALESCE(l.attributes -> 'upstreamIds', '[]'::jsonb), ?))"
                        + " ORDER BY c.created_at, c.id LIMIT " + CANDIDATE_LIMIT,
                family, upstreamIds.toArray(String[]::new), upstreamIds.toArray(String[]::new)).stream()
                .map(row -> String.valueOf(row.get("id"))).toList();
    }

    // ------------------------------------------------------------------------- writes

    /** Inserts a new open event and returns its id. */
    public String insertEvent(String family, ObservationFacts first, List<String> regionCodes, Instant now) {
        String id = Ulids.next(now);
        String wkt = first.located() ? "POINT(" + first.lon() + " " + first.lat() + ")" : null;
        jdbc.update("INSERT INTO canonical_event (id, family, event_time, geom, region_codes, band_value,"
                        + " first_observed_at, first_observation_ref, observation_count, evidence_group_count,"
                        + " quality_flags, source_ids, status, resolver_version, created_at, updated_at)"
                        + " VALUES (?, ?, ?, ST_GeomFromText(CAST(? AS text), 4326), ?, ?, ?,"
                        + " ?, 0, 0, '{}', '{}', 'open', ?, ?, ?)",
                id, family, Timestamp.from(first.eventTime() == null ? first.observedAt() : first.eventTime()), wkt,
                regionCodes.toArray(String[]::new), first.attribute(),
                Timestamp.from(first.observedAt() == null ? now : first.observedAt()), first.ref(),
                EvidenceMatcher.RESOLVER_VERSION, Timestamp.from(now), Timestamp.from(now));
        return id;
    }

    /** Inserts a link. */
    public void insertLink(String canonicalEventId, ObservationFacts facts, Originator originator, double confidence,
            Map<String, Object> reason, Instant now) {
        jdbc.update("INSERT INTO observation_link (id, canonical_event_id, source_id, external_id, seq, version,"
                        + " originator_id, independence_root, upstream_event_id, link_confidence, link_reason,"
                        + " event_time, observed_at, lon, lat, attributes, linked_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,"
                        + " ?, CAST(? AS jsonb), ?, ?, ?, ?, CAST(? AS jsonb), ?)"
                        + " ON CONFLICT (source_id, external_id, canonical_event_id) DO NOTHING",
                Ulids.next(now), canonicalEventId, facts.sourceId(), facts.externalId(), facts.seq(), facts.version(),
                originator.id(), originator.root(), facts.upstreamIds().isEmpty() ? null : facts.upstreamIds().get(0),
                confidence, json.writeValueAsString(reason), ts(facts.eventTime()), ts(facts.observedAt()), facts.lon(),
                facts.lat(), json.writeValueAsString(facts.attributes()), Timestamp.from(now));
    }

    /** Updates an existing link to a newer version of its record. */
    public void updateLink(String linkId, ObservationFacts facts, Originator originator, boolean revised, Instant now) {
        Map<String, Object> attributes = new LinkedHashMap<>(facts.attributes());
        if (revised) {
            attributes.put("revised", true);
        }
        jdbc.update("UPDATE observation_link SET seq = ?, version = ?, originator_id = ?, independence_root = ?,"
                        + " upstream_event_id = ?, event_time = ?, observed_at = ?, lon = ?, lat = ?,"
                        + " attributes = CAST(? AS jsonb), linked_at = ? WHERE id = ?",
                facts.seq(), facts.version(), originator.id(), originator.root(),
                facts.upstreamIds().isEmpty() ? null : facts.upstreamIds().get(0), ts(facts.eventTime()),
                ts(facts.observedAt()), facts.lon(), facts.lat(), json.writeValueAsString(attributes),
                Timestamp.from(now), linkId);
    }

    /**
     * Replaces an event's groups: existing group ids are kept for an unchanged root, links point at their group,
     * and the summary columns are rewritten.
     *
     * @param groups the groups, with link ids as member keys
     * @return the group id of each root
     */
    public Map<String, String> writeGroups(String canonicalEventId, List<EvidenceGrouper.Group> groups, Instant now) {
        Map<String, String> existing = new LinkedHashMap<>();
        for (StoredGroup group : groups(canonicalEventId)) {
            existing.put(group.originatorRoot(), group.id());
        }
        Map<String, String> ids = new LinkedHashMap<>();
        for (EvidenceGrouper.Group group : groups) {
            String id = existing.remove(group.root());
            if (id == null) {
                id = Ulids.next(now);
                jdbc.update("INSERT INTO evidence_group (id, canonical_event_id, originator_root, member_count, first_seq)"
                        + " VALUES (?, ?, ?, ?, ?)", id, canonicalEventId, group.root(), group.members().size(),
                        group.firstSeq());
            } else {
                jdbc.update("UPDATE evidence_group SET member_count = ?, first_seq = ? WHERE id = ?",
                        group.members().size(), group.firstSeq(), id);
            }
            ids.put(group.root(), id);
            jdbc.update("UPDATE observation_link SET evidence_group_id = ? WHERE id = ANY(?)", id,
                    group.members().toArray(String[]::new));
        }
        for (String stale : existing.values()) {
            jdbc.update("DELETE FROM evidence_group WHERE id = ?", stale);
        }
        return ids;
    }

    /** Rewrites an event's summary columns. */
    public void updateSummary(String canonicalEventId, int observations, int groups, List<String> flags,
            List<String> sources, Instant now) {
        jdbc.update("UPDATE canonical_event SET observation_count = ?, evidence_group_count = ?, quality_flags = ?,"
                        + " source_ids = ?, resolver_version = ?, updated_at = ? WHERE id = ?",
                observations, groups, flags.toArray(String[]::new), sources.toArray(String[]::new),
                EvidenceMatcher.RESOLVER_VERSION, Timestamp.from(now), canonicalEventId);
    }

    /**
     * Moves every link of {@code loser} to {@code survivor} (a record already linked to the survivor keeps the
     * survivor's link), deletes the loser's groups and marks it merged.
     */
    public void merge(String loser, String survivor, Instant now) {
        jdbc.update("DELETE FROM observation_link l WHERE l.canonical_event_id = ? AND EXISTS (SELECT 1 FROM"
                + " observation_link s WHERE s.canonical_event_id = ? AND s.source_id = l.source_id"
                + " AND s.external_id = l.external_id)", loser, survivor);
        jdbc.update("UPDATE observation_link SET canonical_event_id = ?, evidence_group_id = NULL"
                + " WHERE canonical_event_id = ?", survivor, loser);
        jdbc.update("DELETE FROM evidence_group WHERE canonical_event_id = ?", loser);
        jdbc.update("UPDATE canonical_event SET status = 'merged', merged_into = ?, observation_count = 0,"
                + " evidence_group_count = 0, updated_at = ? WHERE id = ?", survivor, Timestamp.from(now), loser);
        jdbc.update("UPDATE canonical_event SET merged_into = ?, updated_at = ? WHERE merged_into = ?", survivor,
                Timestamp.from(now), loser);
    }

    // ------------------------------------------------------------------------- mapping

    private StoredLink link(ResultSet rs) throws SQLException {
        Map<String, Object> attributes = map(rs.getString("attributes"));
        ObservationFacts facts = ObservationFacts.fromStored(rs.getString("source_id"), rs.getString("external_id"),
                rs.getLong("seq"), (Integer) rs.getObject("version"), instant(rs, "event_time"),
                instant(rs, "observed_at"), (Double) rs.getObject("lon"), (Double) rs.getObject("lat"), attributes);
        return new StoredLink(rs.getString("id"), rs.getString("canonical_event_id"), facts,
                rs.getString("originator_id"), rs.getString("independence_root"), rs.getString("upstream_event_id"),
                rs.getString("evidence_group_id"), rs.getDouble("link_confidence"), map(rs.getString("link_reason")),
                Boolean.TRUE.equals(attributes.get("revised")), instant(rs, "linked_at"));
    }

    private StoredEvent event(ResultSet rs) throws SQLException {
        return new StoredEvent(rs.getString("id"), rs.getString("family"), instant(rs, "event_time"),
                (Double) rs.getObject("lon"), (Double) rs.getObject("lat"), strings(rs.getArray("region_codes")),
                (Double) rs.getObject("band_value"), instant(rs, "first_observed_at"),
                rs.getString("first_observation_ref"), rs.getInt("observation_count"),
                rs.getInt("evidence_group_count"), strings(rs.getArray("quality_flags")),
                strings(rs.getArray("source_ids")), rs.getString("status"), rs.getString("merged_into"),
                rs.getString("resolver_version"), instant(rs, "created_at"), instant(rs, "updated_at"));
    }

    private Map<String, Object> map(String text) {
        return text == null ? new LinkedHashMap<>() : json.readValue(text, MAP);
    }

    static List<String> strings(Array array) throws SQLException {
        if (array == null) {
            return List.of();
        }
        Object[] values = (Object[]) array.getArray();
        List<String> out = new ArrayList<>(values.length);
        for (Object value : values) {
            if (value != null) {
                out.add(String.valueOf(value));
            }
        }
        return out;
    }

    static Instant instant(ResultSet rs, String column) throws SQLException {
        OffsetDateTime value = rs.getObject(column, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }

    private static Timestamp ts(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }
}
