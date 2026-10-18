package ai.foresignal.chronicle;

import ai.foresignal.export.ExportManifests;
import ai.foresignal.export.ExportStore;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Archives a stopped chronicle to a 111 cold-archive package and restores it (prompt 131). The package lives under the
 * archive root of the 111 {@link ExportStore} ({@code {archive}/chronicles/{id}/}): gzip JSON Lines of the captured
 * versions (every column, the geometry as hex WKB), the series points and the rollups, and a {@code manifest.json}
 * with each file's SHA-256 and row count. Archiving verifies the files, then drops the leaf and deletes the series
 * points and rollups; the frame pins stay, so the referenced raster frames survive for a restore. Restoring re-creates
 * the leaf from the package after checking the hashes; the chronicle returns as {@code stopped}. JSON Lines rather
 * than GeoParquet, because the package must be read back losslessly and the runtime has no Parquet reader.
 */
@Component
public class ChronicleArchiver {

    private static final Logger log = LoggerFactory.getLogger(ChronicleArchiver.class);

    /** The package format name. */
    public static final String FORMAT = "foresignal-chronicle-archive";

    /** The package format version. */
    public static final String VERSION = "1.0.0";

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final ChronicleRepository chronicles;
    private final ChronicleStorage storage;
    private final ChronicleThreadService thread;
    private final ChronicleCaptureService capture;
    private final ExportStore store;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final Clock clock;
    private final TransactionTemplate tx;

    /** Creates the archiver. */
    public ChronicleArchiver(ChronicleRepository chronicles, ChronicleStorage storage, ChronicleThreadService thread,
            ChronicleCaptureService capture, ExportStore store, JdbcTemplate jdbc, JsonMapper json, Clock clock,
            PlatformTransactionManager transactions) {
        this.chronicles = chronicles;
        this.storage = storage;
        this.thread = thread;
        this.capture = capture;
        this.store = store;
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
        this.tx = new TransactionTemplate(transactions);
    }

    /** The package directory of a chronicle. */
    public Path dir(String chronicleId) {
        return store.archiveRoot().resolve("chronicles").resolve(ChronicleDdl.leaf(chronicleId)
                .substring(ChronicleDdl.LEAF_PREFIX.length()));
    }

    /** Deletes a chronicle's package (on deletion). */
    public void deletePackage(String chronicleId) {
        ExportStore.deleteTree(dir(chronicleId));
    }

    // ------------------------------------------------------------------------- archive

    /**
     * Archives a stopped chronicle whose archive state is {@code pending}: writes and verifies the package, drops the
     * leaf, deletes series points and rollups, and moves it to {@code archived}. A failure leaves it {@code stopped}
     * with {@code archive.state = failed}.
     */
    public void archive(String chronicleId) {
        ChronicleEntity c = chronicles.findById(chronicleId).orElse(null);
        if (c == null || !ChronicleVocabulary.STOPPED.equals(c.getStatus())) {
            return;
        }
        try {
            Path dir = dir(chronicleId);
            Files.createDirectories(dir);
            List<Map<String, Object>> files = new ArrayList<>();
            files.add(write(dir.resolve("versions.jsonl.gz"), versionsSql(), chronicleId, this::versionRow));
            files.add(write(dir.resolve("series.jsonl.gz"), "SELECT layer_id, ref, period, period_start, period_end,"
                    + " value, unit, status, point_version, known_at, coverage_ok, seq, backfilled FROM"
                    + " chronicle_series_point WHERE chronicle_id = ? ORDER BY ref, period_start", chronicleId,
                    this::plainRow));
            files.add(write(dir.resolve("rollups.jsonl.gz"), "SELECT layer_id, region, hour_start, versions, backfilled"
                    + " FROM chronicle_rollup WHERE chronicle_id = ? ORDER BY layer_id, region, hour_start",
                    chronicleId, this::plainRow));
            long bytes = 0;
            for (Map<String, Object> f : files) {
                bytes += ((Number) f.get("bytes")).longValue();
            }
            Map<String, Object> manifest = new LinkedHashMap<>();
            manifest.put("format", FORMAT);
            manifest.put("version", VERSION);
            manifest.put("chronicleId", chronicleId);
            manifest.put("displayId", c.getDisplayId());
            manifest.put("archivedAt", clock.instant().toString());
            manifest.put("files", files);
            Files.writeString(dir.resolve("manifest.json"), json.writeValueAsString(manifest));
            verify(dir);
            long archived = bytes;
            capture.exclusive(() -> {
                tx.executeWithoutResult(s -> {
                    ChronicleEntity ch = chronicles.findById(chronicleId).orElseThrow();
                    storage.dropLeaf(chronicleId);
                    jdbc.update("DELETE FROM chronicle_series_point WHERE chronicle_id = ?", chronicleId);
                    jdbc.update("DELETE FROM chronicle_rollup WHERE chronicle_id = ?", chronicleId);
                    Map<String, Object> state = new LinkedHashMap<>();
                    state.put("state", "archived");
                    state.put("exportId", null);
                    state.put("archivedAt", clock.instant().toString());
                    state.put("bytes", archived);
                    state.put("error", null);
                    ch.setArchive(state);
                    ch.setLeafCreated(false);
                    String previous = ch.getStatus();
                    ch.setStatus(ChronicleVocabulary.ARCHIVED);
                    ch.touch(clock.instant());
                    chronicles.save(ch);
                    thread.statusChange(ch, previous, "archived to a cold package", ch.getBytesUsed());
                });
                return null;
            });
        } catch (IOException | RuntimeException e) {
            log.warn("Archiving chronicle {} failed: {}", chronicleId, e.toString());
            fail(chronicleId, e);
        }
    }

    private String versionsSql() {
        return "SELECT layer_id, seq, ingested_at, observed_at, received_at, event_time, valid_until,"
                + " valid_until_explicit, prev_seq, batch_id, version, history_tier, persist_reason, qa_flags, tombstone,"
                + " source_id, external_id, kind, encode(ST_AsBinary(geom), 'hex') AS geom_hex, payload::text AS payload,"
                + " quality::text AS quality, observer::text AS observer, valid_from, backfilled, bucket_start"
                + " FROM " + ChronicleDdl.TABLE + " WHERE chronicle_id = ? ORDER BY layer_id, seq";
    }

    /** A row mapper to a JSON-ready map. */
    @FunctionalInterface
    interface RowMap {

        /** Maps the current row. */
        Map<String, Object> map(java.sql.ResultSet rs) throws java.sql.SQLException;
    }

    private Map<String, Object> versionRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        Map<String, Object> out = plainRow(rs);
        return out;
    }

    private Map<String, Object> plainRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        Map<String, Object> out = new LinkedHashMap<>();
        java.sql.ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            Object v = rs.getObject(i);
            if (v instanceof java.sql.Timestamp t) {
                v = t.toInstant().toString();
            } else if (v instanceof java.time.OffsetDateTime o) {
                v = o.toInstant().toString();
            }
            out.put(meta.getColumnLabel(i), v);
        }
        return out;
    }

    private Map<String, Object> write(Path file, String sql, String chronicleId, RowMap mapper) throws IOException {
        long[] rows = {0};
        boolean leafQuery = sql.contains(ChronicleDdl.TABLE + " ");
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                new GZIPOutputStream(Files.newOutputStream(file)), StandardCharsets.UTF_8))) {
            if (!leafQuery || storage.leafExists(chronicleId)) {
                jdbc.query(sql, rs -> {
                    try {
                        out.write(json.writeValueAsString(mapper.map(rs)));
                        out.write('\n');
                        rows[0]++;
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }, chronicleId);
            }
        }
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("name", file.getFileName().toString());
        entry.put("rows", rows[0]);
        entry.put("bytes", Files.size(file));
        entry.put("sha256", ExportManifests.sha256(file));
        return entry;
    }

    /** Checks every file of a package against its manifest. */
    @SuppressWarnings("unchecked")
    Map<String, Object> verify(Path dir) throws IOException {
        Map<String, Object> manifest = json.readValue(Files.readString(dir.resolve("manifest.json")), MAP);
        if (!FORMAT.equals(manifest.get("format"))) {
            throw new IOException("not a chronicle archive package");
        }
        for (Object o : (List<Object>) manifest.get("files")) {
            Map<String, Object> f = (Map<String, Object>) o;
            Path file = dir.resolve(String.valueOf(f.get("name")));
            if (!Files.isRegularFile(file) || !ExportManifests.sha256(file).equals(f.get("sha256"))) {
                throw new IOException("package file " + f.get("name") + " is missing or does not match its hash");
            }
        }
        return manifest;
    }

    private void fail(String chronicleId, Exception e) {
        try {
            tx.executeWithoutResult(s -> chronicles.findById(chronicleId).ifPresent(ch -> {
                Map<String, Object> state = new LinkedHashMap<>(ch.getArchive() == null ? Map.of() : ch.getArchive());
                state.put("state", "failed");
                state.put("error", e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
                ch.setArchive(state);
                ch.touch(clock.instant());
                chronicles.save(ch);
            }));
        } catch (RuntimeException ex) {
            log.warn("Could not record the archive failure of chronicle {}: {}", chronicleId, ex.toString());
        }
    }

    // ------------------------------------------------------------------------- restore

    /**
     * Restores an archived chronicle whose archive state is {@code restoring}: verifies the package, re-creates the
     * leaf, the series points and the rollups, and moves it to {@code stopped}. A failure leaves it {@code archived}
     * with {@code archive.state = failed}.
     */
    public void restore(String chronicleId) {
        ChronicleEntity c = chronicles.findById(chronicleId).orElse(null);
        if (c == null || !ChronicleVocabulary.ARCHIVED.equals(c.getStatus())) {
            return;
        }
        try {
            Path dir = dir(chronicleId);
            verify(dir);
            capture.exclusive(() -> {
                tx.executeWithoutResult(s -> {
                    storage.createLeaf(chronicleId);
                    try {
                        restoreVersions(chronicleId, dir.resolve("versions.jsonl.gz"));
                        restoreSeries(chronicleId, dir.resolve("series.jsonl.gz"));
                        restoreRollups(chronicleId, dir.resolve("rollups.jsonl.gz"));
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                    ChronicleEntity ch = chronicles.findById(chronicleId).orElseThrow();
                    Map<String, Object> state = new LinkedHashMap<>(ch.getArchive() == null ? Map.of()
                            : ch.getArchive());
                    state.put("state", "archived");
                    state.put("error", null);
                    ch.setArchive(state);
                    ch.setLeafCreated(true);
                    String previous = ch.getStatus();
                    ch.setStatus(ChronicleVocabulary.STOPPED);
                    ch.touch(clock.instant());
                    chronicles.save(ch);
                    thread.statusChange(ch, previous, "restored from its cold package", ch.getBytesUsed());
                });
                return null;
            });
        } catch (IOException | RuntimeException e) {
            log.warn("Restoring chronicle {} failed: {}", chronicleId, e.toString());
            fail(chronicleId, e);
        }
    }

    private void restoreVersions(String chronicleId, Path file) throws IOException {
        List<ChronicleStorage.VersionWrite> batch = new ArrayList<>();
        Instant now = clock.instant();
        for (Map<String, Object> m : lines(file)) {
            byte[] wkb = m.get("geom_hex") == null ? null : HexFormat.of().parseHex(String.valueOf(m.get("geom_hex")));
            CapturedRow row = new CapturedRow(lng(m.get("seq")), inst(m.get("ingested_at")), inst(m.get("observed_at")),
                    inst(m.get("received_at")), inst(m.get("event_time")), inst(m.get("valid_until")),
                    inst(m.get("valid_until_explicit")), lngOrNull(m.get("prev_seq")), lngOrNull(m.get("batch_id")),
                    (int) lng(m.get("version")), (short) lng(m.get("history_tier")), (short) lng(m.get("persist_reason")),
                    (short) lng(m.get("qa_flags")), Boolean.TRUE.equals(m.get("tombstone")),
                    String.valueOf(m.get("source_id")), String.valueOf(m.get("external_id")),
                    String.valueOf(m.get("kind")), wkb, str(m.get("payload")), str(m.get("quality")),
                    str(m.get("observer")), inst(m.get("valid_from")), null);
            batch.add(new ChronicleStorage.VersionWrite(String.valueOf(m.get("layer_id")), row,
                    Boolean.TRUE.equals(m.get("backfilled")), inst(m.get("bucket_start"))));
            if (batch.size() >= 1_000) {
                storage.insertVersions(chronicleId, batch, now);
                batch.clear();
            }
        }
        storage.insertVersions(chronicleId, batch, now);
    }

    private void restoreSeries(String chronicleId, Path file) throws IOException {
        Instant now = clock.instant();
        for (Map<String, Object> m : lines(file)) {
            storage.upsertSeriesPoint(chronicleId, new ChronicleStorage.SeriesPointWrite(String.valueOf(m.get("layer_id")),
                    String.valueOf(m.get("ref")), str(m.get("period")), inst(m.get("period_start")),
                    inst(m.get("period_end")), m.get("value") instanceof Number n ? n.doubleValue() : null,
                    str(m.get("unit")), str(m.get("status")),
                    m.get("point_version") instanceof Number n ? n.intValue() : null, inst(m.get("known_at")),
                    m.get("coverage_ok") instanceof Boolean b ? b : null, lngOrNull(m.get("seq")),
                    Boolean.TRUE.equals(m.get("backfilled"))), now);
        }
    }

    private void restoreRollups(String chronicleId, Path file) throws IOException {
        for (Map<String, Object> m : lines(file)) {
            storage.upsertRollup(chronicleId, String.valueOf(m.get("layer_id")), String.valueOf(m.get("region")),
                    inst(m.get("hour_start")), lng(m.get("versions")), Boolean.TRUE.equals(m.get("backfilled")));
        }
    }

    private List<Map<String, Object>> lines(Path file) throws IOException {
        List<Map<String, Object>> out = new ArrayList<>();
        if (!Files.isRegularFile(file)) {
            return out;
        }
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(file)),
                StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (!line.isBlank()) {
                    out.add(json.readValue(line, MAP));
                }
            }
        }
        return out;
    }

    private static long lng(Object v) {
        return v instanceof Number n ? n.longValue() : v == null ? 0 : Long.parseLong(v.toString());
    }

    private static Long lngOrNull(Object v) {
        return v == null ? null : lng(v);
    }

    private static Instant inst(Object v) {
        return v == null ? null : Instant.parse(v.toString());
    }

    private static String str(Object v) {
        return v == null ? null : v.toString();
    }
}
