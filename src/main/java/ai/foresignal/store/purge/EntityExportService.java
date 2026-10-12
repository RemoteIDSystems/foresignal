package ai.foresignal.store.purge;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.mapper.AlertMapper;
import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.store.query.Slices;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import java.io.OutputStream;
import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Streams everything retained about one entity (prompt 039, {@code exportRecord}, MCP
 * {@code export_entity}): the current state (head plus live, observers included), every retained
 * version in seq order (observers included), the alerts referencing the entity, and a manifest.
 *
 * <p>The manifest is the last item and holds {@code format}, the entity, {@code exportedAt},
 * {@code counts} ({@code current}, {@code versions}, {@code alerts}, plus {@code points} for GPX),
 * {@code alertIds} and {@code sha256}: the lowercase hex SHA-256 of every byte of the export that
 * precedes the manifest (see {@link ExportWriter#manifestMarker()}). Admin only; no visibility
 * filtering applies.
 */
@Service
public class EntityExportService {

    /** Versions read per page. */
    static final int PAGE = 1_000;

    private final RecordQueryService queries;
    private final RecordMapper mapper;
    private final AlertStore alerts;
    private final AlertMapper alertMapper;
    private final SourceRegistry registry;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public EntityExportService(RecordQueryService queries, RecordMapper mapper, AlertStore alerts,
            AlertMapper alertMapper, SourceRegistry registry, JdbcTemplate jdbc, JsonMapper json, Clock clock) {
        this.queries = queries;
        this.mapper = mapper;
        this.alerts = alerts;
        this.alertMapper = alertMapper;
        this.registry = registry;
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
    }

    /**
     * What an export wrote, for the audit log.
     *
     * @param versions versions written
     * @param alerts alerts written (or listed, for GPX)
     * @param sha256 the content hash in the manifest
     */
    public record Summary(int versions, int alerts, String sha256) {}

    /**
     * The entity's current state; call before committing a response so an unknown entity is a 404.
     *
     * @throws NotFoundException when the source is unknown or the entity has no head
     */
    public RecordRow require(String sourceId, String externalId) {
        registry.descriptor(sourceId);
        return current(sourceId, externalId)
                .orElseThrow(() -> NotFoundException.of("Record", sourceId + ":" + externalId));
    }

    /**
     * Writes the export of {@code sourceId}/{@code externalId} in {@code format} to {@code out}.
     *
     * @throws NotFoundException when the entity has no head
     */
    public Summary write(String sourceId, String externalId, ExportFormat format, OutputStream out) {
        RecordRow head = require(sourceId, externalId);
        ExportWriter writer = switch (format) {
            case GEOJSON -> new GeoJsonExportWriter(out, json);
            case NDJSON -> new NdjsonExportWriter(out, json);
            case GPX -> new GpxExportWriter(out, json);
        };
        writer.start(sourceId, externalId, head.kind());
        writer.current(item(head, RecordMapper.Shape.CURRENT));
        int versions = 0;
        String cursor = null;
        do {
            Optional<Slices.VersionSlice> slice = queries.versions(sourceId, externalId, null, null, cursor, PAGE,
                    true, Set.of());
            if (slice.isEmpty()) {
                break;
            }
            for (RecordRow row : slice.get().rows()) {
                writer.version(item(row, RecordMapper.Shape.VERSION));
                versions++;
            }
            cursor = slice.get().nextCursor();
        } while (cursor != null);
        List<Long> alertIds = jdbc.queryForList(
                "SELECT id FROM alert WHERE source_id = ? AND external_id = ? ORDER BY id", Long.class,
                sourceId, externalId);
        for (AlertView view : alerts.findAll(alertIds)) {
            writer.alert(alertMapper.alert(view, null));
        }
        Map<String, Object> counts = new LinkedHashMap<>();
        counts.put("current", 1);
        counts.put("versions", versions);
        counts.put("alerts", alertIds.size());
        Map<String, Object> manifest = new LinkedHashMap<>();
        manifest.put("format", format.wireValue());
        manifest.put("sourceId", sourceId);
        manifest.put("externalId", externalId);
        manifest.put("kind", head.kind());
        manifest.put("exportedAt", clock.instant());
        manifest.put("counts", counts);
        manifest.put("alertIds", new ArrayList<>(alertIds));
        manifest.put("hashed", "SHA-256 of every byte before the manifest marker " + writer.manifestMarker());
        String sha = writer.finishWithHash(manifest, writer instanceof GpxExportWriter gpx
                ? () -> counts.put("points", gpx.points()) : () -> { });
        return new Summary(versions, alertIds.size(), sha);
    }

    private Optional<RecordRow> current(String sourceId, String externalId) {
        return queries.get(sourceId, externalId, true, FieldSelection.ALL, GeometryOptions.DEFAULT, Set.of());
    }

    private ExportItem item(RecordRow row, RecordMapper.Shape shape) {
        Map<String, Object> record = mapper.record(row, FieldSelection.ALL, shape);
        return new ExportItem(record, row.geometry(), row.observedAt(), row.tombstone(), altitude(row.payload()));
    }

    private Double altitude(String payload) {
        if (payload == null || payload.isBlank()) {
            return null;
        }
        try {
            JsonNode node = json.readTree(payload).path("altitudeM");
            return node.isNumber() ? node.asDouble() : null;
        } catch (RuntimeException e) {
            return null;
        }
    }
}
