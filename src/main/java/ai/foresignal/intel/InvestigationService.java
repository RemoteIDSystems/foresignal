package ai.foresignal.intel;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.place.PlaceService;
import ai.foresignal.provenance.AttributionService;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Investigations (prompt 101): operator case files with items that keep a snapshot taken when they
 * were added, so the evidence survives retention, and JSON/Markdown/GeoJSON export.
 *
 * <p>An item built from a private source is private (EGR-1): it is added only by, and returned only
 * to, callers with private access; exports carry only the items the caller may see, and the license
 * attributions of the sources those items cite.
 */
@Service
public class InvestigationService {

    /** The banner every export carries. */
    public static final String BANNER = "Automated analysis inputs; verify before acting.";

    /** The advisory-lock key that serializes display-id allocation. */
    static final long DISPLAY_ID_LOCK = 0x494E5649L;

    private final InvestigationRepository investigations;
    private final InvestigationItemRepository items;
    private final RecordQueryService records;
    private final AlertStore alerts;
    private final IntelligenceEventRepository events;
    private final PlaceService places;
    private final IntelVisibility visibility;
    private final AttributionService attributions;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public InvestigationService(InvestigationRepository investigations, InvestigationItemRepository items,
            RecordQueryService records, AlertStore alerts, IntelligenceEventRepository events, PlaceService places,
            IntelVisibility visibility, AttributionService attributions, JdbcTemplate jdbc, JsonMapper json,
            Clock clock) {
        this.investigations = investigations;
        this.items = items;
        this.records = records;
        this.alerts = alerts;
        this.events = events;
        this.places = places;
        this.visibility = visibility;
        this.attributions = attributions;
        this.jdbc = jdbc;
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

    /**
     * The items of an investigation the caller may see: a private item (built from a private source,
     * prompt 039) is left out unless {@code privateAccess}.
     */
    public List<InvestigationItemEntity> items(String id, boolean privateAccess) {
        get(id);
        return visibleItems(items.findByInvestigationIdOrderByIdAsc(id), privateAccess);
    }

    private static List<InvestigationItemEntity> visibleItems(List<InvestigationItemEntity> rows,
            boolean privateAccess) {
        if (privateAccess) {
            return rows;
        }
        List<InvestigationItemEntity> out = new ArrayList<>();
        for (InvestigationItemEntity item : rows) {
            if (!item.isPrivateItem()) {
                out.add(item);
            }
        }
        return out;
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

    /**
     * Adds an item, taking its snapshot now (capped at {@value SnapshotCaps#ITEM_MAX_BYTES} bytes).
     *
     * <p>An item built from a private source (a record of a private source, a private event, an alert on
     * a private source) is stored private, and only a caller with {@code privateAccess} may add it: for
     * anyone else it is reported exactly like a reference that does not exist.
     */
    @Transactional
    public InvestigationItemEntity addItem(String id, JsonNode body, String actor, boolean privateAccess) {
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
        Snapshot snapshot = snapshot(itemType, ref, seq);
        if (snapshot.privateSource() && !privateAccess) {
            throw ValidationException.forField("ref", "no such " + itemType + ": " + ref);
        }
        item.setPrivateItem(snapshot.privateSource());
        item.setSnapshot(SnapshotCaps.cap(snapshot.content(), SnapshotCaps.ITEM_MAX_BYTES, json));
        investigation.setUpdatedAt(now);
        investigations.save(investigation);
        return items.save(item);
    }

    /** Removes an item of this investigation; an item of another investigation (or a hidden one) is 404. */
    @Transactional
    public void removeItem(String id, long itemId, boolean privateAccess) {
        get(id);
        InvestigationItemEntity item = items.findById(itemId).orElse(null);
        if (item == null || !id.equals(item.getInvestigationId()) || (item.isPrivateItem() && !privateAccess)) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_INVESTIGATION,
                    "Investigation " + id + " has no item " + itemId + ".");
        }
        items.delete(item);
    }

    /**
     * A snapshot and whether it was built from a private source.
     *
     * @param content the snapshot
     * @param privateSource true when the referenced item comes from a private source
     */
    record Snapshot(Map<String, Object> content, boolean privateSource) {}

    /**
     * The snapshot of an item, taken now so it survives retention.
     *
     * @throws ValidationException when a record seq, event or alert does not exist
     */
    Snapshot snapshot(String itemType, String ref, Long seq) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("itemType", itemType);
        out.put("ref", ref);
        out.put("takenAt", clock.instant().toString());
        boolean privateSource = false;
        switch (itemType) {
            case "record" -> {
                if (seq != null) {
                    RecordRow row = records.versionsBySeq(List.of(seq)).get(seq);
                    if (row == null) {
                        throw ValidationException.forField("ref", "no such record: " + ref);
                    }
                    privateSource = visibility.isPrivateSource(row.sourceId());
                    out.put("record", recordSnapshot(row));
                } else {
                    int slash = ref.indexOf('/');
                    privateSource = slash > 0 && visibility.isPrivateSource(ref.substring(0, slash));
                }
            }
            case "event" -> {
                IntelligenceEventEntity event = events.findById(ref)
                        .orElseThrow(() -> ValidationException.forField("ref", "no such event: " + ref));
                privateSource = event.isPrivateEvent();
                out.put("event", IntelEventMapper.summary(event));
            }
            case "alert" -> {
                long alertId;
                try {
                    alertId = Long.parseLong(ref);
                } catch (NumberFormatException e) {
                    throw ValidationException.forField("ref", "no such alert: " + ref);
                }
                AlertView view = alerts.find(alertId)
                        .orElseThrow(() -> ValidationException.forField("ref", "no such alert: " + ref));
                privateSource = visibility.isPrivateSource(view.sourceId());
                Map<String, Object> alert = new LinkedHashMap<>();
                alert.put("id", view.id());
                alert.put("severity", view.severity());
                alert.put("message", view.message());
                alert.put("sourceId", view.sourceId());
                alert.put("externalId", view.externalId());
                out.put("alert", alert);
            }
            case "place" -> places.find(ref).ifPresent(place -> out.put("place", Map.of("id", place.id(),
                    "name", place.name(), "geometry", place.geometry() == null ? "" : place.geometry().toString())));
            default -> out.put("value", ref);
        }
        return new Snapshot(out, privateSource);
    }

    /** Exports an investigation as json, md or geojson, with only the items the caller may see. */
    public String export(String id, String format, boolean privateAccess) {
        InvestigationEntity investigation = get(id);
        List<InvestigationItemEntity> itemRows =
                visibleItems(items.findByInvestigationIdOrderByIdAsc(id), privateAccess);
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
        out.put("attributions", attributionTexts(itemRows));
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
        sb.append("\n### Attributions\n\n");
        List<String> texts = attributionTexts(itemRows);
        if (texts.isEmpty()) {
            sb.append("No source data cited.\n");
        }
        for (String text : texts) {
            sb.append("- ").append(text).append('\n');
        }
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
        out.put("foresignal:attributions", attributionTexts(itemRows));
        out.put("features", features);
        return json.writeValueAsString(out);
    }

    /** The attribution lines of the sources the items cite (prompt 109 licenses), one per source and license. */
    List<String> attributionTexts(List<InvestigationItemEntity> itemRows) {
        Set<String> sources = new LinkedHashSet<>();
        for (InvestigationItemEntity item : itemRows) {
            Map<String, Object> snapshot = item.getSnapshot();
            if (snapshot == null) {
                continue;
            }
            for (String section : List.of("record", "alert")) {
                if (snapshot.get(section) instanceof Map<?, ?> nested && nested.get("sourceId") != null) {
                    sources.add(String.valueOf(nested.get("sourceId")));
                }
            }
        }
        List<String> out = new ArrayList<>();
        for (String source : sources) {
            try {
                List<AttributionService.Attribution> found = attributions.scope(source, null);
                if (found.isEmpty()) {
                    out.add(source);
                }
                for (AttributionService.Attribution attribution : found) {
                    String text = attribution.text() == null || attribution.text().isBlank() ? source
                            : attribution.text();
                    out.add(text + " (" + attribution.licence() + ")");
                }
            } catch (RuntimeException e) {
                out.add(source);
            }
        }
        return out;
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

    /**
     * The next {@code INV-{yyyy}-{nnnn}} of the year. Allocation is serialized by a transaction-scoped
     * advisory lock, so concurrent creates never pick the same number; the unique constraint on
     * {@code display_id} is the backstop. The number is the numeric maximum, so it keeps counting past 9999.
     */
    private String nextDisplayId(Instant now) {
        int year = now.atZone(ZoneOffset.UTC).getYear();
        String prefix = "INV-" + year + "-";
        jdbc.queryForObject("SELECT count(*) FROM (SELECT pg_advisory_xact_lock(?)) AS held", Long.class,
                DISPLAY_ID_LOCK);
        Integer max = jdbc.queryForObject("SELECT max(CAST(substring(display_id FROM ?) AS integer))"
                + " FROM investigation WHERE display_id LIKE ? AND substring(display_id FROM ?) ~ '^[0-9]+$'",
                Integer.class, prefix.length() + 1, prefix + "%", prefix.length() + 1);
        int next = max == null ? 1 : max + 1;
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
