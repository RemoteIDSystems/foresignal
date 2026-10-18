package ai.foresignal.chronicle;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collab.CollabInputs;
import ai.foresignal.place.PlaceService;
import ai.foresignal.situation.SituationEntity;
import ai.foresignal.situation.SituationRepository;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.formats.geojson.GeoJsonWriter;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Envelope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Builds draft chronicles deterministically (prompt 131, {@code suggestChronicle} and the analysis cycles'
 * proposals). From a situation: its title as the name, its geometry buffered by {@value #SITUATION_BUFFER_KM} km (or
 * its regions) as the area, one layer per member source and kind, a layer of the situation and its members, a
 * findings layer for its signal families, the 119 analysis series of its regions, its topics, investigations and open
 * hypothesis sets. From an area and a topic: the topic (an attention topic id, else keywords), the series of the area's
 * regions and the topic, and layers for the sources most active in the area over the last day. Capture modes follow
 * the defaults by kind: TRACK sources downsampled at 60 s, events full, series full, raster products every 4th frame.
 * Every draft carries its storage estimate.
 */
@Component
public class ChronicleSuggester {

    /** The buffer around a situation's geometry. */
    public static final double SITUATION_BUFFER_KM = 50;

    /** Most member layers in a draft. */
    public static final int MAX_MEMBER_LAYERS = 20;

    /** Most series refs in a draft. */
    public static final int MAX_SERIES = 10;

    private final SituationRepository situations;
    private final SourceRegistry registry;
    private final PlaceService places;
    private final ChronicleEstimator estimator;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the suggester. */
    public ChronicleSuggester(SituationRepository situations, SourceRegistry registry, PlaceService places,
            ChronicleEstimator estimator, JdbcTemplate jdbc, JsonMapper json, Clock clock) {
        this.situations = situations;
        this.registry = registry;
        this.places = places;
        this.estimator = estimator;
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
    }

    /**
     * A suggestion from a {@code ChronicleSuggestRequest}.
     *
     * @param body {@code situationId}, or {@code area} with an optional {@code topic}
     * @return the {@code ChronicleSuggestion}
     */
    public Map<String, Object> suggest(JsonNode body) {
        CollabInputs.object(body, "ChronicleSuggestRequest", Set.of("situationId", "area", "topic"));
        String situationId = CollabInputs.text(body, "situationId", 26, 26);
        ChronicleArea area = ChronicleArea.parse(body.get("area"), "area", id -> places.find(id).isPresent());
        String topic = CollabInputs.text(body, "topic", 1, 200);
        if (situationId != null) {
            return fromSituation(situationId);
        }
        if (area == null || area.isEmpty()) {
            throw ValidationException.forField("situationId", "give a situationId, or an area with an optional topic");
        }
        return fromArea(area, topic, null);
    }

    /**
     * The suggestion for a situation.
     *
     * @throws NotFoundException {@code UNKNOWN_SITUATION}
     */
    public Map<String, Object> fromSituation(String situationId) {
        SituationEntity s = situations.findById(situationId).orElseThrow(() -> new NotFoundException(
                ErrorCodes.UNKNOWN_SITUATION, "No situation '" + situationId + "'."));
        List<String> rationale = new ArrayList<>();
        Map<String, Object> area = new LinkedHashMap<>();
        if (s.getGeom() != null && !s.getGeom().isEmpty()) {
            area.put("geometry", new GeoJsonWriter().write(s.getGeom()));
            area.put("bufferKm", SITUATION_BUFFER_KM);
            rationale.add("Area: the situation's geometry buffered by " + (int) SITUATION_BUFFER_KM + " km.");
        } else if (!s.getRegionCodes().isEmpty()) {
            area.put("regionCodes", s.getRegionCodes());
            rationale.add("Area: the situation's regions " + s.getRegionCodes() + ".");
        }
        List<Map<String, Object>> layers = new ArrayList<>();
        List<Map<String, Object>> members = jdbc.queryForList("SELECT source_id, kind, count(*) AS n FROM"
                + " situation_member WHERE situation_id = ? AND source_id IS NOT NULL AND kind IS NOT NULL"
                + " AND member_type IN ('record', 'anomaly') GROUP BY source_id, kind ORDER BY n DESC, source_id, kind"
                + " LIMIT " + MAX_MEMBER_LAYERS, situationId);
        for (Map<String, Object> m : members) {
            String source = String.valueOf(m.get("source_id"));
            String kind = String.valueOf(m.get("kind"));
            if (!registry.contains(source)) {
                continue;
            }
            layers.add(memberLayer(source, kind));
        }
        if (!members.isEmpty()) {
            rationale.add("Layers: the sources and kinds of the situation's " + members.size()
                    + " member groups, with the default capture mode of each kind.");
        }
        layers.add(layer("Situation and members", Map.of("type", ChronicleSelector.SITUATIONS, "situationIds",
                List.of(situationId)), CaptureMode.FULL_MODE));
        if (!s.getSignalFamilies().isEmpty()) {
            layers.add(layer("Findings", Map.of("type", ChronicleSelector.FINDINGS, "families",
                    s.getSignalFamilies()), CaptureMode.FULL_MODE));
            rationale.add("Findings of the signal families " + s.getSignalFamilies() + ".");
        }
        List<String> series = seriesFor(s.getRegionCodes(), null);
        if (!series.isEmpty()) {
            layers.add(layer("Series", Map.of("type", ChronicleSelector.SERIES, "refs", series),
                    CaptureMode.FULL_MODE));
            rationale.add(series.size() + " analysis series of the situation's regions (119 registry).");
        }
        List<String> sets = jdbc.queryForList("SELECT id FROM hypothesis_set WHERE situation_id = ? AND status <>"
                + " 'archived' ORDER BY id", String.class, situationId);
        Map<String, Object> draft = new LinkedHashMap<>();
        draft.put("name", trim(s.getTitle()));
        draft.put("description", "Monitoring thread for the situation " + s.getTitle() + ".");
        if (!area.isEmpty()) {
            draft.put("area", area);
        }
        draft.put("situationIds", List.of(situationId));
        draft.put("hypothesisSetIds", sets);
        draft.put("investigationIds", s.getInvestigationIds());
        draft.put("topicIds", s.getTopicIds());
        draft.put("layers", layers);
        return suggestion(draft, rationale);
    }

    /**
     * The suggestion for an area and a topic.
     *
     * @param area the area
     * @param topic an attention topic id or keywords, or null
     * @param name the name, or null for one built from the topic and area
     */
    public Map<String, Object> fromArea(ChronicleArea area, String topic, String name) {
        List<String> rationale = new ArrayList<>();
        List<String> topicIds = new ArrayList<>();
        String topicName = topic;
        if (topic != null) {
            List<String> names = jdbc.queryForList("SELECT name FROM attention_topic WHERE id = ?", String.class, topic);
            if (!names.isEmpty()) {
                topicIds.add(topic);
                topicName = names.getFirst();
                rationale.add("Topic: the attention topic " + topic + ".");
            } else {
                rationale.add("Topic: the keywords '" + topic + "' (no attention topic has that id).");
            }
        }
        List<Map<String, Object>> layers = new ArrayList<>();
        ChronicleArea.Resolved resolved = area.resolve(id -> places.area(id).orElse(null));
        if (resolved.geometry() != null) {
            Envelope e = resolved.geometry().getEnvelopeInternal();
            List<Map<String, Object>> active = jdbc.queryForList("SELECT source_id, kind, count(*) AS n FROM record_version"
                    + " WHERE observed_at >= ? AND geom && ST_MakeEnvelope(?, ?, ?, ?, 4326) GROUP BY source_id, kind"
                    + " ORDER BY n DESC, source_id, kind LIMIT 8", Timestamp.from(clock.instant().minus(Duration.ofDays(1))),
                    e.getMinX(), e.getMinY(), e.getMaxX(), e.getMaxY());
            for (Map<String, Object> m : active) {
                String source = String.valueOf(m.get("source_id"));
                if (registry.contains(source) && !source.startsWith("foresignal-")) {
                    layers.add(memberLayer(source, String.valueOf(m.get("kind"))));
                }
            }
            if (!active.isEmpty()) {
                rationale.add("Layers: the sources and kinds most active in the area over the last day.");
            }
        }
        layers.add(layer("Findings", Map.of("type", ChronicleSelector.FINDINGS), CaptureMode.FULL_MODE));
        List<String> series = seriesFor(area.regionCodes(), topic);
        if (!series.isEmpty()) {
            layers.add(layer("Series", Map.of("type", ChronicleSelector.SERIES, "refs", series),
                    CaptureMode.FULL_MODE));
            rationale.add(series.size() + " analysis series of the area's regions and the topic.");
        }
        Map<String, Object> draft = new LinkedHashMap<>();
        draft.put("name", trim(name != null ? name : topicName != null ? topicName
                : "Area " + (area.regionCodes().isEmpty() ? "watch" : String.join(", ", area.regionCodes()))));
        draft.put("area", area.toMap());
        draft.put("topicIds", topicIds);
        draft.put("layers", layers);
        return suggestion(draft, rationale);
    }

    private Map<String, Object> memberLayer(String source, String kind) {
        Map<String, Object> selector;
        CaptureMode mode;
        if (ChronicleVocabulary.RASTER_FRAME_KIND.equals(kind)) {
            List<String> products = jdbc.queryForList("SELECT DISTINCT external_id FROM record_head WHERE source_id = ?"
                    + " AND kind = ? AND external_id NOT LIKE '%:partial' LIMIT 20", String.class, source, kind);
            selector = Map.of("type", ChronicleSelector.RASTER_PRODUCTS, "productIds", products.isEmpty()
                    ? List.of(source) : products);
            mode = CaptureMode.frames(CaptureMode.DEFAULT_FRAME_STEP);
        } else {
            selector = Map.of("type", ChronicleSelector.RECORDS, "sources", List.of(source), "kinds", List.of(kind));
            mode = defaultMode(source, kind);
        }
        return layer(source + " " + kind, selector, mode);
    }

    /** The default capture mode of a kind: TRACK sources downsampled at 60 s, everything else full. */
    CaptureMode defaultMode(String source, String kind) {
        boolean track = registry.find(source).map(r -> r.descriptor().isTrack()).orElse(false);
        if (track) {
            return CaptureMode.downsampled(CaptureMode.DEFAULT_INTERVAL);
        }
        return CaptureMode.FULL_MODE;
    }

    private List<String> seriesFor(List<String> regions, String topic) {
        Set<String> out = new LinkedHashSet<>();
        if (regions != null && !regions.isEmpty()) {
            out.addAll(jdbc.queryForList("SELECT ref FROM analysis_series WHERE enabled AND region_code = ANY(?)"
                    + " ORDER BY ref LIMIT " + MAX_SERIES, String.class, (Object) regions.toArray(String[]::new)));
        }
        if (topic != null && !topic.isBlank() && out.size() < MAX_SERIES) {
            String like = "%" + topic.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
            out.addAll(jdbc.queryForList("SELECT ref FROM analysis_series WHERE enabled AND ref ILIKE ? ESCAPE '\\'"
                    + " ORDER BY ref LIMIT " + MAX_SERIES, String.class, like));
        }
        List<String> list = new ArrayList<>(out);
        return list.size() > MAX_SERIES ? list.subList(0, MAX_SERIES) : list;
    }

    private static Map<String, Object> layer(String name, Map<String, Object> selector, CaptureMode mode) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("name", trim(name));
        out.put("selector", selector);
        out.put("capture", mode.toMap());
        return out;
    }

    private Map<String, Object> suggestion(Map<String, Object> draft, List<String> rationale) {
        ChronicleEntity probe = new ChronicleEntity("00000000000000000000000000", "CHR-0000-0000",
                String.valueOf(draft.get("name")), 1, clock.instant());
        List<Map<String, Object>> rows = new ArrayList<>();
        double totalRows = 0;
        double totalBytes = 0;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> layers = (List<Map<String, Object>>) draft.get("layers");
        for (Map<String, Object> l : layers) {
            ChronicleEstimator.Projection p;
            try {
                ChronicleSelector selector = ChronicleSelector.parse(json.valueToTree(l.get("selector")), "selector");
                CaptureMode mode = CaptureMode.parse(json.valueToTree(l.get("capture")), "capture");
                p = estimator.project(probe, selector, mode);
            } catch (RuntimeException e) {
                p = new ChronicleEstimator.Projection(0, 0, "no projection");
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", l.get("name"));
            row.put("rowsPerDay", p.toMap().get("rowsPerDay"));
            row.put("bytesPerDay", p.toMap().get("bytesPerDay"));
            rows.add(row);
            totalRows += p.rowsPerDay();
            totalBytes += p.bytesPerDay();
        }
        Map<String, Object> estimate = new LinkedHashMap<>();
        estimate.put("totalRowsPerDay", Math.round(totalRows * 10.0) / 10.0);
        estimate.put("totalBytesPerDay", Math.round(totalBytes * 10.0) / 10.0);
        estimate.put("layers", rows);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("draft", draft);
        out.put("estimate", estimate);
        out.put("rationale", rationale);
        out.put("textOrigin", "deterministic");
        return out;
    }

    private static String trim(String name) {
        String n = name == null || name.isBlank() ? "Chronicle" : name.strip();
        return n.length() > 200 ? n.substring(0, 200) : n;
    }
}
