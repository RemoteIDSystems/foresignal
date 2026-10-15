package ai.foresignal.intel;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.place.PlaceService;
import ai.foresignal.security.Ulids;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Investigations (prompt 101): operator case files with items that keep a snapshot taken when they
 * were added, so the evidence survives retention, and JSON/Markdown/GeoJSON export.
 */
@Service
public class InvestigationService {

    /** The banner every export carries. */
    public static final String BANNER = "Automated analysis inputs; verify before acting.";

    private final InvestigationRepository investigations;
    private final InvestigationItemRepository items;
    private final RecordQueryService records;
    private final AlertStore alerts;
    private final IntelligenceEventRepository events;
    private final PlaceService places;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public InvestigationService(InvestigationRepository investigations, InvestigationItemRepository items,
            RecordQueryService records, AlertStore alerts, IntelligenceEventRepository events, PlaceService places,
            JsonMapper json, Clock clock) {
        this.investigations = investigations;
        this.items = items;
        this.records = records;
        this.alerts = alerts;
        this.events = events;
        this.places = places;
        this.json = json;
        this.clock = clock;
    }

    /** Lists investigations, newest first, optionally by status. */
    public List<InvestigationEntity> list(String status) {
        return status == null ? investigations.findAll() : investigations.findByStatusOrderByUpdatedAtDesc(status);
    }

    /** One investigation, or 404. */
    public InvestigationEntity get(String id) {
        return investigations.findById(id).orElseThrow(
                () -> new NotFoundException(ErrorCodes.UNKNOWN_INVESTIGATION, "No such investigation: " + id));
    }

    /** The items of an investigation. */
    public List<InvestigationItemEntity> items(String id) {
        get(id);
        return items.findByInvestigationIdOrderByIdAsc(id);
    }

    /** Creates an investigation. */
    @Transactional
    public InvestigationEntity create(JsonNode body, String actor) {
        String title = text(body, "title");
        if (title == null || title.isBlank()) {
            throw ValidationException.forField("title", "is required");
        }
        Instant now = clock.instant();
        InvestigationEntity entity = new InvestigationEntity(Ulids.next(now), nextDisplayId(now), title.strip(), now);
        entity.setUpdatedAt(now);
        entity.setCreatedBy(actor);
        if (body.hasNonNull("summary")) {
            entity.setSummary(text(body, "summary"));
        }
        if (body.hasNonNull("tags")) {
            entity.setTags(strings(body.get("tags")));
        }
        return investigations.save(entity);
    }

    /** Patches title, status, summary and tags. */
    @Transactional
    public InvestigationEntity patch(String id, JsonNode body) {
        InvestigationEntity entity = get(id);
        if (body.hasNonNull("title")) {
            entity.setTitle(text(body, "title"));
        }
        if (body.hasNonNull("status")) {
            entity.setStatus(text(body, "status"));
        }
        if (body.hasNonNull("summary")) {
            entity.setSummary(text(body, "summary"));
        }
        if (body.hasNonNull("tags")) {
            entity.setTags(strings(body.get("tags")));
        }
        entity.setUpdatedAt(clock.instant());
        return investigations.save(entity);
    }

    /** Deletes an investigation and its items. */
    @Transactional
    public void delete(String id) {
        InvestigationEntity entity = get(id);
        items.deleteByInvestigationId(id);
        investigations.delete(entity);
    }

    /** Adds an item, taking its snapshot now. */
    @Transactional
    public InvestigationItemEntity addItem(String id, JsonNode body, String actor) {
        InvestigationEntity investigation = get(id);
        String itemType = text(body, "itemType");
        String ref = text(body, "ref");
        if (itemType == null || ref == null) {
            throw ValidationException.forField("item", "itemType and ref are required");
        }
        Instant now = clock.instant();
        InvestigationItemEntity item = new InvestigationItemEntity(id, itemType, ref, now);
        item.setAddedBy(actor);
        if (body.hasNonNull("note")) {
            item.setNote(text(body, "note"));
        }
        Long seq = body.hasNonNull("seq") ? body.get("seq").asLong() : null;
        item.setSeq(seq);
        item.setSnapshot(snapshot(itemType, ref, seq));
        investigation.setUpdatedAt(now);
        investigations.save(investigation);
        return items.save(item);
    }

    /** Removes an item. */
    @Transactional
    public void removeItem(String id, long itemId) {
        get(id);
        items.deleteById(itemId);
    }

    /** The snapshot of an item, taken now so it survives retention. */
    Map<String, Object> snapshot(String itemType, String ref, Long seq) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("itemType", itemType);
        out.put("ref", ref);
        out.put("takenAt", clock.instant().toString());
        switch (itemType) {
            case "record" -> {
                if (seq != null) {
                    RecordRow row = records.versionsBySeq(List.of(seq)).get(seq);
                    if (row != null) {
                        out.put("record", recordSnapshot(row));
                    }
                }
            }
            case "event" -> events.findById(ref).ifPresent(event -> out.put("event", IntelEventMapper.summary(event)));
            case "alert" -> {
                try {
                    Optional<AlertView> alert = alerts.find(Long.parseLong(ref));
                    alert.ifPresent(view -> out.put("alert", Map.of("id", view.id(), "severity", view.severity(),
                            "message", view.message(), "sourceId", view.sourceId(), "externalId", view.externalId())));
                } catch (NumberFormatException e) {
                    out.put("alert", Map.of("ref", ref));
                }
            }
            case "place" -> places.find(ref).ifPresent(place -> out.put("place", Map.of("id", place.id(),
                    "name", place.name(), "geometry", place.geometry() == null ? null : place.geometry().toString())));
            default -> out.put("value", ref);
        }
        return out;
    }

    /** Exports an investigation as json, md or geojson. */
    public String export(String id, String format) {
        InvestigationEntity investigation = get(id);
        List<InvestigationItemEntity> itemRows = items.findByInvestigationIdOrderByIdAsc(id);
        return switch (format == null ? "json" : format) {
            case "md" -> markdown(investigation, itemRows);
            case "geojson" -> geoJson(investigation, itemRows);
            default -> json(investigation, itemRows);
        };
    }

    /** The content type of an export format. */
    public static String contentType(String format) {
        return switch (format == null ? "json" : format) {
            case "md" -> "text/markdown; charset=utf-8";
            case "geojson" -> "application/geo+json";
            default -> "application/json";
        };
    }

    private String json(InvestigationEntity investigation, List<InvestigationItemEntity> itemRows) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("header", BANNER);
        out.put("investigation", view(investigation));
        List<Map<String, Object>> list = new ArrayList<>();
        for (InvestigationItemEntity item : itemRows) {
            list.add(itemView(item));
        }
        out.put("items", list);
        out.put("attributions", List.of("© European Union, GHSL (population)", "Uber H3 (Apache-2.0)"));
        return json.writeValueAsString(out);
    }

    private String markdown(InvestigationEntity investigation, List<InvestigationItemEntity> itemRows) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(investigation.getTitle()).append("\n\n");
        sb.append("> ").append(BANNER).append("\n\n");
        sb.append("- Display id: ").append(investigation.getDisplayId()).append('\n');
        sb.append("- Status: ").append(investigation.getStatus()).append('\n');
        sb.append("- Created: ").append(investigation.getCreatedAt()).append("\n\n");
        if (investigation.getSummary() != null) {
            sb.append(investigation.getSummary()).append("\n\n");
        }
        sb.append("## Items\n\n");
        for (InvestigationItemEntity item : itemRows) {
            String citation = citation(item);
            sb.append("- ").append(item.getItemType()).append(" `").append(item.getRef()).append('`')
                    .append(item.getNote() == null ? "" : " — " + item.getNote()).append(" ").append(citation)
                    .append('\n');
        }
        sb.append("\n### Attributions\n\n").append("© European Union, GHSL · Uber H3 (Apache-2.0)\n");
        return sb.toString();
    }

    private String geoJson(InvestigationEntity investigation, List<InvestigationItemEntity> itemRows) {
        List<Map<String, Object>> features = new ArrayList<>();
        for (InvestigationItemEntity item : itemRows) {
            Map<String, Object> geometry = geometryOf(item);
            if (geometry == null) {
                continue;
            }
            Map<String, Object> feature = new LinkedHashMap<>();
            feature.put("type", "Feature");
            feature.put("geometry", geometry);
            feature.put("properties", Map.of("itemType", item.getItemType(), "ref", item.getRef(),
                    "note", item.getNote() == null ? "" : item.getNote()));
            features.add(feature);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("type", "FeatureCollection");
        out.put("name", investigation.getTitle());
        out.put("foresignal:header", BANNER);
        out.put("features", features);
        return json.writeValueAsString(out);
    }

    private String citation(InvestigationItemEntity item) {
        Map<String, Object> snapshot = item.getSnapshot();
        if (snapshot != null && snapshot.get("record") instanceof Map<?, ?> record) {
            Object source = record.get("sourceId");
            Object external = record.get("externalId");
            Object version = record.get("version");
            if (source != null && external != null) {
                return "[" + source + "/" + external + "@v" + version + "]";
            }
        }
        return "[" + item.getRef() + "]";
    }

    private Map<String, Object> geometryOf(InvestigationItemEntity item) {
        Map<String, Object> snapshot = item.getSnapshot();
        if (snapshot == null) {
            return null;
        }
        if (snapshot.get("record") instanceof Map<?, ?> record && record.get("geometry") != null) {
            try {
                return json.readValue(String.valueOf(record.get("geometry")), Map.class);
            } catch (RuntimeException e) {
                return null;
            }
        }
        return null;
    }

    private Map<String, Object> view(InvestigationEntity entity) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", entity.getId());
        out.put("displayId", entity.getDisplayId());
        out.put("title", entity.getTitle());
        out.put("status", entity.getStatus());
        out.put("summary", entity.getSummary());
        out.put("tags", entity.getTags());
        out.put("createdBy", entity.getCreatedBy());
        out.put("createdAt", entity.getCreatedAt());
        out.put("updatedAt", entity.getUpdatedAt());
        return out;
    }

    private Map<String, Object> itemView(InvestigationItemEntity item) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", item.getId());
        out.put("itemType", item.getItemType());
        out.put("ref", item.getRef());
        out.put("seq", item.getSeq());
        out.put("note", item.getNote());
        out.put("snapshot", item.getSnapshot());
        out.put("addedAt", item.getAddedAt());
        out.put("addedBy", item.getAddedBy());
        return out;
    }

    private Map<String, Object> recordSnapshot(RecordRow row) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sourceId", row.sourceId());
        out.put("externalId", row.externalId());
        out.put("kind", row.kind());
        out.put("version", row.version());
        out.put("seq", row.seq());
        out.put("observedAt", row.observedAt());
        out.put("geometry", row.geometry());
        out.put("payload", row.payload());
        return out;
    }

    private String nextDisplayId(Instant now) {
        int year = now.atZone(ZoneOffset.UTC).getYear();
        String prefix = "INV-" + year + "-";
        int next = 1;
        Optional<InvestigationEntity> last =
                investigations.findTop1ByDisplayIdStartingWithOrderByDisplayIdDesc(prefix).stream().findFirst();
        if (last.isPresent()) {
            try {
                next = Integer.parseInt(last.get().getDisplayId().substring(prefix.length())) + 1;
            } catch (NumberFormatException e) {
                next = 1;
            }
        }
        return prefix + String.format("%04d", next);
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asString();
    }

    private static List<String> strings(JsonNode node) {
        List<String> out = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                if (item.isString()) {
                    out.add(item.asString());
                }
            }
        }
        return out;
    }
}
