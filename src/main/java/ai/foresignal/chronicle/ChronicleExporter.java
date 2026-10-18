package ai.foresignal.chronicle;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collab.AccessResolver;
import ai.foresignal.collab.CollabInputs;
import ai.foresignal.export.CustomExport;
import ai.foresignal.export.ExportFormat;
import ai.foresignal.export.ExportJobEntity;
import ai.foresignal.export.ExportJobService;
import ai.foresignal.export.ExportLicenceService;
import ai.foresignal.export.ExportRows;
import ai.foresignal.export.ExportSchema;
import ai.foresignal.export.ExportWriter;
import ai.foresignal.export.ExportWriterFactory;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Exports chronicle storage through the 111 export engine (prompt 131, {@code exportChronicle}): GeoParquet or
 * GeoPackage files of the captured versions (the standard export columns plus {@code layer_id}), or a JSON bundle of
 * the chronicle, its layers, its thread, its versions, series points and rollups. The sources go through the export
 * license decision, so private sources the caller may not see and {@code LOCAL_ONLY} sources never leave the instance
 * (EGR-1, EGR-2); private thread items need private access. The chronicle's TLP label is printed in the file metadata
 * and at the head of the bundle (COL-2), and the job runs with the usual quota, manifest and license sidecars.
 */
@Component
public class ChronicleExporter {

    /** The formats. */
    public static final List<String> FORMATS = List.of("geoparquet", "gpkg", "json-bundle");

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final ExportJobService exports;
    private final ExportLicenceService licenses;
    private final SourceRegistry registry;
    private final ChronicleStorage storage;
    private final ChronicleService chronicles;
    private final AccessResolver access;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;

    /** Creates the exporter. */
    public ChronicleExporter(ExportJobService exports, ExportLicenceService licenses, SourceRegistry registry,
            ChronicleStorage storage, ChronicleService chronicles, AccessResolver access, JdbcTemplate jdbc,
            JsonMapper json) {
        this.exports = exports;
        this.licenses = licenses;
        this.registry = registry;
        this.storage = storage;
        this.chronicles = chronicles;
        this.access = access;
        this.jdbc = jdbc;
        this.json = json;
    }

    /**
     * Starts an export job.
     *
     * @param c the chronicle (the caller may view it)
     * @param body the {@code ChronicleExportRequest}
     * @param caller the export caller (token, admin flag, egress viewer, restriction)
     * @param objectCaller the object caller (private access, chronicle view)
     * @return the queued job
     */
    public ExportJobEntity submit(ChronicleEntity c, JsonNode body, ExportJobService.Caller caller,
            ObjectCaller objectCaller) {
        CollabInputs.object(body, "ChronicleExportRequest", Set.of("format", "layers", "from", "to"));
        String format = CollabInputs.required(body, "format", 1, 32);
        if (!FORMATS.contains(format)) {
            throw ValidationException.forField("format", "must be one of " + FORMATS);
        }
        List<String> layerIds = CollabInputs.strings(body, "layers", 100);
        for (String id : layerIds) {
            chronicles.layer(c.getId(), id);
        }
        Instant from = CollabInputs.instant(body, "from");
        Instant to = CollabInputs.instant(body, "to");
        if (from != null && to != null && !from.isBefore(to)) {
            throw ValidationException.forField("to", "must be after from");
        }
        String tlp = access.tlpOf(ChronicleService.ref(c.getId())).wireValue();
        List<String> present = presentSources(c.getId(), layerIds);
        List<SourceDescriptor> candidates = new ArrayList<>();
        List<Map<String, Object>> unknown = new ArrayList<>();
        for (String s : present) {
            registry.find(s).ifPresentOrElse(r -> candidates.add(r.descriptor()),
                    () -> unknown.add(Map.of("sourceId", s, "reason", "unregistered")));
        }
        ExportLicenceService.Decision raw = licenses.decide(candidates, caller.viewer(), caller.admin(), false, false);
        List<String> allowed = new ArrayList<>();
        for (String s : raw.allowed()) {
            if (!objectCaller.excludedSources().contains(s)) {
                allowed.add(s);
            }
        }
        List<Map<String, Object>> excluded = new ArrayList<>(raw.excluded());
        excluded.addAll(unknown);
        ExportLicenceService.Decision decision = new ExportLicenceService.Decision(allowed, raw.personalUseSources()
                .stream().filter(allowed::contains).toList(), excluded);
        Map<String, Object> spec = new LinkedHashMap<>();
        spec.put("name", c.getDisplayId() + " " + c.getName());
        spec.put("chronicleId", c.getId());
        spec.put("displayId", c.getDisplayId());
        spec.put("format", format);
        spec.put("layers", layerIds);
        spec.put("from", from == null ? null : from.toString());
        spec.put("to", to == null ? null : to.toString());
        spec.put("tlp", tlp);
        Map<String, Object> view = chronicles.view(c, objectCaller, true);
        CustomExport export = "json-bundle".equals(format)
                ? bundle(c, view, layerIds, from, to, allowed, tlp, objectCaller.seesPrivate())
                : file("gpkg".equals(format) ? ExportFormat.GEOPACKAGE : ExportFormat.GEOPARQUET, c, layerIds, from,
                        to, allowed, tlp);
        return exports.submitCustom(format, spec, decision, caller, export);
    }

    private List<String> presentSources(String chronicleId, List<String> layerIds) {
        if (!storage.leafExists(chronicleId)) {
            return List.of();
        }
        String sql = "SELECT DISTINCT source_id FROM " + ChronicleDdl.TABLE + " WHERE chronicle_id = ?"
                + (layerIds.isEmpty() ? "" : " AND layer_id = ANY(?)") + " ORDER BY source_id";
        return layerIds.isEmpty() ? jdbc.queryForList(sql, String.class, chronicleId)
                : jdbc.queryForList(sql, String.class, chronicleId, layerIds.toArray(String[]::new));
    }

    /** The WHERE clause and arguments of the exported versions. */
    private Object[] where(StringBuilder sql, String chronicleId, List<String> layerIds, Instant from, Instant to,
            List<String> allowed) {
        List<Object> args = new ArrayList<>();
        sql.append(" WHERE v.chronicle_id = ? AND v.source_id = ANY(?)");
        args.add(chronicleId);
        args.add(allowed.toArray(String[]::new));
        if (!layerIds.isEmpty()) {
            sql.append(" AND v.layer_id = ANY(?)");
            args.add(layerIds.toArray(String[]::new));
        }
        if (from != null) {
            sql.append(" AND v.observed_at >= ?");
            args.add(Timestamp.from(from));
        }
        if (to != null) {
            sql.append(" AND v.observed_at < ?");
            args.add(Timestamp.from(to));
        }
        sql.append(" ORDER BY v.layer_id, v.seq");
        return args.toArray();
    }

    private CustomExport file(ExportFormat format, ChronicleEntity c, List<String> layerIds, Instant from, Instant to,
            List<String> allowed, String tlp) {
        return new CustomExport() {
            @Override
            public String mediaType() {
                return format.mediaType();
            }

            @Override
            public String extension() {
                return format.extension();
            }

            @Override
            public Result write(Path file, BooleanSupplier canceled) throws IOException {
                Map<String, Long> bySource = new HashMap<>();
                long[] max = {0};
                try (ExportWriter writer = ExportWriterFactory.create(format)) {
                    Map<String, String> metadata = new LinkedHashMap<>();
                    metadata.put("name", c.getDisplayId() + " " + c.getName());
                    metadata.put("tlp", "TLP:" + tlp);
                    metadata.put("chronicleId", c.getId());
                    writer.open(file, ExportSchema.standard(List.of("layer_id"), true), metadata);
                    if (!allowed.isEmpty() && storage.leafExists(c.getId())) {
                        StringBuilder sql = new StringBuilder("SELECT " + ExportRows.versionColumns("v")
                                + ", v.layer_id FROM " + ChronicleDdl.TABLE + " v");
                        Object[] args = where(sql, c.getId(), layerIds, from, to, allowed);
                        jdbc.query(sql.toString(), rs -> {
                            if (canceled.getAsBoolean()) {
                                throw new IllegalStateException("canceled");
                            }
                            Map<String, Object> row = ExportRows.map(rs);
                            row.put("layer_id", rs.getString("layer_id"));
                            bySource.merge(String.valueOf(row.get("source_id")), 1L, Long::sum);
                            max[0] = Math.max(max[0], rs.getLong("seq"));
                            try {
                                writer.write(row);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        }, args);
                    }
                    long rows = writer.rows();
                    return new Result(rows, bySource, max[0] == 0 ? null : max[0]);
                } catch (Exception e) {
                    if (e instanceof IOException io) {
                        throw io;
                    }
                    if (e instanceof RuntimeException r) {
                        throw r;
                    }
                    throw new IOException(e);
                }
            }
        };
    }

    private CustomExport bundle(ChronicleEntity c, Map<String, Object> view, List<String> layerIds, Instant from,
            Instant to, List<String> allowed, String tlp, boolean seesPrivate) {
        return new CustomExport() {
            @Override
            public String mediaType() {
                return "application/json";
            }

            @Override
            public String extension() {
                return "json";
            }

            @Override
            public Result write(Path file, BooleanSupplier canceled) throws IOException {
                Map<String, Long> bySource = new HashMap<>();
                long[] rows = {0};
                long[] max = {0};
                try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file),
                        StandardCharsets.UTF_8))) {
                    out.write("{\"tlp\":" + json.writeValueAsString("TLP:" + tlp));
                    out.write(",\"format\":\"foresignal-chronicle-bundle\",\"version\":\"1.0.0\"");
                    out.write(",\"chronicle\":" + json.writeValueAsString(view));
                    out.write(",\"thread\":[");
                    boolean[] first = {true};
                    jdbc.query("SELECT id, type, at, seq, layer_id, title, summary, ref, snapshot::text AS snapshot,"
                            + " gap::text AS gap, backfilled FROM chronicle_event WHERE chronicle_id = ?"
                            + (seesPrivate ? "" : " AND NOT private") + " ORDER BY id", rs -> {
                                Map<String, Object> item = new LinkedHashMap<>();
                                item.put("id", rs.getLong("id"));
                                item.put("type", rs.getString("type"));
                                item.put("at", rs.getTimestamp("at").toInstant().toString());
                                item.put("seq", rs.getObject("seq"));
                                item.put("layerId", rs.getString("layer_id"));
                                item.put("title", rs.getString("title"));
                                item.put("summary", rs.getString("summary"));
                                item.put("ref", rs.getString("ref"));
                                item.put("snapshot", parse(rs.getString("snapshot")));
                                item.put("gap", parse(rs.getString("gap")));
                                item.put("backfilled", rs.getBoolean("backfilled"));
                                append(out, first, item);
                            }, c.getId());
                    out.write("],\"versions\":[");
                    first[0] = true;
                    if (!allowed.isEmpty() && storage.leafExists(c.getId())) {
                        StringBuilder sql = new StringBuilder("SELECT v.layer_id, v.source_id, v.external_id, v.kind,"
                                + " v.seq, v.version, v.observed_at, v.event_time, v.valid_until, v.ingested_at,"
                                + " v.persist_reason, v.tombstone, v.backfilled, ST_AsGeoJSON(v.geom) AS geojson,"
                                + " v.payload::text AS payload FROM " + ChronicleDdl.TABLE + " v");
                        Object[] args = where(sql, c.getId(), layerIds, from, to, allowed);
                        jdbc.query(sql.toString(), rs -> {
                            if (canceled.getAsBoolean()) {
                                throw new IllegalStateException("canceled");
                            }
                            Map<String, Object> v = new LinkedHashMap<>();
                            v.put("layerId", rs.getString("layer_id"));
                            v.put("sourceId", rs.getString("source_id"));
                            v.put("externalId", rs.getString("external_id"));
                            v.put("kind", rs.getString("kind"));
                            v.put("seq", rs.getLong("seq"));
                            v.put("version", rs.getInt("version"));
                            v.put("observedAt", ts(rs.getTimestamp("observed_at")));
                            v.put("eventTime", ts(rs.getTimestamp("event_time")));
                            v.put("validUntil", ts(rs.getTimestamp("valid_until")));
                            v.put("ingestedAt", ts(rs.getTimestamp("ingested_at")));
                            v.put("persistReason", rs.getShort("persist_reason"));
                            v.put("tombstone", rs.getBoolean("tombstone"));
                            v.put("backfilled", rs.getBoolean("backfilled"));
                            v.put("geometry", rs.getString("geojson") == null ? null : parse(rs.getString("geojson")));
                            v.put("payload", parse(rs.getString("payload")));
                            bySource.merge(rs.getString("source_id"), 1L, Long::sum);
                            max[0] = Math.max(max[0], rs.getLong("seq"));
                            rows[0]++;
                            append(out, first, v);
                        }, args);
                    }
                    out.write("],\"seriesPoints\":");
                    out.write(json.writeValueAsString(jdbc.queryForList("SELECT layer_id, ref, period,"
                            + " period_start::text AS period_start, period_end::text AS period_end, value, unit, status,"
                            + " known_at::text AS known_at, backfilled FROM chronicle_series_point WHERE chronicle_id = ?"
                            + " ORDER BY ref, period_start", c.getId())));
                    out.write(",\"rollups\":");
                    out.write(json.writeValueAsString(jdbc.queryForList("SELECT layer_id, region,"
                            + " hour_start::text AS hour_start, versions, backfilled FROM chronicle_rollup WHERE"
                            + " chronicle_id = ? ORDER BY layer_id, hour_start, region", c.getId())));
                    out.write("}\n");
                }
                return new Result(rows[0], bySource, max[0] == 0 ? null : max[0]);
            }
        };
    }

    private void append(BufferedWriter out, boolean[] first, Map<String, Object> value) {
        try {
            if (!first[0]) {
                out.write(',');
            }
            first[0] = false;
            out.write(json.writeValueAsString(value));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Map<String, Object> parse(String text) {
        if (text == null) {
            return null;
        }
        try {
            return json.readValue(text, MAP);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String ts(Timestamp t) {
        return t == null ? null : t.toInstant().toString();
    }
}
