package ai.foresignal.trigger;

import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.ask.CitationSet;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.search.DocInput;
import ai.foresignal.search.SearchRequest;
import ai.foresignal.search.SearchService;
import ai.foresignal.search.SearchStatusService;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.store.query.PgArrays;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Gathers what Foresignal knows around an alert for its enrichment (prompt 030, Architecture §23).
 *
 * <p>The anchor time is {@code T = alert.observedAt} (else {@code firedAt}); the anchor geometry is
 * the alerted version's, loaded by {@code seq}, else the alert's {@code record_snapshot} geometry
 * once retention has removed that version (Core-Data-Model §11). The context holds, in this order:
 *
 * <ol>
 *   <li>the alert and the alerted record (version, or snapshot), never trimmed;
 *   <li>{@code history}: up to {@value #HISTORY_LIMIT} prior versions of the same entity, newest
 *       first, {@code HEARTBEAT} excluded, short form with {@code persistReason};
 *   <li>{@code nearby.records}: records of any kind within the radius and {@code T ± window}: the
 *       latest version in the window per entity ({@code DISTINCT ON (source_id, external_id)},
 *       {@code HEARTBEAT} excluded, the alerted entity excluded), plus current head ⋈ live rows inside
 *       the radius whose live {@code observed_at} is in the window (movement that never made a
 *       version), the newest {@value #NEARBY_LIMIT};
 *   <li>{@code nearby.alerts}: up to {@value #ALERT_LIMIT} other alerts in the radius and window
 *       (by their snapshot geometry), with state;
 *   <li>{@code related}: when semantic search is available, the top {@value #SEMANTIC_LIMIT} hybrid
 *       matches for the alert message in the radius and window;
 *   <li>{@code sections}: one per {@link EnrichmentContextContributor}, in order.
 * </ol>
 *
 * <p>A record without geometry skips the spatial parts (nearby records and alerts) and uses its
 * history and the semantic matches only. Every record and alert passes {@link EgressPolicy} for the
 * destination: sources it withholds (private sources for a non-local provider) are excluded in SQL
 * and filtered again item by item. The serialized context is capped at
 * {@link EnrichmentProperties#maxContextBytes()} (24 KB); the lowest-ranked items go first: related
 * matches, then nearby records, nearby alerts, history and finally whole contributor sections, each
 * from its end. Only what is left is citable.
 */
@Component
public class EnrichmentContextBuilder {

    private static final Logger log = LoggerFactory.getLogger(EnrichmentContextBuilder.class);

    /** Prior versions of the alerted entity in the context. */
    public static final int HISTORY_LIMIT = 10;

    /** Nearby records in the context. */
    public static final int NEARBY_LIMIT = 30;

    /** Nearby alerts in the context. */
    public static final int ALERT_LIMIT = 10;

    /** Semantic matches in the context. */
    public static final int SEMANTIC_LIMIT = 5;

    /** Search hits fetched before the radius and egress filters. */
    static final int SEMANTIC_FETCH = 20;

    /** The longest search query taken from the alert message. */
    static final int MAX_QUERY = 500;

    /** Meters per degree of latitude (and of longitude at the equator). */
    static final double METERS_PER_DEGREE = 111_320.0;

    private static final short HEARTBEAT = PersistReason.HEARTBEAT.code();

    static final String ANCHOR = "ST_SetSRID(ST_GeomFromGeoJSON(:anchor), 4326)";

    static final String HISTORY_SQL = "SELECT v.seq FROM record_version v WHERE v.source_id = :source"
            + " AND v.external_id = :externalId AND v.seq < :seq AND v.persist_reason <> " + HEARTBEAT
            + " ORDER BY v.seq DESC LIMIT " + HISTORY_LIMIT;

    private final NamedParameterJdbcTemplate jdbc;
    private final RecordQueryService records;
    private final RecordMapper recordMapper;
    private final McpOutputShaper shaper;
    private final AlertStore alerts;
    private final SearchService search;
    private final SearchStatusService searchStatus;
    private final EgressPolicy egress;
    private final ObjectProvider<EnrichmentContextContributor> contributors;
    private final EnrichmentProperties properties;
    private final JsonMapper json;
    private final GeoJsonReader geoJson = new GeoJsonReader();

    /** Creates the builder. */
    public EnrichmentContextBuilder(NamedParameterJdbcTemplate jdbc, RecordQueryService records,
            RecordMapper recordMapper, McpOutputShaper shaper, AlertStore alerts, SearchService search,
            SearchStatusService searchStatus, EgressPolicy egress,
            ObjectProvider<EnrichmentContextContributor> contributors, EnrichmentProperties properties,
            JsonMapper json) {
        this.jdbc = jdbc;
        this.records = records;
        this.recordMapper = recordMapper;
        this.shaper = shaper;
        this.alerts = alerts;
        this.search = search;
        this.searchStatus = searchStatus;
        this.egress = egress;
        this.contributors = contributors;
        this.properties = properties;
        this.json = json;
    }

    /** One context item: its JSON and what it lets the model cite. */
    private record Item(JsonNode node, Consumer<CitationSet> cite) {}

    /**
     * Builds the context of {@code alert}.
     *
     * @param alert the alert
     * @param radiusKm the rule's {@code enrichRadiusKm}
     * @param window the rule's {@code enrichWindow}
     * @param destination the chat provider the context is for
     * @return the context, within the size cap, with what it makes citable
     */
    public EnrichmentContext build(AlertView alert, double radiusKm, Duration window,
            EgressPolicy.LlmDestination destination) {
        Set<String> excluded = new HashSet<>(egress.llmExcludedSources(destination));
        RecordRow version = records.versionsBySeq(List.of(alert.seq())).get(alert.seq());
        ObjectNode snapshot = snapshot(alert);
        String geometry = version != null && version.geometry() != null ? version.geometry() : snapshotGeometry(snapshot);
        Instant time = alert.observedAt() != null ? alert.observedAt() : alert.firedAt();
        EnrichmentAnchor anchor = new EnrichmentAnchor(alert, geometry, time, radiusKm, window, destination, excluded);

        List<String> notes = new ArrayList<>();
        ObjectNode alertNode = alertNode(alert);
        ObjectNode recordNode;
        if (version != null) {
            recordNode = shaper.record(recordMapper.record(version, FieldSelection.ALL, RecordMapper.Shape.VERSION),
                    McpOutputShaper.Detail.SHORT);
            recordNode.put("from", "version");
        } else if (snapshot != null) {
            ObjectNode full = snapshot.deepCopy();
            full.put("id", DocInput.recordId(alert.sourceId(), alert.externalId()));
            full.put("source", alert.sourceId());
            full.put("externalId", alert.externalId());
            full.put("seq", alert.seq());
            recordNode = shaper.shortRecord(full);
            recordNode.put("from", "snapshot");
        } else {
            recordNode = json.createObjectNode();
            recordNode.put("id", DocInput.recordId(alert.sourceId(), alert.externalId()));
            recordNode.put("seq", alert.seq());
            recordNode.put("from", "none");
        }
        String recordId = DocInput.recordId(alert.sourceId(), alert.externalId());
        recordNode.put("cite", cite(recordId, alert.seq()));

        List<Item> history = history(alert);
        List<Item> nearbyRecords = List.of();
        List<Item> nearbyAlerts = List.of();
        Optional<Envelope> envelope = Optional.empty();
        if (anchor.spatial()) {
            envelope = envelope(geometry, anchor.radiusMeters());
            nearbyRecords = nearbyRecords(anchor, envelope);
            nearbyAlerts = nearbyAlerts(anchor, envelope);
        } else {
            notes.add("the record has no geometry: nearby records and alerts were not gathered");
        }
        Set<String> present = new HashSet<>();
        for (Item item : nearbyRecords) {
            present.add(item.node().path("id").asString("") + "@" + item.node().path("seq").asLong(-1));
        }
        List<Item> related = related(anchor, envelope, present, notes);
        List<Map.Entry<String, Item>> sections = sections(anchor);

        // Trim the lowest-ranked items first until the serialized context fits.
        List<List<?>> trimOrder = new ArrayList<>();
        List<Item> relatedKept = new ArrayList<>(related);
        List<Item> nearbyKept = new ArrayList<>(nearbyRecords);
        List<Item> alertsKept = new ArrayList<>(nearbyAlerts);
        List<Item> historyKept = new ArrayList<>(history);
        List<Map.Entry<String, Item>> sectionsKept = new ArrayList<>(sections);
        trimOrder.add(relatedKept);
        trimOrder.add(nearbyKept);
        trimOrder.add(alertsKept);
        trimOrder.add(historyKept);
        trimOrder.add(sectionsKept);
        int trimmed = 0;
        String text = serialize(anchor, alertNode, recordNode, historyKept, nearbyKept, alertsKept, relatedKept,
                sectionsKept, notes, 0);
        while (bytes(text) > properties.maxContextBytes()) {
            List<?> victim = trimOrder.stream().filter(l -> !l.isEmpty()).findFirst().orElse(null);
            if (victim == null) {
                break;
            }
            victim.remove(victim.size() - 1);
            trimmed++;
            text = serialize(anchor, alertNode, recordNode, historyKept, nearbyKept, alertsKept, relatedKept,
                    sectionsKept, notes, trimmed);
        }
        if (bytes(text) > properties.maxContextBytes()) {
            // Only the alert and its record are left and they are still too large: drop the record's fields.
            recordNode.remove("fields");
            text = serialize(anchor, alertNode, recordNode, historyKept, nearbyKept, alertsKept, relatedKept,
                    sectionsKept, notes, trimmed);
        }

        CitationSet citable = new CitationSet();
        citable.addAlert(Long.toString(alert.id()), alertTitle(alert));
        citable.addRecord(recordId, alert.seq(), textOrNull(recordNode, "title"));
        for (List<Item> list : List.of(historyKept, nearbyKept, alertsKept, relatedKept)) {
            for (Item item : list) {
                item.cite().accept(citable);
            }
        }
        for (Map.Entry<String, Item> section : sectionsKept) {
            section.getValue().cite().accept(citable);
        }
        citable.addText(text);
        EnrichmentContext.Counts counts = new EnrichmentContext.Counts(historyKept.size(), nearbyKept.size(),
                alertsKept.size(), relatedKept.size(), sectionsKept.size());
        return new EnrichmentContext(anchor, text, citable, counts, trimmed, notes);
    }

    // ------------------------------------------------------------------------- parts

    private ObjectNode alertNode(AlertView alert) {
        ObjectNode node = json.createObjectNode();
        node.put("cite", "[alert:" + alert.id() + "]");
        node.put("id", alert.id());
        node.put("rule", alert.ruleName());
        node.put("severity", alert.severity());
        node.put("state", alert.state());
        node.put("message", alert.message());
        node.put("record", alert.sourceId() + ":" + alert.externalId());
        node.put("kind", alert.kind());
        node.put("observedAt", alert.observedAt() == null ? null : alert.observedAt().toString());
        node.put("firedAt", alert.firedAt() == null ? null : alert.firedAt().toString());
        return node;
    }

    /** Up to {@value #HISTORY_LIMIT} prior versions, newest first, without heartbeats. */
    List<Item> history(AlertView alert) {
        List<Long> seqs = jdbc.queryForList(HISTORY_SQL, new MapSqlParameterSource("source", alert.sourceId())
                .addValue("externalId", alert.externalId()).addValue("seq", alert.seq()), Long.class);
        Map<Long, RecordRow> rows = records.versionsBySeq(seqs);
        List<Item> out = new ArrayList<>();
        for (Long seq : seqs) {
            RecordRow row = rows.get(seq);
            if (row != null) {
                out.add(recordItem(row, RecordMapper.Shape.VERSION, false));
            }
        }
        return out;
    }

    /** Records of any kind within the radius and window, newest first. */
    List<Item> nearbyRecords(EnrichmentAnchor anchor, Optional<Envelope> envelope) {
        MapSqlParameterSource p = spatialParams(anchor, envelope);
        String excluded = excludedClause("v.source_id", anchor, p);
        String prefilter = envelope.isPresent() ? " AND v.geom && ST_MakeEnvelope(:minx, :miny, :maxx, :maxy, 4326)" : "";
        List<Long> seqs = jdbc.queryForList("SELECT seq FROM (SELECT DISTINCT ON (v.source_id, v.external_id)"
                + " v.seq, v.observed_at FROM record_version v WHERE v.geom IS NOT NULL" + prefilter
                + " AND ST_DWithin(v.geom::geography, " + ANCHOR + "::geography, :radius)"
                + " AND v.observed_at BETWEEN :from AND :to AND v.persist_reason <> " + HEARTBEAT
                + " AND NOT (v.source_id = :source AND v.external_id = :externalId)" + excluded
                + " ORDER BY v.source_id, v.external_id, v.observed_at DESC, v.seq DESC) latest"
                + " ORDER BY observed_at DESC, seq DESC LIMIT " + NEARBY_LIMIT, p, Long.class);
        Map<Long, RecordRow> versions = records.versionsBySeq(seqs);
        Map<String, RecordRow> byEntity = new LinkedHashMap<>();
        for (Long seq : seqs) {
            RecordRow row = versions.get(seq);
            if (row != null) {
                byEntity.put(row.id(), row);
            }
        }
        // Live-only movement: entities whose current position is in the radius and window.
        String liveExcluded = excludedClause("h.source_id", anchor, p);
        String livePrefilter = envelope.isPresent()
                ? " AND l.geom && ST_MakeEnvelope(:minx, :miny, :maxx, :maxy, 4326)" : "";
        List<Map.Entry<String, String>> liveKeys = jdbc.query("SELECT h.source_id, h.external_id FROM record_head h"
                + " JOIN record_live l ON l.source_id = h.source_id AND l.external_id = h.external_id"
                + " WHERE l.geom IS NOT NULL" + livePrefilter
                + " AND ST_DWithin(l.geom::geography, " + ANCHOR + "::geography, :radius)"
                + " AND l.observed_at BETWEEN :from AND :to AND NOT h.tombstone"
                + " AND NOT (h.source_id = :source AND h.external_id = :externalId)" + liveExcluded
                + " ORDER BY l.observed_at DESC LIMIT " + NEARBY_LIMIT, p,
                (rs, i) -> Map.entry(rs.getString(1), rs.getString(2)));
        List<Map.Entry<String, String>> missing = liveKeys.stream()
                .filter(k -> !byEntity.containsKey(k.getKey() + ":" + k.getValue())).toList();
        Map<String, RecordRow> live = missing.isEmpty() ? Map.of() : records.currentByKeys(missing, anchor.excludedSources());
        List<RecordRow> candidates = new ArrayList<>(byEntity.values());
        Set<String> liveOnly = new HashSet<>();
        for (RecordRow row : live.values()) {
            candidates.add(row);
            liveOnly.add(row.id());
        }
        candidates = egress.filter(candidates, anchor.destination());
        candidates.sort(Comparator.comparing(RecordRow::observedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(RecordRow::seq, Comparator.reverseOrder()));
        List<Item> out = new ArrayList<>();
        for (RecordRow row : candidates) {
            if (out.size() >= NEARBY_LIMIT) {
                break;
            }
            if (!anchor.allows(row.sourceId())) {
                continue;
            }
            boolean current = liveOnly.contains(row.id());
            out.add(recordItem(row, current ? RecordMapper.Shape.CURRENT : RecordMapper.Shape.VERSION, current));
        }
        return out;
    }

    /** Other alerts within the radius and window (by their snapshot geometry), newest first. */
    List<Item> nearbyAlerts(EnrichmentAnchor anchor, Optional<Envelope> envelope) {
        MapSqlParameterSource p = spatialParams(anchor, envelope);
        p.addValue("alertId", anchor.alert().id());
        String excluded = excludedClause("a.source_id", anchor, p);
        String geom = "ST_SetSRID(ST_GeomFromGeoJSON((a.record_snapshot -> 'geometry')::text), 4326)";
        List<Long> ids = jdbc.queryForList("SELECT a.id FROM alert a WHERE a.id <> :alertId AND NOT a.suppressed AND NOT a.exercise"
                + " AND a.fired_at >= :from AND a.observed_at BETWEEN :from AND :to"
                + " AND jsonb_typeof(a.record_snapshot -> 'geometry') = 'object'"
                + " AND ST_DWithin(" + geom + "::geography, " + ANCHOR + "::geography, :radius)" + excluded
                + " ORDER BY a.observed_at DESC, a.id DESC LIMIT " + ALERT_LIMIT, p, Long.class);
        List<AlertView> views = egress.filter(alerts.findAll(ids), AlertView::sourceId, anchor.destination());
        List<Item> out = new ArrayList<>();
        for (AlertView a : views) {
            ObjectNode node = json.createObjectNode();
            node.put("cite", "[alert:" + a.id() + "]");
            node.put("id", a.id());
            node.put("rule", a.ruleName());
            node.put("severity", a.severity());
            node.put("state", a.state());
            node.put("message", a.message());
            node.put("record", a.sourceId() + ":" + a.externalId());
            node.put("observedAt", a.observedAt() == null ? null : a.observedAt().toString());
            String title = alertTitle(a);
            String id = Long.toString(a.id());
            out.add(new Item(node, set -> set.addAlert(id, title)));
        }
        return out;
    }

    /** The top hybrid matches for the alert message in the radius (when spatial) and window. */
    List<Item> related(EnrichmentAnchor anchor, Optional<Envelope> envelope, Set<String> present, List<String> notes) {
        String message = anchor.alert().message();
        if (message == null || message.isBlank()) {
            return List.of();
        }
        if (!searchStatus.availability().available()) {
            notes.add("semantic search is not available: no related documents were gathered");
            return List.of();
        }
        String q = message.length() > MAX_QUERY ? message.substring(0, MAX_QUERY) : message;
        BboxFilter bbox = envelope.map(e -> new BboxFilter(List.of(e))).orElse(null);
        Map<String, Object> results;
        try {
            results = search.search(new SearchRequest(q, SearchRequest.Mode.HYBRID, Set.of(DocInput.RECORD), List.of(),
                    List.of(), bbox, anchor.from(), anchor.to().plusMillis(1), true, SEMANTIC_FETCH),
                    anchor.excludedSources());
        } catch (RuntimeException e) {
            log.debug("Enrichment search for alert {} failed: {}", anchor.alert().id(), e.getMessage());
            notes.add("the semantic search failed: no related documents were gathered");
            return List.of();
        }
        if (!(results.get("items") instanceof List<?> items)) {
            return List.of();
        }
        String alerted = DocInput.recordId(anchor.alert().sourceId(), anchor.alert().externalId());
        List<Map<?, ?>> candidates = new ArrayList<>();
        Set<Long> seqs = new LinkedHashSet<>();
        for (Object raw : items) {
            if (!(raw instanceof Map<?, ?> item) || !(item.get("seq") instanceof Number seq)) {
                continue;
            }
            Object source = item.get("source");
            Object docId = item.get("docId");
            if (source == null || docId == null || alerted.equals(docId.toString())
                    || !anchor.allows(source.toString())
                    || !egress.allowsLlmContext(source.toString(), anchor.destination())
                    || present.contains(docId + "@" + seq.longValue())) {
                continue;
            }
            candidates.add(item);
            seqs.add(seq.longValue());
        }
        Set<Long> inRadius = null;
        if (anchor.spatial() && !seqs.isEmpty()) {
            MapSqlParameterSource p = spatialParams(anchor, Optional.empty());
            p.addValue("seqs", PgArrays.bigint(seqs));
            inRadius = new HashSet<>(jdbc.queryForList("SELECT v.seq FROM record_version v"
                    + " WHERE v.seq = ANY(CAST(:seqs AS bigint[])) AND v.geom IS NOT NULL"
                    + " AND ST_DWithin(v.geom::geography, " + ANCHOR + "::geography, :radius)", p, Long.class));
        }
        List<Item> out = new ArrayList<>();
        for (Map<?, ?> item : candidates) {
            if (out.size() >= SEMANTIC_LIMIT) {
                break;
            }
            long seq = ((Number) item.get("seq")).longValue();
            if (inRadius != null && !inRadius.contains(seq)) {
                continue;
            }
            String docId = item.get("docId").toString();
            String title = item.get("title") == null ? docId : item.get("title").toString();
            ObjectNode node = json.createObjectNode();
            node.put("cite", cite(docId, seq));
            for (String key : List.of("docId", "seq", "title", "snippet", "kind", "observedAt", "effectiveTime")) {
                Object value = item.get(key);
                if (value != null) {
                    node.set(key, shaper.tree(value));
                }
            }
            shaper.searchHitNotice(item, node);
            out.add(new Item(node, set -> set.addRecord(docId, seq, title)));
        }
        return out;
    }

    /** The contributor sections, in order; a failing contributor is skipped. */
    List<Map.Entry<String, Item>> sections(EnrichmentAnchor anchor) {
        List<EnrichmentContextContributor> ordered = new ArrayList<>(contributors.orderedStream().toList());
        ordered.sort(Comparator.comparingInt(EnrichmentContextContributor::order));
        List<Map.Entry<String, Item>> out = new ArrayList<>();
        for (EnrichmentContextContributor contributor : ordered) {
            try {
                Optional<Object> section = contributor.contribute(anchor);
                if (section.isPresent()) {
                    out.add(Map.entry(contributor.key(), new Item(shaper.tree(section.get()), set -> { })));
                }
            } catch (RuntimeException e) {
                log.warn("Enrichment contributor {} failed for alert {}: {}", contributor.key(), anchor.alert().id(),
                        e.getMessage());
            }
        }
        return out;
    }

    // ------------------------------------------------------------------------- helpers

    private Item recordItem(RecordRow row, RecordMapper.Shape shape, boolean liveOnly) {
        ObjectNode node = shaper.record(recordMapper.record(row, FieldSelection.ALL, shape), McpOutputShaper.Detail.SHORT);
        node.put("cite", cite(row.id(), row.seq()));
        if (liveOnly) {
            node.put("liveOnly", true);
        }
        String id = row.id();
        long seq = row.seq();
        String title = textOrNull(node, "title");
        return new Item(node, set -> set.addRecord(id, seq, title));
    }

    private String serialize(EnrichmentAnchor anchor, ObjectNode alertNode, ObjectNode recordNode, List<Item> history,
            List<Item> nearbyRecords, List<Item> nearbyAlerts, List<Item> related,
            List<Map.Entry<String, Item>> sections, List<String> notes, int trimmed) {
        ObjectNode data = json.createObjectNode();
        data.set("alert", alertNode);
        data.set("record", recordNode);
        data.set("history", array(history));
        ObjectNode nearby = data.putObject("nearby");
        nearby.put("radiusKm", anchor.radiusKm());
        nearby.put("from", anchor.from().toString());
        nearby.put("to", anchor.to().toString());
        nearby.set("records", array(nearbyRecords));
        nearby.set("alerts", array(nearbyAlerts));
        data.set("related", array(related));
        ObjectNode sectionNode = data.putObject("sections");
        for (Map.Entry<String, Item> section : sections) {
            sectionNode.set(section.getKey(), section.getValue().node());
        }
        List<String> allNotes = new ArrayList<>(notes);
        if (trimmed > 0) {
            allNotes.add(trimmed + " lower-ranked items were left out to keep the context small");
        }
        if (!allNotes.isEmpty()) {
            ArrayNode noteNode = data.putArray("notes");
            allNotes.forEach(noteNode::add);
        }
        return json.writeValueAsString(data);
    }

    private ArrayNode array(List<Item> items) {
        ArrayNode out = json.createArrayNode();
        for (Item item : items) {
            out.add(item.node());
        }
        return out;
    }

    private MapSqlParameterSource spatialParams(EnrichmentAnchor anchor, Optional<Envelope> envelope) {
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("anchor", anchor.geometryJson())
                .addValue("radius", anchor.radiusMeters())
                .addValue("from", utc(anchor.from()))
                .addValue("to", utc(anchor.to()))
                .addValue("source", anchor.alert().sourceId())
                .addValue("externalId", anchor.alert().externalId());
        envelope.ifPresent(e -> p.addValue("minx", e.getMinX()).addValue("miny", e.getMinY())
                .addValue("maxx", e.getMaxX()).addValue("maxy", e.getMaxY()));
        return p;
    }

    private static String excludedClause(String column, EnrichmentAnchor anchor, MapSqlParameterSource p) {
        if (anchor.excludedSources().isEmpty()) {
            return "";
        }
        p.addValue("excluded", PgArrays.text(anchor.excludedSources().stream().sorted().toList()));
        return " AND NOT (" + column + " = ANY(CAST(:excluded AS text[])))";
    }

    /**
     * An index-friendly envelope around {@code geometryJson} grown by {@code radiusMeters}, or empty
     * near the poles or across the antimeridian, where the exact distance test runs alone.
     */
    Optional<Envelope> envelope(String geometryJson, double radiusMeters) {
        Geometry g;
        try {
            g = geoJson.read(geometryJson);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
        if (g == null || g.isEmpty()) {
            return Optional.empty();
        }
        return envelope(g.getEnvelopeInternal(), radiusMeters);
    }

    /** {@code e} grown by {@code radiusMeters}, or empty near the poles or across the antimeridian. */
    static Optional<Envelope> envelope(Envelope e, double radiusMeters) {
        double dLat = radiusMeters / METERS_PER_DEGREE * 1.01;
        double maxAbsLat = Math.max(Math.abs(e.getMinY()), Math.abs(e.getMaxY())) + dLat;
        if (maxAbsLat >= 89.0) {
            return Optional.empty();
        }
        double dLon = dLat / Math.cos(Math.toRadians(maxAbsLat));
        double minX = e.getMinX() - dLon;
        double maxX = e.getMaxX() + dLon;
        if (minX < -180 || maxX > 180) {
            return Optional.empty();
        }
        return Optional.of(new Envelope(minX, maxX, Math.max(-90, e.getMinY() - dLat), Math.min(90, e.getMaxY() + dLat)));
    }

    private ObjectNode snapshot(AlertView alert) {
        if (alert.recordSnapshot() == null) {
            return null;
        }
        try {
            JsonNode node = json.readTree(alert.recordSnapshot());
            return node instanceof ObjectNode object ? object : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String snapshotGeometry(ObjectNode snapshot) {
        if (snapshot == null) {
            return null;
        }
        JsonNode geometry = snapshot.get("geometry");
        return geometry != null && geometry.isObject() ? json.writeValueAsString(geometry) : null;
    }

    static String cite(String id, long seq) {
        return "[" + id + "@" + seq + "]";
    }

    private static String alertTitle(AlertView a) {
        return (a.ruleName() == null ? "Alert" : a.ruleName()) + ": " + (a.message() == null ? "" : a.message());
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asString();
    }

    private static int bytes(String text) {
        return text.getBytes(StandardCharsets.UTF_8).length;
    }

    private static OffsetDateTime utc(Instant instant) {
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
